package com.example.boss;

import android.content.Context;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.GestureDetector;

// Android inherent GestureDetectorCompat and ScaleGestureDetector have some glitches
public class MyGestureDetector {
  private MyOnGestureListener listener;

  // We can be in one of these 3 states
  private static final int NONE = 0;
  private static final int MOVE = 1;
  private static final int SCALE = 2;
  private int mode = NONE;

  // Remember some things for scaling
  private PointF lastPoint = new PointF();
  private PointF midPoint = new PointF();
  private float oldDist = 1f;

  public static interface MyOnGestureListener extends
      GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener,
      ScaleGestureDetector.OnScaleGestureListener {
    public abstract boolean onScroll(float tx, float ty); // translate X and Y

    public abstract boolean onScale(float scale, float px, float py);
  }

  public MyGestureDetector(Context context, MyOnGestureListener listener) {
    this.listener = listener;
  }

  public boolean onTouchEvent(MotionEvent event) {
    final int action = event.getAction();
    switch (action & MotionEvent.ACTION_MASK) {
    case MotionEvent.ACTION_DOWN:
      lastPoint.set(event.getX(), event.getY());
      mode = MOVE;
      return listener.onDown(event);
    case MotionEvent.ACTION_POINTER_DOWN:
      oldDist = spacing(event);
      midPoint(midPoint, event);
      mode = SCALE;
      return true;
    case MotionEvent.ACTION_UP:
    case MotionEvent.ACTION_POINTER_UP:
    case MotionEvent.ACTION_CANCEL:
      mode = NONE;
      return true;
    case MotionEvent.ACTION_MOVE:
      if (mode == MOVE) {
        final float translateX = event.getX() - lastPoint.x;
        final float translateY = event.getY() - lastPoint.y;
        lastPoint.set(event.getX(), event.getY());
        return listener.onScroll(translateX, translateY);
      } else if (mode == SCALE) {
        final float newDist = spacing(event);
        final float scale = newDist / oldDist;
        oldDist = newDist;
        return listener.onScale(scale, midPoint.x, midPoint.y);
      }
      return true;
    default:
      return false;
    }
  }

  /** Determine the space between the first two fingers */
  private float spacing(MotionEvent event) {
    float x = event.getX(0) - event.getX(1);
    float y = event.getY(0) - event.getY(1);
    return (float) Math.sqrt(x * x + y * y);
  }

  /** Calculate the mid point of the first two fingers */
  private void midPoint(PointF point, MotionEvent event) {
    float x = event.getX(0) + event.getX(1);
    float y = event.getY(0) + event.getY(1);
    point.set(x / 2, y / 2);
  }
}

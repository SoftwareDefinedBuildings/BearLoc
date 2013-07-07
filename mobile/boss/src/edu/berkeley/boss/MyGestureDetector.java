package edu.berkeley.boss;

import android.content.Context;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.GestureDetector;

// Android inherent GestureDetectorCompat and ScaleGestureDetector have some glitches
public class MyGestureDetector {
  private MyOnGestureListener mListener;

  // We can be in one of these 3 states
  private static final int NONE = 0;
  private static final int MOVE = 1;
  private static final int SCALE = 2;
  private int mode = NONE;

  // Remember some things for scaling
  private PointF mLastPoint = new PointF();
  private PointF mMidPoint = new PointF();
  private float mOldDist = 1f;

  private GestureDetector mGestureDetector;

  public static interface MyOnGestureListener extends
      GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener,
      ScaleGestureDetector.OnScaleGestureListener {
    public abstract boolean onScroll(float tx, float ty); // translate X and Y

    public abstract boolean onScale(float scale, float px, float py);
  }

  public MyGestureDetector(Context context, MyOnGestureListener listener) {
    mListener = listener;

    mGestureDetector = new GestureDetector(context, mListener);
  }

  public boolean onTouchEvent(MotionEvent event) {

    mGestureDetector.onTouchEvent(event);

    final int action = event.getAction();
    switch (action & MotionEvent.ACTION_MASK) {
    case MotionEvent.ACTION_DOWN:
      mLastPoint.set(event.getX(), event.getY());
      mode = MOVE;
      return mListener.onDown(event);
    case MotionEvent.ACTION_POINTER_DOWN:
      mOldDist = spacing(event);
      mMidPoint(mMidPoint, event);
      mode = SCALE;
      return true;
    case MotionEvent.ACTION_UP:
    case MotionEvent.ACTION_POINTER_UP:
    case MotionEvent.ACTION_CANCEL:
      mode = NONE;
      return true;
    case MotionEvent.ACTION_MOVE:
      if (mode == MOVE) {
        final float translateX = event.getX() - mLastPoint.x;
        final float translateY = event.getY() - mLastPoint.y;
        mLastPoint.set(event.getX(), event.getY());
        return mListener.onScroll(translateX, translateY);
      } else if (mode == SCALE) {
        final float newDist = spacing(event);
        final float scale = newDist / mOldDist;
        mOldDist = newDist;
        return mListener.onScale(scale, mMidPoint.x, mMidPoint.y);
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
  private void mMidPoint(PointF point, MotionEvent event) {
    float x = event.getX(0) + event.getX(1);
    float y = event.getY(0) + event.getY(1);
    point.set(x / 2, y / 2);
  }
}

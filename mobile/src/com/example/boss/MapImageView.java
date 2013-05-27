package com.example.boss;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MapImageView extends ImageView {

  // These matrices will be used to move and zoom image
  private Matrix matrix = new Matrix();
  private Matrix savedMatrix = new Matrix();

  // We can be in one of these 3 states
  private static final int NONE = 0;
  private static final int DRAG = 1;
  private static final int ZOOM = 2;
  private int mode = NONE;

  // Remember some things for zooming
  private PointF start = new PointF();
  private PointF mid = new PointF();
  private float oldDist = 1f;

  public MapImageView(Context context) {
    this(context, null, 0);
  }

  public MapImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MapImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    final int action = MotionEventCompat.getActionMasked(event);

    switch (action) {
    case MotionEvent.ACTION_DOWN:
      savedMatrix.set(matrix);
      start.set(event.getX(), event.getY());
      mode = DRAG;
      break;
    case MotionEvent.ACTION_POINTER_DOWN:
      oldDist = spacing(event);
      if (oldDist > 10f) {
        savedMatrix.set(matrix);
        midPoint(mid, event);
        mode = ZOOM;
      }
      break;
    case MotionEvent.ACTION_UP:
    case MotionEvent.ACTION_POINTER_UP:
      mode = NONE;
      break;
    case MotionEvent.ACTION_MOVE:
      if (mode == DRAG) {
        matrix.set(savedMatrix);
        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
      } else if (mode == ZOOM) {
        float newDist = spacing(event);
        if (newDist > 10f) {
          matrix.set(savedMatrix);
          float scale = newDist / oldDist;
          matrix.postScale(scale, scale, mid.x, mid.y);
        }
      }
      break;
    default:
      return super.onTouchEvent(event);
    }

    setImageMatrix(matrix);
    return true; // indicate event was handled
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

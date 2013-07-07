package edu.berkeley.boss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

public class MapImageView extends ImageView {

  private Bitmap mMap;
  private RectF mMapDestRect;
  private Paint mMapPaint;

  private Matrix matrix;

  private MyGestureDetector gestureDetector;

  public MapImageView(Context context) {
    this(context, null, 0);
  }

  public MapImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MapImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    setFocusable(true);

    mMapDestRect = new RectF();
    mMapPaint = new Paint();
    mMapPaint.setFilterBitmap(true);

    gestureDetector = new MyGestureDetector(context, new GestureListener());
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    boolean handled = gestureDetector.onTouchEvent(event);
    return handled;
  }

  // official documentation doesn't mention the deprecation
  @SuppressWarnings("deprecation")
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.save();
    if (matrix == null) {
      matrix = new Matrix(canvas.getMatrix());
    } else {
      canvas.setMatrix(matrix);
    }

    if (mMap != null) {
      final float w = getWidth();
      final float h = w * mMap.getHeight() / mMap.getWidth();
      mMapDestRect.set(0, 0, w, h);
      canvas.drawBitmap(mMap, null, mMapDestRect, mMapPaint);
    }
    canvas.restore();
  }

  public void setMap(Bitmap map) {
    mMap = map;

    invalidate();
  }

  private class GestureListener implements
      MyGestureDetector.MyOnGestureListener {

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      float scale = 1.5f;
      matrix.postScale(scale, scale, e.getX(), e.getY());
      invalidate();
      return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
      return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
        float velocityY) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
      // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
        float distanceY) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
      // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
      // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(float tx, float ty) {
      matrix.postTranslate(tx, ty);
      invalidate();
      return true;
    }

    @Override
    public boolean onScale(float scale, float px, float py) {
      matrix.postScale(scale, scale, px, py);
      invalidate();
      return true;
    }
  }
}

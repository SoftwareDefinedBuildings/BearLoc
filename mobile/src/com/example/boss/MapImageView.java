package com.example.boss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

public class MapImageView extends ImageView {

  private Bitmap map;
  private Rect mapDestRect;
  private Paint mapPaint;
  private boolean newMap;

  private float posX;
  private float posY;

  private float lastTouchX;
  private float lastTouchY;

  private float scaleFactor = 1.0f;
  private float scaleMidX;
  private float scaleMidY;

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

    mapDestRect = new Rect();
    mapPaint = new Paint();
    newMap = false;

    mapPaint.setFilterBitmap(true);

    gestureDetector = new MyGestureDetector(context, new GestureListener());
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    boolean handled = gestureDetector.onTouchEvent(event);
    return handled;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.save();
    canvas.scale(scaleFactor, scaleFactor, scaleMidX, scaleMidY);
    canvas.translate(posX, posY);
    if (newMap == true) {
      int w = getWidth();
      int h = w * map.getHeight() / map.getWidth();
      mapDestRect.set(0, 0, w, h);
      canvas.drawBitmap(map, null, mapDestRect, mapPaint);
    }
    canvas.restore();
  }

  public void setMap(Bitmap map) {
    this.map = map;
    newMap = true;
    
    invalidate();
  }

  private class GestureListener implements
      MyGestureDetector.MyOnGestureListener {

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      // TODO Auto-generated method stub
      return false;
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
      lastTouchX = e.getX();
      lastTouchY = e.getY();
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
      final float x2 = e2.getX();
      final float y2 = e2.getY();

      posX += x2 - lastTouchX;
      posY += y2 - lastTouchY;

      lastTouchX = x2;
      lastTouchY = y2;

      invalidate();
      return true;
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
      scaleFactor *= detector.getScaleFactor();

      invalidate();
      return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
      scaleMidX = detector.getFocusX();
      scaleMidY = detector.getFocusY();
      return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
      // TODO Auto-generated method stub

    }

  }
}

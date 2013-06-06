package com.example.boss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

public class MapImageView extends ImageView {

  private Bitmap map;
  private RectF mapDestRect;
  private Paint mapPaint;

  // private float posX;
  // private float posY;
  //
  // private float scaleFactor;
  // private float scalePivotX;
  // private float scalePivotY;

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

    mapDestRect = new RectF();
    mapPaint = new Paint();
    mapPaint.setFilterBitmap(true);

    // posX = 0;
    // posY = 0;
    // scaleFactor = 1;
    // scalePivotX = 0;
    // scalePivotY = 0;
    matrix = new Matrix();  // identity matrix 

    gestureDetector = new MyGestureDetector(context, new GestureListener());
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    boolean handled = gestureDetector.onTouchEvent(event);
    return handled;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    // Log.d("pos", posX + "," + posY + "," + scaleFactor + "," + scalePivotX
    // + "," + scalePivotY);
    super.onDraw(canvas);

    canvas.save();
    //canvas.translate(0, 0);
    // canvas.scale(scaleFactor, scaleFactor, scalePivotX, scalePivotY);
    canvas.setMatrix(matrix);
    if (map != null) {
      final float w = getWidth();
      final float h = w * map.getHeight() / map.getWidth();
      mapDestRect.set(0, 0, w, h);
      canvas.drawBitmap(map, null, mapDestRect, mapPaint);
    }
    canvas.restore();
  }

  public void setMap(Bitmap map) {
    this.map = map;

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
      // posX += tx;
      // posY += ty;
      matrix.postTranslate(tx, ty);
      invalidate();
      return true;
    }

    @Override
    public boolean onScale(float scale, float px, float py) {
      // scaleFactor *= scale;
      // scalePivotX = px;
      // scalePivotY = py;
      matrix.postScale(scale, scale, px, py);
      invalidate();
      return true;
    }
  }
}

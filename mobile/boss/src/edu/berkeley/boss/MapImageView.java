package edu.berkeley.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

public class MapImageView extends ImageView {

  private Bitmap mMap;
  private RectF mMapDestRect;
  private Paint mMapPaint;

  private Matrix mTransMatrix;

  private ShapeDrawable mTestDrawable;

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

    mTransMatrix = new Matrix();

    List<PointF> vertices = new ArrayList<PointF>();
    vertices.add(new PointF(100, 100));
    vertices.add(new PointF(150, 150));
    vertices.add(new PointF(50, 150));
    mTestDrawable = new ShapeDrawable(new PolygonShape(vertices));
    mTestDrawable.getPaint().setColor(0xff74AC23);
    mTestDrawable.setBounds(0, 0, mTestDrawable.getIntrinsicWidth(),
        mTestDrawable.getIntrinsicHeight());

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

    // canvas.setMatrix has some problem with coordinate system, especially when
    // doing scaling. canvas.concat is doing good. Tested on Android 2.3.4
    canvas.concat(mTransMatrix);

    if (mMap != null) {
      final float w = canvas.getWidth();
      final float h = w * mMap.getHeight() / mMap.getWidth();
      mMapDestRect.set(0, 0, w, h);
      canvas.drawBitmap(mMap, null, mMapDestRect, mMapPaint);
    }

    mTestDrawable.draw(canvas);
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
      mTransMatrix.postScale(scale, scale, e.getX(), e.getY());
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
      PolygonShape shape = (PolygonShape) mTestDrawable.getShape();

      float[] point = new float[] { e.getX(), e.getY() };
      Matrix inverseMatrix = new Matrix();
      mTransMatrix.invert(inverseMatrix);
      inverseMatrix.mapPoints(point);

      if (shape.contains(new PointF(point[0], point[1])) == true) {
        Random r = new Random();
        mTestDrawable.getPaint().setColor(r.nextInt());
      }

      invalidate();
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
      mTransMatrix.postTranslate(tx, ty);
      invalidate();
      return true;
    }

    @Override
    public boolean onScale(float scale, float px, float py) {
      mTransMatrix.postScale(scale, scale, px, py);
      invalidate();
      return true;
    }
  }
}

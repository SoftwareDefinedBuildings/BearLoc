package edu.berkeley.boss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

public class MapImageView extends ImageView {

  private Bitmap mMap;
  private Paint mMapPaint;

  private Map<String, ShapeDrawable> mZones;

  private Matrix mTransMatrix;

  private MyGestureDetector gestureDetector;

  private Random mRandom;

  private OnZoneClickListener mListener;

  public static interface OnZoneClickListener {
    public abstract void onZoneClick(MapImageView parent, ShapeDrawable zone,
        List<String> id);
  }

  public MapImageView(Context context) {
    this(context, null, 0);
  }

  public MapImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MapImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    setFocusable(true);

    mMapPaint = new Paint();
    mMapPaint.setFilterBitmap(true);

    mZones = new HashMap<String, ShapeDrawable>();

    mTransMatrix = new Matrix();

    gestureDetector = new MyGestureDetector(context, new GestureListener());

    mRandom = new Random();
  }

  public void setOnZoneClickListener(OnZoneClickListener listener) {
    mListener = listener;
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
    // scaling. canvas.concat is doing good. Tested on Android 2.3.4
    canvas.concat(mTransMatrix);

    if (mMap != null) {
      canvas.drawBitmap(mMap, 0, 0, mMapPaint);
    }

    for (Map.Entry<String, ShapeDrawable> entry : mZones.entrySet()) {
      final ShapeDrawable zone = entry.getValue();
      zone.draw(canvas);
    }

    canvas.restore();
  }

  // TODO maybe making this view an AdapterView, and use Adapter to manager the
  // map and zones. Both map and zone should be a view.
  public void setMap(final Bitmap map) {
    mMap = map;

    invalidate();
  }

  public void addZone(final String id, final List<PointF> vertices) {
    final ShapeDrawable newZone = new ShapeDrawable(new PolygonShape(vertices));
    newZone.getPaint().setColor(mRandom.nextInt(0x010000000) + 0x7f000000);
    newZone.setBounds(0, 0, newZone.getIntrinsicWidth(),
        newZone.getIntrinsicHeight());

    mZones.put(id, newZone);

    invalidate();
  }

  public void removeZone(final String id) {
    mZones.remove(id);

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
      final float[] screenPointArr = new float[] { e.getX(), e.getY() };
      final Matrix inverseMatrix = new Matrix();
      mTransMatrix.invert(inverseMatrix);
      inverseMatrix.mapPoints(screenPointArr);
      final PointF originP = new PointF(screenPointArr[0], screenPointArr[1]);

      for (Map.Entry<String, ShapeDrawable> entry : mZones.entrySet()) {
        final ShapeDrawable zone = entry.getValue();
        final PolygonShape shape = (PolygonShape) zone.getShape();
        if (shape.contains(originP) == true) {
          zone.getPaint().setColor(mRandom.nextInt(0x010000000) + 0x7f000000);

          // TODO call CallBackFunc with id
        }
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

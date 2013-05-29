package com.example.boss;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.GestureDetector;

public class MyGestureDetector {
  private LocalOnGestureListener localListener;
  private MyOnGestureListener listener;

  private GestureDetectorCompat gestureDetector;
  private ScaleGestureDetector scaleGestureDetector;

  public static interface MyOnGestureListener extends
      GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener,
      ScaleGestureDetector.OnScaleGestureListener {
  }

  public MyGestureDetector(Context context, MyOnGestureListener listener) {
    this.listener = listener;

    localListener = new LocalOnGestureListener();

    gestureDetector = new GestureDetectorCompat(context, localListener);
    scaleGestureDetector = new ScaleGestureDetector(context, localListener);
  }

  public boolean onTouchEvent(MotionEvent event) {
    boolean handled;
    handled = gestureDetector.onTouchEvent(event);
    handled = scaleGestureDetector.onTouchEvent(event) || handled;

    return handled;
  }

  public class LocalOnGestureListener implements MyOnGestureListener {

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      return listener.onDoubleTap(e);
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
      return listener.onDoubleTapEvent(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      return listener.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
      return listener.onDown(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
        float velocityY) {
      return listener.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public void onLongPress(MotionEvent e) {
      listener.onLongPress(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
        float distanceY) {
      // Ensure the entire scroll was done by the same finger
      if (e1.getPointerId(0) == e2.getPointerId(0)) {
        return listener.onScroll(e1, e2, distanceX, distanceY);
      } else {
        return true;
      }
    }

    @Override
    public void onShowPress(MotionEvent e) {
      listener.onShowPress(e);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      return listener.onSingleTapUp(e);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      return listener.onScale(detector);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
      return listener.onScaleBegin(detector);
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
      listener.onScaleEnd(detector);
    }

  }
}

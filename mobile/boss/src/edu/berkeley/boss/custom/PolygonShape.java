package edu.berkeley.boss.custom;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.shapes.Shape;

// Some codes are learned from PathShape
public class PolygonShape extends Shape {
  private List<PointF> mVertices;
  private Path mPath;
  private float mStdWidth;
  private float mStdHeight;

  private float mScaleX = 1; // cached from onResize
  private float mScaleY = 1; // cached from onResize

  public PolygonShape(List<PointF> vertices) {
    mVertices = vertices;

    mPath = new Path();
    mPath.moveTo(mVertices.get(0).x, mVertices.get(0).y);
    for (int i = 1; i < mVertices.size(); i++) {
      mPath.lineTo(mVertices.get(i).x, mVertices.get(i).y);
    }
    mPath.close();

    float minX = mVertices.get(0).x;
    float maxX = mVertices.get(0).x;
    float minY = mVertices.get(0).y;
    float maxY = mVertices.get(0).y;
    for (int i = 1; i < mVertices.size(); i++) {
      if (mVertices.get(i).x < minX) {
        minX = mVertices.get(i).x;
      }
      if (mVertices.get(i).x > maxX) {
        maxX = mVertices.get(i).x;
      }
      if (mVertices.get(i).y < minY) {
        minY = mVertices.get(i).y;
      }
      if (mVertices.get(i).y > maxY) {
        maxY = mVertices.get(i).y;
      }
    }

    mStdWidth = maxX - minX;
    mStdHeight = maxY - minY;
  }

  @Override
  public void draw(Canvas canvas, Paint paint) {
    canvas.save();
    canvas.scale(mScaleX, mScaleY);
    canvas.drawPath(mPath, paint);
    canvas.restore();
  }

  @Override
  protected void onResize(float width, float height) {
    mScaleX = width / mStdWidth;
    mScaleY = height / mStdHeight;
  }

  @Override
  public PolygonShape clone() throws CloneNotSupportedException {
    PolygonShape shape = (PolygonShape) super.clone();
    shape.mVertices = new ArrayList<PointF>(mVertices);
    shape.mPath = new Path(mPath);
    shape.mStdWidth = mStdWidth;
    shape.mStdHeight = mStdWidth;

    return shape;
  }
  
  public Path getPath() {
    return mPath;
  }

  /*
   * Use Ray-casting algorithm to determine whether the point is in this polygon
   * http://rosettacode.org/wiki/Ray-casting_algorithm
   */
  public boolean contains(final PointF point) {
    int count = 0;

    final int size = mVertices.size();
    for (int i = 0; i < size; i++) {
      if (rayIntersectsSegment(point, mVertices.get(i),
          mVertices.get((i + 1) % size)) == true) {
        count = count + 1;
      }
    }

    if (count % 2 == 0) {
      return false;
    } else {
      return true;
    }
  }

  private static boolean rayIntersectsSegment(PointF startP, PointF segP1,
      PointF segP2) {
    
    final float epsilon = 0.001F;

    float segmentTan;
    float pointTan;

    // Ensure segP2.y is larger than segP1.y
    if (segP1.y > segP2.y) {
      final PointF tmpP = segP1;
      segP1 = segP2;
      segP2 = tmpP;
    }

    if (segP1.y == startP.y || segP2.y == startP.y) {
      startP.y = startP.y + epsilon;
    }

    if (startP.y < segP1.y || startP.y > segP2.y) {
      return false;
    } else if (startP.x > Math.max(segP1.x, segP2.x)) {
      return false;
    } else {
      if (startP.x < Math.min(segP1.x, segP2.x)) {
        return true;
      } else {
        if (segP1.x != segP2.x) {
          segmentTan = (segP2.y - segP1.y) / (segP2.x - segP1.x);
        } else {
          segmentTan = Integer.MAX_VALUE;
        }

        if (segP1.x != startP.x) {
          pointTan = (startP.y - segP1.y) / (startP.x - segP1.x);
        } else {
          pointTan = Integer.MAX_VALUE;
        }

        if (pointTan >= segmentTan) {
          return true;
        } else {
          return false;
        }
      }
    }
  }
}
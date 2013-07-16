package edu.berkeley.boss;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.widget.TextView;

public class LocMapActivity extends Activity implements
    DialogInterface.OnCancelListener, BOSSLocClient.LocClientListener,
    MapImageView.OnZoneClickListener {

  private TextView mTextView;
  private MapImageView mMapImageView;

  private BOSSLocClient mLocClient;
  private SynAmbience mSynAmbience;

  private JSONObject mCurLoc;
  private JSONArray mCurSemTarget;
  private JSONObject mCurMetadata;

  private ProgressDialog mProgressDialog;

  private boolean mActive;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.loc_map);

    mTextView = (TextView) findViewById(R.id.loc_text_view);
    mMapImageView = (MapImageView) findViewById(R.id.map_view);

    mLocClient = new BOSSLocClient(this);
    mSynAmbience = new SynAmbience(this);

    Intent intent = getIntent();
    try {
      mCurLoc = new JSONObject(intent.getStringExtra(LocTreeActivity.LOCATION));
      mCurSemTarget = new JSONArray(
          intent.getStringExtra(LocTreeActivity.SEMANTIC_TARGET));
      mCurMetadata = new JSONObject(
          intent.getStringExtra(LocTreeActivity.METADATA));
      
      onLocationChanged();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getResources().getString(
        R.string.progess_message));
    mProgressDialog.setOnCancelListener(this);
  }

  @Override
  protected void onResume() {
    super.onResume();

    mActive = true;

    mLocClient.setOnDataReturnedListener(this);

    mSynAmbience.resume();

    mProgressDialog.show();

    mLocClient.getMap(mCurMetadata);
  }

  private void onLocationChanged() {
    final String locStr = locationString(mCurLoc, mCurSemTarget, 0);
    mTextView.setText(locStr);

    // TODO highlight different paint
  }

  /* Recursively retrieve location string with semantic target */
  private String locationString(final JSONObject loc,
      final JSONArray semTarget, final int semTargetIdx) {
    try {
      if (semTarget.get(semTargetIdx) instanceof String) {
        final String semantic = semTarget.getString(semTargetIdx);

        // get location item
        final Iterator<?> iter = loc.keys();
        while (iter.hasNext()) {
          final String locItemStr = (String) iter.next();

          // Every location item is a String of JSONArray formated as
          // "(semantic, zone)"
          final JSONArray locItem = new JSONArray(locItemStr);
          final String locItemSem = locItem.getString(0);
          if (locItemSem.equals(semantic)) {
            return ":" + semantic;
          }
        }
      } else {
        final String semantic = semTarget.getJSONArray(semTargetIdx).getString(
            0);
        final String zone = semTarget.getJSONArray(semTargetIdx).getString(1);

        final Iterator<?> iter = loc.keys();
        while (iter.hasNext()) {
          final String locItemStr = (String) iter.next();

          // Every location item is a String of JSONArray formated as
          // "(semantic, zone)"
          final JSONArray locItem = new JSONArray(locItemStr);
          final String locItemSem = locItem.getString(0);
          final String locItemZone = locItem.getString(1);
          if (locItemSem.equals(semantic) && locItemZone.equals(zone)) {
            return "/"
                + locItemZone
                + locationString(loc.getJSONObject(locItemStr), semTarget,
                    semTargetIdx + 1);
          }
        }
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }

  @Override
  protected void onPause() {
    // TODO deal with all rotation issues
    super.onPause();

    mActive = false;

    mProgressDialog.dismiss();

    mLocClient.setOnDataReturnedListener(null);

    mSynAmbience.pause();
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    finish();
  }

  @Override
  public void onLocationReturned(JSONObject locInfo) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onReportDone(boolean success) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onMetadataReturned(JSONObject metadata) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onMapReturned(Bitmap bitmap) {
    if (mActive == true) {
      if (bitmap != null) {
        mMapImageView.setMap(bitmap);
      }

      try {
        final String semantic = mCurSemTarget
            .getString(mCurSemTarget.length() - 1);
        final JSONObject zonesJSONObject = mCurMetadata.getJSONObject("child")
            .getJSONObject(semantic);

        final Iterator<?> iter = zonesJSONObject.keys();
        while (iter.hasNext()) {
          final String name = (String) iter.next();
          final JSONObject zoneJSONObject = zonesJSONObject.getJSONObject(name);

          // TODO implement multi-region zone
          final JSONArray zonePoints = zoneJSONObject.getJSONArray("regions")
              .getJSONArray(0);

          List<PointF> vertices = new LinkedList<PointF>();
          for (int i = 0; i < zonePoints.length(); i++) {
            JSONArray pointJSONArray = zonePoints.getJSONArray(i);
            vertices.add(new PointF((float) pointJSONArray.getDouble(0),
                (float) pointJSONArray.getDouble(1)));
          }

          mMapImageView.addZone(name, vertices);
        }
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      mProgressDialog.dismiss();

      onLocationChanged();
    }
  }

  @Override
  public void onZoneClick(MapImageView parent, ShapeDrawable zone,
      List<String> id) {
    // TODO Show selection dialog

  }
}
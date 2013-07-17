package edu.berkeley.boss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

public class LocMapActivity extends Activity implements
    DialogInterface.OnCancelListener, BOSSLocClient.LocClientListener,
    MapImageView.OnZoneClickListener, DialogInterface.OnClickListener {

  private TextView mTextView;
  private MapImageView mMapImageView;

  private BOSSLocClient mLocClient;
  private SynAmbience mSynAmbience;

  private JSONObject mCurLoc;
  private JSONArray mCurSemTarget;
  private JSONObject mCurMetadata;
  private String mCurZone;

  private ProgressDialog mProgressDialog;
  private AlertDialog.Builder mDialogBuilder;
  private AlertDialog mSelectDialog;
  // TODO embed mSelectItems with self-defined Adpater
  private List<String> mSelectItems;
  private int mCheckedIndex;

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

    mDialogBuilder = new AlertDialog.Builder(this);
    mDialogBuilder.setTitle("Choose Zone");
    mDialogBuilder.setOnCancelListener(this);

    mSelectItems = new ArrayList<String>();
    try {
      final String semantic = mCurSemTarget
          .getString(mCurSemTarget.length() - 1);
      final JSONArray zoneJSONArray = mCurMetadata.getJSONObject("child")
          .getJSONObject(semantic).names();
      final int length = zoneJSONArray.length();
      for (int i = 0; i < length; i++) {
        mSelectItems.add(zoneJSONArray.getString(i));
      }
      Collections.sort(mSelectItems);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    mActive = true;

    mLocClient.setOnDataReturnedListener(this);
    mMapImageView.setOnZoneClickListener(this);

    mSynAmbience.resume();

    mProgressDialog.show();

    mLocClient.getMap(mCurMetadata);
  }

  /* Recursively change loc with semantic target and zone */
  private void changeLocation(final JSONObject loc, final JSONArray semTarget,
      final int semTargetIdx, final String newZone) {
    try {
      if (semTarget.get(semTargetIdx) instanceof String) {

        // Remove all semantic location item in this node
        final JSONArray locItems = loc.names();
        final int length = locItems.length();
        for (int i = 0; i < length; i++) {
          loc.remove(locItems.getString(i));
        }

        // add new location item
        final String semantic = semTarget.getString(semTargetIdx);
        final JSONArray newLocItem = new JSONArray();
        newLocItem.put(semantic);
        newLocItem.put(newZone);
        loc.put(newLocItem.toString(), new JSONObject());
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
          if (locItem.getString(0).equals(semantic)
              && locItem.getString(1).equals(zone)) {
            changeLocation(loc.getJSONObject(locItemStr), semTarget,
                semTargetIdx + 1, newZone);
            break;
          }
        }
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void onLocationChanged() {
    final String locStr = locationString(mCurLoc, mCurSemTarget, 0);
    mTextView.setText(locStr);
  }

  private void reportLocation() {
    mProgressDialog.show();

    final JSONObject synAmbiencePack = mSynAmbience.get();
    mSynAmbience.clear(); // clear sensor data that will be reported to server
    mLocClient.reportLocation(synAmbiencePack, mCurLoc);
  }

  // TODO duplicated codes, implement location util
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
          final String locItemZone = locItem.getString(1);
          if (locItemSem.equals(semantic)) {
            mCurZone = locItemZone;
            return ":" + locItemSem + "(" + locItemZone + ")";
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
    mMapImageView.setOnZoneClickListener(null);

    mSynAmbience.pause();
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    if (dialog == mProgressDialog) {
      finish();
    } else if (dialog == mSelectDialog) {

    }
  }

  @Override
  public void onLocationReturned(JSONObject locInfo) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onReportDone(boolean success) {
    mProgressDialog.dismiss();
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

          if (name.equals(mCurZone)) {
            mMapImageView.addZone(name, vertices, true);
          } else {
            mMapImageView.addZone(name, vertices);
          }
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
  public void onZoneClick(MapImageView parent, String id) {
    mCheckedIndex = mSelectItems.indexOf(id);
    CharSequence[] selectItemsArray = mSelectItems
        .toArray(new CharSequence[mSelectItems.size()]);

    mDialogBuilder.setSingleChoiceItems(selectItemsArray, mCheckedIndex, null);
    mDialogBuilder.setPositiveButton(R.string.report, this);

    mSelectDialog = mDialogBuilder.create();

    mSelectDialog.show();
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    // TODO retrieve selected items from mSelections, change current location,
    // and report to server
    switch (which) {
    case DialogInterface.BUTTON_POSITIVE:
      ListView listView = ((AlertDialog) dialog).getListView();
      final String newZone = (String) listView.getAdapter().getItem(
          listView.getCheckedItemPosition());
      changeLocation(mCurLoc, mCurSemTarget, 0, newZone);
      onLocationChanged();

      mMapImageView.setFocusZone(newZone);

      reportLocation();
      break;
    }

  }
}
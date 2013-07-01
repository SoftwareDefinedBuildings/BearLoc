package com.example.boss;

import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.boss.BOSSLocClient.LocClientListener;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class LocActivity extends Activity implements OnItemSelectedListener,
    LocClientListener {

  private final int LOC_ITVL = 2000; // millisecond

  private Spinner mSemSpinner;
  private MapImageView mMapImageView;

  private BOSSLocClient mLocClient;
  private SensorCache mSensorCache;

  private JSONObject mLatestLocInfo;
  private JSONObject latestMetadata;
  private String mCurSemantic;

  private Handler mHandler;
  private Runnable mLocalizationTimeTask;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.loc);

    mSemSpinner = (Spinner) findViewById(R.id.sem_spinner);
    mMapImageView = (MapImageView) findViewById(R.id.map_view);

    mLocClient = new BOSSLocClient(this);
    mSensorCache = new SensorCache(this);

    mHandler = new Handler();
    mLocalizationTimeTask = new Runnable() {
      public void run() {
        final JSONObject sensorDataPack = mSensorCache.getSensorData();
        mLocClient.getLocation(sensorDataPack);
      }
    };

    setAdapters();
    setListeners();
  }

  @Override
  protected void onResume() {
    super.onResume();

    mSensorCache.resume();
    mHandler.postDelayed(mLocalizationTimeTask, LOC_ITVL);
  }

  @Override
  protected void onPause() {
    // TODO deal with all rotation issues
    super.onPause();

    mSensorCache.pause();
    mHandler.removeCallbacks(mLocalizationTimeTask);
  }

  class LocalizationTimeTask extends TimerTask {
    @Override
    public void run() {

    }
  }

  private void setAdapters() {
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.semantics, android.R.layout.simple_spinner_item);
    adapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mSemSpinner.setAdapter(adapter);
  }

  private void setListeners() {
    mSemSpinner.setOnItemSelectedListener(this);
    mLocClient.setOnDataReturnedListener(this);
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    final String newSemantic = parent.getItemAtPosition(pos).toString();
    onSemanticChanged(newSemantic);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // TODO Auto-generated method stub
  }

  private void onSemanticChanged(String newSemantic) {
    if ((mCurSemantic == null) || !(mCurSemantic.equals(newSemantic))) {
      mCurSemantic = newSemantic;
      drawZones(mCurSemantic);
    }
  }

  @Override
  public void onLocationReturned(JSONObject locInfo) {
    if (locInfo != null
        && (mLatestLocInfo == null || !locInfo.toString().equals(
            mLatestLocInfo.toString()))) {
      mLatestLocInfo = locInfo;
      try {
        JSONObject loc = locInfo.getJSONObject("location");
        mLocClient.getMetadata(loc, "floor");
      } catch (JSONException e) {
        // TODO Auto-generated catch block
      }
    }

    mHandler.postDelayed(mLocalizationTimeTask, LOC_ITVL);
  }

  @Override
  public void onMetadataReturned(JSONObject metadata) {
    if (metadata != null
        && (latestMetadata == null || !metadata.toString().equals(
            latestMetadata.toString()))) {
      latestMetadata = metadata;
      mLocClient.getMap(metadata);
    }
  }

  @Override
  public void onMapReturned(Bitmap bitmap) {

    if (bitmap == null) {
      mCurSemantic = null;
    } else {
      mMapImageView.setMap(bitmap);
    }
  }

  private void drawZones(String semantic) {

  }

}

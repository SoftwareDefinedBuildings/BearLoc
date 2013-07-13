package edu.berkeley.boss;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.TextView;
import edu.berkeley.boss.BOSSLocClient.LocClientListener;

public class LocMapActivity extends Activity implements
    DialogInterface.OnCancelListener, LocClientListener {

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

    Intent intent = getIntent();
    try {
      mCurLoc = new JSONObject(intent.getStringExtra(LocTreeActivity.LOCATION));
      mCurSemTarget = new JSONArray(
          intent.getStringExtra(LocTreeActivity.SEMANTIC_TARGET));
      mCurMetadata = new JSONObject(
          intent.getStringExtra(LocTreeActivity.METADATA));
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    mTextView = (TextView) findViewById(R.id.loc_text_view);
    mMapImageView = (MapImageView) findViewById(R.id.map_view);

    mLocClient = new BOSSLocClient(this);
    mSynAmbience = new SynAmbience(this);

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
    // TODO highlight different paint
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

      mProgressDialog.dismiss();

      onLocationChanged();
    }
  }

}
package com.example.boss;

import com.example.boss.BOSSLocClient.LocClientListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class LocActivity extends Activity implements OnItemSelectedListener,
    LocClientListener {

  private Spinner semSpinner;
  private MapImageView mapImageView;

  private String curSemantic;
  private BOSSLocClient locClient;

  private ProgressDialog progressDialog;
  private Toast toast;

  @SuppressLint("ShowToast")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.loc);

    locClient = new BOSSLocClient();

    progressDialog = new ProgressDialog(this);
    progressDialog.setTitle(getResources().getString(R.string.progess_title));
    progressDialog.setMessage(getResources()
        .getString(R.string.progess_message));
    toast = Toast.makeText(this,
        getResources().getString(R.string.fail_download_map),
        Toast.LENGTH_SHORT);

    findViews();
    setAdapters();
    setListeners();
  }

  @Override
  protected void onPause() {
    // TODO deal with all rotation issues
    super.onPause();
    progressDialog.dismiss();
    toast.cancel();
  }

  private void findViews() {
    semSpinner = (Spinner) findViewById(R.id.sem_spinner);
    mapImageView = (MapImageView) findViewById(R.id.map_view);
  }

  private void setAdapters() {
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.semantics, android.R.layout.simple_spinner_item);
    adapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    semSpinner.setAdapter(adapter);
  }

  private void setListeners() {
    semSpinner.setOnItemSelectedListener(this);
    locClient.setOnDataReturnedListener(this);
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
    if ((curSemantic == null) || !(curSemantic.equals(newSemantic))) {
      curSemantic = newSemantic;
      locClient.getMap();
      progressDialog.show();
    }
  }

  @Override
  public void onMapReturned(Bitmap bitmap) {
    progressDialog.dismiss();

    if (bitmap == null) {
      curSemantic = null;
      toast.show();
    } else {
      toast.cancel();
      mapImageView.setMap(bitmap);
    }
  }

  @Override
  public void onLocationReturned(/* Location loc */) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSemanticReturned(/* Semantic sem */) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onMetadataReturned(/* Metadata mdata */) {
    // TODO Auto-generated method stub

  }

}

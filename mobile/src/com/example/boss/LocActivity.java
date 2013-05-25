package com.example.boss;

import com.example.boss.BOSSLocClient.LocClientListener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

public class LocActivity extends Activity implements
    OnItemSelectedListener, LocClientListener {

  private Spinner semSpinner;
  private ImageView mapImageView;

  private String curSemantic;
  private BOSSLocClient locClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.loc);

    locClient = new BOSSLocClient();

    findViews();
    setAdapters();
    setListeners();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // TODO
  }

  private void findViews() {
    semSpinner = (Spinner) findViewById(R.id.sem_spinner);
    mapImageView = (ImageView) findViewById(R.id.map_view);
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
    // TODO
  }

  private void onSemanticChanged(String newSemantic) {
    if ((curSemantic == null) || !(curSemantic.equals(newSemantic))) {
      curSemantic = newSemantic;
      locClient.getMap();
    }
  }

  @Override
  public void onMapReturned(Bitmap bitmap) {
    mapImageView.setImageBitmap(bitmap);
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

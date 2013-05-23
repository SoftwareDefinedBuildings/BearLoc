package com.example.boss;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

public class Loc extends Activity implements OnItemSelectedListener {

  private Spinner semSpinner = null;
  private ImageView mapView = null;

  private String curSemantic = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.loc);

    // initThreading();
    findViews();
    setAdapters();
    setListeners();
  }

  private void findViews() {
    semSpinner = (Spinner) findViewById(R.id.sem_spinner);
    mapView = (ImageView) findViewById(R.id.map_view);
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
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    String newSemantic = parent.getItemAtPosition(pos).toString();
    changeSemantic(newSemantic);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // TODO Auto-generated method stub

  }

  private void changeSemantic(String newSemantic) {
    // TODO
    if (curSemantic != newSemantic) {
      curSemantic = newSemantic;
    }
  }
}

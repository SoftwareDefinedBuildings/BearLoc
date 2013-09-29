package edu.berkeley.bearloc;

import org.json.JSONObject;

import edu.berkeley.bearloc.R;
import edu.berkeley.bearloc.loc.BearLocClient;
import edu.berkeley.bearloc.loc.BearLocClient.LocClientListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class BearLocActivity extends Activity implements LocClientListener,
    OnClickListener {

  private BearLocClient mLocClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    mLocClient = new BearLocClient(this);
    mLocClient.setOnDataReturnedListener(this);

    View refreshButton = findViewById(R.id.refresh);
    refreshButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.refresh:
      mLocClient.localize();
      break;
    }
  }

  @Override
  public void onLocationReturned(JSONObject locInfo) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onReportDone(JSONObject response) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.menu_settings:
      startActivity(new Intent(this, SettingsActivity.class));
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }
}

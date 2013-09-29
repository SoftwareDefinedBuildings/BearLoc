package edu.berkeley.bearloc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class BearLocActivity extends Activity implements LocClientListener,
    OnClickListener {

  private String targetsem = "room";

  private ListView mListView;
  private ArrayAdapter<String> mArrayAdapter;

  private TextView mTextView;

  private BearLocClient mLocClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    mLocClient = new BearLocClient(this);
    mLocClient.setOnDataReturnedListener(this);

    mListView = (ListView) findViewById(R.id.list);
    mArrayAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1);
    mListView.setAdapter(mArrayAdapter);

    mTextView = (TextView) findViewById(R.id.loc);
    Button refreshButton = (Button) findViewById(R.id.refresh);
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
    if (locInfo == null) {
      return;
    }

    try {
      JSONObject loc = locInfo.getJSONObject("loc");
      JSONObject semtree = locInfo.getJSONObject("sem");
      mTextView.setText(BearLocActivity.getLocStr(loc, semtree, "room"));
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      JSONArray locArray = locInfo.getJSONArray("meta");
      String[] stringArray = new String[locArray.length()];

      for (int i = 0; i < locArray.length(); ++i) {
        stringArray[i] = locArray.getString(i);
      }
      Arrays.sort(stringArray);

      mArrayAdapter.clear();
      for (int i = 0; i < locArray.length(); ++i) {
        mArrayAdapter.add(stringArray[i]);
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private static String getLocStr(JSONObject loc, JSONObject semtree,
      String endsem) {
    String locStr = null;

    try {
      final Iterator<?> dataIter = semtree.keys();
      while (dataIter.hasNext()) {
        String sem = (String) dataIter.next();
        if (sem.equals(endsem)) {
          locStr = loc.getString(sem);
          
          break;
        }

        String subLocStr = BearLocActivity.getLocStr(loc,
            semtree.getJSONObject(sem), endsem);
        if (subLocStr != null) {
          locStr = loc.getString(sem) + "/" + subLocStr;
          break;
        }
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return locStr;
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

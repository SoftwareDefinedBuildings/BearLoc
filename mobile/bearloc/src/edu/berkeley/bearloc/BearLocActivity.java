package edu.berkeley.bearloc;

import java.util.Arrays;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.R;
import edu.berkeley.bearloc.loc.BearLocClient;
import edu.berkeley.bearloc.loc.LocClientListener;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class BearLocActivity extends Activity implements LocClientListener,
    OnClickListener, OnItemClickListener, DialogInterface.OnClickListener {

  private String targetsem = "room";

  private AlertDialog.Builder mDialogBuilder;
  private AlertDialog mSelectDialog;
  private String mSelectedLoc;

  private ListView mListView;
  private ArrayAdapter<String> mArrayAdapter;

  private TextView mTextView;

  private BearLocClient mLocClient;

  private JSONObject mCurLocInfo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    mLocClient = new BearLocClient(this, this);

    mDialogBuilder = new AlertDialog.Builder(this);

    mListView = (ListView) findViewById(R.id.list);
    mArrayAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1);
    mListView.setAdapter(mArrayAdapter);
    mListView.setOnItemClickListener(this);

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
  public void onItemClick(AdapterView<?> parent, View view, int position,
      long id) {
    mSelectedLoc = mArrayAdapter.getItem(position);
    if (mSelectedLoc.endsWith("*")) {
      // location cannot end with *, * is used to label current location
      mSelectedLoc = mSelectedLoc.substring(0, mSelectedLoc.length() - 1);
    }

    mDialogBuilder.setMessage("Change " + targetsem + " to " + mSelectedLoc
        + "?");
    mDialogBuilder.setCancelable(true);
    mDialogBuilder.setPositiveButton(R.string.ok, this);
    mDialogBuilder.setNegativeButton(R.string.cancel, this);
    mSelectDialog = mDialogBuilder.create();
    mSelectDialog.show();
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    switch (which) {
    case DialogInterface.BUTTON_POSITIVE:
      try {
        JSONObject loc = mCurLocInfo.getJSONObject("loc");
        loc.put(targetsem, mSelectedLoc);
        mCurLocInfo.put("confidence", 1);
        onLocChanged();

        mLocClient.report(loc);
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      break;
    case DialogInterface.BUTTON_NEGATIVE:
      break;
    default:
      break;
    }
  }

  @Override
  public void onLocationReturned(JSONObject locInfo) {
    if (locInfo != null) {
      mCurLocInfo = locInfo;
      onLocChanged();
    }
  }

  private void onLocChanged() {
    try {
      JSONObject loc = mCurLocInfo.getJSONObject("loc");
      JSONObject semtree = mCurLocInfo.getJSONObject("sem");
      mTextView.setText(getLocStr(loc, semtree, targetsem) + "   ("
          + Double.toString(mCurLocInfo.getDouble("confidence")) + ")");

      JSONArray locArray = mCurLocInfo.getJSONObject("meta").getJSONArray(
          targetsem);
      String[] stringArray = new String[locArray.length()];

      for (int i = 0; i < locArray.length(); i++) {
        stringArray[i] = locArray.getString(i);
        if (loc.getString(targetsem).equals(stringArray[i])) {
          stringArray[i] += "*";
        }
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

  private static String getLocStr(final JSONObject loc,
      final JSONObject semtree, final String endsem) {
    String locStr = null;

    try {
      final Iterator<?> it = semtree.keys();
      while (it.hasNext()) {
        String sem = (String) it.next();
        if (sem.equals(endsem)) {
          locStr = loc.getString(sem);

          break;
        }

        String subLocStr = getLocStr(loc, semtree.getJSONObject(sem), endsem);
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

}

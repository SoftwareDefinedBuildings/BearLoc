package edu.berkeley.locreporter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.locreporter.LocReporterService.LocReporterBinder;
import edu.berkeley.locreporter.R;
import edu.berkeley.bearloc.SemLocListener;

import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LocReporterActivity extends Activity implements SemLocListener,
    OnClickListener, OnItemClickListener, DialogInterface.OnClickListener {

  private String targetsem = "room";

  private AlertDialog mSelectDialog;
  private AlertDialog mAddDialog;
  private EditText mAddLocEditText;
  private String mSelectedLoc;

  private ListView mListView;
  private ArrayAdapter<String> mArrayAdapter;

  private TextView mTextView;

  private LocReporterService mService;
  private boolean mBound = false;

  private ServiceConnection mServiceConn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      LocReporterBinder binder = (LocReporterBinder) service;
      mService = binder.getService();
      mService.setLocListener(LocReporterActivity.this);
      mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      mBound = false;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Set default setting values
    PreferenceManager.setDefaultValues(this, R.xml.settings_general, false);

    mListView = (ListView) findViewById(R.id.list);
    mArrayAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1);
    mListView.setAdapter(mArrayAdapter);
    mListView.setOnItemClickListener(this);

    mTextView = (TextView) findViewById(R.id.loc);

    Button refreshButton = (Button) findViewById(R.id.refresh);
    refreshButton.setOnClickListener(this);

    Intent intent = new Intent(this, LocReporterService.class);
    bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Unbind from the service
    if (mBound) {
      unbindService(mServiceConn);
      mBound = false;
    }
  }

  @Override
  public void onSemLocChanged(JSONObject semLocInfo) {
    if (semLocInfo != null) {
      try {
        // Get location list
        final JSONObject loc = semLocInfo.getJSONObject("loc");
        final JSONObject semtree = semLocInfo.getJSONObject("sem");
        final DecimalFormat df = new DecimalFormat("#.##");
        final String confStr = "   (Conf:"
            + df.format(semLocInfo.getDouble("confidence")).toString() + ")";
        mTextView.setText(getLocStr(loc, semtree, targetsem) + confStr);

        JSONArray locArray = semLocInfo.getJSONObject("meta").getJSONArray(
            targetsem);
        List<String> stringArray = new ArrayList<String>();

        for (int i = 0; i < locArray.length(); i++) {
          if (loc.getString(targetsem).equals(locArray.getString(i))) {
            stringArray.add(locArray.getString(i) + "*");
          } else {
            stringArray.add(locArray.getString(i));
          }
        }
        Collections.sort(stringArray);

        stringArray.add(getString(R.string.add_new));

        // Show locations on ListView
        mArrayAdapter.clear();
        Iterator<String> iterator = stringArray.iterator();
        while (iterator.hasNext()) {
          mArrayAdapter.add(iterator.next());
        }
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      Toast.makeText(this, R.string.loc_updated, Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.refresh:
      mService.localize();
      break;
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position,
      long id) {
    if (position < mArrayAdapter.getCount() - 1) {
      // If NOT the last one (+ Add new) is selected
      mSelectedLoc = mArrayAdapter.getItem(position);
      if (mSelectedLoc.endsWith("*")) {
        // location cannot end with *, * is used to label current location
        mSelectedLoc = mSelectedLoc.substring(0, mSelectedLoc.length() - 1);
      }

      final AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage("Change " + targetsem + " to " + mSelectedLoc + "?");
      builder.setCancelable(true);
      builder.setPositiveButton(R.string.ok, this);
      builder.setNegativeButton(R.string.cancel, this);

      mSelectDialog = builder.create();
      mSelectDialog.show();
    } else {
      // If the last one (+ Add new) is selected
      final AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage("Please input your current location.");
      mAddLocEditText = new EditText(this);
      builder.setView(mAddLocEditText);
      builder.setCancelable(true);
      builder.setPositiveButton(R.string.ok, this);
      builder.setNegativeButton(R.string.cancel, this);

      mAddDialog = builder.create();
      mAddDialog.show();
    }
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    if (dialog == mSelectDialog) {
      switch (which) {
      case DialogInterface.BUTTON_POSITIVE:
        mService.update(targetsem, mSelectedLoc);
        break;
      case DialogInterface.BUTTON_NEGATIVE:
        break;
      default:
        break;
      }
    } else if (dialog == mAddDialog) {
      switch (which) {
      case DialogInterface.BUTTON_POSITIVE:
        final String newInputLoc = mAddLocEditText.getText().toString().trim();
        mService.update(targetsem, newInputLoc);
        break;
      case DialogInterface.BUTTON_NEGATIVE:
        break;
      default:
        break;
      }
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

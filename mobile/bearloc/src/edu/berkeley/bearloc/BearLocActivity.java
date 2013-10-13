package edu.berkeley.bearloc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.R;
import edu.berkeley.bearloc.loc.BearLocClient;
import edu.berkeley.bearloc.loc.LocClientListener;
import edu.berkeley.bearloc.util.DeviceUUIDFactory;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

public class BearLocActivity extends Activity implements LocClientListener,
    OnClickListener, OnItemClickListener, DialogInterface.OnClickListener,
    SensorEventListener {

  private static final long AUTO_REPORT_ITVL = 180000L; // millisecond

  private String targetsem = "room";

  private AlertDialog mSelectDialog;
  private AlertDialog mAddDialog;
  private EditText mAddLocEditText;
  private String mSelectedLoc;

  private ListView mListView;
  private ArrayAdapter<String> mArrayAdapter;

  private TextView mTextView;

  private BearLocClient mLocClient;

  private JSONObject mCurLocInfo;

  private Handler mHandler;

  private Sensor mAcc;
  private final Runnable mReportLocTask = new Runnable() {
    @Override
    public void run() {
      reportLoc();
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Set default setting values
    PreferenceManager.setDefaultValues(this, R.xml.settings_general, false);
    SettingsActivity.setDeviceUUID(this, (new DeviceUUIDFactory(this))
        .getDeviceUUID().toString());
    PreferenceManager.setDefaultValues(this, R.xml.settings_server, false);

    mLocClient = new BearLocClient(this, this);

    mListView = (ListView) findViewById(R.id.list);
    mArrayAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1);
    mListView.setAdapter(mArrayAdapter);
    mListView.setOnItemClickListener(this);

    mTextView = (TextView) findViewById(R.id.loc);

    Button refreshButton = (Button) findViewById(R.id.refresh);
    refreshButton.setOnClickListener(this);

    mHandler = new Handler();

    final SensorManager sensorMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mAcc = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    sensorMgr.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
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
        try {
          final JSONObject loc = mCurLocInfo.getJSONObject("loc");
          loc.put(targetsem, mSelectedLoc);
          mCurLocInfo.put("confidence", 1);
          onLocChanged();

          reportLoc();
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
    } else if (dialog == mAddDialog) {
      switch (which) {
      case DialogInterface.BUTTON_POSITIVE:
        try {
          final String newInputLoc = mAddLocEditText.getText().toString()
              .trim();
          final JSONObject loc = mCurLocInfo.getJSONObject("loc");
          loc.put(targetsem, newInputLoc);

          // Add new location to meta if it doesn't exist
          final JSONArray locArray = mCurLocInfo.getJSONObject("meta")
              .getJSONArray(targetsem);
          boolean newLocExist = false;
          for (int i = 0; i < locArray.length(); i++) {
            if (newInputLoc.equals(locArray.getString(i))) {
              newLocExist = true;
              break;
            }
          }
          if (newLocExist == false) {
            locArray.put(newInputLoc);
          }

          mCurLocInfo.put("confidence", 1);

          onLocChanged();

          reportLoc();
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
  }

  @Override
  public void onLocationReturned(JSONObject locInfo) {
    if (locInfo != null) {
      mCurLocInfo = locInfo;
      onLocChanged();

      Toast.makeText(this, R.string.loc_updated, Toast.LENGTH_SHORT).show();
    }
  }

  private void onLocChanged() {
    try {
      JSONObject loc = mCurLocInfo.getJSONObject("loc");
      JSONObject semtree = mCurLocInfo.getJSONObject("sem");
      final DecimalFormat df = new DecimalFormat("#.##");
      final String confStr = "   (Conf:"
          + df.format(mCurLocInfo.getDouble("confidence")).toString() + ")";
      mTextView.setText(getLocStr(loc, semtree, targetsem) + confStr);

      JSONArray locArray = mCurLocInfo.getJSONObject("meta").getJSONArray(
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

      mArrayAdapter.clear();
      Iterator<String> iterator = stringArray.iterator();
      while (iterator.hasNext()) {
        mArrayAdapter.add(iterator.next());
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void reportLoc() {
    try {
      final JSONObject loc = mCurLocInfo.getJSONObject("loc");
      mLocClient.report(loc);

      if (mAcc != null && SettingsActivity.getAutoReport(this) == true) {
        // report in AUTO_REPORT_ITVL milliseconds
        mHandler.postDelayed(mReportLocTask, AUTO_REPORT_ITVL);
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event != null
        && (Math.abs(event.values[0]) > 1 || Math.abs(event.values[0]) > 1 || event.values[2] < 9)) {
      // If not statically face up, then stop reporting location
      mHandler.removeCallbacks(mReportLocTask);
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

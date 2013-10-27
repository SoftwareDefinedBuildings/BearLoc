/*
 * Copyright (c) 2013, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Author: Kaifei Chen <kaifei@eecs.berkeley.edu>
 */

package edu.berkeley.locreporter;

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

  final private String[] mSems = new String[] { "country", "state", "city",
      "street", "building", "floor", "room" };
  private int mCurSemIdx = mSems.length - 1;

  private AlertDialog mSelectDialog;
  private AlertDialog mAddDialog;
  private EditText mAddLocEditText;
  private String mSelectedLoc;

  private ListView mListView;
  private ArrayAdapter<String> mArrayAdapter;

  private TextView mLocPrefixTextView;
  private TextView mCurSemLocTextView;

  private LocReporterService mService;
  private boolean mBound = false;

  private ServiceConnection mServiceConn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      LocReporterBinder binder = (LocReporterBinder) service;
      mService = binder.getService();
      mService.setSemLocListener(LocReporterActivity.this);
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

    mLocPrefixTextView = (TextView) findViewById(R.id.loc_prefix);
    mCurSemLocTextView = (TextView) findViewById(R.id.cur_sem_loc);

    final Button addButton = (Button) findViewById(R.id.add_loc);
    addButton.setOnClickListener(this);
    final Button semButton = (Button) findViewById(R.id.change_sem);
    semButton.setOnClickListener(this);
    final Button locButton = (Button) findViewById(R.id.localize);
    locButton.setOnClickListener(this);

    Intent intent = new Intent(this, LocReporterService.class);
    bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Unbind from the service
    if (mBound == true) {
      unbindService(mServiceConn);
      mBound = false;
    }
  }

  @Override
  public void onSemLocChanged(JSONObject semLocInfo) {
    if (semLocInfo != null) {
      try {
        // Get location list
        final JSONObject semloc = semLocInfo.getJSONObject("loc");
        if (mCurSemIdx > 0) {
          mLocPrefixTextView.setText(getLocStr(semloc, mSems,
              mSems[mCurSemIdx - 1]));
        } else {
          mLocPrefixTextView.setText("");
        }
        mCurSemLocTextView.setText(mSems[mCurSemIdx] + ":\n"
            + semloc.getString(mSems[mCurSemIdx]));

        JSONArray locArray = semLocInfo.getJSONObject("meta").getJSONArray(
            mSems[mCurSemIdx]);
        List<String> stringArray = new ArrayList<String>();

        for (int i = 0; i < locArray.length(); i++) {
          stringArray.add(locArray.getString(i));
        }
        Collections.sort(stringArray);

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
    case R.id.add_loc:
      final AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage("Please input your current location.");
      mAddLocEditText = new EditText(this);
      builder.setView(mAddLocEditText);
      builder.setCancelable(true);
      builder.setPositiveButton(R.string.ok, this);
      builder.setNegativeButton(R.string.cancel, this);

      mAddDialog = builder.create();
      mAddDialog.show();
      break;
    case R.id.change_sem:
      // TODO
      break;
    case R.id.localize:
      mService.localize();
      break;
    default:
      break;
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position,
      long id) {
    mSelectedLoc = mArrayAdapter.getItem(position);

    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("Change " + mSems[mCurSemIdx] + " to " + mSelectedLoc
        + "?");
    builder.setCancelable(true);
    builder.setPositiveButton(R.string.ok, this);
    builder.setNegativeButton(R.string.cancel, this);

    mSelectDialog = builder.create();
    mSelectDialog.show();
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    if (dialog == mSelectDialog) {
      switch (which) {
      case DialogInterface.BUTTON_POSITIVE:
        mService.update(mSems[mCurSemIdx], mSelectedLoc);
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
        mService.update(mSems[mCurSemIdx], newInputLoc);
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

  private static String getLocStr(final JSONObject semloc, final String[] sems,
      final String endsem) {
    String locStr = "";

    try {
      for (int i = 0; i < sems.length; i++) {
        locStr += "/" + semloc.getString(sems[i]);
        if (sems[i] == endsem) {
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

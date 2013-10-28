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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.berkeley.locreporter.LocReporterService.LocReporterBinder;
import edu.berkeley.locreporter.R;
import edu.berkeley.bearloc.MetaListener;
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
    MetaListener, OnClickListener, OnItemClickListener,
    DialogInterface.OnClickListener {

  private String mCurSem;

  private AlertDialog mAddDialog;
  private AlertDialog mChangeSemDialog;
  private AlertDialog mSelectDialog;
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
      mService.setMetaListener(LocReporterActivity.this);
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

    mCurSem = LocReporterService.Sems[LocReporterService.Sems.length - 1];

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
  public void onClick(View v) {
    AlertDialog.Builder builder;
    switch (v.getId()) {
    case R.id.add_loc:
      builder = new AlertDialog.Builder(this);
      builder.setMessage("Please input your CURRENT " + mCurSem + ".");
      mAddLocEditText = new EditText(this);
      builder.setView(mAddLocEditText);
      builder.setCancelable(true);
      builder.setPositiveButton(R.string.ok, this);
      builder.setNegativeButton(R.string.cancel, this);

      mAddDialog = builder.create();
      mAddDialog.show();
      break;
    case R.id.change_sem:
      builder = new AlertDialog.Builder(this);
      builder.setTitle("Please select a semantic.");
      builder.setCancelable(true);
      builder.setItems(LocReporterService.Sems, this);

      mChangeSemDialog = builder.create();
      mChangeSemDialog.show();
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
    builder.setMessage("Change " + mCurSem + " to " + mSelectedLoc + "?");
    builder.setCancelable(true);
    builder.setPositiveButton(R.string.ok, this);
    builder.setNegativeButton(R.string.cancel, this);

    mSelectDialog = builder.create();
    mSelectDialog.show();
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    if (dialog == mAddDialog) {
      switch (which) {
      case DialogInterface.BUTTON_POSITIVE:
        final String newInputLoc = mAddLocEditText.getText().toString().trim();
        if (newInputLoc.length() > 0) {
          changeSemLoc(newInputLoc);
          refresh();
        }
        break;
      case DialogInterface.BUTTON_NEGATIVE:
        break;
      default:
        break;
      }
    } else if (dialog == mChangeSemDialog) {
      mCurSem = LocReporterService.Sems[which];
      refresh();
    } else if (dialog == mSelectDialog) {
      switch (which) {
      case DialogInterface.BUTTON_POSITIVE:
        changeSemLoc(mSelectedLoc);
        refresh();
        break;
      case DialogInterface.BUTTON_NEGATIVE:
        break;
      default:
        break;
      }
    }
  }

  /*
   * User changes current semantic location
   */
  private void changeSemLoc(final String loc) {
    mService.changeSemLoc(mCurSem, loc);

    // move semantic downward if it is not at lowest level
    int curSemIdx = Arrays.asList(LocReporterService.Sems).indexOf(mCurSem);
    if (curSemIdx < LocReporterService.Sems.length - 1) {
      mCurSem = LocReporterService.Sems[curSemIdx + 1];
    }
  }

  /*
   * Update UI
   */
  private void refresh() {
    // update location text
    final JSONObject semloc = mService.curSemLocInfo().optJSONObject("semloc");
    if (semloc != null) {
      mLocPrefixTextView.setText(LocReporterService.getLocStr(semloc,
          LocReporterService.Sems, mCurSem));
      mCurSemLocTextView.setText(mCurSem + ":\n"
          + semloc.optString(mCurSem, null));
    }

    // update locations of current semantic on ListView
    JSONArray locArray = mService.curMeta().optJSONArray(mCurSem);
    if (locArray != null) {
      List<String> stringArray = new ArrayList<String>();
      for (int i = 0; i < locArray.length(); i++) {
        stringArray.add(locArray.optString(i));
      }
      Collections.sort(stringArray);
      mArrayAdapter.clear();
      Iterator<String> iterator = stringArray.iterator();
      while (iterator.hasNext()) {
        mArrayAdapter.add(iterator.next());
      }
    }
  }

  @Override
  public void onSemLocInfoReturned(JSONObject semLocInfo) {
    refresh();
    Toast.makeText(this, R.string.loc_updated, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onMetaReturned(JSONObject meta) {
    refresh();
    Toast.makeText(this, R.string.meta_updated, Toast.LENGTH_SHORT).show();
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

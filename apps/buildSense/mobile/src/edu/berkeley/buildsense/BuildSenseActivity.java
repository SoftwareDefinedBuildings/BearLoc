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

package edu.berkeley.buildsense;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import edu.berkeley.bearloc.MetaListener;
import edu.berkeley.bearloc.SemLocListener;
import edu.berkeley.buildsense.BuildSenseService.BuildSenseBinder;

public class BuildSenseActivity extends Activity implements SemLocListener,
        MetaListener, OnClickListener, OnItemClickListener,
        DialogInterface.OnClickListener {

    private String mCurSem;

    private AlertDialog mAddDialog;
    private AlertDialog mChangeSemDialog;
    private AlertDialog mSelectDialog;
    private AlertDialog mNoteDialog;
    private EditText mAddLocEditText;
    private EditText mNoteEditText;
    private Spinner mNoteSpinner;
    private String mSelectedLoc;
    private String mLastNote;

    private ListView mListView;
    private ArrayAdapter<String> mArrayAdapter;

    private TextView mLocPrefixTextView;
    private TextView mCurSemLocTextView;
    private Button mAddButton;
    private Button mSemButton;

    private BuildSenseService mService;
    private boolean mBound = false;

    private final ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name,
                final IBinder service) {
            final BuildSenseBinder binder = (BuildSenseBinder) service;
            mService = binder.getService();
            mService.setSemLocListener(BuildSenseActivity.this);
            mService.setMetaListener(BuildSenseActivity.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Set default setting values
        PreferenceManager.setDefaultValues(this, R.xml.general_settings, false);

        mCurSem = BuildSenseService.Sems[BuildSenseService.Sems.length - 1];

        mListView = (ListView) findViewById(R.id.list);
        mArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        mListView.setAdapter(mArrayAdapter);
        mListView.setOnItemClickListener(this);

        mLocPrefixTextView = (TextView) findViewById(R.id.loc_prefix);
        mCurSemLocTextView = (TextView) findViewById(R.id.cur_sem_loc);

        mAddButton = (Button) findViewById(R.id.add_loc);
        mAddButton.setOnClickListener(this);
        mAddButton.setEnabled(false);
        mSemButton = (Button) findViewById(R.id.change_sem);
        mSemButton.setOnClickListener(this);
        mSemButton.setEnabled(false);
        final Button locButton = (Button) findViewById(R.id.localize);
        locButton.setOnClickListener(this);

        final Intent intent = new Intent(this, BuildSenseService.class);
        bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
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
    public void onClick(final View v) {
        AlertDialog.Builder builder;
        switch (v.getId()) {
        case R.id.add_loc:
            builder = new AlertDialog.Builder(this);
            builder.setMessage("Please input your CURRENT " + mCurSem + ".");
            mAddLocEditText = new EditText(this);
            mAddLocEditText.setHint("CURRENT " + mCurSem);
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
            builder.setItems(BuildSenseService.Sems, this);

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
    public void onItemClick(final AdapterView<?> parent, final View view,
            final int position, final long id) {
        // move semantic downward if it is not at lowest level
        final int curSemIdx = Arrays.asList(BuildSenseService.Sems).indexOf(
                mCurSem);
        // Check whether it is lowest level of semantic
        if (curSemIdx < BuildSenseService.Sems.length - 1) {
            mSelectedLoc = mArrayAdapter.getItem(position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Change your CURRENT " + mCurSem + " to "
                    + mSelectedLoc + "?");
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.ok, this);
            builder.setNegativeButton(R.string.cancel, this);

            mSelectDialog = builder.create();
            mSelectDialog.show();
        } else {
            mSelectedLoc = mArrayAdapter.getItem(position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final LayoutInflater inflater = getLayoutInflater();
            final View dialogView = inflater
                    .inflate(R.layout.note_dialog, null);
            builder.setMessage("Report notes about your CURRENT " + mCurSem
                    + ": " + mSelectedLoc + ".");
            builder.setView(dialogView);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.ok, this);
            builder.setNegativeButton(R.string.cancel, this);

            mNoteDialog = builder.create();
            mNoteDialog.show();

            // Handle UI listeners
            mNoteEditText = (EditText) dialogView.findViewById(R.id.note);
            mNoteEditText.setText(mLastNote);
            mNoteSpinner = (Spinner) dialogView
                    .findViewById(R.id.note_candidates_spinner);
            mNoteSpinner
                    .setOnItemSelectedListener(new OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(final AdapterView<?> parent,
                                final View view, final int position,
                                final long id) {
                            if (position > 0) {
                                final String note = mNoteSpinner
                                        .getSelectedItem().toString();
                                mNoteEditText.setText(note);
                            }
                        }

                        @Override
                        public void onNothingSelected(final AdapterView<?> arg0) {
                            // TODO Auto-generated method stub
                        }
                    });
        }
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        if (dialog == mAddDialog) {
            switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                final String newInputLoc = mAddLocEditText.getText().toString()
                        .trim();
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
            mCurSem = BuildSenseService.Sems[which];
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
        } else if (dialog == mNoteDialog) {
            switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mNoteEditText != null) {
                    final String note = mNoteEditText.getText().toString()
                            .trim();
                    if (note.length() > 0) {
                        mService.note(note);
                        mLastNote = note;
                    }
                }
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
        final int curSemIdx = Arrays.asList(BuildSenseService.Sems).indexOf(
                mCurSem);
        if (curSemIdx < BuildSenseService.Sems.length - 1) {
            mCurSem = BuildSenseService.Sems[curSemIdx + 1];
        }
    }

    /*
     * Update UI
     */
    private void refresh() {
        // update add location button text
        mAddButton.setText("Add " + mCurSem);

        if (mService == null) {
            return;
        }

        // update location text
        final JSONObject semloc = mService.curSemLocInfo().optJSONObject(
                "semloc");
        if (semloc != null) {
            mLocPrefixTextView.setText(BuildSenseService.getLocStr(semloc,
                    BuildSenseService.Sems, mCurSem));
            mCurSemLocTextView.setText(mCurSem + ":\n"
                    + semloc.optString(mCurSem, null));
        }

        // update locations of current semantic on ListView
        final JSONArray locArray = mService.curMeta().optJSONArray(mCurSem);
        if (locArray != null) {
            final List<String> stringArray = new ArrayList<String>();
            for (int i = 0; i < locArray.length(); i++) {
                stringArray.add(locArray.optString(i));
            }
            Collections.sort(stringArray);
            mArrayAdapter.clear();
            final Iterator<String> iterator = stringArray.iterator();
            while (iterator.hasNext()) {
                mArrayAdapter.add(iterator.next());
            }
        }
    }

    @Override
    public void onSemLocInfoReturned(final JSONObject semLocInfo) {
        mAddButton.setEnabled(true);
        mSemButton.setEnabled(true);
        refresh();
        Toast.makeText(this, R.string.loc_updated, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMetaReturned(final JSONObject meta) {
        refresh();
        Toast.makeText(this, R.string.meta_updated, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_settings:
            startActivity(new Intent(this, BuildSenseSettingsActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}

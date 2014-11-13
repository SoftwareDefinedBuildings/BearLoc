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
import org.json.JSONException;
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

import edu.berkeley.bearloc.BearLocApp;
import edu.berkeley.bearloc.BearLocApp.LocListener;
import edu.berkeley.bearloc.BearLocSensor;
import edu.berkeley.bearloc.BearLocSensor.Driver.SensorListener;
import edu.berkeley.bearloc.sensor.Wifi;

public class LocReporterActivity extends Activity {

    private JSONObject mCurLoc;
    private String mCurSem;
    private JSONArray mCurCandidate;

    private AlertDialog mAddDialog;
    private AlertDialog mChangeSemDialog;
    private AlertDialog mSelectDialog;
    private EditText mAddLocEditText;
    private String mSelectedLoc;

    private ListView mListView;
    private ArrayAdapter<String> mArrayAdapter;

    private TextView mLocPrefixTextView;
    private TextView mCurSemLocTextView;
    private Button mAddButton;
    private Button mSemButton;
    private Button mLocButton;

    private BearLocApp mBearLocApp;
    private BearLocSensor mWiFiSensor;

    final private static String[] mSemantics = new String[]{"country", "state",
            "city", "street", "building", "locale"};

    private OnClickListener mAddButtonOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(LocReporterActivity.this);
            builder.setMessage("Please input your CURRENT " + mCurSem + ".");
            mAddLocEditText = new EditText(LocReporterActivity.this);
            builder.setView(mAddLocEditText);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.ok, mAddDialogOnClickListener);
            builder.setNegativeButton(R.string.cancel,
                    mAddDialogOnClickListener);

            mAddDialog = builder.create();
            mAddDialog.show();
        }

    };

    private OnClickListener mSemanticButtonOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(LocReporterActivity.this);
            builder.setTitle("Please select a semantic.");
            builder.setCancelable(true);
            builder.setItems(mSemantics, mChangeSemDialogOnClickListener);

            mChangeSemDialog = builder.create();
            mChangeSemDialog.show();
        }

    };

    private OnClickListener mLocateButtonOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mBearLocApp.getLocation() == true) {
                mLocButton.setEnabled(false);
            }
        }

    };

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            mSelectedLoc = mArrayAdapter.getItem(position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    LocReporterActivity.this);
            builder.setMessage("Change your CURRENT " + mCurSem + " to "
                    + mSelectedLoc + "?");
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.ok, mSelectDialogOnClickListener);
            builder.setNegativeButton(R.string.cancel,
                    mSelectDialogOnClickListener);

            mSelectDialog = builder.create();
            mSelectDialog.show();
        }

    };

    private DialogInterface.OnClickListener mAddDialogOnClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE :
                    final String newInputLoc = mAddLocEditText.getText()
                            .toString().trim();
                    if (newInputLoc.length() > 0) {
                        addLoc(newInputLoc);
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE :
                    break;
                default :
                    break;
            }
        }

    };

    private DialogInterface.OnClickListener mChangeSemDialogOnClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            final String sem = mSemantics[which];
            changeSem(sem);
        }

    };

    private DialogInterface.OnClickListener mSelectDialogOnClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE :
                    changeLoc(mSelectedLoc);
                    break;
                case DialogInterface.BUTTON_NEGATIVE :
                    break;
                default :
                    break;
            }
        }

    };

    private LocListener mLocListener = new LocListener() {

        @Override
        public void onResponseReturned(JSONObject response) {
            mAddButton.setEnabled(true);
            mSemButton.setEnabled(true);
            mLocButton.setEnabled(true);

            if (response == null) {
                Toast.makeText(LocReporterActivity.this,
                        R.string.server_no_respond, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                if (response.has("type")) {
                    String type = response.getString("type");
                    if (type == "localize") {
                        onLocEventReturned(response);
                    } else if (type == "candidate") {
                        onCandidateEventReturned(response);
                    }
                }
            } catch (final JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mCurLoc = new JSONObject();
        mCurSem = mSemantics[mSemantics.length - 1];
        mCurCandidate = new JSONArray();

        mListView = (ListView) findViewById(R.id.list);
        mArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        mListView.setAdapter(mArrayAdapter);
        mListView.setOnItemClickListener(mOnItemClickListener);

        mLocPrefixTextView = (TextView) findViewById(R.id.loc_prefix);
        mCurSemLocTextView = (TextView) findViewById(R.id.cur_sem_loc);

        mAddButton = (Button) findViewById(R.id.add_loc);
        mAddButton.setOnClickListener(mAddButtonOnClickListener);
        mAddButton.setEnabled(false);
        mSemButton = (Button) findViewById(R.id.change_sem);
        mSemButton.setOnClickListener(mSemanticButtonOnClickListener);
        mSemButton.setEnabled(false);
        mLocButton = (Button) findViewById(R.id.localize);
        mLocButton.setOnClickListener(mLocateButtonOnClickListener);
        mLocButton.setEnabled(true);

        mBearLocApp = new BearLocApp(this, mLocListener,
                "tcp://bearloc.cal-sdb.org:52411", "algorithm001-request");
        mWiFiSensor = new BearLocSensor(this, new Wifi(this),
                "tcp://bearloc.cal-sdb.org:52411", "algorithm001-request");

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
    }

    private void addLoc(final String loc) {
        for (int i = 0; i < mCurCandidate.length(); i++) {
            if (mCurCandidate.optString(i).equals(loc)) {
                changeLoc(loc);
                return;
            }
        }
        mCurCandidate.put(loc);
        changeLoc(loc);
    }

    private void changeLoc(final String loc) {
        try {
            final String oldLoc = mCurLoc.optString(mCurSem);
            mCurLoc.put(mCurSem, loc);

            if (oldLoc.equals(loc) == false) {
                final JSONObject newLoc = new JSONObject();
                for (final String sem : mSemantics) {
                    newLoc.put(sem, mCurLoc.getString(sem));
                    if (sem.equals(mCurSem) == true) {
                        break;
                    }
                }
                mCurLoc = newLoc;
            }
            mService.reportLocation(mCurLoc);

            // move semantic downward if it is not at lowest level
            final int curSemIdx = Arrays.asList(mSemantics).indexOf(mCurSem);
            if (curSemIdx < mSemantics.length - 1) {
                mCurSem = mSemantics[curSemIdx + 1];
            }

            refresh();
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TODO: move semantic downward if it is not at lowest level
    }

    private void changeSem(final String sem) {
        if (mCurSem.equals(sem) == false) {
            mCurSem = sem;
            if (getCandidate() == false) {
                Toast.makeText(this, R.string.server_no_respond,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
     * Update UI
     */
    private void refresh() {
        // update add location button text
        mAddButton.setText("Add " + mCurSem);

        // update location text
        if (mCurLoc != null) {
            mLocPrefixTextView.setText(getLocStr(mCurLoc, mSemantics, mCurSem));
            mCurSemLocTextView.setText(mCurSem + ":\n"
                    + mCurLoc.optString(mCurSem, null));
        }

        // update locations of current semantic on ListView
        if (mCurCandidate != null) {
            final List<String> stringArray = new ArrayList<String>();
            for (int i = 0; i < mCurCandidate.length(); i++) {
                stringArray.add(mCurCandidate.optString(i));
            }
            // Sort stringArray before showing list
            Collections.sort(stringArray);
            mArrayAdapter.clear();
            final Iterator<String> iterator = stringArray.iterator();
            while (iterator.hasNext()) {
                mArrayAdapter.add(iterator.next());
            }
        }
    }
    
    public void onLocEventReturned(final JSONObject locEvent) {
        final JSONObject oldLoc = mCurLoc;
        mCurLoc = locEvent; // The response is JSON Array
        if (oldLoc == null
                || oldLoc.toString().equals(mCurLoc.toString()) == false) {
            if (getCandidate() == false) {
                mCurLoc = oldLoc;
                Toast.makeText(this, R.string.loc_not_updated,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.loc_updated, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void onCandidateEventReturned(final JSONObject candidateEvent) {
        if (candidateEvent == null) {
            Toast.makeText(this, R.string.server_no_respond, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        try {
            mCurCandidate = candidateEvent.getJSONArray("location candidate");
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Toast.makeText(this, R.string.meta_updated, Toast.LENGTH_SHORT).show();
        refresh();
    }

    private boolean getCandidate() {
        try {
            final JSONObject queryLoc = new JSONObject();
            for (final String sem : mSemantics) {
                if (sem.equals(mCurSem) == true) {
                    break;
                }
                if (mCurLoc.has(sem) == false) {
                    return false;
                }
                queryLoc.put(sem, mCurLoc.getString(sem));
            }
            final boolean rv = mService.getCandidate(queryLoc, this);
            if (rv == false) {
                Toast.makeText(this, R.string.server_no_respond,
                        Toast.LENGTH_SHORT).show();
            }
            return rv;
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    public String getLocStr(final JSONObject loc, final String[] sems,
            final String endSem) {
        String locStr = "";

        for (final String sem : sems) {
            if (sem == endSem) {
                break;
            }
            locStr += "/" + loc.optString(sem, null);
        }

        return locStr;
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
            case R.id.menu_settings :
                startActivity(new Intent(this,
                        LocReporterSettingsActivity.class));
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }
}

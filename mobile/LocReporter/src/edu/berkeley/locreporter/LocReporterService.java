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

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.BearLocService;
import edu.berkeley.bearloc.MetaListener;
import edu.berkeley.bearloc.SemLocListener;
import edu.berkeley.bearloc.BearLocService.BearLocBinder;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

public class LocReporterService extends Service implements SemLocListener,
    MetaListener, SensorEventListener {

  private static final long AUTO_REPORT_ITVL = 180000L; // millisecond

  private BearLocService mBearLocService;
  private boolean mBound = false;
  private ServiceConnection mBearLocConn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      BearLocBinder binder = (BearLocBinder) service;
      mBearLocService = binder.getService();
      mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      mBound = false;
    }
  };

  final public static String[] Sems = new String[] { "country", "state",
      "city", "street", "building", "floor", "room" };
  private JSONObject mCurSemLocInfo;
  private JSONObject mCurMeta;

  private IBinder mBinder;
  private Handler mHandler;
  private SemLocListener mSemLocListener;
  private MetaListener mMetaListener;

  private Sensor mAcc;
  private final Runnable mReportLocTask = new Runnable() {
    @Override
    public void run() {
      reportSemLoc();
    }
  };

  public class LocReporterBinder extends Binder {
    public LocReporterService getService() {
      // Return this instance of LocalService so clients can call public methods
      return LocReporterService.this;
    }
  }

  @Override
  public void onCreate() {
    Intent intent = new Intent(this, BearLocService.class);
    bindService(intent, mBearLocConn, Context.BIND_AUTO_CREATE);

    mCurSemLocInfo = new JSONObject();
    mCurMeta = new JSONObject();

    mBinder = new LocReporterBinder();
    mHandler = new Handler();

    final SensorManager sensorMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mAcc = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    sensorMgr.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    // Unbind from the service
    if (mBound) {
      unbindService(mBearLocConn);
      mBound = false;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

  public void setSemLocListener(final SemLocListener semLocListener) {
    mSemLocListener = semLocListener;
  }

  public void setMetaListener(final MetaListener metaListener) {
    mMetaListener = metaListener;
  }

  public JSONObject curSemLocInfo() {
    return mCurSemLocInfo;
  }

  public JSONObject curMeta() {
    return mCurMeta;
  }

  public boolean localize() {
    if (mSemLocListener == null) {
      return false;
    }

    return mBearLocService.localize(this);
  }

  /*
   * Application changes current semantic location
   */
  public void changeSemLoc(final String sem, final String loc) {
    try {
      final JSONObject semloc = mCurSemLocInfo.getJSONObject("semloc");
      semloc.put(sem, loc);
      mCurSemLocInfo.put("confidence", 1);

      reportSemLoc();

      changeMeta(sem, loc);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void changeMeta(final String sem, final String loc) {
    try {
      // add new location to meta if it doesn't exist
      final JSONArray locArray = mCurMeta.getJSONArray(sem);
      boolean newLocExist = false;
      for (int i = 0; i < locArray.length(); i++) {
        if (loc.equals(locArray.getString(i))) {
          newLocExist = true;
          break;
        }
      }
      if (newLocExist == false) {
        locArray.put(loc);
      }

      // clear all meta in lower levels
      for (int i = Arrays.asList(Sems).indexOf(sem) + 1; i < Sems.length; i++) {
        mCurMeta.put(Sems[i], new JSONArray());
      }

      // request new meta if sem is not at lowest level
      if (Arrays.asList(Sems).indexOf(sem) < Sems.length - 1) {
        requestMeta();
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static String getLocStr(final JSONObject semloc, final String[] sems,
      final String endSem) {
    String locStr = "";

    try {
      for (int i = 0; i < sems.length; i++) {
        if (sems[i] == endSem) {
          break;
        }
        locStr += "/" + semloc.getString(sems[i]);
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return locStr;
  }

  /*
   * Async call to report current location
   */
  private void reportSemLoc() {
    try {
      final JSONObject semloc = mCurSemLocInfo.getJSONObject("semloc");
      mBearLocService.report(semloc);

      if (mAcc != null && SettingsActivity.getAutoReport(this) == true) {
        // report in AUTO_REPORT_ITVL milliseconds
        mHandler.postDelayed(mReportLocTask, AUTO_REPORT_ITVL);
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /*
   * Async call to request meta for current location
   */
  private boolean requestMeta() {
    try {
      final JSONObject semloc = mCurSemLocInfo.getJSONObject("semloc");
      return mBearLocService.meta(semloc, this);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public void onSemLocInfoReturned(JSONObject semLocInfo) {
    mCurSemLocInfo = semLocInfo;
    requestMeta();

    if (mSemLocListener != null) {
      mSemLocListener.onSemLocInfoReturned(mCurSemLocInfo);
    }
  }

  @Override
  public void onMetaReturned(JSONObject meta) {
    mCurMeta = meta;

    if (mMetaListener != null) {
      mMetaListener.onMetaReturned(mCurMeta);
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
}

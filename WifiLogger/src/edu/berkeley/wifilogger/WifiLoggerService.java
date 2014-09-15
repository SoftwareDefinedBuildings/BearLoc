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

package edu.berkeley.wifilogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import au.com.bytecode.opencsv.CSVWriter;
import edu.berkeley.wifilogger.WifiSampler.SamplerListener;

public class WifiLoggerService extends Service implements SamplerListener {

    private static final int DATA_WRITE_ITVL = 60000; // millisecond

    private IBinder mBinder;

    private Handler mHandler;
    private Integer mDataWriteItvl = null; // Millisecond, null if not scheduled

    private WifiLoggerCache mCache;
    private WifiSampler mSampler;
    private WifiLoggerFormat mFormat;
    private LoggerListener mLoggerListener;

    private File mDataDirectory;

    private final Runnable mWriteDataTask = new Runnable() {
        @Override
        public void run() {
            writeData();
        }
    };

    public class WifiLoggerBinder extends Binder {
        public WifiLoggerService getService() {
            // Return this instance of LocalService so clients can call public
            // methods
            return WifiLoggerService.this;
        }
    }

    @Override
    public void onCreate() {
        mBinder = new WifiLoggerBinder();
        mHandler = new Handler();
        mCache = new WifiLoggerCache(this);
        mSampler = new WifiSampler(this, this);
        mFormat = new WifiLoggerFormat(this, mCache);

        final File sdCard = Environment.getExternalStorageDirectory();
        final String timestamp = Long.toString(System.currentTimeMillis());
        mDataDirectory = new File(sdCard.getAbsolutePath() + "/BearLoc"
                + timestamp + "/");
        mDataDirectory.mkdirs();
    }

    @Override
    public void onDestroy() {
        mSampler.stop();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    public void setWriteListener(final LoggerListener writeListener) {
        mLoggerListener = writeListener;
    }

    @Override
    public void onWifiEvent(List<ScanResult> results) {
        final JSONObject meta = new JSONObject();
        try {
            meta.put("type", getResources().getString(R.string.wifi));
            meta.put("epoch", System.currentTimeMillis());
            meta.put("sysnano", System.nanoTime());
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (final ScanResult result : results) {
            final JSONObject formated = mFormat.format(result, meta);
            if (formated != null) {
                mCache.add(formated);
            }
        }
        if (mDataWriteItvl == null) {
            mDataWriteItvl = WifiLoggerService.DATA_WRITE_ITVL;
            while (!mHandler.postDelayed(mWriteDataTask, mDataWriteItvl));
        }
        
        mLoggerListener.onSampleEvent();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean start() {
        return mSampler.start();
    }

    public boolean stop() {
        // TODO Auto-generated method stub
        return mSampler.stop();
    }

    private void writeData() {
        final JSONArray data = mCache.get();

        if (data.length() > 0) {
            long logEpoch = 0;
            int logLength = 0;
            String logError = "SUCCESS";

            boolean success = true;
            try {
                final long curEpoch = System.currentTimeMillis();
                logEpoch = curEpoch;
                final String timestamp = Long.toString(curEpoch);
                final String fileName = timestamp
                        + getResources().getString(R.string.log_ext);
                final File file = new File(mDataDirectory, fileName);
                final CSVWriter csvOutput = new CSVWriter(new FileWriter(file,
                        true), ',', CSVWriter.NO_QUOTE_CHARACTER);
                
                final File metaFile = new File(mDataDirectory, getResources()
                        .getString(R.string.device_metadata)
                        + getResources().getString(R.string.log_ext));
                boolean metaFileExists = false;
                if (metaFile.exists() && mDataDirectory.isDirectory()) {
                    metaFileExists = true;
                }

                // parse report data
                String make = "make";
                String model = "model";
                String uuid = "uuid";
                String RSSI = "RSSI";
                String frequency = "frequency";
                String BSSID = "BSSID";
                String capability = "capability";
                String SSID = "SSID";
                String epoch = "epoch";

                String[] wifiLogOutput = {epoch, SSID, capability, BSSID,
                        frequency, RSSI};
                String[] devicePropLayout = {uuid, model, make};
                csvOutput.writeNext(wifiLogOutput);

                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject event = data.getJSONObject(i);
                        if (event.getString("type").equals(
                                getResources().getString(R.string.wifi))) {
                            String[] arrayOutputList = {event.optString(epoch),
                                    event.optString(SSID),
                                    event.optString(capability),
                                    event.optString(BSSID),
                                    event.optString(frequency),
                                    event.optString(RSSI)};
                            csvOutput.writeNext(arrayOutputList);
                        } else if (event.getString("type").equals(
                                getResources().getString(R.string.device_info))) {
                            if (!metaFileExists) {
                                CSVWriter csvmetaOutput = new CSVWriter(
                                        new FileWriter(metaFile, true), ',',
                                        CSVWriter.NO_QUOTE_CHARACTER);
                                csvmetaOutput.writeNext(devicePropLayout);
                                String[] devicePropVal = {
                                        event.optString(uuid),
                                        event.optString(model),
                                        event.optString(make)};
                                csvmetaOutput.writeNext(devicePropVal);
                                csvmetaOutput.close();
                            }
                        }
                    }
                    csvOutput.close();
                    logLength = data.length();

                } catch (JSONException e) {
                    logError = e.toString();
                    success = false;
                    e.printStackTrace();
                }
            } catch (final IOException e) {
                logError = e.toString();
                success = false;
                e.printStackTrace();
            }

            if (mLoggerListener != null) {
                String[] sendbackData = {Long.toString(logEpoch),
                        Integer.toString(logLength), logError};
                String sendbackMsg = TextUtils.join(",", sendbackData);
                mLoggerListener.onWritten(sendbackMsg);

                try {
                    final String logfileName = getResources().getString(
                            R.string.log_metadata)
                            + getResources().getString(R.string.log_ext);
                    final File logfile = new File(mDataDirectory, logfileName);
                    final FileOutputStream fOut = new FileOutputStream(logfile, true);
                    final OutputStreamWriter osw = new OutputStreamWriter(fOut);
                    osw.write(sendbackMsg +"\n");
                    osw.flush();
                    osw.close();
                } catch (IOException e) {
                    success = false;
                    e.printStackTrace();
                }
            }

            if (success == false) {
                mCache.addAll(data);
            }

            while (!mHandler.postDelayed(mWriteDataTask, mDataWriteItvl));
        } else {
            mDataWriteItvl = null;
        }
    }
}

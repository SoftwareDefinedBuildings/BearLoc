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
import java.io.FileWriter;
import java.io.IOException;
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

    private static final int DATA_WRITE_ITVL = 6000; // millisecond

    private IBinder mBinder;

    private Handler mHandler;
    private Integer mDataWriteItvl = null; // Millisecond, null if not scheduled

    private WifiLoggerCache mCache;
    private WifiSampler mSampler;
    private WifiLoggerFormat mFormat;
    private WriteListener mWriteListener;

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
        mFormat = new WifiLoggerFormat(this);

        final File sdCard = Environment.getExternalStorageDirectory();
        final String timestamp = Long.toString(System.currentTimeMillis());
        mDataDirectory = new File(sdCard.getAbsolutePath() + "/BearLoc"
                + timestamp);
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

    public void setWriteListener(final WriteListener writeListener) {
        mWriteListener = writeListener;
    }

    private void writeData() {
        final JSONObject report = mFormat.dump(mCache.get());
        mCache.clear();

        if (report.length() > 0) {
            String logMsg = "";
            int logLength = 0;
            String logError = "";
            try {
                final String timestamp = Long.toString(System
                        .currentTimeMillis());
                final String fileName = timestamp + ".csv";
                final File file = new File(mDataDirectory, fileName);
                CSVWriter csvOutput = new CSVWriter(new FileWriter(file, true),
                        ',', CSVWriter.NO_QUOTE_CHARACTER);
                final File metaFile = new File(mDataDirectory, "metadata.txt");
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

                JSONArray pointList;
                JSONObject device;
                try {
                    pointList = (JSONArray) report.get("wifi");
                    for (int i = 0; i < pointList.length(); i++) {
                        JSONObject singlePoint = pointList.getJSONObject(i);
                        String[] arrayOutputList = {
                                singlePoint.optString(epoch),
                                singlePoint.optString(SSID),
                                singlePoint.optString(capability),
                                singlePoint.optString(BSSID),
                                singlePoint.optString(frequency),
                                singlePoint.optString(RSSI)};
                        csvOutput.writeNext(arrayOutputList);
                        logMsg = singlePoint.optString(epoch);
                    }
                    csvOutput.close();
                    logLength = pointList.length();

                    if (!metaFileExists) {
                        CSVWriter csvmetaOutput = new CSVWriter(new FileWriter(
                                metaFile, true), ',',
                                CSVWriter.NO_QUOTE_CHARACTER);
                        csvmetaOutput.writeNext(devicePropLayout);
                        device = (JSONObject) report.get("device");
                        String[] devicePropVal = {device.optString(uuid),
                                device.optString(model), device.optString(make)};
                        csvmetaOutput.writeNext(devicePropVal);
                        csvmetaOutput.close();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (final IOException e) {
                logError = "IO ERROR";
                e.printStackTrace();
            }

            if (mWriteListener != null) {
                String[] sendbackMsg = {logMsg, Integer.toString(logLength),
                        logError};
                mWriteListener.onWritten(TextUtils.join(",", sendbackMsg));
                final String logfileName = "loginfo.csv";
                final File logfile = new File(mDataDirectory, logfileName);
                CSVWriter logcsvOutput;
                try {
                    logcsvOutput = new CSVWriter(new FileWriter(logfile, true),
                            ',', CSVWriter.NO_QUOTE_CHARACTER);
                    logcsvOutput.writeNext(sendbackMsg);
                    logcsvOutput.flush();
                    logcsvOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            mHandler.postDelayed(mWriteDataTask, mDataWriteItvl);
        } else {
            mDataWriteItvl = null;
        }
    }

    @Override
    public void onWifiEvent(List<ScanResult> results) {
        final JSONObject meta = new JSONObject();
        try {
            meta.put("epoch", System.currentTimeMillis());
            meta.put("sysnano", System.nanoTime());
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mCache.put("wifi", results, meta);

        if (mDataWriteItvl == null) {
            mDataWriteItvl = WifiLoggerService.DATA_WRITE_ITVL;
            mHandler.postDelayed(mWriteDataTask, mDataWriteItvl);
        }
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
}

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

package edu.berkeley.bearloc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVWriter;
import edu.berkeley.bearloc.BearLocSampler.OnSampleEventListener;
import edu.berkeley.bearloc.util.DeviceUUID;
import edu.berkeley.bearloc.util.JSONHttpPostTask;
import edu.berkeley.bearloc.util.JSONHttpPostTask.onJSONHttpPostRespondedListener;
import edu.berkeley.bearloc.util.ServerSettings;

public class BearLocService extends Service implements SemLocService,
		OnSampleEventListener {

	private static final int DATA_SEND_ITVL = 6000; // millisecond

	private IBinder mBinder;

	private List<SemLocListener> mListeners;
	private Handler mHandler;
	private Integer mDataSendItvl = null; // Millisecond, null if not scheduled

	private BearLocCache mCache;
	private BearLocSampler mSampler;
	private BearLocFormat mFormat;
	private writeListener mWriteListener;

	private final Runnable mSendLocRequestTask = new Runnable() {
		@Override
		public void run() {
			sendLocRequest();
		}
	};

	private final Runnable mSendDataTask = new Runnable() {
		@Override
		public void run() {
			sendData(mWriteListener);
		}
	};

	public class BearLocBinder extends Binder {
		public BearLocService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return BearLocService.this;
		}
	}

	@Override
	public void onCreate() {
		mBinder = new BearLocBinder();
		mListeners = new LinkedList<SemLocListener>();
		mHandler = new Handler();
		mCache = new BearLocCache(this);
		mSampler = new BearLocSampler(this, this);
		mFormat = new BearLocFormat(this);
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return mBinder;
	}

	@Override
	public boolean localize(final SemLocListener listener) {
		if (listener != null) {
			mListeners.add(listener);
		}

		mSampler.sample();

		// Post localization request after 1500 milliseconds
		// mHandler.postDelayed(mSendLocRequestTask, 1500);

		return true;
	}
	
    public void setWriteListener(final writeListener writeListener){
    	mWriteListener = writeListener;
    }

	private void sendLocRequest() {
		try {
			final String path = "/localize";
			final URL url = getHttpURL(path);

			final JSONObject request = new JSONObject();
			request.put("epoch", System.currentTimeMillis());
			request.put("device", BearLocFormat.getDeviceInfo(this));

			new JSONHttpPostTask(new onJSONHttpPostRespondedListener() {
				@Override
				public void onJSONHttpPostResponded(final JSONObject response) {
					if (response == null) {
						Toast.makeText(BearLocService.this,
								R.string.bearloc_server_no_respond,
								Toast.LENGTH_SHORT).show();
						return;
					}

					for (final SemLocListener listener : mListeners) {
						if (listener != null) {
							try {
								// Generate new copy of response as it calls
								// back to several
								// listeners
								listener.onSemLocInfoReturned(new JSONObject(
										response.toString()));
							} catch (final JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					mListeners.clear();
				}
			}).execute(url, request);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final RejectedExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean report(final JSONObject semloc) {
		final JSONObject meta = new JSONObject();
		try {
			meta.put("epoch", System.currentTimeMillis());
			meta.put("sysnano", System.nanoTime());
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCache.put("semloc", semloc, meta);

		mSampler.sample();

		return true;
	}

	@Override
	public boolean meta(final JSONObject semloc, final MetaListener listener) {
		try {
			final String path = "/meta";
			final URL url = getHttpURL(path);

			final JSONObject request = new JSONObject();
			request.put("semloc", semloc);

			new JSONHttpPostTask(new onJSONHttpPostRespondedListener() {
				@Override
				public void onJSONHttpPostResponded(final JSONObject response) {
					if (response == null) {
						Toast.makeText(BearLocService.this,
								R.string.bearloc_server_no_respond,
								Toast.LENGTH_SHORT).show();
						return;
					}
					if (listener != null) {
						listener.onMetaReturned(response);
					}
				}
			}).execute(url, request);
			return true;
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final RejectedExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private void sendData(final writeListener listener) {
		final JSONObject report = mFormat.dump(mCache.get());
		mCache.clear();

		if (report.length() > 0) {
			try {
				// This will get the SD Card directory and create a folder named
				// MyFiles in it.
				final File sdCard = Environment.getExternalStorageDirectory();
				final File directory = new File(sdCard.getAbsolutePath()
						+ "/BearLoc");
				directory.mkdirs();
				// Now create the file in the above directory and write the
				// contents into it
				final String fileName = Long.toString(System
						.currentTimeMillis()) + ".csv";
				final File file = new File(directory, fileName);
				CSVWriter csvOutput = new CSVWriter(new FileWriter(file, true), ',', CSVWriter.NO_QUOTE_CHARACTER);
				final String deviceName = "metadata.txt";
				final File metaFile = new File(directory, deviceName);
				boolean exist = false;
				if(metaFile.exists() && directory.isDirectory()){
					exist = true;
				}
							
				
				//parse report data
				String make = "make";
				String model = "model"; 
				String uuid = "uuid";
				String RSSI = "RSSI";
				String frequency = "frequency";
				String BSSID = "BSSID";
				String capability = "capability";
				String SSID = "SSID";
				String epoch = "epoch";
				
				String[] arrayOutput = {epoch, SSID, capability, BSSID, frequency, RSSI};
				String[] deviceProp = {uuid, model, make};
				csvOutput.writeNext(arrayOutput);
				
				String written = "";
				JSONArray pointList;
				JSONObject device;
				try {
					pointList = (JSONArray) report.get("wifi");
					for (int i=0;i<pointList.length();i++ ) {
						JSONObject singlePoint = pointList.getJSONObject(i);
						String[] arrayOutputList = {singlePoint.optString(epoch), singlePoint.optString(SSID), singlePoint.optString(capability), singlePoint.optString(BSSID), singlePoint.optString(frequency), singlePoint.optString(RSSI)};
						csvOutput.writeNext(arrayOutputList);
						written = singlePoint.optString(epoch);
					}
					csvOutput.close();
					if (listener != null) {
						listener.onwrittenReturned(written+","+Integer.toString(pointList.length()));
					}
					
					if(!exist){
						CSVWriter csvmetaOutput = new CSVWriter(new FileWriter(metaFile, true), ',', CSVWriter.NO_QUOTE_CHARACTER);
						csvmetaOutput.writeNext(deviceProp);
						device = (JSONObject) report.get("device");
						String[] devicePropVal = {device.optString(uuid), device.optString(model), device.optString(make)};
						csvmetaOutput.writeNext(devicePropVal);
						csvmetaOutput.close();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				Toast.makeText(
						this,
						"Save to New File " + fileName + "\nUUID"
								+ DeviceUUID.getDeviceUUID(this),
						Toast.LENGTH_SHORT).show();
				
			} catch (final IOException e) {
				e.printStackTrace();
			}
			mHandler.postDelayed(mSendDataTask, mDataSendItvl);
		} else {
			mDataSendItvl = null;
		}
	}

	@Override
	public void onSampleEvent(final String type, final Object data) {
		final JSONObject meta = new JSONObject();
		try {
			meta.put("epoch", System.currentTimeMillis());
			meta.put("sysnano", System.nanoTime());
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCache.put(type, data, meta);

		if (mDataSendItvl == null) {
			mDataSendItvl = BearLocService.DATA_SEND_ITVL;
			mHandler.postDelayed(mSendDataTask, mDataSendItvl);
		}
	}

	private URL getHttpURL(final String path) {
		URL url = null;
		try {
			final String serverHost = ServerSettings.getServerAddr(this);
			final int serverPort = ServerSettings.getServerPort(this);
			// TODO handle the exception of using IP address
			final URI uri = new URI("http", null, serverHost, serverPort, path,
					null, null);
			url = uri.toURL();
		} catch (final URISyntaxException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.bearloc_url_error, Toast.LENGTH_SHORT)
					.show();
		} catch (final MalformedURLException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.bearloc_url_error, Toast.LENGTH_SHORT)
					.show();
		}

		return url;
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
}

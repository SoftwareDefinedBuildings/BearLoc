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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.BearLocSampler.OnSampleEventListener;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class BearLocService extends Service implements SemLocService,
    OnSampleEventListener {

  private static final int DATA_SEND_ITVL = 300; // millisecond

  private IBinder mBinder;

  private List<SemLocListener> mListeners;
  private Handler mHandler;
  private Integer mDataSendItvl = null; // Millisecond, null if not scheduled

  private BearLocCache mCache;
  private BearLocSampler mSampler;
  private BearLocFormat mFormat;

  private final Runnable mSendLocRequestTask = new Runnable() {
    @Override
    public void run() {
      sendLocRequest();
    }
  };

  private final Runnable mSendDataTask = new Runnable() {
    @Override
    public void run() {
      sendData();
    }
  };

  private static interface onHttpPostRespondedListener {
    void onHttpPostResponded(JSONObject response);
  }

  public class BearLocBinder extends Binder {
    public BearLocService getService() {
      // Return this instance of LocalService so clients can call public methods
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
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

  @Override
  public boolean localize(final SemLocListener listener) {
    if (listener != null) {
      mListeners.add(listener);
    }

    mSampler.sample();

    // Post localization request after 1500 milliseconds
    mHandler.postDelayed(mSendLocRequestTask, 1500);

    return true;
  }

  private void sendLocRequest() {
    try {
      final String path = "/localize";
      final URL url = getHttpURL(this, path);

      final JSONObject request = new JSONObject();
      request.put("epoch", System.currentTimeMillis());

      new BearLocHttpPostTask(new onHttpPostRespondedListener() {
        @Override
        public void onHttpPostResponded(JSONObject response) {
          if (response == null) {
            return;
          }

          for (SemLocListener listener : mListeners) {
            if (listener != null) {
              try {
                // Generate new copy of response as it calls back to several
                // listeners
                listener.onSemLocInfoReturned(new JSONObject(response
                    .toString()));
              } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }
          }
          mListeners.clear();
        }
      }).execute(url, request.toString());
    } catch (JSONException e) {
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
    } catch (JSONException e) {
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
      final URL url = getHttpURL(this, path);

      final JSONObject request = new JSONObject();
      request.put("semloc", semloc);

      new BearLocHttpPostTask(new onHttpPostRespondedListener() {
        @Override
        public void onHttpPostResponded(JSONObject response) {
          if (response == null) {
            return;
          }

          if (listener != null) {
            listener.onMetaReturned(response);
          }
        }
      }).execute(url, request.toString());

      return true;
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return false;
  }

  private void sendData() {
    final String path = "/report";
    final URL url = getHttpURL(this, path);

    final JSONObject report = mFormat.dump(mCache.get());
    mCache.clear();

    if (report.length() > 0) {
      new BearLocHttpPostTask(null).execute(url, report.toString());
      mHandler.postDelayed(mSendDataTask, mDataSendItvl);
    } else {
      mDataSendItvl = null;
    }
  }

  @Override
  public void onSampleEvent(String type, Object data) {
    final JSONObject meta = new JSONObject();
    try {
      meta.put("epoch", System.currentTimeMillis());
      meta.put("sysnano", System.nanoTime());
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    mCache.put(type, data, meta);

    if (mDataSendItvl == null) {
      mDataSendItvl = DATA_SEND_ITVL;
      mHandler.postDelayed(mSendDataTask, mDataSendItvl);
    }
  }

  private static URL getHttpURL(Context context, String path) {
    URL url = null;
    try {
      String serverHost = PreferenceManager
          .getDefaultSharedPreferences(context).getString("pref_server_addr",
              context.getString(R.string.default_server_addr));
      int serverPort = Integer.parseInt(PreferenceManager
          .getDefaultSharedPreferences(context).getString("pref_server_port",
              context.getString(R.string.default_server_port)));
      // TODO handle the exception of using IP address
      final URI uri = new URI("http", null, serverHost, serverPort, path,
          null, null);
      url = uri.toURL();
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return url;
  }

  // BearLoc HTTP Post Task posts with JSON Object and gets JSON Object returned
  private static class BearLocHttpPostTask extends
      AsyncTask<Object, Void, JSONObject> {

    private onHttpPostRespondedListener listener;

    public BearLocHttpPostTask(onHttpPostRespondedListener listener) {
      this.listener = listener;
    }

    private InputStream httpPost(final HttpURLConnection connection,
        final URL url, final String entity) throws IOException {
      final int contentLength = entity.getBytes().length;
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Content-Length",
          Integer.toString(contentLength));
      connection.setFixedLengthStreamingMode(contentLength);
      connection.setDoInput(true);
      connection.setDoOutput(true);

      final OutputStream out = new BufferedOutputStream(
          connection.getOutputStream());
      out.write(entity.getBytes());
      out.flush();
      out.close();

      final InputStream in = new BufferedInputStream(
          connection.getInputStream());

      return in;
    }

    @Override
    protected JSONObject doInBackground(Object... params) {
      final URL url = (URL) params[0];
      String entity = (String) params[1];

      // TODO reuse the connection
      HttpURLConnection connection = null;
      try {
        connection = (HttpURLConnection) url.openConnection();
        final InputStream in = httpPost(connection, url, entity);

        final BufferedReader reader = new BufferedReader(new InputStreamReader(
            in));

        String line;
        final StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
          sb.append(line + '\n');
        }
        reader.close();

        return new JSONObject(sb.toString());
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        if (connection != null) {
          connection.disconnect();
        }
      }

      return null;
    }

    @Override
    protected void onPostExecute(JSONObject response) {
      if (!isCancelled() && listener != null) {
        listener.onHttpPostResponded(response);
      }
    }
  }
}
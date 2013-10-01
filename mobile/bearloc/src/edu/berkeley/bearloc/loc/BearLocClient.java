package edu.berkeley.bearloc.loc;

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
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.SettingsActivity;
import edu.berkeley.bearloc.loc.BearLocSampleAggregator.OnSampleDoneListener;
import android.content.Context;
import android.os.AsyncTask;

public class BearLocClient implements LocClient, OnSampleDoneListener {

  private Context mContext;
  private LocClientListener mListener;

  private BearLocSampleAggregator mAggr;
  private BearLocCache mCache;

  private static interface onHttpPostRespondedListener {
    void onHttpPostResponded(JSONObject response);
  }

  public BearLocClient(Context context, LocClientListener listener) {
    mContext = context;
    mListener = listener;
    mCache = new BearLocCache(mContext);
    mAggr = new BearLocSampleAggregator(mContext, this, mCache);
  }

  @Override
  public boolean localize() {
    final String path = "/localize";
    URL url = getHttpURL(mContext, path);
    if (url == null) {
      return false;
    }

    final JSONObject request = new JSONObject();

    // TODO get sensor data and send httppost

    new BearLocHttpPostTask(new onHttpPostRespondedListener() {
      @Override
      public void onHttpPostResponded(JSONObject response) {
        if (mListener != null) {
          mListener.onLocationReturned(response);
        }
      }
    }).execute(url, request.toString());

    return true;
  }

  @Override
  public void reportSemLoc(final JSONObject semloc) {
    final String type = "semloc";
    final Long epoch = System.currentTimeMillis();
    final Iterator<?> dataIter = semloc.keys();
    while (dataIter.hasNext()) {
      final String sem = (String) dataIter.next();
      final JSONObject event = BearLocFormat.convert(type, semloc, epoch, sem);
      mCache.add(type, event);
    }

    report(new onHttpPostRespondedListener() {
      @Override
      public void onHttpPostResponded(JSONObject response) {
        if (mListener != null) {
          mListener.onReportDone(response);
        }
      }
    });

    mAggr.sample();
  }

  @Override
  public void onSampleDone() {
    report(null);
  }

  // async
  private void report(final onHttpPostRespondedListener listener) {
    final String path = "/report";
    URL url = getHttpURL(mContext, path);
    if (url == null) {
      return;
    }

    final JSONObject report = mCache.get();
    mCache.clear();

    new BearLocHttpPostTask(listener).execute(url, report.toString());
  }

  private static URL getHttpURL(Context context, String path) {
    // TODO check and remove "http://"
    final String host = SettingsActivity.getServerAddr(context);
    final int port = SettingsActivity.getServerPort(context);

    URL url = null;
    try {
      final URI uri = new URI("http", null, host, port, path, null, null);
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

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
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.SettingsActivity;
import edu.berkeley.bearloc.loc.BearLocSampler.OnSampleEventListener;
import android.content.Context;
import android.os.AsyncTask;

public class BearLocClient implements LocClient, OnSampleEventListener {

  private final Context mContext;
  private final LocClientListener mListener;

  private final BearLocCache mCache;
  private final BearLocSampler mSampler;
  private final BearLocFormat mFormat;

  private static interface onHttpPostRespondedListener {
    void onHttpPostResponded(JSONObject response);
  }

  public BearLocClient(Context context, LocClientListener listener) {
    mContext = context;
    mListener = listener;
    mCache = new BearLocCache(mContext);
    mSampler = new BearLocSampler(mContext, this);
    mFormat = new BearLocFormat(mContext);
  }

  @Override
  public boolean localize() {

    sendLocRequest();

    mSampler.sample();

    return true;
  }

  private void sendLocRequest() {
    final String path = "/localize";
    final URL url = getHttpURL(mContext, path);

    final JSONObject request = new JSONObject();

    new BearLocHttpPostTask(new onHttpPostRespondedListener() {
      @Override
      public void onHttpPostResponded(JSONObject response) {
        if (mListener != null) {
          mListener.onLocationReturned(response);
        }
      }
    }).execute(url, request.toString());
  }

  @Override
  public boolean report(final JSONObject semloc) {
    mCache.put("semloc", semloc);

    mSampler.sample();

    return true;
  }

  // async
  private void sendData() {
    final String path = "/report";
    final URL url = getHttpURL(mContext, path);

    final JSONObject report = mFormat.dump(mCache.get());
    mCache.clear();

    new BearLocHttpPostTask(null).execute(url, report.toString());
  }

  @Override
  public void onSampleEvent(String type, Object data) {
    mCache.put(type, data);
  }

  @Override
  public void onSampleDone() {
    // TODO send data in between the data collection
    sendData();
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

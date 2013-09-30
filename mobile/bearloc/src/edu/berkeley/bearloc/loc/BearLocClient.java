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
import android.content.Context;
import android.os.AsyncTask;

public class BearLocClient implements LocClient {

  private Context mContext;

  private LocClientListener mListener;

  private BearLocCache mCache;

  public static interface LocClientListener {
    public abstract void onLocationReturned(JSONObject locInfo);

    public abstract void onReportDone(JSONObject response);
  }

  private static interface OnBearLocHttpPostResponded {
    void onHttpResponded(JSONObject response);
  }

  public BearLocClient(Context context) {
    mContext = context;

    mCache = new BearLocCache(mContext);
  }

  public void setOnDataReturnedListener(LocClientListener listener) {
    mListener = listener;
  }

  private class OnLocationReturned implements OnBearLocHttpPostResponded {
    @Override
    public void onHttpResponded(JSONObject locInfo) {
      if (mListener != null) {
        mListener.onLocationReturned(locInfo);
      }
    }
  }

  @Override
  public boolean localize() {
    final String path = "/localize";
    URL url = getHttpURL(path);
    if (url == null) {
      return false;
    }

    final JSONObject request = new JSONObject();

    // TODO get sensor data and send httppost

    new BearLocHttpPostTask(new OnLocationReturned()).execute(url,
        request.toString());

    return true;
  }

  private class OnReportDone implements OnBearLocHttpPostResponded {
    @Override
    public void onHttpResponded(JSONObject response) {
      if (mListener != null) {
        mListener.onReportDone(response);
      }
    }
  }

  @Override
  public void report(final JSONObject semloc) {
    // get semloc Event List
    mCache.add("semloc", semloc);
    report();
  }

  private void report() {
    final String path = "/report";
    URL url = getHttpURL(path);
    if (url == null) {
      return;
    }

    final JSONObject report = mCache.getAll();
    mCache.clear();

    new BearLocHttpPostTask(new OnReportDone()).execute(url, report.toString());
  }

  private URL getHttpURL(String path) {
    // TODO check and remove "http://"
    final String host = SettingsActivity.getServerAddr(mContext);
    final int port = SettingsActivity.getServerPort(mContext);

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

    private OnBearLocHttpPostResponded listener;

    public BearLocHttpPostTask(OnBearLocHttpPostResponded listener) {
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
      if (!isCancelled()) {
        listener.onHttpResponded(response);
      }
    }
  }
}

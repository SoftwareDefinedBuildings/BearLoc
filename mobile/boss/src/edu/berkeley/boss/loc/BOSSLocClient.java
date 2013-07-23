package edu.berkeley.boss.loc;

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
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.boss.SettingsActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class BOSSLocClient implements LocClient {

  private Context mContext;

  private LocClientListener mListener;

  public static interface LocClientListener {
    public abstract void onLocationReturned(final JSONObject locInfo);

    public abstract void onReportDone(final boolean success);

    public abstract void onMetadataReturned(final JSONObject metadata);

    public abstract void onMapReturned(final Bitmap bitmap);
  }

  private static interface OnBOSSHttpPostResponded {
    void onHttpResponded(JSONObject response);
  }

  public BOSSLocClient(Context context) {
    mContext = context;
  }

  public void setOnDataReturnedListener(LocClientListener listener) {
    mListener = listener;
  }

  private class OnLocationReturned implements OnBOSSHttpPostResponded {
    @Override
    public void onHttpResponded(JSONObject locInfo) {
      if (mListener != null) {
        if (locInfo != null) {
          mListener.onLocationReturned(locInfo);
        } else {
          mListener.onLocationReturned(null);
        }
      }
    }
  }

  @Override
  public boolean getLocation(JSONObject sensorData) {
    final String path = "/localize";
    URL url = getHttpURL(path);
    if (url == null) {
      return false;
    }

    try {
      final JSONObject locRequest = new JSONObject();
      locRequest.put("type", "localize");
      locRequest.put("sensor data", sensorData);
      
      final Long timestamp = System.currentTimeMillis();
      locRequest.put("timestamp", timestamp);

      new BOSSHttpPostTask(new OnLocationReturned()).execute(url,
          locRequest.toString());
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  private class OnReportDone implements OnBOSSHttpPostResponded {
    @Override
    public void onHttpResponded(JSONObject response) {
      if (mListener != null) {
        if (response != null) {
          try {
            mListener.onReportDone(response.getBoolean("result"));
          } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        } else {
          mListener.onReportDone(false);
        }
      }
    }
  }

  @Override
  public boolean reportLocation(JSONObject sensorData, JSONObject loc) {
    final String path = "/localize/report";
    URL url = getHttpURL(path);
    if (url == null) {
      return false;
    }

    try {
      final JSONObject reportRequest = new JSONObject();
      reportRequest.put("type", "report");
      reportRequest.put("sensor data", sensorData);
      reportRequest.put("location", loc);
      
      final Long timestamp = System.currentTimeMillis();
      reportRequest.put("timestamp", timestamp);

      new BOSSHttpPostTask(new OnReportDone()).execute(url,
          reportRequest.toString());
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  private class onMetadataReturned implements OnBOSSHttpPostResponded {
    @Override
    public void onHttpResponded(JSONObject metadata) {
      if (mListener != null) {
        if (metadata != null) {
          mListener.onMetadataReturned(metadata);
        } else {
          mListener.onMetadataReturned(null);
        }
      }
    }
  }

  @Override
  public boolean getMetadata(JSONArray target) {
    final String path = "/metadata";
    URL url = getHttpURL(path);
    if (url == null) {
      return false;
    }

    try {
      final JSONObject metadataRequest = new JSONObject();
      metadataRequest.put("type", "metadata");
      metadataRequest.put("target", target);
      
      final Long timestamp = System.currentTimeMillis();
      metadataRequest.put("timestamp", timestamp);

      new BOSSHttpPostTask(new onMetadataReturned()).execute(url,
          metadataRequest.toString());
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  @Override
  public boolean getMap(JSONObject metadata) {
    String path;
    try {
      path = "/metadata/data"
          + metadata.getJSONObject("views").getJSONObject("floorplan")
              .getString("image");
    } catch (JSONException e) {
      return false;
    }

    URL url = getHttpURL(path);
    if (url == null) {
      return false;
    }

    new BitmapDownloadTask(mListener).execute(url);
    return true;
  }

  // BOSS HTTP Post Task posts with JSON Object and gets JSON Object returned
  private static class BOSSHttpPostTask extends
      AsyncTask<Object, Void, JSONObject> {

    private OnBOSSHttpPostResponded listener;

    public BOSSHttpPostTask(OnBOSSHttpPostResponded listener) {
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
      out.write(entity.getBytes(Charset.forName("UTF-8")));
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

  private static class BitmapDownloadTask extends AsyncTask<URL, Void, Bitmap> {

    private LocClientListener listener;

    public BitmapDownloadTask(LocClientListener listener) {
      this.listener = listener;
    }

    private InputStream httpGet(final HttpURLConnection connection,
        final URL url) throws IOException {
      final InputStream in = new BufferedInputStream(
          connection.getInputStream());

      return in;
    }

    @Override
    protected Bitmap doInBackground(URL... params) {
      final URL url = params[0];

      HttpURLConnection connection = null;
      try {
        connection = (HttpURLConnection) url.openConnection();
        final InputStream in = httpGet(connection, url);
        final Bitmap bitmap = BitmapFactory.decodeStream(in);

        return bitmap;
      } catch (IOException e) {
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
    protected void onPostExecute(Bitmap bitmap) {
      if (!isCancelled()) {
        listener.onMapReturned(bitmap);
      }
    }
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
}

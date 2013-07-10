package edu.berkeley.boss;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

  private static interface OnHttpResponded {
    void onHttpResponded(String response);
  }

  public BOSSLocClient(Context context) {
    mContext = context;
  }

  public void setOnDataReturnedListener(LocClientListener listener) {
    mListener = listener;
  }

  private class OnLocationReturned implements OnHttpResponded {
    @Override
    public void onHttpResponded(String locInfoStr) {
      if (mListener != null) {
        if (locInfoStr != null) {
          try {
            JSONObject locInfo = new JSONObject(locInfoStr);

            mListener.onLocationReturned(locInfo);
          } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
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
      final JSONObject locRequst = new JSONObject();
      locRequst.put("type", "localize");
      locRequst.put("sensor data", sensorData);

      new HttpRequestTask(new OnLocationReturned()).execute(url,
          locRequst.toString());
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  private class OnReportDone implements OnHttpResponded {
    @Override
    public void onHttpResponded(String response) {
      if (mListener != null) {
        if (response != null) {
          mListener.onReportDone(true);
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
      final JSONObject reportRequst = new JSONObject();
      reportRequst.put("type", "report");
      reportRequst.put("sensor data", sensorData);
      reportRequst.put("location", loc);

      new HttpRequestTask(new OnReportDone()).execute(url,
          reportRequst.toString());
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  private class onMetadataReturned implements OnHttpResponded {
    @Override
    public void onHttpResponded(String metadataStr) {
      if (mListener != null) {
        if (metadataStr != null) {
          try {
            JSONObject metadata = new JSONObject(metadataStr);

            mListener.onMetadataReturned(metadata);
          } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
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
      final JSONObject metadataRequst = new JSONObject();
      metadataRequst.put("type", "metadata");
      metadataRequst.put("target", target);

      new HttpRequestTask(new onMetadataReturned()).execute(url,
          metadataRequst.toString());
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  private class onMapReturned implements OnHttpResponded {
    @Override
    public void onHttpResponded(String mapStr) {
      if (mListener != null) {
        if (mapStr != null) {
          final byte[] mapBytes = mapStr.getBytes();
          Bitmap bitmap = BitmapFactory.decodeByteArray(mapBytes, 0,
              mapBytes.length);
          mListener.onMapReturned(bitmap);
        } else {
          mListener.onMapReturned(null);
        }
      }
    }
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

    new HttpRequestTask(new onMapReturned()).execute(url);
    return true;
  }

  private static class HttpRequestTask extends AsyncTask<Object, Void, String> {

    private OnHttpResponded listener;

    public HttpRequestTask(OnHttpResponded listener) {
      this.listener = listener;
    }

    private InputStream httpGet(final HttpURLConnection connection,
        final URL url) throws IOException {
      connection.setRequestMethod("GET");
      connection.setDoInput(true);

      final InputStream in = new BufferedInputStream(
          connection.getInputStream());

      return in;
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
    protected String doInBackground(Object... params) {
      final URL url = (URL) params[0];
      String entity = null;
      if (params.length == 2) {
        entity = (String) params[1];
      }

      HttpURLConnection connection = null;
      try {
        connection = (HttpURLConnection) url.openConnection();
        InputStream in;

        if (entity == null) {
          in = httpGet(connection, url);
        } else {
          in = httpPost(connection, url, entity);
        }

        final BufferedReader rd = new BufferedReader(new InputStreamReader(in));

        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = rd.readLine()) != null) {
          sb.append(line + '\n');
        }
        rd.close();

        return sb.toString();
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
    protected void onPostExecute(String response) {
      if (!isCancelled()) {
        listener.onHttpResponded(response);
      }
    }
  }

  private URL getHttpURL(String path) {
    // TODO check and remove "http://"
    final String host = SettingsActivity.getServerAddr(mContext);
    final int port = SettingsActivity.getServerPort(mContext);

    URL url;
    try {
      url = new URL("http", host, port, path);
    } catch (Exception e) {
      return null;
    }

    return url;
  }
}

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.bearloc.BearLocActivity;
import edu.berkeley.bearloc.SettingsActivity;
import edu.berkeley.bearloc.util.DeviceUUIDFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

public class BearLocClient implements LocClient {

  private Context mContext;

  private LocClientListener mListener;

  private DeviceUUIDFactory mDeviceUUID;

  public static interface LocClientListener {
    public abstract void onLocationReturned(JSONObject locInfo);

    public abstract void onReportDone(JSONObject response);
  }

  private static interface OnBearLocHttpPostResponded {
    void onHttpResponded(JSONObject response);
  }

  public BearLocClient(Context context) {
    mContext = context;

    mDeviceUUID = new DeviceUUIDFactory(mContext);
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
  public boolean report(final JSONObject semloc) {
    JSONArray semlocEvents = getSemLocEventList(semloc);
    Map<String, JSONArray> datamap = new HashMap<String, JSONArray>();
    datamap.put("semloc", semlocEvents);
    return report(datamap);
  }

  private boolean report(final Map<String, JSONArray> datamap) {
    final String path = "/report";
    URL url = getHttpURL(path);
    if (url == null) {
      return false;
    }

    final JSONObject request = new JSONObject();

    // add "device" and data
    try {
      final JSONObject device = new JSONObject();
      device.put("uuid", mDeviceUUID.getDeviceUUID().toString());

      String make = Build.MANUFACTURER;
      String model = Build.MODEL;
      device.put("make", make);
      device.put("model", model);

      request.put("device", device);

      // TODO add sensor meta

      Iterator<Entry<String, JSONArray>> it = datamap.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<String, JSONArray> entry = (Map.Entry<String, JSONArray>) it
            .next();
        String type = entry.getKey();
        JSONArray data = entry.getValue();
        request.put(type, data);
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    new BearLocHttpPostTask(new OnReportDone())
        .execute(url, request.toString());

    return true;
  }

  private static JSONArray getSemLocEventList(final JSONObject loc) {
    final JSONArray events = new JSONArray();
    final Long epoch = System.currentTimeMillis();

    try {
      final Iterator<?> dataIter = loc.keys();
      while (dataIter.hasNext()) {
        final JSONObject event = new JSONObject();
        final String sem = (String) dataIter.next();

        event.put("epoch", epoch);
        event.put("semantic", sem);
        event.put("location", loc.getString(sem));

        events.put(event);
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return events;
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

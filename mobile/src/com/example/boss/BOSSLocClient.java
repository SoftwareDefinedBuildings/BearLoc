package com.example.boss;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.boss.Sensor.SensorDataPack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

public class BOSSLocClient implements LocClient {

  private Context context;

  private LocaliztionTask localiztionTask;
  private MetadataDownloadTask metadataDownloadTask;
  private MapDownloadTask mapDownloadTask;
  private WeakReference<LocClientListener> listenerRef;

  public static interface LocClientListener {
    public abstract void onLocationReturned(JSONObject loc);

    public abstract void onMetadataReturned(JSONObject metadata);

    public abstract void onMapReturned(Bitmap bitmap);
  }

  public BOSSLocClient(Context context) {
    this.context = context;
  }

  public void setOnDataReturnedListener(LocClientListener listener) {
    listenerRef = new WeakReference<LocClientListener>(listener);
  }

  @Override
  public boolean getLocation(SensorDataPack sensorDataPack) {
    if (localiztionTask != null) {
      localiztionTask.cancel(true);
    }
    localiztionTask = new LocaliztionTask();

    // TODO check and remove "http://"
    final String host = SettingsActivity.getServerAddr(context);
    final int port = SettingsActivity.getServerPort(context);
    final String service = "/localize";
    URI uri;
    try {
      uri = new URI("http", null, host, port, service, null, null);
    } catch (Exception e) {
      return false;
    }

    localiztionTask.execute(sensorDataPack, uri);
    return true;
  }

  @Override
  public boolean getMetadata(JSONObject loc, String targetSem) {
    if (metadataDownloadTask != null) {
      metadataDownloadTask.cancel(true);
    }
    metadataDownloadTask = new MetadataDownloadTask();

    // TODO check and remove "http://"
    final String host = SettingsActivity.getServerAddr(context);
    final int port = SettingsActivity.getServerPort(context);
    final String service = "/metadata";
    URI uri;
    try {
      uri = new URI("http", null, host, port, service, null, null);
    } catch (Exception e) {
      return false;
    }

    metadataDownloadTask.execute(loc, targetSem, uri);
    return true;
  }

  @Override
  public boolean getMap(JSONObject metadata) {
    if (mapDownloadTask != null) {
      mapDownloadTask.cancel(true);
    }
    mapDownloadTask = new MapDownloadTask();

    // TODO check and remove "http://"
    final String host = SettingsActivity.getServerAddr(context);
    final int port = SettingsActivity.getServerPort(context);
    String path;
    try {
      path = "/metadata/data"
          + metadata.getJSONObject("views").getJSONObject("floorplan")
              .getString("image");
    } catch (JSONException e) {
      return false;
    }

    URI uri;
    try {
      uri = new URI("http", null, host, port, path, null, null);
    } catch (Exception e) {
      return false;
    }

    mapDownloadTask.execute(uri);
    return true;
  }

  private class LocaliztionTask extends AsyncTask<Object, Void, JSONObject> {

    @Override
    protected JSONObject doInBackground(Object... params) {
      // TODO use sensor data
      @SuppressWarnings("unused")
      final SensorDataPack sensorDataPack = (SensorDataPack) params[0];
      final URI uri = (URI) params[1];
      final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
      final HttpPost postRequest = new HttpPost(uri);

      try {
        StringEntity se;

        // TODO encode all sensor data
        se = new StringEntity("{}");

        postRequest.setEntity(se);
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Content-type", "application/json");

        HttpResponse response = client.execute(postRequest);
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
          return null;
        }

        final HttpEntity entity = response.getEntity();
        if (entity != null) {
          String locInfoStr = EntityUtils.toString(entity);
          JSONObject locInfo = new JSONObject(locInfoStr);

          return locInfo;
        }
      } catch (IOException e) {
        postRequest.abort();
      } catch (IllegalStateException e) {
        postRequest.abort();
      } catch (Exception e) {
        postRequest.abort();
      } finally {
        client.close();
      }

      return null;
    }

    @Override
    protected void onPostExecute(JSONObject loc) {
      if (!isCancelled()) {
        if (listenerRef != null) {
          LocClientListener listener = listenerRef.get();
          if (listener != null) {
            listener.onLocationReturned(loc);
          }
        }
      }

      localiztionTask = null;
    }
  }

  private class MetadataDownloadTask extends
      AsyncTask<Object, Void, JSONObject> {

    @Override
    protected JSONObject doInBackground(Object... params) {
      final JSONObject loc = (JSONObject) params[0];
      final String tartgetSem = (String) params[1];
      final URI uri = (URI) params[2];
      final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
      final HttpPost postRequest = new HttpPost(uri);

      StringEntity se;
      try {
        JSONObject metadataRequst = new JSONObject();
        metadataRequst.put("type", "metadata");
        metadataRequst.put("location", loc);
        metadataRequst.put("targetsem", tartgetSem);
        se = new StringEntity(metadataRequst.toString());

        postRequest.setEntity(se);
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Content-type", "application/json");

        HttpResponse response = client.execute(postRequest);
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
          return null;
        }

        final HttpEntity entity = response.getEntity();
        if (entity != null) {
          String metadataStr = EntityUtils.toString(entity);
          JSONObject metadata = new JSONObject(metadataStr);

          return metadata;
        }
      } catch (IOException e) {
        postRequest.abort();
      } catch (IllegalStateException e) {
        postRequest.abort();
      } catch (Exception e) {
        postRequest.abort();
      } finally {
        client.close();
      }

      return null;
    }

    @Override
    protected void onPostExecute(JSONObject metadata) {
      if (!isCancelled()) {
        if (listenerRef != null) {
          LocClientListener listener = listenerRef.get();
          if (listener != null) {
            listener.onMetadataReturned(metadata);
          }
        }
      }

      localiztionTask = null;
    }
  }

  private class MapDownloadTask extends AsyncTask<URI, Void, Bitmap> {

    @Override
    protected Bitmap doInBackground(URI... params) {
      final URI uri = params[0];
      final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
      final HttpGet getRequest = new HttpGet(uri);

      try {
        HttpResponse response = client.execute(getRequest);
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
          return null;
        }

        final HttpEntity entity = response.getEntity();
        if (entity != null) {
          InputStream inputStream = null;
          try {
            inputStream = entity.getContent();
            // TODO: Bug on slow connections, fixed in future release.
            return BitmapFactory.decodeStream(inputStream);
          } finally {
            if (inputStream != null) {
              inputStream.close();
            }
            entity.consumeContent();
          }
        }
      } catch (IOException e) {
        getRequest.abort();
      } catch (IllegalStateException e) {
        getRequest.abort();
      } catch (Exception e) {
        getRequest.abort();
      } finally {
        client.close();
      }

      return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      if (!isCancelled()) {
        if (listenerRef != null) {
          LocClientListener listener = listenerRef.get();
          if (listener != null) {
            listener.onMapReturned(bitmap);
          }
        }
      }

      mapDownloadTask = null;
    }
  }
}

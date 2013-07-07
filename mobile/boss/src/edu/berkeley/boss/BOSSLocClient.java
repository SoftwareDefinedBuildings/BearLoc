package edu.berkeley.boss;

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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

public class BOSSLocClient implements LocClient {

  private Context mContext;

  private LocaliztionTask mLocaliztionTask;
  private MetadataDownloadTask mMetadataDownloadTask;
  private MapDownloadTask mMapDownloadTask;
  private WeakReference<LocClientListener> mListenerRef;

  public static interface LocClientListener {
    public abstract void onLocationReturned(JSONObject loc);

    public abstract void onMetadataReturned(JSONObject metadata);

    public abstract void onMapReturned(Bitmap bitmap);
  }

  public BOSSLocClient(Context context) {
    mContext = context;
  }

  public void setOnDataReturnedListener(LocClientListener listener) {
    mListenerRef = new WeakReference<LocClientListener>(listener);
  }

  @Override
  public boolean getLocation(JSONObject sensorData) {
    if (mLocaliztionTask != null) {
      mLocaliztionTask.cancel(true);
    }
    mLocaliztionTask = new LocaliztionTask();

    // TODO check and remove "http://"
    final String host = SettingsActivity.getServerAddr(mContext);
    final int port = SettingsActivity.getServerPort(mContext);
    final String service = "/localize";
    URI uri;
    try {
      uri = new URI("http", null, host, port, service, null, null);
    } catch (Exception e) {
      return false;
    }

    mLocaliztionTask.execute(sensorData, uri);
    return true;
  }

  @Override
  public boolean getMetadata(JSONObject loc, String targetSem) {
    if (mMetadataDownloadTask != null) {
      mMetadataDownloadTask.cancel(true);
    }
    mMetadataDownloadTask = new MetadataDownloadTask();

    // TODO check and remove "http://"
    final String host = SettingsActivity.getServerAddr(mContext);
    final int port = SettingsActivity.getServerPort(mContext);
    final String service = "/metadata";
    URI uri;
    try {
      uri = new URI("http", null, host, port, service, null, null);
    } catch (Exception e) {
      return false;
    }

    mMetadataDownloadTask.execute(loc, targetSem, uri);
    return true;
  }

  @Override
  public boolean getMap(JSONObject metadata) {
    if (mMapDownloadTask != null) {
      mMapDownloadTask.cancel(true);
    }
    mMapDownloadTask = new MapDownloadTask();

    // TODO check and remove "http://"
    final String host = SettingsActivity.getServerAddr(mContext);
    final int port = SettingsActivity.getServerPort(mContext);
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

    mMapDownloadTask.execute(uri);
    return true;
  }

  private class LocaliztionTask extends AsyncTask<Object, Void, JSONObject> {

    @Override
    protected JSONObject doInBackground(Object... params) {
      final JSONObject sensorData = (JSONObject) params[0];
      final URI uri = (URI) params[1];
      final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
      final HttpPost postRequest = new HttpPost(uri);

      try {
        final JSONObject locRequst = new JSONObject();
        locRequst.put("type", "localize");
        locRequst.put("sensor data", sensorData);
        
        final StringEntity se = new StringEntity(locRequst.toString());

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
        if (mListenerRef != null) {
          LocClientListener listener = mListenerRef.get();
          if (listener != null) {
            listener.onLocationReturned(loc);
          }
        }
      }

      mLocaliztionTask = null;
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

      try {
        final JSONObject metadataRequst = new JSONObject();
        metadataRequst.put("type", "metadata");
        metadataRequst.put("location", loc);
        metadataRequst.put("targetsem", tartgetSem);
        
        final StringEntity se = new StringEntity(metadataRequst.toString());

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
        if (mListenerRef != null) {
          LocClientListener listener = mListenerRef.get();
          if (listener != null) {
            listener.onMetadataReturned(metadata);
          }
        }
      }

      mLocaliztionTask = null;
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
        if (mListenerRef != null) {
          LocClientListener listener = mListenerRef.get();
          if (listener != null) {
            listener.onMapReturned(bitmap);
          }
        }
      }

      mMapDownloadTask = null;
    }
  }
}

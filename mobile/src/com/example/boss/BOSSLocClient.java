package com.example.boss;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

public class BOSSLocClient implements LocClient {

  private MapDownloadTask mapDownloadTask;
  private WeakReference<LocClientListener> listenerRef;

  public static interface LocClientListener {
    public abstract void onLocationReturned(/* Location loc */);

    public abstract void onMapReturned(Bitmap bitmap);

    public abstract void onSemanticReturned(/* Semantic sem */);

    public abstract void onMetadataReturned(/* Metadata mdata */);
  }

  public void setOnDataReturnedListener(LocClientListener listener) {
    listenerRef = new WeakReference<LocClientListener>(listener);
  }

  @Override
  public boolean getLocation(/* all sensor data (GPS,WiFi,Acoustic,...) */) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean getMap(/* Location loc */) {
    if (mapDownloadTask != null) {
      mapDownloadTask.cancel(true);
    }
    mapDownloadTask = new MapDownloadTask();
    mapDownloadTask
        .execute("http://wpcontent.answers.com/wikipedia/commons/0/02/US_Census_regional_map.gif");
    return true;
  }

  @Override
  public boolean getSemantic() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean getMetadata(String semantic) {
    // TODO Auto-generated method stub
    return false;
  }

  private class MapDownloadTask extends AsyncTask<String, Void, Bitmap> {

    @Override
    protected Bitmap doInBackground(String... params) {
      final String url = params[0];
      final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
      final HttpGet getRequest = new HttpGet(url);

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

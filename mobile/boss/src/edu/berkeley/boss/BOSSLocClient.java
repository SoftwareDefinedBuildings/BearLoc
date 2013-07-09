package edu.berkeley.boss;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.concurrent.Callable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

public class BOSSLocClient implements LocClient {

  private Context mContext;

  private LocClientListener mListener;

  public static interface LocClientListener {
    public abstract void onLocationReturned(JSONObject locInfo);

    public abstract void onReportDone(boolean error);

    public abstract void onMetadataReturned(JSONObject metadata);

    public abstract void onMapReturned(Bitmap bitmap);
  }

  private static interface OnHttpResponded {
    void onHttpResponded(HttpResponse response);
  }

  public BOSSLocClient(Context context) {
    mContext = context;
  }

  public void setOnDataReturnedListener(LocClientListener listener) {
    mListener = listener;
  }

  private class OnLocationReturned implements OnHttpResponded {
    @Override
    public void onHttpResponded(HttpResponse response) {
      if (mListener != null) {
        if (response != null) {
          final HttpEntity entity = response.getEntity();
          if (entity != null) {
            try {
              String locInfoStr = EntityUtils.toString(entity);
              JSONObject locInfo = new JSONObject(locInfoStr);

              mListener.onLocationReturned(locInfo);

              entity.consumeContent();
            } catch (ParseException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (JSONException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          } else {
            mListener.onLocationReturned(null);
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
    URI uri = getHttpURI(path);
    if (uri == null) {
      return false;
    }

    try {
      final HttpPost post = new HttpPost(uri);

      final JSONObject locRequst = new JSONObject();
      locRequst.put("type", "localize");
      locRequst.put("sensor data", sensorData);

      StringEntity se = new StringEntity(locRequst.toString());

      post.setEntity(se);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      new HttpRequestTask(new OnLocationReturned()).execute(post);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  private class OnReportDone implements OnHttpResponded {
    @Override
    public void onHttpResponded(HttpResponse response) {
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
    URI uri = getHttpURI(path);
    if (uri == null) {
      return false;
    }

    try {
      final HttpPost post = new HttpPost(uri);

      final JSONObject reportRequst = new JSONObject();
      reportRequst.put("type", "report");
      reportRequst.put("sensor data", sensorData);
      reportRequst.put("location", loc);

      final StringEntity se = new StringEntity(reportRequst.toString());

      post.setEntity(se);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      new HttpRequestTask(new OnReportDone()).execute(post);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  private class onMetadataReturned implements OnHttpResponded {
    @Override
    public void onHttpResponded(HttpResponse response) {
      if (mListener != null) {
        if (response != null) {
          final HttpEntity entity = response.getEntity();
          if (entity != null) {
            try {
              String metadataStr = EntityUtils.toString(entity);
              JSONObject metadata = new JSONObject(metadataStr);

              mListener.onMetadataReturned(metadata);

              entity.consumeContent();
            } catch (ParseException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (JSONException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          } else {
            mListener.onMetadataReturned(null);
          }
        } else {
          mListener.onMetadataReturned(null);
        }
      }
    }
  }

  @Override
  public boolean getMetadata(JSONObject loc, JSONArray targetSem) {
    final String path = "/metadata";
    URI uri = getHttpURI(path);
    if (uri == null) {
      return false;
    }

    try {
      final HttpPost post = new HttpPost(uri);

      final JSONObject metadataRequst = new JSONObject();
      metadataRequst.put("type", "metadata");
      metadataRequst.put("location", loc);
      metadataRequst.put("targetsem", targetSem);

      final StringEntity se = new StringEntity(metadataRequst.toString());

      post.setEntity(se);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");

      new HttpRequestTask(new onMetadataReturned()).execute(post);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  private class onMapReturned implements OnHttpResponded {
    @Override
    public void onHttpResponded(HttpResponse response) {
      if (mListener != null) {
        if (response != null) {
          final HttpEntity entity = response.getEntity();
          if (entity != null) {
            try {
              InputStream inputStream = entity.getContent();
              if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                mListener.onMapReturned(bitmap);
              } else {
                mListener.onMapReturned(null);
              }

              entity.consumeContent();
            } catch (ParseException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          } else {
            mListener.onMapReturned(null);
          }
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

    URI uri = getHttpURI(path);
    if (uri == null) {
      return false;
    }

    final HttpGet get = new HttpGet(uri);

    new HttpRequestTask(new onMapReturned()).execute(get);
    return true;
  }

  private static class HttpRequestTask extends
      AsyncTask<HttpRequestBase, Void, HttpResponse> {

    private OnHttpResponded listener;

    public HttpRequestTask(OnHttpResponded listener) {
      this.listener = listener;
    }

    @Override
    protected HttpResponse doInBackground(HttpRequestBase... params) {

      final HttpRequestBase request = params[0];
      final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");

      try {
        HttpResponse response = client.execute(request);
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
          return null;
        }

        return response;
      } catch (IOException e) {
        request.abort();
      } catch (IllegalStateException e) {
        request.abort();
      } catch (Exception e) {
        request.abort();
      } finally {
        client.close();
      }

      return null;
    }

    @Override
    protected void onPostExecute(HttpResponse response) {
      if (!isCancelled()) {
        listener.onHttpResponded(response);
      }
    }
  }

  private URI getHttpURI(String path) {
    // TODO check and remove "http://"
    final String host = SettingsActivity.getServerAddr(mContext);
    final int port = SettingsActivity.getServerPort(mContext);

    URI uri;
    try {
      uri = new URI("http", null, host, port, path, null, null);
    } catch (Exception e) {
      return null;
    }

    return uri;
  }
}

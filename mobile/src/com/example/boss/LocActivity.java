package com.example.boss;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

public class LocActivity extends Activity implements OnItemSelectedListener {

  private Spinner semSpinner;
  private ImageView mapView;

  private String curSemantic;
  private MapDownloadTask mapDownloadTask;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.loc);

    findViews();
    setAdapters();
    setListeners();
  }

  private void findViews() {
    semSpinner = (Spinner) findViewById(R.id.sem_spinner);
    mapView = (ImageView) findViewById(R.id.map_view);
  }

  private void setAdapters() {
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.semantics, android.R.layout.simple_spinner_item);
    adapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    semSpinner.setAdapter(adapter);
  }

  private void setListeners() {
    semSpinner.setOnItemSelectedListener(this);
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    final String newSemantic = parent.getItemAtPosition(pos).toString();
    changeSemantic(newSemantic);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // TODO Auto-generated method stub
  }

  private void changeSemantic(String newSemantic) {
    if ((curSemantic == null) || !(curSemantic.equals(newSemantic))) {
      curSemantic = newSemantic;
      if (mapDownloadTask != null) {
        mapDownloadTask.cancel(true);
      }
      mapDownloadTask = new MapDownloadTask(mapView);
      mapDownloadTask
          .execute("http://kaifei.info/wp-content/uploads/2012/09/Figure.jpg");
    }
  }

  private class MapDownloadTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;

    public MapDownloadTask(ImageView imageView) {
      imageViewReference = new WeakReference<ImageView>(imageView);
    }

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
      if (isCancelled()) {
        bitmap = null;
      }

      if (imageViewReference != null) {
        ImageView imageView = imageViewReference.get();
        if (imageView != null) {
          imageView.setImageBitmap(bitmap);
        }
      }

      mapDownloadTask = null;
    }
  }
}

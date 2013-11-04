package edu.berkeley.bearloc.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

/* 
 * HTTP Post Task posts with JSON Object and gets JSON Object returned
 */
public class JSONHttpPostTask extends AsyncTask<Object, Void, JSONObject> {

  private final onJSONHttpPostRespondedListener listener;

  public static interface onJSONHttpPostRespondedListener {
    void onJSONHttpPostResponded(JSONObject response);
  }

  public JSONHttpPostTask(final onJSONHttpPostRespondedListener listener) {
    this.listener = listener;
  }

  private InputStream httpPost(final HttpURLConnection connection,
      final URL url, final JSONObject entity) throws IOException {
    final int contentLength = entity.toString().getBytes().length;
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty("Content-Length",
        Integer.toString(contentLength));
    connection.setFixedLengthStreamingMode(contentLength);
    connection.setDoInput(true);
    connection.setDoOutput(true);

    final OutputStream out = new BufferedOutputStream(
        connection.getOutputStream());
    out.write(entity.toString().getBytes());
    out.flush();
    out.close();

    final InputStream in = new BufferedInputStream(connection.getInputStream());

    return in;
  }

  @Override
  protected JSONObject doInBackground(final Object... params) {
    final URL url = (URL) params[0];
    final JSONObject entity = (JSONObject) params[1];

    // TODO reuse the connection
    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
      final InputStream in = httpPost(connection, url, entity);

      final BufferedReader reader = new BufferedReader(
          new InputStreamReader(in));

      String line;
      final StringBuilder sb = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        sb.append(line + '\n');
      }
      reader.close();

      return new JSONObject(sb.toString());
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final JSONException e) {
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
  protected void onPostExecute(final JSONObject response) {
    if (!isCancelled() && listener != null) {
      listener.onJSONHttpPostResponded(response);
    }
  }
}

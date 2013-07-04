package com.example.boss.ambience;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Pair;

public class Wifi implements Ambience {

  private static final long WIFI_SAMPLE_ITVL = 1000L; // millisecond
  private static final long WIFI_HISTORY_LEN = 10000L; // millisecond

  private final Context mContext;
  private final Handler mHandler;

  private final WifiManager mWifiManager;
  private final BlockingQueue<Pair<Long, ScanResult>> mWifiEventQueue;
  private final Runnable mWifiScanTimeTask = new Runnable() {
    public void run() {
      final List<ScanResult> newResultList = mWifiManager.getScanResults();
      for (ScanResult scanResult : newResultList) {
        final Long timestamp = System.currentTimeMillis();
        final Pair<Long, ScanResult> event = new Pair<Long, ScanResult>(
            timestamp, scanResult);
        // TODO check return result
        mWifiEventQueue.offer(event);
      }

      final Long curTimestamp = System.currentTimeMillis();
      while (mWifiEventQueue.isEmpty() == false) {
        final Long eventTimestamp = mWifiEventQueue.peek().first;
        if ((curTimestamp - eventTimestamp) > WIFI_HISTORY_LEN) {
          // Remove old data
          mWifiEventQueue.poll();
        } else {
          break;
        }
      }

      mHandler.postDelayed(mWifiScanTimeTask, WIFI_SAMPLE_ITVL);
    }
  };

  public Wifi(Context context) {
    mContext = context;

    mHandler = new Handler();

    mWifiManager = (WifiManager) mContext
        .getSystemService(Context.WIFI_SERVICE);
    mWifiEventQueue = new LinkedBlockingQueue<Pair<Long, ScanResult>>();
  }

  @Override
  public void resume() {
    mHandler.postDelayed(mWifiScanTimeTask, WIFI_SAMPLE_ITVL);
  }

  @Override
  public void pause() {
    mHandler.removeCallbacks(mWifiScanTimeTask);
  }

  @Override
  public void clear() {
    mWifiEventQueue.clear();
  }

  @Override
  public JSONObject get() {
    final JSONObject wifiPack = new JSONObject();

    try {
      // Pack cached WiFi data
      final JSONObject wifiJSONObject = new JSONObject();
      wifiPack.put("wifi", wifiJSONObject);

      wifiJSONObject.put("name", "wifi");
      wifiJSONObject.put("type", "wifi");

      final JSONArray eventJSONArray = new JSONArray();
      wifiJSONObject.put("events", eventJSONArray);

      for (Pair<Long, ScanResult> event : mWifiEventQueue) {
        final JSONObject eventJSONObject = new JSONObject();
        eventJSONArray.put(eventJSONObject);

        final Long timestamp = event.first;
        eventJSONObject.put("timestamp", timestamp);

        final ScanResult scanResult = event.second;
        eventJSONObject.put("BSSID", scanResult.BSSID);
        eventJSONObject.put("SSID", scanResult.SSID);
        eventJSONObject.put("capabilities", scanResult.capabilities);
        eventJSONObject.put("frequency", scanResult.frequency);
        eventJSONObject.put("level", scanResult.level);
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
    }

    return wifiPack;
  }

}

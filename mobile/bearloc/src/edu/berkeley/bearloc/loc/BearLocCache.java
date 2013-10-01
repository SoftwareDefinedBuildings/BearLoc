package edu.berkeley.bearloc.loc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import edu.berkeley.bearloc.loc.BearLocSampleAggregator.OnSampleEventListener;

public class BearLocCache implements OnSampleEventListener {

  private JSONObject mDeviceInfo;
  private JSONObject mSensorInfo;

  private final Map<String, BlockingQueue<JSONObject>> mDataMap;

  public BearLocCache(Context context) {
    mDeviceInfo = BearLocFormat.getDeviceInfo(context);
    mSensorInfo = BearLocFormat.getSensorInfo(context);

    mDataMap = new HashMap<String, BlockingQueue<JSONObject>>();
  }

  public void add(final String type, final JSONObject data) {
    if (!mDataMap.containsKey(type)) {
      mDataMap.put(type, new LinkedBlockingQueue<JSONObject>());
    }
    final BlockingQueue<JSONObject> queue = mDataMap.get(type);
    queue.add(data);
  }

  public JSONObject get() {
    JSONObject data = new JSONObject();
    // add "device" and data
    try {
      data.put("device", mDeviceInfo);
      data.put("sensormeta", mSensorInfo);
      Iterator<Entry<String, BlockingQueue<JSONObject>>> it = mDataMap
          .entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<String, BlockingQueue<JSONObject>> entry = it.next();
        String type = entry.getKey();
        BlockingQueue<JSONObject> eventQ = entry.getValue();
        JSONArray eventArr = new JSONArray();
        for (JSONObject event : eventQ) {
          eventArr.put(event);
        }
        data.put(type, eventArr);
      }

      // Generate copy of data, rather than references
      final String dataStr = data.toString();
      data = new JSONObject(dataStr);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return data;
  }

  public void clear() {
    Iterator<Entry<String, BlockingQueue<JSONObject>>> it = mDataMap.entrySet()
        .iterator();
    while (it.hasNext()) {
      Map.Entry<String, BlockingQueue<JSONObject>> entry = it.next();
      BlockingQueue<JSONObject> eventQ = entry.getValue();
      eventQ.clear();
    }
  }

  @Override
  public void onSampleEvent(String type, JSONObject data) {
    add(type, data);
  }
}

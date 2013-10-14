package edu.berkeley.bearloc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;

import android.content.Context;
import android.util.Pair;

public class BearLocCache {
  private final Map<String, List<Pair<Object, JSONObject>>> mDataMap;

  public BearLocCache(Context context) {
    mDataMap = new HashMap<String, List<Pair<Object, JSONObject>>>();
  }

  public void put(final String type, final Object data, final JSONObject meta) {
    if (!mDataMap.containsKey(type)) {
      mDataMap.put(type, new LinkedList<Pair<Object, JSONObject>>());
    }
    final List<Pair<Object, JSONObject>> list = mDataMap.get(type);

    list.add(new Pair<Object, JSONObject>(data, meta));
  }

  public Map<String, List<Pair<Object, JSONObject>>> get() {
    return mDataMap;
  }

  public void clear() {
    Iterator<Entry<String, List<Pair<Object, JSONObject>>>> it = mDataMap
        .entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, List<Pair<Object, JSONObject>>> entry = it.next();
      List<Pair<Object, JSONObject>> events = entry.getValue();
      events.clear();
    }
  }
}

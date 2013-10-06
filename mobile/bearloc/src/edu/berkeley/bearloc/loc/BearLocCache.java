package edu.berkeley.bearloc.loc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.util.Pair;

public class BearLocCache {
  private final Map<String, BlockingQueue<Pair<Long, Object>>> mDataMap;

  public BearLocCache(Context context) {
    mDataMap = new HashMap<String, BlockingQueue<Pair<Long, Object>>>();
  }

  public void put(final String type, final Object data) {
    if (!mDataMap.containsKey(type)) {
      mDataMap.put(type, new LinkedBlockingQueue<Pair<Long, Object>>());
    }
    final BlockingQueue<Pair<Long, Object>> queue = mDataMap.get(type);

    // TODO check returned value
    Long epoch = System.currentTimeMillis();
    queue.offer(new Pair<Long, Object>(epoch, data));
  }

  public Map<String, BlockingQueue<Pair<Long, Object>>> get() {
    return mDataMap;
  }

  public void clear() {
    Iterator<Entry<String, BlockingQueue<Pair<Long, Object>>>> it = mDataMap
        .entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, BlockingQueue<Pair<Long, Object>>> entry = it.next();
      BlockingQueue<Pair<Long, Object>> eventQ = entry.getValue();
      eventQ.clear();
    }
  }
}

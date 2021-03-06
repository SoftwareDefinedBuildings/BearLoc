/*
 * Copyright (c) 2013, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Author: Kaifei Chen <kaifei@eecs.berkeley.edu>
 */

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

  public BearLocCache(final Context context) {
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
    final Iterator<Entry<String, List<Pair<Object, JSONObject>>>> it = mDataMap
        .entrySet().iterator();
    while (it.hasNext()) {
      final Map.Entry<String, List<Pair<Object, JSONObject>>> entry = it.next();
      final List<Pair<Object, JSONObject>> events = entry.getValue();
      events.clear();
    }
  }
}

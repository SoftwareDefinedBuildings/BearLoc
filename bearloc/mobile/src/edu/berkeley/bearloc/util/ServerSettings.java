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

package edu.berkeley.bearloc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import edu.berkeley.bearloc.R;

public class ServerSettings {
    public static String getServerAddr(final Context context) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        final String defaultValue = context
                .getString(R.string.bearloc_default_server_addr);
        final String addr = prefs.getString("bearloc_pref_server_addr_key",
                defaultValue);

        return addr;
    }

    public static void setServerAddr(final Context context, final String addr) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        final Editor editor = prefs.edit();
        editor.putString("bearloc_pref_server_addr_key", addr);
        editor.commit();
    }

    public static Integer getServerPort(final Context context) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        final String defaultValue = context
                .getString(R.string.bearloc_default_server_port);
        final Integer port = Integer.valueOf(prefs.getString(
                "bearloc_pref_server_port_key", defaultValue));

        return port;
    }

    public static void setServerPort(final Context context, final Integer port) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        final Editor editor = prefs.edit();
        editor.putInt("bearloc_pref_server_port_key", port);
        editor.commit();
    }
}

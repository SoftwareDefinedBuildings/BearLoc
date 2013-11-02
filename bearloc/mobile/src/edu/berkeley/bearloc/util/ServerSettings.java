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
    final String defaultValue = context.getString(R.string.default_server_addr);
    final String addr = prefs.getString("pref_server_addr_key", defaultValue);

    return addr;
  }

  public static void setServerAddr(final Context context, final String addr) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_server_addr_key", addr);
    editor.commit();
  }

  public static Integer getServerPort(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context.getString(R.string.default_server_port);
    final Integer port = Integer.valueOf(prefs.getString(
        "pref_server_port_key", defaultValue));

    return port;
  }

  public static void setServerPort(final Context context, final Integer port) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putInt("pref_server_port_key", port);
    editor.commit();
  }
}

package edu.berkeley.bearloc;

import java.util.List;

import edu.berkeley.bearloc.R;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {

  final public static String ACTION_PREF_DEVICE = "edu.berkeley.bearloc.PREF_DEVICE";
  final public static String ACTION_PREF_SERVER = "edu.berkeley.bearloc.PREF_SERVER";

  // Only call addPreferencesFromResource() before Honeycomb
  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String action = getIntent().getAction();
    if (action != null && action.equals(ACTION_PREF_DEVICE)) {
      addPreferencesFromResource(R.xml.settings_device);
    } else if (action != null && action.equals(ACTION_PREF_SERVER)) {
      addPreferencesFromResource(R.xml.settings_server);
    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      // Load the legacy preferences headers
      addPreferencesFromResource(R.xml.pref_headers_legacy);
    }
  }

  // Called only on Honeycomb and later
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  @Override
  public void onBuildHeaders(List<Header> target) {
    loadHeadersFromResource(R.xml.pref_headers, target);
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public static class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      String settings = getArguments().getString("settings");
      if ("device".equals(settings)) {
        addPreferencesFromResource(R.xml.settings_device);
      } else if ("server".equals(settings)) {
        addPreferencesFromResource(R.xml.settings_server);
      }
    }
  }

  public static void setDeviceUUID(Context context, String uuid) {
    SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    Editor editor = prefs.edit();
    editor.putString("pref_device_uuid", uuid);
    editor.commit();
  }

  public static String getDeviceUUID(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getString(
        "pref_device_uuid", null);
  }

  public static String getServerAddr(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getString(
        "pref_server_addr", context.getString(R.string.default_server_addr));
  }

  public static Integer getServerPort(Context context) {
    return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(
        context).getString("pref_server_port",
        context.getString(R.string.default_server_port)));
  }
}
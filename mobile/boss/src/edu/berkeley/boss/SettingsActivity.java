package edu.berkeley.boss;

import java.util.List;

import edu.berkeley.boss.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {

  final static String ACTION_PREF_SERVER = "edu.berkeley.boss.PREF_SERVER";

  // Only call addPreferencesFromResource() before Honeycomb
  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String action = getIntent().getAction();
    if (action != null && action.equals(ACTION_PREF_SERVER)) {
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
      if ("server".equals(settings)) {
        addPreferencesFromResource(R.xml.settings_server);
      }
    }
  }

  public static String getServerAddr(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getString(
        "pref_server_addr", "kaifei.info");
  }

  public static Integer getServerPort(Context context) {
    return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(
        context).getString("pref_server_port", "10080"));
  }
}
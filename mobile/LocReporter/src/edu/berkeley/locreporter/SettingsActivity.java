package edu.berkeley.locreporter;

import java.util.List;

import edu.berkeley.locreporter.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {

  final public static String ACTION_PREF_GENERAL = "edu.berkeley.locreporter.PREF_GENERAL";

  // Only call addPreferencesFromResource() before Honeycomb
  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String action = getIntent().getAction();
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      if (action != null && action.equals(ACTION_PREF_GENERAL)) {
        addPreferencesFromResource(R.xml.settings_general);
      } else {
        // Load the legacy preferences headers
        addPreferencesFromResource(R.xml.pref_headers_legacy);
      }
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
      if ("general".equals(settings)) {
        addPreferencesFromResource(R.xml.settings_general);
      }
    }
  }

  public static Boolean getAutoReport(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
        "pref_auto_report", false);
  }
}
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

package edu.berkeley.locreporter;

import java.util.List;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class LocReporterSettingsActivity extends PreferenceActivity {

    final public static String ACTION_PREF_SERVER = "edu.berkeley.bearloc.PREF_SERVER";
    final public static String ACTION_PREF_SAMPLER = "edu.berkeley.bearloc.PREF_SAMPLER";

    // Only call addPreferencesFromResource() before Honeycomb
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String action = getIntent().getAction();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            if (action != null
                    && action
                            .equals(LocReporterSettingsActivity.ACTION_PREF_SERVER)) {
                addPreferencesFromResource(edu.berkeley.bearloc.R.xml.bearloc_server_settings);
            } else if (action != null
                    && action
                            .equals(LocReporterSettingsActivity.ACTION_PREF_SAMPLER)) {
                addPreferencesFromResource(edu.berkeley.bearloc.R.xml.bearloc_sampler_settings);
            } else {
                // Load the legacy preferences headers
                addPreferencesFromResource(R.xml.pref_headers_legacy);
            }
        }
    }

    // Called only on Honeycomb and later
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(final List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            final String settings = getArguments().getString("settings");
            if ("server".equals(settings)) {
                addPreferencesFromResource(edu.berkeley.bearloc.R.xml.bearloc_server_settings);
            } else if ("sampler".equals(settings)) {
                addPreferencesFromResource(edu.berkeley.bearloc.R.xml.bearloc_sampler_settings);
            }
        }
    }

    @Override
    protected boolean isValidFragment(final String fragmentName) {
        return true;
    }
}
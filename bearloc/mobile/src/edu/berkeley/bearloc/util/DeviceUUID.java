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
 * Original codes from http://stackoverflow.com/questions/5088474/how-can-i-get-the-uuid-of-my-android-phone-in-an-application
 *
 * Author: Kaifei Chen <kaifei@eecs.berkeley.edu>
 */

package edu.berkeley.bearloc.util;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class DeviceUUID {
  private static UUID uuid;

  /**
   * Returns a unique UUID for the current android device. As with all UUIDs,
   * this unique ID is "very highly likely" to be unique across all Android
   * devices. Much more so than ANDROID_ID is.
   * 
   * The UUID is generated by using ANDROID_ID as the base key if appropriate,
   * falling back on TelephonyManager.getDeviceID() if ANDROID_ID is known to be
   * incorrect, and finally falling back on a random UUID that's persisted to
   * SharedPreferences if getDeviceID() does not return a usable value.
   * 
   * In some rare circumstances, this ID may change. In particular, if the
   * device is factory reset a new device ID may be generated. In addition, if a
   * user upgrades their phone from certain buggy implementations of Android 2.2
   * to a newer, non-buggy version of Android, the device ID may change. Or, if
   * a user uninstalls your app on a device that has neither a proper Android ID
   * nor a Device ID, this ID may change on reinstallation.
   * 
   * Note that if the code falls back on using TelephonyManager.getDeviceId(),
   * the resulting ID will NOT change after a factory reset. Something to be
   * aware of.
   * 
   * Works around a bug in Android 2.2 for many devices when using ANDROID_ID
   * directly.
   * 
   * @see http://code.google.com/p/android/issues/detail?id=10603
   * 
   * @return a UUID that may be used to uniquely identify your device for most
   *         purposes.
   */
  public static UUID getDeviceUUID(final Context context) {
    if (DeviceUUID.uuid == null) {
      synchronized (DeviceUUID.class) {
        final String androidId = Secure.getString(context.getContentResolver(),
            Secure.ANDROID_ID);

        // Use the Android ID unless it's broken, in which case fallback on
        // deviceId, unless it's not available, then fallback on a random
        // number which we store to a prefs file
        try {
          if (!"9774d56d682e549c".equals(androidId)) {
            DeviceUUID.uuid = UUID
                .nameUUIDFromBytes(androidId.getBytes("utf8"));
          } else {
            final String deviceId = ((TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            DeviceUUID.uuid = deviceId != null ? UUID
                .nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID
                .randomUUID();
          }
        } catch (final UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
      }
    }

    return DeviceUUID.uuid;
  }
}
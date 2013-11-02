package edu.berkeley.bearloc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import edu.berkeley.bearloc.R;

public class SamplerSettings {
  /*
   * Accelerometer
   */
  public static boolean getAccEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_acc_enable));
    final boolean enable = prefs
        .getBoolean("pref_acc_enable_key", defaultValue);

    return enable;
  }

  public static void setAccEnable(final Context context, final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_acc_enable_key", enable);
    editor.commit();
  }

  public static long getAccDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_acc_duration);
    final long duration = Long.parseLong(prefs.getString(
        "pref_acc_duration_key", defaultValue));

    return duration;
  }

  public static void setAccDuration(final Context context, final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_acc_duration_key", Long.toString(duration));
    editor.commit();
  }

  public static int getAccCnt(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context.getString(R.string.default_acc_cnt);
    final int cnt = Integer.parseInt(prefs.getString("pref_acc_cnt_key",
        defaultValue));

    return cnt;
  }

  public static void setAccCnt(final Context context, final int cnt) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_acc_cnt_key", Integer.toString(cnt));
    editor.commit();
  }

  public static int getAccDelay(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_acc_delay_value);
    final int delay = Integer.parseInt(prefs.getString("pref_acc_delay_key",
        defaultValue));

    return delay;
  }

  public static void setAccDelay(final Context context, final int delay) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_acc_delay_key", Integer.toString(delay));
    editor.commit();
  }

  /*
   * Ambient temperature
   */
  public static boolean getTempEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_temp_enable));
    final boolean enable = prefs.getBoolean("pref_temp_enable_key",
        defaultValue);

    return enable;
  }

  public static void setTempEnable(final Context context, final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_temp_enable_key", enable);
    editor.commit();
  }

  public static long getTempDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_temp_duration);
    final long duration = Long.parseLong(prefs.getString(
        "pref_temp_duration_key", defaultValue));

    return duration;
  }

  public static void setTempDuration(final Context context, final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_temp_duration_key", Long.toString(duration));
    editor.commit();
  }

  public static int getTempCnt(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context.getString(R.string.default_temp_cnt);
    final int cnt = Integer.parseInt(prefs.getString("pref_temp_cnt_key",
        defaultValue));

    return cnt;
  }

  public static void setTempCnt(final Context context, final int cnt) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_temp_cnt_key", Integer.toString(cnt));
    editor.commit();
  }

  public static int getTempDelay(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_temp_delay_value);
    final int delay = Integer.parseInt(prefs.getString("pref_temp_delay_key",
        defaultValue));

    return delay;
  }

  public static void setTempDelay(final Context context, final int delay) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_temp_delay_key", Integer.toString(delay));
    editor.commit();
  }

  /*
   * Atmospheric pressure
   */
  public static boolean getPressureEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_pressure_enable));
    final boolean enable = prefs.getBoolean("pref_pressure_enable_key",
        defaultValue);

    return enable;
  }

  public static void setPressureEnable(final Context context,
      final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_pressure_enable_key", enable);
    editor.commit();
  }

  public static long getPressureDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_pressure_duration);
    final long duration = Long.parseLong(prefs.getString(
        "pref_pressure_duration_key", defaultValue));

    return duration;
  }

  public static void setPressureDuration(final Context context,
      final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_pressure_duration_key", Long.toString(duration));
    editor.commit();
  }

  public static int getPressureCnt(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_pressure_cnt);
    final int cnt = Integer.parseInt(prefs.getString("pref_pressure_cnt_key",
        defaultValue));

    return cnt;
  }

  public static void setPressureCnt(final Context context, final int cnt) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_pressure_cnt_key", Integer.toString(cnt));
    editor.commit();
  }

  public static int getPressureDelay(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_pressure_delay_value);
    final int delay = Integer.parseInt(prefs.getString(
        "pref_pressure_delay_key", defaultValue));

    return delay;
  }

  public static void setPressureDelay(final Context context, final int delay) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_pressure_delay_key", Integer.toString(delay));
    editor.commit();
  }

  /*
   * Audio
   */
  public static boolean getAudioEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_audio_enable));
    final boolean enable = prefs.getBoolean("pref_audio_enable_key",
        defaultValue);

    return enable;
  }

  public static void setAudioEnable(final Context context, final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_audio_enable_key", enable);
    editor.commit();
  }

  public static long getAudioDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_audio_duration);
    final long duration = Long.parseLong(prefs.getString(
        "pref_audio_duration_key", defaultValue));

    return duration;
  }

  public static void setAudioDuration(final Context context, final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_audio_duration_key", Long.toString(duration));
    editor.commit();
  }

  public static int getAudioSrc(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_audio_src_value);
    final int src = Integer.parseInt(prefs.getString("pref_audio_src_key",
        defaultValue));

    return src;
  }

  public static void setAudioSrc(final Context context, final int src) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_audio_src_key", Integer.toString(src));
    editor.commit();
  }

  public static int getAudioSampleRate(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_audio_sample_rate_value);
    final int rate = Integer.parseInt(prefs.getString(
        "pref_audio_sample_rate_key", defaultValue));

    return rate;
  }

  public static void setAudioSampleRate(final Context context, final int rate) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_audio_sample_rate_key", Integer.toString(rate));
    editor.commit();
  }

  public static int getAudioChannel(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_audio_channel_value);
    final int channel = Integer.parseInt(prefs.getString(
        "pref_audio_channel_key", defaultValue));

    return channel;
  }

  public static void setAudioChannel(final Context context, final int channel) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_audio_channel_key", Integer.toString(channel));
    editor.commit();
  }

  public static int getAudioFormat(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_audio_format_value);
    final int format = Integer.parseInt(prefs.getString(
        "pref_audio_format_key", defaultValue));

    return format;
  }

  public static void setAudioFormat(final Context context, final int format) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_audio_format_key", Integer.toString(format));
    editor.commit();
  }

  /*
   * Humidity
   */
  public static boolean getHumidityEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_humidity_enable));
    final boolean enable = prefs.getBoolean("pref_humidity_enable_key",
        defaultValue);

    return enable;
  }

  public static void setHumidityEnable(final Context context,
      final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_humidity_enable_key", enable);
    editor.commit();
  }

  public static long getHumidityDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_humidity_duration);
    final long duration = Long.parseLong(prefs.getString(
        "pref_humidity_duration_key", defaultValue));

    return duration;
  }

  public static void setHumidityDuration(final Context context,
      final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_humidity_duration_key", Long.toString(duration));
    editor.commit();
  }

  public static int getHumidityCnt(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_humidity_cnt);
    final int cnt = Integer.parseInt(prefs.getString("pref_humidity_cnt_key",
        defaultValue));

    return cnt;
  }

  public static void setHumidityCnt(final Context context, final int cnt) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_humidity_cnt_key", Integer.toString(cnt));
    editor.commit();
  }

  public static int getHumidityDelay(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_humidity_delay_value);
    final int delay = Integer.parseInt(prefs.getString(
        "pref_humidity_delay_key", defaultValue));

    return delay;
  }

  public static void setHumidityDelay(final Context context, final int delay) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_humidity_delay_key", Integer.toString(delay));
    editor.commit();
  }

  /*
   * Light
   */
  public static boolean getLightEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_light_enable));
    final boolean enable = prefs.getBoolean("pref_light_enable_key",
        defaultValue);

    return enable;
  }

  public static void setLightEnable(final Context context, final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_light_enable_key", enable);
    editor.commit();
  }

  public static long getLightDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_light_duration);
    final long duration = Long.parseLong(prefs.getString("pref_light_enable",
        defaultValue));

    return duration;
  }

  public static void setLightDuration(final Context context, final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_light_enable", Long.toString(duration));
    editor.commit();
  }

  public static int getLightCnt(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context.getString(R.string.default_light_cnt);
    final int cnt = Integer.parseInt(prefs.getString("pref_light_cnt_key",
        defaultValue));

    return cnt;
  }

  public static void setLightCnt(final Context context, final int cnt) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_light_cnt_key", Integer.toString(cnt));
    editor.commit();
  }

  public static int getLightDelay(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_light_delay_value);
    final int delay = Integer.parseInt(prefs.getString("pref_light_delay_key",
        defaultValue));

    return delay;
  }

  public static void setLightDelay(final Context context, final int delay) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_light_delay_key", Integer.toString(delay));
    editor.commit();
  }

  /*
   * Linear Accelerometer
   */
  public static boolean getLinearAccEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_lacc_enable));
    final boolean enable = prefs.getBoolean("pref_lacc_enable_key",
        defaultValue);

    return enable;
  }

  public static void setLinearAccEnable(final Context context,
      final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_lacc_enable_key", enable);
    editor.commit();
  }

  public static long getLinearAccDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_lacc_duration);
    final long duration = Long.parseLong(prefs.getString(
        "pref_lacc_duration_key", defaultValue));

    return duration;
  }

  public static void setLinearAccDuration(final Context context,
      final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_lacc_duration_key", Long.toString(duration));
    editor.commit();
  }

  public static int getLinearAccCnt(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context.getString(R.string.default_lacc_cnt);
    final int cnt = Integer.parseInt(prefs.getString("pref_lacc_cnt_key",
        defaultValue));

    return cnt;
  }

  public static void setLinearAccCnt(final Context context, final int cnt) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_lacc_cnt_key", Integer.toString(cnt));
    editor.commit();
  }

  public static int getLinearAccDelay(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_lacc_delay_value);
    final int delay = Integer.parseInt(prefs.getString("pref_lacc_delay_key",
        defaultValue));

    return delay;
  }

  public static void setLinearAccDelay(final Context context, final int delay) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_lacc_delay_key", Integer.toString(delay));
    editor.commit();
  }

  /*
   * Magnetic
   */
  public static boolean getMagneticEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_magnetic_enable));
    final boolean enable = prefs.getBoolean("pref_magnetic_enable_key",
        defaultValue);

    return enable;
  }

  public static void setMagneticEnable(final Context context,
      final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_magnetic_enable_key", enable);
    editor.commit();
  }

  public static long getMagneticDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_magnetic_duration);
    final long duration = Long.parseLong(prefs.getString(
        "pref_magnetic_duration_key", defaultValue));

    return duration;
  }

  public static void setMagneticDuration(final Context context,
      final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_magnetic_duration_key", Long.toString(duration));
    editor.commit();
  }

  public static int getMagneticCnt(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_magnetic_cnt);
    final int cnt = Integer.parseInt(prefs.getString("pref_magnetic_cnt_key",
        defaultValue));

    return cnt;
  }

  public static void setMagneticCnt(final Context context, final int cnt) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_magnetic_cnt_key", Integer.toString(cnt));
    editor.commit();
  }

  public static int getMagneticDelay(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_magnetic_delay_value);
    final int delay = Integer.parseInt(prefs.getString(
        "pref_magnetic_delay_key", defaultValue));

    return delay;
  }

  public static void setMagneticDelay(final Context context, final int delay) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_magnetic_delay_key", Integer.toString(delay));
    editor.commit();
  }

  /*
   * Geographic location
   */
  public static boolean getGeoLocEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_geoloc_enable));
    final boolean enable = prefs.getBoolean("pref_geoloc_enable_key",
        defaultValue);

    return enable;
  }

  public static void setGeoLocEnable(final Context context, final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_geoloc_enable_key", enable);
    editor.commit();
  }

  public static long getGeoLocDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_geoloc_duration);
    final long duration = Long.parseLong(prefs.getString(
        "pref_geoloc_duration_key", defaultValue));

    return duration;
  }

  public static void setGeoLocDuration(final Context context,
      final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_geoloc_duration_key", Long.toString(duration));
    editor.commit();
  }

  public static int getGeoLocCnt(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context.getString(R.string.default_geoloc_cnt);
    final int cnt = Integer.parseInt(prefs.getString("pref_geoloc_cnt_key",
        defaultValue));

    return cnt;
  }

  public static void setGeoLocCnt(final Context context, final int cnt) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_geoloc_cnt_key", Integer.toString(cnt));
    editor.commit();
  }

  public static int getGeoLocMinDelay(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_geoloc_min_delay);
    final int delay = Integer.parseInt(prefs.getString(
        "pref_geoloc_min_delay_key", defaultValue));

    return delay;
  }

  public static void setGeoLocMinDelay(final Context context, final int delay) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_geoloc_min_delay_key", Integer.toString(delay));
    editor.commit();
  }

  public static float getGeoLocMinDist(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_geoloc_min_dist);
    final float dist = Float.parseFloat(prefs.getString(
        "pref_geoloc_min_dist_key", defaultValue));

    return dist;
  }

  public static void setGeoLocMinDist(final Context context, final float dist) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_geoloc_min_dist_key", Float.toString(dist));
    editor.commit();
  }

  /*
   * Gravity
   */
  public static boolean getGravityEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_gravity_enable));
    final boolean enable = prefs.getBoolean("pref_gravity_enable_key",
        defaultValue);

    return enable;
  }

  public static void setGravityEnable(final Context context,
      final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_gravity_enable_key", enable);
    editor.commit();
  }

  public static long getGravityDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_gravity_duration);
    final long duration = Long.parseLong(prefs.getString(
        "pref_gravity_duration_key", defaultValue));

    return duration;
  }

  public static void setGravityDuration(final Context context,
      final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_gravity_duration_key", Long.toString(duration));
    editor.commit();
  }

  public static int getGravityCnt(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context.getString(R.string.default_gravity_cnt);
    final int cnt = Integer.parseInt(prefs.getString("pref_gravity_cnt_key",
        defaultValue));

    return cnt;
  }

  public static void setGravityCnt(final Context context, final int cnt) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_gravity_cnt_key", Integer.toString(cnt));
    editor.commit();
  }

  public static int getGravityDelay(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_gravity_delay_value);
    final int delay = Integer.parseInt(prefs.getString(
        "pref_gravity_delay_key", defaultValue));

    return delay;
  }

  public static void setGravityDelay(final Context context, final int delay) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_gravity_delay_key", Integer.toString(delay));
    editor.commit();
  }

  /*
   * Gyro
   */
  public static boolean getGyroEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_gyro_enable));
    final boolean enable = prefs.getBoolean("pref_gyro_enable_key",
        defaultValue);

    return enable;
  }

  public static void setGyroEnable(final Context context, final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_gyro_enable_key", enable);
    editor.commit();
  }

  public static long getGyroDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_gyro_duration);
    final long duration = Long.parseLong(prefs.getString(
        "pref_gyro_duration_key", defaultValue));

    return duration;
  }

  public static void setGyroDuration(final Context context, final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_gyro_duration_key", Long.toString(duration));
    editor.commit();
  }

  public static int getGyroCnt(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context.getString(R.string.default_gyro_cnt);
    final int cnt = Integer.parseInt(prefs.getString("pref_gyro_cnt_key",
        defaultValue));

    return cnt;
  }

  public static void setGyroCnt(final Context context, final int cnt) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_gyro_cnt_key", Integer.toString(cnt));
    editor.commit();
  }

  public static int getGyroDelay(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_gyro_delay_value);
    final int delay = Integer.parseInt(prefs.getString("pref_gyro_delay_key",
        defaultValue));

    return delay;
  }

  public static void setGyroDelay(final Context context, final int delay) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_gyro_delay_key", Integer.toString(delay));
    editor.commit();
  }

  /*
   * Proximity
   */
  public static boolean getProximityEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_proximity_enable));
    final boolean enable = prefs.getBoolean("pref_proximity_enable_key",
        defaultValue);

    return enable;
  }

  public static void setProximityEnable(final Context context,
      final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_proximity_enable_key", enable);
    editor.commit();
  }

  public static long getProximityDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_proximity_duration);
    final long duration = Long.parseLong(prefs.getString(
        "pref_proximity_duration_key", defaultValue));

    return duration;
  }

  public static void setProximityDuration(final Context context,
      final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_proximity_duration_key", Long.toString(duration));
    editor.commit();
  }

  public static int getProximityCnt(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_proximity_cnt);
    final int cnt = Integer.parseInt(prefs.getString("pref_proximity_cnt_key",
        defaultValue));

    return cnt;
  }

  public static void setProximityCnt(final Context context, final int cnt) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_proximity_cnt_key", Integer.toString(cnt));
    editor.commit();
  }

  public static int getProximityDelay(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_proximity_delay_value);
    final int delay = Integer.parseInt(prefs.getString(
        "pref_proximity_delay_key", defaultValue));

    return delay;
  }

  public static void setProximityDelay(final Context context, final int delay) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_proximity_delay_key", Integer.toString(delay));
    editor.commit();
  }

  /*
   * Rotation
   */
  public static boolean getRotationEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_rotation_enable));
    final boolean enable = prefs.getBoolean("pref_rotation_enable_key",
        defaultValue);

    return enable;
  }

  public static void setRotationEnable(final Context context,
      final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_rotation_enable_key", enable);
    editor.commit();
  }

  public static long getRotationDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_rotation_duration);
    final long duration = Long.parseLong(prefs.getString(
        "pref_rotation_duration_key", defaultValue));

    return duration;
  }

  public static void setRotationDuration(final Context context,
      final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_rotation_duration_key", Long.toString(duration));
    editor.commit();
  }

  public static int getRotationCnt(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_rotation_cnt);
    final int cnt = Integer.parseInt(prefs.getString("pref_rotation_cnt_key",
        defaultValue));

    return cnt;
  }

  public static void setRotationCnt(final Context context, final int cnt) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_rotation_cnt_key", Integer.toString(cnt));
    editor.commit();
  }

  public static int getRotationDelay(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_rotation_delay_value);
    final int delay = Integer.parseInt(prefs.getString(
        "pref_rotation_delay_key", defaultValue));

    return delay;
  }

  public static void setRotationDelay(final Context context, final int delay) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_rotation_delay_key", Integer.toString(delay));
    editor.commit();
  }

  /*
   * Wifi
   */
  public static boolean getWifiEnable(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final boolean defaultValue = Boolean.parseBoolean(context
        .getString(R.string.default_wifi_enable));
    final boolean enable = prefs.getBoolean("pref_wifi_enable_key",
        defaultValue);

    return enable;
  }

  public static void setWifiEnable(final Context context, final boolean enable) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putBoolean("pref_wifi_enable_key", enable);
    editor.commit();
  }

  public static long getWifiDuration(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_wifi_duration);
    final long duration = Long.parseLong(prefs.getString(
        "pref_wifi_duration_key", defaultValue));

    return duration;
  }

  public static void setWifiDuration(final Context context, final long duration) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_wifi_duration_key", Long.toString(duration));
    editor.commit();
  }

  public static int getWifiCnt(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context.getString(R.string.default_wifi_cnt);
    final int cnt = Integer.parseInt(prefs.getString("pref_wifi_cnt_key",
        defaultValue));

    return cnt;
  }

  public static void setWifiCnt(final Context context, final int cnt) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_wifi_cnt_key", Integer.toString(cnt));
    editor.commit();
  }

  public static int getWifiDelay(final Context context) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final String defaultValue = context
        .getString(R.string.default_wifi_delay_value);
    final int delay = Integer.parseInt(prefs.getString("pref_wifi_delay_key",
        defaultValue));

    return delay;
  }

  public static void setWifiDelay(final Context context, final int delay) {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    final Editor editor = prefs.edit();
    editor.putString("pref_wifi_delay_key", Integer.toString(delay));
    editor.commit();
  }
}

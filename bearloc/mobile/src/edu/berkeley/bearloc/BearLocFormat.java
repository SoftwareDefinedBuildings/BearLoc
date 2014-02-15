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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.AudioFormat;
import android.media.MediaRecorder.AudioSource;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.util.Pair;
import edu.berkeley.bearloc.util.DeviceUUID;

public class BearLocFormat {

	private final Context mContext;
	private final JSONObject mDeviceInfo;
	private final JSONArray mSensorInfo;

	public BearLocFormat(final Context context) {
		mContext = context;
		mDeviceInfo = getDeviceMeta();
		mSensorInfo = getSensorMetaList();
	}

	public JSONObject dump(
			final Map<String, List<Pair<Object, JSONObject>>> dataMap) {
		final JSONObject dumpObj = new JSONObject();
		// add "device" and data
		try {
			final Iterator<Entry<String, List<Pair<Object, JSONObject>>>> it = dataMap
					.entrySet().iterator();
			while (it.hasNext()) {
				final Map.Entry<String, List<Pair<Object, JSONObject>>> entry = it
						.next();
				final String type = entry.getKey();
				final List<Pair<Object, JSONObject>> list = entry.getValue();

				final JSONArray eventArr = new JSONArray();
				for (final Pair<Object, JSONObject> event : list) {
					final Object data = event.first;
					final JSONObject meta = event.second;
					final JSONArray formated = format(type, data, meta);
					for (int i = 0; i < formated.length(); i++) {
						eventArr.put(formated.get(i));
					}
				}

				if (eventArr.length() > 0) {
					dumpObj.put(type, eventArr);
				}
			}

			if (dumpObj.length() > 0) {
				dumpObj.put("device", mDeviceInfo);
				dumpObj.put("sensormeta", mSensorInfo);
			}
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return dumpObj;
	}

	public JSONObject getDeviceMeta() {
		final JSONObject deviceMeta = new JSONObject();
		try {
			// Device Info
			deviceMeta.put("type", "device meta");
			deviceMeta.put("id", DeviceUUID.getDeviceUUID(mContext));
			deviceMeta.put("make", Build.MANUFACTURER);
			deviceMeta.put("model", Build.MODEL);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return deviceMeta;
	}

	// Only call getMinDelay() before Gingerbread
	@SuppressLint("NewApi")
	private JSONArray getSensorMetaList() {
		final JSONArray sensorMetaList = new JSONArray();
		try {
			// Sensor Info
			final SensorManager sensorMgr = (SensorManager) mContext
					.getSystemService(Context.SENSOR_SERVICE);
			final List<Sensor> sensorList = sensorMgr
					.getSensorList(Sensor.TYPE_ALL);
			final Iterator<Sensor> iterator = sensorList.iterator();
			while (iterator.hasNext()) {
				final Sensor sensor = iterator.next();

				String type = null;
				switch (sensor.getType()) {
				case Sensor.TYPE_ACCELEROMETER:
					type = "acc";
					break;
				case Sensor.TYPE_AMBIENT_TEMPERATURE:
					type = "temp";
					break;
				case Sensor.TYPE_GRAVITY:
					type = "gravity";
					break;
				case Sensor.TYPE_GYROSCOPE:
					type = "gyro";
					break;
				case Sensor.TYPE_LIGHT:
					type = "light";
					break;
				case Sensor.TYPE_LINEAR_ACCELERATION:
					type = "lacc";
					break;
				case Sensor.TYPE_MAGNETIC_FIELD:
					type = "magnetic";
					break;
				case Sensor.TYPE_PRESSURE:
					type = "pressure";
					break;
				case Sensor.TYPE_PROXIMITY:
					type = "proximity";
					break;
				case Sensor.TYPE_RELATIVE_HUMIDITY:
					type = "humidity";
					break;
				case Sensor.TYPE_ROTATION_VECTOR:
					type = "rotation";
					break;
				default:
					break;
				}

				if (type != null) {
					final JSONObject sensorMeta = new JSONObject();
					sensorMeta.put("type", "sensor meta");
					sensorMeta.put("id", DeviceUUID.getDeviceUUID(mContext));
					sensorMeta.put("sensor", type);
					sensorMeta.put("vendor", sensor.getVendor());
					sensorMeta.put("model", sensor.getName());
					sensorMeta.put("version", sensor.getVersion());
					sensorMeta.put("power", sensor.getPower());
					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
						sensorMeta.put("min delay", sensor.getMinDelay());
					}
					sensorMeta.put("max range", sensor.getMaximumRange());
					sensorMeta.put("resolution", sensor.getResolution());

					sensorMetaList.put(sensorMeta);
				}

				// TODO add audio, wifi, and bluetooth info
			}
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sensorMetaList;
	}

	private JSONArray format(final String type, final Object data,
			final JSONObject meta) {
		if ("semloc".equals(type)) {
			return formatSemLoc(data, meta);
		} else if ("wifi".equals(type)) {
			return formatWifi(data, meta);
		} else if ("audio".equals(type)) {
			return formatAudio(data, meta);
		} else if ("geoloc".equals(type)) {
			return formatGeoCoord(data, meta);
		} else if ("acc".equals(type)) {
			return formatAcc(data, meta);
		} else if ("lacc".equals(type)) {
			return formatLinearAcc(data, meta);
		} else if ("gravity".equals(type)) {
			return formatGravity(data, meta);
		} else if ("gyro".equals(type)) {
			return formatGyro(data, meta);
		} else if ("rotation".equals(type)) {
			return formatRotation(data, meta);
		} else if ("magnetic".equals(type)) {
			return formatMagnetic(data, meta);
		} else if ("light".equals(type)) {
			return formatLight(data, meta);
		} else if ("temp".equals(type)) {
			return formatTemp(data, meta);
		} else if ("pressure".equals(type)) {
			return formatPressure(data, meta);
		} else if ("proximity".equals(type)) {
			return formatProximity(data, meta);
		} else if ("humidity".equals(type)) {
			return formatHumidity(data, meta);
		}

		return null;
	}

	private JSONArray formatSemLoc(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final JSONObject from = (JSONObject) data;
		try {
			final JSONObject event = new JSONObject();
			event.put("type", "reported semloc");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", meta.getLong("epoch"));
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("country", from.optString("country", null));
			event.put("state", from.optString("state", null));
			event.put("city", from.optString("city", null));
			event.put("street", from.optString("street", null));
			event.put("building", from.optString("building", null));
			event.put("locale", from.optString("locale", null));

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatWifi(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final ScanResult from = (ScanResult) data;

		try {
			final JSONObject event = new JSONObject();
			event.put("type", "wifi");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", meta.getLong("epoch"));
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("BSSID", from.BSSID);
			event.put("SSID", from.SSID);
			event.put("capability", from.capabilities);
			event.put("frequency", from.frequency);
			event.put("RSSI", from.level);

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatAudio(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final JSONObject from = (JSONObject) data;

		try {
			final JSONObject event = from;
			event.put("type", "audio");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("sysnano", meta.getLong("sysnano"));
			final String source = (from.getInt("source") == AudioSource.CAMCORDER) ? "CAMCORDER"
					: "MIC";
			final int channel = (from.getInt("channel") == AudioFormat.CHANNEL_IN_MONO) ? 1
					: 2;
			final int sampwidth = (from.getInt("sampwidth") == AudioFormat.ENCODING_PCM_16BIT) ? 2
					: 1;
			final int nframes = event.getJSONArray("raw").length()
					/ (event.getInt("sampwidth") * event.getInt("channel"));
			event.put("source", source);
			event.put("channel", channel);
			event.put("sampwidth", sampwidth);
			event.put("nframes", nframes);

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatGeoCoord(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final Location from = (Location) data;

		try {
			final JSONObject event = new JSONObject();
			event.put("type", "geocoord");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", from.getTime());
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("longitude", from.getLongitude());
			event.put("latitude", from.getLatitude());
			event.put("altitude", from.getAltitude());
			event.put("bearing", from.getBearing());
			event.put("speed", from.getSpeed());
			event.put("accuracy", from.getAccuracy());
			event.put("provider", from.getProvider());

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatAcc(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final SensorEvent from = (SensorEvent) data;

		try {
			final JSONObject event = new JSONObject();
			event.put("type", "accelerometer");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", meta.getLong("epoch"));
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("eventnano", from.timestamp);
			event.put("x", from.values[0]);
			event.put("y", from.values[1]);
			event.put("z", from.values[2]);
			event.put("accuracy", from.accuracy);

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatLinearAcc(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final SensorEvent from = (SensorEvent) data;

		try {
			final JSONObject event = new JSONObject();
			event.put("type", "linear accelerometer");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", meta.getLong("epoch"));
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("eventnano", from.timestamp);
			event.put("x", from.values[0]);
			event.put("y", from.values[1]);
			event.put("z", from.values[2]);
			event.put("accuracy", from.accuracy);

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatGravity(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final SensorEvent from = (SensorEvent) data;

		try {
			final JSONObject event = new JSONObject();
			event.put("type", "gravity");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", meta.getLong("epoch"));
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("eventnano", from.timestamp);
			event.put("x", from.values[0]);
			event.put("y", from.values[1]);
			event.put("z", from.values[2]);
			event.put("accuracy", from.accuracy);

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatGyro(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final SensorEvent from = (SensorEvent) data;

		try {
			final JSONObject event = new JSONObject();
			event.put("type", "gyroscope");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", meta.getLong("epoch"));
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("eventnano", from.timestamp);
			event.put("x", from.values[0]);
			event.put("y", from.values[1]);
			event.put("z", from.values[2]);
			event.put("accuracy", from.accuracy);

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatRotation(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final SensorEvent from = (SensorEvent) data;

		try {
			final JSONObject event = new JSONObject();
			event.put("type", "rotation");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", meta.getLong("epoch"));
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("eventnano", from.timestamp);
			event.put("xr", from.values[0]);
			event.put("yr", from.values[1]);
			event.put("zr", from.values[2]);
			if (from.values.length >= 4) {
				event.put("cos", from.values[3]);
			}
			if (from.values.length >= 5) {
				event.put("head_accuracy", from.values[4]);
			}
			event.put("accuracy", from.accuracy);

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatMagnetic(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final SensorEvent from = (SensorEvent) data;

		try {
			final JSONObject event = new JSONObject();
			event.put("type", "magnetic");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", meta.getLong("epoch"));
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("eventnano", from.timestamp);
			event.put("x", from.values[0]);
			event.put("y", from.values[1]);
			event.put("z", from.values[2]);
			event.put("accuracy", from.accuracy);

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatLight(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final SensorEvent from = (SensorEvent) data;

		try {
			final JSONObject event = new JSONObject();
			event.put("type", "light");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", meta.getLong("epoch"));
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("eventnano", from.timestamp);
			event.put("light", from.values[0]);
			event.put("accuracy", from.accuracy);

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatTemp(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final SensorEvent from = (SensorEvent) data;

		try {
			final JSONObject event = new JSONObject();
			event.put("type", "temperature");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", meta.getLong("epoch"));
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("eventnano", from.timestamp);
			event.put("temp", from.values[0]);
			event.put("accuracy", from.accuracy);

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatPressure(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final SensorEvent from = (SensorEvent) data;

		try {
			final JSONObject event = new JSONObject();
			event.put("type", "pressure");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", meta.getLong("epoch"));
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("eventnano", from.timestamp);
			event.put("pressure", from.values[0]);
			event.put("accuracy", from.accuracy);

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatProximity(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final SensorEvent from = (SensorEvent) data;

		try {
			final JSONObject event = new JSONObject();
			event.put("type", "proximity");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", meta.getLong("epoch"));
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("eventnano", from.timestamp);
			event.put("proximity", from.values[0]);
			event.put("accuracy", from.accuracy);

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONArray formatHumidity(final Object data, final JSONObject meta) {
		final JSONArray to = new JSONArray();
		final SensorEvent from = (SensorEvent) data;

		try {
			final JSONObject event = new JSONObject();
			event.put("type", "humidity");
			event.put("id", DeviceUUID.getDeviceUUID(mContext));
			event.put("epoch", meta.getLong("epoch"));
			event.put("sysnano", meta.getLong("sysnano"));
			event.put("eventnano", from.timestamp);
			event.put("humidity", from.values[0]);
			event.put("accuracy", from.accuracy);

			to.put(event);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}
}

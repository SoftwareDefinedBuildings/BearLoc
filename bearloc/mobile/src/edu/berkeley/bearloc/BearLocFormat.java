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
import edu.berkeley.bearloc.util.DeviceUUID;

public class BearLocFormat {

	private final Context mContext;
	private final BearLocCache mCache;

	public BearLocFormat(final Context context, final BearLocCache cache) {
		mContext = context;
		mCache = cache;

		try {
			String type = "device info";
			JSONObject meta = new JSONObject();
            meta.put("type", type);
			meta.put("epoch", System.currentTimeMillis());
			meta.put("sysnano", System.nanoTime());
            JSONObject formated = format(getDeviceInfo(), meta);
            if (formated != null) {
                mCache.add(formated);
            }

			final JSONArray sensorInfoList = getSensorInfoList();
			type = "sensor info";
			for (int i = 0; i < sensorInfoList.length(); i++) {
				final JSONObject sensorInfo = sensorInfoList.getJSONObject(i);

				meta = new JSONObject();
                meta.put("type", type);
				meta.put("epoch", System.currentTimeMillis());
				meta.put("sysnano", System.nanoTime());
                formated = format(sensorInfo, meta);
                if (formated != null) {
                    mCache.add(formated);
                }
			}
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public JSONObject format(final Object data, final JSONObject meta) {
        final String type = meta.optString("type");
        // Android requires compiler compliance level 5.0 or 6.0
        if (type == "device info") {
            return formatDeviceInfo(data, meta);
        } else if (type == "sensor info") {
            return formatSensorInfo(data, meta);
        } else if (type == "reported semloc") {
            return formatSemLoc(data, meta);
        } else if (type == "wifi") {
            return formatWifi(data, meta);
        } else if (type == "audio") {
            return formatAudio(data, meta);
        } else if (type == "geocoord") {
            return formatGeoCoord(data, meta);
        } else if (type == "accelerometer") {
            return formatAcc(data, meta);
        } else if (type == "linear accelerometer") {
            return formatLinearAcc(data, meta);
        } else if (type == "gravity") {
            return formatGravity(data, meta);
        } else if (type == "gyroscope") {
            return formatGyro(data, meta);
        } else if (type == "rotation") {
            return formatRotation(data, meta);
        } else if (type == "magnetic") {
            return formatMagnetic(data, meta);
        } else if (type == "light") {
            return formatLight(data, meta);
        } else if (type == "temperature") {
            return formatTemp(data, meta);
        } else if (type == "pressure") {
            return formatPressure(data, meta);
        } else if (type == "proximity") {
            return formatProximity(data, meta);
        } else if (type == "humidity") {
            return formatHumidity(data, meta);
        }

        return null;
    }


	private JSONObject getDeviceInfo() {
		final JSONObject deviceInfo = new JSONObject();
		try {
			// Device Info
			deviceInfo.put("make", Build.MANUFACTURER);
			deviceInfo.put("model", Build.MODEL);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return deviceInfo;
	}
	// Only call getMinDelay() before Gingerbread
	@SuppressLint("NewApi")
	private JSONArray getSensorInfoList() {
		final JSONArray sensorInfoList = new JSONArray();
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
					case Sensor.TYPE_ACCELEROMETER :
						type = "accelerometer";
						break;
					case Sensor.TYPE_AMBIENT_TEMPERATURE :
						type = "temperature";
						break;
					case Sensor.TYPE_GRAVITY :
						type = "gravity";
						break;
					case Sensor.TYPE_GYROSCOPE :
						type = "gyroscope";
						break;
					case Sensor.TYPE_LIGHT :
						type = "light";
						break;
					case Sensor.TYPE_LINEAR_ACCELERATION :
						type = "linear accelerometer";
						break;
					case Sensor.TYPE_MAGNETIC_FIELD :
						type = "magnetic";
						break;
					case Sensor.TYPE_PRESSURE :
						type = "pressure";
						break;
					case Sensor.TYPE_PROXIMITY :
						type = "proximity";
						break;
					case Sensor.TYPE_RELATIVE_HUMIDITY :
						type = "humidity";
						break;
					case Sensor.TYPE_ROTATION_VECTOR :
						type = "rotation";
						break;
					default :
						break;
				}

				if (type != null) {
					final JSONObject sensorInfo = new JSONObject();
					sensorInfo.put("sensor", type);
					sensorInfo.put("vendor", sensor.getVendor());
					sensorInfo.put("model", sensor.getName());
					sensorInfo.put("version", sensor.getVersion());
					sensorInfo.put("power", sensor.getPower());
					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
						sensorInfo.put("min delay", sensor.getMinDelay());
					}
					sensorInfo.put("max range", sensor.getMaximumRange());
					sensorInfo.put("resolution", sensor.getResolution());

					sensorInfoList.put(sensorInfo);
				}

				// TODO add audio, wifi, and bluetooth info
			}
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sensorInfoList;
	}

	private JSONObject formatDeviceInfo(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final JSONObject from = (JSONObject) data;
		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.optLong("epoch"));
			to.put("sysnano", meta.optLong("sysnano"));
			to.put("make", from.optString("make"));
			to.put("model", from.optString("model"));
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatSensorInfo(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final JSONObject from = (JSONObject) data;
		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.optLong("epoch"));
			to.put("sysnano", meta.optLong("sysnano"));
			to.put("sensor", from.optString("sensor"));
			to.put("vendor", from.optString("vendor"));
			to.put("model", from.optString("model"));
			to.put("version", from.optInt("version"));
			to.put("power", from.optDouble("power", 0));
			to.put("min delay", from.optInt("min delay"));
			to.put("max range", from.optDouble("max range", 0));
			to.put("resolution", from.optDouble("resolution", 0));
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}
	private JSONObject formatSemLoc(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final JSONObject from = (JSONObject) data;
		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.getLong("epoch"));
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("country", from.optString("country", null));
			to.put("state", from.optString("state", null));
			to.put("city", from.optString("city", null));
			to.put("street", from.optString("street", null));
			to.put("building", from.optString("building", null));
			to.put("locale", from.optString("locale", null));
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatWifi(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final ScanResult from = (ScanResult) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.getLong("epoch"));
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("BSSID", from.BSSID);
			to.put("SSID", from.SSID);
			to.put("capability", from.capabilities);
			to.put("frequency", from.frequency);
			to.put("RSSI", from.level);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatAudio(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final JSONObject from = (JSONObject) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("sysnano", meta.getLong("sysnano"));
			final String source = (from.getInt("source") == AudioSource.CAMCORDER)
					? "CAMCORDER"
					: "MIC";
			final int channel = (from.getInt("channel") == AudioFormat.CHANNEL_IN_MONO)
					? 1
					: 2;
			final int sampwidth = (from.getInt("sampwidth") == AudioFormat.ENCODING_PCM_16BIT)
					? 2
					: 1;
			final int nframes = from.getJSONArray("raw").length()
					/ (from.getInt("sampwidth") * from.getInt("channel"));
			to.put("source", source);
			to.put("channel", channel);
			to.put("sampwidth", sampwidth);
			to.put("nframes", nframes);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatGeoCoord(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final Location from = (Location) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", from.getTime());
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("longitude", from.getLongitude());
			to.put("latitude", from.getLatitude());
			to.put("altitude", from.getAltitude());
			to.put("bearing", from.getBearing());
			to.put("speed", from.getSpeed());
			to.put("accuracy", from.getAccuracy());
			to.put("provider", from.getProvider());
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatAcc(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final SensorEvent from = (SensorEvent) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.getLong("epoch"));
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("eventnano", from.timestamp);
			to.put("x", from.values[0]);
			to.put("y", from.values[1]);
			to.put("z", from.values[2]);
			to.put("accuracy", from.accuracy);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatLinearAcc(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final SensorEvent from = (SensorEvent) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.getLong("epoch"));
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("eventnano", from.timestamp);
			to.put("x", from.values[0]);
			to.put("y", from.values[1]);
			to.put("z", from.values[2]);
			to.put("accuracy", from.accuracy);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatGravity(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final SensorEvent from = (SensorEvent) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.getLong("epoch"));
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("eventnano", from.timestamp);
			to.put("x", from.values[0]);
			to.put("y", from.values[1]);
			to.put("z", from.values[2]);
			to.put("accuracy", from.accuracy);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatGyro(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final SensorEvent from = (SensorEvent) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.getLong("epoch"));
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("eventnano", from.timestamp);
			to.put("x", from.values[0]);
			to.put("y", from.values[1]);
			to.put("z", from.values[2]);
			to.put("accuracy", from.accuracy);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatRotation(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final SensorEvent from = (SensorEvent) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.getLong("epoch"));
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("eventnano", from.timestamp);
			to.put("xr", from.values[0]);
			to.put("yr", from.values[1]);
			to.put("zr", from.values[2]);
			if (from.values.length >= 4) {
				to.put("cos", from.values[3]);
			}
			if (from.values.length >= 5) {
				to.put("head_accuracy", from.values[4]);
			}
			to.put("accuracy", from.accuracy);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatMagnetic(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final SensorEvent from = (SensorEvent) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.getLong("epoch"));
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("eventnano", from.timestamp);
			to.put("x", from.values[0]);
			to.put("y", from.values[1]);
			to.put("z", from.values[2]);
			to.put("accuracy", from.accuracy);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatLight(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final SensorEvent from = (SensorEvent) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.getLong("epoch"));
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("eventnano", from.timestamp);
			to.put("light", from.values[0]);
			to.put("accuracy", from.accuracy);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatTemp(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final SensorEvent from = (SensorEvent) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.getLong("epoch"));
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("eventnano", from.timestamp);
			to.put("temp", from.values[0]);
			to.put("accuracy", from.accuracy);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatPressure(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final SensorEvent from = (SensorEvent) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.getLong("epoch"));
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("eventnano", from.timestamp);
			to.put("pressure", from.values[0]);
			to.put("accuracy", from.accuracy);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatProximity(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final SensorEvent from = (SensorEvent) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.getLong("epoch"));
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("eventnano", from.timestamp);
			to.put("proximity", from.values[0]);
			to.put("accuracy", from.accuracy);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}

	private JSONObject formatHumidity(final Object data, final JSONObject meta) {
		final JSONObject to = new JSONObject();
		final SensorEvent from = (SensorEvent) data;

		try {
			to.put("type", meta.getString("type"));
			to.put("id", DeviceUUID.getDeviceUUID(mContext));
			to.put("epoch", meta.getLong("epoch"));
			to.put("sysnano", meta.getLong("sysnano"));
			to.put("eventnano", from.timestamp);
			to.put("humidity", from.values[0]);
			to.put("accuracy", from.accuracy);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return to;
	}
}

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

package edu.berkeley.bearloc.sampler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import edu.berkeley.bearloc.util.SamplerSettings;

public class Acc implements Sampler, SensorEventListener {

	private boolean mBusy;
//	private int mSampleCap;
//	private int nSampleNum;

	private final Context mContext;
	private final SamplerListener mListener;
	private final Handler mHandler;
	private final SensorManager mSensorManager;
	private final Sensor mAcc;

	public static interface SamplerListener {
		public abstract void onAccEvent(SensorEvent event);
	}

	private final Runnable mPauseTask = new Runnable() {
		@Override
		public void run() {
			pause();
		}
	};

	public Acc(final Context context, final SamplerListener listener) {
		mContext = context;
		mListener = listener;
		mHandler = new Handler();
		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	@Override
	public boolean start() {
		if (mBusy == false && SamplerSettings.getAccEnable(mContext) == true) {
			if (mAcc == null) {
				SamplerSettings.setAccEnable(mContext, false);
				return false;
			}

			// final long duration = SamplerSettings.getAccDuration(mContext);
			// final int num = SamplerSettings.getAccCnt(mContext);
			final int delay = SamplerSettings.getAccDelay(mContext);
			// nSampleNum = 0;
			// mSampleCap = num;
			mSensorManager.registerListener(this, mAcc, delay);
			// mHandler.postDelayed(mPauseTask, duration);
			mBusy = true;
			return true;
		} else {
			return false;
		}
	}

	private void pause() {
		if (mBusy == true) {
			mBusy = false;
			mSensorManager.unregisterListener(this);
			mHandler.removeCallbacks(mPauseTask);
		}
	}

	@Override
	public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		if (event == null) {
			return;
		}

		if (mListener != null) {
			mListener.onAccEvent(event);
		}

		// nSampleNum++;
		// if (nSampleNum >= mSampleCap) {
		// pause();
		// }
	}

	@Override
	public void stop() {
		pause();
	}
}

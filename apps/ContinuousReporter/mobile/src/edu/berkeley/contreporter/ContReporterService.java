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

package edu.berkeley.contreporter;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import edu.berkeley.bearloc.BearLocService;
import edu.berkeley.bearloc.BearLocService.BearLocBinder;

public class ContReporterService extends Service {

	private BearLocService mBearLocService;
	private boolean mBound = false;
	private final ServiceConnection mBearLocConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName name,
				final IBinder service) {
			final BearLocBinder binder = (BearLocBinder) service;
			mBearLocService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			mBound = false;
		}
	};

	private IBinder mBinder;

	public class LocReporterBinder extends Binder {
		public ContReporterService getService() {
			// Return this instance so clients can call public methods
			return ContReporterService.this;
		}
	}

	@Override
	public void onCreate() {
		final Intent intent = new Intent(this, BearLocService.class);
		bindService(intent, mBearLocConn, Context.BIND_AUTO_CREATE);

		mBinder = new LocReporterBinder();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Unbind from the service
		if (mBound) {
			unbindService(mBearLocConn);
			mBound = false;
		}
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return mBinder;
	}

	public boolean collectData() {
		return mBearLocService.postData(null, null);
	}

	public void stopData() {
		mBearLocService.stopPostData();
	}
}

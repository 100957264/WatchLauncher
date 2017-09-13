package android.fise.com.fiseassitant;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class FiseShutdownService extends Service{
	SDCardListener mSDCardListener;
	SDCardListener mBindListener;
	SDCardListener mForbidListener;
	SDCardListener mNetworkListener;

	IntentFilter shutdown;
	FiseShutdownReceiver mFiseShutdownReceiver;

    IntentFilter loveFilter;
    AlertReceiver mAlertReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mFiseShutdownReceiver = new FiseShutdownReceiver();
		Log.d("fengqing","FiseShutdownService:--->onCreate: start ....");
		shutdown = new IntentFilter();
		shutdown.addAction("com.android.fise.ACTION_REBOOT");
		shutdown.addAction("com.android.fise.ACTION_SHUTDOWN");
		shutdown.addAction("com.android.fise.ACTION_AUDIO_MODE");
		shutdown.addAction("com.android.fise.ACTION_SCREEN_TIMEOUT");
		shutdown.addAction("net.wecare.watch_launcher.ACTION_LISTEN");
		shutdown.addAction("net.wecare.watch_launcher.ACTION_APP_INSTALL");
		shutdown.addAction("fise.intent.action.FORBID_ENABLE");
		shutdown.addAction("fise.intent.action.FORBID_DISABLE");
        shutdown.addAction("com.fise.action.LESSION_MODE");
        shutdown.addAction("com.fise.action.LOVE_REMIND");
		shutdown.setPriority(1000);
		this.registerReceiver(mFiseShutdownReceiver,shutdown);

		mSDCardListener = new SDCardListener(this,"/sdcard/watchlauncher/number/");
		mSDCardListener.startWatching();
		mBindListener = new SDCardListener(this,"/sdcard/watchlauncher/bind/");
		mBindListener.startWatching();
		mForbidListener = new SDCardListener(this,"/sdcard/watchlauncher/forbid/");
		mForbidListener.startWatching();
		mNetworkListener = new SDCardListener(this,"/sdcard/watchlauncher/network/");
		mNetworkListener.startWatching();

		mAlertReceiver = new AlertReceiver();
		loveFilter = new IntentFilter();
		loveFilter.addAction(AlertReceiver.EVENT_ACTION_LOVE_ALERT);
		loveFilter.addAction(AlertReceiver.EVENT_ACTION_FORBID_EBALE);
		loveFilter.addAction(AlertReceiver.EVENT_ACTION_FORBID_DISABLE);
		loveFilter.setPriority(1000);
		this.registerReceiver(mAlertReceiver,loveFilter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("fengqing","FiseShutdownService:--->onDestroy: start ....");
		mSDCardListener.stopWatching();
		mBindListener.stopWatching();
		mForbidListener.stopWatching();
		mNetworkListener.stopWatching();
		this.unregisterReceiver(mFiseShutdownReceiver);
        this.unregisterReceiver(mAlertReceiver);
	}
}

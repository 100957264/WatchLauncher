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
		shutdown.addAction("net.wecare.watch_launcher.ACTION_LISTEN");
		shutdown.addAction("net.wecare.watch_launcher.ACTION_APP_INSTALL");
		shutdown.addAction("net.wecare.watcha_launcher.SCREEN_ON");
		shutdown.addAction("net.wecare.watch_launcher.ACTION_BIND");
		shutdown.addAction("net.wecare.watch_launcher.ACTION_UNBIND");
		shutdown.addAction("com.fise.intent.action.STATUS_BAR");
		shutdown.addAction("android.intent.action.SCREEN_OFF");
		shutdown.addAction("android.intent.action.SCREEN_ON");
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
	}



	@Override
	public void onDestroy() {
		super.onDestroy();
		mSDCardListener.stopWatching();
		mBindListener.stopWatching();
		mForbidListener.stopWatching();
		mNetworkListener.stopWatching();
		this.unregisterReceiver(mFiseShutdownReceiver);
	}
}

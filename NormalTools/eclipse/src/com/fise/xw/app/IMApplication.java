package com.fise.xw.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.ui.activity.AboutActivity;
import com.fise.xw.utils.ImageLoaderUtil;
import com.fise.xw.utils.Logger;
import com.fise.xw.utils.PlaySound;

public class IMApplication extends Application {

	private Logger logger = Logger.getLogger(IMApplication.class);

	private static IMApplication Application;
	private static PlaySound playSound;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	@Override
	public void onCreate() {
		super.onCreate();
		logger.i("Application starts");
		Application = this;
		startIMService();
		playSound = new PlaySound(this);
		ImageLoaderUtil.initImageLoaderConfig(getApplicationContext());

	}

	private void startIMService() {
		logger.i("start IMService");
		Intent intent = new Intent();
		intent.setClass(this, IMService.class);
		startService(intent);
	}

	public static boolean gifRunning = true;// gif是否运行 gifRunning

	public static IMApplication getApplication() {
		return Application;
	}

	public static PlaySound getPlaySound() {
		return playSound;
	}

}

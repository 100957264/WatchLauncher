package com.fise.xiaoyu.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.telephony.TelephonyManager;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.service.SMSReceiver;
import com.fise.xiaoyu.step.UpdateUiCallBack;
import com.fise.xiaoyu.step.utils.SharedPreferencesUtils;
import com.fise.xiaoyu.ui.activity.LoadingActivity;
import com.fise.xiaoyu.ui.base.ActivityManager;
import com.fise.xiaoyu.utils.CommonUtil;
import com.fise.xiaoyu.utils.ImageLoaderUtil;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.PlaySound;
import com.lzy.okgo.OkGo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class IMApplication extends MultiDexApplication {

	private Logger logger = Logger.getLogger(IMApplication.class);
	private static boolean isFullInit = false;
	private static boolean ableCloseAll = true;

	private static IMApplication Application;
	private static PlaySound playSound;
	private boolean vedioStats;
	private PendingIntent sentPI;
	private PendingIntent deliverPI;

	private SharedPreferencesUtils sp;
	private SMSReceiver smsReceiver;

	public void setFullInit(Context context) {
		if (context instanceof LoadingActivity) {
			isFullInit = true;
			ableCloseAll = true;
		}
	}

	public boolean isFullInit() {
		return isFullInit;
	}

	public void goLoadingActivity() {
		boolean close = false;
		if (ableCloseAll) {
			synchronized (this) {
				if (ableCloseAll) {
					ableCloseAll = false;
					close = true;
				}
			}
			if (close) {
				Logger.dd(Logger.LOG_APPLICATION || Logger.LOG_ACTIVITY_NAME, "Activity will restart!");
				Intent intent = new Intent(this, LoadingActivity.class);
				startActivity(intent);
				Activity activity = ActivityManager.getInstance().currentActivity();
				ActivityManager.getInstance().finishAllActivityExcept(activity);
			}
		}
	}

	@Override
	public void onCreate() {
		Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onCreate");
		super.onCreate();
		logger.i("Application starts");
		EventBus.getDefault().register(this);
		Application = this;
//		LeakCanary.install(this);


		initData();
		startIMService();


		playSound = new PlaySound(this);
		ImageLoaderUtil.initImageLoaderConfig(getApplicationContext());
		CommonUtil.getSavePath(SysConstant.FILE_SAVE_TYPE_AUDIO);
		CommonUtil.setSavePath();
		vedioStats = false;
		OkGo.getInstance().init(this);
		registerSmsReceiver();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onLowMemory() {
		Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onLowMemory");
		super.onLowMemory();
	}

	@Override
	public void onTrimMemory(int level) {
		Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onTrimMemory");
		super.onTrimMemory(level);
	}

	@Override
	public void onTerminate() {
		Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName()+"	onTerminate");
		EventBus.getDefault().unregister(this);
		unregisterSmsReceiver();
		if (isBind) {
			this.unbindService(conn);
		}

		super.onTerminate();
	}

	public void setVedioStats(boolean vedioStats) {
		this.vedioStats = vedioStats;
	}

	public boolean getVedioStats() {
		return this.vedioStats;
	}


	private void startIMService() {
		Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	startIMService");
		logger.i("start IMService");
		Intent intent = new Intent();
		intent.setClass(this, IMService.class);
		startService(intent);

	}

	private boolean isBind = false;

	/**
	 * 启动计步
	 */
	private void initData() {
		sp = new SharedPreferencesUtils(this);
		//设置当前步数为0
	}


	/**
	 * 用于查询应用服务（application Service）的状态的一种interface，
	 * 更详细的信息可以参考Service 和 context.bindService()中的描述，
	 * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
	 */
	ServiceConnection conn = new ServiceConnection() {
		/**
		 * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
		 * @param name 实际所连接到的Service组件名称
		 * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onServiceConnected:IMApplication");
			IMService stepService = ((IMService.IMServiceBinder) service).getService();
			//设置初始化数据
//			String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "7000");
//			cc.setCurrentCount(Integer.parseInt(planWalk_QTY), stepService.getStepCount());

			//设置步数监听回调
			stepService.registerCallback(new UpdateUiCallBack() {
				@Override
				public void updateUi(int stepCount) {
					int dd = stepCount;
//					String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "7000");
//					cc.setCurrentCount(Integer.parseInt(planWalk_QTY), stepCount);
				}
			});
		}

		/**
		 * 当与Service之间的连接丢失的时候会调用该方法，
		 * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
		 * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
		 * @param name 丢失连接的组件名称
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onServiceConnected:IMApplication");

		}
	};


	public static boolean gifRunning = true;// gif是否运行 gifRunning

	public static IMApplication getApplication() {
		return Application;
	}

	public static PlaySound getPlaySound() {
		return playSound;
	}


	@Override
	protected void attachBaseContext(Context base) {
		Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	attachBaseContext");
		super.attachBaseContext(base);
		MultiDex.install(this);
//		initProcessNameAndPackageName(base);
//		if (!isProcessInit()) {
//			// other process install dex
//			MultiDex.install(this);
//		} else {
//			// init process continue
//		}
	}

	public static int getBillType() {

		TelephonyManager telManager = (TelephonyManager) Application.getSystemService(Context.TELEPHONY_SERVICE);
		// 获取SIM卡的IMSI码
		String imsi = telManager.getSubscriberId();
		//半段IMIS中的MNC
		if (imsi != null) {
			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {//因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号 //中国移动
				return DBConstant.IMSI_TYPE_MOBILE;

			} else if (imsi.startsWith("46001")) {

				//中国联通
				return DBConstant.IMSI_TYPE_UNICOM;

			} else if (imsi.startsWith("46003")) {

				//中国电信
				return DBConstant.IMSI_TYPE_TELECOM;

			}
		}
		return DBConstant.IMSI_TYPE_EMPTY;
	}

	public void registerSmsReceiver() {
		//处理返回的发送状态
		String SENT_SMS_ACTION = "SENT_SMS_ACTION";
		Intent sentIntent = new Intent(SENT_SMS_ACTION);
		sentPI = PendingIntent.getBroadcast(this, 0, sentIntent, 0);
		//处理返回的接收状态
		String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
		// create the deilverIntent parameter
		Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
		deliverPI = PendingIntent.getBroadcast(this, 0, deliverIntent, 0);
		// register the Broadcast Receivers
		if (smsReceiver != null) {
			return;
		}
		IntentFilter filter = new IntentFilter(SMSReceiver.SMS_ACTION);
		filter.addAction(SENT_SMS_ACTION);
		filter.addAction(DELIVERED_SMS_ACTION);
		smsReceiver = new SMSReceiver();
		registerReceiver(smsReceiver, filter);
	}

	private void unregisterSmsReceiver() {
		if (smsReceiver != null) {
			unregisterReceiver(smsReceiver);
			smsReceiver = null;
		}
	}

	/**
	 * 直接调用短信接口发短信
	 * @param phoneNumber
	 * @param message
	 */
	public void sendSMS(String phoneNumber, String message) {
		//获取短信管理器
		android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
		//拆分短信内容（手机短信长度限制）
		List<String> divideContents = smsManager.divideMessage(message);
		// for (String text : divideContents) {
		// 	smsManager.sendTextMessage(phoneNumber, null, text, sentPI, deliverPI);
		// }
		if (message.length() > 70) {
			ArrayList<String> msgs = smsManager.divideMessage(message);
			ArrayList<PendingIntent> sentIntents = new ArrayList<>();
			for (int i = 0; i < msgs.size(); i++) {
				sentIntents.add(sentPI);
			}
			smsManager.sendMultipartTextMessage(phoneNumber, null, msgs, sentIntents, null);
		} else {
			smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliverPI);
		}
	}

	// EventBus 事件驱动
	@Subscribe(priority = SysConstant.SERVICE_EVENTBUS_PRIORITY)
	public void onMessageEvent(LoginEvent event) {
		switch (event) {
			case LOGIN_OK:
			case LOCAL_LOGIN_SUCCESS:
				if (smsReceiver != null) {
					smsReceiver.setReceivedAble(true);
				}
				break;
			case LOGIN_OUT:
				if (smsReceiver != null) {
					smsReceiver.setReceivedAble(false);
				}
				break;
		}
	}

	public String getVersion() {

		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			String version = info.versionName;
			return "" + version;
		} catch (Exception e) {
			e.printStackTrace();
			return this.getString(R.string.app_version);
		}
	}
}

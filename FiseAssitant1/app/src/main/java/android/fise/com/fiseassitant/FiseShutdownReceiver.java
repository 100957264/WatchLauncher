package android.fise.com.fiseassitant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.provider.Settings;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class FiseShutdownReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d("fengqing","FiseShutdownReceiver :onReceive: action =" + action);
		if(action.equals("com.android.fise.ACTION_REBOOT")){
			try {
				 PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
				 pm.reboot(null);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(action.equals("com.android.fise.ACTION_SHUTDOWN")){
			/*try {
				Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
				Method getService = ServiceManager.getMethod("getService", java.lang.String.class);
				Object oRemoteService = getService.invoke(null,Context.POWER_SERVICE);
				Class<?> cStub = Class.forName("android.os.IPowerManager$Stub");
				Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
				Object oIPowerManager = asInterface.invoke(null, oRemoteService);
				Method shutdown = oIPowerManager.getClass().getMethod("shutdown",boolean.class,boolean.class);
				shutdown.invoke(oIPowerManager,false,true);
			}catch(Exception e){
				e.printStackTrace();
			}*/
			 PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
			// pm.fiseShutdown();
		} else if(action.equals("net.wecare.watch_launcher.ACTION_APP_INSTALL")){
			String result = FiseInstallApkUtil.installSilently("/sdcard/watchlauncher/WatchLauncher.apk");
			Log.d("fengqing","result =" + result);
		}else if(action.equals("fise.intent.action.FORBID_ENABLE")){
			Settings.System.putString(context.getContentResolver(),"forbid_status","1");
		}else if(action.equals("fise.intent.action.FORBID_DISABLE")){
			Settings.System.putString(context.getContentResolver(),"forbid_status","0");
		}
		else if(action.equals("com.android.fise.ACTION_AUDIO_MODE")){
			AudioManager audioManager =  (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			Bundle bundle = intent.getExtras();
			int audioMode = bundle.getInt("audiomode");
			audioManager.setRingerMode(audioMode);
		}else if(action.equals("com.android.fise.ACTION_SCREEN_TIMEOUT")){
			Bundle bundle = intent.getExtras();
			long time = bundle.getLong("screenofftimeout");
			Settings.System.putLong(context.getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT,time);
		}else if(action.equals("com.fise.action.LOVE_REMIND")){
			Bundle bundle = intent.getExtras();
			String loveName = bundle.getString("loveName");
			long startTime = bundle.getLong("lovebt");
			long endTime = bundle.getLong("loveet");
			int status = bundle.getInt("loves");
			int tpye = bundle.getInt("lovet");
			ArrayList week = bundle.getCharSequenceArrayList("loveweek");
            Log.d("fengqing","morningStart =" + getUTCTime(startTime));
			setAlarmAlert(context,status,startTime,endTime,loveName,week,tpye);
		}else if(action.equals("com.fise.action.LESSION_MODE")){
			Bundle bundle = intent.getExtras();
			long morningStart = bundle.getLong("forms");
			long morningEnd = bundle.getLong("forme");
			long afterStart = bundle.getLong("forras");
			long afterEnd = bundle.getLong("forae");
			int status = bundle.getInt("fors");
			int tpye = bundle.getInt("fort");
			ArrayList week = bundle.getCharSequenceArrayList("forweek");
            Log.d("fengqing","morningStart =" + getUTCTime(morningStart) + ",afterStart =" + getUTCTime(afterStart));
			setAlarmAlert(context,status,morningStart,morningEnd,null,week,tpye);
			setAlarmAlert(context,status,afterStart,afterEnd,null,week,tpye);
		}
	}
	private String getUTCTime(long time){
        String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date(time));
        return date;
    }
	/**
	 * 设置闹钟爱心提醒和禁用
	 */
	public void setAlarmAlert( Context context,int state, long startTime, long endTime, String loveName, ArrayList week, int type) {
		Calendar mCalendar = Calendar.getInstance();
		AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (type == 1) {
			Intent alertIntent = new Intent(AlertReceiver.EVENT_ACTION_LOVE_ALERT);
			alertIntent.setClass(context, AlertReceiver.class);
			Bundle bundle = new Bundle();
			bundle.putString("love", loveName);
			bundle.putCharSequenceArrayList("weeklist", week);
			alertIntent.putExtras(bundle);
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, alertIntent, 0);
			long currentTime = System.currentTimeMillis();
			mCalendar.setTimeInMillis(startTime);
			mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			if (currentTime > mCalendar.getTimeInMillis()) {
				mCalendar.add(Calendar.DAY_OF_MONTH, 1);//如果当前时间大于设置时间，从第二天开始
			}
			if (state == 1) {
				mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), 1000 * 60 * 60 * 24, pi);
			} else {
				mAlarmManager.cancel(pi);
			}
		} else if (type == 0) {
			Intent forbidEnable = new Intent(AlertReceiver.EVENT_ACTION_FORBID_EBALE);
			forbidEnable.setClass(context, AlertReceiver.class);
			Bundle bundle = new Bundle();
			bundle.putCharSequenceArrayList("forbid_week", week);
			forbidEnable.putExtras(bundle);
			PendingIntent enable = PendingIntent.getBroadcast(context, 0, forbidEnable, 0);

			Intent forbidDisable = new Intent(AlertReceiver.EVENT_ACTION_FORBID_DISABLE);
			forbidDisable.setClass(context, AlertReceiver.class);
			forbidDisable.putExtras(bundle);
			PendingIntent disable = PendingIntent.getBroadcast(context, 0, forbidDisable, 0);
			if (state == 1) {
				long currentTime = System.currentTimeMillis();
				mCalendar.setTimeInMillis(startTime);
				mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
				Calendar mCalenderDisable = Calendar.getInstance();
				mCalenderDisable.setTimeInMillis(endTime);
				mCalenderDisable.setTimeZone(TimeZone.getTimeZone("GMT+8"));
				if (currentTime > mCalenderDisable.getTimeInMillis()) {
					mCalendar.add(Calendar.DAY_OF_MONTH, 1);//如果当前时间大于设置时间，从第二天开始
					mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), 1000 * 60 * 60 * 24, enable);
				} else if (currentTime > mCalendar.getTimeInMillis() && currentTime < mCalenderDisable.getTimeInMillis()) {
					mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60 * 24, enable);
				} else {
					mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), 1000 * 60 * 60 * 24, enable);
				}
				if (currentTime > mCalenderDisable.getTimeInMillis()) {
					mCalenderDisable.add(Calendar.DAY_OF_MONTH, 1);
				}
				mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalenderDisable.getTimeInMillis(), 1000 * 60 * 60 * 24, disable);
			} else {
				mAlarmManager.cancel(enable);
				mAlarmManager.cancel(disable);
			}

		}
	}
}

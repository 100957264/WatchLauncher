package android.fise.com.fiseassitant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import java.lang.reflect.Method;

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
			try {
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
			}
		}else if(action.equals("net.wecare.watch_launcher.ACTION_LISTEN")){
            Log.d("fengqing","net.wecare.watch_launcher.ACTION_LISTEN intent receuved");
			Settings.System.putInt(context.getContentResolver(), "listen_call",1);
		}else if(action.equals("net.wecare.watch_launcher.ACTION_APP_INSTALL")){
			String result = FiseInstallApkUtil.installSilently("/sdcard/watchlauncher/WatchLauncher.apk");
			Log.d("fengqing","result =" + result);
		}else if(action.equals("net.wecare.watcha_launcher.SCREEN_ON")){
			PowerManager powerManager = (PowerManager) (context.getSystemService(Context.POWER_SERVICE));
		    PowerManager.WakeLock wakeLock = null;
            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "fise");
		    wakeLock.acquire(15*1000);
			Log.d("fengqing","intent =net.wecare.watcha_launcher.SCREEN_ON" );
		}
	}
}

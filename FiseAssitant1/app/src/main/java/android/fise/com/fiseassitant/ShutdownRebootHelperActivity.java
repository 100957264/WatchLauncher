package android.fise.com.fiseassitant;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.ComponentName;
import android.media.AudioManager;
import android.content.Context;

import java.util.Calendar;
import java.util.TimeZone;

public class ShutdownRebootHelperActivity extends Activity {
    Button rebootButton;
    Button shutdownButton;
    Button mormalMode;
    Button installApp;
	Button startService;
	Button silentMode;
	Button vibrateMode;
	Button screenTimeout;
    Button loveAlert;
    Button forbidMode;
    Button forbidDisable;
	AudioManager audioManager;
	Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shutdown_reboot_helper);
        rebootButton = (Button)findViewById(R.id.button_reboot);
		mContext = this;


        audioManager =  (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,10,AudioManager.FLAG_SHOW_UI);
        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        rebootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent rebootIntent = new Intent();
                rebootIntent.setAction("com.android.fise.ACTION_REBOOT");
                sendBroadcast(rebootIntent);*/
                TestUTCTime();
            }
        });
        shutdownButton = (Button) findViewById(R.id.button_shutdown);
        shutdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shutdownIntent = new Intent();
                shutdownIntent.setAction("com.android.fise.ACTION_SHUTDOWN");
                sendBroadcast(shutdownIntent);
            }
        });
        mormalMode = (Button)findViewById(R.id.normal_mode);
        mormalMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                Intent normalIntent = new Intent("com.android.fise.ACTION_AUDIO_MODE");
                Bundle bundle = new Bundle();
                bundle.putInt("audiomode",AudioManager.RINGER_MODE_NORMAL);
                normalIntent.putExtras(bundle);
                sendBroadcast(normalIntent);
            }
        });
		silentMode = (Button)findViewById(R.id.silent_mode);
        silentMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				Intent silentIntent = new Intent("com.android.fise.ACTION_AUDIO_MODE");
                Bundle bundle = new Bundle();
                bundle.putInt("audiomode",AudioManager.RINGER_MODE_SILENT);
                silentIntent.putExtras(bundle);
                sendBroadcast(silentIntent);
				
            }
        });
	    vibrateMode = (Button)findViewById(R.id.vibrate_mode);
        vibrateMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                Intent vibrateIntent = new Intent("com.android.fise.ACTION_AUDIO_MODE");
                Bundle bundle = new Bundle();
                bundle.putInt("audiomode",AudioManager.RINGER_MODE_VIBRATE);
                vibrateIntent.putExtras(bundle);
                sendBroadcast(vibrateIntent);
            }
        });
        installApp = (Button)findViewById(R.id.install_app);
        installApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent installIntent = new Intent();
                installIntent.setAction("net.wecare.watch_launcher.ACTION_APP_INSTALL");
                sendBroadcast(installIntent);
                Log.d("fengqing","Install App has been sent successfull");
            }
        });
		startService = (Button)findViewById(R.id.start_service);
        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent();
                serviceIntent.setComponent(new ComponentName("android.fise.com.fiseassitant",
                    "android.fise.com.fiseassitant.FiseShutdownService"));
				startService(serviceIntent);
            }
        });
		screenTimeout = (Button)findViewById(R.id.screen_timeout);
        screenTimeout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //Settings.System.putLong(mContext.getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT,30000);
			   Intent screenIntent = new Intent("com.android.fise.ACTION_SCREEN_TIMEOUT");
			   Bundle timeBundle = new Bundle();
               timeBundle.putLong("screenofftimeout",30 * 1000);
			   screenIntent.putExtras(timeBundle);
               sendBroadcast(screenIntent);
            }
        });
        loveAlert = (Button)findViewById(R.id.love_alert);
        loveAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mCalendar = Calendar.getInstance();
                mCalendar.setTimeInMillis(System.currentTimeMillis());
                mCalendar.add(Calendar.SECOND,5);
             Intent loveIntent = new Intent(AlertReceiver.EVENT_ACTION_LOVE_ALERT);
             loveIntent.setClass(ShutdownRebootHelperActivity.this,AlertReceiver.class) ;
             AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
             PendingIntent pi = PendingIntent.getBroadcast(ShutdownRebootHelperActivity.this, 0, loveIntent, 0);
             mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(),1000*10,pi);
                Log.d("fengqing","love button is clicked ,send love alarm");
            }
        });
        forbidMode = (Button)findViewById(R.id.forbid_mode);
        forbidMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar mCalendar = Calendar.getInstance();
                mCalendar.setTimeInMillis(System.currentTimeMillis());
                mCalendar.add(Calendar.SECOND,5);
                Intent loveIntent = new Intent(AlertReceiver.EVENT_ACTION_FORBID_EBALE);
                loveIntent.setClass(ShutdownRebootHelperActivity.this,AlertReceiver.class) ;
                AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                PendingIntent pi = PendingIntent.getBroadcast(ShutdownRebootHelperActivity.this, 0, loveIntent, 0);
                mAlarmManager.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(),pi);
                Log.d("fengqing","forbidMode is clicked ,send love alarm");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Calendar mCalendar = Calendar.getInstance();
                        mCalendar.setTimeInMillis(System.currentTimeMillis());
                        mCalendar.add(Calendar.SECOND,5);
                        Intent loveIntent = new Intent(AlertReceiver.EVENT_ACTION_FORBID_DISABLE);
                        loveIntent.setClass(ShutdownRebootHelperActivity.this,AlertReceiver.class) ;
                        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        PendingIntent pi = PendingIntent.getBroadcast(ShutdownRebootHelperActivity.this, 0, loveIntent, 0);
                        mAlarmManager.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(),pi);
                        Log.d("fengqing","forbidDisable is clicked ,send love alarm");
                    }
                },5000);
            }
        });
        forbidDisable = (Button)findViewById(R.id.forbid_disable);
        forbidDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    private void TestUTCTime(){
       Calendar mCalendar =Calendar.getInstance();
        long currentTime = System.currentTimeMillis();
        Log.d("fengqing","currentTime =" + currentTime);
        mCalendar.setTimeInMillis(currentTime);
        mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        mCalendar.set(Calendar.HOUR_OF_DAY,21);
        mCalendar.set(Calendar.MINUTE,12);
        mCalendar.set(Calendar.SECOND,0);
       // mCalendar.set(Calendar.MILLISECOND,0);
        long time = mCalendar.getTimeInMillis();
        String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date(time));

        Log.d("fengqing","time = " + time + ",date =" + date);
    }
}

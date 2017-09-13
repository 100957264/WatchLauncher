package com.fise.xiaoyu.imservice.service;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.fise.xiaoyu.DB.entity.WhiteEntity;
import com.fise.xiaoyu.app.IMApplication;
import com.fise.xiaoyu.imservice.manager.IMDeviceManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;

import java.util.List;

/**
 * Created by qingfeng on 2017/9/1.
 */

public class SOSService extends Service{
    String TAG = "SOSService";
    SOSBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    WhiteEntity mWhiteEntity;
    TelephonyManager mTelephonyManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate:SOSService start...");
        mReceiver = new SOSBroadcastReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.fise.xiaoyu.ACTION_SOS");
        mIntentFilter.setPriority(1000);
        this.registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mReceiver);

    }

    class SOSBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //press power key three times,the system will send this broadcast
            if(action.equals("com.fise.xiaoyu.ACTION_SOS")){
                Log.d(TAG,"SOSBroadcastReceiver.onReceive ...received the sos intent");
                IMDeviceManager mIMDeviceManager = IMDeviceManager.instance();
                List<WhiteEntity> sosPhone = mIMDeviceManager.getAlarmListContactList(IMLoginManager.instance().getLoginId());
                for(WhiteEntity we:sosPhone){
                    mWhiteEntity = we;
                    String number = mWhiteEntity.getPhone();
                    if(number != null) {
                        dialSOSNumber(context, number);
                    }
                }
            }
        }
    }
    public void dialSOSNumber(Context context,String number){
        mTelephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        Intent sosIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+number));
        sosIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);
        if(result == PackageManager.PERMISSION_GRANTED){
            context.startActivity(sosIntent);
        }
    }
}

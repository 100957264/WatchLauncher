package com.fise.xiaoyu.imservice.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.fise.xiaoyu.imservice.manager.IMReconnectManager;

/**
 * Created by weileiguan on 2017/5/20 0020.
 */
public class MobileBroadReceive extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //**判断当前的网络连接状态是否可用*/
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if ( info != null && info.isAvailable()){
            //当前网络状态可用
            IMReconnectManager.instance().tryReconnectTest();
        }else {
            //当前网络不可用

        }
    }
}

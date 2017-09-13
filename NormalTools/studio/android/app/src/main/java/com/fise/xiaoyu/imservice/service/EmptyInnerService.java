package com.fise.xiaoyu.imservice.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by lenovo on 2017/7/24.
 */

public class EmptyInnerService extends Service {
    private  final int GRAY_SERVICE_ID = 1001;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(GRAY_SERVICE_ID, new Notification());
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

}

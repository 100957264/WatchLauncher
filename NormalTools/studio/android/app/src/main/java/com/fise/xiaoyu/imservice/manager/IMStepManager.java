package com.fise.xiaoyu.imservice.manager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import com.fise.xiaoyu.imservice.callback.Packetlistener;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMMessage;
import com.fise.xiaoyu.ui.activity.MainActivity;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

/**
 * 定时发送位置
 */
@SuppressLint("NewApi") public class IMStepManager extends  IMManager{


    private static IMStepManager inst = new IMStepManager();
    private static IMLoginManager loginManager = IMLoginManager.instance();
    public static IMStepManager instance() {
        return inst;
    }
       
    private Logger logger = Logger.getLogger(IMStepManager.class);
    private final int HEARTBEAT_INTERVAL = 5* 60*1000;//
    private final String ACTION_SENDING_POSTISTION = "com.fise.xiaoyu.imservice.manager.impostistionbeatmanager";
    private PendingIntent pendingIntent;

    @Override
    public void doOnStart() {

    } 

    // 启动上传坐标
    public void onStartPointSuccess(){
       // logger.e("heartbeat#onLocalNetOk");

    }

    @Override
    public void reset() {

    }



}

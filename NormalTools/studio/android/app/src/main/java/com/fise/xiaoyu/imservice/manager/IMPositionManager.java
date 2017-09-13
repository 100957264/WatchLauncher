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
import com.fise.xiaoyu.protobuf.IMUserAction;
import com.fise.xiaoyu.ui.activity.MainActivity;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

/**
 * 定时发送位置
 */
@SuppressLint("NewApi") public class IMPositionManager  extends  IMManager{


    private static IMPositionManager inst = new IMPositionManager();
    private static IMLoginManager loginManager = IMLoginManager.instance();
    public static IMPositionManager instance() {
        return inst;
    }
       
    private Logger logger = Logger.getLogger(IMPositionManager.class);
    private final int HEARTBEAT_INTERVAL =  5* 60*1000;// 5* 60*1000
    private final String ACTION_SENDING_POSTISTION = "com.fise.xiaoyu.imservice.manager.impostistionbeatmanager";
    private PendingIntent pendingIntent;

    @Override
    public void doOnStart() {

    } 

    // 启动上传坐标
    public void onStartPointSuccess(){
       // logger.e("heartbeat#onLocalNetOk");
    	 
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SENDING_POSTISTION); 
        
        ctx.registerReceiver(imReceiver, intentFilter);
        //获取AlarmManager系统服务
        schedulePostion(HEARTBEAT_INTERVAL);
    }

    @Override
    public void reset() {
        logger.d("PositionManager#reset begin");
        try {
            ctx.unregisterReceiver(imReceiver);
            cancelHeartbeatTimer();
            logger.d("PositionManager#reset stop");
        }catch (Exception e){
            logger.e("PositionManager#reset error:%s",e.getCause());
        }
    }

    // MsgServerHandler 直接调用
    public void onMsgServerDisconn(){
        logger.w("PositionManager#onChannelDisconn");
        cancelHeartbeatTimer();
    }

    private void cancelHeartbeatTimer() {
        logger.w("PositionManager#cancelHeartbeatTimer");
        if (pendingIntent == null) {
            logger.w("PositionManager#pi is null");
            return;
        }
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }


    private void schedulePostion(int seconds){
        logger.d("PositionManager#PositionManager every %d seconds", seconds);
        if (pendingIntent == null) {
            logger.w("heartbeat#fill in pendingintent");
            Intent intent = new Intent(ACTION_SENDING_POSTISTION);
            pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
            if (pendingIntent == null) {
                logger.w("heartbeat#scheduleHeartbeat#pi is null");
                return;
            }
        }
 
         
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);   
       am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() , seconds, pendingIntent);
                
        //am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + seconds, seconds, pendingIntent);
    } 
       

    /**--------------------boradcast-广播相关-----------------------------*/
    private BroadcastReceiver imReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            logger.w("heartbeat#im#receive action:%s", action);
            if (action.equals(ACTION_SENDING_POSTISTION)) {
            	sendLocationPacket();
            }
        }
    };

    
    /**
     *  发送位置信息数据包
     */
    public void sendLocationPacket(){
        logger.d("heartbeat#sendLocationPacket");
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "teamtalk_heartBeat_wakelock");
        wl.acquire();
        try { 
        	
            final long timeOut = 5*1000;
            int usrId = loginManager.getLoginId();
            double longitude = MainActivity.longitude;
            double latitude = MainActivity.latitude;
            int BatteryN = MainActivity.BatteryN;
            int signalN = Utils.getIconLevel(MainActivity.signalN);//
            IMBaseDefine.PosFromType from_type =  IMBaseDefine.PosFromType.POS_FROM_GPS;


            // 高德地图 定位类型对照表 http://lbs.amap.com/api/android-location-sdk/guide/utilities/location-type/
            if(MainActivity.locationType == 1)
            {
                from_type =  IMBaseDefine.PosFromType.POS_FROM_GPS;
            }else if(MainActivity.locationType == 5)
            {
                from_type =  IMBaseDefine.PosFromType.POS_FROM_WIFI;
            }else if(MainActivity.locationType == 6)
            {
                from_type =  IMBaseDefine.PosFromType.POS_FROM_BASE;

            }else{
                from_type =  IMBaseDefine.PosFromType.POS_FROM_GPS;
            }

            if(loginManager.getLoginInfo()!=null&&Utils.isClientType(loginManager.getLoginInfo()))
            {

                IMBaseDefine.BaseWiInfo base_info = IMBaseDefine.BaseWiInfo.newBuilder()
                        .setLactionX(latitude+"")
                        .setLactionY(longitude+"")
                        .setFromType(from_type)
                        .setBattery(BatteryN)
                        .setSq(signalN)
                        .build();

                int step =  MainActivity.stepNum ;
                IMBaseDefine.JsonObject json =  IMBaseDefine.JsonObject.newBuilder()
                        .setKeyName("step")
                        .setKeyValue(step +"")
                        .build();

                IMBaseDefine.EventInfo event_info = IMBaseDefine.EventInfo.newBuilder()
                        .setEventKey(IMBaseDefine.EventKey.EVENT_KEY_REPORT_STEP) //EVENT_KEY_REPORT_STEP
                        .setWiInfo(base_info)
                        .setEventLevel(IMBaseDefine.EventLevel.EVENT_LEVEL_COLLECT)
                        .addKeyMap(json)
                        .build();

                IMMessage.IMPublishEventReq  publishEventReq =  IMMessage.IMPublishEventReq.newBuilder()
                        .setUserId(usrId)
                        .setCreateTime(0)
                        .setEventInfo(event_info)
                        .build();

                int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
                int cid = IMBaseDefine.MessageCmdID.CID_MSG_EVENT_PUBLISH_REQ_VALUE; //CID_MSG_EVENT_PUBLISH_REQ


                IMSocketManager.instance().sendRequest(publishEventReq,sid,cid,new Packetlistener(timeOut) {
                    @Override
                    public void onSuccess(Object response) {
                    }

                    @Override
                    public void onFaild() {
                    }

                    @Override
                    public void onTimeout() {
                    }
                });

            }


         /*

            IMUserAction.RTLocation imLocation = IMUserAction.RTLocation.newBuilder()
            		.setUserId(usrId)
            		.setLat(String.valueOf(latitude))
            		.setLng(String.valueOf(longitude))
            		.setSq(signalN)
            		.setBattery(BatteryN)
            		.build();

            int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
            int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_LOCATION_VALUE;

            IMSocketManager.instance().sendRequest(imLocation,sid,cid,new Packetlistener(timeOut) {
                @Override
                public void onSuccess(Object response) {
                }

                @Override
                public void onFaild() {
                }

                @Override
                public void onTimeout() {
                }
            });*/


        } finally {
            wl.release(); 
        }
    }
}

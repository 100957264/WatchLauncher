package com.fise.xiaoyu.imservice.manager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.utils.Logger;

import java.util.List;

/**
  * 定时请求位置信息
 */
@SuppressLint("NewApi") public class IMReqPositionManager  extends  IMManager{
 

    private static IMReqPositionManager inst = new IMReqPositionManager();
    private static IMLoginManager loginManager = IMLoginManager.instance();
    private static IMContactManager contactManager = IMContactManager.instance();
    
    public static IMReqPositionManager instance() {
        return inst;
    } 
       
    private Logger logger = Logger.getLogger(IMReqPositionManager.class);
    private final int HEARTBEAT_INTERVAL =  10* 60*1000;// * 1000;//   10* 60
    private final String ACTION_SENDING_REQ_POSTISTION = "com.fise.xiaoyu.imservice.manager.reqimpostistionbeatmanager";
    private PendingIntent pendingIntent;
      
    @Override
    public void doOnStart() {

    }

    // 启动上传坐标
    public void onStartPointSuccess(){
       // logger.e("heartbeat#onLocalNetOk");
    	 
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SENDING_REQ_POSTISTION); 
        
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
            Intent intent = new Intent(ACTION_SENDING_REQ_POSTISTION);
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
            if (action.equals(ACTION_SENDING_REQ_POSTISTION)) {
            	 
                List<UserEntity> list = contactManager.getContacMonitorList();  
                
                for(int i = 0;i<list.size();i++){
                	sendLocationPacket(list.get(i).getPeerId());
                }
            }
        }
    };

    /**
     *  
     * @param id
     */
    public void sendLocationPacket(int id){
        logger.d("heartbeat#sendLocationPacket");
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "teamtalk_heartBeat_wakelock");
        wl.acquire();
         
        try {
        	 
            int usrId = loginManager.getLoginId(); 
     
//            IMUserAction.LocationReq imLocation = IMUserAction.LocationReq.newBuilder()
//            		.setUserId(id)
//            		.setFromId(usrId)
//            		.setStartTime(0)
//            		.setEndTime(0)
//            		.build();
//
//            int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
//            int cid = IMBaseDefine.UserActionCmdID.CID_LOCATION_REQ_VALUE;
//
//            IMSocketManager.instance().sendRequest(imLocation,sid,cid);
            if(loginManager.getLoginInfo()!=null)
            {

            }


        } finally {
            wl.release(); 
        }
    }
}

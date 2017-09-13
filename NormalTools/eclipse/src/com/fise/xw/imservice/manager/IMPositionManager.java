package com.fise.xw.imservice.manager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.widget.Toast;

import com.fise.xw.imservice.callback.Packetlistener;
import com.fise.xw.protobuf.IMBaseDefine;
import com.fise.xw.protobuf.IMUserAction;
import com.fise.xw.ui.activity.MainActivity;
import com.fise.xw.utils.Logger;
import com.fise.xw.utils.Utils;

/**
 * @author : yingmu on 15-3-26.
 * @email : yingmu@mogujie.com.
 *
 * 备注: 之前采用netty(3.6.6-fianl)支持通道检测IdleStateHandler,发现有些机型
 * 手机休眠之后IdleStateHandler 定时器HashedWheelTimer可能存在被系统停止关闭的现象
 * 所以采用AlarmManager 进行心跳的检测
 *
 * 登陆之后就开始触发心跳检测 【仅仅是在线，重练就会取消的】
 * 退出reset 会释放alarmManager 资源
 */
@SuppressLint("NewApi") public class IMPositionManager  extends  IMManager{
 

    private static IMPositionManager inst = new IMPositionManager();
    private static IMLoginManager loginManager = IMLoginManager.instance();
    public static IMPositionManager instance() {
        return inst;
    }
       
    private Logger logger = Logger.getLogger(IMPositionManager.class);
    private final int HEARTBEAT_INTERVAL =  10* 60*1000;// * 1000;//   10* 60
    private final String ACTION_SENDING_POSTISTION = "com.fise.xw.imservice.manager.impostistionbeatmanager";
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
            });
        } finally {
            wl.release(); 
        }
    }
}

package com.fise.xiaoyu.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.app.IMApplication;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.DevVedioInfo;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.entity.OnLineVedioMessage;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.PriorityEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.ui.base.TTGuideBaseActivity;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.Utils;

import org.anyrtc.core.AnyRTMP;
import org.anyrtc.core.RTMPGuestHelper;
import org.anyrtc.core.RTMPGuestKit;
import org.anyrtc.core.RTMPHosterHelper;
import org.anyrtc.core.RTMPHosterKit;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Eric on 2016/9/16.
 */
public class HosterActivity extends TTGuideBaseActivity implements RTMPHosterHelper ,RTMPGuestHelper {
    private RTMPHosterKit mHoster = null;
    private static IMService imService;

    private RTMPGuestKit mGuest = null;
    private SurfaceViewRenderer mSurfaceView = null;
    private VideoRenderer mRenderer = null;

    private int currentUserId;
    private SurfaceViewRenderer mPullSurfaceView = null;
    private VideoRenderer mPullRenderer = null;
    private Button switch_camera;
    private Button exchange_vedio;
    private Button cancel_vedio;


    private IMBaseImageView user_portrait;
    private TextView  vedio_name_text;
    private  RelativeLayout   cancel_layout;
    private UserEntity userInfo;


    private RelativeLayout cancel_vedio_layout;
    private RelativeLayout switch_camera_layout;
    private RelativeLayout switch_voice_relative;
    private ImageView m_suface_view_bg;
    private  Button   cancel_vedio_end;


    private int call_time = 0;
    private Timer timer = new Timer();
    private TextView vedio_time;
    private boolean isBusy = false;

    private String pushUrl;
    private String pullUrl;

    //是否拒绝
    private boolean isRefuse = false;
    private boolean isTimeOut = false;

    //发起超时处理
    private boolean isAnswer = false;
    private int call_answer = 0;
    private Timer Answer = new Timer();

    private int  vedioType;
    private AudioManager audioManager;


    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101: {
                    updateView();
                }
                break;

            }
            super.handleMessage(msg);
        }
    };
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("config#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                // 后台服务启动链接失败
                return;
            }

            int fromId = imService.getLoginManager().getLoginId();
            userInfo = imService.getContactManager().findFriendsContact(currentUserId);

            if(userInfo ==null){
                return;
            }

            //计时
            Answer.schedule(answerTask, 1000, 1000);       // timeTask

            initView();
            //发起一个视频请求
            if(IMApplication.getApplication().getVedioStats()){
                closeBusyActivity();
                stopBusyRtmp();
                isBusy = true;
            }else{

                if(vedioType != DBConstant.DEV_VEDIO_TYPE){
                    OnLineVedioMessage vedioReqMessage = OnLineVedioMessage.buildForSend("", fromId, currentUserId, DBConstant.MSG_TYPE_VIDEO_CALL);
                    imService.getMessageManager().sendVedioReq(vedioReqMessage);
                }else{
                    statRtmp(pushUrl);
                }

                IMApplication.getPlaySound().Player(HosterActivity.this,R.raw.vido_call);
                IMApplication.getApplication().setVedioStats(true);
                isBusy = false;
            }
        }

        @Override
        public void onServiceDisconnected() {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_hoster);

        //同意之后的　挂断　
        cancel_vedio_end = (Button) findViewById(R.id.cancel_vedio_end);
        cancel_vedio_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                CloseActivity();
                stopRtmp();
                IMApplication.getApplication().setVedioStats(false);
                cancel_vedio_end.setEnabled(false);
            }
        });


        //视频的时间
        vedio_time = (TextView) findViewById(R.id.vedio_time);
        //开始前的背景
        m_suface_view_bg = (ImageView) findViewById(R.id.m_suface_view_bg);
     //   m_suface_view_bg.setVisibility(View.GONE);

        //没有视频前的　背景
        switch_camera_layout = (RelativeLayout) findViewById(R.id.switch_camera_relative);
        switch_camera_layout.setVisibility(View.GONE);

        switch_voice_relative = (RelativeLayout) findViewById(R.id.switch_voice_relative);
        switch_voice_relative.setVisibility(View.GONE);

        cancel_vedio_layout = (RelativeLayout) findViewById(R.id.cancel_vedio_layout);
        cancel_vedio_layout.setVisibility(View.GONE);


        //个人信息
        user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);
        vedio_name_text = (TextView) findViewById(R.id.vedio_name_text);


        cancel_vedio = (Button) findViewById(R.id.cancel_vedio);
        cancel_vedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                CloseActivity();
                stopRtmp();
                isRefuse = true;
                cancel_vedio.setEnabled(false);
                IMApplication.getApplication().setVedioStats(false);

            }
        });



        exchange_vedio = (Button) findViewById(R.id.exchange_vedio);
        exchange_vedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


            }
        });


       cancel_layout= (RelativeLayout) findViewById(R.id.cancel_layout);
        //摄像头切换
        switch_camera = (Button) findViewById(R.id.switch_camera);
        switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mHoster.SwitchCamera();

            }
        });

        mSurfaceView = (SurfaceViewRenderer) findViewById(R.id.suface_view);
        mSurfaceView.init(AnyRTMP.Inst().Egl().getEglBaseContext(), null);
        mRenderer = new VideoRenderer(mSurfaceView);
        mHoster = new RTMPHosterKit(this, this);
        mHoster.SetVideoCapturer(mRenderer.GetRenderPointer(), true);
        mHoster.SetAudioEnable(true);
        mHoster.SetVideoEnable(true);


        // 拉流
        mPullSurfaceView = (SurfaceViewRenderer) findViewById(R.id.pull_suface_view);
        mPullSurfaceView.init(AnyRTMP.Inst().Egl().getEglBaseContext(), null);
        mPullRenderer = new VideoRenderer(mPullSurfaceView);
        mGuest = new RTMPGuestKit(this, this);

        currentUserId = this.getIntent().getIntExtra(IntentConstant.KEY_PEERID,  0);
        vedioType = this.getIntent().getIntExtra(IntentConstant.KEY_DEV_VEDIO,  0);
        if(vedioType == DBConstant.DEV_VEDIO_TYPE){
            pushUrl = this.getIntent().getStringExtra(IntentConstant.KEY_DEV_URL);
        }


        imServiceConnector.connect(this);


        /**
         *  注册插入耳机广播
         */
        audioManager = (AudioManager) HosterActivity.this.getApplicationContext().getSystemService(
                Context.AUDIO_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(receiver, filter);


        /**
         *  屏幕常亮
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Subscribe(priority = SysConstant.MESSAGE_EVENTBUS_PRIORITY_VEDIO)
    public void onMessageEvent(PriorityEvent event) {
        switch (event.event) {
            case MSG_VEDIO_ONLINE_DEV: {
                DevVedioInfo info = (DevVedioInfo) event.object;
                if(info!=null)
                {
                    isAnswer = true;
                    pullUrl = info.getPullUrl();
                    pushUrl = info.getPushUrl();

                    statPullRtmp(pullUrl);
                    Message messageHanler = new Message();
                    messageHanler.what = 101;
                    HosterActivity.this.myHandler.sendMessage(messageHanler);
                    IMApplication.getPlaySound().stop();
                    //计时
                    timer.schedule(task, 1000, 1000);       // timeTask

                }
            }
            break;
            case MSG_RECEIVED_MESSAGE: {
                MessageEntity entity = (MessageEntity) event.object;
                /** 正式当前的会话 */
                if(entity.getMsgType() == DBConstant.MSG_TYPE_VIDEO_ANSWER){

                    isAnswer = true;
                    OnLineVedioMessage message = OnLineVedioMessage.parseFromNet(entity);
                    statPullRtmp(pullUrl);


                    Message messageHanler = new Message();
                    messageHanler.what = 101;
                    HosterActivity.this.myHandler.sendMessage(messageHanler);
                    IMApplication.getPlaySound().stop();
                    //计时
                    timer.schedule(task, 1000, 1000);       // timeTask

                }else if(entity.getMsgType() == DBConstant.MSG_TYPE_VIDEO_CLOSE){
                    isRefuse = true;
                    OnLineVedioMessage onLineMessage = OnLineVedioMessage.parseFromNet(entity);
                    if(onLineMessage.getVedioStatus() == DBConstant.VEDIO_ONLINE_ING){
                       // Utils.showToast(HosterActivity.this,"对方正忙...");
                    }
                    IMApplication.getApplication().setVedioStats(false);
                    CloseActivity2();
                }
            }
            break;
            case MSG_VEDIO_MESSAGE_TEST: {
                MessageEntity entity = (MessageEntity) event.object;
                final OnLineVedioMessage message = OnLineVedioMessage.parseFromNet(entity);

                pushUrl = message.getPullUrl();
                pullUrl = message.getPushUrl();
                statRtmp(message.getPullUrl());

            }
            break;
        }
    }



    TimerTask answerTask = new TimerTask() {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {      // UI thread
                @Override
                public void run() {
                    call_answer++;
                    if(isAnswer == false&&isRefuse == false && isTimeOut == false){
                        if(call_answer>=DBConstant.ONLINE_OUT_TIME){
                            call_answer = 0;
                            isTimeOut = true; //超时
                            closeTimeOutActivity();
                            IMApplication.getApplication().setVedioStats(false);
                            Answer.cancel();
                            stopRtmp(DBConstant.VEDIO_ONLINE_NO_TIMEOUT);
                        }
                    }
                }
            });
        }
    };

    TimerTask task = new TimerTask() {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {      // UI thread
                @Override
                public void run() {
                    call_time++;

                    int h=  call_time/3600;
                    int m=(call_time-h*3600)/60;
                    int s = (call_time-h*3600) % 60;

                    String min;
                    if(m>9){
                        min =  "" + m;
                    }else{
                        min = "0" +m;
                    }

                    String second;
                    if(s>9){
                        second =  "" + s;
                    }else{
                        second = "0" +s;
                    }

                    String hour;
                    if(h>9){
                        hour =  "" + h;
                    }else{
                        hour = "0" +h;
                    }

                    if(h>0){
                        vedio_time.setText(hour + ":" + min +":" + second);
                    }else{
                        vedio_time.setText(min +":" + second);
                    }
                }
            });
        }
    };

    /**
     * 点击同意之后更新界面　
     */
    public void  updateView(){
        m_suface_view_bg.setVisibility(View.GONE);

        RelativeLayout  user_info_image = (RelativeLayout) findViewById(R.id.user_info_image);
        user_info_image.setVisibility(View.GONE);

        switch_camera_layout.setVisibility(View.VISIBLE);
        switch_voice_relative.setVisibility(View.VISIBLE);
        cancel_vedio_layout.setVisibility(View.VISIBLE);


        RelativeLayout  cancel_layout = (RelativeLayout) findViewById(R.id.cancel_layout);
        cancel_layout.setVisibility(View.GONE);

        RelativeLayout  switch_voice_layout = (RelativeLayout) findViewById(R.id.switch_voice_layout);
        switch_voice_layout.setVisibility(View.GONE);

    }


    /**
     * 占线情况
     */
    public void  stopBusyRtmp(){

        if(vedioType == DBConstant.DEV_VEDIO_TYPE){
            timer.cancel();
            int type = 0;
            JSONObject extraContent = new JSONObject();
            try {
                extraContent.put("type", type);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String audioContent = extraContent.toString();

            imService.getUserActionManager().UserP2PCommand(IMLoginManager.instance().getLoginId(), currentUserId,
                    IMBaseDefine.SessionType.SESSION_DEVICE_SINGLE,
                    IMBaseDefine.CommandType.COMMAND_TYPE_DEVICE_BEGIN_VIDEO,audioContent, true);

        }else{
            int fromId = imService.getLoginManager().getLoginId();
            int status = DBConstant.VEDIO_ONLINE_ING;
            timer.cancel();
            String time = Utils.getTimeNum(0);
            OnLineVedioMessage vedioReqMessage = OnLineVedioMessage.buildForSend(time,status, fromId, currentUserId, DBConstant.MSG_TYPE_VIDEO_CLOSE);
            imService.getMessageManager().sendVedioReq(vedioReqMessage);
        }

    }

    public void  initView(){
        user_portrait.setImageUrl(userInfo.getAvatar());
        vedio_name_text.setText(userInfo.getMainName());
    }


    //发送close命令
    public void  stopRtmp(){

        if(vedioType == DBConstant.DEV_VEDIO_TYPE){

            timer.cancel();
            int type = 0;
            JSONObject extraContent = new JSONObject();
            try {
                extraContent.put("type", type);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String audioContent = extraContent.toString();

            imService.getUserActionManager().UserP2PCommand(IMLoginManager.instance().getLoginId(), currentUserId,
                    IMBaseDefine.SessionType.SESSION_DEVICE_SINGLE,
                    IMBaseDefine.CommandType.COMMAND_TYPE_DEVICE_BEGIN_VIDEO,audioContent, true);



        }else{
            int fromId = imService.getLoginManager().getLoginId();
            int status = DBConstant.VEDIO_ONLINE_NO_CALL;
            if(call_time>0){
                status = DBConstant.VEDIO_ONLINE_TALK;
            }
            timer.cancel();
            String time = Utils.getTimeNum(call_time);
            OnLineVedioMessage vedioReqMessage = OnLineVedioMessage.buildForSend(time, status,fromId, currentUserId, DBConstant.MSG_TYPE_VIDEO_CLOSE);
            imService.getMessageManager().sendVedioReq(vedioReqMessage);
        }

    }


    //根据状态发送close命令
    public void  stopRtmp(int status){

        if(vedioType == DBConstant.DEV_VEDIO_TYPE)
        {
            int type = 0;
            JSONObject extraContent = new JSONObject();
            try {
                extraContent.put("type", type);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String audioContent = extraContent.toString();

            imService.getUserActionManager().UserP2PCommand(IMLoginManager.instance().getLoginId(), currentUserId,
                    IMBaseDefine.SessionType.SESSION_DEVICE_SINGLE,
                    IMBaseDefine.CommandType.COMMAND_TYPE_DEVICE_BEGIN_VIDEO,audioContent, true);
        }else{
            int fromId = imService.getLoginManager().getLoginId();
            String time = Utils.getTimeNum(call_time);
            OnLineVedioMessage vedioReqMessage = OnLineVedioMessage.buildForSend(time, status,fromId, currentUserId, DBConstant.MSG_TYPE_VIDEO_CLOSE);
            imService.getMessageManager().sendVedioReq(vedioReqMessage);
        }

    }

    //开始推流
    public  void statRtmp(String pushUrl){
        mHoster.StartRtmpStream(pushUrl);
    }

    //开始拉流
    public void statPullRtmp(String pull_url){
        mGuest.StartRtmpPlay(pull_url, mPullRenderer.GetRenderPointer() );
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(this);
        unregisterReceiver(receiver);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (mHoster != null) {
            mHoster.StopRtmpStream();
            mHoster.Clear();
            mHoster = null;
        }

        if (mGuest != null) {
            mGuest.StopRtmpPlay();
            mGuest.Clear();
            mGuest = null;
        }


        IMApplication.getPlaySound().stop();
        if(!isBusy){
            IMApplication.getApplication().setVedioStats(false);
        }
        timer.cancel();
        Answer.cancel();

        mSurfaceView.release();
        mPullSurfaceView.release();
    }

    /**
     * the button click event listener
     *
     * @param btn
     */
    public void OnBtnClicked(View btn) {
        if (btn.getId() == R.id.btn_close) {
            if (mHoster != null) {
                mHoster.StopRtmpStream();
                mHoster.Clear();
                mHoster = null;
            }
            CloseActivity();
        } else if (btn.getId() == R.id.switch_camera) {
            if (null != mHoster) {
                mHoster.SwitchCamera();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            CloseActivity();
            stopRtmp();
            IMApplication.getApplication().setVedioStats(false);
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }
    /**
     * Implements for RTMPHosterHelper
     */
    @Override
    public void OnRtmpStreamOK() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
             //   mTxtStatus.setText(R.string.str_rtmp_success);
            }
        });
    }

    @Override
    public void OnRtmpStreamReconnecting(final int times) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               // mTxtStatus.setText(String.format(getString(R.string.str_rtmp_reconnecting), times));
            }
        });
    }
    	
    @Override
    public void OnRtmpStreamStatus(final int delayMs, final int netBand) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               // mTxtStatus.setText(String.format(getString(R.string.str_rtmp_status), delayMs, netBand));
            }
        });
    }

    @Override
    public void OnRtmpStreamFailed(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finshActivity();
            }
        });
    }	

    @Override
    public void OnRtmpStreamClosed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finshActivity();
            }
        });
    }

    @Override
    public void OnRtmplayerOK() {

    }

    @Override
    public void OnRtmplayerStatus(int cacheTime, int curBitrate) {

    }

    @Override
    public void OnRtmplayerCache(int time) {

    }
    public void OnRtmplayerFaid(int errcode) {

        Utils.showToast(HosterActivity.this,"网络连接异常");
        HosterActivity.this.finish();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }
    @Override
    public void OnRtmplayerClosed(int errcode) {
        finshActivity();
    }

    public void finshActivity(){
        finish();
    }



    //创建一个Handler
    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                {
                    if(call_time>0){
                        Utils.showToast(HosterActivity.this,"通话结束");
                    }else{
                        Utils.showToast(HosterActivity.this,"已取消");
                    }

                } break;
                case 1002:
                {
                    Utils.showToast(HosterActivity.this,"正在忙中...");
                } break;

                case 1008:
                {
                    Utils.showToast(HosterActivity.this,"对方未接通");
                } break;
                case 1003:
                {
                    if(call_time>0){
                        Utils.showToast(HosterActivity.this,"通话结束");
                    }else{
                        Utils.showToast(HosterActivity.this,"已取消");
                    }

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            //你需要跳转的地方的代码
                            HosterActivity.this.finish();
                        }
                    }, 500); //延迟1秒跳转

                } break;
                default:
                    break;
            }
        }
    };

    public void CloseActivity(){
        Message message = new Message();
        message.what = 1001;
        handler.sendMessage(message);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                //你需要跳转的地方的代码
                finish();
            }
        }, 600); //延迟1秒跳转
    }



    public void CloseActivity2(){
        Message message = new Message();
        message.what = 1003;
        handler.sendMessage(message);
    }



    public void closeBusyActivity(){
        Message message = new Message();
        message.what = 1002;
        handler.sendMessage(message);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                //你需要跳转的地方的代码
                finish();
            }
        }, 600); //延迟1秒跳转
    }

    public void closeTimeOutActivity(){

        Message message = new Message();
        message.what = 1008;
        handler.sendMessage(message);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                //你需要跳转的地方的代码
                finish();
            }
        }, 600); //延迟1秒跳转
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = SysConstant.MESSAGE_EVENTBUS_PRIORITY_VEDIO)
    public void onMessageEvent(LoginEvent event){
        switch (event){
            case LOGIN_OUT:
            {
                CloseActivity();
                IMApplication.getApplication().setVedioStats(false);
            }
            break;
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getIntExtra("state", 0) == 2){

            }else if(intent.getIntExtra("state", 0) == 1){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    // 听筒模式下设置为false
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setMicrophoneMute(false);
                    // 设置成听筒模式
                    audioManager.setMode( AudioManager.MODE_IN_COMMUNICATION);
                    // 设置为通话状态
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                            audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FX_KEY_CLICK);
                }else{
                    //关闭麦克风
                    audioManager.setMicrophoneMute(false);
                    // 打开扬声器
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                }


            }else if(intent.getIntExtra("state", 0) == 0){

                audioManager.setSpeakerphoneOn(true);//打开扬声器
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.MODE_IN_CALL), AudioManager.FX_KEY_CLICK);

            }
        }
    };
}

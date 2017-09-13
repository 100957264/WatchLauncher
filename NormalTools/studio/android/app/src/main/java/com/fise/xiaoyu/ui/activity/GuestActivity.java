
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
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.entity.OnLineVedioMessage;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.PriorityEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTGuideBaseActivity;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.Utils;

import org.anyrtc.core.AnyRTMP;
import org.anyrtc.core.RTMPGuestHelper;
import org.anyrtc.core.RTMPGuestKit;
import org.anyrtc.core.RTMPHosterHelper;
import org.anyrtc.core.RTMPHosterKit;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Eric on 2016/9/16.
 */
public class GuestActivity extends TTGuideBaseActivity implements RTMPGuestHelper ,RTMPHosterHelper {
    private RTMPGuestKit mGuest = null;
    private RTMPHosterKit mHoster = null;

    private SurfaceViewRenderer mSurfaceView = null;
    private VideoRenderer mRenderer = null;
    private  String pushUrl;
    private  String pullUrl;
    private static IMService imService;

    private Button  Agree;
    private Button  Refuse;
    private int currentUserId;

    private SurfaceViewRenderer PushSurfaceView = null;
    private VideoRenderer PushRenderer = null;
    private Button switch_camera;
    private Button exchange_vedio;


    //下面取消　切合摄像头　
    private Button cancel_vedio;
    private RelativeLayout cancel_vedio_layout;
    private RelativeLayout switch_camera_layout;
    private RelativeLayout switch_voice_relative;


    private IMBaseImageView user_portrait;
    private TextView  vedio_name_text;
    private UserEntity userInfo;
    private ImageView m_suface_view_bg;


    private int call_time = 0;
    private Timer timer = new Timer();
    private TextView vedio_time;

    private boolean isBusy = false;


    //是否已经连接成功
    private boolean isAnswer = false;
    private int call_answer = 0;
    private Timer Answer = new Timer();

    //是否拒绝
    private boolean isRefuse = false;
    private boolean isTimeOut = false;



    private AudioManager audioManager;


    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("config#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                // 后台服务启动链接失败
                return;
            }

            userInfo = imService.getContactManager().findFriendsContact(currentUserId);
            if(userInfo ==null){
                return;
            }

            //计时
            Answer.schedule(answerTask, 1000, 1000);       // timeTask

            initView();
            //
            if( IMApplication.getApplication().getVedioStats()){
                closeBusyActivity();
                stopBusyRtmp();
                isBusy = true;
            }else{
                startRtmp();
                IMApplication.getApplication().setVedioStats(true);
                isBusy = false;

                //播放音效
                IMApplication.getPlaySound().Player(GuestActivity.this,R.raw.vido_call);

            }
        }

        @Override
        public void onServiceDisconnected() {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_guest);

        currentUserId = this.getIntent().getIntExtra(IntentConstant.KEY_PEERID,  0);
        m_suface_view_bg = (ImageView) findViewById(R.id.m_suface_view_bg);
      //  m_suface_view_bg.setVisibility(View.GONE);

        //视频的时间
        vedio_time = (TextView) findViewById(R.id.vedio_time);
        vedio_time.setVisibility(View.GONE);


        user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);
        vedio_name_text = (TextView) findViewById(R.id.vedio_name_text);

        //视频转换
        exchange_vedio = (Button) findViewById(R.id.exchange_vedio);
        exchange_vedio.setVisibility(View.GONE);

        switch_camera_layout = (RelativeLayout) findViewById(R.id.switch_camera_relative);
        switch_camera_layout.setVisibility(View.GONE);

        switch_voice_relative = (RelativeLayout) findViewById(R.id.switch_voice_relative);
        switch_voice_relative.setVisibility(View.GONE);


        cancel_vedio = (Button) findViewById(R.id.cancel_vedio);
        cancel_vedio_layout = (RelativeLayout) findViewById(R.id.cancel_vedio_layout);
        cancel_vedio_layout.setVisibility(View.GONE);


        cancel_vedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                IMApplication.getPlaySound().stop();
                CloseActivity();
                stopRtmp();
                IMApplication.getApplication().setVedioStats(false);
                isRefuse = true;
                cancel_vedio.setEnabled(false);
            }
        });



        pushUrl  = this.getIntent().getStringExtra(IntentConstant.PUSHURL);
        pullUrl = this.getIntent().getStringExtra(IntentConstant.PULLURL);

        Agree = (Button) findViewById(R.id.txt_rtmp_agree);
        Refuse = (Button) findViewById(R.id.txt_rtmp_refuse);

        Agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                IMApplication.getPlaySound().stop();

                startPullRtmp();

                Answer.cancel();
                isAnswer = true;
                m_suface_view_bg.setVisibility(View.GONE);
                Agree.setVisibility(View.GONE);
                Refuse.setVisibility(View.GONE);
                switch_camera.setVisibility(View.VISIBLE);

                cancel_vedio_layout.setVisibility(View.VISIBLE);
                switch_camera_layout.setVisibility(View.VISIBLE);
                switch_voice_relative.setVisibility(View.VISIBLE);

                exchange_vedio.setVisibility(View.VISIBLE);
                updateView();

                //视频时间
                vedio_time.setVisibility(View.VISIBLE);

                int fromId = imService.getLoginManager().getLoginId();
                OnLineVedioMessage vedioReqMessage = OnLineVedioMessage.buildForSend("", fromId, currentUserId, DBConstant.MSG_TYPE_VIDEO_ANSWER);
                imService.getMessageManager().sendVedioReq(vedioReqMessage);

                //计时
                if(timer == null){
                    timer = new Timer();
                }
                timer.schedule(task, 1000, 1000);       // timeTask


            }
        });



        Refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                CloseActivity();
                stopRtmp(DBConstant.VEDIO_ONLINE_REFUSE);
                isRefuse = true;
                IMApplication.getApplication().setVedioStats(false);
                Answer.cancel();
                Refuse.setEnabled(false);
            }
        });


        {//* Init UI  拉流
            mSurfaceView = (SurfaceViewRenderer) findViewById(R.id.suface_view);
            mSurfaceView.init(AnyRTMP.Inst().Egl().getEglBaseContext(), null);
            mRenderer = new VideoRenderer(mSurfaceView);
            mGuest = new RTMPGuestKit(this, this);


        }

        {//* Init UI //推流
            PushSurfaceView  = (SurfaceViewRenderer) findViewById(R.id.m_suface_view);
            PushSurfaceView.init(AnyRTMP.Inst().Egl().getEglBaseContext(), null);
            PushRenderer = new VideoRenderer(PushSurfaceView);

            mHoster = new RTMPHosterKit(this, this);
            mHoster.SetVideoCapturer(PushRenderer.GetRenderPointer(), true);
            mHoster.SetAudioEnable(true);
            mHoster.SetVideoEnable(true);
          //  PushSurfaceView.setVisibility(View.GONE);

        }


        switch_camera = (Button) findViewById(R.id.switch_camera);
        switch_camera.setVisibility(View.GONE);
        switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mHoster.SwitchCamera();

            }
        });

        imServiceConnector.connect(this);



        /*
          接受插入耳机广播
         */
        audioManager = (AudioManager) GuestActivity.this.getApplicationContext().getSystemService(
                Context.AUDIO_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(receiver, filter);


        /**
         * 保持屏幕常亮
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    TimerTask answerTask = new TimerTask() {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {      // UI thread
                @Override
                public void run() {
                    call_answer++;
                    if(isAnswer == false&&isRefuse == false&&isTimeOut == false){
                        if(call_answer>=DBConstant.ONLINE_OUT_TIME){
                            isTimeOut = true;
                            Answer.cancel();
                            closeTimeOutActivity();
                            IMApplication.getApplication().setVedioStats(false);
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

    public void startRtmp(){
        statPushRtmp(pushUrl);
    }

    public void startPullRtmp(){
        statPullRtmp(pullUrl);
    }

    public void  initView(){
        user_portrait
                .setImageResource(R.drawable.tt_default_user_portrait_corner);
        user_portrait.setImageUrl(userInfo.getAvatar());
        vedio_name_text.setText(userInfo.getMainName());

    }


    public void  updateView(){
        user_portrait.setVisibility(View.GONE);
        vedio_name_text.setVisibility(View.GONE);

        RelativeLayout  rtmp_refuse_layout = (RelativeLayout) findViewById(R.id.rtmp_refuse_layout);
        rtmp_refuse_layout.setVisibility(View.GONE);

        RelativeLayout  rtmp_agree = (RelativeLayout) findViewById(R.id.rtmp_agree);
        rtmp_agree.setVisibility(View.GONE);

        RelativeLayout  user_info_image = (RelativeLayout) findViewById(R.id.user_info_image);
        user_info_image.setVisibility(View.GONE);

        RelativeLayout switch_voice_layout = (RelativeLayout) findViewById(R.id.switch_voice_layout);
        switch_voice_layout.setVisibility(View.GONE);

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


    @Subscribe(priority = SysConstant.MESSAGE_EVENTBUS_PRIORITY_VEDIO)
    public void onMessageEvent(PriorityEvent event) {
        switch (event.event) {
            case MSG_RECEIVED_MESSAGE: {
                MessageEntity entity = (MessageEntity) event.object;
                /** 正式当前的会话 */
                 if(entity.getMsgType() == DBConstant.MSG_TYPE_VIDEO_CLOSE){
                     isRefuse = true;
                     CloseActivity2();
                     IMApplication.getApplication().setVedioStats(false);
                }
            }
            break;
        }
    }

    public  void statPushRtmp(String pushUrl){
        mHoster.StartRtmpStream(pushUrl);
    }

    public void  stopRtmp(){
        IMApplication.getPlaySound().stop();
        int fromId = imService.getLoginManager().getLoginId();
        int status = DBConstant.VEDIO_ONLINE_NO_CALL;
        if(call_time>0){
            status = DBConstant.VEDIO_ONLINE_TALK;
        }
        timer.cancel();
        timer = null;
        String time = Utils.getTimeNum(call_time);
        OnLineVedioMessage vedioReqMessage = OnLineVedioMessage.buildForSend(time,status, fromId, currentUserId, DBConstant.MSG_TYPE_VIDEO_CLOSE);
        imService.getMessageManager().sendVedioReq(vedioReqMessage);

    }


    public void  stopRtmp(int status){
        int fromId = imService.getLoginManager().getLoginId();
        timer.cancel();
        timer = null;
        IMApplication.getPlaySound().stop();
        String time = Utils.getTimeNum(call_time);
        OnLineVedioMessage vedioReqMessage = OnLineVedioMessage.buildForSend(time,status, fromId, currentUserId, DBConstant.MSG_TYPE_VIDEO_CLOSE);
        imService.getMessageManager().sendVedioReq(vedioReqMessage);

    }

    /**
     * 占线情况
     */
    public void  stopBusyRtmp(){
        int fromId = imService.getLoginManager().getLoginId();
        int status = DBConstant.VEDIO_ONLINE_ING;
        timer.cancel();
        timer = null;
        String time = Utils.getTimeNum(0);
        OnLineVedioMessage vedioReqMessage = OnLineVedioMessage.buildForSend(time,status, fromId, currentUserId, DBConstant.MSG_TYPE_VIDEO_CLOSE);
        imService.getMessageManager().sendVedioReq(vedioReqMessage);
    }

    public void statPullRtmp(String pull_url){
        mGuest.StartRtmpPlay(pull_url, mRenderer.GetRenderPointer() );
    }

    public void OnRtmplayerFaid(int errcode) {

        Utils.showToast(GuestActivity.this,"网络连接异常");
        GuestActivity.this.finish();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                imServiceConnector.disconnect(this);
       // stopRtmp();
        if (mGuest != null) {
            mGuest.StopRtmpPlay();
            mGuest.Clear();
            mGuest = null;
        }


        if (mHoster != null) {
            mHoster.StopRtmpStream();
            mHoster.Clear();
            mHoster = null;
        }

        IMApplication.getPlaySound().stop();
        if(!isBusy){
            IMApplication.getApplication().setVedioStats(false);
        }
        if(timer != null){
            timer.cancel();
            timer = null;
        }

        mSurfaceView.release();
        PushSurfaceView.release();

    }


    /**
     * Implements for RTMPGuestHelper
     */
    @Override
    public void OnRtmplayerOK() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void OnRtmplayerStatus(final int cacheTime, final int curBitrate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void OnRtmplayerCache(int time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void OnRtmplayerClosed(int errcode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finshActivity();
            }
        });
    }


    /**
     * Implements for RTMPHosterHelper
     */
    @Override
    public void OnRtmpStreamOK() {

    }

    @Override
    public void OnRtmpStreamReconnecting(final int times) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void OnRtmpStreamStatus(final int delayMs, final int netBand) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void OnRtmpStreamFailed(final int code) {
        finshActivity();
    }

    @Override
    public void OnRtmpStreamClosed() {
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
                        Utils.showToast(GuestActivity.this,"通话结束");
                    }else{
                        Utils.showToast(GuestActivity.this,"已取消");
                    }

                } break;
                case 1009:
                {
                    Utils.showToast(GuestActivity.this,"正在忙中...");
                } break;
                case 1008:
                {
                    Utils.showToast(GuestActivity.this,"对方未接通");
                } break;

                case 1003:
                {
                    if(call_time>0){
                        Utils.showToast(GuestActivity.this,"通话结束");
                    }else{
                        Utils.showToast(GuestActivity.this,"已取消");
                    }

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            //你需要跳转的地方的代码
                           GuestActivity.this.finish();
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
        }, 500); //延迟1秒跳转
    }


    public void CloseActivity2(){
        Message message = new Message();
        message.what = 1003;
        handler.sendMessage(message);
    }

    public void closeBusyActivity(){

        Message message = new Message();
        message.what = 1009;
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
                    audioManager.setMicrophoneMute(true);
                    // 设置成听筒模式
                    audioManager.setMode( AudioManager.MODE_IN_COMMUNICATION);
                    // 设置为通话状态
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                            audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FX_KEY_CLICK);
                }else{
                    //关闭麦克风
                    audioManager.setMicrophoneMute(false);
                    // 打开扬声器
                    audioManager.setSpeakerphoneOn(true);
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

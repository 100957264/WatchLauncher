
package com.fise.xiaoyu.ui.helper;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Build;

import com.fise.xiaoyu.imservice.event.AudioEvent;
import com.fise.xiaoyu.imservice.support.audio.SpeexDecoder;
import com.fise.xiaoyu.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class AudioPlayerHandler{
    private String currentPlayPath = null;
    private SpeexDecoder speexdec = null;
    private Thread th = null;
    private boolean speakerMode;

    private static AudioPlayerHandler instance = null;
    private Logger logger = Logger.getLogger(AudioPlayerHandler.class);

    public static  AudioPlayerHandler getInstance() {
        if (null == instance) {
            synchronized(AudioPlayerHandler.class){
                instance = new AudioPlayerHandler();
                 EventBus.getDefault().register(instance);
            }
        }
        return instance;
    }


    public  boolean getMessageAudioMode(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE);
        boolean speaker = sp.getBoolean("speaker", false);
        speakerMode = speaker;
        return speaker;
    }


    //语音播放的模式
    public  void setAudioMode(Context ctx,boolean speaker) {

        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);


        SharedPreferences sp = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE);
        //存入数据
        Editor editor = sp.edit();
        editor.putBoolean("speaker", speaker);
        editor.commit();
        speakerMode = speaker;

        if(speaker)
        {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
              // 听筒模式下设置为false
              audioManager.setSpeakerphoneOn(false);
              audioManager.setMicrophoneMute(true);
              // 设置成听筒模式
              audioManager.setMode( AudioManager.MODE_IN_COMMUNICATION);
              // 设置为通话状态
              audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                      audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FX_KEY_CLICK);
          }else{
              //关闭麦克风
              audioManager.setMicrophoneMute(false);
              // 打开扬声器
              audioManager.setSpeakerphoneOn(true);
              audioManager.setMode(AudioManager.MODE_IN_CALL);
          }

        }else{

            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(true);//打开扬声器
            //   OpenSpeaker(ctx);
        }
    }

    //语音播放的模式
    public  void setScreenAudioMode(Context ctx) {

        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

        if(speakerMode)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                // 听筒模式下设置为false
                audioManager.setSpeakerphoneOn(false);
                // 设置成听筒模式
                audioManager.setMode( AudioManager.MODE_IN_COMMUNICATION);
                // 设置为通话状态
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FX_KEY_CLICK);
            }else{
                //关闭麦克风
                audioManager.setMicrophoneMute(false);
                // 打开扬声器
                audioManager.setSpeakerphoneOn(true);
                audioManager.setMode(AudioManager.MODE_IN_CALL);
            }

        }else{

            audioManager.setSpeakerphoneOn(true);//打开扬声器
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(AudioManager.MODE_IN_CALL), AudioManager.FX_KEY_CLICK);
        }
    }

    /**messagePop调用*/
    public int getAudioMode(Context ctx) {
        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getMode();
    }

    public void clear(){
        if (isPlaying()){
            stopPlayer();
        }
        EventBus.getDefault().unregister(instance);
        instance = null;
    }


    private AudioPlayerHandler() {
    }

    /**
     * yingmu modify
     * speexdec 由于线程模型
     * */
    public interface AudioListener{
        public void onStop();
    }

    private AudioListener audioListener;

    public void setAudioListener(AudioListener audioListener) {
        this.audioListener = audioListener;
    }

    private void stopAnimation(){
        if(audioListener!=null){
            audioListener.onStop();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AudioEvent audioEvent){ //onMessageEvent
        switch (audioEvent){
            case AUDIO_STOP_PLAY:{
                currentPlayPath = null;
                stopPlayer();
            }break;
        }
    }

    public void stopPlayer() {
        try {
            if (null != th) {
                th.interrupt();
                th = null;
                Thread.currentThread().interrupt();
            } else {
            }
        } catch (Exception e) {
            logger.e(e.getMessage());
        }finally {
            stopAnimation();
        }
    }

    public boolean isPlaying() {
        return null != th;
    }

    public void startPlay(String filePath) {
        this.currentPlayPath = filePath;
        try {
            speexdec = new SpeexDecoder(new File(this.currentPlayPath));
            RecordPlayThread rpt = new RecordPlayThread();
            if (null == th)
                th = new Thread(rpt);
            th.start();
        } catch (Exception e) {
            // 关闭动画很多地方需要写，是不是需要重新考虑一下@yingmu
            logger.e(e.getMessage());
            stopAnimation();
        }
    }



    class RecordPlayThread extends Thread {
        public void run() {
            try {
                if (null != speexdec)
                    speexdec.decode();

            } catch (Exception e) {
                logger.e(e.getMessage());
                stopAnimation();
            }
        }
    };

    public String getCurrentPlayPath() {
        return currentPlayPath;
    }
}

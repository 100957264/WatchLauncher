package android.fise.com.fiseassitant;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;


public class TesterMic extends Activity {
    FiseTestHandler mHandler;
    final int MSG_START_RECORD = 0;
    final int MSG_STOP_RECORD = 1;
    final int MSG_START_PLAY = 2;
    final int MSG_STOP_PLAY = 3 ;
    AudioManager mAudioManager;
    MediaRecorder mRecorder;
    VUMeter mVUMeter;
    File  mSoundFile;
    private MediaPlayer mPlayer;
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.micrecorder);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        mVUMeter = (VUMeter) findViewById(R.id.uvMeter);
        mHandler = new FiseTestHandler();
        mHandler.sendEmptyMessage(MSG_START_RECORD);
    }
    class FiseTestHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MSG_START_RECORD:
                    startRecording();
                    mHandler.sendEmptyMessageDelayed(MSG_STOP_RECORD,10000);
                    break;
                case MSG_STOP_RECORD:
                    stopRecording();
                    startPlayback();
                    mHandler.sendEmptyMessageDelayed(MSG_STOP_PLAY,10000);
                    break;
                case MSG_STOP_PLAY:
                    stopPlayback();
                    mHandler.sendEmptyMessage(MSG_START_RECORD);
                    break;
            }
        }

    }
    void startRecording() {
        // create file
        try {
            if (mSoundFile.exists()) {
                mSoundFile.delete();
            }
            mSoundFile.createNewFile();
        } catch (IOException e) {
            Log.e("Recorder", "Create file fail", e);
            return;
        }
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        mRecorder.setOutputFile(mSoundFile.getAbsolutePath());

        // Handle IOException
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("Recorder", "Recoder prepare fail", e);
            mRecorder.reset();
            mRecorder.release();
            if (mSoundFile != null) mSoundFile.delete();
            mRecorder = null;
            return;
        }
        // Handle RuntimeException if the recording couldn't start
        try {
            mRecorder.start();
        } catch (IllegalStateException e) {
            Log.e("Recorder", "Recoder start fail", e);
            mRecorder.reset();
            mRecorder.release();
            if (mSoundFile != null) mSoundFile.delete();
            mRecorder = null;
            return;
        }

        mVUMeter.setRecorder(mRecorder);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
    private void startPlayback() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mSoundFile.getAbsolutePath());
            mPlayer.setLooping(true);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            mPlayer = null;
        } catch (IOException e) {
            e.printStackTrace();
            mPlayer = null;
        }

    }
    private void stopRecording() {
        if (mRecorder == null) {
            return;
        }
        try {
            mRecorder.stop();
        } catch (RuntimeException exception) {
            Log.e("Recorder", "Stop Failed");
        }
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
        //*/droi.chenlei,20160803 add mic test animation
        mVUMeter.setRecorder(mRecorder);
    }
    private void stopPlayback() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;

    }
}

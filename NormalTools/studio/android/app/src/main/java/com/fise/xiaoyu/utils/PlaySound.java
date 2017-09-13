package com.fise.xiaoyu.utils;

/**
 * Created by zhujian on 15/1/14.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;

import com.fise.xiaoyu.R;

/**
 * 
 */
public class PlaySound  implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, Closeable{
	private final Context activity;
	private MediaPlayer mediaPlayer;
	private static final float BEEP_VOLUME = 0.10f;
	private static final long VIBRATE_DURATION = 200L;
	
	
	public PlaySound(Context activity) {
		this.activity = activity;
		//this.mediaPlayer = null;
		this.mediaPlayer = new MediaPlayer();
		//updatePrefs();
	}
	
//	private synchronized void updatePrefs() {
//		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
//		mediaPlayer = buildMediaPlayer(activity);
//	}
	
	public synchronized void playBeepSoundAndVibrate() {
		if (mediaPlayer != null) {
			mediaPlayer.start();
		}
		 
	}
	
	private MediaPlayer buildMediaPlayer(Context activity) {

		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		try {
			AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
			} finally {
				file.close();
			}
			mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
			mediaPlayer.prepare();
			return mediaPlayer;
		} catch (IOException ioe) {
			 
			mediaPlayer.release();
			return null;
		}
	}


	public void  Player(Context activity,int id) {

		if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
			mediaPlayer.stop();
		}
		mediaPlayer = MediaPlayer.create(activity, id);
		mediaPlayer.start();
		//监听音频播放完的代码，实现音频的自动循环播放
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer arg0) {
				mediaPlayer.start();
				mediaPlayer.setLooping(true);
			}
		});

//		mediaPlayer = new MediaPlayer();
//		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//		mediaPlayer.setOnCompletionListener(this);
//		mediaPlayer.setOnErrorListener(this);
//		try {
//			AssetFileDescriptor file = activity.getResources().openRawResourceFd(id);
//			try {
//				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
//			} finally {
//				file.close();
//			}
//			mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
//			mediaPlayer.prepare();
//			mediaPlayer.start();
//			mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//
//				@Override
//				public void onCompletion(MediaPlayer arg0) {
//					mediaPlayer.start();
//					mediaPlayer.setLooping(true);
//				}
//			});
//			//	return mediaPlayer;
//		} catch (IOException ioe) {
//
//			mediaPlayer.release();
//		}

	}

	public void stop() {
		// TODO Auto-generated method stub
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}


	public  void mediaPlayer() {

		MediaPlayer mediaPlayerSound = new MediaPlayer();
		mediaPlayerSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayerSound.setOnCompletionListener(this);
		mediaPlayerSound.setOnErrorListener(this);
		try {
			AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.electronic_fence_alarm);
			try {
				mediaPlayerSound.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
			} finally {
				file.close();
			}
			mediaPlayerSound.setVolume(BEEP_VOLUME, BEEP_VOLUME);
			mediaPlayerSound.prepare();
			mediaPlayerSound.start();

		} catch (IOException ioe) {

			mediaPlayerSound.release();
		}
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int arg2) {
		// TODO Auto-generated method stub
		if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
			// we are finished, so put up an appropriate error toast if required
			// and finish
			//activity.finish();
		} else {
			// possibly media player error, so release and recreate
			mp.release();
			mediaPlayer = null;
			//updatePrefs();
		}
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		mp.seekTo(0);
		mp.release();
	}

}
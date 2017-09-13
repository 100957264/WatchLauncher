package com.fise.xw.utils;

/**
 * Created by zhujian on 15/1/14.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;

import com.fise.xw.R;

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
		this.mediaPlayer = null;
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
		MediaPlayer mediaPlayer = new MediaPlayer();
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

	
	
	public  void mediaPlayer() {
		MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		try {
			AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.electronic_fence_alarm);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
			} finally {
				file.close();
			}
			mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
			mediaPlayer.prepare(); 
			mediaPlayer.start();
			
		} catch (IOException ioe) {
			 
			mediaPlayer.release(); 
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
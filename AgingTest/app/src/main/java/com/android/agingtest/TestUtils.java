package com.android.agingtest;

import java.util.ArrayList;

import com.android.agingtest.test.BackTakingPictureActivity;
import com.android.agingtest.test.FrontTakingPictureActivity;
import com.android.agingtest.test.PlayVideoActivity;
import com.android.agingtest.test.RebootActivity;
import com.android.agingtest.test.ReceiverActivity;
import com.android.agingtest.test.SleepActivity;
import com.android.agingtest.test.VibrateActivity;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class TestUtils {

	public static final String TEST_STATE = "test_state";
	public static final String REBOOT_STATE = "reboot_state";
	public static final String SLEEP_STATE = "sleep_state";
	public static final String VIBRATE_STATE = "vibrate_state";
	public static final String RECEIVER_STATE = "receiver_state";
	public static final String FRONT_TAKING_PICTURE_STATE = "front_taking_picture_state";
	public static final String BACK_TAKING_PICTURE_STATE = "back_taking_picture_state";
	public static final String PLAY_VIDEO_STATE = "play_video_state";
	public static final String CIRCULATION_STATE = "circulation_state";

	public static final String REBOOT_TIME = "reboot_time";
	public static final String SLEEP_TIME = "sleep_time";
	public static final String VIBRATE_TIME = "vibrate_time";
	public static final String RECEIVER_TIME = "receiver_time";
	public static final String FRONT_TAKING_PICTURE_TIME = "front_taking_picture_time";
	public static final String BACK_TAKING_PICTURE_TIME = "back_taking_picture_time";
	public static final String PLAY_VIDEO_TIME = "play_video_time";

	public static final String REBOOT_RESULT = "reboot_result";
	public static final String SLEEP_RESULT = "sleep_result";
	public static final String VIBRATE_RESULT = "vibrate_result";
	public static final String RECEIVER_RESULT = "receiver_result";
	public static final String FRONT_TAKING_PICTURE_RESULT = "front_taking_picture_result";
	public static final String BACK_TAKING_PICTURE_RESULT = "back_taking_picture_result";
	public static final String PLAY_VIDEO_RESULT = "play_video_result";

	public static final String REBOOT_START_TIME = "reboot_start_time";
	public static final String SLEEP_START_TIME = "sleep_start_time";
	public static final String VIBRATE_START_TIME = "vibrate_start_time";
	public static final String RECEIVER_START_TIME = "receiver_start_time";
	public static final String FRONT_TAKING_PICTURE_START_TIME = "front_taking_picture_start_time";
	public static final String BACK_TAKING_PICTURE_START_TIME = "back_taking_picture_start_time";
	public static final String PLAY_VIDEO_START_TIME = "play_video_start_time";
	
	public static final String CIRCULATION_EXTRA= "circulation";

	public static final long MILLSECOND = 60 * 1000;

	private static final ArrayList<Class> mTestList = new ArrayList<Class>();

	public static ArrayList<Class> getTestList() {
		return mTestList;
	}

	public static void updateTestList(Context context) {
		Resources res = context.getResources();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		boolean reboot = sp.getBoolean(REBOOT_STATE, res.getBoolean(R.bool.default_reboot_value));
		boolean sleep = sp.getBoolean(SLEEP_STATE, res.getBoolean(R.bool.default_sleep_value));
		boolean vibrate = sp.getBoolean(VIBRATE_STATE, res.getBoolean(R.bool.default_vibrate_value));
		boolean receiver = sp.getBoolean(RECEIVER_STATE, res.getBoolean(R.bool.default_receiver_value));
		boolean frontCamera = sp.getBoolean(FRONT_TAKING_PICTURE_STATE,
				res.getBoolean(R.bool.default_front_taking_picture_value));
		boolean backCamera = sp.getBoolean(BACK_TAKING_PICTURE_STATE,
				res.getBoolean(R.bool.default_back_taking_picture_value));
		boolean playVideo = sp.getBoolean(PLAY_VIDEO_STATE, res.getBoolean(R.bool.default_play_video_value));
		mTestList.clear();
		if (reboot) {
			mTestList.add(RebootActivity.class);
		}
		
		if (sleep) {
			mTestList.add(SleepActivity.class);
		}
		
		if (vibrate) {
			mTestList.add(VibrateActivity.class);
		}
		
		if (receiver) {
			mTestList.add(ReceiverActivity.class);
		}
		
		if (frontCamera) {
			mTestList.add(FrontTakingPictureActivity.class);
		}
		
		if (backCamera) {
			mTestList.add(BackTakingPictureActivity.class);
		}
		
		if (playVideo) {
			mTestList.add(PlayVideoActivity.class);
		}
		Log.d("TestUtils", "updateTestList=>size: " + mTestList.size() + " first: " + (mTestList.size() > 0 ? mTestList.get(0) : "null"));
	}

	public static void clearHistoryValue(Context context) {
		Resources res = context.getResources();
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		
		editor.remove(TestUtils.REBOOT_RESULT);
		editor.remove(TestUtils.SLEEP_RESULT);
		editor.remove(TestUtils.VIBRATE_RESULT);
		editor.remove(TestUtils.RECEIVER_RESULT);
		editor.remove(TestUtils.FRONT_TAKING_PICTURE_RESULT);
		editor.remove(TestUtils.BACK_TAKING_PICTURE_RESULT);
		editor.remove(TestUtils.PLAY_VIDEO_RESULT);

		editor.remove(TestUtils.REBOOT_START_TIME);
		editor.remove(TestUtils.SLEEP_START_TIME);
		editor.remove(TestUtils.VIBRATE_START_TIME);
		editor.remove(TestUtils.RECEIVER_START_TIME);
		editor.remove(TestUtils.FRONT_TAKING_PICTURE_START_TIME);
		editor.remove(TestUtils.BACK_TAKING_PICTURE_START_TIME);
		editor.remove(TestUtils.PLAY_VIDEO_START_TIME);

		editor.commit();
	}

	public static int getCurrentTestIndex(Class c) {
		int index = -1;
		for (int i = 0; i < mTestList.size(); i++) {
			if (mTestList.get(i).equals(c)) {
				index = i;
				break;
			}
		}
		return index;
	}

	public static void reboot(Context context, String reason) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		pm.reboot(reason);
	}
	
	public static void goToSleep(Context context) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		//pm.goToSleep(SystemClock.uptimeMillis());
	}

	public static void vibrate(Context context) {
		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(new long[] { 0, 1000, 0, 1000, 0, 1000 }, 0);
	}

	public static void cancelVibrate(Context context) {
		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		if (v.hasVibrator()) {
			v.cancel();
		}
	}

}
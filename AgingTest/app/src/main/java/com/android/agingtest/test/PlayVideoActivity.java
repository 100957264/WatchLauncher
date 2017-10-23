package com.android.agingtest.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import com.android.agingtest.AgingTest;
import com.android.agingtest.Log;
import com.android.agingtest.Player;
import com.android.agingtest.Player.PlayerListener;
import com.android.agingtest.R;
import com.android.agingtest.ReportActivity;
import com.android.agingtest.TestUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PlayVideoActivity extends Activity implements OnClickListener, PlayerListener {

	public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
	private static final String VIDEO_PATH = Environment.getExternalStorageDirectory() + "/moveTest.mp4";

	private static final int MSG_UPDATE_TIME = 0;

	private SurfaceView mSurfaceView;
	private TextView mTestTimeTv;
	private Button mStopBt;
	private SharedPreferences mSharedPreferences;
	private SurfaceView surfaceView;
	private Button btnPause, btnPlayUrl, btnStop;
	private Player mPlayer;
	private PowerManager mPowerManager;
	private WakeLock mLock;

	private int mPlayVideoTime;
	private long mStartTime;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(FLAG_HOMEKEY_DISPATCHED);
		getWindow().addFlags(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		initActionBar();
		setContentView(R.layout.activity_play_video);

		initValues();
		initViews();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPlayer.playUrl(this);
		mHandler.sendEmptyMessage(MSG_UPDATE_TIME);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mHandler.removeMessages(MSG_UPDATE_TIME);
		long testTime = System.currentTimeMillis() - mStartTime;
		if (testTime < mPlayVideoTime * TestUtils.MILLSECOND && !mPowerManager.isScreenOn()) {
			mPlayer.stop();
			Editor e = mSharedPreferences.edit();
			e.putInt(TestUtils.PLAY_VIDEO_RESULT, 0);
			e.commit();
			Intent intent = new Intent(this, AgingTest.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mLock.isHeld()) {
			mLock.release();
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mHandler.removeMessages(MSG_UPDATE_TIME);
			mPlayer.stop();
			long testTime = System.currentTimeMillis() - mStartTime;
			Editor e = mSharedPreferences.edit();
			if (testTime >= mPlayVideoTime * TestUtils.MILLSECOND) {
				e.putInt(TestUtils.PLAY_VIDEO_RESULT, 1);
			} else {
				e.putInt(TestUtils.PLAY_VIDEO_RESULT, 0);
			}
			e.commit();
			Intent intent = new Intent(this, AgingTest.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_HOME:
			Toast.makeText(this, R.string.testing_tip, Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.stop_test:
			mHandler.removeMessages(MSG_UPDATE_TIME);
			mPlayer.stop();
			long testTime = System.currentTimeMillis() - mStartTime;
			Editor e = mSharedPreferences.edit();
			if (testTime >= mPlayVideoTime * TestUtils.MILLSECOND) {
				e.putInt(TestUtils.PLAY_VIDEO_RESULT, 1);
			} else {
				e.putInt(TestUtils.PLAY_VIDEO_RESULT, 0);
			}
			e.commit();
			ArrayList<Class> testList = TestUtils.getTestList();
			int index = TestUtils.getCurrentTestIndex(PlayVideoActivity.class);
			Intent intent = null;
			if ((index + 1) < testList.size()) {
				Log.d(this, "onClick=>testList size: " + testList.size());
				intent = new Intent(this, testList.get(index + 1));
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			} else {
				intent = new Intent(this, ReportActivity.class);
				intent.putExtra(TestUtils.CIRCULATION_EXTRA, true);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			finish();
			break;
		}
	}

	private void initActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void initValues() {
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "play_video_test");
		mLock.acquire();

		mStartTime = mSharedPreferences.getLong(TestUtils.PLAY_VIDEO_START_TIME, -1);
		mPlayVideoTime = mSharedPreferences.getInt(TestUtils.PLAY_VIDEO_TIME,
				getResources().getInteger(R.integer.default_play_video_time));
		if (mStartTime == -1) {
			mStartTime = System.currentTimeMillis();
			Editor e = mSharedPreferences.edit();
			e.putLong(TestUtils.PLAY_VIDEO_START_TIME, mStartTime);
			e.commit();
		}
	}

	private void initViews() {
		mTestTimeTv = (TextView) findViewById(R.id.test_time);
		mStopBt = (Button) findViewById(R.id.stop_test);
		mSurfaceView = (SurfaceView) findViewById(R.id.video_view);
		mPlayer = new Player(mSurfaceView);
		mPlayer.setListener(this);

		mTestTimeTv.setText(R.string.default_time_string);
		mStopBt.setOnClickListener(this);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_TIME:
				long testTime = System.currentTimeMillis() - mStartTime;
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				sdf.setTimeZone(TimeZone.getTimeZone("GMT-0:00"));
				mTestTimeTv.setText(sdf.format(new Date(testTime)));
				if (testTime >= mPlayVideoTime * TestUtils.MILLSECOND) {
					mPlayer.stop();
					Editor e = mSharedPreferences.edit();
					e.putInt(TestUtils.PLAY_VIDEO_RESULT, 1);
					e.commit();
					ArrayList<Class> testList = TestUtils.getTestList();
					int index = TestUtils.getCurrentTestIndex(PlayVideoActivity.class);
					Intent intent = null;
					if ((index + 1) < testList.size()) {
						Log.d(this, "onClick=>testList size: " + testList.size());
						intent = new Intent(PlayVideoActivity.this, testList.get(index + 1));
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					} else {
						intent = new Intent(PlayVideoActivity.this, ReportActivity.class);
						intent.putExtra(TestUtils.CIRCULATION_EXTRA, true);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					finish();
				} else {
					mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
				}
				break;
			}
		};
	};

	@Override
	public void onError() {
		mHandler.removeMessages(MSG_UPDATE_TIME);
		if (mPlayer != null) {
			mPlayer.stop();
		}
		Editor e = mSharedPreferences.edit();
		e.putInt(TestUtils.PLAY_VIDEO_RESULT, 0);
		e.commit();
		ArrayList<Class> testList = TestUtils.getTestList();
		int index = TestUtils.getCurrentTestIndex(PlayVideoActivity.class);
		Intent intent = null;
		if ((index + 1) < testList.size()) {
			Log.d(this, "onClick=>testList size: " + testList.size());
			intent = new Intent(this, testList.get(index + 1));
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else {
			intent = new Intent(this, ReportActivity.class);
			intent.putExtra(TestUtils.CIRCULATION_EXTRA, true);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		finish();
	}

}
package com.android.agingtest.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import com.android.agingtest.AgingTest;
import com.android.agingtest.Log;
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
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SleepActivity extends Activity implements OnClickListener {

	public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;

	private static final int MSG_UPDATE_TIME = 0;
	private static final int MSG_WAKE_UP = 1;
	private static final int MSG_GOTO_SLEEP = 2;

	private TextView mTestTimeTv;
	private Button mStopBt;
	private SharedPreferences mSharedPreferences;
	private PowerManager.WakeLock mLock;
	private PowerManager mPowerManager;

	private int mWakeUpDelayed;
	private int mGoToSleepDelayed;
	private int mSleepTime;
	private long mStartTime;
	private boolean mIsFirstStart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(FLAG_HOMEKEY_DISPATCHED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		super.onCreate(savedInstanceState);

		initActionBar();
		setContentView(R.layout.activity_reboot);

		initValues();
		initViews();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(this, "onResume=>first: " + mIsFirstStart);
		if (mIsFirstStart) {
			TestUtils.goToSleep(this);
			mHandler.sendEmptyMessageDelayed(MSG_WAKE_UP, mWakeUpDelayed);
			mHandler.sendEmptyMessage(MSG_UPDATE_TIME);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mIsFirstStart = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mLock.release();
		mHandler.removeMessages(MSG_WAKE_UP);
		mHandler.removeMessages(MSG_GOTO_SLEEP);
		mHandler.removeMessages(MSG_UPDATE_TIME);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mHandler.removeMessages(MSG_UPDATE_TIME);
			mHandler.removeMessages(MSG_GOTO_SLEEP);
			mHandler.removeMessages(MSG_UPDATE_TIME);
			long testTime = System.currentTimeMillis() - mStartTime;
			Editor e = mSharedPreferences.edit();
			if (testTime >= mSleepTime * TestUtils.MILLSECOND) {
				e.putInt(TestUtils.REBOOT_RESULT, 1);
			} else {
				e.putInt(TestUtils.REBOOT_RESULT, 0);
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
			mHandler.removeMessages(MSG_GOTO_SLEEP);
			long testTime = System.currentTimeMillis() - mStartTime;
			Editor e = mSharedPreferences.edit();
			if (testTime >= mSleepTime * TestUtils.MILLSECOND) {
				e.putInt(TestUtils.SLEEP_RESULT, 1);
			} else {
				e.putInt(TestUtils.SLEEP_RESULT, 0);
			}
			e.commit();
			ArrayList<Class> testList = TestUtils.getTestList();
			int index = TestUtils.getCurrentTestIndex(SleepActivity.class);
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
		mWakeUpDelayed = getResources().getInteger(R.integer.wake_up_delayed);
		mGoToSleepDelayed = getResources().getInteger(R.integer.go_to_sleep_delayed);
		mPowerManager = ((PowerManager) getSystemService(Context.POWER_SERVICE));
		mLock = mPowerManager.newWakeLock(1, "sprocomm");
		mLock.acquire();

		mStartTime = mSharedPreferences.getLong(TestUtils.SLEEP_START_TIME, -1);
		mSleepTime = mSharedPreferences.getInt(TestUtils.SLEEP_TIME,
				getResources().getInteger(R.integer.default_sleep_time));
		if (mStartTime == -1) {
			mIsFirstStart = true;
			mStartTime = System.currentTimeMillis();
			Editor e = mSharedPreferences.edit();
			e.putLong(TestUtils.SLEEP_START_TIME, mStartTime);
			e.commit();
		}
	}

	private void initViews() {
		mTestTimeTv = (TextView) findViewById(R.id.test_time);
		mStopBt = (Button) findViewById(R.id.stop_test);

		mTestTimeTv.setText(R.string.default_time_string);
		mStopBt.setOnClickListener(this);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			Log.d(this, "handleMessage=>what: " + msg.what);
			switch (msg.what) {
			case MSG_UPDATE_TIME:
				long testTime = System.currentTimeMillis() - mStartTime;
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				sdf.setTimeZone(TimeZone.getTimeZone("GMT-0:00"));
				mTestTimeTv.setText(sdf.format(new Date(testTime)));
				if (testTime >= mSleepTime * TestUtils.MILLSECOND) {
					if (!mPowerManager.isScreenOn()) {
						//mPowerManager.wakeUp(SystemClock.uptimeMillis());
					}
					mHandler.removeMessages(MSG_GOTO_SLEEP);
					mHandler.removeMessages(MSG_WAKE_UP);
					Editor e = mSharedPreferences.edit();
					e.putInt(TestUtils.SLEEP_RESULT, 1);
					e.commit();
					ArrayList<Class> testList = TestUtils.getTestList();
					int index = TestUtils.getCurrentTestIndex(SleepActivity.class);
					Intent intent = null;
					if ((index + 1) < testList.size()) {
						Log.d(this, "onClick=>testList size: " + testList.size());
						intent = new Intent(SleepActivity.this, testList.get(index + 1));
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					} else {
						intent = new Intent(SleepActivity.this, ReportActivity.class);
						intent.putExtra(TestUtils.CIRCULATION_EXTRA, true);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					finish();
				} else {
					mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
				}
				break;

			case MSG_WAKE_UP:
			//	mPowerManager.wakeUp(SystemClock.uptimeMillis());
				mHandler.sendEmptyMessageDelayed(MSG_GOTO_SLEEP, mGoToSleepDelayed);
				break;

			case MSG_GOTO_SLEEP:
			//	mPowerManager.goToSleep(SystemClock.uptimeMillis());
				mHandler.sendEmptyMessageDelayed(MSG_WAKE_UP, mWakeUpDelayed);
				break;
			}
		};
	};

}
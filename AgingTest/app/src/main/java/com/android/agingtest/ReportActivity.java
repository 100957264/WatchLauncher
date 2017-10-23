package com.android.agingtest;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import junit.framework.Test;

import com.android.agingtest.test.RebootActivity;

public class ReportActivity extends Activity implements OnClickListener {

	private static final int MSG_CIRCULATION = 0;

	private View mRebootContainer;
	private View mSleepContainer;
	private View mVibrateContainer;
	private View mReceiverContainer;
	private View mFrontTakingPictureContainer;
	private View mBackTakingPictureContainer;
	private View mPlayVideoContainer;

	private TextView mRebootTv;
	private TextView mSleepTv;
	private TextView mVibrateTv;
	private TextView mReceiverTv;
	private TextView mFrontTakingPictureTv;
	private TextView mBackTakingPictureTv;
	private TextView mPlayVideoTv;
	private Button mOk;
	private Button mCancel;

	private SharedPreferences mSharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		super.onCreate(savedInstanceState);

		initActionBar();

		setContentView(R.layout.activity_report);

		initValues();
		initViews();
	}

	@Override
	protected void onStart() {
		updateViewsVisible();
		super.onStart();
	}

	@Override
	protected void onResume() {
		updateUI();
		Intent intent = getIntent();
		boolean enabledCirculation = intent.getBooleanExtra(TestUtils.CIRCULATION_EXTRA, false);
		boolean isCirculation = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(TestUtils.CIRCULATION_STATE, getResources().getBoolean(R.bool.default_circulation_value));
		Log.d(this, "onResume=>enabled: " + enabledCirculation + " circulation: " + isCirculation);
		if (isCirculation && enabledCirculation) {
			if (!mHandler.hasMessages(MSG_CIRCULATION)) {
				mHandler.sendEmptyMessageDelayed(MSG_CIRCULATION, getResources().getInteger(R.integer.circulation_delayed));
			}
		} else {
			Editor e = mSharedPreferences.edit();
			e.putBoolean(TestUtils.TEST_STATE, true);
			e.commit();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mHandler.removeMessages(MSG_CIRCULATION);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, AgingTest.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.ok:
			mHandler.removeMessages(MSG_CIRCULATION);
			intent = new Intent(this, AgingTest.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			break;

		case R.id.cancel:
			mHandler.removeMessages(MSG_CIRCULATION);
			intent = new Intent(this, AgingTest.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			break;
		}
		Editor e = mSharedPreferences.edit();
		e.putBoolean(TestUtils.TEST_STATE, true);
		e.commit();
	}

	private void initActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void initValues() {
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	}

	private void initViews() {
		mRebootContainer = findViewById(R.id.reboot_container);
		mSleepContainer = findViewById(R.id.sleep_container);
		mVibrateContainer = findViewById(R.id.vibrate_container);
		mReceiverContainer = findViewById(R.id.receiver_container);
		mFrontTakingPictureContainer = findViewById(R.id.front_taking_picture_container);
		mBackTakingPictureContainer = findViewById(R.id.back_taking_picture_container);
		mPlayVideoContainer = findViewById(R.id.play_video_container);

		mRebootTv = (TextView) findViewById(R.id.reboot_report);
		mSleepTv = (TextView) findViewById(R.id.sleep_report);
		mVibrateTv = (TextView) findViewById(R.id.vibrate_report);
		mReceiverTv = (TextView) findViewById(R.id.receiver_report);
		mFrontTakingPictureTv = (TextView) findViewById(R.id.front_taking_picture_report);
		mBackTakingPictureTv = (TextView) findViewById(R.id.back_taking_picture_report);
		mPlayVideoTv = (TextView) findViewById(R.id.play_video_report);

		mOk = (Button) findViewById(R.id.ok);
		mCancel = (Button) findViewById(R.id.cancel);

		mOk.setOnClickListener(this);
		mCancel.setOnClickListener(this);
	}

	private void updateViewsVisible() {
		Resources res = getResources();

		if (!res.getBoolean(R.bool.reboot_visible)) {
			mRebootContainer.setVisibility(View.GONE);
		}

		if (!res.getBoolean(R.bool.sleep_visible)) {
			mSleepContainer.setVisibility(View.GONE);
		}

		if (!res.getBoolean(R.bool.vibrate_visible)) {
			mVibrateContainer.setVisibility(View.GONE);
		}

		if (!res.getBoolean(R.bool.receiver_visible)) {
			mReceiverContainer.setVisibility(View.GONE);
		}

		if (!res.getBoolean(R.bool.front_taking_picture_visible)) {
			mFrontTakingPictureContainer.setVisibility(View.GONE);
		}

		if (!res.getBoolean(R.bool.back_taking_picture_visible)) {
			mBackTakingPictureContainer.setVisibility(View.GONE);
		}

		if (!res.getBoolean(R.bool.play_video_visible)) {
			mPlayVideoContainer.setVisibility(View.GONE);
		}
	}

	private void updateUI() {
		int state = mSharedPreferences.getInt(TestUtils.REBOOT_RESULT, -1);
		mRebootTv.setText(getStateText(state));
		mRebootTv.setTextColor(getStateColor(state));

		state = mSharedPreferences.getInt(TestUtils.SLEEP_RESULT, -1);
		mSleepTv.setText(getStateText(state));
		mSleepTv.setTextColor(getStateColor(state));

		state = mSharedPreferences.getInt(TestUtils.VIBRATE_RESULT, -1);
		mVibrateTv.setText(getStateText(state));
		mVibrateTv.setTextColor(getStateColor(state));

		state = mSharedPreferences.getInt(TestUtils.RECEIVER_RESULT, -1);
		mReceiverTv.setText(getStateText(state));
		mReceiverTv.setTextColor(getStateColor(state));

		state = mSharedPreferences.getInt(TestUtils.FRONT_TAKING_PICTURE_RESULT, -1);
		mFrontTakingPictureTv.setText(getStateText(state));
		mFrontTakingPictureTv.setTextColor(getStateColor(state));

		state = mSharedPreferences.getInt(TestUtils.BACK_TAKING_PICTURE_RESULT, -1);
		mBackTakingPictureTv.setText(getStateText(state));
		mBackTakingPictureTv.setTextColor(getStateColor(state));

		state = mSharedPreferences.getInt(TestUtils.PLAY_VIDEO_RESULT, -1);
		mPlayVideoTv.setText(getStateText(state));
		mPlayVideoTv.setTextColor(getStateColor(state));
	}

	private int getStateText(int state) {
		switch (state) {
		case 0:
			return R.string.fail;

		case 1:
			return R.string.pass;

		default:
			return R.string.not_tested;
		}
	}

	private int getStateColor(int state) {
		Resources res = getResources();
		switch (state) {
		case 0:
			return res.getColor(R.color.fail_text_color);

		case 1:
			return res.getColor(R.color.pass_text_color);

		default:
			return res.getColor(R.color.not_test_text_color);
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_CIRCULATION:
				ArrayList<Class> testList = TestUtils.getTestList();
				Log.d(this, "handlMessage=>size: " + testList.size() + " first: " + testList.get(0).toString());
				if (testList.contains(RebootActivity.class)) {
					Editor e = mSharedPreferences.edit();
					e.putBoolean(TestUtils.TEST_STATE, true);
					e.commit();
				}
				if (testList.size() > 0) {
					TestUtils.clearHistoryValue(ReportActivity.this);
					Intent intent = new Intent(ReportActivity.this, testList.get(0));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);
				}
				break;
			}
		};
	};

}
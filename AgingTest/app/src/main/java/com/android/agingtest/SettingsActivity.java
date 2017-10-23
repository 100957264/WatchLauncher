package com.android.agingtest;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity implements OnClickListener, OnFocusChangeListener {

	private View mRebootContainer;
	private View mSleepContainer;
	private View mVibrateContainer;
	private View mReceiverContainer;
	private View mFrontTakingPictureContainer;
	private View mBackTakingPictureContainer;
	private View mPlayVideoContainer;
	private EditText mRebootEt;
	private EditText mSleepEt;
	private EditText mVibrateEt;
	private EditText mReceiverEt;
	private EditText mFrontTakingPictureEt;
	private EditText mBackTakingPictureEt;
	private EditText mPlayVideoEt;
	private Button mOk;
	private Button mCancel;
	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initActionBar();

		setContentView(R.layout.activity_settings);

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
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok:
			boolean result = updateSharedPreference();
			Toast.makeText(this, result ? R.string.setting_success : R.string.setting_fail, Toast.LENGTH_SHORT).show();
			finish();
			break;

		case R.id.cancel:
			finish();
			break;
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			switch (v.getId()) {
			case R.id.reboot_time:
				mRebootEt.setSelection(mRebootEt.getText().toString().length());
				break;

			case R.id.sleep_time:
				mSleepEt.setSelection(mSleepEt.getText().toString().length());
				break;

			case R.id.vibrate_time:
				mVibrateEt.setSelection(mVibrateEt.getText().toString().length());
				break;

			case R.id.receiver_time:
				mReceiverEt.setSelection(mReceiverEt.getText().toString().length());
				break;
				
			case R.id.front_taking_picture_time:
				mFrontTakingPictureEt.setSelection(mFrontTakingPictureEt.getText().toString().length());
				break;

			case R.id.back_taking_picture_time:
				mBackTakingPictureEt.setSelection(mBackTakingPictureEt.getText().toString().length());
				break;

			case R.id.play_video_time:
				mPlayVideoEt.setSelection(mPlayVideoEt.getText().toString().length());
				break;
			}
		}
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

		mRebootEt = (EditText) findViewById(R.id.reboot_time);
		mSleepEt = (EditText) findViewById(R.id.sleep_time);
		mVibrateEt = (EditText) findViewById(R.id.vibrate_time);
		mReceiverEt = (EditText) findViewById(R.id.receiver_time);
		mFrontTakingPictureEt = (EditText) findViewById(R.id.front_taking_picture_time);
		mBackTakingPictureEt = (EditText) findViewById(R.id.back_taking_picture_time);
		mPlayVideoEt = (EditText) findViewById(R.id.play_video_time);
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
		Resources res = getResources();
		mRebootEt.setText(
				mSharedPreferences.getInt(TestUtils.REBOOT_TIME, res.getInteger(R.integer.default_reboot_time)) + "");
		mSleepEt.setText(
				mSharedPreferences.getInt(TestUtils.SLEEP_TIME, res.getInteger(R.integer.default_sleep_time)) + "");
		mVibrateEt.setText(
				mSharedPreferences.getInt(TestUtils.VIBRATE_TIME, res.getInteger(R.integer.default_vibrate_time)) + "");
		mReceiverEt.setText(
				mSharedPreferences.getInt(TestUtils.RECEIVER_TIME, res.getInteger(R.integer.default_receiver_time))
						+ "");
		mFrontTakingPictureEt.setText(mSharedPreferences.getInt(TestUtils.FRONT_TAKING_PICTURE_TIME,
				res.getInteger(R.integer.default_front_taking_picture_time)) + "");
		mBackTakingPictureEt.setText(mSharedPreferences.getInt(TestUtils.BACK_TAKING_PICTURE_TIME,
				res.getInteger(R.integer.default_back_taking_picture_time)) + "");
		mPlayVideoEt.setText(
				mSharedPreferences.getInt(TestUtils.PLAY_VIDEO_TIME, res.getInteger(R.integer.default_play_video_time))
						+ "");

		mRebootEt.setOnFocusChangeListener(this);
		mSleepEt.setOnFocusChangeListener(this);
		mVibrateEt.setOnFocusChangeListener(this);
		mReceiverEt.setOnFocusChangeListener(this);
		mFrontTakingPictureEt.setOnFocusChangeListener(this);
		mBackTakingPictureEt.setOnFocusChangeListener(this);
		mPlayVideoEt.setOnFocusChangeListener(this);
	}

	private boolean updateSharedPreference() {
		boolean result = true;
		int time = 0;
		Resources res = getResources();
		Editor editor = mSharedPreferences.edit();
		try {
			String timeStr = mRebootEt.getText().toString();
            Log.d(this, "dengli SettingsActivity timeStr== "+timeStr);
			if (TextUtils.isEmpty(timeStr) || !TextUtils.isDigitsOnly(timeStr)) {
				editor.putInt(TestUtils.REBOOT_TIME, res.getInteger(R.integer.default_reboot_time));
			} else {
				time = Integer.parseInt(timeStr);
                Log.d(this, "dengli SettingsActivity time== "+time);
				editor.putInt(TestUtils.REBOOT_TIME, time);
			}

			timeStr = mSleepEt.getText().toString();
			if (TextUtils.isEmpty(timeStr) || !TextUtils.isDigitsOnly(timeStr)) {
				editor.putInt(TestUtils.SLEEP_TIME, res.getInteger(R.integer.default_sleep_time));
			} else {
				time = Integer.parseInt(timeStr);
				editor.putInt(TestUtils.SLEEP_TIME, time);
			}

			timeStr = mVibrateEt.getText().toString();
			if (TextUtils.isEmpty(timeStr) || !TextUtils.isDigitsOnly(timeStr)) {
				editor.putInt(TestUtils.VIBRATE_TIME, res.getInteger(R.integer.default_vibrate_time));
			} else {
				time = Integer.parseInt(timeStr);
				editor.putInt(TestUtils.VIBRATE_TIME, time);
			}

			timeStr = mReceiverEt.getText().toString();
			if (TextUtils.isEmpty(timeStr) || !TextUtils.isDigitsOnly(timeStr)) {
				editor.putInt(TestUtils.RECEIVER_TIME, res.getInteger(R.integer.default_receiver_time));
			} else {
				time = Integer.parseInt(timeStr);
				editor.putInt(TestUtils.RECEIVER_TIME, time);
			}
			
			timeStr = mFrontTakingPictureEt.getText().toString();
			if (TextUtils.isEmpty(timeStr) || !TextUtils.isDigitsOnly(timeStr)) {
				editor.putInt(TestUtils.FRONT_TAKING_PICTURE_TIME, res.getInteger(R.integer.default_front_taking_picture_time));
			} else {
				time = Integer.parseInt(timeStr);
				editor.putInt(TestUtils.FRONT_TAKING_PICTURE_TIME, time);
			}

			timeStr = mBackTakingPictureEt.getText().toString();
			if (TextUtils.isEmpty(timeStr) || !TextUtils.isDigitsOnly(timeStr)) {
				editor.putInt(TestUtils.BACK_TAKING_PICTURE_TIME, res.getInteger(R.integer.default_back_taking_picture_time));
			} else {
				time = Integer.parseInt(timeStr);
				editor.putInt(TestUtils.BACK_TAKING_PICTURE_TIME, time);
			}

			timeStr = mPlayVideoEt.getText().toString();
			if (TextUtils.isEmpty(timeStr) || !TextUtils.isDigitsOnly(timeStr)) {
				editor.putInt(TestUtils.PLAY_VIDEO_TIME, res.getInteger(R.integer.default_play_video_time));
			} else {
				time = Integer.parseInt(timeStr);
				editor.putInt(TestUtils.PLAY_VIDEO_TIME, time);
			}
		} catch (Exception e) {
			Log.e(this, "updateSharedPreference=>error: ", e);
			result = false;
		} finally {
			editor.commit();
		}
		return result;
	}

}
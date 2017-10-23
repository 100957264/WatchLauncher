package com.android.agingtest;

import java.util.ArrayList;

import com.android.agingtest.test.BackTakingPictureActivity;
import com.android.agingtest.test.FrontTakingPictureActivity;
import com.android.agingtest.test.PlayVideoActivity;
import com.android.agingtest.test.RebootActivity;
import com.android.agingtest.test.ReceiverActivity;
import com.android.agingtest.test.SleepActivity;
import com.android.agingtest.test.VibrateActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
public class AgingTest extends Activity implements OnClickListener, OnCheckedChangeListener {
	
	private CheckBox mRebootCb;
	private CheckBox mSleepCb;
	private CheckBox mVibrateCb;
	private CheckBox mReceiverCb;
	private CheckBox mFrontTakingPictureCb;
	private CheckBox mBackTakingPictureCb;
	private CheckBox mPlayVideoCb;
	private CheckBox mCirculationCb;
	
    private RadioGroup mTotaltesttime; 
    private RadioButton mTwohour;
    private RadioButton mFourhour;
    private RadioButton mSixhour;
    private RadioButton mTwelvehour;
    private RadioButton mTwentyfourhour;
	private Button mStartBt;
//	private Button mStopBt;
	
	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aging_test);
		
		initValues();
		initViews();
		updateViewsVisible();
	}
	
	@Override
	protected void onResume() {
		updateTestList();
		updateUI();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.aging_test, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		/*case R.id.action_test_time:
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			return true;*/
			
		case R.id.action_test_report:
			Intent report = new Intent(this, ReportActivity.class);
			report.putExtra(TestUtils.CIRCULATION_EXTRA, false);
			startActivity(report);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Editor editor = mSharedPreferences.edit();
		ArrayList<Class> testList = TestUtils.getTestList();
		switch (buttonView.getId()) {
		case R.id.reboot_test:
			editor.putBoolean(TestUtils.REBOOT_STATE, isChecked);
			if (isChecked) {
				if (!testList.contains(RebootActivity.class)) {
					testList.add(RebootActivity.class);
				}
			} else {
				if (testList.contains(RebootActivity.class)) {
					testList.remove(RebootActivity.class);
				}
			}
			break;
			
		case R.id.sleep_test:
			editor.putBoolean(TestUtils.SLEEP_STATE, isChecked);
			if (isChecked) {
				if (!testList.contains(SleepActivity.class)) {
					testList.add(SleepActivity.class);
				}
			} else {
				if (testList.contains(SleepActivity.class)) {
					testList.remove(SleepActivity.class);
				}
			}
			break;
			
		case R.id.vibrate_test:
			editor.putBoolean(TestUtils.VIBRATE_STATE, isChecked);
			if (isChecked) {
				if (!testList.contains(VibrateActivity.class)) {
					testList.add(VibrateActivity.class);
				}
			} else {
				if (testList.contains(VibrateActivity.class)) {
					testList.remove(VibrateActivity.class);
				}
			}
			break;
			
		case R.id.receiver_test:
			editor.putBoolean(TestUtils.RECEIVER_STATE, isChecked);
			if (isChecked) {
				if (!testList.contains(ReceiverActivity.class)) {
					testList.add(ReceiverActivity.class);
				}
			} else {
				if (testList.contains(ReceiverActivity.class)) {
					testList.remove(ReceiverActivity.class);
				}
			}
			break;
			
		case R.id.front_taking_picture_test:
			editor.putBoolean(TestUtils.FRONT_TAKING_PICTURE_STATE, isChecked);
			if (isChecked) {
				if (!testList.contains(FrontTakingPictureActivity.class)) {
					testList.add(FrontTakingPictureActivity.class);
				}
			} else {
				if (testList.contains(FrontTakingPictureActivity.class)) {
					testList.remove(FrontTakingPictureActivity.class);
				}
			}
			break;
			
		case R.id.back_taking_picture_test:
			editor.putBoolean(TestUtils.BACK_TAKING_PICTURE_STATE, isChecked);
			if (isChecked) {
				if (!testList.contains(BackTakingPictureActivity.class)) {
					testList.add(BackTakingPictureActivity.class);
				}
			} else {
				if (testList.contains(BackTakingPictureActivity.class)) {
					testList.remove(BackTakingPictureActivity.class);
				}
			}
			break;
			
		case R.id.play_video_test:
			editor.putBoolean(TestUtils.PLAY_VIDEO_STATE, isChecked);
			if (isChecked) {
				if (!testList.contains(PlayVideoActivity.class)) {
					testList.add(PlayVideoActivity.class);
				}
			} else {
				if (testList.contains(PlayVideoActivity.class)) {
					testList.remove(PlayVideoActivity.class);
				}
			}
			break;
			
		case R.id.circulation_test:
			editor.putBoolean(TestUtils.CIRCULATION_STATE, isChecked);
			break;
		}
		editor.commit();
		mStartBt.setEnabled(testList.size() > 0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start:
            boolean result = updateSharedPreference(TestUtils.getTestList().size());
			//Toast.makeText(this, result ? R.string.setting_success : R.string.setting_fail, Toast.LENGTH_SHORT).show();
            Log.d(this, "dengli  AgingTest result== "+result);
            if(result){
                TestUtils.clearHistoryValue(this);
                TestUtils.updateTestList(this);
                if (TestUtils.getTestList().contains(RebootActivity.class)) {
                    Editor e = mSharedPreferences.edit();
                    e.putBoolean(TestUtils.TEST_STATE, true);
                    e.commit();
                }
                Log.d(this, "onClick=>size: " + TestUtils.getTestList().size() + " first: " + TestUtils.getTestList().get(0));
                Intent intent = new Intent(this, TestUtils.getTestList().get(0));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else{
                Toast.makeText(this, R.string.setting_fail, Toast.LENGTH_SHORT).show();
            }
			break;
			
//		case R.id.stop:
//			
//			break;
		}
	}
	
	private void initValues() {
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		Editor e = mSharedPreferences.edit();
		e.putBoolean(TestUtils.TEST_STATE, false);
		e.commit();
	}

    int checkTime;
	private void initViews() {
		mRebootCb = (CheckBox) findViewById(R.id.reboot_test);
		mSleepCb = (CheckBox) findViewById(R.id.sleep_test);
		mVibrateCb = (CheckBox) findViewById(R.id.vibrate_test);
		mReceiverCb = (CheckBox) findViewById(R.id.receiver_test);
		mFrontTakingPictureCb = (CheckBox) findViewById(R.id.front_taking_picture_test);
		mBackTakingPictureCb = (CheckBox) findViewById(R.id.back_taking_picture_test);
		mPlayVideoCb = (CheckBox) findViewById(R.id.play_video_test);
		mCirculationCb = (CheckBox) findViewById(R.id.circulation_test);
		mTotaltesttime=(RadioGroup)findViewById(R.id.totaltesttime);
        
        
        mTwohour=(RadioButton)findViewById(R.id.twohour);
        mTwohour.setChecked(true);
        checkTime=120;
        mFourhour=(RadioButton)findViewById(R.id.fourhour);
        mSixhour=(RadioButton)findViewById(R.id.sixhour);
        mTwelvehour=(RadioButton)findViewById(R.id.twelvehour);
        mTwentyfourhour=(RadioButton)findViewById(R.id.twentyfourhour);
        
		mStartBt = (Button) findViewById(R.id.start);
//		mStopBt = (Button) findViewById(R.id.stop);
        //Editor editor = mSharedPreferences.edit();
        
		mTotaltesttime.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){  
            @Override  
            public void onCheckedChanged(RadioGroup rg,int checkedId)  
            {
                switch(checkedId){  
                case R.id.twohour:
                    String timeStr = "120";
                    Log.d(this, "dengli  AgingTest timeStr== "+timeStr);
                    checkTime=120;
                    break;  
                case R.id.fourhour:
                    checkTime=240;
                    break;
                case R.id.sixhour:
                    checkTime=360;
                    break;
                case R.id.twelvehour:
                    checkTime=720;
                    break;
                case R.id.twentyfourhour:
                    checkTime=1440;
                    break;
                }  
            }  
        });  
		mRebootCb.setOnCheckedChangeListener(this);
		mSleepCb.setOnCheckedChangeListener(this);
		mVibrateCb.setOnCheckedChangeListener(this);
		mReceiverCb.setOnCheckedChangeListener(this);
		mFrontTakingPictureCb.setOnCheckedChangeListener(this);
		mBackTakingPictureCb.setOnCheckedChangeListener(this);
		mPlayVideoCb.setOnCheckedChangeListener(this);
		mCirculationCb.setOnCheckedChangeListener(this);
		
		mStartBt.setOnClickListener(this);
//		mStopBt.setOnClickListener(this);
	}
	private boolean updateSharedPreference(int size) {
        boolean result = true;
        Editor editor = mSharedPreferences.edit();
        try {
            int time = checkTime/size;
            Log.d(this, "dengli  AgingTest updateSharedPreference time== "+time);    
            editor.putInt(TestUtils.REBOOT_TIME, time);
            editor.putInt(TestUtils.SLEEP_TIME, time);
            editor.putInt(TestUtils.VIBRATE_TIME, time);
            editor.putInt(TestUtils.RECEIVER_TIME, time);
            editor.putInt(TestUtils.FRONT_TAKING_PICTURE_TIME, time);
            editor.putInt(TestUtils.BACK_TAKING_PICTURE_TIME, time);
            editor.putInt(TestUtils.PLAY_VIDEO_TIME, time);
        } catch (Exception e) {
			Log.e(this, "updateSharedPreference=>error: ", e);
            result = false;
		} finally {
			editor.commit();
		}
        return result;
    }
	private void updateViewsVisible() {
		Resources res = getResources();
		if (!res.getBoolean(R.bool.reboot_visible)) {
			mRebootCb.setVisibility(View.GONE);
		}
		
		if (!res.getBoolean(R.bool.sleep_visible)) {
			mSleepCb.setVisibility(View.GONE);
		}
		
		if (!res.getBoolean(R.bool.vibrate_visible)) {
			mVibrateCb.setVisibility(View.GONE);
		}
		
		if (!res.getBoolean(R.bool.receiver_visible)) {
			mReceiverCb.setVisibility(View.GONE);
		}
		
		if (!res.getBoolean(R.bool.front_taking_picture_visible)) {
			mFrontTakingPictureCb.setVisibility(View.GONE);
		}
		
		if (!res.getBoolean(R.bool.back_taking_picture_visible)) {
			mBackTakingPictureCb.setVisibility(View.GONE);
		}
		
		if (!res.getBoolean(R.bool.play_video_visible)) {
			mPlayVideoCb.setVisibility(View.GONE);
		}
	}
	
	private void updateTestList() {
		ArrayList<Class> testList = TestUtils.getTestList();
		testList.clear();
		boolean isChecked = mRebootCb.isChecked();
		if (isChecked) {
			if (!testList.contains(RebootActivity.class)) {
				testList.add(RebootActivity.class);
			}
		} else {
			if (testList.contains(RebootActivity.class)) {
				testList.remove(RebootActivity.class);
			}
		}
		
		isChecked = mSleepCb.isChecked();
		if (isChecked) {
			if (!testList.contains(SleepActivity.class)) {
				testList.add(SleepActivity.class);
			}
		} else {
			if (testList.contains(SleepActivity.class)) {
				testList.remove(SleepActivity.class);
			}
		}
		
		isChecked = mVibrateCb.isChecked();
		if (isChecked) {
			if (!testList.contains(VibrateActivity.class)) {
				testList.add(VibrateActivity.class);
			}
		} else {
			if (testList.contains(VibrateActivity.class)) {
				testList.remove(VibrateActivity.class);
			}
		}
		
		isChecked = mReceiverCb.isChecked();
		if (isChecked) {
			if (!testList.contains(ReceiverActivity.class)) {
				testList.add(ReceiverActivity.class);
			}
		} else {
			if (testList.contains(ReceiverActivity.class)) {
				testList.remove(ReceiverActivity.class);
			}
		}
		
		isChecked = mFrontTakingPictureCb.isChecked();
		if (isChecked) {
			if (!testList.contains(FrontTakingPictureActivity.class)) {
				testList.add(FrontTakingPictureActivity.class);
			}
		} else {
			if (testList.contains(FrontTakingPictureActivity.class)) {
				testList.remove(FrontTakingPictureActivity.class);
			}
		}
		
		isChecked = mBackTakingPictureCb.isChecked();
		if (isChecked) {
			if (!testList.contains(BackTakingPictureActivity.class)) {
				testList.add(BackTakingPictureActivity.class);
			}
		} else {
			if (testList.contains(BackTakingPictureActivity.class)) {
				testList.remove(BackTakingPictureActivity.class);
			}
		}
		
		isChecked = mPlayVideoCb.isChecked();
		if (isChecked) {
			if (!testList.contains(PlayVideoActivity.class)) {
				testList.add(PlayVideoActivity.class);
			}
		} else {
			if (testList.contains(PlayVideoActivity.class)) {
				testList.remove(PlayVideoActivity.class);
			}
		}
	}
	
	private void updateUI() {
		Resources res = getResources();
		mRebootCb.setChecked(mSharedPreferences.getBoolean(TestUtils.REBOOT_STATE, res.getBoolean(R.bool.default_reboot_value)));
		mSleepCb.setChecked(mSharedPreferences.getBoolean(TestUtils.SLEEP_STATE, res.getBoolean(R.bool.default_sleep_value)));
		mVibrateCb.setChecked(mSharedPreferences.getBoolean(TestUtils.VIBRATE_STATE, res.getBoolean(R.bool.default_vibrate_value)));
		mReceiverCb.setChecked(mSharedPreferences.getBoolean(TestUtils.RECEIVER_STATE, res.getBoolean(R.bool.default_receiver_value)));
		mFrontTakingPictureCb.setChecked(mSharedPreferences.getBoolean(TestUtils.FRONT_TAKING_PICTURE_STATE, res.getBoolean(R.bool.default_front_taking_picture_value)));
		mBackTakingPictureCb.setChecked(mSharedPreferences.getBoolean(TestUtils.BACK_TAKING_PICTURE_STATE, res.getBoolean(R.bool.default_back_taking_picture_value)));
		mPlayVideoCb.setChecked(mSharedPreferences.getBoolean(TestUtils.PLAY_VIDEO_STATE, res.getBoolean(R.bool.default_play_video_value)));
		mCirculationCb.setChecked(mSharedPreferences.getBoolean(TestUtils.CIRCULATION_STATE, res.getBoolean(R.bool.default_circulation_value)));
		
		mRebootCb.setTextColor(getStateColor(mSharedPreferences.getInt(TestUtils.REBOOT_RESULT, -1)));
		mSleepCb.setTextColor(getStateColor(mSharedPreferences.getInt(TestUtils.SLEEP_RESULT, -1)));
		mVibrateCb.setTextColor(getStateColor(mSharedPreferences.getInt(TestUtils.VIBRATE_RESULT, -1)));
		mReceiverCb.setTextColor(getStateColor(mSharedPreferences.getInt(TestUtils.RECEIVER_RESULT, -1)));
		mFrontTakingPictureCb.setTextColor(getStateColor(mSharedPreferences.getInt(TestUtils.FRONT_TAKING_PICTURE_RESULT, -1)));
		mBackTakingPictureCb.setTextColor(getStateColor(mSharedPreferences.getInt(TestUtils.BACK_TAKING_PICTURE_RESULT, -1)));
		mPlayVideoCb.setTextColor(getStateColor(mSharedPreferences.getInt(TestUtils.PLAY_VIDEO_RESULT, -1)));
		
		mStartBt.setEnabled(TestUtils.getTestList().size() > 0);
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

}

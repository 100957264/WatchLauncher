package com.android.agingtest;

import java.util.ArrayList;

import com.android.agingtest.test.RebootActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

import android.preference.PreferenceManager;
import android.os.PowerManager;


public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(this, "onReceive=>action: " + intent.getAction());
		String action = intent.getAction();
		Resources res = context.getResources();
		PowerManager pm =  ((PowerManager) context.getSystemService(Context.POWER_SERVICE));
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			boolean isInTest = sp.getBoolean(TestUtils.TEST_STATE, false);
			boolean isCirculation = sp.getBoolean(TestUtils.CIRCULATION_STATE,
					res.getBoolean(R.bool.default_circulation_value));
			boolean testRebootEnabled = sp.getBoolean(TestUtils.REBOOT_STATE,
					res.getBoolean(R.bool.default_reboot_value));
			int rebootTestTime = sp.getInt(TestUtils.REBOOT_TIME, res.getInteger(R.integer.default_reboot_time));
			long rebootStartTime = sp.getLong(TestUtils.REBOOT_START_TIME, System.currentTimeMillis());
			Log.d(this, "onReceive=>isInTest: " + isInTest + " isCirculation: " + isCirculation + " reboot: "
					+ testRebootEnabled + " rebootStartTime: " + rebootStartTime);
			Log.d(this, "onReceive=>current time: " + System.currentTimeMillis() );
			if (isInTest) {
				Log.d(this, "onReceive=>enabled: " + testRebootEnabled);
				if (testRebootEnabled) {
					if (!pm.isScreenOn()) {
						//pm.wakeUp(SystemClock.uptimeMillis());
					}
					Log.d(this, "onReceive=>testTime: " + rebootTestTime);
					if (System.currentTimeMillis() - rebootStartTime >= rebootTestTime
							* TestUtils.MILLSECOND) {
						Editor e = sp.edit();
						e.putInt(TestUtils.REBOOT_RESULT, 1);
						e.putBoolean(TestUtils.TEST_STATE, false);
						e.commit();
						TestUtils.updateTestList(context);
						ArrayList<Class> testList = TestUtils.getTestList();
						int index = TestUtils.getCurrentTestIndex(RebootActivity.class);
						Log.d(this, "onReceive=>size: " + testList.size() + " index: " + index );
						if ((index + 1) < testList.size()) {
							Log.d(this, "onReceive=>class: " + testList.get(index + 1));
							Intent nextTest = new Intent(context, testList.get(index + 1));
							nextTest.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(nextTest);
						} else {
							Editor editor = sp.edit();
							editor.putBoolean(TestUtils.TEST_STATE, false);
							editor.commit();
							Intent report = new Intent(context, ReportActivity.class);
							intent.putExtra(TestUtils.CIRCULATION_EXTRA, true);
							report.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(report);
						}
					} else {
						Intent reboot = new Intent(context, RebootActivity.class);
						reboot.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(reboot);
					}
				}
			}
		}/* else if (TelephonyIntents.SECRET_CODE_ACTION.equals(action)) {
			Uri agingTestUri = Uri.parse("android_secret_code://" + context.getResources().getString(R.string.aging_test_secret_code));
			Uri uri = intent.getData();
			Log.d(this, "onReceive=>uri: " + uri + " agingTest: " + agingTestUri);
			if (agingTestUri.equals(uri)) {
				Intent agingTest = new Intent(context, AgingTest.class);
				agingTest.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(agingTest);
			}
		}*/
	}

}

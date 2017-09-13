package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;


/**
 *  搜索消息界面
 */
public class SearchMessageActivity extends   TTBaseFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
      //  ActivityManager.getInstance().pushActivity(this);
		setContentView(R.layout.tt_fragment_message_activity_search); //tt_fragment_activity_search
	//	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
     //   ActivityManager.getInstance().popActivity(this);
		super.onDestroy();
	}

}

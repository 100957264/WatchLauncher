package com.fise.xiaoyu.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.utils.StatusBarUtil;


/**
 *  搜索界面
 */
public class SearchActivity extends   TTBaseFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_fragment_activity_search);
		StatusBarUtil.transparencyBar1(this);
	//	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
       // ActivityManager.getInstance().popActivity(this);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
        startActivity(new Intent(this , MainActivity.class));
        finish();

    }
}

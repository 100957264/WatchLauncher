package com.fise.xw.ui.activity;

import android.os.Bundle;
import android.view.WindowManager;

import com.fise.xw.R;
import com.fise.xw.imservice.manager.IMStackManager;
import com.fise.xw.ui.base.TTBaseFragmentActivity;



/**
 *  搜索界面
 * @author weileiguan
 *
 */
public class SearchActivity extends   TTBaseFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        IMStackManager.getStackManager().pushActivity(this);
		setContentView(R.layout.tt_fragment_activity_search);
	//	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
        IMStackManager.getStackManager().popActivity(this);
		super.onDestroy();
	}

}

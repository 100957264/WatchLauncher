package com.fise.xiaoyu.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.ui.fragment.WebviewFragment;


/**
 *  跳转Url界面
 */
public class WebViewFragmentActivity extends TTBaseFragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent=getIntent();
		if (intent.hasExtra(IntentConstant.WEBVIEW_URL)) {
			WebviewFragment.setUrl(intent.getStringExtra(IntentConstant.WEBVIEW_URL));
		}
		setContentView(R.layout.tt_fragment_activity_webview);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}

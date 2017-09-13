package com.fise.xw.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.fise.xw.R;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.ui.fragment.WebviewFragment;


/**
 *  跳转Url界面
 * @author weileiguan
 *
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

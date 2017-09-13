package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.ProgressWebView;
import com.fise.xiaoyu.utils.Logger;

/**
 * 跳转到Url 界面  可作优化 与webviewTextAcitivy 有点重复
 */
@SuppressLint("NewApi")
public class WebViewActivity extends TTBaseActivity {
		
	private Logger logger = Logger.getLogger(WebViewActivity.class);
	private Handler uiHandler = new Handler();
	private IMService imService;
	
	private ProgressWebView webView;
	private String url;
	private String text;
	private boolean showBack;


	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onServiceDisconnected() {
		}

		@Override
		public void onIMServiceConnected() {
			logger.d("login#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			try {
				do {
					if (imService == null) {
						// 后台服务启动链接失败
						break;
					}
					
					return;
				} while (false);

				// 异常分支都会执行这个
				// handleNoLoginIdentity();
			} catch (Exception e) {
				// 任何未知的异常
				logger.w("loadIdentity failed");
				// handleNoLoginIdentity();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_webview);
		
		
		imServiceConnector.connect(WebViewActivity.this);

		url = getIntent().getStringExtra(IntentConstant.WEB_URL); 
		text = getIntent().getStringExtra(IntentConstant.WEB_URL_RETURN);
		showBack =  getIntent().getBooleanExtra(IntentConstant.WEB_IS_RETURN,true);


		if(showBack == false){
			FrameLayout top_color = (FrameLayout) findViewById(R.id.top_color);
			View show_line = (View) findViewById(R.id.show_line);

			top_color.setVisibility(View.GONE);
			show_line.setVisibility(View.GONE);

		}
		TextView returnText = (TextView) findViewById(R.id.returnText);  
		returnText.setText("" + text);

		webView = (ProgressWebView) findViewById(R.id.webView);
		webView.loadUrl(url);
        webView.getSettings().setJavaScriptEnabled(true);
       // webView.getSettings().setUseWideViewPort(true); 
       // webView.getSettings().setLoadWithOverviewMode(true); 
       /// webView.getSettings().setTextSize(TextSize.SMALLER);
        webView.getSettings().setDisplayZoomControls(false);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.getSettings().setSupportMultipleWindows(true);

        returnText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WebViewActivity.this.finish();
			}
		});
         
        Button icon_arrow = (Button) findViewById(R.id.icon_arrow);   
        icon_arrow.setOnClickListener(new OnClickListener() {

  			@Override
  			public void onClick(View v) {
  				WebViewActivity.this.finish();
  			}
  		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(WebViewActivity.this);
	}

	// 为什么会有两个这个
	// 可能是 兼容性的问题 导致两种方法onBackPressed
	@Override
	public void onBackPressed() {
		logger.d("login#onBackPressed");
		// imLoginMgr.cancel();
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
 

	 
}

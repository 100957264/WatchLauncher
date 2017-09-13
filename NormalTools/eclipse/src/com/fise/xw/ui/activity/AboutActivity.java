package com.fise.xw.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.SystemConfigEntity;
import com.fise.xw.DB.sp.LoginSp; 
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.utils.Logger;  
import de.greenrobot.event.EventBus;



/**
 * 关于界面
 */
@SuppressLint("NewApi")
public class AboutActivity extends TTBaseActivity {
		
	private Logger logger = Logger.getLogger(AboutActivity.class);
	private Handler uiHandler = new Handler();
	private IMService imService;

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
					IMLoginManager loginManager = imService.getLoginManager();
					LoginSp loginSp = imService.getLoginSp();
					if (loginManager == null || loginSp == null) {
						// 无法获取登陆控制器
						break;
					}

					TextView app_version = (TextView) findViewById(R.id.app_version);
					app_version.setText(getVersion());

					TextView unread_update_version = (TextView) findViewById(R.id.unread_update_version);
					unread_update_version.setText("1"); 

					final SystemConfigEntity systemVerstion = imService.getContactManager().getSystemConfig(); 
					RelativeLayout receiver_mode = (RelativeLayout) findViewById(R.id.receiver_mode);  
					
					if (getVersion().compareTo(systemVerstion.getVersion()) < 0) {
						
						unread_update_version.setVisibility(View.VISIBLE); 
						receiver_mode.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub 
								openApplicationMarket(systemVerstion.getUpdateUrl());

							}
						});
					} else {
						unread_update_version.setVisibility(View.GONE);
					}

					RelativeLayout mark = (RelativeLayout) findViewById(R.id.mark);
					mark.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub 
							// openApplicationPackageMarket(getSystemPackageName());
							openApplicationMarket(systemVerstion.getUpdateUrl());

						}
					});

					RelativeLayout check_version = (RelativeLayout) findViewById(R.id.check_version);
					check_version.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub 
							
							if (getVersion().compareTo(systemVerstion.getVersion()) < 0) { 
								
								openApplicationMarket(systemVerstion.getUpdateUrl());
							} else {
								Toast.makeText(AboutActivity.this, "当前为最新版本",
										Toast.LENGTH_SHORT).show();
							}

						}
					});
					
					
					RelativeLayout function_introduction = (RelativeLayout) findViewById(R.id.function_introduction);
					function_introduction.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub 
								  
							//Intent intent = new Intent();  
							//intent.setClass(AboutActivity.this, FunctionActivity.class);  
							//startActivity(intent);  
							Intent intentUrl = new Intent(AboutActivity.this, WebViewActivity.class);
							intentUrl.putExtra(IntentConstant.WEB_URL, systemVerstion.getVersionComment());
							intentUrl.putExtra(IntentConstant.WEB_URL_RETURN, "功能介绍");
							startActivity(intentUrl);
							
						}
					});
					 
					
					
					RelativeLayout system_notification = (RelativeLayout) findViewById(R.id.system_notification);
					system_notification.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub 
					  
							Intent intentUrl = new Intent(AboutActivity.this, WebViewActivity.class);
							intentUrl.putExtra(IntentConstant.WEB_URL, systemVerstion.getSystemNotice());
							intentUrl.putExtra(IntentConstant.WEB_URL_RETURN, "系统通知");
							startActivity(intentUrl);
							
							
						}
					});
					 

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

		imServiceConnector.connect(AboutActivity.this);
		EventBus.getDefault().register(this);

		setContentView(R.layout.tt_activity_about);
		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AboutActivity.this.finish();
			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AboutActivity.this.finish();
			}
		});
		
		
		
		

	}

	/**
	 * 通过包名 在应用商店打开应用
	 * 
	 * @param packageName
	 *            包名
	 */
	private void openApplicationPackageMarket(String packageName) {
		try {
			String str = "market://details?id=" + packageName;
			Intent localIntent = new Intent(Intent.ACTION_VIEW);
			localIntent.setData(Uri.parse(str));
			startActivity(localIntent);
		} catch (Exception e) {
			// 打开应用商店失败 可能是没有手机没有安装应用市场
			e.printStackTrace();
			Toast.makeText(AboutActivity.this, "打开应用商店失败",
					Toast.LENGTH_SHORT).show();
			// 调用系统浏览器进入商城
			String url = "http://app.mi.com/detail/163525?ref=search";
			openLinkBySystem(url);
		}
	}

	/**
	 * 通过url 在应用商店打开应用
	 * 
	 * @param packageName
	 *            包名
	 */
	private void openApplicationMarket(String url) { // packageName

		try {
			// String str = "market://details?id=" + packageName;
			Intent localIntent = new Intent(Intent.ACTION_VIEW);
			localIntent.setData(Uri.parse(url));
			AboutActivity.this.startActivity(localIntent);
		} catch (Exception e) {
			// 打开应用商店失败 可能是没有手机没有安装应用市场
			e.printStackTrace();
			Toast.makeText(AboutActivity.this, "打开应用商店失败",
					Toast.LENGTH_SHORT).show();
			// 调用系统浏览器进入商城
			// String urlTest = "http://app.mi.com/detail/163525?ref=search";
			openLinkBySystem(url);
		}

		// Intent intent = new Intent(AboutActivity.this,
		// UpdateAppActivity.class);
		// intent.putExtra("url", url);
		// AboutActivity.this.startActivity(intent);
	}

	/**
	 * 调用系统浏览器打开网页
	 * 
	 * @param url
	 *            地址
	 */
	private void openLinkBySystem(String url) {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(intent);

	}

	//
	public String getVersion() {
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			String version = info.versionName;
			return "" + version;
		} catch (Exception e) {
			e.printStackTrace();
			return this.getString(R.string.app_version);
		}
	}

	//
	public String getSystemPackageName() {
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			String packageName = info.packageName;
			return "" + packageName;
		} catch (Exception e) {
			e.printStackTrace();
			return this.getString(R.string.app_version);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(AboutActivity.this);
		EventBus.getDefault().unregister(this);
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

	/**
	 * ----------------------------event 事件驱动----------------------------
	 */
	public void onEventMainThread(LoginEvent event) {
		switch (event) {
		case LOCAL_LOGIN_SUCCESS:
		case LOGIN_OK:
			break;

		}
	}

	 
}

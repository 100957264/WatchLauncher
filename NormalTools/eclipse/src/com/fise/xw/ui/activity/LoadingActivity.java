package com.fise.xw.ui.activity;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.SystemConfigEntity;
import com.fise.xw.DB.sp.LoginSp;
import com.fise.xw.DB.sp.SystemConfigSp;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.UrlConstant;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.event.SocketEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.helper.ProtoBuf2JavaBean;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.widget.IMBaseImageView;

import de.greenrobot.event.EventBus;

/**
 * 启动Activity
 * 
 */
public class LoadingActivity extends TTBaseActivity {
	private Button btn_login;
	private boolean autoLogin = true;
	private Handler uiHandler = new Handler();
	private Button mRegist;
	private IMService imService;
	private boolean loginSuccess = false;

	public final int GOLOGIN = 11111;
	public final int GORIGET = 11112;

	public final int POSTSYSTEM = 304;
	public final int ADVERTISEMENT = 0;
	public final int YINGDAORIGHT = 101;

	private String imei;
	private String advertisement;
	private String advertisementUrl;
	private String updateUrl;

	private String versionNew;
	private String versionApp;
	private int advTime;
	private boolean yindao; 
	private IMBaseImageView activity_bg;
	
	

	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GOLOGIN: {
				Intent intent = new Intent(LoadingActivity.this,
						LoginActivity.class);
				startActivity(intent);
				LoadingActivity.this.finish();
			}
				break;
			case GORIGET: {
				Intent intent = new Intent(LoadingActivity.this,
						LoginActivity.class);
				startActivity(intent);
				LoadingActivity.this.finish();
			}
				break;

			case POSTSYSTEM: {
				autoLogin();
			}
				break;

			case YINGDAORIGHT: {
				yindao = true;

				Context ctx = LoadingActivity.this;
				SharedPreferences sp = ctx.getSharedPreferences("CITY",
						MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.putBoolean("Yindao", true);
				editor.commit();

				Intent intent = new Intent(LoadingActivity.this,
						GuideActivity.class);
				LoadingActivity.this.startActivity(intent);
				LoadingActivity.this.finish();
			}
				break;

			case ADVERTISEMENT: {

				if (advertisement == null||advertisement.equals("")) {

					// 当前版本小于最低版本 强制更新
					if (getVersion().compareTo(versionApp) < 0) {
						dialog();
					} else {
						autoLogin();
					}
					
				} else { 
						
					//activity_bg.setBackgroundResource(R.drawable.loading);
					activity_bg.setImageUrl(advertisement);   
					activity_bg.setOnClickListener(new Button.OnClickListener() {// 创建监听
						public void onClick(View v) {

							Intent intent = new Intent();
							intent.setAction("android.intent.action.VIEW");
							Uri content_url = Uri.parse(advertisementUrl);
							intent.setData(content_url);
							startActivity(intent);

						}

					});

					uiHandler.postDelayed(new Runnable() {
						@Override
						public void run() {

							// 当前版本小于最低版本 强制更新
							if (getVersion().compareTo(versionApp) < 0) {
								dialog();
							} else {
								autoLogin();
							}
						}
					}, 1000 * advTime);
				}

			}
				break;

			}
			super.handleMessage(msg);
		}
	};

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onServiceDisconnected() {
		}

		@Override
		public void onIMServiceConnected() {
			logger.d("login#onIMServiceConnected");
			imService = imServiceConnector.getIMService();

			uiHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					postSystemConf(imei);
				}
			}, 1000 * 2);

		}
	};

	void autoLogin() {

		IMLoginManager loginManager = imService.getLoginManager();
		LoginSp loginSp = imService.getLoginSp();
		if (loginManager == null || loginSp == null) {
			// 无法获取登陆控制器
			GotRegisterIdentity();
			return;
		}

		LoginSp.SpLoginIdentity loginIdentity = loginSp.getLoginIdentity();
		if (loginIdentity == null) {
			// 之前没有保存任何登陆相关的，跳转到登陆页面
			GotRegisterIdentity();
			return;
		}
		if (TextUtils.isEmpty(loginIdentity.getPwd())) {
			// 密码为空，可能是loginOut

			GotLoginIdentity();
			return;
		}

		// 调整到小位界面
		handleGotLoginIdentity(loginIdentity);
	}

	/**
	 * 自动登录
	 */
	private void handleGotLoginIdentity(
			final LoginSp.SpLoginIdentity loginIdentity) {

		uiHandler.postDelayed(new Runnable() {
			@Override
			public void run() {

				if (imService == null || imService.getLoginManager() == null) {
					// Toast.makeText(LoginActivity.this,
					// getString(R.string.login_failed),
					// Toast.LENGTH_SHORT).show();
					// showLoginPage();
				}

				imService.getLoginManager().login(loginIdentity);
			}
		}, 500);
	}

	/**
	 * 跳转到登录界面
	 */
	private void GotLoginIdentity() {

		uiHandler.postDelayed(new Runnable() {
			@Override
			public void run() {

				Message message = new Message();
				message.what = GOLOGIN;
				LoadingActivity.this.myHandler.sendMessage(message);
			}
		}, 1000 * 2);
	}

	/**
	 * 跳转到登录注册界面
	 */
	private void GotRegisterIdentity() {

		uiHandler.postDelayed(new Runnable() {
			@Override
			public void run() {

				Message message = new Message();
				message.what = GORIGET;
				LoadingActivity.this.myHandler.sendMessage(message);
			}
		}, 1000 * 2);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading_activity);

		activity_bg = (IMBaseImageView) this.findViewById(R.id.bg);
		activity_bg.setDefaultImageRes(R.drawable.loading);
  
		
		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonyManager.getDeviceId();

		Context ctx = LoadingActivity.this;
		SharedPreferences sp = ctx.getSharedPreferences("CITY", MODE_PRIVATE);
		yindao = sp.getBoolean("Yindao", false);

		imServiceConnector.connect(LoadingActivity.this);
		EventBus.getDefault().register(LoadingActivity.this);

		if (yindao) {
			SystemConfigSp.instance().init(getApplicationContext());
			if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(
					SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
				SystemConfigSp.instance().setStrConfig(
						SystemConfigSp.SysCfgDimension.LOGINSERVER,
						UrlConstant.ACCESS_MSG_ADDRESS);
			}

		}

	}

	void postSystemConf(final String imei) {

		new Thread() {
			@Override
			public void run() {
				String url = UrlConstant.ACCESS_MSG_SYSTEM;
				// 第一步，创建HttpPost对象
				HttpPost request = new HttpPost(url);

				try {
					// 设置httpPost请求参数
					String md5Imei = new String(com.fise.xw.Security
							.getInstance().EncryptPass(
									imei + "fise_zn_xw@fise.com.cn"));

					// 先封装一个 JSON 对象
					JSONObject param = new JSONObject();

					param.put("app_dev", imei);
					param.put("app_key", md5Imei);
					param.put("client_type", "android");

					// 绑定到请求 Entry
					StringEntity se = new StringEntity(param.toString());
					request.setEntity(se);
					// 发送请求
					HttpResponse httpResponse = new DefaultHttpClient()
							.execute(request);
					// 得到应答的字符串，这也是一个 JSON 格式保存的数据

					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						// 第三步，使用getEntity方法活得返回结果
						String result = EntityUtils.toString(httpResponse
								.getEntity());

						JSONObject json = new JSONObject(result);

						String error_code = json.getString("error_code");

						if (error_code.equals("0")) {

							JSONObject launchJson = json
									.getJSONObject("launch");
							String launch = launchJson.getString("value");
							String launchAction = launchJson
									.getString("action");

							int launch_time = json.getJSONObject("launch_time")
									.getInt("value");
							String comment_url = json.getJSONObject(
									"comment_url").getString("value");
							String system_notice = json.getJSONObject(
									"system_notice").getString("value");
							String update_url = json
									.getJSONObject("update_url").getString(
											"value");

							String version_support = json.getJSONObject(
									"version_support").getString("value");

							String website = json.getJSONObject("website")
									.getString("value");
							String version = json.getJSONObject("version")
									.getString("value");
							
							
							String version_comment = json.getJSONObject("version_comment")
									.getString("value");

							SystemConfigEntity entityTemp = ProtoBuf2JavaBean
									.getSystemConfigEntity(launch, launch_time,
											launchAction, system_notice,
											update_url, website,
											version_support, comment_url,
											version,version_comment);

							imService.getContactManager().setsystemConfig(
									entityTemp);

							advertisement = launch;
							advertisementUrl = launchAction;
							updateUrl = update_url;
							versionNew = version;
							versionApp = version_support;
							advTime = launch_time;

							if (yindao) {

								Message message = new Message();
								message.what = ADVERTISEMENT;
								LoadingActivity.this.myHandler
										.sendMessage(message);
							} else {
								Message message = new Message();
								message.what = YINGDAORIGHT;
								LoadingActivity.this.myHandler
										.sendMessage(message);
							}

						} else {

							Message message = new Message();
							message.what = POSTSYSTEM;
							LoadingActivity.this.myHandler.sendMessage(message);
						}

					} else {

						Message message = new Message();
						message.what = POSTSYSTEM;
						LoadingActivity.this.myHandler.sendMessage(message);
					}
				} catch (ClientProtocolException e) { // 超时

					Message message = new Message();
					message.what = POSTSYSTEM;
					LoadingActivity.this.myHandler.sendMessage(message);

					e.printStackTrace();
				} catch (IOException e) {
					Message message = new Message();
					message.what = POSTSYSTEM;
					LoadingActivity.this.myHandler.sendMessage(message);

					e.printStackTrace();
				} catch (JSONException e) {
					Message message = new Message();
					message.what = POSTSYSTEM;
					LoadingActivity.this.myHandler.sendMessage(message);

					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onDestroy() {

		imServiceConnector.disconnect(LoadingActivity.this);
		EventBus.getDefault().unregister(LoadingActivity.this);

		super.onDestroy();
	}

	// 主动退出的时候， 这个地方会有值,更具pwd来判断
	private boolean shouldAutoLogin() {
		SharedPreferences read = getSharedPreferences(
				IntentConstant.KEY_LOGIN_NOT_AUTO, Activity.MODE_PRIVATE);
		return read.getBoolean("login_not_auto", false);
	}

	// 为什么会有两个这个
	// 可能是 兼容性的问题 导致两种方法onBackPressed
	@Override
	public void onBackPressed() {
		// imLoginMgr.cancel();
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	/**
	 * ----------------------------event 事件驱动----------------------------
	 */
	public void onEventMainThread(LoginEvent event) {
		switch (event) {
		case LOCAL_LOGIN_SUCCESS:
		case LOGIN_OK:
			// onLoginSuccess();
			// Intent intent = new Intent(LoadingActivity.this,
			// LoginProtectionActivity.class);
			// intent.putExtra(IntentConstant.KEY_REGIST_NAME,
			// imService.getLoginManager().getLoginInfo().getPhone());
			// startActivity(intent);
			// LoadingActivity.this.finish();

			onLoginSuccess();
			break;

		case LOGIN_AUTH_DEVICE: {

			IMLoginManager loginManager = imService.getLoginManager();
			LoginSp loginSp = imService.getLoginSp();
			if (loginManager == null || loginSp == null) {
				return;
			}

			Intent intent = new Intent(LoadingActivity.this,
					LoginProtectionActivity.class);
			intent.putExtra(IntentConstant.KEY_REGIST_NAME, loginSp
					.getLoginIdentity().getLoginName());
			intent.putExtra(IntentConstant.KEY_LOGIN_PASS, loginSp
					.getLoginIdentity().getPwd());
			intent.putExtra(IntentConstant.KEY_LOGIN_IMEI, loginSp
					.getLoginIdentity().getImei());

			startActivity(intent);
			LoadingActivity.this.finish();
			// imService.getLoginManager().login(loginIdentity);
		}
			break;

		case FORCE_FAILED:
			if (!loginSuccess)
				onLoginFailure(event);
			break;
		case LOGIN_AUTH_FAILED:
		case LOGIN_INNER_FAILED:
			if (!loginSuccess)
				onLoginFailure(event);
			break;
		}
	}

	public void onEventMainThread(SocketEvent event) {
		switch (event) {
		case CONNECT_MSG_SERVER_FAILED:
		case REQ_MSG_SERVER_ADDRS_FAILED:
			if (!loginSuccess)
				onSocketFailure(event);
			break;
		}
	}

	private void onLoginSuccess() {

		loginSuccess = true;

		Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
		startActivity(intent);
		LoadingActivity.this.finish();
	}

	protected void dialog() {

		// AlertDialog.Builder builder = new Builder(LoadingActivity.this);
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(LoadingActivity.this,
						android.R.style.Theme_Holo_Light_Dialog));

		builder.setMessage("当前版本为:" + getVersion() + " " + "系统最新版本为: "
				+ versionNew + "   如果不更新 小位APP 将不能使用");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// dialog.dismiss();

				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = Uri.parse(updateUrl);
				intent.setData(content_url);
				startActivity(intent);

			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				LoadingActivity.this.finish();
			}
		});
		builder.create().show();
	}

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

	private void openApplicationMarket(String url) { // packageName

		try {
			Intent localIntent = new Intent(Intent.ACTION_VIEW);
			localIntent.setData(Uri.parse(url));
			this.startActivity(localIntent);
		} catch (Exception e) {
			// 打开应用商店失败 可能是没有手机没有安装应用市场
			e.printStackTrace();
			Toast.makeText(LoadingActivity.this, "打开应用商店失败", Toast.LENGTH_SHORT)
					.show();
		}

	}

	private void onLoginFailure(LoginEvent event) {

	}

	private void onSocketFailure(SocketEvent event) {

		LoginSp loginSp = imService.getLoginSp();
		if (loginSp == null) {
			return;
		}
		LoginSp.SpLoginIdentity loginIdentity = loginSp.getLoginIdentity();
		if (loginIdentity == null) {
			// 之前没有保存任何登录相关的，跳转到登录页面
			return;
		}
		if (TextUtils.isEmpty(loginIdentity.getPwd())) {
			// 密码为空，可能是loginOut
			return;
		}

		Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
		startActivity(intent);
		LoadingActivity.this.finish();
	}

}

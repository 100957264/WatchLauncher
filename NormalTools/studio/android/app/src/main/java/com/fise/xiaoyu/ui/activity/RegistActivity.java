package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.sp.RegistSp;
import com.fise.xiaoyu.DB.sp.SystemConfigSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.UrlConstant;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.SocketEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.HttpUtil;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 注册最终输入密码界面
 */
@SuppressLint("NewApi")
public class RegistActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(RegistActivity.class);
	private Handler uiHandler = new Handler();

	private EditText mConfirmPassword;
	private EditText mPasswordView;
	private View loginPage;
	private InputMethodManager intputManager;
	// private TextView show_name;
	private Button sign_in_button;

	public String phoneName;
	private IMService imService;
	private boolean autoLogin = true;
	private boolean loginSuccess = false;

	public String sms;
	public static String ShowText = null;

	private int inputNum;

	private String loginName;
	private int user_id;
	private int user_reg;
	private String err_code;
	private String mLoginName;
	private String mPass;
	private String imei;
	private int type;
	private ImageView loginLoading;
	private AnimationDrawable loginLoadingAni;
	Handler againSmsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: {
				if (type == 0) {
					imService.getLoginManager().login(mLoginName, mPass, imei,DBConstant.ANDROIDTYPE);
				} else {
					//RegistActivity.this.finish();
					imService.getLoginManager().login(mLoginName, mPass, imei,DBConstant.ANDROIDTYPE);
				}

			}
				break;
			default: {
				Utils.showToast(RegistActivity.this, "" + err_code);
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
			try {
				do {
					if (imService == null) {
						// 后台服务启动链接失败
						break;
					}

					if (user_reg == 1)
						return;

					IMLoginManager loginManager = imService.getLoginManager();
					RegistSp registSp = imService.getRegistSp();
					if (loginManager == null || registSp == null) {
						// 无法获取登陆控制器
						break;
					}

					RegistSp.SpRegistIdentity registIdentity = registSp
							.getRegistIdentity();
					if (registIdentity == null) {
						// 之前没有保存任何登陆相关的，跳转到登陆页面
						break;
					}

					// mNameView.setText(registIdentity.getRegistName());
					if (TextUtils.isEmpty(registIdentity.getPwd())) {
						// 密码为空，可能是loginOut
						break;
					}
					mPasswordView.setText(registIdentity.getPwd());

					if (autoLogin == false) {
						break;
					}

					handleGotLoginIdentity(registIdentity);
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

	/**
	 * 自动登陆
	 */
	private void handleGotLoginIdentity(
			final RegistSp.SpRegistIdentity registIdentity) {
		logger.i("login#handleGotLoginIdentity");

		uiHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				logger.d("login#start auto login");
				if (imService == null || imService.getLoginManager() == null) {
					Utils.showToast(RegistActivity.this,
							getString(R.string.login_failed));
					showLoginPage();
				}

				imService.getLoginManager().Register(registIdentity);
			}
		}, 500);
	}

	private void showLoginPage() {
		loginPage.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		intputManager = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
		logger.d("login#onCreate");

		setContentView(R.layout.tt_activity_regist);
		loginName = RegistActivity.this.getIntent().getStringExtra(
				IntentConstant.KEY_REGIST_NAME);

		type = RegistActivity.this.getIntent().getIntExtra(
				IntentConstant.KEY_REGIST_TYPE, 0);

		if (type == DBConstant.REGIST_TYPE_BLACKSMS) {
			TextView title = (TextView) findViewById(R.id.title);
			title.setText("找回密码");

//			RelativeLayout registration_layout = (RelativeLayout) findViewById(R.id.registration_layout);
//			registration_layout.setVisibility(View.GONE);
		}

		user_id = RegistActivity.this.getIntent().getIntExtra(
				IntentConstant.KEY_PASS_SMS, 0);

		user_reg = RegistActivity.this.getIntent().getIntExtra(
				IntentConstant.KEY_PASS_IS_REG, 0);

		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		try{
			imei = telephonyManager.getDeviceId();
		}catch (Exception e){
			imei = "";
		}

		SystemConfigSp.instance().init(getApplicationContext());
		if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(
				SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
			SystemConfigSp.instance().setStrConfig(
					SystemConfigSp.SysCfgDimension.LOGINSERVER,
					UrlConstant.ACCESS_MSG_ADDRESS);
		}

		imServiceConnector.connect(RegistActivity.this);

		inputNum = 0;

		initAutoLogin();

		SharedPreferences read = getSharedPreferences(
				IntentConstant.KEY_REGIST_NAME, Activity.MODE_PRIVATE);
		phoneName = read.getString("regist_name", "");

		// 短信验证缓存
		SharedPreferences smsRead = getSharedPreferences(
				IntentConstant.KEY_REGIST_NAME, Activity.MODE_PRIVATE);
		sms = smsRead.getString("sms_name", "");

		// String text = "你的登录账户为:  " + phoneName + "  请设置密码";
		// show_name = (TextView) findViewById(R.id.show_name);
		// show_name.setText(text);

		mConfirmPassword = (EditText) findViewById(R.id.confirmPassword);
		mPasswordView = (EditText) findViewById(R.id.pass);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {

						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							if (inputNum > 0) {
								attemptLogin();
								return true;
							}
						}
						return false;
					}
				});
		loginLoading = (ImageView) findViewById(R.id.login_loading_img);
		loginLoadingAni = (AnimationDrawable)loginLoading.getBackground();
		sign_in_button = (Button) findViewById(R.id.sign_in_button);
		sign_in_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (inputNum > 0) {
					intputManager.hideSoftInputFromWindow(
							mPasswordView.getWindowToken(), 0);
					attemptLogin();
				}

			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RegistActivity.this.finish();
			}
		});

		if (inputNum > 0) {
			sign_in_button.setBackgroundResource(R.drawable.button_normal);
		} else {
			sign_in_button.setBackgroundResource(R.drawable.button_disabled);
		}

		mPasswordView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				inputNum = s.length();
				if (inputNum > 0) {
					sign_in_button
							.setBackgroundResource(R.drawable.button_normal);
				} else {
					sign_in_button
							.setBackgroundResource(R.drawable.button_disabled);
				}
			}
		});

	}

	private void initAutoLogin() {
		logger.i("login#initAutoLogin");

		loginPage = findViewById(R.id.login_page);
		autoLogin = shouldAutoLogin();

		loginPage.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (mPasswordView != null) {
					intputManager.hideSoftInputFromWindow(
							mPasswordView.getWindowToken(), 0);
				}

				return false;
			}
		});

	}

	// 主动退出的时候， 这个地方会有值,更具pwd来判断
	private boolean shouldAutoLogin() {
		SharedPreferences read = getSharedPreferences(
				IntentConstant.KEY_LOGIN_NOT_AUTO, Activity.MODE_PRIVATE);
		return read.getBoolean("login_not_auto", false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(RegistActivity.this);
				loginPage = null;
	}

	public boolean vd(String str) {

		char[] chars = str.toCharArray();
		boolean isGB2312 = false;
		for (int i = 0; i < chars.length; i++) {
			byte[] bytes = ("" + chars[i]).getBytes();
			if (bytes.length == 2) {
				int[] ints = new int[2];
				ints[0] = bytes[0] & 0xff;
				ints[1] = bytes[1] & 0xff;

				if (ints[0] >= 0x81 && ints[0] <= 0xFE && ints[1] >= 0x40
						&& ints[1] <= 0xFE) {
					isGB2312 = true;
					break;
				}
			}
		}
		return isGB2312;
	}

	 public static boolean ispsd(String psd) {
	        Pattern p = Pattern
	                .compile("^[a-zA-Z].*[0-9]|.*[0-9].*[a-zA-Z]");
	        Matcher m = p.matcher(psd);

	        return m.matches();
	  }
	 
	 
		/**
		 * * 纯字母
		 * 
		 * @param data
		 * @return
		 */
		public  boolean isChar(String data) {
			for (int i = data.length(); --i >= 0;) {
				char c = data.charAt(i);
				if (((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
					return true;
				} else {
					return false;
				}
			}
			return true;
		}
		
		
		
	public void attemptLogin() {

		if (!(mPasswordView.getText().toString().equals(mConfirmPassword
				.getText().toString()))) {
			Utils.showToast(this, "密码不一样 请重新输入");
			return;
		} 
		 
		
		Pattern pattern = Pattern.compile("[0-9]{1,}");
		Matcher matcher = pattern.matcher((CharSequence)(mPasswordView.getText().toString()));
		boolean result=matcher.matches();
		if (result) {
			Utils.showToast(this, "密码不能是纯数字");
			return;
		}
		
		
		boolean resultHan = vd(mPasswordView.getText().toString());
		if (resultHan) {
			Utils.showToast(this, "密码不能是汉字");
			return;
		}
		  
		if( mPasswordView.getText().toString().indexOf(" ")>=0 ){
			//含有空格 
			Utils.showToast(this, "密码不能有空格");
			return;
		} 
		 
		
		if( mPasswordView.getText().toString().length()<DBConstant.PASS_LENGTH ){  
			//含有空格 
			Utils.showToast(this, "密码不能少于6位");
			return;
		} 
		
		
		
		//boolean aaa = ispsd(mPasswordView.getText().toString());
		
		boolean resultZi= ispsd(mPasswordView.getText().toString());//isChar(mPasswordView.getText().toString());
		if (resultZi == false) {
			Utils.showToast(this, "密码不能是纯字母");
			return;
		}
		  
		
		
		
		String loginName = phoneName;// read.getString("regist_name",
										// "");//mNameView.getText().toString();
		String mPassword = mPasswordView.getText().toString();
		boolean cancel = false;
		View focusView = null;

		if (TextUtils.isEmpty(mPassword)) {
			Utils.showToast(this, getString(R.string.error_pwd_required));
			focusView = mPasswordView;
			cancel = true;
		}

		if (TextUtils.isEmpty(loginName)) {
			Utils.showToast(this, getString(R.string.error_name_required));
			cancel = true;
		}

		if (cancel) {
			focusView.requestFocus();
		} else {
			showProgress(true);
			if (imService != null) {
				// boolean userNameChanged = true;
				// boolean pwdChanged = true;
				loginName = loginName.trim();
				mPassword = mPassword.trim();

				SharedPreferences ww = getSharedPreferences(
						IntentConstant.KEY_LOGIN_NOT_AUTO,
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = ww.edit();
				editor.putBoolean("login_not_auto", true);
				editor.commit();

				if(user_reg  == 0){
					imService.getLoginManager().Regist(loginName,  mPassword, imei);
				} else if(user_reg  == 1)
				{
					
					mLoginName = loginName;
					mPass = mPassword; 
					String desPwd = new String(Security.getInstance().EncryptPass(mPass));
					postHttp(desPwd);
				}
				

			}
		}
	}

	private void showProgress(final boolean show) {
		if (show) {
			loginLoading.setVisibility(View.VISIBLE);
			loginLoadingAni.start();
		} else {
			loginLoading.setVisibility(View.GONE);
			if(loginLoadingAni.isRunning()){
				loginLoadingAni.stop();
			}
		}
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		// {
		// LoginActivity.this.finish();
		// return true;
		// }
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * ----------------------------event 事件驱动----------------------------
	 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(LoginEvent event) {
		switch (event) {
		case LOCAL_LOGIN_SUCCESS:

		case REGIST_OK:
		case LOGIN_OK:
			onLoginSuccess();
			break;

		case LOGIN_AUTH_DEVICE:

			Intent intent = new Intent(RegistActivity.this,
					LoginProtectionActivity.class);
			intent.putExtra(IntentConstant.KEY_REGIST_NAME, mLoginName);
			intent.putExtra(IntentConstant.KEY_LOGIN_PASS, mPass);
			intent.putExtra(IntentConstant.KEY_LOGIN_IMEI, imei);

			startActivity(intent);
			RegistActivity.this.finish();

			break;

		case FORCE_FAILED:
			if (!loginSuccess)
				onRegistFailure(event);
			break;

		case REGIST_AUTH_FAILED:
		case REGIST_INNER_FAILED:
		case LOGIN_AUTH_FAILED:
		case LOGIN_INNER_FAILED:

			if (!loginSuccess)
				onRegistFailure(event);
			break;
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(SocketEvent event) {
		switch (event) {
		case CONNECT_MSG_SERVER_FAILED:
		case REQ_MSG_SERVER_ADDRS_FAILED:
			if (!loginSuccess)
				onSocketFailure(event);
			break;
		}
	}

	private void onLoginSuccess() {
		logger.i("login#onLoginSuccess");
		loginSuccess = true;

		Intent intent = new Intent(RegistActivity.this, MainActivity.class);
		startActivity(intent);
		RegistActivity.this.finish();
	}

	private void onRegistFailure(LoginEvent event) {
		logger.e("login#onLoginError -> errorCode:%s", event.name());
		showLoginPage();
		// String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
		String errorTip = IMLoginManager.instance().getError();
		logger.d("login#errorTip:%s", errorTip);
		loginLoading.setVisibility(View.GONE);

		if (ShowText != null) {
			Utils.showToast(this, ShowText);
		} else {
			Utils.showToast(this, errorTip);
		}

	}

	private void onSocketFailure(SocketEvent event) {
		logger.e("login#onLoginError -> errorCode:%s,", event.name());
		showLoginPage();
		String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
		logger.d("login#errorTip:%s", errorTip);
		loginLoading.setVisibility(View.GONE);
		Utils.showToast(this, errorTip);
	}

	void postHttp(final String pass) {

		new Thread() {
			@Override
			public void run() {

				String url = UrlConstant.ACCESS_MSG_REST_PASSWORD;
				OkHttpClient okHttpClient = new OkHttpClient();
				try {
					String md5Imei = new String(Security.getInstance().EncryptPass(imei + "fise_zn_xw@fise.com.cn"));
					JSONObject param = new JSONObject();
					param.put("user_id", user_id);
					param.put("password", pass);
					param.put("app_dev", imei);
					param.put("app_key", md5Imei);
					RequestBody requestBody = RequestBody.create(HttpUtil.JSON, param.toString());
					Request request = new Request.Builder()
							.url(url)
							.post(requestBody)
							.build();
					Response response = okHttpClient.newCall(request).execute();

					if (response.isSuccessful()) {
						String result = response.body().string();
						JSONObject json = new JSONObject(result);
						String code = json.getString("error_code");
						err_code = json.getString("error_msg");

						Message message = new Message();
						message.what = Integer.valueOf(code);
						RegistActivity.this.againSmsHandler
								.sendMessage(message);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();

	}
}

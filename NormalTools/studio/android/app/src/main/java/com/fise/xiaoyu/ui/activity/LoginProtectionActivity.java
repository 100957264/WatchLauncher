package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import com.fise.xiaoyu.protobuf.IMSms.SmsActionType;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.FilletDialog;
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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 登录保护界面
 */
@SuppressLint("NewApi")
public class LoginProtectionActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(LoginProtectionActivity.class);
	public Button submit;
	private EditText mCodeword;
	private int inputNum;
	private TextView name;
	public String phoneName;
	public String loginPass;
	public String imei;
	
	public String err_code;
	public String error_msg;

	private TimeCount time;
	private TextView retransmission;
	private IMService imService;
	private boolean loginSuccess = false;

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

	Handler smsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: { 

	               imService.getLoginManager().login(phoneName, loginPass,imei, DBConstant.ANDROIDTYPE);
			}
				break;
			case 1: {
				Utils.showToast(LoginProtectionActivity.this, "输入验证码不对");
			}
				break;
			default: {
				Utils.showToast(LoginProtectionActivity.this, "" + err_code);
			}
				break;
			}
			super.handleMessage(msg);
		}
	};

	Handler againSmsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: {

			}
				break;
			case 1: {
				Utils.showToast(LoginProtectionActivity.this, "" + error_msg);
			}
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		inputNum = 0;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_sms_verify);
		mCodeword = (EditText) findViewById(R.id.password);

		phoneName = LoginProtectionActivity.this.getIntent().getStringExtra(
				IntentConstant.KEY_REGIST_NAME);

		loginPass= LoginProtectionActivity.this.getIntent().getStringExtra(
				IntentConstant.KEY_LOGIN_PASS);
		
		imei = LoginProtectionActivity.this.getIntent().getStringExtra(
				IntentConstant.KEY_LOGIN_IMEI);
		
		imServiceConnector.connect(LoginProtectionActivity.this);

 
		 
		name = (TextView) findViewById(R.id.name);
		name.setText(phoneName);

		time = new TimeCount(60000, 1000);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText("短信验证");

		submit = (Button) findViewById(R.id.submit_in_button);
        findViewById(R.id.registration_layout).setVisibility(View.GONE);
//		submit.setText("登录");

		time.start();
		new Thread() {
			@Override
			public void run() {
				postSendSmS(phoneName,
						SmsActionType.SMS_ACTION_AUTH_DEVICE);
			}
		}.start();
		
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mCodeword.getText().toString() != null) {

					postHttp(phoneName, mCodeword.getText().toString(),
							imService.getLoginManager().getLoginUserImei(),
							imService.getLoginManager().getLoginId());

				} else {
					Utils.showToast(LoginProtectionActivity.this, "请输入验证码");
				}
			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				LoginProtectionActivity.this.finish();
			}
		});

		retransmission = (TextView) findViewById(R.id.switch_submit_text);

		retransmission.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				final FilletDialog myDialog = new FilletDialog(LoginProtectionActivity.this ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE);
				myDialog.setTitle(LoginProtectionActivity.this.getString(R.string.receiver_auth_code_again));//设置内容
				myDialog.dialog.show();//显示

				//确认按键回调，按下确认后在此做处理
				myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
					@Override
					public void ok() {
                        time.start();
						new Thread() {
							@Override
							public void run() {
								postSendSmS(phoneName,
										SmsActionType.SMS_ACTION_AUTH_DEVICE);
							}
						}.start();
						myDialog.dialog.dismiss();;

					}
				});

			}
		});

		if ((inputNum > 0) && (true)) {
			submit.setBackgroundResource(R.drawable.button_normal);
			submit.setClickable(true);
		} else {
			submit.setBackgroundResource(R.drawable.button_disabled);
			submit.setClickable(false);
		}

		mCodeword.addTextChangedListener(new TextWatcher() {
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
					submit.setBackgroundResource(R.drawable.button_normal);
					submit.setClickable(true);
				} else {
					submit.setBackgroundResource(R.drawable.button_disabled);
					submit.setClickable(false);
				}
			}
		});
			
	}	

	void postSendSmS(String phone, SmsActionType type) {
		String url = UrlConstant.ACCESS_MSG_SEND_SMS;
		OkHttpClient okHttpClient = new OkHttpClient();
		try {
			String md5Imei = new String(Security.getInstance().EncryptPass(imei + "fise_zn_xw@fise.com.cn"));
			JSONObject param = new JSONObject();
			param.put("action", type.ordinal());
			param.put("mobile", phone);
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
				error_msg = json.getString("error_msg");

				Message message = new Message();
				message.what = Integer.valueOf(code);
				LoginProtectionActivity.this.againSmsHandler
						.sendMessage(message);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	void postHttp(final String phone, final String auth_code,
			final String imei, final int user_id) {

		new Thread() {
			@Override
			public void run() {

				String url = UrlConstant.ACCESS_MSG_ACTION_VERIFY;
				OkHttpClient okHttpClient = new OkHttpClient();
				try {
					String md5Imei = new String(Security.getInstance().EncryptPass(imei + "fise_zn_xw@fise.com.cn"));
					JSONObject param = new JSONObject();
					param.put("mobile", phone);
					param.put("action",
							SmsActionType.SMS_ACTION_AUTH_DEVICE.ordinal());
					param.put("auth_code", auth_code);
					param.put("user_id", user_id);
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
						LoginProtectionActivity.this.smsHandler
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

	@Override
	protected void onDestroy() {
		imServiceConnector.disconnect(LoginProtectionActivity.this);

		super.onDestroy();

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

	class TimeCount extends CountDownTimer {

		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// btnGetcode.setBackgroundColor(Color.parseColor("#B6B6D8"));
			retransmission.setClickable(false);
			retransmission.setText(millisUntilFinished / 1000 + "秒");
		}

		@Override
		public void onFinish() {
			retransmission.setText("重发");
			retransmission.setClickable(true);
			// btnGetcode.setBackgroundColor(Color.parseColor("#4EB84A"));

		}
	}

	/**
	 * ----------------------------event 事件驱动----------------------------
	 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(LoginEvent event) {
		switch (event) {
		case LOCAL_LOGIN_SUCCESS:
		case LOGIN_OK:
			 onLoginSuccess();
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
 
		
		Intent intent = new Intent(LoginProtectionActivity.this,
				MainActivity.class);
		startActivity(intent);
		LoginProtectionActivity.this.finish();
	}
 
	private void onLoginFailure(LoginEvent event) {
		//String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
		 String errorTip = IMLoginManager.instance().getError();
		Utils.showToast(this, errorTip);
	}

	private void onSocketFailure(SocketEvent event) {
		String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
		Utils.showToast(this, errorTip);
	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//			Intent intent = new Intent(LoginProtectionActivity.this,
//					QiDongActivity.class);
//			startActivity(intent);
//			LoginProtectionActivity.this.finish();
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}

}

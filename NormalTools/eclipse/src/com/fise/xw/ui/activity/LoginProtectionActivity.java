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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R; 
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.UrlConstant;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.event.SocketEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMSms.SmsActionType;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;

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
	private Button retransmission;
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
  
	               imService.getLoginManager().login(phoneName, loginPass,imei);
			}
				break;
			case 1: {
				Toast.makeText(LoginProtectionActivity.this, "输入验证码不对",
						Toast.LENGTH_SHORT).show();
			}
				break;
			default: {
				Toast.makeText(LoginProtectionActivity.this, "" + err_code,
						Toast.LENGTH_SHORT).show();
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
				Toast.makeText(LoginProtectionActivity.this, "" + error_msg,
						Toast.LENGTH_SHORT).show();
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
		EventBus.getDefault().register(this);

 
		 
		name = (TextView) findViewById(R.id.name);
		name.setText(phoneName);

		time = new TimeCount(60000, 1000);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText("登录保护验证");

		submit = (Button) findViewById(R.id.submit_in_button);
		submit.setText("登录");

		
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
					Toast.makeText(LoginProtectionActivity.this, "请输入验证码",
							Toast.LENGTH_SHORT).show();
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

		retransmission = (Button) findViewById(R.id.retransmission);
		retransmission.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				time.start();

				new Thread() {
					@Override
					public void run() {
						postSendSmS(phoneName,
								SmsActionType.SMS_ACTION_AUTH_DEVICE);
					}
				}.start();

			}
		});

		if ((inputNum > 0) && (true)) {
			submit.setBackgroundResource(R.drawable.button_normal);
		} else {
			submit.setBackgroundResource(R.drawable.button_disabled);
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
				} else {
					submit.setBackgroundResource(R.drawable.button_disabled);
				}
			}
		});
			
	}	

	void postSendSmS(String phone, SmsActionType type) {

		String url = UrlConstant.ACCESS_MSG_SEND_SMS;
		// 第一步，创建HttpPost对象
		HttpPost request = new HttpPost(url);

		try {
			// 设置httpPost请求参数

			 String  md5Imei = new String(com.fise.xw.Security.getInstance().EncryptPass( imei + "fise_zn_xw@fise.com.cn"));
			// 先封装一个 JSON 对象
			JSONObject param = new JSONObject();
			param.put("action", type.ordinal());
			param.put("mobile", phone);
			param.put("app_dev", imei);
			param.put("app_key", md5Imei);
			
			// 绑定到请求 Entry
			StringEntity se = new StringEntity(param.toString());
			request.setEntity(se);
			// 发送请求
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(request);
			// 得到应答的字符串，这也是一个 JSON 格式保存的数据

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// 第三步，使用getEntity方法活得返回结果
				String result = EntityUtils.toString(httpResponse.getEntity());

				JSONObject json = new JSONObject(result);
				String code = json.getString("error_code");
				error_msg = json.getString("error_msg");

				Message message = new Message();
				message.what = Integer.valueOf(code);
				LoginProtectionActivity.this.againSmsHandler
						.sendMessage(message);

			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void postHttp(final String phone, final String auth_code,
			final String imei, final int user_id) {

		new Thread() {
			@Override
			public void run() {

				String url = UrlConstant.ACCESS_MSG_ACTION_VERIFY;
				// 第一步，创建HttpPost对象
				HttpPost request = new HttpPost(url);

				try {
					// 设置httpPost请求参数


					 String  md5Imei = new String(com.fise.xw.Security.getInstance().EncryptPass(imei + "fise_zn_xw@fise.com.cn"));
					// 先封装一个 JSON 对象
					JSONObject param = new JSONObject();
					param.put("mobile", phone);
					param.put("action",
							SmsActionType.SMS_ACTION_AUTH_DEVICE.ordinal());
					param.put("auth_code", auth_code); 
					param.put("user_id", user_id);
					param.put("app_dev", imei);
					param.put("app_key", md5Imei);
					

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
						String code = json.getString("error_code");
						err_code = json.getString("error_msg");

						Message message = new Message();
						message.what = Integer.valueOf(code);
						LoginProtectionActivity.this.smsHandler
								.sendMessage(message);

					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}.start();

	}

	@Override
	protected void onDestroy() {
		imServiceConnector.disconnect(LoginProtectionActivity.this);
		EventBus.getDefault().unregister(this);

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
	public void onEventMainThread(LoginEvent event) {
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
		Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
	}

	private void onSocketFailure(SocketEvent event) {
		String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
		Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
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

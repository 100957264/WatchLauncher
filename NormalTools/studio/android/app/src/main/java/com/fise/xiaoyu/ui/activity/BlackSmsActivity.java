package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.UrlConstant;
import com.fise.xiaoyu.protobuf.IMSms.SmsActionType;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.HttpUtil;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 找回密码 短信验证界面
 */
@SuppressLint("NewApi")
public class BlackSmsActivity extends TTBaseActivity {
   
	private Logger logger = Logger.getLogger(BlackSmsActivity.class);
	public Button submit;
	private EditText mCodeword;
	private int inputNum;
	private TextView name;
	public String phoneName;
	public String err_code;
	public String error_msg;
	
	private TimeCount time;
	private TextView retransmission;
	private int user_id;
	public String imei;
	
	
	Handler checkHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: {
				Utils.showToast(BlackSmsActivity.this, "该帐户不存在" );
			}
				break;
			default: {
				Utils.showToast(BlackSmsActivity.this, "该帐户已经被注册了");
			}
				break;
			}
			super.handleMessage(msg);
		}

	};
	
	Handler smsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: {
				Intent intent = new Intent();
				intent.setClass(BlackSmsActivity.this, RegistActivity.class);
				intent.putExtra(IntentConstant.KEY_REGIST_NAME,  phoneName);
				intent.putExtra(IntentConstant.KEY_PASS_SMS,  user_id);
				intent.putExtra(IntentConstant.KEY_PASS_IS_REG,  1);
				intent.putExtra(IntentConstant.KEY_REGIST_TYPE, DBConstant.REGIST_TYPE_BLACKSMS);
				
				BlackSmsActivity.this.finish();  //guanweile
				
				BlackSmsActivity.this.startActivity(intent);
			}
				break;
			case 1: {
				Utils.showToast(BlackSmsActivity.this, "输入验证码不对");
			}
				break;
			default: {
				Utils.showToast(BlackSmsActivity.this, "" + err_code);
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
				Utils.showToast(BlackSmsActivity.this, "" + error_msg);
			}
				break;
			default: {
				Utils.showToast(BlackSmsActivity.this, "" + error_msg);
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
 
		phoneName = BlackSmsActivity.this.getIntent().getStringExtra(
				IntentConstant.KEY_REGIST_NAME);
		
		user_id = BlackSmsActivity.this.getIntent().getIntExtra(
				IntentConstant.KEY_PASS_SMS,0);
 
		TelephonyManager telephonyManager=(TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		try{
			imei =telephonyManager.getDeviceId();
		}catch (Exception e){
			imei = "";
		}

		
		name = (TextView) findViewById(R.id.name);
		name.setText(phoneName);

		 time = new TimeCount(60000, 1000); 
		 time.start(); 
		 
		submit = (Button) findViewById(R.id.submit_in_button);
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mCodeword.getText().toString() != null) {

					postHttp(phoneName, mCodeword.getText().toString());

				} else {
					Utils.showToast(BlackSmsActivity.this, "请输入验证码");
				}
			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				BlackSmsActivity.this.finish();
			}
		});
		
		
		
		retransmission = (TextView) findViewById(R.id.switch_submit_text);

		retransmission.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				AlertDialog.Builder builder = new AlertDialog.Builder(
						new ContextThemeWrapper(BlackSmsActivity.this,
								android.R.style.Theme_Holo_Light_Dialog));

				String[] items = new String[] { BlackSmsActivity.this
						.getString(R.string.receiver_data) };

				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								// guanweile
							{
								time.start();

								new Thread() {
									@Override
									public void run() {
										postSendSmS(phoneName,SmsActionType.SMS_ACTION_FORGET_PASSWORD);
									}
								}.start();
							}
							break;
						}
					}
				});
				AlertDialog alertDialog = builder.create();
				alertDialog.setCanceledOnTouchOutside(true);
				alertDialog.show();

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
	
	
	
	void postHttpCheck(final String phone, final SmsActionType type) {

		new Thread() {
			@Override
			public void run() {

				String url = UrlConstant.ACCESS_MSG_USER_INFO;
				OkHttpClient okHttpClient = new OkHttpClient();
				try {
					String md5Imei = new String(Security.getInstance().EncryptPass(imei + "fise_zn_xw@fise.com.cn"));
					JSONObject param = new JSONObject();
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
						int user_id = json.getInt("user_id");
						if (user_id == 0) {
							Message message = new Message();
							message.what = user_id;
							BlackSmsActivity.this.checkHandler.sendMessage(message);
						} else {
							postSendSmS(phone, type);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}.start();

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
				BlackSmsActivity.this.againSmsHandler.sendMessage(message);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	void postHttp(final String phone, final String auth_code) {

		new Thread() {
			@Override
			public void run() {

				String url = UrlConstant.ACCESS_MSG_ACTION_VERIFY;
				OkHttpClient okHttpClient = new OkHttpClient();
				try {
					String md5Imei = new String(Security.getInstance().EncryptPass(imei + "fise_zn_xw@fise.com.cn"));
					JSONObject param = new JSONObject();
					param.put("mobile", phone);
					param.put("action", SmsActionType.SMS_ACTION_FORGET_PASSWORD.ordinal());
					param.put("auth_code", auth_code);
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
						BlackSmsActivity.this.smsHandler
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
	        	retransmission.setText("接收短信大约需要"+ millisUntilFinished / 1000 +"秒");
	        }  
	    
	        @Override  
	        public void onFinish() {  
	        	retransmission.setText("收不到短信验证码？");
	        	retransmission.setClickable(true);  
	           // btnGetcode.setBackgroundColor(Color.parseColor("#4EB84A"));  
	    
	        }  
	    }  

}

package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fise.xiaoyu.DB.sp.SystemConfigSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.UrlConstant;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMSms.SmsActionType;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.HttpUtil;
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
 * 注册输入帐户界面
 * 
 */
@SuppressLint("NewApi")
public class RegistActivityName extends TTBaseActivity {

	private Logger logger = Logger.getLogger(RegistActivityName.class);
	public Button next;
	private EditText mPhone;
	private int inputNum;
	private AlertDialog myDialog = null;
	private IMService imService;
	public String error_msg;
	public String imei;
	public final int data = 303;
	
	public  final int OUTTIME = 101;
	Handler checkHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: {

				Utils.showToast(RegistActivityName.this, "该帐户已被注册过");
			}
				break;
			case 10: {
				
			}
				break;
			case 1: {
				Utils.showToast(RegistActivityName.this, "" + error_msg);
			}
				break;
			case data: {
				Utils.showToast(RegistActivityName.this, "该帐户已被注册过");
			}
				break;
				case OUTTIME: {
					Utils.showToast(RegistActivityName.this, "网络连接失败");
				}
				break;
			default: {
				Utils.showToast(RegistActivityName.this, "" + error_msg);
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
				intent.setClass(RegistActivityName.this,
						RegistActivityVerify.class);
				intent.putExtra(IntentConstant.KEY_REGIST_NAME, mPhone
						.getText().toString());
				RegistActivityName.this.startActivity(intent);
			}
				break;
			case 1: {
				Utils.showToast(RegistActivityName.this, "" + error_msg);
			}
				break;
			default: {
				Utils.showToast(RegistActivityName.this, "" + error_msg);
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

			if (imService == null) {
				return;
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		inputNum = 0;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_regist_name);
		mPhone = (EditText) findViewById(R.id.phoneName);
		mPhone.setInputType(InputType.TYPE_CLASS_NUMBER);

		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(TELEPHONY_SERVICE);
		try{
			imei = tm.getDeviceId();
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

		imServiceConnector.connect(RegistActivityName.this);

		next = (Button) findViewById(R.id.next_in_button);
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if ((inputNum > 0) && (true)) {
					if (mPhone.getText().toString() != null) {

						if (Utils.isMobileNO(mPhone.getText().toString().trim())) {
							SharedPreferences ww = getSharedPreferences(
									IntentConstant.KEY_REGIST_NAME,
									Activity.MODE_PRIVATE);
							SharedPreferences.Editor editor = ww.edit();
							editor.putString("regist_name", mPhone.getText()
									.toString());
							editor.commit();

							myDialog = new AlertDialog.Builder(
									RegistActivityName.this).create();
							myDialog.show();
							myDialog.getWindow().setContentView(
									R.layout.message_tell);
							myDialog.setCanceledOnTouchOutside(false);

							((TextView) (myDialog.getWindow()
									.findViewById(R.id.send_phone)))
									.setText("我们将发送验证码短信到这个号码: "
											+ mPhone.getText().toString());
							myDialog.getWindow()
									.findViewById(R.id.button_cancel)
									.setOnClickListener(
											new View.OnClickListener() {
												@Override
												public void onClick(View v) {
													myDialog.dismiss();
												}
											});

							myDialog.getWindow()
									.findViewById(R.id.button_que)
									.setOnClickListener(
											new View.OnClickListener() {
												@Override
												public void onClick(View v) {

													myDialog.dismiss();
													postHttp(
															mPhone.getText()
																	.toString(),
															SmsActionType.SMS_ACTION_REGIST);
													// imService.getLoginManager().SendSms(mPhone.getText().toString());
												}
											});
						} else {

							Utils.showToast(RegistActivityName.this, "输入手机号码有误");
						}


					} else {
						Utils.showToast(RegistActivityName.this, "请输入小雨号");
					}
				}
			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				RegistActivityName.this.finish();
			}
		});

		if ((inputNum > 0) && (true)) {
			next.setBackgroundResource(R.drawable.button_normal);
		} else {
			next.setBackgroundResource(R.drawable.button_disabled);
		}

		mPhone.addTextChangedListener(new TextWatcher() {
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
					next.setBackgroundResource(R.drawable.button_normal);
				} else {
					next.setBackgroundResource(R.drawable.button_disabled);
				}
			}
		});

		TextView agreement_text = (TextView) findViewById(R.id.agreement_text);
		agreement_text.setText("<<小雨软件许可及服务协议>>");
		agreement_text.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线
		agreement_text.getPaint().setAntiAlias(true);// 抗锯齿

		agreement_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Intent intentUrl = new Intent(RegistActivityName.this,
						WebViewActivity.class);
				intentUrl.putExtra(IntentConstant.WEB_URL,
						"file:///android_asset/xw_describe.html");
				intentUrl.putExtra(IntentConstant.WEB_URL_RETURN, "返回");
				startActivity(intentUrl);

			}
		});

	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(LoginEvent event) {
		switch (event) {
		case REGIST_SMS_FAILED: {
			Utils.showToast(RegistActivityName.this, "注册失败");
		}
			break;
		case REGIST_SMS_SUCCESS: {
			Intent intent = new Intent();
			intent.setClass(RegistActivityName.this, RegistActivityVerify.class);
			RegistActivityName.this.startActivity(intent);
		}
			break;
		}
	}

	void postHttp(final String phone, final SmsActionType type) {

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

						int error_code = Integer.parseInt(code);
						if (error_code == 0) {

							Message message = new Message();
							message.what = data;
							RegistActivityName.this.checkHandler
									.sendMessage(message);

							// int user_id = json.getInt("user_id");
							// if (user_id == 0) {
							//
							// } else {
							// Message message = new Message();
							// message.what = user_id;
							// RegistActivityName.this.checkHandler
							// .sendMessage(message);
							// }

						} else {

							// 如果是10表示成功
							if (error_code == 10) {
								postSendSmS(phone, type);
							} else {
								Message message = new Message();
								message.what = error_code;
								RegistActivityName.this.checkHandler
										.sendMessage(message);
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Message message = new Message();
					message.what = OUTTIME;
					RegistActivityName.this.checkHandler
							.sendMessage(message);
				} catch (IOException e) {
					e.printStackTrace();
					Message message = new Message();
					message.what = OUTTIME;
					RegistActivityName.this.checkHandler
							.sendMessage(message);
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
				RegistActivityName.this.smsHandler.sendMessage(message);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
				imServiceConnector.disconnect(RegistActivityName.this);
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

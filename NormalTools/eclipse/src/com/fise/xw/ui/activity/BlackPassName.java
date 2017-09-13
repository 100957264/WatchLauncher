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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.sp.SystemConfigSp;
import com.fise.xw.app.IMApplication;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.UrlConstant;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMSms.SmsActionType;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.utils.Logger;
import com.fise.xw.utils.Utils;
 

/**
 * 找回密码界面
 */
@SuppressLint("NewApi")
public class BlackPassName extends TTBaseActivity {

	private Logger logger = Logger.getLogger(BlackPassName.class);
	public Button next;
	private EditText mPhone;
	private int inputNum;
	private AlertDialog myDialog = null;
	private IMService imService;
	public String error_msg;
	public int user_id;
	public String imei;

	Handler checkHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: {
				Toast.makeText(BlackPassName.this, "该帐户不存在", Toast.LENGTH_SHORT)
						.show();
			}
				break;
			default: {
				Toast.makeText(BlackPassName.this, "" + error_msg,
						Toast.LENGTH_SHORT).show();
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
				intent.setClass(BlackPassName.this, BlackSmsActivity.class);
				intent.putExtra(IntentConstant.KEY_REGIST_NAME, mPhone
						.getText().toString());
				intent.putExtra(IntentConstant.KEY_PASS_SMS, user_id);

				BlackPassName.this.startActivity(intent);
				BlackPassName.this.finish();
				
			}
				break;
			case 1: {
				Toast.makeText(BlackPassName.this, "" + error_msg,
						Toast.LENGTH_SHORT).show();
			}
				break;
			default: {
				Toast.makeText(BlackPassName.this, "" + error_msg,
						Toast.LENGTH_SHORT).show();
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
		setContentView(R.layout.tt_activity_back_password);
		mPhone = (EditText) findViewById(R.id.phoneName);
		mPhone.setInputType(InputType.TYPE_CLASS_NUMBER);

		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonyManager.getDeviceId();

		SystemConfigSp.instance().init(getApplicationContext());
		if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(
				SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
			SystemConfigSp.instance().setStrConfig(
					SystemConfigSp.SysCfgDimension.LOGINSERVER,
					UrlConstant.ACCESS_MSG_ADDRESS);
		}

		imServiceConnector.connect(BlackPassName.this);
		 

		next = (Button) findViewById(R.id.next_in_button);
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if ((inputNum > 0) && (true)) {
					if (mPhone.getText().toString() != null) {

						if (Utils.isMobileNO(mPhone.getText().toString())) {
							SharedPreferences ww = getSharedPreferences(
									IntentConstant.KEY_REGIST_NAME,
									Activity.MODE_PRIVATE);
							SharedPreferences.Editor editor = ww.edit();
							editor.putString("regist_name", mPhone.getText()
									.toString());
							editor.commit();

							myDialog = new AlertDialog.Builder(
									BlackPassName.this).create();
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
															SmsActionType.SMS_ACTION_FORGET_PASSWORD);
													// imService.getLoginManager().SendSms(mPhone.getText().toString());
												}
											});

						} else {

							Toast.makeText(BlackPassName.this, "输入手机号码有误",
									Toast.LENGTH_SHORT).show();
						}

					} else {
						Toast.makeText(BlackPassName.this, "请输入小位号",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				BlackPassName.this.finish();
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

	}

	void postHttp(final String phone, final SmsActionType type) {

		new Thread() {
			@Override
			public void run() {

				String url = UrlConstant.ACCESS_MSG_USER_INFO;
				// 第一步，创建HttpPost对象
				HttpPost request = new HttpPost(url);

				try {
					// 设置httpPost请求参数

					String md5Imei = new String(com.fise.xw.Security
							.getInstance().EncryptPass(
									imei + "fise_zn_xw@fise.com.cn"));
					// 先封装一个 JSON 对象
					JSONObject param = new JSONObject();
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
						String result = EntityUtils.toString(httpResponse
								.getEntity());

						JSONObject json = new JSONObject(result);
						String code = json.getString("error_code");
						error_msg = json.getString("error_msg");

						if (code.equals("1")) {

							Message message = new Message();
							message.what = Integer.parseInt(code);
							BlackPassName.this.checkHandler
									.sendMessage(message);
						} else {
							user_id = json.getInt("user_id");
							if (user_id == 0) {
								Message message = new Message();
								message.what = user_id;
								BlackPassName.this.checkHandler
										.sendMessage(message);
							} else {
								postSendSmS(phone, type);
							}

						}

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

	void postSendSmS(String phone, SmsActionType type) {

		String url = UrlConstant.ACCESS_MSG_SEND_SMS;
		// 第一步，创建HttpPost对象
		HttpPost request = new HttpPost(url);

		try {
			// 设置httpPost请求参数
			String md5Imei = new String(com.fise.xw.Security.getInstance()
					.EncryptPass(imei + "fise_zn_xw@fise.com.cn"));

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
				BlackPassName.this.smsHandler.sendMessage(message);

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

	@Override
	protected void onDestroy() {
		super.onDestroy(); 
		imServiceConnector.disconnect(BlackPassName.this);
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

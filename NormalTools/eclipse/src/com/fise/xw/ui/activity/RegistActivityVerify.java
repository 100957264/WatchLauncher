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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.UrlConstant;
import com.fise.xw.protobuf.IMSms.SmsActionType;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;

/**
 * 注册界面
 */
@SuppressLint("NewApi")
public class RegistActivityVerify extends TTBaseActivity {

	private Logger logger = Logger.getLogger(RegistActivityVerify.class);
	public Button submit;
	private EditText mCodeword;
	private int inputNum;
	private TextView name;
	public String phoneName;
	public String err_code;
	public String imei;
	private TextView switch_submit_text;
	private TimeCount time;
	private String error_msg;

	Handler smsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: {
				Intent intent = new Intent();
				intent.setClass(RegistActivityVerify.this, RegistActivity.class);
				intent.putExtra(IntentConstant.KEY_REGIST_NAME, phoneName);
				intent.putExtra(IntentConstant.KEY_PASS_SMS, 0);
				intent.putExtra(IntentConstant.KEY_PASS_IS_REG, 0);
				RegistActivityVerify.this.finish();  //guanweile
				
				
				RegistActivityVerify.this.startActivity(intent);
			}
				break;
			case 1: {
				Toast.makeText(RegistActivityVerify.this, "输入验证码不对",
						Toast.LENGTH_SHORT).show();
			}
				break;
			default: {
				Toast.makeText(RegistActivityVerify.this, "" + err_code,
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
		setContentView(R.layout.tt_activity_regist_verify);
		mCodeword = (EditText) findViewById(R.id.password);

		 
		// SharedPreferences read = getSharedPreferences(
		// IntentConstant.KEY_REGIST_NAME, Activity.MODE_PRIVATE);
		// phoneName = read.getString("regist_name", "");

		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonyManager.getDeviceId();

		phoneName = RegistActivityVerify.this.getIntent().getStringExtra(
				IntentConstant.KEY_REGIST_NAME);

		name = (TextView) findViewById(R.id.name);
		name.setText(phoneName);
		time = new TimeCount(60000, 1000);
		time.start();
		
		switch_submit_text = (TextView) findViewById(R.id.switch_submit_text);

		switch_submit_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				AlertDialog.Builder builder = new AlertDialog.Builder(
						new ContextThemeWrapper(RegistActivityVerify.this,
								android.R.style.Theme_Holo_Light_Dialog));

				String[] items = new String[] { RegistActivityVerify.this
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
									postSendSmS(phoneName,
											SmsActionType.SMS_ACTION_REGIST);
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

		submit = (Button) findViewById(R.id.submit_in_button);
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mCodeword.getText().toString() != null) {

					postHttp(phoneName, mCodeword.getText().toString());

				} else {
					Toast.makeText(RegistActivityVerify.this, "请输入验证码",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				RegistActivityVerify.this.finish();
			}
		});

		if (inputNum > 0) {
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

	void postHttp(final String phone, final String auth_code) {

		new Thread() {
			@Override
			public void run() {

				String url = UrlConstant.ACCESS_MSG_ACTION_VERIFY;
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
					param.put("action",
							SmsActionType.SMS_ACTION_REGIST.ordinal());
					param.put("auth_code", auth_code);

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
						RegistActivityVerify.this.smsHandler
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
			switch_submit_text.setClickable(false);
			// retransmission.setText("(" + millisUntilFinished / 1000 + ") 秒");
			//switch_submit_text.setTextColor(Color.parseColor("#ebebeb"));
			switch_submit_text.setTextColor(Color.parseColor("#2247a0"));
			switch_submit_text.setText("接收短信大约需要" + millisUntilFinished / 1000 + "秒" );
		}

		@Override
		public void onFinish() {
			// retransmission.setText("重发");
			switch_submit_text.setClickable(true);
			switch_submit_text.setTextColor(Color.parseColor("#2247a0"));
			switch_submit_text.setText(RegistActivityVerify.this.getString(R.string.switch_submit_text));
			

		}
	}

}

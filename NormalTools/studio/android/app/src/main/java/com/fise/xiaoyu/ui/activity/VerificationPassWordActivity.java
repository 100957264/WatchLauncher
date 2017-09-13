package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fise.xiaoyu.DB.sp.LoginSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.manager.IMUserActionManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMSms;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 *
 */
@SuppressLint("NewApi")
public class VerificationPassWordActivity extends TTBaseActivity {


	private Logger logger = Logger.getLogger(VerificationPassWordActivity.class);
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

					TextView xiaowei_tel_text = (TextView) findViewById(R.id.xiaowei_tel_text);
					xiaowei_tel_text.setText(""+loginManager.getLoginInfo().getRealName());
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

		imServiceConnector.connect(VerificationPassWordActivity.this);

		setContentView(R.layout.tt_activity_verification_password);
		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                startActivity(new Intent(VerificationPassWordActivity.this ,AccountSecurityActivity.class));
				VerificationPassWordActivity.this.finish();
			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(VerificationPassWordActivity.this ,AccountSecurityActivity.class));
				VerificationPassWordActivity.this.finish();
			}
		});

		final EditText xiaowei_pass_word_text = (EditText) findViewById(R.id.xiaowei_pass_word_text);


		TextView right_text = (TextView) findViewById(R.id.right_text);
		right_text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				if(xiaowei_pass_word_text.getText().toString().equals("")){
					Utils.showToast(VerificationPassWordActivity.this, "请输入密码");
				}else{
					String Pwd = xiaowei_pass_word_text.getText().toString();
					String desPwd = new String(Security
							.getInstance().EncryptPass(Pwd));

					IMLoginManager loginManager = imService.getLoginManager();
					IMUserActionManager.instance().verifyAuthValue(
							loginManager.getLoginId(),
							IMBaseDefine.AuthConfirmType.AUTH_CONFIRM_TYPE_PWD,
							IMSms.SmsActionType.SMS_ACTION_RESET_PASSWORD, desPwd,1);
				}
			}
		});


	}



	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(VerificationPassWordActivity.this);
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
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(LoginEvent event) {
		switch (event) {
		case LOCAL_LOGIN_SUCCESS:
		case LOGIN_OK:
			break;

		}
	}

	/**
	 * ----------------------------event 事件驱动----------------------------
	 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

			case USER_INFO_VERIFYAUTH_PASS_SUCCESS: {
				Intent intent = new Intent(VerificationPassWordActivity.this, InputTelActivity.class);
				VerificationPassWordActivity.this.startActivity(intent);
                finish();
			}
			 break;

//			case USER_INFO_VERIFYAUTH_FAIL:
//				Utils.showToast(VerificationPassWordActivity.this,
//						"" + IMUserActionManager.instance().getPassCode());
//				break;
//			case USER_INFO_VERIFYAUTH_TIME_OUT:
//				Utils.showToast(VerificationPassWordActivity.this,"验证失败");
//				break;
		}
	}
	 
}

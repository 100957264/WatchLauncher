package com.fise.xw.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.sp.LoginSp;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.manager.IMUserActionManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.AuthConfirmType;
import com.fise.xw.protobuf.IMSms.SmsActionType;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.widget.PassDialog;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;

/**
 * 账户安全 界面
 */
@SuppressLint("NewApi")
public class AccountSecurityActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(AccountSecurityActivity.class);
	private Handler uiHandler = new Handler();
	private IMService imService;
	private ImageView icon_lock;
	private TextView icon_lock_text;

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

		imServiceConnector.connect(AccountSecurityActivity.this);
		EventBus.getDefault().register(this);

		setContentView(R.layout.tt_activity_account_security);

		icon_lock = (ImageView) findViewById(R.id.icon_lock);
		icon_lock_text = (TextView) findViewById(R.id.icon_lock_text);

//		RelativeLayout xiaowei_name = (RelativeLayout) findViewById(R.id.xiaowei_name);
//		xiaowei_name.setOnClickListener(new View.OnClickListener() {
//
//			public void onClick(View v) {
//				IMUIHelper.openNickNameActivity(AccountSecurityActivity.this);
//			}
//		});

		RelativeLayout emailPage = (RelativeLayout) findViewById(R.id.emailPage);
		emailPage.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openEmailActivity(AccountSecurityActivity.this);
			}
		});

		RelativeLayout passwordPage = (RelativeLayout) findViewById(R.id.passwordPage);
		passwordPage.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				showAddDialog();
			}
		});

		RelativeLayout AccountProtection = (RelativeLayout) findViewById(R.id.AccountProtection);
		AccountProtection.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openAccountProtection(AccountSecurityActivity.this);
			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				AccountSecurityActivity.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				AccountSecurityActivity.this.finish();
			}
		});

		initName();

	}

	protected void showAddDialog() {

		
		/*
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.password_dialog,
				null);
		final EditText editTextName = (EditText) textEntryView
				.findViewById(R.id.editTextName);
		//AlertDialog.Builder ad1 = new AlertDialog.Builder(
		//		AccountSecurityActivity.this);
		AlertDialog.Builder ad1 = new AlertDialog.Builder(new ContextThemeWrapper(
				AccountSecurityActivity.this, android.R.style.ThemeDialog1));
			
		editTextName.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD); 

		ad1.setTitle("为保障你的数据安全,修改密码前请填写原密码"); 
		ad1.setView(textEntryView);
		ad1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {

				if (editTextName.getText().toString() != "") {
					String Pwd = editTextName.getText().toString();
					String desPwd = new String(com.fise.xw.Security
							.getInstance().EncryptPass(Pwd));

					IMLoginManager loginManager = imService.getLoginManager();
					IMUserActionManager.instance().verifyAuthValue(
							loginManager.getLoginId(),
							AuthConfirmType.AUTH_CONFIRM_TYPE_PWD,
							SmsActionType.SMS_ACTION_RESET_PASSWORD, desPwd);
				} else {
					Toast.makeText(AccountSecurityActivity.this, "输入原始秘密不能为空",
							Toast.LENGTH_SHORT).show();
				}

			}
		});
		ad1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {

			}
		});
		ad1.show();// 显示对话框
		*/
		final PassDialog myDialog = new PassDialog(this);
        myDialog.setTitle("验证原密码");//设置标题
        myDialog.setMessage("为了保障你的数据安全,请输入原密码");//设置内容

        myDialog.dialog.show();//显示 
        myDialog.getEditText().setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD); 
        

        //确认按键回调，按下确认后在此做处理
        myDialog.setMyDialogOnClick(new PassDialog.MyDialogOnClick() {
            @Override
            public void ok() {
                
               if(myDialog.getEditText().getText().toString().equals("")){
            	   Toast.makeText(AccountSecurityActivity.this, "请输入密码",
							Toast.LENGTH_SHORT).show();
               }else{
            	   String Pwd = myDialog.getEditText().getText().toString();
					String desPwd = new String(com.fise.xw.Security
							.getInstance().EncryptPass(Pwd));

					IMLoginManager loginManager = imService.getLoginManager();
					IMUserActionManager.instance().verifyAuthValue(
							loginManager.getLoginId(),
							AuthConfirmType.AUTH_CONFIRM_TYPE_PWD,
							SmsActionType.SMS_ACTION_RESET_PASSWORD, desPwd);
					
					myDialog.dialog.dismiss(); 
               }
            }
        });

	}

	public void initName() {
		UserEntity user = IMLoginManager.instance().getLoginInfo();
 
		TextView phone_text = (TextView) findViewById(R.id.phone_text);
		phone_text.setText(user.getPhone());

		TextView emil_text = (TextView) findViewById(R.id.emil_text);
		emil_text.setText(user.getEmail());

		if (user.getLoginSafeSwitch() == 1) {
			icon_lock.setBackgroundResource(R.drawable.icon_lock);
			//icon_lock.setVisibility(View.VISIBLE);
			//icon_lock_text.setVisibility(View.VISIBLE);
			icon_lock_text.setText("已保护");
		} else {
			icon_lock.setBackgroundResource(R.drawable.un_icon_lock);
			//icon_lock.setVisibility(View.GONE);
			//icon_lock_text.setVisibility(View.GONE);
			icon_lock_text.setText("未保护");
		}
	}		

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(AccountSecurityActivity.this);
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
	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			initName();
			break;

		case USER_INFO_VERIFYAUTH_SUCCESS:
			IMUIHelper.openModifyPassActivity(AccountSecurityActivity.this,
					imService.getLoginManager().getLoginId());

			break;

		case USER_INFO_VERIFYAUTH_FAIL:
			Toast.makeText(AccountSecurityActivity.this,
					"" + IMUserActionManager.instance().getPassCode(),
					Toast.LENGTH_SHORT).show();
			break;

		}
	}

}

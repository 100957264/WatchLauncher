package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.LoginSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.manager.IMUserActionManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine.AuthConfirmType;
import com.fise.xiaoyu.protobuf.IMSms.SmsActionType;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.PassDialog;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

					TextView	tel_text = (TextView) findViewById(R.id.tel_text);
					tel_text.setText(imService.getLoginManager().getLoginInfo().getPhone());

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


		RelativeLayout tel_page = (RelativeLayout) findViewById(R.id.tel_page);
		tel_page.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openUpdateTel(AccountSecurityActivity.this);
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

		////小雨号是否已修改0-未修改，非0已修改
		RelativeLayout phone_Page = (RelativeLayout) findViewById(R.id.phone_Page);
		phone_Page.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMLoginManager loginManager = imService.getLoginManager();
				if(loginManager.getLoginInfo()!=null)
				{
					Log.i("aaa", "onClick: "+loginManager.getLoginInfo().getPinyinName());
					if(loginManager.getLoginInfo().getPinyinName().equals("0"))
					{
						Intent intent = new Intent(AccountSecurityActivity.this, XiaoWeiNameActivity.class);
						AccountSecurityActivity.this.startActivity(intent);
					}
				}
			}
		});


		initName();

	}

	protected void showAddDialog() {

		final PassDialog myDialog = new PassDialog(this,PassDialog.PASS_DIALOG_TYPE.PASS_DIALOG_WITH_MESSAGE);
        myDialog.setTitle("验证原密码");//设置标题
        myDialog.setMessage(getString(R.string.pass_modify));//设置内容

        myDialog.dialog.show();//显示 
        myDialog.getEditText().setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD); 
        

        //确认按键回调，按下确认后在此做处理
        myDialog.setMyDialogOnClick(new PassDialog.MyDialogOnClick() {
            @Override
            public void ok() {
                
               if(myDialog.getEditText().getText().toString().equals("")){
				   Utils.showToast(AccountSecurityActivity.this, "请输入密码");
               }else{
            	   String Pwd = myDialog.getEditText().getText().toString();
					String desPwd = new String(Security
							.getInstance().EncryptPass(Pwd));

					IMLoginManager loginManager = imService.getLoginManager();
					IMUserActionManager.instance().verifyAuthValue(
							loginManager.getLoginId(),
							AuthConfirmType.AUTH_CONFIRM_TYPE_PWD,
							SmsActionType.SMS_ACTION_RESET_PASSWORD, desPwd,0);  //两处调用　所以用０　１　区分
					
					myDialog.dialog.dismiss(); 
               }
            }
        });

	}

	public void initName() {
		UserEntity user = IMLoginManager.instance().getLoginInfo();

		if(!user.getPinyinName().equals("0"))
		{
			ImageView	real_name_image = (ImageView) findViewById(R.id.real_name_image);
			real_name_image.setVisibility(View.GONE);

		}

		TextView phone_text = (TextView) findViewById(R.id.phone_text);
		phone_text.setText(user.getRealName());

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
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			initName();
			break;
			case USER_INFO_DATA_UPDATE_PHONE:
			{
				TextView	tel_text = (TextView) findViewById(R.id.tel_text);
				tel_text.setText(imService.getLoginManager().getLoginInfo().getPhone());
			}
				break;

		case USER_INFO_VERIFYAUTH_SUCCESS:
			IMUIHelper.openModifyPassActivity(AccountSecurityActivity.this,
					imService.getLoginManager().getLoginId());

			break;

		case USER_INFO_VERIFYAUTH_FAIL:
			Utils.showToast(AccountSecurityActivity.this,
					"" + IMUserActionManager.instance().getPassCode());
			break;
			case USER_INFO_VERIFYAUTH_TIME_OUT:
				Utils.showToast(AccountSecurityActivity.this,"验证失败");
				break;
		}
	}

}

package com.fise.xw.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.widget.FilletDialog;
import com.fise.xw.ui.widget.IMBaseImageView;
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;

/**
 *  我的界面的个人信息界面
 * @author weileiguan
 *
 */
public class ActivityLoginInfo extends TTBaseActivity {
	
	private static IMService imService;
	private UserEntity loginInfo;
	private IMLoginManager imLoginManager = IMLoginManager.instance();

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			loginInfo = imService.getLoginManager().getLoginInfo();

			initDetailProfile();
		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_activity_my);
		imServiceConnector.connect(this);
		EventBus.getDefault().register(this);

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				ActivityLoginInfo.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				ActivityLoginInfo.this.finish();
			}
		});

		RelativeLayout qr_code = (RelativeLayout) findViewById(R.id.qr_code);
		qr_code.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(ActivityLoginInfo.this,
						InfoQRActivity.class);
				ActivityLoginInfo.this.startActivity(intent);
			}
		});

		Button logout_button = (Button) findViewById(R.id.logout_button);
		logout_button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				 
				final FilletDialog myDialog = new FilletDialog(ActivityLoginInfo.this);
		       // myDialog.setTitle("修改密码");//设置标题
		        myDialog.setMessage(getString(R.string.exit_teamtalk_tip));//设置内容

		        myDialog.dialog.show();//显示 
		        
		        //确认按键回调，按下确认后在此做处理
		        myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
		            @Override
		            public void ok() {
		            	IMLoginManager.instance().setKickout(false);
						IMLoginManager.instance().logOut();
						ActivityLoginInfo.this.finish();
						myDialog.dialog.dismiss();
		            }
		        });
		         
			}
		});

		 

	}

	public void initDetailProfile() {

		TextView locality_text = (TextView) findViewById(R.id.locality_text);
		if (loginInfo.getProvince().equals(loginInfo.getCity())) {
			locality_text.setText("" + loginInfo.getCity());
		} else {
			locality_text.setText("" + loginInfo.getProvince() + " "
					+ loginInfo.getCity());
		}

		TextView sex_text = (TextView) findViewById(R.id.sex_text);
		if (loginInfo.getGender() == DBConstant.SEX_MAILE) {
			sex_text.setText("男");
		} else {
			sex_text.setText("女");
		}

		RelativeLayout sex = (RelativeLayout) findViewById(R.id.sex);
		sex.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openLoginInfoSexActivity(ActivityLoginInfo.this,DBConstant.SEX_INFO_USER,loginInfo.getPeerId());
			}
		});

		RelativeLayout signature = (RelativeLayout) findViewById(R.id.signature);
		signature.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openLoginInfoSignedActivity(ActivityLoginInfo.this);
			}
		});

		RelativeLayout locality = (RelativeLayout) findViewById(R.id.locality_relat);
		locality.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openCityListActivity(ActivityLoginInfo.this);
			}
		});

		RelativeLayout login_name = (RelativeLayout) findViewById(R.id.login_name);
		login_name.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// IMUIHelper.openAvatarActivity(ActivityLoginInfo.this);
				Intent intent = new Intent(ActivityLoginInfo.this,
						HeadPortraitActivity.class);
				intent.putExtra(IntentConstant.KEY_AVATAR_URL,
						loginInfo.getAvatar());
				intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR,
						true);

				startActivity(intent);

			}
		});

		RelativeLayout nickname = (RelativeLayout) findViewById(R.id.nickname);
		nickname.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openSetNickNameActivity(ActivityLoginInfo.this,
						DBConstant.OWN_NICK, loginInfo.getPeerId());
			}
		});

		TextView wei_text = (TextView) findViewById(R.id.wei_text);
		wei_text.setText(loginInfo.getPhone());

		TextView nickname_text = (TextView) findViewById(R.id.nickname_text);
		nickname_text.setText(loginInfo.getMainName());

		IMBaseImageView user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);
		// 头像设置
		user_portrait
				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
		user_portrait.setCorner(8);
		user_portrait
				.setImageResource(R.drawable.tt_default_user_portrait_corner);
		user_portrait.setImageUrl(loginInfo.getAvatar());

		TextView signature_text = (TextView) findViewById(R.id.signature_text);
		signature_text.setText(loginInfo.getSign_info() + "");
	}

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			initDetailProfile();
			break;

		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
		super.onDestroy();
	}

	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loginInfo = imLoginManager.getLoginInfo();
	}
}

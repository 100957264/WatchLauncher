package com.fise.xw.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.manager.IMDeviceManager;
import com.fise.xw.imservice.manager.IMLoginManager; 
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ChangeDataType; 
import com.fise.xw.protobuf.IMDevice.SettingType;
import com.fise.xw.ui.base.TTBaseActivity;

import de.greenrobot.event.EventBus;

/**
 * 登录的个人性别设置界面
 * 
 * @author weileiguan
 * 
 */
public class LoginInfoSex extends TTBaseActivity {
	private static IMService imService;
	private UserEntity userInfo;
	private IMLoginManager imLoginManager = IMLoginManager.instance();
	private ImageView female_right;
	private ImageView man_right;
	private int type;
	private int currentUserId;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			if (type == DBConstant.SEX_INFO_DEV) {

				userInfo = imService.getContactManager().findDeviceContact(
						currentUserId);
				if (userInfo == null) {
					return;
				}

				if (userInfo.getGender() == DBConstant.SEX_MAILE) {
					female_right.setVisibility(View.GONE);
					man_right.setVisibility(View.VISIBLE);
					//
				} else {

					female_right.setVisibility(View.VISIBLE);
					man_right.setVisibility(View.GONE);
				}

				TextView left_text = (TextView) findViewById(R.id.left_text);
				left_text.setText("设备性别");

			}

		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_activity_sex);
		imServiceConnector.connect(this);
		EventBus.getDefault().register(this);

		type = this.getIntent().getIntExtra(IntentConstant.SEX_INFO_TYPE, 0);

		currentUserId = this.getIntent().getIntExtra(IntentConstant.KEY_PEERID,
				0);

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				LoginInfoSex.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				LoginInfoSex.this.finish();
			}
		});

		female_right = (ImageView) findViewById(R.id.female_right);
		man_right = (ImageView) findViewById(R.id.man_right);

		if (type == DBConstant.SEX_INFO_USER) {

			userInfo = imLoginManager.getLoginInfo();
			if (userInfo.getGender() == DBConstant.SEX_MAILE) {
				female_right.setVisibility(View.GONE);
				man_right.setVisibility(View.VISIBLE);
				//
			} else {

				female_right.setVisibility(View.VISIBLE);
				man_right.setVisibility(View.GONE);
			}
		}

		RelativeLayout female = (RelativeLayout) findViewById(R.id.female);
		female.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				female_right.setVisibility(View.VISIBLE);
				man_right.setVisibility(View.GONE);

				if (type == DBConstant.SEX_INFO_DEV) {

					IMDeviceManager.instance().settingPhone(userInfo, userInfo,
							DBConstant.SEX_FEMALE + "",
							SettingType.SETTING_TYPE_USER_SEX,
							DBConstant.SEX_FEMALE + "");
				} else {
					userInfo.setGender(DBConstant.SEX_FEMALE);
					imLoginManager.setLoginInfo(userInfo);

					UserEntity user = IMLoginManager.instance().getLoginInfo();
					String data = "" + DBConstant.SEX_FEMALE;
					IMContactManager.instance().ChangeUserInfo(
							user.getPeerId(),
							ChangeDataType.CHANGE_USERINFO_SEX, data);

				}

			}
		});

		RelativeLayout man = (RelativeLayout) findViewById(R.id.man);
		man.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				female_right.setVisibility(View.GONE);
				man_right.setVisibility(View.VISIBLE);
				imLoginManager.setLoginInfo(userInfo);

				if (type == DBConstant.SEX_INFO_DEV) {

					IMDeviceManager.instance().settingPhone(userInfo, userInfo,
							DBConstant.SEX_MAILE + "",
							SettingType.SETTING_TYPE_USER_SEX,
							DBConstant.SEX_MAILE + "");
				} else {

					UserEntity user = IMLoginManager.instance().getLoginInfo();
					String data = "" + DBConstant.SEX_MAILE;
					IMContactManager.instance().ChangeUserInfo(
							user.getPeerId(),
							ChangeDataType.CHANGE_USERINFO_SEX, data);
				}
			}
		});
	}

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			LoginInfoSex.this.finish();
			// Toast.makeText(LoginInfoSex.this, "修改性别成功",
			// Toast.LENGTH_SHORT).show();
			break;

		}
	}

	public void onEventMainThread(DeviceEvent event) {
		switch (event) {
		case USER_INFO_SETTING_DEVICE_SUCCESS:
			LoginInfoSex.this.finish();
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

}

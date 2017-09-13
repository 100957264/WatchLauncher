package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMDeviceManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xiaoyu.protobuf.IMDevice.SettingType;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 登录的个人性别设置界面
 */
public class LoginInfoSex extends TTBaseActivity {
	private static IMService imService;
	private UserEntity userInfo;
    private UserEntity devInfo;
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

				if (type == DBConstant.SEX_INFO_DEV) {

					IMDeviceManager.instance().settingPhone(userInfo, userInfo,
							DBConstant.SEX_MAILE + "",
							SettingType.SETTING_TYPE_USER_SEX,
							DBConstant.SEX_MAILE + "");
				} else {
                    imLoginManager.setLoginInfo(userInfo);

					UserEntity user = IMLoginManager.instance().getLoginInfo();
					String data = "" + DBConstant.SEX_MAILE;
					IMContactManager.instance().ChangeUserInfo(
							user.getPeerId(),
							ChangeDataType.CHANGE_USERINFO_SEX, data);
				}
			}
		});
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			LoginInfoSex.this.finish();
			break;

		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(DeviceEvent event) {
		switch (event) {
		case USER_INFO_SETTING_DEVICE_SUCCESS:
			Utils.showToast(LoginInfoSex.this , getString(R.string.set_success));
			LoginInfoSex.this.finish();
			break;

		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
				imServiceConnector.disconnect(this);
		super.onDestroy();
	}

}

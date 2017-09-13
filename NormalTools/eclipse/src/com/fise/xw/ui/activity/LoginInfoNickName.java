package com.fise.xw.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ChangeDataType; 
import com.fise.xw.protobuf.IMDevice.SettingType;
import com.fise.xw.ui.base.TTBaseActivity;

import de.greenrobot.event.EventBus;

/**
 * 登录的个人昵称设置界面
 * 
 * @author weileiguan
 * 
 */
public class LoginInfoNickName extends TTBaseActivity {
	private static IMService imService;
	private UserEntity loginInfo;
	private IMLoginManager imLoginManager = IMLoginManager.instance();
	private ImageView female_right;
	private ImageView man_right;
	private EditText nick_name;
	private int nick_mode;
	private int currentUserId;
	private UserEntity currentUser;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			if (currentUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);
			if (currentUser == null) {
				return;
			}

			if (nick_mode == DBConstant.DEVICE_NICK) {
				nick_name.setText(currentUser.getMainName());

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

		setContentView(R.layout.tt_activity_set_nick);
		imServiceConnector.connect(this);
		EventBus.getDefault().register(this);

		nick_mode = this.getIntent().getIntExtra(IntentConstant.KEY_NICK_MODE,
				0);
		currentUserId = this.getIntent().getIntExtra(IntentConstant.KEY_PEERID,
				0);

		nick_name = (EditText) findViewById(R.id.nick_name);
		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				LoginInfoNickName.this.finish();
			}
		});

		TextView right_text = (TextView) findViewById(R.id.right_text);
		right_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//

				if (nick_mode == DBConstant.OWN_NICK) {
					UserEntity user = IMLoginManager.instance().getLoginInfo();
					String data = nick_name.getText().toString();
					String trimData = data.trim();

					if (trimData != null && (!trimData.equals(""))) {
						IMContactManager.instance().ChangeUserInfo(
								user.getPeerId(),
								ChangeDataType.CHANGE_USERINFO_NICK, trimData);
					} else {
						Toast.makeText(LoginInfoNickName.this, "输入为空",
								Toast.LENGTH_SHORT).show();
					}

				} else if (nick_mode == DBConstant.DEVICE_NICK) {

					String data = nick_name.getText().toString();
					String trimData = data.trim();

					if (trimData != null && (!trimData.equals(""))) {
						imService.getDeviceManager().settingPhone(currentUser,
								currentUser, trimData,
								SettingType.SETTING_TYPE_DEVICE_NICK, "");
					} else {
						Toast.makeText(LoginInfoNickName.this, "输入为空",
								Toast.LENGTH_SHORT).show();
					}

				}

			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				LoginInfoNickName.this.finish();
			}
		});

		if (nick_mode == DBConstant.OWN_NICK) {
			loginInfo = imLoginManager.getLoginInfo();
			nick_name = (EditText) findViewById(R.id.nick_name);
			nick_name.setText(loginInfo.getMainName());
		}

	}

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			LoginInfoNickName.this.finish();
			break;

		}
	}

	public void onEventMainThread(DeviceEvent event) {
		switch (event) {

		case USER_INFO_SETTING_DEVICE_SUCCESS:
			LoginInfoNickName.this.finish();
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

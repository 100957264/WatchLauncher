package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
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
 * 登录的个人昵称设置界面
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
	private final int LIMITNUM = 15;

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

		nick_mode = this.getIntent().getIntExtra(IntentConstant.KEY_NICK_MODE,
				0);
		currentUserId = this.getIntent().getIntExtra(IntentConstant.KEY_PEERID,
				0);

		nick_name = (EditText) findViewById(R.id.nick_name);
		nick_name.addTextChangedListener(new TextWatcher() {
			private CharSequence temp;
			private int selectionStart;
			private int selectionEnd;

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				temp = s;
			}

			@Override
			public void afterTextChanged(Editable s) {

				int number = LIMITNUM - s.length();
				selectionStart = nick_name.getSelectionStart();
				selectionEnd = nick_name.getSelectionEnd();
				//删除多余输入的字（不会显示出来）
				if (temp.length() > LIMITNUM) {
					s.delete(selectionStart - 1, selectionEnd);
					nick_name.setText(s);
				}
				//设置光标在最后
				//nick_name.setSelection(s.length());
			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				LoginInfoNickName.this.finish();
			}
		});

		TextView right_text = (TextView) findViewById(R.id.right_text);
		right_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				if (nick_mode == DBConstant.OWN_NICK) {
					UserEntity user = IMLoginManager.instance().getLoginInfo();
					String data = nick_name.getText().toString();
					String trimData = data.trim();

					if (trimData != null && (!trimData.equals(""))) {
						if(trimData.length()>LIMITNUM){
							Utils.showToast(LoginInfoNickName.this, "输入超过15字,请重新输入");
						}else{
							IMContactManager.instance().ChangeUserInfo(
									user.getPeerId(),
									ChangeDataType.CHANGE_USERINFO_NICK, trimData);
						}

					} else {
						Utils.showToast(LoginInfoNickName.this, "输入为空");
					}

				} else if (nick_mode == DBConstant.DEVICE_NICK) {

					String data = nick_name.getText().toString();
					String trimData = data.trim();

					if (trimData != null && (!trimData.equals(""))) {

						if(trimData.length()>LIMITNUM){
							Utils.showToast(LoginInfoNickName.this, "输入超过15字,请重新输入");
						}else{
							imService.getDeviceManager().settingPhone(currentUser,
									currentUser, trimData,
									SettingType.SETTING_TYPE_DEVICE_NICK, "");
						}

					} else {
						Utils.showToast(LoginInfoNickName.this, "输入为空");
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

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			LoginInfoNickName.this.finish();
			break;

		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(DeviceEvent event) {
		switch (event) {

		case USER_INFO_SETTING_DEVICE_SUCCESS:
			LoginInfoNickName.this.finish();
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

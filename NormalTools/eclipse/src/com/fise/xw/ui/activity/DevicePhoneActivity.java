package com.fise.xw.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R; 
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector; 
import com.fise.xw.protobuf.IMDevice.SettingType;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.utils.Logger;
import com.fise.xw.utils.Utils;

import de.greenrobot.event.EventBus;

/**
 * 设置设备的手机号码 界面
 * 
 * @author weileiguan
 * 
 */
public class DevicePhoneActivity extends TTBaseFragmentActivity {
	private Logger logger = Logger.getLogger(HelpActivity.class);
	private Handler uiHandler = new Handler();
	private IMService imService;
	private ListView listView = null;
	private int currentUserId;
	private UserEntity currentUser;
	private EditText device_phone;
	private DeviceEntity rsp;
	private UserEntity loginContact;
	private TextView confirm;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onServiceDisconnected() {
		}

		@Override
		public void onIMServiceConnected() {
			logger.d("login#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			currentUserId = DevicePhoneActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);

			if (currentUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);
			if (currentUser == null) {
				return;
			}

			rsp = imService.getDeviceManager().findDeviceCard(currentUserId);
			if (rsp == null) {
				return;
			}

			// device_phone.setHint(currentUser.getPhone());
			device_phone.setText("" + currentUser.getPhone());

			loginContact = IMLoginManager.instance().getLoginInfo();
			if (rsp.getMasterId() != loginContact.getPeerId()) {
				device_phone.setEnabled(false);
				confirm.setVisibility(View.GONE);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		imServiceConnector.connect(DevicePhoneActivity.this);
		EventBus.getDefault().register(this);

		setContentView(R.layout.tt_activity_device_phone);

		device_phone = (EditText) findViewById(R.id.device_phone);

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DevicePhoneActivity.this.finish();
			}
		});

		confirm = (TextView) findViewById(R.id.confirm);
		confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (device_phone.getText().toString().equals("")) {
					Toast.makeText(DevicePhoneActivity.this, "请输入号码",
							Toast.LENGTH_SHORT).show();
				} else {

					if (Utils.isMobileNO(device_phone.getText().toString())) {
						if (currentUser.getPhone().equals("")) {
							imService.getDeviceManager().settingWhite(
									currentUserId,
									device_phone.getText().toString(),
									SettingType.SETTING_TYPE_DEVICE_MOBILE,
									DBConstant.ADD);
						} else {
							imService.getDeviceManager().settingWhite(
									currentUserId,
									device_phone.getText().toString(),
									SettingType.SETTING_TYPE_DEVICE_MOBILE,
									DBConstant.UPDATE);
						}
					} else {
						Toast.makeText(DevicePhoneActivity.this, "您的输入手机号码不正确",
								Toast.LENGTH_SHORT).show();
					}

				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(DevicePhoneActivity.this);
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
	public void onEventMainThread(DeviceEvent event) {
		switch (event) {

		case USER_INFO_SETTING_DEVICE_SUCCESS:
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);
			if (currentUser != null) {
				device_phone.setHint(currentUser.getPhone());
			}
			DevicePhoneActivity.this.finish();

			break;
		case USER_INFO_SETTING_DEVICE_FAILED:
			Toast.makeText(DevicePhoneActivity.this,
					"" + imService.getDeviceManager().getPhoneCode(),
					Toast.LENGTH_SHORT).show();

			break;

		}
	}

}

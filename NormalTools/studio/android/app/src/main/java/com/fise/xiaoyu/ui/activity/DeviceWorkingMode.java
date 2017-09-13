package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ClientType;
import com.fise.xiaoyu.protobuf.IMDevice.SettingType;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 设置设备的工作模式界面
 */
public class DeviceWorkingMode extends TTBaseActivity {

	private static IMService imService;
	private IMLoginManager imLoginManager = IMLoginManager.instance();
	private ImageView ordinary_right;
	private ImageView power_right;
	private ImageView dormancy_right;
	private DeviceEntity rsp;
	private int currentUserId;
	private UserEntity currentUser;
	private DeviceEntity device;
	private UserEntity loginContact;
	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			rsp = imService.getDeviceManager().findDeviceCard(currentUserId);
			device = imService.getDeviceManager().findDeviceCard(currentUserId);
			loginContact = IMLoginManager.instance().getLoginInfo();
			if (rsp == null) {
				return;
			}
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);

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

		setContentView(R.layout.tt_activity_working_model);

		imServiceConnector.connect(this);

		currentUserId = DeviceWorkingMode.this.getIntent().getIntExtra(
				IntentConstant.KEY_PEERID, 0);

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceWorkingMode.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceWorkingMode.this.finish();
			}
		});

		ordinary_right = (ImageView) findViewById(R.id.ordinary_right);
		power_right = (ImageView) findViewById(R.id.power_right);
		dormancy_right = (ImageView) findViewById(R.id.dormancy_right);

		RelativeLayout ordinary = (RelativeLayout) findViewById(R.id.ordinary);
		ordinary.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (device.getMasterId() == loginContact.getPeerId()){
					imService.getDeviceManager().settingOpen(currentUserId, "",
							SettingType.SETTING_TYPE_WORK_MODE, 1, rsp);
				}else{
					Utils.showToast(DeviceWorkingMode.this , getString(R.string.no_authority_to_operate));
				}


			}
		});

		RelativeLayout power = (RelativeLayout) findViewById(R.id.power);
		power.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (device.getMasterId() == loginContact.getPeerId()){
					imService.getDeviceManager().settingOpen(currentUserId, "",
							SettingType.SETTING_TYPE_WORK_MODE, 2, rsp);
				}else{
					Utils.showToast(DeviceWorkingMode.this , getString(R.string.no_authority_to_operate));
				}

			}
		});

		RelativeLayout dormancy = (RelativeLayout) findViewById(R.id.dormancy);
		dormancy.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (device.getMasterId() == loginContact.getPeerId()){
					imService.getDeviceManager().settingOpen(currentUserId, "",
							SettingType.SETTING_TYPE_WORK_MODE, 3, rsp);
				}else{
					Utils.showToast(DeviceWorkingMode.this , getString(R.string.no_authority_to_operate));
				}

			}
		});

	}

	public void initDetailProfile() {

		if (rsp.getMode() == 1) {
			ordinary_right.setVisibility(View.VISIBLE);
			power_right.setVisibility(View.GONE);
			dormancy_right.setVisibility(View.GONE);

		} else if (rsp.getMode() == 2) {
			ordinary_right.setVisibility(View.GONE);
			power_right.setVisibility(View.VISIBLE);
			dormancy_right.setVisibility(View.GONE);

		} else if (rsp.getMode() == 3) {

			ordinary_right.setVisibility(View.GONE);
			power_right.setVisibility(View.GONE);
			dormancy_right.setVisibility(View.VISIBLE);

		}

		if (currentUser != null) {
			if (currentUser.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE) {
				TextView left_text = (TextView) findViewById(R.id.left_text);
				left_text.setText("小雨手机");
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(DeviceEvent event) {
		switch (event) {

		case USER_INFO_SETTING_DEVICE_SUCCESS:
			DeviceWorkingMode.this.finish();
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

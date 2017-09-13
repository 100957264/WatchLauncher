package com.fise.xw.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xw.R; 
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.protobuf.IMDevice.SettingType;
import com.fise.xw.ui.base.TTBaseActivity;

import de.greenrobot.event.EventBus;

/**
 * 设备的铃声设置
 * 
 * @author weileiguan
 * 
 */
public class DeviceInfoBells extends TTBaseActivity {

	private static IMService imService;
	private IMLoginManager imLoginManager = IMLoginManager.instance();
	private ImageView bells_right;
	private ImageView vibrates_right;
	private ImageView bells_vibrates_right;
	private DeviceEntity rsp;
	private int currentUserId;
	private UserEntity currentDevice;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			currentDevice = imService.getContactManager().findDeviceContact(
					currentUserId);
			rsp = imService.getDeviceManager().findDeviceCard(currentUserId);
			if (rsp == null) {
				return;
			}

			if (currentDevice != null) {
				if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE) {
					TextView left_text = (TextView) findViewById(R.id.left_text);
					left_text.setText("定位卡片机");
				} else if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_FISE_CAR_VALUE) {
					TextView left_text = (TextView) findViewById(R.id.left_text);
					left_text.setText("电动车");
				}
			}

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

		setContentView(R.layout.tt_activity_bell);
		imServiceConnector.connect(this);
		EventBus.getDefault().register(this);

		currentUserId = DeviceInfoBells.this.getIntent().getIntExtra(
				IntentConstant.KEY_PEERID, 0);

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceInfoBells.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceInfoBells.this.finish();
			}
		});

		bells_right = (ImageView) findViewById(R.id.bells_right);
		vibrates_right = (ImageView) findViewById(R.id.vibrates_right);
		bells_vibrates_right = (ImageView) findViewById(R.id.bells_vibrates_right);

		RelativeLayout bells = (RelativeLayout) findViewById(R.id.bells);
		bells.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				imService.getDeviceManager().settingOpen(currentUserId, "",
						SettingType.SETTING_TYPE_BELL_MODE, 1, rsp);

			}
		});

		RelativeLayout vibrates = (RelativeLayout) findViewById(R.id.vibrates);
		vibrates.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				imService.getDeviceManager().settingOpen(currentUserId, "",
						SettingType.SETTING_TYPE_BELL_MODE, 2, rsp);
			}
		});

		RelativeLayout bells_vibrates = (RelativeLayout) findViewById(R.id.bells_vibrates);
		bells_vibrates.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) { 

				imService.getDeviceManager().settingOpen(currentUserId, "",
						SettingType.SETTING_TYPE_BELL_MODE, 3, rsp);
			}
		});
	}

	public void initDetailProfile() {

		if (rsp.getBellMode() == 1) {
			bells_right.setVisibility(View.VISIBLE);
			vibrates_right.setVisibility(View.GONE);
			bells_vibrates_right.setVisibility(View.GONE);

		} else if (rsp.getBellMode() == 2) {
			bells_right.setVisibility(View.GONE);
			vibrates_right.setVisibility(View.VISIBLE);
			bells_vibrates_right.setVisibility(View.GONE);

		} else if (rsp.getBellMode() == 3) {

			bells_right.setVisibility(View.GONE);
			vibrates_right.setVisibility(View.GONE);
			bells_vibrates_right.setVisibility(View.VISIBLE);

		}
	}

	public void onEventMainThread(DeviceEvent event) {
		switch (event) {

		case USER_INFO_SETTING_DEVICE_SUCCESS:
			DeviceInfoBells.this.finish();
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

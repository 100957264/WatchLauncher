package com.fise.xw.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.protobuf.IMDevice.SettingType;
import com.fise.xw.ui.base.TTBaseFragmentActivity;

import de.greenrobot.event.EventBus;



/**
 * 设备的报警管理界面
 * @author weileiguan
 *
 */
public class WarningActivity extends TTBaseFragmentActivity {

	private WarningActivity activity;
	private static IMService imService;
	private IMContactManager contactMgr;
	private int currentUserId;

	private UserEntity currentUser;
	private DeviceEntity device;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			currentUserId = WarningActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);

			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);
			if (currentUser == null) {
				return;
			}

			device = imService.getDeviceManager().findDeviceCard(currentUserId);
			if (device == null) {
				return;
			}

			initDetailProfile();

		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@SuppressLint("ResourceAsColor")
	private void initDetailProfile() {

		final UserEntity loginContact = IMLoginManager.instance()
				.getLoginInfo();

		// 获取CheckBox实例
		CheckBox shutdown = (CheckBox) this
				.findViewById(R.id.shutdown_warning_Checkbox);

		TextView shutdown_warning_text = (TextView) this
				.findViewById(R.id.shutdown_warning_text);

		ImageView shutdown_default_arrow = (ImageView) this
				.findViewById(R.id.shutdown_default_arrow);

		boolean isShutdown = false;
		if (device.getAlrPoweroff() == 1) {
			isShutdown = true;
		}
		shutdown.setChecked(isShutdown);

		// 绑定监听器
		shutdown.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub

				int shutodown;
				if (arg1 == false) {
					shutodown = 0;
				} else {
					shutodown = 1;
				}
				 
				
				imService.getDeviceManager()
						.settingOpen(currentUserId, "",
								SettingType.SETTING_TYPE_ALARM_OPWEROFF,
								shutodown, device);

			}
		});

		if (device.getMasterId() != loginContact.getPeerId()) {
			shutdown.setEnabled(false);
			shutdown.setVisibility(View.GONE);

			shutdown_warning_text.setVisibility(View.VISIBLE);
			shutdown_default_arrow.setVisibility(View.VISIBLE);

			if (device.getAlrPoweroff() == 1) {
				shutdown_warning_text.setText("打开");
			} else {
				shutdown_warning_text.setText("关闭");
			}

		} else {
			shutdown.setVisibility(View.VISIBLE);
			shutdown_warning_text.setVisibility(View.GONE);
			shutdown_default_arrow.setVisibility(View.GONE);
		}

		TextView electricity_warning_text = (TextView) this
				.findViewById(R.id.electricity_warning_text);

		ImageView electricity_default_arrow = (ImageView) this
				.findViewById(R.id.electricity_default_arrow);

		// 获取CheckBox实例
		CheckBox electricity = (CheckBox) this
				.findViewById(R.id.electricity_warning_Checkbox);
		boolean isElectricity = false;
		if (device.getAlrBattery() == 1) {
			isElectricity = true;
		}
		electricity.setChecked(isElectricity);

		// 绑定监听器
		electricity.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub

				int battery;
				if (arg1 == false) {
					battery = 0;
				} else {
					battery = 1;
				} 
				
				imService.getDeviceManager().settingOpen(currentUserId, "",
						SettingType.SETTING_TYPE_ALARM_BATTERY, battery, device);

			}
		});

		if (device.getMasterId() != loginContact.getPeerId()) {
			electricity.setEnabled(false);
			electricity.setVisibility(View.GONE);
			electricity_warning_text.setVisibility(View.VISIBLE);
			electricity_default_arrow.setVisibility(View.VISIBLE);

			if (device.getAlrBattery() == 1) {
				electricity_warning_text.setText("打开");
			} else {
				electricity_warning_text.setText("关闭");
			}

		} else {
			electricity.setVisibility(View.VISIBLE);
			electricity_warning_text.setVisibility(View.GONE);
			electricity_default_arrow.setVisibility(View.GONE);

		}

		// 获取CheckBox实例
		CheckBox callAlarm_Checkbox = (CheckBox) this
				.findViewById(R.id.CallAlarm_Checkbox);

		TextView callAlarm_warning_text = (TextView) this
				.findViewById(R.id.CallAlarm_warning_text);

		ImageView callAlarm_default_arrow = (ImageView) this
				.findViewById(R.id.CallAlarm_default_arrow);

		boolean isCallAlarm = false;
		if (device.getAlrCall() == 1) {
			isCallAlarm = true;
		}
		callAlarm_Checkbox.setChecked(isCallAlarm);

		// 绑定监听器
		callAlarm_Checkbox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						// TODO Auto-generated method stub

						int callAlarm;
						if (arg1 == false) {
							callAlarm = 0;
						} else {
							callAlarm = 1;
						} 
						
						imService.getDeviceManager().settingOpen(currentUserId,
								"", SettingType.SETTING_TYPE_ALARM_CALL,
								callAlarm, device);

					}
				});

		if (device.getMasterId() != loginContact.getPeerId()) {

			callAlarm_Checkbox.setEnabled(false);
			callAlarm_Checkbox.setVisibility(View.GONE);

			callAlarm_warning_text.setVisibility(View.VISIBLE);
			callAlarm_default_arrow.setVisibility(View.VISIBLE);

			if (device.getAlrCall() == 1) {
				callAlarm_warning_text.setText("打开");
			} else {
				callAlarm_warning_text.setText("关闭");
			}

		} else {
			callAlarm_Checkbox.setVisibility(View.VISIBLE);

			callAlarm_warning_text.setVisibility(View.GONE);
			callAlarm_default_arrow.setVisibility(View.GONE);

		}

		
		
		// 获取CheckBox实例 
		/*  新需求暂时屏蔽
		CheckBox electronic = (CheckBox) this
				.findViewById(R.id.electronic_warning_Checkbox);
		
		
		TextView electronic_warning_text = (TextView) this
				.findViewById(R.id.electronic_warning_text1);

		ImageView electronic_default_arrow = (ImageView) this
				.findViewById(R.id.electronic_default_arrow);
		
		boolean isElectronic = false;
		if (rsp.getPen() == 1) {
			isElectronic = true; 
		}
		electronic.setChecked(isElectronic);

		// 绑定监听器
		electronic.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub

				int pen;
				if (arg1 == false) {
					pen = 0;
				} else {
					pen = 1;
				}
				imService.getDeviceManager().settingOpen(currentUserId, "",
						SettingType.SETTING_TYPE_ALARM_FENCE, pen, rsp);

			}
		});

		if (rsp.getMasterId() != loginContact.getPeerId()) {
			electronic.setEnabled(false);
			
			electronic.setVisibility(View.GONE);

			electronic_warning_text.setVisibility(View.VISIBLE);
			electronic_default_arrow.setVisibility(View.VISIBLE);

			if (rsp.getPen() == 1) {
				electronic_warning_text.setText("打开");
			} else {
				electronic_warning_text.setText("关闭");
			}
		}else{
			
			electronic.setVisibility(View.VISIBLE);

			electronic_warning_text.setVisibility(View.GONE);
			electronic_default_arrow.setVisibility(View.GONE);
		}
	*/
	

	   	if(currentUser!=null){
    		if (currentUser.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE) {
    			TextView black = (TextView) findViewById(R.id.black);
    			black.setText("定位卡片机");
    		}else if (currentUser.getUserType() == ClientType.CLIENT_TYPE_FISE_CAR_VALUE) {
    			TextView black = (TextView) findViewById(R.id.black);
    			black.setText("电动车");
    		}
    	} 
	   	
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.warning_info_follow);
		imServiceConnector.connect(this);
		EventBus.getDefault().register(this);

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				WarningActivity.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				WarningActivity.this.finish();
			}
		});

	}

	public void onEventMainThread(DeviceEvent event) {
		switch (event) {

		case USER_INFO_ADD_DEVICE_FAILED:
			break;
		case USER_INFO_SETTING_DEVICE_SUCCESS:
		case USER_INFO_UPDATE_DEVICE_SUCCESS:
		case USER_INFO_UPDATE_INFO_SUCCESS:
			device = imService.getDeviceManager().findDeviceCard(currentUserId);
			if (device !=null) {
				initDetailProfile();
			}
			break;
		case USER_INFO_SETTING_DEVICE_FAILED:
			break;

		}
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
		super.onDestroy();
	}
}

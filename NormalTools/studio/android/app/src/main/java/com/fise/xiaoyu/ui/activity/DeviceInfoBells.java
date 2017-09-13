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
import com.fise.xiaoyu.config.DBConstant;
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
 * 设备的铃声设置
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
    private UserEntity loginContact;
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

			currentDevice = imService.getContactManager().findDeviceContact(
					currentUserId);
            device = imService.getDeviceManager().findDeviceCard(currentUserId);
			rsp = imService.getDeviceManager().findDeviceCard(currentUserId);
            loginContact = IMLoginManager.instance().getLoginInfo();
			if (rsp == null) {
				return;
			}

			if (currentDevice != null) {
				 if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE) {
					TextView left_text = (TextView) findViewById(R.id.left_text);
					vibreateTv.setText(R.string.mute_mode);
					bells_vibrates.setVisibility(View.GONE);
					left_text.setText("小雨手机");
				}
			}

			initDetailProfile();
		}

		@Override
		public void onServiceDisconnected() {

		}
	};
	private RelativeLayout bells_vibrates;
	private TextView vibreateTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_activity_bell);
		imServiceConnector.connect(this);

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

                if (device.getMasterId() == loginContact.getPeerId()){
                    imService.getDeviceManager().settingOpen(currentUserId, "",
                            SettingType.SETTING_TYPE_BELL_MODE, DBConstant.RINGER_MODE_NORMAL, rsp);
                }else{
                    Utils.showToast(DeviceInfoBells.this , getString(R.string.no_authority_to_operate));
                }

			}
		});


		//如果是　小雨手机静音模式
		RelativeLayout vibrates = (RelativeLayout) findViewById(R.id.vibrates);
		vibreateTv = (TextView) findViewById(R.id.vibrates_text);
		vibrates.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
                if (device.getMasterId() == loginContact.getPeerId()){
					if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE) {

						imService.getDeviceManager().settingOpen(currentUserId, "",
								SettingType.SETTING_TYPE_BELL_MODE, DBConstant.RINGER_MODE_SILENT, rsp);
					}else{
						imService.getDeviceManager().settingOpen(currentUserId, "",
								SettingType.SETTING_TYPE_BELL_MODE, DBConstant.RINGER_MODE_VIBRATE, rsp);
					}

                }else{
                    Utils.showToast(DeviceInfoBells.this , getString(R.string.no_authority_to_operate));
                }

			}
		});


		//响铃 + 振动
		bells_vibrates = (RelativeLayout) findViewById(R.id.bells_vibrates);
		bells_vibrates.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

                if (device.getMasterId() == loginContact.getPeerId()){
                    imService.getDeviceManager().settingOpen(currentUserId, "",
                            SettingType.SETTING_TYPE_BELL_MODE, DBConstant.RINGER_MODE_NORMALORVIBRATE, rsp);
                }else{
                    Utils.showToast(DeviceInfoBells.this , getString(R.string.no_authority_to_operate));
                }

			}
		});
	}

	public void initDetailProfile() {

		if (rsp.getBellMode() == DBConstant.RINGER_MODE_NORMAL) {
			bells_right.setVisibility(View.VISIBLE);
			vibrates_right.setVisibility(View.GONE);
			bells_vibrates_right.setVisibility(View.GONE);

		} else if (rsp.getBellMode() == DBConstant.RINGER_MODE_VIBRATE) {
			bells_right.setVisibility(View.GONE);
			vibrates_right.setVisibility(View.VISIBLE);
			bells_vibrates_right.setVisibility(View.GONE);

		} else if (rsp.getBellMode() == DBConstant.RINGER_MODE_SILENT) {


			if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE) {

				RelativeLayout vibrates = (RelativeLayout) findViewById(R.id.vibrates);
				vibrates.setVisibility(View.VISIBLE);

				bells_right.setVisibility(View.GONE);
				vibrates_right.setVisibility(View.VISIBLE);
				bells_vibrates_right.setVisibility(View.GONE);
			}else{
				bells_right.setVisibility(View.GONE);
				vibrates_right.setVisibility(View.GONE);
				bells_vibrates_right.setVisibility(View.VISIBLE);
			}


		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(DeviceEvent event) {
		switch (event) {

		case USER_INFO_SETTING_DEVICE_SUCCESS:
			DeviceInfoBells.this.finish();
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

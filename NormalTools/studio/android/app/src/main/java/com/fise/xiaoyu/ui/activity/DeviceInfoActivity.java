package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.device.entity.MobilePhoneDeviceEntity;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ClientType;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.protobuf.IMDevice.SettingType;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 *  设备设置界面 包含了设备的信息
 */
public class DeviceInfoActivity extends TTBaseFragmentActivity {

	private DeviceInfoActivity activity;
	private static IMService imService;
	private IMContactManager contactMgr;
	private UserEntity currentUser;
	private int currentUserId;
	private DeviceEntity device;

	private  TextView light_time_text;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			currentUserId = DeviceInfoActivity.this.getIntent().getIntExtra(
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

			device = imService.getDeviceManager().findDeviceCard(currentUserId);
			loginContact = IMLoginManager.instance().getLoginInfo();
			if (device == null) {
				return;
			}

			initDetailProfile();

			/*
			ArrayList<Integer> userIds = new ArrayList<>(1);
			// just single type
			userIds.add(currentUserId);
			imService.getContactManager().reqGetDetaillUsers(userIds);
			*/

		}

		@Override
		public void onServiceDisconnected() {

		}
	};
	private UserEntity loginContact;
	private RelativeLayout bell_mode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_activity_device_info_follow);
		imServiceConnector.connect(this);

		activity = this;
				bell_mode = (RelativeLayout) this
				.findViewById(R.id.bell_mode);
		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceInfoActivity.this.finish();
			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceInfoActivity.this.finish();
			}
		});

//		user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);
//		remarksName = (TextView) findViewById(R.id.remarksName);
//		userName = (TextView) findViewById(R.id.userName);
//		modelName = (TextView) findViewById(R.id.modelName);

	}

	private void initDetailProfile() {
		hideProgressBar();
		// 白名单
		RelativeLayout white_list = (RelativeLayout) findViewById(R.id.white_list);
		white_list.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openWhiteListActivity(DeviceInfoActivity.this,
						currentUserId);
			}
		});

		// 紧急
		RelativeLayout emergency_call = (RelativeLayout) findViewById(R.id.emergency_call);
		emergency_call.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openAlarmListActivity(DeviceInfoActivity.this,
						currentUserId);
			}
		});

		// 报警管理
		RelativeLayout setting_police = (RelativeLayout) findViewById(R.id.setting_police);
		setting_police.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openWarningActivity(DeviceInfoActivity.this,
						currentUserId);

			}
		});

		// 亲情管理
		RelativeLayout setting_family = (RelativeLayout) findViewById(R.id.setting_family);
		setting_family.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openFamilyFollowActivity(DeviceInfoActivity.this,
						currentUserId );

			}
		});


		// 亮屏时间
		LinearLayout light_time = (LinearLayout) findViewById(R.id.light_time);
		light_time.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openDeviceLightTimeActivity(DeviceInfoActivity.this,
						currentUserId);
			}
		});

		light_time_text =  (TextView) findViewById(R.id.light_time_text);



		//上课模式
		RelativeLayout attend_class =  (RelativeLayout) findViewById(R.id.attend_class_mode);
		//爱心提醒
		View sweetRemindLine = findViewById(R.id.sweet_remind_line);
		View attendClassLine = findViewById(R.id.attend_class_line);

		RelativeLayout sweet_remind = (RelativeLayout) findViewById(R.id.sweet_remind);

		View attend_class_line1 = (View) findViewById(R.id.attend_class_line1);
		View attend_class_height = (View) findViewById(R.id.attend_class_height);
		View bell_mode_line = (View) findViewById(R.id.bell_mode_line);
		bell_mode.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openDeviceBellsActivity(DeviceInfoActivity.this,
						currentUserId);


			}
		});

		//工作模式
		RelativeLayout working_mode = (RelativeLayout) findViewById(R.id.working_mode);
		working_mode.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				IMUIHelper.openDeviceWorkingModeActivity(DeviceInfoActivity.this,
						currentUserId);
			}
		});

		sweet_remind.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {

				IMUIHelper.openSweetRemindSettingActivity(DeviceInfoActivity.this,
						currentUserId ,IMDevice.TaskType.TASK_TYPE_LOVE_REMIND.ordinal());

			}
		});

		attend_class.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO
				IMUIHelper.openSweetRemindSettingActivity(DeviceInfoActivity.this,
						currentUserId,IMDevice.TaskType.TASK_TYPE_LESSION_MODE.ordinal());

			}
		});

		final UserEntity loginContact = IMLoginManager.instance().getLoginInfo();

		//计步开关
		RelativeLayout step_counter_layout = (RelativeLayout)findViewById(R.id.step_counter);

		//远程关机
		RelativeLayout  shown_down = (RelativeLayout) findViewById(R.id.shown_down);
		shown_down.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				final FilletDialog myDialog = new FilletDialog(DeviceInfoActivity.this ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE);
				myDialog.setTitle("确定远程关机?");// 设置内容
				myDialog.dialog.show();// 显示
				myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
					@Override
					public void ok() {

						imService.getUserActionManager().UserP2PCommand(loginContact.getPeerId(), device.getDeviceId(),
								IMBaseDefine.SessionType.SESSION_TYPE_SINGLE,
								IMBaseDefine.CommandType.COMMAND_TYPE_DEVICE_SHUTDOWN, "", true);

						myDialog.dialog.dismiss();
					}
				});


			}
		});




		boolean step_counter = false;

		ImageView  step_counter_arrow = (ImageView) findViewById(R.id.step_counter_arrow);
		TextView  step_counter_text  = (TextView) findViewById(R.id.step_counter_text);
		CheckBox  step_counter_checkbox  = (CheckBox) findViewById(R.id.step_counter_checkbox);

		MobilePhoneDeviceEntity mobilePhoneEntity = MobilePhoneDeviceEntity.parseFromDB(device);
		if(device != null && device.getMasterId() != loginContact.getPeerId()){
			step_counter_checkbox.setVisibility(View.GONE);
			step_counter_arrow.setVisibility(View.VISIBLE);
			step_counter_text.setVisibility(View.VISIBLE);

			if (mobilePhoneEntity.getStep_mode() == 1) {
				step_counter_text.setText("打开");
			} else {
				step_counter_text.setText("关闭");
			}

		}else{
			step_counter_checkbox.setVisibility(View.VISIBLE);
			step_counter_arrow.setVisibility(View.GONE);
			step_counter_text.setVisibility(View.GONE);

			if (mobilePhoneEntity.getStep_mode() == 1) {
				step_counter = true;
			}

			step_counter_checkbox.setChecked(step_counter);
			// 绑定监听器
			step_counter_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					// TODO Auto-generated method stub
					int isMode;
					if (arg1 == false) {
						isMode = 0;
					} else {

						isMode = 1;

					}
					imService.getDeviceManager().settingOpen(currentUserId, "",
							SettingType.SETTING_TYPE_PHONE_STEP_MODE, isMode, device);

				}
			});
		}


		// 获取CheckBox实例
		CheckBox listening = (CheckBox) this
				.findViewById(R.id.silent_listening_Checkbox);

		TextView silent_listening_text = (TextView) this
				.findViewById(R.id.silent_listening_text);

		ImageView silent_listening_default_arrow = (ImageView) this
				.findViewById(R.id.silent_listening_default_arrow);


		if (device.getMasterId() != loginContact.getPeerId()) {
			listening.setEnabled(false);
			listening.setVisibility(View.GONE);

			silent_listening_text.setVisibility(View.VISIBLE);
			silent_listening_default_arrow.setVisibility(View.VISIBLE);

		} else {

			listening.setVisibility(View.VISIBLE);

			silent_listening_text.setVisibility(View.GONE);
			silent_listening_default_arrow.setVisibility(View.GONE);


			boolean listen = false;
			listening.setChecked(listen);
		}
		TextView show_bell_text = (TextView) this
				.findViewById(R.id.show_bell_text);
		if (device.getBellMode() == DBConstant.RINGER_MODE_NORMAL) {
			show_bell_text.setText("响铃");
		} else if (device.getBellMode() == DBConstant.RINGER_MODE_SILENT) {
           if(currentUser.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE){
			   show_bell_text.setText("静音");
		   }else{
			   show_bell_text.setText("振动");
		   }


		} else if (device.getBellMode() == DBConstant.RINGER_MODE_NORMALORVIBRATE) {
			show_bell_text.setText("响铃+振动");
		}

		// 绑定监听器
		listening.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				int Silent;
				if (arg1 == false) {
					Silent = 0;
				} else {
					Silent = 1;
				}

				imService.getDeviceManager().settingOpen(currentUserId, "",
						SettingType.SETTING_TYPE_LISTEN_MODE, Silent, device);

			}
		});


		TextView working_mode_text = (TextView) this
				.findViewById(R.id.working_mode_text);

		ImageView working_mode_default_arrow = (ImageView) this
				.findViewById(R.id.working_mode_default_arrow);

		if (device.getMode() == 1){
			working_mode_text.setText("普通模式");
		}else if (device.getMode() == 2){
			working_mode_text.setText("省电模式");
		}else if (device.getMode() == 3){
			working_mode_text.setText("休眠模式");
		}


 	 if (currentUser.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE){

			MobilePhoneDeviceEntity mobilePhoneDeviceEntity = MobilePhoneDeviceEntity.parseFromDB(device);
			light_time_text.setText(mobilePhoneDeviceEntity.getLight()+"S");

			attend_class.setVisibility(View.VISIBLE);
			sweet_remind.setVisibility(View.VISIBLE);
			sweetRemindLine.setVisibility(View.VISIBLE);
			attendClassLine.setVisibility(View.VISIBLE);
			attend_class_line1.setVisibility(View.VISIBLE);
			attend_class_height.setVisibility(View.VISIBLE);
			light_time.setVisibility(View.VISIBLE);

			step_counter_layout.setVisibility(View.VISIBLE);
			shown_down.setVisibility(View.VISIBLE);
			findViewById(R.id.shown_down_line1).setVisibility(View.VISIBLE);

			View  step_counter_line = (View) findViewById(R.id.step_counter_line);
			step_counter_line.setVisibility(View.VISIBLE);
			View  step_counter_line1 = (View) findViewById(R.id.step_counter_line1);
			step_counter_line1.setVisibility(View.GONE);


			View  shown_down_line = (View) findViewById(R.id.shown_down_line);
			shown_down_line.setVisibility(View.VISIBLE);
            findViewById(R.id.up_grade_line).setVisibility(View.GONE);


			Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
			icon_arrow.setBackgroundResource(R.drawable.nav_balck);

			TextView left_text = (TextView) findViewById(R.id.black);
			left_text.setTextColor(getResources().getColor(R.color.device_info_left));

//			bell_mode.setVisibility(View.GONE);
//			bell_mode_line.setVisibility(View.GONE);
			RelativeLayout top_color = (RelativeLayout) findViewById(R.id.top_color);
			top_color.setBackgroundResource(R.color.default_bk);

			TextView black = (TextView) findViewById(R.id.black);
			black.setText("儿童手表");
		}



		TextView firmware_version = (TextView) findViewById(R.id.firmware_version);
		firmware_version.setText(device.getOld_version());


		RelativeLayout firmware_upgrade = (RelativeLayout) findViewById(R.id.firmware_upgrade);
		firmware_upgrade.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				if(device.getNew_version().equals(device.getOld_version())){
					Utils.showToast(DeviceInfoActivity.this,"当前版本为最新版本");
					return;
				}
				if((device.getCharge() == DBConstant.END_CHARGING)&&(currentUser.getBattery()<20)){
					Utils.showToast(DeviceInfoActivity.this,"设备电量低于20%,请接上电源后再次操作");
					return;
				}


				final FilletDialog myDialog = new FilletDialog(DeviceInfoActivity.this ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITH_MESSAGE_FOTA);
				myDialog.setTitle("固件升级");
				myDialog.setMessage("发现新版本为:" + device.getNew_version()  + "\n" + "\n"+ "升级即将开始，此过程预计5-10分钟。期间禁止操作设备，等待完成更新并自动重启后可正常使用");// 设置内容
				myDialog.dialog.show();// 显示
				myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
					@Override
					public void ok() {

						imService.getUserActionManager().UserP2PCommand(loginContact.getPeerId(), device.getDeviceId(),
								IMBaseDefine.SessionType.SESSION_TYPE_SINGLE,
								IMBaseDefine.CommandType.COMMAND_TYPE_DEVICE_VERSION_UPDATE, device.getUpdate_info(), true);

						myDialog.dialog.dismiss();
					}
				});


			}
		});


		RelativeLayout  firmware_upgradeT = (RelativeLayout) findViewById(R.id.firmware_upgrade);
		firmware_upgradeT.setVisibility(View.GONE);
		findViewById(R.id.up_grade_line).setVisibility(View.GONE);
		findViewById(R.id.white_list_short_line).setVisibility(View.GONE);
		findViewById(R.id.white_list_long_line).setVisibility(View.VISIBLE);


		if(device != null && device.getMasterId() != loginContact.getPeerId()){


			shown_down.setVisibility(View.GONE);
			View  shown_down_line = (View) findViewById(R.id.shown_down_line);
			shown_down_line.setVisibility(View.GONE);
			findViewById(R.id.shown_down_line1).setVisibility(View.GONE);


			View  step_counter_line = (View) findViewById(R.id.step_counter_line);
			step_counter_line.setVisibility(View.GONE);

			View  step_counter_line1 = (View) findViewById(R.id.step_counter_line1);
			step_counter_line1.setVisibility(View.VISIBLE);




			findViewById(R.id.speedlimit_line).setVisibility(View.GONE);
		}


		//如果不是小雨手机隐藏
		if (currentUser.getUserType() != ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE){

			View  step_counter_line = (View) findViewById(R.id.step_counter_line);
			step_counter_line.setVisibility(View.GONE);

			View  step_counter_line1 = (View) findViewById(R.id.step_counter_line1);
			step_counter_line1.setVisibility(View.GONE);

			findViewById(R.id.up_grade_line).setVisibility(View.GONE);
			findViewById(R.id.step_counter_line2).setVisibility(View.GONE);
		}

	}

	private void setTextViewContent(int id, String content) {
		TextView textView = (TextView) findViewById(id);
		if (textView == null) {
			return;
		}

		textView.setText(content);
	}

	private void hideProgressBar() {

		ProgressBar progressbar = (ProgressBar) findViewById(R.id.progress_bar);
		progressbar.setVisibility(View.GONE);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(DeviceEvent event) {
		switch (event) {

			case USER_INFO_ADD_DEVICE_FAILED:
				break;
			case USER_INFO_UPDATE_DEVICE_SUCCESS:
			case USER_INFO_UPDATE_INFO_SUCCESS:
			case USER_INFO_SETTING_DEVICE_SUCCESS:
				currentUser = imService.getContactManager().findDeviceContact(
						currentUserId);
				if (currentUser == null) {
					return;
				}
				device = imService.getDeviceManager().findDeviceCard(currentUserId);
				if (device != null) {
					initDetailProfile();

				}
				break;
			case USER_INFO_SETTING_DEVICE_FAILED:
				break;


		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

			case USER_INFO_UPDATE:
				UserEntity entity = imService.getContactManager().findDeviceContact(
						currentUserId);

				if(entity == null){
					entity = imService.getContactManager().findContact(
							currentUserId);
				}
				if (entity != null) {
					currentUser = entity;
					initDetailProfile();
				}
				break;
			case USER_INFO_UPDATE_STAT:
				currentUser = imService.getContactManager().findDeviceContact(
						currentUserId);
				if (currentUser == null) {
					return;
				}
				device = imService.getDeviceManager().findDeviceCard(currentUserId);
				if (device != null) {
					initDetailProfile();
				}
				break;
			case USER_COMMAND_TYPE_DEVICE_VERSION_UPDATE:
			{
				//Utils.showToast(DeviceInfoActivity.this,"固件升级指令发送成功");
			//	Utils.showToast(DeviceInfoActivity.this,"升级已经开始，预计需要5-10分钟。升级过程中请勿断电，禁止手动操作开关机，等待设备升级成功自动重启后可正常使用");
			}
			break;
			case USER_COMMAND_TYPE_DEVICE_SHUTDOWN:
			{
				Utils.showToast(DeviceInfoActivity.this,"关机指令发送成功");
			}
			break;

		}
	}

	@Override
	public void onDestroy() {
				imServiceConnector.disconnect(this);
		super.onDestroy();
	}
}

package com.fise.xw.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R; 
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.protobuf.IMBaseDefine.CommandType;
import com.fise.xw.protobuf.IMBaseDefine.SessionType;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.ui.widget.BillCustomDialog;
import com.fise.xw.ui.widget.BillCustomDialog.Dialogcallback;
import com.fise.xw.ui.widget.IMBaseImageView;
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;


/**
 * 设备的 个人信息设置
 * @author weileiguan
 *
 */
public class DeviceInfoSettingActivity extends TTBaseFragmentActivity {

	private DeviceInfoSettingActivity activity;
	private static IMService imService;
	private IMContactManager contactMgr;
	private UserEntity currentUser;
	private int currentUserId;
	private DeviceEntity rsp;

	private IMBaseImageView user_portrait;
	private TextView xiaoweiName;
	private TextView nickName;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			currentUserId = DeviceInfoSettingActivity.this.getIntent()
					.getIntExtra(IntentConstant.KEY_PEERID, 0);

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

			initDetailProfile();

		}

		@Override
		public void onServiceDisconnected() {

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.device_info_setting);
		imServiceConnector.connect(this);

		activity = this;
		EventBus.getDefault().register(this);

		nickName = (TextView) findViewById(R.id.nickname_text);
		xiaoweiName = (TextView) findViewById(R.id.xiaowei_text);
		user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				if (currentUser.getPhone().equals("")) {
					Toast.makeText(DeviceInfoSettingActivity.this, "请设置设备手机号码",
							Toast.LENGTH_SHORT).show();
				} else {
					DeviceInfoSettingActivity.this.finish();
				}

			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (currentUser.getPhone().equals("")) {
					Toast.makeText(DeviceInfoSettingActivity.this, "请设置设备手机号码",
							Toast.LENGTH_SHORT).show();
				} else {
					DeviceInfoSettingActivity.this.finish();
				}
			}
		});

		RelativeLayout dev_sex_layout = (RelativeLayout) findViewById(R.id.dev_sex_layout);
		dev_sex_layout.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				UserEntity loginContact = IMLoginManager.instance()
						.getLoginInfo();
				if (rsp.getMasterId() == loginContact.getPeerId()) {

					IMUIHelper.openLoginInfoSexActivity(
							DeviceInfoSettingActivity.this,
							DBConstant.SEX_INFO_DEV, currentUserId);

				}

			}
		});

		RelativeLayout setting_device_layaout = (RelativeLayout) findViewById(R.id.setting_device_layaout);
		setting_device_layaout.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openDevicePhonActivity(
						DeviceInfoSettingActivity.this, currentUserId);
			}
		});

		RelativeLayout calls_inquiry_layaout = (RelativeLayout) findViewById(R.id.calls_inquiry_layaout);
		calls_inquiry_layaout.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final BillCustomDialog myDialog = new BillCustomDialog(
						DeviceInfoSettingActivity.this);
				myDialog.setDialogCallback(new Dialogcallback() {
					@Override
					public void dialogdo(String string) {

						imService.getUserActionManager().UserP2PCommand(
								imService.getLoginManager().getLoginId(),
								currentUserId,
								SessionType.SESSION_DEVICE_SINGLE, // SESSION_TYPE_SINGLE
								CommandType.COMMAND_TYPE_DEVICE_BILL,
								myDialog.getText().toString(),true);
					}
				});
				myDialog.show();
			}
		});

		RelativeLayout nickname = (RelativeLayout) findViewById(R.id.nickname);
		nickname.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				UserEntity loginContact = IMLoginManager.instance()
						.getLoginInfo();
				if (rsp.getMasterId() == loginContact.getPeerId()) {

					IMUIHelper.openSetNickNameActivity(
							DeviceInfoSettingActivity.this,
							DBConstant.DEVICE_NICK, currentUser.getPeerId());

				}

			}
		});

		RelativeLayout setting_device_phone = (RelativeLayout) findViewById(R.id.setting_device_phone);
		setting_device_phone.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				UserEntity loginContact = IMLoginManager.instance()
						.getLoginInfo();
				if (rsp.getMasterId() == loginContact.getPeerId()) {

					Intent intent = new Intent(DeviceInfoSettingActivity.this,
							HeadPortraitActivity.class);
					intent.putExtra(IntentConstant.KEY_AVATAR_URL,
							currentUser.getAvatar());
					intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR,
							true);

					intent.putExtra(IntentConstant.KEY_NICK_MODE,
							DBConstant.DEVICE_NICK);
					intent.putExtra(IntentConstant.KEY_PEERID, currentUserId);

					startActivity(intent);

				}

			}
		});
	}

	private void initDetailProfile() {
		hideProgressBar();

		setTextViewContent(R.id.nickname_text, currentUser.getMainName());
		setTextViewContent(R.id.xiaowei_text, currentUser.getRealName());
		setTextViewContent(R.id.setting_device_text, currentUser.getPhone());

		TextView dev_sex_text = (TextView) findViewById(R.id.dev_sex_text);
		if (currentUser.getGender() == DBConstant.SEX_MAILE) {
			dev_sex_text.setText("男");
		} else {
			dev_sex_text.setText("女");
		}
		setTextViewContent(R.id.xiaowei_text, currentUser.getRealName());

		IMBaseImageView user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);
		//user_portrait.setBackgroundResource(R.drawable.default_avatar_default);
		user_portrait.setImageUrl(currentUser.getAvatar()); 
		
		TextView black = (TextView) findViewById(R.id.black);
		TextView phone_tishi_text = (TextView) findViewById(R.id.phone_tishi_text); 
		
		if (currentUser.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE) {
			black.setText("定位卡片机");
			phone_tishi_text.setText("输入定位卡片机中手机卡号码, 才能正常使用静默监听和通话功能");
		}else if (currentUser.getUserType() == ClientType.CLIENT_TYPE_FISE_CAR_VALUE){
			black.setText("电动车");
			phone_tishi_text.setText("输入电动车中手机卡号码, 才能正常使用静默监听和通话功能");
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

	public void onEventMainThread(DeviceEvent event) {
		switch (event) {

		case USER_INFO_ADD_DEVICE_FAILED:
			break;
		case USER_INFO_SETTING_DEVICE_SUCCESS:
		case USER_INFO_ADD_DEVICE_SUCCESS:
		case USER_INFO_UPDATE_DEVICE_SUCCESS:
		case USER_INFO_UPDATE_INFO_SUCCESS:
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);
			if (currentUser == null) {
				return;
			}
			rsp = imService.getDeviceManager().findDeviceCard(currentUserId);
			if (rsp != null) {
				initDetailProfile();
			}
			break;
		case USER_INFO_SETTING_DEVICE_FAILED:
			break;

		}
	}

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_UPDATE:
			UserEntity entity = imService.getContactManager().findContact(
					currentUserId);
			if (entity != null) {
				currentUser = entity;
				initDetailProfile();
			}
			break;
		case USER_COMMAND_TYPE_DEVICE_BELL:
			Toast.makeText(DeviceInfoSettingActivity.this, "话费查询成功",
					Toast.LENGTH_SHORT).show();
			break;

		}
	}

	/**
	 * 监听Back键按下事件,方法2: 注意: 返回值表示:是否能完全处理该事件 在此处返回false,所以会继续传播该事件.
	 * 在具体项目中此处的返回值视情况而定.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (currentUser.getPhone().equals("")) {
				Toast.makeText(DeviceInfoSettingActivity.this, "请设置设备手机号码",
						Toast.LENGTH_SHORT).show();
				return false;
			} else {
				return super.onKeyDown(keyCode, event);
			}
		} else {
			return super.onKeyDown(keyCode, event);
		}
   
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
		super.onDestroy();
	}
}

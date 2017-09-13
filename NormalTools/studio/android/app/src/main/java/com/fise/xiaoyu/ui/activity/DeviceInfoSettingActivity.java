package com.fise.xiaoyu.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
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
import com.fise.xiaoyu.protobuf.IMBaseDefine.ClientType;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * 设备的 个人信息设置
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
	private TextView nickNameText;


	private int year;
	private int month;
	private int day;
    private  UserEntity loginContact;
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
             loginContact = IMLoginManager.instance().getLoginInfo();
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

		nickNameText = (TextView) findViewById(R.id.nickname_text);
		xiaoweiName = (TextView) findViewById(R.id.xiaowei_text);
		user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);


		Calendar mycalendar = Calendar.getInstance();
		year = mycalendar.get(Calendar.YEAR); //获取Calendar对象中的年
		month = mycalendar.get(Calendar.MONTH);//获取Calendar对象中的月
		day = mycalendar.get(Calendar.DAY_OF_MONTH);//获取这个月的第几天


		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (currentUser.getPhone().equals("")) {
					final FilletDialog myDialog = new FilletDialog(DeviceInfoSettingActivity.this  , FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITH_MESSAGE);
					myDialog.setTitle("特别提醒");
					myDialog.setMessage(getString(R.string.without_write_number_notice_text));//设置内容
					myDialog.dialog.show();//显示

					//确认按键回调，按下确认后在此做处理
					myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
						@Override
						public void ok() {
							myDialog.dialog.dismiss();
							DeviceInfoSettingActivity.this.finish();

						}
					});
				}

			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (currentUser.getPhone().equals("")) {

					final FilletDialog myDialog = new FilletDialog(DeviceInfoSettingActivity.this ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITH_MESSAGE);
					myDialog.setTitle("特别提醒");
					myDialog.setMessage(getString(R.string.without_write_number_notice_text));//设置内容
					myDialog.dialog.show();//显示

					//确认按键回调，按下确认后在此做处理
					myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
						@Override
						public void ok() {
							myDialog.dialog.dismiss();
							DeviceInfoSettingActivity.this.finish();

						}
					});
				} else {
					DeviceInfoSettingActivity.this.finish();
				}
			}
		});

		RelativeLayout dev_sex_layout = (RelativeLayout) findViewById(R.id.dev_sex_layout);
		dev_sex_layout.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				if (rsp.getMasterId() == imService.getLoginManager().getLoginId()) {

					IMUIHelper.openLoginInfoSexActivity(
							DeviceInfoSettingActivity.this,
							DBConstant.SEX_INFO_DEV, currentUserId);

				}else{
					Utils.showToast(DeviceInfoSettingActivity.this, getString(R.string.no_authority_to_operate));
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


		RelativeLayout nickname = (RelativeLayout) findViewById(R.id.nickname);
		nickname.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				if (rsp.getMasterId() == imService.getLoginManager().getLoginId()) {
					if(currentUser.getMainName() != null){

					}
					IMUIHelper.openSetNickNameActivity(
							DeviceInfoSettingActivity.this,
							DBConstant.DEVICE_NICK, currentUser.getPeerId());

				}else{
					Utils.showToast(DeviceInfoSettingActivity.this, getString(R.string.no_authority_to_operate));
				}

			}
		});

		RelativeLayout setting_device_phone = (RelativeLayout) findViewById(R.id.setting_device_phone);
		setting_device_phone.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

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
		});
	}




	private void initDetailProfile() {
		hideProgressBar();

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
		user_portrait.setCorner(90);


		TextView black = (TextView) findViewById(R.id.black);
		TextView phone_tishi_text = (TextView) findViewById(R.id.phone_tishi_text);


		RelativeLayout dev_info_height = (RelativeLayout) findViewById(R.id.dev_info_height);
		View dev_info_height_line = (View) findViewById(R.id.dev_info_height_line);

		RelativeLayout dev_info_weight = (RelativeLayout) findViewById(R.id.dev_info_weight);
		View dev_info_weight_line = (View) findViewById(R.id.dev_info_weight_line);

		RelativeLayout dev_info_birthday = (RelativeLayout) findViewById(R.id.dev_info_birthday);
		View dev_info_birthday_line = (View) findViewById(R.id.dev_info_birthday_line);

		nickNameText.setText(currentUser.getMainName());

	   if (currentUser.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE) {
			black.setText("小雨手机");
			phone_tishi_text.setText(" 输入手表中设备电话号码,才能使用回拔监听和通话功能");


			TextView dev_info_height_text = (TextView) findViewById(R.id.dev_info_height_text);
			TextView dev_info_weight_text = (TextView) findViewById(R.id.dev_info_weight_text);
			TextView dev_info_birthday_text = (TextView) findViewById(R.id.dev_info_birthday_text);

			dev_info_height_text.setText(currentUser.getHeight() + " " +"cm");
			dev_info_weight_text.setText(currentUser.getWeight() + " " + "kg");


			if(currentUser.getBirthday()!=null
					&&(!currentUser.getBirthday().equals(""))
					&&(!currentUser.getBirthday().equals("0"))
					&&(Utils.isDataNumeric(currentUser.getBirthday()))){
				dev_info_birthday_text.setText(Utils.timesTwo(currentUser.getBirthday()));//(currentUser.getBirthday()+"");
			}

			if (rsp.getMasterId() == imService.getLoginManager().getLoginId()) {

				//身高
				dev_info_height.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						IMUIHelper.openDeviceHeightActivity(DeviceInfoSettingActivity.this,currentUserId,DBConstant.DEVICE_HEIGHT);
						/*
						final PassDialog myDialog = new PassDialog(DeviceInfoSettingActivity.this ,PassDialog.PASS_DIALOG_TYPE.PASS_DIALOG_WITHOUT_MESSAGE);
						myDialog.setTitle("设置儿童的身高");//设置标题

						myDialog.dialog.show();//显示
						//确认按键回调，按下确认后在此做处理
						myDialog.setMyDialogOnClick(new PassDialog.MyDialogOnClick() {
							@Override
							public void ok() {
								String data = myDialog.getEditText().getText().toString();
								if (Utils.isNumeric(data)) {
									if (currentUser.getHeight()==0) {
										imService.getDeviceManager().settingWhite(
												currentUserId,
												data,
												IMDevice.SettingType.SETTING_TYPE_USER_HEIGHT,
												DBConstant.ADD);
									} else {
										imService.getDeviceManager().settingWhite(
												currentUserId,
												data,
												IMDevice.SettingType.SETTING_TYPE_USER_HEIGHT,
												DBConstant.UPDATE);
									}
								} else {
									Utils.showToast(DeviceInfoSettingActivity.this, "请输入数字");
								}
								myDialog.dialog.dismiss();
							}
						});*/

					}
				});


				//体重
				dev_info_weight.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {

						IMUIHelper.openDeviceHeightActivity(DeviceInfoSettingActivity.this,currentUserId,DBConstant.DEVICE_WEIGHT);

						/*
						final PassDialog myDialog = new PassDialog(DeviceInfoSettingActivity.this,PassDialog.PASS_DIALOG_TYPE.PASS_DIALOG_WITHOUT_MESSAGE);
						myDialog.setTitle("设置儿童的体重");//设置标题
						myDialog.dialog.show();//显示
						//确认按键回调，按下确认后在此做处理
						myDialog.setMyDialogOnClick(new PassDialog.MyDialogOnClick() {
							@Override
							public void ok() {
								String data = myDialog.getEditText().getText().toString();
								if (Utils.isNumeric(data)) {
									if (currentUser.getWeight() == 0) {
										imService.getDeviceManager().settingWhite(
												currentUserId,
												data,
												IMDevice.SettingType.SETTING_TYPE_USER_WEIGHT,
												DBConstant.ADD);
									} else {
										imService.getDeviceManager().settingWhite(
												currentUserId,
												data,
												IMDevice.SettingType.SETTING_TYPE_USER_WEIGHT,
												DBConstant.UPDATE);
									}
								} else {
									Utils.showToast(DeviceInfoSettingActivity.this, "请输入数字");
								}

								myDialog.dialog.dismiss();
							}
						});*/

					}
				});

				//生日设置
				dev_info_birthday.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						IMUIHelper.openDeviceBirthdayActivity(DeviceInfoSettingActivity.this,currentUserId);
					}
				});
			}
		}
	}



	public String TimeData(String time) {
		SimpleDateFormat sdr = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒",
				Locale.CHINA);
		Date date;
		String times = null;
		try {
			date = sdr.parse(time);
			long l = date.getTime();
			String stf = String.valueOf(l);
			times = stf.substring(0, 10);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return times;
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

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
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
				Utils.showToast(DeviceInfoSettingActivity.this, "话费查询成功");
				break;

		}
	}

	/**
	 * 监听Back键按下事件,方法2: 注意: 返回值表示:是否能完全处理该事件 在此处返回false,所以会继续传播该事件.
	 * 在具体项目中此处的返回值视情况而定.
	 */
	@Override
	public boolean onKeyDown(final int keyCode, final  KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if(currentUser != null){
				if (currentUser.getPhone().equals("")) {

					final FilletDialog myDialog = new FilletDialog(DeviceInfoSettingActivity.this ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITH_MESSAGE);
					myDialog.setTitle("特别提醒");
					myDialog.setMessage(getString(R.string.without_write_number_notice_text));//设置内容
					myDialog.dialog.show();//显示

					//确认按键回调，按下确认后在此做处理
					myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
						@Override
						public void ok() {
							myDialog.dialog.dismiss();
							DeviceInfoSettingActivity.this.finish();

						}
					});
			}


				return false;
			}

			else {
				return super.onKeyDown(keyCode, event);
			}
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	@Override
	public void onDestroy() {
				imServiceConnector.disconnect(this);
		super.onDestroy();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();

	}
}

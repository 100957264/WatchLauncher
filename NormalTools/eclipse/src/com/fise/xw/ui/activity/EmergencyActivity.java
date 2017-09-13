package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.entity.WhiteEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMDevice.SettingType;
import com.fise.xw.ui.adapter.WhiteListAdspter;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.ui.widget.WhiteDialog.Dialogcallback;
import com.fise.xw.utils.Logger;
import com.fise.xw.utils.Utils;

import de.greenrobot.event.EventBus;

/**
 * 设置设备的紧急号码界面
 * 
 * @author weileiguan
 * 
 */
public class EmergencyActivity extends TTBaseFragmentActivity {
	private Logger logger = Logger.getLogger(EmergencyActivity.class);
	private IMService imService;
	private int currentUserId;
	List<WhiteEntity> alarmList = new ArrayList<>();
	private AlertDialog myDialog = null;
	private ListView listView = null;
	private WhiteListAdspter adapter;
	private EditText white_phone;
	private DeviceEntity device;
	private UserEntity currentUser;

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

			currentUserId = EmergencyActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);

			if (currentUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);

			device = imService.getDeviceManager().findDeviceCard(currentUserId);
			if (device == null) {
				return;
			}

			if (device != null
					&& device.getMasterId() != imService.getLoginManager()
							.getLoginId()) {
				RelativeLayout add_zhang = (RelativeLayout) findViewById(R.id.add_zhanghu);
				add_zhang.setVisibility(View.GONE);

				View show_zhanghu = (View) findViewById(R.id.show_zhanghu);
				show_zhanghu.setVisibility(View.GONE);
			}

			alarmList = imService.getDeviceManager().getAlarmListContactList(
					currentUserId);
			listView = (ListView) findViewById(R.id.list);

			adapter = new WhiteListAdspter(EmergencyActivity.this, alarmList,
					currentUserId, SettingType.SETTING_TYPE_ALARM_MOBILE, device);
			listView.setAdapter(adapter);
			listView.setOnItemLongClickListener(adapter);

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		imServiceConnector.connect(EmergencyActivity.this);
		EventBus.getDefault().register(this);

		setContentView(R.layout.tt_activity_urgency_list);

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EmergencyActivity.this.finish();
			}
		});

		RelativeLayout add_zhang = (RelativeLayout) findViewById(R.id.add_zhanghu);
		add_zhang.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				UserEntity loginContact = IMLoginManager.instance()
						.getLoginInfo();
				if (device != null
						&& device.getMasterId() != loginContact.getPeerId()) {
					Toast.makeText(EmergencyActivity.this, "你没有权限",
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (alarmList.size() >= DBConstant.SESSION_GROUP_ALARM_NUM) {
					Toast.makeText(
							EmergencyActivity.this,
							"紧急号码最多" + DBConstant.SESSION_GROUP_ALARM_NUM + "个",
							Toast.LENGTH_SHORT).show();
					return;
				}

				myDialog = new AlertDialog.Builder(EmergencyActivity.this)
						.create();
				myDialog.show();
				myDialog.getWindow().setContentView(R.layout.white_list_dialog);
				myDialog.setCanceledOnTouchOutside(false);

				myDialog.getWindow().findViewById(R.id.button_cancel)
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								myDialog.dismiss();
							}
						});

				white_phone = (EditText) myDialog.getWindow().findViewById(
						R.id.white_phone);

				myDialog.getWindow().findViewById(R.id.button_que)
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {

								boolean isPhone = Utils.isMobileNO(white_phone
										.getText().toString());
								if (isPhone == false) {
									Toast.makeText(EmergencyActivity.this,
											"输入的号码不正确", Toast.LENGTH_SHORT)
											.show();

									return;
								}

								boolean isExisted = false;
								alarmList = imService.getDeviceManager()
										.getAlarmListContactList(currentUserId);
								for (int i = 0; i < alarmList.size(); i++) {
									if (alarmList
											.get(i)
											.getPhone()
											.equals(white_phone.getText()
													.toString())) {
										isExisted = true;
										break;
									}
								}

								if (isExisted) {
									Toast.makeText(EmergencyActivity.this,
											"您的输入手机号码已经存在", Toast.LENGTH_SHORT)
											.show();
									return;
								}

								imService.getDeviceManager().settingWhite(
										currentUserId,
										white_phone.getText().toString(),
										SettingType.SETTING_TYPE_ALARM_MOBILE,
										DBConstant.ADD);
								myDialog.dismiss();

							}
						});

				myDialog.getWindow()
						.clearFlags(
								WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
										| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				myDialog.getWindow().setSoftInputMode(
						WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			}
		});

	}

	/**
	 * 设置mydialog需要处理的事情
	 */
	Dialogcallback dialogcallback = new Dialogcallback() {
		@Override
		public void dialogdo(String string) {

		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(EmergencyActivity.this);
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

		case USER_INFO_DELETE_ALARM_SUCCESS:
			Toast.makeText(EmergencyActivity.this, "求助号码删除成功",
					Toast.LENGTH_SHORT).show();
			updateEmergencyList();
			break;

		case USER_INFO_SETTING_DEVICE_SUCCESS:
			renderEmergencyList();
			break;
		case USER_INFO_SETTING_DEVICE_FAILED:
			Toast.makeText(EmergencyActivity.this, "求助号码设置失败",
					Toast.LENGTH_SHORT).show();
			break;
		case USER_INFO_UPDATE_INFO_SUCCESS:
			renderEmergencyList();
			break;
			 
		}
	}

	public void renderEmergencyList() {
		alarmList = imService.getDeviceManager().getAlarmListContactList(
				currentUserId);

		// 没有任何的联系人数据
		if (alarmList.size() <= 0) {
			return;
		}
		adapter.putDeviceList(alarmList);
	}

	public void updateEmergencyList() {
		alarmList = imService.getDeviceManager().getAlarmListContactList(
				currentUserId);

		adapter.updateDeviceList(alarmList);
	}

}

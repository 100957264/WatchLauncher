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
 * 白名单界面
 * 
 * @author weileiguan
 * 
 */
public class WhiteListActivity extends TTBaseFragmentActivity {
	private Logger logger = Logger.getLogger(WhiteListActivity.class);
	private IMService imService;
	private int currentUserId;
	List<WhiteEntity> whiteList = new ArrayList<>();
	private AlertDialog myDialog = null;
	private ListView listView = null;
	private WhiteListAdspter adapter;
	private EditText white_phone;
	private DeviceEntity device;
	private UserEntity loginContact;
	private UserEntity currentDevice;

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

			currentUserId = WhiteListActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);

			if (currentUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}

			device = imService.getDeviceManager().findDeviceCard(currentUserId);
			if (device == null) {
				return;
			}
			currentDevice = imService.getContactManager().findDeviceContact(
					currentUserId);

			loginContact = IMLoginManager.instance().getLoginInfo();

			whiteList = imService.getDeviceManager().getWhiteListContactList(
					currentUserId);
			listView = (ListView) findViewById(R.id.list);

			adapter = new WhiteListAdspter(WhiteListActivity.this, whiteList,
					currentUserId, SettingType.SETTING_TYPE_ALLOW_MOBILE, device);
			listView.setAdapter(adapter);
			listView.setOnItemLongClickListener(adapter);

			if (device != null && device.getMasterId() != loginContact.getPeerId()) {
				RelativeLayout add_zhang = (RelativeLayout) findViewById(R.id.add_zhanghu);
				add_zhang.setVisibility(View.GONE);

				View show_zhanghu = (View) findViewById(R.id.show_zhanghu);
				show_zhanghu.setVisibility(View.GONE);
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		imServiceConnector.connect(WhiteListActivity.this);
		EventBus.getDefault().register(this);

		setContentView(R.layout.tt_activity_white_list);

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				WhiteListActivity.this.finish();
			}
		});

		RelativeLayout add_zhang = (RelativeLayout) findViewById(R.id.add_zhanghu);
		add_zhang.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (device != null
						&& device.getMasterId() != loginContact.getPeerId()) {
					Toast.makeText(WhiteListActivity.this, "你没有权限",
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (whiteList.size() >= DBConstant.SESSION_GROUP_WHILTE_NUM) {
					Toast.makeText(
							WhiteListActivity.this,
							"白名单号码最多" + DBConstant.SESSION_GROUP_WHILTE_NUM
									+ "个", Toast.LENGTH_SHORT).show();
					return;
				}

				myDialog = new AlertDialog.Builder(WhiteListActivity.this)
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
									Toast.makeText(WhiteListActivity.this,
											"输入的号码不正确", Toast.LENGTH_SHORT)
											.show();

									return;
								}

								boolean isExisted = false;
								whiteList = imService.getDeviceManager()
										.getWhiteListContactList(currentUserId);
								for (int i = 0; i < whiteList.size(); i++) {
									if (whiteList
											.get(i)
											.getPhone()
											.equals(white_phone.getText()
													.toString())) {
										isExisted = true;
										break;
									}
								}

								if (isExisted) {
									Toast.makeText(WhiteListActivity.this,
											"您的输入手机号码已经存在", Toast.LENGTH_SHORT)
											.show();
									return;
								}

								imService.getDeviceManager().settingWhite(
										currentUserId,
										white_phone.getText().toString(),
										SettingType.SETTING_TYPE_ALLOW_MOBILE,
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

		imServiceConnector.disconnect(WhiteListActivity.this);
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
			renderAuthList();
			break;
		case USER_INFO_SETTING_DEVICE_FAILED:
			Toast.makeText(WhiteListActivity.this, "亲情号码设置失败",
					Toast.LENGTH_SHORT).show();
			break;
		case USER_INFO_DELETE_WHITE_SUCCESS:
			Toast.makeText(WhiteListActivity.this, "亲情号码删除成功",
					Toast.LENGTH_SHORT).show();
			renderAuthList();
			break;
		case USER_INFO_UPDATE_INFO_SUCCESS: 
			renderAuthList();
			break;
			 
		}
	}

	public void renderAuthList() {
		whiteList = imService.getDeviceManager().getWhiteListContactList(
				currentUserId);

		// 没有任何的联系人数据
//		if (whiteList.size() <= 0) {
//			return;
//		}
		adapter.putDeviceList(whiteList);
	}

	 

}

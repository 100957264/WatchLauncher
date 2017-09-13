package com.fise.xw.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;

public class DeviceActivity extends TTBaseFragmentActivity {

	private DeviceActivity activity;
	private static IMService imService;
	private IMContactManager contactMgr;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_activity_device_info);
		imServiceConnector.connect(this);

		activity = this;
		EventBus.getDefault().register(this);
		
		
		TextView weiwang = (TextView) findViewById(R.id.weiwang);
		weiwang.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceActivity.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceActivity.this.finish();
			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceActivity.this.finish();
			}
		});

		Button addDevice = (Button) findViewById(R.id.new_device_add);
		addDevice.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openAddDeviceActivity(DeviceActivity.this); 
			}
		});

		EditText text = (EditText) findViewById(R.id.search_phone);
		text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openSearchFriendActivity(DeviceActivity.this,DBConstant.SEACHDEVICE);
			}
		});
		
		
		
		RelativeLayout telephone_Page = (RelativeLayout) findViewById(R.id.telephone_Page);
		telephone_Page.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openDeviceActivity(DeviceActivity.this,DBConstant.TELEPHONE);
			}
		});
		
		RelativeLayout camera_Page = (RelativeLayout) findViewById(R.id.camera_Page);
		camera_Page.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openDeviceActivity(DeviceActivity.this,DBConstant.CAMERA);
			}
		});
		
		
		RelativeLayout intercom_Page = (RelativeLayout) findViewById(R.id.intercom_Page);
		intercom_Page.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openDeviceActivity(DeviceActivity.this,DBConstant.INTERCOM);
			}
		});
		

	} 

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
		super.onDestroy();
	}
}

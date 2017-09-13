package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.DeviceEntity; 
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.adapter.BlackListAdspter;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;


/**
 *  黑名单
 * @author weileiguan
 *
 */
public class BlackListActivity extends TTBaseFragmentActivity {

	private BlackListActivity activity;
	private static IMService imService;
	private UserEntity currentUser;
	private int currentUserId;
	private DeviceEntity rsp;
	private BlackListAdspter adapter;
	private ListView listView = null;
	private List<UserEntity> userList;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			initDetailProfile();
			userList = imService.getContactManager().getBlackList();
			adapter = new BlackListAdspter(activity, userList, imService);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					IMUIHelper.openBlackListInfoActivity(
							BlackListActivity.this, userList.get(arg2)
									.getPeerId());

				}
			});

		}

		@Override
		public void onServiceDisconnected() {

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_activity_black_list);

		listView = (ListView) findViewById(R.id.list);
		imServiceConnector.connect(this);

		activity = this;
		EventBus.getDefault().register(this);

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				BlackListActivity.this.finish();
			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				BlackListActivity.this.finish();
			}
		});

	}

	private void initDetailProfile() {
		hideProgressBar();
	}

	private void hideProgressBar() {

		ProgressBar progressbar = (ProgressBar) findViewById(R.id.progress_bar);
		progressbar.setVisibility(View.GONE);
	}

	public void onEventMainThread(DeviceEvent event) {
		switch (event) {
  
		case USER_INFO_SETTING_DEVICE_SUCCESS:
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
		case USER_BLACKLIST_FAIL:
			break;

		case USER_BLACKLIST_SUCCESS:
		case USER_BLACKLIST_DEL_SUCCESS:
			userList = imService.getContactManager().getBlackList();
			adapter.putBlackList(userList);
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

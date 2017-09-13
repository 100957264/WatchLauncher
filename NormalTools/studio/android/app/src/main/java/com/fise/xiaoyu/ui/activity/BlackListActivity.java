package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.adapter.BlackListAdspter;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.utils.IMUIHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


/**
 *  黑名单
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

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(DeviceEvent event) {
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
				imServiceConnector.disconnect(this);
		super.onDestroy();
	}
}

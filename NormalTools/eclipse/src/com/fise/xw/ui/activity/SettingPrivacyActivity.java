package com.fise.xw.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xw.ui.base.TTBaseFragmentActivity;

import de.greenrobot.event.EventBus;


/**
 *  个人隐私界面
 * @author weileiguan
 *
 */
public class SettingPrivacyActivity extends TTBaseFragmentActivity {

	private static IMService imService;
	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			initView();

		}

		@Override
		public void onServiceDisconnected() {

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_privacy);
		imServiceConnector.connect(this);
		EventBus.getDefault().register(this);

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				SettingPrivacyActivity.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				SettingPrivacyActivity.this.finish();
			}
		});

		RelativeLayout hei_mingdan = (RelativeLayout) findViewById(R.id.hei_mingdan);
		hei_mingdan.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(SettingPrivacyActivity.this,
						BlackListActivity.class);
				SettingPrivacyActivity.this.startActivity(intent);
			}
		});

	}

	private void initView() {

		final UserEntity Login = imService.getLoginManager().getLoginInfo();
		
		
		
		// 获取CheckBox实例
		CheckBox forbid_checkbox = (CheckBox) this
				.findViewById(R.id.forbid_checkbox);

		if (Login.getFriendNeedAuth() == 1) {
			forbid_checkbox.setChecked(true);
		} else {
			forbid_checkbox.setChecked(false);
		}

		// 绑定监听器
		forbid_checkbox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						// TODO Auto-generated method stub

						if (arg1) {
							imService
									.getContactManager()
									.ChangeUserInfo(
											Login.getPeerId(),
											ChangeDataType.CHANGE_USERSET_FRIEND_NEED_AUTH,
											"1");

						} else {
							imService
									.getContactManager()
									.ChangeUserInfo(
											Login.getPeerId(),
											ChangeDataType.CHANGE_USERSET_FRIEND_NEED_AUTH,
											"0");
						}
					}
				});

		// 获取CheckBox实例
		CheckBox search_checkbox = (CheckBox) this
				.findViewById(R.id.search_checkbox);

		if (Login.getSearchAllow() == 1) {
			search_checkbox.setChecked(true);
		} else {
			search_checkbox.setChecked(false);
		} 

		// 绑定监听器
		search_checkbox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						// TODO Auto-generated method stub
						if (arg1) {
							imService
									.getContactManager()
									.ChangeUserInfo(
											Login.getPeerId(),
											ChangeDataType.CHANGE_USERSET_ALLOW_SEARCH_FIND,
											"1");

						} else {
							imService
									.getContactManager()
									.ChangeUserInfo(
											Login.getPeerId(),
											ChangeDataType.CHANGE_USERSET_ALLOW_SEARCH_FIND,
											"0");
						}
					}
				});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);

		super.onDestroy();
	}

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			initView();
			break;

		}
	}

}

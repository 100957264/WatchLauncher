package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xiaoyu.ui.base.TTBaseActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 *  账户保护设置界面
 */
public class AccountProtectionActivity extends TTBaseActivity {

	private IMService imService;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onServiceDisconnected() {
		}

		@Override
		public void onIMServiceConnected() {
			logger.d("login#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			try {
				do {
					if (imService == null) {
						// 后台服务启动链接失败
						break;
					}

					initView();
					return;
				} while (false);

				// 异常分支都会执行这个
				// handleNoLoginIdentity();
			} catch (Exception e) {
				// 任何未知的异常
				logger.w("loadIdentity failed");
				// handleNoLoginIdentity();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_protection);

		imServiceConnector.connect(AccountProtectionActivity.this);

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				AccountProtectionActivity.this.finish();
			}
		});
		 

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				AccountProtectionActivity.this.finish();
			}
		});

	}

	void initView() {

		final UserEntity Login = imService.getLoginManager().getLoginInfo();
		// 获取CheckBox实例
		CheckBox protection_checkbox = (CheckBox) this
				.findViewById(R.id.protection_checkbox);

		if (Login.getLoginSafeSwitch() == 1) {
			protection_checkbox.setChecked(true);
		} else {
			protection_checkbox.setChecked(false);
		}
		// 绑定监听器
		protection_checkbox
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
											ChangeDataType.CHANGE_USERSET_SAFE_LOGIN_SWITCH,
											"1");

						} else {
							imService
									.getContactManager()
									.ChangeUserInfo(
											Login.getPeerId(),
											ChangeDataType.CHANGE_USERSET_SAFE_LOGIN_SWITCH,
											"0");
						}

					}
				});

	}
	
	  @Subscribe(threadMode = ThreadMode.MAIN)
	  public void onMessageEvent(UserInfoEvent event){
	        switch (event){ 
	         
	                
	        }
	    }
	     

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		imServiceConnector.disconnect(AccountProtectionActivity.this);
			}

	

}
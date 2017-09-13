package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;


/**
 * 个性签名页面 (好友可以查看个人签名)
 */
public class UserInfoSigned extends TTBaseActivity {
	private static IMService imService;
	private IMLoginManager imLoginManager = IMLoginManager.instance();
	private TextView signed_name; 
	private String userSigned;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			userSigned = UserInfoSigned.this.getIntent().getStringExtra(IntentConstant.KEY_PEERID_SIGNED);
 

			signed_name = (TextView) findViewById(R.id.signed_name);
			signed_name.setText(userSigned);
		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_activity_info_signature); // tt_activity_set_signature
		imServiceConnector.connect(this); 
		
		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				UserInfoSigned.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				UserInfoSigned.this.finish();
			}
		});

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub 
		imServiceConnector.disconnect(this);
		super.onDestroy();
	}

}

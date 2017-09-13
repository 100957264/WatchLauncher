package com.fise.xw.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.base.TTBaseFragmentActivity;

import de.greenrobot.event.EventBus;


/**
 * 个性签名页面 (好友可以查看个人签名)
 * @author weileiguan
 *
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

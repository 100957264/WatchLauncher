package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine.CommentType;
import com.fise.xiaoyu.ui.base.TTBaseActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 *  好友信息设置备注界面
 */
public class UserInfoRemarks extends TTBaseActivity {
	private static IMService imService;
	private UserEntity currentUser;
	private int currentUserId;

	private IMLoginManager imLoginManager = IMLoginManager.instance();
	private ImageView female_right;
	private ImageView man_right;
	private EditText nick_name;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			currentUserId = UserInfoRemarks.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);

			if (currentUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}
			currentUser = imService.getContactManager().findContact(
					currentUserId);

			nick_name = (EditText) findViewById(R.id.nick_name);

			if (currentUser.getComment().equals("")) {
				nick_name.setText(currentUser.getMainName());
			} else {
				nick_name.setText(currentUser.getComment());

			}

		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_activity_set_remarks);
		imServiceConnector.connect(this);

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				UserInfoRemarks.this.finish();
			}
		});

		TextView right_text = (TextView) findViewById(R.id.right_text);
		right_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//
				UserEntity user = IMLoginManager.instance().getLoginInfo();
				String data = nick_name.getText().toString();


				if (currentUser.getComment().equals("")) {

					IMContactManager.instance().ChangeUserInfo(
							user.getPeerId(), currentUser.getPeerId(), 1,
							CommentType.COMMENT_TYPE_SECONED_NAME, data);

				} else {

					
					IMContactManager.instance().ChangeUserInfo(
							user.getPeerId(), currentUser.getPeerId(), 2,
							CommentType.COMMENT_TYPE_SECONED_NAME, data);
				}

				// IMContactManager.instance().ChangeUserInfo(user.getPeerId(),ChangeDataType.CHANGE_USERINFO_NICK,data);
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				UserInfoRemarks.this.finish();
			}
		});

	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			break;
		case USER_INFO_UPDATE:
			UserInfoRemarks.this.finish();
			break;

		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

				imServiceConnector.disconnect(this);
	}

}

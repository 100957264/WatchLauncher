package com.fise.xw.ui.activity;

import android.os.Bundle;
import android.util.Log;
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
import com.fise.xw.protobuf.IMBaseDefine.CommentType;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.base.TTBaseFragmentActivity;

import de.greenrobot.event.EventBus;



/**
 *  好友信息设置备注界面
 * @author weileiguan
 *
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
		EventBus.getDefault().register(this);

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
				// if(data.equals(""))
				// {
				// Toast.makeText(UserInfoRemarks.this, "请输入备注",
				// Toast.LENGTH_SHORT).show();
				// }else{
				// if(currentUser.getComment().equals(""))
				// {
				// IMContactManager.instance().ChangeUserInfo(user.getPeerId(),currentUser.getPeerId(),1,CommentType.COMMENT_TYPE_SECONED_NAME,data);
				// }else{
				// IMContactManager.instance().ChangeUserInfo(user.getPeerId(),currentUser.getPeerId(),2,CommentType.COMMENT_TYPE_SECONED_NAME,data);
				// }
				// //ChangeUserInfo(int from_user ,final int dest_user,int
				// set_status,final CommentType comment_type,final String
				// value){
				// }

				if (currentUser.getComment().equals("")) {
					
					/*
					if (data.equals("")) {
						 Toast.makeText(UserInfoRemarks.this, "请输入备注",Toast.LENGTH_SHORT).show();
					} else {
						IMContactManager.instance().ChangeUserInfo(
								user.getPeerId(), currentUser.getPeerId(), 1,
								CommentType.COMMENT_TYPE_SECONED_NAME, data);
					}
					*/
					IMContactManager.instance().ChangeUserInfo(
							user.getPeerId(), currentUser.getPeerId(), 1,
							CommentType.COMMENT_TYPE_SECONED_NAME, data);

				} else {

					/*
					if (data.equals("")) {
						 Toast.makeText(UserInfoRemarks.this, "请输入备注",Toast.LENGTH_SHORT).show();
					} else {
						IMContactManager.instance().ChangeUserInfo(
								user.getPeerId(), currentUser.getPeerId(), 2,
								CommentType.COMMENT_TYPE_SECONED_NAME, data);

					}*/
					
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

	public void onEventMainThread(UserInfoEvent event) {
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

		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
	}

}

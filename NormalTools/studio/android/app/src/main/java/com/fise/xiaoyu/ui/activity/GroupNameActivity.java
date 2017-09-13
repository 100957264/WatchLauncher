package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.GroupEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 群名称界面
 */
@SuppressLint("NewApi")
public class GroupNameActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(GroupNameActivity.class);
	private Handler uiHandler = new Handler();
	private IMService imService;
	private String curSessionKey;
	private EditText group_name;
	private PeerEntity peerEntity;

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

			curSessionKey = GroupNameActivity.this.getIntent().getStringExtra(
					IntentConstant.KEY_SESSION_KEY);
			if (TextUtils.isEmpty(curSessionKey)) {
				logger.e("groupmgr#getSessionInfoFromIntent failed");
				return;
			}
			peerEntity = imService.getSessionManager().findPeerEntity(
					curSessionKey);
			if (peerEntity == null) {
				logger.e("groupmgr#findPeerEntity failed,sessionKey:%s",
						curSessionKey);
				return;
			}

			group_name = (EditText) findViewById(R.id.group_name);

			switch (peerEntity.getType()) {
			case DBConstant.SESSION_TYPE_GROUP: {
				final GroupEntity groupEntity = (GroupEntity) peerEntity;
				group_name.setText(groupEntity.getMainName());

				Button conf = (Button) findViewById(R.id.icon_user_info);
				conf.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						imService.getGroupManager().modifyChangeGroupMember(
								groupEntity.getPeerId(),
								ChangeDataType.CHANGE_GROUP_UPDATE_GROUPNAME,
								group_name.getText().toString(), groupEntity);
					}
				});
			}
				break;
			} 
		}
	};
             
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		imServiceConnector.connect(GroupNameActivity.this);

		setContentView(R.layout.tt_activity_group_name);

		ImageView left_btn_tt = (ImageView) findViewById(R.id.left_btn_tt);
		left_btn_tt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				GroupNameActivity.this.finish();
			}
		});

		TextView left_txt_tt = (TextView) findViewById(R.id.left_txt_tt);
		left_txt_tt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				GroupNameActivity.this.finish();
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(GroupNameActivity.this);
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

	/** 事件驱动通知 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(GroupEvent event) {
		switch (event.getEvent()) {

		case CHANGE_GROUP_MODIFY_FAIL: {
			Utils.showToast(GroupNameActivity.this, "修改失败");
		}
			break;
		case CHANGE_GROUP_MODIFY_TIMEOUT: {
			Utils.showToast(GroupNameActivity.this, "修改超时");
		}
			break;
		case CHANGE_GROUP_MODIFY_SUCCESS: {
			Utils.showToast(GroupNameActivity.this, "修改成功");
			GroupNameActivity.this.finish();
		}
			break;
		}
	}

}

package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.GroupNickEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.GroupEvent;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.adapter.GroupManagerAdapter;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 * 查看全部成员列表
 */
@SuppressLint("NewApi")
public class GroupMemberActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(GroupMemberActivity.class);
	private Handler uiHandler = new Handler();
	private IMService imService;
	private GroupEntity groupEntity;
	private int currentGroupId;
	private GridView gridView;
	private GroupManagerAdapter adapter;
	private GroupNickEntity entity;

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

					groupEntity = imService.getGroupManager().findGroup(currentGroupId);
					if(groupEntity!=null){
						TextView base_activity_title = (TextView) findViewById(R.id.base_activity_title);
						base_activity_title.setText("聊天成员" +"(" + groupEntity.getlistGroupMemberIds().size()+")");
						entity = imService.getGroupManager().findGroupNick(
								groupEntity.getPeerId(), imService.getLoginManager().getLoginId());


						gridView = (GridView) findViewById(R.id.group_manager_grid);
						gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));// 去掉点击时的黄色背影
						gridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
								.getInstance(), true, true));

						adapter = new GroupManagerAdapter(GroupMemberActivity.this, imService, groupEntity,
								entity,true);

						gridView.setAdapter(adapter);

					}

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

	/** 事件驱动通知 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(GroupEvent event) {
		switch (event.getEvent()) {
			case CHANGE_GROUP_MEMBER_SUCCESS: {
				groupEntity = imService.getGroupManager().findGroup(currentGroupId);
				onMemberChangeSuccess(event);
			}
			break;
		}
	}
	private void onMemberChangeSuccess(GroupEvent event) {
		int groupId = event.getGroupEntity().getPeerId();
		if (groupId != groupEntity.getPeerId()) {
			return;
		}
		List<Integer> changeList = event.getChangeList();
		if (changeList == null || changeList.size() <= 0) {
			return;
		}
		int changeType = event.getChangeType();

		switch (changeType) {
			case DBConstant.GROUP_MODIFY_TYPE_ADD:
				ArrayList<UserEntity> newList = new ArrayList<>();
				for (Integer userId : changeList) {
					UserEntity userEntity = imService.getContactManager()
							.findContact(userId);
					if (userEntity != null) {
						newList.add(userEntity);
					}
				}
				adapter.add(newList);
				break;
			case DBConstant.GROUP_MODIFY_TYPE_DEL:
				for (Integer userId : changeList) {
					adapter.removeById(userId);
				}
				break;
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		imServiceConnector.connect(GroupMemberActivity.this);


		currentGroupId = getIntent().getIntExtra(
				IntentConstant.KEY_PEERID, 0);
		setContentView(R.layout.tt_activity_group_member);
		ImageView left_btn_tt = (ImageView) findViewById(R.id.left_btn_tt);
		left_btn_tt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				GroupMemberActivity.this.finish();
			}
		});

		TextView left_txt_tt = (TextView) findViewById(R.id.left_txt_tt);
		left_txt_tt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				GroupMemberActivity.this.finish();
			}
		});

		
		

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(GroupMemberActivity.this);
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

	/**
	 * ----------------------------event 事件驱动----------------------------
	 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(LoginEvent event) {
		switch (event) {
		case LOCAL_LOGIN_SUCCESS:
		case LOGIN_OK:
			break;

		}
	}

	 
}

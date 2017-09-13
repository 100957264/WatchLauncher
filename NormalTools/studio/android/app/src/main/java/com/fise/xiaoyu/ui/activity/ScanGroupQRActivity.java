package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.event.GroupEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMGroupManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.IMGroupAvatar;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.ScreenUtil;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 *    扫描二维进群界面
 */
public class ScanGroupQRActivity extends TTBaseActivity {
	private static IMService imService;
	private GroupEntity groupInfo;

	private int curGroupId;
	/*
	 * private int version; private int type;
	 */
	private int status;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			curGroupId = ScanGroupQRActivity.this.getIntent().getIntExtra(
					IntentConstant.QR_GROUP_ID, 0); 
			groupInfo = imService.getGroupManager().findGroup(curGroupId);
			 
			if (groupInfo == null){
				groupInfo = imService.getGroupManager().findQRGroup(curGroupId); 
				 
			}
 
			 
			if (groupInfo == null) {  
				IMGroupManager.instance().reqGroupQrDetailInfo(curGroupId);

			} else {
				status = groupInfo.getSave(); 
				initDetailProfile();
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

		setContentView(R.layout.scan_qr_group_activity);
		imServiceConnector.connect(this);

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				ScanGroupQRActivity.this.finish();
			}
		});
		//
		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				ScanGroupQRActivity.this.finish();
			}
		});

	}

	public void initDetailProfile() {

		ProgressBar progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
		progress_bar.setVisibility(View.GONE);

		IMGroupAvatar groupImage = (IMGroupAvatar) this
				.findViewById(R.id.contact_portrait);
		TextView groupName = (TextView) this.findViewById(R.id.group_name);

		String showName="";
		List<String> avatarUrlList = new ArrayList<>();
		Set<Integer> userIds = groupInfo.getlistGroupMemberIds();
		int i = 0;
		for (Integer buddyId : userIds) {
			UserEntity entity = imService.getContactManager().findContact(
					buddyId);
			if (entity == null) {
				// logger.d("已经离职。userId:%d", buddyId);
				continue;
			} 
			
			showName = showName +" " + entity.getMainName();
			if (i > DBConstant.GROUP_AVATAR_NUM-1) {   //默认显示头的数量
				break;
			}
			
			avatarUrlList.add(entity.getAvatar());
			i++;
		}
		setGroupAvatar(groupImage, avatarUrlList);
		groupName.setText(showName);
		//groupName.setText(groupInfo.getMainName());
		groupName.setVisibility(View.VISIBLE);
		
		TextView group_num = (TextView) this.findViewById(R.id.group_num);
		group_num.setText("(共" + userIds.size()+"人"+ ")");
		
		
		Button chat_btn = (Button) this.findViewById(R.id.chat_btn);

		
		final GroupEntity groupInfoTemp = imService.getGroupManager().findGroup(curGroupId);


		if(groupInfoTemp == null){
			chat_btn.setText("确定加入" + groupInfo.getMainName());
		}else{
			if(groupInfoTemp.getSave() == DBConstant.GROUP_MEMBER_STATUS_EXIT){
				chat_btn.setText("确定加入" + groupInfo.getMainName());
			}else{
				chat_btn.setText("开始群聊" );
			}

		}


		if (groupInfo.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
			chat_btn.setVisibility(View.GONE);
		}

		chat_btn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//if (status == DBConstant.GROUP_MEMBER_STATUS_SAVE
				//		|| status == DBConstant.GROUP_MEMBER_STATUS_TEMP) {
				if(groupInfoTemp != null
						&&(groupInfoTemp.getSave() != DBConstant.GROUP_MEMBER_STATUS_EXIT)){

					IMUIHelper.openChatActivity(ScanGroupQRActivity.this,
							groupInfo.getSessionKey());

				} else {
					// reqGroupDetailInfo();
					Set<Integer> addMemberlist = new HashSet<>();
					addMemberlist.add(imService.getLoginManager().getLoginId());
					imService.getGroupManager().reqAddGroupMember(
							groupInfo.getPeerId(), addMemberlist,ChangeDataType.CHANGE_GROUP_USER_ADD_BY_SCAN);

				}
			}
		});

	}

	/**
	 * 与search 有公用的地方，可以抽取IMUIHelper 设置群头像
	 * 
	 * @param avatar
	 * @param avatarUrlList
	 */
	private void setGroupAvatar(IMGroupAvatar avatar, List<String> avatarUrlList) {
		try {
			avatar.setViewSize(ScreenUtil.instance(ScanGroupQRActivity.this)
					.dip2px(48));
			avatar.setChildCorner(2);
			avatar.setAvatarUrlAppend(SysConstant.AVATAR_APPEND_32);
			avatar.setParentPadding(3);
			avatar.setAvatarUrls((ArrayList<String>) avatarUrlList);
		} catch (Exception e) {
			// logger.e(e.toString());
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(GroupEvent event) {
		switch (event.getEvent()) {

		case GROUP_INFO_UPDATED: 
			status = DBConstant.GROUP_MEMBER_STATUS_EXIT;
			groupInfo = imService.getGroupManager().findQRGroup(curGroupId);
			if(groupInfo == null){
				groupInfo = imService.getGroupManager().findGroup(curGroupId);
			}
			
			if(groupInfo != null){ 
				initDetailProfile();
			}
			
			break;
			
		case CHANGE_GROUP_MEMBER_SUCCESS:
			Utils.showToast(ScanGroupQRActivity.this, "加入群成功");
			ScanGroupQRActivity.this.finish();
			break;	
			 
		case CHANGE_GROUP_MEMBER_FAIL:
			Utils.showToast(ScanGroupQRActivity.this, "加入群失败");
			  
			break;	
			   
		}
	}
	 
	
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {
		case USER_INFO_UPDATE: {
			groupInfo = imService.getGroupManager().findQRGroup(curGroupId);
			if(groupInfo == null){
				groupInfo = imService.getGroupManager().findGroup(curGroupId);
			}
			
			if(groupInfo != null){ 
				initDetailProfile();
			}
		}

			break;

		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
	    		imServiceConnector.disconnect(this);
		super.onDestroy();
	}

	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
}

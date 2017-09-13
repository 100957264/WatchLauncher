package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
 
import com.fise.xw.R;
import com.fise.xw.DB.entity.GroupEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.SysConstant;
import com.fise.xw.imservice.event.GroupEvent;
import com.fise.xw.imservice.event.UserInfoEvent; 
import com.fise.xw.imservice.manager.IMGroupManager; 
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.widget.IMGroupAvatar;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.ScreenUtil;

import de.greenrobot.event.EventBus;



/**
 *    扫描二维进群界面
 * @author weileiguan
 *
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
		EventBus.getDefault().register(this);

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
		
		/*
		if (status == DBConstant.GROUP_MEMBER_STATUS_SAVE
				|| status == DBConstant.GROUP_MEMBER_STATUS_TEMP) {
			chat_btn.setText("开始群聊");
		} else {
			chat_btn.setText("加入群聊");
		}

		Toast.makeText(ScanGroupQRActivity.this, "status状态" + status,
			     Toast.LENGTH_SHORT).show();
			     */
		
		final GroupEntity groupInfoTemp = imService.getGroupManager().findGroup(curGroupId);
//		 
//		if (groupInfoTemp == null){
//			groupInfoTemp = imService.getGroupManager().findQRGroup(curGroupId); 
//			 
//		}

		if(groupInfoTemp == null){
			chat_btn.setText("确定加入" + groupInfo.getMainName());
		}else{
			chat_btn.setText("开始群聊" );
		}
		 
		chat_btn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//if (status == DBConstant.GROUP_MEMBER_STATUS_SAVE
				//		|| status == DBConstant.GROUP_MEMBER_STATUS_TEMP) {
				if(groupInfoTemp != null){

					IMUIHelper.openChatActivity(ScanGroupQRActivity.this,
							groupInfo.getSessionKey());

				} else {
					// reqGroupDetailInfo();
					Set<Integer> addMemberlist = new HashSet<>();
					addMemberlist.add(imService.getLoginManager().getLoginId());
					if (groupInfo.getGroupType() == DBConstant.GROUP_TYPE_WEI_TEMP) {
						imService.getGroupManager().reqAddGroupMemberWei(
								groupInfo.getPeerId(), addMemberlist,ChangeDataType.CHANGE_GROUP_USER_ADD_BY_SCAN);

					} else {
						imService.getGroupManager().reqAddGroupMember(
								groupInfo.getPeerId(), addMemberlist,ChangeDataType.CHANGE_GROUP_USER_ADD_BY_SCAN);
					}

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

	public void onEventMainThread(GroupEvent event) {
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
			Toast.makeText(ScanGroupQRActivity.this, "加入群成功",
				     Toast.LENGTH_SHORT).show();
			ScanGroupQRActivity.this.finish();
			break;	
			 
		case CHANGE_GROUP_MEMBER_FAIL:
			Toast.makeText(ScanGroupQRActivity.this, "加入群失败",
				     Toast.LENGTH_SHORT).show();
			  
			break;	
			   
		}
	}
	 
	
	public void onEventMainThread(UserInfoEvent event) {
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
	    EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
		super.onDestroy();
	}

	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
}

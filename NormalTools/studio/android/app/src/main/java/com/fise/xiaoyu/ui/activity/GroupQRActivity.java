package com.fise.xiaoyu.ui.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.menu.QrMenu;
import com.fise.xiaoyu.ui.widget.IMGroupAvatar;
import com.fise.xiaoyu.utils.ScreenUtil;
import com.fise.xiaoyu.utils.Utils;
import com.google.zxing.WriterException;
import com.jinlin.zxing.example.activity.CodeCreator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/** 
 *  二维码扫描群界面
 */
public class GroupQRActivity extends TTBaseActivity {
	private static IMService imService;
	private GroupEntity groupInfo;
	private PeerEntity peerEntity;
	private String curSessionKey;
	public QrMenu menu;
	public Bitmap bitmap = null;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			curSessionKey = GroupQRActivity.this.getIntent().getStringExtra(
					IntentConstant.KEY_SESSION_KEY);
			peerEntity = imService.getSessionManager().findPeerEntity(
					curSessionKey);

			if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
				groupInfo = (GroupEntity) peerEntity;
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

		setContentView(R.layout.group_qr_activity);
		imServiceConnector.connect(this);

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				GroupQRActivity.this.finish();
			}
		});
		//
		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				GroupQRActivity.this.finish();
			}
		});

		Button icon_user_info = (Button) findViewById(R.id.icon_user_info);
		icon_user_info.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (menu != null) {
//					menu.showAsDropDown(v);
					menu.showBottomDia();
				}
			}
		});

	}

	public void initDetailProfile() {

		ProgressBar progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
		progress_bar.setVisibility(View.GONE);

		IMGroupAvatar groupImage = (IMGroupAvatar) this
				.findViewById(R.id.contact_portrait);
		TextView groupName = (TextView) this.findViewById(R.id.group_name);

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
			if (i > DBConstant.GROUP_AVATAR_NUM - 1) { // 默认显示头的数量
				break;
			}

			avatarUrlList.add(entity.getAvatar());
			i++;
		}

		setGroupAvatar(groupImage, avatarUrlList);

		String name = groupInfo.getMainName();
		groupName.setText(groupInfo.getMainName());
		groupName.setVisibility(View.VISIBLE);

		// Bitmap bitmap = null;
		int peeid = groupInfo.getPeerId();
		 

		/* 
        JSONObject extraContent = new JSONObject();
        try {
			extraContent.put("curGroupId",peeid);
	        extraContent.put("version",groupInfo.getVersion());
	        extraContent.put("type", groupInfo.getGroupType());
	        extraContent.put("status", groupInfo.getSave());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/

		// String groupContent = extraContent.toString();

		// BitmapDrawable bd = (BitmapDrawable) (groupImage.getBackground());
		try {
			// bitmap = CodeCreator.createCode("group_" + peeid,bd.getBitmap());
			String infoUrl = imService.getContactManager().getSystemConfig()
					.getWebsite();

			String peerIdString = "wgid=" + peeid;
			String content = new String(Security.getInstance()
					.EncryptMsg(peerIdString));

			bitmap = CodeCreator.createQRCode(infoUrl + content);

		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ImageView icon_qr = (ImageView) findViewById(R.id.icon_qr);
		icon_qr.setVisibility(View.VISIBLE);
		icon_qr.setImageBitmap(bitmap);

		menu = new QrMenu(GroupQRActivity.this, bitmap, groupInfo.getPeerId(),
				groupInfo.getMainName());
		menu.addItems(new String[] { "保存到手机" });

	}

	/**
	 * 与search 有公用的地方，可以抽取IMUIHelper 设置群头像
	 * 
	 * @param avatar
	 * @param avatarUrlList
	 */
	private void setGroupAvatar(IMGroupAvatar avatar, List<String> avatarUrlList) {
		try {
			avatar.setViewSize(ScreenUtil.instance(GroupQRActivity.this)
					.dip2px(45));
			avatar.setChildCorner(2);
			avatar.setAvatarUrlAppend(SysConstant.AVATAR_APPEND_32);
			avatar.setParentPadding(3);
			avatar.setAvatarUrls((ArrayList<String>) avatarUrlList);
		} catch (Exception e) {
			// logger.e(e.toString());
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			initDetailProfile();
			break;

		case USER_QR_CODE_SAVE:
			Utils.showToast(GroupQRActivity.this, "二维码保存成功");
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

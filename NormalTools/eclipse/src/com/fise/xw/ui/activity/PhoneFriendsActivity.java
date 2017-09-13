package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.ui.widget.IMBaseImageView;
import com.fise.xw.utils.IMUIHelper;
import com.xiaowei.phone.PhoneMemberBean;

import de.greenrobot.event.EventBus;


/**
 *  通讯录中的好友信息
 * @author weileiguan
 *
 */
public class PhoneFriendsActivity extends TTBaseFragmentActivity {
	static IMService imService;
	private UserEntity currentUser;
	private int listId;
	public List<PhoneMemberBean> sourceList = new ArrayList<PhoneMemberBean>();

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			// init set adapter service

			if (imService == null) {
				logger.e("detail#imService is null");
				return;
			}

			listId = getIntent().getIntExtra(IntentConstant.PHONE_ID, 0);
			sourceList = imService.getUserActionManager()
					.getPhoneMemberBeanList();

			if ((listId >= 0) && (listId <= sourceList.size())) {
				currentUser = sourceList.get(listId).getUserEntity();
				if (currentUser != null) {
					initDetailProfile();
				}

			}
		}

		@Override
		public void onServiceDisconnected() {

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_phone_friends);
		imServiceConnector.connect(this);
		EventBus.getDefault().register(this);

		// text.addTextChangedListener(textWatcher);
	}

	private void initDetailProfile() {
		ProgressBar progressbar = (ProgressBar) this
				.findViewById(R.id.progress_bar);
		progressbar.setVisibility(View.GONE);

		setSex(currentUser.getGender());
		IMBaseImageView portraitImageView = (IMBaseImageView) findViewById(R.id.user_portrait);

		// setTextViewContent(R.id.remarksName, currentUser.getMainName());
		if (currentUser.getComment().equals("")) {
			setTextViewContent(R.id.remarksName, currentUser.getMainName());
		} else {
			setTextViewContent(R.id.remarksName, currentUser.getComment());
		}

		setTextViewContent(R.id.userName, "小位号: " + currentUser.getRealName());
		setTextViewContent(R.id.nickName, "昵称: " + currentUser.getMainName());
		// setTextViewContent(R.id.userName, currentUser.getRealName());
		// 头像设置
		portraitImageView
				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
		portraitImageView.setCorner(8);
		portraitImageView
				.setImageResource(R.drawable.tt_default_user_portrait_corner);
		portraitImageView.setImageUrl(currentUser.getAvatar());

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				PhoneFriendsActivity.this.finish();
			}
		});

		
		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				PhoneFriendsActivity.this.finish();
			}
		});

		
		
		RelativeLayout more = (RelativeLayout) findViewById(R.id.more);
		more.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				IMUIHelper.openUserInfoSignedActivity(
						PhoneFriendsActivity.this, currentUser.getSign_info());
			}
		});

		// 设置界面信息
		Button chatBtn = (Button) findViewById(R.id.chat_btn);

		if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO) {
			chatBtn.setText("添加好友");

		} else if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {
			chatBtn.setText("发送消息");
		} else if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_WEI) {
			chatBtn.setText("发送消息");
		}
		if (currentUser.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE
				||currentUser.getUserType() == ClientType.CLIENT_TYPE_FISE_CAR_VALUE) {
			chatBtn.setVisibility(View.GONE);
		}

		if (currentUser.getPeerId() == imService.getLoginManager().getLoginId()) {

			chatBtn.setVisibility(View.GONE);
		} else {
			chatBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {

					if ((currentUser.getUserType() != ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE)
							&&(currentUser.getUserType() != ClientType.CLIENT_TYPE_FISE_CAR_VALUE)) {
						
						if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO) {
							// UserEntity loginUser =
							// imService.getLoginManager().getLoginInfo();
							// String content = "你和" + loginUser.getMainName() +
							// "是朋友,现在可以聊天了";
							// imService.getUserActionManager().addReqFriends(currentUser,content);

							Intent intent = new Intent(
									PhoneFriendsActivity.this,
									ActivityReqVerification.class);
							intent.putExtra(IntentConstant.KEY_PEERID,
									currentUser.getPeerId());
							PhoneFriendsActivity.this.startActivity(intent);

						} else if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {

							IMUIHelper.openChatActivity(
									PhoneFriendsActivity.this,
									currentUser.getSessionKey());
						}
					}

				}
			});

		}
	}

	private void setTextViewContent(int id, String content) {
		TextView textView = (TextView) findViewById(id);
		if (textView == null) {
			return;
		}

		textView.setText(content);
	}

	private void setSex(int sex) {

		ImageView sexImageView = (ImageView) findViewById(R.id.sex);
		if (sexImageView == null) {
			return;
		}

		if (sex == DBConstant.SEX_MAILE) {
			sexImageView.setBackgroundResource(R.drawable.sex_head_man);
		} else {
			sexImageView.setBackgroundResource(R.drawable.icon_head_woman);
		}

	}

	public void onEvent(UserInfoEvent event) {
		switch (event) {
		case USER_INFO_REQ_UPDATE: {

		}
			break;
		}
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
		super.onDestroy();
	}
}

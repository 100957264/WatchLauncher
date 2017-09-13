package com.fise.xw.ui.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.PeerEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;

/**
 *选择聊天背景界面
 */
@SuppressLint("NewApi")
public class SelectMessageBgActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(SelectMessageBgActivity.class);
	private Handler uiHandler = new Handler();
	private IMService imService;

	private PeerEntity peerEntity;
	private String curSessionKey;
	private ImageView select_default_check;
	private String file;
	private boolean allMessageBg;

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

					curSessionKey = SelectMessageBgActivity.this.getIntent()
							.getStringExtra(IntentConstant.KEY_SESSION_KEY);
					if (TextUtils.isEmpty(curSessionKey)) {
						logger.e("groupmgr#getSessionInfoFromIntent failed");
						return;
					}
					
					allMessageBg = SelectMessageBgActivity.this.getIntent()
							.getBooleanExtra(IntentConstant.KEY_ALL_MESSAGE_BG, false);
					peerEntity = imService.getSessionManager().findPeerEntity(
							curSessionKey);

					SharedPreferences sp = SelectMessageBgActivity.this
							.getSharedPreferences("select_bg", MODE_PRIVATE);

					if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
						file = sp.getString("group_" + peerEntity.getPeerId(),
								"0");

					} else if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
						file = sp.getString("single_" + peerEntity.getPeerId(),
								"0");
					}

					if (file != null && (!file.equals("0"))) {
						select_default_check.setVisibility(View.GONE);
					}

					
					if(allMessageBg){
						String all = sp.getString("message_bg_all",
								"0"); 
						if(all!=null&&(!all.equals("0")))
						{
							select_default_check.setVisibility(View.GONE);
						}
						
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_message_bg);
		select_default_check = (ImageView) findViewById(R.id.select_default_check);

		imServiceConnector.connect(SelectMessageBgActivity.this);
		EventBus.getDefault().register(this);

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SelectMessageBgActivity.this.finish();
			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SelectMessageBgActivity.this.finish();
			}
		});

		Button select_default_bg = (Button) findViewById(R.id.select_default_bg);
		select_default_bg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				SharedPreferences sp = SelectMessageBgActivity.this
						.getSharedPreferences("select_bg", MODE_PRIVATE);
				
				if(allMessageBg){
					Editor editor = sp.edit();
					editor.putString("message_bg_all", "0");
					editor.commit();
 
				}else{
					if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
						Editor editor = sp.edit();
						editor.putString("group_" + peerEntity.getPeerId(), "0");
						editor.commit();

					} else if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
						Editor editor = sp.edit();
						editor.putString("single_" + peerEntity.getPeerId(), "0");
						editor.commit();
					}
				}
	

				triggerEvent(UserInfoEvent.USER_UPDATE_MESSAGE_BG_SUCCESS);
				select_default_check.setVisibility(View.VISIBLE);
			}
		});

	}

	/**
	 * @param event
	 */
	public void triggerEvent(UserInfoEvent event) {
		// 先更新自身的状态
		EventBus.getDefault().postSticky(event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(SelectMessageBgActivity.this);
		EventBus.getDefault().unregister(this);
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
	public void onEventMainThread(LoginEvent event) {
		switch (event) {
		case LOCAL_LOGIN_SUCCESS:
		case LOGIN_OK:
			break;

		}
	}

}

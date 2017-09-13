package com.fise.xw.ui.activity;

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.PeerEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.widget.HeadImageView;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;

public class MessageBgQueActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(MessageBgQueActivity.class);
	public static String imageUri = ""; 
	private IMService imService; 
	private HeadImageView portraitView; 
	private Button user_button;
	private PeerEntity peerEntity;
	private String curSessionKey;
	private boolean allMessageBg;
	

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}
			
			curSessionKey = MessageBgQueActivity.this.getIntent()
					.getStringExtra(IntentConstant.KEY_SESSION_KEY);
			if (TextUtils.isEmpty(curSessionKey)) {
				logger.e("groupmgr#getSessionInfoFromIntent failed");
				return;
			}
			
			peerEntity = imService.getSessionManager().findPeerEntity(
					curSessionKey);

		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_message_gallery);
		imServiceConnector.connect(this);
		EventBus.getDefault().register(this);


		allMessageBg = MessageBgQueActivity.this.getIntent()
				.getBooleanExtra(IntentConstant.KEY_ALL_MESSAGE_BG, false);
		Intent intent = getIntent();
		if (intent == null) {
			logger.e("detailPortrait#displayimage#null intent");
			return;
		}

		String resUri = intent.getStringExtra(IntentConstant.KEY_AVATAR_URL);
		imageUri = resUri;

		boolean isContactAvatar = intent.getBooleanExtra(
				IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, false);
		portraitView = (HeadImageView) findViewById(R.id.head_portrait);
		portraitView.setAvatar(resUri);

		if (portraitView == null) {
			logger.e("detailPortrait#displayimage#portraitView is null");
			return;
		}

		logger.d("detailPortrait#displayimage#going to load the detail portrait");

//		if (isContactAvatar) {
//			IMUIHelper.setEntityImageViewAvatarNoDefaultPortrait(portraitView,
//					resUri, DBConstant.SESSION_TYPE_SINGLE, 0);
//		} else {
//			IMUIHelper.displayImageNoOptions(portraitView, resUri, -1, 0);
//		}
		
		if(fileIsExists(imageUri))
		{
			 Bitmap bm = BitmapFactory.decodeFile(imageUri);
			 portraitView.setImageBitmap(bm);
		}

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MessageBgQueActivity.this.finish();
			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MessageBgQueActivity.this.finish();
			}
		});
 

		user_button = (Button) findViewById(R.id.user_button);
		user_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// menu.showAsDropDown(arg0);
				SharedPreferences sp = MessageBgQueActivity.this
						.getSharedPreferences("select_bg", MODE_PRIVATE);
				
				
				if(allMessageBg){
					Editor  editor  =  sp.edit();  
					editor.putString("message_bg_all",imageUri);
					editor.commit();
					
				}else{
					if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) { 
						Editor  editor  =  sp.edit();  
						editor.putString("group_" + peerEntity.getPeerId(),imageUri);
						editor.commit();
						 
					} else if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
			 
						Editor  editor  =  sp.edit();  
						editor.putString("single_" + peerEntity.getPeerId(),imageUri);
						editor.commit();
					}
				}
				
		
				triggerEvent(UserInfoEvent.USER_UPDATE_MESSAGE_BG_SUCCESS);
				MessageBgQueActivity.this.finish();
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
	public boolean fileIsExists(String name) {
		try {
			File f = new File(name);
			if (!f.exists()) {
				return false;
			}

		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return true;
	}

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			MessageBgQueActivity.this.finish();
			break;

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
	}

	public interface finishActivity {
		public void finish();
	}

}

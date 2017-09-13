package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 请求好友界面
 * 例如 请求附加信息的我是**
 */
public class ActivityReqVerification extends TTBaseActivity {
	private static IMService imService;
	private UserEntity loginInfo;
	private IMLoginManager imLoginManager = IMLoginManager.instance();
	private UserEntity currentUser;
	private int currentUserId;
	private EditText editTextId;
	
	
	
	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}
			currentUserId = ActivityReqVerification.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);
			if (currentUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}
			currentUser = imService.getContactManager().findContact(
					currentUserId);
			
			if(currentUser == null){
				currentUser = imService.getContactManager().findReq(
						currentUserId);
				 
			}
			editTextId.setText("我是" + imService.getLoginManager().getLoginInfo().getMainName());
 
		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_activity_req_verification);
		imServiceConnector.connect(this);
				editTextId = (EditText) findViewById(R.id.editTextId);
		
		
		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				ActivityReqVerification.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				ActivityReqVerification.this.finish();
			}
		});

		
		
		Button send = (Button) findViewById(R.id.send);
		send.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				
				UserEntity loginUser = imService.getLoginManager().getLoginInfo(); 
            	String content =  editTextId.getText().toString();
            	imService.getUserActionManager().addReqFriends(currentUser,content);
			}
		});
 
		 

	}

	public void initDetailProfile() {

	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event){
        switch (event){ 
                
            case USER_INFO_REQ_FRIENDS_SUCCESS:
				Utils.showToast(ActivityReqVerification.this, "请求加好友成功");
            	ActivityReqVerification.this.finish(); 
                break;
                
            case USER_INFO_REQ_FRIENDS_FAIL:
				Utils.showToast(ActivityReqVerification.this, "请求加好友失败");
            	ActivityReqVerification.this.finish();
                break;

			case USER_INFO_REQ_FRIENDS_ALREADY:
				Utils.showToast(ActivityReqVerification.this, "对方已经同意,你们已经是好友");
				ActivityReqVerification.this.finish();
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

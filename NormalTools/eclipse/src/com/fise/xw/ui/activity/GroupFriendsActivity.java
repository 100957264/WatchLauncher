package com.fise.xw.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
 
import android.widget.Toast;
  
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.R;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.SysConstant;
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.imservice.entity.ReqMessage;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.ui.activity.DetailPortraitActivity;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.widget.IMBaseImageView;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;

/**
 * 1.18 添加currentUser变量
 */
public class GroupFriendsActivity extends TTBaseFragmentActivity {
 
    private IMService imService;
    private UserEntity currentUser;
    private int currentUserId;
    
	
    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("detail#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("detail#imService is null");
                return;
            }
    		currentUserId = GroupFriendsActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);
    		
            currentUser = imService.getContactManager().findContact(currentUserId);
            if(currentUser != null) {
                initBaseProfile();
                initDetailProfile();
            } 
             
        }
        @Override
        public void onServiceDisconnected() {}
    };
     

    @Override
    public void onDestroy() { 
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(this);
        super.onDestroy();
    }

	@Override
	 public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		 
        setContentView(R.layout.tt_activity_group_friends); 
        imServiceConnector.connect(this);
        
		EventBus.getDefault().register(this); 
	 
		initRes();
		 
	}

	@Override
	public void onResume() {
	 
		super.onResume();
	}

	/**
	 * @Description 初始化资源
	 */
	private void initRes() { 
		
		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				GroupFriendsActivity.this.finish();
			}
		});
		 
         TextView left_text = (TextView) findViewById(R.id.left_text);
         left_text.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				GroupFriendsActivity.this.finish();
			}
		});
	 
	}
 
    public void onEventMainThread(UserInfoEvent event){
        switch (event){
            case USER_INFO_UPDATE:
                UserEntity entity  = imService.getContactManager().findContact(currentUserId);
                if(entity !=null && currentUser.equals(entity)){
                    initBaseProfile();
                    initDetailProfile();
                }
                break;
                
            case USER_INFO_REQ_FRIENDS_SUCCESS:
            	//Toast.makeText(GroupFriendsActivity.this, "请求加好友成功", Toast.LENGTH_SHORT).show();
            	GroupFriendsActivity.this.finish(); 
                break;
                
            case USER_INFO_REQ_FRIENDS_FAIL:
            //	Toast.makeText(GroupFriendsActivity.this, "请求加好友失败", Toast.LENGTH_SHORT).show();
            	GroupFriendsActivity.this.finish();
                break;
        }
    }


	private void initBaseProfile() {
        IMBaseImageView portraitImageView = (IMBaseImageView) findViewById(R.id.user_portrait);

          
        setTextViewContent(R.id.remarksName, currentUser.getMainName()); 
		setTextViewContent(R.id.userName, "小位号: "+ currentUser.getRealName());
		setTextViewContent(R.id.nickName, "昵称: " + currentUser.getMainName());
		//setTextViewContent(R.id.userName, currentUser.getRealName());
        //头像设置
        portraitImageView.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setCorner(8);
        portraitImageView.setImageResource(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setImageUrl(currentUser.getAvatar());

		portraitImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GroupFriendsActivity.this, DetailPortraitActivity.class);
				intent.putExtra(IntentConstant.KEY_AVATAR_URL, currentUser.getAvatar());
				intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, true);
				
				startActivity(intent);
			}
		});

		
		
		RelativeLayout more = (RelativeLayout)findViewById(R.id.more);
		more.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) { 
				 IMUIHelper.openUserInfoSignedActivity(GroupFriendsActivity.this,currentUser.getSign_info());
			}
		});

		
		ProgressBar progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
		progress_bar.setVisibility(View.GONE);
		
		// 设置界面信息
		Button chatBtn = (Button) findViewById(R.id.chat_btn);
		 
		 
		if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO)
		{
			chatBtn.setText("添加好友");
			
		}else if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YES)
		{
			chatBtn.setText("发送消息");
		}else if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_WEI)
		{
			chatBtn.setText("发送消息");
		}
		
		if(currentUser.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE
				||currentUser.getUserType() == ClientType.CLIENT_TYPE_FISE_CAR_VALUE){
			chatBtn.setVisibility(View.GONE);
		}
		
		if (currentUserId == imService.getLoginManager().getLoginId()) {
			chatBtn.setVisibility(View.GONE);
		}else{
            chatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                	if((currentUser.getUserType() != ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE)
                			&&(currentUser.getUserType() != ClientType.CLIENT_TYPE_FISE_CAR_VALUE)){
                		
                    	if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO) {
                    		
//                        	UserEntity loginUser = imService.getLoginManager().getLoginInfo(); 
//                        	String content = "你和" + loginUser.getMainName() + "是朋友,现在可以聊天了";  
//                        	//IMUserActionManager
//                            imService.getUserActionManager().addReqFriends(currentUser,content);
                    		
	                	      Intent intent = new Intent(GroupFriendsActivity.this, ActivityReqVerification.class); 
	                	      intent.putExtra(IntentConstant.KEY_PEERID, currentUser.getPeerId());   
	                	      GroupFriendsActivity.this.startActivity(intent);
                               
                    	}else{ 
                    		
                            IMUIHelper.openChatActivity(GroupFriendsActivity.this,currentUser.getSessionKey());
                            GroupFriendsActivity.this.finish();
                    	}
                	} 
                }      
            });

        }
	}

	private void initDetailProfile() { 

		setSex(currentUser.getGender());
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
		}else{
			sexImageView.setBackgroundResource(R.drawable.icon_head_woman);
		}

//		sexTextView.setVisibility(View.VISIBLE);
//		sexTextView.setText(text);
//		sexTextView.setTextColor(textColor);
	}

}

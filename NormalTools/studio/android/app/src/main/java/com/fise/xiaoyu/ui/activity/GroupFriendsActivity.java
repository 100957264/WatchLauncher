package com.fise.xiaoyu.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.entity.OnLineVedioMessage;
import com.fise.xiaoyu.imservice.event.PriorityEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
                imServiceConnector.disconnect(this);
        super.onDestroy();
    }

	@Override
	 public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		 
        setContentView(R.layout.tt_activity_group_friends); 
        imServiceConnector.connect(this);
	 
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
 
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event){
        switch (event){
            case USER_INFO_UPDATE:
                UserEntity entity  = imService.getContactManager().findContact(currentUserId);
                if(entity !=null && currentUser.equals(entity)){
                    initBaseProfile();
                    initDetailProfile();
                }
                break;
                
            case USER_INFO_REQ_FRIENDS_SUCCESS:
            	//Utils.showToast(GroupFriendsActivity.this, "请求加好友成功");
            	GroupFriendsActivity.this.finish(); 
                break;
                
            case USER_INFO_REQ_FRIENDS_FAIL:
            //	Utils.showToast(GroupFriendsActivity.this, "请求加好友失败");
            	GroupFriendsActivity.this.finish();
                break;
        }
    }


	private void initBaseProfile() {
        IMBaseImageView portraitImageView = (IMBaseImageView) findViewById(R.id.user_portrait);

          
        setTextViewContent(R.id.remarksName, currentUser.getMainName()); 
		setTextViewContent(R.id.userName, "小雨号: "+ currentUser.getRealName());
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
		 
		 
		if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO
				||currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_VERIFY)
		{
			chatBtn.setText("添加好友");
			
		}else if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YES)
		{
			chatBtn.setText("发送消息");
		}else if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)
		{
			chatBtn.setText("发送消息");
		}

		if(Utils.isClientType(currentUser)){
			chatBtn.setVisibility(View.GONE);
		}


		if (currentUserId == imService.getLoginManager().getLoginId()) {
			chatBtn.setVisibility(View.GONE);
		}else{
            chatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                	if(!Utils.isClientType(currentUser)){
                		
                    	if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO
								||currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_VERIFY) {
                    		
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


	//创建一个Handler
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 2001:
				{
					final MessageEntity entity = (MessageEntity) msg.obj;
					new Handler(new Handler.Callback() {
						@Override
						public boolean handleMessage(Message msg) {
							//实现页面跳转

							Intent intent = new Intent(GroupFriendsActivity.this,
									GuestActivity.class);

							OnLineVedioMessage message = OnLineVedioMessage.parseFromNet(entity);
							String pushUrl = message.getPushUrl();
							String pullUrl = message.getPullUrl();

							intent.putExtra(IntentConstant.KEY_PEERID, message.getFromId());
							intent.putExtra(IntentConstant.PUSHURL, pushUrl);
							intent.putExtra(IntentConstant.PULLURL, pullUrl);

							GroupFriendsActivity.this.startActivity(intent);

							return false;
						}
					}).sendEmptyMessageDelayed(0,600);//表示延迟3秒发送任务
				}
				break;
				default:
					break;
			}
		}
	};

	@Subscribe
	public void onMessageEvent(PriorityEvent event) {
		switch (event.event) {

			case MSG_RECEIVED_MESSAGE: {
				MessageEntity entity = (MessageEntity) event.object;

//				if(entity.getMsgType() == DBConstant.MSG_TYPE_VIDEO_CALL){
//
//					Message message = new Message();
//					message.what = 2001;
//					message.obj = entity;
//					handler.sendMessage(message);
//				}
			}
			break;

		}
	}
}

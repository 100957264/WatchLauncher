package com.fise.xiaoyu.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.activity.ActivityReqVerification;
import com.fise.xiaoyu.ui.activity.DetailPortraitActivity;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.IMUIHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * 1.18 添加currentUser变量
 */
public class SearchFriendsFragment extends MainFragment {

	private View curView = null;
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
 
            currentUser = imService.getUserActionManager().getSearchInfo().get(0); 
             
            if(currentUser ==null){
            	return ;
            }
            
            if(currentUser != null) {
            	
            	int userId = currentUser.getPeerId();
            	UserEntity user1 = imService.getContactManager().findContact(userId);
            	if(user1!=null){
            		currentUser = user1;
            	}
                
                initBaseProfile();
                initDetailProfile();
            }
            
            
            currentUserId = currentUser.getPeerId();
            UserEntity user = imService.getContactManager().findContact(currentUserId);
            if(user==null)
            {
                ArrayList<Integer> userIds = new ArrayList<>(1);
                //just single type
                userIds.add(currentUserId);
                imService.getContactManager().reqGetDetaillUsers(userIds);
            } else{
            	 initBaseProfile();
                 initDetailProfile();
            }
        }
        @Override
        public void onServiceDisconnected() {}
    };
     

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		imServiceConnector.disconnect(getActivity());
		 	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		imServiceConnector.connect(getActivity());
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
				curView = inflater.inflate(R.layout.tt_fragment_search_friends, topContentView);
		super.init(curView);
		showProgressBar();
		initRes();
 
		return curView;
	}

	@Override
	public void onResume() {
		Intent intent = getActivity().getIntent();
		if (null != intent) {
			String fromPage = intent.getStringExtra(IntentConstant.USER_DETAIL_PARAM);
			setTopLeftText(fromPage);
		}
		super.onResume();
	}

	/**
	 * @Description 初始化资源
	 */
	private void initRes() {
		// 设置标题栏 
		hideTopBar();
		
		Button left = (Button) curView.findViewById(R.id.icon_arrow);
		left.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
		
		TextView left_text = (TextView) curView.findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
		
		topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
		
		
		topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
		setTopLeftText(getResources().getString(R.string.top_left_back));
	}

	@Override
	protected void initHandler() {
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event){
        switch (event){
            case USER_INFO_UPDATE:
            case USER_INFO_REQ_ALL:
                UserEntity entity  = imService.getContactManager().findContact(currentUserId);
                if(entity !=null && currentUser.equals(entity)){
                    initBaseProfile();
                    initDetailProfile();
                }
                break;
                
            case USER_INFO_REQ_FRIENDS_SUCCESS:
                break; 
                
            case USER_INFO_REQ_FRIENDS_FAIL:
                break; 
        }
    } 


	private void initBaseProfile() {
		logger.d("detail#initBaseProfile");
        IMBaseImageView portraitImageView = (IMBaseImageView) curView.findViewById(R.id.user_portrait); 

       // setTextViewContent(R.id.remarksName, currentUser.getMainName()); 
		if(currentUser.getComment().equals(""))
		{

			setTextViewContent(R.id.remarksName, currentUser.getMainName());
		}else{

			setTextViewContent(R.id.remarksName, currentUser.getComment());
		}
		
		setTextViewContent(R.id.userName, "小雨号: "+ currentUser.getRealName());
		setTextViewContent(R.id.nickName, "昵称: " + currentUser.getMainName());
		//setTextViewContent(R.id.userName, currentUser.getRealName());
		
		
		setTextViewContent(R.id.locality_string, currentUser.getProvince() + " " + currentUser.getCity());
		setTextViewContent(R.id.qianming, currentUser.getSign_info());
		
		
        //头像设置
        portraitImageView.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setCorner(8);
        portraitImageView.setImageResource(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setImageUrl(currentUser.getAvatar());

		portraitImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), DetailPortraitActivity.class);
				intent.putExtra(IntentConstant.KEY_AVATAR_URL, currentUser.getAvatar());
				intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, true);
				
				startActivity(intent);
			}
		});


		
		RelativeLayout more = (RelativeLayout) curView.findViewById(R.id.more);
		more.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) { 
				 IMUIHelper.openUserInfoSignedActivity(SearchFriendsFragment.this.getActivity(),currentUser.getSign_info());
			}
		});
 
		// 设置界面信息
		Button chatBtn = (Button) curView.findViewById(R.id.chat_btn); 
		
		if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO
				||currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_VERIFY){
			
			chatBtn.setText("添加好友");
			
		}else if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YES){
			
			chatBtn.setText("发送消息");
		}else if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU){
			
			chatBtn.setText("发送消息");
		}
		
		
//		if(Utils.isClientType(currentUser)){
//			chatBtn.setVisibility(View.GONE);
//		}
		
		if (currentUserId == imService.getLoginManager().getLoginId()) {
			chatBtn.setVisibility(View.GONE);
			
		}else{
            chatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) { 
                	//if(!Utils.isClientType(currentUser))
                	{ 
                		
	                	if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO
								||currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_VERIFY)
	                	{  
	                	      Intent intent = new Intent(getActivity(), ActivityReqVerification.class); 
	                	      intent.putExtra(IntentConstant.KEY_PEERID, currentUser.getPeerId());   
	                	      getActivity().startActivity(intent);
	                	        
	                	}else {
	                		
	                        IMUIHelper.openChatActivity(getActivity(),currentUser.getSessionKey());
	                        getActivity().finish();
	                	}
                	
                	}
     
                }
            });

        }
	}

	private void initDetailProfile() {
		logger.d("detail#initDetailProfile");
		hideProgressBar();

		setSex(currentUser.getGender());
	}

	private void setTextViewContent(int id, String content) {
		TextView textView = (TextView) curView.findViewById(id);
		if (textView == null) {
			return;
		}

		textView.setText(content);
	}

	private void setSex(int sex) {
		if (curView == null) {
			return;
		}

		ImageView sexImageView = (ImageView) curView.findViewById(R.id.sex);
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

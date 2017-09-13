package com.fise.xw.ui.fragment;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
 
import android.widget.Toast;
  
import com.fise.xw.DB.entity.GroupEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.R;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.SysConstant;
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Utils;
import com.fise.xw.imservice.entity.ReqMessage;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.ui.activity.ActivityReqVerification;
import com.fise.xw.ui.activity.AuthSelectActivity;
import com.fise.xw.ui.activity.DetailPortraitActivity;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.widget.FilletDialog;
import com.fise.xw.ui.widget.IMBaseImageView;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.18 添加currentUser变量
 */
public class CardFriendsFragment extends MainFragment {

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
 
            currentUserId = CardFriendsFragment.this.getActivity().getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0); 
            
            currentUser  = imService.getContactManager().findContact(currentUserId);
            if(currentUser != null) {
                initBaseProfile();
                initDetailProfile();
            }else 
            {
                ArrayList<Integer> userIds = new ArrayList<>(1);
                //just single type
                userIds.add(currentUserId);
                imService.getContactManager().reqGetDetaillUsers(userIds);
            } 
        }
        @Override
        public void onServiceDisconnected() {}
    };
     

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		imServiceConnector.disconnect(getActivity());
		 EventBus.getDefault().unregister(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		imServiceConnector.connect(getActivity());
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		EventBus.getDefault().register(this);
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

    public void onEventMainThread(UserInfoEvent event){
        switch (event){
            case USER_INFO_UPDATE:
                UserEntity entity  = imService.getContactManager().findContact(currentUserId);
                if(entity !=null ){
                	currentUser = entity;
                    initBaseProfile();
                    initDetailProfile();
                }
                break;
                 
            case USER_INFO_REQ_FRIENDS_SUCCESS:
            //	Toast.makeText(this.getActivity(), "请求加好友成功", Toast.LENGTH_SHORT).show();
            //	getActivity().finish(); 
                break;
                
            case USER_INFO_REQ_FRIENDS_FAIL:
            	//Toast.makeText(this.getActivity(), "请求加好友失败", Toast.LENGTH_SHORT).show();
            	//getActivity().finish();
                break;
                
    		case WEI_FRIENDS_REQ_SUCCESS:
    			Toast.makeText(this.getActivity(), "发送请求成功,等待对方同意",
    					Toast.LENGTH_SHORT).show();
    			break;
    		case WEI_FRIENDS_REQ_FAIL:
    			Toast.makeText(this.getActivity(), "发送失败,请检查网络是否正常",
    					Toast.LENGTH_SHORT).show();
    			break;
    		case USER_INFO_CALCEL_FOLLOW:
    			Toast.makeText(this.getActivity(), "禁止取消位友", Toast.LENGTH_SHORT)
    					.show();
    			break;
        }
    }


	private void initBaseProfile() {
		logger.d("detail#initBaseProfile");
        IMBaseImageView portraitImageView = (IMBaseImageView) curView.findViewById(R.id.user_portrait);

    	Button button_follow = (Button) curView.findViewById(R.id.button_follow);

       // setTextViewContent(R.id.remarksName, currentUser.getMainName()); 
		if(currentUser.getComment().equals(""))
		{

			setTextViewContent(R.id.remarksName, currentUser.getMainName());
		}else{

			setTextViewContent(R.id.remarksName, currentUser.getComment());
		}
		
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
				Intent intent = new Intent(getActivity(), DetailPortraitActivity.class);
				intent.putExtra(IntentConstant.KEY_AVATAR_URL, currentUser.getAvatar());
				intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, true);
				
				startActivity(intent);
			}
		});

		
		
		
		
		if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_WEI) {
 
			button_follow.setBackgroundResource(R.drawable.button_follow);
			button_follow.setText(getResources().getString(
					R.string.cancel_follow));

		} else {
			button_follow.setText(getResources().getString(
					R.string.chat_follow)); //
			button_follow.setBackgroundResource(R.drawable.button_follow);
		}
		 
		if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO) {
			button_follow.setVisibility(View.GONE);
		}
		
		RelativeLayout more = (RelativeLayout) curView.findViewById(R.id.more);
		more.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) { 
				 IMUIHelper.openUserInfoSignedActivity(CardFriendsFragment.this.getActivity(),currentUser.getSign_info());
			}
		});


		
		
		// 设置界面信息
		Button chatBtn = (Button) curView.findViewById(R.id.chat_btn);
		
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
		
		
		if(Utils.isClientType(currentUser)){
			chatBtn.setVisibility(View.GONE);
		}
		
		if (currentUserId == imService.getLoginManager().getLoginId()) {
			chatBtn.setVisibility(View.GONE);
		}else{
            chatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) { 
                	if((currentUser.getUserType() != ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE)
                			&&(currentUser.getUserType() != ClientType.CLIENT_TYPE_FISE_CAR_VALUE))
                	{ 
                		
	                	if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO)
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

            
            
            


            
            button_follow.setOnClickListener(new View.OnClickListener() {

    			@Override
    			public void onClick(View arg0) {
    				final int toId = currentUser.getPeerId();

    				//String title = "";
    				String number = "";
    				if (currentUser.getIsFriend() != DBConstant.FRIENDS_TYPE_WEI) {
    					//title = "申请位友";
    					number = "你确定申请加位友 可以看到位友的 位置等信息";
    				} else if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_WEI) {
    					//title = "取消位友";
    					number = "取消位友, 你们相互看不到对方的位置等信息";
    				}
     
    				final FilletDialog myDialog = new FilletDialog(
    						CardFriendsFragment.this.getActivity());
    				// myDialog.setTitle(title);//设置标题
    				myDialog.setMessage(number);// 设置内容

    				myDialog.dialog.show();// 显示

    				// 确认按键回调，按下确认后在此做处理
    				myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
    					@Override
    					public void ok() {

    						if (currentUser.getIsFriend() != DBConstant.FRIENDS_TYPE_WEI) {
    							imService.getUserActionManager()
    									.addWeiFriends(toId);
    						} else if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_WEI) {

    							boolean weiCreator = false;

    							List<GroupEntity> WeiGroup = imService
    									.getGroupManager().getNormalWeiGroupList();
    							for (int i = 0; i < WeiGroup.size(); i++) {
    								if (WeiGroup.get(i).getCreatorId() == currentUser
    										.getPeerId()) {
    									weiCreator = true;
    									break;
    								}
    							}
    							//
    							// 取消关注
    							if (weiCreator)//
    							{
    								Toast.makeText(
    										CardFriendsFragment.this
    												.getActivity(), "你在位群中无法取消关注",
    										Toast.LENGTH_SHORT).show();
    							} else {

    								imService.getUserActionManager()
    										.confirmXiaoWei(toId, currentUser);
    							}

    						}
    						myDialog.dialog.dismiss();
    						
    					}
    				});

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

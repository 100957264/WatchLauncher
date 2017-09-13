package com.fise.xw.ui.fragment;
 
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMUserAction.ActionType;
import com.fise.xw.ui.activity.DetailPortraitActivity;
import com.fise.xw.ui.widget.IMBaseImageView;
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;

/**
 * 1.18 添加currentUser变量
 */
public class UserInfoFragment extends MainFragment {

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

            currentUserId = getActivity().getIntent().getIntExtra(IntentConstant.KEY_PEERID,0);
            if(currentUserId == 0){
                logger.e("detail#intent params error!!");
                return;
            }
            currentUser = imService.getContactManager().findFriendsContact(currentUserId); // findContact
            if(currentUser != null) {
                initBaseProfile();
                initDetailProfile();
            }
             
            ArrayList<Integer> userIds = new ArrayList<>(1);
            //just single type
            userIds.add(currentUserId);
            imService.getContactManager().reqGetDetaillUsers(userIds);
        }
        @Override
        public void onServiceDisconnected() {
        	
        }
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
		
		curView = inflater.inflate(R.layout.tt_fragment_user_follow, topContentView);
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
				//imService.getUserActionManager().cancelWeiFriends(ActionType.ACTION_TYPE_OPEN_PEER_MONITOR, currentUser.getPeerId());
			}
		});
		
		
		TextView left_text = (TextView) curView.findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish(); 
				//imService.getUserActionManager().cancelWeiFriends(ActionType.ACTION_TYPE_OPEN_PEER_MONITOR, currentUser.getPeerId());
			}
		});
		
		topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
				//imService.getUserActionManager().cancelWeiFriends(ActionType.ACTION_TYPE_OPEN_PEER_MONITOR, currentUser.getPeerId());
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
                if(entity !=null){
                	currentUser = entity; 
                	initBaseProfile();
                    initDetailProfile();
                }
                break; 
                
            case WEI_FRIENDS_REQ_SUCCESS: 
            	Toast.makeText(this.getActivity(), "发送请求成功,等待对方同意",
            		      Toast.LENGTH_SHORT).show();
                break; 
            case WEI_FRIENDS_REQ_FAIL: 
            	Toast.makeText(this.getActivity(), "发送失败,请检查网络是否正常",
          		      Toast.LENGTH_SHORT).show();
                break; 
                
            case USER_INFO_DELETE_SUCCESS: 
            	Toast.makeText(this.getActivity(), "删除好友成功", Toast.LENGTH_SHORT).show();
            	UserInfoFragment.this.getActivity().finish();
                break; 
            case USER_INFO_DELETE_FAIL: 
            	Toast.makeText(this.getActivity(), "删除好友失败", Toast.LENGTH_SHORT).show(); 
                break;  
                
            case USER_BLACKLIST_FAIL: 
            	Toast.makeText(this.getActivity(), "加入黑名单失败", Toast.LENGTH_SHORT).show(); 
                break;    
                
            case USER_BLACKLIST_SUCCESS: 
            	Toast.makeText(this.getActivity(), "加入黑名单成功", Toast.LENGTH_SHORT).show(); 
            	this.getActivity().finish();
                break;    
            case USER_BLACKLIST_DEL_SUCCESS: 
            	Toast.makeText(this.getActivity(), "移除黑名单成功", Toast.LENGTH_SHORT).show(); 
            	this.getActivity().finish();
                break;   
                
		default:
			break;
        }
    }
 

	private void initBaseProfile() {
		logger.d("detail#initBaseProfile");
        
		//  if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_WEI ) 
		TextView show_name = (TextView) curView.findViewById(R.id.show_name); 
		if(currentUser.getComment().equals(""))
		{

			show_name.setText(currentUser.getMainName());
		}else{ 
			show_name.setText(currentUser.getComment());
		}
		
		
		RelativeLayout setting_label = (RelativeLayout) curView.findViewById(R.id.setting_label); 
		setting_label.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				 
				IMUIHelper.openUserInfoRemarks(
						UserInfoFragment.this.getActivity(), currentUserId);
			}
		});
		
		Button delete = (Button) curView.findViewById(R.id.delete_wei_btn); 
		delete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) 
			{
				
				imService.getUserActionManager().deleteFriends(currentUser.getPeerId(), currentUser);
			}  
			});
		
		
		//获取CheckBox实例
		CheckBox blackListCheckbox = (CheckBox)curView.findViewById(R.id.BlackListCheckbox);
		
		if(currentUser.getAuth() == DBConstant.AUTH_TYPE_BLACK)
		{
			blackListCheckbox.setChecked(true);
		}else{
			blackListCheckbox.setChecked(false);
		}
		
		//绑定监听器
		blackListCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		            
		            @Override
		            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		                // TODO Auto-generated method stub

		            	if(arg1)
		            	{
		            		imService.getUserActionManager().cancelBlackList(ActionType.ACTION_TYPE_ADD_BLACKLIST, currentUser.getPeerId(), currentUser);
		            	}else{
		            		imService.getUserActionManager().cancelBlackList(ActionType.ACTION_TYPE_DEL_BLACKLIST,currentUser.getPeerId(), currentUser);
		            	}
		            }
		        });
 
        
	}

	private void initDetailProfile() {
		logger.d("detail#initDetailProfile");
		hideProgressBar();
	} 
}

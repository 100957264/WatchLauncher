package com.fise.xiaoyu.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMUserAction.ActionType;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

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

			if(currentUser ==null)
			{
				ArrayList<Integer> userIds = new ArrayList<>(1);
				//just single type
				userIds.add(currentUserId);
				imService.getContactManager().reqGetDetaillUsers(userIds);
			}

        }
        @Override
        public void onServiceDisconnected() {
        	
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event){
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
				Utils.showToast(this.getActivity(), "发送请求成功,等待对方同意");
                break; 
            case WEI_FRIENDS_REQ_FAIL:
				Utils.showToast(this.getActivity(), "发送失败,请检查网络是否正常");
                break; 
                
            case USER_INFO_DELETE_SUCCESS:
				Utils.showToast(this.getActivity(), "删除好友成功");
            	UserInfoFragment.this.getActivity().finish();
                break; 
            case USER_INFO_DELETE_FAIL:
				Utils.showToast(this.getActivity(), "删除好友失败");
                break;  
                
            case USER_BLACKLIST_FAIL:
				Utils.showToast(this.getActivity(), "加入黑名单失败");
                break;    
                
            case USER_BLACKLIST_SUCCESS:
				Utils.showToast(this.getActivity(), "加入黑名单成功");
            	this.getActivity().finish();
                break;    
            case USER_BLACKLIST_DEL_SUCCESS:
				Utils.showToast(this.getActivity(), "移除黑名单成功");
            	this.getActivity().finish();
                break;   
                
		default:
			break;
        }
    }
 

	private void initBaseProfile() {
		logger.d("detail#initBaseProfile");
        
		//  if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU )

		Button delete = (Button) curView.findViewById(R.id.delete_wei_btn);
		delete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) 
			{
				final FilletDialog myDialog = new FilletDialog(UserInfoFragment.this.getActivity() ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE);

				if(currentUser.getComment().equals(""))
				{
					myDialog.setTitle("确定删除好友"+"("+currentUser.getMainName()+")");//
				}else{
					myDialog.setTitle("确定删除好友"+"("+currentUser.getComment()+")");//
				}

				myDialog.dialog.show();//显示

				//确认按键回调，按下确认后在此做处理
				myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
					@Override
					public void ok() {

						imService.getUserActionManager().deleteFriends(currentUser.getPeerId(), currentUser);
						myDialog.dialog.dismiss();
                       //跳转
                        IMUIHelper.openMainActivity(UserInfoFragment.this.getActivity() , 1);



					}
				});



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

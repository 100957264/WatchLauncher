package com.fise.xiaoyu.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMUserAction.ActionType;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 1.18 添加currentUser变量
 * 黑名单消息界面
 */
public class BlackListInfoActivity extends TTBaseFragmentActivity {
 
	private IMService imService;
	private UserEntity currentUser;
	private int currentUserId;
 
	private Button icon_user_info; 
 

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("detail#onIMServiceConnected");

			imService = imServiceConnector.getIMService();
			if (imService == null) {
				logger.e("detail#imService is null");
				return;
			}

			currentUserId = BlackListInfoActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);
			if (currentUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}
			 
			currentUser = imService.getContactManager().findBlackList(currentUserId);
			if (currentUser != null) {
				initBaseProfile();
				initDetailProfile();
			}
  
		}

		@Override
		public void onServiceDisconnected() {

		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		imServiceConnector.disconnect(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_black_list_follow);
		
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
		// 设置标题栏 
		Button icon_arrow = (Button) this.findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) { 
				// imService.getUserActionManager().cancelWeiFriends(ActionType.ACTION_TYPE_OPEN_PEER_MONITOR,
				// currentUser.getPeerId());
				BlackListInfoActivity.this.finish();
			}
		});
		

		TextView left_text = (TextView) this.findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) { 
				// imService.getUserActionManager().cancelWeiFriends(ActionType.ACTION_TYPE_OPEN_PEER_MONITOR,
				// currentUser.getPeerId());
				BlackListInfoActivity.this.finish();
			}
		}); 

		

        TextView show_phone_name = (TextView) findViewById(R.id.phone);
        show_phone_name.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        new ContextThemeWrapper(BlackListInfoActivity.this,
                                android.R.style.Theme_Holo_Light_Dialog));
                //builder.setTitle(recentInfo.getName()); //暂时屏蔽

                String[] items = new String[] { getString(R.string.call_phone_name)};

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // guanweile
                                //用intent启动拨打电话
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ currentUser.getPhone()));
                                startActivity(intent);
                                break;

                        }
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();

            }
        });
 
	}
 

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {
	 

        case USER_BLACKLIST_FAIL:
			Utils.showToast(BlackListInfoActivity.this, "加入黑名单失败");
            break;
            
        case USER_BLACKLIST_SUCCESS:
			Utils.showToast(BlackListInfoActivity.this, "加入黑名单成功");
        	BlackListInfoActivity.this.finish();
            break;    
            
        case USER_BLACKLIST_DEL_SUCCESS:
			Utils.showToast(BlackListInfoActivity.this, "移除黑名单成功");
        	BlackListInfoActivity.this.finish();
            break;   
			 
		default:
			break;
		}
	} 

	   
	private void initBaseProfile() { 
		 
		ProgressBar progress_bar = (ProgressBar)this.findViewById(R.id.progress_bar);
		progress_bar.setVisibility(View.GONE);
		
		
	     
		IMBaseImageView portraitImageView = (IMBaseImageView)this.findViewById(R.id.user_portrait);

		if(currentUser.getComment().equals(""))
		{ 
			setTextViewContent(R.id.remarksName, currentUser.getMainName());
		}else{ 
			setTextViewContent(R.id.remarksName, currentUser.getComment());
		}
		
		setTextViewContent(R.id.userName, "小雨号: " + currentUser.getRealName());
		setTextViewContent(R.id.nickName, "昵称: " + currentUser.getMainName());
		setTextViewContent(R.id.phone,   currentUser.getPhone());
		
		if(currentUser.getProvince().equals(currentUser.getCity()))
		{
			setTextViewContent(R.id.locality_string,   " " +currentUser.getCity());
		}else{
			setTextViewContent(R.id.locality_string,   currentUser.getProvince() + " " +currentUser.getCity());
		}
		
		
		 
		// setTextViewContent(R.id.userName, currentUser.getRealName());
		// 头像设置
		portraitImageView
				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
		portraitImageView.setCorner(8);
		portraitImageView
				.setImageResource(R.drawable.tt_default_user_portrait_corner);
		portraitImageView.setImageUrl(currentUser.getAvatar());


		Button remove_black_list = (Button) this
				.findViewById(R.id.remove_black_list);

		
		RelativeLayout setting_label = (RelativeLayout) this
				.findViewById(R.id.setting_label);
		
		
		RelativeLayout more = (RelativeLayout) this
				.findViewById(R.id.more);
		more.setOnClickListener(new OnClickListener() {
		      @Override
		      public void onClick(View v) {
		        // TODO Auto-generated method stub 
		    	  IMUIHelper.openUserInfoSignedActivity(BlackListInfoActivity.this,currentUser.getSign_info());
		      }
		    }); 
		




		TextView more_text = (TextView) this.findViewById(R.id.more_text);
		more_text.setText(currentUser.getSign_info()+"");
		 
		
		TextView show_name = (TextView) this
				.findViewById(R.id.show_name);
		if(currentUser.getComment().equals(""))
		{ 
			show_name.setText(currentUser.getMainName());
		}else{ 
			show_name.setText(currentUser.getComment());
		}
		
		
		setting_label.setOnClickListener(new OnClickListener() {
		      @Override
		      public void onClick(View v) {
		        // TODO Auto-generated method stub 
		    	   
		      }
		    }); 
	
		 
		
		// 设置界面信息
		Button chatBtn = (Button) this.findViewById(R.id.chat_btn);
		chatBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				IMUIHelper.openChatActivity(BlackListInfoActivity.this,
						currentUser.getSessionKey());
				BlackListInfoActivity.this.finish();
				
			}
		});

		remove_black_list.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				imService.getUserActionManager().cancelBlackList(ActionType.ACTION_TYPE_DEL_BLACKLIST,currentUser.getPeerId(), currentUser);
			}
		});
		 
	}
	
	 
	private void initDetailProfile() { 
		setSex(currentUser.getGender());
	}

	private void setTextViewContent(int id, String content) {
		TextView textView = (TextView) this.findViewById(id);
		if (textView == null) {
			return;
		}

		textView.setText(content);
	}

	private void setSex(int sex) { 

		ImageView sexImageView = (ImageView) this.findViewById(R.id.sex);
		if (sexImageView == null) {
			return;
		}

		if (sex == DBConstant.SEX_MAILE) {
			sexImageView.setBackgroundResource(R.drawable.sex_head_man);
		} else {
			sexImageView.setBackgroundResource(R.drawable.icon_head_woman);
		}
 
	}
	
}

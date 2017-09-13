package com.fise.xiaoyu.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.ReqFriendsEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.entity.WeiEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMNotificationManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.adapter.NewFriendsAdspter;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.utils.IMUIHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 *  新的朋友界面
 *
 */
public class NewFriendActivity extends TTBaseFragmentActivity {
	private ListView listView = null;
	private NewFriendActivity activity;
	private NewFriendsAdspter adapter;
	private static IMService imService; 
	private List<WeiEntity> userList = new ArrayList<>();

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				return;
			}
			 
			// init set adapter service
			List<WeiEntity> userList1 = imService.getUserActionManager()
					.getReqFriendsList();

			ArrayList<Integer> userIds = new ArrayList<>();
			// just single type
			for (int i = 0; i < userList1.size(); i++) {
				if (userList1.get(i).getFromId() != imService.getLoginManager()
						.getLoginId()) {
					userList.add(userList1.get(i));
					if (imService.getContactManager().findContact(
							userList1.get(i).getFromId()) == null  ) {
						userIds.add(userList1.get(i).getFromId());
					}
				}
			}

			if (userIds.size() > 0) {
				imService.getContactManager().reqGetDetaillUsers(userIds);
			}



			adapter = new NewFriendsAdspter(activity, userList,
					imService);
			listView.setAdapter(adapter);
            if(userList.size() == 0 ){
              findViewById(R.id.list_line1).setVisibility(View.GONE);
				findViewById(R.id.list_line2).setVisibility(View.GONE);
			}

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int postion, long arg3) {

					// int realIndex = position - userSize;
					UserEntity currentUser =  imService.getContactManager().findContact(userList.get(postion).getFromId());
					if(currentUser!=null){

						//如果是好友　　进入聊天详情
						if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YES
								||currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {

							Intent intent = new Intent(NewFriendActivity.this, UserInfoFollowActivity.class);
							intent.putExtra(IntentConstant.KEY_PEERID, currentUser.getPeerId());
							NewFriendActivity.this.startActivity(intent);

						}else{
							Intent intent = new Intent(NewFriendActivity.this,
									ReqFriendsActivity.class);

							intent.putExtra(IntentConstant.KEY_PEERID, userList
									.get(postion).getFromId());
							intent.putExtra(IntentConstant.LIST_ID,
									userList.get(postion).getActType());
							NewFriendActivity.this.startActivity(intent);
						}
					}else{
						Intent intent = new Intent(NewFriendActivity.this,
								ReqFriendsActivity.class);

						intent.putExtra(IntentConstant.KEY_PEERID, userList
								.get(postion).getFromId());
						intent.putExtra(IntentConstant.LIST_ID,
								userList.get(postion).getActType());
						NewFriendActivity.this.startActivity(intent);
					}


				}
			});

			listView.setOnItemLongClickListener(adapter);
			
			
			
			if(imService.getUnReadMsgManager().getTotalReqMessageCount() >0){
				
				List<ReqFriendsEntity> reqUnFriends = imService.getUnReadMsgManager().getReqUnFriendsMap();
				for(int i=0;i<reqUnFriends.size();i++){
					IMNotificationManager.instance().cancelSessionNotifications(reqUnFriends.get(i).getUserId()+"");
				}
			}
			 
			imService.getUnReadMsgManager().updateReqUnreadCount();
			imService.getUnReadMsgManager().updateReqMessageUnreadCount();

		}

		@Override
		public void onServiceDisconnected() {
			
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.new_add_friends);
		imServiceConnector.connect(this);

		listView = (ListView) findViewById(R.id.list);
		activity = this;

		TextView add_friends = (TextView) findViewById(R.id.new_friends_add);
		add_friends.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openAddFriendActivity(NewFriendActivity.this);
			}
		});

		TextView new_friends_bk = (TextView) findViewById(R.id.new_friends_bk);
		new_friends_bk.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				NewFriendActivity.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				NewFriendActivity.this.finish();
			}
		});


	}

	private void renderReqList() {

		userList.clear();
		List<WeiEntity> userList1 = imService.getUserActionManager()
				.getReqFriendsList();
		for (int i = 0; i < userList1.size(); i++) {
			if (userList1.get(i).getFromId() != imService.getLoginManager()
					.getLoginId()) {
				userList.add(userList1.get(i));
			}
		}

 
		adapter.putReqFriendsList(userList);

	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {
		case USER_INFO_REQ_ALL:
		case USER_INFO_REQ_UPDATE:
		case WEI_FRIENDS_INFO_REQ_ALL:
		case USER_INFO_UPDATE:
		case WEI_FRIENDS_WEI_REQ_ALL:
			renderReqList();
			break;

		}
	}

	@Override
	public void onDestroy() {
				imServiceConnector.disconnect(this);
		super.onDestroy();
	}
}

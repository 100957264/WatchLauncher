package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.List;
 
import android.content.Intent;
import android.os.Bundle; 
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener; 
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xw.R; 
import com.fise.xw.DB.entity.ReqFriendsEntity;
import com.fise.xw.DB.entity.WeiEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.manager.IMNotificationManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector; 
import com.fise.xw.ui.adapter.NewFriendsAdspter;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;


/**
 *  新的朋友界面
 * @author weileiguan
 *
 */
public class NewFriendActivity extends TTBaseFragmentActivity {
	private ListView listView = null;
	private NewFriendActivity activity;
	private NewFriendsAdspter adapter;
	private static IMService imService; 
	private List<WeiEntity> userList = new ArrayList<>();
	private List<WeiEntity> userWeiList = new ArrayList<>();

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
			List<WeiEntity> userWeiList1 = imService.getUserActionManager()
					.getReqWeiList();

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

			ArrayList<Integer> userWeiIds = new ArrayList<>();
			for (int i = 0; i < userWeiList1.size(); i++) {
				if (userWeiList1.get(i).getFromId() != imService
						.getLoginManager().getLoginId()) {
					userWeiList.add(userWeiList1.get(i));

					if (imService.getContactManager().findContact(
							userWeiList1.get(i).getFromId()) == null ) {
						userWeiIds.add(userWeiList1.get(i).getFromId());
					}
				}
			}

			if (userIds.size() > 0) {
				imService.getContactManager().reqGetDetaillUsers(userWeiIds);
			}
 
			adapter = new NewFriendsAdspter(activity, userList, userWeiList,
					imService);
			listView.setAdapter(adapter);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int postion, long arg3) {

					// int realIndex = position - userSize;
					if (postion < userList.size()) {

						Intent intent = new Intent(NewFriendActivity.this,
								ReqFriendsActivity.class);

						intent.putExtra(IntentConstant.KEY_PEERID, userList
								.get(postion).getFromId());
						intent.putExtra(IntentConstant.LIST_ID,
								userList.get(postion).getActType());
						NewFriendActivity.this.startActivity(intent);

					} else {

						int index = postion - userList.size();
						Intent intent = new Intent(NewFriendActivity.this,
								ReqFriendsActivity.class);

						intent.putExtra(IntentConstant.KEY_PEERID, userWeiList
								.get(index).getFromId());
						intent.putExtra(IntentConstant.LIST_ID, userWeiList
								.get(index).getActType());
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
		EventBus.getDefault().register(this);

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

		EditText text = (EditText) findViewById(R.id.search_phone);
		text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openSearchFriendActivity(NewFriendActivity.this,
						DBConstant.SEACHFRIENDS);
			}
		});
		// text.addTextChangedListener(textWatcher);

	}

	private void renderReqList() {
		// userList = imService.getUserActionManager()
		// .getReqFriendsList();
		// userWeiList = imService.getUserActionManager()
		// .getReqWeiList();

		userList.clear();
		userWeiList.clear();

		List<WeiEntity> userList1 = imService.getUserActionManager()
				.getReqFriendsList();
		List<WeiEntity> userWeiList1 = imService.getUserActionManager()
				.getReqWeiList();
		for (int i = 0; i < userList1.size(); i++) {
			if (userList1.get(i).getFromId() != imService.getLoginManager()
					.getLoginId()) {
				userList.add(userList1.get(i));
			}
		}

		for (int i = 0; i < userWeiList1.size(); i++) {
			if (userWeiList1.get(i).getFromId() != imService.getLoginManager()
					.getLoginId()) {
				userWeiList.add(userWeiList1.get(i));
			}
		}

 
		adapter.putReqFriendsList(userList, userWeiList);

	}

	public void onEventMainThread(UserInfoEvent event) {
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
		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
		super.onDestroy();
	}
}

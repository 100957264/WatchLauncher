package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.WeiEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.adapter.YuMessageAdspter;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 *  请求设备为好友的请求列表
 */
public class ReqYuFriendsListActivity extends TTBaseFragmentActivity {

	private ListView listView = null;
	private ReqYuFriendsListActivity activity;
	private YuMessageAdspter adapter;
	private static IMService imService;
	private IMContactManager contactMgr;
	private List<WeiEntity> weiEntityList;

	
	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}


			imService.getUnReadMsgManager().updateReqUnreadYuFriendsCount();
			imService.getUnReadMsgManager().updateReqYuFriendsUnreadCount();

			imService.getUnReadMsgManager().updateParentRefuseCount();
			imService.getUnReadMsgManager().updateParentRefuseUnreadCount();  //请求

			weiEntityList = imService.getUserActionManager().getReqYuList();
			//weiEntityList.addAll(imService.getUserActionManager().getParentRefuseList());

			List<WeiEntity> weiEntityListTmp = new ArrayList<>();
			weiEntityListTmp.addAll(weiEntityList);
			weiEntityListTmp.addAll(imService.getUserActionManager().getParentRefuseList());



			adapter = new YuMessageAdspter(activity, weiEntityList, imService);
			listView.setAdapter(adapter);
			listView.setOnItemLongClickListener(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					Object object = weiEntityList.get(arg2);
					if (object instanceof WeiEntity) {


					}

				}
			});

		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_activity_yufriends_list);
		imServiceConnector.connect(this);

		listView = (ListView) findViewById(R.id.list_yu_friends);

		activity = this;


		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				ReqYuFriendsListActivity.this.finish();
			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				ReqYuFriendsListActivity.this.finish();
			}
		});

	}       

	private void renderYuFriendsList() {

		weiEntityList = imService.getUserActionManager().getReqYuList();
		//weiEntityList.addAll(imService.getUserActionManager().getParentRefuseList());
		List<WeiEntity> weiEntityListTmp = new ArrayList<>();
		weiEntityListTmp.addAll(weiEntityList);
		weiEntityListTmp.addAll(imService.getUserActionManager().getParentRefuseList());

		adapter.putWeiEntityList(weiEntityList);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {
			case USER_INFO_REQ_ALL:
			case USER_INFO_UPDATE:
				renderYuFriendsList();
				break;

			case WEI_FRIENDS_WEI_REQ_ALL:
			case WEI_FRIENDS_INFO_REQ_ALL:
				renderYuFriendsList();
				break;
			case USER_INFO_REQ_YU:
				renderYuFriendsList();
				break;
		}
	}

	@Override
	public void onDestroy() {
				imServiceConnector.disconnect(this);
		super.onDestroy();
	}


}

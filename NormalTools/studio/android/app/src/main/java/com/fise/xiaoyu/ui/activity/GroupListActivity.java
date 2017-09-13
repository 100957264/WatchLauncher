package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.imservice.event.GroupEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.adapter.NewGroupAdspter;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.utils.IMUIHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


/**
 *  好友群的列表界面
 */
public class GroupListActivity extends TTBaseFragmentActivity {

	private ListView listView = null;
	private GroupListActivity activity;
	private NewGroupAdspter adapter;
	private static IMService imService;
	private IMContactManager contactMgr;
	private List<GroupEntity> groupList;

	
	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			groupList = imService.getGroupManager().getNormalGroupSortedList();

			adapter = new NewGroupAdspter(activity, groupList, imService);
			listView.setAdapter(adapter);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					Object object = groupList.get(arg2);
					if (object instanceof GroupEntity) {
						GroupEntity groupEntity = (GroupEntity) object;
						IMUIHelper.openChatActivity(GroupListActivity.this,
								groupEntity.getSessionKey());
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

		setContentView(R.layout.tt_activity_group_list);
		imServiceConnector.connect(this);

		listView = (ListView) findViewById(R.id.list_group);

		activity = this;

		TextView weiwang = (TextView) findViewById(R.id.weiwang);
		weiwang.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				GroupListActivity.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				GroupListActivity.this.finish();
			}
		});


//		EditText text = (EditText) findViewById(R.id.search_phone);
//		text.setOnClickListener(new View.OnClickListener() {
//
//			public void onClick(View v) {
//				IMUIHelper.openSearchFriendActivity(GroupListActivity.this,
//						DBConstant.SEACHFRIENDS);
//			}
//		});

		Button new_group_add = (Button) findViewById(R.id.new_group_add);
		new_group_add.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openGroupMemberSelectActivity(GroupListActivity.this, false);
			}
		});

	}       

	private void renderGroupList() {

		 groupList = imService.getGroupManager()
				.getNormalGroupSortedList();
		// 没有任何的联系人数据
//		if (groupList.size() <= 0) {
//			return;
//		}
		adapter.putGroupList(groupList);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(GroupEvent event) {
		switch (event.getEvent()) {

		case CREATE_GROUP_OK:
		case USER_GROUP_DELETE_SUCCESS:
		case CHANGE_GROUP_DELETE_SUCCESS: {
			renderGroupList();

		}
			break;

		}
	}

	@Override
	public void onDestroy() {
				imServiceConnector.disconnect(this);
		super.onDestroy();
	}


}

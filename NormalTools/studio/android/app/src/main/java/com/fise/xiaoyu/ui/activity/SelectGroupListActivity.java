package com.fise.xiaoyu.ui.activity;

import android.content.Intent;
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
 *  选择群列表界面
 */
public class SelectGroupListActivity extends TTBaseFragmentActivity {

	private ListView listView = null;
	private SelectGroupListActivity activity;
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
						IMUIHelper.openChatActivity(
								SelectGroupListActivity.this,
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

		setContentView(R.layout.tt_activity_select_group);
		imServiceConnector.connect(this);
		Intent intent = this.getIntent();

		listView = (ListView) findViewById(R.id.list_group);

		activity = this;

		TextView weiwang = (TextView) findViewById(R.id.weiwang);
		weiwang.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				SelectGroupListActivity.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				SelectGroupListActivity.this.finish();
			}
		});

	}

	private void renderGroupList() {


		groupList = imService.getGroupManager().getNormalGroupSortedList();
//		groupList = imService.getGroupManager()
//				.getNormalGroupSortedList();
		// 没有任何的联系人数据
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

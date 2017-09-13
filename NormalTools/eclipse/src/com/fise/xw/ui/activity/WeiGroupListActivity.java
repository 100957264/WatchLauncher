package com.fise.xw.ui.activity;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.GroupEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.imservice.event.GroupEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.adapter.NewGroupAdspter;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;



/**
 *  位群列表界面
 * @author weileiguan
 *
 */
public class WeiGroupListActivity extends TTBaseFragmentActivity {

	private ListView listView = null;
	private WeiGroupListActivity activity;
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

			groupList = imService.getGroupManager().getNormalWeiGroupList();

			adapter = new NewGroupAdspter(activity, groupList, imService);
			listView.setAdapter(adapter);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					Object object = groupList.get(arg2);
					if (object instanceof GroupEntity) {
						GroupEntity groupEntity = (GroupEntity) object;
						IMUIHelper.openChatActivity(WeiGroupListActivity.this,
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

		setContentView(R.layout.tt_activity_wei_group_list);
		imServiceConnector.connect(this);

		listView = (ListView) findViewById(R.id.list_group);

		activity = this;
		EventBus.getDefault().register(this);

		Button add_group = (Button) findViewById(R.id.new_group_add);
		add_group.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// guanweile
				IMUIHelper
						.openGroupWeiSelectActivity(WeiGroupListActivity.this);
			}
		});

		TextView weiwang = (TextView) findViewById(R.id.weiwang);
		weiwang.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				WeiGroupListActivity.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				WeiGroupListActivity.this.finish();
			}
		});

		EditText text = (EditText) findViewById(R.id.search_phone);
		text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openSearchFriendActivity(WeiGroupListActivity.this,
						DBConstant.SEACHFRIENDS);
			}
		});

	}

	private void renderGroupList() {

		 groupList = imService.getGroupManager()
				.getNormalWeiGroupList();
		// 没有任何的联系人数据
		// if (groupList.size() <= 0) {
		// return;
		// }
		adapter.putGroupList(groupList);
	}

	public void onEventMainThread(GroupEvent event) {
		switch (event.getEvent()) {

		case CREATE_GROUP_OK: 
		case CHANGE_GROUP_DELETE_SUCCESS:{
				renderGroupList();

		}
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

package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.FamilyConcernEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.adapter.AuthSelectAdapter;
import com.fise.xiaoyu.ui.adapter.AuthSelectAdapter.AuthHolder;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.ui.fragment.GroupSelectFragment;
import com.fise.xiaoyu.ui.widget.SearchEditText;
import com.fise.xiaoyu.ui.widget.SortSideBar;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *  亲情关注界面  授权
 */
public class AuthSelectActivity extends TTBaseFragmentActivity {

	private static Logger logger = Logger.getLogger(GroupSelectFragment.class);

	private View curView = null;
	private IMService imService;
	private TextView right_txt;

	/**
	 * 列表视图 1. 需要两种状态:选中的成员List --》确定之后才会回话页面或者详情 2. 已经被选的状态 -->已经在群中的成员
	 * */
	private AuthSelectAdapter adapter;
	private ListView contactListView;

	private SortSideBar sortSideBar;
	private TextView dialog;
	private SearchEditText searchEditText;

	// private PeerEntity peerEntity;
	private ArrayList<String> listStr;

	private UserEntity currentUser;
	private int currentUserId;
	List<UserEntity> authList = new ArrayList<>();
	private boolean showView ;
    private DeviceEntity device;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_auth_select);

		showView = false;
				imServiceConnector.connect(this);

		contactListView = (ListView) findViewById(R.id.all_contact_list);

		TextView group_left_txt = (TextView) findViewById(R.id.group_left_txt);
		group_left_txt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AuthSelectActivity.this.finish();
			}
		});

		// RelativeLayout select_group =
		// (RelativeLayout)findViewById(R.id.select_group);
		// select_group.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// IMUIHelper.openSelectGroupActivity(AuthSelectActivity.this,false);
		//
		// }
		// });

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
				imServiceConnector.disconnect(this);
	}

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("groupselmgr#onIMServiceConnected");

			imService = imServiceConnector.getIMService();
			if (imService == null) {
				return;
			}

			currentUserId = AuthSelectActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);

			if (currentUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}

			 device = imService.getDeviceManager().findDeviceCard(currentUserId);
			//authList = imService.getDeviceManager().getAuthUserContactList();  如果是设备管理员，不显示
		    List<FamilyConcernEntity> family =  imService.getDeviceManager().findFamilyConcern(currentUserId);
			for(int i=0;i<family.size();i++){
				if(imService.getContactManager().findFriendsContact(family.get(i).getPeeId())!=null){
					authList.add(imService.getContactManager().findFriendsContact(family.get(i).getPeeId()));
				}else if(imService.getContactManager().findContact(family.get(i).getPeeId())!=null){
						authList.add(imService.getContactManager().findContact(family.get(i).getPeeId()));
				}
			}
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);
			if (currentUser == null) {
				logger.e("detail#intent params error!!");
				return;
			}

			listStr = new ArrayList<String>();

			/** 已经处于选中状态的list */
			Set<Integer> alreadyList = getAlreadyCheckList();
			initContactList(alreadyList);
			right_txt = (TextView) findViewById(R.id.group_right_txt);
			right_txt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (adapter.getCheckListSet().size() <= 0) {
						Utils.showToast(AuthSelectActivity.this, "请选择授权人");
						return;
					}
                    List<UserEntity> contactList = adapter.getSelectList();
//					if ((authList.size() + contactList.size()) >= DBConstant.AUTCH_SELECT_NUM) {
//						Utils.showToast(AuthSelectActivity.this, "最多添加6个授权人");
//						return;
//					}

					Set<Integer> checkListSet = adapter.getCheckListSet();

					showView = true;

					 
					for (int i = 0; i < contactList.size(); i++) {
						imService.getDeviceManager().authDevice(currentUser, 
								contactList.get(i).getPeerId(),
								contactList.get(i));
					}

				}
			});

		}

		@Override
		public void onServiceDisconnected() {

		}
	};

	/**
	 * 获取列表中 默认选中成员列表
	 * 
	 * @return
	 */
	private Set<Integer> getAlreadyCheckList() {
		Set<Integer> alreadyListSet = new HashSet<>();
		return alreadyListSet;
	}

	private void initContactList(final Set<Integer> alreadyList) {
		// 根据拼音排序
		adapter = new AuthSelectAdapter(this, imService);
		contactListView.setAdapter(adapter);

		// contactListView.setOnItemClickListener(adapter);
		contactListView.setOnItemLongClickListener(adapter);

		contactListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				AuthHolder holder = (AuthHolder) view.getTag();
				holder.checkBox.toggle();// 在每次获取点击的item时改变checkbox的状态

				UserEntity contact = (UserEntity) (adapter.getItem(position));
				int loginId = imService.getLoginManager().getLoginId();
 
				//已经选中的不再处理
				boolean disable = false; 
				if (contact != null) {
					disable = adapter.getAlreadyListSet().contains(
							contact.getPeerId());
				}
				if(disable){
					return ;
				}
				
				
				if (holder.checkBox.isChecked() == true) { // &&((contact.getPeerId()!=loginId))
					listStr.add(holder.nameView.getText().toString());
					right_txt.setTextColor(getResources().getColor(
							R.color.cancel_color));
					adapter.setCheck(contact.getPeerId(),
							holder.checkBox.isChecked(), contact);
				} else {

					listStr.remove(holder.nameView.getText().toString());
					if (listStr.size() <= 0) {
						right_txt.setTextColor(getResources().getColor(
								R.color.select_group_disabled));
					}
					adapter.setCheck(contact.getPeerId(),
							holder.checkBox.isChecked(), contact);
				}

				if (listStr.size() > 0) {
					right_txt.setText("授权" + "(" + listStr.size() + ")");
				} else {
					right_txt.setText("授权");
				}
			}

		});

		List<UserEntity> contactList = imService.getContactManager()
				.getContactFriendsSortedList(); // guanweile  获取好友和位友信息
        UserEntity mastUser = null;
        for (int i = 0; i < contactList.size(); i++) {
            if(device.getMasterId() == contactList.get(i).getPeerId()){
                mastUser = contactList.get(i);
            }
        }
        if(mastUser != null){
            contactList.remove(mastUser);
        }
		contactList.removeAll(authList);

		adapter.setAllUserList(contactList);
		adapter.setAlreadyListSet(alreadyList);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(DeviceEvent event) {
		switch (event) {

		case USER_INFO_AUTH_DEVICE_SUCCESS:
			
			if(showView){  
				showView = false;
				Utils.showToast(AuthSelectActivity.this, "授权成功");
				AuthSelectActivity.this.finish();
			}
			break;
		case USER_INFO_AUTH_DEVICE_FAILED:
			if(showView){  
				showView = false;
				Utils.showToast(AuthSelectActivity.this,
						imService.getDeviceManager().getAuthError());
			}
			break;
		case USER_INFO_AUTH_DEVICE_OUT:
			if(showView){  
				showView = false;
				Utils.showToast(AuthSelectActivity.this, "授权超时");
			}
			break;
		}
	}

}

package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.FamilyConcernEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.adapter.AuthSelectAdapter;
import com.fise.xw.ui.adapter.AuthSelectAdapter.AuthHolder;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.ui.fragment.GroupSelectFragment;
import com.fise.xw.ui.widget.SearchEditText;
import com.fise.xw.ui.widget.SortSideBar;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;


/**
 *  亲情关注界面  授权
 * @author weileiguan
 *
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_auth_select);

		showView = false;
		EventBus.getDefault().register(this);
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
		EventBus.getDefault().unregister(this);
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

			
			//authList = imService.getDeviceManager().getAuthUserContactList();
		    List<FamilyConcernEntity> family =  imService.getDeviceManager().findFamilyConcern(currentUserId);
			for(int i=0;i<family.size();i++){
				if(imService.getContactManager().findXiaoWeiContact(family.get(i).getPeeId())!=null){
					 authList.add(imService.getContactManager().findXiaoWeiContact(family.get(i).getPeeId()));
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
						Toast.makeText(AuthSelectActivity.this, "请选择授权人",
								Toast.LENGTH_SHORT).show();
						return;
					}

					if (authList.size() >= 5) {
						Toast.makeText(AuthSelectActivity.this, "最多添加5个授权人",
								Toast.LENGTH_SHORT).show();
						return;
					}

					Set<Integer> checkListSet = adapter.getCheckListSet();

					// ArrayList<Integer> checkList = new ArrayList<Integer>();
					// Iterator it= checkListSet.iterator();
					// while(it.hasNext())
					// {
					// int userId =(int)it.next();
					// checkList.add(userId);
					// }
					// for(int i = 0;i<checkList.size();i++){
					// imService.getDeviceManager().authDevice(currentUser.getMainName(),
					// checkList.get(i),);
					// }

					showView = true;
					List<UserEntity> contactList = adapter.getSelectList();
					 
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
				.getContactWeiFriendsList(); // guanweile getContactSortedList

		contactList.removeAll(authList);

		adapter.setAllUserList(contactList);
		adapter.setAlreadyListSet(alreadyList);
	}

	public void onEventMainThread(DeviceEvent event) {
		switch (event) {

		case USER_INFO_AUTH_DEVICE_SUCCESS:
			
			if(showView){  
				showView = false;
				Toast.makeText(AuthSelectActivity.this, "授权成功", Toast.LENGTH_SHORT)
						.show(); 
				AuthSelectActivity.this.finish();
			}
			break;
		case USER_INFO_AUTH_DEVICE_FAILED:
			if(showView){  
				showView = false; 
				Toast.makeText(AuthSelectActivity.this,
						imService.getDeviceManager().getAuthError(),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case USER_INFO_AUTH_DEVICE_OUT:
			if(showView){  
				showView = false; 
				Toast.makeText(AuthSelectActivity.this, "授权超时", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		}
	}

}

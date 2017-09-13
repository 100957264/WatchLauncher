package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.FamilyConcernEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.adapter.FamilyFollowAdspter;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;



/**
 *  亲情关注人的列表界面
 * @author weileiguan
 *
 */
public class FamilyFollowActivity extends TTBaseFragmentActivity {
	
	private Logger logger = Logger.getLogger(FamilyFollowActivity.class); 
	private IMService imService;
	private ListView listView = null;
	private int currentUserId;
	List<UserEntity> authList = new ArrayList<>();
	private FamilyFollowAdspter adapter;
	private UserEntity currentUser;
	private DeviceEntity device;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onServiceDisconnected() {
		}

		@Override
		public void onIMServiceConnected() {
			logger.d("login#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			currentUserId = FamilyFollowActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);

			if (currentUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);
			if (currentUser == null) {
				return;
			}
			device = imService.getDeviceManager().findDeviceCard(currentUserId);
			if (device == null) {
				return;
			}

			List<FamilyConcernEntity> family = imService.getDeviceManager()
					.findFamilyConcern(currentUserId);
			for (int i = 0; i < family.size(); i++) {
				if (imService.getContactManager().findXiaoWeiContact(
						family.get(i).getPeeId()) != null) {
					authList.add(imService.getContactManager()
							.findXiaoWeiContact(family.get(i).getPeeId()));
				} else if (imService.getContactManager().findContact(
						family.get(i).getPeeId()) != null) {
					authList.add(imService.getContactManager().findContact(
							family.get(i).getPeeId()));
				}
			}

			// imService.getDeviceManager().getAuthUserContactList();
			listView = (ListView) findViewById(R.id.list);
			adapter = new FamilyFollowAdspter(FamilyFollowActivity.this,
					authList, currentUser.getRealName(), currentUser,device);
			listView.setAdapter(adapter);
			listView.setOnItemLongClickListener(adapter);

			 
			RelativeLayout add_zhang = (RelativeLayout) findViewById(R.id.add_zhanghu);
			add_zhang.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					UserEntity loginContact = IMLoginManager.instance()
							.getLoginInfo();
					if (device != null
							&& device.getMasterId() != loginContact.getPeerId()) {
						Toast.makeText(FamilyFollowActivity.this, "你没有权限",
								Toast.LENGTH_SHORT).show();
						return;
					}
					IMUIHelper.openAuthSelectActivity(
							FamilyFollowActivity.this, currentUserId);
				}
			});
			
			
			if (device != null
					&& device.getMasterId() != imService.getLoginManager().getLoginId()) { 
				add_zhang.setVisibility(View.GONE);
				View add_zhanghu_view = (View) findViewById(R.id.add_zhanghu_view);
				add_zhanghu_view.setVisibility(View.GONE);
				
				View show_zhanghu = (View) findViewById(R.id.show_zhanghu);
				show_zhanghu.setVisibility(View.GONE);
				
				RelativeLayout add_zhanghu_layout = (RelativeLayout) findViewById(R.id.add_zhanghu_layout);
				add_zhanghu_layout.setVisibility(View.GONE);
				
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		imServiceConnector.connect(FamilyFollowActivity.this);
		EventBus.getDefault().register(this);

		setContentView(R.layout.tt_activity_family_follow);

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FamilyFollowActivity.this.finish();
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(FamilyFollowActivity.this);
		EventBus.getDefault().unregister(this);
	}

	// 为什么会有两个这个
	// 可能是 兼容性的问题 导致两种方法onBackPressed
	@Override
	public void onBackPressed() {
		logger.d("login#onBackPressed");
		// imLoginMgr.cancel();
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * ----------------------------event 事件驱动----------------------------
	 */
	public void onEventMainThread(DeviceEvent event) {
		switch (event) {

		case USER_INFO_AUTH_DEVICE_SUCCESS:
			renderAuthList();
			break;

		case USER_INFO_DELETE_AUTH:
			Toast.makeText(FamilyFollowActivity.this, "亲情关注人员删除成功",
				     Toast.LENGTH_SHORT).show();
			renderUpdateAuthList();
			break;

		}
	}

	public void renderAuthList() {
		// authList = imService.getDeviceManager().getAuthUserContactList();

		authList.clear();
		List<FamilyConcernEntity> family = imService.getDeviceManager()
				.findFamilyConcern(currentUserId);
		for (int i = 0; i < family.size(); i++) {
			if (imService.getContactManager().findXiaoWeiContact(
					family.get(i).getPeeId()) != null) {
				authList.add(imService.getContactManager().findXiaoWeiContact(
						family.get(i).getPeeId()));
			} else if (imService.getContactManager().findContact(
					family.get(i).getPeeId()) != null) {
				authList.add(imService.getContactManager().findContact(
						family.get(i).getPeeId()));
			}
		}

		// 没有任何的联系人数据
		if (authList.size() <= 0) {
			return;
		}
		adapter.putDeviceList(authList);
	}

	public void renderUpdateAuthList() {
		// authList = imService.getDeviceManager().getAuthUserContactList();
		authList.clear();
		List<FamilyConcernEntity> family = imService.getDeviceManager()
				.findFamilyConcern(currentUserId);
		for (int i = 0; i < family.size(); i++) {
			if (imService.getContactManager().findXiaoWeiContact(
					family.get(i).getPeeId()) != null) {
				authList.add(imService.getContactManager().findXiaoWeiContact(
						family.get(i).getPeeId()));
			} else if (imService.getContactManager().findContact(
					family.get(i).getPeeId()) != null) {
				authList.add(imService.getContactManager().findContact(
						family.get(i).getPeeId()));
			}
		}

		adapter.putUpdateDeviceList(authList);
	}
}

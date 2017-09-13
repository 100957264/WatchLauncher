package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xw.R; 
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.ElectricFenceEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.ui.adapter.ElectronicListAdapter;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;


/**
 *  电子围栏的列表界面 (设备界面电子围栏先显示的是电子围栏列表)
 * @author weileiguan
 *
 */
public class ElectronicListActivity extends TTBaseFragmentActivity {

	private ListView listView = null;
	private ElectronicListActivity activity;
	private ElectronicListAdapter adapter;
	private static IMService imService;
	private IMContactManager contactMgr;
	private List<ElectricFenceEntity> electronicList = new ArrayList<>();
	private int currentUserId;
	private DeviceEntity rsp;
	private UserEntity currentDevice;
	

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}
 
			currentUserId = ElectronicListActivity.this.getIntent()
					.getIntExtra(IntentConstant.KEY_PEERID, 0);
			
			currentDevice = imService.getContactManager().findDeviceContact(
					currentUserId);
			
			rsp = imService.getDeviceManager().findDeviceCard(
					currentUserId);
			if (rsp == null) {
				return;
			}
			
		   	if(currentDevice!=null){
        		if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE) {
        			TextView left_text = (TextView) findViewById(R.id.left_text);
        			left_text.setText("定位卡片机");
        		}else if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_FISE_CAR_VALUE) {
        			TextView left_text = (TextView) findViewById(R.id.left_text);
        			left_text.setText("电动车");
        		}
        	} 

			 List<ElectricFenceEntity> electronicListTemp = imService.getDeviceManager().findElectrice(
					currentUserId);
			for(int i=0;i<electronicListTemp.size();i++){
				if(electronicListTemp.get(i).getStatus()!= DBConstant.ELECTRONIC_DELETE){
					electronicList.add(electronicListTemp.get(i));
				}
			}
			  
			adapter = new ElectronicListAdapter(activity, electronicList,
					imService,currentUserId,rsp);
			listView.setAdapter(adapter);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					IMUIHelper.openElectronicActivity(
							ElectronicListActivity.this, currentUserId,
							DBConstant.ELECTRONIC_UPDATE,
							electronicList.get(arg2));
				}
			});

			
			if(imService.getLoginManager().getLoginId()!= rsp.getMasterId()){
				Button right_button = (Button) findViewById(R.id.right_button);
				right_button.setVisibility(View.GONE);
			}
			
			listView.setOnItemLongClickListener(new OnItemLongClickListener(){  
	            @Override  
	            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,  
	                    final int arg2, long arg3) {  
	                // TODO Auto-generated method stub  
	                //
	            	if(rsp.getMasterId()!=imService.getLoginManager().getLoginId()){
						 
	            		 return true;  
					}
					
	            	
	            	
	            	AlertDialog.Builder builder = new AlertDialog.Builder(
							new ContextThemeWrapper(
									ElectronicListActivity.this,
									android.R.style.Theme_Holo_Light_Dialog));
					String[] items = new String[] { ElectronicListActivity.this
							.getString(R.string.delete_electornic) };

					builder.setItems(items,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
									 	imService.getDeviceManager().settingElectronic(
						            			imService.getLoginManager().getLoginId(), currentUserId, DBConstant.ELECTRONIC_DELETE,
												electronicList.get(arg2).getFenceId(), electronicList.get(arg2).getLng()+"",
												electronicList.get(arg2).getLat()+"", electronicList.get(arg2).getRadius(),
												electronicList.get(arg2).getMark(),DBConstant.ELECTRONIC_STATS_ENABLE);
										break;
									}
								}
							});
					AlertDialog alertDialog = builder.create();
					alertDialog.setCanceledOnTouchOutside(true);
					alertDialog.show();
					 
	                return true;  
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

		setContentView(R.layout.tt_activity_electronic_list);
		imServiceConnector.connect(this);

		listView = (ListView) findViewById(R.id.list_electronic);

		activity = this;
		EventBus.getDefault().register(this);

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				ElectronicListActivity.this.finish();
			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				ElectronicListActivity.this.finish();
			}
		});

		Button right_button = (Button) findViewById(R.id.right_button);
		right_button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openElectronicActivity(ElectronicListActivity.this,
						currentUserId, DBConstant.ELECTRONIC_ADD, null);
			}
		});

	}

	public void renderDeviceList() {
		//electronicList = imService.getDeviceManager().findElectrice(
		//		currentUserId);
		electronicList.clear();
		 List<ElectricFenceEntity> electronicListTemp = imService.getDeviceManager().findElectrice(
					currentUserId);
			for(int i=0;i<electronicListTemp.size();i++){
				if(electronicListTemp.get(i).getStatus()!= DBConstant.ELECTRONIC_DELETE){
					electronicList.add(electronicListTemp.get(i));
				}
			} 
			adapter.putDeviceList(electronicList);

	}

	public void onEventMainThread(DeviceEvent event) {
		switch (event) {

		case USER_INFO_SETTING_DEVICE_SUCCESS:
			renderDeviceList();
			break;
		case USER_INFO_ELECTIRC_FENCE:
			renderDeviceList();
			break;
		}
	}

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		}
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
		super.onDestroy();
	}
}

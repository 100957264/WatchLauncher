package com.fise.xw.ui.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.DeviceTrajectory;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMDevice.AlarmType;
import com.fise.xw.protobuf.IMDevice.ConfigSyncMode;
import com.fise.xw.ui.base.TTBaseActivity;

import de.greenrobot.event.EventBus;

/**
 * 测试设备 消息通知列表
 */
@SuppressLint("NewApi")
public class MyListView extends TTBaseActivity {

	private ListView listView;
	private IMService imService;
	private UserEntity deviceUser;
	private int deviceUserId;
	
	private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService(); 
            if(imService ==null)
            {
            	return ;
            }
            
            deviceUserId = MyListView.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);
 
			if (deviceUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}
			deviceUser = imService.getContactManager().findDeviceContact(
					deviceUserId);
			if (deviceUser == null) {
				return;
			}
			
			int loginId = imService.getLoginManager().getLoginId();
			imService.getDeviceManager().deviceActionRequest(loginId,deviceUserId,0);
			
			listView = (ListView) findViewById(R.id.my_list);
			// listView = new ListView(this);
			List<String> data1 = new ArrayList<String>();
			
			listView.setAdapter(new ArrayAdapter<String>(MyListView.this,
					android.R.layout.simple_expandable_list_item_1, data1));
			
			

        }

        @Override
        public void onServiceDisconnected() {
        }
    };


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_list_activity);
		imServiceConnector.connect(this); 
		 EventBus.getDefault().register(this);
		 

		//setContentView(listView);

	}

	
    public void onEventMainThread(DeviceEvent event){
        switch (event){ 
        case USER_INFO_DEVICE_GUIJIN_FAILED:    
        	Toast.makeText(MyListView.this, "设备历史行为失败", Toast.LENGTH_SHORT).show();
            break;
            
        case USER_INFO_DEVICE_GUIJIN_SUCCESS:    
        	listView.setAdapter(new ArrayAdapter<String>(MyListView.this,
					android.R.layout.simple_expandable_list_item_1, getData()));
            break;
                       
        }
    }
	private List<String> getData() {

		List<String> data = new ArrayList<String>();
		//data.add("测试数据1");
		//data.add("测试数据2");
		//data.add("测试数据3");
		//data.add("测试数据4");
		List<DeviceTrajectory> tt = imService.getDeviceManager().getTrajectory();
		for(int i=0;i<tt.size();i++)
		{
			String xinwei=""; 
			if(tt.get(i).getActionType() == AlarmType.ALARM_TYPE_OPWEROFF.getNumber()){
				xinwei = "关机";
			}else if(tt.get(i).getActionType() == AlarmType.ALARM_TYPE_LOW_BATTARY.getNumber()){
				xinwei = "低电量报警";
			}else if(tt.get(i).getActionType() == AlarmType.ALARM_TYPE_OUT_FENCE.getNumber()){
				xinwei = "安全围栏";
			}else if(tt.get(i).getActionType() == AlarmType.ALARM_TYPE_URGENCY.getNumber()){
				xinwei = "紧急号码";
			}
//			else if(tt.get(i).getActionType() == AlarmType.ALARM_TYPE_ACTION_LOGIN.getNumber()){
//				xinwei = "登录";
//			}else if(tt.get(i).getActionType() == AlarmType.ALARM_TYPE_ACTION_LOGOUT.getNumber()){
//				xinwei = "退出";
//			}else if(tt.get(i).getActionType() == AlarmType.ALARM_TYPE_ACTION_CONFIG.getNumber()){
//				
//				if(tt.get(i).getActionValue()== ConfigSyncMode.CONFIG_SYNC_BASEINFO.getNumber()){
//					xinwei = "同步基本信息";
//				}else if(tt.get(i).getActionValue()== ConfigSyncMode.CONFIG_SYNC_ALLOW_MOBILE.getNumber()){
//					xinwei = "同步白名单";
//				}else if(tt.get(i).getActionValue()== ConfigSyncMode.CONFIG_SYNC_ALARM_MOBILE.getNumber()){
//					xinwei = "同步报警电话";
//				}else if(tt.get(i).getActionValue()== ConfigSyncMode.CONFIG_SYNC_ELECTIRC_FENCE.getNumber()){
//					xinwei = "同步安全围栏";
//				}else if(tt.get(i).getActionValue()== ConfigSyncMode.CONFIG_SYNC_ALL.getNumber()){
//					xinwei = "同步全部";
//				}
//			}
//			
			SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
			//Long time=new Long(tt.get(i).getUpdated());  
			String time1 = format.format(tt.get(i).getUpdated());  
			data.add(tt.get(i).getActionId() + "  " + xinwei);
		}

		return data;
	}
	
	@Override
	public void onDestroy() { 
		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this); 
        super.onDestroy();
        
	}
}

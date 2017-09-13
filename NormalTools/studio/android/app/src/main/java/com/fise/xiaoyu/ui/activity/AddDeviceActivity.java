package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.ui.base.ActivityManager;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMDevice.ManageType;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


/**
 *  添加设备界面
 */
public class AddDeviceActivity extends  TTBaseFragmentActivity{
	 
	private  AddDeviceActivity activity; 
	private  static IMService imService;
	private IMContactManager contactMgr; 
	private EditText device_name;
    private int school_id;
    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("config#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return ;
			}
			   
			
			   Button addDevice =(Button)findViewById(R.id.add_device);
		        addDevice.setOnClickListener(new View.OnClickListener() {

			         public void onClick(View v) {  
			        	 String device = device_name.getText().toString();
			        	 List<UserEntity> devList = imService.getDeviceManager().loadDevice();
			        	 boolean isDev = false;
			        	 for(int i=0;i<devList.size();i++){
			        		 if(devList.get(i).getRealName().equals(device)){
			        			 isDev = true;
			        			 break;
			        		 }
			        	 } 
			        	 
			        	 if(isDev == false){ 
			        		 imService.getDeviceManager().addDevice(device,ManageType.MANAGE_TYPE_ADD_DEVICE,null,school_id);
			        	 }else{
                             Utils.showToast(AddDeviceActivity.this, "你已经添加该设备");
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
         
        setContentView(R.layout.tt_activity_add_device); 
        imServiceConnector.connect(this);

        device_name =(EditText)findViewById(R.id.device_name);

        school_id = this.getIntent()
                .getIntExtra(IntentConstant.SCHOOL_ID, 0);
        
        
        
        Button icon_arrow =(Button)findViewById(R.id.icon_arrow);  
        icon_arrow.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) {  
	        	 AddDeviceActivity.this.finish();
	         } 
         });
        
        
        TextView device_name_tt =(TextView)findViewById(R.id.device_name_tt);  
        device_name_tt.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) {  
	        	 AddDeviceActivity.this.finish();
	         } 
         }); 
        
    }
    
    
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeviceEvent event){
        switch (event){ 
        
            case USER_INFO_UPDATE_DEVICE_SUCCESS:    
            	AddDeviceActivity.this.finish();
                break;
            case USER_INFO_ADD_DEVICE_SUCCESS:    {
                ActivityManager.getInstance().finishActivity(SelectSchoolActivity.class);
                AddDeviceActivity.this.finish();

            }

                break;
                
            case USER_INFO_ADD_DEVICE_FAILED:
                Utils.showToast(AddDeviceActivity.this, imService.getDeviceManager().getError());
                break;
        }
    }
    
    
    @Override
    public void onDestroy() { 
                imServiceConnector.disconnect(this);
        super.onDestroy();
    }
}

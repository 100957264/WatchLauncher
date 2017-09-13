package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R; 
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.imservice.event.DeviceEvent; 
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector; 
import com.fise.xw.protobuf.IMDevice.ManageType;
import com.fise.xw.ui.base.TTBaseFragmentActivity; 

import de.greenrobot.event.EventBus;


/**
 *  添加设备界面
 */
public class AddDeviceActivity extends  TTBaseFragmentActivity{
	 
	private  AddDeviceActivity activity; 
	private  static IMService imService;
	private IMContactManager contactMgr; 
	private EditText device_name;
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
			        		 if(devList.get(i).getPinyinName().equals(device)){
			        			 isDev = true;
			        			 break;
			        		 }
			        	 } 
			        	 
			        	 if(isDev == false){ 
			        		 imService.getDeviceManager().addDevice(device,ManageType.MANAGE_TYPE_ADD_DEVICE,null);
			        	 }else{
			                 Toast.makeText(AddDeviceActivity.this, "你已经添加该设备",
			                 	      Toast.LENGTH_SHORT).show();
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
        EventBus.getDefault().register(this);
        
        device_name =(EditText)findViewById(R.id.device_name);
        
     
        
        
        
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
    
    
    public void onEventMainThread(DeviceEvent event){
        switch (event){ 
        
            case USER_INFO_UPDATE_DEVICE_SUCCESS:    
            	AddDeviceActivity.this.finish();
                break;
            case USER_INFO_ADD_DEVICE_SUCCESS:    
            	AddDeviceActivity.this.finish();
                break;
                
            case USER_INFO_ADD_DEVICE_FAILED:    
                Toast.makeText(AddDeviceActivity.this, imService.getDeviceManager().getError(),
              	      Toast.LENGTH_SHORT).show();
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

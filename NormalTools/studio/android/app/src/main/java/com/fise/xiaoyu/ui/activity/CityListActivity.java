package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseData.AddressCity;
import com.fise.xiaoyu.protobuf.IMBaseData.AddressType;
import com.fise.xiaoyu.ui.adapter.CityAdspter;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 *  个人信息 城市列表
 */
public class CityListActivity extends  TTBaseActivity{
	
	private  ListView listView=null;  
	private  CityAdspter adapter;
	private  static IMService imService; 
    public   List<AddressCity>  cityList = new ArrayList<>();
	private  UserEntity loginInfo;
	private int provinceId;
	private String provinceName;
    
    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("config#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return ;
			}
			

	        loginInfo =  IMLoginManager.instance().getLoginInfo();
	        
			provinceId = CityListActivity.this.getIntent().getIntExtra(
						IntentConstant.KEY_PROVINCE_ID, 0); 
			
			provinceName= CityListActivity.this.getIntent().getStringExtra(
					IntentConstant.KEY_PROVINCE_NAME); 
			
			IMLoginManager.instance().AddressRequest(loginInfo.getPeerId(), 0, AddressType.ADDRESS_TYPE_CITY, provinceId);
			   
			listView = (ListView)findViewById(R.id.list);   
	       // IMLoginManager.instance().AddressRequest(loginInfo.getPeerId(), 0, AddressType.ADDRESS_TYPE_PROVINCE, 0);
	        
	        adapter = new CityAdspter(CityListActivity.this, cityList,imService);
	        listView.setAdapter(adapter);  
	        listView.setOnItemClickListener(new OnItemClickListener(){   
                @Override   
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,   
                        long arg3) { 
               	 if(arg2>0)
            	 {
               		//List<AddressProvince>  _provinceList = adapter.getProvinceList();
            		//int provinceId =  _provinceList.get(arg2 -2).getProvinceId(); 
            		//IMUIHelper.openCityListActivity(ProvinceListActivity.this, provinceId);
               		List<AddressCity>  _cityList = adapter.getCityList();
               		int cityId = _cityList.get(arg2 - 1).getCityId();
               		String CityName = _cityList.get(arg2 - 1).getCityName();
               		IMLoginManager.instance().AddressRequest(loginInfo.getPeerId(), "中国",provinceName,CityName);
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
         
        setContentView(R.layout.tt_activity_province); 
        imServiceConnector.connect(this); 

        
        Button icon_arrow =(Button)findViewById(R.id.icon_arrow);  
        icon_arrow.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) { 
	        	 CityListActivity.this.finish();
	         } 
         });
        
        
        TextView left_text =(TextView)findViewById(R.id.left_text);  
        left_text.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) { 
	        	 CityListActivity.this.finish();
	         } 
         });
         
        
    }
     
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(LoginEvent event){
        switch (event){ 
            case INFO_CITY_SUCCESS:
            	List<AddressCity> cityList = IMLoginManager.instance().CityList();
            	adapter.putCityList(cityList);
                break;
            case INFO_ADDRESS_FAILED:
				Utils.showToast(CityListActivity.this,"地址修改失败");

            	
            	break; 
        }
    } 
	
	  @Subscribe(threadMode = ThreadMode.MAIN)
	  public void onMessageEvent(UserInfoEvent event){
	        switch (event){ 
	        
	        case USER_INFO_DATA_UPDATE:
	        	CityListActivity.this.finish();
	        	break; 
	                
	        }
	    }

	
    
    @Override
    public void onDestroy() { 
                imServiceConnector.disconnect(this);
        super.onDestroy();
    }
}

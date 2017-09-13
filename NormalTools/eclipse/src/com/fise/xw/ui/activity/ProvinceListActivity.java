package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View; 
import android.widget.AdapterView;
import android.widget.Button; 
import android.widget.ListView;
import android.widget.TextView; 
import android.widget.AdapterView.OnItemClickListener;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity; 
import com.fise.xw.imservice.event.LoginEvent; 
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseData.AddressProvince;
import com.fise.xw.protobuf.IMBaseData.AddressType;
import com.fise.xw.ui.adapter.ProvinceAdspter; 
import com.fise.xw.ui.base.TTBaseActivity; 
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;

/**
 *  设置个人信息的 省界面
 * @author weileiguan
 *
 */
public class ProvinceListActivity extends  TTBaseActivity{
	
	private  ListView listView=null;  
	private  ProvinceAdspter adapter;
	private  static IMService imService; 
    public   List<AddressProvince>  provinceList = new ArrayList<>();
	private  UserEntity loginInfo;
    
    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("config#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return ;
			}
			
			listView = (ListView)findViewById(R.id.list);  
	          
	         
	        loginInfo =  IMLoginManager.instance().getLoginInfo();
	        IMLoginManager.instance().AddressRequest(loginInfo.getPeerId(), 0, AddressType.ADDRESS_TYPE_PROVINCE, 0);
	        
	        adapter = new ProvinceAdspter(ProvinceListActivity.this, provinceList,imService);
	        listView.setAdapter(adapter);     
	        listView.setOnItemClickListener(new OnItemClickListener(){   
                @Override   
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,   
                        long arg3) {    
               	 if(arg2>1) 
            	 {
               		List<AddressProvince>  _provinceList = adapter.getProvinceList();
            		int provinceId =  _provinceList.get(arg2 -2).getProvinceId(); 
            		String provinceName = _provinceList.get(arg2 -2).getProvinceName(); 
            		IMUIHelper.openCityListActivity(ProvinceListActivity.this, provinceId,provinceName);
            	  	ProvinceListActivity.this.finish();    
            	 }else if(arg2 == 0){

            		 if(adapter.getCity()!=null&&(!adapter.getCity().equals(""))
            			&&adapter.getAdminArea()!=null&&(!adapter.getAdminArea().equals(""))){
            			IMLoginManager.instance().AddressRequest(loginInfo.getPeerId(), "中国",adapter.getAdminArea(),adapter.getCity());
            			ProvinceListActivity.this.finish();    
            		 }
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
        EventBus.getDefault().register(this);
        
        
        Button icon_arrow =(Button)findViewById(R.id.icon_arrow);  
        icon_arrow.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) { 
	        	 ProvinceListActivity.this.finish();
	         } 
         });
        
        
        TextView left_text =(TextView)findViewById(R.id.left_text);  
        left_text.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) { 
	        	 ProvinceListActivity.this.finish();
	         } 
         });
         
        
    }
     
	public void onEventMainThread(LoginEvent event){
        switch (event){
            case INFO_PROVINCE_SUCCESS:
            	List<AddressProvince> provinceList = IMLoginManager.instance().provinceList();
            	adapter.putProvinceList(provinceList);
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

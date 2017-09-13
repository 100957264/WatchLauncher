package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * 实时监护页面
 */
public class RealTimeInfoActivity extends  TTBaseFragmentActivity  implements OnGeocodeSearchListener{

	private  RealTimeInfoActivity activity; 
	private  static IMService imService;
	private IMContactManager contactMgr; 
	private UserEntity currentUser;
	private int currentUserId;
	private GeocodeSearch geocoderSearch;
	private TextView address;
	  
    
    static final String[] TELEPHONY_SIGNAL_STRENGTH = { "没信号","很弱", "弱", "一般",
		"好", "强" };
    
  
    
    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("config#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return ;
			}
			currentUserId = RealTimeInfoActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);

			if (currentUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);
			if (currentUser != null) {
				initDetailProfile();
			} 
			 
        }
        @Override
        public void onServiceDisconnected() {
        	
        }
    };
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        setContentView(R.layout.real_time_info_follow); 
        imServiceConnector.connect(this);
          
        activity = this;


		// 初始化搜索模块，注册事件监听
		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);
		
         
        Button icon_arrow =(Button)findViewById(R.id.icon_arrow);  
        icon_arrow.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) { 
	        	 RealTimeInfoActivity.this.finish();
	         } 
         });
        
        
        
         
    } 
    
    
	private void initDetailProfile() { 
		hideProgressBar(); 
		
		double jingdu = currentUser.getLongitude();
		double weidu = currentUser.getLatitude();
		
		LatLonPoint ptCenter = new LatLonPoint(weidu, jingdu);
		RegeocodeQuery query = new RegeocodeQuery(ptCenter, 200,
				GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
		
		
		
		address  = (TextView)findViewById(R.id.position_text);
		TextView electricity  = (TextView)findViewById(R.id.electricity_text);  
		electricity.setText(currentUser.getBattery()+"%");
		
		
		TextView signal_text  = (TextView)findViewById(R.id.signal_text);
		signal_text.setText(TELEPHONY_SIGNAL_STRENGTH[currentUser.getSignal()]);
		
		
        TextView black = (TextView)findViewById(R.id.black);
        black.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) { 
            	RealTimeInfoActivity.this.finish();
            }
        });
        
		 
        Button icon_arrow = (Button)findViewById(R.id.icon_arrow);
        icon_arrow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) { 
            	RealTimeInfoActivity.this.finish();
            }
        }); 
         
	}
	
	private void setTextViewContent(int id, String content) {
		TextView textView = (TextView)findViewById(id);
		if (textView == null) {
			return;
		}

		textView.setText(content);
	}
	
	private void hideProgressBar(){
		
		ProgressBar progressbar = (ProgressBar)findViewById(R.id.progress_bar);
		progressbar.setVisibility(View.GONE);
		 
		 
	}
	

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeviceEvent event){
        switch (event){  
            case USER_INFO_UPDATE_DEVICE_SUCCESS:    
                break;
            case USER_INFO_ADD_DEVICE_FAILED:    
                
                break;
                
        }
    }
     
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event){
        switch (event){ 
        
        case USER_INFO_UPDATE: 
        	UserEntity entity = imService.getContactManager().findContact(
        			currentUserId);
        	if (entity != null) {
        		currentUser = entity; 
        		initDetailProfile();
        	}
        	break; 
        }
    }
      
    @Override
    public void onDestroy() { 
                imServiceConnector.disconnect(this);
        super.onDestroy();
    }

 

	/**
	 * 地理编码查询回调
	 */
	@Override
	public void onGeocodeSearched(GeocodeResult result, int rCode) {
	}

	/**
	 * 逆地理编码回调
	 */
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		// dismissDialog();
		if (rCode == 1000) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {

				// String addressName =
				// result.getRegeocodeAddress().getFormatAddress()
				// + "附近";
				String addressName = result.getRegeocodeAddress()
						.getFormatAddress();
				address.setText(addressName);

			} else {

			}
		} else {

		}
	}
}

package com.fise.xw.ui.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.model.LatLng;
import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.app.IMApplication;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.event.UnreadEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMUnreadMsgManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.ui.fragment.ChatFragment;
import com.fise.xw.ui.fragment.ContactFragment;
import com.fise.xw.ui.widget.NaviTabButton;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;

/**
 *   App 登录之后的主界面
 * @author weileiguan
 *
 */
public class MainActivity extends TTBaseFragmentActivity implements AMapLocationListener{
	private Fragment[] mFragments;
	private NaviTabButton[] mTabButtons;
	private Logger logger = Logger.getLogger(MainActivity.class);
    private IMService imService;
    public static MainActivity  activity = null;
    public static int BatteryN = 0;       //目前电量 
    public static int signalN = 0;        //信号强度  
    
    public static  double latitude;
    public static double longitude;  
    
    private TelephonyManager        Tel;  
    private MyPhoneStateListener    MyListener;  
    private LatLng lat;
    
    
	// 声明mLocationOption对象
	public AMapLocationClientOption mLocationOption = null;
	private AMapLocationClient mlocationClient;
	
  
	private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService(); 
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

    
    /* 创建广播接收器 */  
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver()   
    {  
        public void onReceive(Context context, Intent intent)   
        {  
            String action = intent.getAction();  
            /* 
             * 如果捕捉到的action是ACTION_BATTERY_CHANGED， 就运行onBatteryInfoReceiver() 
             */  
            if (Intent.ACTION_BATTERY_CHANGED.equals(action))   
            {  
                BatteryN = intent.getIntExtra("level", 0);    //目前电量  
             
            }  
        }  
    };  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		logger.d("MainActivity#savedInstanceState:%s", savedInstanceState);
		//todo eric when crash, this will be called, why?
		if (savedInstanceState != null) {
			logger.w("MainActivity#crashed and restarted, just exit");
			jumpToLoginPage();
			finish();
		}
		 
		activity = this;
		registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));  
		  /* Update the listener, and start it */  
		   
        MyListener   = new MyPhoneStateListener();  
        Tel       = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);  
        Tel.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS); 
      
        // 在这个地方加可能会有问题吧
        EventBus.getDefault().register(this);
		imServiceConnector.connect(this); 
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tt_activity_main);
 
		initTab();
		initFragment();
		setFragmentIndicator(0);
		
		
		
		mlocationClient = new AMapLocationClient(this);
		// 初始化定位参数 
		mLocationOption = new AMapLocationClientOption();
		// 设置定位监听
		mlocationClient.setLocationListener(this);
		// 设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		// 设置定位间隔,单位毫秒,默认为2000ms
		mLocationOption.setInterval(2000);
		// 设置定位参数
		mlocationClient.setLocationOption(mLocationOption);
		// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
		// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
		// 在定位结束后，在合适的生命周期调用onDestroy()方法
		// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
		// 启动定位
		mlocationClient.startLocation();
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}

	
    @Override
    protected void onStart() {
        super.onStart();
         
    }
	@Override
	public void onBackPressed() {
		//don't let it exit
		//super.onBackPressed();

		//nonRoot	If false then this only works if the activity is the root of a task; if true it will work for any activity in a task.
		//document http://developer.android.com/reference/android/app/Activity.html

		//moveTaskToBack(true);

		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addCategory(Intent.CATEGORY_HOME);
		startActivity(i);

	} 


	private void initFragment() {
		mFragments = new Fragment[4];
		mFragments[0] = getSupportFragmentManager().findFragmentById(R.id.fragment_chat);
		mFragments[1] = getSupportFragmentManager().findFragmentById(R.id.fragment_contact);
		mFragments[2] = getSupportFragmentManager().findFragmentById(R.id.fragment_internal);
		mFragments[3] = getSupportFragmentManager().findFragmentById(R.id.fragment_my);
	}

	private void initTab() {
		mTabButtons = new NaviTabButton[4];

		mTabButtons[0] = (NaviTabButton) findViewById(R.id.tabbutton_chat);
		mTabButtons[1] = (NaviTabButton) findViewById(R.id.tabbutton_contact);
		mTabButtons[2] = (NaviTabButton) findViewById(R.id.tabbutton_internal);
		mTabButtons[3] = (NaviTabButton) findViewById(R.id.tabbutton_my);
             
		mTabButtons[0].setTitle(getString(R.string.main_chat));
		mTabButtons[0].setIndex(0);
		mTabButtons[0].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_sel));
		mTabButtons[0].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_nor));
                       
		mTabButtons[1].setTitle(getString(R.string.main_contact));
		mTabButtons[1].setIndex(1);
		mTabButtons[1].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_sel));
		mTabButtons[1].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_nor));

//		mTabButtons[2].setTitle(getString(R.string.main_innernet));
//		mTabButtons[2].setIndex(2);
//		mTabButtons[2].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_select));
//		mTabButtons[2].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_nor));
		
		mTabButtons[2].setTitle(getString(R.string.main_device));
		mTabButtons[2].setIndex(2);
		mTabButtons[2].setSelectedImage(getResources().getDrawable(R.drawable.icon_tt_sheb_press));
		mTabButtons[2].setUnselectedImage(getResources().getDrawable(R.drawable.icon_tt_sheb_nomal));
		

		mTabButtons[3].setTitle(getString(R.string.main_me_tab));
		mTabButtons[3].setIndex(3);
		mTabButtons[3].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_me_sel));
		mTabButtons[3].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_me_nor));
	}

	public void setFragmentIndicator(int which) {
		getSupportFragmentManager().beginTransaction().hide(mFragments[0]).hide(mFragments[1]).hide(mFragments[2]).hide(mFragments[3]).show(mFragments[which]).commit();

		mTabButtons[0].setSelectedButton(false);
		mTabButtons[1].setSelectedButton(false);
		mTabButtons[2].setSelectedButton(false);
		mTabButtons[3].setSelectedButton(false);

		mTabButtons[which].setSelectedButton(true);
		
		if(which == 1)
		{  
			IMUnreadMsgManager.instance().updateReqUnreadCount();
			showReqFriendsCount(); 
		} else if(which == 0){  
			showReqFriendsCount(); 
		}else if(which == 2){
			List<UserEntity> listUser = imService.getContactManager().getContactDevicesList();

			// 查看用户状态 是否在线
			ArrayList<Integer> userIdStats = new ArrayList<>();
			for(int i=0;i<listUser.size();i++){
				if(listUser.get(i)!=null){
					userIdStats.add(listUser.get(i).getPeerId());
				}
			} 
			imService.getContactManager().reqGetDetaillUsersStat( userIdStats);

						
		}
	
	}

	public void setUnreadMessageCnt(int unreadCnt) {
		mTabButtons[0].setUnreadNotify(unreadCnt);
	}
 


    /**双击事件*/
	public void chatDoubleListener() {
        setFragmentIndicator(0);
        ((ChatFragment) mFragments[0]).scrollToUnreadPosition();
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleLocateDepratment(intent);
	}



	private void handleLocateDepratment(Intent intent) {
		int departmentIdToLocate= intent.getIntExtra(IntentConstant.KEY_LOCATE_DEPARTMENT,-1);
		if (departmentIdToLocate == -1) {
			return;
		}

		logger.d("department#got department to locate id:%d", departmentIdToLocate);
		setFragmentIndicator(1);
		ContactFragment fragment = (ContactFragment) mFragments[1];
		if (fragment == null) {
			logger.e("department#fragment is null");
			return;
		}
		  
		fragment.locateDepartment(departmentIdToLocate);
	}

	@Override
	public void onPause() {
		super.onPause(); 
		Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE); 
	}
	

	@Override
	public void onResume() {
        super.onResume(); 
        Tel.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);  
	}
	
	@Override
	public void onStop() {
		super.onStop();
	} 
	@Override
	public void onDestroy() {
		logger.d("mainactivity#onDestroy");
		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
		this.unregisterReceiver(mBatInfoReceiver);
        super.onDestroy();
        
	}



    public void onEventMainThread(UnreadEvent event){
        switch (event.event){
            case SESSION_READED_UNREAD_MSG:
            case UNREAD_MSG_LIST_OK:
            case UNREAD_MSG_RECEIVED:
                showUnreadMessageCount();
               // showUnreadReqCount();
                break;
        }
    } 
    
    public void onEventMainThread(UserInfoEvent event){
        switch (event){ 
        
            case USER_INFO_REQ_UPDATE: 
            	showReqFriendsCount();
               // showUnreadReqCount();
                break;
            case USER_INFO_WEI_DATA: 
            	showReqFriendsCount(); 
                break;
                       
        }
    }

    
//    public void onEventMainThread(UserInfoEvent event){
//        switch (event){  
//            case USER_INFO_AVATAR_UPDATE:    
//            	UserEntity user =  IMLoginManager.instance().getLoginInfo();
//            	IMContactManager.instance().ChangeAvatar(IMLoginManager.instance().getTempUrl(),user.getPeerId());
//                break; 
//                
//        }
//    }
    
    private void showUnreadMessageCount() {
        //todo eric when to
        if(imService!=null)
        {
            int unreadNum = IMUnreadMsgManager.instance().getTotalUnreadCount();
            mTabButtons[0].setUnreadNotify(unreadNum);
        } 
    }
    
    
    private void showReqFriendsCount() {
        //todo eric when to
        if(imService!=null)
        {
            int unreadNum = imService.getUnReadMsgManager().getTotalReqUnreadCount();  
            mTabButtons[1].setUnreadNotify(unreadNum);
        } 
    }
     

    
    

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (amapLocation != null) {
			if (amapLocation.getErrorCode() == 0) {
				// 定位成功回调信息，设置相关消息
				amapLocation.getLocationType();// 获取当前定位结果来源，如网络定位结果，详见定位类型表
				amapLocation.getLatitude();// 获取纬度
				amapLocation.getLongitude();// 获取经度
				amapLocation.getAccuracy();// 获取精度信息
				lat = new LatLng(amapLocation.getLatitude(),
						amapLocation.getLongitude());

				MainActivity.latitude = lat.latitude;
				MainActivity.longitude = lat.longitude;

				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date date = new Date(amapLocation.getTime());
				df.format(date);// 定位时间
			} else {
				// 显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
				Log.e("AmapError",
						"location Error, ErrCode:"
								+ amapLocation.getErrorCode() + ", errInfo:"
								+ amapLocation.getErrorInfo());
			}
		}
	}

	
	
	public void onEventMainThread(LoginEvent event){
        switch (event){
            case LOGIN_OUT:
                handleOnLogout();
                break;
        }
    } 

	private void handleOnLogout() {
		logger.d("mainactivity#login#handleOnLogout");
		finish();
		logger.d("mainactivity#login#kill self, and start login activity");
		jumpToLoginPage();

	}

	private void jumpToLoginPage() {
		
   	   SharedPreferences ww = getSharedPreferences(IntentConstant.KEY_LOGIN_NOT_AUTO, Activity.MODE_PRIVATE);
   	   SharedPreferences.Editor editor = ww.edit(); 
   	   editor.putBoolean("login_not_auto", false); 
   	   editor.commit(); 
   	 
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(IntentConstant.KEY_LOGIN_NOT_AUTO, false);
		startActivity(intent);
	}
	
	 
    private class MyPhoneStateListener extends PhoneStateListener 
    {  
           
      /* Get the Signal strength from the provider, each tiome there is an update  从得到的信号强度,每个tiome供应商有更新*/   
      @Override  
      public void onSignalStrengthsChanged(SignalStrength signalStrength){  
   
         super.onSignalStrengthsChanged(signalStrength);  
         signalN = signalStrength.getGsmSignalStrength();
             
      }   
    };/* End of private Class */  
}

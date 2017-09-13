package com.fise.xiaoyu.ui.activity;

import android.Manifest;
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
import android.view.WindowManager;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;
import com.fise.xiaoyu.DB.sp.LoginSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.UnreadEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.manager.IMNotificationManager;
import com.fise.xiaoyu.ui.base.ActivityManager;
import com.fise.xiaoyu.imservice.manager.IMUnreadMsgManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.ui.fragment.ChatFragment;
import com.fise.xiaoyu.ui.fragment.ContactFragment;
import com.fise.xiaoyu.ui.widget.NaviTabButton;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.PermissionUtil;
import com.fise.xiaoyu.utils.StatusBarUtil;
import com.fise.xiaoyu.utils.Utils;
import com.shortcutbadger.ShortcutBadger;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *   App 登录之后的主界面
 */
public class MainActivity extends TTBaseFragmentActivity implements AMapLocationListener{
	private Fragment[] mFragments;
	private NaviTabButton[] mTabButtons;
	private Logger logger = Logger.getLogger(MainActivity.class);
    private IMService imService;
    public static MainActivity  activity = null;
    public static int BatteryN = 0;       //目前电量 
    public static int signalN = 0;        //信号强度  
	public static int stepNum = 0;        //计步数量


    public static  double latitude;
    public static double longitude;
	public static int  locationType;


	public static String province;
	public static String cityName;
	public static String address="";


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
			jumpToLoginPage(false ,0);
			finish();
		}

		activity = this;
		registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		  /* Update the listener, and start it */

        MyListener   = new MyPhoneStateListener();
        Tel       = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        // 在这个地方加可能会有问题吧
        		imServiceConnector.connect(this);

	//	requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tt_activity_main);

		initTab();
		initFragment();
		setFragmentIndicator(0);


		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		checkLocaltionPermission();
	}

	private void checkLocaltionPermission() {
		requestRunPermisssion(new String[]{
				Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.ACCESS_FINE_LOCATION,
		}, new PermissionListener() {
			@Override
			protected void onGranted() {
				if(Logger.DEBUG_LOCATION) {
					startLocation();
				}
			}

			@Override
			protected void onDenied(List<String> deniedPermission) {
				Toast.makeText(MainActivity.this, PermissionUtil.getPermissionString(MainActivity.this, deniedPermission), Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void startLocation() {
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

	}


	@Override
    protected void onStart() {
        super.onStart();

    }
	@Override
	public void onBackPressed() {
		//don't let it exit
		//super.onBackPressed();

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
		mTabButtons[2].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_select));
		mTabButtons[2].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_nor));


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

		}else if(which == 3)
		{
			IMUnreadMsgManager.instance().updateReqUnreadYuFriendsCount();
			IMUnreadMsgManager.instance().updateParentRefuseCount();
			showReqYuFriendsCount();
		}

		StatusBarUtil.transparencyBar1(this);

	}


	public void setUnreadMessageCnt(int unreadCnt) {
		mTabButtons[0].setUnreadNotify(unreadCnt);
	}


	public void setUnreadYuReqCnt(int unreadCnt) {
		mTabButtons[3].setUnreadNotify(unreadCnt);
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
	    int  fragment_id = getIntent().getIntExtra("fragment_id" , -1);
		if(fragment_id != -1){
			setFragmentIndicator(fragment_id);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}
	@Override
	public void onDestroy() {
		logger.d("mainactivity#onDestroy");
				imServiceConnector.disconnect(this);
		this.unregisterReceiver(mBatInfoReceiver);
        super.onDestroy();

	}



	@Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UnreadEvent event){
        switch (event.event){
            case SESSION_READED_UNREAD_MSG:
            case UNREAD_MSG_LIST_OK:
            case UNREAD_MSG_RECEIVED:
                showUnreadMessageCount();
               // showUnreadReqCount();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event){
        switch (event){

            case USER_INFO_REQ_UPDATE:
            	showReqFriendsCount();
				showReqYuFriendsCount();
               // showUnreadReqCount();
                break;
            case USER_INFO_WEI_DATA:
            	showReqFriendsCount();
				showReqYuFriendsCount();
                break;

			/*
			case RINGER_MODE_SILENT:
				Utils.showToast(MainActivity.this,"模式为　Silent");
				break;

			case RINGER_MODE_NORMAL:
				Utils.showToast(MainActivity.this,"模式为　Normal");
				break;

			case RINGER_MODE_VIBRATE:
				Utils.showToast(MainActivity.this,"模式为　Vibrate");
				break;
				*/
			case USER_BE_KICK_OUT_IOS_LOGIN:
            case USER_BE_KICK_OUT_ANDROID_LOGIN:
                IMLoginManager.instance().setKickout(false);
                IMLoginManager.instance().resetOut();
                jumpToLoginPage(true , event.ordinal());
                MainActivity.this.finish();

                break;



        }
    }

    private void showUnreadMessageCount() {
        //todo eric when to
        if(imService!=null) {

            int unreadNum = IMUnreadMsgManager.instance().getTotalUnreadCount();
            mTabButtons[0].setUnreadNotify(unreadNum);
			//显示桌面未读条数
            if(unreadNum > 0){
                boolean success = ShortcutBadger.applyCount(MainActivity.this, unreadNum);
			}else{
				boolean success = ShortcutBadger.removeCount(MainActivity.this);
			}
        }
    }


    private void showReqFriendsCount() {
        //todo eric when to
        if(imService!=null) {
            int unreadNum = imService.getUnReadMsgManager().getTotalReqUnreadCount();
            mTabButtons[1].setUnreadNotify(unreadNum);
        }
    }

	private void showReqYuFriendsCount() {
		//todo eric when to
		if(imService!=null) {
			int unreadNum = imService.getUnReadMsgManager().getTotalReqYuUnreadCount();
			mTabButtons[3].setUnreadNotify(unreadNum);
		}
	}





	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (amapLocation != null) {
			if (amapLocation.getErrorCode() == 0) {
				// 定位成功回调信息，设置相关消息
				// 获取当前定位结果来源，如网络定位结果，详见定位类型表
				amapLocation.getLatitude();// 获取纬度
				amapLocation.getLongitude();// 获取经度
				amapLocation.getAccuracy();// 获取精度信息

				lat = new LatLng(amapLocation.getLatitude(),
						amapLocation.getLongitude());

				MainActivity.latitude = lat.latitude;
				MainActivity.longitude = lat.longitude;
				MainActivity.locationType =  amapLocation.getLocationType();


				address = amapLocation.getAddress();
				MainActivity.province  = amapLocation.getProvince();
				MainActivity.cityName  = amapLocation.getCity();



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



	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(LoginEvent event){
        switch (event){
			case LOCAL_LOGIN_SUCCESS:
			case LOGIN_OK:
				break;
            case LOGIN_OUT:
                handleOnLogout();
                break;
        }
    }


	private void handleOnLogout() {
		logger.d("mainactivity#login#handleOnLogout");
		finish();
		logger.d("mainactivity#login#kill self, and start login activity");
		jumpToLoginPage(false , 0);

	}

	private void jumpToLoginPage(Boolean isKickOut ,int kickoutDev) {

	   SharedPreferences ww = getSharedPreferences(IntentConstant.KEY_LOGIN_NOT_AUTO, Activity.MODE_PRIVATE);
	   SharedPreferences.Editor editor = ww.edit();
	   editor.putBoolean("login_not_auto", false);
	   editor.commit();
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(IntentConstant.KEY_LOGIN_NOT_AUTO, false);
		intent.putExtra(IntentConstant.KEY_IS_KICK_OUT, isKickOut);

		SharedPreferences wwKick = getSharedPreferences(IntentConstant.KEY_LOGIN_NOT_AUTO, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editorKick = wwKick.edit();
		editorKick.putBoolean("login_kick", isKickOut);
		editorKick.commit();


		if(isKickOut){
			intent.putExtra(IntentConstant.KEY_IS_KICK_OUT_DEVICE_TYPE, kickoutDev);
			LoginSp.instance().setLoginInfo(LoginSp.instance().getLoginIdentity().getLoginName(), null, LoginSp.instance().getLoginIdentity().getLoginId(), LoginSp.instance().getLoginIdentity().getImei());
			ActivityManager.getInstance().finishAllActivityExcept(this);

			IMNotificationManager.instance().cancelAllNotifications();
		}
		startActivity(intent);

		MainActivity.this.finish();
	}


    private class MyPhoneStateListener extends PhoneStateListener {

      /* Get the Signal strength from the provider, each tiome there is an update  从得到的信号强度,每个tiome供应商有更新*/
      @Override
      public void onSignalStrengthsChanged(SignalStrength signalStrength){

         super.onSignalStrengthsChanged(signalStrength);
         signalN = signalStrength.getGsmSignalStrength();

      }
    };/* End of private Class */

}

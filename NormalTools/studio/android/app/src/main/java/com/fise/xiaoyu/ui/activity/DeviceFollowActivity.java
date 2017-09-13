package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearch.OnWeatherSearchListener;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ClientType;
import com.fise.xiaoyu.ui.adapter.DeviceGridAdapter;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.DeviceScrollView;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.ui.widget.MyGridView;
import com.fise.xiaoyu.utils.FileUtil;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.StatusBarUtil;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * 设备中设备信息界面
 */
@SuppressLint("NewApi")
public class DeviceFollowActivity extends TTBaseActivity implements
		OnWeatherSearchListener, OnGeocodeSearchListener {

	private Logger logger = Logger.getLogger(DeviceFollowActivity.class);
	private Handler uiHandler = new Handler();
	private IMService imService;
	private int deviceUserId;
	private ProgressBar progress_bar;
	private UserEntity currentDevice;
	private DeviceEntity rsp;

	static final String[] SIGNAL_STRENGTH_NAMES = { "无",  "差", "良",
			"好", "强" };

	private MyGridView gridview;
	private DeviceGridAdapter adapter;

	private int mPhoneSignalIconId;
	private int[] iconList;
	private int iconLevel;

	private int[] electricityList;
	private DeviceScrollView scrollView;


	// GSM/UMTS
	static final int[][] TELEPHONY_SIGNAL_STRENGTH = {

	{ R.drawable.dev_signal_0, R.drawable.dev_signal_1,
			R.drawable.dev_signal_2,
			R.drawable.dev_signal_3,
			R.drawable.dev_signal_4,
			R.drawable.dev_signal_5 } };


	static final int[][] WATCH_SIGNAL_STRENGTH = {

			{ R.drawable.watch_sys_signal_ls_full, R.drawable.watch_sys_signal_ls_1,
					R.drawable.watch_sys_signal_ls_2,
					R.drawable.watch_sys_signal_ls_3,
					R.drawable.watch_sys_signal_ls_4,
					R.drawable.watch_sys_signal_ls_5 } };



	static final int[][] WATCH_SIGNAL_ELECTRICITY = {

			{ R.drawable.icon_electricity_0, R.drawable.icon_electricity_1_red,
					R.drawable.icon_electricity_1,
					R.drawable.icon_electricity_2,
					R.drawable.icon_electricity_3,
					R.drawable.icon_electricity_4 ,
					R.drawable.icon_electricity_charging,} };



	 final int[][] CARD_SIGNAL_ELECTRICITY = {

			{ R.drawable.card_electricity_0, R.drawable.card_electricity_1_red,
					R.drawable.card_electricity_1,
					R.drawable.card_electricity_2,
					R.drawable.card_electricity_3,
					R.drawable.card_electricity_4 ,R.drawable.card_electricity__charging,} };


	private WeatherSearchQuery mquery;
	private WeatherSearch mweathersearch;
	private LocalWeatherLive weatherlive;
	private ImageView fenxiang_weather;
	private TextView weather_text;
	private GeocodeSearch geocoderSearch;
	private TextView fenxiang_weather_text;
	private TextView postion_text;
	private TextView dev_name_text;

	private RelativeLayout information;
	private LinearLayout information_bg;
	private RelativeLayout top_color;
	private LinearLayout device_follow_info;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onServiceDisconnected() {
		}

		@Override
		public void onIMServiceConnected() {
			logger.d("login#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			try {
				do {
					if (imService == null) {
						// 后台服务启动链接失败
						break;
					}

					deviceUserId = DeviceFollowActivity.this.getIntent()
							.getIntExtra(IntentConstant.KEY_PEERID, 0);

					currentDevice = imService.getContactManager()
							.findDeviceContact(deviceUserId);
					if (currentDevice == null) {
						return;
					}

					progress_bar.setVisibility(View.GONE);

					if(currentDevice!=null){
						// 请求信息
						ArrayList<Integer> userIds = new ArrayList<>(1);
						// just single type
						userIds.add(deviceUserId);
						IMContactManager.instance().reqGetDetaillUsers(userIds);
					}


					rsp = imService.getDeviceManager().findDeviceCard(deviceUserId);
					if (rsp == null) {
						return;
					}

					scrollView.setScrollData(imService,currentDevice,rsp);
					gridview = (MyGridView) findViewById(R.id.gridview);

					if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE)   {

//						String[] img_text = { "位置信息",  "联系小位", "安全围栏", "通话", "同步数据" ,"计步"};
						String[] img_text = { "位置信息", "安全围栏", "更新数据" , "通话", "联系小雨",  "计步"};

						int[] imgs = { R.drawable.icon_shebei_position,R.drawable.icon_security_fence, 	R.drawable.icon_synchronous_data,
								R.drawable.icon_conversation,R.drawable.icon_contact_bit,R.drawable.icon_motor_step }; // R.color.transparent

						adapter = new DeviceGridAdapter(DeviceFollowActivity.this,
								currentDevice, rsp,imService.getLoginManager().getLoginInfo(),imService ,img_text,imgs);



						//儿童手表区分定位卡片机
						gridview.setBackgroundResource(R.drawable.children_watch_bj);
						information.setBackgroundResource(R.drawable.bg_shebei_watch);
						information_bg.setBackgroundResource(R.color.default_bk);
						top_color.setBackgroundResource(R.color.default_bk);
						device_follow_info.setBackgroundResource(R.color.default_bk);
					}

					gridview.setAdapter(adapter);
					gridview.setOnItemClickListener(adapter);

					if (currentDevice != null) {
                        initbaseprofile();
					}


					// guan  新需求  提示是否在线
//					if(currentDevice.getOnLine() != DBConstant.ONLINE){
//						Utils.showToast(DeviceFollowActivity.this, "设备不在线");
//					}

					/*
					// 查看用户状态 是否在线
					ArrayList<Integer> userIdStats = new ArrayList<>(1);
					// just single type
					userIdStats.add(deviceUserId);
					imService.getContactManager().reqGetDetaillUsersStat(
							userIdStats);
					*/
					
					return;
				} while (false);

				// 异常分支都会执行这个
				// handleNoLoginIdentity();
			} catch (Exception e) {
				// 任何未知的异常
				logger.w("loadIdentity failed");
				// handleNoLoginIdentity();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_activity_device_detail_follow);
		progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
		imServiceConnector.connect(DeviceFollowActivity.this);

		scrollView  = (DeviceScrollView) findViewById(R.id.device_scroll);
		fenxiang_weather = (ImageView) findViewById(R.id.fenxiang_weather);
		weather_text = (TextView) findViewById(R.id.weather_text);
		fenxiang_weather_text = (TextView) findViewById(R.id.fenxiang_weather_text);

		postion_text = (TextView) findViewById(R.id.postion_text);
		dev_name_text = (TextView) findViewById(R.id.dev_name_text);



		information = (RelativeLayout) findViewById(R.id.information);
		information_bg = (LinearLayout) findViewById(R.id.information_bg);

		top_color = (RelativeLayout) findViewById(R.id.top_color);
		device_follow_info = (LinearLayout) findViewById(R.id.device_follow_info);


		LinearLayout icon_arrow_layout = (LinearLayout) findViewById(R.id.icon_arrow_layout);
		icon_arrow_layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DeviceFollowActivity.this.finish();
			}
		});

		LinearLayout icon_user_info = (LinearLayout) findViewById(R.id.icon_user_info);
		icon_user_info.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				IMUIHelper.openDeviceProfileActivity(DeviceFollowActivity.this,
						deviceUserId);
			}
		});

	}

	private void updateChange() {

		ImageView icon_electricity = (ImageView) findViewById(R.id.icon_electricity);

		if((currentDevice!=null)&&(currentDevice.getOnLine() == DBConstant.ONLINE) && rsp.getCharge() == DBConstant.BEGIN_CHARGING){

			if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE) {
				icon_electricity.setBackgroundResource(R.drawable.icon_electricity_charge);
			}else{
				icon_electricity.setBackgroundResource(R.drawable.icon_dev_electricity_charge);
			}

		}else{

			if (currentDevice.getBattery() <= 0) {
				icon_electricity.setBackgroundResource(electricityList[0]);
			} else if (currentDevice.getBattery() > 0
					&& currentDevice.getBattery() < 20) {
				icon_electricity.setBackgroundResource(electricityList[1]);
			} else if (currentDevice.getBattery() >= 20
					&& currentDevice.getBattery() < 25) {
				icon_electricity.setBackgroundResource(electricityList[2]);
			} else if (currentDevice.getBattery() >= 25
					&& currentDevice.getBattery() < 50) {
				icon_electricity.setBackgroundResource(electricityList[3]);
			} else if (currentDevice.getBattery() >= 50
					&& currentDevice.getBattery() < 75) {
				icon_electricity.setBackgroundResource(electricityList[4]);
			}else if(currentDevice.getBattery() >= 75
                    && currentDevice.getBattery() <= 100){
				icon_electricity.setBackgroundResource(electricityList[5]);
			}else{
				//TODO chong dian
				icon_electricity.setBackgroundResource(electricityList[6]);
			}
		}
	}
	private void initbaseprofile() {
		logger.d("detail#initBaseProfile");
		IMBaseImageView portraitImageView = (IMBaseImageView) findViewById(R.id.user_portrait);


		portraitImageView.setCorner(360);
		// 头像设置
		if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE) {
			portraitImageView.setBackgroundResource(R.drawable.icon_head);
		}
		portraitImageView.setImageUrl(currentDevice.getAvatar());


		portraitImageView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				IMUIHelper.openDeviceSettingActivity(DeviceFollowActivity.this,
						deviceUserId);
			}
		});

		if ((currentDevice.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)
				&& (currentDevice.getOnLine() != DBConstant.ONLINE)) {

			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(0);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
			portraitImageView.setColorFilter(filter);
		} else {
			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(1);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
			portraitImageView.setColorFilter(filter);
		}

		// 显示昵称
		dev_name_text.setText(currentDevice.getMainName());



		//小雨手机
		if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE) {
			iconList = WATCH_SIGNAL_STRENGTH[0];
			electricityList = WATCH_SIGNAL_ELECTRICITY[0];
		}else{
			iconList = TELEPHONY_SIGNAL_STRENGTH[0];
			electricityList = CARD_SIGNAL_ELECTRICITY[0];
		}



		mPhoneSignalIconId = R.drawable.dev_signal_0;
		iconLevel = currentDevice.getSignal();// getIconLevel(currentDevice.getSignal());
		if (iconLevel >= 5) {
			if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE) {
				mPhoneSignalIconId = R.drawable.watch_sys_signal_ls_5;
			}else{
				mPhoneSignalIconId = R.drawable.dev_signal_5;
			}

		} else {
			mPhoneSignalIconId = iconList[iconLevel];
		}

		ImageView mSignalImageView = (ImageView) findViewById(R.id.level_icon_signal);
		mSignalImageView.setBackgroundResource(mPhoneSignalIconId);

		TextView signal_text = (TextView) findViewById(R.id.signal_text);
		if (iconLevel >= 5) {
			signal_text.setText("强");

		} else {

			signal_text.setText(SIGNAL_STRENGTH_NAMES[iconLevel]);
		}

		ImageView icon_electricity = (ImageView) findViewById(R.id.icon_electricity);
		// icon_electricity.setProgress(currentDevice.getBattery());


		if((currentDevice!=null)&&(currentDevice.getOnLine() == DBConstant.ONLINE)&&rsp.getCharge() == DBConstant.BEGIN_CHARGING){

			if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE) {
				icon_electricity.setBackgroundResource(R.drawable.icon_electricity_charge);
			}else{
				icon_electricity.setBackgroundResource(R.drawable.icon_dev_electricity_charge);
			}


		}else{


			if (currentDevice.getBattery() <= 0) {
				icon_electricity.setBackgroundResource(electricityList[0]);
			} else if (currentDevice.getBattery() > 0 && currentDevice.getBattery() < 20) {
				//红色
				icon_electricity.setBackgroundResource(electricityList[1]);

			} else if (currentDevice.getBattery() >= 20
					&& currentDevice.getBattery() < 25) {
				//1
				icon_electricity.setBackgroundResource(electricityList[2]);
			} else if (currentDevice.getBattery() >= 25
					&& currentDevice.getBattery() < 50) {
				//2
				icon_electricity.setBackgroundResource(electricityList[3]);
			} else if (currentDevice.getBattery() >= 50
					&& currentDevice.getBattery() < 75) {
				//3
				icon_electricity.setBackgroundResource(electricityList[4]);
			}else if( currentDevice.getBattery() >= 75
					&& currentDevice.getBattery() <= 100){
				icon_electricity.setBackgroundResource(electricityList[5]);
			}else{
				//TODO chong dian
				icon_electricity.setBackgroundResource(electricityList[6]);
			}
		}


		TextView electricity_text_type = (TextView) findViewById(R.id.electricity_text);
		electricity_text_type.setText(currentDevice.getBattery() + "%");

		// longitude = currentDevice.getLongitude();
		// latitude = currentDevice.getLatitude();
			
		LatLonPoint latLonPoint = new LatLonPoint(currentDevice.getLatitude(),
				currentDevice.getLongitude());

		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);
		

         
	  if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE) {
			TextView dev_name = (TextView) findViewById(R.id.dev_name);
			dev_name.setText("小雨手机");
			dev_name.setTextColor(getResources().getColor(R.color.device_info_left));

			ImageView icon_arrow = (ImageView) findViewById(R.id.icon_arrow);
			icon_arrow.setBackgroundResource(R.drawable.nav_balck);



			ImageView icon_user_info = (ImageView) findViewById(R.id.icon_user_info_icon);
			icon_user_info.setBackgroundResource(R.drawable.nav_set_up);

			TextView left_text = (TextView) findViewById(R.id.left_text);
			left_text.setTextColor(getResources().getColor(R.color.device_info_left));

			ImageView postion_text_image = (ImageView) findViewById(R.id.postion_text_image);
			postion_text_image.setBackgroundResource(R.drawable.watch_icon_location);

			TextView postion_text = (TextView) findViewById(R.id.postion_text);
			postion_text.setTextColor(getResources().getColor(R.color.device_info_color));




			signal_text.setTextColor(getResources().getColor(R.color.device_info_color));
			electricity_text_type.setTextColor(getResources().getColor(R.color.device_info_color));
			weather_text.setTextColor(getResources().getColor(R.color.device_info_color));
			fenxiang_weather_text.setTextColor(getResources().getColor(R.color.device_info_color));
			dev_name_text.setTextColor(getResources().getColor(R.color.device_info_color));

			StatusBarUtil.transparencyBar1(this);

			//天气图
			ImageView shebei_temperature = (ImageView) findViewById(R.id.shebei_temperature);
			shebei_temperature.setBackgroundResource(R.drawable.watch_temperature);

			ImageView user_portrait_housing = (ImageView) findViewById(R.id.user_portrait_housing);
			user_portrait_housing.setVisibility(View.VISIBLE);

			fenxiang_weather.setBackgroundResource(R.drawable.icon_sun);
		}


		//如果是设备
		 /*
		if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE) {
			
			LatLng sourceLatLng  =  new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
			CoordinateConverter converter  = new CoordinateConverter(); 
			// CoordType.GPS 待转换坐标类型
			converter.from(CoordType.GPS); 
			// sourceLatLng待转换坐标点 LatLng类型
			converter.coord(sourceLatLng); 
			// 执行转换操作
			LatLng desLatLng = converter.convert();
			LatLonPoint desPoint = new LatLonPoint(desLatLng.latitude,
					desLatLng.longitude); 
			getAddress(desPoint);

		}else{
			getAddress(latLonPoint);
		}
		*/

		getAddress(latLonPoint);

	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {
		case USER_INFO_UPDATE:
			currentDevice = imService.getContactManager()
					.findDeviceContact(deviceUserId);
			rsp = imService.getDeviceManager().findDeviceCard(deviceUserId);
			if (currentDevice != null) {
                initbaseprofile();
			}
			break;
		case USER_COMMAND_TYPE_DEVICE_SHUTDOWN:
			Utils.showToast(DeviceFollowActivity.this, "断电指令成功");
			break;

		case USER_INFO_UPDATE_STAT:
			currentDevice = imService.getContactManager()
					.findDeviceContact(deviceUserId);
			rsp = imService.getDeviceManager().findDeviceCard(deviceUserId);
			if (currentDevice != null) {
                initbaseprofile();

			}
			break;

        case USER_P2PCOMMAND_OFFLINE_HINT:
            Utils.showToast(this, "对方不在线");
            break;

		case USER_COMMAND_TYPE_DEVICE_CALLBACK:
			//
			Utils.showToast(DeviceFollowActivity.this, "您的回拨指令发送成功");
			break;
//        case USER_P2PCOMMAND_OFFLINE_HINT:
//
//            Utils.showToast(DeviceFollowActivity.this, "对方不在线");
//            break;
			
		case USER_COMMAND_TYPE_DEVICE_CURRENT:
			Utils.showToast(DeviceFollowActivity.this, "正在更新最新数据，请稍后");
			break;
			 
		case USER_INFO_DEVE_BATTY_SUCCESS:
			//Utils.showToast(DeviceFollowActivity.this.getApplicationContext(), "同步数据成功");
			
			currentDevice = imService.getContactManager()
					.findDeviceContact(deviceUserId);
			rsp = imService.getDeviceManager().findDeviceCard(deviceUserId);
			if (currentDevice != null) {
                initbaseprofile();
			}
			break;


			case USER_INFO_UPDATE_QUERY_SUCCESS:
				currentDevice = imService.getContactManager()
						.findDeviceContact(deviceUserId);
				rsp = imService.getDeviceManager().findDeviceCard(deviceUserId);
				if (currentDevice != null) {
                    initbaseprofile();
				}
				break;



		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(DeviceFollowActivity.this);
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
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(DeviceEvent event) {
		switch (event) { 
		case USER_INFO_SETTING_DEVICE_SUCCESS:
			UserEntity entity = imService.getContactManager()
					.findDeviceContact(deviceUserId);
			rsp = imService.getDeviceManager().findDeviceCard(deviceUserId);
			if (entity != null) {
				currentDevice = entity;
				adapter.setCurrentDevice(currentDevice);

                initbaseprofile();
			}
			break;

			case USER_INFO_UPDATE_DEVICE_SUCCESS:{
				currentDevice = imService.getContactManager()
						.findDeviceContact(deviceUserId);
				rsp = imService.getDeviceManager().findDeviceCard(deviceUserId);
				if (currentDevice != null) {
					initbaseprofile();
				}
			}
			 break;



			case DEVICE_CHARGE_UPDATE_SUCCESS:
				currentDevice = imService.getContactManager().findDeviceContact(
						deviceUserId);
				if (currentDevice == null) {
					return;
				}
				rsp = imService.getDeviceManager().findDeviceCard(deviceUserId);
				if (rsp != null) {
                    updateChange();
				}
				break;
		}
	}

	@Override
	public void onWeatherForecastSearched(LocalWeatherForecastResult arg0,
			int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult,
			int rCode) {
		// TODO Auto-generated method stub

		if (rCode == 1000) {
			if (weatherLiveResult != null
					&& weatherLiveResult.getLiveResult() != null) {

				weatherlive = weatherLiveResult.getLiveResult();
				fenxiang_weather_text.setText(weatherlive.getWeather());

				if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE) {
					FileUtil.setWatchWeather(fenxiang_weather, weatherlive.getWeather());
				}else{
					FileUtil.setDevWeather(fenxiang_weather, weatherlive.getWeather());
				}

				// fenxiang_weather.setText(weatherlive.getWeather());
				weather_text.setText(weatherlive.getTemperature() + "℃");
			} else {
				// Utils.showToast(DeviceFollowActivity.this, "" + rCode);
			}
		} else {
			// Utils.showToast(DeviceFollowActivity.this, "" + rCode);
		}
	}

	/**
	 * 逆地理编码回调
	 */
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		if (rCode == 1000) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {

				
				//天气
				String cityName = result.getRegeocodeAddress().getCity();
				mquery = new WeatherSearchQuery(cityName,
						WeatherSearchQuery.WEATHER_TYPE_LIVE);
 
				/*

				if ((currentDevice.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)
						&& (currentDevice.getOnLine() != DBConstant.ONLINE)) {
 
					postion_text.setText(""
							+ result.getRegeocodeAddress().getFormatAddress() + "(最后一次在线位置)");
					
				} else {
					 
					postion_text.setText(""
							+ result.getRegeocodeAddress().getFormatAddress() +"(当前位置)");
					
				}
				*/
				postion_text.setText(""
						+ result.getRegeocodeAddress().getFormatAddress());
				
				//
				mweathersearch = new WeatherSearch(this);
				mweathersearch.setOnWeatherSearchListener(this);
				mweathersearch.setQuery(mquery);
				mweathersearch.searchWeatherAsyn(); // 异步搜索

			}
		}
	}

	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * 响应逆地理编码
	 */
	public void getAddress(final LatLonPoint latLonPoint) {
		RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
				GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
	}

}

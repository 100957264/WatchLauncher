package com.fise.xw.ui.activity;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.CoordinateConverter;
import com.amap.api.maps2d.CoordinateConverter.CoordType;
import com.amap.api.maps2d.model.LatLng;
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
import com.fise.xw.R; 
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.manager.IMUserActionManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.protobuf.IMBaseDefine.CommandType;
import com.fise.xw.protobuf.IMBaseDefine.SessionType;
import com.fise.xw.ui.adapter.DeviceGridAdapter;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.widget.IMBaseImageView;
import com.fise.xw.ui.widget.MyGridView;
import com.fise.xw.utils.FileUtil;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;

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

	static final String[] SIGNAL_STRENGTH_NAMES = { "没信号", "很弱", "弱", "一般",
			"好", "强" };

	private MyGridView gridview;
	private DeviceGridAdapter adapter;

	private int mPhoneSignalIconId;
	private int[] iconList;
	private int iconLevel;

	// GSM/UMTS
	static final int[][] TELEPHONY_SIGNAL_STRENGTH = {

	{ R.drawable.dev_signal_0, R.drawable.dev_signal_1,
			R.drawable.dev_signal_2,
			R.drawable.dev_signal_3,
			R.drawable.dev_signal_4,
			R.drawable.dev_signal_5 } };

	private WeatherSearchQuery mquery;
	private WeatherSearch mweathersearch;
	private LocalWeatherLive weatherlive;
	private ImageView fenxiang_weather;
	private TextView weather_text;
	private GeocodeSearch geocoderSearch;
	private TextView fenxiang_weather_text;
	private TextView postion_text;
	private TextView dev_name_text;

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

					 
					progress_bar.setVisibility(View.GONE);

					rsp = imService.getDeviceManager().findDeviceCard(
							deviceUserId);
					if (rsp == null) {
						return;
					}

					gridview = (MyGridView) findViewById(R.id.gridview);
					
					if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE) {
						 String[] img_text = { "位置信息", "静默监听", "联系设备", "历史轨迹 ", "通话", "同步数据" };
						 int[] imgs = { R.drawable.icon_dingwdh_jingrxw,
								R.drawable.icon_dingwdh_jingmjt, R.drawable.icon_dingwdh_baojg,
								R.drawable.icon_dingwdh_shilgj, R.drawable.icon_dingwdh_tongh,
								R.drawable.icon_dingwdh_tongbsj }; // R.color.transparent

						adapter = new DeviceGridAdapter(DeviceFollowActivity.this,
								currentDevice, rsp,imService.getLoginManager().getLoginInfo(),img_text,imgs);
						
						//电动车
					}else if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_FISE_CAR_VALUE) {
						
						 String[] img_text = { "位置信息", "静默监听", "联系设备", "历史轨迹 ", "通话", "同步数据" ,"断电控制"};
						 int[] imgs = { R.drawable.icon_dingwdh_jingrxw,
								R.drawable.icon_dingwdh_jingmjt, R.drawable.icon_dingwdh_baojg,
								R.drawable.icon_dingwdh_shilgj, R.drawable.icon_dingwdh_tongh,
								R.drawable.icon_dingwdh_tongbsj,R.drawable.icon_dingwdh_tongbsj }; // R.color.transparent

						adapter = new DeviceGridAdapter(DeviceFollowActivity.this,
								currentDevice, rsp,imService.getLoginManager().getLoginInfo(),img_text,imgs);
					}
					 
					
					gridview.setAdapter(adapter);
					gridview.setOnItemClickListener(adapter);

					if (currentDevice != null) {
						initBaseProfile();
					}

					// 请求信息
					ArrayList<Integer> userIds = new ArrayList<>(1);
					// just single type
					userIds.add(deviceUserId);
					IMContactManager.instance().reqGetDetaillUsers(userIds);

					
					// guan  新需求  提示是否在线  
					if(currentDevice.getOnLine() != DBConstant.ONLINE){
						Toast.makeText(DeviceFollowActivity.this, "定位卡片机不在线",
								Toast.LENGTH_SHORT).show();
					} else{
						//同步数据
						IMUserActionManager.instance().UserP2PCommand(
								IMLoginManager.instance().getLoginId(),
								currentDevice.getPeerId(),
								SessionType.SESSION_DEVICE_SINGLE, // SESSION_TYPE_SINGLE 
								CommandType.COMMAND_TYPE_DEVICE_CURRENT_INFO, "",false); 
					}
					
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
		EventBus.getDefault().register(this);

		fenxiang_weather = (ImageView) findViewById(R.id.fenxiang_weather);
		weather_text = (TextView) findViewById(R.id.weather_text);
		fenxiang_weather_text = (TextView) findViewById(R.id.fenxiang_weather_text);

		postion_text = (TextView) findViewById(R.id.postion_text);
		dev_name_text = (TextView) findViewById(R.id.dev_name_text);

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DeviceFollowActivity.this.finish();
			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DeviceFollowActivity.this.finish();
			}
		});

		
     RelativeLayout icon_user_user = (RelativeLayout) findViewById(R.id.icon_user_user);
		
		//Button icon_user_info = (Button) findViewById(R.id.icon_user_info);
		icon_user_user.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				IMUIHelper.openDeviceProfileActivity(DeviceFollowActivity.this,
						deviceUserId);
			}
		});

	}

	private void initBaseProfile() {
		logger.d("detail#initBaseProfile");
		IMBaseImageView portraitImageView = (IMBaseImageView) findViewById(R.id.user_portrait);
		
		// 头像设置 
		portraitImageView.setCorner(90);
		portraitImageView.setImageUrl(currentDevice.getAvatar());

		portraitImageView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				IMUIHelper.openDeviceSettingActivity(DeviceFollowActivity.this,
						deviceUserId);
			}
		});

		if ((currentDevice.getIsFriend() == DBConstant.FRIENDS_TYPE_WEI)
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

		iconList = TELEPHONY_SIGNAL_STRENGTH[0];

		mPhoneSignalIconId = R.drawable.dev_signal_0;
		iconLevel = currentDevice.getSignal();// getIconLevel(currentDevice.getSignal());
		if (iconLevel >= 5) {
			mPhoneSignalIconId = R.drawable.dev_signal_5;
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
		if (currentDevice.getBattery() <= 0) {
			icon_electricity.setBackgroundResource(R.drawable.dev_noelectric);
		} else if (currentDevice.getBattery() > 0
				&& currentDevice.getBattery() <= 15) {
			icon_electricity.setBackgroundResource(R.drawable.dev_electric_1);

		} else if (currentDevice.getBattery() > 15
				&& currentDevice.getBattery() <= 30) {
			icon_electricity.setBackgroundResource(R.drawable.dev_electric_2);

		} else if (currentDevice.getBattery() > 30
				&& currentDevice.getBattery() <= 45) {

			icon_electricity.setBackgroundResource(R.drawable.dev_electric_3);

		} else if (currentDevice.getBattery() > 45
				&& currentDevice.getBattery() <= 60) {

			icon_electricity.setBackgroundResource(R.drawable.dev_electric_4);
		} else if (currentDevice.getBattery() > 60
				&& currentDevice.getBattery() <= 75) {

			icon_electricity.setBackgroundResource(R.drawable.dev_electric_5);
		} else if (currentDevice.getBattery() > 75
				&& currentDevice.getBattery() < 90) {

			icon_electricity.setBackgroundResource(R.drawable.dev_electric_6);
		} else {
			icon_electricity.setBackgroundResource(R.drawable.dev_electric_7);
		}

		TextView electricity_text_type = (TextView) findViewById(R.id.electricity_text);
		electricity_text_type.setText(currentDevice.getBattery() + "%");

		// longitude = currentDevice.getLongitude();
		// latitude = currentDevice.getLatitude();
			
		LatLonPoint latLonPoint = new LatLonPoint(currentDevice.getLatitude(),
				currentDevice.getLongitude());

		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);
		

         
		if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE) {
			TextView dev_name = (TextView) findViewById(R.id.dev_name);
			dev_name.setText("定位卡片机");
		}else if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_FISE_CAR_VALUE) {
			TextView dev_name = (TextView) findViewById(R.id.dev_name);
			dev_name.setText("电动车");
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

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {
		case USER_INFO_UPDATE:
			currentDevice = imService.getContactManager()
					.findDeviceContact(deviceUserId);
			rsp = imService.getDeviceManager().findDeviceCard(deviceUserId);
			if (currentDevice != null) { 
				initBaseProfile();
			}
			break;
		case USER_COMMAND_TYPE_DEVICE_SHUTDOWN:
			Toast.makeText(DeviceFollowActivity.this, "断电指令成功",
					Toast.LENGTH_SHORT).show();
			break;
			
			 
		case USER_INFO_UPDATE_STAT:
			currentDevice = imService.getContactManager()
					.findDeviceContact(deviceUserId);
			rsp = imService.getDeviceManager().findDeviceCard(deviceUserId);
			if (currentDevice != null) {
				initBaseProfile();    
			}
			break;
		case USER_COMMAND_TYPE_DEVICE_CALLBACK:
			Toast.makeText(DeviceFollowActivity.this, "静默监听发送成功",
					Toast.LENGTH_SHORT).show();
			break;
			
		case USER_COMMAND_TYPE_DEVICE_CURRENT:
			Toast.makeText(DeviceFollowActivity.this, "发送指令成功",
					Toast.LENGTH_SHORT).show();
			break;
			 
		case USER_INFO_DEVE_BATTY_SUCCESS:
			//Toast.makeText(DeviceFollowActivity.this.getApplicationContext(), "同步数据成功",
			//		Toast.LENGTH_SHORT).show();
			
			currentDevice = imService.getContactManager()
					.findDeviceContact(deviceUserId);
			rsp = imService.getDeviceManager().findDeviceCard(deviceUserId);
			if (currentDevice != null) { 
				initBaseProfile();
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
		EventBus.getDefault().unregister(this);
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
	public void onEventMainThread(DeviceEvent event) {
		switch (event) { 
		case USER_INFO_SETTING_DEVICE_SUCCESS:
			UserEntity entity = imService.getContactManager()
					.findDeviceContact(deviceUserId);
			rsp = imService.getDeviceManager().findDeviceCard(deviceUserId);
			if (entity != null) {
				currentDevice = entity;
				adapter.setCurrentDevice(currentDevice);
				
				initBaseProfile();
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
				FileUtil.setDevWeather(fenxiang_weather, weatherlive.getWeather());
				// fenxiang_weather.setText(weatherlive.getWeather());

				weather_text.setText(weatherlive.getTemperature() + "℃");
			} else {
				// Toast.makeText(DeviceFollowActivity.this, "" + rCode,
				// Toast.LENGTH_SHORT).show();
			}
		} else {
			// Toast.makeText(DeviceFollowActivity.this, "" + rCode,
			// Toast.LENGTH_SHORT).show();

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

				if ((currentDevice.getIsFriend() == DBConstant.FRIENDS_TYPE_WEI)
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

			} else {

			}
		} else {

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

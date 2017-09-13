package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static com.fise.xiaoyu.config.IntentConstant.DEV_USER_ID;

/**
 * 位置信息的界面
 */
public class PostionTouchActivity extends TTBaseActivity implements
		OnClickListener, GeocodeSearch.OnGeocodeSearchListener {
	private MapView mapView;
	private AMap aMap;
	private double lat;
	private double lng;
	private GeocodeSearch geocoderSearch;
	private TextView postion_text;
	private int type;
	ArrayList<UserEntity> infoList = new ArrayList<UserEntity>();
	private ArrayList<Integer> list;
	private IMService imService;
	private ProgressBar progress_bar;
	private TimeCount time;

	private Button btn_refresh;
	private int addPostion;
	private UserEntity currentDevice;
	private int currentId;

	private UserEntity indexUser;  //如果是位有群　选中人的信息
    private  UserEntity  weiUserEntity;
	@SuppressLint("InflateParams")
	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onServiceDisconnected() {
		}

		@Override
		public void onIMServiceConnected() {
			logger.d("groupmgr#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) { 
				return;
			}

			 if (type == DBConstant.POSTION_DEV) {

				currentDevice = imService.getContactManager()
						.findDeviceContact(currentId);
 
				btn_refresh.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (currentDevice != null) {
							ArrayList<Integer> userIds = new ArrayList<>(1);
							// just single type
							userIds.add(currentId);
							imService.getContactManager()
									.reqGetDetaillUsersStat1(userIds);
						}
					}
				});

				Button location = (Button) findViewById(R.id.location);
				location.setVisibility(View.GONE);

				// guan 新需求 提示是否在线
				if (currentDevice.getOnLine() != DBConstant.ONLINE) {
					Utils.showToast(PostionTouchActivity.this, "设备不在线");
				}

				imService.getUserActionManager().onRepLocationReq(currentDevice.getPeerId() ,0,0);


			}
            initPostion();
		}
	};
	private TextView location_type;
    private TextView user_name;
    private TextView lastPosHint;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.postion_touch_activity);
		/*
		 * 设置离线地图存储目录，在下载离线地图或初始化地图设置; 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
		 * 则需要在离线地图下载和使用地图页面都进行路径设置
		 */
		// Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
		// MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
		imServiceConnector.connect(this);
				time = new TimeCount(4000, 1000);

		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		init();
		location_type = (TextView) findViewById(R.id.location_type);
        lastPosHint = (TextView) findViewById(R.id.last_position_hint);
        user_name = (TextView) findViewById(R.id.user_name);
		postion_text = (TextView) findViewById(R.id.postion_text);
		progress_bar = (ProgressBar) findViewById(R.id.progress_bar);

		btn_refresh = (Button) findViewById(R.id.btn_refresh);
        findViewById(R.id.zoom_in_btn).setOnClickListener(this);
        findViewById(R.id.zoom_out_btn).setOnClickListener(this);
		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);
		type = getIntent().getIntExtra(IntentConstant.POSTION_TYPE, 0);

		if (type == DBConstant.POSTION_DEV) {
			currentId = getIntent().getIntExtra(DEV_USER_ID, 0);

			TextView historical_text = (TextView) findViewById(R.id.historical_text);
			historical_text.setVisibility(View.VISIBLE);
			historical_text.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(currentId>0){
						IMUIHelper.openHistoryTrackActivity(PostionTouchActivity.this,currentId);
					}
				}
			});

		}


		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PostionTouchActivity.this.finish();
			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PostionTouchActivity.this.finish();
			}
		});

		Button location = (Button) findViewById(R.id.location);
		location.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LatLng cenpt = new LatLng(MainActivity.latitude,
						MainActivity.longitude);

				aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 12));// 设置指定的可视区域地图

			}
		});

	}

	private Bitmap getViewBitmap(View addViewContent) {

		addViewContent.setDrawingCacheEnabled(true);

		addViewContent.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		addViewContent.layout(0, 0, addViewContent.getMeasuredWidth(),
				addViewContent.getMeasuredHeight());

		addViewContent.buildDrawingCache();
		Bitmap cacheBitmap = addViewContent.getDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

		return bitmap;
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

			case USER_INFO_UPDATE_WEIFRIENDS:
				initPostion();
				break;
		case USER_INFO_UPDATE_STAT:
			initPostion();

			// 如果在线请求个人信息
			ArrayList<Integer> infoList = new ArrayList<Integer>();
			if(list!=null){
				for (int i = 0; i < list.size(); i++) {
					UserEntity user = imService.getContactManager().findContact(
							list.get(i));
					if (user != null && user.getOnLine() == DBConstant.ONLINE) {
						infoList.add(list.get(i));
					}
				}
				imService.getContactManager().reqGetDetaillUsers(infoList);
			}
			break;

		case USER_INFO_UPDATE:
			initPostion();
			break;

			case USER_INFO_UPDATE_QUERY_SUCCESS:
			{
				if (type == DBConstant.POSTION_DEV) {
					currentDevice = imService.getContactManager()
							.findDeviceContact(currentId);
					initDevInfo();

				}
			}
				break;
		case USER_INFO_UPDATE_POSTION_TOUCH:

			if (type == DBConstant.POSTION_DEV) {
				Utils.showToast(PostionTouchActivity.this, "刷新成功");
				currentDevice = imService.getContactManager()
						.findDeviceContact(currentId);

				initDevInfo();

			}  else {

				initPostion();
				addPostion = addPostion + 1;
				if (addPostion == 1) {
					Utils.showToast(PostionTouchActivity.this, "刷新成功");
				}
				// 如果在线请求个人信息
				ArrayList<Integer> infoList1 = new ArrayList<Integer>();
				for (int i = 0; i < list.size(); i++) {

					UserEntity user = imService.getContactManager()
							.findContact(list.get(i));
					if (user != null && user.getOnLine() == DBConstant.ONLINE) {
						infoList1.add(list.get(i));
					}
				}
				imService.getContactManager().reqGetDetaillUsers(infoList1);
			}

			break;

		default:
			break;
		}
	}


    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update, AMap.CancelableCallback callback) {
            aMap.animateCamera(update, 1000, callback);
    }

	/**
	 * 响应逆地理编码
	 */
	public void getAddress(final LatLonPoint latLonPoint) {
		RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 1000,
				GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
	}

	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		// TODO Auto-generated method stub
		if (rCode == 1000) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {

				postion_text.setVisibility(View.VISIBLE);
				
				if (type == DBConstant.POSTION_DEV) {

					String loationType ="";
					if(currentDevice.getLocationType() == DBConstant.LOCATION_GPS){
						loationType = "GPS定位";
					}else if(currentDevice.getLocationType() == DBConstant.LOCATION_BASE_STATION){
						loationType = "基站定位";
					}else if(currentDevice.getLocationType() == DBConstant.LOCATION_WIFI){
						loationType = "WIFI定位";
					}

					if(currentDevice.getOnLine() != DBConstant.ONLINE){
                        lastPosHint.setVisibility(View.VISIBLE);
					}else{
                        lastPosHint.setVisibility(View.GONE);
					}

					if (currentDevice.getComment().equals("")) {
						user_name.setText(currentDevice.getMainName());
					}else{
						user_name.setText(currentDevice.getComment());
					}


                    location_type.setText(loationType);
                    postion_text.setText(result.getRegeocodeAddress().getFormatAddress());

				}else{


						if(currentDevice!=null){
							String nameText ;
							if (currentDevice.getComment().equals("")) {
								nameText = currentDevice.getMainName();
							}else{
								nameText = currentDevice.getComment();
							}
                            //gzc
                            if(currentDevice.getOnLine() != DBConstant.ONLINE){
                                lastPosHint.setVisibility(View.VISIBLE);
                            }else{
                                lastPosHint.setVisibility(View.GONE);
                            }
							user_name.setText(nameText +"  ");
						}

					postion_text.setText("" + result.getRegeocodeAddress().getFormatAddress());
				}
	
			}
		}
	}
	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();

            UiSettings uiSettings = aMap.getUiSettings();
            uiSettings.setZoomControlsEnabled(false);
//            uiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
		}

	}

	public void initPostion() {

		aMap.clear();
		 {

			lat = getIntent().getDoubleExtra(IntentConstant.POSTION_LAT, 0);
			lng = getIntent().getDoubleExtra(IntentConstant.POSTION_LNG, 0);

			if (lat == 0.0f || lat == 0.0f) {

				LatLng cenpt = new LatLng(MainActivity.latitude,
						MainActivity.longitude);
				aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 14));

				time.start();

			} else {

				if (type != DBConstant.POSTION_MESSAGE_INFO) {
					LatLonPoint latLonPoint = new LatLonPoint(lat, lng);
					getAddress(latLonPoint);
				}else{

					String info = PostionTouchActivity.this.getIntent().getStringExtra(IntentConstant.MESSAGE_POSTION_TITLE);
					String postionName = PostionTouchActivity.this.getIntent().getStringExtra(IntentConstant.MESSAGE_POSTION_INFO);

					user_name.setText(info);
					postion_text.setText( postionName);
				}
				// 设定中心点坐标
				LatLng cenpt = new LatLng(lat, lng);
				aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 18));

				int bitmapId;
				if (type == DBConstant.POSTION_DEV   ) {
					if (currentDevice!=null&&currentDevice.getOnLine() == DBConstant.ONLINE) {
						bitmapId = R.drawable.icon_dingw_bird_sheb;
					}else{
						bitmapId = R.drawable.icon_dingw_bird_hui;
					}
				}else{
                    bitmapId = R.drawable.icon_dingw_bird_ren;
                }


				Bitmap bMap = BitmapFactory.decodeResource(
						PostionTouchActivity.this.getResources(), bitmapId);
				BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);

				MarkerOptions options = new MarkerOptions();
				options.icon(des);
				options.anchor(0.5f, 0.5f);
				options.position(cenpt);
				aMap.addMarker(options);

				progress_bar.setVisibility(View.GONE);
			}

		}
	}

	public void initDevInfo() {

		lat = currentDevice.getLatitude();
		lng = currentDevice.getLongitude();
		aMap.clear();

		if (lat == 0.0f || lat == 0.0f) {

			LatLng cenpt = new LatLng(MainActivity.latitude,
					MainActivity.longitude);
			aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 14));

			time.start();

		} else { 

			
			/*
			if (type == DBConstant.POSTION_DEV) {

				LatLng sourceLatLng = new LatLng(lat, lng);
				CoordinateConverter converter = new CoordinateConverter();
				// CoordType.GPS 待转换坐标类型
				converter.from(CoordType.GPS);
				// sourceLatLng待转换坐标点 LatLng类型
				converter.coord(sourceLatLng);
				// 执行转换操作
				LatLng desLatLng = converter.convert();
				LatLonPoint desPoint = new LatLonPoint(desLatLng.latitude,
						desLatLng.longitude);

				getAddress(desPoint);

			} else {
				LatLonPoint latLonPoint = new LatLonPoint(lat, lng);
				getAddress(latLonPoint);
			}
			*/

			LatLonPoint latLonPoint = new LatLonPoint(lat, lng);
			getAddress(latLonPoint);

			// 设定中心点坐标
			LatLng cenpt = new LatLng(lat, lng);
			aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 18));

			int bitmapId;

			if (type == DBConstant.POSTION_DEV) {

				if (currentDevice.getOnLine() == DBConstant.ONLINE) {
					bitmapId = R.drawable.icon_dingw_bird_sheb;
				}else{
					bitmapId = R.drawable.icon_dingw_bird_hui;
				}
			} else {

				if (currentDevice != null
						&& (!Utils.isClientType(currentDevice))) {
					//bitmapId = R.drawable.icon_dingw_bird_sheb;
					if (currentDevice.getOnLine() == DBConstant.ONLINE) {
						bitmapId = R.drawable.icon_dingw_bird_sheb;
					}else{
						bitmapId = R.drawable.icon_dingw_bird_hui;
					}
				} else {
					bitmapId = R.drawable.icon_dingw_bird_ren;
				}
			}

			Bitmap bMap = BitmapFactory.decodeResource(
					PostionTouchActivity.this.getResources(), bitmapId);
			BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);

			MarkerOptions options = new MarkerOptions();
			options.icon(des);
			options.anchor(0.5f, 0.5f);
			options.position(cenpt);
			aMap.addMarker(options);

			progress_bar.setVisibility(View.GONE);
		}

	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();

		if (type == DBConstant.POSTION_DEV) {
			if(imService!=null){
				imService.getUserActionManager().onRepLocationReq(currentDevice.getPeerId() ,0,0);
			}
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		imServiceConnector.disconnect(this);
			}

	@Override
	public void onClick(View view) {
		//
        switch (view.getId()){
            case R.id.zoom_in_btn:
                changeCamera(CameraUpdateFactory.zoomIn(), null);
                break;

            case R.id.zoom_out_btn:
                changeCamera(CameraUpdateFactory.zoomOut(), null);
                break;

        }
	}

	class TimeCount extends CountDownTimer {

		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
		}

		@Override
		public void onFinish() {
			// btnGetcode.setBackgroundColor(Color.parseColor("#4EB84A"));
			progress_bar.setVisibility(View.GONE);
			Utils.showToast(PostionTouchActivity.this, "获取失败");

		}
	}

}

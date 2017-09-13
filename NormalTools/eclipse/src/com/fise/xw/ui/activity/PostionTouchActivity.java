package com.fise.xw.ui.activity;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.CoordinateConverter;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.CoordinateConverter.CoordType;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.ui.base.TTBaseActivity;

import de.greenrobot.event.EventBus;

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

			initPostion();

			if (type == DBConstant.POSTION_WEI) {
				list = (ArrayList<Integer>) getIntent()
						.getIntegerArrayListExtra("infoList");
				imService.getContactManager().reqGetDetaillUsersStat(list);
			} else if (type == DBConstant.POSTION_DEV) {
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
					Toast.makeText(PostionTouchActivity.this, "定位卡片机不在线",
							Toast.LENGTH_SHORT).show();
				}

			} else if(type == DBConstant.POSTION_INFO_WEI ){
				
				currentDevice = imService.getContactManager().findXiaoWeiContact(currentId);
				
				btn_refresh.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ArrayList<Integer> userIds = new ArrayList<>(1);
						// just single type
						userIds.add(currentId);
						imService.getContactManager()
								.reqGetDetaillUsersStat1(userIds);
					}
				});
			}

		}
	};

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
		EventBus.getDefault().register(this);
		time = new TimeCount(4000, 1000);

		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		init();

		postion_text = (TextView) findViewById(R.id.postion_text);
		progress_bar = (ProgressBar) findViewById(R.id.progress_bar);

		btn_refresh = (Button) findViewById(R.id.btn_refresh);

		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);
		type = getIntent().getIntExtra(IntentConstant.POSTION_TYPE, 0);

		if (type == DBConstant.POSTION_DEV) {
			currentId = getIntent().getIntExtra(IntentConstant.DEV_USER_ID, 0);
			  
		}else if(type == DBConstant.POSTION_INFO_WEI){
			currentId = getIntent().getIntExtra(IntentConstant.DEV_USER_ID, 0);
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

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_UPDATE_STAT:
			initPostion();

			// 如果在线请求个人信息
			ArrayList<Integer> infoList = new ArrayList<Integer>();
			for (int i = 0; i < list.size(); i++) {

				UserEntity user = imService.getContactManager().findContact(
						list.get(i));
				if (user != null && user.getOnLine() == DBConstant.ONLINE) {
					infoList.add(list.get(i));

				}
			}
			imService.getContactManager().reqGetDetaillUsers(infoList);
			break;

		case USER_INFO_UPDATE:
			initPostion();
			break;

		case USER_INFO_UPDATE_POSTION_TOUCH:

			if (type == DBConstant.POSTION_DEV) {
				Toast.makeText(PostionTouchActivity.this, "刷新成功",
						Toast.LENGTH_SHORT).show();
				currentDevice = imService.getContactManager()
						.findDeviceContact(currentId);

				initDevInfo();

			} else if (type == DBConstant.POSTION_INFO_WEI) {
				
				Toast.makeText(PostionTouchActivity.this, "刷新成功",
						Toast.LENGTH_SHORT).show();
				currentDevice = imService.getContactManager()
						.findXiaoWeiContact(currentId);

				initDevInfo();

			} else {
				initPostion();

				addPostion = addPostion + 1;
				if (addPostion == 1) {
					Toast.makeText(PostionTouchActivity.this, "刷新成功",
							Toast.LENGTH_SHORT).show();
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
					
					if(currentDevice.getOnLine() != DBConstant.ONLINE){
						postion_text.setText(""
								+ result.getRegeocodeAddress().getFormatAddress() + "(最后一次在线位置)");
					}else{
						postion_text.setText(""
								+ result.getRegeocodeAddress().getFormatAddress());
					}
					 
				}else{
					postion_text.setText(""
							+ result.getRegeocodeAddress().getFormatAddress());
				}
	
			}
		} else {

		}
	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
		}

	}

	public void initPostion() {

		if (type == DBConstant.POSTION_WEI) {
			list = (ArrayList<Integer>) getIntent().getIntegerArrayListExtra(
					"infoList");
			// infoList = getIntent().get("infoList");
			RelativeLayout postion_layout = (RelativeLayout) PostionTouchActivity.this
					.findViewById(R.id.postion_layout);
			postion_layout.setVisibility(View.GONE);

			for (int i = 0; i < list.size(); i++) {

				UserEntity user = imService.getContactManager().findContact(
						list.get(i));
				if (user != null && user.getOnLine() == DBConstant.ONLINE
						&& user.getLongitude() != 0.0f
						&& user.getLatitude() != 0.0f) {

					infoList.add(user);
					// 设定中心点坐标
					LatLng cenpt = new LatLng(user.getLatitude(),
							user.getLongitude());
					aMap.moveCamera(CameraUpdateFactory
							.newLatLngZoom(cenpt, 18));

					LayoutInflater mInflater = PostionTouchActivity.this
							.getLayoutInflater();// getLayoutInflater();
					View popup = (View) mInflater.inflate(R.layout.show_marker,
							null, false);

					TextView showName = (TextView) popup
							.findViewById(R.id.show_name);

					String name;
					if (user.getComment().equals("")) {
						name = user.getMainName();
					} else {
						name = user.getComment();
					}

					if (name.length() > 4) {
						String show = name.substring(0, 4);
						showName.setText(show + "..");
					} else {
						showName.setText(name);

					}

					BitmapDescriptor bdA = BitmapDescriptorFactory
							.fromBitmap(getViewBitmap(popup));

					MarkerOptions options = new MarkerOptions();
					options.icon(bdA);
					options.anchor(0.5f, 0.5f);
					options.position(cenpt);
					aMap.addMarker(options);
				}
			}

			if (infoList.size() > 0) {
				LatLng cenpt = new LatLng(infoList.get(0).getLatitude(),
						infoList.get(0).getLongitude());
				aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 12));// 设置指定的可视区域地图

			} else {
				LatLng cenpt = new LatLng(MainActivity.latitude,
						MainActivity.longitude);
				aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 12));// 设置指定的可视区域地图
			}

			progress_bar.setVisibility(View.GONE);

			btn_refresh.setVisibility(View.VISIBLE);
			btn_refresh.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					addPostion = 0;
					list = (ArrayList<Integer>) getIntent()
							.getIntegerArrayListExtra("infoList");
					imService.getContactManager().reqGetDetaillUsersStat1(list);

				}
			});

		} else {

			lat = getIntent().getDoubleExtra(IntentConstant.POSTION_LAT, 0);
			lng = getIntent().getDoubleExtra(IntentConstant.POSTION_LNG, 0);

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
					bitmapId = R.drawable.icon_dingw_bird_sheb;
				} else {
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
				bitmapId = R.drawable.icon_dingw_bird_sheb;
			} else {

				if (currentDevice == null
						&& (currentDevice.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE
						||currentDevice.getUserType() == ClientType.CLIENT_TYPE_FISE_CAR_VALUE)) {
					bitmapId = R.drawable.icon_dingw_bird_sheb;
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
		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

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
			Toast.makeText(PostionTouchActivity.this, "获取失败",
					Toast.LENGTH_SHORT).show();

		}
	}

}

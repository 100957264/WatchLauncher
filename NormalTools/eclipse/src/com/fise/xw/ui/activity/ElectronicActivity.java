package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.CancelableCallback;
import com.amap.api.maps2d.AMap.OnMapTouchListener;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.Projection;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.Circle;
import com.amap.api.maps2d.model.CircleOptions;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.fise.historytrack.data.Info;
import com.fise.xw.R;
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.ElectricFenceEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.ui.base.TTBaseActivity;

import de.greenrobot.event.EventBus;


/**
 *  设备的电子围栏界面 (设置电子围栏)
 * @author weileiguan
 *
 */
public class ElectronicActivity extends TTBaseActivity implements
		OnGeocodeSearchListener, OnPoiSearchListener, TextWatcher,
		InputtipsListener {

	MapView mMapView = null;
	private AMap aMap;
	private View popview;

	// bean类
	List<Info> infos;
	Info info;
	LatLng latlng;

	private SeekBar seek;
	private LatLng llCircle;
	private Circle circle;
	private LatLng role;

	BitmapDescriptor roleTou = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_dingw_bird_sheb);

	private double Lng;
	private double Lat;

	// private TextView text_name;
	private int distance;
	private Button button_first;
	private IMService imService;
	private int currentUserId;
	private UserEntity currentUser;

	// private Button mZoomInButton;
	// private Button mZoomOutButton;
	// private List<ElectricFenceEntity> fenceEntity;

	private ElectricFenceEntity fenceEntity;
	private GeocodeSearch geocoderSearch;
	private LatLonPoint latLonPoint;
	private String addressName = "";
	private DeviceEntity rsp;

	private int tempDistance;
	private LatLonPoint tempLatLonPoint;

	// 搜索的位置列表
	// ListView mListView;
	// PlaceListAdapter mAdapter;
	// List<PoiItem> mInfoList;

	private AutoCompleteTextView input_name;
	private AutoCompleteTextView mAddressText;
	private ListView minputlist;
	private List<Tip> tipList;

	private PoiSearch poiSearch;
	private List<PoiItem> poiItems;// poi数据

	private PoiResult poiResult; // poi返回的结果
	private int currentPage = 0;// 当前页面，从0开始计数
	private PoiSearch.Query query;// Poi查询条件类

	private int electronicType;
	private String electronicName;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}
			currentUserId = ElectronicActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);

			if (currentUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);
			
		 	if(currentUser!=null){
        		if (currentUser.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE) {
        			TextView black = (TextView) findViewById(R.id.black);
        			black.setText("定位卡片机");
        		}else if (currentUser.getUserType() == ClientType.CLIENT_TYPE_FISE_CAR_VALUE) {
        			TextView black = (TextView) findViewById(R.id.black);
        			black.setText("电动车");
        		}
        	} 
		 	

			role = new LatLng(currentUser.getLatitude(),
					currentUser.getLongitude());

			// fenceEntity = imService.getDeviceManager().findElectrice(
			// currentUserId);

			rsp = imService.getDeviceManager().findDeviceCard(currentUserId);
			if (rsp == null) {
				return;
			}

			initData();

			UserEntity loginContact = IMLoginManager.instance().getLoginInfo();
			if (rsp != null && rsp.getMasterId() != loginContact.getPeerId()) {
				TextView electronic_save = (TextView) findViewById(R.id.electronic_save);
				electronic_save.setVisibility(View.GONE);
			}
		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	public void initData() {

		aMap.clear();

		//如果不是管理员无权修改名字和
		if(rsp.getMasterId()!= imService.getLoginManager().getLoginId()){

			mAddressText.setEnabled(false); 
			input_name.setEnabled(false);
			seek.setEnabled(false);	
		}
		
		
		if (electronicType == DBConstant.ELECTRONIC_ADD) {

			distance = 300;
			tempDistance = 300;

			LatLng cenpt = new LatLng(MainActivity.latitude,
					MainActivity.longitude);

			latLonPoint = new LatLonPoint(MainActivity.latitude,
					MainActivity.longitude);

			aMap.addMarker(new MarkerOptions().position(role).icon(
					BitmapDescriptorFactory
							.fromResource(R.drawable.icon_dingw_bird_sheb)));

			aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 16));// 设置指定的可视区域地图

			// getAddress(latLonPoint);
			// return;
			
			
		} else if (electronicType == DBConstant.ELECTRONIC_UPDATE) {

			distance = fenceEntity.getRadius();
			tempDistance = fenceEntity.getRadius();

			String[] electName = fenceEntity.getMark().split("##"); // 名字
			if (electName[0] != null) {
				electronicName = electName[0];
			} 

			if(electName.length>1){
				mAddressText.setText("" + electName[1]);
			}
			
			Lat = fenceEntity.getLat();
			Lng = fenceEntity.getLng();

			if (Lat == 0.0f || Lng == 0.0f) {
				latLonPoint = new LatLonPoint(MainActivity.latitude,
						MainActivity.longitude);

				tempLatLonPoint = new LatLonPoint(MainActivity.latitude,
						MainActivity.longitude);

				//getAddress(latLonPoint);
				llCircle = new LatLng(MainActivity.latitude,
						MainActivity.longitude);

			} else {
				latLonPoint = new LatLonPoint(Lat, Lng);
				tempLatLonPoint = new LatLonPoint(Lat, Lng);

				//getAddress(latLonPoint);
				llCircle = new LatLng(Lat, Lng);
			}

			// 添加圆
			// text_name.setText("范围:" + distance);
			String[] name = fenceEntity.getMark().split("##"); // 名字

			if (name != null) {
				input_name.setText("" + name[0]);
			}

			// text_name.setText("以蓝色位置为中心点,范围" + distance + "米以内");

			circle = aMap.addCircle(new CircleOptions().center(llCircle)
					.radius(distance).strokeColor(Color.argb(40, 9, 123, 244))
					.fillColor(Color.argb(40, 9, 123, 244)).strokeWidth(1));

			seek.setProgress((distance - 300) / 3); // 10

			role = new LatLng(Lat, Lng);
			aMap.addMarker(new MarkerOptions().position(role).icon(
					BitmapDescriptorFactory
							.fromResource(R.drawable.icon_dingw_bird_sheb)));

			aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(llCircle, 16));// 设置指定的可视区域地图
		}
		
		aMap.setOnMapTouchListener(mapTouchListener);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.electronic_map);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.onCreate(savedInstanceState);// 此方法必须重写
		aMap = mMapView.getMap();

		fenceEntity = (ElectricFenceEntity) getIntent().getSerializableExtra(
				IntentConstant.FENCE_ENTITY);
		electronicType = this.getIntent().getIntExtra(
				IntentConstant.ELECTRONIC_TYPE, 0);

		minputlist = (ListView) findViewById(R.id.inputlist);

		mAddressText = (AutoCompleteTextView) findViewById(R.id.input_edittext);
		mAddressText.addTextChangedListener(this);
		input_name = (AutoCompleteTextView) findViewById(R.id.input_name);

		minputlist.setOnItemClickListener(itemClickListener);
		minputlist.setEnabled(false);
		minputlist.setVisibility(View.GONE);

		aMap.setOnMapTouchListener(mapTouchListener);

		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);

		seek = (SeekBar) findViewById(R.id.mySeekBar);
		seek.setOnSeekBarChangeListener(seekbarChangeListener);

		// text_name = (TextView) findViewById(R.id.text_name);

		distance = 300;
		imServiceConnector.connect(this);
		EventBus.getDefault().register(this);

		// 多个标记点的点击事件
		aMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {

				return true;
			}
		});

		aMap.getUiSettings().setZoomControlsEnabled(false);
 
         
		//TextView electronic_save = (TextView) findViewById(R.id.electronic_save);
         RelativeLayout electronic_layout = (RelativeLayout) findViewById(R.id.electronic_layout);
         electronic_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (input_name.getText().toString().equals("")) {

					Toast.makeText(ElectronicActivity.this, "请设置安全围栏的名字",
							Toast.LENGTH_SHORT).show();

				} else {

					if (tempDistance == distance
							&& (tempLatLonPoint != null)
							&& (latLonPoint != null)
							&& (tempLatLonPoint.getLatitude() == latLonPoint
									.getLatitude())
							&& (tempLatLonPoint.getLongitude() == latLonPoint
									.getLongitude())

							&& (electronicName.equals(input_name.getText()
									.toString()))) {

						Toast.makeText(ElectronicActivity.this, "安全围栏设置成功",
								Toast.LENGTH_SHORT).show();

					} else {
						if (currentUserId > 0) {

							String Lng1 = String.valueOf(Lng);
							String Lat1 = String.valueOf(Lat);
							int loginId = IMLoginManager.instance()
									.getLoginId();

							//
							if (electronicType == DBConstant.ELECTRONIC_ADD) {

								// 默认添加为disable
								imService.getDeviceManager().settingElectronic(
										loginId,
										currentUserId,
										1,
										0,
										Lng1,
										Lat1,
										distance,
										input_name.getText().toString() + "##"
												+ addressName,
										DBConstant.ELECTRONIC_STATS_DISABLE); // ELECTRONIC_STATS_ENABLE

							} else if (electronicType == DBConstant.ELECTRONIC_UPDATE) {

								int stats = DBConstant.ELECTRONIC_STATS_ENABLE;
								if (fenceEntity.getStatus() == 1) {
									stats = DBConstant.ELECTRONIC_STATS_ENABLE;
								} else if (fenceEntity.getStatus() == 2) {
									stats = DBConstant.ELECTRONIC_STATS_DISABLE;
								}

								imService.getDeviceManager().settingElectronic(
										loginId,
										currentUserId,
										2,
										fenceEntity.getFenceId(),
										Lng1,
										Lat1,
										distance,
										input_name.getText().toString() + "##"
												+ addressName, stats);// DBConstant.ELECTRONIC_STATS_ENABLE
							}

						}
					}

				}

			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ElectronicActivity.this.finish();
			}
		});

		Button search_button = (Button) findViewById(R.id.search_button);
		search_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(rsp.getMasterId() == imService.getLoginManager().getLoginId()){
					String newText = mAddressText.getText().toString().trim();
					InputtipsQuery inputquery = new InputtipsQuery(newText, "");
					inputquery.setCityLimit(true);
					Inputtips inputTips = new Inputtips(ElectronicActivity.this,
							inputquery);
					inputTips.setInputtipsListener(ElectronicActivity.this);
					inputTips.requestInputtipsAsyn();
				}
		 
			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ElectronicActivity.this.finish();
			}
		});

		aMap.setOnMapTouchListener(mapTouchListener);
		// text_name.setText("以蓝色位置为中心点,范围" + distance + "米以内");

	}

	// listView选项点击事件监听器
	OnMapTouchListener mapTouchListener = new OnMapTouchListener() {

		@Override
		public void onTouch(MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN:

				break;
			case MotionEvent.ACTION_UP: {
				
				
				if(rsp.getMasterId() == imService.getLoginManager().getLoginId()){
					
					Projection projection = aMap.getProjection();

					Point point = new Point((int) event.getX(), (int) event.getY());// 将坐标保存在Point类
					LatLng geo = projection.fromScreenLocation(point);

					Lng = geo.longitude;
					Lat = geo.latitude;

					latLonPoint = new LatLonPoint(Lat, Lng);
					getAddress(latLonPoint);

					aMap.clear();
					llCircle = new LatLng(Lat, Lng);
					circle = aMap.addCircle(new CircleOptions().center(llCircle)
							.radius(distance)
							.strokeColor(Color.argb(40, 9, 123, 244))
							.fillColor(Color.argb(40, 9, 123, 244)).strokeWidth(1));

					role = new LatLng(Lat, Lng);
					// text_name.setText("以绿色位置为中心点,范围" + distance + "米以内");

					aMap.addMarker(new MarkerOptions().position(role).icon(
							BitmapDescriptorFactory
									.fromResource(R.drawable.icon_dingw_bird_sheb)));

					minputlist.setVisibility(View.GONE);
					minputlist.setEnabled(false);	
				}
		
			}
				break;
			}
		}

	};

	private OnSeekBarChangeListener seekbarChangeListener = new OnSeekBarChangeListener() {

		// 停止拖动时执行
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		// 在进度开始改变时执行
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		// 当进度发生改变时执行
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

			distance = 300 + progress * 3;

			if (circle != null) {
				aMap.clear();
				circle = aMap.addCircle(new CircleOptions().center(llCircle)
						.radius(distance)
						.strokeColor(Color.argb(40, 9, 123, 244))
						.fillColor(Color.argb(40, 9, 123, 244)).strokeWidth(1));

				role = new LatLng(Lat, Lng);
				aMap.addMarker(new MarkerOptions().position(role).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.icon_dingw_bird_sheb)));

			}
		}
	};

	// listView选项点击事件监听器
	OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// 通知是适配器第position个item被选择了

			aMap.clear();
			minputlist.setVisibility(View.GONE);
			minputlist.setEnabled(false);

			if (tipList.get(position).getPoint() != null) {

				Lng = tipList.get(position).getPoint().getLongitude();
				Lat = tipList.get(position).getPoint().getLatitude();

				role = new LatLng(tipList.get(position).getPoint()
						.getLatitude(), tipList.get(position).getPoint()
						.getLongitude());

				latLonPoint = new LatLonPoint(Lat, Lng);

				llCircle = new LatLng(tipList.get(position).getPoint()
						.getLatitude(), tipList.get(position).getPoint()
						.getLongitude());

				addressName = tipList.get(position).getAddress();
				circle = aMap.addCircle(new CircleOptions().center(llCircle)
						.radius(distance)
						.strokeColor(Color.argb(40, 9, 123, 244))
						.fillColor(Color.argb(40, 9, 123, 244)).strokeWidth(1));
				// text_name.setText("范围:" + distance);
				// text_name.setText("以" + addressName + "位置为中心点,范围" + distance
				// + "米以内");

				aMap.addMarker(new MarkerOptions().position(role).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.icon_dingw_bird_sheb)));

				aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(role, 16));// 设置指定的可视区域地图
				aMap.setOnMapTouchListener(mapTouchListener);

			} else {
				Toast.makeText(ElectronicActivity.this, "区域太大无法获取具体位置信息",
						Toast.LENGTH_SHORT).show();

			}
		}

	};

	/**
	 * 处理放大;
	 */
	private void performZoomIn() {

		if (distance >= 1000) {
			distance = 1000;
		} else {
			distance = distance + 50;
		}

		if (circle != null) {
			aMap.clear();
			circle = aMap.addCircle(new CircleOptions().center(llCircle)
					.radius(distance).strokeColor(Color.argb(40, 9, 123, 244))
					.fillColor(Color.argb(40, 9, 123, 244)).strokeWidth(1));

			// text_name.setText("以" + addressName + "位置为中心点,范围" + distance
			// + "米以内");

			role = new LatLng(Lat, Lng);
			aMap.addMarker(new MarkerOptions().position(role).icon(
					BitmapDescriptorFactory
							.fromResource(R.drawable.icon_dingw_bird_sheb)));
		}

	}

	/**
	 * 处理缩小;
	 */
	private void performZoomOut() {

		if (distance <= 0) {
			distance = 0;
		} else {
			distance = distance - 50;
		}

		if (circle != null) {
			aMap.clear();
			circle = aMap.addCircle(new CircleOptions().center(llCircle)
					.radius(distance).strokeColor(Color.argb(40, 9, 123, 244))
					.fillColor(Color.argb(40, 9, 123, 244)).strokeWidth(1));
			// text_name.setText("范围:" + distance);
			// text_name.setText("以" + addressName + "位置为中心点,范围" + distance
			// + "米以内");

			role = new LatLng(Lat, Lng);
			aMap.addMarker(new MarkerOptions().position(role).icon(
					BitmapDescriptorFactory
							.fromResource(R.drawable.icon_dingw_bird_sheb)));

		}
	}

	/**
	 * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
	 */
	private void changeCamera(CameraUpdate update, CancelableCallback callback) {
		aMap.moveCamera(update);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();

		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	/**
	 * ----------------------------event 事件驱动----------------------------
	 */
	public void onEventMainThread(DeviceEvent event) {
		switch (event) {

		case USER_INFO_SETTING_DEVICE_SUCCESS:

			if (electronicType == DBConstant.ELECTRONIC_ADD) {
				Toast.makeText(ElectronicActivity.this, "安全围栏设置成功",
						Toast.LENGTH_SHORT).show();

				ElectronicActivity.this.finish();
			} else {

				Toast.makeText(ElectronicActivity.this, "安全围栏设置成功",
						Toast.LENGTH_SHORT).show();
				
				fenceEntity = imService.getDeviceManager().findElectriceFence(
						fenceEntity.getFenceId());

				// fenceEntity = imService.getDeviceManager().findElectrice(
				// currentUserId);
				initData();

			}

			break;
		case USER_INFO_SETTING_DEVICE_FAILED:
			Toast.makeText(ElectronicActivity.this, "安全围栏设置失败",
					Toast.LENGTH_SHORT).show();
			break;

		case USER_INFO_ELECTIRC_FENCE:
			// Toast.makeText(ElectronicActivity.this, "安全围栏设置成功",
			// Toast.LENGTH_SHORT).show();
			fenceEntity = imService.getDeviceManager().findElectriceFence(
					fenceEntity.getFenceId());
			// fenceEntity = imService.getDeviceManager().findElectrice(
			// currentUserId);
			initData();
			break;

		case USER_INFO_UPDATE_INFO_SUCCESS:
			break;

		}
	}

	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * 逆地理编码回调
	 */
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		if (rCode == 1000) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {

				/*
				 * if
				 * (result.getRegeocodeAddress().getStreetNumber().getStreet()
				 * .equals("")) { addressName =
				 * result.getRegeocodeAddress().getTownship(); } else {
				 * addressName = result.getRegeocodeAddress()
				 * .getStreetNumber().getStreet(); }
				 */
				addressName = result.getRegeocodeAddress().getFormatAddress();
				mAddressText.setText(""
						+ result.getRegeocodeAddress().getFormatAddress());
				// text_name.setText("以" + addressName + "位置为中心点,范围" + distance
				// + "米以内");
			} else {

			}
		} else {

		}
	}

	/**
	 * 响应逆地理编码
	 */
	public void getAddress(final LatLonPoint latLonPoint) {
		RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
				GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
	}

	@Override
	public void onPoiItemSearched(PoiItem arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPoiSearched(PoiResult result, int rcode) {
		// TODO Auto-generated method stub
		if (rcode == 1000) {
			if (result != null && result.getQuery() != null) {// 搜索poi的结果
				if (result.getQuery().equals(query)) {// 是否是同一条
					poiResult = result;
					poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
					// List<SuggestionCity> suggestionCities = poiResult
					// .getSearchSuggestionCitys();//
					// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

				}
			} else {

			}
		}
	}

	@Override
	public void onGetInputtips(final List<Tip> tipList, int rCode) {
		// TODO Auto-generated method stub
		if (rCode == 1000) {

			minputlist.setVisibility(View.VISIBLE);
			minputlist.setEnabled(true);

			List<HashMap<String, String>> listString = new ArrayList<HashMap<String, String>>();
			this.tipList = tipList;

			for (int i = 0; i < tipList.size(); i++) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("name", tipList.get(i).getName());
				map.put("address", tipList.get(i).getDistrict());
				listString.add(map);
			}
			SimpleAdapter aAdapter = new SimpleAdapter(getApplicationContext(),
					listString, R.layout.item_layout, new String[] { "name",
							"address" }, new int[] { R.id.poi_field_id,
							R.id.poi_value_id });

			minputlist.setAdapter(aAdapter);
			aAdapter.notifyDataSetChanged();

		}

	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}
}

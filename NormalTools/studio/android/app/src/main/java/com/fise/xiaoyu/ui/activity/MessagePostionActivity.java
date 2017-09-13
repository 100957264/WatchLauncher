package com.fise.xiaoyu.ui.activity;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.entity.PostionMessage;
import com.fise.xiaoyu.imservice.event.MessageEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.adapter.PlaceListAdapter;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.PermissionUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 *  聊天界面 位置信息的界面
 */
public class MessagePostionActivity extends TTBaseActivity implements
		OnClickListener, AMap.OnMapClickListener,
		PoiSearch.OnPoiSearchListener, AMap.OnCameraChangeListener,
		GeocodeSearch.OnGeocodeSearchListener, AMap.OnMapLoadedListener,
		AMapLocationListener {

	private AMap aMap;
	private MapView mMapView;
	ProgressBar mLoadBar;
	private Marker mLocMarker;

	// 声明mLocationOption对象
	public AMapLocationClientOption mLocationOption = null;
	private AMapLocationClient mlocationClient;
	private static IMService imService;

	// 位置列表
	ListView mListView;
	PlaceListAdapter mAdapter;
	List<PoiItem> mInfoList;
	PoiItem mCurentInfo;
	private GeocodeSearch geocoderSearch;
	private String curSessionKey;
	private PeerEntity peerEntity;
	private double lat;
	private double lng;
	private String postName;
	private String infoName;
	private TextView send;

	//是否选择过位置
	private boolean isSelectPotion = false;



	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			curSessionKey = MessagePostionActivity.this.getIntent()
					.getStringExtra(IntentConstant.KEY_SESSION_KEY);

			peerEntity = imService.getSessionManager().findPeerEntity(
					curSessionKey);
			if (peerEntity == null) {
				logger.e("groupmgr#findPeerEntity failed,sessionKey:%s",
						curSessionKey);
				return;
			}
			send.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					UserEntity loginInfo = imService.getLoginManager()
							.getLoginInfo();

					//没有选择过位置　就是默认值　从第一个选
					if(isSelectPotion == false){
						if(mAdapter.getNotifyTip() == 0){
							if(mInfoList.size()>0&&mInfoList.get(0)!=null){
								infoName = mInfoList.get(0).getTitle();
							}
						}
					}

					PostionMessage postionMessage = PostionMessage
							.buildForSend(lat, lng,postName,infoName, loginInfo, peerEntity);
					imService.getMessageManager().sendPostion(postionMessage,true);

				}
			});

		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_send_postion);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.onCreate(savedInstanceState);// 此方法必须重写
		aMap = mMapView.getMap();

		imServiceConnector.connect(this);

		send = (TextView) findViewById(R.id.send);
		send.setEnabled(false);
//		send.setBackgroundResource(R.drawable.button_send_press);


		checkLocaltionPermission();
		initView();
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
				Toast.makeText(MessagePostionActivity.this, PermissionUtil.getPermissionString(MessagePostionActivity.this, deniedPermission), Toast.LENGTH_SHORT).show();
				finish();
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

		aMap.setOnMapClickListener(this);// 对amap添加单击地图事件监听器
	}
	@Override
	public void onDestroy() {
		imServiceConnector.disconnect(this);
				super.onDestroy();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(MessageEvent event) {
		switch (event.getEvent()) {
		case POSTION_SUCCESS: {
				MessagePostionActivity.this.finish();
			} 
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

	/**
	 * 初始化界面
	 */
	private void initView() {

		// 周边位置列表
		mListView = (ListView) findViewById(R.id.place_list);
		mLoadBar = (ProgressBar) findViewById(R.id.place_progressBar);
		mListView.setOnItemClickListener(itemClickListener);
		// 初始化POI信息列表
		mInfoList = new ArrayList<PoiItem>();

		mAdapter = new PlaceListAdapter(getLayoutInflater(), mInfoList);
		mListView.setAdapter(mAdapter);
		mAdapter.setNotifyTip(0);
		mListView.setSelection(0);


		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);

		LatLonPoint latLonPoint = new LatLonPoint(MainActivity.latitude,
				MainActivity.longitude);
		getAddress(latLonPoint);
		mLoadBar.setVisibility(View.VISIBLE);

		aMap.getUiSettings().setZoomControlsEnabled(false);
		// 设定中心点坐标
		LatLng cenpt = new LatLng(MainActivity.latitude, MainActivity.longitude);
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 14));

		Bitmap bMap = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.icon_dingw_bird_ren);
		BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);

		MarkerOptions options = new MarkerOptions();
		options.icon(des);
		options.anchor(0.5f, 0.5f);
		options.position(cenpt);
		mLocMarker = aMap.addMarker(options);

		Button btn_dingwei_normal = (Button) findViewById(R.id.btn_dingwei_normal);
		btn_dingwei_normal.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 设定中心点坐标
				LatLng cenpt = new LatLng(MainActivity.latitude,
						MainActivity.longitude);
				aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 14));

				aMap.clear();
				
				
				Bitmap bMap = BitmapFactory.decodeResource(
						MessagePostionActivity.this.getResources(),
						R.drawable.icon_dingw_bird_ren);
				BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);

				MarkerOptions options = new MarkerOptions();
				options.icon(des);
				options.anchor(0.5f, 0.5f);
				options.position(cenpt);
				mLocMarker = aMap.addMarker(options);
				LatLonPoint latLonPoint = new LatLonPoint(
						MainActivity.latitude, MainActivity.longitude);
				getAddress(latLonPoint);

			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MessagePostionActivity.this.finish();
			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MessagePostionActivity.this.finish();
			}
		});

	}

	/**
	 * 响应逆地理编码
	 */
	public void getAddress(final LatLonPoint latLonPoint) {
		RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 1000,
				GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
	} 

	// listView选项点击事件监听器
	OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			    
			if(mInfoList.get(position)!=null&&
					mInfoList.get(position).getLatLonPoint()!=null){

				// 通知是适配器第position个item被选择了
				mAdapter.setNotifyTip(position);

				isSelectPotion = true;
				infoName = mInfoList.get(position).getTitle();
				LatLonPoint latLonPoint = mInfoList.get(position).getLatLonPoint();
				getAddress(latLonPoint);

				// 设定中心点坐标
				LatLng cenpt = new LatLng(latLonPoint.getLatitude(),
						latLonPoint.getLongitude());
				aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 14));
			} 
		}

	};

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

				// result.getRegeocodeAddress().
				// 
				 // result.getRegeocodeAddress().get 
				
				// // 将周边信息加入表 
				lat = result.getRegeocodeQuery().getPoint().getLatitude();
				lng = result.getRegeocodeQuery().getPoint().getLongitude();
				postName = result.getRegeocodeAddress().getFormatAddress();

				aMap.clear();
				Bitmap bMap = BitmapFactory.decodeResource(this.getResources(),
						R.drawable.icon_dingw_bird_ren);
				BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);

				LatLonPoint latLong = result.getRegeocodeQuery().getPoint();
				MarkerOptions options = new MarkerOptions(); 
				options.icon(des);
				options.anchor(0.5f, 0.5f);
				options.position(new LatLng(latLong.getLatitude(), latLong.getLongitude()));
				mLocMarker = aMap.addMarker(options);

				if (result.getRegeocodeAddress().getPois() != null) {
					mInfoList.clear();
					mInfoList.addAll(result.getRegeocodeAddress().getPois());

					mAdapter.setList(mInfoList);
					mAdapter.notifyDataSetChanged();
					// mAdapter.setNotifyTip(0);
					// mListView.setSelection(0);
				}
				// 通知适配数据已改变
				// mAdapter.notifyDataSetChanged();
				mLoadBar.setVisibility(View.GONE);

//				send.setBackgroundResource(R.drawable.button_send);
				send.setEnabled(true);
				
			}
		} else {

		}
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPoiItemSearched(PoiItem arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPoiSearched(PoiResult arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		LatLonPoint latLonPoint = new LatLonPoint(arg0.latitude, arg0.longitude);
		getAddress(latLonPoint);
		mAdapter.setNotifyTip(0);
		mListView.setSelection(0);
		mLoadBar.setVisibility(View.VISIBLE);

	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapLoaded() {
		// TODO Auto-generated method stub

	}

}
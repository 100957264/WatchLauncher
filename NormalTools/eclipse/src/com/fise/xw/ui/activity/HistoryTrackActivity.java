package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
import com.amap.api.maps2d.CoordinateConverter.CoordType;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.CoordinateConverter;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;   
import com.fise.historytrack.data.Info;
import com.fise.xw.R;
import com.fise.xw.imservice.manager.IMUserActionManager;
import com.fise.xw.ui.base.TTBaseActivity;


/**
 *  历史轨迹界面
 *  
 * @author weileiguan
 *
 */
public class HistoryTrackActivity extends TTBaseActivity implements
		OnGeocodeSearchListener {

	MapView mMapView = null;
	private AMap aMap;
	private View popview;

	// bean类
	List<Info> infos;
	Info info;
	LatLng latlng;

	private GeocodeSearch geocoderSearch;
	private int isStart = 0;
	//
	private List<LatLng> point = new ArrayList<LatLng>();
	
	private TextView start_name;
	private TextView end_name;
	 
	
	 
	//
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.historical_map);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.onCreate(savedInstanceState);// 此方法必须重写
		aMap = mMapView.getMap();

		start_name =(TextView)findViewById(R.id.start_name);  
		end_name =(TextView)findViewById(R.id.end_name);  
		
		point = IMUserActionManager.instance().onLocation();

		LatLng cenpt;
		if (point.size() <= 0) {
			// 设定中心点坐标
			cenpt = new LatLng(MainActivity.latitude, MainActivity.longitude);
		} else {
			cenpt = point.get(0);
		}

		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 14));
		aMap.getUiSettings().setZoomControlsEnabled(false);

		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);

		// 添加多个点，从bean得到数据
		info = new Info();

		for (int i = 0; i < point.size(); i++) {
			for (int j = point.size() - 1; j > i; j--) {
				if (AMapUtils.calculateLineDistance(point.get(i), point.get(j)) <= 80) { // if
																							// (list.get(j).equals(list.get(i)))
					point.remove(point.get(j));
				}
			}

		}

		for (int i = 0; i < point.size(); i++) {
			info.addInfo(point.get(i).latitude, point.get(i).longitude);
		}
  
		infos = info.infos;   
		
		
		// aMap.addOverlay(options2);

//		for (int i = 0; i < point.size(); i++) {
//			 if ((i + 1) < point.size()) {
//					
//				 LatLng latlng2 = new LatLng(point.get(i + 1).latitude,
//				 point.get(i + 1).longitude);
//				 aMap.addPolyline((new PolylineOptions()).add(latlng, latlng2)
//				 .color(Color.RED));
//				 }
//		}
 
		
		
		for (int i = 0; i < point.size(); i++) {

			latlng = new LatLng(point.get(i).latitude, point.get(i).longitude);

			LayoutInflater mInflater = HistoryTrackActivity.this
					.getLayoutInflater();
			View popup = (View) mInflater.inflate(R.layout.history_marker,
					null, false);

			ImageView history_bg = (ImageView) popup
					.findViewById(R.id.history_bg);
			TextView show_name = (TextView) popup.findViewById(R.id.show_name);

			if (point.size() == 1) {

				show_name.setText("起点");
				history_bg.setBackgroundResource(R.drawable.icon_locationstart);

				// BitmapDescriptor bitmap = BitmapDescriptorFactory
				// .fromResource(R.drawable.icon_locationstart);

				BitmapDescriptor bitmap = BitmapDescriptorFactory
						.fromBitmap(getBitmapFromView(popup));

				MarkerOptions options = new MarkerOptions();
				options.icon(bitmap);
				options.anchor(0.5f, 0.5f);
				options.position(latlng);
				Marker marker = aMap.addMarker(options);

				LatLonPoint latLonPoint1 = new LatLonPoint(latlng.latitude,
						latlng.longitude);
				getAddress(latLonPoint1);

			} else {
				if (i == 0) {

					show_name.setText("起点");
					history_bg.setBackgroundResource(R.drawable.icon_locationstart);
 
					BitmapDescriptor bitmap = BitmapDescriptorFactory 
							.fromBitmap(getBitmapFromView(popup));
					 
					MarkerOptions options = new MarkerOptions();
					options.icon(bitmap);
					options.anchor(0.5f, 0.5f);
					options.position(latlng);
					Marker marker = aMap.addMarker(options);
					LatLonPoint latLonPoint1 = new LatLonPoint(latlng.latitude,
							latlng.longitude);
					getAddress(latLonPoint1);

				} else if ((point.size() - 1) == i) {

					show_name.setText("终点");
					history_bg
							.setBackgroundResource(R.drawable.icon_locationend);
 
					BitmapDescriptor bitmapEnd = BitmapDescriptorFactory
							.fromBitmap(getBitmapFromView(popup));

					MarkerOptions options = new MarkerOptions();
					options.icon(bitmapEnd);
					options.anchor(0.5f, 0.5f);
					options.position(latlng);
					Marker marker = aMap.addMarker(options);

					LatLonPoint latLonPoint1 = new LatLonPoint(latlng.latitude,
							latlng.longitude);
					getAddress(latLonPoint1);
					
				} else {

					show_name.setText("" + i);
					history_bg
							.setBackgroundResource(R.drawable.icon_locationstart);
 
					BitmapDescriptor bitmap = BitmapDescriptorFactory
							.fromBitmap(getBitmapFromView(popup));

					MarkerOptions options = new MarkerOptions();
					options.icon(bitmap);
					options.anchor(0.5f, 0.5f);
					options.position(latlng);
					Marker marker = aMap.addMarker(options);

				}

			} 

		}
		


		// 多个标记点的点击事件
		aMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {

				return true;
			}
		});

		// 绘制折线图层
 

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				HistoryTrackActivity.this.finish();
			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				HistoryTrackActivity.this.finish();
			}
		});

	}										

	public Bitmap getBitmapFromView(View addViewContent) {

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

	/**
	 * 响应逆地理编码
	 */
	public void getAddress(final LatLonPoint latLonPoint) {
		
		/*
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
		 */
		
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

				 
				 if(isStart == 0){
					 isStart = isStart+ 1;
					 start_name.setText("" + result.getRegeocodeAddress().getFormatAddress()); 
//					 Toast.makeText(getApplicationContext(), "起点" + result.getRegeocodeQuery().getPoint().getLatitude() + " " + result.getRegeocodeQuery().getPoint().getLongitude()  + result.getRegeocodeAddress().getFormatAddress(),
//						     Toast.LENGTH_SHORT).show();
				 }else {
					 end_name.setText("" + result.getRegeocodeAddress().getFormatAddress()); 
//					 Toast.makeText(getApplicationContext(), "终点" + result.getRegeocodeQuery().getPoint().getLatitude() + " " + result.getRegeocodeQuery().getPoint().getLongitude() + result.getRegeocodeAddress().getFormatAddress() ,
//						     Toast.LENGTH_SHORT).show();
				 }
				//
				// postion_text.setVisibility(View.VISIBLE);
				// postion_text.setText("" 
				// + result.getRegeocodeAddress().getFormatAddress());
			}
		} else {

		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
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
}

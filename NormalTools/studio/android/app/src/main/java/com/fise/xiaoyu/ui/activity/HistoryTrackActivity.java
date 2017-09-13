package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.codbking.calendar.CaledarAdapter;
import com.codbking.calendar.CalendarBean;
import com.codbking.calendar.CalendarDateView;
import com.codbking.calendar.CalendarLayout;
import com.codbking.calendar.CalendarUtil;
import com.codbking.calendar.CalendarView;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.bean.TrajectoryInfo;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMUserActionManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 *  历史轨迹界面
 */
public class HistoryTrackActivity extends TTBaseActivity implements
		OnGeocodeSearchListener ,OnClickListener{

	MapView mMapView = null;
	private AMap aMap;
	private View popview;

	// bean类
	LatLng latlng;

	private GeocodeSearch geocoderSearch;
	//
	private List<TrajectoryInfo> point = new ArrayList<TrajectoryInfo>();
	private TextView start_name;
	private TextView end_name;


	private RelativeLayout history_address;
	private RelativeLayout address_layout;
	private RelativeLayout hoster_start;
	private RelativeLayout address_icon_layout;


	private TextView address;
	private TextView time;
	private ImageView address_icon;
	private TextView address_text;



	TextView mTitle;
	ImageView icon_calendar;
	CalendarDateView mCalendarDateView;
	CalendarLayout calendarLayout;
	LinearLayout week_layout;
	LinearLayout postion_layout;


	private IMService imService;
	private int currentUserId;
	private UserEntity currentDevice;
	private TextView next;

	private int year;
	private int month;
	private int day ;

	private int tempYyear;
	private int tempMonth;
	private int tempDay ;

	private boolean touchItem = false;
	private IMServiceConnector imServiceConnector = new IMServiceConnector(){
		@Override
		public void onIMServiceConnected() {
			imService = imServiceConnector.getIMService();
			if(imService == null){
				throw new RuntimeException("#connect imservice success,but is null");
			}

			currentUserId = HistoryTrackActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);

			currentDevice = imService.getContactManager()
					.findDeviceContact(currentUserId);

			if(currentDevice.getLatitude()!=0.0f||currentDevice.getLongitude()!=0.0f){

				LatLng cenpt  = new LatLng(currentDevice.getLatitude(), currentDevice.getLongitude());
				aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 14));
				aMap.getUiSettings().setZoomControlsEnabled(false);
//                aMap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
			}

		}

		@Override
		public void onServiceDisconnected() {

		}
	};
	//
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.historical_map);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.onCreate(savedInstanceState);// 此方法必须重写
		aMap = mMapView.getMap();
		imServiceConnector.connect(this);


		next  =(TextView)findViewById(R.id.next);
		start_name =(TextView)findViewById(R.id.start_name);  
		end_name =(TextView)findViewById(R.id.end_name);


		address =(TextView)findViewById(R.id.address);
		time =(TextView)findViewById(R.id.time);
		address_icon =(ImageView)findViewById(R.id.address_icon);
		address_text =(TextView)findViewById(R.id.address_text);



		history_address =(RelativeLayout)findViewById(R.id.history_address);
		address_layout =(RelativeLayout)findViewById(R.id.address_layout);
		hoster_start =(RelativeLayout)findViewById(R.id.hoster_start);
		address_icon_layout =(RelativeLayout)findViewById(R.id.address_icon_layout);


		mCalendarDateView =(CalendarDateView)findViewById(R.id.calendarDateView);
		calendarLayout  =(CalendarLayout)findViewById(R.id.calendarLayout);
		week_layout  =(LinearLayout)findViewById(R.id.week_layout);
		postion_layout  =(LinearLayout) findViewById(R.id.postion_relative_layout);
		postion_layout.setVisibility(View.GONE);

		icon_calendar  =(ImageView)findViewById(R.id.icon_calendar);
		mTitle =(TextView)findViewById(R.id.title);

		findViewById(R.id.zoom_in_btn).setOnClickListener(this);
		findViewById(R.id.zoom_out_btn).setOnClickListener(this);
		int[] data = CalendarUtil.getYMD(new Date());


		year = data[0];
		month = data[1];
		day  = data[2];


		tempYyear = data[0];
		tempMonth = data[1];
		tempDay  = data[2];

		initView();
		// 多个标记点的点击事件
		aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {

				int id = marker.getPeriod();

				history_address.setVisibility(View.GONE);
				address_layout.setVisibility(View.VISIBLE);
				address_icon_layout.setVisibility(View.VISIBLE);
				hoster_start.setVisibility(View.GONE);

				start_name.setVisibility(View.GONE);
				end_name.setVisibility(View.GONE);

				int index = id-1;
				if(index>=0&&(index<=(point.size()-1))){
					if(point.get(index)!=null){
						LatLonPoint latLong = new LatLonPoint(point.get(index).getLatitude(),
								point.get(index).getLongitude());
						getAddress(latLong);
						if(point.size()==1){
							address_text.setText("起点");
							address_icon.setBackgroundResource(R.drawable.icon_locationstart);
						}else{

							if(index == 0){
								address_text.setText("起点");
								address_icon.setBackgroundResource(R.drawable.icon_locationstart);
							}else if(index == point.size()-1){
								address_text.setText("终点");
								address_icon.setBackgroundResource(R.drawable.icon_locationend);
							}else {
								address_text.setText(index+"");
								address_icon.setBackgroundResource(R.drawable.icon_locationstart);
							}
						}
						time.setText(timesTwo(point.get(index).getTime()+""));

					}
				}

				touchItem = true;
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

	public static String timesTwo(String time) {
		SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd");
		@SuppressWarnings("unused")
		long lcc = Long.valueOf(time);
		//	int i = Integer.parseInt(time);
		Date data = new Date(lcc * 1000L);
		String min ="";
		if(data.getMinutes()<=9){
			min = "0" + data.getMinutes();
		}else{
			min = data.getMinutes() +"";
		}
		int month = data.getMonth() + 1;

		String times = "" + month + "月" + data.getDate() + "日" +"    " + data.getHours()+":" + min;
		//String times = sdr.format(new Date(lcc * 1000L));
		return times;

	}

	void showHistory(){

		point = IMUserActionManager.instance().onLocation();

		LatLng cenpt;
		if (point.size() <= 0) {
			// 设定中心点坐标
			cenpt = new LatLng(MainActivity.latitude, MainActivity.longitude);
		} else {
			cenpt = new LatLng(point.get(0).getLatitude(),point.get(0).getLongitude());
		}


		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cenpt, 14));
//		aMap.getUiSettings().setZoomControlsEnabled(false);

		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);


		aMap.clear();

 //测试数据用　暂时屏蔽
//		for (int i = 0; i < point.size(); i++) {
//			for (int j = point.size() - 1; j > i; j--) {
//				LatLng lat  = new LatLng(point.get(i).getLatitude(), point.get(i).getLongitude());
//				LatLng lat1  = new LatLng(point.get(j).getLatitude(), point.get(j).getLongitude());
//				if (AMapUtils.calculateLineDistance(lat, lat1) <= 80) {
//					point.remove(point.get(j));
//				}
//			}
//		}



		if(point.size()<=0){
			Utils.showToast(HistoryTrackActivity.this,"没有查询到数据");
			postion_layout.setVisibility(View.GONE);
		}else{
			postion_layout.setVisibility(View.VISIBLE);
		}


		for (int i = 0; i < point.size(); i++) {

			latlng = new LatLng(point.get(i).getLatitude(), point.get(i).getLongitude());

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


				BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(popup);
				//BitmapDescriptor	BitmapDescriptorFactory.fromView(android.view.View view)

				MarkerOptions options = new MarkerOptions();
				options.icon(bitmap);
				options.anchor(0.5f, 0.5f);
				options.position(new LatLng(point.get(i).getLatitude(), point.get(i).getLongitude()));
				Marker marker = aMap.addMarker(options);
				marker.setPeriod(i+1);

				LatLonPoint latLonStart = new LatLonPoint(point.get(i).getLatitude(), point.get(i).getLongitude());
				getAddress(latLonStart);

			} else {
				if (i == 0) {

					show_name.setText("起点");
					history_bg.setBackgroundResource(R.drawable.icon_locationstart);
					BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(popup);
					MarkerOptions options = new MarkerOptions();
					options.icon(bitmap);
					options.anchor(0.5f, 0.5f);
					options.position(new LatLng(point.get(i).getLatitude(), point.get(i).getLongitude()));
					Marker marker = aMap.addMarker(options);
					marker.setPeriod(i+1);

					LatLonPoint latLonStart = new LatLonPoint(point.get(i).getLatitude(), point.get(i).getLongitude());
					getAddress(latLonStart);

				} else if ((point.size() - 1) == i) {

					show_name.setText("终点");
					history_bg
							.setBackgroundResource(R.drawable.icon_locationend);

					BitmapDescriptor bitmapEnd = BitmapDescriptorFactory.fromView(popup);

					MarkerOptions options = new MarkerOptions();
					options.icon(bitmapEnd);
					options.anchor(0.5f, 0.5f);
					options.position(new LatLng(point.get(i).getLatitude(), point.get(i).getLongitude()));
					Marker marker = aMap.addMarker(options);
					marker.setPeriod(i+1);
					LatLonPoint latLonEnd = new LatLonPoint(point.get(i).getLatitude(), point.get(i).getLongitude());
					getAddress(latLonEnd);

				} else {

					show_name.setText("" + i);
					history_bg
							.setBackgroundResource(R.drawable.icon_locationstart);

					BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(popup);

					MarkerOptions options = new MarkerOptions();
					options.icon(bitmap);
					options.anchor(0.5f, 0.5f);
					options.position(new LatLng(point.get(i).getLatitude(), point.get(i).getLongitude()));
					Marker marker = aMap.addMarker(options);
					marker.setPeriod(i+1);

				}
			}
		}
	}


	private void initView() {

		mCalendarDateView.setAdapter(new CaledarAdapter() {
			@Override
			public View getView(View convertView, ViewGroup parentView, CalendarBean bean) {

				if (convertView == null) {
					convertView = LayoutInflater.from(parentView.getContext()).inflate(R.layout.item_calendar, null);
				}
				TextView chinaText = (TextView) convertView.findViewById(R.id.chinaText);
				TextView text = (TextView) convertView.findViewById(R.id.text);

				text.setText("" + bean.day);

				if ((bean.mothFlag != 0)  || (bean.year > tempYyear) || ((bean.year == tempYyear) && (bean.moth > tempMonth)) ||
                        ((bean.year == tempYyear) && (bean.moth == tempMonth) && (bean.day > tempDay))) {
					text.setTextColor(0xff9299a1);
				} else {
					text.setTextColor(0xff444444);
				}

				chinaText.setText(bean.chinaDay);

				return convertView;
			}
		});

		mCalendarDateView.setOnItemClickListener(new CalendarView.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int postion, CalendarBean bean) {


				mTitle.setText(bean.year + "年" + bean.moth + "月" + bean.day +"日");
				calendarLayout.setVisibility(View.GONE);
				week_layout.setVisibility(View.GONE);


				if((bean.year == tempYyear)&&(bean.moth == tempMonth)&&(bean.day == tempDay)){
					next.setVisibility(View.GONE);
				}else{
					next.setVisibility(View.VISIBLE);
				}

				SimpleDateFormat statDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

				String statTimeData = bean.year+"-" + bean.moth+"-" + bean.day+" " + "00:00:00";//"1970-01-06 11:45:55";
				String endTimeData = bean.year+"-" + bean.moth+"-" + bean.day+" " + "23:59:59";//"1970-01-06 11:45:55";

				long statTime = 0;
				long endTime  = 0;
				try {
					Date startDate =  statDateFormat.parse(statTimeData);
					statTime = startDate.getTime() / 1000;

					Date  endDate =  statDateFormat.parse(endTimeData);
					endTime = endDate.getTime() / 1000;

				} catch (ParseException e) {
					e.printStackTrace();
				}

				year  = bean.year;
				month = bean.moth;
				day   = bean.day;

				sendLocationPacket(currentUserId,statTime,endTime);

			}


			@Override
			public void onItemUpdateClick(View view, int postion, CalendarBean bean){

				mTitle.setText(bean.year + "年" + bean.moth + "月" + bean.day +"日");
			}
		});


		//int[] data = CalendarUtil.getYMD(new Date());
		mTitle.setText(year + "年" + month + "月" + day +"日");


		mTitle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(calendarLayout.getVisibility() == View.VISIBLE){
					calendarLayout.setVisibility(View.GONE);
					week_layout.setVisibility(View.GONE);

				}else{
					calendarLayout.setVisibility(View.VISIBLE);
					week_layout.setVisibility(View.VISIBLE);
				}

				mTitle.setText(year + "年" + month + "月" + day +"日");
			}
		});

		icon_calendar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(calendarLayout.getVisibility() == View.VISIBLE){
					calendarLayout.setVisibility(View.GONE);
					week_layout.setVisibility(View.GONE);

				}else{
					calendarLayout.setVisibility(View.VISIBLE);
					week_layout.setVisibility(View.VISIBLE);
				}
				mTitle.setText(year + "年" + month + "月" + day +"日");
			}
		});



		next.setVisibility(View.GONE);
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				calendarLayout.setVisibility(View.GONE);
				week_layout.setVisibility(View.GONE);

				SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
				String timeData = year+"-" + month +"-" + day+" " + "00:00:00";//"1970-01-06 11:45:55";


				try {
					Date data =  dateFormat.parse(timeData);
				 	long	dataTime = data.getTime() / 1000;

					long  statTime = dataTime + 86400;//86400000
					long  endTime = dataTime + 86400*2;

					sendLocationPacket(currentUserId,statTime,endTime);

					SimpleDateFormat format1 = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
					format1.format(new Date((statTime + 30)*1000L)); // + 30 防止误差
					Calendar calendar = format1.getCalendar();
					year = calendar.get(Calendar.YEAR);
					month = calendar.get(Calendar.MONTH) +1;
					day = calendar.get(Calendar.DATE);

					mTitle.setText(year + "年" + month + "月" + day +"日");


					if((year == tempYyear)&&(month == tempMonth)&&(day == tempDay)){
						next.setVisibility(View.GONE);
					}else{
						next.setVisibility(View.VISIBLE);
					}

				} catch (ParseException e) {
					e.printStackTrace();
				}

			}
		});

		TextView last  =(TextView)findViewById(R.id.last);
		last.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				calendarLayout.setVisibility(View.GONE);
				week_layout.setVisibility(View.GONE);


				SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
				String timeData = year+"-" + month +"-" + day+" " + "00:00:00";//"1970-01-06 11:45:55";


				try {
					Date data =  dateFormat.parse(timeData);
					long	dataTime = data.getTime() / 1000;

					long  statTime =dataTime - 86400 ;
					long  endTime =   dataTime;
					sendLocationPacket(currentUserId,statTime,endTime);

					SimpleDateFormat format1 = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
					format1.format(new Date((statTime + 30)*1000L)); // + 30 防止误差
					Calendar calendar = format1.getCalendar();
					year = calendar.get(Calendar.YEAR);
					month = calendar.get(Calendar.MONTH) +1;
					day = calendar.get(Calendar.DATE);

					mTitle.setText(year + "年" + month + "月" + day +"日");

					if((year == tempYyear)&&(month == tempMonth)&&(day == tempDay)){
						next.setVisibility(View.GONE);
					}else{
						next.setVisibility(View.VISIBLE);
					}

				} catch (ParseException e) {
					e.printStackTrace();
				}

			}
		});
	}


	public void sendLocationPacket(int userId ,long statTime,long endTime){

		start_name.setText("");
		end_name.setText("");

		start_name.setVisibility(View.VISIBLE);
		end_name.setVisibility(View.VISIBLE);

		aMap.clear();
		touchItem = false;

		hoster_start.setVisibility(View.VISIBLE);
		history_address.setVisibility(View.VISIBLE);
		address_layout.setVisibility(View.GONE);
		address_icon_layout.setVisibility(View.GONE);
		imService.getUserActionManager().onRepLocationReq(userId ,statTime,endTime);
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

				LatLonPoint resultPoint = result.getRegeocodeQuery().getPoint();

				if(touchItem == false){
					for (int i = 0; i < point.size(); i++) {
						if(i==0&&(point.get(i).getLatitude() == resultPoint.getLatitude())&&(point.get(i).getLongitude() == resultPoint.getLongitude())){
							start_name.setText("" + result.getRegeocodeAddress().getFormatAddress());
						}else  if(i==(point.size()-1)&&(point.get(i).getLatitude() == resultPoint.getLatitude())&&(point.get(i).getLongitude() == resultPoint.getLongitude())){
							end_name.setText("" + result.getRegeocodeAddress().getFormatAddress());
						}
					}
				}else{
					address.setText(""+result.getRegeocodeAddress().getFormatAddress());
				}

			}
		} else {

		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {
			case USER_INFO_UPDATE_QUERY_SUCCESS:
				showHistory();
				break;

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
	 * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
	 */
	private void changeCamera(CameraUpdate update, AMap.CancelableCallback callback) {
		aMap.animateCamera(update, 1000, callback);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.zoom_in_btn:
				changeCamera(CameraUpdateFactory.zoomIn(), null);
				break;
			case R.id.zoom_out_btn:
				changeCamera(CameraUpdateFactory.zoomOut(), null);
				break;


		}
	}
}

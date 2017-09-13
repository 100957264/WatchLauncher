package com.fise.marechat.function.location.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.fise.marechat.KApplication;
import com.fise.marechat.R;
import com.fise.marechat.function.location.LocationService;

public class IsHotWifiActivity extends Activity {
	

	private LocationService locService;
	private TextView res;
	private Button startBtn;
	private MyLocationListener listener;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_hotwifi);
		res = (TextView)findViewById(R.id.hotwifiResult);
		startBtn = (Button)findViewById(R.id.start);
		locService = ((KApplication) getApplication()).locationService;
		//LocationClientOption mOption = locService.getDefaultLocationClientOption();
		//mOption.setLocationMode(LocationClientOption.LocationMode.Battery_Saving); 
		//mOption.setCoorType("bd09ll");
		//locService.setLocationOption(mOption);
		listener = new MyLocationListener();
		locService.registerListener(listener);
		locService.start();
		
		
		startBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				boolean b = locService.requestHotSpotState();
				res.setText("");
				Log.i("lxm", b+"");
			}
			
		});

	}
	


	/***
	 * 定位结果回调，在此方法中处理定位结果
	 */
	
	class MyLocationListener extends BDAbstractLocationListener {

		@Override
		public void onConnectHotSpotMessage(String s, int i) {
			// TODO Auto-generated method stub
			Log.i("lxm", i+"  "+s);
			String resText = "";
			
			if(i == 0){
				resText = "不是wifi热点, wifi:"+s;
			} else if(i == 1){
				resText = "是wifi热点, wifi:"+s;
			} else if (i == -1){
				resText = "未连接wifi";
			}
        	res.setText(resText);
		}

		@Override
		public void onReceiveLocation(BDLocation arg0) {
			// TODO Auto-generated method stub
		}
		
	}
	
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
//		WriteLog.getInstance().close();
		locService.unregisterListener(listener);
		locService.stop();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

}

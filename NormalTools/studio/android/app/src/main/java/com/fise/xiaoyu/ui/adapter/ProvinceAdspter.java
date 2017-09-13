package com.fise.xiaoyu.ui.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout; 
import android.widget.TextView;
 
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.protobuf.IMBaseData.AddressProvince;
import com.fise.xiaoyu.ui.activity.MainActivity;

public class ProvinceAdspter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private Context context;
	public List<AddressProvince> provinceList;
	private UserEntity loginInfo;
	private String _city;
	private String _adminArea;

	public ProvinceAdspter(Context context, List<AddressProvince> provinceList,
			IMService imService) {
		this.context = context;
		this.provinceList = provinceList;
		this.layoutInflater = LayoutInflater.from(context);
		loginInfo = IMLoginManager.instance().getLoginInfo();

	}

	/**
	 * 组件集合，对应list.xml中的控件
	 */
	public final class City {
		public RelativeLayout top;
		public TextView top_name;

		public RelativeLayout location_info;
		public TextView location_text;
		public TextView location_right;

		public RelativeLayout city;
		public TextView city_name;
		public ImageView default_arrow;

	}

	@Override
	public int getCount() {
		return (provinceList.size() + 2);
	}

	/**
	 * 获得某一位置的数据
	 */
	@Override
	public Object getItem(int position) {
		if (position == 0) {
			return null;
		} else if (position == 1) {
			return null;
		} else {

			return provinceList.get(position);
		}
	}

	/**
	 * 获得唯一标识
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	public List<AddressProvince> getProvinceList() {
		return provinceList;
	}

	public void putProvinceList(List<AddressProvince> provinceList) {
		// this.deviceList.clear();
//		if (provinceList == null || provinceList.size() <= 0) {
//			return;
//		}
		this.provinceList = provinceList;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		City zujian = null;
		if (convertView == null) {
			zujian = new City();
			// 获得组件，实例化组件
			convertView = layoutInflater.inflate(R.layout.province_list_item,
					null);

			zujian.top = (RelativeLayout) convertView
					.findViewById(R.id.user_top_ling);
			zujian.top_name = (TextView) convertView
					.findViewById(R.id.top_name);

			zujian.location_info = (RelativeLayout) convertView
					.findViewById(R.id.location_info);
			zujian.location_text = (TextView) convertView
					.findViewById(R.id.location_text);
			zujian.location_right = (TextView) convertView
					.findViewById(R.id.location_right);

			zujian.city = (RelativeLayout) convertView.findViewById(R.id.city);
			zujian.city_name = (TextView) convertView
					.findViewById(R.id.city_name);

			zujian.default_arrow = (ImageView) convertView
					.findViewById(R.id.default_arrow);

			convertView.setTag(zujian);
		} else {
			zujian = (City) convertView.getTag();
		}

		if (position == 0) {

			zujian.top.setVisibility(View.VISIBLE);
			zujian.top.setVisibility(View.VISIBLE);
			zujian.top_name.setText("定位到的位置");

			zujian.location_info.setVisibility(View.VISIBLE);
			zujian.location_text.setVisibility(View.VISIBLE);
			//String name = getAddressbyGeoPoint(MainActivity.latitude,
			//		MainActivity.longitude);// getAddressbyGeoPoint(geoPoint);

		/*
			if(getAdminArea()==null||getAdminArea().equals("")
					||getCity()==null||getCity().equals("")){
				zujian.location_text.setText("无法获取到你的位置信息");
			}else{ 
			//	zujian.location_text.setText(name);
			}
			*/
			if(MainActivity.province==null||MainActivity.province.equals("")
					||MainActivity.cityName==null||MainActivity.cityName.equals("")){
				zujian.location_text.setText("无法获取到你的位置信息");
			}else{
				 zujian.location_text.setText("" + MainActivity.province + MainActivity.cityName);
			}
			_adminArea = MainActivity.province;
			_city = MainActivity.cityName;

			zujian.city.setVisibility(View.GONE);
			zujian.city_name.setVisibility(View.GONE);
			zujian.location_right.setVisibility(View.GONE);
			zujian.default_arrow.setVisibility(View.GONE);

		} else if (position == 1) {
			zujian.top.setVisibility(View.VISIBLE);
			zujian.top_name.setText("全部");

			zujian.city.setVisibility(View.VISIBLE);
			zujian.city_name.setVisibility(View.VISIBLE);
			zujian.city_name.setText("" + loginInfo.getProvince());

			zujian.location_text.setVisibility(View.GONE);
			zujian.location_info.setVisibility(View.GONE);

			zujian.location_right.setVisibility(View.VISIBLE);
			zujian.location_right.setText("已选地区");

			zujian.default_arrow.setVisibility(View.GONE);

		} else {

			zujian.top.setVisibility(View.GONE);
			zujian.location_info.setVisibility(View.GONE);

			zujian.location_text.setVisibility(View.GONE);
			zujian.location_info.setVisibility(View.GONE);

			zujian.city.setVisibility(View.VISIBLE);
			zujian.city_name.setVisibility(View.VISIBLE);
			zujian.location_right.setVisibility(View.GONE);

			zujian.city_name.setText(""
					+ provinceList.get(position - 2).getProvinceName());
			zujian.default_arrow.setVisibility(View.VISIBLE);

		}

		return convertView;
	}

	
	public String getCity(){
		return _city;
	}
	
	public String getAdminArea(){
		return _adminArea;
	}

	
}

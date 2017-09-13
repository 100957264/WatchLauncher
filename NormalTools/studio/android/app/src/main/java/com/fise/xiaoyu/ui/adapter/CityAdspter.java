package com.fise.xiaoyu.ui.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.entity.ReqMessage;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.protobuf.IMBaseData.AddressCity;
import com.fise.xiaoyu.protobuf.IMBaseData.AddressProvince;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;

public class CityAdspter extends BaseAdapter {  
   
    private  LayoutInflater layoutInflater;  
    private  Context context;   
    public   List<AddressCity>  cityList;
    private UserEntity loginInfo;
    
    
    public CityAdspter(Context context ,List<AddressCity> cityList ,IMService imService){  
        this.context=context;  
        this.cityList = cityList;  
        this.layoutInflater= LayoutInflater.from(context); 
        loginInfo = IMLoginManager.instance().getLoginInfo();
         
    }  
    /** 
     * 组件集合，对应list.xml中的控件
     */  
    public final class City{  
        public RelativeLayout top;  
        public TextView top_name;
        
        public RelativeLayout location_info;   
        public TextView location_text;
        public TextView location_right;
        
        
        public RelativeLayout city;
        public TextView  city_name;
        
    }  
    @Override  
    public int getCount() {  
        return (cityList.size() + 1);  
    }  
    /** 
     * 获得某一位置的数据 
     */  
    @Override  
    public Object getItem(int position) {  
    	if(position == 0)
    	{
            return null; 
    	} else{

            return cityList.get(position); 
    	} 
    }  
    /** 
     * 获得唯一标识 
     */  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
  
    public List<AddressCity> getCityList(){
    	return cityList;
    }
    
    
    
    public void putCityList(List<AddressCity> cityList){
       // this.deviceList.clear();
        if(cityList == null || cityList.size() <=0) {
            return;
        }
        this.cityList = cityList;
        notifyDataSetChanged();
    }
    
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
    	City zujian=null;  
        if(convertView==null){  
            zujian=new City();  
            //获得组件，实例化组件  
            convertView=layoutInflater.inflate(R.layout.city_list_item, null); 
            
            zujian.top = (RelativeLayout)convertView.findViewById(R.id.user_top_ling);  
            zujian.top_name  = (TextView)convertView.findViewById(R.id.top_name);  
            
            zujian.location_info = (RelativeLayout)convertView.findViewById(R.id.location_info);  
            zujian.location_text  = (TextView)convertView.findViewById(R.id.location_text);  
            
            zujian.city = (RelativeLayout)convertView.findViewById(R.id.city);  
            zujian.city_name  = (TextView)convertView.findViewById(R.id.city_name);   
            zujian.location_right  = (TextView)convertView.findViewById(R.id.location_right);  
             
            convertView.setTag(zujian);  
        }else{  
            zujian=(City)convertView.getTag();  
        }  
         
        
//        if(position == 0)
//        {
//
//        	zujian.top.setVisibility(View.VISIBLE);
//        	zujian.top.setVisibility(View.VISIBLE);
//        	zujian.top_name.setText("定位到的位置");
//        	 
//        	zujian.location_info.setVisibility(View.VISIBLE); 
//        	zujian.location_text.setVisibility(View.VISIBLE); 
//        	zujian.location_text.setText("广东 深圳");  
//        	 
//        	zujian.city.setVisibility(View.GONE);
//        	zujian.city_name.setVisibility(View.GONE);
//        	
//        	 
//        }else 
        
        if(position == 0)
        {
        	zujian.top.setVisibility(View.VISIBLE);
        	zujian.top_name.setText("全部"); 
        	 
        	zujian.city.setVisibility(View.VISIBLE); 
        	zujian.city_name.setVisibility(View.VISIBLE); 
        	zujian.city_name.setText("" + loginInfo.getCity());
        	 
        	zujian.location_text.setVisibility(View.GONE); 
        	zujian.location_info.setVisibility(View.GONE);
        	  
        	zujian.location_right.setVisibility(View.VISIBLE);
        	zujian.location_right.setText("已选地区");
        	
        }else{
        	
        	zujian.top.setVisibility(View.GONE); 
        	zujian.location_info.setVisibility(View.GONE); 
        	
        	zujian.location_text.setVisibility(View.GONE); 
        	zujian.location_info.setVisibility(View.GONE);
        	
        	zujian.city.setVisibility(View.VISIBLE); 
        	zujian.city_name.setVisibility(View.VISIBLE); 
        	
        	zujian.city_name.setText("" + cityList.get(position -1).getCityName());
        	zujian.location_right.setVisibility(View.GONE);
        	
        }
       
        return convertView;  
    }   
}  


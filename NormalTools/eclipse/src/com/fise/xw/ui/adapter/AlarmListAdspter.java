package com.fise.xw.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

import com.fise.xw.R;
import com.fise.xw.DB.entity.WhiteEntity;

public class AlarmListAdspter extends BaseAdapter {  
  
    private List<WhiteEntity> alarmList;  
    private LayoutInflater layoutInflater;  
    private Context context;  
    
    public AlarmListAdspter(Context context,List<WhiteEntity> alarmList){  
        this.context=context;  
        this.alarmList = alarmList;  
        this.layoutInflater=LayoutInflater.from(context);  
    }  
    /** 
     * 组件集合，对应list.xml中的控件 
     * @author Administrator 
     */  
    public final class Zujian{   
        public EditText phone;   
    }  
    @Override  
    public int getCount() {  
        return alarmList.size();  
    }  
    /** 
     * 获得某一位置的数据 
     */  
    @Override  
    public Object getItem(int position) {  
        return alarmList.get(position);  
    }  
    /** 
     * 获得唯一标识 
     */  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
  
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
        Zujian zujian=null;  
        if(convertView==null){  
            zujian=new Zujian();  
            //获得组件，实例化组件  
            convertView =layoutInflater.inflate(R.layout.alarm_list_item, null);   
            
            convertView.setTag(zujian);  
        }else{  
            zujian=(Zujian)convertView.getTag();  
        }  
         
        zujian.phone =(EditText)convertView.findViewById(R.id.alarm_list_phone);   
        //绑定数据    
        zujian.phone.setText(alarmList.get(position).getPhone());   
        return convertView;  
    }  
  
    
    public void putDeviceList(List<WhiteEntity> alarmList){ 
    	
         if(alarmList == null || alarmList.size() <=0) {
             return;
         }
         this.alarmList = alarmList;
         notifyDataSetChanged();
     }
}  
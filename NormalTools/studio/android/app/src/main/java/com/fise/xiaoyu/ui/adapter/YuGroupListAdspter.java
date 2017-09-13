package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;

import java.util.ArrayList;
import java.util.List;

public class YuGroupListAdspter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private Context context;
    private IMService imService;
    public  List<GroupEntity>  YuGroupList = new ArrayList<>();

    public YuGroupListAdspter(Context context , List<GroupEntity> YuGroupList , IMService imService){
        this.context=context;  
        this.YuGroupList = YuGroupList;
        this.layoutInflater=LayoutInflater.from(context);  
        this.imService = imService; 
    }  
    
    /** 
     * 组件集合，对应list.xml中的控件
     */  
    public final class Zujian{  
        public IMBaseImageView userImage;  
        public TextView userName;    
    }  
    @Override  
    public int getCount() {  
        return YuGroupList.size();
    }  
    /** 
     * 获得某一位置的数据 
     */  
    @Override  
    public Object getItem(int position) {  
        return YuGroupList.get(position);
    }  
    /** 
     * 获得唯一标识 
     */  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
   
    public void putYuGroupList(List<GroupEntity> YuGroupList){
        this.YuGroupList = YuGroupList;
        notifyDataSetChanged();  
    }
     
    
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {
        Zujian zujian=null;  
        if(convertView==null){  
            zujian=new Zujian();  
            //获得组件，实例化组件  
            convertView= layoutInflater.inflate(R.layout.black_list_item, null);
            zujian.userImage = (IMBaseImageView)convertView.findViewById(R.id.img);  
            zujian.userName  = (TextView)convertView.findViewById(R.id.name);     
            
            convertView.setTag(zujian);  
        }else{  
            zujian=(Zujian)convertView.getTag();  
        }  

        final GroupEntity entity =  YuGroupList.get(position);
         
        //绑定数据  
        zujian.userImage.setImageUrl(entity.getAvatar());
        zujian.userName.setText(entity.getMainName());

        return convertView;
    }   
}  


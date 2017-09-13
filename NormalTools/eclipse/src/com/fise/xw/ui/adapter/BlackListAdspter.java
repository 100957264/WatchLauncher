package com.fise.xw.ui.adapter;

import java.util.ArrayList;
import java.util.List; 
import android.content.Context; 
import android.view.LayoutInflater;
import android.view.View; 
import android.view.ViewGroup;
import android.widget.BaseAdapter; 
import android.widget.TextView;

import com.fise.xw.R; 
import com.fise.xw.DB.entity.UserEntity;  
import com.fise.xw.imservice.service.IMService; 
import com.fise.xw.ui.widget.IMBaseImageView; 

public class BlackListAdspter extends BaseAdapter {  
   
    private LayoutInflater layoutInflater;  
    private Context context;  
    private IMService imService;
    public  List<UserEntity>  blackList = new ArrayList<>();
    
    public BlackListAdspter(Context context ,List<UserEntity> blackList ,IMService imService){  
        this.context=context;  
        this.blackList = blackList;  
        this.layoutInflater=LayoutInflater.from(context);  
        this.imService = imService; 
    }  
    
    /** 
     * 组件集合，对应list.xml中的控件 
     * @author Administrator 
     */  
    public final class Zujian{  
        public IMBaseImageView userImage;  
        public TextView userName;    
    }  
    @Override  
    public int getCount() {  
        return blackList.size();  
    }  
    /** 
     * 获得某一位置的数据 
     */  
    @Override  
    public Object getItem(int position) {  
        return blackList.get(position);  
    }  
    /** 
     * 获得唯一标识 
     */  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
   
    public void putBlackList(List<UserEntity> blackList){
        //this.blackList.clear();
        //if(blackList == null || blackList.size() <=0) {
        //    return;
       // }
        this.blackList = blackList;
        notifyDataSetChanged();  
    }
     
    
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
        Zujian zujian=null;  
        if(convertView==null){  
            zujian=new Zujian();  
            //获得组件，实例化组件  
            convertView=layoutInflater.inflate(R.layout.black_list_item, null);  
            zujian.userImage = (IMBaseImageView)convertView.findViewById(R.id.img);  
            zujian.userName  = (TextView)convertView.findViewById(R.id.name);     
            
            convertView.setTag(zujian);  
        }else{  
            zujian=(Zujian)convertView.getTag();  
        }  
         
        final UserEntity entity =  blackList.get(position); 
         
        //绑定数据  
        zujian.userImage.setImageUrl(entity.getAvatar());  
        
        if(entity.getComment().equals(""))
        {
        	zujian.userName.setText(entity.getMainName());   
        }else{
        	zujian.userName.setText(entity.getComment());   
        }
          
       return convertView;  
    }   
}  


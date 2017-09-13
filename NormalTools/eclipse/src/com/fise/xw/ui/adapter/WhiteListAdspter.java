package com.fise.xw.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.entity.WhiteEntity;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.protobuf.IMDevice.SettingType;
import com.fise.xw.utils.IMUIHelper;

public class WhiteListAdspter extends BaseAdapter  implements  
AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {  
  
    private List<WhiteEntity> whiteList;  
    private LayoutInflater layoutInflater;  
    private Context context;  
    private int deviceId;
    private SettingType type;
    private DeviceEntity device;
    private UserEntity loginContact;
    
    public WhiteListAdspter(Context context,List<WhiteEntity> whiteList,int deviceId,SettingType type,DeviceEntity device){  
        this.context=context;  
        this.whiteList = whiteList;  
        this.layoutInflater=LayoutInflater.from(context);  
        this.deviceId = deviceId;
        this.type = type;
        this.device = device; 
        
        loginContact = IMLoginManager.instance().getLoginInfo();
        
    }  
    /** 
     * 组件集合，对应list.xml中的控件 
     * @author Administrator 
     */  
    public final class Zujian{   
        public TextView phone;   
    }  
    @Override  
    public int getCount() {  
        return whiteList.size();  
    }  
    /** 
     * 获得某一位置的数据 
     */  
    @Override  
    public Object getItem(int position) {  
        return whiteList.get(position);  
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
            convertView =layoutInflater.inflate(R.layout.white_list_item, null);   
            zujian.phone =(TextView)convertView.findViewById(R.id.white_list_phone);   
            
            convertView.setTag(zujian);  
        }else{  
            zujian=(Zujian)convertView.getTag();  
        }  
        //绑定数据    
        zujian.phone.setText(whiteList.get(position).getPhone());   
        return convertView;  
    }  
  
    
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    	
    	if (device != null&& device.getMasterId() == loginContact.getPeerId()) { 
        	WhiteEntity contact = (WhiteEntity) getItem(position);
            IMUIHelper.handleDeviceItemLongClick(deviceId,contact, type,context);
		}
		
        return true;
    }
    
    public void putDeviceList(List<WhiteEntity> whiteList){ 
    	
         if(whiteList == null) {
             return;
         }
         this.whiteList = whiteList;
         notifyDataSetChanged();
     }
    
    
    public void updateDeviceList(List<WhiteEntity> whiteList){ 
    	
        if(whiteList == null ) {
            return;
        }
        this.whiteList = whiteList;
        notifyDataSetChanged();
    }
    
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub 
	}
}  
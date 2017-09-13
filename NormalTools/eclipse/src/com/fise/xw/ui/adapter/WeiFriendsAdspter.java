package com.fise.xw.ui.adapter;

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
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.GroupEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.entity.WeiEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.imservice.entity.ReqMessage;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.protobuf.IMUserAction.ActionResult;
import com.fise.xw.ui.activity.WeiFriendsActivity;
import com.fise.xw.ui.widget.IMBaseImageView;
import com.fise.xw.utils.IMUIHelper;

public class WeiFriendsAdspter extends BaseAdapter {  
   
    private LayoutInflater layoutInflater;  
    private Context context;  
    private IMService imService;
    public  List<WeiEntity>  weiList = new ArrayList<>();
    
    public WeiFriendsAdspter(Context context ,List<WeiEntity> weiList ,IMService imService){  
        this.context=context;  
        this.weiList = weiList;  
        this.layoutInflater=LayoutInflater.from(context);  
        this.imService = imService; 
    }  
    
    /** 
     * 组件集合，对应list.xml中的控件 
     * @author Administrator 
     */  
    public final class Zujian{  
        public IMBaseImageView groupImage;  
        public TextView groupName;    
        public TextView caption; 
        public Button confReq;   
    }  
    @Override  
    public int getCount() {  
        return weiList.size();  
    }  
    /** 
     * 获得某一位置的数据 
     */  
    @Override  
    public Object getItem(int position) {  
        return weiList.get(position);  
    }  
    /** 
     * 获得唯一标识 
     */  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
   
    public void putWeiList(List<WeiEntity> weiList){
        this.weiList.clear();
        if(weiList == null || weiList.size() <=0) {
            return;
        }
        this.weiList = weiList;
        notifyDataSetChanged();
    }
    
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
        Zujian zujian=null;  
        if(convertView==null){  
            zujian=new Zujian();  
            //获得组件，实例化组件  
            convertView=layoutInflater.inflate(R.layout.wei_list_item, null);  
            zujian.groupImage = (IMBaseImageView)convertView.findViewById(R.id.img);  
            zujian.groupName  = (TextView)convertView.findViewById(R.id.tv);    
            zujian.caption  = (TextView)convertView.findViewById(R.id.caption); 
            zujian.confReq  = (Button)convertView.findViewById(R.id.confReq);    
              
            convertView.setTag(zujian);  
        }else{  
            zujian=(Zujian)convertView.getTag();  
        }  
        
        int id = weiList.get(position).getFromId(); 
        final UserEntity entity = imService.getContactManager().findContact(id);
         
        //绑定数据  
        zujian.groupImage.setImageUrl(entity.getAvatar());  
        
        if(entity.getComment().equals(""))
        {
        	zujian.groupName.setText(entity.getMainName());   
        }else{
        	zujian.groupName.setText(entity.getComment());   
        }
        
        zujian.caption.setText(entity.getMainName()+"请求加你为位友"); 
      
        if(entity.getIsFriend() == DBConstant.FRIENDS_TYPE_WEI){
        	zujian.confReq.setText("已同意");
        }
         
        
        final int toId = weiList.get(position).getFromId();
        final int actId = weiList.get(position).getActId();
        final WeiEntity req =  weiList.get(position);
        final int actType = weiList.get(position).getActType();
        zujian.confReq.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) { 
	        	 if(entity.getIsFriend() != DBConstant.FRIENDS_TYPE_WEI)
	        	 { 
	        		 imService.getUserActionManager().confirmWeiFriends(toId, actId, actType,ActionResult.ACTION_RESULT_YES,req,""); 
	        	 } 
	         } 
       }); 
           
       return convertView;  
    }   
}  


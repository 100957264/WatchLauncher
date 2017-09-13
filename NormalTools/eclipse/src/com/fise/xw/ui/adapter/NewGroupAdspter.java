package com.fise.xw.ui.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.SysConstant;
import com.fise.xw.imservice.entity.ReqMessage;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.ui.widget.IMBaseImageView;
import com.fise.xw.ui.widget.IMGroupAvatar;
import com.fise.xw.utils.ScreenUtil;

public class NewGroupAdspter extends BaseAdapter {  
   
    private LayoutInflater layoutInflater;  
    private Context context;  
    private IMService imService;
    public   List<GroupEntity>  groupList = new ArrayList<>();
    
    public NewGroupAdspter(Context context ,List<GroupEntity> groupList ,IMService imService){  
        this.context=context;  
        this.groupList = groupList;  
        this.layoutInflater=LayoutInflater.from(context);  
        this.imService = imService; 
    }  
    /** 
     * 组件集合，对应list.xml中的控件 
     * @author Administrator 
     */  
    public final class Zujian{  
        public IMGroupAvatar groupImage;  
        public TextView groupName;    
    }  
    @Override  
    public int getCount() {  
        return groupList.size();  
    }  
    /** 
     * 获得某一位置的数据 
     */  
    @Override  
    public Object getItem(int position) {  
        return groupList.get(position);  
    }  
    /** 
     * 获得唯一标识 
     */  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
  
    
    public void putGroupList(List<GroupEntity> pGroupList){
//        this.groupList.clear();
//        if(pGroupList == null || pGroupList.size() <=0) {
//            return;
//        }
        this.groupList = pGroupList;
        notifyDataSetChanged();
    }
    
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
        Zujian zujian=null;  
        if(convertView==null){  
            zujian=new Zujian();  
            //获得组件，实例化组件  
            convertView=layoutInflater.inflate(R.layout.group_list_item, null);  
            zujian.groupImage = (IMGroupAvatar)convertView.findViewById(R.id.contact_portrait);  
            zujian.groupName  = (TextView)convertView.findViewById(R.id.tv);    
            convertView.setTag(zujian);  
        }else{  
            zujian=(Zujian)convertView.getTag();  
        }  
         
        zujian.groupImage.setVisibility(View.VISIBLE);
        List<String> avatarUrlList = new ArrayList<>();
		Set<Integer> userIds = groupList.get(position).getlistGroupMemberIds();
		int i = 0;
		for (Integer buddyId : userIds) {
			UserEntity entity = imService.getContactManager().findContact(
					buddyId);
			if (entity == null) {
				// logger.d("已经离职。userId:%d", buddyId);
				continue;
			}
			 
			avatarUrlList.add(entity.getAvatar());
			if (i >= DBConstant.GROUP_AVATAR_NUM -1) {
				break;
			}
			i++;
		} 
		setGroupAvatar(zujian.groupImage, avatarUrlList);
		
       // zujian.groupImage.setImageUrl(groupList.get(position).getAvatar());  
        zujian.groupName.setText(groupList.get(position).getMainName());   
      
  
             
        return convertView;  
    }  
	/**
	 * 与search 有公用的地方，可以抽取IMUIHelper 设置群头像
	 * 
	 * @param avatar
	 * @param avatarUrlList
	 */
	private void setGroupAvatar(IMGroupAvatar avatar, List<String> avatarUrlList) {
		try {
			avatar.setViewSize(ScreenUtil.instance(context).dip2px(45));
			avatar.setChildCorner(2);
			avatar.setAvatarUrlAppend(SysConstant.AVATAR_APPEND_32);
			avatar.setParentPadding(3);
			avatar.setAvatarUrls((ArrayList<String>) avatarUrlList);
		} catch (Exception e) {
			//logger.e(e.toString());
		}
		
//		try {
//			if (null == avatarUrlList) {
//				return;
//			}
//			avatar .setAvatarUrlAppend(SysConstant.AVATAR_APPEND_32);
//			avatar.setChildCorner(3);
//			if (null != avatarUrlList) { 
//				avatar.setAvatarUrls(new ArrayList<String>(
//						avatarUrlList));
//			}
//		} catch (Exception e) {
//			//logger.e(e.toString());
//		}
	}

  
}  


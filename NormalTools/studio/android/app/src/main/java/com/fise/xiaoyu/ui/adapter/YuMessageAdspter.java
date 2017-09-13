package com.fise.xiaoyu.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.entity.WeiEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.entity.UnreadEntity;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.protobuf.IMUserAction;
import com.fise.xiaoyu.ui.activity.MessageActivity;
import com.fise.xiaoyu.ui.activity.RefuseReqActivity;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class YuMessageAdspter extends BaseAdapter implements
        AdapterView.OnItemLongClickListener {

    private  LayoutInflater layoutInflater;
    private  Context context;
    private  IMService imService;
    public   List<WeiEntity>  weiEntityList = new ArrayList<>();

    public YuMessageAdspter(Context context , List<WeiEntity> weiEntityList , IMService imService){
        this.context=context;  
        this.weiEntityList = weiEntityList;
        this.layoutInflater=LayoutInflater.from(context);  
        this.imService = imService;
        updateInfo(this.weiEntityList);
    }  
    /** 
     * 组件集合，对应list.xml中的控件
     */  
    public final class Zujian{  
        public IMBaseImageView reqYuImage;
        public TextView reqYuName;

        public TextView message_data;
        public TextView message_refuse;
        public TextView message_agree;
        public TextView message_verify;


    }  
    @Override  
    public int getCount() {  
        return weiEntityList.size();
    }  
    /** 
     * 获得某一位置的数据 
     */  
    @Override  
    public Object getItem(int position) {  
        return weiEntityList.get(position);
    }  
    /** 
     * 获得唯一标识 
     */  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }



    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                   int position, long arg3) {

        // TODO Auto-generated method stub
        // When clicked, show a toast with the TextView text

        int type = 0;
        Object object;
        //int realIndex = position - userList.size();

        object = weiEntityList.get(position);
        type = DBConstant.DETLE_REQ_TYPE_YU;


         int tempType = type;
        if (object instanceof WeiEntity) {

            final WeiEntity Entity = (WeiEntity) object;
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    new ContextThemeWrapper(context,
                            android.R.style.Theme_Holo_Light_Dialog));

            if(Entity.getFromId() == IMLoginManager.instance().getLoginId()){
                tempType  = DBConstant.DETLE_PARENT_REFUSE;
            }
            String temp = "";

            if (tempType == DBConstant.DETLE_REQ_TYPE_YU) {
                temp = "删除雨友请求";
            } else if (tempType == DBConstant.DETLE_REQ_TYPE) {
                temp = "删除好友请求";
            }else if(tempType == DBConstant.DETLE_PARENT_REFUSE)
            {
                temp = "删除请求";
            }

            String[] items = new String[] { temp };

            // String[] items = new String[] { NewFriendActivity.this
            // .getString(R.string.delete_device) };

            final int finalTempType = tempType;
            builder.setItems(items, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:

                            if (imService != null)
                            {
                                imService.getUserActionManager().deleteReqFriends(
                                        finalTempType, Entity);

                                int toActId = 0;
                                int actId = 0;
                                int actType = 0;

                                toActId = Entity.getFromId();
                                actId = Entity.getActId();
                                actType = Entity.getActType();

                                imService.getUserActionManager().confirmWeiFriends(
                                        toActId, actId, actType,
                                        IMUserAction.ActionResult.ACTION_RESULT_DELETE, Entity,
                                        "");
                            }
                            break;
                    }
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();

        }
        return true;

    }
    public void updateInfo(List<WeiEntity> weiEntityList) {

        ArrayList<Integer> userIds = new ArrayList<>();
        for(int i=0;i<weiEntityList.size();i++){
            UserEntity user = imService.getContactManager().findFriendsContact(weiEntityList.get(i).getFromId());
            if(user==null)
            {
                user = imService.getContactManager().findContact(weiEntityList.get(i).getFromId());
            }
            if(user==null)
            {
                user = imService.getContactManager().findReq(weiEntityList.get(i).getFromId());
            }
            if(user==null)
            {
                user = imService.getContactManager().findDeviceContact(weiEntityList.get(i).getFromId());
            }

            if(user==null)
            {
                userIds.add(weiEntityList.get(i).getFromId());
            }
        }

        if(userIds.size()>0)
        {
            imService.getContactManager().reqGetDetaillUsers(userIds);
        }

    }
    
    public void putWeiEntityList(List<WeiEntity> weiEntityList){
        this.weiEntityList = weiEntityList;
        updateInfo(this.weiEntityList);
        notifyDataSetChanged();
    }

    @Override  
    public View getView(final int position, View convertView, ViewGroup parent) {
        Zujian zujian=null;  
        if(convertView==null){  
            zujian=new Zujian();  
            //获得组件，实例化组件  
            convertView=layoutInflater.inflate(R.layout.yumessage_list_item, null);
            zujian.reqYuImage = (IMBaseImageView)convertView.findViewById(R.id.img);
            zujian.reqYuName  = (TextView)convertView.findViewById(R.id.yu_name);

            zujian.message_data  = (TextView)convertView.findViewById(R.id.message_data);
            zujian.message_refuse  = (TextView)convertView.findViewById(R.id.message_refuse);
            zujian.message_agree  = (TextView)convertView.findViewById(R.id.message_agree);
            zujian.message_verify  = (TextView)convertView.findViewById(R.id.message_verify);


            convertView.setTag(zujian);  
        }else{  
            zujian=(Zujian)convertView.getTag();  
        }

        int id;
        if(weiEntityList.get(position).getFromId() == IMLoginManager.instance().getLoginId()){
            id = weiEntityList.get(position).getToId();
        }else
        {
            id = weiEntityList.get(position).getFromId();
        }

        //绑定数据
        UserEntity user = imService.getContactManager().findFriendsContact(id);

        if(user==null)
        {
            user = imService.getContactManager().findReq(id);
        }
        if(user==null)
        {
            user = imService.getContactManager().findDeviceContact(id);
        }
        if(user==null)
        {
            user = imService.getContactManager().findContact(id);
        }


        if(user==null)
        {
            return convertView;
        }



        if(Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {

            zujian.message_agree.setVisibility(View.GONE);
            zujian.message_refuse.setVisibility(View.GONE);
            zujian.message_verify.setVisibility(View.VISIBLE);
            if( weiEntityList.get(position).getStatus() == DBConstant.FRIENDS_AGREE)
            {
                zujian.message_verify.setText("已同意");
            }else if( weiEntityList.get(position).getStatus() == DBConstant.FRIENDS_REFUSE) {
                zujian.message_verify.setText("申请被拒");
            }else{
                zujian.message_verify.setText("正在审核中");
            }

        }else{

            if( weiEntityList.get(position).getStatus() == DBConstant.FRIENDS_AGREE)
            {

                zujian.message_verify.setVisibility(View.VISIBLE);
                zujian.message_verify.setText("已同意");
                zujian.message_agree.setVisibility(View.GONE);
                zujian.message_refuse.setVisibility(View.GONE);

            }else if( weiEntityList.get(position).getStatus() == DBConstant.FRIENDS_REFUSE) {

                zujian.message_verify.setVisibility(View.VISIBLE);
                zujian.message_verify.setText("已拒绝");
                zujian.message_agree.setVisibility(View.GONE);
                zujian.message_refuse.setVisibility(View.GONE);

            }else{
                zujian.message_agree.setVisibility(View.VISIBLE);
                zujian.message_refuse.setVisibility(View.VISIBLE);
                zujian.message_verify.setVisibility(View.GONE);
            }

        }



        zujian.reqYuImage.setCorner(90);
        zujian.reqYuImage.setImageUrl(user.getAvatar());
        zujian.reqYuName.setText(user.getMainName());
        zujian.message_data.setText(weiEntityList.get(position).getMasgData());


        zujian.message_refuse.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

            }
        });


        final UserEntity finalUser = user;
        zujian.message_agree.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                String content = "你们是朋友 ,现在可以相互聊天了";
                imService.getUserActionManager().confirmYuFriends(
                        finalUser.getPeerId(), weiEntityList.get(position).getActId(),
                        weiEntityList.get(position).getActType(), IMUserAction.ActionResult.ACTION_RESULT_YES,
                        weiEntityList.get(position), content,weiEntityList.get(position).getDevice_id());
            }
        });


        zujian.message_refuse.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {


                Intent intent = new Intent(context, RefuseReqActivity.class);
                intent.putExtra(IntentConstant.YU_REQ_TYPE, DBConstant.FRIENDS_REQ);
                intent.putExtra(IntentConstant.YU_ENTITY_ID, weiEntityList.get(position).getFromId());
                intent.putExtra(IntentConstant.YU_REQ_ID, finalUser.getPeerId());

                context.startActivity(intent);

            }
        });


        return convertView;  
    }  
     
  
}  


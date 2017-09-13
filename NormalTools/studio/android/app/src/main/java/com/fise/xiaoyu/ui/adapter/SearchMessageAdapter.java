package com.fise.xiaoyu.ui.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.ui.widget.IMGroupAvatar;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.ScreenUtil;

/**
 * SearchMessageAdapter
 */
public class SearchMessageAdapter extends BaseAdapter implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener{

    private Logger logger = Logger.getLogger(SearchMessageAdapter.class);

    private List<MessageEntity>  userList = new ArrayList<>(); 

    private String searchKey;
    private Context ctx;
    private IMService imService;
    public SearchMessageAdapter(Context context,IMService pimService){
        this.ctx = context;
        this.imService = pimService;
    }

    public void clear(){
        this.userList.clear(); 
        notifyDataSetChanged();
    }

    public void putMessageList(List<MessageEntity> pUserList){
        this.userList.clear();
        if(pUserList == null || pUserList.size() <=0){
            return;
        }
        this.userList = pUserList;
    }
  
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object =  getItem(position);
        if(object instanceof MessageEntity){
        	MessageEntity userEntity = (MessageEntity) object;

            //找到对应是第几行消息
            int index = 0;
            for(int i=0;i<userList.size();i++){
                if(userList.get(i).getMsgId() == userEntity.getMsgId()){
                    index = i;
                    break;
                }
            }
            IMUIHelper.openChaSearchtActivity(ctx, userEntity.getSessionKey(),index); //userEntity.getMsgId()
        } 
    }
 
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Object object =  getItem(position);
//        if(object instanceof UserEntity){
//            UserEntity userEntity = (UserEntity) object;
//            IMUIHelper.handleContactItemLongClick(userEntity, ctx);
//        }else{
//        }
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        // 根据entity的类型进行判断，  或者根据长度判断
        int userSize = userList==null?0:userList.size(); 

        if(position < userSize){
            return SearchType.USER.ordinal();
        }
		return userSize;   
    }

    @Override
    public int getViewTypeCount() {
        return SearchType.values().length;
    }

    @Override
    public int getCount() {
        // todo  Elegant code 
        int userSize = userList==null?0:userList.size(); 
        return userSize;
    }

    @Override
    public Object getItem(int position) {
        int typeIndex =  getItemViewType(position);
        SearchType renderType = SearchType.values()[typeIndex];
        switch (renderType){
            case USER:{
                return userList.get(position);
            }  

            default:
                throw new IllegalArgumentException("SearchAdapter#getItem#不存在的类型" + renderType.name());
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int typeIndex =  getItemViewType(position);
        SearchType renderType = SearchType.values()[typeIndex];
        View view = null;
        switch (renderType){
            case USER:{
                view = renderUser(position,convertView,parent);
            }
            break;  
        }
        return view;
    }

    public View renderUser(int position, View view, ViewGroup parent){
        UserHolder userHolder = null;
        MessageEntity  userEntity= (MessageEntity)getItem(position);
        if(userEntity == null){
            logger.e("SearchAdapter#renderUser#userEntity is null!position:%d",position);
            return null;
        }
        if (view == null) {
            userHolder = new UserHolder();
            view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_contact, parent,false);
            userHolder.nameView = (TextView) view.findViewById(R.id.contact_item_title);
            userHolder.realNameView = (TextView) view.findViewById(R.id.contact_realname_title);
            userHolder.sectionView = (TextView) view.findViewById(R.id.contact_category_title);
            userHolder.avatar = (IMBaseImageView)view.findViewById(R.id.contact_portrait);
            userHolder.divider = view.findViewById(R.id.contact_divider);
            view.setTag(userHolder);
        } else {
            userHolder = (UserHolder) view.getTag();
        }

       // IMUIHelper.setTextHilighted(userHolder.nameView,userEntity.getMainName(),userEntity.getSearchElement());
        
        userHolder.nameView.setText(userEntity.getContent());

        userHolder.avatar.setImageResource(R.drawable.tt_default_user_portrait_corner);
        userHolder.divider.setVisibility(View.VISIBLE);

        // 分栏显示“联系人”
        if (position == 0) {
            userHolder.sectionView.setVisibility(View.VISIBLE);
            userHolder.sectionView.setText(ctx.getString(R.string.contact));

            //分栏已经显示，最上面的分割线不用显示
            userHolder.divider.setVisibility(View.GONE);
            userHolder.sectionView.setVisibility(View.GONE);
        }else{
            userHolder.sectionView.setVisibility(View.GONE);
            userHolder.divider.setVisibility(View.VISIBLE);
        }

        userHolder.avatar.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        userHolder.avatar.setCorner(0);
      //  userHolder.avatar.setImageUrl(userEntity.getAvatar());

        userHolder.realNameView.setText(userEntity.getContent());
        userHolder.realNameView.setVisibility(View.GONE);
        return view;
    }

    public View renderGroup(int position, View view, ViewGroup parent){
        GroupHolder groupHolder = null;
        GroupEntity groupEntity = (GroupEntity) getItem(position);
        if(groupEntity == null){
            logger.e("SearchAdapter#renderGroup#groupEntity is null!position:%d",position);
            return null;
        }
        if (view == null) {
            groupHolder = new GroupHolder();
            view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_contact_group, parent,false);
            groupHolder.nameView = (TextView) view.findViewById(R.id.contact_item_title);
            groupHolder.sectionView = (TextView) view.findViewById(R.id.contact_category_title);
            groupHolder.avatar = (IMGroupAvatar)view.findViewById(R.id.contact_portrait);
            groupHolder.divider = view.findViewById(R.id.contact_divider);
            view.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) view.getTag();
        }

      //groupHolder.nameView.setText(groupEntity.getGroupName());
      IMUIHelper.setTextHilighted(groupHolder.nameView,groupEntity.getMainName(),groupEntity.getSearchElement());

        groupHolder.sectionView.setVisibility(View.GONE);
        // 分割线的处理【位于控件的最上面】
        groupHolder.divider.setVisibility(View.VISIBLE);

        // 分栏显示“群或讨论组”
        int userSize = userList==null?0:userList.size();
        if (position == userSize) {
            groupHolder.sectionView.setVisibility(View.VISIBLE);
            groupHolder.sectionView.setText(ctx.getString(R.string.fixed_group_or_temp_group));
            //分栏已经显示，最上面的分割线不用显示
            groupHolder.divider.setVisibility(View.GONE);
            groupHolder.sectionView.setVisibility(View.GONE);
            
        }else{
            groupHolder.sectionView.setVisibility(View.GONE);
        }

        groupHolder.avatar.setVisibility(View.VISIBLE);
        List<String> avatarUrlList = new ArrayList<>();
        Set<Integer> userIds = groupEntity.getlistGroupMemberIds();
        int i = 0;
        for(Integer buddyId:userIds){
            UserEntity entity = imService.getContactManager().findContact(buddyId);
            if (entity == null) {
                //logger.d("已经离职。userId:%d", buddyId);
                continue;
            }
           
            if (i > DBConstant.GROUP_AVATAR_NUM-1) {
                break;
            }
            avatarUrlList.add(entity.getAvatar());
            i++;
        }
        setGroupAvatar(groupHolder.avatar,avatarUrlList);
        return view;
    }

//    public View renderDept(int position, View view, ViewGroup parent){
//        DeptHolder deptHolder = null;
//        DepartmentEntity  deptEntity= (DepartmentEntity)getItem(position);
//        if(deptEntity == null){
//            logger.e("SearchAdapter#renderDept#deptEntity is null!position:%d",position);
//            return null;
//        }
//        if (view == null) {
//            deptHolder = new DeptHolder();
//            view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_contact, parent,false);
//            deptHolder.avatar = (IMBaseImageView)view.findViewById(R.id.contact_portrait);
//            deptHolder.nameView = (TextView) view.findViewById(R.id.contact_item_title);
//            deptHolder.sectionView = (TextView) view.findViewById(R.id.contact_category_title);
//            deptHolder.divider = view.findViewById(R.id.contact_divider);
//            view.setTag(deptHolder);
//        } else {
//            deptHolder = (DeptHolder) view.getTag();
//        }
//        deptHolder.avatar.setVisibility(View.INVISIBLE);
//        //deptHolder.nameView.setText(deptEntity.getDepartName());
//        IMUIHelper.setTextHilighted(deptHolder.nameView,deptEntity.getDepartName(),deptEntity.getSearchElement());
//        deptHolder.divider.setVisibility(View.VISIBLE);
//
//        // 分栏信息的展示 可以保存结果，优化
//        int groupSize = groupList==null?0:groupList.size();
//        int userSize = userList==null?0:userList.size();
//        int realIndex = position - groupSize - userSize;
//        if (realIndex == 0) {
//            deptHolder.sectionView.setVisibility(View.VISIBLE);
//            deptHolder.sectionView.setText(ctx.getString(R.string.department));
//
//            //分栏已经显示，最上面的分割线不用显示
//            deptHolder.divider.setVisibility(View.GONE);
//        }else{
//            deptHolder.sectionView.setVisibility(View.GONE);
//        }
//        return view;
//    }


    /**
     *  与contactAdapter 有公用的地方，可以抽取到IMUIHelper
     * 设置群头像
     * @param avatar
     * @param avatarUrlList
     */
    private void setGroupAvatar(IMGroupAvatar avatar,List<String> avatarUrlList){
        try {
            avatar.setViewSize(ScreenUtil.instance(ctx).dip2px(45));
            avatar.setChildCorner(2);
            avatar.setAvatarUrlAppend(SysConstant.AVATAR_APPEND_32);
            avatar.setParentPadding(3);
            avatar.setAvatarUrls((ArrayList<String>) avatarUrlList);
        }catch (Exception e){
            logger.e(e.toString());
        }
    }



    // 将分割线放在上面，利于判断
    public static class UserHolder {
        View divider;
        TextView sectionView;
        TextView nameView;
        TextView realNameView;
        IMBaseImageView avatar;
    }

    public static class DeptHolder {
        View divider;
        TextView sectionView;
        TextView nameView;
        IMBaseImageView avatar;
    }

    public static class GroupHolder {
        View divider;
        TextView sectionView;
        TextView nameView;
        IMGroupAvatar avatar;
    }

    private enum SearchType{
        USER, 
        ILLEGAL
    }

    /**---------------------------set/get--------------------------*/
    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }
}

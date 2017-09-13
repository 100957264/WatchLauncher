package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.entity.WhiteEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.ui.widget.IMGroupAvatar;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.ScreenUtil;
import com.fise.xiaoyu.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class YuFriendsListAdspter extends BaseAdapter implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private LayoutInflater layoutInflater;
    private Context context;
    private IMService imService;
    public List<UserEntity> YuFriendsList = new ArrayList<>();

    private List<UserEntity> manageYuList;
    private List<GroupEntity> groupList;

    public YuFriendsListAdspter(Context context, List<UserEntity> YuFriendsList, List<UserEntity> manageYuList, List<GroupEntity> groupList, IMService imService) {
        this.context = context;
        this.YuFriendsList = YuFriendsList;
        this.manageYuList = manageYuList;
        this.groupList = groupList;
        this.layoutInflater = LayoutInflater.from(context);
        this.imService = imService;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }


    @Override
    public int getCount() {

        int yuFriendsSize = YuFriendsList == null ? 0 : YuFriendsList.size();
        int manageYuSize = manageYuList == null ? 0 : manageYuList.size();
        int groupSize = groupList == null ? 0 : groupList.size();

        int sum = yuFriendsSize + manageYuSize + groupSize;
        return sum;

    }

    /**
     * 获得某一位置的数据
     */
    @Override
    public Object getItem(int position) {
        int typeIndex = getItemViewType(position);
        ContactType renderType = ContactType.values()[typeIndex];

        switch (renderType) {
            case USER: {
                return YuFriendsList.get(position);
            }

            case MANAGE: {
                int realIndex = position - manageYuList.size();
                if (realIndex < 0) {
                    throw new IllegalArgumentException(
                            "ContactAdapter#getItem#user类型判断错误!");
                }
                return manageYuList.get(realIndex);
            }

            case GROUP: {
                int realIndex = position - (manageYuList.size() + YuFriendsList.size());
                if (realIndex < 0) {
                    throw new IllegalArgumentException(
                            "ContactAdapter#getItem#user类型判断错误!");
                }
                return groupList.get(realIndex);
            }

            default:
                throw new IllegalArgumentException("ContactAdapter#getItem#不存在的类型"
                        + renderType.name());
        }
    }

    /**
     * 获得唯一标识
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    public void putYuFriendsList(List<UserEntity> YuFriendsList, List<UserEntity> manageYuList, List<GroupEntity> groupList) {
        this.YuFriendsList = YuFriendsList;
        this.manageYuList = manageYuList;
        this.groupList = groupList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        if (position < this.YuFriendsList.size()) {
            return ContactType.USER.ordinal();
        } else if ((position >= this.YuFriendsList.size()) && (position < (this.YuFriendsList.size() + manageYuList.size()))) {
            return ContactType.MANAGE.ordinal();
        }

        return ContactType.GROUP.ordinal();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int typeIndex = getItemViewType(position);
        ContactType renderType = ContactType.values()[typeIndex];
        View view = null;
        switch (renderType) {

            case USER: {
                view = renderYuUser(position, convertView, parent, YuFriendsList, ContactType.USER.ordinal());
            }
            break;
            case MANAGE: {
                view = renderYuUser(position - YuFriendsList.size(), convertView, parent, manageYuList, ContactType.MANAGE.ordinal());
            }
            break;

            case GROUP: {
                view = renderGroupUser(position - (YuFriendsList.size() + manageYuList.size()), convertView, parent, groupList);
            }
            break;

        }

        return view;
    }

    public View renderYuUser(int position, View view, ViewGroup parent, List<UserEntity> peerEntityList, int renderType) {

        UserHolder zujian = null;
        if (view == null) {
            zujian = new UserHolder();
            //获得组件，实例化组件
            view = layoutInflater.inflate(R.layout.tt_yufriends_list_item, null);
            zujian.user_image = (IMBaseImageView) view.findViewById(R.id.img);
            zujian.group_image = (IMGroupAvatar) view.findViewById(R.id.group_img);
            zujian.user_name = (TextView) view.findViewById(R.id.name);
            zujian.group_name = (TextView) view.findViewById(R.id.group_name);

            zujian.item_type = (TextView) view.findViewById(R.id.item_type);
            zujian.item_type_line = (View) view.findViewById(R.id.item_type_line);

            view.setTag(zujian);
        } else {
            zujian = (UserHolder) view.getTag();
        }

        if (peerEntityList.size() <= 0) {
            return view;
        }

        String type = "";
        if (renderType == ContactType.MANAGE.ordinal()) {
            type = "管理我的雨友";
        } else {
            type = "绑定的雨友";
        }
        final PeerEntity entity = peerEntityList.get(position);
        if (position == 0) {
            zujian.item_type.setText("" + type);
            zujian.item_type.setVisibility(View.VISIBLE);
            zujian.item_type_line.setVisibility(View.VISIBLE);
        } else {
            zujian.item_type.setVisibility(View.GONE);
            zujian.item_type_line.setVisibility(View.GONE);
        }

        //绑定数据
        zujian.user_image.setImageUrl(entity.getAvatar());
        zujian.group_image.setVisibility(View.GONE);
        zujian.user_image.setVisibility(View.VISIBLE);
        zujian.group_name.setVisibility(View.GONE);
        zujian.user_name.setVisibility(View.VISIBLE);


        if (entity.getType() == DBConstant.SESSION_TYPE_SINGLE) {

            UserEntity userEntity = (UserEntity) entity;
            if (userEntity.getComment().equals("")) {
                zujian.user_name.setText(entity.getMainName());
            } else {
                zujian.user_name.setText(userEntity.getComment());
            }
        } else {
            zujian.user_name.setText(entity.getMainName());
        }

        return view;
    }

    public View renderGroupUser(int position, View view, ViewGroup parent, List<GroupEntity> peerEntityList) {

        UserHolder zujian = null;
        if (view == null) {
            zujian = new UserHolder();
            //获得组件，实例化组件
            view = layoutInflater.inflate(R.layout.tt_yufriends_list_item, null);
            zujian.user_image = (IMBaseImageView) view.findViewById(R.id.img);
            zujian.group_image = (IMGroupAvatar) view.findViewById(R.id.group_img);
            zujian.user_name = (TextView) view.findViewById(R.id.name);
            zujian.item_type = (TextView) view.findViewById(R.id.item_type);
            zujian.item_type_line = (View) view.findViewById(R.id.item_type_line);
            zujian.group_name = (TextView) view.findViewById(R.id.group_name);

            view.setTag(zujian);
        } else {
            zujian = (UserHolder) view.getTag();
        }

        final GroupEntity entity = peerEntityList.get(position);
        if (position == 0) {
            zujian.item_type.setText("雨友群");
            zujian.item_type.setVisibility(View.VISIBLE);
            zujian.item_type_line.setVisibility(View.VISIBLE);
        } else {
            zujian.item_type.setVisibility(View.GONE);
            zujian.item_type_line.setVisibility(View.GONE);
        }
        //绑定数据
        List<String> avatarUrlList = new ArrayList<>();
        Set<Integer> userIds = entity.getlistGroupMemberIds();
        int i = 0;
        for (Integer buddyId : userIds) {
            UserEntity userEntity = imService.getContactManager().findParentContact(buddyId);
            if (entity == null) {
                userEntity = imService.getContactManager().findDeviceContact(buddyId);
            }
            if (entity == null) {
                userEntity = imService.getContactManager().findFriendsContact(buddyId);
            }

            if (buddyId == imService.getLoginManager().getLoginId()) {
                userEntity = imService.getLoginManager().getLoginInfo();
            }
            if (entity == null) {
                userEntity = imService.getContactManager().findContact(buddyId);
            }


            if (entity == null) {
                continue;
            }

            if (i > DBConstant.GROUP_AVATAR_NUM - 1) {
                break;
            }
            if (userEntity != null) {
                avatarUrlList.add(userEntity.getAvatar());
            }

            i++;
        }

        zujian.group_image.setVisibility(View.VISIBLE);
        zujian.user_image.setVisibility(View.GONE);
        setGroupAvatar(zujian.group_image, avatarUrlList);
        zujian.group_name.setVisibility(View.VISIBLE);
        zujian.user_name.setVisibility(View.GONE);
        zujian.group_name.setText(entity.getMainName());

        return view;
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

        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        if (!Utils.isClientType(imService.getLoginManager().getLoginInfo())) {
            final UserEntity Entity = (UserEntity) getItem(position);
            if (Entity != null) {

                final FilletDialog myDialog = new FilletDialog(context, FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE);
                myDialog.setTitle(context.getString(R.string.delete_device_notice));//
                myDialog.dialog.show();//显示

                //确认按键回调，按下确认后在此做处理
                myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
                    @Override
                    public void ok() {

                        imService.getDeviceManager().addDevice(Entity.getRealName(),
                                IMDevice.ManageType.MANAGE_TYPE_DEL_DEVICE,
                                Entity);

                        myDialog.dialog.dismiss();
                    }
                });

            }
        }
        return true;
    }

    public static class UserHolder {
        public IMBaseImageView user_image;
        public IMGroupAvatar group_image;
        public TextView user_name;
        public TextView group_name;
        public TextView item_type;
        public View item_type_line;

    }


    private enum ContactType {
        USER, MANAGE, GROUP
        // GROUP
    }
}  


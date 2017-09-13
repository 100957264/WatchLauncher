package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.ui.widget.IMGroupAvatar;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 通讯录默认页 “全部”展示 包含字母序
 * <p/>
 * todo 这几个adapter都有公用的部分，今后如果需求变更有需要，抽离父类adapter [DeptAdapter]
 * [GroupSelectAdapter] [SearchAdapter]
 */
public class ContactAdapter extends BaseAdapter implements SectionIndexer,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private Logger logger = Logger.getLogger(ContactAdapter.class);
    public List<UserEntity> userList = new ArrayList<>();

    private Context ctx;
    private IMService imService;

    public int FuncSize = 3;

    public boolean isUnFriends = false;

    public ContactAdapter(Context context, IMService imService) {
        this.ctx = context;
        this.imService = imService;
    }

    public void putUserList(List<UserEntity> pUserList) {
        this.userList.clear();
        if (pUserList == null || pUserList.size() < 0) {
            return;
        }
        // if(pUserList == null || pUserList.size() <=0){
        // return;
        // }
        this.userList = pUserList;
        notifyDataSetChanged();
    }

    public void putUser(boolean isUnFriends) {

        this.isUnFriends = isUnFriends;
        // this.UnFriend = UnFriend;
        notifyDataSetChanged();
    }

    private List<Map<String, Object>> getData() {
        // map.put(参数名字,参数值)

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();

        map = new HashMap<String, Object>();
        map.put("title", "联系人");
        map.put("img", R.drawable.icon_weiyou_haoyouqun);
        list.add(map);

        return list;
    }

    private List<Map<String, Object>> getData1() {
        // map.put(参数名字,参数值)

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();

        map = new HashMap<String, Object>();
        map.put("title", "联系人");
        map.put("img", R.drawable.icon_weiyou_haoyouqun);
        list.add(map);

        return list;
    }


    private List<Map<String, Object>> getNewFriendsData() {
        // map.put(参数名字,参数值)

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();

        map = new HashMap<String, Object>();
        map.put("title", "新的好友");
        map.put("img", R.drawable.icon_weiyou_haoyouqun);
        list.add(map);


        map = new HashMap<String, Object>();
        map.put("title", "群聊");
        map.put("img", R.drawable.icon_weiyou_haoyouqun);
        list.add(map);


        map = new HashMap<String, Object>();
        map.put("title", "我的雨友");
        map.put("img", R.drawable.icon_weiyou_haoyouqun);
        list.add(map);


        return list;
    }

    /**
     * Returns an array of objects representing sections of the list. The
     * returned array and its contents should be non-null.
     * <p/>
     * The list view will call toString() on the objects to get the preview text
     * to display while scrolling. For example, an adapter may return an array
     * of Strings representing letters of the alphabet. Or, it may return an
     * array of objects whose toString() methods return their section titles.
     *
     * @return the array of section objects
     */
    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    /**
     * getPositionForSection
     */
    @Override
    public int getPositionForSection(int section) {

        // 用户列表的起始位置是群组结束的位置，要特别注意
        for (int i = 0; i < userList.size(); i++) {
            String pinYin = userList.get(i).getPinyinElement().pinyin;
            if (pinYin.length() == 0) {
                return -1;
            }

            int firstCharacter = pinYin.charAt(0);
            // logger.d("firstCharacter:%d", firstCharacter);
            if (firstCharacter == section) {
                return i;
            }
        }
        return -1;
    }

    /**
     * getSectionForPosition
     */
    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {


        if (position == 0) {
            IMUIHelper.openFriendActivity(ctx);
        } else if (position == 1) {
            IMUIHelper.openGroupActivity(ctx);

        } else if (position == 2) {
            IMUIHelper.openYuFriendsActivity(ctx);
        } else { //if (position != 0 && position != 1)

            Object object = getItem(position);
            if (object instanceof UserEntity) {
                UserEntity userEntity = (UserEntity) object;

                boolean isWeiFriends = false;
                if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                    isWeiFriends = true;
                }
                IMUIHelper.openUserProfileActivity(ctx, userEntity.getPeerId(),
                        isWeiFriends);

            } else if (object instanceof GroupEntity) {
                GroupEntity groupEntity = (GroupEntity) object;
                IMUIHelper.openChatActivity(ctx, groupEntity.getSessionKey());
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        Object object = getItem(position);
        if (object instanceof UserEntity) {
            UserEntity contact = (UserEntity) object;
            IMUIHelper.handleContactItemLongClick(contact, ctx);
        } else {
        }
        return true;
    }

    @Override
    public int getItemViewType(int position) {

//		if (position == 0) {
//			return ContactType.SEARCH.ordinal();
//		} else if (position == 1) {
//			return ContactType.FUNC.ordinal();
//		}

        //小雨
        if (position == 0) {
            return ContactType.NEWFRIEND.ordinal();
        } else if (position == 1) {
            return ContactType.GROUP.ordinal();
        } else if (position == 2) {
            return ContactType.YUFRIEND.ordinal();
        }

        return ContactType.USER.ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return ContactType.values().length;
    }

    @Override
    public int getCount() {
        // guanweile
        int userSize = userList == null ? 0 : userList.size();
        // int sum = groupSize + userSize + FuncSize;
        int sum = userSize + FuncSize;
        return sum;
    }

    @Override
    public Object getItem(int position) {
        int typeIndex = getItemViewType(position);
        ContactType renderType = ContactType.values()[typeIndex];
        switch (renderType) {

            case NEWFRIEND: {
                if (position == 0) {
                    return getNewFriendsData().get(position);
                }
            }

            case GROUP: {
                if (position == 1) {
                    return getNewFriendsData().get(position);
                }
            }

            case YUFRIEND: {
                if (position == 2) {
                    return getNewFriendsData().get(position);
                }
            }
/*
            case SEARCH: {
			if (position == 0) {
				return getData().get(position);
			}
		}
		case FUNC: {
			if (position == 1) {
				return getData1().get(position);
			}
		}
*/
            case USER: {
                int realIndex = position - FuncSize;
                if (realIndex < 0) {
                    throw new IllegalArgumentException(
                            "ContactAdapter#getItem#user类型判断错误!");
                }
                return userList.get(realIndex);
            }

            default:
                throw new IllegalArgumentException("ContactAdapter#getItem#不存在的类型"
                        + renderType.name());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int typeIndex = getItemViewType(position);
        ContactType renderType = ContactType.values()[typeIndex];
        View view = null;
        switch (renderType) {

            case SEARCH: {
                view = renderNewFunc(position, convertView, parent);
            }
            break;

            case FUNC: {
                view = renderFunc(position, convertView, parent);
            }
            break;

            case NEWFRIEND: {
                view = renderYuUser(position, convertView, parent);
            }
            break;


            case GROUP: {
                view = renderYuUser(position, convertView, parent);
            }
            break;

            case YUFRIEND: {
                view = renderYuUser(position, convertView, parent);
            }
            break;
            case USER: {
                view = renderUser(position, convertView, parent);
            }
            break;
        }

        return view;
    }

    public View renderNewFunc(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_friends,
                    parent, false);

            EditText search_phone = (EditText) view
                    .findViewById(R.id.search_phone);
            search_phone.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    IMUIHelper.openSearchFriendActivity(ctx,
                            DBConstant.SEACHFRIENDS);

                }
            });

        }

        return view;
    }

    public View renderFunc(int position, View view, ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_func,
                    parent, false);

        }

        final TextView unReqView = (TextView) view
                .findViewById(R.id.contact_unmessage_title);

        RelativeLayout new_friends_view = (RelativeLayout) view
                .findViewById(R.id.new_friends_view);
        new_friends_view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                isUnFriends = false;
                IMUIHelper.openFriendActivity(ctx);
                unReqView.setVisibility(View.GONE);

            }
        });

        if (isUnFriends) {
            int unReqNum = imService.getUnReadMsgManager()
                    .getTotalReqMessageCount();
            unReqView.setVisibility(View.VISIBLE);
            unReqView.setText("" + unReqNum);
        } else {
            unReqView.setVisibility(View.GONE);
        }


        RelativeLayout friends_group_view = (RelativeLayout) view
                .findViewById(R.id.friends_group_view);
        friends_group_view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                IMUIHelper.openGroupActivity(ctx);
            }
        });

        return view;
    }


    public View renderYuUser(int position, View view, ViewGroup parent) {
        UserHolder userHolder = null;
        if (view == null) {
            userHolder = new UserHolder();
            view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_contact,
                    parent, false);
            userHolder.nameView = (TextView) view
                    .findViewById(R.id.contact_item_title);
            userHolder.realNameView = (TextView) view
                    .findViewById(R.id.contact_realname_title);
            userHolder.unReqView = (TextView) view
                    .findViewById(R.id.contact_unmessage_title);

            userHolder.sectionView = (TextView) view
                    .findViewById(R.id.contact_category_title);
            userHolder.avatar = (IMBaseImageView) view
                    .findViewById(R.id.contact_portrait);
            userHolder.divider = view.findViewById(R.id.contact_divider);
            userHolder.contact_buttom = view.findViewById(R.id.contact_buttom);
            view.setTag(userHolder);
        } else {
            userHolder = (UserHolder) view.getTag();
        }

        userHolder.sectionView.setVisibility(View.GONE);

        // 分栏已经显示，最上面的分割线不用显示
        userHolder.divider.setVisibility(View.VISIBLE);
        userHolder.contact_buttom.setVisibility(View.VISIBLE); // guan


        userHolder.avatar.setBackgroundResource((Integer) getNewFriendsData().get(position).get("img"));
        userHolder.nameView.setText((String) getNewFriendsData().get(position).get("title"));
        userHolder.realNameView.setVisibility(View.GONE);

        if (position == 0) {

            if (isUnFriends) {
                int unReqNum = imService.getUnReadMsgManager()
                        .getTotalReqMessageCount();
                userHolder.unReqView.setVisibility(View.VISIBLE);
                userHolder.unReqView.setText("" + unReqNum);
            } else {
                userHolder.unReqView.setVisibility(View.GONE);
            }
        }


        if (position != 0) {
            userHolder.divider.setVisibility(View.GONE);
        }

        return view;
    }

    public View renderUser(int position, View view, ViewGroup parent) {
        UserHolder userHolder = null;
        UserEntity userEntity = (UserEntity) getItem(position);
        if (userEntity == null) {
            logger.e(
                    "ContactAdapter#renderUser#userEntity is null!position:%d",
                    position);
            // todo 这个会报错误的，怎么处理
            return null;
        }
        if (view == null) {
            userHolder = new UserHolder();
            view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_contact,
                    parent, false);
            userHolder.nameView = (TextView) view
                    .findViewById(R.id.contact_item_title);
            userHolder.realNameView = (TextView) view
                    .findViewById(R.id.contact_realname_title);
            userHolder.unReqView = (TextView) view
                    .findViewById(R.id.contact_unmessage_title);

            userHolder.sectionView = (TextView) view
                    .findViewById(R.id.contact_category_title);
            userHolder.avatar = (IMBaseImageView) view
                    .findViewById(R.id.contact_portrait);
            userHolder.divider = view.findViewById(R.id.contact_divider);
            userHolder.contact_buttom = view.findViewById(R.id.contact_buttom);
            view.setTag(userHolder);
        } else {
            userHolder = (UserHolder) view.getTag();
        }

        /*** reset-- 控件的默认值 */
        if (userEntity.getComment().equals("")) {
            userHolder.nameView.setText(userEntity.getMainName());
        } else {
            userHolder.nameView.setText(userEntity.getComment());
        }
        userHolder.avatar
                .setImageResource(R.drawable.tt_default_user_portrait_corner);
        userHolder.divider.setVisibility(View.VISIBLE);
        userHolder.sectionView.setVisibility(View.VISIBLE);

        userHolder.unReqView.setVisibility(View.GONE);

        // 字母序第一个要展示
        // todo pinyin控件不能处理多音字的情况，或者UserEntity类型的统统用pinyin字段进行判断
        String sectionName = userEntity.getSectionName();
        // 正式群在用户列表的上方展示
        if (position == (FuncSize)) {
            userHolder.sectionView.setText(sectionName);

            // 分栏已经显示，最上面的分割线不用显示
            userHolder.divider.setVisibility(View.VISIBLE);
            userHolder.contact_buttom.setVisibility(View.VISIBLE); // guan
        } else {

            // 获取上一个实体的preSectionName,这个时候position > groupSize
            UserEntity preUser = (UserEntity) getItem(position - 1);
            String preSectionName = preUser.getSectionName();
            if (TextUtils.isEmpty(preSectionName)
                    || !preSectionName.equals(sectionName)) {
                // userHolder.sectionView.setVisibility(View.GONE); // guanweile
                userHolder.sectionView.setText(sectionName);
                // 不显示分割线
                // userHolder.divider.setVisibility(View.GONE);
                userHolder.divider.setVisibility(View.VISIBLE);
                userHolder.contact_buttom.setVisibility(View.VISIBLE);
            } else {
                userHolder.sectionView.setVisibility(View.GONE);
                userHolder.divider.setVisibility(View.GONE);
                userHolder.contact_buttom.setVisibility(View.VISIBLE);
            }
        }

        userHolder.avatar
                .setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        userHolder.avatar
                .setImageResource(R.drawable.tt_default_user_portrait_corner);

        userHolder.avatar.setImageUrl(userEntity.getAvatar());

        userHolder.realNameView.setText(userEntity.getRealName());
        userHolder.realNameView.setVisibility(View.GONE);

        return view;
    }

    public View renderGroup(int position, View view, ViewGroup parent) {
        GroupHolder groupHolder = null;
        GroupEntity groupEntity = (GroupEntity) getItem(position);
        if (groupEntity == null) {
            logger.e(
                    "ContactAdapter#renderGroup#groupEntity is null!position:%d",
                    position);
            return null;
        }
        if (view == null) {
            groupHolder = new GroupHolder();
            view = LayoutInflater.from(ctx).inflate(
                    R.layout.tt_item_contact_group, parent, false);
            groupHolder.nameView = (TextView) view
                    .findViewById(R.id.contact_item_title);
            groupHolder.sectionView = (TextView) view
                    .findViewById(R.id.contact_category_title);
            groupHolder.avatar = (IMGroupAvatar) view
                    .findViewById(R.id.contact_portrait);
            groupHolder.divider = view.findViewById(R.id.contact_divider);
            view.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) view.getTag();
        }

        groupHolder.nameView.setText(groupEntity.getMainName());
        // 分割线的处理【位于控件的最上面】
        groupHolder.divider.setVisibility(View.VISIBLE);
        if (position == FuncSize) { // guan
            groupHolder.divider.setVisibility(View.GONE);
        }

        groupHolder.avatar.setVisibility(View.VISIBLE);
        List<String> avatarUrlList = new ArrayList<>();
        Set<Integer> userIds = groupEntity.getlistGroupMemberIds();
        int i = 0;
        for (Integer buddyId : userIds) {
            UserEntity entity = imService.getContactManager().findContact(
                    buddyId);
            if (entity == null) {
                // logger.d("已经离职。userId:%d", buddyId);
                continue;
            }

            avatarUrlList.add(entity.getAvatar());
            if (i > DBConstant.GROUP_AVATAR_NUM - 1) {
                break;
            }
            i++;
        }
        setGroupAvatar(groupHolder.avatar, avatarUrlList);
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
            avatar.setViewSize(ScreenUtil.instance(ctx).dip2px(45));
            avatar.setChildCorner(2);
            avatar.setAvatarUrlAppend(SysConstant.AVATAR_APPEND_32);
            avatar.setParentPadding(3);
            avatar.setAvatarUrls((ArrayList<String>) avatarUrlList);
        } catch (Exception e) {
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
        View contact_buttom;
        TextView unReqView;

    }

    public static class GroupHolder {
        View divider;
        TextView sectionView;
        TextView nameView;
        IMGroupAvatar avatar;
    }

    private enum ContactType {
        SEARCH, FUNC, NEWFRIEND, GROUP, YUFRIEND, USER// ,
        // GROUP
    }
}

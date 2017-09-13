package com.fise.xiaoyu.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.CommonUserInfo;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.ConfigurationSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMGroupManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.ui.widget.IMGroupAvatar;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.ScreenUtil;
import com.fise.xiaoyu.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Description 联系人列表适配器
 */
@SuppressLint("ResourceAsColor")
public class MessageTansponddApter extends BaseAdapter  implements AdapterView.OnItemClickListener ,SectionIndexer {
	private LayoutInflater mInflater = null;
	private List<CommonUserInfo> recentSessionList = new ArrayList<>();
    private List<CommonUserInfo> backupList = new ArrayList<>();
	private Logger logger = Logger.getLogger(MessageTansponddApter.class);

	private static final int CONTACT_TYPE_INVALID = 0;
	private static final int CONTACT_TYPE_USER = 1;
	private static final int CONTACT_TYPE_GROUP = 2;
	private Context ctx;
	private IMService imService;
	private String searchKey;
    private  Boolean isSearchMode = false;
	private static ConfigurationSp configurationSp = null;
	private List<String> checkListSet= new ArrayList<>();

	public MessageTansponddApter(Context context) {
		this.ctx = context;
		this.mInflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {
		return recentSessionList.size();
	}

	@Override
	public CommonUserInfo getItem(int position) {
		logger.d("recent#getItem position:%d", position);
		if (position > recentSessionList.size()
				|| position < 0) {
			return null;
		}

		if (position != 0 && position != 1) {
			return recentSessionList.get(position );
		}
		return null;
	}



	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 用户HOLDER
	 */
	private final class ContactViewHolder extends UserHolder {
		public IMBaseImageView avatar;
	}

	/**
	 * 基本HOLDER
	 */
	public static class UserHolder {
		public TextView uname;
		public ImageView device_group_portrait;
		public CheckBox checkBox;
		// public ImageView wei_title;

	}

	/**
	 * 群组HOLDER
	 */
	private final static class GroupViewHolder extends UserHolder {
		public IMGroupAvatar avatarLayout;
	}

	public void recover(){
        isSearchMode = false;
        recentSessionList = backupList;
		notifyDataSetChanged();
	}

	public void onSearch(String key){
		searchKey = key;
        isSearchMode = true;
		List<CommonUserInfo> searchList = new ArrayList<>();
		for(CommonUserInfo entity:backupList){
			if(IMUIHelper.handleContactSearch(searchKey,entity)){
				searchList.add(entity);
			}
		}

		if(searchList.size() >0){
            recentSessionList = searchList;
            notifyDataSetChanged();
        }

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		UserHolder viewHolder = (UserHolder) view.getTag();
		CommonUserInfo recentInfo = (CommonUserInfo) getItem(position);
		UserHolder holder = (UserHolder) view.getTag();

		viewHolder.checkBox.toggle();
		boolean checked = holder.checkBox.isChecked();
		String sessionKey = recentInfo.getSessionKey();
		if (checked) {
			checkListSet.add(sessionKey);
		} else {
			checkListSet.remove(sessionKey);
		}
	}


	private View renderUser(int position, View convertView, ViewGroup parent) {
		CommonUserInfo recentInfo = recentSessionList.get(position);
		ContactViewHolder holder;
		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.tt_item_message_transpond_user, parent,
					false);
			holder = new ContactViewHolder();
			holder.avatar = (IMBaseImageView) convertView
					.findViewById(R.id.contact_portrait);
			holder.uname = (TextView) convertView.findViewById(R.id.user_name);

			holder.avatar
					.setImageResource(R.drawable.tt_default_user_portrait_corner);
			holder.checkBox = (CheckBox)convertView.findViewById(R.id.checkBox);
			convertView.setTag(holder);
		} else {
			holder = (ContactViewHolder) convertView.getTag();
		}
		if(checkListSet.contains(recentInfo.getSessionKey())){
			holder.checkBox.setChecked(true);
		}else{
			holder.checkBox.setChecked(false);
		}
		UserEntity info = IMContactManager.instance().findFriendsContact(
				recentInfo.getUserInfoID());

		if (info == null) {
			info = IMContactManager.instance().findContact(
					recentInfo.getUserInfoID());
		}

		if (info == null) {
			info = IMContactManager.instance().findDeviceContact(
					recentInfo.getUserInfoID());
		}

		//如果是设备
		if (info != null&&(Utils.isClientType(info))) {
			recentInfo.setUserName(info.getMainName());
			//区分设备与人的区别
			holder.avatar.setCorner(90);

		}else{
			holder.avatar.setCorner(8);
		}

        if(isSearchMode){
            // 高亮显示
            IMUIHelper.setTextHilighted(holder.uname, recentInfo.getUserName(),
                    recentInfo.getSearchElement());
        }else{
            String userName = recentInfo.getUserName();
            holder.uname.setText(userName);
        }

        String avatarUrl = "";
		if (null != recentInfo.getAvatarUrl() ) {
		 	avatarUrl = recentInfo.getAvatarUrl();

		}
		holder.avatar.setImageUrl(avatarUrl);
		// 设置其它信息
		return convertView;
	}

	private View renderGroup(int position, View convertView, ViewGroup parent) {
		CommonUserInfo recentInfo = recentSessionList.get(position);
		GroupViewHolder holder;
		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.tt_item_message_transpond_group,
					parent, false);
			holder = new GroupViewHolder();
			holder.avatarLayout = (IMGroupAvatar) convertView
					.findViewById(R.id.contact_portrait);
			holder.uname = (TextView) convertView.findViewById(R.id.user_name);
			holder.checkBox = (CheckBox)convertView.findViewById(R.id.checkBox);
			holder.device_group_portrait = (ImageView) convertView.findViewById(R.id.device_group_portrait);
			convertView.setTag(holder);

		} else {
			holder = (GroupViewHolder) convertView.getTag();
		}

		if(checkListSet.contains(recentInfo.getSessionKey())){
			holder.checkBox.setChecked(true);
		}else{
			holder.checkBox.setChecked(false);
		}
        List<String> avatarUrlList = new ArrayList<>();
        Set<Integer> userIds = recentSessionList.get(position).getlistGroupMemberIds();
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
        if(isSearchMode){
            // 高亮显示
            IMUIHelper.setTextHilighted(holder.uname, recentInfo.getUserName(),
                    recentInfo.getSearchElement());
        }else{
            String userName = recentInfo.getUserName();
            holder.uname.setText(userName);
        }
//		avatarUrlList.add(recentInfo.getAvatarUrl());
		setGroupAvatar(holder, avatarUrlList, recentInfo);
		return convertView;
	}

	// yingmu base-adapter-helper 了解一下
	@Override
	public View getView( final int position,   View convertView, ViewGroup parent) {
		// logger.d("recent#getview position:%d", position);
		try {
			final int type = getItemViewType(position);

			switch (type) {
			case CONTACT_TYPE_USER:
				convertView = renderUser(position ,
						convertView, parent);
				break;
			case CONTACT_TYPE_GROUP:
				convertView = renderGroup(position ,
						convertView, parent);
				break;
			}


            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserHolder viewHolder = (UserHolder) v.getTag();
                    CommonUserInfo recentInfo = recentSessionList.get(position);
                    UserHolder holder = (UserHolder) v.getTag();

                    viewHolder.checkBox.toggle();
                    boolean checked = holder.checkBox.isChecked();
                    String sessionKey = recentInfo.getSessionKey();
                    if (checked) {
                        checkListSet.add(sessionKey);
                    } else {
                        checkListSet.remove(sessionKey);
                    }
                }
            });
			return convertView;
		} catch (Exception e) {
			logger.e(e.toString());
			return null;
		}
	}

	public void setService(IMService imService) {
		this.imService = imService;
		configurationSp = ConfigurationSp.instance(ctx, imService
				.getLoginManager().getLoginId());
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public int getItemViewType(int position) {

		try {

			CommonUserInfo recentInfo = recentSessionList.get(position);

			if (recentInfo.getUserType() == DBConstant.SESSION_TYPE_SINGLE) {
				return CONTACT_TYPE_USER;
			} else if (recentInfo.getUserType() == DBConstant.SESSION_TYPE_GROUP) {
				return CONTACT_TYPE_GROUP;
			} else {
				return CONTACT_TYPE_INVALID;
			}

		} catch (Exception e) {
			logger.e(e.toString());
			return CONTACT_TYPE_INVALID;
		}
	}

	public void setData(List<CommonUserInfo> recentSessionList) {
		logger.d("recent#set New recent session list");
		logger.d("recent#notifyDataSetChanged");
		this.recentSessionList = recentSessionList;
        this.backupList = recentSessionList;
		notifyDataSetChanged();
	}

	/**
	 * 设置群头像
	 * 
	 * @param holder
	 * @param avatarUrlList
	 */
	private void setGroupAvatar(GroupViewHolder holder,
			List<String> avatarUrlList  , CommonUserInfo recentInfo) {

		try {
			if (null == avatarUrlList) {
				return;
			}
            GroupEntity entity = IMGroupManager.instance().findFamilyGroup(recentInfo.getUserInfoID());
			holder.avatarLayout
					.setAvatarUrlAppend(SysConstant.AVATAR_APPEND_32);
			//holder.avatarLayout.setChildCorner(2);
			holder.avatarLayout.setViewSize(ScreenUtil.instance(this.ctx)
					.dip2px(48));

//            if(entity!=null){
//                holder.avatarLayout.setBackgroundResource(R.drawable.group_circular_bk);
//				holder.device_group_portrait.setVisibility(View.GONE);
//
//			}

            if (null != avatarUrlList) {
					holder.avatarLayout.setAvatarUrls(new ArrayList<String>(
							avatarUrlList));
            }

		} catch (Exception e) {
			logger.e(e.toString());
		}

	}

	public List<String> getCheckListSet() {
		return checkListSet;
	}

    @Override
    public Object[] getSections() {
        return new Object[0];
    }


    // 在搜索模式下，直接返回
    @Override
    public int getPositionForSection(int sectionIndex) {
        logger.d("pinyin#getPositionForSection secton:%d", sectionIndex);
        int index = 0;
        for(CommonUserInfo entity:recentSessionList){
            int firstCharacter = entity.getSectionName().charAt(0);
            // logger.d("firstCharacter:%d", firstCharacter);
            if (firstCharacter == sectionIndex) {
                logger.d("pinyin#find sectionName");
                return index;
            }
            index++;
        }
        logger.e("pinyin#can't find such section:%d", sectionIndex);
        return -1;
    }


    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }
}

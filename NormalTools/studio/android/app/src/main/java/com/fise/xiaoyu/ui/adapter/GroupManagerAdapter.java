package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.GroupNickEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMGroupManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @YM 改造
 */
public class GroupManagerAdapter extends BaseAdapter {
	private Logger logger = Logger.getLogger(GroupManagerAdapter.class);
	private Context context;

	// 用于控制是否是删除状态，也就是那个减号是否出现
	private boolean removeState = false;
	private boolean showMinusTag = false;
	private boolean showPlusTag = false;

	private List<UserEntity> memberList = new ArrayList<>();
	private IMService imService;
	private int groupCreatorId = -1;
	private PeerEntity peerEntity;
	private GroupNickEntity entity;
	private boolean member = true;

	public GroupManagerAdapter(Context c, IMService imService,
			PeerEntity peerEntity, GroupNickEntity entity,boolean member) {
		memberList.clear();
		this.context = c;
		this.imService = imService;
		this.peerEntity = peerEntity;
		this.entity = entity;
		this.member = member;
		setData();
	}

	// todo 在选择添加人页面，currentGroupEntity 的值没有设定
	public void setData() {
		int sessionType = peerEntity.getType();
		switch (sessionType) {
		case DBConstant.SESSION_TYPE_GROUP: {
			GroupEntity groupEntity = (GroupEntity) peerEntity;
			setGroupData(groupEntity);
		}
			break;
		case DBConstant.SESSION_TYPE_SINGLE: {
			setSingleData((UserEntity) peerEntity);
		}
			break;
		}
		notifyDataSetChanged();
	}

	// 更新群的昵称
	public void updateNiick(GroupNickEntity entity) {
		this.entity = entity;
		notifyDataSetChanged();
	}

	private void setGroupData(GroupEntity entity) {
		int loginId = imService.getLoginManager().getLoginId();
		int ownerId = entity.getCreatorId();
		IMContactManager manager = imService.getContactManager();

		int groupNum = 0;

		for (Integer memId : entity.getlistGroupMemberIds()) {

			UserEntity user = manager.findContact(memId);

			boolean isAddUser =true;
			if(member == false){
				groupNum = groupNum +1;
				if(groupNum>DBConstant.GROUP_USER_NUM){
					isAddUser = false;
				}
			}

			if(isAddUser){
				if (user != null) {
					if (ownerId == user.getPeerId()) {
						// 群主放在第一个
						groupCreatorId = ownerId;
						memberList.add(0, user);
					} else {
						memberList.add(user);
					}
				} else {

					UserEntity user2 = manager.findReq(memId);
					if (user2 != null) {
						memberList.add(user2);
					}else {
						user2 = manager.findContact(memId);
						if (user2 != null) {
							memberList.add(user2);
						}else {
							user2 = manager.findDeviceContact(memId);
							if (user2 != null) {
								memberList.add(user2);
							}
						}
					}
				}
			  }
		}

		// 按钮状态的判断
		switch (entity.getGroupType()) {
		case DBConstant.GROUP_TYPE_TEMP: {
			if (loginId == entity.getCreatorId()) {
				showMinusTag = true;
				showPlusTag = true;
			} else {
				// 展示 +
				showPlusTag = true;
			}
		}
			break;


		case DBConstant.GROUP_TYPE_NORMAL: {
			if (loginId == entity.getCreatorId()) {
				// 展示加减
				showMinusTag = true;
				showPlusTag = true;
			} else {
				// 什么也不展示
			}
		}
			break;
		}
	}

	private void setSingleData(UserEntity userEntity) {
		if (userEntity != null) {
			memberList.add(userEntity);
			showPlusTag = true;
		}
	}

	public int getCount() {
		if (null != memberList) {
			int memberListSize = memberList.size();
			if (showPlusTag) {
				memberListSize = memberListSize + 1;
			}
			// 现在的情况是有减 一定有加
			if (showMinusTag) {
				memberListSize = memberListSize + 1;
			}
			return memberListSize;
		}
		return 0;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public void removeById(int contactId) {
		for (UserEntity contact : memberList) {
			if (contact.getPeerId() == contactId) {
				memberList.remove(contact);
				break;
			}
		}
		notifyDataSetChanged();
	}

	public void add(UserEntity contact) {
		removeState = false;
		memberList.add(contact);
		notifyDataSetChanged();
	}

	public void add(List<UserEntity> list) {
		removeState = false;
		// 群成员的展示没有去重，在收到IMGroupChangeMemberNotify 可能会造成重复数据
		for (UserEntity userEntity : list) {
			if (!memberList.contains(userEntity)) {
				memberList.add(userEntity);
			}
		}
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		logger.d("debug#getView position:%d, member size:%d", position,
				memberList.size());

		GroupHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.tt_group_manage_grid_item,
					null);

			AbsListView.LayoutParams param = new AbsListView.LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			convertView.setLayoutParams(param);

			holder = new GroupHolder();
			holder.imageView = (IMBaseImageView) convertView
					.findViewById(R.id.grid_item_image);
			holder.userTitle = (TextView) convertView
					.findViewById(R.id.group_manager_user_title);
//			holder.role = (ImageView) convertView
//					.findViewById(R.id.grid_item_image_role);
			holder.deleteImg = convertView.findViewById(R.id.deleteLayout);
			holder.imageView
					.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
			convertView.setTag(holder);
		} else {
			holder = (GroupHolder) convertView.getTag();
		}

//		holder.role.setVisibility(View.GONE);

		if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
			GroupEntity groupEntity = (GroupEntity) peerEntity;

			if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
				holder.imageView.setCorner(90);
			}else{
				holder.imageView.setCorner(8);
			}
		}
		if (position >= 0 && memberList.size() > position) {
			logger.d("groupmgr#in mebers area");
			final UserEntity userEntity = memberList.get(position);

			boolean isGroupNick = false;
 			if (entity != null
					&& (entity.getStatus() == DBConstant.SHOW_GROUP_NICK_OPEN)) {
				isGroupNick = true;
			}

			String ShowName;
			if (isGroupNick && (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP)) {

				
				if(userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YES
				  ||userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU){

					GroupNickEntity entity = IMGroupManager.instance()
							.findGroupNick(peerEntity.getPeerId(),
									userEntity.getPeerId());

					if (entity != null) {
						ShowName = entity.getNick();
					}else{
						if (userEntity.getComment().equals("")) {
							ShowName = userEntity.getMainName();
						} else {
							ShowName = userEntity.getComment();
						}
					}

					
				}else{ 
					GroupNickEntity entity = IMGroupManager.instance()
							.findGroupNick(peerEntity.getPeerId(),
									userEntity.getPeerId());
					if (entity == null) {
						if (userEntity.getComment().equals("")) {
							ShowName = userEntity.getMainName();
						} else {
							ShowName = userEntity.getComment();
						}
					} else {
						ShowName = entity.getNick();
					} 
				}
	
			} else {
				if (userEntity.getComment().equals("")) {
					ShowName = userEntity.getMainName();
				} else {
					ShowName = userEntity.getComment();
				}
			}

			setHolder(holder, position, userEntity.getAvatar(), 0, ShowName,
					userEntity);

			if (holder.imageView != null) {
				holder.imageView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean isWeiFriends = false;
						if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
							isWeiFriends = true;
						}

						if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_NO
								||userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_VERIFY) {
							IMUIHelper.opeGroupFriendsActivity(context,
									userEntity.getPeerId());
						} else {


//							if (Utils.isClientType(userEntity)) {
//
//								IMUIHelper.openDeviceInfoActivity(context,
//										userEntity.getPeerId());
//							}else{
//								IMUIHelper.openUserProfileActivity(context,
//										userEntity.getPeerId(), isWeiFriends);
//							}

							IMUIHelper.openUserProfileActivity(context,
									userEntity.getPeerId(), isWeiFriends);
						}

					}
				});
			}
//			if (groupCreatorId > 0 && groupCreatorId == userEntity.getPeerId()) {
//				holder.role.setVisibility(View.VISIBLE);
//			}

			if (removeState && userEntity.getPeerId() != groupCreatorId) {

				if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
					GroupEntity groupEntity = (GroupEntity) peerEntity;

					if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
						holder.deleteImg.setVisibility(View.INVISIBLE);
					}else{
						holder.deleteImg.setVisibility(View.VISIBLE);
					}
				}else{
					holder.deleteImg.setVisibility(View.VISIBLE);
				}

			} else {
				holder.deleteImg.setVisibility(View.INVISIBLE);
			}

		} else if (position == memberList.size() && showPlusTag) {
			setHolder(holder, position, null, R.drawable.button_weix_chat_add_nornal, "", null); //guanweile  add_chat_details
			holder.imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					int sessionType = peerEntity.getType();
					if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
						GroupEntity groupEntity = (GroupEntity) peerEntity;
						if (groupEntity.getSave() != DBConstant.GROUP_MEMBER_STATUS_EXIT) {

							IMUIHelper.openGroupMemberSelectActivity(context,
									peerEntity.getSessionKey(),DBConstant.SESSION_TYPE_GROUP);
						}
					} else {
						IMUIHelper.openGroupMemberSelectActivity(context,
								peerEntity.getSessionKey(),DBConstant.SESSION_TYPE_SINGLE);
					}

				}
			});
			holder.deleteImg.setVisibility(View.INVISIBLE);

		} else if (position == memberList.size() + 1 && showMinusTag) {
			logger.d("groupmgr#onAddMsg - button");
			setHolder(holder, position, null,
					R.drawable.button_weix_chat_delete_nornal, "", null);

			holder.imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					logger.d("groupmgr#click delete MemberButton");
					toggleDeleteIcon();
				}
			});
			holder.deleteImg.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}

	private void setHolder(final GroupHolder holder, int position,
			String avatarUrl, int avatarResourceId, String name,
			UserEntity contactEntity) {

		logger.d("debug#setHolder position:%d", position);

		if (null != holder) {

			// holder.imageView.setAdjustViewBounds(false);
			// holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			if (avatarUrl != null) {
				// 头像设置
				holder.imageView
						.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);

				holder.imageView
						.setImageResource(R.drawable.tt_default_user_portrait_corner);
				holder.imageView.setImageUrl(avatarUrl);

			} else {
				logger.d("groupmgr#setimageresid %d", avatarResourceId);
				holder.imageView.setImageId(0);
				holder.imageView.setDefaultImageRes(avatarResourceId);
				holder.imageView.setBackgroundResource(avatarResourceId);
				holder.imageView.setImageId(avatarResourceId);
				holder.imageView.setImageUrl(avatarUrl);
			}

			holder.contactEntity = contactEntity;
			if (contactEntity != null) {
				logger.d("debug#setHolderContact name:%s",
						contactEntity.getMainName());

				holder.deleteImg.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (holder.contactEntity == null) {
							return;
						}
						int userId = holder.contactEntity.getPeerId();
						removeById(userId);
						Set<Integer> removeMemberlist = new HashSet<>(1);
						removeMemberlist.add(userId);
						imService.getGroupManager().reqRemoveGroupMember(
								peerEntity.getPeerId(), removeMemberlist);
					}
				});
			}

			holder.userTitle.setText(name);
			holder.imageView.setVisibility(View.VISIBLE);
			holder.userTitle.setVisibility(View.VISIBLE);
		}
	}

	final class GroupHolder {
		IMBaseImageView imageView;
		TextView userTitle;
		View deleteImg;
		UserEntity contactEntity;
//		ImageView role;
	}

	public void toggleDeleteIcon() {
		removeState = !removeState;
		notifyDataSetChanged();
	}

}

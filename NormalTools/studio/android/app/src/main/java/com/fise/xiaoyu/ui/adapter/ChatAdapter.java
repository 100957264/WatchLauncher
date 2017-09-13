package com.fise.xiaoyu.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.ConfigurationSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.entity.RecentInfo;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMGroupManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.manager.IMReconnectManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.ui.fragment.ChatFragment;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.ui.widget.IMGroupAvatar;
import com.fise.xiaoyu.utils.DateUtil;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.NetworkUtil;
import com.fise.xiaoyu.utils.ScreenUtil;
import com.fise.xiaoyu.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 联系人列表适配器
 */
@SuppressLint("ResourceAsColor")
public class ChatAdapter extends BaseAdapter {
	private LayoutInflater mInflater = null;
	private List<RecentInfo> recentSessionList = new ArrayList<>();
	private Logger logger = Logger.getLogger(ChatAdapter.class);

	private static final int CONTACT_TYPE_INVALID = 0;
	private static final int CONTACT_TYPE_USER = 1;
	private static final int CONTACT_TYPE_GROUP = 2;
	private static final int CONTACT_TYPE_SEARCH = 3; //去掉
	private static final int CONTACT_TYPE_NO_NETWORK = 4;

	private static final int CONTACT_TYPE_NUM = 1;
	private boolean isProgress = false;
	private boolean noNetwork = false;

	private int touch = 0;
	private boolean isOnline;

	private Context ctx;
	private ChatFragment fragment;
	private IMService imService;
	private static ConfigurationSp configurationSp = null;
	private AnimationDrawable frameAnimation;


	public ChatAdapter(Context context, ChatFragment fragment) {
		this.ctx = context;
		this.fragment = fragment;
		// this.imService = imService; ,IMService imService
		this.mInflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {
		return recentSessionList.size() + CONTACT_TYPE_NUM;
	}

	@Override
	public RecentInfo getItem(int position) {
		logger.d("recent#getItem position:%d", position);
		if (position > recentSessionList.size() + CONTACT_TYPE_NUM
				|| position < 0) {
			return null;
		}

		if (position != 0) { //&& position != 1 // postion 0 为网络异常的Item选项
			return recentSessionList.get(position - CONTACT_TYPE_NUM);
		}
		return null;
	}

	/** 更新单个RecentInfo 屏蔽群组信息 */
	public void updateRecentInfoByShield(GroupEntity entity) {
		String sessionKey = entity.getSessionKey();
		for (RecentInfo recentInfo : recentSessionList) {
			if (recentInfo.getSessionKey().equals(sessionKey)) {
//				int status = entity.getStatus();
//				boolean isFor = status == DBConstant.GROUP_STATUS_SHIELD;
//				recentInfo.setForbidden(isFor);
//				a
				//消息免打扰
				boolean Notification = ConfigurationSp.instance(ctx, IMLoginManager.instance().getLoginId())
						.getCfg(sessionKey,
								ConfigurationSp.CfgDimension.NOTIFICATION);
				recentInfo.setForbidden(Notification);
				notifyDataSetChanged();
				break;
			}
		}
	}

	/** 置顶状态的更新 not use now */
	public void updateRecentInfoByTop(String sessionKey, boolean isTop) {
		for (RecentInfo recentInfo : recentSessionList) {
			if (recentInfo.getSessionKey().equals(sessionKey)) {
				recentInfo.setTop(isTop);
				notifyDataSetChanged();
				break;
			}
		}
	}

	public int getUnreadPositionOnView(int currentPostion) {
		int nextIndex = currentPostion + 1;
		int sum = getCount() - CONTACT_TYPE_NUM;
		if (nextIndex > sum) {
			currentPostion = 0;
		}
		/** 从当前点到末尾 */
		for (int index = nextIndex; index < sum; index++) {
			int unCnt = recentSessionList.get(index).getUnReadCnt();
			if (unCnt > 0) {
				return index;
			}
		}
		/** 从末尾到当前点 */
		for (int index = 0; index < currentPostion; index++) {
			int unCnt = recentSessionList.get(index).getUnReadCnt();
			if (unCnt > 0) {
				return index;
			}
		}
		// 最后返回到最上面
		return 0;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 用户HOLDER
	 */
	private final class ContactViewHolder extends ContactHolderBase {
		public IMBaseImageView avatar;
	}

	/**
	 * 基本HOLDER
	 */
	public static class ContactHolderBase {
		public TextView uname;
		public TextView lastContent;
		public TextView lastTime;
		public TextView msgCount;
		public ImageView noDisturb;
		public ImageView device_group_portrait;
		public ImageView forbiddenIcon;
		// public ImageView wei_title;

	}

	/**
	 * 群组HOLDER
	 */
	private final static class GroupViewHolder extends ContactHolderBase {
		public IMGroupAvatar avatarLayout;
	}

	private View renderUser(int position, View convertView, ViewGroup parent) {
		RecentInfo recentInfo = recentSessionList.get(position);
		ContactViewHolder holder;
		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.tt_item_chat, parent,
					false);
			holder = new ContactViewHolder();
			holder.avatar = (IMBaseImageView) convertView
					.findViewById(R.id.contact_portrait);
			holder.uname = (TextView) convertView.findViewById(R.id.user_name);
			holder.lastContent = (TextView) convertView
					.findViewById(R.id.message_body);
			holder.lastTime = (TextView) convertView
					.findViewById(R.id.message_time);
			holder.msgCount = (TextView) convertView
					.findViewById(R.id.message_count_notify);
			holder.noDisturb = (ImageView) convertView
					.findViewById(R.id.message_time_no_disturb_view);
			// holder.wei_title = (ImageView) convertView
			// .findViewById(R.id.wei_title);
            holder.forbiddenIcon = (ImageView) convertView.findViewById(R.id.forbidden_icon);
			holder.avatar
					.setImageResource(R.drawable.tt_default_user_portrait_corner);
			convertView.setTag(holder);
		} else {
			holder = (ContactViewHolder) convertView.getTag();
		}

		/** 群屏蔽的设定 */
		// if (recentInfo.isForbidden()) {
		// holder.noDisturb.setVisibility(View.VISIBLE);
		// } else {
		// holder.noDisturb.setVisibility(View.GONE);
		// }

		UserEntity info = IMContactManager.instance().findFriendsContact(
				recentInfo.getPeerId());
		if (info == null) {
			info = IMContactManager.instance().findContact(
					recentInfo.getPeerId());
		}

		if (info == null) {
			info = IMContactManager.instance().findDeviceContact(
					recentInfo.getPeerId()); 
		}

		// 置顶
		if (recentInfo.isTop()) {
			// todo R.color.top_session_background
			convertView.setBackgroundColor(Color.parseColor("#E9E6E6"));  //f4f4f4f4
		} else {
			convertView.setBackgroundColor(Color.WHITE);
		}

		if (recentInfo.isForbidden()) {
			holder.noDisturb.setVisibility(View.VISIBLE);
		} else {
			holder.noDisturb.setVisibility(View.GONE);
		}

		
	     boolean singleOnOff = configurationSp.getCfg(recentInfo.getSessionKey(),ConfigurationSp.CfgDimension.NOTIFICATION);
		if (singleOnOff) {
			holder.noDisturb.setVisibility(View.VISIBLE);
		} else {
			holder.noDisturb.setVisibility(View.GONE);
		}

		//如果是设备
		if (info != null&&(Utils.isClientType(info))) { 
			recentInfo.setName(info.getMainName());
			//区分设备与人的区别
			holder.avatar.setCorner(90);

		}else{
			holder.avatar.setCorner(8);
		}
		  
		handleCommonContact(holder, recentInfo);
		return convertView;
	}

	private View renderGroup(int position, View convertView, ViewGroup parent) {
		RecentInfo recentInfo = recentSessionList.get(position);
		GroupViewHolder holder;
		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.tt_item_chat_group,
					parent, false);
			holder = new GroupViewHolder();
			holder.avatarLayout = (IMGroupAvatar) convertView
					.findViewById(R.id.contact_portrait);
			holder.uname = (TextView) convertView.findViewById(R.id.user_name);
			holder.lastContent = (TextView) convertView
					.findViewById(R.id.message_body);
			holder.lastTime = (TextView) convertView
					.findViewById(R.id.message_time);
			holder.msgCount = (TextView) convertView
					.findViewById(R.id.message_count_notify);
			holder.noDisturb = (ImageView) convertView
					.findViewById(R.id.message_time_no_disturb_view);

			holder.device_group_portrait = (ImageView) convertView
					.findViewById(R.id.device_group_portrait);
            holder.forbiddenIcon = (ImageView) convertView.findViewById(R.id.forbidden_icon);
			convertView.setTag(holder);

		} else {
			holder = (GroupViewHolder) convertView.getTag();
		}



		// recentInfo.get 
		// 置顶
		if (recentInfo.isTop()) {
			// todo R.color.top_session_background
			convertView.setBackgroundColor(Color.parseColor("#E9E6E6")); //f4f4f4f4
		} else {
			convertView.setBackgroundColor(Color.WHITE);
		}
		//
		/** 群屏蔽的设定 */
		if (recentInfo.isForbidden()) {
			holder.noDisturb.setVisibility(View.VISIBLE);
		} else {
			holder.noDisturb.setVisibility(View.GONE);
		}

	    boolean singleOnOff = configurationSp.getCfg(recentInfo.getSessionKey(),ConfigurationSp.CfgDimension.NOTIFICATION);
		if (singleOnOff) {
			holder.noDisturb.setVisibility(View.VISIBLE);
		} else {
			holder.noDisturb.setVisibility(View.GONE);
		}
		
		// /** 群屏蔽的设定 */ 
		handleGroupContact(holder, recentInfo);
		return convertView;
	}

	// yingmu base-adapter-helper 了解一下
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// logger.d("recent#getview position:%d", position);
		try {
			final int type = getItemViewType(position);
			ContactHolderBase holder = null;

			switch (type) {
			case CONTACT_TYPE_USER:
				convertView = renderUser(position - CONTACT_TYPE_NUM,
						convertView, parent);
				break;
			case CONTACT_TYPE_GROUP:
				convertView = renderGroup(position - CONTACT_TYPE_NUM,
						convertView, parent);
				break;
			case CONTACT_TYPE_SEARCH:
				convertView = renderNewFunc(position, convertView, parent);
				break;
			case CONTACT_TYPE_NO_NETWORK:
				convertView = renderNetworkFunc(position, convertView, parent);
				break;
			}
			return convertView;
		} catch (Exception e) {
			logger.e(e.toString());
			return null;
		}
	}

	public View renderNewFunc(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.tt_item_friends, parent,
					false);

			EditText search_phone = (EditText) convertView
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

		return convertView;
	}

	public View renderNetworkFunc(int position, View convertView,
			ViewGroup parent) {

		ImageView reconnectingProgressBar = null;
		TextView displayView = null;
		ImageView notifyImage = null;
		View noNetworkView = null;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.tt_item_network, parent,
					false);

		}

		noNetworkView = convertView.findViewById(R.id.layout_no_network);
		reconnectingProgressBar = (ImageView) convertView
				.findViewById(R.id.progressbar_reconnect);
		frameAnimation = (AnimationDrawable)reconnectingProgressBar.getBackground();

		displayView = (TextView) convertView.findViewById(R.id.disconnect_text);
		notifyImage = (ImageView) convertView.findViewById(R.id.imageWifi);


		/** 添加踢出事件 */
		if (touch == 1) {

			if (isOnline) {
				reconnectingProgressBar.setVisibility(View.GONE);
				if(frameAnimation.isRunning()){
					frameAnimation.stop();
				}
				noNetworkView.setVisibility(View.VISIBLE);
				// noNetwork = true;
				notifyImage.setImageResource(R.drawable.pc_notify);
				displayView.setText(R.string.pc_status_notify);

				/** 娣诲姞韪㈠嚭浜嬩欢 */
				noNetworkView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						imService.getLoginManager().reqKickPCClient();
					}
				});
			} else {
				noNetworkView.setVisibility(View.GONE);
				// noNetwork = false;

				if (imService == null) {
					reconnectingProgressBar.setVisibility(View.GONE);
					if(frameAnimation.isRunning()){
						frameAnimation.stop();
					}
					noNetworkView.setVisibility(View.VISIBLE);

					return noNetworkView;
				}

			}

		} else if (touch == 2) {

			if (reconnectingProgressBar != null) {
				reconnectingProgressBar.setVisibility(View.GONE);
				if(frameAnimation.isRunning()){
					frameAnimation.stop();
				}
				// isProgress = false;
			}

			if (noNetworkView != null) {

				notifyImage.setImageResource(R.drawable.warning);
				noNetworkView.setVisibility(View.VISIBLE);
				// noNetwork = true;
				if (imService != null) {
					if (imService.getLoginManager().isKickout()) {
						displayView.setText(R.string.disconnect_kickout);
					} else {
						displayView.setText(R.string.no_network);
					}
				}

				noNetworkView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						logger.d("chatFragment#noNetworkView clicked");
						IMReconnectManager manager = imService
								.getReconnectManager();
						if (NetworkUtil.isNetWorkAvalible(ctx)) {
							fragment.setManualMConnect(true);
							IMLoginManager.instance().relogin();
						} else {
							Utils.showToast(ctx, R.string.no_network_toast);
							return;
						}
						// isProgress = true;

					}
				});
			}
		}

		if (isProgress == false) {
			reconnectingProgressBar.setVisibility(View.GONE);
			if(frameAnimation.isRunning()){
				frameAnimation.stop();
			}
		} else {
			reconnectingProgressBar.setVisibility(View.VISIBLE);
			frameAnimation.start();
		}

		if (noNetwork == false) {
			noNetworkView.setVisibility(View.GONE);
		}else {
			noNetworkView.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

	public void setService(IMService imService) {
		this.imService = imService;
		configurationSp = ConfigurationSp.instance(ctx, imService
				.getLoginManager().getLoginId());
	}

	@Override
	public int getViewTypeCount() {
		return 5;
	}

	@Override
	public int getItemViewType(int position) {

		/*
		if (position == 0) {
			return CONTACT_TYPE_SEARCH;
		} else if (position == 1) {
			return CONTACT_TYPE_NO_NETWORK;
		}
*/
		if (position == 0) {
			return CONTACT_TYPE_NO_NETWORK;
		}
		try {

			// if (position >= recentSessionList.size() - CONTACT_TYPE_NUM) {
			// return CONTACT_TYPE_INVALID;
			// }
			RecentInfo recentInfo = recentSessionList.get(position
					- CONTACT_TYPE_NUM);
			if (recentInfo.getSessionType() == DBConstant.SESSION_TYPE_SINGLE) {
				return CONTACT_TYPE_USER;
			} else if (recentInfo.getSessionType() == DBConstant.SESSION_TYPE_GROUP) {
				return CONTACT_TYPE_GROUP;
			} else {
				return CONTACT_TYPE_INVALID;
			}

		} catch (Exception e) {
			logger.e(e.toString());
			return CONTACT_TYPE_INVALID;
		}
	}

	public void setData(List<RecentInfo> recentSessionList) {
		logger.d("recent#set New recent session list");
		logger.d("recent#notifyDataSetChanged");
		this.recentSessionList = recentSessionList;
		notifyDataSetChanged();
	}

	public void updateSetData() {
		notifyDataSetChanged();
	}

	private void handleCommonContact(ContactViewHolder contactViewHolder,
			RecentInfo recentInfo) {

		String avatarUrl = null;
		String userName = "";
		String lastContent = "";
		String lastTime = "";
		int unReadCount = 0;
		int sessionType = DBConstant.SESSION_TYPE_SINGLE;

		userName = recentInfo.getName();
		lastContent = recentInfo.getLatestMsgData();
		// todo 是不是每次都需要计算
		lastTime = DateUtil.getSessionTime(recentInfo.getUpdateTime());
		unReadCount = recentInfo.getUnReadCnt();
		if (null != recentInfo.getAvatar() && recentInfo.getAvatar().size() > 0) {
			avatarUrl = recentInfo.getAvatar().get(0);

		}
		// 设置未读消息计数
		if (unReadCount > 0) {
			
			String strCountString;
			if (recentInfo.isForbidden()) {  //是否屏蔽通知
				strCountString = "";
				contactViewHolder.msgCount.setVisibility(View.GONE);
//				contactViewHolder.msgCount.setBackgroundResource(R.drawable.red_dot);  //tt_message_botify_no_disturb  test
				contactViewHolder.forbiddenIcon.setVisibility(View.VISIBLE);
//				((RelativeLayout.LayoutParams) contactViewHolder.msgCount
//						.getLayoutParams()).leftMargin = ScreenTools.instance(
//						this.mInflater.getContext()).dip2px(-7);
//				((RelativeLayout.LayoutParams) contactViewHolder.msgCount
//						.getLayoutParams()).topMargin = ScreenTools.instance(
//						this.mInflater.getContext()).dip2px(6);
//				contactViewHolder.msgCount.getLayoutParams().width = ScreenTools
//						.instance(this.mInflater.getContext()).dip2px(10);
//				contactViewHolder.msgCount.getLayoutParams().height = ScreenTools
//						.instance(this.mInflater.getContext()).dip2px(10);
				
			
			}else{
				strCountString = String.valueOf(unReadCount);
				if (unReadCount > 99) {
					strCountString = "99+";
				}
				contactViewHolder.msgCount.setVisibility(View.VISIBLE);
//				contactViewHolder.msgCount.setBackgroundResource(R.drawable.red_dot);  //tt_message_botify_no_disturb  test
				contactViewHolder.forbiddenIcon.setVisibility(View.GONE);
//				contactViewHolder.msgCount.setBackgroundResource(R.drawable.tt_message_notify);  //
//
//				contactViewHolder.msgCount.setVisibility(View.VISIBLE);
//				((RelativeLayout.LayoutParams) contactViewHolder.msgCount
//						.getLayoutParams()).leftMargin = ScreenTools.instance(
//						this.mInflater.getContext()).dip2px(-10);
//				((RelativeLayout.LayoutParams) contactViewHolder.msgCount
//						.getLayoutParams()).topMargin = ScreenTools.instance(
//						this.mInflater.getContext()).dip2px(3);
//				contactViewHolder.msgCount.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
//				contactViewHolder.msgCount.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
//				contactViewHolder.msgCount.setPadding(
//						ScreenTools.instance(this.mInflater.getContext())
//								.dip2px(3), 0,
//						ScreenTools.instance(this.mInflater.getContext())
//								.dip2px(3), 0);

				
			}		
		
//			contactViewHolder.msgCount.setVisibility(View.VISIBLE);
			contactViewHolder.msgCount.setText(strCountString);
		} else {
			contactViewHolder.msgCount.setVisibility(View.GONE);
		}
		// 头像设置
		contactViewHolder.avatar.setImageUrl(avatarUrl);
		// 设置其它信息
		contactViewHolder.uname.setText(userName);
		contactViewHolder.lastContent.setText(lastContent);
		contactViewHolder.lastTime.setText(lastTime);

	}


	private void handleGroupContact(GroupViewHolder groupViewHolder,
			RecentInfo recentInfo) {
		String avatarUrl = null;
		String userName = "";
		String lastContent = "";
		String lastTime = "";
		int unReadCount = 0;
		int sessionType = DBConstant.SESSION_TYPE_SINGLE;

		userName = recentInfo.getName();
		lastContent = recentInfo.getLatestMsgData();
		// todo 是不是每次都需要计算
		lastTime = DateUtil.getSessionTime(recentInfo.getUpdateTime());
		unReadCount = recentInfo.getUnReadCnt();
		// sessionType = recentInfo.getSessionType();
		// 设置未读消息计数 只有群组有的
 
		if (unReadCount > 0) {
			if (recentInfo.isForbidden()) {  //是否屏蔽通知1
//				groupViewHolder.msgCount
//						.setBackgroundResource(R.drawable.tt_message_notify);  //tt_message_botify_no_disturb  test
				 
				groupViewHolder.msgCount.setVisibility(View.GONE);
                groupViewHolder.forbiddenIcon.setVisibility(View.VISIBLE);
//				groupViewHolder.msgCount.setText("");
//				((RelativeLayout.LayoutParams) groupViewHolder.msgCount
//						.getLayoutParams()).leftMargin = ScreenTools.instance(
//						this.mInflater.getContext()).dip2px(-7);
//				((RelativeLayout.LayoutParams) groupViewHolder.msgCount
//						.getLayoutParams()).topMargin = ScreenTools.instance(
//						this.mInflater.getContext()).dip2px(6);
//				groupViewHolder.msgCount.getLayoutParams().width = ScreenTools
//						.instance(this.mInflater.getContext()).dip2px(10);
//				groupViewHolder.msgCount.getLayoutParams().height = ScreenTools
//						.instance(this.mInflater.getContext()).dip2px(10);

			} else {
//				groupViewHolder.msgCount
//						.setBackgroundResource(R.drawable.tt_message_notify);
//				groupViewHolder.msgCount.setVisibility(View.VISIBLE);
//				((RelativeLayout.LayoutParams) groupViewHolder.msgCount
//						.getLayoutParams()).leftMargin = ScreenTools.instance(
//						this.mInflater.getContext()).dip2px(-10);
//				((RelativeLayout.LayoutParams) groupViewHolder.msgCount
//						.getLayoutParams()).topMargin = ScreenTools.instance(
//						this.mInflater.getContext()).dip2px(3);
//				groupViewHolder.msgCount.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
//				groupViewHolder.msgCount.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
//				groupViewHolder.msgCount.setPadding(
//						ScreenTools.instance(this.mInflater.getContext())
//								.dip2px(3), 0,
//						ScreenTools.instance(this.mInflater.getContext())
//								.dip2px(3), 0);

				String strCountString = String.valueOf(unReadCount);
				if (unReadCount > 99) {
					strCountString = "99+";
				}
				groupViewHolder.msgCount.setVisibility(View.VISIBLE);
				groupViewHolder.msgCount.setText(strCountString);
                groupViewHolder.forbiddenIcon.setVisibility(View.GONE);
			}

		} else {
			groupViewHolder.msgCount.setVisibility(View.GONE);
		}

		// 头像设置
		setGroupAvatar(groupViewHolder, recentInfo.getAvatar(), recentInfo);
		// 设置其它信息
		groupViewHolder.uname.setText(userName);
        groupViewHolder.lastContent.setText(lastContent);
		groupViewHolder.lastTime.setText(lastTime);
	}

	public void putProgress(boolean isProgress) {

		this.isProgress = isProgress;
		notifyDataSetChanged();
	}

	public void putNetwork(boolean noNetwork) {

		this.noNetwork = noNetwork;
		notifyDataSetChanged();
	}

	public void putTouch(int touch, boolean isOnline, boolean noNetwork) {

		this.noNetwork = noNetwork;

		this.touch = touch;
		this.isOnline = isOnline;
		notifyDataSetChanged();
	}

	/**
	 * 设置群头像
	 * 
	 * @param holder
	 * @param avatarUrlList
	 */
	private void setGroupAvatar(GroupViewHolder holder,
			List<String> avatarUrlList  , RecentInfo recentInfo) {

		try {
			if (null == avatarUrlList) {
				return;
			}

            GroupEntity entity = IMGroupManager.instance().findFamilyGroup(recentInfo.getPeerId());


			holder.avatarLayout
					.setAvatarUrlAppend(SysConstant.AVATAR_APPEND_32);
			holder.avatarLayout.setChildCorner(90);
			holder.avatarLayout.setViewSize(ScreenUtil.instance(this.ctx)
					.dip2px(48));

			//如果是家庭群
            if(entity!=null&&(entity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY)){
                holder.avatarLayout.setBackgroundResource(R.drawable.group_circular_bk);
				holder.device_group_portrait.setVisibility(View.VISIBLE);
				holder.avatarLayout.setDefaultChildAvatarRes(R.drawable.default_circular_avatar);
				holder.avatarLayout.setChildCorner(90);
				if (recentInfo.isTop()) {
					holder.device_group_portrait.setBackgroundResource(R.drawable.dev_group_gray_bg);
				}else{
					holder.device_group_portrait.setBackgroundResource(R.drawable.dev_group_bg);
				}
			}else{
//				holder.avatarLayout.setBackgroundResource(0);
                holder.avatarLayout.setDefaultChildAvatarRes(R.drawable.group_default);
				holder.device_group_portrait.setVisibility(View.GONE);
				holder.avatarLayout.setChildCorner(0);
			}

            if (null != avatarUrlList) {
					holder.avatarLayout.setAvatarUrls(new ArrayList<String>(
							avatarUrlList));
            }

		} catch (Exception e) {
			logger.e(e.toString());
		}

	}

}

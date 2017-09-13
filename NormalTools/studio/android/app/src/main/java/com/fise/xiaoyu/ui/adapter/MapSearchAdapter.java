package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
 
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.ui.widget.IMGroupAvatar;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *        列表的顺序是： 用户-->群组-->部门
 */
public class MapSearchAdapter extends BaseAdapter implements
		AdapterView.OnItemLongClickListener {

	private Logger logger = Logger.getLogger(MapSearchAdapter.class);

	private List<UserEntity> userList = new ArrayList<>();

	private String searchKey;
	private Context ctx;
	private IMService imService;

	public MapSearchAdapter(Context context, IMService pimService) {
		this.ctx = context;
		this.imService = pimService;
	}

	public void clear() {
		this.userList.clear();
		notifyDataSetChanged();
	}

	public void putUserList(List<UserEntity> pUserList) {
		this.userList.clear();
		if (pUserList == null || pUserList.size() <= 0) {
			return;
		}
		this.userList = pUserList;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Object object = getItem(position);
		if (object instanceof UserEntity) {
			UserEntity userEntity = (UserEntity) object;
			IMUIHelper.handleContactItemLongClick(userEntity, ctx);
		} else {
		}
		return true;
	}

	@Override
	public int getItemViewType(int position) {
		// 根据entity的类型进行判断， 或者根据长度判断
		int userSize = userList == null ? 0 : userList.size();

		return SearchType.USER.ordinal();
	}

	@Override
	public int getViewTypeCount() {
		return SearchType.values().length;
	}

	@Override
	public int getCount() {
		// todo Elegant code
		int userSize = userList == null ? 0 : userList.size();
		return userSize;
	}

	@Override
	public Object getItem(int position) {
		int typeIndex = getItemViewType(position);
		SearchType renderType = SearchType.values()[typeIndex];
		switch (renderType) {
		case USER: {
			return userList.get(position);
		}

		default:
			throw new IllegalArgumentException("SearchAdapter#getItem#不存在的类型"
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
		SearchType renderType = SearchType.values()[typeIndex];
		View view = null;
		switch (renderType) {
		case USER: {
			view = renderUser(position, convertView, parent);
		}
			break;
		}
		return view;
	}

	public View renderUser(int position, View view, ViewGroup parent) {
		UserHolder userHolder = null;
		UserEntity userEntity = (UserEntity) getItem(position);
		if (userEntity == null) {
			logger.e("SearchAdapter#renderUser#userEntity is null!position:%d",
					position);
			return null;
		}
		if (view == null) {
			userHolder = new UserHolder();
			view = LayoutInflater.from(ctx).inflate(
					R.layout.tt_item_map_contact, parent, false);
			userHolder.nameView = (TextView) view
					.findViewById(R.id.contact_item_title);
			userHolder.realNameView = (TextView) view
					.findViewById(R.id.contact_realname_title);
			userHolder.avatar = (IMBaseImageView) view
					.findViewById(R.id.contact_portrait);
			userHolder.divider = view.findViewById(R.id.contact_divider);
			view.setTag(userHolder);
		} else {
			userHolder = (UserHolder) view.getTag();
		}

		

		if(userEntity.getComment().equals(""))
		{
			IMUIHelper.setTextHilighted(userHolder.nameView,
					userEntity.getMainName(), userEntity.getSearchElement());
		}else{
			IMUIHelper.setTextHilighted(userHolder.nameView,
					userEntity.getComment(), userEntity.getSearchElement());
		}
	
		// userHolder.nameView.setText(userEntity.getNickName());

		userHolder.avatar
				.setImageResource(R.drawable.tt_default_user_portrait_corner);
		userHolder.divider.setVisibility(View.VISIBLE);

		// 分栏显示“联系人”
		if (position == 0) {
			// 分栏已经显示，最上面的分割线不用显示
			userHolder.divider.setVisibility(View.GONE);
		} else {
			userHolder.divider.setVisibility(View.VISIBLE);
		}

		userHolder.avatar
				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
		userHolder.avatar.setCorner(0);
		userHolder.avatar.setImageUrl(userEntity.getAvatar());

		userHolder.realNameView.setText(userEntity.getRealName());
		userHolder.realNameView.setVisibility(View.GONE);
		return view;
	}

	/**
	 * 与contactAdapter 有公用的地方，可以抽取到IMUIHelper 设置群头像
	 * 
	 * @param avatar
	 * @param avatarUrlList
	 */
	private void setGroupAvatar(IMGroupAvatar avatar, List<String> avatarUrlList) {
		try {
			avatar.setViewSize(ScreenUtil.instance(ctx).dip2px(38));
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
		TextView nameView;
		TextView realNameView;
		IMBaseImageView avatar;
	}

	private enum SearchType {
		USER, ILLEGAL
	}

	/** ---------------------------set/get-------------------------- */
	public String getSearchKey() {
		return searchKey;
	}

	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}
}

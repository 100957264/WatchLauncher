package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 
 */
public class WeiContactAdapter extends BaseAdapter implements SectionIndexer,
		AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

	private Logger logger = Logger.getLogger(WeiContactAdapter.class);
	// public List<GroupEntity> groupList = new ArrayList<>();
	public List<UserEntity> weiList = new ArrayList<>();

	private Context ctx;
	private IMService imService;

	public WeiContactAdapter(Context context, IMService imService) {
		this.ctx = context;
		this.imService = imService;
	}

	public void putWeiList(List<UserEntity> pUserList) {
		this.weiList.clear();
		if (pUserList == null || pUserList.size() < 0) {
			return;
		}
		// if(pUserList == null || pUserList.size() <=0){
		// return;
		// }
		this.weiList = pUserList;
		notifyDataSetChanged();
	}

 

	private List<Map<String, Object>> getData() {
		// map.put(参数名字,参数值)
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

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
	 * Given the index of a section within the array of section objects, returns
	 * the starting position of that section within the adapter.
	 * <p/>
	 * If the section's starting position is outside of the adapter bounds, the
	 * position must be clipped to fall within the size of the adapter.
	 * 
	 * @param section
	 *            the index of the section within the array of section objects
	 * @return the starting position of that section within the adapter,
	 *         constrained to fall within the adapter bounds
	 */
	@Override
	public int getPositionForSection(int section) {

		for (int i = 0; i < weiList.size(); i++) {

			int firstCharacter = weiList.get(i).getPinyinElement().pinyin.charAt(0);
			// logger.d("firstCharacter:%d", firstCharacter);
			if (firstCharacter == section) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Given a position within the adapter, returns the index of the
	 * corresponding section within the array of section objects.
	 * <p/>
	 * If the section index is outside of the section array bounds, the index
	 * must be clipped to fall within the size of the section array.
	 * <p/>
	 * For example, consider an indexer where the section at array index 0
	 * starts at adapter position 100. Calling this method with position 10,
	 * which is before the first section, must return index 0.
	 * 
	 * @param position
	 *            the position within the adapter for which to return the
	 *            corresponding section index
	 * @return the index of the corresponding section within the array of
	 *         section objects, constrained to fall within the array bounds
	 */
	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
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

		return ContactType.USER.ordinal();
	}

	@Override
	public int getViewTypeCount() {
		return ContactType.values().length;
	}

	@Override
	public int getCount() {
		// guanweile
		// int groupSize = groupList==null?0:groupList.size();
		int userSize = weiList == null ? 0 : weiList.size();
		// int sum = groupSize + userSize + FuncSize;
		int sum = userSize;
		return sum;
	}

	@Override
	public Object getItem(int position) {
		int typeIndex = getItemViewType(position);
		ContactType renderType = ContactType.values()[typeIndex];
		switch (renderType) {

		case USER: {
			// int groupSize = groupList==null?0:groupList.size();- groupSize
			int realIndex = position;
//			if (realIndex < 0) {
//				throw new IllegalArgumentException(
//						"ContactAdapter#getItem#user类型判断错误!");
//			}
			if(realIndex >=0){
				return weiList.get(realIndex);
			}

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
 

		case USER: {
			view = renderUser(position, convertView, parent);
		}
			break;
 
		}
		return view;
	}

	public View renderFunc(int position, View view, ViewGroup parent) {

		UserHolder userHolder = null;

		if (view == null) {
			userHolder = new UserHolder();
			view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_contact,
					parent, false);
			userHolder.nameView = (TextView) view
					.findViewById(R.id.contact_item_title);
			userHolder.realNameView = (TextView) view
					.findViewById(R.id.contact_realname_title);
			userHolder.sectionView = (TextView) view
					.findViewById(R.id.contact_category_title);
			userHolder.avatar = (IMBaseImageView) view
					.findViewById(R.id.contact_portrait);
			userHolder.divider = view.findViewById(R.id.contact_divider);
			view.setTag(userHolder);
		} else {
			userHolder = (UserHolder) view.getTag();
		}

		String name = (getData().get(position)).get("title").toString();
		int id = (int) (getData().get(position)).get("img");
		/*** reset-- 控件的默认值 */
		userHolder.nameView.setText(name);
		userHolder.avatar.setImageResource(id);
		//userHolder.divider.setVisibility(View.VISIBLE);

		userHolder.sectionView.setVisibility(View.GONE);

        // 不显示分割线
        userHolder.divider.setVisibility(View.GONE);
        
          
		userHolder.avatar.setDefaultImageRes(id);
		userHolder.avatar.setCorner(0);
		userHolder.realNameView.setVisibility(View.GONE);
		return view;
	}

//	public View renderUser(int position, View view, ViewGroup parent) {
//		UserHolder userHolder = null;
//		UserEntity userEntity = (UserEntity) getItem(position);
//		if (userEntity == null) {
//			logger.e(
//					"ContactAdapter#renderUser#userEntity is null!position:%d",
//					position);
//			// todo 这个会报错误的，怎么处理
//			return null;
//		}
//		if (view == null) {
//			userHolder = new UserHolder();
//			view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_contact,
//					parent, false);
//			userHolder.nameView = (TextView) view
//					.findViewById(R.id.contact_item_title);
//			userHolder.realNameView = (TextView) view
//					.findViewById(R.id.contact_realname_title);
//			userHolder.sectionView = (TextView) view
//					.findViewById(R.id.contact_category_title);
//			userHolder.avatar = (IMBaseImageView) view
//					.findViewById(R.id.contact_portrait);
//			userHolder.divider = view.findViewById(R.id.contact_divider);
//			view.setTag(userHolder);
//		} else {
//			userHolder = (UserHolder) view.getTag();
//		}
//
//		/*** reset-- 控件的默认值 */
//		//userHolder.nameView.setText(userEntity.getMainName());
//		if (userEntity.getComment().equals("")) {
//			userHolder.nameView.setText(userEntity.getMainName());
//		} else {
//			userHolder.nameView.setText(userEntity.getComment());
//		}
//		userHolder.avatar
//				.setImageResource(R.drawable.tt_default_user_portrait_corner);
//		userHolder.divider.setVisibility(View.VISIBLE);
//		userHolder.sectionView.setVisibility(View.GONE);
//
//		// 字母序第一个要展示
//		// todo pinyin控件不能处理多音字的情况，或者UserEntity类型的统统用pinyin字段进行判断
//		String sectionName = userEntity.getSectionName();
//		// 正式群在用户列表的上方展示
//
//		// 获取上一个实体的preSectionName,这个时候position > groupSize
//		UserEntity preUser = (UserEntity) getItem(position);
//		String preSectionName = preUser.getSectionName();
////		if (TextUtils.isEmpty(preSectionName)
////				|| !preSectionName.equals(sectionName)) {
////			userHolder.sectionView.setVisibility(View.VISIBLE);
////			userHolder.sectionView.setText(sectionName);
////			// 不显示分割线
////			userHolder.divider.setVisibility(View.GONE);
////		} else {
////			userHolder.sectionView.setVisibility(View.GONE);
////		}
//
//		userHolder.sectionView.setVisibility(View.VISIBLE);
//		userHolder.sectionView.setText(sectionName);
//		// 不显示分割线
//		userHolder.divider.setVisibility(View.GONE);
//
//		userHolder.avatar
//				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
//		userHolder.avatar.setCorner(0);
//		userHolder.avatar.setImageUrl(userEntity.getAvatar());
//
//		if(userEntity.getOnLine() != DBConstant.ONLINE)
//		{
//			ColorMatrix matrix = new ColorMatrix();
//			matrix.setSaturation(0);
//			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
//			userHolder.avatar.setColorFilter(filter);
//		}else{
//
//			ColorMatrix matrix = new ColorMatrix();
//			matrix.setSaturation(1);
//			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(
//					matrix);
//			userHolder.avatar.setColorFilter(filter);
//		}
//
//		userHolder.realNameView.setText(userEntity.getRealName());
//		userHolder.realNameView.setVisibility(View.GONE);
//
//		return view;
//	}



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
//		if (position == (FuncSize)) {
//			userHolder.sectionView.setText(sectionName);
//
//			// 分栏已经显示，最上面的分割线不用显示
//			userHolder.divider.setVisibility(View.VISIBLE);
//			userHolder.contact_buttom.setVisibility(View.VISIBLE); // guan
//		} else {

			// 获取上一个实体的preSectionName,这个时候position > groupSize
        UserEntity preUser = null;
           if(position >= 1) {
                preUser = (UserEntity) getItem(position - 1);
           }else{
                preUser = (UserEntity) getItem(position);
           }
               String preSectionName = preUser.getSectionName();
               if (TextUtils.isEmpty(preSectionName) || !preSectionName.equals(sectionName) || position == 0) {
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


		userHolder.avatar
				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
		userHolder.avatar
				.setImageResource(R.drawable.tt_default_user_portrait_corner);

		userHolder.avatar.setImageUrl(userEntity.getAvatar());
		userHolder.realNameView.setText(userEntity.getRealName());
		userHolder.realNameView.setVisibility(View.GONE);
        if(userEntity.getOnLine() != DBConstant.ONLINE)
		{
			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(0);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
			userHolder.avatar.setColorFilter(filter);
		}else{

			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(1);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(
					matrix);
			userHolder.avatar.setColorFilter(filter);
		}

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
		groupHolder.sectionView.setVisibility(View.GONE);

		// 分割线的处理【位于控件的最上面】
		groupHolder.divider.setVisibility(View.VISIBLE);

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
			if (i > DBConstant.GROUP_AVATAR_NUM-1) {
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
//		View divider;
//		TextView sectionView;
//		TextView nameView;
//		TextView realNameView;
//		IMBaseImageView avatar;

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

	private enum ContactType {  USER// ,
		// GROUP
	}
}

package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.RankingListEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;

import java.util.ArrayList;
import java.util.List;

public class RankingListAdspter extends BaseAdapter implements
		AdapterView.OnItemLongClickListener {

	private LayoutInflater layoutInflater;
	private Context context;
	private IMService imService;
	public List<RankingListEntity> userList = new ArrayList<>();

	public RankingListAdspter(Context context, List<RankingListEntity> userList, IMService imService) {
		this.context = context;
		this.userList = userList;
		this.layoutInflater = LayoutInflater.from(context);
		this.imService = imService;
	}

	/**
	 * 组件集合，对应list.xml中的控件
	 */
	public final class Zujian {
		public IMBaseImageView image;
		public TextView title;
		public TextView info;
		public TextView setp_num;
	}

	@Override
	public int getCount() {

		int userSize = userList == null ? 0 : userList.size();

		return userSize ;
	}

	@Override
	public int getItemViewType(int position) {

		int userSize = userList == null ? 0 : userList.size();
		//
		return ContactType.FRIENDS.ordinal();
	}

	/**
	 * 获得某一位置的数据
	 */
	@Override
	public Object getItem(int position) {
		int typeIndex = getItemViewType(position);
		ContactType renderType = ContactType.values()[typeIndex];
		int userSize = userList == null ? 0 : userList.size();
		switch (renderType) {

		case FRIENDS: {
			if (position < userSize) {
				return userList.get(position);
			}
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

	public void putRankingList(List<RankingListEntity> pUserList) {
		// this.userList.clear();
		this.userList = pUserList;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int typeIndex = getItemViewType(position);
		ContactType renderType = ContactType.values()[typeIndex];
		View view = null;
		int userSize = userList == null ? 0 : userList.size();

		switch (renderType) {

		case FRIENDS: {
			view = renderFunc(position, convertView, parent);
		}
			break;


		}

		return view;
	}

	public View renderFunc(int position, View convertView, ViewGroup parent) {

		Zujian zujian = null;
		if (convertView == null) {
			zujian = new Zujian();
			// 获得组件，实例化组件
			convertView = layoutInflater.inflate(R.layout.tt_item_setp_ranking,
					null);

			convertView.setTag(zujian);
		} else {
			zujian = (Zujian) convertView.getTag();
		}

		zujian.image = (IMBaseImageView) convertView.findViewById(R.id.img);
		zujian.title = (TextView) convertView.findViewById(R.id.tv);
		zujian.info = (TextView) convertView.findViewById(R.id.info);
		zujian.setp_num = (TextView) convertView.findViewById(R.id.setp_num);

		UserEntity userEntity = null;
		if(userList.get(position).getChampion_id() == imService.getLoginManager().getLoginId())
		{
			userEntity = imService.getLoginManager().getLoginInfo();
		}else{
			userEntity = imService.getContactManager().findFriendsContact(userList.get(position).getChampion_id());
		}


		if(userEntity ==null)
		{
			userEntity = imService.getContactManager().findParentContact(userList.get(position).getChampion_id());
		}
		if(userEntity ==null)
		{
			userEntity = imService.getContactManager().findDeviceContact(userList.get(position).getChampion_id());
		}



		if(userEntity ==null)
		{
			return convertView;
		}


		zujian.image.setImageUrl(userEntity.getAvatar());
		zujian.title.setText(userEntity.getMainName());
		int ranking = position + 1;
		zujian.info.setText("" + ranking);
		if(position <userList.size())
		{
			int num = userList.get(position).getStep_num();
			zujian.setp_num.setText("" + userList.get(position).getStep_num());
		}


		return convertView;
	}


	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			int position, long arg3) {

		// TODO Auto-generated method stub
		// When clicked, show a toast with the TextView text

		return true;

	}

	private enum ContactType {
		FRIENDS// ,
		// GROUP
	}
}

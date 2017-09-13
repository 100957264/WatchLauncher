package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.StepRanking;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StepRankingAdspter extends BaseAdapter implements
		AdapterView.OnItemLongClickListener {

	private LayoutInflater layoutInflater;
	private Context context;
	private IMService imService;
	public List<StepRanking> rankingList = new ArrayList<>();

	public StepRankingAdspter(Context context, List<StepRanking> rankingList, IMService imService) {
		this.context = context;
		this.rankingList = rankingList;
		this.layoutInflater = LayoutInflater.from(context);
		this.imService = imService;
	}

	/**
	 * 组件集合，对应list.xml中的控件
	 */
	public final class Zujian {
		public IMBaseImageView image;
		public TextView ranking;
		public TextView step_num;
		public TextView step_date;

		public ImageView line_title;

	}

	@Override
	public int getCount() {

		int userSize = rankingList == null ? 0 : rankingList.size();

		return userSize ;
	}

	@Override
	public int getItemViewType(int position) {

		int userSize = rankingList == null ? 0 : rankingList.size();
		//
		return ContactType.RANKINGS.ordinal();
	}

	/**
	 * 获得某一位置的数据
	 */
	@Override
	public Object getItem(int position) {
		int typeIndex = getItemViewType(position);
		ContactType renderType = ContactType.values()[typeIndex];
		int userSize = rankingList == null ? 0 : rankingList.size();
		switch (renderType) {

		case RANKINGS: {
			if (position < userSize) {
				return rankingList.get(position);
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

	public void putRankingList(List<StepRanking> rankingList) {
		// this.userList.clear();
		this.rankingList = rankingList;
		notifyDataSetChanged();
	}

	public void addRankingList(List<StepRanking> rankingList) {
		// this.userList.clear();
		this.rankingList.addAll(rankingList);
		notifyDataSetChanged();
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int typeIndex = getItemViewType(position);
		ContactType renderType = ContactType.values()[typeIndex];
		View view = null;
		int userSize = rankingList == null ? 0 : rankingList.size();

		switch (renderType) {

		case RANKINGS: {
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
			convertView = layoutInflater.inflate(R.layout.tt_ranking_list_item,
					null);

			convertView.setTag(zujian);
		} else {
			zujian = (Zujian) convertView.getTag();
		}

		zujian.image = (IMBaseImageView) convertView.findViewById(R.id.img);
		zujian.ranking = (TextView) convertView.findViewById(R.id.ranking);
		zujian.step_num = (TextView) convertView.findViewById(R.id.step_num);
		zujian.step_date = (TextView) convertView.findViewById(R.id.step_date);

		zujian.line_title = (ImageView) convertView
				.findViewById(R.id.line_title);


		zujian.ranking.setText("名次:" + rankingList.get(position).getRanking());
		zujian.step_num.setText("步数:" + rankingList.get(position).getStep_num());


		UserEntity userEntity = null;
		if(rankingList.get(position).getChampion_id() == imService.getLoginManager().getLoginId())
		{
			userEntity = imService.getLoginManager().getLoginInfo();
		}else{
			userEntity = imService.getContactManager().findFriendsContact(rankingList.get(position).getChampion_id());
		}


		if(userEntity ==null)
		{
			userEntity = imService.getContactManager().findParentContact(rankingList.get(position).getChampion_id());
		}
		if(userEntity ==null)
		{
			userEntity = imService.getContactManager().findDeviceContact(rankingList.get(position).getChampion_id());
		}
		if(userEntity !=null)
		{
			zujian.image.setImageUrl(userEntity.getAvatar());
			long lt = new Long(rankingList.get(position).getUpdate_time());
			Date date = new Date(lt*1000L);

			int month = date.getMonth() + 1;
			zujian.step_date.setText( userEntity.getMainName() + "夺得" + month + "月" + date.getDate() + "日"  + "冠军");
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
		RANKINGS// ,
		// GROUP
	}
}

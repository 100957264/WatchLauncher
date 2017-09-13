package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.protobuf.IMUserAction;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.ui.widget.IMGroupAvatar;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * SearchAdapter
 *
 */
public class SchoolAdapter extends BaseAdapter implements
		AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

	private Logger logger = Logger.getLogger(SchoolAdapter.class);

	private List<IMUserAction.SchoolInfo> schoolList = new ArrayList<>();

	private String searchKey;
	private Context ctx;
	private IMService imService;

	public SchoolAdapter(Context context, IMService pimService, List<IMUserAction.SchoolInfo> schoolList) {
		this.ctx = context;
		this.imService = pimService;
		//this.schoolList = schoolList;
		for(int i=0;i<schoolList.size();i++){
			this.schoolList.add(schoolList.get(i));
		}
	}

	public void clear() {
		this.schoolList.clear();
		notifyDataSetChanged();
	}

	public List<IMUserAction.SchoolInfo> getSchoolList(){
		return this.schoolList;
	}

	public void putSchoolList(List<IMUserAction.SchoolInfo> schoolList) {
		this.schoolList.clear();
		//this.schoolList = schoolList;
		for(int i=0;i<schoolList.size();i++){
			this.schoolList.add(schoolList.get(i));
		}
		notifyDataSetChanged();
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		IMUserAction.SchoolInfo schoolInfo = (IMUserAction.SchoolInfo) getItem(position);
		if (schoolInfo == null) {
			return ;
		}

		IMUIHelper.openAddDeviceActivity(ctx,schoolInfo.getSchoolId());


	} 

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Object object = getItem(position);

		return true;
	}

	@Override
	public int getItemViewType(int position) {
		// 根据entity的类型进行判断， 或者根据长度判断
		return SearchType.SCHOOL.ordinal();
	}

	@Override
	public int getViewTypeCount() {
		return SearchType.values().length;
	}

	@Override
	public int getCount() {
		// todo Elegant code
		return schoolList.size();
	}

	@Override
	public Object getItem(int position) {
		int typeIndex = getItemViewType(position);
		SearchType renderType = SearchType.values()[typeIndex];
		switch (renderType) {
		case SCHOOL: {
			return schoolList.get(position);
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
		case SCHOOL: {
			view = renderSchool(position, convertView, parent);
		}
			break;
		}
		return view;
	}

	public View renderSchool(int position, View view, ViewGroup parent) {
		SchoolHolder schoolHolder = null;
		IMUserAction.SchoolInfo schoolInfo = (IMUserAction.SchoolInfo) getItem(position);
		if (schoolInfo == null) {
			logger.e("SearchAdapter#renderUser#userEntity is null!position:%d",
					position);
			return null;
		}
		if (view == null) {
			schoolHolder = new SchoolHolder();
			view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_school,
					parent, false);
			schoolHolder.schoolName = (TextView) view
					.findViewById(R.id.school_name);
			view.setTag(schoolHolder);
		} else {
			schoolHolder = (SchoolHolder) view.getTag();
		}

		schoolHolder.schoolName.setText(schoolInfo.getSchoolName());


		return view;
	}
  




	// 将分割线放在上面，利于判断
	public static class SchoolHolder {
		TextView schoolName;
	}

	private enum SearchType {
		SCHOOL
	}

	/** ---------------------------set/get-------------------------- */
	public String getSearchKey() {
		return searchKey;
	}

	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}
}

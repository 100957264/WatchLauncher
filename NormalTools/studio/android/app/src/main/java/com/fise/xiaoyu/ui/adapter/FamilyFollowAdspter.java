package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.FamilyConcernEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.imservice.manager.IMDeviceManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.IMUIHelper;

import java.util.List;

public class FamilyFollowAdspter extends BaseAdapter implements
		AdapterView.OnItemLongClickListener {

	private List<UserEntity> authList;
	private LayoutInflater layoutInflater;
	private Context context;
	private String deviceId;
	private UserEntity deviceUser;
	private DeviceEntity device;
	private UserEntity loginContact;

	public FamilyFollowAdspter(Context context, List<UserEntity> authList,
			String deviceId,UserEntity deviceUser,DeviceEntity device) {
		this.context = context;
		this.authList = authList;
		this.layoutInflater = LayoutInflater.from(context);
		this.deviceId = deviceId;
		this.deviceUser = deviceUser;
		
		this.device = device;
	    loginContact = IMLoginManager.instance().getLoginInfo();
	}

	/**
	 * 组件集合，对应list.xml中的控件
	 */
	public final class Zujian {
		IMBaseImageView contact_portrait;
		
		public TextView title;
		public TextView phone;

	}

	@Override
	public int getCount() {
        return authList.size();
	}

	/**
	 * 获得某一位置的数据
	 */
	@Override
	public Object getItem(int position) {
		return authList.get(position);
	}

	/**
	 * 获得唯一标识
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Zujian zujian = null;
		if (convertView == null) {
			zujian = new Zujian();
			// 获得组件，实例化组件
			convertView = layoutInflater.inflate(R.layout.family_list_item,
					null);
			 
	        zujian.contact_portrait = (IMBaseImageView) convertView.findViewById(R.id.contact_portrait);
			zujian.title = (TextView) convertView.findViewById(R.id.tv_name);
			zujian.phone = (TextView) convertView.findViewById(R.id.tv_phone);

			convertView.setTag(zujian);
		} else {
			zujian = (Zujian) convertView.getTag();
		}

		FamilyConcernEntity family = IMDeviceManager.instance()
				.findFamilyConcern(authList.get(position).getPeerId(),deviceUser.getPeerId());
		 
		 
		if (family != null) {
			// 绑定数据
			zujian.contact_portrait.setImageUrl(family.getUserAvatar());
			zujian.title.setText(family.getIdentity());
			zujian.phone.setText(authList.get(position).getPhone());
		} else {

			// 绑定数据
			zujian.contact_portrait.setImageUrl(authList.get(position).getUserAvatar());
			zujian.title.setText(authList.get(position).getMainName());
			zujian.phone.setText(authList.get(position).getPhone());
			if (authList.get(position).getComment().equals("")) {
				zujian.title.setText(authList.get(position).getMainName());
			} else {
				//userName.setText("" + currentUser.getComment());
				zujian.title.setText(authList.get(position).getComment());
			}
		}
		return convertView;
	}

	public void putDeviceList(List<UserEntity> authList) {

		if (authList == null || authList.size() <= 0) {
			return;
		}
		this.authList = authList;
		notifyDataSetChanged();
	}

	public void putUpdateDeviceList(List<UserEntity> authList) {

		if (authList == null) {
			return;
		}
		this.authList = authList;
		notifyDataSetChanged();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		
	 	if (device != null&& device.getMasterId() == loginContact.getPeerId()) { 
 
			UserEntity object = (UserEntity) getItem(position);
			FamilyConcernEntity family = IMDeviceManager.instance()
					.findFamilyConcern(authList.get(position).getPeerId(),deviceUser.getPeerId());
			IMUIHelper.handleDeviceItemLongClick(deviceId, context, object, family,deviceUser);
		}
		

		return true;
	}
	 
}
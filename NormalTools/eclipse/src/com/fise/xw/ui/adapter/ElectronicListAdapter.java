package com.fise.xw.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.ElectricFenceEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.imservice.service.IMService;

public class ElectronicListAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private Context context;
	private IMService imService;
	public List<ElectricFenceEntity> electronicList = new ArrayList<>();
	private int currentUserId;
	private DeviceEntity device;

	public ElectronicListAdapter(Context context,
			List<ElectricFenceEntity> electronicList, IMService imService,
			int currentUserId, DeviceEntity device) {
		this.context = context;
		this.electronicList = electronicList;
		this.layoutInflater = LayoutInflater.from(context);
		this.imService = imService;
		this.currentUserId = currentUserId;
		this.device = device;

	}

	/**
	 * 组件集合，对应list.xml中的控件
	 * 
	 * @author Administrator
	 */
	public final class Zujian {
		public TextView electronic_name;
		public TextView electronic_distance;
		public CheckBox electronic_checkbox;
	}

	@Override
	public int getCount() {
		return electronicList.size();
	}

	/**
	 * 获得某一位置的数据
	 */
	@Override
	public Object getItem(int position) {
		return electronicList.get(position);
	}

	/**
	 * 获得唯一标识
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	public void putDeviceList(List<ElectricFenceEntity> electronicList) {
		this.electronicList = electronicList;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Zujian zujian = null;
		if (convertView == null) {
			zujian = new Zujian();
			// 获得组件，实例化组件
			convertView = layoutInflater.inflate(R.layout.electronic_list_item,
					null);

			convertView.setTag(zujian);
		} else {
			zujian = (Zujian) convertView.getTag();
		}

		zujian.electronic_name = (TextView) convertView
				.findViewById(R.id.electronic_name);
		zujian.electronic_distance = (TextView) convertView
				.findViewById(R.id.electronic_distance);
		zujian.electronic_checkbox = (CheckBox) convertView
				.findViewById(R.id.electronic_checkbox);

		String[] name = electronicList.get(position).getMark().split("##"); // 名字

		if (name[0] != null) {
			zujian.electronic_name.setText(name[0] + "");
		}

		if (name.length > 1) {
			zujian.electronic_distance.setText(electronicList.get(position)
					.getRadius() + "米," + name[1]);
		} else {
			zujian.electronic_distance.setText(electronicList.get(position)
					.getRadius() + "米,");
		}
 

		final int postionTemp = position;

		if (electronicList.get(position).getStatus() == 1) {
			zujian.electronic_checkbox.setChecked(true);
		} else if (electronicList.get(position).getStatus() == 2) {
			zujian.electronic_checkbox.setChecked(false);
		}

		if (device.getMasterId() != imService.getLoginManager().getLoginId()) {
			zujian.electronic_checkbox.setEnabled(false);
		}

		final CheckBox checkBox = zujian.electronic_checkbox;
		zujian.electronic_checkbox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (device.getMasterId() != imService.getLoginManager()
						.getLoginId()) {
					Toast.makeText(context, "你没有权限修改", Toast.LENGTH_SHORT)
							.show();
					return;
				}

				boolean isElect = false;
				int enbaleNum = 0;
				for (int i = 0; i < electronicList.size(); i++) {
					if (electronicList.get(i).getStatus() == 1) {
						enbaleNum = enbaleNum + 1;
					}
					if (enbaleNum > 0) {
						isElect = true;
						break;
					}
				}

				if (isElect == false) {

					int Stats_enbale;
					if (checkBox.isChecked()) {
						Stats_enbale = DBConstant.ELECTRONIC_STATS_ENABLE;
					} else {
						Stats_enbale = DBConstant.ELECTRONIC_STATS_DISABLE;
					}

					imService.getDeviceManager().settingElectronic(
							imService.getLoginManager().getLoginId(),
							currentUserId, DBConstant.ELECTRONIC_UPDATE,
							electronicList.get(postionTemp).getFenceId(),
							electronicList.get(postionTemp).getLng() + "",
							electronicList.get(postionTemp).getLat() + "",
							electronicList.get(postionTemp).getRadius(),
							electronicList.get(postionTemp).getMark(),
							Stats_enbale);
				} else {

					if (checkBox.isChecked() == false) {
						imService.getDeviceManager().settingElectronic(
								imService.getLoginManager().getLoginId(),
								currentUserId, DBConstant.ELECTRONIC_UPDATE,
								electronicList.get(postionTemp).getFenceId(),
								electronicList.get(postionTemp).getLng() + "",
								electronicList.get(postionTemp).getLat() + "",
								electronicList.get(postionTemp).getRadius(),
								electronicList.get(postionTemp).getMark(),
								DBConstant.ELECTRONIC_STATS_DISABLE);
					} else {
						checkBox.setChecked(false);
						Toast.makeText(context, "卡片机目前支持一个安全围栏,请先关闭对应的安全围栏",
								Toast.LENGTH_SHORT).show();
					}
				}

			}
		});

		return convertView;
	}

}

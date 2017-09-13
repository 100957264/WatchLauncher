package com.fise.xw.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R; 
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.manager.IMDeviceManager;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.manager.IMUserActionManager; 
import com.fise.xw.protobuf.IMBaseDefine.CommandType;
import com.fise.xw.protobuf.IMBaseDefine.SessionType;
import com.fise.xw.protobuf.IMDevice.AlarmType;
import com.fise.xw.ui.activity.PostionTouchActivity;
import com.fise.xw.utils.IMUIHelper;

/**
 * @Description:gridview的Adapter
 * @author  九宫格
 */
public class DeviceGridAdapter extends BaseAdapter implements
		AdapterView.OnItemClickListener {
	private Context mContext;
	private UserEntity currentDevice;
	private DeviceEntity device;
	private UserEntity loginUser;
	public String[] img_text;
	 public int[] imgs;
	
/*
	public String[] img_text = { "位置信息", "静默监听", "设备记录", "历史轨迹 ", "通话", "同步数据" };
	public int[] imgs = { R.drawable.icon_dingwdh_jingrxw,
			R.drawable.icon_dingwdh_jingmjt, R.drawable.icon_dingwdh_baojg,
			R.drawable.icon_dingwdh_shilgj, R.drawable.icon_dingwdh_tongh,
			R.drawable.icon_dingwdh_tongbsj }; // R.color.transparent
			*/

	public DeviceGridAdapter(Context mContext, UserEntity currentDevice,
			DeviceEntity device,UserEntity loginUser,String[] img_text,int[] imgs) {
		super();
		this.mContext = mContext;
		this.currentDevice = currentDevice;
		this.device = device;
		this.loginUser = loginUser;
		
		this.img_text = img_text;
		this.imgs = imgs;
	 
		
	}

	public DeviceGridAdapter(Context mContext) {
		super();
		this.mContext = mContext;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return img_text.length;
	}   

	public void setCurrentDevice(UserEntity device) { 
		if(device!=null){
			this.currentDevice = device;
			notifyDataSetChanged();
		} 
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.grid_item, parent, false);
		}
		TextView tv = BaseViewHolder.get(convertView, R.id.tv_item);
		ImageView iv = BaseViewHolder.get(convertView, R.id.iv_item);
		iv.setBackgroundResource(imgs[position]);

		View left = BaseViewHolder.get(convertView, R.id.left);
		if (position == 0 || position == 3) {
			left.setVisibility(View.GONE);
		}
		tv.setText(img_text[position]);
		return convertView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long arg3) {
		// TODO Auto-generated method stub
		if (position == 0) {

			// 位置信息
			Intent intent = new Intent(mContext, PostionTouchActivity.class);
			intent.putExtra(IntentConstant.POSTION_LAT,
					currentDevice.getLatitude());
			intent.putExtra(IntentConstant.POSTION_LNG,
					currentDevice.getLongitude());
			intent.putExtra(IntentConstant.POSTION_TYPE, DBConstant.POSTION_DEV); 
			intent.putExtra(IntentConstant.DEV_USER_ID,currentDevice.getPeerId());

			mContext.startActivity(intent);
			  

		} else if (position == 1) {

			// 静默监听
			boolean listen = false;
//			if (device.getSilent() == 1) {
//				listen = true;
//			}

			if (listen) { 
				IMUserActionManager.instance().UserP2PCommand(
						IMLoginManager.instance().getLoginId(),
						currentDevice.getPeerId(),
						SessionType.SESSION_DEVICE_SINGLE, // SESSION_TYPE_SINGLE
						CommandType.COMMAND_TYPE_DEVICE_CALLBACK, loginUser.getPhone()+"",true);

			} else {
				Toast.makeText(mContext, "请开启静默监听", Toast.LENGTH_SHORT).show();
			}

		} else if (position == 2) { 
			IMUIHelper
					.openChatActivity(mContext, currentDevice.getSessionKey());

		} else if (position == 3) {

			// 历史轨迹
			IMUIHelper.openHistoryQueryActivity(mContext,
					currentDevice.getPeerId());

		} else if (position == 4) {
			// IMUIHelper.openDeviceActivity(mContext,DBConstant.MORE);
			String phone = currentDevice.getPhone();
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ phone));
			mContext.startActivity(intent);

			IMDeviceManager.instance().DeviceSendPhone(
					currentDevice.getPeerId(),
					AlarmType.ALARM_TYPE_AUTH_NORMAL_CALL);

		}else if(position == 5){
			//同步数据
			IMUserActionManager.instance().UserP2PCommand(
					IMLoginManager.instance().getLoginId(),
					currentDevice.getPeerId(),
					SessionType.SESSION_DEVICE_SINGLE, // SESSION_TYPE_SINGLE 
					CommandType.COMMAND_TYPE_DEVICE_CURRENT_INFO, "",true); 
			
		}else if(position == 6){
			//断电控制
			IMUserActionManager.instance().UserP2PCommand(
					IMLoginManager.instance().getLoginId(),
					currentDevice.getPeerId(),
					SessionType.SESSION_DEVICE_SINGLE, // 
					CommandType.COMMAND_TYPE_DEVICE_SHUTDOWN, "",true); 
		}
	}
}

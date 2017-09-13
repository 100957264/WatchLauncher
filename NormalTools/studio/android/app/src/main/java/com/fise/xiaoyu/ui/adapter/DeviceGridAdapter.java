package com.fise.xiaoyu.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.entity.TextMessage;
import com.fise.xiaoyu.imservice.manager.IMGroupManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.manager.IMUserActionManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMBaseDefine.CommandType;
import com.fise.xiaoyu.protobuf.IMBaseDefine.SessionType;
import com.fise.xiaoyu.ui.activity.PostionTouchActivity;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Utils;

/**
 * @Description:gridview的Adapter
 */
public class DeviceGridAdapter extends BaseAdapter implements
		AdapterView.OnItemClickListener {
	private Context mContext;
	private UserEntity currentDevice;
	private DeviceEntity device;
	private UserEntity loginUser;
	public String[] img_text;
    public int[] imgs;
	public   IMService imService;
/*
	public String[] img_text = { "位置信息", "静默监听", "设备记录", "历史轨迹 ", "通话", "同步数据" };
	public int[] imgs = { R.drawable.icon_dingwdh_jingrxw,
			R.drawable.icon_dingwdh_jingmjt, R.drawable.icon_dingwdh_baojg,
			R.drawable.icon_dingwdh_shilgj, R.drawable.icon_dingwdh_tongh,
			R.drawable.icon_dingwdh_tongbsj }; // R.color.transparent
			*/

	public DeviceGridAdapter(Context mContext, UserEntity currentDevice,
                             DeviceEntity device, UserEntity loginUser, IMService imService , String[] img_text, int[] imgs) {
		super();
		this.mContext = mContext;
		this.currentDevice = currentDevice;
		this.device = device;
		this.loginUser = loginUser;
		
		this.img_text = img_text;
		this.imgs = imgs;
        this.imService = imService;
		
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


		//小雨手机
		if (currentDevice.getUserType() == IMBaseDefine.ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE){
			tv.setTextColor(mContext.getResources().getColor(
					R.color.default_bk));


			ImageView right = BaseViewHolder.get(convertView, R.id.right);
			ImageView bottom = BaseViewHolder.get(convertView, R.id.bottom);
			tv.setTextColor(mContext.getResources().getColor(
					R.color.default_bk));

            right.setBackgroundResource(R.color.device_follow_line);
			bottom.setBackgroundResource(R.color.device_follow_line);
		}

		View right = BaseViewHolder.get(convertView, R.id.right);
		if (position == 2 || position == 5) {
            right.setVisibility(View.GONE);
		}
		tv.setText(img_text[position]);

		//宽度高度适配
		Activity test = (Activity) this.mContext;
		WindowManager wm = test.getWindowManager();
		int width = wm.getDefaultDisplay().getWidth();
		AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams((int)(width/2.8), (int)(width/2.8));//((int)(width/2.4), (int)(width/2.4));
		convertView.setLayoutParams(layoutParams);

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

			}else if (position == 4) {

				GroupEntity group = IMGroupManager.instance().findFamilyGroup(device.getFamilyGroupId());
				 if(group!=null){
					 IMUIHelper.openChatActivity(mContext, group.getSessionKey() );
				}

			} else if (position == 1) {
				//原历史轨迹改为安全围栏

			} else if (position == 3) {
				//通话
				String phone = currentDevice.getPhone();
				Intent intent =new Intent();
				intent.setAction(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:" +phone));
				mContext.startActivity(intent);

			}else if(position == 2){
				//同步数据
				if(currentDevice.getOnLine() == DBConstant.ONLINE) {
					String posContent = "更新" + currentDevice.getMainName() + "数据";
					sendTextToServer(posContent);
					IMUserActionManager.instance().UserP2PCommand(
							IMLoginManager.instance().getLoginId(),
							currentDevice.getPeerId(),
							SessionType.SESSION_DEVICE_SINGLE, // SESSION_TYPE_SINGLE
							CommandType.COMMAND_TYPE_DEVICE_CURRENT_INFO, "", true);
				}else{
					Utils.showToast(mContext , "设备不在线");
				}

			}else if(position == 5){

			  if (currentDevice.getUserType() == IMBaseDefine.ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE){

					IMUIHelper.openStepCounterActivity(mContext, currentDevice.getPeerId() );
				}else {
                    //回拨
					if(currentDevice.getOnLine() == DBConstant.ONLINE){
						String content = currentDevice.getMainName() + "电话回拨";
						sendTextToServer(content);
						IMUserActionManager.instance().UserP2PCommand(
								IMLoginManager.instance().getLoginId(),
								currentDevice.getPeerId(),
								SessionType.SESSION_DEVICE_SINGLE, // SESSION_TYPE_SINGLE
								CommandType.COMMAND_TYPE_DEVICE_CALLBACK, loginUser.getPhone() + "",true);
					}else{
						Utils.showToast(mContext , "设备不在线");
					}


                }

			}

	}

    private void sendTextToServer(String content) {
        GroupEntity group = IMGroupManager.instance().findFamilyGroup(device.getFamilyGroupId());
        PeerEntity peerEntity = imService.getSessionManager().findPeerEntity(
                group.getSessionKey());
        TextMessage posTextMessage = TextMessage.buildForSend(content,
                loginUser, peerEntity);
        imService.getMessageManager().sendText(posTextMessage);

    }

	/*
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
				Utils.showToast(mContext, "请开启静默监听");
			}
	 */
}

package com.fise.xw.ui.activity;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R; 
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.manager.IMDeviceManager;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.protobuf.IMDevice.SettingType;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.ui.widget.IMBaseImageView;
import com.fise.xw.ui.widget.PassDialog;
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;

/**
 *  设备设置界面 包含了设备的信息
 * @author weileiguan
 *
 */
public class DeviceInfoActivity extends TTBaseFragmentActivity {

	private DeviceInfoActivity activity;
	private static IMService imService;
	private IMContactManager contactMgr;
	private UserEntity currentUser;
	private int currentUserId;
	private DeviceEntity device;

//	private IMBaseImageView user_portrait;
//	private TextView remarksName;
//	private TextView userName;
//	private TextView modelName;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			currentUserId = DeviceInfoActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);

			if (currentUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);
			if (currentUser == null) {
				return;
			}

			device = imService.getDeviceManager().findDeviceCard(currentUserId);
			if (device == null) {
				return;
			}
 
			initDetailProfile();

			/*
			ArrayList<Integer> userIds = new ArrayList<>(1);
			// just single type
			userIds.add(currentUserId);
			imService.getContactManager().reqGetDetaillUsers(userIds);
			*/

		}

		@Override
		public void onServiceDisconnected() {

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.device_info_follow);
		imServiceConnector.connect(this);

		activity = this;
		EventBus.getDefault().register(this);

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceInfoActivity.this.finish();
			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceInfoActivity.this.finish();
			}
		});

//		user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);
//		remarksName = (TextView) findViewById(R.id.remarksName);
//		userName = (TextView) findViewById(R.id.userName);
//		modelName = (TextView) findViewById(R.id.modelName);

	}

	private void initDetailProfile() {
		hideProgressBar();

		
		/*
		setTextViewContent(R.id.remarksName, currentUser.getMainName());
		setTextViewContent(R.id.userName, "小位号: " + currentUser.getRealName());
	   	if(currentUser!=null){
    		if (currentUser.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE) {
    			setTextViewContent(R.id.modelName, "设备: " + "定位卡片机");
    			TextView black = (TextView) findViewById(R.id.black);
    			black.setText("定位卡片机");
    			
    			RelativeLayout speedlimit = (RelativeLayout) findViewById(R.id.speedlimit);
    			View speedlimit_line = (View) findViewById(R.id.speedlimit_line);
    			speedlimit_line.setVisibility(View.GONE);
    			speedlimit.setVisibility(View.GONE);
    			
    			
    			TextView speed_limit_text = (TextView) findViewById(R.id.speed_limit_text);
    			speed_limit_text.setText("");
    			
    			
    		}else if (currentUser.getUserType() == ClientType.CLIENT_TYPE_FISE_CAR_VALUE) {
    			setTextViewContent(R.id.modelName, "设备: " + "电动车");
    			TextView black = (TextView) findViewById(R.id.black);
    			black.setText("电动车");
    			
    			RelativeLayout speedlimit = (RelativeLayout) findViewById(R.id.speedlimit);
    			View speedlimit_line = (View) findViewById(R.id.speedlimit_line);
    			speedlimit_line.setVisibility(View.VISIBLE);
    			speedlimit.setVisibility(View.VISIBLE);
    			
    			TextView speed_limit_text = (TextView) findViewById(R.id.speed_limit_text);
    			speed_limit_text.setText("限制速度: " + rsp.getSpeedLimit());
    			
    			
    			speedlimit.setOnClickListener(new View.OnClickListener() {

    				public void onClick(View v) {
    					final PassDialog myDialog = new PassDialog(DeviceInfoActivity.this);
    			        myDialog.setTitle("设置限度");//设置标题
    			        myDialog.setMessage("请输入速度,速度为整数");//设置内容

    			        myDialog.dialog.show();//显示  
    			        

    			        //确认按键回调，按下确认后在此做处理
    			        myDialog.setMyDialogOnClick(new PassDialog.MyDialogOnClick() {
    			            @Override
    			            public void ok() {
    			                
    			               if(myDialog.getEditText().getText().toString().equals("")){
    			            	   Toast.makeText(DeviceInfoActivity.this, "请输入速度",
    										Toast.LENGTH_SHORT).show();
    			               }else{
    			            	   String numString = myDialog.getEditText().getText().toString();
    			            	   int num;
									try { // 如果转换异常则输入的不是数字
										num = Integer.parseInt(numString);
										if (num > 0) {
										
											
											IMDeviceManager.instance().settingWhite(currentUserId,numString,SettingType.SETTING_TYPE_SPEED_LIMIT,2);
											myDialog.dialog.dismiss(); 
											 
										} else{
											Toast.makeText(DeviceInfoActivity.this, "输入的速度不对,请重新再输入",
		    										Toast.LENGTH_SHORT).show();
										}
									} catch (Exception e) {
										Toast.makeText(DeviceInfoActivity.this, "输入的速度不对,请重新再输入",
	    										Toast.LENGTH_SHORT).show();
									}
    								
    			               }
    			            }
    			        });
    				}
    			});
    			
    		}
    	}  
	   	  
		IMBaseImageView user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);
		// user_portrait.setBackgroundResource(R.drawable.default_avatar_default);
		user_portrait.setImageUrl(currentUser.getAvatar());
		user_portrait.setCorner(90);

		if ((currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_WEI)
				&& (currentUser.getOnLine() != DBConstant.ONLINE)) {

			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(0);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
			user_portrait.setColorFilter(filter);
		} else {
			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(1);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
			user_portrait.setColorFilter(filter);
		}

		// 暂时隐藏
		// RelativeLayout device_info = (RelativeLayout)
		// findViewById(R.id.device_info);
		// device_info.setOnClickListener(new View.OnClickListener() {
		//
		// public void onClick(View v) {
		// IMUIHelper.openDeviceSettingActivity(DeviceInfoActivity.this,
		// currentUserId);
		// }
		// });
	*/
		// 安全围栏列表
		RelativeLayout electronic = (RelativeLayout) findViewById(R.id.electronic);
		electronic.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openElectronicListActivity(DeviceInfoActivity.this,
						currentUserId);
			}
		});

		// 白名单
		RelativeLayout white_list = (RelativeLayout) findViewById(R.id.white_list);
		white_list.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openWhiteListActivity(DeviceInfoActivity.this,
						currentUserId);
			}
		});

		// 紧急
		RelativeLayout emergency_call = (RelativeLayout) findViewById(R.id.emergency_call);
		emergency_call.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openAlarmListActivity(DeviceInfoActivity.this,
						currentUserId);
			}
		});

		// 报警管理
		RelativeLayout setting_police = (RelativeLayout) findViewById(R.id.setting_police);
		setting_police.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openWarningActivity(DeviceInfoActivity.this,
						currentUserId);

			}
		});

		// 亲情管理
		RelativeLayout setting_family = (RelativeLayout) findViewById(R.id.setting_family);
		setting_family.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openFamilyFollowActivity(DeviceInfoActivity.this,
						currentUserId);

			}
		});

		UserEntity loginContact = IMLoginManager.instance().getLoginInfo();
 

		// 获取CheckBox实例
		CheckBox listening = (CheckBox) this
				.findViewById(R.id.silent_listening_Checkbox);

		TextView silent_listening_text = (TextView) this
				.findViewById(R.id.silent_listening_text);

		ImageView silent_listening_default_arrow = (ImageView) this
				.findViewById(R.id.silent_listening_default_arrow);

		
		if (device.getMasterId() != loginContact.getPeerId()) {
			listening.setEnabled(false);
			listening.setVisibility(View.GONE);

			silent_listening_text.setVisibility(View.VISIBLE);
			silent_listening_default_arrow.setVisibility(View.VISIBLE);

			// if (device.getSilent() == 1) {
			//	silent_listening_text.setText("打开");
			// } else {
			//	silent_listening_text.setText("关闭");
			//}
		} else {

			listening.setVisibility(View.VISIBLE);

			silent_listening_text.setVisibility(View.GONE);
			silent_listening_default_arrow.setVisibility(View.GONE);
			

			boolean listen = false;
//			if (device.getSilent() == 1) {
//				listen = true;
//			}
			listening.setChecked(listen);
		}
		
		

		TextView show_bell_text = (TextView) this
				.findViewById(R.id.show_bell_text);
		if (device.getBellMode() == 1) {
			show_bell_text.setText("响铃");
		} else if (device.getBellMode() == 2) {
			show_bell_text.setText("振动");
		} else if (device.getBellMode() == 3) {
			show_bell_text.setText("响铃+振动");
		}

		RelativeLayout bell_mode = (RelativeLayout) this
				.findViewById(R.id.bell_mode);

		int showId = device.getMasterId();
		int loginId = loginContact.getPeerId();
		if (device.getMasterId() == loginContact.getPeerId()) {
			bell_mode.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					IMUIHelper.openDeviceBellsActivity(DeviceInfoActivity.this,
							currentUserId);
			
					
				}
			});

		} else {

			ImageView show_bell_arrow = (ImageView) this
					.findViewById(R.id.show_bell_arrow);
			show_bell_arrow.setVisibility(View.GONE);

		}

		// 绑定监听器
		listening.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				int Silent;
				if (arg1 == false) {
					Silent = 0;
				} else {
					Silent = 1;
				}
				 
    			
				imService.getDeviceManager().settingOpen(currentUserId, "",
						SettingType.SETTING_TYPE_LISTEN_MODE, Silent, device);

			}
		});

	 
		
		// 工作模式
	//	CheckBox working_mode = (CheckBox) this
		//		.findViewById(R.id.working_mode_Checkbox);

		TextView working_mode_text = (TextView) this
				.findViewById(R.id.working_mode_text);

		ImageView working_mode_default_arrow = (ImageView) this
				.findViewById(R.id.working_mode_default_arrow);

		if (device.getMode() == 1){
			working_mode_text.setText("普通模式");
		}else if (device.getMode() == 2){
			working_mode_text.setText("省电模式");
		}else if (device.getMode() == 3){
			working_mode_text.setText("休眠模式");
		}
		
		
		if (device.getMasterId() != loginContact.getPeerId()) { 
			working_mode_default_arrow.setVisibility(View.GONE);
		} else {  
			working_mode_default_arrow.setVisibility(View.VISIBLE); 
			
			//工作模式
			RelativeLayout working_mode = (RelativeLayout) findViewById(R.id.working_mode);
			working_mode.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) { 
					
					IMUIHelper.openDeviceWorkingModeActivity(DeviceInfoActivity.this,
							currentUserId);
				}
			});
		}
		
		

		
		
		/*
		boolean mode = false;
		if (rsp.getMode() == 1) {
			mode = true;
		}
		working_mode.setChecked(mode);

		// 绑定监听器
		working_mode.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				int isMode;
				if (arg1 == false) {
					isMode = 0;
				} else {
					isMode = 1;
				}
				imService.getDeviceManager().settingOpen(currentUserId, "",
						SettingType.SETTING_TYPE_WORK_MODE, isMode, rsp);

			}
		});

		if (rsp.getMasterId() != loginContact.getPeerId()) {
			working_mode.setEnabled(false);
			working_mode.setVisibility(View.GONE);

			working_mode_text.setVisibility(View.VISIBLE);
			working_mode_default_arrow.setVisibility(View.VISIBLE);

			if (rsp.getMode() == 1) {
				working_mode_text.setText("打开");
			} else {
				working_mode_text.setText("关闭");
			}
		} else {

			working_mode.setVisibility(View.VISIBLE);

			working_mode_text.setVisibility(View.GONE);
			working_mode_default_arrow.setVisibility(View.GONE);
		}
		*/
		
	

	}

	private void setTextViewContent(int id, String content) {
		TextView textView = (TextView) findViewById(id);
		if (textView == null) {
			return;
		}

		textView.setText(content);
	}

	private void hideProgressBar() {

		ProgressBar progressbar = (ProgressBar) findViewById(R.id.progress_bar);
		progressbar.setVisibility(View.GONE);
	}

	public void onEventMainThread(DeviceEvent event) {
		switch (event) {

		case USER_INFO_ADD_DEVICE_FAILED:
			break;
		case USER_INFO_UPDATE_DEVICE_SUCCESS:
		case USER_INFO_UPDATE_INFO_SUCCESS:
		case USER_INFO_SETTING_DEVICE_SUCCESS:
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);
			if (currentUser == null) {
				return;
			}
			device = imService.getDeviceManager().findDeviceCard(currentUserId);
			if (device != null) {
				initDetailProfile();
				 
			}
			break;
		case USER_INFO_SETTING_DEVICE_FAILED:
			break;
			
		case USER_INFO_SETTING_SPEED_LIMIT:
			Toast.makeText(DeviceInfoActivity.this, "限速设置成功",
					Toast.LENGTH_SHORT).show();
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);
			if (currentUser == null) {
				return;
			}
			device = imService.getDeviceManager().findDeviceCard(currentUserId);
			if (device != null) {
				initDetailProfile();
			}
			break; 

		}
	}

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_UPDATE:
			UserEntity entity = imService.getContactManager().findDeviceContact(
					currentUserId);
			
			if(entity == null){
				entity = imService.getContactManager().findContact(
						currentUserId);
			}
			if (entity != null) {
				currentUser = entity;
				initDetailProfile();
			}
			break;
		case USER_INFO_UPDATE_STAT:
			currentUser = imService.getContactManager().findDeviceContact(
					currentUserId);
			if (currentUser == null) {
				return;
			}
			device = imService.getDeviceManager().findDeviceCard(currentUserId);
			if (device != null) {
				initDetailProfile();
			}
			break; 
			

		}
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
		super.onDestroy();
	}
}

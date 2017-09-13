package com.fise.xiaoyu.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.device.entity.MobilePhoneDeviceEntity;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.ui.activity.AppMarketActivity;
import com.fise.xiaoyu.ui.activity.DevWebViewActivity;
import com.fise.xiaoyu.ui.activity.StepRankingActivity;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class DeviceListFragment extends MainFragment {

	private View curView = null;
	public LayoutInflater inflater;

	private static IMService imService;
	private String schoolId="";

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			List<DeviceEntity> deviceEntityList = imService.getDeviceManager().getDeviceRspContactList();
			for(int i=0;i<deviceEntityList.size();i++){
				if(deviceEntityList.get(i).getDevType() == IMBaseDefine.ClientType.CLIENT_TYPE_MOBILE_DEVICE.ordinal()){
					MobilePhoneDeviceEntity mobilePhoneEntity = MobilePhoneDeviceEntity.parseFromDB(deviceEntityList.get(i));
					int mSchoolId = mobilePhoneEntity.getSchool_id();
					String schoolString;
					if(i==(deviceEntityList.size() -1)){
						schoolString = mSchoolId + "";
					}else{
						schoolString = mSchoolId + ",";
					}

					schoolId = schoolId + schoolString;
				}
			}

		}

		@Override
		public void onServiceDisconnected() {
			if (EventBus.getDefault().isRegistered(DeviceListFragment.this)) {
				EventBus.getDefault().unregister(DeviceListFragment.this);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//EventBus粘性注册出现很奇怪的问题
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		this.inflater = inflater;
        		imServiceConnector.connect(this.getActivity());
		curView = inflater.inflate(R.layout.tt_fragment_find, null);


		RelativeLayout my_app_market = (RelativeLayout) curView.findViewById(R.id.my_app_market);
		my_app_market.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(DeviceListFragment.this.getActivity(), AppMarketActivity.class);
				DeviceListFragment.this.getActivity().startActivity(intent);
			}
		});



		//问答
		RelativeLayout interlocution = (RelativeLayout) curView.findViewById(R.id.interlocution);
		interlocution.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				String resultString = "http://192.168.2.101:8082/#/?";
				String type="";
				if(Utils.isClientType(IMLoginManager.instance().getLoginInfo())){
					type = "type=2&";
				}else{
					type = "type=1&";
				}

				String data = resultString + type + "schoolId=" +schoolId + "&userId=" + imService.getLoginManager().getLoginId();

				Intent intentUrl = new Intent(DeviceListFragment.this.getActivity(), DevWebViewActivity.class);
				intentUrl.putExtra(IntentConstant.WEB_URL, data);
				intentUrl.putExtra(IntentConstant.WEB_IS_RETURN, false);
				intentUrl.putExtra(IntentConstant.WEB_URL_RETURN, "返回");

				DeviceListFragment.this.getActivity().startActivity(intentUrl);
			}
		});


		RelativeLayout step_counter = (RelativeLayout) curView.findViewById(R.id.step_counter);
		step_counter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(DeviceListFragment.this.getActivity(), StepRankingActivity.class);
				DeviceListFragment.this.getActivity().startActivity(intent);
			}
		});

		return curView;
	}

	public void renderDeviceList()
	{

	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(LoginEvent event) {
		switch (event) {
			case LOCAL_LOGIN_MSG_SERVICE:
			case LOGIN_OK: {
				renderDeviceList();
			}
			break;
		}
	}
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(DeviceEvent event) {
		switch (event) {

		case USER_INFO_ADD_DEVICE_SUCCESS:
			break;
		case USER_INFO_UPDATE_INFO_SUCCESS:
			renderDeviceList();
			break;
		case USER_INFO_ADD_DEVICE_FAILED:
			// Utils.showToast(DeviceListActivity.this,
			// imService.getDeviceManager().getError());
			break;

		case USER_INFO_DELETE_DEVICE_SUCCESS:
			Utils.showToast(DeviceListFragment.this.getActivity(), "删除设备成功");
			renderDeviceList();
			break;

		case USER_INFO_DELETE_AUTH_SUCCESS:
			renderDeviceList();
			break;

		case USER_INFO_SETTING_DEVICE_SUCCESS:
			renderDeviceList();
			break;

		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DELETE_FAIL:
			Utils.showToast(DeviceListFragment.this.getActivity(), "删除设备失败");
			break;

//		case USER_P2PCOMMAND_OFFLINE_HINT:
//            RuntimeException here = new RuntimeException("here");
//            here.fillInStackTrace();
//            Log.w("aaa", "CallStackTrace: " + this, here);
//            Utils.showToast(this.getActivity(), "对方不在线");
//			break;
		case USER_INFO_UPDATE_STAT:
			renderDeviceList();
			break;
		}
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		imServiceConnector.disconnect(this.getActivity());
	}

	@Override
	protected void initHandler() {
		// TODO Auto-generated method stub

	}
}

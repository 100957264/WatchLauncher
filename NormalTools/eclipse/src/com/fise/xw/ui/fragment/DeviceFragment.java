package com.fise.xw.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.activity.DeviceInfoActivity;
import com.fise.xw.ui.adapter.DeviceGridAdapter;
import com.fise.xw.ui.base.TTBaseFragment;
import com.fise.xw.ui.widget.IMBaseImageView;
import com.fise.xw.ui.widget.MyGridView;
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;

@SuppressLint("NewApi")
public class DeviceFragment extends TTBaseFragment {
 
	private View curView = null;
	public LayoutInflater inflater;
	IMService imService;
	private MyGridView gridview;
	private DeviceGridAdapter adapter;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("detail#onIMServiceConnected");

			if (curView == null) {
				return;
			}
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				return;
			}
			initView();
		}

		@Override
		public void onServiceDisconnected() {

		}
	};

	public void initView() {
		IMBaseImageView user_portrait = (IMBaseImageView) curView
				.findViewById(R.id.user_portrait);
		user_portrait.setCorner(90);
		 
		
		if(imService!=null){ 
			UserEntity loginInfo = imService.getLoginManager().getLoginInfo();
			if(loginInfo!=null){
				user_portrait.setImageUrl(loginInfo.getAvatar());
			}
		} 
		
		
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		this.inflater = inflater;

		EventBus.getDefault().register(this);
		imServiceConnector.connect(getActivity()); 
		curView = inflater.inflate(R.layout.tt_fragment_device, null);

		gridview = (MyGridView) curView.findViewById(R.id.gridview);
		adapter = new DeviceGridAdapter(this.getActivity());
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(adapter);
		
		
		RelativeLayout telePhone = (RelativeLayout) curView
				.findViewById(R.id.telephone_layout);
		telePhone.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				IMUIHelper.openDeviceActivity(
						DeviceFragment.this.getActivity(), DBConstant.TELEPHONE);
			}
		});

		RelativeLayout children_watch = (RelativeLayout) curView
				.findViewById(R.id.children_watch_layout);
		children_watch.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				IMUIHelper.openDeviceActivity(
						DeviceFragment.this.getActivity(),
						DBConstant.CHILDRENWATCH);
			}
		});

		RelativeLayout electric_vehicle = (RelativeLayout) curView
				.findViewById(R.id.electric_vehicle_layout);
		electric_vehicle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				IMUIHelper.openDeviceActivity(
						DeviceFragment.this.getActivity(),
						DBConstant.ELECTRICVEHICLE);
			}
		});

		return curView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		imServiceConnector.disconnect(getActivity());
		EventBus.getDefault().unregister(this);

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void initHandler() {
		// TODO Auto-generated method stub

	}

	public void onEventMainThread(DeviceEvent event) {
		switch (event) {

		}
	}

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			initView();
			break;
		case USER_INFO_UPDATE:
			initView();
			break;

			   
		}
	}

}

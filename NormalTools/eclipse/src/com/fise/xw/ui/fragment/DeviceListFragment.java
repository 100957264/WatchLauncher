package com.fise.xw.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.manager.IMDeviceManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMDevice.ManageType;
import com.fise.xw.ui.adapter.DeviceAdspter;
import com.fise.xw.ui.base.TTBaseFragment;
import com.fise.xw.ui.menu.QrDevMenu;
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;

public class DeviceListFragment extends TTBaseFragment {

	private View curView = null;
	public LayoutInflater inflater;

	private ListView listView = null;
	private DeviceListFragment activity;
	private DeviceAdspter adapter;
	private static IMService imService;
	private IMContactManager contactMgr;
	private List<UserEntity> deviceList;
	private int deviceType;
	private ImageView icon_nodevice;

	private QrDevMenu qrDevMenu;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			deviceType = DeviceListFragment.this.getActivity().getIntent()
					.getIntExtra(IntentConstant.KEY_DEVICE_TYPE, 0);

			if (deviceType == DBConstant.TELEPHONE) {
				deviceList = imService.getDeviceManager().loadDevice(); // imService.getContactManager().getContactDevicesList();//
			} else if (deviceType == DBConstant.CAMERA) {
				deviceList = new ArrayList();
			} else if (deviceType == DBConstant.INTERCOM) {
				deviceList = new ArrayList();
			} else if (deviceType == DBConstant.CHILDRENWATCH) {
				deviceList = new ArrayList();
			} else if (deviceType == DBConstant.ELECTRICVEHICLE) {
				deviceList = new ArrayList();
			} else if (deviceType == DBConstant.VEHICLEDEVICE) {
				deviceList = new ArrayList();
			} else if (deviceType == DBConstant.NAVIGATOR) {
				deviceList = new ArrayList();
			} else if (deviceType == DBConstant.SMARTHOME) {
				deviceList = new ArrayList();
			} else if (deviceType == DBConstant.MORE) {
				deviceList = new ArrayList();
			}

			adapter = new DeviceAdspter(activity.getActivity(), deviceList,
					imService);
			listView.setAdapter(adapter);

			if (deviceList.size() <= 0) {
				icon_nodevice.setVisibility(View.VISIBLE);
			} else {
				icon_nodevice.setVisibility(View.GONE);
			}

			listView.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					// When clicked, show a toast with the TextView text
					Object object = deviceList.get(arg2);
					if (object instanceof UserEntity) {
						final UserEntity Entity = (UserEntity) object;

						AlertDialog.Builder builder = new AlertDialog.Builder(
								new ContextThemeWrapper(DeviceListFragment.this
										.getActivity(),
										android.R.style.Theme_Holo_Light_Dialog));
						String[] items = new String[] { DeviceListFragment.this
								.getString(R.string.delete_device) };

						builder.setItems(items,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										switch (which) {
										case 0:
											imService
													.getDeviceManager()
													.addDevice(
															Entity.getRealName(),
															ManageType.MANAGE_TYPE_DEL_DEVICE,
															Entity);
											// imService.getUserActionManager().deleteFriends(Entity.getPeerId(),
											// Entity);
											break;
										}
									}
								});
						AlertDialog alertDialog = builder.create();
						alertDialog.setCanceledOnTouchOutside(true);
						alertDialog.show();
					}
					return true;
				}
			});

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					Object object = deviceList.get(arg2);
					if (object instanceof UserEntity) {
						UserEntity Entity = (UserEntity) object;
						// IMUIHelper.openDeviceProfileActivity(
						// DeviceListActivity.this, Entity.getPeerId());
						// openDeviceInfoActivity

						IMUIHelper.openDeviceInfoActivity(
								DeviceListFragment.this.getActivity(),
								Entity.getPeerId());
					}

				}
			});

		}

		@Override
		public void onServiceDisconnected() {
			if (EventBus.getDefault().isRegistered(DeviceListFragment.this)) {
				EventBus.getDefault().unregister(DeviceListFragment.this);
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		this.inflater = inflater;

		EventBus.getDefault().registerSticky(DeviceListFragment.this);

		imServiceConnector.connect(this.getActivity());
		curView = inflater.inflate(R.layout.tt_activity_device_list, null);

		listView = (ListView) curView.findViewById(R.id.list_device);
		icon_nodevice = (ImageView) curView.findViewById(R.id.icon_nodevice);
		activity = this;

		qrDevMenu = new QrDevMenu(getActivity());
		qrDevMenu.addItems(new String[] { "扫描设备", "输入加设备" });

		Button addDevice = (Button) curView.findViewById(R.id.new_device_add);
		addDevice.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// IMUIHelper.openAddDeviceActivity(DeviceListFragment.this.getActivity());
				qrDevMenu.showAsDropDown(v);
			}
		});

		EditText text = (EditText) curView.findViewById(R.id.search_phone);
		text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openSearchFriendActivity(
						DeviceListFragment.this.getActivity(),
						DBConstant.SEACHDEVICE);
			}
		});

		return curView;
	}

	public void renderDeviceList() {

		deviceList = IMDeviceManager.instance().loadDevice(); // imService.getContactManager().getContactDevicesList();//

		// 没有任何的联系人数据
		// if (deviceList.size() <= 0) {
		// return;
		// }
		adapter.putDeviceList(deviceList);
		if (deviceList.size() <= 0) {
			icon_nodevice.setVisibility(View.VISIBLE);
		} else {
			icon_nodevice.setVisibility(View.GONE);
		}

	}

	public void onEventMainThread(DeviceEvent event) {
		switch (event) {

		case USER_INFO_ADD_DEVICE_SUCCESS:
			renderDeviceList();
			IMUIHelper
					.openDeviceSettingActivity(
							DeviceListFragment.this.getActivity(),
							IMDeviceManager.instance().getAddDeviceId());
			break;
		case USER_INFO_UPDATE_INFO_SUCCESS:
			renderDeviceList();
			break;
		case USER_INFO_ADD_DEVICE_FAILED:
			// Toast.makeText(DeviceListActivity.this,
			// imService.getDeviceManager().getError(), Toast.LENGTH_SHORT)
			// .show();
			break;

		case USER_INFO_DELETE_DEVICE_SUCCESS:
			Toast.makeText(DeviceListFragment.this.getActivity(), "删除设备成功",
					Toast.LENGTH_SHORT).show();
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

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DELETE_FAIL:
			Toast.makeText(DeviceListFragment.this.getActivity(), "删除设备失败",
					Toast.LENGTH_SHORT).show();
			break;

		case USER_P2PCOMMAND_OFFLINE:
			Toast.makeText(this.getActivity(), "对方不在线", Toast.LENGTH_SHORT)
					.show();
			break;
		case USER_INFO_UPDATE_STAT:
			renderDeviceList();
			break;
		}
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this.getActivity());
		imServiceConnector.disconnect(this.getActivity());
		super.onDestroy();
	}

	@Override
	protected void initHandler() {
		// TODO Auto-generated method stub

	}
}

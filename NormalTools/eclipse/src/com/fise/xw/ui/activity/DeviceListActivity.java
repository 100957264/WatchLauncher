package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMDevice.ManageType;
import com.fise.xw.ui.adapter.DeviceAdspter;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;



/**
 * 小位的 设备列表
 * @author weileiguan
 *
 */
public class DeviceListActivity extends TTBaseFragmentActivity {

	private ListView listView = null;
	private DeviceListActivity activity;
	private DeviceAdspter adapter;
	private static IMService imService;
	private IMContactManager contactMgr;
	private List<UserEntity> deviceList;
	private int deviceType;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			deviceType = DeviceListActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_DEVICE_TYPE, 0);

			if (deviceType == DBConstant.TELEPHONE) {
				deviceList = imService.getDeviceManager().loadDevice();
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

			adapter = new DeviceAdspter(activity, deviceList, imService);
			listView.setAdapter(adapter);

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
								new ContextThemeWrapper(
										DeviceListActivity.this,
										android.R.style.Theme_Holo_Light_Dialog));
						String[] items = new String[] { DeviceListActivity.this
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
								DeviceListActivity.this, Entity.getPeerId());
					}

				}
			});

		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_activity_device_list);
		imServiceConnector.connect(this);

		listView = (ListView) findViewById(R.id.list_device);

		activity = this;
		EventBus.getDefault().register(this);

		TextView gourp_name = (TextView) findViewById(R.id.device_name);
		gourp_name.setText("设备");

		TextView weiwang = (TextView) findViewById(R.id.weiwang);
		weiwang.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceListActivity.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceListActivity.this.finish();
			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DeviceListActivity.this.finish();
			}
		});

		Button addDevice = (Button) findViewById(R.id.new_device_add);
		addDevice.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openAddDeviceActivity(DeviceListActivity.this);
			}
		});

		EditText text = (EditText) findViewById(R.id.search_phone);
		text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openSearchFriendActivity(DeviceListActivity.this,
						DBConstant.SEACHDEVICE);
			}
		});

	}

	public void renderDeviceList() {

		deviceList = imService.getDeviceManager().loadDevice();

		// 没有任何的联系人数据
		// if (deviceList.size() <= 0) {
		// return;
		// }
		adapter.putDeviceList(deviceList);
	}

	public void onEventMainThread(DeviceEvent event) {
		switch (event) {

		case USER_INFO_ADD_DEVICE_SUCCESS:
		case USER_INFO_UPDATE_DEVICE_SUCCESS:
		case USER_INFO_UPDATE_INFO_SUCCESS:
			renderDeviceList();
			break;
		case USER_INFO_ADD_DEVICE_FAILED: 
			break;

		case USER_INFO_DELETE_DEVICE_SUCCESS:
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
			Toast.makeText(DeviceListActivity.this, "删除设备失败",
					Toast.LENGTH_SHORT).show();
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

package com.fise.xw.imservice.manager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import android.widget.Toast;

import com.fise.xw.DB.DBInterface; 
import com.fise.xw.DB.entity.DeviceCrontab; 
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.DeviceTrajectory;
import com.fise.xw.DB.entity.ElectricFenceEntity;
import com.fise.xw.DB.entity.FamilyConcernEntity;
import com.fise.xw.DB.entity.GroupEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.entity.WhiteEntity;
import com.fise.xw.app.IMApplication;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.MessageConstant;
import com.fise.xw.device.entity.ElectrombileDeviceEntity;
import com.fise.xw.imservice.callback.Packetlistener;
import com.fise.xw.imservice.entity.DevMessage;
import com.fise.xw.imservice.entity.RecentInfo;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.event.PriorityEvent;
import com.fise.xw.imservice.event.ReqFriendsEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.protobuf.IMBaseDefine;
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.protobuf.IMBaseDefine.DeviceAction;
import com.fise.xw.protobuf.IMBaseDefine.OperateType;
import com.fise.xw.protobuf.IMDevice;
import com.fise.xw.protobuf.IMDevice.AlarmType;
import com.fise.xw.protobuf.IMDevice.ConfigSyncMode;
import com.fise.xw.protobuf.IMDevice.ControlType;
import com.fise.xw.protobuf.IMDevice.DeviceAlarmRequest;
import com.fise.xw.protobuf.IMDevice.DeviceControl;
import com.fise.xw.protobuf.IMDevice.ElectricFence;
import com.fise.xw.protobuf.IMDevice.IMDeviceSyncNotice;
import com.fise.xw.protobuf.IMDevice.ManageType;
import com.fise.xw.protobuf.IMDevice.SettingType;
import com.fise.xw.protobuf.IMDevice.TaskType;
import com.fise.xw.protobuf.IMDevice.WatchConf;
import com.fise.xw.protobuf.helper.ProtoBuf2JavaBean;
import com.fise.xw.ui.activity.DeviceInfoActivity;
import com.fise.xw.utils.Logger;
import com.fise.xw.utils.Utils;
import com.fise.xw.utils.pinyin.PinYin;
import com.google.protobuf.CodedInputStream;

import de.greenrobot.event.EventBus;

/**
 * 负责用户信息的请求 为回话页面以及联系人页面提供服务
 * 
 * 联系人信息管理 普通用户的version 有总版本 群组没有总version的概念， 每个群有version 具体请参见 服务端具体的pd协议
 */
public class IMDeviceManager extends IMManager {
	private Logger logger = Logger.getLogger(IMDeviceManager.class);

	// 单例
	private static IMDeviceManager inst = new IMDeviceManager();

	public static IMDeviceManager instance() {
		return inst;
	}

	private IMSocketManager imSocketManager = IMSocketManager.instance();
	private DBInterface dbInterface = DBInterface.instance();
	private IMContactManager imContactManager = IMContactManager.instance();

	// 自身状态字段
	private boolean userDataReady = false;
	private Map<Integer, UserEntity> deiveMap = new ConcurrentHashMap<>();
	private String error_code = "连接失败";
	private String setting_phone_code = "设置失败";
	private String error_auth = "授权失败";

	private Map<Integer, UserEntity> authUserMap = new ConcurrentHashMap<>();
	private Map<Integer, DeviceEntity> deviceRspMap = new ConcurrentHashMap<>();

	private Map<Integer, ElectricFenceEntity> electriceMap = new ConcurrentHashMap<>();
	private Map<Integer, FamilyConcernEntity> familyConcernMap = new ConcurrentHashMap<>();
	// private List<FamilyConcernEntity> familyConcernList = new ArrayList<>();

	private Map<Integer, DeviceCrontab> crontabMap = new ConcurrentHashMap<>();

	List<WhiteEntity> whiteList = new ArrayList<>();
	List<WhiteEntity> alarmList = new ArrayList<>();
	private int addDevId;

	@Override
	public void doOnStart() {

	}

	/**
	 * 登陆成功触发 auto自动登陆
	 * */
	public void onNormalLoginOk() {
		onLocalLoginOk();
		onLocalNetOk();
	}

	/**
	 * 加载本地DB的状态 不管是离线还是在线登陆，loadFromDb 要运行的
	 */
	public void onLocalLoginOk() {

		List<UserEntity> userlist = dbInterface.loadAllDevice();
		for (UserEntity userInfo : userlist) {
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			PinYin.getPinYin(userInfo.getMainName(),
					userInfo.getPinyinElement());
			deiveMap.put(userInfo.getPeerId(), userInfo);
		}
		//

		List<FamilyConcernEntity> familyConcernList = dbInterface
				.loadAllFamilyConcern();

		for (FamilyConcernEntity familyInfo : familyConcernList) {
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			familyConcernMap.put(familyInfo.getPeeId(), familyInfo);
			// familyConcernList.add(familyInfo);
		}

		// 加载设备的任务
		List<DeviceCrontab> crontabList = dbInterface.loadAllDevTask();
		for (DeviceCrontab crontabInfo : crontabList) {
			crontabMap.put(crontabInfo.getTaskId(), crontabInfo);
		}

		LoaddAllAuthUser();
		LoaddAllWhite();
		LoaddAllAlarm();
		LoadDeviceRsp();
		LoadElectrice();

		triggerEvent(UserInfoEvent.USER_INFO_OK);
	}

	/**
	 * 获取群的配置信息
	 * 
	 * @param device_id
	 */
	public void DeviceConfigReq(int device_id) {

		int userId = IMLoginManager.instance().getLoginId();
		IMDevice.IMDeviceConfigReq imDeviceReq = IMDevice.IMDeviceConfigReq
				.newBuilder().setUserId(userId).setDeviceId(device_id)
				.setSyncMode(ConfigSyncMode.CONFIG_SYNC_ALL).build();
		int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
		int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_CONFIG_REQ_VALUE;
		imSocketManager.sendRequest(imDeviceReq, sid, cid);

	}

	/**
	 * 根据mode 获取对应的配置信息
	 * 
	 * @param device_id
	 * @param mode
	 */
	public void DeviceConfigTypeReq(int device_id, ConfigSyncMode mode) {

		int loginId = IMLoginManager.instance().getLoginId();
		IMDevice.IMDeviceConfigReq imDeviceReq = IMDevice.IMDeviceConfigReq
				.newBuilder().setUserId(loginId).setDeviceId(device_id)
				.setSyncMode(mode).build();
		int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
		int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_CONFIG_REQ_VALUE;
		imSocketManager.sendRequest(imDeviceReq, sid, cid);

	}

	/**
	 * 设备控制 例如关机报警
	 * 
	 * @param device_id
	 * @param type
	 */
	public void DeviceSendPhone(int device_id, AlarmType type) {

		int userId = IMLoginManager.instance().getLoginId();
		IMDevice.DeviceAlarmRequest imDeviceReq = IMDevice.DeviceAlarmRequest
				.newBuilder().setFromId(userId).setAlarmType(type)
				.setParam("" + device_id).build();
		int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
		int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_ALARM_REQ_VALUE; // CID_DEVICE_ALARM_REQ
		imSocketManager.sendRequest(imDeviceReq, sid, cid);

	}

	/**
	 * 设备通知回复
	 * 
	 * @param imResponse
	 */
	public void onRepDeviceSyncNoticeResponse(IMDeviceSyncNotice imResponse) {

		int userId = imResponse.getUserId();
		ConfigSyncMode mode = imResponse.getSyncMode();
		if (mode == ConfigSyncMode.CONFIG_SYNC_USERINFO) {

			ArrayList<Integer> userIds = new ArrayList<>(1);
			// just single type
			userIds.add(userId);
			IMContactManager.instance().reqGetDetaillUsers(userIds);

		} else if (mode == ConfigSyncMode.CONFIG_SYNC_ALL) {

			// 同步全部 受到这个通知特殊处理-表明设备被管理员删除（设备恢复初始状态，所有亲情人员需要删除设备，不再和设备有关系）
			UserEntity devInfo = IMContactManager.instance().findDeviceContact(
					userId);
			// dbInterface.insertOrDeleteDevice(devInfo);
			DeviceEntity rsp = IMDeviceManager.instance().findDeviceCard(
					devInfo.getPeerId());

			IMContactManager.instance().deleteDevUser(devInfo);

			if (rsp != null) {
				List<FamilyConcernEntity> list = IMDeviceManager.instance()
						.findFamilyConcern(devInfo.getPeerId());
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i) != null) {
						IMDeviceManager.instance().deleteFamilyConcernEntity(
								list.get(i));
					}
				}

			}

			List<WhiteEntity> whiteListTemp = getWhiteListContactList(devInfo
					.getPeerId());
			for (int i = 0; i < whiteListTemp.size(); i++) {
				if (whiteListTemp.get(i) != null) {
					IMDeviceManager.instance().deleteWhiteUser(
							whiteListTemp.get(i));
				}
			}

			List<WhiteEntity> alarmListTemp = getAlarmListContactList(devInfo
					.getPeerId());
			for (int i = 0; i < alarmListTemp.size(); i++) {
				if (alarmListTemp.get(i) != null) {
					IMDeviceManager.instance().deleteAlarmUser(
							alarmListTemp.get(i));
				}
			}

			LoaddAllWhite();
			LoaddAllAlarm();

			IMDeviceManager.instance().deleteFamilyConcernEntity();
			triggerEvent(DeviceEvent.USER_INFO_DELETE_DEVICE_SUCCESS);

		} else if (mode == ConfigSyncMode.CONFIG_SYNC_ALARM_MOBILE) {

			UserEntity currentDev = IMContactManager.instance()
					.findDeviceContact(userId);
			if (currentDev != null) {
				DeviceConfigTypeReq(userId, mode);
			}

		} else {
			UserEntity currentDev = IMContactManager.instance()
					.findDeviceContact(userId);
			if (currentDev != null) {
				DeviceConfigTypeReq(userId, mode);

			} else {
				ArrayList<Integer> userIds = new ArrayList<>(1);
				// just single type
				userIds.add(userId);
				IMContactManager.instance().reqGetDetaillUsers(userIds);
			}

		}
	}

	/**
	 * 设备记录信息通知
	 * 
	 * @param imResponse
	 */
	public void onRepDeviceAlarmResponse(DeviceAlarmRequest imResponse) {

		UserEntity user = IMContactManager.instance().findDeviceContact(
				imResponse.getFromId());

		if (imResponse.getAlarmType() == AlarmType.ALARM_TYPE_UPD_LOCATION) {

			if (user != null) {
				user.setLatitude(Double.valueOf(imResponse.getLat()));
				user.setLongitude(Double.valueOf(imResponse.getLng()));

				String param = imResponse.getParam();
				String[] sourceStrArray = param.split(",");
				for (int i = 0; i < sourceStrArray.length; i++) {
					if (i == 0) {
						user.setBattery(Integer.parseInt(sourceStrArray[i]));
					} else if (i == 1) {
						user.setSignal(Integer.parseInt(sourceStrArray[i]));
					}
				}

				IMContactManager.instance().updateOrDevice(user);
			}
 
		}

		UserEntity info;
		int sendId;

		String content = "安全提醒";
		if (imResponse.getParam().equals("")) {
			// info = user;//IMLoginManager.instance().getLoginInfo();
			info = IMLoginManager.instance().getLoginInfo();
			sendId = 0;
		} else if (imResponse.getAlarmType() == AlarmType.ALARM_TYPE_DEVICE_BILL) {
			info = IMLoginManager.instance().getLoginInfo();
			sendId = imResponse.getFromId();

			content = imResponse.getParam();
		} else {

			// 超出安全围栏和低电量报警
			if (imResponse.getAlarmType() == AlarmType.ALARM_TYPE_OUT_FENCE) {
				IMApplication.getPlaySound().mediaPlayer();
			}
			if (imResponse.getAlarmType() == AlarmType.ALARM_TYPE_LOW_BATTARY) {
				IMApplication.getPlaySound().mediaPlayer();
			}

			int param;
			if (isNumeric(imResponse.getParam())) {
				param = Integer.parseInt(imResponse.getParam());
			} else {
				param = imResponse.getFromId();
			}

			// info = IMContactManager.instance().findContact(param);
			// info = user;
			info = IMLoginManager.instance().getLoginInfo();
			sendId = param;
		}

		long timeLong = imResponse.getCreateTime();
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date(timeLong * 1000));

		DevMessage devMessage = DevMessage.buildForSend(content, user, info,
				imResponse.getAlarmType(), sendId, date);

		devMessage.setStatus(MessageConstant.MSG_SUCCESS);
		devMessage.setMsgId(imResponse.getActionId());
		long pkId = DBInterface.instance().insertOrUpdateMessage(devMessage);
		IMSessionManager.instance().updateSession(devMessage);

		devMessage.setAddressName(imResponse.getLat(), imResponse.getLng());

		/**
		 * 发送已读确认由上层的activity处理 特殊处理 1. 未读计数、 通知、session页面 2. 当前会话
		 * */
		PriorityEvent notifyEvent = new PriorityEvent();
		notifyEvent.event = PriorityEvent.Event.MSG_DEV_MESSAGE;
		notifyEvent.object = devMessage;
		triggerEvent(notifyEvent);

		IMMessageManager.instance().triggerEvent(
				UserInfoEvent.USER_INFO_DEV_DATA_SUCCESS);

	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	/**
	 * 设备配置信息回复数据
	 * 
	 * @param imDeviceRsp
	 */        
	public void onRepDeviceUsers(IMDevice.IMDeviceConfigRsp imDeviceRsp) {

		int deviceId = imDeviceRsp.getDeviceId();
		dbInterface.deleteOrUpdateAlarmAll();// 先删除报警号码
		dbInterface.deleteOrUpdateWhiteAll();// 先删除白名单号码
		dbInterface.removeAllFamily(); // 先删除
		familyConcernMap.clear();
		whiteList.clear();

		IMDevice.DeviceConfig config = imDeviceRsp.getBaseInfo(); 
		DeviceEntity info = null;
		
		if(imDeviceRsp.getClientType() == ClientType.CLIENT_TYPE_FISE_DEVICE){
			info = ProtoBuf2JavaBean.getDeviceCardEntity(deviceId,
					config.getMasterId(), imDeviceRsp.getClientType().ordinal(),config.getMobile(),
					config.getAlrBattery(), config.getAlrPoweroff(), config.getAlrCall(),
					config.getMode(), config.getBellMode(),
					config.getUpdated(), config.getFamilyGroupId()); //getFamilyGroupId
		}else if(imDeviceRsp.getClientType() == ClientType.CLIENT_TYPE_FISE_CAR){
			
//			info = ProtoBuf2JavaBean.getDeviceCarEntity(deviceId,
//					config.getMasterId(), imDeviceRsp.getClientType().ordinal(),config.getMobile(),
//					config.getAlrBattery(), config.getAlrPoweroff(), config.getAlrCall(),
//					config.getMode(), config.getBellMode(),
//					config.getUpdated(), config.getUpdated(),config.getSpeed(),config.getSpeedLimit());  
			
		}else if(imDeviceRsp.getClientType() == ClientType.CLIENT_TYPE_FISE_WATCH){
			
			WatchConf configWatch = imDeviceRsp.getWatchBase(); 
			info = ProtoBuf2JavaBean.getDeviceWatchEntity(deviceId,
					configWatch.getMasterId(), imDeviceRsp.getClientType().ordinal(),configWatch.getMobile(),
					configWatch.getAlrBattery(), configWatch.getAlrPoweroff(), configWatch.getAlrCall(),
					configWatch.getWorkMode(), configWatch.getBellMode(),
					configWatch.getUpdated(), configWatch.getFamilyGroupId(),configWatch.getLightTime());  
		}
 
		
		
		List<ElectricFence> electric = imDeviceRsp.getFenceListList(); 
		deleteElectriceAll();
		// 安全围栏
		for (int i = 0; i < electric.size(); i++) {
			ElectricFenceEntity electricEntity = ProtoBuf2JavaBean
					.getElectricFenceEntity(electric.get(i));
			updateElectrice(electricEntity);
		}

		if(info!=null){ 
			updateDeveiceRsp(info);
		}
		
		 
		//授权人信息
		List<DeviceControl> control_list = imDeviceRsp.getControlListList();
		for (int i = 0; i < control_list.size(); i++) {

			DeviceControl control = control_list.get(i);
			if (control.getAuthType() == ControlType.CONTROL_TYPE_FAMILY) {// 亲情关注
				int peerID = Integer.parseInt(control.getMobile());

				UserEntity userEntity = IMContactManager.instance()
						.findXiaoWeiContact(peerID);

				 
				// 授权人的信息
				if (userEntity == null) {
					userEntity = IMContactManager.instance()
							.findContact(peerID);
				}

				if (userEntity != null) {

					insertAuthUser(userEntity);
					if (findFamilyConcern(peerID, deviceId) == null) {

						String name;
						if (control.getName().equals("")) {
							name = userEntity.getMainName();
						} else {
							name = control.getName();
						}
						
						// int peeId,String familyConcern, String avatar,String
						// phone
						FamilyConcernEntity entity = ProtoBuf2JavaBean
								.getFindFamilyConcern(peerID, name,
										userEntity.getUserAvatar(),
										userEntity.getPhone(), deviceId);
						insertFamilyConcern(entity);
					} else {
						FamilyConcernEntity entity = findFamilyConcern(peerID,
								deviceId);
						String name;
						if (control.getName().equals("")) {
							name = userEntity.getMainName();
						} else {
							name = control.getName();
						}
						entity.setIdentity(name);
						insertFamilyConcern(entity);
					}

				}

			} else if (control.getAuthType() == ControlType.CONTROL_TYPE_ALLOW) {

				WhiteEntity whiteTemp = findWhiteList(control.getMobile(),
						deviceId);
				if (whiteTemp != null) {
					whiteTemp.setDevId(deviceId);
					whiteTemp.setPhone(control.getMobile());
					insertWhiteUser(whiteTemp);
				} else {
					WhiteEntity white = ProtoBuf2JavaBean.getPhoneEntity(
							control.getMobile(), deviceId);
					insertWhiteUser(white);
				}

			} else if (control.getAuthType() == ControlType.CONTROL_TYPE_ALARM) {

				WhiteEntity whiteTemp = findAlarmList(control.getMobile(),
						deviceId);
				if (whiteTemp != null) {
					whiteTemp.setDevId(deviceId);
					whiteTemp.setPhone(control.getMobile());
					insertAlarmList(whiteTemp);
				} else {

					WhiteEntity white = ProtoBuf2JavaBean.getPhoneEntity(
							control.getMobile(), deviceId);
					insertAlarmList(white);
				}

			}
		}

		if (imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ELECTIRC_FENCE) {
			triggerEvent(DeviceEvent.USER_INFO_ELECTIRC_FENCE);
		} else {
			triggerEvent(DeviceEvent.USER_INFO_UPDATE_INFO_SUCCESS);
		}

		
		
	}

	/**
	 * 加载授权人的信息
	 */
	public void LoaddAllAuthUser() {

		List<UserEntity> userlist = dbInterface.loadAllAuthUser();
		for (UserEntity userInfo : userlist) {
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			PinYin.getPinYin(userInfo.getMainName(),
					userInfo.getPinyinElement());
			authUserMap.put(userInfo.getPeerId(), userInfo);

		}
	}

	/**
	 * 加载本地的设备配置信息
	 */
	public void LoadDeviceRsp() {

		List<DeviceEntity> userlist = dbInterface.loadAllCard();
		for (DeviceEntity deviceInfo : userlist) {
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			deviceRspMap.put(deviceInfo.getDeviceId(), deviceInfo);
		}
	}

	/**
	 * 加载电子围栏数据
	 */
	public void LoadElectrice() {

		List<ElectricFenceEntity> userlist = dbInterface.loadAllElectricFence();
		for (ElectricFenceEntity deviceInfo : userlist) {
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			electriceMap.put(deviceInfo.getFenceId(), deviceInfo);
		}
	}

	/**
	 * 删除电子围栏数据
	 */
	public void deleteElectrice() {

		electriceMap.clear();
		dbInterface.deleteElectricFence();
	}

	/**
	 * 加载紧急号码
	 */
	public void LoaddAllAlarm() {

		List<WhiteEntity> userlist = dbInterface.loadAllAlarmList();
		for (WhiteEntity user : userlist) {
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			alarmList.add(user);
		}
	}

	/**
	 * 插入紧急号码
	 * 
	 * @param user
	 */
	public void insertAlarmList(WhiteEntity user) {

		if (user != null) {
			dbInterface.insertOrUpdateAlarmList(user);
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			alarmList = dbInterface.loadAllAlarmList();
			// alarmList.add(user);
		}
	}

	/**
	 * 删除紧急号码
	 * 
	 * @param user
	 */
	public void deleteAlarmList(WhiteEntity user) {

		if (user != null) {
			dbInterface.deleteOrUpdateAlarmList(user);
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			alarmList.remove(user);
		}
	}

	/**
	 * 加载白名单
	 */
	public void LoaddAllWhite() {

		List<WhiteEntity> userlist = dbInterface.loadAllWhiteList();
		for (WhiteEntity user : userlist) {
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			whiteList.add(user);
		}
	}

	/**
	 * 插入白名单
	 * 
	 * @param user
	 */
	public void insertWhiteUser(WhiteEntity user) {

		if (user != null) {
			dbInterface.insertOrUpdateWhiteList(user);
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			whiteList = dbInterface.loadAllWhiteList();
			// whiteList.add(user);
		}
	}

	/**
	 * 通过手机号和id查找白名单数据
	 * 
	 * @param phone
	 * @param devId
	 * @return
	 */
	public WhiteEntity findWhiteList(String phone, int devId) {

		WhiteEntity white = null;
		for (int i = 0; i < whiteList.size(); i++) {
			if ((whiteList.get(i).getDevId() == devId)
					&& (whiteList.get(i).getPhone().equals(phone))) {
				white = whiteList.get(i);
				break;
			}
		}
		return white;
	}

	/**
	 * 通过手机号和id查找紧急号码数据
	 * 
	 * @param phone
	 * @param devId
	 * @return
	 */
	public WhiteEntity findAlarmList(String phone, int devId) {

		WhiteEntity white = null;
		for (int i = 0; i < alarmList.size(); i++) {
			if ((alarmList.get(i).getDevId() == devId)
					&& (alarmList.get(i).getPhone().equals(phone))) {
				white = alarmList.get(i);
				break;
			}
		}
		return white;
	}

	/**
	 * 删除 白名单
	 * 
	 * @param user
	 */
	public void deleteWhiteUser(WhiteEntity user) {

		if (user != null) {
			dbInterface.deleteOrUpdateWhiteList(user);
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			whiteList.remove(user);
		}
	}

	/**
	 * 删除紧急号码
	 * 
	 * @param user
	 */
	public void deleteAlarmUser(WhiteEntity user) {

		if (user != null) {
			dbInterface.deleteOrUpdateAlarmList(user);
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			alarmList.remove(user);
		}
	}

	/**
	 * id 查找全部电子围栏数据
	 * 
	 * @param device_id
	 * @return
	 */
	public List<ElectricFenceEntity> findElectrice(int device_id) {
		List<ElectricFenceEntity> contactList = new ArrayList<>(
				electriceMap.values());
		List<ElectricFenceEntity> electriceList = new ArrayList<>();
		for (int i = 0; i < contactList.size(); i++) {
			if (contactList.get(i).getDeviceId() == device_id) {
				electriceList.add(contactList.get(i));
			}
		}
		return electriceList;
	}

	/**
	 * 通过 电子围栏ID找到对应的电子围栏
	 * 
	 * @param fenceId
	 * @return
	 */
	public ElectricFenceEntity findElectriceFence(int fenceId) {
		if (fenceId > 0 && electriceMap.containsKey(fenceId)) {
			return electriceMap.get(fenceId);
		}
		return null;
	}

	/**
	 * 插入授权人的数据
	 * 
	 * @param user
	 */
	public void insertAuthUser(UserEntity user) {

		if (user != null) {
			dbInterface.insertOrUpdateAuthUser(user);
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			PinYin.getPinYin(user.getMainName(), user.getPinyinElement());
			authUserMap.put(user.getPeerId(), user);
		}
	}

	/**
	 * 插入授权人的基本数据
	 * 
	 * @param user
	 */
	public void insertFamilyConcern(FamilyConcernEntity user) {

		if (user != null) {
			dbInterface.insertOrUpdatFamilyConcern(user);
			familyConcernMap.put(user.getPeeId(), user);
			// familyConcernList.add(user);
		}
	}

	/**
	 * 删除授权人的数据 和 Family基本数据
	 * 
	 * @param user
	 * @param Family
	 */
	public void deleteAuthUser(UserEntity user, FamilyConcernEntity Family) {

		if (user != null) {
			dbInterface.deleteOrUpdateAuthUser(user);
			authUserMap.remove(user.getPeerId());
		}

		if (Family != null) {
			dbInterface.deleteOrUpdateFamilyConcern(Family);
			familyConcernMap.remove(user.getPeerId());
			// familyConcernList.remove(Family);
		}
	}

	/**
	 * 删除授权人Family基本数据
	 * 
	 * @param Family
	 */
	public void deleteFamilyConcernEntity(FamilyConcernEntity Family) {

		if (Family != null) {
			dbInterface.deleteOrUpdateFamilyConcern(Family);
			// familyConcernList.remove(Family);
			familyConcernMap.remove(Family.getDevId());
		}
	}

	/**
	 * 情空授权人的 Family基本数据
	 */
	public void deleteFamilyConcernEntity() {
		familyConcernMap.clear();
		List<FamilyConcernEntity> familyConcernList = dbInterface
				.loadAllFamilyConcern();

		for (FamilyConcernEntity familyInfo : familyConcernList) {
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			// familyConcernList.add(familyInfo);
			familyConcernMap.put(familyInfo.getPeeId(), familyInfo);
		}

	}

	/**
	 * 更新设备的配置信息
	 * 
	 * @param rsp
	 */
	public void updateDeveiceRsp(DeviceEntity device) {

		if (device != null) {
			dbInterface.insertOrUpdateCard(device);
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			deviceRspMap.put(device.getDeviceId(), device);
		}
	}

	/**
	 * 更新电子围栏数据
	 * 
	 * @param rsp
	 */
	public void updateElectrice(ElectricFenceEntity rsp) {

		if (rsp != null) {
			dbInterface.insertOrUpdateElectricFence(rsp);
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			electriceMap.put(rsp.getFenceId(), rsp);
		}
	}

	/**
	 * 清空电子围栏数据
	 */
	public void deleteElectriceAll() {
		dbInterface.deleteElectricFenceAll();
		// todo DB的状态不包含拼音的，这个样每次都要加载啊
		electriceMap.clear();
	}

	/**
	 * 删除电子围栏数据
	 * 
	 * @param rsp
	 */
	public void deleteElectrice(ElectricFenceEntity rsp) {

		if (rsp != null) {
			dbInterface.deleteElectricFence(rsp);
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			electriceMap.remove(rsp.getFenceId());
			// electriceMap.put(rsp.getFenceId(), rsp);
		}
	}

	/**
	 * 加载本地DB的状态 不管是离线还是在线登陆，loadFromDb 要运行的
	 */
	public List<UserEntity> loadDevice() {
		logger.d("contact#reqWeilist");

		// List<DepartmentEntity> deptlist = dbInterface.loadAllDept();
		List<UserEntity> deviceList = dbInterface.loadAllDevice();

		return deviceList;
	}

	/**
	 * 加载全部设备信息
	 */
	public void updateAllFriends() {

		List<UserEntity> userlist = dbInterface.loadAllDevice();

		for (UserEntity userInfo : userlist) {
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			PinYin.getPinYin(userInfo.getMainName(),
					userInfo.getPinyinElement());
			deiveMap.put(userInfo.getPeerId(), userInfo);

		}
	}

	/**
	 * 网络链接成功，登陆之后请求
	 */
	public void onLocalNetOk() {
		// 部门信息
	}

	@Override
	public void reset() {
		userDataReady = false;
		deiveMap.clear();
		authUserMap.clear();
		whiteList.clear();
		alarmList.clear();
		deviceRspMap.clear();
		electriceMap.clear();
		familyConcernMap.clear();
		crontabMap.clear();
	}

	/**
	 * @param event
	 */
	public void triggerEvent(UserInfoEvent event) {
		// 先更新自身的状态
		switch (event) {
		case USER_INFO_OK:
			userDataReady = true;
			break;
		}
		EventBus.getDefault().postSticky(event);
	}

	/** -----------------------事件驱动---end--------- */

	/**
	 * 设置白名单号码
	 * 
	 * @param devId
	 * @param value
	 * @param type
	 * @param isAdd
	 * @param rsp
	 */
	public void settingOpen(int devId, String value, SettingType type,
			int isAdd, DeviceEntity device) {

		int loginId = IMLoginManager.instance().getLoginId(); // IMDeviceAddReq

		IMDevice.IMDeviceSettingReq settingReq = IMDevice.IMDeviceSettingReq
				.newBuilder().setUserId(loginId).setDeviceId(devId)
				.setSettingType(type).addValueList(value).setStatus(isAdd)
				.build();

		int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
		int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_SETTING_REQ_VALUE;

		final DeviceEntity showRsp = device;
		final String phone = value;
		final SettingType showType = type;
		final int mode = isAdd;
		imSocketManager.sendRequest(settingReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMDevice.IMDeviceSettingRsp imSettingRsp = IMDevice.IMDeviceSettingRsp
							.parseFrom((CodedInputStream) response);
					onRepMsgServerRspDevice(imSettingRsp, phone, showType,
							showRsp, mode);

				} catch (IOException e) {
					triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			}
		});
	}

	public void onRepMsgServerRspDevice(
			IMDevice.IMDeviceSettingRsp imSettingRsp, String phone,
			SettingType showType, DeviceEntity rsp, int open) {

		if (imSettingRsp == null) {
			logger.e("login#decode LoginResponse failed");
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			return;
		}

		IMBaseDefine.ResultType code = imSettingRsp.getResultCode();
		switch (code) {
		case REFUSE_REASON_NONE: {
			setting_phone_code = imSettingRsp.getResultString();

			if (showType == SettingType.SETTING_TYPE_ALLOW_MOBILE) {

				WhiteEntity whiteTemp = findWhiteList(phone, rsp.getDeviceId());
				if (whiteTemp != null) {
					whiteTemp.setDevId(rsp.getDeviceId());
					whiteTemp.setPhone(phone);
					insertWhiteUser(whiteTemp);
				} else {
					WhiteEntity white = ProtoBuf2JavaBean.getPhoneEntity(phone,
							rsp.getDeviceId());
					insertWhiteUser(white);
				}

			} else if (showType == SettingType.SETTING_TYPE_ALARM_MOBILE) {

				WhiteEntity whiteTemp = findAlarmList(phone, rsp.getDeviceId());
				if (whiteTemp != null) {
					whiteTemp.setDevId(rsp.getDeviceId());
					whiteTemp.setPhone(phone);
					insertAlarmList(whiteTemp);
				} else {
					WhiteEntity white = ProtoBuf2JavaBean.getPhoneEntity(phone,
							rsp.getDeviceId());
					insertAlarmList(white);
				}

			} else if (showType == SettingType.SETTING_TYPE_WORK_MODE) {
				rsp.setMode(open);
				updateDeveiceRsp(rsp);

			} else if (showType == SettingType.SETTING_TYPE_LISTEN_MODE) {
			//	rsp.setSilent(open);
			//	updateDeveiceRsp(rsp);
			} else if (showType == SettingType.SETTING_TYPE_ALARM_OPWEROFF) {
				rsp.setAlrPoweroff(open);
				updateDeveiceRsp(rsp);
			} else if (showType == SettingType.SETTING_TYPE_ALARM_BATTERY) {
				rsp.setAlrBattery(open);
				updateDeveiceRsp(rsp);
			} else if (showType == SettingType.SETTING_TYPE_ALARM_FENCE) {
				//rsp.setPen(open);
				//updateDeveiceRsp(rsp);
			} else if (showType == SettingType.SETTING_TYPE_BELL_MODE) {
				rsp.setBellMode(open);
				updateDeveiceRsp(rsp);
			} else if (showType == SettingType.SETTING_TYPE_ALARM_CALL) {
				rsp.setAlrCall(open);
				updateDeveiceRsp(rsp);
			}

			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_SUCCESS);

		}
			break;

		case REFUSE_REASON_DB_VALIDATE_FAILED: {
			setting_phone_code = imSettingRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
		}
			break;

		default: {
			setting_phone_code = imSettingRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
		}
			break;
		}
	}

	/**
	 * 删除白名单
	 * 
	 * @param devId
	 * @param white
	 * @param type
	 * @param status
	 */
	public void settingDeleteWhite(int devId, WhiteEntity white,
			SettingType type, int status) {

		int loginId = IMLoginManager.instance().getLoginId(); // IMDeviceAddReq

		IMDevice.IMDeviceSettingReq settingReq = IMDevice.IMDeviceSettingReq
				.newBuilder().setUserId(loginId).setDeviceId(devId)
				.setSettingType(type).addValueList(white.getPhone())
				.setStatus(status).build();

		int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
		int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_SETTING_REQ_VALUE;

		final WhiteEntity finalWhite = white;
		final SettingType showType = type;
		imSocketManager.sendRequest(settingReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMDevice.IMDeviceSettingRsp imSettingRsp = IMDevice.IMDeviceSettingRsp
							.parseFrom((CodedInputStream) response);
					onRepDeletePhoneDevice(imSettingRsp, finalWhite, showType);

				} catch (IOException e) {
					triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			}
		});
	}

	/**
	 * 删除配置信息回复
	 * 
	 * @param imSettingRsp
	 * @param finalWhite
	 * @param showType
	 */
	public void onRepDeletePhoneDevice(
			IMDevice.IMDeviceSettingRsp imSettingRsp, WhiteEntity finalWhite,
			SettingType showType) {
		logger.i("login#onRepMsgServerLogin");

		if (imSettingRsp == null) {
			logger.e("login#decode LoginResponse failed");
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			return;
		}

		IMBaseDefine.ResultType code = imSettingRsp.getResultCode();
		switch (code) {
		case REFUSE_REASON_NONE: {
			setting_phone_code = imSettingRsp.getResultString();

			if (showType == SettingType.SETTING_TYPE_ALLOW_MOBILE) {

				deleteWhiteUser(finalWhite);
				triggerEvent(DeviceEvent.USER_INFO_DELETE_WHITE_SUCCESS);

			} else if (showType == SettingType.SETTING_TYPE_ALARM_MOBILE) {
				deleteAlarmList(finalWhite);
				triggerEvent(DeviceEvent.USER_INFO_DELETE_ALARM_SUCCESS);
			} else {
				triggerEvent(DeviceEvent.USER_INFO_DELETE_DEVICE_SUCCESS);
			}

		}
			break;

		case REFUSE_REASON_DB_VALIDATE_FAILED: {
			setting_phone_code = imSettingRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
		}
			break;

		default: {
			setting_phone_code = imSettingRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
		}
			break;
		}
	}

	/**
	 * 设置白名单号码
	 * 
	 * @param devId
	 * @param value
	 * @param type
	 * @param isAdd
	 */
	public void settingWhite(int devId, String value, SettingType type,
			int isAdd) {

		int loginId = IMLoginManager.instance().getLoginId(); // IMDeviceAddReq

		IMDevice.IMDeviceSettingReq settingReq = IMDevice.IMDeviceSettingReq
				.newBuilder().setUserId(loginId).setDeviceId(devId)
				.setSettingType(type).addValueList(value).setStatus(isAdd)
				.build();

		int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
		int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_SETTING_REQ_VALUE;

		final String phone = value;
		final SettingType showType = type;
		imSocketManager.sendRequest(settingReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMDevice.IMDeviceSettingRsp imSettingRsp = IMDevice.IMDeviceSettingRsp
							.parseFrom((CodedInputStream) response);
					onRepMsgServerSettingDevice(imSettingRsp, phone, showType);

				} catch (IOException e) {
					triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			}
		});
	}

	/**
	 * 设置配置信息回复
	 * 
	 * @param imSettingRsp
	 * @param phone
	 * @param showType
	 */
	public void onRepMsgServerSettingDevice(
			IMDevice.IMDeviceSettingRsp imSettingRsp, String phone,
			SettingType showType) {
		logger.i("login#onRepMsgServerLogin");

		if (imSettingRsp == null) {
			logger.e("login#decode LoginResponse failed");
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			return;
		}

		IMBaseDefine.ResultType code = imSettingRsp.getResultCode();
		switch (code) {
		case REFUSE_REASON_NONE: {
			setting_phone_code = imSettingRsp.getResultString();

			if (showType == SettingType.SETTING_TYPE_ALLOW_MOBILE) {

				WhiteEntity whiteTemp = findWhiteList(phone,
						imSettingRsp.getDeviceId());
				if (whiteTemp != null) {
					whiteTemp.setDevId(imSettingRsp.getDeviceId());
					whiteTemp.setPhone(phone);
					insertWhiteUser(whiteTemp);
				} else {
					WhiteEntity white = ProtoBuf2JavaBean.getPhoneEntity(phone,
							imSettingRsp.getDeviceId());
					insertWhiteUser(white);
				}

			} else if (showType == SettingType.SETTING_TYPE_ALARM_MOBILE) {

				WhiteEntity whiteTemp = findAlarmList(phone,
						imSettingRsp.getDeviceId());
				if (whiteTemp != null) {
					whiteTemp.setDevId(imSettingRsp.getDeviceId());
					whiteTemp.setPhone(phone);
					insertAlarmList(whiteTemp);
				} else {
					WhiteEntity white = ProtoBuf2JavaBean.getPhoneEntity(phone,
							imSettingRsp.getDeviceId());
					insertAlarmList(white);
				}

			} else if (showType == SettingType.SETTING_TYPE_DEVICE_MOBILE) {
				UserEntity device = IMContactManager.instance()
						.findDeviceContact(imSettingRsp.getDeviceId());
				device.setPhone(phone);
				if (device != null) {
					IMContactManager.instance().updateOrDevice(device);
				}
			} else if (showType == SettingType.SETTING_TYPE_DEVICE_MOBILE) {
				UserEntity device = IMContactManager.instance()
						.findDeviceContact(imSettingRsp.getDeviceId());
				device.setPhone(phone);
				if (device != null) {
					IMContactManager.instance().updateOrDevice(device);
				}
			} else if (showType == SettingType.SETTING_TYPE_SPEED_LIMIT) { // 速度限制
				DeviceEntity entity;
				entity = IMDeviceManager.instance().findDeviceCard(
						imSettingRsp.getDeviceId());
				
				if (entity != null) {
					ElectrombileDeviceEntity device = ElectrombileDeviceEntity.parseFromDB(entity); 
					device.setSpeedLimit(Integer.parseInt(phone)); 
					device.setContent();
					IMDeviceManager.instance().updateDeveiceRsp(device);
				}
			}

			if (showType == SettingType.SETTING_TYPE_SPEED_LIMIT) {
				triggerEvent(DeviceEvent.USER_INFO_SETTING_SPEED_LIMIT);
			} else {
				triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_SUCCESS);
			}

		}
			break;

		case REFUSE_REASON_DB_VALIDATE_FAILED: {
			setting_phone_code = imSettingRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
		}
			break;

		default: {
			setting_phone_code = imSettingRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
		}
			break;
		}
	}

	/**
	 * 添加/修改/删除 设备电子围栏的数据
	 * 
	 * @param from_user
	 * @param dest_user
	 * @param status
	 * @param fence_id
	 * @param Lng
	 * @param Lat
	 * @param distance
	 * @param mark
	 * @param value
	 */
	public void settingElectronic(int from_user, final int dest_user,
			final int status, int fence_id, final String Lng, final String Lat,
			final int distance, final String mark, final int value) {

		IMDevice.SettingFenceReq settingReq = IMDevice.SettingFenceReq
				.newBuilder().setFromUser(from_user).setDestUser(dest_user)
				.setSetType(status).setFenceId(fence_id).setLng(Lng)
				.setLat(Lat).setRadius(distance).setMark(mark).setStatus(value)
				.build(); // 原status字段改名set_type (0-删除
							// 1-增加
							// 2修改)

		// CID_SETTING_FENCE_REQ
		int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
		int cid = IMBaseDefine.DeviceCmdID.CID_SETTING_FENCE_REQ_VALUE;

		imSocketManager.sendRequest(settingReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMDevice.SettingFenceRsp imSettingRsp = IMDevice.SettingFenceRsp
							.parseFrom((CodedInputStream) response);
					onRepMsgServerElectronicDevice(imSettingRsp, dest_user,
							status, Lng, Lat, distance, mark, 0, 0, value);

				} catch (IOException e) {
					triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			}
		});
	}

	/**
	 * 电子围栏数据 新增,修改,删除 回复数据
	 * 
	 * @param imSettingRsp
	 * @param DeviceId
	 * @param status
	 * @param Lng
	 * @param Lat
	 * @param radius
	 * @param Mark
	 * @param updated
	 * @param created
	 * @param value
	 */
	public void onRepMsgServerElectronicDevice(
			IMDevice.SettingFenceRsp imSettingRsp, int DeviceId, int status,
			String Lng, String Lat, int radius, String Mark, int updated,
			int created, int value) {

		if (imSettingRsp == null) {
			logger.e("login#decode LoginResponse failed");
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			return;
		}

		IMBaseDefine.ResultType code = imSettingRsp.getResultCode();

		switch (code) {
		case REFUSE_REASON_NONE: {
			setting_phone_code = imSettingRsp.getResultString();

			int fenceId = imSettingRsp.getFenceId();
			int tempStats = 0;
			if (value == 1) {
				tempStats = 1;
			} else if (value == 2) {
				tempStats = 2;
			} else {
				tempStats = status;
			}

			if (DBConstant.ELECTRONIC_DELETE == status) {
				ElectricFenceEntity electricEntity = findElectriceFence(fenceId);
				deleteElectrice(electricEntity);

			} else {
				ElectricFenceEntity electricEntity = findElectriceFence(fenceId);
				if (electricEntity == null) {
					electricEntity = ProtoBuf2JavaBean.getElectricFenceEntity(
							fenceId, DeviceId, tempStats, Lng, Lat, radius,
							Mark, updated, created);
				} else {
					electricEntity
							.setElectricFenceEntity(fenceId, DeviceId,
									tempStats, Lng, Lat, radius, Mark, updated,
									created);
				}
				updateElectrice(electricEntity);
			}

			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_SUCCESS);

		}
			break;

		default: {
			setting_phone_code = imSettingRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
		}
			break;
		}
	}

	/**
	 * 设置情亲称呼
	 * 
	 * @param devInfo
	 * @param userfo
	 * @param value
	 * @param type
	 * @param temp
	 */
	public void settingPhone(final UserEntity devInfo, UserEntity userfo,
			String value, SettingType type, String temp) {

		int loginId = IMLoginManager.instance().getLoginId(); // IMDeviceAddReq
		int stats;
		final UserEntity info;

		if (type == SettingType.SETTING_TYPE_FAMILIT_NAME) {
			stats = userfo.getPeerId();
			info = userfo;
		} else {
			stats = 2;
			info = devInfo;
		}

		IMDevice.IMDeviceSettingReq settingReq = IMDevice.IMDeviceSettingReq
				.newBuilder().setUserId(loginId)
				.setDeviceId(devInfo.getPeerId()).addValueList(value)
				// .setValueList(0,
				// //
				// value)
				.setSettingType(type).setStatus(stats).build();

		int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
		int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_SETTING_REQ_VALUE;

		final SettingType devType = type;
		String tempValue = null;
		if (type == SettingType.SETTING_TYPE_DEVICE_NICK) {
			tempValue = value;
		} else if (type == SettingType.SETTING_TYPE_DEVICE_AVATAR) {
			tempValue = temp;
		} else {
			tempValue = value;
		}

		final String devValue = tempValue;
		imSocketManager.sendRequest(settingReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMDevice.IMDeviceSettingRsp imSettingRsp = IMDevice.IMDeviceSettingRsp
							.parseFrom((CodedInputStream) response);
					onRepMsgSettingDevice(imSettingRsp, info, devType,
							devValue, devInfo);

				} catch (IOException e) {
					triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			}
		});

	}

	/**
	 * 情亲称呼 回复
	 * 
	 * @param imSettingRsp
	 * @param userInfo
	 * @param type
	 * @param value
	 * @param devInfo
	 */
	public void onRepMsgSettingDevice(IMDevice.IMDeviceSettingRsp imSettingRsp,
			UserEntity userInfo, final SettingType type, String value,
			UserEntity devInfo) {
		logger.i("login#onRepMsgServerLogin");

		if (imSettingRsp == null) {
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
			return;
		}

		IMBaseDefine.ResultType code = imSettingRsp.getResultCode();
		switch (code) {
		case REFUSE_REASON_NONE: {

			setting_phone_code = imSettingRsp.getResultString();
			if (type == SettingType.SETTING_TYPE_DEVICE_NICK) {
				userInfo.setMainName(value);
				IMContactManager.instance().updateOrDevice(userInfo);
			} else if (type == SettingType.SETTING_TYPE_DEVICE_AVATAR) {
				userInfo.setAvatar(value);
				IMContactManager.instance().updateOrDevice(userInfo);

			} else if (type == SettingType.SETTING_TYPE_USER_SEX) {
				userInfo.setGender(Integer.parseInt(value));
				IMContactManager.instance().updateOrDevice(userInfo);

			} else if (type == SettingType.SETTING_TYPE_FAMILIT_NAME) {

				int userID = userInfo.getPeerId();
				if (findFamilyConcern(userID, devInfo.getPeerId()) == null) {

					// int peeId,String familyConcern, String avatar,String
					FamilyConcernEntity entity = ProtoBuf2JavaBean
							.getFindFamilyConcern(userID, value,
									devInfo.getUserAvatar(),
									devInfo.getPhone(), devInfo.getPeerId());
					insertFamilyConcern(entity);
				} else {
					FamilyConcernEntity entity = findFamilyConcern(userID,
							devInfo.getPeerId());
					entity.setIdentity(value);
					insertFamilyConcern(entity);
				}
			}

			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_SUCCESS);

		}
			break;

		case REFUSE_REASON_DB_VALIDATE_FAILED: {
			setting_phone_code = imSettingRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
		}
			break;

		default: {
			setting_phone_code = imSettingRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_SETTING_DEVICE_FAILED);
		}
			break;
		}
	}

	/**
	 * 授权删除设备的请求
	 * 
	 * @param deviceId
	 * @param user
	 * @param family
	 */
	public void authDeleteDevice(String deviceId, UserEntity user,
			FamilyConcernEntity family) {

		int loginId = IMLoginManager.instance().getLoginId(); // IMDeviceAddReq

		IMDevice.IMDeviceManageReq deviceReq = IMDevice.IMDeviceManageReq
				.newBuilder().setFromId(loginId).setUserId(user.getPeerId())
				.setType(ManageType.MANAGE_TYPE_AUTH_DEL_DEVICE)
				.setAccount(deviceId).setDeviceSeq(deviceId).build();

		int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
		int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_MONITOR_REQ_VALUE;

		final UserEntity authUser = user;
		final FamilyConcernEntity Tempfamily = family;
		imSocketManager.sendRequest(deviceReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMDevice.IMDeviceManageRsp imDeviceRsp = IMDevice.IMDeviceManageRsp
							.parseFrom((CodedInputStream) response);
					onRepMsgServerAuthDeleteDevice(imDeviceRsp, authUser,
							Tempfamily);
				} catch (IOException e) {
					triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_FAILED);
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_OUT);
			}
		});

	}

	/**
	 * 删除授权人的回复
	 * 
	 * @param imDeviceRsp
	 * @param user
	 * @param Family
	 */
	public void onRepMsgServerAuthDeleteDevice(
			IMDevice.IMDeviceManageRsp imDeviceRsp, UserEntity user,
			FamilyConcernEntity Family) {
		logger.i("login#onRepMsgServerLogin");

		if (imDeviceRsp == null) {
			logger.e("login#decode LoginResponse failed");
			triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_FAILED);
			return;
		}

		IMBaseDefine.ResultType code = imDeviceRsp.getResultCode();
		switch (code) {
		case REFUSE_REASON_NONE: {
			deleteAuthUser(user, Family);
			triggerEvent(DeviceEvent.USER_INFO_DELETE_AUTH);

		}
			break;

		case REFUSE_REASON_DB_VALIDATE_FAILED: {
			error_auth = imDeviceRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_DELETE_DEVICE_FAILED);
		}
			break;

		default: {
			error_auth = imDeviceRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_DELETE_DEVICE_FAILED);
		}
			break;
		}
	}

	/**
	 * 获取设备获取行为信息
	 * 
	 * @param from_id
	 * @param device_id
	 * @param last_update
	 */
	public void deviceActionRequest(int from_id, int device_id, int last_update) {

		int loginId = IMLoginManager.instance().getLoginId(); // IMDeviceAddReq

		IMDevice.DeviceActionRequest deviceReq = IMDevice.DeviceActionRequest
				.newBuilder().setFromId(loginId).setDeviceId(device_id)
				.setLastUpdate(last_update).build();

		int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
		int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_ACTION_REQ_VALUE; // CID_DEVICE_MONITOR_REQ_VALUE

		imSocketManager.sendRequest(deviceReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMDevice.DeviceActionResponse imDeviceRsp = IMDevice.DeviceActionResponse
							.parseFrom((CodedInputStream) response);
					onDeviceActionResponse(imDeviceRsp);
				} catch (IOException e) {
					triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_FAILED);
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_OUT);
			}
		});

	}

	/**
	 * 获取行为信息 回复
	 * 
	 * @param imDeviceRsp
	 */
	public void onDeviceActionResponse(IMDevice.DeviceActionResponse imDeviceRsp) {
		logger.i("login#onRepMsgServerLogin");

		if (imDeviceRsp == null) {
			logger.e("login#decode LoginResponse failed");
			triggerEvent(DeviceEvent.USER_INFO_DEVICE_GUIJIN_FAILED);
			return;
		}

		List<DeviceAction> action = imDeviceRsp.getActionListList();
		List<DeviceTrajectory> trajectory = new ArrayList<>();
		for (int i = 0; i < action.size(); i++) {
			DeviceTrajectory device = ProtoBuf2JavaBean
					.getDeviceTrajectory(action.get(i));
			trajectory.add(device);
		}

		dbInterface.batchInsertOrUpdateTrajectoryList(trajectory);
		triggerEvent(DeviceEvent.USER_INFO_DEVICE_GUIJIN_SUCCESS);

		// IMBaseDefine.ResultType code = imDeviceRsp.getResultCode();

	}

	public List<DeviceTrajectory> getTrajectory() {
		return dbInterface.loadAllTrajectory();
	}

	/**
	 * 授权加设备的请求
	 * 
	 * @param devUser
	 * @param authId
	 * @param user
	 */
	public void authDevice(UserEntity devUser, int authId, UserEntity user) {

		int loginId = IMLoginManager.instance().getLoginId(); // IMDeviceAddReq

		IMDevice.IMDeviceManageReq deviceReq = IMDevice.IMDeviceManageReq
				.newBuilder().setFromId(loginId).setUserId(authId)
				.setType(ManageType.MANAGE_TYPE_AUTH_ADD_DEVICE)
				.setAccount(devUser.getRealName())
				.setDeviceSeq(devUser.getRealName()).build();

		int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
		int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_MONITOR_REQ_VALUE;

		final UserEntity authUser = user;
		final UserEntity tempUser = devUser;

		imSocketManager.sendRequest(deviceReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMDevice.IMDeviceManageRsp imDeviceRsp = IMDevice.IMDeviceManageRsp
							.parseFrom((CodedInputStream) response);
					onRepMsgServerAuthDevice(imDeviceRsp, authUser, tempUser);
				} catch (IOException e) {
					triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_FAILED);
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_OUT);
			}
		});

	}

	public String getAuthError() {
		return error_auth;
	}

	/**
	 * 请求加设备的请求
	 * 
	 * @param deviceId
	 * @param type
	 * @param devUser
	 */
	public void addDevice(String deviceId, ManageType type, UserEntity devUser) {

		int loginId = IMLoginManager.instance().getLoginId(); // IMDeviceAddReq

		IMDevice.IMDeviceManageReq deviceReq = IMDevice.IMDeviceManageReq
				.newBuilder().setFromId(loginId).setUserId(loginId)
				.setType(type).setAccount(deviceId).setDeviceSeq(deviceId)
				.build();

		int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
		int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_MONITOR_REQ_VALUE;
		final ManageType devType = type;
		final UserEntity devInfo = devUser;

		imSocketManager.sendRequest(deviceReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMDevice.IMDeviceManageRsp imDeviceRsp = IMDevice.IMDeviceManageRsp
							.parseFrom((CodedInputStream) response);
					onRepMsgServerAddDevice(imDeviceRsp, devType, devInfo);
				} catch (IOException e) {
					triggerEvent(DeviceEvent.USER_INFO_ADD_DEVICE_FAILED);
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(DeviceEvent.USER_INFO_ADD_DEVICE_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(DeviceEvent.USER_INFO_ADD_DEVICE_FAILED);
			}
		});

	}

	/**
	 * 添加设备回复
	 * 
	 * @param imDeviceRsp
	 * @param user
	 * @param devInfo
	 */
	public void onRepMsgServerAuthDevice(
			IMDevice.IMDeviceManageRsp imDeviceRsp, UserEntity user,
			UserEntity devInfo) {
		logger.i("login#onRepMsgServerLogin");

		if (imDeviceRsp == null) {
			logger.e("login#decode LoginResponse failed");
			triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_FAILED);
			return;
		}

		IMBaseDefine.ResultType code = imDeviceRsp.getResultCode();
		switch (code) {
		case REFUSE_REASON_NONE: {
			error_auth = "授权成功";
			insertAuthUser(user);

			int devID = devInfo.getPeerId();
			if (findFamilyConcern(devID, user.getPeerId()) == null) {

				// int peeId,String familyConcern, String avatar,String
				// phone
				FamilyConcernEntity entity = ProtoBuf2JavaBean
						.getFindFamilyConcern(user.getPeerId(),
								user.getMainName(), user.getUserAvatar(),
								user.getPhone(), devID);
				insertFamilyConcern(entity);
			} else {
				FamilyConcernEntity entity = findFamilyConcern(
						user.getPeerId(), devID);
				entity.setIdentity(user.getMainName());
				insertFamilyConcern(entity);
			}

			triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_SUCCESS);

		}
			break;

		case REFUSE_REASON_DB_VALIDATE_FAILED: {
			error_auth = imDeviceRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_FAILED);
		}
			break;

		default: {
			error_auth = imDeviceRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_AUTH_DEVICE_FAILED);
		}
			break;
		}
	}

	/**
	 * 添加设备回复
	 * 
	 * @param imDeviceRsp
	 * @param devType
	 * @param devInfo
	 */
	public void onRepMsgServerAddDevice(IMDevice.IMDeviceManageRsp imDeviceRsp,
			ManageType devType, UserEntity devInfo) {
		logger.i("login#onRepMsgServerLogin");

		if (imDeviceRsp == null) {
			logger.e("login#decode LoginResponse failed");
			triggerEvent(DeviceEvent.USER_INFO_ADD_DEVICE_FAILED);
			return;
		}

		IMBaseDefine.ResultType code = imDeviceRsp.getResultCode();
		switch (code) {
		case REFUSE_REASON_NONE: {

			if (devType == ManageType.MANAGE_TYPE_ADD_DEVICE) {
				IMBaseDefine.UserInfo userInfo = imDeviceRsp.getUserInfo();
				UserEntity userEntity = ProtoBuf2JavaBean
						.getUserEntity(userInfo);
				ArrayList<UserEntity> devicebReq = new ArrayList<>();
				
				if (Utils.isClientType(userEntity)) {
					
					devicebReq.add(userEntity); 
					DeviceConfigReq(userEntity.getPeerId());
				}
				 

				dbInterface.batchInsertOrUpdateDevice(devicebReq);
				IMContactManager.instance().updateDevice();
				addDevId = userInfo.getUserId();
				triggerEvent(DeviceEvent.USER_INFO_ADD_DEVICE_SUCCESS);

			} else if (devType == ManageType.MANAGE_TYPE_DEL_DEVICE) {

				if (Utils.isClientType(devInfo)) {

					// dbInterface.insertOrDeleteDevice(devInfo);
					DeviceEntity rsp = IMDeviceManager.instance()
							.findDeviceCard(devInfo.getPeerId());

					IMContactManager.instance().deleteDevUser(devInfo);

					if (rsp != null) {
						List<FamilyConcernEntity> list = IMDeviceManager
								.instance().findFamilyConcern(
										devInfo.getPeerId());
						for (int i = 0; i < list.size(); i++) {
							if (list.get(i) != null) {
								IMDeviceManager.instance()
										.deleteFamilyConcernEntity(list.get(i));
							}
						}

					}

					List<WhiteEntity> whiteListTemp = getWhiteListContactList(devInfo
							.getPeerId());
					for (int i = 0; i < whiteListTemp.size(); i++) {
						if (whiteListTemp.get(i) != null) {
							IMDeviceManager.instance().deleteWhiteUser(
									whiteListTemp.get(i));
						}
					}

					List<WhiteEntity> alarmListTemp = getAlarmListContactList(devInfo
							.getPeerId());
					for (int i = 0; i < alarmListTemp.size(); i++) {
						if (alarmListTemp.get(i) != null) {
							IMDeviceManager.instance().deleteAlarmUser(
									alarmListTemp.get(i));
						}
					}

					LoaddAllWhite();
					LoaddAllAlarm();

					IMDeviceManager.instance().deleteElectrice();
					IMDeviceManager.instance().deleteFamilyConcernEntity();
					triggerEvent(DeviceEvent.USER_INFO_DELETE_DEVICE_SUCCESS);

					// 删除设备的通知信息
					String session = devInfo.getSessionKey();
					List<RecentInfo> recentSessionList = IMSessionManager
							.instance().getRecentListInfo();
					for (int i = 0; i < recentSessionList.size(); i++) {
						if (recentSessionList.get(i).getSessionKey()
								.equals(session)) {
							IMSessionManager.instance().reqRemoveSession(
									recentSessionList.get(i),
									DBConstant.SESSION_ALL);
						}
					}
					triggerEvent(UserInfoEvent.USER_INFO_DELETE_SUCCESS); //

				} else {

					String session = devInfo.getSessionKey();
					if ((devInfo.getUserType() != ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE)
							&& (devInfo.getUserType() != ClientType.CLIENT_TYPE_FISE_CAR_VALUE)) {
						imContactManager.deleteFriends(devInfo);

						List<RecentInfo> recentSessionList = IMSessionManager
								.instance().getRecentListInfo();
						for (int i = 0; i < recentSessionList.size(); i++) {
							if (recentSessionList.get(i).getSessionKey()
									.equals(session)) {
								IMSessionManager.instance().reqRemoveSession(
										recentSessionList.get(i),
										DBConstant.SESSION_ALL);
							}
						}
						// triggerEvent(UserInfoEvent.USER_INFO_DATA_UPDATE); //
						triggerEvent(UserInfoEvent.USER_INFO_DELETE_SUCCESS); //
					}
				}

			}

		}
			break;

		case REFUSE_REASON_DB_VALIDATE_FAILED: {
			error_code = imDeviceRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_ADD_DEVICE_FAILED);
		}
			break;

		default: {
			error_code = imDeviceRsp.getResultString();
			triggerEvent(DeviceEvent.USER_INFO_ADD_DEVICE_FAILED);
		}
			break;
		}
	}

	/**
	 * 设置设备的任务/上课/爱心提醒
	 * 
	 * @param deviceId
	 * @param type
	 * @param devUser
	 */
	public void setDevTask(final int from_id, final int device_id,
			OperateType operate_type, final int task_id,
			final TaskType task_type, final String task_name,
			final String task_param, final long begin_time,
			final long end_time, final int status, final int repeat_mode,
			final String repeat_value) {

		IMDevice.SettingCrontabRequest deviceReq = IMDevice.SettingCrontabRequest
				.newBuilder().setFromId(from_id).setDeviceId(device_id)
				.setOperateType(operate_type).setTaskId(task_id)
				.setTaskType(task_type).setTaskName(task_name)
				.setTaskParam(task_param).setBeginTime(begin_time + "")
				.setEndTime(end_time + "").setStatus(status)
				.setRepeatMode(repeat_mode).setRepeatValue(repeat_value)
				.build();

		int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
		int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_CRONTAB_SETTING_REQ_VALUE;

		imSocketManager.sendRequest(deviceReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMDevice.SettingCrontabResponse imCrontabRsp = IMDevice.SettingCrontabResponse
							.parseFrom((CodedInputStream) response);
					onRepMsgCrontab(imCrontabRsp, from_id, device_id, task_id,
							task_type, task_name, task_param, begin_time,
							end_time, status, repeat_mode, repeat_value);

				} catch (IOException e) {
					triggerEvent(DeviceEvent.USER_INFO_CRONTAB_DEVICE_FAILED);

				}
			}

			@Override
			public void onFaild() {
				triggerEvent(DeviceEvent.USER_INFO_CRONTAB_DEVICE_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(DeviceEvent.USER_INFO_CRONTAB_DEVICE_FAILED);
			}
		});

	}
	 

	/**
	 * 添加设备回复
	 * 
	 * @param imDeviceRsp
	 * @param devType
	 * @param devInfo
	 */
	public void onRepMsgCrontab(IMDevice.SettingCrontabResponse imCrontabRsp,
			int from_id, int device_id, int task_id, TaskType task_type,
			String task_name, String task_param, long begin_time,
			long end_time, int status, int repeat_mode, String repeat_value) {

		if (imCrontabRsp == null) {

			triggerEvent(DeviceEvent.USER_INFO_CRONTAB_DEVICE_FAILED);
			return;
		}

		// 0是成功
		if (imCrontabRsp.getRetCode() == 0) {

			// 新增
			if (imCrontabRsp.getOperateType() == OperateType.OPERATE_TYPE_INSERT) {
 
				DeviceCrontab crontab = new DeviceCrontab(); 
				crontab.setTaskId(task_id);
				crontab.setDeviceId(device_id);
				crontab.setTaskType(task_type.ordinal());
				crontab.setTaskName(task_name);
				crontab.setTaskParam(task_param);
				crontab.setBeginTime(begin_time);
				crontab.setEndTime(end_time);
				crontab.setStatus(status);
				crontab.setRepeatMode(repeat_mode);
				crontab.setRepeatValue(repeat_value); 
				inserOrFriendsContact(crontab);
				
				triggerEvent(DeviceEvent.USER_INFO_CRONTAB_DEVICE_SUCCESS);
				
				// 修改
			} else if (imCrontabRsp.getOperateType() == OperateType.OPERATE_TYPE_UPDATE) {

				DeviceCrontab crontab = findCrontab(task_id);
				if (crontab != null) {

					crontab.setTaskId(task_id);
					crontab.setDeviceId(device_id);
					crontab.setTaskType(task_type.ordinal());
					crontab.setTaskName(task_name);
					crontab.setTaskParam(task_param);
					crontab.setBeginTime(begin_time);
					crontab.setEndTime(end_time);
					crontab.setStatus(status);
					crontab.setRepeatMode(repeat_mode);
					crontab.setRepeatValue(repeat_value);

					updateCrontab(crontab);

					triggerEvent(DeviceEvent.USER_INFO_CRONTAB_DEVICE_SUCCESS);

				}
				// 删除
			} else if (imCrontabRsp.getOperateType() == OperateType.OPERATE_TYPE_DELETE) {
				DeviceCrontab crontab = findCrontab(task_id);
				if (crontab != null) {
					deleteCrontab(crontab);
					triggerEvent(DeviceEvent.USER_INFO_CRONTAB_DEVICE_SUCCESS);
				}
			}
		} 
	}


	
	/**
	 * 插入任务信息(上课/爱心)模式
	 * 
	 * @param user
	 */
	public void inserOrFriendsContact(DeviceCrontab crontab) {

		if (crontab != null) {
			dbInterface.insertOrUpdateDevTask(crontab);
			crontabMap.put(crontab.getTaskId(), crontab);
		}
	}
	
	/**
	 * 更新任务配置
	 * 
	 * @param crontab
	 */
	public void updateCrontab(DeviceCrontab crontab) {

		if (crontab.getTaskId() > 0
				&& crontabMap.containsKey(crontab.getTaskId())) {
			crontabMap.put(crontab.getTaskId(), crontab);
			dbInterface.insertOrUpdateDevTask(crontab);
		}
	}

	/**
	 * 更新任务配置
	 * 
	 * @param crontab
	 */
	public void deleteCrontab(DeviceCrontab crontab) {

		if (crontab.getTaskId() > 0
				&& crontabMap.containsKey(crontab.getTaskId())) {
			crontabMap.remove(crontab.getTaskId());
			dbInterface.deleteOrUpdateDevTask(crontab);
		}
	}
	
	

	public String getError() {
		return error_code;
	}

	public String getPhoneCode() {
		return setting_phone_code;
	}

	public int getAddDeviceId() {
		return addDevId;
	}

	/**
	 * 自身的事件驱动
	 * 
	 * @param event
	 */
	public void triggerEvent(Object event) {
		EventBus.getDefault().post(event);
	}

	public UserEntity findContact(int buddyId) {
		if (buddyId > 0 && deiveMap.containsKey(buddyId)) {
			return deiveMap.get(buddyId);
		}
		return null;
	}

	public DeviceCrontab findCrontab(int taskId) {
		if (taskId > 0 && crontabMap.containsKey(taskId)) {
			return crontabMap.get(taskId);
		}
		return null;
	}

	/**
	 * 查找授权人的基本信息
	 * 
	 * @param buddyId
	 * @param devId
	 * @return
	 */
	public FamilyConcernEntity findFamilyConcern(int buddyId, int devId) {

		List<FamilyConcernEntity> familyConcernList = new ArrayList<>(
				familyConcernMap.values());

		FamilyConcernEntity entity = null;
		for (int i = 0; i < familyConcernList.size(); i++) {
			if (familyConcernList.get(i).getDevId() == devId
					&& familyConcernList.get(i).getPeeId() == buddyId) {
				entity = familyConcernList.get(i);
				break;
			}
		}
		return entity;
	}

	/**
	 * 查找全授权人的基本信息
	 * 
	 * @param devId
	 * @return
	 */
	public List<FamilyConcernEntity> findFamilyConcern(int devId) {

		List<FamilyConcernEntity> contactList = new ArrayList<>(
				familyConcernMap.values());
		List<FamilyConcernEntity> familyList = new ArrayList<>();
		for (int i = 0; i < contactList.size(); i++) {
			if (contactList.get(i).getDevId() == devId) {
				familyList.add(contactList.get(i));
			}
		}
		return familyList;
	}

	/**
	 * 查找设备的配置信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public DeviceEntity findDeviceCard(int deviceId) {
		if (deviceId > 0 && deviceRspMap.containsKey(deviceId)) {
			return deviceRspMap.get(deviceId);
		}
		return null;
	}

	/**
	 * 删除设备的配置信息
	 * 
	 * @param device
	 */
	public void deleteDeviceRsp(DeviceEntity device) {

		if (findDeviceCard(device.getDeviceId()) != null) {
			deviceRspMap.remove(device.getDeviceId());
			dbInterface.deleteOrUpdateCard(device);
		}
	}

	/**
	 * 获取全部设备数据
	 * 
	 * @return
	 */

	public List<UserEntity> getContactSortedList() {
		// todo eric efficiency
		List<UserEntity> contactList = new ArrayList<>(deiveMap.values());
		Collections.sort(contactList, new Comparator<UserEntity>() {
			@Override
			public int compare(UserEntity entity1, UserEntity entity2) {
				if (entity2.getPinyinElement().pinyin.startsWith("#")) {
					return -1;
				} else if (entity1.getPinyinElement().pinyin.startsWith("#")) {
					// todo eric guess: latter is > 0
					return 1;
				} else {
					if (entity1.getPinyinElement().pinyin == null) {
						PinYin.getPinYin(entity1.getMainName(),
								entity1.getPinyinElement());
					}
					if (entity2.getPinyinElement().pinyin == null) {
						PinYin.getPinYin(entity2.getMainName(),
								entity2.getPinyinElement());
					}
					return entity1.getPinyinElement().pinyin
							.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
				}
			}
		});
		return contactList;
	}

	/**
	 * 获取设备ID的白名单
	 * 
	 * @param devId
	 * @return
	 */
	public List<WhiteEntity> getWhiteListContactList(int devId) {
		List<WhiteEntity> devList = new ArrayList<>();

		for (int i = 0; i < whiteList.size(); i++) {
			if (whiteList.get(i).getDevId() == devId) {
				devList.add(whiteList.get(i));
			}
		}

		return devList;
	}

	/**
	 * 获取设备ID的紧急号码
	 * 
	 * @param devId
	 * @return
	 */
	public List<WhiteEntity> getAlarmListContactList(int devId) {

		List<WhiteEntity> devList = new ArrayList<>();

		for (int i = 0; i < alarmList.size(); i++) {
			if (alarmList.get(i).getDevId() == devId) {
				devList.add(alarmList.get(i));
			}
		}
		return devList;
	}

	public List<DeviceEntity> getDeviceRspContactList() {
		List<DeviceEntity> contactList = new ArrayList<>(
				deviceRspMap.values());
		return contactList;
	}

	/**
	 * 获取授权人列表的数据
	 * 
	 * @return
	 */
	public List<UserEntity> getAuthUserContactList() {

		List<UserEntity> contactList = new ArrayList<>(authUserMap.values());
		Collections.sort(contactList, new Comparator<UserEntity>() {
			@Override
			public int compare(UserEntity entity1, UserEntity entity2) {
				if (entity2.getPinyinElement().pinyin.startsWith("#")) {
					return -1;
				} else if (entity1.getPinyinElement().pinyin.startsWith("#")) {
					// todo eric guess: latter is > 0
					return 1;
				} else {
					if (entity1.getPinyinElement().pinyin == null) {
						PinYin.getPinYin(entity1.getMainName(),
								entity1.getPinyinElement());
					}
					if (entity2.getPinyinElement().pinyin == null) {
						PinYin.getPinYin(entity2.getMainName(),
								entity2.getPinyinElement());
					}
					return entity1.getPinyinElement().pinyin
							.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
				}
			}
		});
		return contactList;
	}

	/**
	 * 实现自身的事件驱动
	 * 
	 * @param event
	 */
	public void triggerReqEvent(ReqFriendsEvent event) {
		EventBus.getDefault().postSticky(event);
	}

	//
	// /**------------------------部门相关的协议 end------------------------------*/

	/** -----------------------实体 get set 定义----------------------------------- */

	public Map<Integer, UserEntity> getUserMap() {
		return deiveMap;
	}

	//
	public Map<Integer, UserEntity> getAuthUserMap() {
		return authUserMap;
	}

	public boolean isUserDataReady() {
		return userDataReady;
	}

}

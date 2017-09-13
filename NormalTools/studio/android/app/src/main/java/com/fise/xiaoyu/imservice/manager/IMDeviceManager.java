package com.fise.xiaoyu.imservice.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.fise.xiaoyu.DB.DBInterface;
import com.fise.xiaoyu.DB.entity.DeviceCrontab;
import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.DeviceTrajectory;
import com.fise.xiaoyu.DB.entity.FamilyConcernEntity;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.StepData;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.entity.WhiteEntity;
import com.fise.xiaoyu.app.IMApplication;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.MessageConstant;
import com.fise.xiaoyu.device.entity.MobilePhoneDeviceEntity;
import com.fise.xiaoyu.imservice.callback.Packetlistener;
import com.fise.xiaoyu.imservice.entity.DevMessage;
import com.fise.xiaoyu.imservice.entity.TextMessage;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.PriorityEvent;
import com.fise.xiaoyu.imservice.event.ReqFriendsEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.AlertReceiver;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ClientType;
import com.fise.xiaoyu.protobuf.IMBaseDefine.DeviceAction;
import com.fise.xiaoyu.protobuf.IMBaseDefine.OperateType;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.protobuf.IMDevice.AlarmType;
import com.fise.xiaoyu.protobuf.IMDevice.ConfigSyncMode;
import com.fise.xiaoyu.protobuf.IMDevice.ControlType;
import com.fise.xiaoyu.protobuf.IMDevice.DeviceAlarmRequest;
import com.fise.xiaoyu.protobuf.IMDevice.DeviceControl;
import com.fise.xiaoyu.protobuf.IMDevice.IMDeviceSyncNotice;
import com.fise.xiaoyu.protobuf.IMDevice.ManageType;
import com.fise.xiaoyu.protobuf.IMDevice.SettingType;
import com.fise.xiaoyu.protobuf.IMDevice.TaskType;
import com.fise.xiaoyu.protobuf.IMMessage;
import com.fise.xiaoyu.protobuf.helper.ProtoBuf2JavaBean;
import com.fise.xiaoyu.ui.activity.MainActivity;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;
import com.fise.xiaoyu.utils.pinyin.PinYin;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 负责用户信息的请求 为回话页面以及联系人页面提供服务
 * <p/>
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

    private Map<Integer, FamilyConcernEntity> familyConcernMap = new ConcurrentHashMap<>();
    // private List<FamilyConcernEntity> familyConcernList = new ArrayList<>();

    private Map<Integer, DeviceCrontab> crontabMap = new ConcurrentHashMap<>();
    private Map<String, StepData> stepDataMap = new ConcurrentHashMap<>();


    List<WhiteEntity> whiteList = new ArrayList<>();
    List<WhiteEntity> alarmList = new ArrayList<>();
    private int addDevId;

    private String verificationCode = "";//短信验证码值

    @Override
    public void doOnStart() {

    }

    /**
     * 登陆成功触发 auto自动登陆
     */
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


        // 加载设备的数据
        List<StepData> stepDataDaoList = dbInterface.loadAllStepData();
        for (StepData stepData : stepDataDaoList) {
            stepDataMap.put(stepData.getToday(), stepData);
        }


        // 固件版本
//		List<FirmwareInfo> firmwareList = dbInterface.loadAllFirmwareInfo();
//		for (FirmwareInfo firmwareInfo : firmwareList) {
//			firmwareMap.put(firmwareInfo.(), firmwareInfo);
//		}


        LoaddAllAuthUser();
        LoaddAllWhite();
        LoaddAllAlarm();
        LoadDeviceRsp();
        triggerEvent(UserInfoEvent.USER_INFO_OK);
    }

    /**
     * 获取的配置信息
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
        if (mode == ConfigSyncMode.CONFIG_SYNC_USERINFO) { //

            ArrayList<Integer> userIds = new ArrayList<>(1);
            // just single type
            userIds.add(userId);
            IMContactManager.instance().reqGetDetaillUsers(userIds);


            //管理员删除了设备 收到的通知
        } else if (mode == ConfigSyncMode.CONFIG_SYNC_ALL) {

            UserEntity devInfo = IMContactManager.instance().findDeviceContact(
                    userId);
            if (devInfo != null) {
                deleteDevice(devInfo);
            }

        } else if (mode == ConfigSyncMode.CONFIG_SYNC_ALARM_MOBILE) { //报警电话

            DeviceConfigTypeReq(imResponse.getUserId(), imResponse.getSyncMode());

            //取消关注 管理员收到
        } else if (mode == ConfigSyncMode.CONFIG_SYNC_FAMILY_MOBILE) {

            if (imResponse.getUserId() == IMLoginManager.instance().getLoginId()) {

                DeviceConfigTypeReq(imResponse.getUserId(), imResponse.getSyncMode());

            } else {
                UserEntity currentDev = IMContactManager.instance()
                        .findDeviceContact(userId);
                if (currentDev != null) {
                    DeviceConfigTypeReq(userId, mode);
                    DeviceEntity rsp = IMDeviceManager.instance().findDeviceCard(userId);
                    IMGroupManager.instance().reqGroupDetailInfo(rsp.getFamilyGroupId());
                }
            }

        } else if (mode == ConfigSyncMode.CONFIG_SYNC_USERINFO   //基本信息
                || mode == ConfigSyncMode.CONFIG_SYNC_ALLOW_MOBILE   //白名单
                || mode == ConfigSyncMode.CONFIG_SYNC_CRONTAB) { //设备任务

            DeviceConfigTypeReq(imResponse.getUserId(), imResponse.getSyncMode());

        } else {

            if (userId == IMLoginManager.instance().getLoginId()) {
                DeviceConfigTypeReq(userId, mode);
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
//			if (imResponse.getAlarmType() == AlarmType.ALARM_TYPE_OUT_FENCE) {
//				IMApplication.getPlaySound().mediaPlayer();
//			}
//			if (imResponse.getAlarmType() == AlarmType.ALARM_TYPE_LOW_BATTARY) {
//				IMApplication.getPlaySound().mediaPlayer();
//			}
//			if (imResponse.getAlarmType() == AlarmType.ALARM_TYPE_URGENCY) {
//				IMApplication.getPlaySound().mediaPlayer();
//			}

            int param;
            if (isNumeric(imResponse.getParam())) {
                param = Integer.parseInt(imResponse.getParam());
            } else {
                param = imResponse.getFromId();
            }

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

    public void deleteDevice(UserEntity devInfo) {

        DeviceEntity rsp = IMDeviceManager.instance().findDeviceCard(
                devInfo.getPeerId());

        GroupEntity devGroup = null;
        if (rsp != null) {
            List<FamilyConcernEntity> list = IMDeviceManager.instance()
                    .findFamilyConcern(devInfo.getPeerId());

            //查找家庭群
            devGroup = IMGroupManager.instance().findFamilyGroup(rsp.getFamilyGroupId());

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

        //删除家庭群
        if (devGroup != null) {
            //ceshi
            IMSessionManager.instance().reqRemoveSessionByKey(devGroup.getSessionKey());
            IMMessageManager.instance().deleteMessageAll(devGroup.getSessionKey());
            IMGroupManager.instance().deleteGroup(devGroup);

        }

        //删除会话窗口
        String session = devInfo.getSessionKey();
        IMSessionManager.instance().reqRemoveSessionByKey(session);
        IMContactManager.instance().deleteDevUser(devInfo);


        //gzc
        deleteAllCrontab(devInfo);
        LoaddAllWhite();
        LoaddAllAlarm();


        IMDeviceManager.instance().deleteFamilyConcernEntity();
        triggerEvent(DeviceEvent.USER_INFO_DELETE_DEVICE_SUCCESS);
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

        if (imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_FAMILY_MOBILE
                || imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ALL) {
            dbInterface.removeAllFamily(); // 亲情人员
            familyConcernMap.clear();

        }

        //报警消息
        if (imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ALARM_MOBILE
                || imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ALL) {
            dbInterface.deleteOrUpdateAlarmAll();// 先删除报警号码
            alarmList.clear();

        }

        //任务删除
        if (imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_CRONTAB
                || imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ALL) {
            dbInterface.deleteOrUpdateDevTask();// 先删除所有任务
            crontabMap.clear();

        }

        // 先删除白名单号码
        if (imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ALLOW_MOBILE
                || imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ALL) {
            dbInterface.deleteOrUpdateWhiteAll();
            whiteList.clear();

        }

        //只要同步全部或者基本信息 才解析基本数据
        if (imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_BASEINFO
                || imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ALL) {


            DeviceEntity info = null;
            IMDevice.ClientVersion clientVersion = imDeviceRsp.getVersionInfo();
            if (imDeviceRsp.getDeviceType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE) { //CLIENT_TYPE_FISE_DEVICE


                IMDevice.DeviceConfig config = null;
                try {
                    config = IMDevice.DeviceConfig
                            .parseFrom(imDeviceRsp.getBaseInfo());
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }

                int charge = config.getElectricize();
                info = ProtoBuf2JavaBean.getDeviceMobilePhoneEntity(deviceId,
                        config.getMasterId(), imDeviceRsp.getDeviceType().ordinal(), config.getMobile(),
                        config.getAlrBattery(), config.getAlrPoweroff(), config.getAlrCall(),
                        config.getMode(), config.getBellMode(),
                        config.getUpdated(), config.getFamilyGroupId(), config.getLightTime(), config.getStepMode(), clientVersion.getOldVersion(), clientVersion.getNewVersion(), clientVersion.getUpdateInfo(), clientVersion.getUpdateUrl(), charge, config.getSchoolid()); //getFamilyGroupId

                //　如果自己是设备
                if (imDeviceRsp.getDeviceId() == IMLoginManager.instance().getLoginId()) {

                    //响铃模式
                    int mode = 0;
                    if (config.getBellMode() == DBConstant.RINGER_MODE_SILENT) {
                        mode = AudioManager.RINGER_MODE_SILENT;
                    } else if (config.getBellMode() == DBConstant.RINGER_MODE_NORMAL) {
                        mode = AudioManager.RINGER_MODE_NORMAL;
                    } else if (config.getBellMode() == DBConstant.RINGER_MODE_VIBRATE) {
                        mode = AudioManager.RINGER_MODE_VIBRATE;
                    }


                    Intent audioIntent = new Intent("com.android.fise.ACTION_AUDIO_MODE");
                    Bundle bundle = new Bundle();
                    bundle.putInt("audiomode", mode);
                    audioIntent.putExtras(bundle);
                    IMApplication.getApplication().sendBroadcast(audioIntent);


                    //Settings.System.putInt(IMApplication.getApplication().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, config.getLightTime() * 1000);
                    Intent screenIntent = new Intent("com.android.fise.ACTION_SCREEN_TIMEOUT");
                    Bundle timeBundle = new Bundle();
                    timeBundle.putLong("screenofftimeout", config.getLightTime() * 1000);
                    screenIntent.putExtras(timeBundle);
                    IMApplication.getApplication().sendBroadcast(screenIntent);


                }
            }


            if (info != null) {
                if (info.getFamilyGroupId() != 0) {
                    GroupEntity entity = IMGroupManager.instance().findFamilyGroup(info.getFamilyGroupId());
                    if (entity == null) {
                        IMGroupManager.instance().reqGroupDetailInfo(info.getFamilyGroupId());
                    }
                }
            }

            if (info != null) {
                updateDeveiceConfigure(info);
            }
        }


        List<IMDevice.Crontab> crontabs = imDeviceRsp.getCrontabListList();
        for (int i = 0; i < crontabs.size(); i++) {

            IMDevice.Crontab crontab = crontabs.get(i);
            if (findCrontab(crontabs.get(i).getTaskId()) == null) {
                DeviceCrontab deviceCrontab = new DeviceCrontab();
                deviceCrontab.setTaskId(crontabs.get(i).getTaskId());
                deviceCrontab.setDeviceId(crontab.getDeviceId());
                deviceCrontab.setTaskType(crontab.getTaskType().ordinal());
                deviceCrontab.setTaskName(crontab.getTaskName());
                deviceCrontab.setTaskParam(crontab.getTaskParam());
                deviceCrontab.setBeginTime(crontab.getBeginTime());
                deviceCrontab.setEndTime(crontab.getEndTime());
                deviceCrontab.setStatus(crontab.getStatus());
                deviceCrontab.setRepeatValue(crontab.getRepeatValue());
                inserOrFriendsContact(deviceCrontab);
            } else {

                DeviceCrontab deviceCrontab = findCrontab(crontabs.get(i).getTaskId());
                deviceCrontab.setTaskId(crontabs.get(i).getTaskId());
                deviceCrontab.setDeviceId(crontab.getDeviceId());
                deviceCrontab.setTaskType(crontab.getTaskType().ordinal());
                deviceCrontab.setTaskName(crontab.getTaskName());
                deviceCrontab.setTaskParam(crontab.getTaskParam());
                deviceCrontab.setBeginTime(crontab.getBeginTime());
                deviceCrontab.setEndTime(crontab.getEndTime());
                deviceCrontab.setStatus(crontab.getStatus());
                deviceCrontab.setRepeatValue(crontab.getRepeatValue());
                inserOrFriendsContact(deviceCrontab);
            }
        }

        // 设备任务
        if (imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_CRONTAB
                || imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ALL) {
            if (imDeviceRsp.getDeviceId() == IMLoginManager.instance().getLoginId()) {
                //  处理任务
                SettingDeviceCrontab();
            }
        }

        //授权人信息
        List<DeviceControl> control_list = imDeviceRsp.getControlListList();
        for (int i = 0; i < control_list.size(); i++) {

            DeviceControl control = control_list.get(i);
            if (control.getAuthType() == ControlType.CONTROL_TYPE_FAMILY) {// 亲情关注
                int peerID = Integer.parseInt(control.getMobile());

                UserEntity userEntity = IMContactManager.instance().findFriendsContact(peerID);


                // 授权人的信息
                if (userEntity == null) {
                    userEntity = IMContactManager.instance()
                            .findContact(peerID);
                }

                if (userEntity != null) {

                    insertAuthUser(userEntity);
                    if (findFamilyConcern(peerID, deviceId) == null) {

                        //如果有备注显示备注
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
                            control.getMobile(), control.getName(), deviceId);
                    insertWhiteUser(white);
                }

            } else if (control.getAuthType() == ControlType.CONTROL_TYPE_ALARM) {  // sos 号码

                WhiteEntity whiteTemp = findAlarmList(control.getMobile(),
                        deviceId);
                if (whiteTemp != null) {
                    whiteTemp.setDevId(deviceId);
                    whiteTemp.setPhone(control.getMobile());
                    insertAlarmList(whiteTemp);
                } else {

                    WhiteEntity white = ProtoBuf2JavaBean.getPhoneEntity(
                            control.getMobile(), control.getName(), deviceId);
                    insertAlarmList(white);
                }

            }
        }

        //SOS
        if (imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ALARM_MOBILE
                || imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ALL) {

            //　如果自己是设备
            if (imDeviceRsp.getDeviceId() == IMLoginManager.instance().getLoginId()) {
                SettingSOSPhone();
            }
        }

        // 白名单号码
        if (imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ALLOW_MOBILE
                || imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ALL) {

            //　如果自己是设备
            if (imDeviceRsp.getDeviceId() == IMLoginManager.instance().getLoginId()) {
                SettingWhiteListPhone();
            }
        }


        if (imDeviceRsp.getSyncMode() == ConfigSyncMode.CONFIG_SYNC_ELECTIRC_FENCE) {
            triggerEvent(DeviceEvent.USER_INFO_ELECTIRC_FENCE);
        } else {
            triggerEvent(DeviceEvent.USER_INFO_UPDATE_INFO_SUCCESS);
        }

    }

    /**
     * 白名单
     */
    String numbers = "";

    public void SettingWhiteListPhone() {
        //mWhiteList 的变量phone 为设置号码
        List<WhiteEntity> mWhiteList = getWhiteListContactList(IMLoginManager.instance().getLoginId());
        WhiteEntity mWhiteEntity = null;

        for (WhiteEntity we : mWhiteList) {
            mWhiteEntity = we;
            numbers = numbers + mWhiteEntity.getPhone() + ",";
        }
        Log.d("IMDeviceManager", "numbers =" + numbers);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                com.fise.xiaoyu.utils.FileUtil.writeToSDFromInput("XiaoYu", "mumbers.txt", numbers);
            }
        });
    }


    /**
     * SOS 紧急号码
     */
    public void SettingSOSPhone() {
        //sosPhone 的变量phone 为设置号码
        List<WhiteEntity> sosPhone = getAlarmListContactList(IMLoginManager.instance().getLoginId());

    }

    /**
     * 设置闹钟爱心提醒和禁用
     */
    public void setAlarmAlert(int state, long startTime, long endTime, String loveName, ArrayList week, int type) {
        Calendar mCalendar = Calendar.getInstance();

        AlarmManager mAlarmManager = (AlarmManager) IMApplication.getApplication().getSystemService(Context.ALARM_SERVICE);
        if (type == IMDevice.TaskType.TASK_TYPE_LOVE_REMIND.ordinal()) {
            Intent alertIntent = new Intent(AlertReceiver.EVENT_ACTION_LOVE_ALERT);
            alertIntent.setClass(IMApplication.getApplication(), AlertReceiver.class);
            Bundle bundle = new Bundle();
            bundle.putString("love", loveName);
            bundle.putCharSequenceArrayList("weeklist", week);
            alertIntent.putExtras(bundle);
            PendingIntent pi = PendingIntent.getBroadcast(IMApplication.getApplication(), 0, alertIntent, 0);
            long currentTime = System.currentTimeMillis();
            mCalendar.setTimeInMillis(startTime);
            mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            if (currentTime > mCalendar.getTimeInMillis()) {
                mCalendar.add(Calendar.DAY_OF_MONTH, 1);//如果当前时间大于设置时间，从第二天开始
            }
            if (state == 1) {
                mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), 1000 * 60 * 60 * 24, pi);
            } else {
                mAlarmManager.cancel(pi);
            }
        } else if (type == IMDevice.TaskType.TASK_TYPE_LESSION_MODE.ordinal()) {
            Intent forbidEnable = new Intent(AlertReceiver.EVENT_ACTION_FORBID_EBALE);
            forbidEnable.setClass(IMApplication.getApplication(), AlertReceiver.class);
            Bundle bundle = new Bundle();
            bundle.putCharSequenceArrayList("forbid_week", week);
            forbidEnable.putExtras(bundle);
            PendingIntent enable = PendingIntent.getBroadcast(IMApplication.getApplication(), 0, forbidEnable, 0);

            Intent forbidDisable = new Intent(AlertReceiver.EVENT_ACTION_FORBID_DISABLE);
            forbidDisable.setClass(IMApplication.getApplication(), AlertReceiver.class);
            forbidDisable.putExtras(bundle);
            PendingIntent disable = PendingIntent.getBroadcast(IMApplication.getApplication(), 0, forbidDisable, 0);
            if (state == 1) {
                long currentTime = System.currentTimeMillis();
                mCalendar.setTimeInMillis(startTime);
                mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                Calendar mCalenderDisable = Calendar.getInstance();
                mCalenderDisable.setTimeInMillis(endTime);
                mCalenderDisable.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                if (currentTime > mCalenderDisable.getTimeInMillis()) {
                    mCalendar.add(Calendar.DAY_OF_MONTH, 1);//如果当前时间大于设置时间，从第二天开始
                    mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), 1000 * 60 * 60 * 24, enable);
                } else if (currentTime > mCalendar.getTimeInMillis() && currentTime < mCalenderDisable.getTimeInMillis()) {
                    mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60 * 24, enable);
                } else {
                    mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), 1000 * 60 * 60 * 24, enable);
                }
                if (currentTime > mCalenderDisable.getTimeInMillis()) {
                    mCalenderDisable.add(Calendar.DAY_OF_MONTH, 1);
                }
                mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalenderDisable.getTimeInMillis(), 1000 * 60 * 60 * 24, disable);
            } else {
                mAlarmManager.cancel(enable);
                mAlarmManager.cancel(disable);
            }

        }
    }

    /**
     * 任务处理  爱心提醒
     */
    public void SettingDeviceCrontab() {

        //时间为时间戳
        ArrayList<DeviceCrontab> allCrotabList = getAllCrotabList(IMLoginManager.instance().getLoginId());
        for (int i = 0; i < allCrotabList.size(); i++) {
            if (allCrotabList.get(i).getTaskType() == IMDevice.TaskType.TASK_TYPE_LESSION_MODE.ordinal()) {// 上课禁用

                String begin_time = allCrotabList.get(i).getBeginTime();
                String end_time = allCrotabList.get(i).getEndTime();

                String name = allCrotabList.get(i).getTaskName(); //上课的名字
                String[] timeList = begin_time.split(",");


//                long morningStart = 0;//= Long.parseLong(timeList[0]);  //上午的上课时间
//                String timeString = "2017年10月17日 " + timeList[0];
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
//                Date d;
//                try {
//                    d = sdf.parse(timeString);
//                    morningStart = d.getTime() / 1000;
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }

                String[] morning = timeList[0].split(":");
                int hour = Integer.parseInt(morning[0]);
                int minute = Integer.parseInt(morning[1]);


                Calendar mCalendar = Calendar.getInstance();
                mCalendar.setTimeInMillis(System.currentTimeMillis());

                //是设置日历的时间，主要是让日历的年月日和当前同步
                mCalendar.setTimeInMillis(System.currentTimeMillis());
                // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
                mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                //设置在几点提醒  设置的为13点
                mCalendar.set(Calendar.HOUR_OF_DAY, hour);
                //设置在几分提醒  设置的为25分
                mCalendar.set(Calendar.MINUTE, minute);
                mCalendar.set(Calendar.SECOND, 0);
                long morningStart = mCalendar.getTimeInMillis();




              //  long AfternoonStart = 0;//Long.parseLong(timeList[1]); //下午的上课时间
//                String timeAfternoon = "2017年10月17日 " + timeList[1];
//                SimpleDateFormat sdfAfternoon = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
//                Date afternoon;
//                try {
//                    afternoon = sdfAfternoon.parse(timeAfternoon);
//                    AfternoonStart = afternoon.getTime() / 1000;
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }

                String[] afternoon = timeList[1].split(":");
                int afternoonHour = Integer.parseInt(afternoon[0]);
                int afternoonMinute = Integer.parseInt(afternoon[1]);


                Calendar mCalendarAfternoon = Calendar.getInstance();
                //是设置日历的时间，主要是让日历的年月日和当前同步
                mCalendarAfternoon.setTimeInMillis(System.currentTimeMillis());
                // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
                mCalendarAfternoon.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                //设置在几点提醒  设置的为13点
                mCalendarAfternoon.set(Calendar.HOUR_OF_DAY, afternoonHour);
                //设置在几分提醒  设置的为25分
                mCalendarAfternoon.set(Calendar.MINUTE, afternoonMinute);
                mCalendarAfternoon.set(Calendar.SECOND, 0);
                long AfternoonStart = mCalendarAfternoon.getTimeInMillis();



                String[] endTimeList = end_time.split(",");
         //       long morningEnd = 0;//Long.parseLong(timeList[0]);  //上午的下课时间
//                String timeMorningEnd = "2017年10月17日 " + endTimeList[0];
//                SimpleDateFormat sdfMorningEnd = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
//                Date morningEndData;
//                try {
//                    morningEndData = sdfMorningEnd.parse(timeMorningEnd);
//                    morningEnd = morningEndData.getTime() / 1000;
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }

                String[] morningEndString = endTimeList[0].split(":");
                int morningEndHour = Integer.parseInt(morningEndString[0]);
                int morningEndMinute = Integer.parseInt(morningEndString[1]);


                Calendar mCalendarMorningEnd = Calendar.getInstance();
                //是设置日历的时间，主要是让日历的年月日和当前同步
                mCalendarMorningEnd.setTimeInMillis(System.currentTimeMillis());
                // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
                mCalendarMorningEnd.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                //设置在几点提醒  设置的为13点
                mCalendarMorningEnd.set(Calendar.HOUR_OF_DAY, morningEndHour);
                //设置在几分提醒  设置的为25分
                mCalendarMorningEnd.set(Calendar.MINUTE, morningEndMinute);
                mCalendarMorningEnd.set(Calendar.SECOND, 0);
                long morningEnd = mCalendarMorningEnd.getTimeInMillis();






//                long afternoonEnd = 0;//Long.parseLong(endTimeList[1]); //下午的下课时间
//                String timeAfternoonEnd = "2017年10月17日 " + endTimeList[1];
//                SimpleDateFormat sdfAfternoonEnd = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
//                Date afternoonEndData;
//                try {
//                    afternoonEndData = sdfAfternoonEnd.parse(timeAfternoonEnd);
//                    afternoonEnd = afternoonEndData.getTime() / 1000;
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }

                String[] afternoonEndString = endTimeList[1].split(":");
                int afternoonEndHour = Integer.parseInt(afternoonEndString[0]);
                int afternoonEndMinute = Integer.parseInt(afternoonEndString[1]);


                Calendar mCalendarAfternoonEnd = Calendar.getInstance();
                //是设置日历的时间，主要是让日历的年月日和当前同步
                mCalendarAfternoonEnd.setTimeInMillis(System.currentTimeMillis());
                // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
                mCalendarAfternoonEnd.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                //设置在几点提醒  设置的为13点
                mCalendarAfternoonEnd.set(Calendar.HOUR_OF_DAY, afternoonEndHour);
                //设置在几分提醒  设置的为25分
                mCalendarAfternoonEnd.set(Calendar.MINUTE, afternoonEndMinute);
                mCalendarAfternoonEnd.set(Calendar.SECOND, 0);
                long afternoonEnd = mCalendarAfternoonEnd.getTimeInMillis();



                int status = allCrotabList.get(i).getStatus();

                String[] weekList = allCrotabList.get(i).getRepeatValue().split(",");
                ArrayList week = new ArrayList();  //设置了 星期几  1-7
                for (int ii = 0; ii < weekList.length; ii++) {
                    week.add(Integer.parseInt(weekList[ii]));
                }
                // setAlarmAlert(status, morningStart, morningEnd, null, week, IMDevice.TaskType.TASK_TYPE_LESSION_MODE.ordinal());
                //setAlarmAlert(status, AfternoonStart, AfternoonEnd, null, week, IMDevice.TaskType.TASK_TYPE_LESSION_MODE.ordinal());
                int type = IMDevice.TaskType.TASK_TYPE_LESSION_MODE.ordinal();
                Intent forbidIntent = new Intent("com.fise.action.LESSION_MODE");
                Bundle forbidBundle = new Bundle();
                forbidBundle.putLong("forms", morningStart);
                forbidBundle.putLong("forme", morningEnd);
                forbidBundle.putLong("forras", AfternoonStart);
                forbidBundle.putLong("forae", afternoonEnd);
                forbidBundle.putInt("fors", status);
                forbidBundle.putInt("fort", type);
                forbidBundle.putCharSequenceArrayList("forweek", week);
                forbidIntent.putExtras(forbidBundle);
                IMApplication.getApplication().sendBroadcast(forbidIntent);

            } else if (allCrotabList.get(i).getTaskType() == IMDevice.TaskType.TASK_TYPE_LOVE_REMIND.ordinal()) {  //爱心提醒

                String loveName = allCrotabList.get(i).getTaskName(); //爱心提醒名字
               // long loveBeginTime = 0;//Long.parseLong(allCrotabList.get(i).getBeginTime());  //爱心提醒开始时间
//                String loveBegin = "2017年10月17日 " + allCrotabList.get(i).getBeginTime();
//                SimpleDateFormat sdfLoveBeginTime = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
//                Date loveBeginTimeData;
//                try {
//                    loveBeginTimeData = sdfLoveBeginTime.parse(loveBegin);
//                    loveBeginTime = loveBeginTimeData.getTime() / 1000;
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }

                String[] loveBeginTimeString = allCrotabList.get(i).getBeginTime().split(":");
                int loveBeginTimeHour = Integer.parseInt(loveBeginTimeString[0]);
                int loveBeginTimeMinute = Integer.parseInt(loveBeginTimeString[1]);

                Calendar mCalendar = Calendar.getInstance();
                //是设置日历的时间，主要是让日历的年月日和当前同步
                mCalendar.setTimeInMillis(System.currentTimeMillis());
                // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
                mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                //设置在几点提醒  设置的为时
                mCalendar.set(Calendar.HOUR_OF_DAY, loveBeginTimeHour);
                //设置在几分提醒  设置的为分
                mCalendar.set(Calendar.MINUTE, loveBeginTimeMinute);
                mCalendar.set(Calendar.SECOND, 0);
                long loveBeginTime = mCalendar.getTimeInMillis();



                long loveEndTime = loveBeginTime;//Long.parseLong(allCrotabList.get(i).getEndTime());  //爱心提醒结束时间

                String[] weekList = allCrotabList.get(i).getRepeatValue().split(",");
                ArrayList week = new ArrayList();  //设置了 星期几  1-7
                for (int ii = 0; ii < weekList.length; ii++) {
                    week.add(Integer.parseInt(weekList[ii]));
                }

                int status = allCrotabList.get(i).getStatus();
                int type = IMDevice.TaskType.TASK_TYPE_LOVE_REMIND.ordinal();
                //开始设置提醒
                // setAlarmAlert(status, loveBeginTime, loveEndTime, loveName, week, type);
                Intent loveIntent = new Intent("com.fise.action.LOVE_REMIND");
                Bundle loveBundle = new Bundle();
                loveBundle.putString("lovename", loveName);
                loveBundle.putLong("lovebt", loveBeginTime);
                loveBundle.putLong("loveet", loveEndTime);
                loveBundle.putInt("loves", status);
                loveBundle.putInt("lovet", type);
                loveBundle.putCharSequenceArrayList("loveweek", week);
                loveIntent.putExtras(loveBundle);
                IMApplication.getApplication().sendBroadcast(loveIntent);
            }

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

        List<DeviceEntity> userlist = dbInterface.loadAllConfigure();
        //test guan 如果登陆之后将是否充电置为结束充电
        for (DeviceEntity deviceInfo : userlist) {
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            deviceRspMap.put(deviceInfo.getDeviceId(), deviceInfo);
        }
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
     * @param device
     */
    public void updateDeveiceConfigure(DeviceEntity device) {

        if (device != null) {
            dbInterface.insertOrUpdateConfigure(device);
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            deviceRspMap.put(device.getDeviceId(), device);
        }
    }


    /**
     * 加载本地DB的状态 不管是离线还是在线登陆，在线的排在前面，loadFromDb 要运行的
     */
    public List<UserEntity> loadDevice() {
        logger.d("contact#reqWeilist");
        List<UserEntity> deviceList = dbInterface.loadAllDevice();
        // todo eric efficiency
        Collections.sort(deviceList, new Comparator<UserEntity>() {
            @Override
            public int compare(UserEntity entity1, UserEntity entity2) {
                if (entity2.getPinyinElement().pinyin != null
                        && entity2.getPinyinElement().pinyin.startsWith("#")) {
                    return -1;
                } else if (entity1.getPinyinElement().pinyin != null
                        && entity1.getPinyinElement().pinyin.startsWith("#")) {
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

        List<UserEntity> onLineDeviceList = new ArrayList<>();
        for (int i = 0; i < deviceList.size(); i++) {
            if (deviceList.get(i).getOnLine() == DBConstant.ONLINE) {
                onLineDeviceList.add(deviceList.get(i));
            }
        }
        for (int i = 0; i < onLineDeviceList.size(); i++) {
            deviceList.remove(onLineDeviceList.get(i));
        }

        for (int i = 0; i < onLineDeviceList.size(); i++) {
            deviceList.add(i, onLineDeviceList.get(i));
        }
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
        familyConcernMap.clear();
        crontabMap.clear();
        stepDataMap.clear();
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
     * @param device
     */
    public void settingOpen(int devId, String value, SettingType type,
                            int isAdd, DeviceEntity device) {

        int loginId = IMLoginManager.instance().getLoginId(); // IMDeviceAddReq

        IMDevice.IMDeviceSettingReq settingReq = IMDevice.IMDeviceSettingReq
                .newBuilder().setUserId(loginId).setDeviceId(devId)
                .setSettingType(type).addValueList(IMDevice.DeviceSetValue.newBuilder().setValue(value).build()).setStatus(isAdd)
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
                    String resultStr = imSettingRsp.getResultString();
                    onRepMsgServerRspDevice(imSettingRsp, phone, "", showType,
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
            IMDevice.IMDeviceSettingRsp imSettingRsp, String phone, String name,
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
                        WhiteEntity white = ProtoBuf2JavaBean.getPhoneEntity(phone, name,
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
                        WhiteEntity white = ProtoBuf2JavaBean.getPhoneEntity(phone, "",
                                rsp.getDeviceId());
                        insertAlarmList(white);
                    }

                } else if (showType == SettingType.SETTING_TYPE_WORK_MODE) {
                    rsp.setMode(open);
                    updateDeveiceConfigure(rsp);

                } else if (showType == SettingType.SETTING_TYPE_PHONE_LIGHT_TIME) {  //亮屏时间

                    MobilePhoneDeviceEntity deviceEntity = MobilePhoneDeviceEntity.parseFromDB(rsp);
                    deviceEntity.setLight(open);
                    deviceEntity.setContent();
                    updateDeveiceConfigure(deviceEntity);

                } else if (showType == SettingType.SETTING_TYPE_LISTEN_MODE) {
                    //	rsp.setSilent(open);
                    //	updateDeveiceRsp(rsp);
                } else if (showType == SettingType.SETTING_TYPE_ALARM_OPWEROFF) {
                    rsp.setAlrPoweroff(open);
                    updateDeveiceConfigure(rsp);

                } else if (showType == SettingType.SETTING_TYPE_ALARM_BATTERY) {
                    rsp.setAlrBattery(open);
                    updateDeveiceConfigure(rsp);
                } else if (showType == SettingType.SETTING_TYPE_BELL_MODE) {
                    rsp.setBellMode(open);
                    updateDeveiceConfigure(rsp);
                } else if (showType == SettingType.SETTING_TYPE_ALARM_CALL) {
                    rsp.setAlrCall(open);
                    updateDeveiceConfigure(rsp);

                    //计步
                } else if (showType == SettingType.SETTING_TYPE_PHONE_STEP_MODE) {
                    MobilePhoneDeviceEntity mobilePhoneEntity = MobilePhoneDeviceEntity.parseFromDB(rsp);
                    mobilePhoneEntity.setStep_mode(open);
                    mobilePhoneEntity.setContent();

                    rsp.setDiff(mobilePhoneEntity.getDiff());
                    updateDeveiceConfigure(rsp);
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
                .setSettingType(type).addValueList(IMDevice.DeviceSetValue.newBuilder().setValue(white.getPhone()).build())
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
     * 设置白名单号码/用户信息接口
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
                .setSettingType(type).addValueList(IMDevice.DeviceSetValue.newBuilder().setValue(value).build()).setStatus(isAdd)
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
                    if (phone.contains(":")) {
                        String name = phone.substring(0, phone.lastIndexOf(":"));
//						String phoneNumber = phone.split(":")[1];
//						String name = phone.split(":")[0];
                        String phoneNumber = phone.substring(phone.lastIndexOf(":") + 1, phone.length());
                        onRepMsgServerSettingDevice(imSettingRsp, phoneNumber, name, showType);
                    } else {
                        onRepMsgServerSettingDevice(imSettingRsp, phone, "", showType);
                    }


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
            IMDevice.IMDeviceSettingRsp imSettingRsp, String phone, String name,
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
                        whiteTemp.setName(name);
                        insertWhiteUser(whiteTemp);
                    } else {
                        WhiteEntity white = ProtoBuf2JavaBean.getPhoneEntity(phone, name,
                                imSettingRsp.getDeviceId());
                        insertWhiteUser(white);
                    }

                } else if (showType == SettingType.SETTING_TYPE_ALARM_MOBILE) {

                    WhiteEntity whiteTemp = findAlarmList(phone,
                            imSettingRsp.getDeviceId());
                    if (whiteTemp != null) {
                        whiteTemp.setDevId(imSettingRsp.getDeviceId());
                        whiteTemp.setPhone(phone);
                        whiteTemp.setName(name);
                        insertAlarmList(whiteTemp);
                    } else {
                        //紧急号码,启用name字段
                        WhiteEntity white = ProtoBuf2JavaBean.getPhoneEntity(phone, name,
                                imSettingRsp.getDeviceId());
                        insertAlarmList(white);
                    }

                } else if (showType == SettingType.SETTING_TYPE_DEVICE_MOBILE) {
                    UserEntity device = IMContactManager.instance()
                            .findDeviceContact(imSettingRsp.getDeviceId());

                    if (device != null) {
                        device.setPhone(phone);
                        IMContactManager.instance().updateOrDevice(device);
                    }
                } else if (showType == SettingType.SETTING_TYPE_DEVICE_MOBILE) {
                    UserEntity device = IMContactManager.instance()
                            .findDeviceContact(imSettingRsp.getDeviceId());
                    device.setPhone(phone);
                    if (device != null) {
                        IMContactManager.instance().updateOrDevice(device);
                    }
                } else if (showType == SettingType.SETTING_TYPE_USER_HEIGHT) { //设备身高设置
                    UserEntity device;
                    device = IMContactManager.instance().findDeviceContact(imSettingRsp.getDeviceId());
                    if (device != null) {
                        device.setHeight(Integer.parseInt(phone));
                        IMContactManager.instance().updateOrDevice(device);
                    }

                } else if (showType == SettingType.SETTING_TYPE_USER_WEIGHT) { // 设备体重设置
                    UserEntity device;
                    device = IMContactManager.instance().findDeviceContact(imSettingRsp.getDeviceId());
                    if (device != null) {
                        device.setWeight(Integer.parseInt(phone));
                        IMContactManager.instance().updateOrDevice(device);
                    }
                } else if (showType == SettingType.SETTING_TYPE_USER_BIRTHDAY) { // 设备生日
                    UserEntity device;
                    device = IMContactManager.instance().findDeviceContact(imSettingRsp.getDeviceId());
                    if (device != null) {
                        device.setBirthday(phone);
                        IMContactManager.instance().updateOrDevice(device);
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
                .setDeviceId(devInfo.getPeerId()).addValueList(IMDevice.DeviceSetValue.newBuilder().setValue(value).build())
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
                if (type == SettingType.SETTING_TYPE_DEVICE_NICK) {  //修改昵称
                    userInfo.setMainName(value);
                    IMContactManager.instance().updateOrDevice(userInfo);

                    //如果是家庭群对于得群名称做更改
                    if (Utils.isClientType(userInfo)) {
                        DeviceEntity deviceCard = IMDeviceManager.instance().findDeviceCard(userInfo.getPeerId());
                        GroupEntity devGroup = IMGroupManager.instance().findFamilyGroup(deviceCard.getFamilyGroupId());
                        if (devGroup != null) {
                            devGroup.setMainName(value);
                            IMGroupManager.instance().updateGroup(devGroup);
                        }
                    }
                } else if (type == SettingType.SETTING_TYPE_DEVICE_AVATAR) {  //修改头像
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
                DeviceEntity device = IMDeviceManager.instance().findDeviceCard(Family.getDevId());
                GroupEntity group = null;

                if (device != null) {
                    group = IMGroupManager.instance().findFamilyGroup(device.getFamilyGroupId());
                }
                if (group != null) {
                    group.getlistGroupMemberIds().remove(user.getPeerId());
                }

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
        if (authId == loginId) {
            return;
        }


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
     * 请求加设备的请求
     *
     * @param deviceId
     * @param type
     * @param devUser
     */
    public void addDevice(String deviceId, ManageType type, UserEntity devUser, int schoolId) {

        int loginId = IMLoginManager.instance().getLoginId(); // IMDeviceAddReq
        IMDevice.IMDeviceManageReq deviceReq = IMDevice.IMDeviceManageReq
                .newBuilder().setFromId(loginId).setUserId(loginId)
                .setType(type).setAccount(deviceId).setDeviceSeq(schoolId + "")
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

                    String name = "";
                    if (user.getComment().equals("")) {
                        name = user.getMainName();
                    } else {
                        name = user.getComment();
                    }

                    FamilyConcernEntity entity = ProtoBuf2JavaBean
                            .getFindFamilyConcern(user.getPeerId(),
                                    name, user.getUserAvatar(),
                                    user.getPhone(), devID);
                    insertFamilyConcern(entity);

                } else {

                    String name = "";
                    if (user.getComment().equals("")) {
                        name = user.getMainName();
                    } else {
                        name = user.getComment();
                    }
                    FamilyConcernEntity entity = findFamilyConcern(
                            user.getPeerId(), devID);
                    entity.setIdentity(name);
                    insertFamilyConcern(entity);

                }

                GroupEntity group = IMGroupManager.instance().findFamilyGroup(devID);
                if (group != null) {
                    group.getlistGroupMemberIds().add(user.getPeerId());
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
                        if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                            devicebReq.add(userEntity);
                            DeviceConfigReq(userEntity.getPeerId());

                        }
                    }

                    dbInterface.batchInsertOrUpdateDevice(devicebReq);
                    IMContactManager.instance().updateDevice();
                    addDevId = userInfo.getUserId();
                    triggerEvent(DeviceEvent.USER_INFO_ADD_DEVICE_SUCCESS);

                } else if (devType == ManageType.MANAGE_TYPE_DEL_DEVICE) {

                    if (Utils.isClientType(devInfo)) {
                        if (devInfo != null) {

                            deleteDevice(devInfo);
                            triggerEvent(UserInfoEvent.USER_INFO_DELETE_SUCCESS); //
                        }
                    } else {

                        String session = devInfo.getSessionKey();
                        if (!Utils.isClientType(devInfo)) {

                            IMSessionManager.instance().reqRemoveSessionByKey(session);
                            imContactManager.deleteFriends(devInfo);


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
     * @param device_id
     * @param operate_type
     * @param task_id
     */
    public void setDevTask(final int from_id, final int device_id,
                           OperateType operate_type, final int task_id,
                           final TaskType task_type, final String task_name,
                           final String task_param, final String begin_time,
                           final String end_time, final int status, final int repeat_mode,
                           final String repeat_value) {

        IMDevice.SettingCrontabRequest deviceReq = IMDevice.SettingCrontabRequest
                .newBuilder().setFromId(from_id).setDeviceId(device_id)
                .setOperateType(operate_type).setTaskId(task_id)
                .setTaskType(task_type).setTaskName(task_name)
                .setTaskParam(task_param).setBeginTime(begin_time)
                .setEndTime(end_time).setStatus(status)
                .setRepeatValue(repeat_value) //.setRepeatMode(repeat_mode)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
        int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_CRONTAB_SETTING_REQ_VALUE;

        imSocketManager.sendRequest(deviceReq, sid, cid, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMDevice.SettingCrontabResponse imCrontabRsp = IMDevice.SettingCrontabResponse
                            .parseFrom((CodedInputStream) response);

                    int task_id = imCrontabRsp.getTaskId();
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
     * @param
     * @param
     * @param
     */
    public void onRepMsgCrontab(IMDevice.SettingCrontabResponse imCrontabRsp,
                                int from_id, int device_id, int task_id, TaskType task_type,
                                String task_name, String task_param, String begin_time,
                                String end_time, int status, int repeat_mode, String repeat_value) {

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
     * 获取数据
     *
     * @param
     */
    public StepData getStep(String today) {
        if ((!today.equals("")) && stepDataMap.containsKey(today)) {
            return stepDataMap.get(today);
        }
        return null;
    }


    /**
     * 插入更新 计步数量
     *
     * @param
     */
    public void updateStepData(StepData stepData) {
        if (stepData != null) {
            if (dbInterface != null) {
                DBInterface.instance().insertOrUpdateStepData(stepData);
                stepDataMap.put(stepData.getToday(), stepData);
            }
        }
    }


    /**
     * 插入任务信息(上课/爱心)模式
     *
     * @param
     */
    public void inserOrFriendsContact(DeviceCrontab crontab) {
        if (crontab != null) {
            dbInterface.insertOrUpdateDevTask(crontab);
            crontabMap.put(crontab.getTaskId(), crontab);
            triggerEvent(DeviceEvent.DEVICE_TASK_ADD_SUCCESS);
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
            triggerEvent(DeviceEvent.DEVICE_TASK_UPDATE_SUCCESS);
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
            triggerEvent(DeviceEvent.DEVICE_TASK_DELETE_SUCCESS);
        }
    }


    public void deleteAllCrontab(UserEntity devInfo) {

        for (Map.Entry<Integer, DeviceCrontab> entry : crontabMap.entrySet()) {
            DeviceCrontab deviceCrontab = entry.getValue();
            if (deviceCrontab.getDeviceId() == devInfo.getPeerId()) {
                crontabMap.remove(entry.getKey());
                dbInterface.deleteOrUpdateDevTask(deviceCrontab);
            }
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
        if (taskId >= 0 && crontabMap.containsKey(taskId)) {
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
    public void deleteDeviceConfigure(DeviceEntity device) {

        if (findDeviceCard(device.getDeviceId()) != null) {
            deviceRspMap.remove(device.getDeviceId());
            dbInterface.deleteOrUpdateConfigure(device);
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


    public ArrayList<DeviceCrontab> getAllCrotabList(int deviceId) {
        ArrayList<DeviceCrontab> deviceTaskList = new ArrayList<>();
        for (Map.Entry<Integer, DeviceCrontab> entry : crontabMap.entrySet()) {
            DeviceCrontab deviceCrontab = entry.getValue();
            if (deviceCrontab.getDeviceId() == deviceId) {
                deviceTaskList.add(deviceCrontab);
            }
        }
        return deviceTaskList;
    }


    /**
     * 短信验证码
     *
     * @param data
     */
    public void verificationCode(String data) {
        if (IMLoginManager.instance().getLoginInfo() == null) {
            return;
        }

        if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {
            verificationCode = data;
            triggerEvent(DeviceEvent.DEVICE_VERIFICATION_CODE);
        }
    }

    public String getVerificationCode() {
        return verificationCode;
    }


    /**
     * @param data
     */
    public void postionPublishEvent(String data) {

        if (IMLoginManager.instance().getLoginInfo() == null) {
            return;
        }


        int usrId = IMLoginManager.instance().getLoginId();
        IMBaseDefine.JsonObject jsonObject = IMBaseDefine.JsonObject.newBuilder()
                .setKeyName("content")
                .setKeyValue(data)
                .build();

        IMBaseDefine.EventInfo event_info = IMBaseDefine.EventInfo.newBuilder()
                .setEventKey(IMBaseDefine.EventKey.EVENT_KEY_REPORT_BILL) //EVENT_KEY_REPORT_STEP
                .addKeyMap(jsonObject)
                .setEventLevel(IMBaseDefine.EventLevel.EVENT_LEVEL_MESSAGE)
                .build();

        IMMessage.IMPublishEventReq publishEventReq = IMMessage.IMPublishEventReq.newBuilder()
                .setUserId(usrId)
                .setCreateTime(0)
                .setSessionType(IMUserActionManager.instance().getSmsType())
                .setToId(IMUserActionManager.instance().getSmsId())
                .setEventInfo(event_info)
                .build(); //,int toId ,IMBaseDefine.SessionType session_type

        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_EVENT_PUBLISH_REQ_VALUE; //CID_MSG_EVENT_PUBLISH_REQ


        IMSocketManager.instance().sendRequest(publishEventReq, sid, cid, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {

            }

            @Override
            public void onFaild() {

            }

            @Override
            public void onTimeout() {

            }
        });

    }


    public void postionPublishEvent(IMBaseDefine.EventKey key, int toId, IMBaseDefine.SessionType session_type) {

        int usrId = IMLoginManager.instance().getLoginId();
        double longitude = MainActivity.longitude;
        double latitude = MainActivity.latitude;
        int BatteryN = MainActivity.BatteryN;
        int signalN = Utils.getIconLevel(MainActivity.signalN);//
        String address = MainActivity.address;
        IMBaseDefine.PosFromType from_type = IMBaseDefine.PosFromType.POS_FROM_GPS;

        // 高德地图 定位类型对照表 http://lbs.amap.com/api/android-location-sdk/guide/utilities/location-type/
        if (MainActivity.locationType == 1) {
            from_type = IMBaseDefine.PosFromType.POS_FROM_GPS;
        } else if (MainActivity.locationType == 5) {
            from_type = IMBaseDefine.PosFromType.POS_FROM_WIFI;
        } else if (MainActivity.locationType == 6) {
            from_type = IMBaseDefine.PosFromType.POS_FROM_BASE;

        } else {
            from_type = IMBaseDefine.PosFromType.POS_FROM_GPS;
        }

        IMBaseDefine.BaseWiInfo base_info = IMBaseDefine.BaseWiInfo.newBuilder()
                .setLactionX(latitude + "")
                .setLactionY(longitude + "")
                .setFromType(from_type)
                .setBattery(BatteryN)
                .setAddrInfo(address)
                .setSq(signalN)
                .build();

        IMBaseDefine.EventInfo event_info = IMBaseDefine.EventInfo.newBuilder()
                .setEventKey(key) //EVENT_KEY_REPORT_STEP
                .setWiInfo(base_info)
                .setEventLevel(IMBaseDefine.EventLevel.EVENT_LEVEL_MESSAGE)
                .build();

        IMMessage.IMPublishEventReq publishEventReq = IMMessage.IMPublishEventReq.newBuilder()
                .setUserId(usrId)
                .setCreateTime(0)
                .setSessionType(session_type)
                .setToId(toId)
                .setEventInfo(event_info)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_EVENT_PUBLISH_REQ_VALUE; //CID_MSG_EVENT_PUBLISH_REQ


        IMSocketManager.instance().sendRequest(publishEventReq, sid, cid, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
            }

            @Override
            public void onFaild() {
            }

            @Override
            public void onTimeout() {
            }
        });

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

    /**
     * -----------------------实体 get set 定义-----------------------------------
     */

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

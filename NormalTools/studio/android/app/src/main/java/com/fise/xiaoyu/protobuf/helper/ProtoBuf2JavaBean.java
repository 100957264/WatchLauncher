package com.fise.xiaoyu.protobuf.helper;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.DeviceTrajectory;
import com.fise.xiaoyu.DB.entity.FamilyConcernEntity;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.GroupVersion;
import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.ReqFriendsEntity;
import com.fise.xiaoyu.DB.entity.SessionEntity;
import com.fise.xiaoyu.DB.entity.SystemConfigEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.entity.WeiEntity;
import com.fise.xiaoyu.DB.entity.WhiteEntity;
import com.fise.xiaoyu.DB.sp.SystemConfigSp;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.MessageConstant;
import com.fise.xiaoyu.device.entity.MobilePhoneDeviceEntity;
import com.fise.xiaoyu.imservice.entity.AudioMessage;
import com.fise.xiaoyu.imservice.entity.DevMessage;
import com.fise.xiaoyu.imservice.entity.MsgAnalyzeEngine;
import com.fise.xiaoyu.imservice.entity.UnreadEntity;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMDeviceManager;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMBaseDefine.DeviceAction;
import com.fise.xiaoyu.protobuf.IMBaseDefine.GroupMemberStatus;
import com.fise.xiaoyu.protobuf.IMBaseDefine.UserActionInfo;
import com.fise.xiaoyu.protobuf.IMDevice.DeviceAlarmRequest;
import com.fise.xiaoyu.protobuf.IMGroup;
import com.fise.xiaoyu.protobuf.IMMessage;
import com.fise.xiaoyu.utils.CommonUtil;
import com.fise.xiaoyu.utils.FileUtil;
import com.fise.xiaoyu.utils.pinyin.PinYin;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ProtoBuf2JavaBean
 */
public class ProtoBuf2JavaBean {


    public static DeviceTrajectory getDeviceTrajectory(DeviceAction rInfo) {
        DeviceTrajectory userEntity = new DeviceTrajectory();
        int timeNow = (int) (System.currentTimeMillis() / 1000);

        userEntity.setActionId(rInfo.getActionId());
        userEntity.setDeviceId(rInfo.getDeviceId());
        userEntity.setActionType(rInfo.getActionType());
        userEntity.setActionValue(rInfo.getActionValue());
        userEntity.setActionParam(rInfo.getActionParam());
        userEntity.setStatus(rInfo.getStatus());
        userEntity.setUpdated(rInfo.getUpdated());
        userEntity.setLastUpdated(timeNow);


        JSONObject extraContent = null;
        try {
            userEntity.setLng(extraContent.getString("lng"));
            userEntity.setLat(extraContent.getString("lat"));
            userEntity.setParam(extraContent.getString("param"));

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return userEntity;
    }


    public static DeviceTrajectory getDeviceTrajectory(DeviceAlarmRequest rInfo) {
        DeviceTrajectory userEntity = new DeviceTrajectory();
        int timeNow = (int) (System.currentTimeMillis() / 1000);

        userEntity.setActionId(rInfo.getActionId());
        userEntity.setDeviceId(rInfo.getFromId());
        userEntity.setActionType(0);
        userEntity.setActionValue(rInfo.getAlarmType().ordinal());
        userEntity.setActionParam(rInfo.getParam());
        userEntity.setStatus(1);
        userEntity.setUpdated(timeNow);
        userEntity.setLastUpdated(timeNow);
        userEntity.setLng(rInfo.getLng());
        userEntity.setLat(rInfo.getLat());
        userEntity.setParam(rInfo.getParam());


        return userEntity;
    }


    public static UserEntity getUserEntity(IMBaseDefine.UserInfo userInfo) {
        UserEntity userEntity = new UserEntity();
        int timeNow = (int) (System.currentTimeMillis() / 1000);

        userEntity.setStatus(userInfo.getStatus());
//		userEntity.setAvatar(SystemConfigSp.instance().getStrConfig(
//				SystemConfigSp.SysCfgDimension.MSFSSERVER)
//				+ userInfo.getAvatarUrl());
//		adsf

        userEntity.setAvatar(userInfo.getAvatarUrl());

        // userEntity.setAvatar(userInfo.getAvatarUrl());
        userEntity.setCreated(timeNow);
        userEntity.setDepartmentId(userInfo.getDepartmentId());
        userEntity.setEmail(userInfo.getEmail());
        userEntity.setGender(userInfo.getUserGender());
        userEntity.setMainName(userInfo.getUserNickName());
        userEntity.setPhone(userInfo.getUserTel());

        //userEntity.setPinyinName(userInfo.getUserDomain());
        //userEntity.setRealName(userInfo.getUserRealName());  //"0"  "1"
        //用于是否能够修改小位号判断
        userEntity.setPinyinName(userInfo.getUserRealName());
        userEntity.setRealName(userInfo.getUserDomain());

        userEntity.setUpdated(timeNow);
        userEntity.setPeerId(userInfo.getUserId());
        userEntity.setFriend(userInfo.getIsFriend());
        userEntity.setAuth(userInfo.getAuth());
        //userEntity.setOnLine(0); // userInfo.getOnline

        userEntity.setUserType(userInfo.getUserType());

        userEntity.setCountry(userInfo.getCountry());
        userEntity.setProvince(userInfo.getProvince());
        userEntity.setCity(userInfo.getCity());
        userEntity.setSign_info(userInfo.getSignInfo());
        userEntity.setComment(userInfo.getComment());
        userEntity.setGroupNick(userInfo.getGroupNick());

        userEntity.setBirthday(userInfo.getBirthday());
        userEntity.setWeight(userInfo.getWeight());
        userEntity.setHeight(userInfo.getHeight());


        if (userInfo.getLng() != "") {
            userEntity.setLongitude(Double.parseDouble(userInfo.getLng()));
        } else {
            userEntity.setLongitude(0.0f);
        }

        if (userInfo.getLat() != "") {
            userEntity.setLatitude(Double.parseDouble(userInfo.getLat()));
        } else {
            userEntity.setLatitude(0.0f);
        }

        userEntity.setBattery(userInfo.getBattery());
        userEntity.setSignal(userInfo.getSq());


        userEntity.setFriendNeedAuth(userInfo.getFriendNeedAuth());
        userEntity.setLoginSafeSwitch(userInfo.getLoginSafeSwitch());
        userEntity.setSearchAllow(userInfo.getSearchAllow());

        PinYin.getPinYin(userEntity.getMainName(),
                userEntity.getPinyinElement());
        return userEntity;
    }


    public static WeiEntity getWeiEntity(UserActionInfo weiInfo) {

        WeiEntity weiEntity = new WeiEntity();

        weiEntity.setDevice_id(weiInfo.getDeviceId());
        weiEntity.setFromId(weiInfo.getFromId());
        weiEntity.setToId(weiInfo.getToId());
        weiEntity.setActId(weiInfo.getActId());
        weiEntity.setActType(weiInfo.getActType());
        weiEntity.setStatus(weiInfo.getStatus());
        weiEntity.setUpdated(weiInfo.getUpdated());
        weiEntity.setMasgData(weiInfo.getMsgData());

        return weiEntity;
    }

    public static SystemConfigEntity getSystemConfigEntity(String launch, int launch_time, String launchAction, String system_notice,
                                                           String update_url, String website, String version_support, String comment_url, String version, String version_comment, String adviceFeedbackUrl, String mallUrl) {


        SystemConfigEntity systemConfig = new SystemConfigEntity();

        systemConfig.setLaunch(launch);
        systemConfig.setLaunchTime(launch_time);
        systemConfig.setLaunchAction(launchAction);

        systemConfig.setSystemNotice(system_notice);
        systemConfig.setUpdateUrl(update_url);
        systemConfig.setWebsite(website);
        systemConfig.setVersionSupport(version_support);
        systemConfig.setCommentUrl(comment_url);
        systemConfig.setVersion(version);
        systemConfig.SetVersionComment(version_comment);
        systemConfig.setSuggestUrl(adviceFeedbackUrl);
        systemConfig.setMallUrl(mallUrl);
        return systemConfig;
    }


    public static UserEntity getUserCopyEntity(UserEntity userCopyEntity) {
        UserEntity userEntity = new UserEntity();

        userEntity.setStatus(userCopyEntity.getStatus());
        userEntity.setAvatar(userCopyEntity.getUserAvatar());
        userEntity.setCreated(userCopyEntity.getCreated());
        userEntity.setDepartmentId(userCopyEntity.getDepartmentId());
        userEntity.setEmail(userCopyEntity.getEmail());
        userEntity.setGender(userCopyEntity.getGender());
        userEntity.setMainName(userCopyEntity.getMainName());
        userEntity.setPhone(userCopyEntity.getPhone());

        userEntity.setPinyinName(userCopyEntity.getPinyinName());
        userEntity.setRealName(userCopyEntity.getRealName());

        userEntity.setUpdated(userCopyEntity.getUpdated());
        userEntity.setPeerId(userCopyEntity.getPeerId());
        userEntity.setFriend(userCopyEntity.getIsFriend());

        userEntity.setUserType(userCopyEntity.getUserType());
        userEntity.setLongitude(userCopyEntity.getLongitude());
        userEntity.setLatitude(userCopyEntity.getLatitude());

        userEntity.setBattery(userCopyEntity.getBattery());
        userEntity.setSignal(userCopyEntity.getSignal());
        userEntity.setAuth(userCopyEntity.getAuth());
        userEntity.setOnLine(userCopyEntity.getOnLine());

        userEntity.setCountry(userCopyEntity.getCountry());
        userEntity.setProvince(userCopyEntity.getProvince());
        userEntity.setCity(userCopyEntity.getCity());
        userEntity.setSign_info(userCopyEntity.getSign_info());
        userEntity.setComment(userCopyEntity.getComment());

        userEntity.setFriendNeedAuth(userCopyEntity.getFriendNeedAuth());
        userEntity.setLoginSafeSwitch(userCopyEntity.getLoginSafeSwitch());
        userEntity.setSearchAllow(userCopyEntity.getSearchAllow());
        userEntity.setGroupNick(userCopyEntity.getGroupNick());
        userEntity.setLocationType(userCopyEntity.getLocationType());

        userEntity.setWeight(userCopyEntity.getWeight());
        userEntity.setHeight(userCopyEntity.getHeight());
        userEntity.setBirthday(userCopyEntity.getBirthday());


        PinYin.getPinYin(userEntity.getMainName(),
                userEntity.getPinyinElement());
        return userEntity;
    }

    public static SessionEntity getSessionEntity(
            IMBaseDefine.ContactSessionInfo sessionInfo) {
        SessionEntity sessionEntity = new SessionEntity();

        int msgType = getJavaMsgType(sessionInfo.getLatestMsgType());
        sessionEntity.setLatestMsgType(msgType);
        sessionEntity.setPeerType(getJavaSessionType(sessionInfo
                .getSessionType()));
        sessionEntity.setPeerId(sessionInfo.getSessionId());
        sessionEntity.buildSessionKey();
        sessionEntity.setTalkId(sessionInfo.getLatestMsgFromUserId());
        sessionEntity.setLatestMsgId(sessionInfo.getLatestMsgId());
        sessionEntity.setCreated(sessionInfo.getUpdatedTime());

        sessionEntity.setMuteNotification(sessionInfo.getMuteNotification());
        sessionEntity.setStockOnTop(sessionInfo.getStickyOnTop());

        String content = sessionInfo.getLatestMsgData().toStringUtf8();
        String desMessage = new String(Security.getInstance()
                .DecryptMsg(content));
        // 判断具体的类型是什么
        if (//msgType == DBConstant.MSG_TYPE_GROUP_TEXT
            //|| msgType == DBConstant.MSG_TYPE_SINGLE_TEXT
                msgType == DBConstant.MSG_TYPE_SINGLE_AUTH_IMAGE
                        || msgType == DBConstant.MSG_TYPE_GROUP_AUTH_IMAGE
                        || msgType == DBConstant.MSG_TYPE_SINGLE_IMAGE
                        || msgType == DBConstant.MSG_TYPE_GROUP_IMAGE) {
            desMessage = MsgAnalyzeEngine.analyzeMessageDisplay(desMessage);
            sessionEntity.setLatestMsgData(desMessage);

        } else if (msgType == DBConstant.MSG_TYPE_SINGLE_VIDIO
                || msgType == DBConstant.MSG_TYPE_GROUP_VIDIO) {
            sessionEntity.setLatestMsgData(DBConstant.DISPLAY_FOR_VEDIO);

        } else if (msgType == DBConstant.MSG_TYPE_SINGLE_BUSSINESS_CARD
                || msgType == DBConstant.MSG_TYPE_GROUP_BUSSINESS_CARD) {
            sessionEntity.setLatestMsgData(DBConstant.DISPLAY_FOR_CARD);

        } else if (msgType == DBConstant.MSG_TYPE_SINGLE_LOCATION
                || msgType == DBConstant.MSG_TYPE_GROUP_LOCATION) {
            sessionEntity.setLatestMsgData(DBConstant.DISPLAY_FOR_POSTION);

        } else if (msgType == DBConstant.MSG_TYPE_SINGLE_NOTICE) {
            sessionEntity.setLatestMsgData(DBConstant.DISPLAY_FOR_NOTICE);

        } else if (msgType == DBConstant.MSG_TYPE_VIDEO_CALL
                || msgType == DBConstant.MSG_TYPE_VIDEO_ANSWER
                || msgType == DBConstant.MSG_TYPE_VIDEO_CLOSE) {
            sessionEntity.setLatestMsgData(DBConstant.DISPLAY_FOR_ONLINE_VIDEO);

        } else if (msgType == DBConstant.MSG_TYPE_GROUP_DEV_MESSAGE
                || msgType == DBConstant.MSG_TYPE_SINGLE_DEV_MESSAGE) {

            IMBaseDefine.EventInfo eventInfo = null;
            try {
                eventInfo = IMBaseDefine.EventInfo.parseFrom(Security.getInstance().DecryptMsg(sessionInfo.getLatestMsgData().toStringUtf8()));

                //离开电子围栏
                if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_CROSS_SAFE_AREA.ordinal()) {
                    sessionEntity.setLatestMsgData(DBConstant.EVENT_KEY_CROSS_SAFE_AREA);
                } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_ENTER_SAFE_AREA.ordinal()) {
                    sessionEntity.setLatestMsgData(DBConstant.EVENT_KEY_ENTER_SAFE_AREA);
                } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_LOW_BATTERY.ordinal()) {
                    sessionEntity.setLatestMsgData(DBConstant.EVENT_KEY_LOW_BATTERY);
                } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_BEGIN_CHARGING.ordinal()) {
                    sessionEntity.setLatestMsgData(DBConstant.EVENT_KEY_BEGIN_CHARGING);
                } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_END_CHARGING.ordinal()) {
                    sessionEntity.setLatestMsgData(DBConstant.EVENT_KEY_END_CHARGING);
                } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_SOS.ordinal()) {
                    sessionEntity.setLatestMsgData(DBConstant.EVENT_KEY_SOS);
                } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_CALL_OUT.ordinal()) {
                    sessionEntity.setLatestMsgData(DBConstant.EVENT_KEY_CALL_OUT);
                } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_CALL_IN.ordinal()) {
                    sessionEntity.setLatestMsgData(DBConstant.EVENT_KEY_CALL_IN);
                } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_SHUTDOWN.ordinal()) {
                    sessionEntity.setLatestMsgData(DBConstant.EVENT_KEY_SHUTDOWN);
                } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_CURRENT_INFO.ordinal()) {
                    sessionEntity.setLatestMsgData(DBConstant.EVENT_KEY_CURRENT_INFO);
                } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_REPORT_BILL.ordinal()) {
                    sessionEntity.setLatestMsgData(DBConstant.EVENT_KEY_REPORT_BILL);

                } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_DROP_DOWN.ordinal()) {
                    sessionEntity.setLatestMsgData(DBConstant.EVENT_KEY_DROP_DOWN);
                } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_WEAR_ON.ordinal()) {
                    sessionEntity.setLatestMsgData(DBConstant.EVENT_KEY_WEAR_ON);
                }

            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }

        } else if (msgType == DBConstant.MSG_TYPE_SINGLE_AUTH_SOUND || msgType == DBConstant.MSG_TYPE_GROUP_AUTH_SOUND
                || msgType == DBConstant.MSG_TYPE_SINGLE_AUDIO || msgType == DBConstant.MSG_TYPE_GROUP_AUDIO) {

            sessionEntity.setLatestMsgData(DBConstant.DISPLAY_FOR_AUDIO);

        } else {

            if (desMessage == null) {
                desMessage = "";
            }
            sessionEntity.setLatestMsgData(desMessage);
        }

        sessionEntity.setUpdated(sessionInfo.getUpdatedTime());

        return sessionEntity;
    }

    public static boolean isPhoneNumberValid(String phoneNumber) {

        boolean isValid = false;
        /**
         * valid phone number format;
         */
        String expression1 = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{5})$";
        /**
         * valid phone number format;
         */
        String expression2 = "^\\(?(\\d{3})\\)?[- ]?(\\d{4})[- ]?(\\d{4})$";
        CharSequence inputStr = phoneNumber;
        Pattern pattern1 = Pattern.compile(expression1);
        Matcher matcher1 = pattern1.matcher(inputStr);

        Pattern pattern2 = Pattern.compile(expression2);
        Matcher matcher2 = pattern2.matcher(inputStr);
        if (matcher1.matches() || matcher2.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static WhiteEntity getPhoneEntity(String phone, String name, int devId) {
        WhiteEntity phoneEntity = new WhiteEntity();
        phoneEntity.setPhone(phone);
        phoneEntity.setDevId(devId);
        phoneEntity.setName(name);
        return phoneEntity;
    }


    public static FamilyConcernEntity getFindFamilyConcern(int peeId, String familyConcern, String avatar, String phone, int devId) {
        FamilyConcernEntity entity = new FamilyConcernEntity();
        entity.setPeeId(peeId);
        entity.setIdentity(familyConcern);
        entity.setAvatar(avatar);
        entity.setPhone(phone);
        entity.setDevId(devId);
        return entity;
    }

    //儿童手表的系统配置
    public static DeviceEntity getDeviceCardEntity(int deviceId, int master_id,
                                                   int devType, String mobile, int alr_battery, int alr_poweroff, int alr_call,
                                                   int mode, int bell_mode, int updated, int family_group_id, String old_version, String new_version, String update_info, String update_url, int charge) {
        DeviceEntity cardEntity = new DeviceEntity();

        cardEntity.setDeviceId(deviceId);
        cardEntity.setMasterId(master_id);
        cardEntity.setDevType(devType);
        cardEntity.setMobile(mobile);

        cardEntity.setAlrBattery(alr_battery);
        cardEntity.setAlrPoweroff(alr_poweroff);
        cardEntity.setAlrCall(alr_call);
        cardEntity.setMode(mode);

        cardEntity.setBellMode(bell_mode);
        cardEntity.setUpdated(updated);
        cardEntity.setFamilyGroupId(family_group_id);

        cardEntity.setOld_version(old_version);
        cardEntity.setNew_version(new_version);
        cardEntity.setUpdate_info(update_info);
        cardEntity.setUpdate_url(update_url);
        cardEntity.setCharge(charge);

        return cardEntity;
    }


    //小雨手机和
    public static DeviceEntity getDeviceMobilePhoneEntity(int deviceId, int master_id,
                                                          int devType, String mobile, int alr_battery, int alr_poweroff
            , int alr_call, int mode, int bell_mode, int updated
            , int family_group_id, int light_time, int step_mode,
                                                          String old_version, String new_version, String update_info, String update_url, int charge, int school_id) {
        MobilePhoneDeviceEntity mobilePhoneEntity = new MobilePhoneDeviceEntity();

        mobilePhoneEntity.setDeviceId(deviceId);
        mobilePhoneEntity.setMasterId(master_id);
        mobilePhoneEntity.setDevType(devType);
        mobilePhoneEntity.setMobile(mobile);

        mobilePhoneEntity.setAlrBattery(alr_battery);
        mobilePhoneEntity.setAlrPoweroff(alr_poweroff);
        mobilePhoneEntity.setAlrCall(alr_call);
        mobilePhoneEntity.setMode(mode);

        mobilePhoneEntity.setBellMode(bell_mode);
        mobilePhoneEntity.setUpdated(updated);
        mobilePhoneEntity.setFamilyGroupId(family_group_id);
        mobilePhoneEntity.setLight(light_time);
        mobilePhoneEntity.setStep_mode(step_mode);
        mobilePhoneEntity.setSchool_id(school_id);

        mobilePhoneEntity.setOld_version(old_version);
        mobilePhoneEntity.setNew_version(new_version);
        mobilePhoneEntity.setUpdate_info(update_info);
        mobilePhoneEntity.setUpdate_url(update_url);
        mobilePhoneEntity.setCharge(charge);
        mobilePhoneEntity.setContent();

        return mobilePhoneEntity;
    }


    public static GroupVersion getGroupVersion(int groupId, int type,
                                               int status, int version) {
        GroupVersion groupVersion = new GroupVersion();
        groupVersion.setGroupId(groupId);
        groupVersion.setType(type);
        groupVersion.setStats(status);
        groupVersion.setVersion(version);

        return groupVersion;
    }


    public static GroupEntity getGroupEntity(IMBaseDefine.GroupInfo groupInfo) {
        GroupEntity groupEntity = new GroupEntity();
        int timeNow = (int) (System.currentTimeMillis() / 1000);
        groupEntity.setUpdated(timeNow);
        groupEntity.setCreated(timeNow);
        groupEntity.setMainName(groupInfo.getGroupName());
        groupEntity.setAvatar(SystemConfigSp.instance().getStrConfig(
                SystemConfigSp.SysCfgDimension.MSFSSERVER)
                + groupInfo.getGroupAvatar());
        groupEntity.setCreatorId(groupInfo.getGroupCreatorId());
        groupEntity.setPeerId(groupInfo.getGroupId());
        groupEntity.setGroupType(getJavaGroupType(groupInfo.getGroupType()));
        groupEntity.setStatus(groupInfo.getShieldStatus());
        groupEntity.setUserCnt(groupInfo.getGroupMemberListCount());
        groupEntity.setVersion(groupInfo.getVersion());
        groupEntity.setlistGroupMemberIds(groupInfo.getGroupMemberListList());


        // groupEntity.setSave(getJavaSaveType(groupInfo.getStatus()));

        groupEntity.setBoard(groupInfo.getBoard());
        groupEntity.setBoardTime(String.valueOf(groupInfo.getBoardTime()));
        // guanweile

        // may be not good place
        PinYin.getPinYin(groupEntity.getMainName(),
                groupEntity.getPinyinElement());

        return groupEntity;
    }


    /**
     * 创建群时候的转化
     *
     * @param groupCreateRsp
     * @return
     */
    public static GroupEntity getGroupEntity(
            IMGroup.IMGroupCreateRsp groupCreateRsp) {
        GroupEntity groupEntity = new GroupEntity();
        int timeNow = (int) (System.currentTimeMillis() / 1000);
        groupEntity.setMainName(groupCreateRsp.getGroupName());
        groupEntity.setlistGroupMemberIds(groupCreateRsp.getUserIdListList());
        groupEntity.setCreatorId(groupCreateRsp.getUserId());
        groupEntity.setPeerId(groupCreateRsp.getGroupId());

        groupEntity.setUpdated(timeNow);
        groupEntity.setCreated(timeNow);
        groupEntity.setAvatar("");
        groupEntity.setGroupType(DBConstant.GROUP_TYPE_TEMP);
        groupEntity.setStatus(DBConstant.GROUP_STATUS_ONLINE);
        groupEntity.setUserCnt(groupCreateRsp.getUserIdListCount());
        groupEntity.setVersion(1);
        groupEntity.setSave(DBConstant.GROUP_MEMBER_STATUS_TEMP);
        groupEntity.setBoard("");
        groupEntity.setBoardTime("");


        PinYin.getPinYin(groupEntity.getMainName(),
                groupEntity.getPinyinElement());
        return groupEntity;
    }

    /**
     * 拆分消息在上层做掉 图文混排 在这判断
     */
    public static MessageEntity getMessageEntity(IMBaseDefine.MsgInfo msgInfo) {
        MessageEntity messageEntity = null;
        IMBaseDefine.MsgType msgType = msgInfo.getMsgType();
        switch (msgType) {
            case MSG_TYPE_SINGLE_AUDIO:
            case MSG_TYPE_GROUP_AUDIO:
                try {
                    /** 语音的解析不能转自 string再返回来 */
                    messageEntity = analyzeMsgAudio(msgInfo);
                } catch (JSONException e) {
                    return null;
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
                break;


            case MSG_TYPE_SINGLE_AUTH_SOUND:
            case MSG_TYPE_GROUP_AUTH_SOUND:
                try {
                    /** 语音的解析不能转自 string再返回来 */
                    messageEntity = analyzeMsgAudio(msgInfo);
                } catch (JSONException e) {
                    return null;
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
                break;


            case MSG_TYPE_GROUP_TEXT:
            case MSG_TYPE_SINGLE_TEXT:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_GROUP_AUTH_IMAGE:
            case MSG_TYPE_SINGLE_AUTH_IMAGE:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_VIDEO_CALL:
            case MSG_TYPE_VIDEO_ANSWER:
            case MSG_TYPE_VIDEO_CLOSE:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_SINGLE_IMAGE:
            case MSG_TYPE_GROUP_IMAGE:
                messageEntity = analyzeText(msgInfo);
                break;


            //case MSG_TYPE_DEVICE_COMMAND:
            //	break;
            case MSG_TYPE_SINGLE_NOTICE:
                messageEntity = analyzeText(msgInfo);
                break;

            case MSG_TYPE_SINGLE_BUSSINESS_CARD:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_GROUP_BUSSINESS_CARD:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_SINGLE_LOCATION:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_GROUP_LOCATION:
                messageEntity = analyzeText(msgInfo);
                break;

            case MSG_TYPE_SINGLE_VIDIO:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_GROUP_VIDIO:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_GROUP_EVENT_MSG: {
                IMBaseDefine.EventInfo eventInfo = null;
                try {
                    eventInfo = IMBaseDefine.EventInfo.parseFrom(Security.getInstance().DecryptMsg(msgInfo.getMsgData().toStringUtf8()));

                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
                messageEntity = analyzeDevMessage(eventInfo, msgInfo);
            }
            break;

            case MSG_TYPE_SINGLE_EVENT_MSG: {
                IMBaseDefine.EventInfo eventInfo = null;
                try {
                    eventInfo = IMBaseDefine.EventInfo.parseFrom(Security.getInstance().DecryptMsg(msgInfo.getMsgData().toStringUtf8()));

                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
                messageEntity = analyzeDevMessage(eventInfo, msgInfo);
            }
            break;
//		default:
//			throw new RuntimeException(
//					"ProtoBuf2JavaBean#getMessageEntity wrong type!");

        }
        return messageEntity;
    }

    public static MessageEntity analyzeText(IMBaseDefine.MsgInfo msgInfo) {
        return MsgAnalyzeEngine.analyzeMessage(msgInfo);
    }

    public static MessageEntity analyzeDevMessage(IMBaseDefine.EventInfo eventInfo, IMBaseDefine.MsgInfo msgInfo) {

        DevMessage devMessage = new DevMessage();
        devMessage.setFromId(msgInfo.getFromSessionId());
        devMessage.setMsgId(msgInfo.getMsgId());
        devMessage.setMsgType(getJavaMsgType(msgInfo.getMsgType()));
        devMessage.setStatus(MessageConstant.MSG_SUCCESS);
        devMessage.setDisplayType(DBConstant.SHOW_TYPE_DEV_MESSAGE);
        devMessage.setCreated(msgInfo.getCreateTime());
        devMessage.setUpdated(msgInfo.getCreateTime());
        long timeLong = msgInfo.getCreateTime();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(timeLong * 1000));


        // 内容的设定
        devMessage.setType(eventInfo.getEventKey().ordinal());
        devMessage.setSendId(msgInfo.getFromSessionId());
        devMessage.setMessageTime(date);

        String devCont = "";
        if (eventInfo.hasWiInfo()) {
            //devMessage.setAddressName(eventInfo.getWiInfo().getLactionX(), eventInfo.getWiInfo().getLactionY());
            if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_REPORT_BILL.ordinal()
                    || eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_DROP_DOWN.ordinal()) {

                //IMBaseDefine.JsonObject extraContent= eventInfo.
                //devCont =extraContent.getKeyValue();
                for (int i = 0; i < eventInfo.getKeyMapList().size(); i++) {
                    devCont = eventInfo.getKeyMap(i).getKeyValue();
                }
            }

            JSONObject extraContent = new JSONObject();
            try {
                extraContent.put("from_type", eventInfo.getWiInfo().getFromType().ordinal());
                extraContent.put("battery", eventInfo.getWiInfo().getBattery());
                extraContent.put("dev_content", devCont);

                if (eventInfo.getWiInfo().getLactionY().equals("")) {
                    extraContent.put("longitude", "0");
                } else {
                    extraContent.put("longitude", eventInfo.getWiInfo().getLactionY());
                }

                if (eventInfo.getWiInfo().getLactionX().equals("")) {
                    extraContent.put("latitude", "0");
                } else {
                    extraContent.put("latitude", eventInfo.getWiInfo().getLactionX());
                }

                if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_CURRENT_INFO.ordinal()) {
                  //  devMessage.setAddressName(eventInfo.getWiInfo().getLactionX(), eventInfo.getWiInfo().getLactionY());
                    devMessage.setAddress(eventInfo.getWiInfo().getAddrInfo());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            devMessage.setContent("" + extraContent.toString());

        } else {

            if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_REPORT_BILL.ordinal()) {

                int size = eventInfo.getKeyMapCount();
                String msgData = "";
                for (int i = 0; i < size; i++) {
                    msgData = msgData + eventInfo.getKeyMap(i).getKeyValue();
                    devMessage.setContent(msgData);
                }

            } else {
                devMessage.setContent("");
            }
        }

        return devMessage;
    }


    public static AudioMessage analyzeMsgAudio(IMBaseDefine.MsgInfo msgInfo)
            throws JSONException, UnsupportedEncodingException {
        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setFromId(msgInfo.getFromSessionId());
        audioMessage.setMsgId(msgInfo.getMsgId());
        audioMessage.setMsgType(getJavaMsgType(msgInfo.getMsgType()));
        audioMessage.setStatus(MessageConstant.MSG_SUCCESS);
        audioMessage.setReadStatus(MessageConstant.AUDIO_UNREAD);
        audioMessage.setDisplayType(DBConstant.SHOW_AUDIO_TYPE);
        audioMessage.setCreated(msgInfo.getCreateTime());
        audioMessage.setUpdated(msgInfo.getCreateTime());


        //序列化语音消息
//		IMMessage.IMAudioData data = null;
//		try {
//			data= IMMessage.IMAudioData.parseFrom( msgInfo.getMsgData());
//		} catch (InvalidProtocolBufferException e) {
//			e.printStackTrace();
//		}


        if (msgInfo != null) {

            ByteString bytes = msgInfo.getMsgData();
            //ByteString bytes = msgInfo.getMsgData();

            byte[] audioStream = bytes.toByteArray();
            if (audioStream.length < 4) {
                audioMessage.setReadStatus(MessageConstant.AUDIO_READED);
                audioMessage.setAudioPath("");
                audioMessage.setAudiolength(0);
            } else {
                int msgLen = audioStream.length;
                byte[] playTimeByte = new byte[4];
                byte[] audioContent = new byte[msgLen - 4];

                System.arraycopy(audioStream, 0, playTimeByte, 0, 4);
                System.arraycopy(audioStream, 4, audioContent, 0, msgLen - 4);
                int playTime = CommonUtil.byteArray2int(playTimeByte);
                String audioSavePath = FileUtil.saveAudioResourceToFile(
                        audioContent, audioMessage.getFromId());
                audioMessage.setAudiolength(playTime);
                audioMessage.setAudioPath(audioSavePath);
            }
        }

        /** 抽离出来 或者用gson */
        JSONObject extraContent = new JSONObject();
        extraContent.put("audioPath", audioMessage.getAudioPath());
        extraContent.put("audiolength", audioMessage.getAudiolength());
        extraContent.put("readStatus", audioMessage.getReadStatus());
        String audioContent = extraContent.toString();
        audioMessage.setContent(audioContent);

        return audioMessage;
    }


    public static AudioMessage analyzeAudio(IMBaseDefine.MsgInfo msgInfo)
            throws JSONException, UnsupportedEncodingException {
        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setFromId(msgInfo.getFromSessionId());
        audioMessage.setMsgId(msgInfo.getMsgId());
        audioMessage.setMsgType(getJavaMsgType(msgInfo.getMsgType()));
        audioMessage.setStatus(MessageConstant.MSG_SUCCESS);
        audioMessage.setReadStatus(MessageConstant.AUDIO_UNREAD);
        audioMessage.setDisplayType(DBConstant.SHOW_AUDIO_TYPE);
        audioMessage.setCreated(msgInfo.getCreateTime());
        audioMessage.setUpdated(msgInfo.getCreateTime());

        ByteString bytes = msgInfo.getMsgData();

        byte[] audioStream = bytes.toByteArray();
        if (audioStream.length < 4) {
            audioMessage.setReadStatus(MessageConstant.AUDIO_READED);
            audioMessage.setAudioPath("");
            audioMessage.setAudiolength(0);
        } else {
            int msgLen = audioStream.length;
            byte[] playTimeByte = new byte[4];
            byte[] audioContent = new byte[msgLen - 4];

            System.arraycopy(audioStream, 0, playTimeByte, 0, 4);
            System.arraycopy(audioStream, 4, audioContent, 0, msgLen - 4);
            int playTime = CommonUtil.byteArray2int(playTimeByte);
            String audioSavePath = FileUtil.saveAudioResourceToFile(
                    audioContent, audioMessage.getFromId());
            audioMessage.setAudiolength(playTime);
            audioMessage.setAudioPath(audioSavePath);
        }

        /** 抽离出来 或者用gson */
        JSONObject extraContent = new JSONObject();
        extraContent.put("audioPath", audioMessage.getAudioPath());
        extraContent.put("audiolength", audioMessage.getAudiolength());
        extraContent.put("readStatus", audioMessage.getReadStatus());
        String audioContent = extraContent.toString();
        audioMessage.setContent(audioContent);

        return audioMessage;
    }


    public static MessageEntity getMessageEntity(IMMessage.IMMsgData msgData) {

        MessageEntity messageEntity = null;
        IMBaseDefine.MsgType msgType = msgData.getMsgType();

        IMBaseDefine.MsgInfo msgInfo = IMBaseDefine.MsgInfo.newBuilder()
                .setMsgData(msgData.getMsgData()).setMsgId(msgData.getMsgId())
                .setMsgType(msgType).setCreateTime(msgData.getCreateTime())
                .setFromSessionId(msgData.getFromUserId()).build();

        switch (msgType) {
            case MSG_TYPE_SINGLE_AUDIO:
            case MSG_TYPE_GROUP_AUDIO:
                try {
                    messageEntity = analyzeAudio(msgInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_TYPE_GROUP_EVENT_MSG: {

                IMBaseDefine.EventInfo eventInfo = null;
                try {
                    eventInfo = IMBaseDefine.EventInfo.parseFrom(Security.getInstance().DecryptMsg(msgInfo.getMsgData().toStringUtf8()));

//				IMApplication.getPlaySound().mediaPlayer();
                    //开始充电
                    if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_BEGIN_CHARGING.ordinal()) {

                        UserEntity device = IMContactManager.instance().findDeviceContact(msgData.getFromUserId());
                        if (device != null) {
                            DeviceEntity rsp = IMDeviceManager.instance().findDeviceCard(
                                    device.getPeerId());
                            rsp.setCharge(DBConstant.BEGIN_CHARGING);
                            IMDeviceManager.instance().updateDeveiceConfigure(rsp);
                            IMDeviceManager.instance().triggerEvent(DeviceEvent.DEVICE_CHARGE_UPDATE_SUCCESS);
                        }
                        //结束充电
                    } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_END_CHARGING.ordinal()) {
                        UserEntity device = IMContactManager.instance().findDeviceContact(msgData.getFromUserId());
                        if (device != null) {
                            DeviceEntity rsp = IMDeviceManager.instance().findDeviceCard(
                                    device.getPeerId());
                            rsp.setCharge(DBConstant.END_CHARGING);
                            IMDeviceManager.instance().updateDeveiceConfigure(rsp);

                            IMDeviceManager.instance().triggerEvent(DeviceEvent.DEVICE_CHARGE_UPDATE_SUCCESS);

                        }
                    }

                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
                messageEntity = analyzeDevMessage(eventInfo, msgInfo);
            }
            break;


            case MSG_TYPE_SINGLE_EVENT_MSG: {

                IMBaseDefine.EventInfo eventInfo = null;
                try {
                    eventInfo = IMBaseDefine.EventInfo.parseFrom(Security.getInstance().DecryptMsg(msgInfo.getMsgData().toStringUtf8()));

//				IMApplication.getPlaySound().mediaPlayer();
                    //开始充电
                    if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_BEGIN_CHARGING.ordinal()) {

                        UserEntity device = IMContactManager.instance().findDeviceContact(msgData.getFromUserId());
                        if (device != null) {
                            DeviceEntity rsp = IMDeviceManager.instance().findDeviceCard(
                                    device.getPeerId());
                            rsp.setCharge(DBConstant.BEGIN_CHARGING);
                            IMDeviceManager.instance().updateDeveiceConfigure(rsp);
                            IMDeviceManager.instance().triggerEvent(DeviceEvent.DEVICE_CHARGE_UPDATE_SUCCESS);
                        }
                        //结束充电
                    } else if (eventInfo.getEventKey().ordinal() == IMBaseDefine.EventKey.EVENT_KEY_END_CHARGING.ordinal()) {
                        UserEntity device = IMContactManager.instance().findDeviceContact(msgData.getFromUserId());
                        if (device != null) {
                            DeviceEntity rsp = IMDeviceManager.instance().findDeviceCard(
                                    device.getPeerId());
                            rsp.setCharge(DBConstant.END_CHARGING);
                            IMDeviceManager.instance().updateDeveiceConfigure(rsp);

                            IMDeviceManager.instance().triggerEvent(DeviceEvent.DEVICE_CHARGE_UPDATE_SUCCESS);

                        }
                    }

                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
                messageEntity = analyzeDevMessage(eventInfo, msgInfo);
            }
            break;

            case MSG_TYPE_SINGLE_AUTH_SOUND:
            case MSG_TYPE_GROUP_AUTH_SOUND:
                try {
                    messageEntity = analyzeAudio(msgInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;

            case MSG_TYPE_GROUP_TEXT:
            case MSG_TYPE_SINGLE_TEXT:
                messageEntity = analyzeText(msgInfo);
                break;

            case MSG_TYPE_GROUP_AUTH_IMAGE:
            case MSG_TYPE_SINGLE_AUTH_IMAGE:
                messageEntity = analyzeText(msgInfo);
                break;

            case MSG_TYPE_VIDEO_CALL:
            case MSG_TYPE_VIDEO_ANSWER:
            case MSG_TYPE_VIDEO_CLOSE:
                messageEntity = analyzeText(msgInfo);
                break;

            case MSG_TYPE_SINGLE_IMAGE:
            case MSG_TYPE_GROUP_IMAGE:
                messageEntity = analyzeText(msgInfo);
                break;


            case MSG_TYPE_SINGLE_BUSSINESS_CARD:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_GROUP_BUSSINESS_CARD:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_SINGLE_LOCATION:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_GROUP_LOCATION:
                messageEntity = analyzeText(msgInfo);
                break;

            case MSG_TYPE_SINGLE_VIDIO:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_GROUP_VIDIO:
                messageEntity = analyzeText(msgInfo);
                break;
            //case MSG_TYPE_DEVICE_COMMAND:
            //	messageEntity = analyzeText(msgInfo);
            //	break;
            case MSG_TYPE_SINGLE_NOTICE:
                messageEntity = analyzeText(msgInfo);
                break;
            default:
                throw new RuntimeException(
                        "ProtoBuf2JavaBean#getMessageEntity wrong type!");
        }
        if (messageEntity != null) {
            messageEntity.setToId(msgData.getToSessionId());
        }

        /**
         * 消息的发送状态与 展示类型需要在上层做掉 messageEntity.setStatus();
         * messageEntity.setDisplayType();
         */
        return messageEntity;
    }

    public static UnreadEntity getUnreadEntity(IMBaseDefine.UnreadInfo pbInfo) {

        UnreadEntity unreadEntity = new UnreadEntity();
        unreadEntity
                .setSessionType(getJavaSessionType(pbInfo.getSessionType()));
        unreadEntity.setLatestMsgData(pbInfo.getLatestMsgData().toString());
        unreadEntity.setPeerId(pbInfo.getSessionId());
        unreadEntity.setLaststMsgId(pbInfo.getLatestMsgId());
        unreadEntity.setUnReadCnt(pbInfo.getUnreadCnt());
//		for(int i=0;i<pbInfo.getMsgIdListList().size();i++){
//			unreadEntity.getListMsgId().add(pbInfo.getMsgIdList(i));
//		}

        //设置未读的数量
        if (pbInfo.getMsgIdListList().size() > 0) {
            unreadEntity.getListMsgId().addAll(pbInfo.getMsgIdListList());
        }

        //unreadEntity.setListMsgId(pbInfo.getMsgIdListList());
        unreadEntity.buildSessionKey();
        return unreadEntity;
    }

    public static ReqFriendsEntity getUnreadFriendsEntity(int userId,
                                                          int tableReq, int messageReq) {
        ReqFriendsEntity unreadEntity = new ReqFriendsEntity();
        unreadEntity.setUser(userId);
        unreadEntity.setTableReq(tableReq);
        unreadEntity.setMessageReq(messageReq);

        return unreadEntity;
    }

    /**
     * ----enum 转化接口--
     */
    public static int getJavaMsgType(IMBaseDefine.MsgType msgType) {
        switch (msgType) {
            case MSG_TYPE_GROUP_TEXT:
                return DBConstant.MSG_TYPE_GROUP_TEXT;
            case MSG_TYPE_GROUP_AUDIO:
                return DBConstant.MSG_TYPE_GROUP_AUDIO;
            case MSG_TYPE_SINGLE_AUDIO:
                return DBConstant.MSG_TYPE_SINGLE_AUDIO;
            case MSG_TYPE_SINGLE_TEXT:
                return DBConstant.MSG_TYPE_SINGLE_TEXT;


            case MSG_TYPE_SINGLE_AUTH_IMAGE:
                return DBConstant.MSG_TYPE_SINGLE_AUTH_IMAGE;
            case MSG_TYPE_GROUP_AUTH_IMAGE:
                return DBConstant.MSG_TYPE_GROUP_AUTH_IMAGE;

            case MSG_TYPE_VIDEO_CALL:
                return DBConstant.MSG_TYPE_VIDEO_CALL;

            case MSG_TYPE_VIDEO_ANSWER:
                return DBConstant.MSG_TYPE_VIDEO_ANSWER;

            case MSG_TYPE_VIDEO_CLOSE:
                return DBConstant.MSG_TYPE_VIDEO_CLOSE;

            case MSG_TYPE_SINGLE_AUTH_SOUND:
                return DBConstant.MSG_TYPE_SINGLE_AUTH_SOUND;
            case MSG_TYPE_GROUP_AUTH_SOUND:
                return DBConstant.MSG_TYPE_GROUP_AUTH_SOUND;

            case MSG_TYPE_SINGLE_EVENT_MSG:
                return DBConstant.MSG_TYPE_GROUP_DEV_MESSAGE;
            case MSG_TYPE_GROUP_EVENT_MSG:
                return DBConstant.MSG_TYPE_SINGLE_DEV_MESSAGE;

            case MSG_TYPE_SINGLE_IMAGE:
                return DBConstant.MSG_TYPE_SINGLE_IMAGE;
            case MSG_TYPE_GROUP_IMAGE:
                return DBConstant.MSG_TYPE_GROUP_IMAGE;
            //	case MSG_TYPE_DEVICE_COMMAND:
            //	return DBConstant.MSG_TYPE_DEVICE_COMMAND;
            case MSG_TYPE_SINGLE_NOTICE:
                return DBConstant.MSG_TYPE_SINGLE_NOTICE;
            case MSG_TYPE_SINGLE_BUSSINESS_CARD:
                return DBConstant.MSG_TYPE_SINGLE_BUSSINESS_CARD;
            case MSG_TYPE_GROUP_BUSSINESS_CARD:
                return DBConstant.MSG_TYPE_GROUP_BUSSINESS_CARD;

            case MSG_TYPE_SINGLE_VIDIO:
                return DBConstant.MSG_TYPE_SINGLE_VIDIO;
            case MSG_TYPE_GROUP_VIDIO:
                return DBConstant.MSG_TYPE_GROUP_VIDIO;

            case MSG_TYPE_SINGLE_LOCATION:
                return DBConstant.MSG_TYPE_SINGLE_LOCATION;
            case MSG_TYPE_GROUP_LOCATION:
                return DBConstant.MSG_TYPE_GROUP_LOCATION;
            default:
                throw new IllegalArgumentException(
                        "msgType is illegal,cause by #getProtoMsgType#" + msgType);
        }
    }

    public static int getJavaSessionType(IMBaseDefine.SessionType sessionType) {
        switch (sessionType) {
            case SESSION_TYPE_SINGLE:
                return DBConstant.SESSION_TYPE_SINGLE;
            case SESSION_TYPE_GROUP:
                return DBConstant.SESSION_TYPE_GROUP;
            default:
                throw new IllegalArgumentException(
                        "sessionType is illegal,cause by #getProtoSessionType#"
                                + sessionType);
        }
    }

    public static int getJavaSaveType(GroupMemberStatus groupMemberStatus) {
        switch (groupMemberStatus) {
            case GROUP_MEMBER_STATUS_TEMP:
                return DBConstant.GROUP_MEMBER_STATUS_TEMP;
            case GROUP_MEMBER_STATUS_SAVE:
                return DBConstant.GROUP_MEMBER_STATUS_SAVE;
            case GROUP_MEMBER_STATUS_EXIT:
                return DBConstant.GROUP_MEMBER_STATUS_EXIT;

            default:
                throw new IllegalArgumentException(
                        "sessionType is illegal,cause by #getProtoSessionType#"
                                + groupMemberStatus);
        }
    }


    public static int getJavaGroupType(IMBaseDefine.GroupType groupType) {
        switch (groupType) {
            case GROUP_TYPE_NORMAL:
                return DBConstant.GROUP_TYPE_NORMAL;
            case GROUP_TYPE_TMP:
                return DBConstant.GROUP_TYPE_TEMP;
            case GROUP_TYPE_DEVICE:
                return DBConstant.GROUP_TYPE_FAMILY;

            default:
                throw new IllegalArgumentException(
                        "sessionType is illegal,cause by #getProtoSessionType#"
                                + groupType);
        }
    }

    public static int getGroupChangeType(IMBaseDefine.ChangeDataType modifyType) {
        switch (modifyType) {
            case CHANGE_GROUP_USER_ADD:
                return DBConstant.GROUP_MODIFY_TYPE_ADD;
            case CHANGE_GROUP_USER_DEL:
                return DBConstant.GROUP_MODIFY_TYPE_DEL;
            case CHANGE_GROUP_USER_ADD_BY_SCAN:
                return DBConstant.GROUP_USER_ADD_BY_SCAN;
            default:
                throw new IllegalArgumentException(
                        "GroupModifyType is illegal,cause by " + modifyType);
        }
    }

    public static int getDepartStatus(
            IMBaseDefine.DepartmentStatusType statusType) {
        switch (statusType) {
            case DEPT_STATUS_OK:
                return DBConstant.DEPT_STATUS_OK;
            case DEPT_STATUS_DELETE:
                return DBConstant.DEPT_STATUS_DELETE;
            default:
                throw new IllegalArgumentException(
                        "getDepartStatus is illegal,cause by " + statusType);
        }

    }


}

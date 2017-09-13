package com.fise.xiaoyu.imservice.manager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.fise.xiaoyu.DB.DBInterface;
import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.FamilyConcernEntity;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.RankingListEntity;
import com.fise.xiaoyu.DB.entity.ReqFriendsEntity;
import com.fise.xiaoyu.DB.entity.StepRanking;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.entity.WeiEntity;
import com.fise.xiaoyu.DB.entity.WhiteEntity;
import com.fise.xiaoyu.DB.sp.LoginSp;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.app.IMApplication;
import com.fise.xiaoyu.bean.TrajectoryInfo;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.DevVedioInfo;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.MessageConstant;
import com.fise.xiaoyu.imservice.callback.Packetlistener;
import com.fise.xiaoyu.imservice.entity.AddFriendsMessage;
import com.fise.xiaoyu.imservice.entity.AudioMessage;
import com.fise.xiaoyu.imservice.entity.PostionMessage;
import com.fise.xiaoyu.imservice.entity.RecentInfo;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.PriorityEvent;
import com.fise.xiaoyu.imservice.event.ReqEvent;
import com.fise.xiaoyu.imservice.event.ReqFriendsEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.event.VideoInfoEvent;
import com.fise.xiaoyu.imservice.service.LocalService;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMBaseDefine.AuthConfirmType;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ClientType;
import com.fise.xiaoyu.protobuf.IMBaseDefine.CommandType;
import com.fise.xiaoyu.protobuf.IMBaseDefine.SessionType;
import com.fise.xiaoyu.protobuf.IMBaseDefine.UserActionInfo;
import com.fise.xiaoyu.protobuf.IMBaseDefine.UserLocation;
import com.fise.xiaoyu.protobuf.IMBuddy;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.protobuf.IMSms.SmsActionType;
import com.fise.xiaoyu.protobuf.IMUserAction;
import com.fise.xiaoyu.protobuf.IMUserAction.ActionResult;
import com.fise.xiaoyu.protobuf.IMUserAction.ActionType;
import com.fise.xiaoyu.protobuf.helper.ProtoBuf2JavaBean;
import com.fise.xiaoyu.ui.activity.MainActivity;
import com.fise.xiaoyu.ui.base.ActivityManager;
import com.fise.xiaoyu.ui.helper.AudioPlayerHandler;
import com.fise.xiaoyu.ui.helper.AudioRecordHandler;
import com.fise.xiaoyu.utils.CommonUtil;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;
import com.google.protobuf.CodedInputStream;
import com.xiaowei.phone.PhoneMemberBean;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责用户信息的请求 为回话页面以及联系人页面提供服务
 * <p/>
 * 联系人信息管理 普通用户的version 有总版本 群组没有总version的概念， 每个群有version 具体请参见 服务端具体的pd协议
 */

public class IMUserActionManager extends IMManager {
    private Logger logger = Logger.getLogger(IMUserActionManager.class);

    // 单例
    private static IMUserActionManager inst = new IMUserActionManager();

    private AudioRecordHandler audioRecorderInstance = null;
    private Thread audioRecorderThread = null;
    public String audioSavePath;
    private AudioTimeCount time = new AudioTimeCount(20000, 1000);
    private int audioId;
    private int session_type;

    public static IMUserActionManager instance() {
        return inst;
    }

    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private DBInterface dbInterface = DBInterface.instance();
    private IMContactManager imContactManager = IMContactManager.instance();

    // 自身状态字段
    private boolean userDataReady = false;

    private Map<Integer, UserEntity> userMonitorMap = new ConcurrentHashMap<>();
    ArrayList<UserEntity> monitorDb = new ArrayList<>();
    private List<UserEntity> searchInfo = new ArrayList<UserEntity>();

    private List<TrajectoryInfo> infoLat = new ArrayList<TrajectoryInfo>();
    public ArrayList<UserEntity> phoneList = new ArrayList<>();

    public List<PhoneMemberBean> sourceList = new ArrayList<>();
    public List<IMUserAction.SchoolInfo> schoolList = new ArrayList<>();

    private String pass_code = "";
    private String modify_code = "";
    private Map<Integer, WeiEntity> userFriendsMap = new ConcurrentHashMap<>();
    private Map<Integer, WeiEntity> userYuMap = new ConcurrentHashMap<>();
    private Map<Integer, WeiEntity> rarentRefuseMap = new ConcurrentHashMap<>();

    private int step_cnt;
    private String step_date;
    private SessionType smsType = SessionType.SESSION_TYPE_GROUP;
    private int smsId = 0;

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

        // 雨友请求信息
        List<WeiEntity> userYulist = dbInterface.loadAllReqYuFriends();
        for (WeiEntity userInfo : userYulist) {
            userYuMap.put(userInfo.getFromId(), userInfo);
        }

        // 管理员同意或拒绝请求信息
        List<WeiEntity> refuselist = dbInterface.loadAllRefuse();
        for (WeiEntity userInfo : refuselist) {
            rarentRefuseMap.put(userInfo.getToId(), userInfo);
        }

        // 好友请求信息
        List<WeiEntity> userReqlist = dbInterface.loadAllUserReq();
        for (WeiEntity userInfo : userReqlist) {
            userFriendsMap.put(userInfo.getFromId(), userInfo);
        }

        triggerEvent(UserInfoEvent.USER_INFO_OK);
    }

    /**
     * 加载本地DB的状态 不管是离线还是在线登陆，loadFromDb 要运行的
     */
    public List<UserEntity> monitorFriends() {

        List<UserEntity> userlist = dbInterface.loadAllUsers();
        List<UserEntity> monitorDbTest = new ArrayList<>();

        for (UserEntity userInfo : userlist) { // guanweile
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            int loginId = IMLoginManager.instance().getLoginId();
            if (true && (loginId != userInfo.getPeerId())) {
                monitorDbTest.add(userInfo);
            }
        }
        return monitorDbTest;
    }

    /**
     * 网络链接成功，登陆之后请求
     */
    public void onLocalNetOk() {

    }

    @Override
    public void reset() {
        userDataReady = false;
        userMonitorMap.clear();
        searchInfo.clear();
        infoLat.clear();
        userFriendsMap.clear();
        userYuMap.clear();
        rarentRefuseMap.clear();
    }

    public List<UserEntity> getSearchInfo() {
        return searchInfo;
    }

    public void setSearchInfo(List<UserEntity> searchInfo) {
        this.searchInfo = searchInfo;
    }

    public void setSearchInfo(UserEntity Info) {
        this.searchInfo.clear();
        this.searchInfo.add(Info);
    }

    public void changePassword(int fromId, final String password) {

        String desPwd = new String(Security.getInstance().EncryptPass(password));
        IMUserAction.ChangePasswordReq passReq = IMUserAction.ChangePasswordReq
                .newBuilder().setFromId(fromId).setPassword(desPwd).build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_UPDATE_PASSWORD_REQ_VALUE;

        imSocketManager.sendRequest(passReq, sid, cid, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMUserAction.ChangePasswordRsp imPassRes = IMUserAction.ChangePasswordRsp
                            .parseFrom((CodedInputStream) response);
                    onChangePasswordRsp(imPassRes, password);
                } catch (IOException e) {
                    triggerEvent(UserInfoEvent.USER_MODIFY_PASS_FAIL);
                    logger.e("login failed,cause by %s", e.getCause());
                }
            }

            @Override
            public void onFaild() {
                triggerEvent(UserInfoEvent.USER_MODIFY_PASS_FAIL);
            }

            @Override
            public void onTimeout() {
                triggerEvent(UserInfoEvent.USER_MODIFY_PASS_FAIL);
            }
        });
    }

    public void onChangePasswordRsp(IMUserAction.ChangePasswordRsp imPassRes,
                                    String pass) {

        if (imPassRes == null) {
            triggerEvent(UserInfoEvent.USER_MODIFY_PASS_FAIL);
            return;
        }

        int code = imPassRes.getRetCode();

        switch (code) {
            case 0: {
                LoginSp.instance().setLoginInfo(
                        LoginSp.instance().getLoginIdentity().getLoginName(), pass,
                        LoginSp.instance().getLoginIdentity().getLoginId(),
                        LoginSp.instance().getLoginIdentity().getImei());

                triggerEvent(UserInfoEvent.USER_MODIFY_PASS_SUCCESS);
            }
            break;

            default: {
                modify_code = imPassRes.getRetMsg();
                triggerEvent(UserInfoEvent.USER_MODIFY_PASS_FAIL);
            }
            break;
        }
    }

    public String getModifyPass() {
        return modify_code;
    }

    /**
     * 计步排行榜信息
     *
     * @param fromId
     * @param user_id
     * @param step_date
     */
    public void stepRequest(int fromId, int user_id, String step_date) {

        IMUserAction.IMStepRequest passReq = IMUserAction.IMStepRequest
                .newBuilder().setFromId(fromId).setUserId(user_id)
                .setStepDate(step_date).build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_STEPCNT_REQ_VALUE;

        imSocketManager.sendRequest(passReq, sid, cid, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMUserAction.IMStepResponse imStepResponse = IMUserAction.IMStepResponse
                            .parseFrom((CodedInputStream) response);
                    onStepResponsedRsp(imStepResponse);
                } catch (IOException e) {
                    triggerEvent(UserInfoEvent.USER_STEP_FAIL);
                    logger.e("login failed,cause by %s", e.getCause());
                }
            }

            @Override
            public void onFaild() {
                triggerEvent(UserInfoEvent.USER_STEP_FAIL);
            }

            @Override
            public void onTimeout() {
                triggerEvent(UserInfoEvent.USER_STEP_FAIL);
            }
        });

    }

    public void onStepResponsedRsp(IMUserAction.IMStepResponse imStepResponse) {

        if (imStepResponse == null) {
            triggerEvent(UserInfoEvent.USER_STEP_FAIL);
            return;
        }

        int code = imStepResponse.getRetCode();

        switch (code) {
            case 0: {
                step_date = imStepResponse.getStepDate();
                step_cnt = imStepResponse.getStepCnt();
                triggerEvent(UserInfoEvent.USER_STEP_SUCCESS);
            }
            break;

            default: {
                triggerEvent(UserInfoEvent.USER_STEP_FAIL);
            }
            break;
        }
    }

    public String getStepdate() {
        return step_date;
    }

    public int getStepCnt() {
        return step_cnt;
    }

    public boolean isXiaoYu(String str) {

        // Pattern pattern = Pattern.compile("^[A-Za-z_$]+[A-Za-z_$\\d]+$");
        Pattern pattern = Pattern.compile("^[_a-zA-Z]\\w*$");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    /**
     * 发送搜索请求
     *
     * @param searReq
     */
    public void reqFriends(String searReq) {

        logger.i("login#reqLoginMsgServer");

        int userId = IMLoginManager.instance().getLoginId();

        IMUserAction.SearchType type = IMUserAction.SearchType.SEARCH_TYPE_PHONE;
        if (isXiaoYu(searReq)) {
            type = IMUserAction.SearchType.SEARCH_TYPE_XYNO; // LOGIN_TYPE_XWNO
        } else {
            type = IMUserAction.SearchType.SEARCH_TYPE_PHONE;
        }

        IMUserAction.SearchReq search = IMUserAction.SearchReq.newBuilder()
                .setSearchType(type).setUserId(userId).setSearchName(searReq)
                .build();
        // .setSearchType("").build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_SEARCH_REQ_VALUE;
        imSocketManager.sendRequest(search, sid, cid, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMUserAction.SearchRes imSearchRes = IMUserAction.SearchRes
                            .parseFrom((CodedInputStream) response);
                    onRepMsgServerReqFriends(imSearchRes);

                } catch (IOException e) {
                    triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_FAILED);
                    logger.e("login failed,cause by %s", e.getCause());
                }
            }

            @Override
            public void onFaild() {
                triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_FAILED);
            }

            @Override
            public void onTimeout() {
                triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_FAILED);
            }
        });
    }

    /**
     * 验证好友请求信息结果
     *
     * @param
     */
    public void onRepMsgServerReqFriends(IMUserAction.SearchRes imSearchRes) {
        logger.i("login#onRepMsgServerLogin");

        if (imSearchRes == null) {
            logger.e("login#decode LoginResponse failed");
            triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_FAILED);
            return;
        }

        IMBaseDefine.ResultType code = imSearchRes.getResultCode();

        switch (code) {
            case REFUSE_REASON_NONE: {

                searchInfo.clear();
                for (int i = 0; i < imSearchRes.getInfoListCount(); i++) {
                    IMBaseDefine.UserInfo userInfo = imSearchRes.getInfoList(i);
                    UserEntity info = ProtoBuf2JavaBean.getUserEntity(userInfo);
                    searchInfo.add(info);
                }
                triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_SUCCESS);
            }
            break;

            case REFUSE_REASON_DB_VALIDATE_FAILED: {
                triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_FAILED);
            }
            break;

            default: {
                triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_FAILED);
            }
            break;
        }
    }

    /**
     * 事件分发
     *
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
     * 打开或者取消位友监控的请求
     *
     * @param fromId
     * @param verifyType
     * @param actionType
     * @param value
     */
    public void verifyAuthValue(int fromId, AuthConfirmType verifyType,
                                SmsActionType actionType, String value, final int type) {

        IMUserAction.VerifyAuthValueReq verifyReq = IMUserAction.VerifyAuthValueReq
                .newBuilder().setFromId(fromId).setVerifyType(verifyType)
                .setActionType(actionType).setAuthValue(value).build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_VERIFY_VALUE_REQ_VALUE;
        imSocketManager.sendRequest(verifyReq, sid, cid, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {

                IMUserAction.VerifyAuthValueRsp authValueRsp;
                try {
                    authValueRsp = IMUserAction.VerifyAuthValueRsp
                            .parseFrom((CodedInputStream) response);
                    onVerifyAuthValue(authValueRsp, type);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            @Override
            public void onFaild() {
                triggerEvent(UserInfoEvent.USER_INFO_VERIFYAUTH_TIME_OUT);
            }

            @Override
            public void onTimeout() {
                triggerEvent(UserInfoEvent.USER_INFO_VERIFYAUTH_TIME_OUT);
            }
        });
    }

    /**
     * 原密码验证请求 (修改密码需要密码验证)
     *
     * @param authValueRsp
     */
    public void onVerifyAuthValue(IMUserAction.VerifyAuthValueRsp authValueRsp,
                                  int type) {

        // if(authValueRsp.getRetCode()!=0){
        // throw new RuntimeException("请求验证原始秘密不正确");
        // }
        //

        int code = authValueRsp.getRetCode();
        switch (code) {
            case 0: {

                if (type == 1) {
                    triggerEvent(UserInfoEvent.USER_INFO_VERIFYAUTH_PASS_SUCCESS);
                } else {
                    triggerEvent(UserInfoEvent.USER_INFO_VERIFYAUTH_SUCCESS);
                }

            }
            break;

            default: {
                logger.e("Avatar#Avatar msg server inner failed, result:%s", code);
                pass_code = authValueRsp.getRetMsg();
                triggerEvent(UserInfoEvent.USER_INFO_VERIFYAUTH_FAIL);

            }
            break;
        }

    }

    public String getPassCode() {
        return pass_code;
    }

    /**
     * 黑名单
     *
     * @param type
     * @param toId
     * @param entity
     */
    public void cancelBlackList(ActionType type, int toId,
                                final UserEntity entity) {

        int loginId = IMLoginManager.instance().getLoginId();
        IMUserAction.CommonRequest weiActionReq = IMUserAction.CommonRequest
                .newBuilder().setFromId(loginId).setToId(toId).setActId(0)
                .setActType(type).build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_VALUE;

        final ActionType Reqtype = type;
        final int showToId = toId;
        imSocketManager.sendRequest(weiActionReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {

                        IMUserAction.ActionAck imMsgDataAck = null;
                        try {
                            imMsgDataAck = IMUserAction.ActionAck
                                    .parseFrom((CodedInputStream) response);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (imMsgDataAck.getRetCode() != IMUserAction.RespResultCode.RESP_RESULT_YES) {
                            // 更新黑名单信息
                            triggerEvent(UserInfoEvent.USER_BLACKLIST_FAIL); // guanweile
                            throw new RuntimeException(
                                    "Msg ack error,cause by msgId <=0");
                        }

                        // 加入黑名单
                        if (Reqtype == ActionType.ACTION_TYPE_ADD_BLACKLIST) {

                            imContactManager.addBlackList(entity);
                            // 更新好友信息
                            triggerEvent(UserInfoEvent.USER_INFO_UPDATE); // guanweile

                            // 更新黑名单信息
                            triggerEvent(UserInfoEvent.USER_BLACKLIST_SUCCESS); // guanweile

                            // 移出黑名单
                        } else if (Reqtype == ActionType.ACTION_TYPE_DEL_BLACKLIST) {

                            imContactManager.removeBlackList(entity);
                            // 更新黑名单信息
                            triggerEvent(UserInfoEvent.USER_BLACKLIST_DEL_SUCCESS); // guanweile

                            // 更新好友信息
                            triggerEvent(UserInfoEvent.USER_INFO_UPDATE); // guanweile

                        }

                    }

                    @Override
                    public void onFaild() {
                        // 更新黑名单信息
                        triggerEvent(UserInfoEvent.USER_BLACKLIST_FAIL); // guanweile
                    }

                    @Override
                    public void onTimeout() {
                        // 更新黑名单信息
                        triggerEvent(UserInfoEvent.USER_BLACKLIST_FAIL); // guanweile

                    }
                });

    }

    /**
     * 请求获取加位友的请求
     *
     * @param lastUpdateTime
     */
    public void reqYuFriends(int lastUpdateTime) {

        int loginId = IMLoginManager.instance().getLoginId();
        IMUserAction.UserActionReq userActionReq = IMUserAction.UserActionReq
                .newBuilder().setFromId(loginId).setLastUpdate(lastUpdateTime)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_INFOLIST_REQ_VALUE;
        imSocketManager.sendRequest(userActionReq, sid, cid);
    }

    /**
     * 请求加好友的请求
     *
     * @param userInfo
     * @param msg_data
     */
    public void addReqFriends(UserEntity userInfo, String msg_data) {

        //
        // IMUserAction.ActionType type ;
        // if(Utils.isClientType(userInfo))
        // {
        // type = ActionType.ACTION_TYPE_DEVICE_ADD_FRIEND;
        // }else{
        // type = ActionType.ACTION_TYPE_NEW_FRIEDN;
        // }

        int loginId = IMLoginManager.instance().getLoginId();
        IMUserAction.CommonRequest friendsActionReq = IMUserAction.CommonRequest
                .newBuilder().setFromId(loginId).setToId(userInfo.getPeerId())
                .setActId(0).setActType(ActionType.ACTION_TYPE_NEW_FRIEDN)
                .setToChildDate(msg_data).setMsgData(msg_data).build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_VALUE;

        final UserEntity mUserInfo = userInfo;

        imSocketManager.sendRequest(friendsActionReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {

                        IMUserAction.ActionAck imMsgDataAck = null;
                        try {
                            imMsgDataAck = IMUserAction.ActionAck
                                    .parseFrom((CodedInputStream) response);

                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        // cuowu
                        if (imMsgDataAck.getRetCode() == IMUserAction.RespResultCode.RESP_RESULT_NOT_AUTH_YES) {

                            mUserInfo.setFriend(DBConstant.FRIENDS_TYPE_YES);
                            dbInterface.insertOrUpdateFriends(mUserInfo);

                            UserEntity userInfoInt = ProtoBuf2JavaBean
                                    .getUserCopyEntity(mUserInfo);
                            dbInterface.insertOrUpdateUser(userInfoInt);
                            triggerEvent(UserInfoEvent.USER_INFO_UPDATE);

                        } else if (imMsgDataAck.getRetCode() == IMUserAction.RespResultCode.RESP_RESULT_REPEAT_REQUEST) { // 已经是好友
                            triggerEvent(UserInfoEvent.USER_INFO_REQ_FRIENDS_ALREADY); // guanweile
                        } else if (imMsgDataAck.getRetCode() != IMUserAction.RespResultCode.RESP_RESULT_YES) {
                            throw new RuntimeException("处理失败");
                        } else {
                            triggerEvent(UserInfoEvent.USER_INFO_REQ_FRIENDS_SUCCESS); // guanweile
                        }

                    }

                    @Override
                    public void onFaild() {
                        triggerEvent(UserInfoEvent.USER_INFO_REQ_FRIENDS_FAIL); // guanweile

                    }

                    @Override
                    public void onTimeout() {
                        triggerEvent(UserInfoEvent.USER_INFO_REQ_FRIENDS_FAIL); // guanweile

                    }
                });
    }

    /**
     * 删除好友的请求
     *
     * @param toId
     * @param currentUser
     */
    public void deleteFriends(int toId, final UserEntity currentUser) {

        int loginId = IMLoginManager.instance().getLoginId();
        IMUserAction.CommonRequest deleteReq = IMUserAction.CommonRequest
                .newBuilder().setFromId(loginId).setToId(toId).setActId(0)
                .setActType(ActionType.ACTION_TYPE_DELETE_FRIEND).build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_VALUE;

        imSocketManager.sendRequest(deleteReq, sid, cid, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {

                IMUserAction.ActionAck imMsgDataAck = null;
                try {
                    imMsgDataAck = IMUserAction.ActionAck
                            .parseFrom((CodedInputStream) response);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // asdf
                if (imMsgDataAck.getRetCode() == IMUserAction.RespResultCode.RESP_RESULT_ACTION_ERROR) {
                    throw new RuntimeException("处理失败");
                }

                if (Utils.isClientType(currentUser)
                        && (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)) {

                    DeviceEntity rsp = IMDeviceManager.instance()
                            .findDeviceCard(currentUser.getPeerId());

                    // 删除会话窗口
                    String session = currentUser.getSessionKey();
                    imContactManager.deleteFriends(currentUser);

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

                    IMContactManager.instance().deleteDevUser(currentUser);

                    if (rsp != null) {
                        List<FamilyConcernEntity> list = IMDeviceManager
                                .instance().findFamilyConcern(
                                        currentUser.getPeerId());
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i) != null) {
                                IMDeviceManager.instance()
                                        .deleteFamilyConcernEntity(list.get(i));
                            }
                        }
                    }

                    List<WhiteEntity> whiteListTemp = IMDeviceManager
                            .instance().getWhiteListContactList(
                                    currentUser.getPeerId());
                    for (int i = 0; i < whiteListTemp.size(); i++) {
                        if (whiteListTemp.get(i) != null) {
                            IMDeviceManager.instance().deleteWhiteUser(
                                    whiteListTemp.get(i));
                        }
                    }

                    List<WhiteEntity> alarmListTemp = IMDeviceManager
                            .instance().getAlarmListContactList(
                                    currentUser.getPeerId());
                    for (int i = 0; i < alarmListTemp.size(); i++) {
                        if (alarmListTemp.get(i) != null) {
                            IMDeviceManager.instance().deleteAlarmUser(
                                    alarmListTemp.get(i));
                        }
                    }

                    IMDeviceManager.instance().LoaddAllWhite();
                    IMDeviceManager.instance().LoaddAllAlarm();

                    IMDeviceManager.instance().deleteFamilyConcernEntity();
                    triggerEvent(DeviceEvent.USER_INFO_DELETE_DEVICE_SUCCESS);

                } else {

                    String session = currentUser.getSessionKey();
                    imContactManager.deleteFriends(currentUser);

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
                }

            }

            @Override
            public void onFaild() {
                triggerEvent(UserInfoEvent.USER_INFO_DELETE_FAIL); //

            }

            @Override
            public void onTimeout() {
                triggerEvent(UserInfoEvent.USER_INFO_DELETE_FAIL); // guanweile

            }
        });
    }

    /**
     * 删除好友/位友 请求
     *
     * @param type
     * @param entity
     */
    public void deleteReqFriends(int type, WeiEntity entity) {

        if (type == DBConstant.DETLE_REQ_TYPE_YU) {
            dbInterface.deletUpdateReqYuFriends(entity);// (weiReq);
            userYuMap.remove(entity.getFromId());

        } else if (type == DBConstant.DETLE_REQ_TYPE) {
            dbInterface.insertOrDeleteUserFriends(entity);
            userFriendsMap.remove(entity.getFromId());

        } else if (type == DBConstant.DETLE_PARENT_REFUSE) {
            dbInterface.deletUpdateRefuse(entity);
            rarentRefuseMap.remove(entity.getFromId());
        }

        triggerEvent(UserInfoEvent.WEI_FRIENDS_WEI_REQ_ALL);
    }

    /**
     * 同意或者拒绝加位友的请求
     *
     * @param toId
     * @param act_id
     * @param actType
     * @param type
     * @param weiReq
     * @param msgData
     */
    public void confirmWeiFriends(int toId, int act_id, int actType,
                                  ActionResult type, final WeiEntity weiReq, String msgData) {

        final ActionType actionType = ActionType.values()[actType];
        int loginId = IMLoginManager.instance().getLoginId();
        IMUserAction.CommonConfirm weiActionReq = IMUserAction.CommonConfirm
                .newBuilder().setFromId(loginId).setToId(toId).setActId(act_id)
                .setActType(actionType).setResult(type).setMsgData(msgData)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_CONFIRM_VALUE;

        final ActionResult _type = type;
        final int id = toId;
        final int _actType = actType;
        imSocketManager.sendRequest(weiActionReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {

                        IMUserAction.ActionAck imMsgDataAck = null;
                        try {
                            imMsgDataAck = IMUserAction.ActionAck
                                    .parseFrom((CodedInputStream) response);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        if (imMsgDataAck.getRetCode() != IMUserAction.RespResultCode.RESP_RESULT_YES) {
                            throw new RuntimeException("处理失败");
                        }

                        if (_type == ActionResult.ACTION_RESULT_DELETE) {

                            weiReq.setStatus(1);
                            if (weiReq.getActType() == ActionType.ACTION_TYPE_NEW_FRIEDN
                                    .ordinal()) {

                                ReqFriendsEntity reqFriends = IMUnreadMsgManager
                                        .instance().findUnFriendsMap(
                                                weiReq.getFromId());
                                if (reqFriends != null) {
                                    IMUnreadMsgManager.instance()
                                            .removeUnReqFriends(reqFriends);
                                }
                                deleteReqFriends(DBConstant.DETLE_REQ_TYPE,
                                        weiReq);
                            } else if (weiReq.getActType() == ActionType.ACTION_TYPE_DEVICE_ADD_FRIEND
                                    .ordinal()) {

                                if (weiReq.getFromId() == IMLoginManager
                                        .instance().getLoginId()) { // 管理员同意或拒绝的脚表

                                    ReqFriendsEntity reqFriends = IMUnreadMsgManager
                                            .instance().findParentRefuseMap(
                                                    weiReq.getToId());
                                    if (reqFriends != null) {
                                        IMUnreadMsgManager.instance()
                                                .removeUnReqParentRefuse(
                                                        reqFriends);
                                    }
                                    deleteReqFriends(
                                            DBConstant.DETLE_PARENT_REFUSE,
                                            weiReq);

                                } else {

                                    ReqFriendsEntity reqFriends = IMUnreadMsgManager
                                            .instance().findUnYuFriendsMap(
                                                    weiReq.getFromId());
                                    if (reqFriends != null) {
                                        IMUnreadMsgManager.instance()
                                                .removeUnReqYuFriends(
                                                        reqFriends);
                                    }
                                    deleteReqFriends(
                                            DBConstant.DETLE_REQ_TYPE_YU,
                                            weiReq);
                                }

                            }

                            triggerEvent(UserInfoEvent.USER_INFO_UPDATE); // guanweile

                        }

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
     * 同意请求 好友同意
     *
     * @param toId
     * @param act_id
     * @param actType
     * @param type
     * @param weiReq
     * @param msgData
     * @param entity
     */
    public void confirmFriends(int toId, int act_id, int actType,
                               ActionResult type, final WeiEntity weiReq, String msgData,
                               UserEntity entity) {

        ActionType actionType = ActionType.values()[actType];

        int loginId = IMLoginManager.instance().getLoginId();
        IMUserAction.CommonConfirm weiActionReq = IMUserAction.CommonConfirm
                .newBuilder().setFromId(loginId).setToId(toId).setActId(act_id)
                .setActType(actionType).setResult(type).setMsgData(msgData)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_CONFIRM_VALUE;

        final ActionResult _type = type;
        final int id = toId;
        final int _actType = actType;
        final UserEntity agreeInfo = entity;

        imSocketManager.sendRequest(weiActionReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {

                        IMUserAction.ActionAck imMsgDataAck = null;
                        try {
                            imMsgDataAck = IMUserAction.ActionAck
                                    .parseFrom((CodedInputStream) response);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        if (imMsgDataAck.getRetCode() == IMUserAction.RespResultCode.RESP_RESULT_DEVICE_AGREE) {

                            // 同意了
                            weiReq.setStatus(DBConstant.FRIENDS_AGREE);
                            dbInterface.insertOrUpdateUserReqFriens(weiReq);// (weiReq);
                            userFriendsMap.put(weiReq.getFromId(), weiReq);

                            triggerEvent(UserInfoEvent.USER_INFO_REQ_ALL);

                        } else {
                            if (imMsgDataAck.getRetCode() != IMUserAction.RespResultCode.RESP_RESULT_YES) {
                                throw new RuntimeException("处理失败");
                            }

                            // 同意了
                            weiReq.setStatus(DBConstant.FRIENDS_AGREE);
                            dbInterface.insertOrUpdateUserReqFriens(weiReq);// (weiReq);
                            userFriendsMap.put(weiReq.getFromId(), weiReq);

                            // 登陆的是设备 需要家长同意 才设置为好友
                            if (!Utils.isClientType(IMLoginManager.instance()
                                    .getLoginInfo())) {
                                agreeInfo
                                        .setFriend(DBConstant.FRIENDS_TYPE_YES);
                                dbInterface.insertOrUpdateReqFriens(agreeInfo);

                                //
                                UserEntity userFriends = ProtoBuf2JavaBean
                                        .getUserCopyEntity(agreeInfo);
                                imContactManager
                                        .inserOrFriendsContact(userFriends);

                                UserEntity userInfo = ProtoBuf2JavaBean
                                        .getUserCopyEntity(agreeInfo);
                                imContactManager.insertOrUpdateUser(userInfo);

                                UserEntity loginUser = IMLoginManager
                                        .instance().getLoginInfo();

                                String content = "我通过了你的朋友验证请求，现在我们可以开始聊天了";

                                AddFriendsMessage textMessage = AddFriendsMessage
                                        .buildForSend(content, agreeInfo,
                                                loginUser, 0);
                                textMessage
                                        .setStatus(MessageConstant.MSG_SUCCESS);
                                long pkId = DBInterface.instance()
                                        .insertOrUpdateMessage(textMessage);
                                IMSessionManager.instance().updateSession(
                                        textMessage);

                                /**
                                 * 发送已读确认由上层的activity处理 特殊处理 1. 未读计数、
                                 * 通知、session页面 2. 当前会话
                                 * */
                                PriorityEvent notifyEvent = new PriorityEvent();
                                notifyEvent.event = PriorityEvent.Event.MSG_DEV_MESSAGE;
                                notifyEvent.object = textMessage;
                                triggerEvent(notifyEvent);

                            }
                            triggerEvent(UserInfoEvent.USER_INFO_REQ_ALL);
                        }

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
     * 请求详细的排行榜
     *
     * @param client_type
     * @param step_type
     */
    public void getStepRecordListRequest(final Long id, ClientType client_type,
                                         IMDevice.StepRecordType step_type, final int timeNew,
                                         final StepRanking stepRanking) {

        int loginId = IMLoginManager.instance().getLoginId();
        IMDevice.GetStepRecordListRequest setpRecordList = IMDevice.GetStepRecordListRequest
                .newBuilder().setUserId(loginId).setClientType(client_type)
                .setStepType(step_type).setStepDate(timeNew + "").build();

        int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
        int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_STEP_RECORD_REQ_VALUE;

        imSocketManager.sendRequest(setpRecordList, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {

                        try {
                            IMDevice.GetStepRecordListResponse imCrontabRsp = IMDevice.GetStepRecordListResponse
                                    .parseFrom((CodedInputStream) response);

                            IMBaseDefine.ResultType code = imCrontabRsp
                                    .getRetCode();
                            if (code != IMBaseDefine.ResultType.REFUSE_REASON_NONE) // 接收失败
                            {
                                triggerEvent(DeviceEvent.DEVICE_STEP_RECORD_LIST_FAILED);
                            }

                            if (id != -1) {
                                // 如果不是今天的数据　已经请求过的不要再请求
                                if (stepRanking != null) {

                                    long lt = new Long(timeNew);
                                    Date date = new Date(lt * 1000L);

                                    long lt1 = new Long(System
                                            .currentTimeMillis() / 1000);
                                    Date date1 = new Date(lt1 * 1000L);

                                    if (stepRanking != null
                                            && ((date.getMonth() != date1
                                            .getMonth()) || (date
                                            .getDate() != date1
                                            .getDate()))) {
                                        stepRanking.setLatest_data(1);
                                        dbInterface
                                                .insertOrUpdateStepRanking(stepRanking);
                                    }

                                }
                            }

                            // if(id !=-1)
                            deleteStepRankingList(id);

                            for (int i = 0; i < imCrontabRsp.getStepListCount(); i++) {

                                // int timeNow = (int)
                                // (System.currentTimeMillis() / 1000);
                                RankingListEntity rankingListEntity = new RankingListEntity();
                                rankingListEntity.setRankingId(id);
                                rankingListEntity.setChampion_id(imCrontabRsp
                                        .getStepList(i).getUserId());
                                rankingListEntity.setStep_num(imCrontabRsp
                                        .getStepList(i).getStepCnt());
                                rankingListEntity.setCreate_time(Integer
                                        .parseInt(imCrontabRsp.getStepDate()));
                                rankingListEntity.setUpdate_time(Integer
                                        .parseInt(imCrontabRsp.getStepDate()));
                                dbInterface
                                        .insertOrUpdateRankingList(rankingListEntity);
                            }

                            triggerEvent(DeviceEvent.DEVICE_RANKING_LIST_SUCCESS);

                        } catch (IOException e) {
                            triggerEvent(DeviceEvent.DEVICE_RANKING_LIST_FAILED);
                        }
                    }

                    @Override
                    public void onFaild() {
                        triggerEvent(DeviceEvent.DEVICE_RANKING_LIST_FAILED);
                    }

                    @Override
                    public void onTimeout() {
                        triggerEvent(DeviceEvent.DEVICE_RANKING_LIST_FAILED);
                    }
                });

    }

    /**
     * 请求名次列表
     *
     * @param client_type
     * @param step_type
     */
    public void getRankingListRequest(ClientType client_type,
                                      IMDevice.StepRecordType step_type, final int timeNow) {

        int loginId = IMLoginManager.instance().getLoginId();
        IMDevice.GetStepRecordListRequest setpRecordList = IMDevice.GetStepRecordListRequest
                .newBuilder().setUserId(loginId).setClientType(client_type)
                .setStepType(step_type).setStepDate(timeNow + "").build();

        int sid = IMBaseDefine.ServiceID.SID_DEVICE_VALUE;
        int cid = IMBaseDefine.DeviceCmdID.CID_DEVICE_STEP_RECORD_REQ_VALUE;

        imSocketManager.sendRequest(setpRecordList, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {

                        try {
                            IMDevice.GetStepRecordListResponse imCrontabRsp = IMDevice.GetStepRecordListResponse
                                    .parseFrom((CodedInputStream) response);

                            IMBaseDefine.ResultType code = imCrontabRsp
                                    .getRetCode();
                            if (code != IMBaseDefine.ResultType.REFUSE_REASON_NONE) // 接收失败
                            {
                                triggerEvent(DeviceEvent.DEVICE_STEP_RECORD_LIST_FAILED);
                            }

                            boolean data = true;
                            if ((imCrontabRsp.getStepListCount() == 1)
                                    && (imCrontabRsp.getStepList(0)
                                    .getStepCnt() == 0)) {
                                data = false;
                            }
                            // 如果有数据表示这个日期有数据
                            if (imCrontabRsp.getStepListCount() > 0 && data) {

                                updateStepRankingList(timeNow);
                                // int timeNow = (int)
                                // (System.currentTimeMillis() / 1000);

                                StepRanking stepRanking = new StepRanking();
                                stepRanking.setRanking(imCrontabRsp
                                        .getStepRank());

                                for (int i = 0; i < imCrontabRsp
                                        .getStepListCount(); i++) {
                                    if (imCrontabRsp.getStepList(i).getUserId() == IMLoginManager
                                            .instance().getLoginId()) {
                                        stepRanking.setStep_num(imCrontabRsp
                                                .getStepList(i).getStepCnt());
                                        stepRanking
                                                .setChampion_id(IMLoginManager
                                                        .instance()
                                                        .getLoginId());
                                    } else {
                                        if (imCrontabRsp.getStepList(i)
                                                .getUserId() == 0) {
                                            stepRanking
                                                    .setChampion_id(IMLoginManager
                                                            .instance()
                                                            .getLoginId());
                                        } else {
                                            stepRanking
                                                    .setChampion_id(imCrontabRsp
                                                            .getStepList(i)
                                                            .getUserId());
                                        }
                                    }
                                }

                                stepRanking.setCreate_time(Integer
                                        .parseInt(imCrontabRsp.getStepDate()));
                                stepRanking.setUpdate_time(Integer
                                        .parseInt(imCrontabRsp.getStepDate()));
                                dbInterface
                                        .insertOrUpdateStepRanking(stepRanking);

                                triggerEvent(DeviceEvent.DEVICE_STEP_RECORD_LIST_SUCCESS);
                            }

                        } catch (IOException e) {
                            triggerEvent(DeviceEvent.DEVICE_STEP_RECORD_LIST_FAILED);

                        }
                    }

                    @Override
                    public void onFaild() {
                        triggerEvent(DeviceEvent.DEVICE_STEP_RECORD_LIST_FAILED);
                    }

                    @Override
                    public void onTimeout() {
                        triggerEvent(DeviceEvent.DEVICE_STEP_RECORD_LIST_FAILED);
                    }
                });

    }

    /**
     * 如果是time的时间　在数据中对应删除 再添加
     *
     * @param timeNow
     */
    public void updateStepRankingList(int timeNow) {
        List<StepRanking> list = dbInterface.loadAllStepRanking();
        for (int i = 0; i < list.size(); i++) {

            long lt = new Long(timeNow);
            Date date = new Date(lt * 1000L);

            long lt1 = new Long(list.get(i).getUpdate_time());
            Date date1 = new Date(lt1 * 1000L);

            if ((date.getMonth() == date1.getMonth())
                    && (date.getDate() == date1.getDate())) {
                deleteStepRankingList(list.get(i).getId());
                dbInterface.deleteOrUpdateStepRanking(list.get(i));
            }
        }
    }

    public List<StepRanking> getStepRankingList() {
        List<StepRanking> list = dbInterface.loadAllStepRanking();
        Collections.sort(list, new Comparator<StepRanking>() {

            @Override
            public int compare(StepRanking o1, StepRanking o2) {
                int i = o2.getUpdate_time() - o1.getUpdate_time();
                return i;
            }
        });

        return list;
    }

    /**
     * 删除指定排名id 的排行榜
     *
     * @param Id
     */
    public void deleteStepRankingList(Long Id) {
        List<RankingListEntity> list = dbInterface.loadAllRankingList();
        for (int i = 0; i < list.size(); i++) {
            if (Id == list.get(i).getRankingId()) {
                dbInterface.deleteOrUpdateRankingList(list.get(i));
            }
        }
    }

    public List<RankingListEntity> getRankingList(Long Id) {

        List<RankingListEntity> list = dbInterface.loadAllRankingList();
        List<RankingListEntity> data = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (Id == list.get(i).getRankingId()) {
                data.add(list.get(i));
            }
        }
        return data;
    }

    public StepRanking getSetpRanking(Long id) {
        List<StepRanking> list = dbInterface.loadAllStepRanking();
        StepRanking stepRanking = null;
        for (int i = 0; i < list.size(); i++) {

            if (list.get(i).getId() == id) {
                stepRanking = list.get(i);
                break;
            }
        }
        return stepRanking;
    }

    public List<RankingListEntity> getRankingList() {
        List<RankingListEntity> list = dbInterface.loadAllRankingList();
        return list;
    }

    /**
     * 同意请求 雨友同意
     *
     * @param toId
     * @param act_id
     * @param actType
     * @param type
     * @param yuReq
     * @param msgData
     */
    public void confirmYuFriends(int toId, int act_id, int actType,
                                 final ActionResult type, final WeiEntity yuReq, String msgData,
                                 int deviceId) {

        final ActionType actionType = ActionType.values()[actType];

        int loginId = IMLoginManager.instance().getLoginId();
        IMUserAction.CommonConfirm weiActionReq = IMUserAction.CommonConfirm
                .newBuilder().setFromId(loginId).setToId(toId).setActId(act_id)
                .setDeviceId(deviceId).setActType(actionType).setResult(type)
                .setMsgData(msgData).build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_CONFIRM_VALUE;

        final ActionResult _type = type;
        final int id = toId;
        final int _actType = actType;

        imSocketManager.sendRequest(weiActionReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {

                        IMUserAction.ActionAck imMsgDataAck = null;
                        try {
                            imMsgDataAck = IMUserAction.ActionAck
                                    .parseFrom((CodedInputStream) response);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        if (imMsgDataAck.getRetCode() != IMUserAction.RespResultCode.RESP_RESULT_YES) {
                            throw new RuntimeException("处理失败");
                        }

                        // 管理员同意了
                        if (actionType == ActionType.ACTION_TYPE_DEVICE_ADD_FRIEND) {
                            if (IMUserAction.ActionResult.ACTION_RESULT_YES == type) {
                                yuReq.setStatus(DBConstant.FRIENDS_AGREE);
                            } else if (IMUserAction.ActionResult.ACTION_RESULT_NO == type) {
                                yuReq.setStatus(DBConstant.FRIENDS_REFUSE);
                            }

                            dbInterface.insertOrUpdateReqYuFriends(yuReq);
                            userYuMap.put(yuReq.getFromId(), yuReq);
                        }

                        triggerEvent(UserInfoEvent.USER_INFO_REQ_ALL);

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
     * 命令(例如抓拍等命令)
     *
     * @param fromId
     * @param toId
     * @param type
     * @param commandType
     * @param ext_value   ext_value
     * @param isShow      是否弹出toast，
     */
    public void UserP2PCommand(int fromId, int toId, SessionType type,
                               CommandType commandType, String ext_value, final boolean isShow) {

        // int loginId = IMLoginManager.instance().getLoginId();
        IMUserAction.P2PCommand commandReq = IMUserAction.P2PCommand
                .newBuilder().setFromId(fromId).setToId(toId)
                .setCmdType(commandType).setType(type).setExtValue(ext_value)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMAND_VALUE;
        final CommandType _commandType = commandType;
        imSocketManager.sendRequest(commandReq, sid, cid, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {

                IMUserAction.ActionAck imMsgDataAck = null;
                try {
                    imMsgDataAck = IMUserAction.ActionAck
                            .parseFrom((CodedInputStream) response);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (imMsgDataAck.getRetCode() == IMUserAction.RespResultCode.RESP_RESULT_ACTION_ERROR) { // 当对方离线
                    // throw new RuntimeException("处理失败");
                    Log.i("aaa", "onSuccess: USER_P2PCOMMAND_OFFLINE");
                    triggerEvent(UserInfoEvent.USER_P2PCOMMAND_OFFLINE_HINT);

                } else if (imMsgDataAck.getRetCode() == IMUserAction.RespResultCode.RESP_RESULT_YES) { // 当发送成功

                    // triggerEvent(UserInfoEvent.USER_P2PCOMMAND_ONLINE);

                    if (_commandType == CommandType.COMMAND_TYPE_TAKE_PHOTO) {
                        triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_TAKE_PHOTO);
                    } else if (_commandType == CommandType.COMMAND_TYPE_SOUND_COPY) {
                        triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_SOUND_COPY);

                    } else if (_commandType == CommandType.COMMAND_TYPE_DEVICE_CALLBACK) { // 回拨

                        if (isShow) {
                            triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_DEVICE_CALLBACK);
                        } else {
                            triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_DEVICE_CALLBACK_ACTIVITY);
                        }

                    } else if (_commandType == CommandType.COMMAND_TYPE_DEVICE_BILL) {
                        triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_DEVICE_BELL);

                    } else if (_commandType == CommandType.COMMAND_TYPE_DEVICE_CURRENT_INFO) { // 同步数据
                        // adf
                        if (isShow) {
                            triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_DEVICE_CURRENT);
                        } else {
                            // activity 获取实时位置
                            triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_DEVICE_CURRENT_ACTIVITY);
                        }

                    } else if (_commandType == CommandType.COMMAND_TYPE_DEVICE_SHUTDOWN) { // 断电控制
                        triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_DEVICE_SHUTDOWN);

                    } else if (_commandType == CommandType.COMMAND_TYPE_DEVICE_VERSION_UPDATE) { // 固件升级
                        triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_DEVICE_VERSION_UPDATE);

                    } else if (_commandType == CommandType.COMMAND_TYPE_DEVICE_DOWNLOAD) {

                    } else if (_commandType == CommandType.COMMAND_TYPE_DEVICE_BEGIN_VIDEO) { // 设备视频通话

                        String ext_value = imMsgDataAck.getExtValue();
                        JSONObject extraContent = null;
                        try {
                            extraContent = new JSONObject(ext_value);
                            int type = 4;
                            String pullUrl = "";
                            String pushUrl = "";
                            if (!extraContent.isNull("type")) {
                                type = extraContent.getInt("type");
                            }

                            if (type == 0) {
                                String repeateValue;
                                if (!extraContent.isNull("url")) {
                                    repeateValue = extraContent
                                            .getString("url");
                                    String[] sourceStrArray = repeateValue
                                            .split(";");

                                    for (int i = 0; i < sourceStrArray.length; i++) {
                                        if (i == 0) {
                                            pushUrl = sourceStrArray[i];
                                        } else if (i == 1) {
                                            pullUrl = sourceStrArray[i];
                                        }
                                    }
                                }
                            }

                            DevVedioInfo info = new DevVedioInfo(type, pullUrl,
                                    pushUrl);
                            PriorityEvent notifyEvent = new PriorityEvent();
                            notifyEvent.event = PriorityEvent.Event.MSG_VEDIO_ONLINE_DEV_START;
                            notifyEvent.object = info;
                            triggerEvent(notifyEvent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_DEVICE_ONLINE);
                    }

                } else {
                    triggerEvent(UserInfoEvent.USER_P2PCOMMAND_FAIL);
                    throw new RuntimeException("处理失败");
                }

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
     * 手机通讯录好友查询是否注册小位
     *
     * @param from_id
     * @param list
     */
    public void onPhoneBookFriendReq(int from_id, List<String> list) { // ,

        IMUserAction.DiscoverFriendReq imDiscoverFriendReq = IMUserAction.DiscoverFriendReq
                .newBuilder().setFromId(from_id).addAllPhoneList(list).build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_DISCOVERY_FRIEND_REQ_VALUE;

        imSocketManager.sendRequest(imDiscoverFriendReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {

                        IMUserAction.DiscoverFriendRsp DiscoverFriendRsp;
                        try {
                            DiscoverFriendRsp = IMUserAction.DiscoverFriendRsp
                                    .parseFrom((CodedInputStream) response);
                            onDiscoverFriendRsp(DiscoverFriendRsp);

                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFaild() {
                        triggerEvent(UserInfoEvent.USER_PHONE_FAIL);
                        // 注册失败
                    }

                    @Override
                    public void onTimeout() {
                        triggerEvent(UserInfoEvent.USER_PHONE_FAIL);
                        // 注册超时
                    }
                });

    }

    /**
     * 通讯录查询回复
     *
     * @param imDiscoverFriendRsp
     */
    public void onDiscoverFriendRsp(
            IMUserAction.DiscoverFriendRsp imDiscoverFriendRsp) {

        int code = imDiscoverFriendRsp.getRetCode(); // getActId
        if (code != 0) {
            triggerEvent(UserInfoEvent.USER_PHONE_FAIL);
            return;
        }

        phoneList.clear();
        List<IMBaseDefine.UserInfo> changeList = imDiscoverFriendRsp
                .getUserListList();
        for (IMBaseDefine.UserInfo userInfo : changeList) {
            UserEntity entity = ProtoBuf2JavaBean.getUserEntity(userInfo);
            IMContactManager.instance().insertOrUpdateUser(entity);
            phoneList.add(entity);
        }
        triggerEvent(UserInfoEvent.USER_PHONE_SUCCESS);

    }

    public ArrayList<UserEntity> getPhoneUserList() {

        return phoneList;
    }

    public List<PhoneMemberBean> getPhoneMemberBeanList() {
        return sourceList;
    }

    public void setPhoneMemberBeanList(List<PhoneMemberBean> sourceList) {
        this.sourceList = sourceList;
    }

    /**
     * 自身的事件驱动
     *
     * @param event
     */
    public void triggerEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    /**
     * 请求位置信息
     *
     * @param userId
     * @param statTime
     * @param endTime
     */
    public void onRepLocationReq(int userId, long statTime, long endTime) {

        int usrId = IMLoginManager.instance().getLoginId();
        IMUserAction.LocationReq imLocation = IMUserAction.LocationReq
                .newBuilder().setUserId(userId).setFromId(usrId)
                .setStartTime(((int) statTime)).setEndTime(((int) endTime))
                .build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_LOCATION_REQ_VALUE;

        final int queryId = userId;
        final long statTimeTemp = statTime;
        final long endTimeTemp = endTime;
        imSocketManager.sendRequest(imLocation, sid, cid, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMUserAction.LocationRsp LocationRsp = IMUserAction.LocationRsp
                            .parseFrom((CodedInputStream) response);
                    onLocationRsp(LocationRsp, queryId, statTimeTemp,
                            endTimeTemp);

                } catch (IOException e) {
                    triggerEvent(LoginEvent.REGIST_INNER_FAILED);
                    // 注册失败
                    logger.e("login failed,cause by %s", e.getCause());
                }
            }

            @Override
            public void onFaild() {
                triggerEvent(UserInfoEvent.USER_INFO_UPDATE_QUERY_FAIL);
                // 注册失败
            }

            @Override
            public void onTimeout() {
                triggerEvent(UserInfoEvent.USER_INFO_UPDATE_QUERY_FAIL);
                // 注册超时
            }
        });
    }

    /**
     * 请求位置回复
     *
     * @param imLocationRsp
     */
    public void onLocationRsp(IMUserAction.LocationRsp imLocationRsp,
                              int queryId, long startTime, long endTime) {

        infoLat.clear();
        int fromId = imLocationRsp.getFromId();
        int userId = imLocationRsp.getUserId();
        List<UserLocation> Location = imLocationRsp.getInfoListList();
        for (int i = 0; i < Location.size(); i++) {

            double latLng = 0;
            double Lng = 0;
            int time = 0;
            try {
                latLng = (new Double(Location.get(i).getLat())).doubleValue();
                Lng = (new Double(Location.get(i).getLng())).doubleValue();

            } catch (NumberFormatException e) {
                System.out.println("s is not a number");
            }

            time = Location.get(i).getCreated();

            TrajectoryInfo info = new TrajectoryInfo(latLng, Lng, time);
            infoLat.add(info);
        }

        if (IMContactManager.instance().findDeviceContact(queryId) != null) {

            if (startTime == 0 || endTime == 0.0) {
                if (Location.size() > 0) {
                    UserEntity entity = IMContactManager.instance()
                            .findDeviceContact(queryId);
                    double latLng = 0;
                    double Lng = 0;

                    latLng = (new Double(Location.get(Location.size() - 1)
                            .getLat())).doubleValue();
                    Lng = (new Double(Location.get(Location.size() - 1)
                            .getLng())).doubleValue();

                    entity.setLongitude(latLng);
                    entity.setLongitude(Lng);
                    entity.setLocationType(Location.get(Location.size() - 1)
                            .getFromType());
                    IMContactManager.instance().updateOrDevice(entity);

                }
            }
        }

        List<TrajectoryInfo> lat1 = infoLat;
        triggerEvent(UserInfoEvent.USER_INFO_UPDATE_QUERY_SUCCESS);

    }

    public List<TrajectoryInfo> onLocation() {
        return infoLat;
    }

    /**
     * 位置信息回复
     *
     * @param imLocationRsp
     */
    public void onRepLocationRsp(IMUserAction.LocationRsp imLocationRsp) {

        int fromId = imLocationRsp.getFromId();
        int userId = imLocationRsp.getUserId();
        List<UserLocation> Location = imLocationRsp.getInfoListList();

        List<UserEntity> list = imContactManager.getContacMonitorList();

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getPeerId() == userId) {
                if (Location.size() <= 0) {
                    list.get(i).setLongitude(0.0);
                    list.get(i).setLongitude(0.0);
                    list.get(i).setLoseMonitor(false);

                    if (list.get(i).getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU
                            && Utils.isClientType(list.get(i))) {
                        imContactManager.updateOrDevice(list.get(i));
                    } else if (list.get(i).getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU
                            && (!Utils.isClientType(list.get(i)))) {
                        imContactManager.UpdateWeiFriendsContact(list.get(i));
                    }

                } else {

                    String lnt = Location.get(0).getLng();
                    String lat = Location.get(0).getLat();

                    if (lnt == "") {
                        list.get(i).setLongitude(0.0);
                    } else {
                        list.get(i).setLongitude(Double.parseDouble(lnt));
                    }

                    if (lat == "") {
                        list.get(i).setLatitude(0.0);
                    } else {
                        list.get(i).setLatitude(Double.parseDouble(lat));
                    }

                    list.get(i).setLoseMonitor(true);
                    list.get(i).setLocationType(Location.get(0).getFromType());

                    if (list.get(i).getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU
                            && Utils.isClientType(list.get(i))) {
                        imContactManager.updateOrDevice(list.get(i));
                    } else if (list.get(i).getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU
                            && (!Utils.isClientType(list.get(i)))) {
                        imContactManager.UpdateWeiFriendsContact(list.get(i));
                    }

                }
            }
        }

        triggerEvent(UserInfoEvent.USER_INFO_UPDATE_WEIFRIENDS);

    }

    /**
     * 接收到攝像頭視頻下載ack
     *
     * @param videoCommandAck
     */

    public void onRepCameraVideoRsp(IMUserAction.P2PCommandAck videoCommandAck) {

        String extValue = videoCommandAck.getExtValue();
        try {
            JSONObject extraContent = new JSONObject(extValue);
            String videoUrl = extraContent.getString("url");
            String videoName = extraContent.getString("name");
            VideoInfoEvent videoInfoEvent = new VideoInfoEvent();
            videoInfoEvent.videoName = videoName;
            videoInfoEvent.videoUrl = videoUrl;
            videoInfoEvent.event = VideoInfoEvent.Event.GET_NET_VIDEO_URL_SUCCESS;
            triggerEvent(videoInfoEvent);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 接收到同意获取拒绝通知
     *
     * @param imWeiRsp
     */
    public void onRepConfirmRequest(IMUserAction.CommonConfirm imWeiRsp) {

        int act_it = imWeiRsp.getActId(); // getActId
        if (imWeiRsp.getActType() == ActionType.ACTION_TYPE_NEW_FRIEDN) {

            UserEntity entity = imContactManager.findContact(imWeiRsp
                    .getFromId());

            entity.setFriend(DBConstant.FRIENDS_TYPE_YES);
            imContactManager.inserOrFriendsContact(entity);

            UserEntity userInfoInt = ProtoBuf2JavaBean
                    .getUserCopyEntity(entity);
            imContactManager.insertOrUpdateUser(entity);

            UserEntity loginUser = IMLoginManager.instance().getLoginInfo();

            String content = "我通过了你的朋友验证请求，现在我们可以开始聊天了";

            AddFriendsMessage textMessage = AddFriendsMessage.buildForSend(
                    content, entity, loginUser, 0);

            textMessage.setStatus(MessageConstant.MSG_SUCCESS);
            long pkId = DBInterface.instance().insertOrUpdateMessage(
                    textMessage);
            IMSessionManager.instance().updateSession(textMessage);

            /**
             * 发送已读确认由上层的activity处理 特殊处理 1. 未读计数、 通知、session页面 2. 当前会话
             * */
            PriorityEvent notifyEvent = new PriorityEvent();
            notifyEvent.event = PriorityEvent.Event.MSG_DEV_MESSAGE;
            notifyEvent.object = textMessage;
            triggerEvent(notifyEvent);

            triggerEvent(UserInfoEvent.USER_INFO_UPDATE);

            // 雨友请求
        } else if (imWeiRsp.getActType() == ActionType.ACTION_TYPE_DEVICE_ADD_FRIEND) {

            if (imWeiRsp.getFromId() == IMLoginManager.instance().getLoginId()) { // 管理员同意拒绝
                // 的请求

                int status = DBConstant.FRIENDS_REFUSE;
                if (imWeiRsp.getResult() == ActionResult.ACTION_RESULT_YES) {
                    status = DBConstant.FRIENDS_AGREE;
                } else if (imWeiRsp.getResult() == ActionResult.ACTION_RESULT_NO) {
                    status = DBConstant.FRIENDS_REFUSE;
                }

                WeiEntity weiEntity = findParentRefuseEntity(imWeiRsp.getToId());
                if (weiEntity == null) {
                    weiEntity = new WeiEntity();
                    int timeNow = (int) (System.currentTimeMillis() / 1000);

                    weiEntity.setFromId(imWeiRsp.getFromId());
                    weiEntity.setToId(imWeiRsp.getToId());

                    weiEntity.setActId(imWeiRsp.getActId());
                    weiEntity.setActType(imWeiRsp.getActType().ordinal());
                    weiEntity.setStatus(status);
                    weiEntity.setUpdated(timeNow);
                    weiEntity.setDevice_id(imWeiRsp.getDeviceId());
                    weiEntity.setMasgData(imWeiRsp.getMsgData());

                } else {

                    int timeNow = (int) (System.currentTimeMillis() / 1000);

                    weiEntity.setFromId(imWeiRsp.getFromId());
                    weiEntity.setToId(imWeiRsp.getToId());
                    weiEntity.setActId(imWeiRsp.getActId());
                    weiEntity.setActType(imWeiRsp.getActType().ordinal());
                    weiEntity.setStatus(status);
                    weiEntity.setUpdated(timeNow);
                    weiEntity.setDevice_id(imWeiRsp.getDeviceId());
                    weiEntity.setMasgData(imWeiRsp.getMsgData());
                }

                dbInterface.insertOrUpdateRefuse(weiEntity);// (weiReq);
                rarentRefuseMap.put(weiEntity.getToId(), weiEntity);

                // 位友请求标注
                ReqFriendsEntity reqFriends = ProtoBuf2JavaBean
                        .getUnreadFriendsEntity(imWeiRsp.getToId(), 0, 0);
                IMUnreadMsgManager.instance().updateUnParentRefuse(reqFriends);

                ReqEvent reqEvent = new ReqEvent();
                reqEvent.event = ReqEvent.Event.REQ_FRIENDS_MESSAGE;

                reqEvent.entity = weiEntity;
                triggerEvent(reqEvent);

            } else {

                int id = imWeiRsp.getDeviceId();
                if (imWeiRsp.getResult() == ActionResult.ACTION_RESULT_YES) {

                    if (imWeiRsp.getDeviceId() == IMLoginManager.instance()
                            .getLoginId()) {
                        id = imWeiRsp.getToId();
                    } else {
                        id = imWeiRsp.getDeviceId();

                    }
                    UserEntity userEntity = imContactManager.findContact(id);
                    if (userEntity != null) {
                        userEntity.setFriend(DBConstant.FRIENDS_TYPE_YES);
                        imContactManager.inserOrFriendsContact(userEntity);

                        UserEntity entity1 = ProtoBuf2JavaBean
                                .getUserCopyEntity(userEntity);
                        imContactManager.insertOrUpdateUser(entity1);

                    } else {
                        ArrayList<Integer> userIds = new ArrayList<>(1);
                        // just single type
                        userIds.add(id);
                        imContactManager.reqGetDetaillUsers(userIds);
                    }
                }

                int status = DBConstant.FRIENDS_PENDING_REVIEW;
                if (imWeiRsp.getResult() == ActionResult.ACTION_RESULT_YES) {
                    status = DBConstant.FRIENDS_AGREE;
                } else if (imWeiRsp.getResult() == ActionResult.ACTION_RESULT_NO) {
                    status = DBConstant.FRIENDS_REFUSE;
                }

                // 发起端
                if (id != IMLoginManager.instance().getLoginId()) {

                    WeiEntity weiEntity = findYuEntity(id);
                    if (weiEntity == null) {
                        weiEntity = new WeiEntity();
                        int timeNow = (int) (System.currentTimeMillis() / 1000);

                        if (imWeiRsp.getDeviceId() == IMLoginManager.instance()
                                .getLoginId()) {
                            weiEntity.setFromId(imWeiRsp.getToId());
                            weiEntity.setToId(imWeiRsp.getDeviceId());
                        } else {
                            weiEntity.setFromId(imWeiRsp.getFromId());
                            weiEntity.setToId(imWeiRsp.getToId());
                        }

                        weiEntity.setActId(imWeiRsp.getActId());
                        weiEntity.setActType(imWeiRsp.getActType().ordinal());
                        weiEntity.setStatus(status);
                        weiEntity.setUpdated(timeNow);
                        weiEntity.setDevice_id(imWeiRsp.getDeviceId());
                        weiEntity.setMasgData(imWeiRsp.getMsgData());

                    } else {

                        int timeNow = (int) (System.currentTimeMillis() / 1000);

                        if (imWeiRsp.getDeviceId() == IMLoginManager.instance()
                                .getLoginId()) {
                            weiEntity.setFromId(imWeiRsp.getToId());
                            weiEntity.setToId(imWeiRsp.getDeviceId());
                        } else {
                            weiEntity.setFromId(imWeiRsp.getFromId());
                            weiEntity.setToId(imWeiRsp.getToId());
                        }
                        weiEntity.setActId(imWeiRsp.getActId());
                        weiEntity.setActType(imWeiRsp.getActType().ordinal());
                        weiEntity.setStatus(status);
                        weiEntity.setUpdated(timeNow);
                        weiEntity.setDevice_id(imWeiRsp.getDeviceId());
                        weiEntity.setMasgData(imWeiRsp.getMsgData());
                    }

                    dbInterface.insertOrUpdateReqYuFriends(weiEntity);// (weiReq);
                    userYuMap.put(weiEntity.getFromId(), weiEntity);

                    if (imWeiRsp.getDeviceId() == IMLoginManager.instance()
                            .getLoginId()) {

                        // 位友请求标注
                        ReqFriendsEntity reqFriends = ProtoBuf2JavaBean
                                .getUnreadFriendsEntity(imWeiRsp.getToId(), 0,
                                        0);
                        IMUnreadMsgManager.instance().updateUnReqYuFriends(
                                reqFriends);

                        ReqEvent reqEvent = new ReqEvent();
                        reqEvent.event = ReqEvent.Event.REQ_FRIENDS_MESSAGE;

                        reqEvent.entity = weiEntity;
                        triggerEvent(reqEvent);

                    }
                }

            }

            triggerEvent(UserInfoEvent.USER_INFO_REQ_YU); // guanweile

        }
    }

    /**
     * 接收命令请求 (抓拍等命令)
     *
     * @param commandRsp
     */
    public void onP2PCommandRequest(IMUserAction.P2PCommand commandRsp) {

        CommandType cmd_type = commandRsp.getCmdType();
        if (cmd_type == CommandType.COMMAND_TYPE_TAKE_PHOTO) {// 拍照

            int type = commandRsp.getType().ordinal();
            Intent loadImageIntent = new Intent(ctx, LocalService.class);
            loadImageIntent.putExtra(IntentConstant.KEY_PEERID,
                    commandRsp.getFromId());
            loadImageIntent.putExtra(IntentConstant.KEY_SESSION_TYPE,
                    commandRsp.getType().ordinal());
            ctx.startService(loadImageIntent);

        } else if (cmd_type == CommandType.COMMAND_TYPE_SOUND_COPY) { // 录音

            if (AudioPlayerHandler.getInstance().isPlaying())
                AudioPlayerHandler.getInstance().stopPlayer();

            session_type = commandRsp.getType().ordinal();
            audioId = commandRsp.getFromId();
            audioSavePath = CommonUtil.getAudioSavePath(commandRsp.getFromId());

            audioRecorderInstance = new AudioRecordHandler(audioSavePath);
            audioRecorderThread = new Thread(audioRecorderInstance);
            audioRecorderInstance.setRecording(true);
            audioRecorderThread.start();

            time.start();
        } else if (cmd_type == CommandType.COMMAND_TYPE_DEVICE_BEGIN_VIDEO) { // 开始视频通话

            String ext_value = commandRsp.getExtValue();
            JSONObject extraContent = null;
            try {
                extraContent = new JSONObject(ext_value);
                int type = 4;
                String pullUrl = "";
                String pushUrl = "";
                if (!extraContent.isNull("type")) {
                    type = extraContent.getInt("type");
                }

                if (type == 0) {
                    String repeateValue;
                    if (!extraContent.isNull("url")) {
                        repeateValue = extraContent.getString("url");
                        String[] sourceStrArray = repeateValue.split(";");

                        for (int i = 0; i < sourceStrArray.length; i++) {
                            if (i == 0) {
                                pushUrl = sourceStrArray[i];
                            } else if (i == 1) {
                                pullUrl = sourceStrArray[i];
                            }
                        }
                    }
                }

                DevVedioInfo info = new DevVedioInfo(type, pullUrl, pushUrl);
                PriorityEvent notifyEvent = new PriorityEvent();
                notifyEvent.event = PriorityEvent.Event.MSG_VEDIO_ONLINE_DEV;
                notifyEvent.object = info;
                triggerEvent(notifyEvent);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (cmd_type == CommandType.COMMAND_TYPE_DEVICE_END_VIDEO) {// 结束视频通话

        } else if (cmd_type == CommandType.COMMAND_TYPE_DEVICE_CALLBACK) {// 回拨

            String phone = commandRsp.getExtValue();
            //用intent启动拨打电话
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            if (ActivityCompat.checkSelfPermission(IMApplication.getApplication(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                Utils.showToast(IMApplication.getApplication(), "没有拨号权限");
                return;
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                IMApplication.getApplication().startActivity(intent);
            }

        } else if (cmd_type == CommandType.COMMAND_TYPE_DEVICE_SHUTDOWN) { // 远程关机

            Intent shutdownIntent = new Intent("com.android.fise.ACTION_SHUTDOWN");
            IMApplication.getApplication().sendBroadcast(shutdownIntent);

        } else if (cmd_type == CommandType.COMMAND_TYPE_DEVICE_BILL) {// 话费上报

            if (IMApplication.getBillType() == DBConstant.IMSI_TYPE_MOBILE) {// 移动
                IMApplication.getApplication().sendSMS("10086", "101");
            } else if (IMApplication.getBillType() == DBConstant.IMSI_TYPE_UNICOM) { // 联通
                IMApplication.getApplication().sendSMS("10010", "101");
            } else if (IMApplication.getBillType() == DBConstant.IMSI_TYPE_TELECOM) {// 电信
                IMApplication.getApplication().sendSMS("10000", "101");
            }

            smsType = commandRsp.getType();
            smsId = commandRsp.getFromId();
        } else if (cmd_type == CommandType.COMMAND_TYPE_DEVICE_CURRENT_INFO) { // 上报设备的最新位置信息

            IMDeviceManager.instance().postionPublishEvent(
                    IMBaseDefine.EventKey.EVENT_KEY_CURRENT_INFO, commandRsp.getFromId(),commandRsp.getType());

            /*
            if (commandRsp.getType().ordinal() == SessionType.SESSION_TYPE_GROUP
                    .ordinal()) {
                IMDeviceManager.instance().postionPublishEvent(
                        IMBaseDefine.EventKey.EVENT_KEY_CURRENT_INFO);
            } else if (commandRsp.getType().ordinal() == SessionType.SESSION_TYPE_SINGLE
                    .ordinal()) {

                UserEntity userEntity = IMContactManager.instance()
                        .findParentContact(commandRsp.getFromId());
                if (userEntity == null) {
                    userEntity = IMContactManager.instance().findDeviceContact(
                            commandRsp.getFromId());
                }
                if (userEntity != null) {
//                    PostionMessage postionMessage = PostionMessage
//                            .buildForSend(MainActivity.latitude,
//                                    MainActivity.longitude,
//                                    MainActivity.address, IMLoginManager
//                                            .instance().getLoginInfo(),
//                                    userEntity, DBConstant.XIAOYU_POSTION_TYPE,
//                                    MainActivity.locationType);
//                    IMMessageManager.instance().sendPostion(postionMessage,
//                            true);
                    IMDeviceManager.instance().postionPublishEvent(
                            IMBaseDefine.EventKey.EVENT_KEY_CURRENT_INFO,);

                }
            }*/
        }
    }

    public SessionType getSmsType() {
        return smsType;
    }

    public int getSmsId() {
        return smsId;
    }

    /**
     * 接收到请求通知
     *
     * @param imWeiRsp
     */
    public void onRepFriendRequest(IMUserAction.CommonRequest imWeiRsp) {

        int peeid = imWeiRsp.getFromId();
        int toId = imWeiRsp.getToId();
        int act_id = imWeiRsp.getActId();
        int act_type = imWeiRsp.getActType().ordinal();

        if (imWeiRsp.getActType() == ActionType.ACTION_TYPE_NEW_FRIEDN) {

            UserEntity loginUser = IMLoginManager.instance().getLoginInfo();

            if (loginUser.getFriendNeedAuth() == 0) {
                ArrayList<Integer> userIds = new ArrayList<>(1);
                // just single type
                userIds.add(peeid);
                reqGetDetaillUserFriends(userIds, 0);

            } else {

                ArrayList<Integer> userIds = new ArrayList<>(1);
                // just single type
                userIds.add(peeid);
                reqGetDetaillUserFriends(userIds, 1);

                // 好友请求列表数据
                int timeNow = (int) (System.currentTimeMillis() / 1000);

                WeiEntity weiEntity = findUserReqEntity(peeid);
                if (weiEntity == null) {

                    weiEntity = new WeiEntity();
                    weiEntity.setFromId(peeid);
                    weiEntity.setToId(toId);
                    weiEntity.setActId(act_id);
                    weiEntity.setActType(act_type);
                    weiEntity.setDevice_id(imWeiRsp.getDeviceId());
                    weiEntity.setStatus(DBConstant.FRIENDS_PENDING_REVIEW);
                    weiEntity.setUpdated(timeNow);
                    weiEntity.setMasgData(imWeiRsp.getMsgData());
                } else {

                    weiEntity.setFromId(peeid);
                    weiEntity.setToId(toId);
                    weiEntity.setActId(act_id);
                    weiEntity.setDevice_id(imWeiRsp.getDeviceId());
                    weiEntity.setActType(act_type);
                    weiEntity.setStatus(DBConstant.FRIENDS_PENDING_REVIEW);
                    weiEntity.setUpdated(timeNow);
                    weiEntity.setMasgData(imWeiRsp.getMsgData());
                }

                dbInterface.insertOrUpdateUserReqFriens(weiEntity);// (weiReq);
                userFriendsMap.put(weiEntity.getFromId(), weiEntity);

                // 位友请求标注
                ReqFriendsEntity reqFriends = ProtoBuf2JavaBean
                        .getUnreadFriendsEntity(peeid, 0, 0);
                IMUnreadMsgManager.instance().updateUnReqFriends(reqFriends);

                ReqEvent reqEvent = new ReqEvent();
                reqEvent.event = ReqEvent.Event.REQ_FRIENDS_MESSAGE;
                reqEvent.entity = weiEntity;
                triggerEvent(reqEvent);

                triggerEvent(UserInfoEvent.WEI_FRIENDS_INFO_REQ_ALL);

            }

        } else if (imWeiRsp.getActType() == ActionType.ACTION_TYPE_DEVICE_ADD_FRIEND) {

        } else if (imWeiRsp.getActType() == ActionType.ACTION_TYPE_DEVICE_ADD_FRIEND) {

            // 设备好友请求列表数据
            int timeNow = (int) (System.currentTimeMillis() / 1000);

            WeiEntity weiEntity = findYuEntity(peeid);
            if (weiEntity == null) {

                weiEntity = new WeiEntity();
                weiEntity.setFromId(peeid);
                weiEntity.setToId(toId);
                weiEntity.setActId(act_id);
                weiEntity.setActType(act_type);
                weiEntity.setDevice_id(imWeiRsp.getDeviceId());
                weiEntity.setStatus(DBConstant.FRIENDS_PENDING_REVIEW);
                weiEntity.setUpdated(timeNow);
                weiEntity.setMasgData(imWeiRsp.getMsgData());
            } else {

                weiEntity.setFromId(peeid);
                weiEntity.setToId(toId);
                weiEntity.setActId(act_id);
                weiEntity.setActType(act_type);
                weiEntity.setDevice_id(imWeiRsp.getDeviceId());
                weiEntity.setStatus(DBConstant.FRIENDS_PENDING_REVIEW);
                weiEntity.setUpdated(timeNow);
                weiEntity.setMasgData(imWeiRsp.getMsgData());
            }

            dbInterface.insertOrUpdateReqYuFriends(weiEntity);// (weiReq);
            userYuMap.put(weiEntity.getFromId(), weiEntity);

            // 位友请求标注
            ReqFriendsEntity reqFriends = ProtoBuf2JavaBean
                    .getUnreadFriendsEntity(peeid, 0, 0);
            IMUnreadMsgManager.instance().updateUnReqYuFriends(reqFriends);

            ReqEvent reqEvent = new ReqEvent();
            reqEvent.event = ReqEvent.Event.REQ_FRIENDS_MESSAGE;
            reqEvent.entity = weiEntity;
            triggerEvent(reqEvent);

            triggerEvent(UserInfoEvent.WEI_FRIENDS_INFO_REQ_ALL);

        }
    }

    /**
     * 请求用户详细信息
     *
     * @param userIds
     */
    public void reqGetDetaillUserFriends(ArrayList<Integer> userIds, int isAuth) {
        logger.i("contact#contact#reqGetDetaillUsers");
        if (null == userIds || userIds.size() <= 0) {
            logger.i("contact#contact#reqGetDetaillUsers return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMUsersInfoReq imUsersInfoReq = IMBuddy.IMUsersInfoReq
                .newBuilder().setUserId(loginId).addAllUserIdList(userIds)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_USER_INFO_REQUEST_VALUE;

        final int _isAuth = isAuth;
        imSocketManager.sendRequest(imUsersInfoReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {
                        try {

                            IMBuddy.IMUsersInfoRsp imAllUserRsp = IMBuddy.IMUsersInfoRsp
                                    .parseFrom((CodedInputStream) response);
                            onRepDetailUsersReq(imAllUserRsp, _isAuth);

                        } catch (IOException e) {
                            triggerEvent(UserInfoEvent.USER_INFO_DATA_FAIL);

                            logger.e("login failed,cause by %s", e.getCause());
                        }
                    }

                    @Override
                    public void onFaild() {
                        triggerEvent(UserInfoEvent.USER_INFO_DATA_FAIL);

                    }

                    @Override
                    public void onTimeout() {
                        triggerEvent(UserInfoEvent.USER_INFO_DATA_FAIL);

                    }
                });

    }

    /**
     * 好友请求的个人信息
     *
     * @param imUsersInfoRsp
     * @param _isAuth
     */
    public void onRepDetailUsersReq(IMBuddy.IMUsersInfoRsp imUsersInfoRsp,
                                    int _isAuth) {

        List<IMBaseDefine.UserInfo> userInfoList = imUsersInfoRsp
                .getUserInfoListList();

        if (_isAuth == 0) {

            ArrayList<UserEntity> needDb = new ArrayList<>();
            ArrayList<UserEntity> friendsDbReq = new ArrayList<>();

            for (IMBaseDefine.UserInfo userInfo : userInfoList) {

                UserEntity entity = ProtoBuf2JavaBean.getUserEntity(userInfo);
                entity.setFriend(DBConstant.FRIENDS_TYPE_YES);

                friendsDbReq.add(entity);

                UserEntity entityUser = ProtoBuf2JavaBean
                        .getUserCopyEntity(entity);
                needDb.add(entityUser);
                imContactManager.inserOrFriendsContact(entity);
                imContactManager.insertOrUpdateUser(entityUser);

            }

            triggerEvent(UserInfoEvent.USER_INFO_UPDATE); //

        } else {
            for (IMBaseDefine.UserInfo userInfo : userInfoList) {

                UserEntity entity = ProtoBuf2JavaBean.getUserEntity(userInfo);
                UserEntity entityUser = ProtoBuf2JavaBean
                        .getUserCopyEntity(entity);
                dbInterface.insertOrUpdateReqFriens(entity);
                imContactManager.insertOrUpdateUser(entityUser);

                int UserId = entity.getPeerId();
                ReqFriendsEntity reqFriends = ProtoBuf2JavaBean
                        .getUnreadFriendsEntity(UserId, 0, 0);
                IMUnreadMsgManager.instance().updateUnReqFriends(reqFriends);
            }

            triggerEvent(UserInfoEvent.USER_INFO_REQ_UPDATE);
        }

    }

    /**
     * 请求回复　离线
     *
     * @param imWeiRsp
     */
    public void onRepWeiFriends(IMUserAction.UserActionRsp imWeiRsp) {

        int actCnt = imWeiRsp.getActCnt();
        if (actCnt <= 0) {
            return;
        }

        ArrayList<WeiEntity> needDb = new ArrayList<>();
        ArrayList<UserEntity> needDbReq = new ArrayList<>();
        ArrayList<ReqFriendsEntity> reqUnFriends = new ArrayList<>();

        List<UserActionInfo> changeList = imWeiRsp.getActionListList();
        for (UserActionInfo weiInfo : changeList) {

            // 自己发出去的消息不作处理
            if (weiInfo.getFromId() == IMLoginManager.instance().getLoginId()) {
                return;
            }

            if (weiInfo.getActType() == ActionType.ACTION_TYPE_NEW_FRIEDN
                    .ordinal()) {

                ReqFriendsEntity reqFriends = IMUnreadMsgManager.instance()
                        .findUnFriendsMap(weiInfo.getFromId());

                if (reqFriends == null) {
                    ReqFriendsEntity reqUnMessage = ProtoBuf2JavaBean
                            .getUnreadFriendsEntity(weiInfo.getFromId(), 0, 0);
                    IMUnreadMsgManager.instance().updateUnReqFriends(
                            reqUnMessage);
                } else {

                    reqFriends.setMessageReq(1);
                    reqFriends.setTableReq(1);

                    IMUnreadMsgManager.instance()
                            .updateUnReqFriends(reqFriends);
                }

                dbInterface.batchInsertOrUpdateReq(needDbReq);
                // 好友请求列表数据
                int timeNow = (int) (System.currentTimeMillis() / 1000);

                WeiEntity weiEntity = findUserReqEntity(weiInfo.getFromId());
                if (weiEntity == null) {

                    weiEntity = new WeiEntity();
                    weiEntity.setFromId(weiInfo.getFromId());
                    weiEntity.setToId(weiInfo.getToId());
                    weiEntity.setActId(weiInfo.getActId());
                    weiEntity.setActType(weiInfo.getActType());
                    weiEntity.setStatus(weiInfo.getStatus());
                    weiEntity.setUpdated(timeNow);
                    weiEntity.setMasgData(weiInfo.getMsgData());
                } else {

                    weiEntity.setFromId(weiInfo.getFromId());
                    weiEntity.setToId(weiInfo.getToId());
                    weiEntity.setActId(weiInfo.getActId());
                    weiEntity.setActType(weiInfo.getActType());
                    weiEntity.setStatus(weiInfo.getStatus());
                    weiEntity.setUpdated(timeNow);
                    weiEntity.setMasgData(weiInfo.getMsgData());
                }

                dbInterface.insertOrUpdateUserReqFriens(weiEntity);// (weiReq);
                userFriendsMap.put(weiEntity.getFromId(), weiEntity);

                triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
                if (reqUnFriends.size() > 0) {
                    triggerEvent(UserInfoEvent.USER_INFO_REQ_UPDATE);
                }

            } else if (weiInfo.getActType() == ActionType.ACTION_TYPE_DEVICE_ADD_FRIEND
                    .ordinal()) {

                if (weiInfo.getFromId() == IMLoginManager.instance()
                        .getLoginId()) { // 管理员拒绝 或者同意的请求

                    WeiEntity entity = findParentRefuseEntity(weiInfo.getToId());
                    if (entity == null) {
                        entity = new WeiEntity();
                        int timeNow = (int) (System.currentTimeMillis() / 1000);

                        entity.setFromId(weiInfo.getFromId());
                        entity.setToId(weiInfo.getToId());
                        entity.setActId(weiInfo.getActId());
                        entity.setActType(weiInfo.getActType());
                        entity.setStatus(weiInfo.getStatus());
                        entity.setUpdated(timeNow);
                        entity.setDevice_id(weiInfo.getDeviceId());
                        entity.setMasgData(weiInfo.getMsgData());

                    } else {

                        int timeNow = (int) (System.currentTimeMillis() / 1000);

                        entity.setFromId(weiInfo.getFromId());
                        entity.setToId(weiInfo.getToId());
                        entity.setActId(weiInfo.getActId());
                        entity.setActType(weiInfo.getActType());
                        entity.setStatus(weiInfo.getStatus());
                        entity.setUpdated(timeNow);
                        entity.setDevice_id(weiInfo.getDeviceId());
                        entity.setMasgData(weiInfo.getMsgData());
                    }

                    dbInterface.insertOrUpdateRefuse(entity);
                    rarentRefuseMap.put(entity.getToId(), entity);

                    ReqFriendsEntity reqWeiFriends = IMUnreadMsgManager
                            .instance()
                            .findParentRefuseMap(weiInfo.getFromId());

                    if (reqWeiFriends == null) {
                        ReqFriendsEntity reqFriends = ProtoBuf2JavaBean
                                .getUnreadFriendsEntity(weiInfo.getFromId(), 0,
                                        0);
                        IMUnreadMsgManager.instance().updateUnParentRefuse(
                                reqFriends);
                    } else {
                        reqWeiFriends.setMessageReq(1);
                        reqWeiFriends.setTableReq(1);

                        IMUnreadMsgManager.instance().updateUnParentRefuse(
                                reqWeiFriends);
                    }

                } else {

                    WeiEntity entity = findYuEntity(weiInfo.getFromId());
                    if (entity == null) {
                        entity = new WeiEntity();
                        int timeNow = (int) (System.currentTimeMillis() / 1000);

                        entity.setFromId(weiInfo.getFromId());
                        entity.setToId(weiInfo.getToId());
                        entity.setActId(weiInfo.getActId());
                        entity.setActType(weiInfo.getActType());
                        entity.setStatus(weiInfo.getStatus());
                        entity.setUpdated(timeNow);
                        entity.setDevice_id(weiInfo.getDeviceId());
                        entity.setMasgData(weiInfo.getMsgData());

                    } else {

                        int timeNow = (int) (System.currentTimeMillis() / 1000);

                        entity.setFromId(weiInfo.getFromId());
                        entity.setToId(weiInfo.getToId());
                        entity.setActId(weiInfo.getActId());
                        entity.setActType(weiInfo.getActType());
                        entity.setStatus(weiInfo.getStatus());
                        entity.setUpdated(timeNow);
                        entity.setDevice_id(weiInfo.getDeviceId());
                        entity.setMasgData(weiInfo.getMsgData());
                    }

                    dbInterface.insertOrUpdateReqYuFriends(entity);
                    userYuMap.put(entity.getFromId(), entity);

                    ReqFriendsEntity reqWeiFriends = IMUnreadMsgManager
                            .instance().findUnFriendsMap(weiInfo.getFromId());

                    if (reqWeiFriends == null) {
                        ReqFriendsEntity reqFriends = ProtoBuf2JavaBean
                                .getUnreadFriendsEntity(weiInfo.getFromId(), 0,
                                        0);
                        IMUnreadMsgManager.instance().updateUnReqYuFriends(
                                reqFriends);
                    } else {
                        reqWeiFriends.setMessageReq(1);
                        reqWeiFriends.setTableReq(1);

                        IMUnreadMsgManager.instance().updateUnReqYuFriends(
                                reqWeiFriends);
                    }

                }
            }
        }

        triggerEvent(UserInfoEvent.WEI_FRIENDS_INFO_REQ_ALL);
    }

    /**
     * 请求城市的学校
     *
     * @param city_name
     */
    public void reqSchoolInfoReq(String city_name) {
        if (city_name.equals("")) {
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        IMUserAction.IMGetSchoolInfoReq imUsersInfoReq = IMUserAction.IMGetSchoolInfoReq
                .newBuilder().setUserId(loginId).setCityName(city_name)
                .setQuerryKey("").build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_GETSCHOOLINFO_REQ_VALUE;

        imSocketManager.sendRequest(imUsersInfoReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {
                        try {

                            IMUserAction.IMGetSchoolInfoResp imAllUserRsp = IMUserAction.IMGetSchoolInfoResp
                                    .parseFrom((CodedInputStream) response);

                            schoolList = imAllUserRsp.getSchoolInfoList();
                            triggerEvent(UserInfoEvent.USER_INFO_DEVICE_SCHOOL_SUCCESS);

                        } catch (IOException e) {
                            triggerEvent(UserInfoEvent.USER_INFO_DEVICE_SCHOOL_FAIL);

                            logger.e("login failed,cause by %s", e.getCause());
                        }
                    }

                    @Override
                    public void onFaild() {
                        triggerEvent(UserInfoEvent.USER_INFO_DEVICE_SCHOOL_FAIL);

                    }

                    @Override
                    public void onTimeout() {
                        triggerEvent(UserInfoEvent.USER_INFO_DEVICE_SCHOOL_FAIL);

                    }
                });

    }

    public List<IMUserAction.SchoolInfo> getSchoolList() {
        return schoolList;
    }

    /**
     * 查询雨友请求的数据
     *
     * @param buddyId
     * @return
     */
    public WeiEntity findYuEntity(int buddyId) {
        if (buddyId > 0 && userYuMap.containsKey(buddyId)) {
            return userYuMap.get(buddyId);
        }
        return null;
    }

    /**
     * 查询管理员同意或拒绝请求的数据
     *
     * @param buddyId
     * @return
     */
    public WeiEntity findParentRefuseEntity(int buddyId) {
        if (buddyId > 0 && rarentRefuseMap.containsKey(buddyId)) {
            return rarentRefuseMap.get(buddyId);
        }
        return null;
    }

    public WeiEntity findUserReqEntity(int buddyId) {
        if (buddyId > 0 && userFriendsMap.containsKey(buddyId)) {
            return userFriendsMap.get(buddyId);
        }
        return null;
    }

    /**
     * 返回雨友请求数据
     *
     * @return
     */
    public List<WeiEntity> getReqYuList() {
        List<WeiEntity> contactList = new ArrayList<>(userYuMap.values());
        return contactList;
    }

    /**
     * 返回管理员同意或拒绝请求数据
     *
     * @return
     */
    public List<WeiEntity> getParentRefuseList() {
        List<WeiEntity> contactList = new ArrayList<>(rarentRefuseMap.values());
        return contactList;
    }

    /**
     * 返回好友请求数据
     *
     * @return
     */
    public List<WeiEntity> getReqFriendsList() {
        List<WeiEntity> contactList = new ArrayList<>(userFriendsMap.values());
        return contactList;
    }

    /**
     * 清空所有好友请求
     */
    public void ClearReqFriends() {

        dbInterface.clearUserFriends();
        userFriendsMap.clear();

        triggerEvent(UserInfoEvent.WEI_FRIENDS_WEI_REQ_ALL);
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

    public boolean isUserDataReady() {
        return userDataReady;
    }

    /**
     * 定时器 用于时间计算
     */
    class AudioTimeCount extends CountDownTimer {

        public AudioTimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (audioRecorderInstance.isRecording()) {
                audioRecorderInstance.setRecording(false);
            }

            // 　设备　录音
            if (session_type == IMBaseDefine.SessionType.SESSION_TYPE_GROUP
                    .ordinal()) {

                GroupEntity peerEntity = IMGroupManager.instance()
                        .findFamilyGroup(audioId);
                UserEntity loginUser = IMLoginManager.instance().getLoginInfo();
                if (peerEntity != null) {
                    AudioMessage audioMessage = AudioMessage.buildForSend(
                            audioRecorderInstance.getRecordTime(),
                            audioSavePath, loginUser, peerEntity, "");

                    IMMessageManager.instance().sendAuthVoice(audioMessage);
                }

            } else if (session_type == IMBaseDefine.SessionType.SESSION_TYPE_SINGLE
                    .ordinal()) {

                UserEntity loginUser = IMLoginManager.instance().getLoginInfo();
                UserEntity peerEntity = IMContactManager.instance()
                        .findFriendsContact(audioId);

                if (peerEntity == null) {
                    peerEntity = IMContactManager.instance().findDeviceContact(
                            audioId);
                }

                if (peerEntity == null) {
                    peerEntity = IMContactManager.instance().findParentContact(
                            audioId);
                }

                if (peerEntity != null) {
                    AudioMessage audioMessage = AudioMessage.buildForSend(
                            audioRecorderInstance.getRecordTime(),
                            audioSavePath, loginUser, peerEntity, "");

                    IMMessageManager.instance().sendAuthVoice(audioMessage);
                }
            }
        }
    }
}
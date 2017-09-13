package com.fise.xiaoyu.imservice.manager;

import android.util.Log;

import com.fise.xiaoyu.DB.DBInterface;
import com.fise.xiaoyu.DB.entity.CommonUserInfo;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.GroupNickEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.SystemConfigEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.callback.Packetlistener;
import com.fise.xiaoyu.imservice.entity.NoticeMessage;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.GroupEvent;
import com.fise.xiaoyu.imservice.event.SessionEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xiaoyu.protobuf.IMBaseDefine.CommentType;
import com.fise.xiaoyu.protobuf.IMBaseDefine.UserStat;
import com.fise.xiaoyu.protobuf.IMBaseDefine.UserStatType;
import com.fise.xiaoyu.protobuf.IMBuddy;
import com.fise.xiaoyu.protobuf.IMUserAction;
import com.fise.xiaoyu.protobuf.helper.ProtoBuf2JavaBean;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;
import com.fise.xiaoyu.utils.pinyin.PinYin;
import com.google.protobuf.CodedInputStream;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 负责用户信息的请求 为回话页面以及联系人页面提供服务
 * <p>
 * 联系人信息管理 普通用户的version 有总版本 群组没有总version的概念， 每个群有version 具体请参见 服务端具体的pd协议
 */
public class IMContactManager extends IMManager {
    private Logger logger = Logger.getLogger(IMContactManager.class);

    // 单例
    private static IMContactManager inst = new IMContactManager();

    public static IMContactManager instance() {
        return inst;
    }

    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private DBInterface dbInterface = DBInterface.instance();
    private IMUnreadMsgManager imUnreadManager = IMUnreadMsgManager.instance();
    private IMLoginManager imLoginManager = IMLoginManager.instance();

    private IMDeviceManager imDeviceManager = IMDeviceManager.instance();
    private IMGroupManager imGroupManager = new IMGroupManager().instance();

    // 自身状态字段
    private boolean userDataReady = false;
    private Map<Integer, UserEntity> userMap = new ConcurrentHashMap<>();
    private Map<Integer, UserEntity> userReqMap = new ConcurrentHashMap<>();

    List<UserEntity> relationsDb = new ArrayList<>();


    private Map<Integer, UserEntity> yuFriendsMap = new ConcurrentHashMap<>();

    private Map<Integer, UserEntity> friendsMap = new ConcurrentHashMap<>();
    private Map<Integer, UserEntity> deviceMap = new ConcurrentHashMap<>();
    private Map<Integer, UserEntity> userReqFriendsMap = new ConcurrentHashMap<>();
    private Map<Integer, UserEntity> blackListMap = new ConcurrentHashMap<>();

    List<SystemConfigEntity> systemConfigList = new ArrayList<>();
    private SystemConfigEntity systemConfig;
    public int xiaoweiCode = 0;


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

        List<UserEntity> userlist = dbInterface.loadAllUsers();
        logger.d("contact#loadAllUserInfo dbsuccess");

        for (UserEntity userInfo : userlist) {
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(),
                    userInfo.getPinyinElement());
            if (Utils.isClientType(userInfo)) {

            } else {
                userMap.put(userInfo.getPeerId(), userInfo);
            }

        }

        // 跟我有关系的人
        List<UserEntity> relations = dbInterface.loadAllRelationsList();
        for (UserEntity userInfo : relations) {
            userReqMap.put(userInfo.getPeerId(), userInfo);
        }

        // 加载设备数据
        List<UserEntity> devicelist = dbInterface.loadAllDevice();
        for (UserEntity userInfo : devicelist) {
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(),
                    userInfo.getPinyinElement());

            if (Utils.isClientType(userInfo)) {
                if (userInfo.getIsFriend() != DBConstant.FRIENDS_TYPE_NO
                        || userInfo.getIsFriend() != DBConstant.FRIENDS_TYPE_VERIFY) {
                    if (userInfo.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                        deviceMap.put(userInfo.getPeerId(), userInfo);
                    }
                }
            }
        }

        List<UserEntity> friendsList = dbInterface.loadAllFriends();
        for (UserEntity userInfo : friendsList) {
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(),
                    userInfo.getPinyinElement());
            if (Utils.isClientType(userInfo)) {

            } else {
                if (userInfo.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {
                    friendsMap.put(userInfo.getPeerId(), userInfo);
                }
            }
        }


        List<UserEntity> blackList = dbInterface.loadAllBlackList();
        for (UserEntity userInfo : blackList) {
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(),
                    userInfo.getPinyinElement());
            blackListMap.put(userInfo.getPeerId(), userInfo);
        }


        //孩子端的父母列表
        List<UserEntity> yuList = dbInterface.loadAllParentInfo();
        for (UserEntity userInfo : yuList) {
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(),
                    userInfo.getPinyinElement());
            yuFriendsMap.put(userInfo.getPeerId(), userInfo);
        }


        // 加载配置信息
        systemConfigList = dbInterface.loadAllSystemConfig();
        if (systemConfigList.size() > 0) {
            if (systemConfig != null) {
                systemConfig.setId(systemConfigList.get(0).getId());
                DBInterface.instance().insertOrUpdateSystemConfig(systemConfig);
            }

        } else {
            if (systemConfig != null) {
                DBInterface.instance().insertOrUpdateSystemConfig(systemConfig);
            }
        }
        systemConfigList = dbInterface.loadAllSystemConfig();
        triggerEvent(UserInfoEvent.USER_INFO_OK);

    }

    public void setsystemConfig(SystemConfigEntity systemConfig) {
        this.systemConfig = systemConfig;
    }


    /**
     * 加载本地DB的状态 黑名单本地数据
     *
     * @return
     */
    public List<UserEntity> getBlackList() {
        List<UserEntity> blackList = dbInterface.loadAllBlackList();
        return blackList;
    }

    /**
     * 加载本地DB的状态 好友请求 本地数据
     *
     * @return
     */
    public List<UserEntity> reqFriends() {
        logger.d("contact#loadAllUserInfo");

        List<UserEntity> reqFriendslist = dbInterface.loadAllReqFriends();
        return reqFriendslist;
    }


    /**
     * 更新 好友数据 (从数据库中获取)
     */
    public void updateFriends() {
        List<UserEntity> userlist = dbInterface.loadAllFriends();
        friendsMap.clear();
        for (UserEntity userInfo : userlist) {
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(),
                    userInfo.getPinyinElement());
            if (userInfo.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {
                friendsMap.put(userInfo.getPeerId(), userInfo);
            }
        }
    }

    /**
     * 查找请求位友或好友的个人信息
     *
     * @param buddyId
     * @return
     */
    public UserEntity findReqFriends(int buddyId) {

        List<UserEntity> userlist = dbInterface.loadAllReqFriends();
        userReqFriendsMap.clear();
        for (UserEntity userInfo : userlist) {
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(),
                    userInfo.getPinyinElement());
            userReqFriendsMap.put(userInfo.getPeerId(), userInfo);
        }

        if (buddyId > 0 && userReqFriendsMap.containsKey(buddyId)) {
            return userReqFriendsMap.get(buddyId);
        }
        return null;
    }

    /**
     * 更新设备信息
     */
    public void updateDevice() {

        List<UserEntity> devicelist = dbInterface.loadAllDevice();
        for (UserEntity userInfo : devicelist) {
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(),
                    userInfo.getPinyinElement());
            if (Utils.isClientType(userInfo)) {
                if (userInfo.getIsFriend() != DBConstant.FRIENDS_TYPE_NO
                        || userInfo.getIsFriend() != DBConstant.FRIENDS_TYPE_VERIFY) {

                    if (userInfo.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                        deviceMap.put(userInfo.getPeerId(), userInfo);
                    }
                }
            }
        }
    }

    /**
     * 更新监位友信息
     */
    public void updateAllFriends() {


        //设备列表
        List<UserEntity> devlist = dbInterface.loadAllDevice();
        deviceMap.clear();
        for (UserEntity userInfo : devlist) {
            PinYin.getPinYin(userInfo.getMainName(),
                    userInfo.getPinyinElement());

            if (Utils.isClientType(userInfo)) {
                if (userInfo.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                    deviceMap.put(userInfo.getPeerId(), userInfo);
                }
            }
        }


        /**
         * 所有人列表
         */
        List<UserEntity> userlist = dbInterface.loadAllUsers();
        for (UserEntity userInfo : userlist) {
            if (userInfo.getComment().equals("")) {
                PinYin.getPinYin(userInfo.getMainName(),
                        userInfo.getPinyinElement());
            } else {
                PinYin.getPinYin(userInfo.getComment(),
                        userInfo.getPinyinElement());
            }
            userMap.put(userInfo.getPeerId(), userInfo);
        }
    }

    /**
     * 网络链接成功，登陆之后请求
     */
    public void onLocalNetOk() {
        // 用户信息
        int updateTime = dbInterface.getUserInfoLastTime();
        logger.d("contact#loadAllUserInfo req-updateTime:%d", updateTime);
        reqGetAllUsers(updateTime);//updateTime


        int friendsUpdateTime = dbInterface.getFriendsLastTime();
        // 请求好友请求
        //	IMUserActionManager.instance().reqYuFriends(friendsUpdateTime);
        int yuUpdateTime = dbInterface.getYuInfoLastTime();

        int yuParentRefuseTime = dbInterface.getParentRefuseLastTime();

        int time;
        if (friendsUpdateTime > yuUpdateTime) {
            time = friendsUpdateTime;
        } else {
            time = yuUpdateTime;
        }


        int showTime;
        if (yuParentRefuseTime > time) {
            showTime = yuParentRefuseTime;
        } else {
            showTime = time;
        }

        // 雨友请求
        IMUserActionManager.instance().reqYuFriends(showTime);


        if (IMLoginManager.instance().getLoginInfo() != null) {
            if (findUserContact(IMLoginManager.instance().getLoginId()) == null) {
                insertOrUpdateUser(IMLoginManager.instance().getLoginInfo());
            }
        }
    }

    @Override
    public void reset() {
        userDataReady = false;
        userMap.clear();
        userReqMap.clear();
        friendsMap.clear();
        relationsDb.clear();
        deviceMap.clear();
        yuFriendsMap.clear();


        systemConfigList.clear();
    }

    /**
     * @param event
     */
    public void triggerEvent(GroupEvent event) {
        // 先更新自身的状态
        EventBus.getDefault().postSticky(event);
    }

    /**
     * @param event
     */
    public void triggerEvent(DeviceEvent event) {
        // 先更新自身的状态
        EventBus.getDefault().postSticky(event);
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
     * 全部好友位友请求协议 lastUpdateTime 上一次更新的数据 (目前时间和上一次更新的时间内更新的信息)
     *
     * @param lastUpdateTime
     */
    private void reqGetAllUsers(int lastUpdateTime) {
        logger.i("contact#reqGetAllUsers");
        int userId = IMLoginManager.instance().getLoginId();

        IMBuddy.IMAllUserReq imAllUserReq = IMBuddy.IMAllUserReq.newBuilder()
                .setUserId(userId).setLatestUpdateTime(lastUpdateTime).build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_ALL_USER_REQUEST_VALUE;
        imSocketManager.sendRequest(imAllUserReq, sid, cid);
    }

    /**
     * lastUpdateTime 到现在全部信息更新的回复
     *
     * @param imAllUserRsp 1.请求所有用户的信息,总的版本号version 2.匹配总的版本号，返回可能存在变更的
     *                     3.选取存在变更的，请求用户详细信息 4.更新DB，保存globalVersion 以及用户的信息
     */
    public void onRepAllUsers(IMBuddy.IMAllUserRsp imAllUserRsp) {
        int userId = imAllUserRsp.getUserId();
        int lastTime = imAllUserRsp.getLatestUpdateTime();
        // lastTime 需要保存嘛? 不保存了

        int count = imAllUserRsp.getUserListCount();
        if (count <= 0) {
            return;
        }

        int loginId = IMLoginManager.instance().getLoginId();
        if (userId != loginId) {
            logger.e("[fatal error] userId not equels loginId ,cause by onRepAllUsers");
            return;
        }

        List<IMBaseDefine.UserInfo> changeList = imAllUserRsp.getUserListList();
        ArrayList<UserEntity> needDb = new ArrayList<>();
        ArrayList<UserEntity> needDbReq = new ArrayList<>();
        ArrayList<UserEntity> friendsDbReq = new ArrayList<>();
        ArrayList<UserEntity> blackList = new ArrayList<>();

        ArrayList<UserEntity> devicebReq = new ArrayList<>();
        ArrayList<UserEntity> parentList = new ArrayList<>();


        for (IMBaseDefine.UserInfo userInfo : changeList) {

            // 黑名单
            if (userInfo.getAuth() == DBConstant.AUTH_TYPE_BLACK) {
                UserEntity entity = ProtoBuf2JavaBean.getUserEntity(userInfo);
                if (entity.getPeerId() == IMLoginManager.instance()
                        .getLoginId()) {
                    entity.setFriendNeedAuth(IMLoginManager.instance()
                            .getLoginInfo().getFriendNeedAuth());
                }

                blackListMap.put(entity.getPeerId(), entity);
                blackList.add(entity);

                UserEntity entityUser = ProtoBuf2JavaBean
                        .getUserCopyEntity(entity);

                userReqMap.put(entityUser.getPeerId(), entityUser);
                needDbReq.add(entityUser);
                relationsDb.add(entityUser);

            } else {

                UserEntity entity = ProtoBuf2JavaBean.getUserEntity(userInfo);
                if (entity.getPeerId() == IMLoginManager.instance()
                        .getLoginId()) {
                    entity.setFriendNeedAuth(IMLoginManager.instance()
                            .getLoginInfo().getFriendNeedAuth());
                }

                if (Utils.isClientType(entity)) {

                    if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                        deviceMap.put(entity.getPeerId(), entity);
                        devicebReq.add(entity);

                        UserEntity entity1 = ProtoBuf2JavaBean
                                .getUserCopyEntity(entity);
                        userMap.put(entity1.getPeerId(), entity1);
                        needDb.add(entity1);
                    } else if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {
                        friendsMap.put(entity.getPeerId(), entity);
                        friendsDbReq.add(entity);

                        UserEntity entity1 = ProtoBuf2JavaBean
                                .getUserCopyEntity(entity);
                        userMap.put(entity1.getPeerId(), entity1);
                        needDb.add(entity1);
                    } else {

                        userMap.put(entity.getPeerId(), entity);
                        needDb.add(entity);

                        if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {

                            UserEntity parents = findParentContact(entity.getPeerId());
                            if (parents != null) {
                                deletUpdateParentInfo(parents);
                            }
                        } else {
                            //跟Login的不是位友关系的 本地有的 删除
                            //删除配置文件
                            UserEntity devices = findDeviceContact(entity.getPeerId());
                            if (devices != null) {
                                deleteDevUser(devices);
                            }
                        }
                    }


                    //else 好友关系
                } else {

                    if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) { // guanweile

                        userMap.put(entity.getPeerId(), entity);
                        needDb.add(entity);

                        friendsMap.put(entity.getPeerId(), entity);
                        friendsDbReq.add(entity);

                    } else if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_NO) {

                        if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {

                            UserEntity parents = findParentContact(entity.getPeerId());
                            if (parents != null) {
                                deletUpdateParentInfo(parents);
                            }
                        } else {
                            //跟Login的不是位友关系的 本地有的 删除
                            //删除配置文件
                            UserEntity devices = findDeviceContact(entity.getPeerId());
                            if (devices != null) {
                                deleteDevUser(devices);
                            }
                        }

                        // 如果在本地数据不对需要变更
                        UserEntity friends = findFriendsContact(entity.getPeerId());
                        if (friends != null) {
                            friends.setFriend(DBConstant.FRIENDS_TYPE_NO);
                            dbInterface.insertOrDeleteFriens(friends);
                            friendsMap.remove(friends.getPeerId());

                            //删除这个人的sessionKey
                            PeerEntity peerEntity = IMSessionManager.instance().findPeerEntity(
                                    friends.getSessionKey());
                            if (peerEntity != null) {
                                IMSessionManager.instance().removeSession(peerEntity.getSessionKey());
                            }
                        }

                        userReqMap.put(entity.getPeerId(), entity);
                        needDbReq.add(entity);
                        relationsDb.add(entity);

                        userMap.put(entity.getPeerId(), entity);
                        needDb.add(entity);
                    } else if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {

                        yuFriendsMap.put(entity.getPeerId(), entity);
                        parentList.add(entity);

                    }
                }
            }
        }


        // 黑名单
        dbInterface.batchInsertOrUpdateBlackList(blackList);

        dbInterface.batchInsertOrUpdateDevice(devicebReq);
        dbInterface.batchInsertOrUpdateRelations(needDbReq);
        dbInterface.batchInsertOrUpdateUser(needDb);
        dbInterface.batchInsertOrUpdateFriends(friendsDbReq);
        dbInterface.batchInsertOrUpdateParentInfo(parentList);  //孩子端　的父母列表


        for (int i = 0; i < devicebReq.size(); i++) {
            if (devicebReq.get(i).getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                imDeviceManager.DeviceConfigReq(devicebReq.get(i).getPeerId());
            }
        }

        triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
    }

    /**
     * 查找更我有关系的信息
     *
     * @param buddyId
     * @return
     */
    public UserEntity findContact(int buddyId) {
        if (buddyId > 0 && userMap.containsKey(buddyId)) {
            return userMap.get(buddyId);
        } else if (imLoginManager != null && (buddyId == imLoginManager.getLoginId())) {
            return imLoginManager.getLoginInfo();
        } else {
            return findReq(buddyId);
        }
    }


    public UserEntity findUserContact(int buddyId) {
        if (buddyId > 0 && userMap.containsKey(buddyId)) {
            return userMap.get(buddyId);
        }
        return null;
    }


    /**
     * 查找黑名单
     *
     * @param buddyId
     * @return
     */
    public UserEntity findBlackList(int buddyId) {
        if (buddyId > 0 && blackListMap.containsKey(buddyId)) {
            return blackListMap.get(buddyId);
        }
        return null;
    }


    /**
     * 查找设备
     *
     * @param buddyId
     * @return
     */
    public UserEntity findDeviceContact(int buddyId) {
        if (buddyId > 0 && deviceMap.containsKey(buddyId)) {
            return deviceMap.get(buddyId);
        }
        return null;
    }


    /**
     * 更新设备信息
     *
     * @param user
     */
    public void updateOrDevice(UserEntity user) {

        if (user != null) {
            if (user != null) {
                if (Utils.isClientType(user)) {
                    if (user.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {

                        dbInterface.insertOrUpdateDevice(user);
                        deviceMap.put(user.getPeerId(), user);

                        if (findContact(user.getPeerId()) != null) {

                            UserEntity entityUser = ProtoBuf2JavaBean
                                    .getUserCopyEntity(user);
                            UserEntity dev = findContact(user.getPeerId());
                            entityUser.setId(dev.getId());
                            insertOrUpdateUser(entityUser);
                        } else {
                            UserEntity entityUser = ProtoBuf2JavaBean
                                    .getUserCopyEntity(user);
                            insertOrUpdateUser(entityUser);
                        }
                    } else if (user.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {

                        friendsMap.put(user.getPeerId(), user);
                        dbInterface.insertOrUpdateFriends(user);


                        UserEntity entity1 = ProtoBuf2JavaBean
                                .getUserCopyEntity(user);
                        userMap.put(entity1.getPeerId(), entity1);
                        insertOrUpdateUser(entity1);

                    }
                }
            }

        }
    }

    /**
     * 查找好友信息
     *
     * @param buddyId
     * @return
     */
    public UserEntity findFriendsContact(int buddyId) {
        if (buddyId > 0 && friendsMap.containsKey(buddyId)) {
            return friendsMap.get(buddyId);
        }
        return null;
    }


    /**
     * 孩子端查找父母信息
     *
     * @param buddyId
     * @return
     */
    public UserEntity findParentContact(int buddyId) {
        if (buddyId > 0 && yuFriendsMap.containsKey(buddyId)) {
            return yuFriendsMap.get(buddyId);
        }
        return null;
    }

    /**
     * 更新好友数据
     *
     * @param buddyId
     */
    public void UpdateFriendsContact(int buddyId) {
        UserEntity user = null;

        if (buddyId > 0 && friendsMap.containsKey(buddyId)) {
            user = friendsMap.get(buddyId);
        }

        if (user != null) {
            userMap.put(user.getPeerId(), user);
        }
    }

    /**
     * 删除设备
     *
     * @param dev
     */
    public void deleteDevUser(UserEntity dev) {

        if (dev != null) {
            dbInterface.insertOrDeleteDevice(dev);
            deviceMap.remove(dev.getPeerId());
        }

    }

    /**
     * 更新好友信息
     *
     * @param user
     */
    public void UpdateFriendsContact(UserEntity user) {

        if (user.getPeerId() > 0 && friendsMap.containsKey(user.getPeerId())) {
            friendsMap.put(user.getPeerId(), user);
            dbInterface.insertOrUpdateFriends(user);
        }
    }


    /**
     * 更新父母信息
     *
     * @param user
     */
    public void updateParentContact(UserEntity user) {

        if (user.getPeerId() > 0 && yuFriendsMap.containsKey(user.getPeerId())) {
            yuFriendsMap.put(user.getPeerId(), user);
            dbInterface.insertOrUpdateParentInfo(user);
        }

    }


    /**
     * 删除父母的信息
     *
     * @param user
     */
    public void deletUpdateParentInfo(UserEntity user) {

        if (user != null) {
            dbInterface.deletUpdateParentInfo(user);
            yuFriendsMap.remove(user.getPeerId());
        }
    }


    /**
     * 更新好友信息 (数据更新)
     *
     * @param user
     */
    public void inserOrFriendsContact(UserEntity user) {

        if (user != null) {
            dbInterface.insertOrUpdateFriends(user);
            friendsMap.put(user.getPeerId(), user);
        }
    }


    /**
     * 删除好友信息 (数据更新)
     *
     * @param user
     */
    public void deleteOrFriendsContact(UserEntity user) {

        if (user != null) {
            dbInterface.insertOrDeleteFriens(user);
            friendsMap.remove(user.getPeerId());
        }

    }

    /**
     * 更新位友/设备 信息(数据更新)
     *
     * @param user
     */
    public void UpdateWeiFriendsContact(UserEntity user) {
        if (user.getPeerId() > 0
                && deviceMap.containsKey(user.getPeerId())) {

            if (Utils.isClientType(user)) {
                if (user.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                    deviceMap.put(user.getPeerId(), user);
                    dbInterface.insertOrUpdateDevice(user);

                    if (findContact(user.getPeerId()) != null) {

                        UserEntity entityUser = ProtoBuf2JavaBean
                                .getUserCopyEntity(user);
                        UserEntity dev = findContact(user.getPeerId());
                        entityUser.setId(dev.getId());
                        insertOrUpdateUser(entityUser);
                    } else {
                        UserEntity entityUser = ProtoBuf2JavaBean
                                .getUserCopyEntity(user);
                        insertOrUpdateUser(entityUser);
                    }
                } else if (user.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {

                    friendsMap.put(user.getPeerId(), user);
                    dbInterface.insertOrUpdateFriends(user);


                    UserEntity entity1 = ProtoBuf2JavaBean
                            .getUserCopyEntity(user);
                    userMap.put(entity1.getPeerId(), entity1);
                    insertOrUpdateUser(entity1);

                }
            }
        }
    }


    /**
     * 查找请求加好友/位友的信息
     *
     * @param buddyId
     * @return
     */
    public UserEntity findReq(int buddyId) {
        if (buddyId > 0 && userReqMap.containsKey(buddyId)) {
            return userReqMap.get(buddyId);
        }
        return null;
    }

    /**
     * 不是好友关系的信息 例如搜索到的数据
     *
     * @return
     */
    public List<UserEntity> getRelations() {
        return relationsDb;
    }


    /**
     * 群成员变更的　人员信息
     *
     * @param userIds
     */

    public void reqGetDetaillUsersAdd(ArrayList<Integer> userIds, final String userAll, final PeerEntity peerEntity, final int type) {
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
        imSocketManager.sendRequest(imUsersInfoReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            IMBuddy.IMUsersInfoRsp imChangInfo = IMBuddy.IMUsersInfoRsp
                                    .parseFrom((CodedInputStream) response);

                            List<IMBaseDefine.UserInfo> userInfoList = imChangInfo
                                    .getUserInfoListList();

                            String userName = userAll;
                            for (IMBaseDefine.UserInfo userInfo : userInfoList) {

                                UserEntity userEntity = ProtoBuf2JavaBean.getUserEntity(userInfo);
                                if (userEntity.getPeerId() == IMLoginManager.instance()
                                        .getLoginId()) {
                                    userEntity.setFriendNeedAuth(IMLoginManager.instance()
                                            .getLoginInfo().getFriendNeedAuth());
                                }
                                userName = userName + userEntity.getRealName() + ",";
                            }

                            if (type == DBConstant.REQ_USER_ADD) {
                                NoticeMessage noticeMessage = NoticeMessage.buildGroupForSend(userName + "加入群中", IMLoginManager.instance().getLoginInfo(), peerEntity);
                                IMMessageManager.instance().sendGorupNotice(noticeMessage);
                            } else {
                                NoticeMessage noticeMessage = NoticeMessage.buildGroupForSend(userName + "已被移出群了", IMLoginManager.instance().getLoginInfo(), peerEntity);
                                IMMessageManager.instance().sendGorupNotice(noticeMessage);
                            }

                        } catch (IOException e) {
                            triggerEvent(UserInfoEvent.USER_INFO_DATA_FAIL);
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
     * 请求用户详细信息
     *
     * @param userIds
     */
    public void reqGetDetaillUsers(ArrayList<Integer> userIds) {
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
        imSocketManager.sendRequest(imUsersInfoReq, sid, cid);
    }

    /**
     * 请求群昵称详细信息
     *
     * @param userIds
     */
    public void reqGetGroupNick(ArrayList<Integer> userIds, final int groupId,
                                boolean isGroupNick) {
        logger.i("contact#contact#reqGetDetaillUsers");
        if (null == userIds || userIds.size() <= 0) {
            logger.i("contact#contact#reqGetDetaillUsers return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMUsersInfoReq imUsersInfoReq = IMBuddy.IMUsersInfoReq
                .newBuilder().setUserId(loginId).addAllUserIdList(userIds)
                .setGroupId(groupId).build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_USER_INFO_REQUEST_VALUE;
        imSocketManager.sendRequest(imUsersInfoReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            IMBuddy.IMUsersInfoRsp imChangInfo = IMBuddy.IMUsersInfoRsp
                                    .parseFrom((CodedInputStream) response);
                            onRepDetailUsersGroup(imChangInfo, groupId);

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
     * 请求用户状态信息
     *
     * @param userIds
     */
    public void reqGetDetaillUsersStat(ArrayList<Integer> userIds) {
        Log.i("xiaowei", "reqGetDetaillUsersStat: ");
        if (null == userIds || userIds.size() <= 0) {
            logger.i("contact#contact#reqGetDetaillUsers return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMUsersStatReq imUsersInfoReq = IMBuddy.IMUsersStatReq
                .newBuilder().setUserId(loginId).addAllUserIdList(userIds)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_USERS_STATUS_REQUEST_VALUE;
        imSocketManager.sendRequest(imUsersInfoReq, sid, cid);
    }

    /**
     * 用户状态返回数据
     *
     * @param userIds
     */
    public void reqGetDetaillUsersStat1(ArrayList<Integer> userIds) {
        logger.i("contact#contact#reqGetDetaillUsers");
        if (null == userIds || userIds.size() <= 0) {
            logger.i("contact#contact#reqGetDetaillUsers return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMUsersStatReq imUsersInfoReq = IMBuddy.IMUsersStatReq
                .newBuilder().setUserId(loginId).addAllUserIdList(userIds)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_USERS_STATUS_REQUEST_VALUE;
        imSocketManager.sendRequest(imUsersInfoReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            IMBuddy.IMUsersStatRsp imChangInfo = IMBuddy.IMUsersStatRsp
                                    .parseFrom((CodedInputStream) response);
                            onRepDetailUsersStat1(imChangInfo);

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
     * 修改 session中的 免打扰和消息置顶
     *
     * @param from_user
     * @param dest_user
     * @param set_status
     * @param comment_type
     * @param value
     * @param session
     */
    public void ChangeSessionInfo(int from_user, final int dest_user,
                                  int set_status, final CommentType comment_type, final String value,
                                  final PeerEntity session) {

        IMBuddy.IMSettingCommentReq imUsersInfoReq = IMBuddy.IMSettingCommentReq
                .newBuilder().setFromUser(from_user).setDestUser(dest_user)
                .setSetStatus(set_status).setCommentType(comment_type)
                .setValue(value).build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_SETTING_COMMENT_REQ_VALUE;

        imSocketManager.sendRequest(imUsersInfoReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            IMBuddy.IMSettingCommentRsp imChangInfo = IMBuddy.IMSettingCommentRsp
                                    .parseFrom((CodedInputStream) response);
                            onRepChangeCommentSessionInfo(imChangInfo,
                                    dest_user, comment_type, value, session);

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
     * 免打扰和消息置顶 的回复包
     *
     * @param imChangInfo
     * @param dest_user
     * @param comment_type
     * @param value
     * @param peerEntity
     */
    public void onRepChangeCommentSessionInfo(
            IMBuddy.IMSettingCommentRsp imChangInfo, int dest_user,
            CommentType comment_type, String value, PeerEntity peerEntity) {
        if (imChangInfo == null) {
            logger.e("Session#Session LoginResponse failed");
            return;
        }

        int code = (imChangInfo.getResultCode()).ordinal();
        switch (code) {
            case 0: {

                // 群的置顶盒免打扰
                if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
                    final GroupEntity groupEntity = (GroupEntity) peerEntity;

                    // if (comment_type ==
                    // CommentType.COMMENT_TYPE_MUTE_NOTIFICATION) {
                    // groupEntity.setMuteNotification(Integer.parseInt(value));
                    //
                    // } else if (comment_type ==
                    // CommentType.COMMENT_TYPE_STICKY_ON_TOP) {
                    // groupEntity.setStickyOnTop(Integer.parseInt(value));
                    // }

                    IMGroupManager.instance().updateGroup(groupEntity);

                    // 个人的置顶盒免打扰
                } else if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {

                    UserEntity userEntity = (UserEntity) peerEntity;

                    if (comment_type == CommentType.COMMENT_TYPE_MUTE_NOTIFICATION) {
                        // userEntity.setMuteNotification(Integer.parseInt(value));

                    } else if (comment_type == CommentType.COMMENT_TYPE_STICKY_ON_TOP) {
                        // userEntity.setStickyOnTop(Integer.parseInt(value));
                    }

                    if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {

                        if (findContact(userEntity.getPeerId()) != null) {
                            UserEntity user = findContact(dest_user);
                            // user.setMuteNotification(userEntity.getMuteNotification());
                            // user.setStickyOnTop(userEntity.getStickyOnTop());
                            insertOrUpdateUser(user);
                        }

                        UpdateFriendsContact(userEntity);

                    } else if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {

                        if (findContact(userEntity.getPeerId()) != null) {
                            UserEntity user = findContact(dest_user);
                            // user.setMuteNotification(userEntity.getMuteNotification());
                            // user.setStickyOnTop(userEntity.getStickyOnTop());
                            insertOrUpdateUser(user);
                        }

                        UpdateWeiFriendsContact(userEntity);
                    }
                }

                if (comment_type == CommentType.COMMENT_TYPE_MUTE_NOTIFICATION) {
                    triggerEvent(UserInfoEvent.USER_MUTE_NOTIFICATION);
                } else if (comment_type == CommentType.COMMENT_TYPE_STICKY_ON_TOP) {
                    EventBus.getDefault().post(SessionEvent.SET_SESSION_MUTE_TOP);
                }
            }
            break;

            default: {
                logger.e("Avatar#Avatar msg server inner failed, result:%s", code);
                triggerEvent(UserInfoEvent.USER_INFO_DATA_FAIL);

            }
            break;
        }

    }

    /**
     * 设置静音与置顶
     *
     * @param from_user
     * @param dest_user
     * @param set_status
     * @param comment_type
     * @param value
     */
    public void ChangeUserInfo(int from_user, final int dest_user,
                               int set_status, final CommentType comment_type, final String value) {

        IMBuddy.IMSettingCommentReq imUsersInfoReq = IMBuddy.IMSettingCommentReq
                .newBuilder().setFromUser(from_user).setDestUser(dest_user)
                .setSetStatus(set_status).setCommentType(comment_type)
                .setValue(value).build(); // IMChangeUserInfoReq

        // CID_BUDDY_SETTING_COMMENT_REQ/
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_SETTING_COMMENT_REQ_VALUE;

        imSocketManager.sendRequest(imUsersInfoReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            IMBuddy.IMSettingCommentRsp imChangInfo = IMBuddy.IMSettingCommentRsp
                                    .parseFrom((CodedInputStream) response);
                            onRepChangeCommentUserInfo(imChangInfo, dest_user,
                                    comment_type, value);

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
     * 设置静音与置顶 回复
     *
     * @param imChangInfo
     * @param dest_user
     * @param comment_type
     * @param value
     */
    public void onRepChangeCommentUserInfo(
            IMBuddy.IMSettingCommentRsp imChangInfo, int dest_user,
            CommentType comment_type, String value) {
        if (imChangInfo == null) {
            logger.e("Avatar#Avatar LoginResponse failed");
            return;
        }
        int code = (imChangInfo.getResultCode()).ordinal();
        switch (code) {
            case 0: {

                if (findFriendsContact(dest_user) != null) {
                    UserEntity user = findFriendsContact(dest_user);
                    user.setComment(value); // comment
                    //user.setPinyinName(value);
                    UpdateFriendsContact(user);

                }

                if (findContact(dest_user) != null) {
                    UserEntity user = findContact(dest_user);
                    user.setComment(value);
                    //user.setPinyinName(value);
                    insertOrUpdateUser(user);
                }

                triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
            }
            break;

            default: {
                logger.e("Avatar#Avatar msg server inner failed, result:%s", code);
                triggerEvent(UserInfoEvent.USER_INFO_DATA_FAIL);

            }
            break;
        }

    }

    /**
     * 修改用户信息
     *
     * @param from_user
     * @param change_type 通知类型定义 例如 昵称 性别
     * @param value       修改的内容
     */
    public void ChangeUserInfo(int from_user, ChangeDataType change_type,
                               String value) {

        IMBuddy.IMChangeUserInfoReq imUsersInfoReq = IMBuddy.IMChangeUserInfoReq
                .newBuilder().setFromUser(from_user).setValue(value)
                .setChangeType(change_type).build(); // IMChangeUserInfoReq
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_CHANGE_USERINFO_REQ_VALUE;

        final String tempValue = value;
        final ChangeDataType tempType = change_type;
        imSocketManager.sendRequest(imUsersInfoReq, sid, cid,
                new Packetlistener() {
                    @Override
                    public void onSuccess(Object response) {
                        try {
                            IMBuddy.IMChangeUserInfoRsp imChangInfo = IMBuddy.IMChangeUserInfoRsp
                                    .parseFrom((CodedInputStream) response);
                            onRepChangeUserInfo(imChangInfo, tempValue);

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
     * 修改用户信息
     *
     * @param imChangInfo
     * @param value
     */
    public void onRepChangeUserInfo(IMBuddy.IMChangeUserInfoRsp imChangInfo,
                                    String value) {

        if (imChangInfo == null) {
            logger.e("Avatar#Avatar LoginResponse failed");
            return;
        }

        int code = (imChangInfo.getResultCode()).ordinal();

        switch (code) {
            case 0: {

                if (imChangInfo.getChangeType() == ChangeDataType.CHANGE_USERINFO_AVATAR) {

                    UserEntity login = IMLoginManager.instance().getLoginInfo();
                    login.setAvatar(value);

                    IMLoginManager.instance().setLoginInfo(login);

                    UserEntity currentUser = IMContactManager.instance()
                            .findContact(login.getPeerId());
                    if (currentUser != null) {
                        currentUser.setAvatar(login.getUserAvatar());
                        IMContactManager.instance().insertOrUpdateUser(currentUser);
                    }

                } else if (imChangInfo.getChangeType() == ChangeDataType.CHANGE_USERINFO_NICK) {

                    UserEntity login = IMLoginManager.instance().getLoginInfo();
                    login.setMainName(value);
                    IMLoginManager.instance().setLoginInfo(login);

                    UserEntity currentUser = IMContactManager.instance()
                            .findContact(login.getPeerId());
                    if (currentUser != null) {
                        currentUser.setMainName(login.getMainName());
                        IMContactManager.instance().insertOrUpdateUser(currentUser);
                    }

                } else if (imChangInfo.getChangeType() == ChangeDataType.CHANGE_USERINFO_SIGNINFO) {

                    UserEntity login = IMLoginManager.instance().getLoginInfo();
                    login.setSign_info(value);
                    IMLoginManager.instance().setLoginInfo(login);

                    UserEntity currentUser = IMContactManager.instance()
                            .findContact(login.getPeerId());
                    if (currentUser != null) {
                        currentUser.setSign_info(login.getSign_info());
                        IMContactManager.instance().insertOrUpdateUser(currentUser);
                    }

                } else if (imChangInfo.getChangeType() == ChangeDataType.CHANGE_USERINFO_SEX) {

                    UserEntity login = IMLoginManager.instance().getLoginInfo();
                    login.setGender(Integer.parseInt(value));
                    IMLoginManager.instance().setLoginInfo(login);

                    UserEntity currentUser = IMContactManager.instance()
                            .findContact(login.getPeerId());
                    if (currentUser != null) {
                        currentUser.setGender(login.getGender());
                        IMContactManager.instance().insertOrUpdateUser(currentUser);
                    }

                } else if (imChangInfo.getChangeType() == ChangeDataType.CHANGE_USERINFO_EMAIL) {

                    UserEntity login = IMLoginManager.instance().getLoginInfo();
                    login.setEmail(value);
                    IMLoginManager.instance().setLoginInfo(login);

                    UserEntity currentUser = IMContactManager.instance()
                            .findContact(login.getPeerId());
                    if (currentUser != null) {
                        currentUser.setEmail(login.getEmail());
                        IMContactManager.instance().insertOrUpdateUser(currentUser);
                    }

                } else if (imChangInfo.getChangeType() == ChangeDataType.CHANGE_USERSET_FRIEND_NEED_AUTH) { // 添加好友是否需要验证

                    UserEntity login = IMLoginManager.instance().getLoginInfo();
                    int friends = Integer.parseInt(value);
                    login.setFriendNeedAuth(friends);
                    IMLoginManager.instance().setLoginInfo(login);

                    UserEntity currentUser = IMContactManager.instance()
                            .findContact(login.getPeerId());
                    if (currentUser != null) {
                        currentUser.setFriendNeedAuth(login.getFriendNeedAuth());
                        IMContactManager.instance().insertOrUpdateUser(currentUser);
                    }

                } else if (imChangInfo.getChangeType() == ChangeDataType.CHANGE_USERSET_ALLOW_SEARCH_FIND) { // 是否允许搜索

                    UserEntity login = IMLoginManager.instance().getLoginInfo();
                    int searchFriends = Integer.parseInt(value);
                    login.setSearchAllow(searchFriends);
                    IMLoginManager.instance().setLoginInfo(login);

                    UserEntity currentUser = IMContactManager.instance()
                            .findContact(login.getPeerId());
                    if (currentUser != null) {
                        currentUser.setSearchAllow(login.getSearchAllow());
                        IMContactManager.instance().insertOrUpdateUser(currentUser);
                    }

                } else if (imChangInfo.getChangeType() == ChangeDataType.CHANGE_USERSET_SAFE_LOGIN_SWITCH) { // 登录保护开关

                    UserEntity login = IMLoginManager.instance().getLoginInfo();
                    int searchFriends = Integer.parseInt(value);
                    login.setLoginSafeSwitch(searchFriends);
                    IMLoginManager.instance().setLoginInfo(login);

                    UserEntity currentUser = IMContactManager.instance()
                            .findContact(login.getPeerId());
                    if (currentUser != null) {
                        currentUser.setLoginSafeSwitch(login.getLoginSafeSwitch());
                        IMContactManager.instance().insertOrUpdateUser(currentUser);
                    }
                } else if (imChangInfo.getChangeType() == ChangeDataType.CHANGE_USERINFO_PHONE) { // 手机号码修改

                    UserEntity login = IMLoginManager.instance().getLoginInfo();
                    login.setPhone(value);
                    IMLoginManager.instance().setLoginInfo(login);

                    UserEntity currentUser = IMContactManager.instance()
                            .findContact(login.getPeerId());
                    if (currentUser != null) {
                        currentUser.setPhone(login.getPhone());
                        IMContactManager.instance().insertOrUpdateUser(currentUser);
                    }

                    triggerEvent(UserInfoEvent.USER_INFO_DATA_UPDATE_PHONE);

                } else if (imChangInfo.getChangeType() == ChangeDataType.CHANGE_USERINFO_DOMAIN) { // 小雨号修改

                    UserEntity login = IMLoginManager.instance().getLoginInfo();
                    login.setRealName(value);
                    login.setPinyinName(value);
                    IMLoginManager.instance().setLoginInfo(login);

                    UserEntity currentUser = IMContactManager.instance()
                            .findContact(login.getPeerId());
                    if (currentUser != null) {
                        currentUser.setRealName(value);
                        currentUser.setPinyinName(value);

                        IMContactManager.instance().insertOrUpdateUser(currentUser);
                    }

                    triggerEvent(UserInfoEvent.USER_INFO_DATA_UPDATE_PHONE);
                }

                triggerEvent(UserInfoEvent.USER_INFO_DATA_UPDATE);

            }
            break;

            default: {
                xiaoweiCode = code;
                logger.e("Avatar#Avatar msg server inner failed, result:%s", code);
                triggerEvent(UserInfoEvent.USER_INFO_DATA_FAIL);

            }
            break;
        }

    }

    /**
     * 修改通知
     */
    public void onRepChangeUserInfo(IMBuddy.IMChangeNotice imChangInfo) {
        if (imChangInfo == null) {
            logger.e("Avatar#Avatar LoginResponse failed");
            return;
        }
        ChangeDataType change_type = imChangInfo.getChangeType();

        switch (change_type) {
            case CHANGE_NEW_FRIEND: {

                if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {
                    int changeUser = imChangInfo.getChangeUser();
                    // guanweile
                    ArrayList<Integer> list = new ArrayList<>();
                    list.add(changeUser);
                    reqGetDetaillUsers(list);


                } else {
                    int deviceId = Integer.parseInt(imChangInfo.getValue());

                    // guanweile
                    ArrayList<Integer> list = new ArrayList<>();
                    list.add(deviceId);
                    reqGetDetaillUsers(list);

                    //请求配置信息
                    IMDeviceManager.instance().DeviceConfigReq(deviceId);
                }

            }
            break;
            case CHANGE_DEL_FRIEND: {

                if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {

                    int deviceId = imChangInfo.getChangeUser();
                    UserEntity deviceUser = findParentContact(deviceId);
                    if (deviceUser != null) {
                        deletUpdateParentInfo(deviceUser);
                    }
                } else {
                    int deviceId = Integer.parseInt(imChangInfo.getValue());
                    UserEntity deviceUser = findDeviceContact(deviceId);
                    if (deviceUser != null) {
                        IMDeviceManager.instance().deleteDevice(deviceUser);
                    }
                }

            }
            break;

            case CHANGE_GROUP_CREAT: {

                String value = imChangInfo.getValue();
                int groupId = Integer.parseInt(value);
                IMGroupManager.instance().reqGroupDetailInfo(groupId);
            }
            break;

            case CHANGE_USERINFO_NICK: // 昵称
            {
                int change_user = imChangInfo.getChangeUser();
                String value = imChangInfo.getValue();
                // guanweile
                if (findFriendsContact(change_user) != null) {
                    UserEntity user = findFriendsContact(change_user);
                    user.setMainName(value);
                    UpdateFriendsContact(user);

                }

                if (findContact(change_user) != null) {
                    UserEntity user = findContact(change_user);
                    user.setMainName(value);
                    insertOrUpdateUser(user);
                }

                triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
            }
            break;
            case CHANGE_USERINFO_PHONE: // 电话
            {
                int change_user = imChangInfo.getChangeUser();
                String value = imChangInfo.getValue();
                // guanweile
                if (findFriendsContact(change_user) != null) {
                    UserEntity user = findFriendsContact(change_user);
                    user.setPhone(value);
                    UpdateFriendsContact(user);

                }

                if (findContact(change_user) != null) {
                    UserEntity user = findContact(change_user);
                    user.setPhone(value);
                    insertOrUpdateUser(user);
                }
                triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
            }
            break;
            case CHANGE_USERINFO_AVATAR: // 头像
            {
                int change_user = imChangInfo.getChangeUser();
                String value = imChangInfo.getValue();
                // guanweile
                if (findFriendsContact(change_user) != null) {
                    UserEntity user = findFriendsContact(change_user);
                    // user.setAvatar(SystemConfigSp.instance().getStrConfig(
                    // SystemConfigSp.SysCfgDimension.MSFSSERVER)
                    // + value);

                    user.setAvatar(value);

                    UpdateFriendsContact(user);

                }

                if (findContact(change_user) != null) {
                    UserEntity user = findContact(change_user);
                    // user.setAvatar(SystemConfigSp.instance().getStrConfig(
                    // SystemConfigSp.SysCfgDimension.MSFSSERVER)
                    // + value);

                    user.setAvatar(value);
                    insertOrUpdateUser(user);
                }

                triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
            }
            break;
            case CHANGE_USERINFO_SEX: // 性别
            {
                int change_user = imChangInfo.getChangeUser();
                String value = imChangInfo.getValue();
                // guanweile
                if (findFriendsContact(change_user) != null) {
                    UserEntity user = findFriendsContact(change_user);
                    user.setGender(Integer.parseInt(value));
                    UpdateFriendsContact(user);

                }

                if (findContact(change_user) != null) {
                    UserEntity user = findContact(change_user);
                    user.setGender(Integer.parseInt(value));
                    insertOrUpdateUser(user);
                }

                triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
            }
            break;
            case CHANGE_USERINFO_EMAIL: // 邮箱
            {
                int change_user = imChangInfo.getChangeUser();
                String value = imChangInfo.getValue();
                // guanweile
                if (findFriendsContact(change_user) != null) {
                    UserEntity user = findFriendsContact(change_user);
                    user.setEmail(value);
                    UpdateFriendsContact(user);

                }

                if (findContact(change_user) != null) {
                    UserEntity user = findContact(change_user);
                    user.setEmail(value);
                    insertOrUpdateUser(user);
                }

                triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
            }
            break;
            case CHANGE_USERINFO_DEPART: // 部门
            {
                int change_user = imChangInfo.getChangeUser();
                String value = imChangInfo.getValue();
                // guanweile
                if (findFriendsContact(change_user) != null) {
                    UserEntity user = findFriendsContact(change_user);
                    user.setDepartmentId(Integer.parseInt(value));
                    UpdateFriendsContact(user);

                }

                if (findContact(change_user) != null) {
                    UserEntity user = findContact(change_user);
                    user.setDepartmentId(Integer.parseInt(value));
                    insertOrUpdateUser(user);
                }
                triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
            }
            break;
            case CHANGE_USERINFO_SIGNINFO: // 签名
            {
                int change_user = imChangInfo.getChangeUser();
                String value = imChangInfo.getValue();
                // guanweile
                if (findFriendsContact(change_user) != null) {
                    UserEntity user = findFriendsContact(change_user);
                    user.setSign_info(value);
                    UpdateFriendsContact(user);

                }

                if (findContact(change_user) != null) {
                    UserEntity user = findContact(change_user);
                    user.setSign_info(value);
                    insertOrUpdateUser(user);
                }
                triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
            }
            break;
            case CHANGE_USERINFO_ADDRESS: // 地区
            {
                int change_user = imChangInfo.getChangeUser();
                String value = imChangInfo.getValue();
                // guanweile
                if (findFriendsContact(change_user) != null) {
                    UserEntity user = findFriendsContact(change_user);
                    user.setMainName(value);
                    UpdateFriendsContact(user);

                }

                if (findContact(change_user) != null) {
                    UserEntity user = findContact(change_user);
                    user.setMainName(value);
                    insertOrUpdateUser(user);
                }
                triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
            }
            break;

            case CHANGE_GROUP_UPDATE_GROUPNAME: // 群名称修改
            {
                int change_user = imChangInfo.getChangeUser();
                String gourpName = imChangInfo.getValue();
                String gourpId = imChangInfo.getExtValue();

                GroupEntity group = IMGroupManager.instance().findGroup(
                        Integer.parseInt(gourpId));

                if (group != null) {
                    group.setMainName(gourpName);
                    IMGroupManager.instance().updateGroup(group);
                    triggerEvent(UserInfoEvent.USER_INFO_UPDATE); // guanweile
                }

            }
            break;

            case CHANGE_GROUP_USER_UPDATE_NICK: // 群昵称修改
            {
                String extValue = imChangInfo.getExtValue(); // 修改的群Id
                String value = imChangInfo.getValue(); // 修改的值

                int userId = imChangInfo.getChangeUser(); // 谁修改的
                int groupId = Integer.parseInt(extValue);

                GroupNickEntity entity = IMGroupManager.instance().findGroupNick(
                        groupId, userId);
                if (entity != null) {
                    entity.setNick(value);
                    int timeNow = (int) (System.currentTimeMillis() / 1000);
                    entity.setUpdated(timeNow);
                    dbInterface.insertOrUpdateGroupNick(entity);
                }

                triggerEvent(new GroupEvent(
                        GroupEvent.Event.CHANGE_GROUP_NICK_SUCCESS));
            }
            break;

            case CHANGE_GROUP_NOTICE_BOARD: // 群公告
            {

                String value = imChangInfo.getExtValue();
                int groupId = Integer.parseInt(value);
                int change_user = imChangInfo.getChangeUser();
                String board = imChangInfo.getValue();
                String time = imChangInfo.getServerTime() + "";

                GroupEntity group = IMGroupManager.instance().findGroup(groupId);
                if (group != null) {
                    group.setBoard(board);
                    group.setBoardTime(time);
                    IMGroupManager.instance().updateGroup(group);
                    triggerEvent(new GroupEvent(
                            GroupEvent.Event.CHANGE_GROUP_EXIT_SUCCESS));
                }
            }
            break;

            case CHANGE_GROUP_USER_EXIT: // 退出通知
            {
                String value = imChangInfo.getExtValue();
                int groupId = Integer.parseInt(value);
                int change_user = imChangInfo.getChangeUser();

                GroupEntity group = IMGroupManager.instance().findGroup(groupId);

                if (group != null) {

                    IMGroupManager.instance().deleteGroup(group);
                    triggerEvent(new GroupEvent(
                            GroupEvent.Event.CHANGE_GROUP_EXIT_SUCCESS));

                }

            }
            break;

//			case CONFIG_SYNC_FAMILY_MOBILE: //
//			{
//
//			}
//			break;
            default:
                break;

        }

    }

    /**
     * 获取用户状态的信息 回复
     *
     * @param imUsersInfoRsp
     */
    public void onRepDetailUsersGroup(IMBuddy.IMUsersInfoRsp imUsersInfoRsp,
                                      int groupId) {

        int loginId = imUsersInfoRsp.getUserId();

        boolean needEvent = false;
        List<IMBaseDefine.UserInfo> userInfoList = imUsersInfoRsp
                .getUserInfoListList();

        ArrayList<UserEntity> dbNeed = new ArrayList<>();
        ArrayList<UserEntity> friendsNeed = new ArrayList<>();
        ArrayList<UserEntity> weiFriendsNeed = new ArrayList<>();

        for (IMBaseDefine.UserInfo userInfo : userInfoList) {
            UserEntity userEntity = ProtoBuf2JavaBean.getUserEntity(userInfo);
            if (userEntity.getPeerId() == IMLoginManager.instance()
                    .getLoginId()) {
                userEntity.setFriendNeedAuth(IMLoginManager.instance()
                        .getLoginInfo().getFriendNeedAuth());
            }

            int userId = userEntity.getPeerId();
            if (userEntity.getPeerId() == IMLoginManager.instance()
                    .getLoginId()) {
                userEntity.setFriendNeedAuth(IMLoginManager.instance()
                        .getLoginInfo().getFriendNeedAuth());
            }

            GroupNickEntity entity = IMGroupManager.instance().findGroupNick(
                    groupId, userEntity.getPeerId());

            if (entity != null) {

                int timeNow = (int) (System.currentTimeMillis() / 1000);
                entity.setUpdated(timeNow);
                if (userInfo.getGroupNick().equals("")) {

                    if (userInfo.getComment().equals("")) {
                        entity.setNick(userInfo.getUserNickName());
                    } else {
                        entity.setNick(userInfo.getComment());
                    }

                } else {
                    entity.setNick(userInfo.getGroupNick());
                }

                // dbInterface.insertOrUpdateGroupNick(entity);
                IMGroupManager.instance().addGroupNick(entity);

            } else {

                GroupNickEntity entity1 = new GroupNickEntity();
                int timeNow = (int) (System.currentTimeMillis() / 1000);
                entity1.setUpdated(timeNow);
                entity1.setCreated(timeNow);
                if (userInfo.getGroupNick().equals("")) {

                    if (userInfo.getComment().equals("")) {
                        entity1.setNick(userInfo.getUserNickName());
                    } else {
                        entity1.setNick(userInfo.getComment());
                    }

                } else {
                    entity1.setNick(userInfo.getGroupNick());
                }
                entity1.setStatus(DBConstant.SHOW_GROUP_NICK_OPEN);
                entity1.setUserId(userId);
                entity1.setGroupId(groupId);

                IMGroupManager.instance().addGroupNick(entity1);

            }

            if (userMap.containsKey(userId)
                    && userMap.get(userId).equals(userEntity)) {
                // 没有必要通知更新
                triggerEvent(UserInfoEvent.USER_SCAN_INFO_UPDATE);

            } else {

                needEvent = true;
                if (userMap.containsKey(userId)) {
                    userEntity.setId(userMap.get(userId).getId());
                }

                // 如果是设备
                userMap.put(userEntity.getPeerId(), userEntity);
                dbNeed.add(userEntity);
                if (userInfo.getUserId() == loginId) {
                    IMLoginManager.instance().setLoginInfo(userEntity);

                } else if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {
                    friendsMap.put(userEntity.getPeerId(), userEntity);
                    friendsNeed.add(userEntity);

                } else if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {

                    if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {

                        UserEntity user = findParentContact(userEntity.getPeerId());
                        if (user != null) {
                            user.setOnLine(userEntity.getOnLine());
                            updateParentContact(user);
                        } else {
                            updateParentContact(userEntity);
                        }

                    } else {
                        // 设备在线数据
                        UserEntity user = findDeviceContact(userEntity.getPeerId());
                        if (user != null) {
                            user.setOnLine(userEntity.getOnLine());
                            dbInterface.insertOrUpdateDevice(user);
                        }
                    }

                } else if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_NO) {

                    if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {

                        UserEntity user = findParentContact(userEntity.getPeerId());
                        if (user != null) {
                            deletUpdateParentInfo(user);
                        }
                    } else {
                        // 设备在线数据
                        UserEntity user = findDeviceContact(userEntity.getPeerId());
                        if (user != null) {
                            user.setOnLine(userEntity.getOnLine());
                            deleteDevUser(user);
                        }
                    }
                }
            }
        }

        triggerEvent(new GroupEvent(GroupEvent.Event.CHANGE_GROUP_NICK_SUCCESS));

        dbInterface.batchInsertOrUpdateUser(dbNeed);
        dbInterface.batchInsertOrUpdateFriends(friendsNeed);

        // 判断有没有必要进行推送
        if (needEvent) {
            triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
        }

    }

    /**
     * 获取用户状态的信息 回复
     *
     * @param imUsersInfoRsp
     */
    public void onRepDetailUsersStat(IMBuddy.IMUsersStatRsp imUsersInfoRsp) {
        Log.i("xiaowei", "onRepDetailUsersStat: ");
        int userId = imUsersInfoRsp.getUserId();

        List<UserStat> userInfoList = imUsersInfoRsp.getUserStatListList();
        if (userInfoList.size() <= 0) {
            return;
        }

        for (IMBaseDefine.UserStat userInfo : userInfoList) {

            int id = userInfo.getUserId();
            UserStatType stat = userInfo.getStatus();
            UserEntity entity = findContact(id);


            if (entity == null) {
                entity = findDeviceContact(id);
                if (entity == null) {
                    entity = findFriendsContact(id);
                }
            }

			/*
            if ((entity != null)
					&& (Utils.isClientType(entity))) {
				entity = findDeviceContact(id);
			} else {
				if (entity == null) {
					entity = findDeviceContact(id);
					if(){

					}
				}else if(!Utils.isClientType(entity)){
					entity = findContact(id);
				}
			}
			*/

            if (entity != null) {

                entity.setOnLine(stat.ordinal());

                if (Utils.isClientType(entity)) {
                    if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                        // 更新设备
                        insertOrUpdateDev(entity);
                        if (findContact(entity.getPeerId()) != null) {
                            UserEntity entityUser = ProtoBuf2JavaBean
                                    .getUserCopyEntity(entity);
                            UserEntity dev = findContact(entity.getPeerId());
                            entityUser.setId(dev.getId());
                            insertOrUpdateUser(entityUser);

                        } else {
                            UserEntity entityUser = ProtoBuf2JavaBean
                                    .getUserCopyEntity(entity);
                            insertOrUpdateUser(entityUser);
                        }
                    } else if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {


                        friendsMap.put(entity.getPeerId(), entity);
                        dbInterface.insertOrUpdateFriends(entity);


                        UserEntity entity1 = ProtoBuf2JavaBean
                                .getUserCopyEntity(entity);
                        userMap.put(entity1.getPeerId(), entity1);
                        insertOrUpdateUser(entity1);

                    } else {

                        userMap.put(entity.getPeerId(), entity);
                        insertOrUpdateUser(entity);


                        if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {

                            UserEntity parents = findParentContact(entity.getPeerId());
                            if (parents != null) {
                                deletUpdateParentInfo(parents);
                            }
                        } else {
                            //跟Login的不是位友关系的 本地有的 删除
                            //删除配置文件
                            UserEntity devices = findDeviceContact(entity.getPeerId());
                            if (devices != null) {
                                deleteDevUser(devices);
                            }
                        }
                    }
                } else {

                    if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {
                        dbInterface.insertOrUpdateFriends(entity);
                        friendsMap.put(entity.getPeerId(), entity);

                    } else if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) { // guanweile

                        dbInterface.insertOrUpdateParentInfo(entity);
                        yuFriendsMap.put(entity.getPeerId(), entity);
                    } else if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_NO) {

                        if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {

                            UserEntity parents = findParentContact(entity.getPeerId());
                            if (parents != null) {
                                deletUpdateParentInfo(parents);
                            }
                        } else {
                            //跟Login的不是位友关系的 本地有的 删除
                            //删除配置文件
                            UserEntity devices = findDeviceContact(entity.getPeerId());
                            if (devices != null) {
                                deleteDevUser(devices);
                            }
                        }
                    }

                    UserEntity tempUser = findContact(entity.getPeerId());
                    if (tempUser != null) {

                        UserEntity entityUser = ProtoBuf2JavaBean
                                .getUserCopyEntity(entity);
                        entityUser.setId(tempUser.getId());
                        userMap.put(entityUser.getPeerId(), entityUser);
                        insertOrUpdateUser(entityUser);

                    } else {
                        UserEntity entityUser = ProtoBuf2JavaBean
                                .getUserCopyEntity(entity);

                        userMap.put(entityUser.getPeerId(), entityUser);
                        insertOrUpdateUser(entityUser);

                    }
                }
            }
        }
        triggerEvent(UserInfoEvent.USER_INFO_UPDATE_STAT);
    }


    /**
     * 用户状态回复1
     *
     * @param imUsersInfoRsp
     */
    public void onRepDetailUsersStat1(IMBuddy.IMUsersStatRsp imUsersInfoRsp) {
        int userId = imUsersInfoRsp.getUserId();

        List<UserStat> userInfoList = imUsersInfoRsp.getUserStatListList();
        if (userInfoList.size() <= 0) {
            return;
        }

        for (IMBaseDefine.UserStat userInfo : userInfoList) {

            int id = userInfo.getUserId();
            UserStatType stat = userInfo.getStatus();
            UserEntity entity = findContact(id);
//			if ((entity != null)
//					&& (entity.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE 
//					|| entity.getUserType() == ClientType.CLIENT_TYPE_FISE_CAR_VALUE
//					|| entity.getUserType() == ClientType.CLIENT_TYPE_FISE_WATCH_VALUE)) {
            if ((entity != null)
                    && (Utils.isClientType(entity))) {

                entity = findDeviceContact(id);
            } else {
                if (entity == null) {
                    entity = findDeviceContact(id);
                } else if (!Utils.isClientType(entity)) {
                    entity = findContact(id);
                }
            }

            if (entity != null) {

                entity.setOnLine(stat.ordinal());

                if (Utils.isClientType(entity)) {

                    if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                        // 更新设备
                        insertOrUpdateDev(entity);
                        if (findContact(entity.getPeerId()) != null) {

                            UserEntity entityUser = ProtoBuf2JavaBean
                                    .getUserCopyEntity(entity);
                            UserEntity dev = findContact(entity.getPeerId());
                            entityUser.setId(dev.getId());
                            insertOrUpdateUser(entityUser);
                        } else {
                            UserEntity entityUser = ProtoBuf2JavaBean
                                    .getUserCopyEntity(entity);
                            insertOrUpdateUser(entityUser);
                        }
                    } else if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {

                        friendsMap.put(entity.getPeerId(), entity);
                        dbInterface.insertOrUpdateFriends(entity);


                        UserEntity entity1 = ProtoBuf2JavaBean
                                .getUserCopyEntity(entity);
                        userMap.put(entity1.getPeerId(), entity1);
                        insertOrUpdateUser(entity1);

                    } else {

                        userMap.put(entity.getPeerId(), entity);
                        insertOrUpdateUser(entity);


                        if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {

                            UserEntity parents = findParentContact(entity.getPeerId());
                            if (parents != null) {
                                deletUpdateParentInfo(parents);
                            }
                        } else {
                            //跟Login的不是位友关系的 本地有的 删除
                            //删除配置文件
                            UserEntity devices = findDeviceContact(entity.getPeerId());
                            if (devices != null) {
                                deleteDevUser(devices);
                            }
                        }
                    }
                } else {

                    if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {
                        friendsMap.put(entity.getPeerId(), entity);
                        inserOrFriendsContact(entity);

                    } else if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) { // guanweile

                        if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {
                            yuFriendsMap.put(entity.getPeerId(), entity);
                            updateParentContact(entity);
                        } else {
                            insertOrUpdateDev(entity);
                        }


                    } else if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_NO) {
                        if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {

                            UserEntity parents = findParentContact(entity.getPeerId());
                            if (parents != null) {
                                deletUpdateParentInfo(parents);
                            }
                        } else {
                            //跟Login的不是位友关系的 本地有的 删除
                            //删除配置文件
                            UserEntity devices = findDeviceContact(entity.getPeerId());
                            if (devices != null) {
                                deleteDevUser(devices);
                            }
                        }
                    }

                    UserEntity tempUser = findContact(entity.getPeerId());
                    if (tempUser != null) {

                        UserEntity entityUser = ProtoBuf2JavaBean
                                .getUserCopyEntity(entity);
                        entityUser.setId(tempUser.getId());
                        userMap.put(entityUser.getPeerId(), entityUser);
                        insertOrUpdateUser(entityUser);

                    } else {
                        UserEntity entityUser = ProtoBuf2JavaBean
                                .getUserCopyEntity(entity);

                        userMap.put(entityUser.getPeerId(), entityUser);
                        insertOrUpdateUser(entityUser);

                    }
                }
                triggerEvent(UserInfoEvent.USER_INFO_UPDATE_POSTION_TOUCH);

            }
        }
    }


    /**
     * 获取用户详细的信息
     *
     * @param imUsersInfoRsp
     */
    public void onRepDetailUsers(IMBuddy.IMUsersInfoRsp imUsersInfoRsp) {
        int loginId = imUsersInfoRsp.getUserId();

        boolean needEvent = false;
        boolean needDevEvent = false;

        List<IMBaseDefine.UserInfo> userInfoList = imUsersInfoRsp
                .getUserInfoListList();

        ArrayList<UserEntity> dbNeed = new ArrayList<>();
        ArrayList<UserEntity> friendsNeed = new ArrayList<>();

        ArrayList<UserEntity> devicebReq = new ArrayList<>();
        ArrayList<UserEntity> parentReq = new ArrayList<>();

        for (IMBaseDefine.UserInfo userInfo : userInfoList) {
            UserEntity userEntity = ProtoBuf2JavaBean.getUserEntity(userInfo);
            int userId = userEntity.getPeerId();
            if (userEntity.getPeerId() == IMLoginManager.instance()
                    .getLoginId()) {
                userEntity.setFriendNeedAuth(IMLoginManager.instance()
                        .getLoginInfo().getFriendNeedAuth());
            }

            //　是否为设备
            if (Utils.isClientType(userEntity)) {

                //如果是位友一定是我的设备
                if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {  //FRIENDS_TYPE_NO   FRIENDS_TYPE_VERIFY

                    if (findDeviceContact(userEntity.getPeerId()) != null) {
                        // 更新再现状态
                        UserEntity user = findDeviceContact(userEntity
                                .getPeerId());
                        userEntity.setOnLine(user.getOnLine());

                        // 没有必要通知更新
                        deviceMap.put(userEntity.getPeerId(), userEntity);
                        devicebReq.add(userEntity);

                        UserEntity entity1 = ProtoBuf2JavaBean
                                .getUserCopyEntity(userEntity);
                        userMap.put(entity1.getPeerId(), entity1);
                        dbNeed.add(entity1);

                        needDevEvent = true;

                    } else {

                        deviceMap.put(userEntity.getPeerId(), userEntity);
                        devicebReq.add(userEntity);

                        UserEntity entity1 = ProtoBuf2JavaBean
                                .getUserCopyEntity(userEntity);
                        userMap.put(entity1.getPeerId(), entity1);
                        dbNeed.add(entity1);

                        needDevEvent = true;
                    }
                } else if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {


                    friendsMap.put(userEntity.getPeerId(), userEntity);
                    friendsNeed.add(userEntity);

                    UserEntity entity1 = ProtoBuf2JavaBean
                            .getUserCopyEntity(userEntity);
                    userMap.put(entity1.getPeerId(), entity1);
                    dbNeed.add(entity1);

                    needDevEvent = true;

                } else {

                    userMap.put(userEntity.getPeerId(), userEntity);
                    dbNeed.add(userEntity);

                    if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {

                        UserEntity parents = findParentContact(userEntity.getPeerId());
                        if (parents != null) {
                            deletUpdateParentInfo(parents);
                        }
                    } else {
                        //跟Login的不是位友关系的 本地有的 删除
                        //删除配置文件
                        UserEntity devices = findDeviceContact(userEntity.getPeerId());
                        if (devices != null) {
                            deleteDevUser(devices);
                        }
                    }
                }

            } else {
                if (userMap.containsKey(userId)
                        && userMap.get(userId).equals(userEntity)) {

                    if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                        yuFriendsMap.put(userEntity.getPeerId(), userEntity);
                        parentReq.add(userEntity);

                        needEvent = true;
                    }
                    // 没有必要通知更新
                    triggerEvent(UserInfoEvent.USER_SCAN_INFO_UPDATE);
                } else {

                    needEvent = true;
                    //
                    if (userMap.containsKey(userId)) {
                        userEntity.setId(userMap.get(userId).getId());
                    }

                    if (userInfo.getUserId() == loginId) {
                        IMLoginManager.instance().setLoginInfo(userEntity);

                    } else if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {
                        friendsMap.put(userEntity.getPeerId(), userEntity);
                        friendsNeed.add(userEntity);

                    } else if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {


                        if (Utils.isClientType(userEntity)) {
                            // 更新在线状态 原来在线的数据是正确的
                            UserEntity user = findDeviceContact(userEntity
                                    .getPeerId());
                            if (user != null) {
                                userEntity.setOnLine(user.getOnLine());
                                dbInterface.insertOrUpdateDevice(userEntity);
                            }
                        } else {
                            yuFriendsMap.put(userEntity.getPeerId(), userEntity);
                            parentReq.add(userEntity);

                        }
                    } else if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_NO) {

                        if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {

                            UserEntity parents = findParentContact(userEntity.getPeerId());
                            if (parents != null) {
                                deletUpdateParentInfo(parents);
                            }
                        } else {
                            //跟Login的不是位友关系的 本地有的 删除
                            //删除配置文件
                            UserEntity devices = findDeviceContact(userEntity.getPeerId());
                            if (devices != null) {
                                deleteDevUser(devices);
                            }
                        }
                    }

                    // 全部数据
                    userMap.put(userEntity.getPeerId(), userEntity);
                    dbNeed.add(userEntity);

                }
            }
        }

        for (int i = 0; i < devicebReq.size(); i++) {
            imDeviceManager.DeviceConfigReq(devicebReq.get(i).getPeerId());
        }

        // 负责userMap
        dbInterface.batchInsertOrUpdateDevice(devicebReq);
        dbInterface.batchInsertOrUpdateUser(dbNeed);
        dbInterface.batchInsertOrUpdateFriends(friendsNeed);
        dbInterface.batchInsertOrUpdateParentInfo(parentReq);  //孩子端的父母列表


        // 判断有没有必要进行推送
        if (needEvent) {
            triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
        }

        // 更新设备
        if (needDevEvent) {
            triggerEvent(DeviceEvent.USER_INFO_UPDATE_DEVICE_SUCCESS);
        }
    }


    /**
     * 删除 好友/位友数据
     *
     * @param entity
     */
    public void deleteFriends(UserEntity entity) {

        UserEntity user = entity;
        UserEntity userInfo = ProtoBuf2JavaBean.getUserCopyEntity(user);

        if (entity.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {
            friendsMap.remove(entity.getPeerId());
            dbInterface.insertOrDeleteFriens(entity);
        }

        userInfo.setFriend(DBConstant.FRIENDS_TYPE_NO);
        insertOrUpdateUser(userInfo);
        triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
    }


    /**
     * 插入一条跟user有关系的数据
     * db /map都要更新
     *
     * @param userEntity
     */
    public void insertOrUpdateUser(UserEntity userEntity) {
        if (userEntity != null) {
            dbInterface.insertOrUpdateUser(userEntity);
            userMap.put(userEntity.getPeerId(), userEntity);
        }
    }

    /**
     * 插入一条设备数据
     *
     * @param userEntity
     */
    public void insertOrUpdateDev(UserEntity userEntity) {
        if (userEntity != null) {
            if (Utils.isClientType(userEntity)) {
                if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                    dbInterface.insertOrUpdateDevice(userEntity);
                    deviceMap.put(userEntity.getPeerId(), userEntity);
                }
            }

        }
    }


    /**
     * 将好友加入黑名单
     *
     * @param userEntity
     */
    public void addBlackList(UserEntity userEntity) {
        if (userEntity != null) {

            userEntity.setAuth(DBConstant.AUTH_TYPE_BLACK);
            UserEntity userInfo = ProtoBuf2JavaBean
                    .getUserCopyEntity(userEntity);
            UserEntity userAll = ProtoBuf2JavaBean
                    .getUserCopyEntity(userEntity);

            dbInterface.insertOrDeleteFriens(userEntity);
            // friendsMap.remove(userEntity);
            friendsMap.remove(userEntity.getPeerId());

            dbInterface.insertOrUpdateBlackList(userInfo);
            blackListMap.put(userInfo.getPeerId(), userInfo);

            dbInterface.insertOrUpdateUser(userAll);
            userMap.put(userAll.getPeerId(), userAll);
        }
    }


    /**
     * 移出黑名单
     *
     * @param userEntity
     */
    public void removeBlackList(UserEntity userEntity) {

        if (userEntity != null) {

            userEntity.setAuth(userEntity.getIsFriend());
            UserEntity userInfo = ProtoBuf2JavaBean
                    .getUserCopyEntity(userEntity);
            UserEntity userAll = ProtoBuf2JavaBean
                    .getUserCopyEntity(userEntity);

            // blackListMap.remove(userEntity);

            dbInterface.insertOrDeleteBlackList(userEntity);
            blackListMap.remove(userEntity.getPeerId());

            dbInterface.insertOrUpdateFriends(userInfo);
            friendsMap.put(userInfo.getPeerId(), userInfo);

            dbInterface.insertOrUpdateUser(userAll);
            userMap.put(userAll.getPeerId(), userAll);
        }
    }


    /**
     * 获取全部的设备列表信息
     *
     * @return
     */
    public List<UserEntity> getContactDevicesList() {

        // todo eric efficiency
        //List<UserEntity> contactListAll = new ArrayList<>(deviceMap.values());
        List<UserEntity> contactList = new ArrayList<>(deviceMap.values());


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
     * 获取全部的管理员/父母列表信息
     *
     * @return
     */
    public List<UserEntity> getContactParentList() {

        // todo eric efficiency
        List<UserEntity> contactList = new ArrayList<>(yuFriendsMap.values());


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
     * 获取全部监控的数据(包含设备/位友)
     *
     * @return
     */
    public List<UserEntity> getContacMonitorList() {

        // todo eric efficiency
        List<UserEntity> deviceList = new ArrayList<>(deviceMap.values());
        List<UserEntity> monitorList = new ArrayList<>();
        for (int i = 0; i < deviceList.size(); i++) {
            monitorList.add(deviceList.get(i));
        }

        Collections.sort(monitorList, new Comparator<UserEntity>() {
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
        return monitorList;
    }


    /**
     * 获取位置信息请求
     *
     * @param id
     */
    public void sendLocationPacket(int id) {
        logger.d("heartbeat#sendLocationPacket");

        int usrId = IMLoginManager.instance().getLoginId();

        IMUserAction.LocationReq imLocation = IMUserAction.LocationReq
                .newBuilder().setUserId(id).setFromId(usrId).setStartTime(0)
                .setEndTime(0).build();

        int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
        int cid = IMBaseDefine.UserActionCmdID.CID_LOCATION_REQ_VALUE;

        IMSocketManager.instance().sendRequest(imLocation, sid, cid);

    }


    /**
     * 获取好友数据  gzc
     *
     * @return
     */
    public List<UserEntity> getContactFriendsList() {

        // todo eric efficiency
        List<UserEntity> contactListTemp = new ArrayList<>(friendsMap.values());

        List<UserEntity> contactList = new ArrayList<>();
        for (int i = 0; i < contactListTemp.size(); i++) {
            //如果不是设备类型则加入
            contactList.add(contactListTemp.get(i));
        }

        Collections.sort(contactList, new Comparator<UserEntity>() {
            @Override
            public int compare(UserEntity entity1, UserEntity entity2) {

                //如果有备注以备注　排序
                if (entity1.getPinyinElement().pinyin != null) {
                    if (entity1.getComment() != null && (!entity1.getComment().equals(""))) {
                        PinYin.getPinYin(entity1.getComment(),
                                entity1.getPinyinElement());

                    } else {
                        PinYin.getPinYin(entity1.getMainName(),
                                entity1.getPinyinElement());
                    }

                }

                if (entity2.getPinyinElement().pinyin != null) {
                    if (entity2.getComment() != null && (!entity2.getComment().equals(""))) {
                        PinYin.getPinYin(entity2.getComment(),
                                entity2.getPinyinElement());

                    } else {
                        PinYin.getPinYin(entity2.getMainName(),
                                entity2.getPinyinElement());
                    }
                }

                if (entity2.getPinyinElement().pinyin != null
                        && entity2.getPinyinElement().pinyin.startsWith("#")) {
                    return -1;
                } else if (entity1.getPinyinElement().pinyin != null
                        && entity1.getPinyinElement().pinyin.startsWith("#")) {
                    // todo eric guess: latter is > 0
                    return 1;
                } else {
                    if (entity1.getPinyinElement().pinyin == null) {
                        if (entity1.getComment() != null && (!entity1.getComment().equals(""))) {
                            PinYin.getPinYin(entity1.getComment(),
                                    entity1.getPinyinElement());
                        } else {
                            PinYin.getPinYin(entity1.getMainName(),
                                    entity1.getPinyinElement());
                        }

                    }
                    if (entity2.getPinyinElement().pinyin == null) {

                        if (entity2.getComment() != null && (!entity2.getComment().equals(""))) {
                            PinYin.getPinYin(entity2.getComment(),
                                    entity2.getPinyinElement());
                        } else {
                            PinYin.getPinYin(entity2.getMainName(),
                                    entity2.getPinyinElement());
                        }

                    }
                    return entity1.getPinyinElement().pinyin
                            .compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                }
            }
        });
        return contactList;
    }


    public List<CommonUserInfo> getCommonUserSortedList() {

        List<CommonUserInfo> commonUserList = new ArrayList<>();
        List<UserEntity> contactList = new ArrayList<>(friendsMap.values());
        List<GroupEntity> normalGroupList = imGroupManager.getNormalGroupList();

        for (int i = 0; i < contactList.size(); i++) {
            CommonUserInfo commonUser = new CommonUserInfo();
            commonUser.setUserInfoID(contactList.get(i).getPeerId());
            commonUser.setUserName(contactList.get(i).getMainName());
            commonUser.setUserType(contactList.get(i).getType());
            commonUser.setAvatarUrl(contactList.get(i).getAvatar());
            commonUser.setSessionKey(contactList.get(i).getSessionKey());
            PinYin.getPinYin(commonUser.getUserName(), commonUser.getPinyinElement());
            commonUserList.add(commonUser);
        }

        for (int i = 0; i < normalGroupList.size(); i++) {

            CommonUserInfo commonUser = new CommonUserInfo();
            commonUser.setUserInfoID(normalGroupList.get(i).getPeerId());
            commonUser.setUserName(normalGroupList.get(i).getMainName());
            commonUser.setUserType(normalGroupList.get(i).getType());
            commonUser.setAvatarUrl(normalGroupList.get(i).getAvatar());
            commonUser.setSessionKey(normalGroupList.get(i).getSessionKey());
            commonUser.setUserList(normalGroupList.get(i).getUserList());

            PinYin.getPinYin(commonUser.getUserName(), commonUser.getPinyinElement());
            commonUserList.add(commonUser);
        }


        //sort

        Collections.sort(commonUserList, new Comparator<CommonUserInfo>() {
            @Override
            public int compare(CommonUserInfo entity1, CommonUserInfo entity2) {
                if (entity2.getPinyinElement().pinyin != null && entity2.getPinyinElement().pinyin.startsWith("#")) {
                    return -1;
                } else if (entity1.getPinyinElement().pinyin != null && entity1.getPinyinElement().pinyin.startsWith("#")) {
                    // todo eric guess: latter is > 0
                    return 1;
                } else {
                    if (entity1.getPinyinElement().pinyin == null) {
                        PinYin.getPinYin(entity1.getUserName(),
                                entity1.getPinyinElement());
                    }
                    if (entity2.getPinyinElement().pinyin == null) {
                        PinYin.getPinYin(entity2.getUserName(),
                                entity2.getPinyinElement());
                    }
                    return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                }
            }
        });

        return commonUserList;
    }

    /**
     * 获取好友和位友的数据
     *
     * @return
     */
    public List<UserEntity> getContactFriendsSortedList() {

        // todo eric efficiency
        List<UserEntity> contactList = new ArrayList<>(friendsMap.values());
        List<UserEntity> getContactList = new ArrayList<>();
        //getContactList.addAll(montList);

        for (int i = 0; i < contactList.size(); i++) {
            //如果不是 则加入列表
            getContactList.add(contactList.get(i));
        }

        getContactList.add(imLoginManager.getLoginInfo());
        // guanweile 多了一个for 不可采取

        if (getContactList.size() <= 1) {
            return getContactList;
        }


        Collections.sort(getContactList, new Comparator<UserEntity>() {
            @Override
            public int compare(UserEntity entity1, UserEntity entity2) {
                if (entity2.getPinyinElement().pinyin != null && entity2.getPinyinElement().pinyin.startsWith("#")) {
                    return -1;
                } else if (entity1.getPinyinElement().pinyin != null && entity1.getPinyinElement().pinyin.startsWith("#")) {
                    // todo eric guess: latter is > 0
                    return 1;
                } else {


                    if (entity1.getPinyinElement().pinyin == null) {

                        if (entity1.getComment() != null && (!entity1.getComment().equals(""))) {
                            PinYin.getPinYin(entity1.getComment(),
                                    entity1.getPinyinElement());

                        } else {
                            PinYin.getPinYin(entity1.getMainName(),
                                    entity1.getPinyinElement());
                        }

                    }
                    if (entity2.getPinyinElement().pinyin == null) {
                        if (entity2.getComment() != null && (!entity2.getComment().equals(""))) {
                            PinYin.getPinYin(entity2.getComment(),
                                    entity2.getPinyinElement());
                        } else {
                            PinYin.getPinYin(entity2.getMainName(),
                                    entity2.getPinyinElement());
                        }
                    }
                    return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                }
            }
        });


        List<UserEntity> contactTemp = new ArrayList<>();

        for (int i = 0; i < getContactList.size(); i++) {

            if (getContactList.get(i).getIsFriend() != DBConstant.FRIENDS_TYPE_NO
                    && getContactList.get(i).getIsFriend() != DBConstant.FRIENDS_TYPE_VERIFY) {
                contactTemp.add(getContactList.get(i));
            }
        }
        //return getContactList;
        return contactTemp;
    }


    /**
     * 获取全部跟user有关系的数据
     *
     * @return
     */
    public List<UserEntity> getContactSortedList() {

        // todo eric efficiency
        List<UserEntity> contactList = new ArrayList<>(userMap.values());
        Collections.sort(contactList, new Comparator<UserEntity>() {
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
        return contactList;
    }


    /**
     * 从user中查询与搜索输入的数据匹配
     *
     * @param key
     * @return
     */
    public List<UserEntity> getSearchContactList(String key) {
        List<UserEntity> searchList = new ArrayList<>();
        //friendsMap
        for (Map.Entry<Integer, UserEntity> entry : userMap.entrySet()) {
            UserEntity user = entry.getValue();

            if (user.getIsFriend() != DBConstant.FRIENDS_TYPE_NO
                    && user.getIsFriend() != DBConstant.FRIENDS_TYPE_VERIFY) // guanweile
            {
                if (IMUIHelper.handleContactSearch(key, user)) {
                    searchList.add(user);
                }
            }

        }
        return searchList;
    }

    /**
     * 设备数据中查询与搜索输入的数据匹配
     *
     * @param key
     * @return
     */
    public List<UserEntity> getSearchDeviceList(String key) {
        List<UserEntity> searchList = new ArrayList<>();
        for (Map.Entry<Integer, UserEntity> entry : deviceMap.entrySet()) {
            UserEntity user = entry.getValue();
            if (IMUIHelper.handleContactSearch(key, user)) {
                searchList.add(user);
            }

        }
        return searchList;
    }


    /**
     * 搜索userMap  全部的数据
     *
     * @param key
     * @return
     */
    public UserEntity getSearchContact(String key) {
        UserEntity search = null;
        for (Map.Entry<Integer, UserEntity> entry : userMap.entrySet()) {
            UserEntity user = entry.getValue();
            if (user.getIsFriend() != DBConstant.FRIENDS_TYPE_NO
                    || user.getIsFriend() != DBConstant.FRIENDS_TYPE_VERIFY) { // guanweile

                if (key.equals(user.getMainName())) {
                    search = user;
                }
            }
        }
        return search;
    }

    /**
     * -----------------------实体 get set 定义-----------------------------------
     */

    public Map<Integer, UserEntity> getUserMap() {
        return userMap;
    }

    public Map<Integer, UserEntity> getDeviceMap() {
        return deviceMap;
    }


    public boolean isUserDataReady() {
        return userDataReady;
    }


    /**
     * 获取系统配置
     *
     * @return
     */
    public SystemConfigEntity getSystemConfig() {
        if (systemConfigList.size() > 0) {
            return systemConfigList.get(0);
        }
        return null;
    }

}

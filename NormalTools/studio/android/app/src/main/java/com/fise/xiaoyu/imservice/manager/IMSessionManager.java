package com.fise.xiaoyu.imservice.manager;

import android.text.TextUtils;

import com.fise.xiaoyu.DB.DBInterface;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.GroupNickEntity;
import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.SessionEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.ConfigurationSp;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.entity.PostionMessage;
import com.fise.xiaoyu.imservice.entity.RecentInfo;
import com.fise.xiaoyu.imservice.entity.UnreadEntity;
import com.fise.xiaoyu.imservice.event.SessionEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMBaseDefine.SessionType;
import com.fise.xiaoyu.protobuf.IMBuddy;
import com.fise.xiaoyu.protobuf.helper.EntityChangeEngine;
import com.fise.xiaoyu.protobuf.helper.Java2ProtoBuf;
import com.fise.xiaoyu.protobuf.helper.ProtoBuf2JavaBean;
import com.fise.xiaoyu.ui.activity.MessageActivity;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * app显示首页 最近联系人列表
 */
public class IMSessionManager extends IMManager {
    private Logger logger = Logger.getLogger(IMSessionManager.class);
    private static IMSessionManager inst = new IMSessionManager();

    public static IMSessionManager instance() {
        return inst;
    }

    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private IMLoginManager imLoginManager = IMLoginManager.instance();
    private DBInterface dbInterface = DBInterface.instance();
    private IMGroupManager groupManager = IMGroupManager.instance();

    // key = sessionKey --> sessionType_peerId
    private Map<String, SessionEntity> sessionMap = new ConcurrentHashMap<>();
    // SessionManager 状态字段
    private boolean sessionListReady = false;

    @Override
    public void doOnStart() {
    }

    @Override
    public void reset() {
        sessionListReady = false;
        sessionMap.clear();
    }

    /**
     * 实现自身的事件驱动
     *
     * @param event
     */
    public void triggerEvent(SessionEvent event) {
        switch (event) {
            case RECENT_SESSION_LIST_SUCCESS:
                sessionListReady = true;
                break;
        }
        EventBus.getDefault().post(event);
    }

    public void triggerEvent(UserInfoEvent event) {
        EventBus.getDefault().post(event);
    }

    public void onNormalLoginOk() {
        logger.d("recent#onLogin Successful");
        onLocalLoginOk();
        onLocalNetOk();
    }

    public void onLocalLoginOk() {
        logger.i("session#loadFromDb");
        List<SessionEntity> sessionInfoList = dbInterface.loadAllSession();
        for (SessionEntity sessionInfo : sessionInfoList) {
            sessionMap.put(sessionInfo.getSessionKey(), sessionInfo);
        }

        triggerEvent(SessionEvent.RECENT_SESSION_LIST_SUCCESS);
    }

    public void onLocalNetOk() {
        int latestUpdateTime = dbInterface.getSessionLastTime();
        logger.d("session#更新时间:%d", latestUpdateTime);
        // reqGetRecentContacts(latestUpdateTime);
    }

    /** ----------------------------分割线-------------------------------- */

    // /**
    // * 请求最近回话
    // */
    // private void reqGetRecentContacts(int latestUpdateTime) {
    // logger.i("session#reqGetRecentContacts");
    // int loginId = IMLoginManager.instance().getLoginId();
    // IMBuddy.IMRecentContactSessionReq recentContactSessionReq =
    // IMBuddy.IMRecentContactSessionReq
    // .newBuilder()
    // .setLatestUpdateTime(latestUpdateTime)
    // .setUserId(loginId)
    // .build();
    // int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
    // int cid =
    // IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_RECENT_CONTACT_SESSION_REQUEST_VALUE;
    // imSocketManager.sendRequest(recentContactSessionReq, sid, cid);
    // }

    /**
     * 请求最近回话
     */
    public void reqGetRecentContacts(List<Integer> peerId,
                                     List<SessionType> peerType) {
        logger.i("session#reqGetRecentContacts");
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMRecentContactSessionReq recentContactSessionReq = IMBuddy.IMRecentContactSessionReq
                .newBuilder().setUserId(loginId).addAllPeerIdList(peerId)
                .addAllSessionTypeList(peerType).build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_RECENT_CONTACT_SESSION_REQUEST_VALUE;
        imSocketManager.sendRequest(recentContactSessionReq, sid, cid);
    }

    /**
     * 最近回话返回 与本地的进行merge
     *
     * @param recentContactSessionRsp
     */
    public void onRepRecentContacts(
            IMBuddy.IMRecentContactSessionRsp recentContactSessionRsp) {
        logger.i("session#onRepRecentContacts");
        int userId = recentContactSessionRsp.getUserId();
        List<IMBaseDefine.ContactSessionInfo> contactSessionInfoList = recentContactSessionRsp
                .getContactSessionListList();
        logger.i("contact#user:%d  cnt:%d", userId,
                contactSessionInfoList.size());
        /** 更新最近联系人列表 */

        ArrayList<SessionEntity> needDb = new ArrayList<>();
        for (IMBaseDefine.ContactSessionInfo sessionInfo : contactSessionInfoList) {
            // 返回的没有主键Id
            SessionEntity sessionEntity = ProtoBuf2JavaBean.getSessionEntity(sessionInfo);
            // 并没有按照时间来排序
            sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
            needDb.add(sessionEntity);
        }

        logger.d("session#onRepRecentContacts is ready,now broadcast");

        // 将最新的session信息保存在DB中
        dbInterface.batchInsertOrUpdateSession(needDb);
        if (needDb.size() > 0) {
            triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
        }
    }

    /**
     * 请求删除会话
     */
    public void reqRemoveSessionAll(UserEntity user, int type) {
        logger.i("session#reqRemoveSession");

        int loginId = imLoginManager.getLoginId();

        IMBuddy.IMRemoveSessionReq removeSessionReq = IMBuddy.IMRemoveSessionReq
                .newBuilder().setUserId(loginId).setSessionId(user.getPeerId())
                .setRemoveType(type).build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_REMOVE_SESSION_REQ_VALUE;
        imSocketManager.sendRequest(removeSessionReq, sid, cid);

        // ...To-do
        DBInterface.instance().deleteHistoryMsg();
        DBInterface.instance().removeSession();
        triggerEvent(UserInfoEvent.USER_INFO_DELETE_DATA_SUCCESS);
        removeRecentSessionList();

        IMUnreadMsgManager.instance().readUnreadSessionAll();

        triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
        triggerEvent(SessionEvent.RECENT_SESSION_GRPUP_LIST_UPDATE);

    }


    public void reqRemoveSessionByKey(String sessionKey) {

        DBInterface.instance().deleteHistoryMsg(sessionKey);
        if (sessionMap.containsKey(sessionKey)) {
            sessionMap.remove(sessionKey);
            IMUnreadMsgManager.instance().readUnreadSession(sessionKey);
            dbInterface.deleteSession(sessionKey);

        }
        triggerEvent(SessionEvent.RECENT_SESSION_GRPUP_LIST_UPDATE);
    }

    /**
     * 请求删除会话
     */
    public void reqRemoveSession(RecentInfo recentInfo, int type) {
        logger.i("session#reqRemoveSession");

        int loginId = imLoginManager.getLoginId();
        String sessionKey = recentInfo.getSessionKey();

        // 先删除
        if (type == DBConstant.SESSION_MESSAGE) {

            DBInterface.instance().deleteHistoryMsg(sessionKey);
            triggerEvent(UserInfoEvent.USER_INFO_DELETE_DATA_SUCCESS);

        } else if (type == DBConstant.SESSION_MESSAGE_ALL) { //清空记录

            // ...To-do
            DBInterface.instance().deleteHistoryMsg();
            DBInterface.instance().removeSession();
            triggerEvent(UserInfoEvent.USER_INFO_DELETE_DATA_SUCCESS);
            removeRecentSessionList();

        } else {
            /** 直接本地先删除,清楚未读消息 */
            if (sessionMap.containsKey(sessionKey)) {
                sessionMap.remove(sessionKey);
                IMUnreadMsgManager.instance().readUnreadSession(sessionKey);
                dbInterface.deleteSession(sessionKey);
                ConfigurationSp.instance(ctx, loginId).setSessionTop(
                        sessionKey, false);

                if (recentInfo.getSessionType() == DBConstant.SESSION_TYPE_SINGLE) {
                    triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);

                } else {
                    triggerEvent(SessionEvent.RECENT_SESSION_GRPUP_LIST_UPDATE);
                }

            }
        }

        IMBuddy.IMRemoveSessionReq removeSessionReq = IMBuddy.IMRemoveSessionReq
                .newBuilder()
                .setUserId(loginId)
                .setSessionId(recentInfo.getPeerId())
                .setSessionType(
                        Java2ProtoBuf.getProtoSessionType(recentInfo
                                .getSessionType())).setRemoveType(type).build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_REMOVE_SESSION_REQ_VALUE;
        imSocketManager.sendRequest(removeSessionReq, sid, cid);
    }

    public void removeSession(String sessionKey) {

        if (sessionMap.containsKey(sessionKey)) {
            sessionMap.remove(sessionKey);
            IMUnreadMsgManager.instance().readUnreadSession(sessionKey);
            dbInterface.deleteSession(sessionKey);
        }
    }


    /**
     * 删除会话返回
     */
    public void onRepRemoveSession(IMBuddy.IMRemoveSessionRsp removeSessionRsp) {
        logger.i("session#onRepRemoveSession");
        int resultCode = removeSessionRsp.getResultCode();
        if (0 != resultCode) {
            logger.e("session#removeSession failed");
            return;
        }
    }

    /**
     * 新建群组时候的更新
     */
    public void updateSession(GroupEntity entity) {
        logger.d("recent#updateSession GroupEntity:%s", entity);
        SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.setLatestMsgType(DBConstant.MSG_TYPE_GROUP_TEXT);
        sessionEntity.setUpdated(entity.getUpdated());
        sessionEntity.setCreated(entity.getCreated());
        sessionEntity.setLatestMsgData("[你创建的新群喔]");
        sessionEntity.setTalkId(entity.getCreatorId());
        sessionEntity.setLatestMsgId(0);
        sessionEntity.setPeerId(entity.getPeerId());
        sessionEntity.setPeerType(DBConstant.SESSION_TYPE_GROUP);
        sessionEntity.buildSessionKey();

        sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
        ArrayList<SessionEntity> needDb = new ArrayList<>(1);
        needDb.add(sessionEntity);
        dbInterface.batchInsertOrUpdateSession(needDb);
        triggerEvent(SessionEvent.RECENT_SESSION_GRPUP_LIST_UPDATE);
    }

    /**
     * 1.自己发送消息 2.收到消息
     *
     * @param msg
     */
    public void updateSession(MessageEntity msg) {

        if (msg == null) {
            logger.d("recent#updateSession is end,cause by msg is null");
            return;
        }

//        if (msg.getMsgType() == DBConstant.MSG_TYPE_GROUP_LOCATION
//                || msg.getMsgType() == DBConstant.MSG_TYPE_SINGLE_LOCATION) {
//
//            final PostionMessage postionMessage = PostionMessage.parseFromDB(msg);
//            if (!(postionMessage.getPotionType() == DBConstant.XIAOYU_POSTION_TYPE)) {
//                return;
//            }
//        }
        int loginId = imLoginManager.getLoginId();
        boolean isSend = msg.isSend(loginId);

        // 因为多端同步的问题
        int peerId = msg.getPeerId(isSend);

        SessionEntity sessionEntity = sessionMap.get(msg.getSessionKey());
        if (sessionEntity == null) {
            logger.d("session#updateSession#not found msgSessionEntity");
            sessionEntity = EntityChangeEngine.getSessionEntity(msg);
            sessionEntity.setPeerId(peerId);
            sessionEntity.buildSessionKey();
            // 判断群组的信息是否存在
            if (sessionEntity.getPeerType() == DBConstant.SESSION_TYPE_GROUP) {
                GroupEntity groupEntity = groupManager.findGroup(peerId);
                if (groupEntity == null) {
                    groupManager.reqGroupDetailInfo(peerId);
                }
            }
        } else {
            // todo check if msgid is null/0
            logger.d("session#updateSession#msgSessionEntity already in Map");
            sessionEntity.setUpdated(msg.getUpdated());

            sessionEntity.setLatestMsgData(msg.getMessageDisplay());
            sessionEntity.setTalkId(msg.getFromId());
            sessionEntity.setLatestMsgId(msg.getMsgId());
            sessionEntity.setLatestMsgType(msg.getMsgType());
        }

        /** DB 先更新 */
        ArrayList<SessionEntity> needDb = new ArrayList<>(1);
        needDb.add(sessionEntity);
        dbInterface.batchInsertOrUpdateSession(needDb);
        sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);

        if (sessionEntity.getPeerType() == DBConstant.SESSION_TYPE_GROUP) {
            triggerEvent(SessionEvent.RECENT_SESSION_GRPUP_LIST_UPDATE);
        } else {
            triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
        }
    }


    /**
     * 1.群通知消息更新
     *
     * @param msg
     */
    public void updateNoteSession(MessageEntity msg) {

        if (msg == null) {
            logger.d("recent#updateSession is end,cause by msg is null");
            return;
        }
        int loginId = imLoginManager.getLoginId();
        boolean isSend = msg.isSend(loginId);

        // 因为多端同步的问题
        int peerId = msg.getPeerId(isSend);

        SessionEntity sessionEntity = sessionMap.get(msg.getSessionKey());
        if (sessionEntity == null) {
            logger.d("session#updateSession#not found msgSessionEntity");
            sessionEntity = EntityChangeEngine.getNoteSessionEntity(msg);
            sessionEntity.setPeerId(peerId);
            sessionEntity.buildSessionKey();
            // 判断群组的信息是否存在
            if (sessionEntity.getPeerType() == DBConstant.SESSION_TYPE_GROUP) {
                GroupEntity groupEntity = groupManager.findGroup(peerId);
                if (groupEntity == null) {
                    groupManager.reqGroupDetailInfo(peerId);
                }
            }
        } else {
            logger.d("session#updateSession#msgSessionEntity already in Map");
            sessionEntity.setUpdated(msg.getUpdated());
            sessionEntity.setLatestMsgData(msg.getMessageDisplay());
            sessionEntity.setTalkId(msg.getFromId());
            // todo check if msgid is null/0
            sessionEntity.setLatestMsgId(msg.getMsgId());
            sessionEntity.setLatestMsgType(msg.getMsgType());
        }

        /** DB 先更新 */
        ArrayList<SessionEntity> needDb = new ArrayList<>(1);
        needDb.add(sessionEntity);
        dbInterface.batchInsertOrUpdateSession(needDb);

        sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);

        if (sessionEntity.getPeerType() == DBConstant.SESSION_TYPE_GROUP) {
            triggerEvent(SessionEvent.RECENT_SESSION_GRPUP_LIST_UPDATE);
        } else {
            triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
        }
    }

    public List<SessionEntity> getRecentSessionList() {
        List<SessionEntity> recentInfoList = new ArrayList<>(
                sessionMap.values());
        return recentInfoList;
    }

    public void removeRecentSessionList() {
        sessionMap.clear();
        dbInterface.removeSession();
    }

    private static void sort(List<RecentInfo> data) {
        Collections.sort(data, new Comparator<RecentInfo>() {
            public int compare(RecentInfo o1, RecentInfo o2) {
                Integer a = o1.getUpdateTime();
                Integer b = o2.getUpdateTime();

                boolean isTopA = o1.isTop();
                boolean isTopB = o2.isTop();

                if (isTopA == isTopB) {
                    // 升序
                    // return a.compareTo(b);]
                    // 降序
                    return b.compareTo(a);
                } else {
                    if (isTopA) {
                        return -1;
                    } else {
                        return 1;
                    }
                }

            }
        });
    }

    // 获取最近联系人列表，RecentInfo 是sessionEntity unreadEntity user/group 等等实体的封装
    // todo every time it has to sort, kind of inefficient, change it
    public List<RecentInfo> getRecentListInfo() {
        /** 整理topList */
        List<RecentInfo> recentSessionList = new ArrayList<>();
        int loginId = IMLoginManager.instance().getLoginId();

        List<SessionEntity> sessionList = getRecentSessionList();
        Map<Integer, UserEntity> userMap = IMContactManager.instance()
                .getUserMap();

        Map<Integer, UserEntity> deviceMap = IMContactManager.instance()
                .getDeviceMap();

        Map<String, UnreadEntity> unreadMsgMap = IMUnreadMsgManager.instance()
                .getUnreadMsgMap();

        HashSet<String> topList = ConfigurationSp.instance(ctx, loginId)
                .getSessionTopList();

        for (SessionEntity recentSession : sessionList) {
            int sessionType = recentSession.getPeerType();
            int peerId = recentSession.getPeerId();
            String sessionKey = recentSession.getSessionKey();

            UnreadEntity unreadEntity = unreadMsgMap.get(sessionKey);
            if (sessionType == DBConstant.SESSION_TYPE_GROUP) {

                GroupEntity groupEntity = IMGroupManager.instance().findGroup(
                        peerId);

                RecentInfo recentInfo = new RecentInfo(recentSession,
                        groupEntity, unreadEntity);

                //消息免打扰
                boolean Notification = ConfigurationSp.instance(ctx, loginId)
                        .getCfg(sessionKey,
                                ConfigurationSp.CfgDimension.NOTIFICATION);
                recentInfo.setForbidden(Notification);

                if (unreadEntity != null) {
                    unreadEntity.setForbidden(Notification);
                    unreadMsgMap
                            .put(unreadEntity.getSessionKey(), unreadEntity);
                }

                // 消息置顶
                if (topList != null && topList.contains(sessionKey)) {
                    recentInfo.setTop(true);
                }

                // 谁说的这条信息，只有群组需要，例如 【XXX:您好】
                int lastFromId = recentSession.getTalkId();
                UserEntity talkUser = userMap.get(lastFromId);
                if (talkUser == null) {
                    talkUser = deviceMap.get(lastFromId);
                }

                // 用户已经不存在了
                if (talkUser != null) {
                    String oriContent = recentInfo.getLatestMsgData();

                    String finalContent;
                    // guanweile 显示在群中的昵称
                    GroupNickEntity entity = IMGroupManager.instance()
                            .findGroupNick(recentSession.getPeerId(),
                                    talkUser.getPeerId());

                    if (entity != null) {

                        if (MessageActivity.isShowNick) {

                            //如果是　设备群信息且最后一条消息是设备发出来的 显示昵称
                            if (groupEntity != null && groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY && Utils.isClientType(talkUser)) {
                                finalContent = talkUser.getMainName() + ": "
                                        + oriContent;
                            } else {
                                finalContent = entity.getNick() + ": " + oriContent;
                            }


                        } else {

                            if (talkUser.getComment().equals("")) {
                                finalContent = talkUser.getMainName() + ": "
                                        + oriContent;
                            } else {
                                finalContent = talkUser.getComment() + ": "
                                        + oriContent;
                            }
                        }

                    } else {
                        if (talkUser.getComment().equals("")) {
                            finalContent = talkUser.getMainName() + ": "
                                    + oriContent;
                        } else {
                            finalContent = talkUser.getComment() + ": "
                                    + oriContent;
                        }
                    }

                    // String finalContent = talkUser.getMainName() +
                    // ": "+oriContent;

                    recentInfo.setLatestMsgData(finalContent);
                }
                recentSessionList.add(recentInfo);
            } else if (sessionType == DBConstant.SESSION_TYPE_SINGLE) {

                UserEntity userEntity = IMContactManager.instance()
                        .findFriendsContact(peerId);


                if (userEntity == null) {
                    userEntity = userMap.get(peerId);
                }

                if (userEntity == null) {
                    userEntity = IMContactManager.instance().findDeviceContact(
                            peerId);
                }

                RecentInfo recentInfo = new RecentInfo(recentSession,
                        userEntity, unreadEntity);

                //消息免打扰
                boolean Notification = ConfigurationSp.instance(ctx, loginId)
                        .getCfg(sessionKey,
                                ConfigurationSp.CfgDimension.NOTIFICATION);
                recentInfo.setForbidden(Notification);

                if (unreadEntity != null) {
                    unreadEntity.setForbidden(Notification);
                    unreadMsgMap
                            .put(unreadEntity.getSessionKey(), unreadEntity);
                }

                // 消息置顶
                if (topList != null && topList.contains(sessionKey)) {
                    recentInfo.setTop(true);
                }

                recentSessionList.add(recentInfo);
            }
        }
        sort(recentSessionList);
        return recentSessionList;
    }

    public SessionEntity findSession(String sessionKey) {
        if (sessionMap.size() <= 0 || TextUtils.isEmpty(sessionKey)) {
            return null;
        }
        if (sessionMap.containsKey(sessionKey)) {
            return sessionMap.get(sessionKey);
        }
        return null;
    }

    public PeerEntity findPeerEntity(String sessionKey) {
        if (TextUtils.isEmpty(sessionKey)) {
            return null;
        }
        // 拆分
        PeerEntity peerEntity;
        String[] sessionInfo = EntityChangeEngine.spiltSessionKey(sessionKey);
        int peerType = Integer.parseInt(sessionInfo[0]);
        int peerId = Integer.parseInt(sessionInfo[1]);
        switch (peerType) {
            case DBConstant.SESSION_TYPE_SINGLE: {
                peerEntity = IMContactManager.instance().findContact(peerId);
                if (peerEntity == null) {
                    peerEntity = IMContactManager.instance().findDeviceContact(
                            peerId);
                }
            }
            break;
            case DBConstant.SESSION_TYPE_GROUP: {

                peerEntity = IMGroupManager.instance().findGroup(peerId);
            }
            break;
            default:
                throw new IllegalArgumentException(
                        "findPeerEntity#peerType is illegal,cause by " + peerType);
        }
        return peerEntity;
    }

    /**
     * ------------------------实体的get set-----------------------------
     */
    public boolean isSessionListReady() {
        return sessionListReady;
    }

    public void setSessionListReady(boolean sessionListReady) {
        this.sessionListReady = sessionListReady;
    }
}

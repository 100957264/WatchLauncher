package com.fise.xw.imservice.manager;


import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.fise.xw.config.DBConstant;
import com.fise.xw.DB.DBInterface;
import com.fise.xw.DB.entity.GroupEntity;
import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.DB.entity.ReqFriendsEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.imservice.entity.UnreadEntity;
import com.fise.xw.imservice.event.UnreadEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.protobuf.helper.EntityChangeEngine;
import com.fise.xw.protobuf.helper.Java2ProtoBuf;
import com.fise.xw.protobuf.helper.ProtoBuf2JavaBean;
import com.fise.xw.protobuf.IMBaseDefine;
import com.fise.xw.protobuf.IMBaseDefine.SessionType;
import com.fise.xw.protobuf.IMMessage;
import com.fise.xw.protobuf.IMUserAction;
import com.fise.xw.utils.Logger;
import com.fise.xw.utils.pinyin.PinYin;

import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

/**
 * 未读消息相关的处理，归属于messageEvent中
 * 可以理解为MessageManager的又一次拆分
 * 为session提供未读支持。
 * DB 中不保存
 */
public class IMUnreadMsgManager extends IMManager {
    private Logger logger = Logger.getLogger(IMUnreadMsgManager.class);
	private static IMUnreadMsgManager inst = new IMUnreadMsgManager();
	public static IMUnreadMsgManager instance() {
			return inst;
	}

    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private IMLoginManager loginManager =IMLoginManager.instance(); 
    private DBInterface dbInterface = DBInterface.instance();

    /**key=> sessionKey*/
    private ConcurrentHashMap<String,UnreadEntity> unreadMsgMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,UnreadEntity> unreaReqdMsgMap = new ConcurrentHashMap<>();
    
    private Map<Integer, ReqFriendsEntity> reqUnFriendsMap = new ConcurrentHashMap<>();
    
    private List<UnreadEntity> unreaReqdList =new ArrayList<UnreadEntity>();   
    
    
    private int totalUnreadCount = 0;
    private int totalUnreadReqCount = 0;
    
    private boolean unreadListReady = false;

    @Override
    public void doOnStart() {}


    // 未读消息控制器，本地是不存状态的
    public void onNormalLoginOk(){
        unreadMsgMap.clear();
        unreaReqdMsgMap.clear();
        unreaReqdList.clear();
        reqUnFriendsMap.clear();
        
        reqUnreadMsgContactList();
        LoadAllUnReqFriends();
    }

    public void onLocalNetOk(){
        unreadMsgMap.clear();
        unreaReqdMsgMap.clear();
        unreaReqdList.clear();
        reqUnFriendsMap.clear();
        
        reqUnreadMsgContactList();
        LoadAllUnReqFriends();
    }
    
    
    void LoadAllUnReqFriends(){
    	
		List<ReqFriendsEntity> weiList = dbInterface.loadAllReqUnMessage(); 
		for (ReqFriendsEntity userInfo : weiList) { 
			 
			if(userInfo.getTableReq()!=userInfo.getMessageReq()){ 
				userInfo.setTableReq(userInfo.getMessageReq());
				dbInterface.insertOrUpdateReqUnMessage(userInfo);
				 
			}
			reqUnFriendsMap.put(userInfo.getUserId(), userInfo); 
		}
		
		triggerEvent(UserInfoEvent.USER_INFO_WEI_DATA);
    }  

    @Override
    public void reset() {
        unreadListReady = false;
        unreadMsgMap.clear();
        unreaReqdMsgMap.clear();
        unreaReqdList.clear();
        
    }
    
	public ReqFriendsEntity findUnFriendsMap(int buddyId) {
		if (buddyId > 0 && reqUnFriendsMap.containsKey(buddyId)) {
			return reqUnFriendsMap.get(buddyId);
		}
		return null;
	}

    /**
     * 继承该方法实现自身的事件驱动
     * @param event
     */
    public synchronized void triggerEvent(UnreadEvent event) {
        switch (event.event){
            case UNREAD_MSG_LIST_OK:
                unreadListReady = true;
                break;
        }

        EventBus.getDefault().post(event);
    }

    /**-------------------------------分割线----------------------------------*/
    
 
    
    /**
     * 请求未读消息列表
     */
    private void reqUnreadMsgContactList() {
        logger.i("unread#1reqUnreadMsgContactList");
        int loginId = IMLoginManager.instance().getLoginId();
        IMMessage.IMUnreadMsgCntReq  unreadMsgCntReq  = IMMessage.IMUnreadMsgCntReq
                .newBuilder()
                .setUserId(loginId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_UNREAD_CNT_REQUEST_VALUE;
        imSocketManager.sendRequest(unreadMsgCntReq,sid,cid);
    }

    public void onRepUnreadMsgContactList(IMMessage.IMUnreadMsgCntRsp unreadMsgCntRsp) {
        logger.i("unread#2onRepUnreadMsgContactList");
         
        totalUnreadCount = unreadMsgCntRsp.getTotalCnt();
  
        List<IMBaseDefine.UnreadInfo> unreadInfoList =  unreadMsgCntRsp.getUnreadinfoListList();
        logger.i("unread#unreadMsgCnt:%d, unreadMsgInfoCnt:%d",unreadInfoList.size(),totalUnreadCount);
        logger.i("unread#unreadMsgCnt:%d, unreadMsgInfoCnt:%d",unreadInfoList.size(),totalUnreadReqCount);
        
        ArrayList<UserEntity> needDb = new ArrayList<>();
        ArrayList<ReqFriendsEntity> reqUnFriends = new ArrayList<>();
         
        
        ArrayList<Integer> sessionId = new ArrayList<>();
        ArrayList<SessionType> sessionType = new ArrayList<>();
        
        
        
        for(IMBaseDefine.UnreadInfo unreadInfo:unreadInfoList){
            UnreadEntity unreadEntity = ProtoBuf2JavaBean.getUnreadEntity(unreadInfo);
              
            if((unreadInfo.getLatestMsgType() != IMBaseDefine.MsgType.MSG_TYPE_SINGLE_NOTICE))
            {

                //屏蔽的设定
                addIsForbidden(unreadEntity);
            	 unreadMsgMap.put(unreadEntity.getSessionKey(), unreadEntity);
            	  
            	 sessionId.add(unreadInfo.getSessionId());
            	 sessionType.add(unreadInfo.getSessionType());

                 
            }
             
  
        } 
          
        dbInterface.batchInsertOrUpdateReqFirends(reqUnFriends); 
        dbInterface.batchInsertOrUpdateReq(needDb);
        totalUnreadCount = unreadMsgMap.size();
        totalUnreadReqCount = unreaReqdMsgMap.size();
        
 
        //如果是<=0表示没用session
        if(sessionId.size()>0){ 
            IMSessionManager.instance().reqGetRecentContacts(sessionId,sessionType); 
        }
                   
        if(reqUnFriends.size()>0){
        	triggerEvent(UserInfoEvent.USER_INFO_REQ_UPDATE);
        }
        
         
        triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
        triggerEvent(new UnreadEvent(UnreadEvent.Event.UNREAD_MSG_LIST_OK));
    }

    
    public void updateUnReqFriends(ReqFriendsEntity reqFriends){
    	if(reqFriends!=null){
        	reqUnFriendsMap.put(reqFriends.getUserId(), reqFriends);
        	dbInterface.insertOrUpdateReqUnMessage(reqFriends); 
        	triggerEvent(UserInfoEvent.USER_INFO_REQ_UPDATE);
    	}
    }
    
    
    public List<ReqFriendsEntity> getReqUnFriendsMap() {

		// todo eric efficiency
		List<ReqFriendsEntity> reqUnFriends = new ArrayList<>(reqUnFriendsMap.values());
		return reqUnFriends;
    }
     
	/**
	 * @param event
	 */
	public void triggerEvent(UserInfoEvent event) { 
		EventBus.getDefault().postSticky(event);
	}
	
    /**
     * 回话是否已经被设定为屏蔽
     * @param unreadEntity
     */
    private void addIsForbidden(UnreadEntity unreadEntity){
        if(unreadEntity.getSessionType() == DBConstant.SESSION_TYPE_GROUP){
            GroupEntity groupEntity= IMGroupManager.instance().findGroup(unreadEntity.getPeerId());
            if(groupEntity !=null && groupEntity.getStatus() == DBConstant.GROUP_STATUS_SHIELD){
                unreadEntity.setForbidden(true);
            }
        }
    }

    /**设定未读回话为屏蔽回话 仅限于群组 todo*/
    public void setForbidden(String sessionKey,boolean isFor){
       UnreadEntity unreadEntity =  unreadMsgMap.get(sessionKey);
       if(unreadEntity !=null){
           unreadEntity.setForbidden(isFor);
       }
    }

	public void add(MessageEntity msg) {
		 
        //更新session list中的msg信息
        //更新未读消息计数
        if(msg == null){
            logger.d("unread#unreadMgr#add msg is null!");
            return;
        }
        // isFirst场景:出现一条未读消息，出现小红点，需要触发 [免打扰的情况下]
        boolean isFirst = false;
		logger.d("unread#unreadMgr#add unread msg:%s", msg);
        UnreadEntity unreadEntity;
        int loginId = IMLoginManager.instance().getLoginId();
        String sessionKey = msg.getSessionKey();
        boolean isSend = msg.isSend(loginId);
         
        //msgid  == 0  同意好友的提示消息
        if(msg.getMsgId() == 0){  //guanweile
        	isSend = false;
        }
        if(isSend){
            IMNotificationManager.instance().cancelSessionNotifications(sessionKey);
            return;
        }       

        if(unreadMsgMap.containsKey(sessionKey)){
            unreadEntity = unreadMsgMap.get(sessionKey);
            // 判断最后一条msgId是否相同
            if(unreadEntity.getLaststMsgId() == msg.getMsgId()){
                return;
            }
            unreadEntity.setUnReadCnt(unreadEntity.getUnReadCnt()+1);
        }else{
            isFirst = true;
            unreadEntity = new UnreadEntity();
            unreadEntity.setUnReadCnt(1);
            unreadEntity.setPeerId(msg.getPeerId(isSend));
            unreadEntity.setSessionType(msg.getSessionType());
            unreadEntity.buildSessionKey();
        }
             
        unreadEntity.setLatestMsgData(msg.getMessageDisplay());
        unreadEntity.setLaststMsgId(msg.getMsgId());
        addIsForbidden(unreadEntity);

        /**放入manager 状态中*/
        unreadMsgMap.put(unreadEntity.getSessionKey(),unreadEntity);

        /**没有被屏蔽才会发送广播*/
        if(!unreadEntity.isForbidden() || isFirst) {
            UnreadEvent unreadEvent = new UnreadEvent();
            unreadEvent.event = UnreadEvent.Event.UNREAD_MSG_RECEIVED;
            unreadEvent.entity = unreadEntity;
            triggerEvent(unreadEvent);
        }
	}
 
    public void ackReadMsg(MessageEntity entity){
        logger.d("chat#ackReadMsg -> msg:%s", entity);
        int loginId = loginManager.getLoginId();
        IMBaseDefine.SessionType sessionType = Java2ProtoBuf.getProtoSessionType(entity.getSessionType());
        IMMessage.IMMsgDataReadAck readAck = IMMessage.IMMsgDataReadAck.newBuilder()
                .setMsgId(entity.getMsgId())
                .setSessionId(entity.getPeerId(false))
                .setSessionType(sessionType)
                .setUserId(loginId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_READ_ACK_VALUE;
        imSocketManager.sendRequest(readAck,sid,cid);
        
    }

    public void ackReadMsg(UnreadEntity unreadEntity){
        logger.d("chat#ackReadMsg -> msg:%s", unreadEntity);
        int loginId = loginManager.getLoginId();
        IMBaseDefine.SessionType sessionType = Java2ProtoBuf.getProtoSessionType(unreadEntity.getSessionType());
        IMMessage.IMMsgDataReadAck readAck = IMMessage.IMMsgDataReadAck.newBuilder()
                .setMsgId(unreadEntity.getLaststMsgId())
                .setSessionId(unreadEntity.getPeerId())
                .setSessionType(sessionType)
                .setUserId(loginId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_READ_ACK_VALUE;
        imSocketManager.sendRequest(readAck,sid,cid);
    }


    /**
     * 服务端主动发送已读通知
     * @param readNotify
     */
    public void onNotifyRead(IMMessage.IMMsgDataReadNotify readNotify){
        logger.d("chat#onNotifyRead");
        //发送此信令的用户id
        int trigerId = readNotify.getUserId();
        int loginId = IMLoginManager.instance().getLoginId();
        if(trigerId != loginId){
            logger.i("onNotifyRead# trigerId:%s,loginId:%s not Equal",trigerId,loginId);
            return ;
        }
        //现在的逻辑是msgId之后的 全部都是已读的
        // 不做复杂判断了，简单处理
        int msgId = readNotify.getMsgId();
        int peerId = readNotify.getSessionId();
        int sessionType = ProtoBuf2JavaBean.getJavaSessionType(readNotify.getSessionType());
        String sessionKey = EntityChangeEngine.getSessionKey(peerId,sessionType);

        // 通知栏也要去除掉
        NotificationManager notifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifyMgr == null) {
            return;
        }
        int notificationId = IMNotificationManager.instance().getSessionNotificationId(sessionKey);
        notifyMgr.cancel(notificationId);

        UnreadEntity unreadSession =  findUnread(sessionKey);
        if(unreadSession!=null && unreadSession.getLaststMsgId() <= msgId){
            // 清空会话session
            logger.d("chat#onNotifyRead# unreadSession onLoginOut");
            readUnreadSession(sessionKey);
        }
    }

    /**
     * 备注: 先获取最后一条消息
     * 1. 清除回话内的未读计数
     * 2. 发送最后一条msgId的已读确认
     * @param sessionKey
     */
    public void  readUnreadSession(String sessionKey){
        logger.d("unread#readUnreadSession# sessionKey:%s", sessionKey);
         if(unreadMsgMap.containsKey(sessionKey)){
             UnreadEntity entity = unreadMsgMap.remove(sessionKey);
             ackReadMsg(entity);
             triggerEvent(new UnreadEvent(UnreadEvent.Event.SESSION_READED_UNREAD_MSG));
         }
    }

    
    
    public void  readUnreadSessionAll(){ 
    	
//    	for(int i = 0;i<unreadMsgMap.size();i++){
//    		  UnreadEntity entity = unreadMsgMap.get(i);
//              ackReadMsg(entity);
//              unreadMsgMap.remove(entity.getSessionKey());
//    	}
    	
    	 unreadMsgMap.clear();
//         if(unreadMsgMap.containsKey(sessionKey)){
//             UnreadEntity entity = unreadMsgMap.remove(sessionKey);
//             ackReadMsg(entity);
//             triggerEvent(new UnreadEvent(UnreadEvent.Event.SESSION_READED_UNREAD_MSG));
//         }
    }
    

    public UnreadEntity findUnread(String sessionKey){
        logger.d("unread#findUnread# buddyId:%s", sessionKey);
        if(TextUtils.isEmpty(sessionKey) || unreadMsgMap.size()<=0){
            logger.i("unread#findUnread# no unread info");
            return null;
        }
        if(unreadMsgMap.containsKey(sessionKey)){
            return unreadMsgMap.get(sessionKey);
        }
        return null;
    }
    
   

    /**----------------实体set/get-------------------------------*/
    public ConcurrentHashMap<String, UnreadEntity> getUnreadMsgMap() {
        return unreadMsgMap;
    }
 
     
    public int getTotalUnreadCount() {
        int count = 0;
        for(UnreadEntity entity:unreadMsgMap.values()){
            if(!entity.isForbidden()){
                count  = count +  entity.getUnReadCnt();
            }
        }
        return count;
    }
    
    
    public int getUnreadFriendsUserId() {
        int userId = 0;
        for(ReqFriendsEntity entity:reqUnFriendsMap.values()){
            if(entity!=null){
            	userId =  entity.getUserId();
            	break;
            }
        }
        return userId;
    }
    
    public int getTotalReqUnreadCount() {
        int count = 0;
        for(ReqFriendsEntity entity:reqUnFriendsMap.values()){
            if(entity.getTableReq()==0){
                count  = count +  1;
            }
        }
        return count;
    }
     
    public int getTotalReqMessageCount() {
        int count = 0;
        for(ReqFriendsEntity entity:reqUnFriendsMap.values()){
            if(entity.getMessageReq()==0){
                count  = count +  1;
            }
        }
        return count;
    }
    
    
    public void  updateReqUnreadCount() { 
        for(ReqFriendsEntity entity:reqUnFriendsMap.values()){
            if(entity.getTableReq()==0){
            	entity.setTableReq(1);
            	updateUnReqFriends(entity);
            }
        } 
        triggerEvent(UserInfoEvent.USER_INFO_REQ_UPDATE);
    }
    
    
    public void  updateReqMessageUnreadCount() { 
        for(ReqFriendsEntity entity:reqUnFriendsMap.values()){
            if(entity.getMessageReq()==0){
            	entity.setMessageReq(1);
            	updateUnReqFriends(entity);
            }
        } 
        
        triggerEvent(UserInfoEvent.USER_INFO_REQ_UPDATE);
    }
    
 
    public ConcurrentHashMap<String, UnreadEntity> getUnreadReqMsgMap() {
        return unreaReqdMsgMap;
    } 

    public List<UnreadEntity> getUnreadReqList() {
        return unreaReqdList;
    } 

    
    
    /**
     * 未读消息的Num
     * @return
     */
    public int getTotalUnreadReqCount() {
        int count = 0;
        for(UnreadEntity entity:unreaReqdMsgMap.values()){
            if(!entity.isForbidden()){
                count  = count +  entity.getUnReadCnt();
            }
        }
        return count;
    }  
     
     
    public boolean isUnreadListReady() {
        return unreadListReady;
    }
    
}

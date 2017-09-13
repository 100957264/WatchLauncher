package com.fise.xiaoyu.imservice.entity;

import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.MessageConstant;
import com.fise.xiaoyu.imservice.support.SequenceNumberMaker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * 通知消息
 */
public class NoticeMessage extends MessageEntity implements Serializable {

     public NoticeMessage(){
         msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
     }

     private NoticeMessage(MessageEntity entity){
         /**父类的id*/
         id =  entity.getId();
         msgId  = entity.getMsgId();
         fromId = entity.getFromId();
         toId   = entity.getToId();
         sessionKey = entity.getSessionKey();
         content=entity.getContent();
         msgType=entity.getMsgType();
         displayType=entity.getDisplayType();
         status = entity.getStatus();
         created = entity.getCreated();
         updated = entity.getUpdated();
     }

     public static NoticeMessage parseFromNet(MessageEntity entity){
         NoticeMessage noticeMessage = new NoticeMessage(entity);
         noticeMessage.setStatus(MessageConstant.MSG_SUCCESS);
         
         String originContent = entity.getContent();
 		JSONObject extraContent;
 		try {
 			extraContent = new JSONObject(originContent);
 			noticeMessage.setDisplayType(extraContent.getInt("type"));

 		} catch (JSONException e) {
 			e.printStackTrace();
 		}

 		
        // noticeMessage.setDisplayType(DBConstant.SHOW_TYPE_NOTICE);
         return noticeMessage;
     }

    public static NoticeMessage parseFromDB(MessageEntity entity){
        if(entity.getDisplayType()!=DBConstant.CHANGE_NOT_FRIEND
        		||entity.getDisplayType()!=DBConstant.SHOW_TYPE_NOTICE_BLACK){
            throw new RuntimeException("#TextMessage# parseFromDB,not SHOW_TYPE_NOTICE");
        }
        NoticeMessage noticeMessage = new NoticeMessage(entity);
        return noticeMessage;
    }
    
    public static NoticeMessage parseFromNoteDB(MessageEntity entity){
      
        NoticeMessage noticeMessage = new NoticeMessage(entity);
        return noticeMessage;
    }

    public static NoticeMessage buildForSend(String content,UserEntity fromUser,PeerEntity peerEntity){
        NoticeMessage noticeMessage = new NoticeMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        noticeMessage.setFromId(fromUser.getPeerId());
        noticeMessage.setToId(peerEntity.getPeerId());
        noticeMessage.setUpdated(nowTime);
        noticeMessage.setCreated(nowTime);
        noticeMessage.setDisplayType(DBConstant.CHANGE_NOT_FRIEND);
        noticeMessage.setGIfEmo(true);
        int peerType = peerEntity.getType();
        int msgType =  DBConstant.MSG_TYPE_SINGLE_NOTICE;
//        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_TEXT
//                : DBConstant.MSG_TYPE_SINGLE_TEXT;
        noticeMessage.setMsgType(msgType);
        noticeMessage.setStatus(MessageConstant.MSG_SENDING);
        // 内容的设定
        noticeMessage.setContent(content);
        noticeMessage.buildSessionKey(true);
        noticeMessage.setMessageTime(""+nowTime);
        
        return noticeMessage;
    }



    public static NoticeMessage buildGroupForSend(String content,UserEntity fromUser,PeerEntity peerEntity){
        NoticeMessage noticeMessage = new NoticeMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        noticeMessage.setFromId(fromUser.getPeerId());
        noticeMessage.setToId(peerEntity.getPeerId());
        noticeMessage.setUpdated(nowTime);
        noticeMessage.setCreated(nowTime);
        noticeMessage.setDisplayType(DBConstant.CHANGE_NOT_FRIEND);
        noticeMessage.setGIfEmo(true);
        int peerType = peerEntity.getType();
        int msgType =  DBConstant.MSG_TYPE_SINGLE_NOTICE;
        noticeMessage.setMsgType(msgType);
        noticeMessage.setStatus(MessageConstant.MSG_SENDING);
        // 内容的设定
        noticeMessage.setContent(content);
        noticeMessage.buildNoticeSessionKey(true);
        noticeMessage.setMessageTime(""+nowTime);

        return noticeMessage;
    }


    /**
     * Not-null value.
     * DB的时候需要
     */
    @Override
    public String getContent() {
        return content;
    }

    @Override
    public byte[] getSendContent() {
        try {
            /** 加密*/
            String sendContent =new String(Security.getInstance().EncryptMsg(content));
            return sendContent.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

package com.fise.xw.imservice.entity;

import com.fise.xw.DB.entity.PeerEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.config.MessageConstant;
import com.fise.xw.imservice.support.SequenceNumberMaker;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 */
public class ActionMessage extends MessageEntity implements Serializable {

     public ActionMessage(){
         msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
     }
     

     private ActionMessage(MessageEntity entity){
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

     public static ActionMessage parseFromNet(MessageEntity entity){
         ActionMessage textMessage = new ActionMessage(entity);
         textMessage.setStatus(MessageConstant.MSG_SUCCESS);
         textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
         return textMessage;
     }

    public static ActionMessage parseFromDB(MessageEntity entity){
        if(entity.getDisplayType()!=DBConstant.SHOW_ORIGIN_TEXT_TYPE){
            throw new RuntimeException("#TextMessage# parseFromDB,not SHOW_ORIGIN_TEXT_TYPE");
        }
        ActionMessage textMessage = new ActionMessage(entity);
        return textMessage;
    } 

    
    public static ActionMessage buildForSend(String content,UserEntity fromUser,int toId,int type){
        ActionMessage textMessage = new ActionMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        textMessage.setFromId(fromUser.getPeerId());
        textMessage.setToId(toId);
        textMessage.setUpdated(nowTime);
        textMessage.setCreated(nowTime); 
        textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
        textMessage.setGIfEmo(false); 
        int msgType =  type;
        textMessage.setMsgType(msgType);
        textMessage.setStatus(MessageConstant.MSG_SENDING);
        // 内容的设定
        textMessage.setContent(content);
        textMessage.buildSessionKey(true);
        return textMessage;
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
           // String sendContent =new String(com.fise.xw.Security.getInstance().EncryptMsg(content)); 
            return content.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

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
 * 在线视频消息
 */
public class OnLineVedioMessage extends MessageEntity implements Serializable {

    private String pushUrl;
    private String pullUrl;
    private int vedioStatus;
    private String time;

    public OnLineVedioMessage() {
        msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
    }

    private OnLineVedioMessage(MessageEntity entity) {
        /**父类的id*/
        id = entity.getId();
        msgId = entity.getMsgId();
        fromId = entity.getFromId();
        toId = entity.getToId();
        sessionKey = entity.getSessionKey();
        content = entity.getContent();
        msgType = entity.getMsgType();
        displayType = entity.getDisplayType();
        status = entity.getStatus();
        created = entity.getCreated();
        updated = entity.getUpdated();

    }

    public static OnLineVedioMessage parseFromNet(MessageEntity entity) {
        OnLineVedioMessage vedioMessage = new OnLineVedioMessage(entity);
        vedioMessage.setStatus(MessageConstant.MSG_SUCCESS);
        vedioMessage.setDisplayType(DBConstant.SHOW_TYPE_ONLINE_VIDEO);

        String content = entity.getContent();
        String[] sourceStrArray = content.split(";");
        for (int i = 0; i < sourceStrArray.length; i++) {
            if (i == 0) {
                vedioMessage.setPushUrl(sourceStrArray[i]);
            } else if (i == 1) {
                vedioMessage.setPullUrl(sourceStrArray[i]);
            }
        }
        return vedioMessage;
    }


    public static OnLineVedioMessage parseFromDB(MessageEntity entity) {
        if (entity.getDisplayType() != DBConstant.SHOW_TYPE_ONLINE_VIDEO) {
            throw new RuntimeException("#OnLineVedioMessage# parseFromDB,not SHOW_TYPE_ONLINE_VIDEO");
        }
        OnLineVedioMessage vedioReqMessage = new OnLineVedioMessage(entity);
        return vedioReqMessage;
    }

    public static OnLineVedioMessage parseFromNoteDB(MessageEntity entity) {

        OnLineVedioMessage onlineMessage = new OnLineVedioMessage(entity);
        if (onlineMessage.getMsgType() == DBConstant.MSG_TYPE_VIDEO_CLOSE) {
            try {
                JSONObject jsonObject = new JSONObject(entity.getContent());

                if (!jsonObject.isNull("status")) {
                    onlineMessage.setVedioStatus(jsonObject.getInt("status"));
                }

                if (!jsonObject.isNull("time")) {
                    onlineMessage.setTime(jsonObject.getString("time"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return onlineMessage;
    }

    public static OnLineVedioMessage buildForSend(String content, UserEntity fromUser, PeerEntity peerEntity, int msgType) {
        OnLineVedioMessage vedioReqMessage = new OnLineVedioMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        vedioReqMessage.setFromId(fromUser.getPeerId());
        vedioReqMessage.setToId(peerEntity.getPeerId());
        vedioReqMessage.setUpdated(nowTime);
        vedioReqMessage.setCreated(nowTime);
        vedioReqMessage.setDisplayType(DBConstant.SHOW_TYPE_ONLINE_VIDEO);
        vedioReqMessage.setGIfEmo(true);
        int peerType = peerEntity.getType();
        // int msgType = peerType ==  DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_TEXT
        //         : DBConstant.MSG_TYPE_SINGLE_TEXT;
        //  int msgType = DBConstant.MSG_TYPE_VIDEO_CALL;
        vedioReqMessage.setMsgType(msgType);
        vedioReqMessage.setStatus(MessageConstant.MSG_SUCCESS);
        vedioReqMessage.setMessageTime("");
        // 内容的设定
        vedioReqMessage.setContent(content);
        vedioReqMessage.buildSessionKey(true);
        vedioReqMessage.setMessageTime("" + nowTime);


        return vedioReqMessage;
    }

    public static OnLineVedioMessage buildForSend(String content, int fromId, int toId, int msgType) {
        OnLineVedioMessage vedioReqMessage = new OnLineVedioMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        vedioReqMessage.setFromId(fromId);
        vedioReqMessage.setToId(toId);
        vedioReqMessage.setUpdated(nowTime);
        vedioReqMessage.setCreated(nowTime);
        vedioReqMessage.setDisplayType(DBConstant.SHOW_TYPE_ONLINE_VIDEO);
        vedioReqMessage.setGIfEmo(true);
        vedioReqMessage.setMsgType(msgType);
        vedioReqMessage.setStatus(MessageConstant.MSG_SUCCESS);
        // 内容的设定
        vedioReqMessage.setContent(content);
        vedioReqMessage.buildSessionKey(true);
        vedioReqMessage.setMessageTime("" + nowTime);

        return vedioReqMessage;
    }


    public static OnLineVedioMessage buildForSend(String time, int status, int fromId, int toId, int msgType) {
        OnLineVedioMessage vedioReqMessage = new OnLineVedioMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        vedioReqMessage.setFromId(fromId);
        vedioReqMessage.setToId(toId);
        vedioReqMessage.setUpdated(nowTime);
        vedioReqMessage.setCreated(nowTime);
        vedioReqMessage.setDisplayType(DBConstant.SHOW_TYPE_ONLINE_VIDEO);
        vedioReqMessage.setGIfEmo(true);
        vedioReqMessage.setMsgType(msgType);
        vedioReqMessage.setStatus(MessageConstant.MSG_SUCCESS);
        // 内容的设定

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("time", time);
            jsonObject.put("status", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        vedioReqMessage.setContent(jsonObject.toString());
        vedioReqMessage.buildSessionKey(true);
        vedioReqMessage.setMessageTime("" + nowTime);

        return vedioReqMessage;
    }


    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
    }

    public String getPushUrl() {
        return this.pushUrl;
    }

    public void setPullUrl(String pullUrl) {
        this.pullUrl = pullUrl;
    }

    public String getPullUrl() {
        return this.pullUrl;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return this.time;
    }


    public void setVedioStatus(int vedioStatus) {
        this.vedioStatus = vedioStatus;
    }

    public int getVedioStatus() {
        return this.vedioStatus;
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
            String sendContent = new String(Security.getInstance().EncryptMsg(content));
            return sendContent.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

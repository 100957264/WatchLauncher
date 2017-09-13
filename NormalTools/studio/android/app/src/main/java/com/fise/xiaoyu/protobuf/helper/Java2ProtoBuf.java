package com.fise.xiaoyu.protobuf.helper;

import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.protobuf.IMBaseDefine;

/**
 * Java2ProtoBuf
 */
public class Java2ProtoBuf {
    /**----enum 转化接口--*/
    public static IMBaseDefine.MsgType getProtoMsgType(int msgType){
        switch (msgType){
            case DBConstant.MSG_TYPE_GROUP_TEXT:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_TEXT;
            case DBConstant.MSG_TYPE_GROUP_AUDIO:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_AUDIO;
            case DBConstant.MSG_TYPE_SINGLE_AUDIO:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_AUDIO;
            case DBConstant.MSG_TYPE_SINGLE_TEXT:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_TEXT;
            case DBConstant.MSG_TYPE_GROUP_DEV_MESSAGE:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_EVENT_MSG;
            case DBConstant.MSG_TYPE_SINGLE_DEV_MESSAGE:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_EVENT_MSG;


            case DBConstant.MSG_TYPE_VIDEO_CALL:
                return IMBaseDefine.MsgType.MSG_TYPE_VIDEO_CALL;

            case DBConstant.MSG_TYPE_VIDEO_ANSWER:
                return IMBaseDefine.MsgType.MSG_TYPE_VIDEO_ANSWER;
            case DBConstant.MSG_TYPE_VIDEO_CLOSE:
                return IMBaseDefine.MsgType.MSG_TYPE_VIDEO_CLOSE;

            case DBConstant.MSG_TYPE_SINGLE_AUTH_IMAGE:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_AUTH_IMAGE;
            case DBConstant.MSG_TYPE_SINGLE_AUTH_SOUND:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_AUTH_SOUND;
                
            case DBConstant.MSG_TYPE_SINGLE_BUSSINESS_CARD:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_BUSSINESS_CARD;
            case DBConstant.MSG_TYPE_GROUP_BUSSINESS_CARD:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_BUSSINESS_CARD;
                
            case DBConstant.MSG_TYPE_SINGLE_VIDIO:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_VIDIO;
            case DBConstant.MSG_TYPE_GROUP_VIDIO:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_VIDIO;
                
                
            case DBConstant.MSG_TYPE_GROUP_AUTH_IMAGE:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_AUTH_IMAGE;
            case DBConstant.MSG_TYPE_GROUP_AUTH_SOUND:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_AUTH_SOUND;
                
                
            case DBConstant.MSG_TYPE_SINGLE_IMAGE:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_IMAGE;
            case DBConstant.MSG_TYPE_GROUP_IMAGE:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_IMAGE;
                
           // case DBConstant.MSG_TYPE_MAKE_FRIEND:
           //     return IMBaseDefine.MsgType.MSG_TYPE_MAKE_FRIEND;
           // case DBConstant.MSG_TYPE_CONFIRM_FRIEND:
            //    return IMBaseDefine.MsgType.MSG_TYPE_CONFIRM_FRIEND;
           // case DBConstant.MSG_TYPE_REFUSE_FRIEND  :
            //    return IMBaseDefine.MsgType.MSG_TYPE_REFUSE_FRIEND;
                
            case DBConstant.MSG_TYPE_SINGLE_LOCATION:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_LOCATION;
            case DBConstant.MSG_TYPE_GROUP_LOCATION:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_LOCATION;
           // case DBConstant.MSG_TYPE_DEVICE_COMMAND  :
           //     return IMBaseDefine.MsgType.MSG_TYPE_DEVICE_COMMAND;
            case DBConstant.MSG_TYPE_SINGLE_NOTICE  :
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_NOTICE; 
            default:
                throw new IllegalArgumentException("msgType is illegal,cause by #getProtoMsgType#" +msgType);
        }
    }

 
    public static IMBaseDefine.SessionType getProtoSessionType(int sessionType){
        switch (sessionType){
            case DBConstant.SESSION_TYPE_SINGLE:
                return IMBaseDefine.SessionType.SESSION_TYPE_SINGLE;
            case DBConstant.SESSION_TYPE_GROUP:
                return IMBaseDefine.SessionType.SESSION_TYPE_GROUP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" +sessionType);
        }
    }
}

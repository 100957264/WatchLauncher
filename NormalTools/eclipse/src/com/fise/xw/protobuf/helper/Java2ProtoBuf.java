package com.fise.xw.protobuf.helper;

import com.fise.xw.config.DBConstant;
import com.fise.xw.protobuf.IMBaseDefine;

/**
 * @author : yingmu on 15-1-6.
 * @email : yingmu@mogujie.com.
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

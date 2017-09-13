package com.fise.marechat.bean.msg;

import com.fise.marechat.parser.MsgParser;

/**
 * Created by fanyang on 2017/8/4.
 */

public class BaseMsg {

    public String msgType;

    /**
     * 解析收到的信息
     */
    public void recvMsg(TcpMsg msg) {
        if (msg.isNeedReply()){
//            composeSendingMsg
        }
        recvDone(msg);
    }

    public void recvDone(TcpMsg msg) {
    }

    /**
     * 发送完整的消息
     */
    public void sendingMsg(String msgType,String content) {
        MsgParser.instance().composedTypeContent(msgType, content);
        sendDone(msgType, content);
    }

    public void sendDone(String msgType, String content) {
    }

}

package com.fise.xiaoyu.imservice.event;

/**
 *  socket 事件
 */
public enum SocketEvent {
    /**登录之前的动作*/
    NONE,
    REQING_MSG_SERVER_ADDRS,
    REQ_MSG_SERVER_ADDRS_FAILED,
    REQ_MSG_SERVER_ADDRS_SUCCESS,

    REQ_MSG_SERVER_SMS_FAILED,
    REQ_MSG_SERVER_SMS_SUCCESS,
    
    /**请求登录的过程*/
    CONNECTING_MSG_SERVER,
    CONNECT_MSG_SERVER_SUCCESS,
    CONNECT_MSG_SERVER_FAILED,
    MSG_SERVER_DISCONNECTED    //channel disconnect 会触发，再应用开启内，要重连【可能是服务端、客户端断掉】
}

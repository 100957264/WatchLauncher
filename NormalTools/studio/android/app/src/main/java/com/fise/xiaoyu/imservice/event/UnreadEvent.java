package com.fise.xiaoyu.imservice.event;

import com.fise.xiaoyu.imservice.entity.UnreadEntity;

/**
 * 未读消息事件
 */
public class UnreadEvent {

    public UnreadEntity entity;
    public Event event;

    public UnreadEvent(){}
    public UnreadEvent(Event e){
        this.event = e;
    }

    public enum Event {
        UNREAD_MSG_LIST_OK,
        UNREAD_MSG_RECEIVED,

        SESSION_READED_UNREAD_MSG
    }
}

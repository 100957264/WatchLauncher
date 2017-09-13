package com.fise.xiaoyu.imservice.event;

import com.fise.xiaoyu.DB.entity.WeiEntity;
import com.fise.xiaoyu.imservice.entity.UnreadEntity;

/**
 * 请求事件
 */
public class ReqEvent {

    public WeiEntity entity;
    public Event event;

    public ReqEvent(){}
    public ReqEvent(Event e){
        this.event = e;
    }

    public enum Event {
        REQ_FRIENDS_MESSAGE,
        REQ_WEI_MESSAGE 
    }
}

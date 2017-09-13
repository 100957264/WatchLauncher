package com.fise.xw.imservice.event;

import com.fise.xw.DB.entity.WeiEntity;
import com.fise.xw.imservice.entity.UnreadEntity;

/**
 * @author : yingmu on 15-1-6.
 * @email : yingmu@mogujie.com.
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

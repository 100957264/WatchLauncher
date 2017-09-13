package com.fise.xiaoyu.imservice.event;

import com.fise.xiaoyu.DB.entity.MessageEntity;

import java.util.List;

/**
 *
 * 异步刷新历史消息
 */
public class RefreshHistoryMsgEvent {
   public int pullTimes;
   public int lastMsgId;
   public int count;
   public List<MessageEntity> listMsg;
   public int peerId;
   public int peerType;
   public String sessionKey;

   public RefreshHistoryMsgEvent(){}

}

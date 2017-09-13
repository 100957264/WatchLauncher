package com.fise.xw.imservice.event;

/**
 * @author : yingmu on 14-12-30.
 * @email : yingmu@mogujie.com.
 */
public enum  SessionEvent {

   RECENT_SESSION_LIST_SUCCESS,
   RECENT_SESSION_LIST_FAILURE,

   //回话人列表更新
   RECENT_SESSION_LIST_UPDATE,
   RECENT_SESSION_GRPUP_LIST_UPDATE,
   SET_SESSION_TOP,
   SET_SESSION_NO_DISTURB,
   SET_REMOVE_MESSAGE_SUCCESS,
   SET_REMOVE_MESSAGE_FAIL,
   SET_SESSION_MUTE_TOP

}

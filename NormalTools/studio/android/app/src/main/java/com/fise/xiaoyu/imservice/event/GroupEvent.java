package com.fise.xiaoyu.imservice.event;


import com.fise.xiaoyu.DB.entity.GroupEntity;

import java.util.List;

/**
 * 群事件
 */
public class GroupEvent {

    private GroupEntity groupEntity;
    private Event event;

    /**很多的场景只是关心改变的类型以及change的Ids*/
    private int changeType;
    private List<Integer> changeList;

    public GroupEvent(Event event){
        this.event = event;
    }

    public GroupEvent(Event event,GroupEntity groupEntity){
        this.groupEntity = groupEntity;
        this.event = event;
    }

    public enum Event{
        NONE,

        GROUP_INFO_OK,
        GROUP_INFO_UPDATED,

        CHANGE_GROUP_MEMBER_SUCCESS,
        CHANGE_GROUP_MEMBER_FAIL,
        CHANGE_GROUP_MEMBER_TIMEOUT,
 
        CHANGE_GROUP_MODIFY_SUCCESS,
        CHANGE_GROUP_MODIFY_FAIL,
        CHANGE_GROUP_MODIFY_TIMEOUT,
        
        CREATE_GROUP_OK,
        CREATE_GROUP_FAIL,
        CREATE_GROUP_TIMEOUT,

        SHIELD_GROUP_OK,
        SHIELD_GROUP_TIMEOUT,
        SHIELD_GROUP_FAIL,
 
        CHANGE_GROUP_DELETE_SUCCESS,
        USER_GROUP_DELETE_SUCCESS,//主动退出群
        CHANGE_GROUP_DELETE_FAIL,
        CHANGE_GROUP_DELETE_TIMEOUT,
        CHANGE_GROUP_EXIT_SUCCESS,
        CHANGE_GROUP_NICK_SUCCESS,
        CHANGE_GROUP_NOTICE_SUCCESS

    }

    public int getChangeType() {
        return changeType;
    }

    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    public List<Integer> getChangeList() {
        return changeList;
    }

    public void setChangeList(List<Integer> changeList) {
        this.changeList = changeList;
    }

    public GroupEntity getGroupEntity() {
        return groupEntity;
    }
    public void setGroupEntity(GroupEntity groupEntity) {
        this.groupEntity = groupEntity;
    }

    public Event getEvent() {
        return event;
    }
    public void setEvent(Event event) {
        this.event = event;
    }
}

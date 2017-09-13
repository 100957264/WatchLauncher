package com.fise.xiaoyu.DB.entity;

import android.text.TextUtils;
import android.util.Log;

import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.entity.SearchElement;
import com.fise.xiaoyu.utils.pinyin.PinYin;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 *  群的基本信息
 *
 */
public class GroupEntity extends PeerEntity {

    private int groupType;
    private int creatorId;
    private int userCnt;
    /** Not-null value. */
    private String userList;
    private int version;
    private int status;
    private int save;
    private String Board; 
    private String boardTime;
    
//	private int muteNotification;
//	private int stickyOnTop;
 
    // KEEP FIELDS - put your custom fields here

    private PinYin.PinYinElement pinyinElement = new PinYin.PinYinElement();
    private SearchElement searchElement = new SearchElement();
    // KEEP FIELDS END

    public GroupEntity() {
    }

    public GroupEntity(Long id) {
        this.id = id;
    }

    public GroupEntity(Long id, int peerId, int groupType, String mainName, String avatar, int creatorId, int userCnt, String userList, int version, 
    		int status, int created, int updated,int save,String Board,String boardTime
    		) {
    	
//    	,int muteNotification
//		,int stickyOnTop
		
        this.id = id;
        this.peerId = peerId;
        this.groupType = groupType;
        this.mainName = mainName;
        this.avatar = avatar;
        this.creatorId = creatorId;
        this.userCnt = userCnt;
        this.userList = userList;
        this.version = version;
        this.status = status;
        this.created = created;
        this.updated = updated;
        this.save = save;
        
        this.Board = Board;
        this.boardTime = boardTime;
        
//        this.muteNotification = muteNotification;
//        this.stickyOnTop = stickyOnTop;
        
        
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    
    public int getSave() {
        return save;
    }

    public void setSave(int save) {
        this.save = save;
    }
     
    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    /** Not-null value. */
    public String getMainName() {
        return mainName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setMainName(String mainName) {
        this.mainName = mainName;
    }

    /** Not-null value. */
    public String getAvatar() {
        return  avatar;
    }
 
    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public int getUserCnt() {
        return userCnt;
    }

    public void setUserCnt(int userCnt) {
        this.userCnt = userCnt;
    }

    /** Not-null value. */
    public String getUserList() {
        return userList;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setUserList(String userList) {
        this.userList = userList;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }
    
     
    public String getBoard() {
        return Board;
    }

    public void setBoard(String Board) {
        this.Board = Board;
    }
    
    
    
    public String getBoardTime() {
        return boardTime;
    }

    public void setBoardTime(String boardTime) {
        this.boardTime = boardTime;
    }
    
    
    
    
//    public int getMuteNotification() {
//        return muteNotification;
//    }
//
//    public void setMuteNotification(int muteNotification) {
//        this.muteNotification = muteNotification;
//    }
//    
//    
//    
//    public int getStickyOnTop() {
//        return stickyOnTop;
//    }
//
//    public void setStickyOnTop(int stickyOnTop) {
//        this.stickyOnTop = stickyOnTop;
//    }
    
     

    // KEEP METHODS - put your custom methods here
 
    @Override
    public int getType() {
        return DBConstant.SESSION_TYPE_GROUP;
    }

    /**
     * yingmu
     * 获取群组成员的list
     * -- userList 前后去空格，按照逗号区分， 不检测空的成员(非法)
     */
    public Set<Integer> getlistGroupMemberIds(){
        if(TextUtils.isEmpty(userList)){
          return  Collections.emptySet();
        }
        String[] arrayUserIds =  userList.trim().split(",");
        if(arrayUserIds.length <=0){
            return Collections.emptySet();
        }
        /**zhe'g*/
        Set<Integer> result = new LinkedHashSet<Integer>(); //TreeSet

        for(int index=0;index < arrayUserIds.length;index++){
            int userId =  Integer.parseInt(arrayUserIds[index]);
            result.add(userId);
        }
        return result;
    }
    //todo 入参变为 set【自动去重】
    // 每次都要转换 性能不是太好，todo
    public void setlistGroupMemberIds(List<Integer> memberList){
        String userList = TextUtils.join(",",memberList);
        setUserList(userList);


    }

    public PinYin.PinYinElement getPinyinElement() {
        return pinyinElement;
    }

    public void setPinyinElement(PinYin.PinYinElement pinyinElement) {
        this.pinyinElement = pinyinElement;
    }

    public SearchElement getSearchElement() {
        return searchElement;
    }

    public void setSearchElement(SearchElement searchElement) {
        this.searchElement = searchElement;
    }
    // KEEP METHODS END
}

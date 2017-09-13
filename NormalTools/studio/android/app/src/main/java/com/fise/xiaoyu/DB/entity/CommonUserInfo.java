package com.fise.xiaoyu.DB.entity;

import android.text.TextUtils;

import com.fise.xiaoyu.imservice.entity.SearchElement;
import com.fise.xiaoyu.utils.pinyin.PinYin;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by lenovo on 2017/5/24.  用于数据转发时对象使用
 */

public class CommonUserInfo {
    private int userInfoID;
    private int userType;
    private String avatarUrl;
    private String userName;
    private String sessionKey;
    private String userList;
    private PinYin.PinYinElement pinyinElement = new PinYin.PinYinElement();
    private SearchElement searchElement = new SearchElement();

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }


    public PinYin.PinYinElement getPinyinElement() {
        return pinyinElement;
    }

    public SearchElement getSearchElement() {
        return searchElement;
    }


    public int getUserInfoID() {
        return userInfoID;
    }

    public void setUserInfoID(int userInfoID) {
        this.userInfoID = userInfoID;
    }

    /**
     * yingmu
     * 获取群组成员的list
     * -- userList 前后去空格，按照逗号区分， 不检测空的成员(非法)
     */
    public Set<Integer> getlistGroupMemberIds() {
        if (TextUtils.isEmpty(userList)) {
            return Collections.emptySet();
        }
        String[] arrayUserIds = userList.trim().split(",");
        if (arrayUserIds.length <= 0) {
            return Collections.emptySet();
        }
        /**zhe'g*/
        Set<Integer> result = new TreeSet<Integer>();
        for (int index = 0; index < arrayUserIds.length; index++) {
            int userId = Integer.parseInt(arrayUserIds[index]);
            result.add(userId);
        }
        return result;
    }

    //todo 入参变为 set【自动去重】
    // 每次都要转换 性能不是太好，todo
    public void setlistGroupMemberIds(List<Integer> memberList) {
        String userList = TextUtils.join(",", memberList);
        setUserList(userList);

    }

    /**
     * Not-null value; ensure this value is available before it is saved to the database.
     */
    public void setUserList(String userList) {
        this.userList = userList;
    }

    public String getSectionName() {
        if (TextUtils.isEmpty(pinyinElement.pinyin)) {
            return "";
        }
        return pinyinElement.pinyin.substring(0, 1);
    }
}

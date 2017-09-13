package com.fise.xiaoyu.DB.entity;

import android.text.TextUtils;

import com.fise.xiaoyu.imservice.entity.SearchElement;
import com.fise.xiaoyu.utils.pinyin.PinYin;

/**
 * Created by lenovo on 2017/6/1. 手机联系人号码对象封装，便于排序
 */

public class PhoneNumberItemInfo {

    private  Long contactId = 0L;
    private String contactName;
    private String contactPhoneNumber;

    private PinYin.PinYinElement pinyinElement = new PinYin.PinYinElement();
    private SearchElement searchElement = new SearchElement();

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }


    public PinYin.PinYinElement getPinyinElement() {
        return pinyinElement;
    }

    public String getPinName() {
        if (TextUtils.isEmpty(pinyinElement.pinyin)) {
            return "";
        }
        return pinyinElement.pinyin.substring(0, 1);
    }

}

package com.fise.marechat.bean.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author mare
 * @Description:
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/10
 * @time 17:56
 */
@Entity
public class PhoneContractor extends CenterSettingBase {

    @Id(autoincrement = true)
    private long id;
    public String imei;//主键
    private long phone_contactor_id;//PhoneBook的主键作为外键
    private String phone_contactor_name;
    private String phone_contactor_num;//电话号码

    private String phone_contactor_nick;//昵称

    @Generated(hash = 1798766442)
    public PhoneContractor(long id, String imei, long phone_contactor_id,
            String phone_contactor_name, String phone_contactor_num,
            String phone_contactor_nick) {
        this.id = id;
        this.imei = imei;
        this.phone_contactor_id = phone_contactor_id;
        this.phone_contactor_name = phone_contactor_name;
        this.phone_contactor_num = phone_contactor_num;
        this.phone_contactor_nick = phone_contactor_nick;
    }

    @Generated(hash = 113019231)
    public PhoneContractor() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPhone_contactor_id() {
        return this.phone_contactor_id;
    }

    public void setPhone_contactor_id(long phone_contactor_id) {
        this.phone_contactor_id = phone_contactor_id;
    }

    public String getPhone_contactor_name() {
        return this.phone_contactor_name;
    }

    public void setPhone_contactor_name(String phone_contactor_name) {
        this.phone_contactor_name = phone_contactor_name;
    }

    public String getPhone_contactor_num() {
        return this.phone_contactor_num;
    }

    public void setPhone_contactor_num(String phone_contactor_num) {
        this.phone_contactor_num = phone_contactor_num;
    }

    public String getPhone_contactor_nick() {
        return this.phone_contactor_nick;
    }

    public void setPhone_contactor_nick(String phone_contactor_nick) {
        this.phone_contactor_nick = phone_contactor_nick;
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

}

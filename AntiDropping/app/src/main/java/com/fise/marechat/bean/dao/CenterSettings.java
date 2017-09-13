package com.fise.marechat.bean.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author mare
 * @Description:
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/10
 * @time 16:47
 */
@Entity
public class CenterSettings  extends CenterSettingBase{
    @Id(autoincrement = false)
    private long id;

    public String imei;//主键

    private String centerPhoneNum;

    private String upload_interval;//上传时间间隔

    private String centerPwd;//中心密码

    //=========2个SOS号码同时设置=====
    private String sosPhone;

    private String sosPhone2;
    //=========2个SOS号码同时设置=====

    //=========IP端口设置=====
//    [CS*YYYYYYYYYY*LEN*IP,IP或域名,端口]
    @Property(nameInDb = "ip")
    private String center_ip;

    @Property(nameInDb = "port")
    private String center_port;
    //=========IP端口设置=====

    @Generated(hash = 225306235)
    public CenterSettings(long id, String imei, String centerPhoneNum,
            String upload_interval, String centerPwd, String sosPhone,
            String sosPhone2, String center_ip, String center_port) {
        this.id = id;
        this.imei = imei;
        this.centerPhoneNum = centerPhoneNum;
        this.upload_interval = upload_interval;
        this.centerPwd = centerPwd;
        this.sosPhone = sosPhone;
        this.sosPhone2 = sosPhone2;
        this.center_ip = center_ip;
        this.center_port = center_port;
    }

    @Generated(hash = 461948431)
    public CenterSettings() {
    }

    public CenterSettings(String imei){
        this.imei = imei;
    }

     public String getCenterPhoneNum() {
        return this.centerPhoneNum;
    }

    public void setCenterPhoneNum(String centerPhoneNum) {
        this.centerPhoneNum = centerPhoneNum;
    }

    public String getUpload_interval() {
        return this.upload_interval;
    }

    public void setUpload_interval(String upload_interval) {
        this.upload_interval = upload_interval;
    }

    public String getCenterPwd() {
        return this.centerPwd;
    }

    public void setCenterPwd(String centerPwd) {
        this.centerPwd = centerPwd;
    }

    public String getSosPhone() {
        return this.sosPhone;
    }

    public void setSosPhone(String sosPhone) {
        this.sosPhone = sosPhone;
    }

    public String getSosPhone2() {
        return this.sosPhone2;
    }

    public void setSosPhone2(String sosPhone2) {
        this.sosPhone2 = sosPhone2;
    }

    public String getCenter_ip() {
        return this.center_ip;
    }

    public void setCenter_ip(String center_ip) {
        this.center_ip = center_ip;
    }

    public String getCenter_port() {
        return this.center_port;
    }

    public void setCenter_port(String center_port) {
        this.center_port = center_port;
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

}

package com.fise.marechat.bean.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author mare
 * @Description:报警功能开关
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/10
 * @time 17:15
 */
@Entity
public class CenterAlert extends CenterSettingBase{
    @Id
    private long id;

    public String imei;//主键

    //SOS短信报警开关
    private int sos_alert_switch;// 0 或者1(1打开，0关闭)

    //取下手环报警开关
    private int remove_alert_switch;// 0 或者1 (1打开，0关闭)

    //取下手表短信报警开关
    private int remove_sms_alert_switch;// 0 或者1 (1打开，0关闭)

    @Generated(hash = 1934880259)
    public CenterAlert(long id, String imei, int sos_alert_switch,
            int remove_alert_switch, int remove_sms_alert_switch) {
        this.id = id;
        this.imei = imei;
        this.sos_alert_switch = sos_alert_switch;
        this.remove_alert_switch = remove_alert_switch;
        this.remove_sms_alert_switch = remove_sms_alert_switch;
    }

    @Generated(hash = 2132777583)
    public CenterAlert() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSos_alert_switch() {
        return this.sos_alert_switch;
    }

    public void setSos_alert_switch(int sos_alert_switch) {
        this.sos_alert_switch = sos_alert_switch;
    }

    public int getRemove_alert_switch() {
        return this.remove_alert_switch;
    }

    public void setRemove_alert_switch(int remove_alert_switch) {
        this.remove_alert_switch = remove_alert_switch;
    }

    public int getRemove_sms_alert_switch() {
        return this.remove_sms_alert_switch;
    }

    public void setRemove_sms_alert_switch(int remove_sms_alert_switch) {
        this.remove_sms_alert_switch = remove_sms_alert_switch;
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }
}

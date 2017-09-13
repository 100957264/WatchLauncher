package com.fise.marechat.bean.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author mare
 * @Description:
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/16
 * @time 17:34
 */
@Entity
public class StepHistory extends CenterSettingBase {
    @Id(autoincrement = true)
    private long id;

    public String imei;//主键

    private String date;

    private String time;

    private long step_count;

    private long step_energy;//消耗的能量

    @Generated(hash = 589915079)
    public StepHistory(long id, String imei, String date, String time,
            long step_count, long step_energy) {
        this.id = id;
        this.imei = imei;
        this.date = date;
        this.time = time;
        this.step_count = step_count;
        this.step_energy = step_energy;
    }

    @Generated(hash = 1435858099)
    public StepHistory() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getStep_count() {
        return this.step_count;
    }

    public void setStep_count(long step_count) {
        this.step_count = step_count;
    }

    public long getStep_energy() {
        return this.step_energy;
    }

    public void setStep_energy(long step_energy) {
        this.step_energy = step_energy;
    }

}

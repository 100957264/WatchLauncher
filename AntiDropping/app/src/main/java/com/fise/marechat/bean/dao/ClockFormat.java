package com.fise.marechat.bean.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author mare
 * @Description:
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/11
 * @time 9:59
 */
@Entity
public class ClockFormat extends CenterSettingBase {
    @Id(autoincrement = true)
    private Long id;

    private long clock_id;//CenterClock的主键作为外键

    private String date;

    private String time;

    private int clock_switch;

    private int clock_style;

    private int clock_day;//周一至周日

    private String clock_time_string;//08:10-1-3-0111110

    @Generated(hash = 863517105)
    public ClockFormat(Long id, long clock_id, String date, String time,
            int clock_switch, int clock_style, int clock_day,
            String clock_time_string) {
        this.id = id;
        this.clock_id = clock_id;
        this.date = date;
        this.time = time;
        this.clock_switch = clock_switch;
        this.clock_style = clock_style;
        this.clock_day = clock_day;
        this.clock_time_string = clock_time_string;
    }

    @Generated(hash = 793083540)
    public ClockFormat() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getClock_id() {
        return this.clock_id;
    }

    public void setClock_id(long clock_id) {
        this.clock_id = clock_id;
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

    public int getClock_switch() {
        return this.clock_switch;
    }

    public void setClock_switch(int clock_switch) {
        this.clock_switch = clock_switch;
    }

    public int getClock_style() {
        return this.clock_style;
    }

    public void setClock_style(int clock_style) {
        this.clock_style = clock_style;
    }

    public int getClock_day() {
        return this.clock_day;
    }

    public void setClock_day(int clock_day) {
        this.clock_day = clock_day;
    }

    public String getClock_time_string() {
        return this.clock_time_string;
    }

    public void setClock_time_string(String clock_time_string) {
        this.clock_time_string = clock_time_string;
    }

}
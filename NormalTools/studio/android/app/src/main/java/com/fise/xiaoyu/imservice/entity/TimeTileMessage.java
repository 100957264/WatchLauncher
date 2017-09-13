package com.fise.xiaoyu.imservice.entity;

/**
 * 时间标题消息
 */
public class TimeTileMessage {
    private int time;
    public TimeTileMessage(int mTime){
        time= mTime;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}

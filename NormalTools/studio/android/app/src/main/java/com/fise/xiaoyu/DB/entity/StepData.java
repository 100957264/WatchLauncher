package com.fise.xiaoyu.DB.entity;


/**
 * Created by dylan on 2016/1/30.
 */

public class StepData {

    // 指定自增，每个对象需要有一个主键
    private long id;

    private String today;
    private String step;

    public StepData() {
    }

    public StepData(Long id,String today,String step) {
        this.id = id;
        this.today = today;
        this.step = step;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return "StepData{" +
                "id=" + id +
                ", today='" + today + '\'' +
                ", step='" + step + '\'' +
                '}';
    }
}

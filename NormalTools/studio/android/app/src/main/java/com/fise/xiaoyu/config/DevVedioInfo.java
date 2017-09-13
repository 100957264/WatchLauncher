package com.fise.xiaoyu.config;

/**
 * Created by weileiguan on 2017/7/20 0020.
 */
public class DevVedioInfo {
    private int type;
    private String pullUrl = "";
    private String pushUrl ="";


    public  DevVedioInfo(int type,String pullUrl,String pushUrl) {
        this.type = type;
        this.pullUrl = pullUrl;
        this.pushUrl = pushUrl;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setPullUrl(String pullUrl) {
        this.pullUrl = pullUrl;
    }

    public String getPullUrl() {
        return pullUrl;
    }

    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
    }

    public String getPushUrl() {
        return pushUrl;
    }

}

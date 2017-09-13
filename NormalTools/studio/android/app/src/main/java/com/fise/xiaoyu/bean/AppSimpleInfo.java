package com.fise.xiaoyu.bean;

import java.io.Serializable;

/**
 * Created by xiejianghong on 2017/8/29.
 * 应用商店列表 应用的简要信息
 */

public class AppSimpleInfo implements Serializable {
    public int id;
    public String appId;
    public String appName;
    public String descrition;
    public String currentVersion;
    public String className;
    public String iconUrl;
    public String[] imageList;
    public String downloadUrl;
    public String packageSize;
    public int upated;
    public int created;
}

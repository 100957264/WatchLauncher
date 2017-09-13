package com.fise.xiaoyu.bean;

import java.io.Serializable;

/**
 * Created by xiejianghong on 2017/8/29.
 * 基础json bean
 */

public class BaseResponse<T> implements Serializable {
    public int code;
    public String msg;
    public T data;
}
package com.fise.xiaoyu.bean;

import java.io.Serializable;

/**
 * Created by xiejianghong on 2017/8/29.
 * 无附加data的基础json bean
 */

public class SimpleResponse implements Serializable {
    public int code;
    public String msg;

    public BaseResponse toBaseResponse() {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.code = code;
        baseResponse.msg = msg;
        return baseResponse;
    }
}
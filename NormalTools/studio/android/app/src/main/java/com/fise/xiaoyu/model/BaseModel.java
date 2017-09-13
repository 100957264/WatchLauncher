package com.fise.xiaoyu.model;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;

import java.lang.reflect.Type;

/**
 * Created by xiejianghong on 2017/8/29.
 * 封装网络请求，感觉不够简便，暂未使用。
 */

public abstract class BaseModel<T> {
    protected String url;
    protected Type type;

    public void get() {
        OkGo.<T>get(url)
                .tag(this)
                .execute(new JsonCallback<T>(type) {
                    @Override
                    public void onSuccess(Response<T> response) {
                        BaseModel.this.onSuccess(response);
                    }

                    @Override
                    public void onError(Response<T> response) {
                        BaseModel.this.onError(response);
                    }
                });
    }

    public void post() {
        OkGo.<T>post(url)
                .tag(this)
                .execute(new JsonCallback<T>() {
                    @Override
                    public void onSuccess(Response<T> response) {
                        BaseModel.this.onSuccess(response);
                    }

                    @Override
                    public void onError(Response<T> response) {
                        BaseModel.this.onError(response);
                    }
                });
    }

    public BaseModel<T> url(String url) {
        this.url = url;
        return this;
    }

    public BaseModel<T> type(Type type) {
        this.type = type;
        return this;
    }

    public void onError(Response<T> response) {
    }

    public abstract void onSuccess(Response<T> response);
}

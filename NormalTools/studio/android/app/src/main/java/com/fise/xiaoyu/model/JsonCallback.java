package com.fise.xiaoyu.model;

import com.fise.xiaoyu.bean.BaseResponse;
import com.fise.xiaoyu.bean.SimpleResponse;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.lzy.okgo.callback.AbsCallback;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;

/**
 * Created by xiejianghong on 2017/8/29.
 * Json数据通用回调接口，java泛型会进行类型擦除，如有多级封装必须传入type或者clazz，否则无法解析成bean。
 */

public abstract class JsonCallback<T> extends AbsCallback<T> {
    private Type type;
    private Class<T> clazz;

    public JsonCallback() {
    }

    public JsonCallback(Type type) {
        this.type = type;
    }

    public JsonCallback(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T convertResponse(okhttp3.Response response) throws Throwable {
        ResponseBody body = response.body();
        if (body == null) return null;
        T data = null;
        Gson gson = new Gson();
        JsonReader jsonReader = new JsonReader(body.charStream());
        if (type != null) {
            data = gson.fromJson(jsonReader, type);
        } else if (clazz != null) {
            data = gson.fromJson(jsonReader, clazz);
        } else {
            Type genType = getClass().getGenericSuperclass();
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            Type type = params[0];
            if (!(type instanceof ParameterizedType)) {
                data = gson.fromJson(jsonReader, type);
                // response.close();
                // throw new IllegalStateException("没有填写泛型参数");
            } else {
                Type rawType = ((ParameterizedType) type).getRawType();
                Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
                if (rawType != BaseResponse.class) {
                    data = gson.fromJson(jsonReader, type);
                } else {
                    if (typeArgument == Void.class) {
                        SimpleResponse simpleResponse = gson.fromJson(jsonReader, SimpleResponse.class);
                        data = (T) simpleResponse.toBaseResponse();
                    } else {
                        BaseResponse baseResponse = gson.fromJson(jsonReader, type);
                        int code = baseResponse.code;
                        if (code == 0) {
                            data = (T) baseResponse;
                        } else {
                            response.close();
                            throw new IllegalStateException("错误代码:" + code + ",错误信息:" + baseResponse.msg);
                        }
                    }
                }
            }
        }
        response.close();
        return data;
    }
}

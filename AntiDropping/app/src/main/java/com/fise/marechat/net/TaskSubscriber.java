package com.fise.marechat.net;

import android.content.Context;

import com.fise.marechat.exception.CustomThrowable;

/**
 * @author mare
 * @Description:处理网络类事件
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/9
 * @time 9:39
 */
public class TaskSubscriber<T> extends BaseSubscriber<T> {


    private Context context;
    com.fise.marechat.task.TaskBean.TaskCallback callback;

    /**
     * @param context
     */
    public TaskSubscriber(Context context, com.fise.marechat.task.TaskBean.TaskCallback callback) {
        super(context);
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void onStart() {
        super.onStart();
        onBegin();
    }

    @Override
    public void onError(CustomThrowable e) {
        dismissDialog();
    }

    @Override
    public void onCompleted() {
        super.onCompleted();
        dismissDialog();
    }

    @Override
    public void onNext(T t) {
        callback.doTaskBack();
    }

}

package com.fise.marechat.net;

import android.content.Context;

import com.fise.marechat.exception.CustomThrowable;
import com.fise.marechat.utils.LogUtils;
import com.fise.marechat.utils.NetworkUtil;

/**
 * @author mare
 * @Description:处理网络类事件
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/9
 * @time 9:39
 */
public class CustomSubscriber<T> extends BaseSubscriber<T> {


    private Context context;

    /**
     * @param context
     */
    public CustomSubscriber(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!NetworkUtil.isNetworkAvailable(context)) {
            LogUtils.e("网络异常 ~~");
            onCompleted();
            return;
        }
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

    }

}

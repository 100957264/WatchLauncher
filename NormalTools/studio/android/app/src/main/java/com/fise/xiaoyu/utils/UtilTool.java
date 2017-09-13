package com.fise.xiaoyu.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by weileiguan on 2017/8/21 0021.
 */

public class UtilTool {
    /**
     * Drawable转换成Bitmap
     * @param d
     * @return
     */
    public static Bitmap drawToBmp(Drawable d) {
        if (null != d) {
            BitmapDrawable bd = (BitmapDrawable) d;
            return bd.getBitmap();
        }
        return null;
    }

    /**
     * 检测网络连接
     *
     * @return
     */
    public static boolean checkConnection(Context context) {
        @SuppressWarnings("static-access")
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
    }

    /**
     * Wifi是否可用
     * @param mContext
     * @return
     */
    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getTypeName().equals("WIFI")) {
            return true;
        }
        return false;
    }
}
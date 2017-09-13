package com.fise.xiaoyu.ui.base;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;

import com.fise.xiaoyu.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xiejianghong on 2017/9/4.
 * Activity基类，进行动态权限请求、EventBus注册、Activity管理
 * 小米等国产rom可能有修改，可在PermissionListener.onConvert判断权限是否真正获取。
 */

public class AppBaseActivity extends AppCompatActivity {
    private PermissionListener mListener;
    private static final int PERMISSION_REQUESTCODE = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.dd(Logger.LOG_APPLICATION || Logger.LOG_ACTIVITY_NAME, getClass().getSimpleName() + "	onCreate");
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Logger.dd(Logger.LOG_EVENTBUS,  getClass().getSimpleName() + "	EventBus->register");
        ActivityManager.getInstance().addActivity(this);
    }

    @Override
    protected void onStart() {
        Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onStart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Logger.dd(Logger.LOG_APPLICATION || Logger.LOG_ACTIVITY_NAME, getClass().getSimpleName() + "	onDestroy");
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Logger.dd(Logger.LOG_EVENTBUS,  getClass().getSimpleName() + "	EventBus->unregister");
    }

    @Override
    public void finish() {
        Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	finish");
        super.finish();
    }

    @Override
    public void onLowMemory() {
        Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onTrimMemory");
        super.onTrimMemory(level);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onNewIntent");
        super.onNewIntent(intent);
    }

    public void requestRunPermisssion(String permission, PermissionListener listener) {
        requestRunPermisssion(new String[]{permission}, listener);
    }

    public void requestRunPermisssion(String[] permissions, PermissionListener listener) {
        mListener = listener;
        List<String> permissionLists = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED || !isRealPermission(permission)) {
                permissionLists.add(permission);
            }
        }

        if (!permissionLists.isEmpty()) {
            Logger.dd(Logger.LOG_PERMISSION, "请求权限：" + permissionLists.size());
            ActivityCompat.requestPermissions(this, permissionLists.toArray(new String[permissionLists.size()]), PERMISSION_REQUESTCODE);
        } else {
            //表示全都授权了
            mListener.done(new ArrayList<>(Arrays.asList(permissions)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUESTCODE:
                if (grantResults.length > 0) {
                    //存放没授权的权限
                    List<String> deniedPermissions = new ArrayList<>();
                    for (int i = 0; i < grantResults.length; i++) {
                        int grantResult = grantResults[i];
                        String permission = permissions[i];
                        if (grantResult != PackageManager.PERMISSION_GRANTED || !isRealPermission(permission)) {
                            deniedPermissions.add(permission);
                        }
                    }
                    if (deniedPermissions.isEmpty()) {
                        //说明都授权了
                        mListener.done(deniedPermissions);
                    } else {
                        Logger.dd(Logger.LOG_PERMISSION, "请求权限失败：" + deniedPermissions.size());
                        mListener.onDenied(deniedPermissions);
                    }
                }
                break;
            default:
                break;
        }
    }

    //适配一些非原生ROM的修改，比如获取imei号显示已授权，实际并未授权，需要在此通过imei==null判断是否获得授权
    private boolean isRealPermission(String permission) {
        switch (permission) {
            case Manifest.permission.READ_PHONE_STATE:
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    String imei = telephonyManager.getDeviceId();
                    if (imei == null) {
                        return false;
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 已授权、未授权的接口回调
     */
    public abstract static class PermissionListener {

        protected abstract void onGranted();//已授权

        protected abstract void onDenied(List<String> deniedPermission);//未授权

        //自定义适配一些非原生ROM的修改
        protected boolean onConvert() {
            return true;
        }

        private void done(List<String> deniedPermission) {
            if (onConvert()) {
                Logger.dd(Logger.LOG_PERMISSION, "全部授权");
                onGranted();
            } else {
                Logger.dd(Logger.LOG_PERMISSION, "非原生ROM，请求权限失败");
                onDenied(deniedPermission);
            }
        }
    }
}

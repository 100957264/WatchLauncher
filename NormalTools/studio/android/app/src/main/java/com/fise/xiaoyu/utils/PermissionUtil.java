package com.fise.xiaoyu.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.ui.base.ActivityManager;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiejianghong on 2017/9/4.
 * 动态权限处理工具
 */

public class PermissionUtil {
    // 设备端必备权限，否则无法启动应用
    public static final String[] ALL_PERMISSION_GROUP = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,

            Manifest.permission.CAMERA,

            Manifest.permission.RECORD_AUDIO,

            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            // Manifest.permission.BODY_SENSORS,

            Manifest.permission.READ_CONTACTS,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.WRITE_CONTACTS,

            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,

            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,

            // Manifest.permission.WRITE_SETTINGS,
            // Manifest.permission.WRITE_SECURE_SETTINGS,
    };
    // 普通端必备权限，否则无法启动应用
    public static final String[] MUST_PERMISSION_GROUP = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    // 获取权限缺少的提示语
    public static String getPermissionString(Context context, List<String> permissions) {
        ArrayList<String> lackPermissions = new ArrayList<>();
        for (String permisson : permissions) {
            String p = getString(context, permisson);
            if (!lackPermissions.contains(p)) {
                lackPermissions.add(p);
            }
        }
        String text = String.format(context.getString(R.string.no_permission), TextUtils.join("/", lackPermissions));
        return text;
    }

    private static int getResource(Context context, String stringName) {
        int resId = context.getResources().getIdentifier(stringName, "string", context.getPackageName());
        //如果没有在"string"下找到stringName,将会返回0
        return resId;
    }

    private static String getString(Context context, String stringName) {
        int resId = getResource(context, stringName);
        if (resId == 0) {
            return "Unkown";
        }
        return context.getResources().getString(resId);
    }

    /**
     * 跳转到权限设置界面
     */
    public static void getAppDetailSettingIntent(Context context) {

        // vivo 点击设置图标>加速白名单>我的app
        //      点击软件管理>软件管理权限>软件>我的app>信任该软件
        Intent appIntent = context.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure");
        if (appIntent != null) {
            context.startActivity(appIntent);
            // floatingView = new SettingFloatingView(this, "SETTING", getApplication(), 0);
            // floatingView.createFloatingView();
            return;
        }

        // oppo 点击设置图标>应用权限管理>按应用程序管理>我的app>我信任该应用
        //      点击权限隐私>自启动管理>我的app
        appIntent = context.getPackageManager().getLaunchIntentForPackage("com.oppo.safe");
        if (appIntent != null) {
            context.startActivity(appIntent);
            // floatingView = new SettingFloatingView(this, "SETTING", getApplication(), 1);
            // floatingView.createFloatingView();
            return;
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(intent);
    }

    // 判断是否有权限缺少
    public static boolean lackPermissions(List<String> permissions) {
        Activity activity = ActivityManager.getInstance().currentActivity();
        for (String permission : permissions) {
            if (lackPermission(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否有权限缺少
    public static boolean lackPermissions(String[] permissions) {
        Activity activity = ActivityManager.getInstance().currentActivity();
        for (String permission : permissions) {
            if (lackPermission(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否有权限缺少
    public static boolean lackPermission(String... permissions) {
        Activity activity = ActivityManager.getInstance().currentActivity();
        for (String permission : permissions) {
            if (lackPermission(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否缺少权限
    private static boolean lackPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) !=
                PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @param command 命令：String apkRoot="chmod 777 "+getPackageCodePath(); RootCommand(apkRoot);
     * @return 应用程序是/否获取Root权限
     */
    public static boolean rootCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Logger.dd(Logger.LOG_PERMISSION, "ROOT FAIL! " + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                Logger.dd(Logger.LOG_PERMISSION, e.getMessage());
            }
        }
        Logger.dd(Logger.LOG_PERMISSION, "Root SUCCESS. ");
        return true;
    }

    public static boolean isRoot() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (Exception e) {
            Logger.dd(Logger.LOG_PERMISSION, "ROOT FAIL! " + e.getMessage());
            return false;
        } finally {
            try {
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                Logger.dd(Logger.LOG_PERMISSION, e.getMessage());
            }
        }
        Logger.dd(Logger.LOG_PERMISSION, "Root SUCCESS. ");
        return true;
    }
}

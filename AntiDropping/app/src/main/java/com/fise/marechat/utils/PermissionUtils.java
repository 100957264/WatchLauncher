package com.fise.marechat.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import com.fise.marechat.KApplication;
import com.fise.marechat.exception.CustomException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by blue on 2016/6/14.
 * <p/>
 * 检查权限的工具类
 */
public class PermissionUtils {

    public static final int REQUEST_CODE = 0;//请求码

    //检查权限时，判断系统的权限集合
    public static List<String> verifyPermissions(Context context, String... permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (isDenied(context, permission)) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions;
    }

    //检查系统权限是，判断当前是否缺少权限(PERMISSION_DENIED:权限是否足够)
    public static boolean isDenied(Context ctx, String permission) {
        return ActivityCompat.checkSelfPermission(ctx, permission) != PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isGranted(String permission) {
        PackageManager pm = KApplication.sContext.getPackageManager();
        return PackageManager.PERMISSION_GRANTED == pm.checkPermission(permission, KApplication.sContext.getPackageName());
    }

    public static boolean hasDeniedPermissions(Context context, String... permissions) {
        boolean checked = false;
        for (String permission : permissions) {
            if (isDenied(context, permission)) {
                return true;
            }
        }
        return checked;
    }

    public static void checkpermission(Activity ctx, String[] permissions) {
        if (Build.VERSION.SDK_INT >= 23 && verifyPermissions(ctx, permissions).size() > 0) {
            ActivityCompat.requestPermissions(ctx, permissions, PermissionUtils.REQUEST_CODE);
        }
    }

    public static void checkpermission(Activity ctx, String permission) {
        if (Build.VERSION.SDK_INT >= 23 && isDenied(ctx, permission)) {
            ActivityCompat.requestPermissions(ctx, new String[]{permission}, PermissionUtils.REQUEST_CODE);
        }
    }

    public static boolean getWriteSettingsPermission(Context ctx) {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + ctx.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            CustomException.handleException(e);
            return  false;
        }
        return true;
    }

    public static void checkWriteSettingsPermission(Activity ctx, String permission,int code){
        if(isDenied(ctx, permission)) {
            ActivityCompat.requestPermissions(ctx, new String[]{permission}, code);
        }
    }

}

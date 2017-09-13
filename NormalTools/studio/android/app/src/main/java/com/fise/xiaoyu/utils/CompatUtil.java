package com.fise.xiaoyu.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;

import com.fise.xiaoyu.BuildConfig;

import java.io.File;

/**
 * Created by xiejianghong on 2017/8/30.
 */

public class CompatUtil {
    private static final String FILE_PROVIDER = BuildConfig.APPLICATION_ID + ".fileprovider";

    /**
     * 在API16以前使用setBackgroundDrawable，在API16以后使用setBackground
     * API16<---->4.1
     *
     * @param view
     * @param drawable
     */
    public static void setBackgroundOfVersion(View view, Drawable drawable) {
        if (drawable == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Android系统大于等于API16，使用setBackground
            view.setBackground(drawable);
        } else {
            // Android系统小于API16，使用setBackgroundDrawable
            view.setBackgroundDrawable(drawable);
        }
    }

    public static void startActionInstall(Context context, File file) {
        //判断是否是AndroidN以及更高的版本
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, FILE_PROVIDER, file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    /**
     * 打开文件
     * 兼容7.0
     *
     * @param context     activity
     * @param file        File
     * @param contentType 文件类型如：文本（text/html）
     *                    当手机中没有一个app可以打开file时会抛ActivityNotFoundException
     */
    public static void startActionFile(Context context, File file, String contentType) throws ActivityNotFoundException {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);//增加读写权限
        intent.setDataAndType(getUriForFile(context, file), contentType);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 打开相机
     * 兼容7.0
     *
     * @param activity    Activity
     * @param file        File
     * @param requestCode result requestCode
     */
    public static void startActionCapture(Activity activity, File file, int requestCode) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getUriForFile(activity, file));
        activity.startActivityForResult(intent, requestCode);
    }

    // file路径转为FileProvider的uri
    public static Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Logger.dd(Logger.LOG_URI, file.getAbsolutePath());
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(), FILE_PROVIDER, file);
        } else {
            uri = Uri.fromFile(file);
        }
        Logger.dd(Logger.LOG_URI, uri.toString());
        return uri;
    }

    // uri转为真实file路径
    public static File getFileForUri(Context context, Uri uri) {
        Logger.dd(Logger.LOG_URI, "Format: " + uri.toString());
        if (context == null || uri == null) {
            throw new NullPointerException();
        }
        if (uri.getScheme().equals("file")) {
            return new File(uri.getPath());
        }
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor actualimagecursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (actualimagecursor == null) {
            return null;
        }
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String img_path = actualimagecursor.getString(actual_image_column_index);
        File file = new File(img_path);
        actualimagecursor.close();
        return file;
    }

    // 可能是部分第三方应用未兼容android7.0，部分uri无法临时授权，先获取实际file路径再转为FileProvider的uri
    public static Uri convertUri(Context context, Uri uri) {
        Logger.dd(Logger.LOG_URI, "Convert: " + uri.toString());
        if (uri.getHost().equals(FILE_PROVIDER)) {
            return uri;
        }
        File file = getFileForUri(context, uri);
        if (file == null) {
            return null;
        }
        return getUriForFile(context, file);
    }

}

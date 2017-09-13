package com.fise.marechat.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.fise.marechat.KApplication;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class AppUtils {
    private static final String TAG = "AppUtils";

    /**
     * 获取Activity堆栈顶层的名字
     *
     * @return
     */
    public static String getTopActivity() {

        boolean IS_LOL_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        if (IS_LOL_OR_LATER) {
        } else if (!checkPermission("android.permission.GET_TASKS")) {
            return null;
        }
        ActivityManager mAm = (ActivityManager) KApplication.sContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = mAm.getRunningTasks(1);
        String topStr = "";
        if (taskList != null && taskList.size() > 0) {
            topStr = taskList.get(0).topActivity.toString();
        }
        return topStr;
    }

    /**
     * 获取设备所有已安装应用的包名
     *
     * @return
     */
    public static List<String> getAllPackage() {
        List<String> list = new ArrayList<>();
        try {
            List<PackageInfo> packs = KApplication.sContext.getPackageManager().getInstalledPackages(0);
            for (int i = 0; i < packs.size(); i++) {
                PackageInfo p = packs.get(i);
                list.add(p.packageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * 通过包名获取应用信息
     *
     * @return null 表示找不到
     */
    public static PackageInfo getPackageInfoByPackageName(String packageName) {
        try {
            PackageInfo packageInfo = KApplication.sContext.getPackageManager().getPackageInfo(packageName, 0);
            return packageInfo;

        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }

    /**
     * 获取设备权限列表
     */
    public static List<String> getDevicePermissionList() {
        String[] permissionStrings = null;
        try {
            PackageManager pm = KApplication.sContext.getPackageManager();
            PackageInfo pack = pm.getPackageInfo(KApplication.sContext.getPackageName(), PackageManager.GET_PERMISSIONS);
            permissionStrings = pack.requestedPermissions;

        } catch (PackageManager.NameNotFoundException e) {
        }

        List<String> lstPermission = new ArrayList<>();
        if (permissionStrings != null) {
            for (int i = 0; i < permissionStrings.length; i++) {
                lstPermission.add(permissionStrings[i]);
            }
        }

        return lstPermission;
    }

    /**
     * 判断设备是否具有某权限
     */
    public static boolean checkPermission(String permName) {
        PackageManager pm = KApplication.sContext.getPackageManager();
        return PackageManager.PERMISSION_GRANTED == pm.checkPermission(permName, KApplication.sContext.getPackageName());
    }


    /**
     * 获取用户唯一标识
     */
    public static JSONArray getUserUniqueID() {

        String android_id = "", device_id = "", wifi_mac = "";
        String device_serial = "";
        String simSerialNumber = "";
        String imsi = "";
        Context ctx = KApplication.sContext;
        try {
            //GET ANDROID_ID
            android_id = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);

            //GET DEVICE_ID
            if (checkPermission("android.permission.READ_PHONE_STATE")) {
                TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
                device_id = tm.getDeviceId();
                simSerialNumber = tm.getSimSerialNumber();
                imsi = tm.getSubscriberId();
            }

            //GET WIFI MAC
            if (checkPermission("android.permission.ACCESS_WIFI_STATE")) {
                WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
                wifi_mac = wifiManager.getConnectionInfo().getMacAddress();
            }

            //device_serial
            device_serial = Build.SERIAL;


        } catch (Exception e) {
        }

        JSONArray jsonArray = new JSONArray();
        if (!TextUtils.isEmpty(android_id)) jsonArray.put("device.adid." + android_id);
        if (!TextUtils.isEmpty(device_id)) jsonArray.put("phone.deviceid." + device_id);
        if (!TextUtils.isEmpty(wifi_mac)) jsonArray.put("device.mac." + wifi_mac);
        if (!TextUtils.isEmpty(device_serial)) jsonArray.put("device.serial." + device_serial);
        if (!TextUtils.isEmpty(simSerialNumber)) jsonArray.put("sim.serial." + simSerialNumber);
        if (!TextUtils.isEmpty(imsi)) jsonArray.put("sim.imsi." + imsi);
        return jsonArray;

    }


    /**
     * 判断当前应用是否是debug状态
     */
    public static boolean isApkDebuggable() {
        try {
            ApplicationInfo info = KApplication.sContext.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断设备是否启用WIFI
     */
    public static boolean checkWifiOpened() {
        if (checkPermission("android.permission.ACCESS_WIFI_STATE")) {
            WifiManager wifiManager = (WifiManager) KApplication.sContext.getSystemService(Context.WIFI_SERVICE);
            return wifiManager.isWifiEnabled();
        }
        return false;
    }

    /**
     * 获取网络状态，wifi,MOBILE
     *
     * @return int -1 获取失败 ; 0 MOBILE; 1 WIFI
     */
    public static int getNetWorkType() {

        int nType = -1;
        ConnectivityManager manager = (ConnectivityManager) KApplication.sContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (null != networkInfo && networkInfo.isConnected()) {
            nType = networkInfo.getType();
        }

        return nType;
    }

    /**
     * 判断设备是否启用WIFI
     */
    public static String getNetProvider() {
        String imsi = "";
        try {
            //GET IMSI
            if (checkPermission("android.permission.READ_PHONE_STATE")) {
                TelephonyManager tm = (TelephonyManager) KApplication.sContext.getSystemService(Context.TELEPHONY_SERVICE);
                imsi = tm.getSubscriberId();
            }
        } catch (Exception e) {
        }
        if (null != imsi && imsi.length() >= 5) {
            return imsi.substring(0, 5);
        }

        return "";
    }

    /**
     * 通过获取包信息判断文件是否可以安装
     *
     * @param apkPath
     * @return
     */
    public static boolean isApkCanInstall(Context context, String apkPath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


    public static void installApk(Context context, Uri uri) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(i);
    }

    public static void installApk(Context context, String apkFilePath) {
        File apkFile = new File(apkFilePath);
        if (!apkFile.exists()) {
            Toast.makeText(context, "apk file is not exist", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = Uri.fromFile(apkFile);
        installApk(context, uri);
    }

    public static String getApplicationName(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName =
                (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

    /**
     * 获取单个App的签名
     **/
    public String getAppSignature(Context ctx) throws PackageManager.NameNotFoundException {
        String pkgName = ctx.getPackageName();
        PackageManager pm = ctx.getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
        String allSignature = packageInfo.signatures[0].toCharsString();
        return allSignature;
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;
        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pi;
    }

    //版本名
    public static String getVersionName(Context context) {
        return getPackageInfo(context).versionName;
    }

    //版本号
    public static int getVersionCode(Context context) {
        return getPackageInfo(context).versionCode;
    }
}

//package com.fise.antidropping.utils;
//
//import android.content.Context;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.reeve.battery.entity.ads.TabInfo;
//import com.reeve.battery.entity.app.GameDetail;
//import com.reeve.battery.entity.dfhn.DFHeadRequest;
//import com.reeve.battery.knews.PreferenceUtils;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import okhttp3.FormBody;
//
///**
// * Created by blue on 2017/6/19 0019.
// */
//
//public class BeanUtils {
//
//    public static FormBody.Builder getFormBodyBuilder(String json) {
//        FormBody.Builder builder = new FormBody.Builder();
//        String[] pairs = json.split("&");
//        int len = pairs.length;
//        String arg;
//        String arg_key, arg_value;
//        String[] args;
//        for (int i = 0; i < len; i++) {
//            arg = pairs[i];
//            args = arg.split("=");
//            if (null == args || args.length < 2) continue;
//            arg_key = args[0];
//            arg_value = args[1];
//            LogUtils.e("", arg_key + "=" + arg_value);
//            if ("null".equals(arg_value) || TextUtils.isEmpty(arg_value)) {
//                continue;
//            }
//            builder.add(arg_key, arg_value);
//        }
//        return builder;
//    }
//
//    /***
//     * 获取东方头条Entity
//     * @param ctx
//     * @param loadType
//     * @param tabInfo
//     * @param key
//     * @return
//     */
//    public static DFHeadRequest getDFHN(Context ctx, int loadType, TabInfo tabInfo, String key) {
//        boolean isFirst = AdsShare.getBoolean(PreferenceUtils.IS_FIRST_REQUEST, true);
////                Context ctx,int loadType, TabInfo tabInfo, RequestBody formBody, long pgnum, ProgressListener listener
//        long pgnum = 1;//下次请求的正页数或者负页数
//        long idx = 0;//之前已经获取到的正数据的总和，或者之前已经获取到的负数据的总和
//
//        idx = AdsShare.getLong(PreferenceUtils.getCompleteKeyName(tabInfo.getTabCategory(), PreferenceUtils.PARAMETER_IDX, loadType), 0);
//        pgnum = PreferenceUtils.requestParameterPgnum(tabInfo.getTabCategory(), PreferenceUtils.PARAMETER_PGNUM, loadType);//根据loadType确定：下次请求的正负页数
//        String startkey = AdsShare.getString(PreferenceUtils.getCompleteKeyName(tabInfo.getTabCategory(), PreferenceUtils.PARAMETER_START_KEY,
//                loadType), null);
//        String newKey = AdsShare.getString(PreferenceUtils.getCompleteKeyName(tabInfo.getTabCategory(), PreferenceUtils.PARAMETER_NEW_KEY,
//                loadType), null);
//        String imei = DeviceInfoUtils.getIMEI(ctx);
//
//        LogUtils.d("获取请求体：   loadType = " + loadType
//                + " , startKey = " + startkey
//                + " , newKey = " + newKey
//                + " , pgnum = " + pgnum
//                + " , idx = " + idx);
////        String type, String startkey, String newkey, int pgnum, String ime, String key, int idx, String apptypeid, String appver, String qid
//        DFHeadRequest entity = new DFHeadRequest(tabInfo.getTabCategory(), startkey, newKey, pgnum, imei, key, idx, PreferenceUtils.VALUE_KDAOHANG, AppUtils.getVersionName(ctx), PreferenceUtils.VALUE_KDAOHANG);
//        String jsonStr = entity.toString();
//        LogUtils.i(jsonStr);
//        return entity;
//    }
//
//    /**
//     * Entity --------> HashMap
//     *
//     * @param jsonStr
//     * @return
//     */
//    public static Map<String, Object> bean2MapPrameters(String jsonStr) {
//        LogUtils.i(jsonStr);
//        Map<String, Object> map = new HashMap<>();
//        String[] pairs = jsonStr.split("&");
//        int len = pairs.length;
//        String arg;
//        String arg_key, arg_value;
//        String[] args;
//        for (int i = 0; i < len; i++) {
//            arg = pairs[i];
//            args = arg.split("=");
//            if (null == args || args.length < 2) continue;
//            arg_key = args[0];
//            arg_value = args[1];
//            if ("null".equals(arg_value) || TextUtils.isEmpty(arg_value)) {
//                continue;
//            }
//            map.put(arg_key, arg_value);
//        }
//        return map;
//    }
//
//
//    /**
//     * 获取此url项所在的位置
//     *
//     * @param url
//     * @return
//     */
//    public static int findPosByGameUrl(String url, List<GameDetail> details) {
//        int pos = -1;
//        GameDetail detail;
//        for (int i = 0; i < details.size(); i++) {
//            detail = details.get(i);
//            if (url.equals(detail.getDownloadUrl())) {
//                return i;
//            }
//        }
//        return pos;
//    }
//
//
//}

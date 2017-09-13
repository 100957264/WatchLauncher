//package com.fise.antidropping.utils;
//
//import android.content.Context;
//import android.text.TextUtils;
//
//import okhttp3.FormBody;
//
///**
// * Created by blue on 2017/6/13 0013.
// */
//
//public class AdsUtils {
//
//    //广告内容来源类型
//    public static final int SID_FROM_EXTRAS_INFO = 0;//广告内容来源于附加信息
//    public static final int SID_FROM_OUR_AD_PLATFORM = 1;//广告内容来源于我公司自身的广告平台
//    public static final int SID_FROM_BAIDU_AD = 2;// 广告内容来源于百度
//    public static final int SID_FROM_ADVIE_AD = 3;// 广告内容来源于adview
//
//    public static final String AD_ORDER = "no";//必返回字段:排序位置
//    public static final String AD_OPEN_OPTION = "ops";//必返回字段:打开方式
//    public static final String AD_SID = "sid";//必返回字段:广告位,id是广告平台分配给我们的值，对接api时需要这个字段
//    public static final String AD_EXTRA = "ai";//附加字段
//
//    public static final String defaultImageUrl = "http://imglf1.nosdn.127.net/img/a0JLb2MxL0R1RXFTK0xvaTVuZ3o5L3oyV3lLMXlJYTlKdHorYkNsdU5vZHYzV0pxdHpKSy9RPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg";
//    public static final String defaultClickImageUrl = "http://www.cnlofter.com/";
//
//    public static final String SID = "1024";
//    public static final String AID = "10018";
//
//    private static final int PERMISSION_RESULT_CODE = 200;
//
//    public static int getPermissionCode() {
//        return PERMISSION_RESULT_CODE;
//    }
//
//    public static AdsRequestEntity getAdsRequestEntity(Context ctx) {
////        int sid, int aid, String imsi, String mac, String adrid, String opid, String imei, int osv, String dv,
////          String dm, int nt, int sw, int sh, int num, int type
//        int sid = DeviceInfoUtils.SID;
//        AdsRequestEntity original = DaoUtils.getInstance().getAdsRequestEntity(ctx, sid);
//        AdsRequestEntity postInfo = null;
//        String aid = DeviceInfoUtils.AID;
//        int type = DeviceInfoUtils.TYPE_ADS;
//        String imsi, mac, adrid, opid, imei, osv, dv, dm, nt, sw, sh, num;
//        if (null != original) {
//            imsi = original.getImsi();
//            mac = original.getMac();
//            adrid = original.getAdrid();
//            opid = original.getOpid();
//            imei = original.getImei();
//            osv = original.getOsv();
//            dv = original.getDv();
//            dm = original.getDm();
//            nt = original.getNt();
//            sw = original.getSw();
//            sh = original.getSh();
//            num = original.getNum();
//            mac = TextUtils.isEmpty(mac) ? DeviceInfoUtils.getMac(ctx) : mac;
//            adrid = TextUtils.isEmpty(adrid) ? DeviceInfoUtils.getDeviceId(ctx) : adrid;
//            opid = TextUtils.isEmpty(opid) ? DeviceInfoUtils.getOPID(ctx) : opid;
//            imei = TextUtils.isEmpty(imei) ? DeviceInfoUtils.getIMEI(ctx) : imei;
//            imsi = TextUtils.isEmpty(imsi) ? DeviceInfoUtils.getSubscriberId(ctx) : imsi;
//            osv = TextUtils.isEmpty(osv) ? DeviceInfoUtils.getOsVersion() : osv;
//            dv = TextUtils.isEmpty(dv) ? DeviceInfoUtils.getDV() : dv;
//            dm = TextUtils.isEmpty(dm) ? DeviceInfoUtils.getDM() : dm;
//            nt = TextUtils.isEmpty(nt) ? DeviceInfoUtils.getNT(ctx) : nt;
//            sw = TextUtils.isEmpty(sw) ? DeviceInfoUtils.getSCreenWidth(ctx) : sw;
//            sh = TextUtils.isEmpty(sh) ? DeviceInfoUtils.getScreenHeight(ctx) : sh;
//            num = TextUtils.isEmpty(num) ? DeviceInfoUtils.getPhoneNumber(ctx) : num;
//            postInfo = new AdsRequestEntity(sid, aid, imsi, mac, adrid, opid, imei, osv, dv, dm, nt, sw, sh, num, type);
//            postInfo.setId(original.getId());
//        } else {
//            imsi = DeviceInfoUtils.getSubscriberId(ctx);
//            mac = DeviceInfoUtils.getMac(ctx);
//            adrid = DeviceInfoUtils.getDeviceId(ctx);
//            opid = DeviceInfoUtils.getOPID(ctx);
//            imei = DeviceInfoUtils.getIMEI(ctx);
//            osv = DeviceInfoUtils.getOsVersion();
//            dv = DeviceInfoUtils.getDV();
//            dm = DeviceInfoUtils.getDM();
//            nt = DeviceInfoUtils.getNT(ctx);
//            sw = DeviceInfoUtils.getSCreenWidth(ctx);
//            sh = DeviceInfoUtils.getScreenHeight(ctx);
//            num = DeviceInfoUtils.getPhoneNumber(ctx);
//            postInfo = new AdsRequestEntity(sid, aid, imsi, mac, adrid, opid, imei, osv, dv, dm, nt, sw, sh, num, type);
//        }
//        DaoUtils.getInstance().updateAdsRequestEntity(ctx, postInfo);
//        return postInfo;
//    }
//
//    private static FormBody.Builder getFormBodyBuilder(String json) {
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
//            if ("null".equals(arg_value) || TextUtils.isEmpty(arg_value)) {
//                continue;
//            }
//            builder.add(arg_key, arg_value);
//        }
//        return builder;
//    }
//
//}

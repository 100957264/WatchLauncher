package com.fise.marechat.client.msg;

import android.text.TextUtils;

import java.util.HashMap;

/**
 * 消息头部标记位
 * Created by fanyang on 2017/8/4.
 */
public class MsgType {
    /*链路保持*/
    public static final String LK = "LK";
    /*位置数据上报*/
    public static final String UD = "UD";
    /*终端发送（电信基站)*/
    public static final String UD_CDMA = "UD_CDMA";
    /*盲点补传数据-移动联通*/
    public static final String UD2 = "UD2";
    /*盲点补传数据-电信*/
    public static final String UD_CDMA2 = "UD_CDMA2";
    /*报警数据上报-移动联通*/
    public static final String AL = "AL";
    /*报警数据上报-电信*/
    public static final String AL_CDMA = "AL_CDMA";
    /*获取服务器端时间*/
    public static final String Time = "Time";
    /*获取天气-气温段*/
    public static final String WT = "WT";
    /*获取天气-风*/
    public static final String WT2 = "WT2";
    /*上传拍照内容*/
    public static final String img = "img";
    /*上传指令语音包*/
    public static final String VOICE = "VOICE";
    /*数据上传间隔设置*/
    public static final String UPLOAD = "UPLOAD";
    /*中心号码设置*/
    public static final String CENTER = "CENTER";
    /*控制密码设置*/
    public static final String PW = "PW";
    /*拨打电话*/
    public static final String CALL = "CALL";
    /*监听*/
    public static final String MONITOR = "MONITOR";
    /*SOS号码设置*/
    public static final String SOS = "SOS";
    /*SOS号码设置-终端回复*/
    public static final String SOS3 = "SOS3";
    /*IP端口设置*/
    public static final String IP = "IP";
    /*恢复出厂设置*/
    public static final String FACTORY = "FACTORY";
    /*设置语言和时区*/
    public static final String LZ = "LZ";
    /*SOS短信报警开关*/
    public static final String SOSSMS = "SOSSMS";
    /*低电短信报警开关*/
    public static final String LOWBAT = "LOWBAT";
    /*版本查询*/
    public static final String VERNO = "VERNO";
    /*重启*/
    public static final String RESET = "RESET";
    /*定位指令*/
    public static final String CR = "CR";
    /*关机指令*/
    public static final String POWEROFF = "POWEROFF";
    /*取下手环报警开关*/
    public static final String REMOVE = "REMOVE";
    /*计步功能开关*/
    public static final String REMOVESMS = "REMOVESMS";
    /*计步功能开关*/
    public static final String PEDO = "PEDO";
    /*计步时间段设置*/
    public static final String WALKTIME = "WALKTIME";
    /*翻转检测时间段设置,计步时间段设置的 终端回复*/
    public static final String ANY = "ANY";
    /*翻转检测时间段设置*/
    public static final String SLEEPTIME = "SLEEPTIME";
    /*免打扰时间段设置*/
    public static final String SILENCETIME = "SILENCETIME";
    /*找手表指令*/
    public static final String FIND = "FIND";
    /*小红花个数设置指令（目前停用）*/
    public static final String FLOWER = "FLOWER";
    /*闹钟设置指令*/
    public static final String REMIND = "REMIND";
    /*对讲功能*/
    public static final String TK = "TK";
    /*终端检测离线语音*/
    public static final String TKQ = "TKQ";
    /*短语显示设置指令*/
    public static final String MESSAGE = "MESSAGE";
    /*设置白名单*/
    public static final String WHITELIST1 = "WHITELIST1";
    public static final String WHITELIST2 = "WHITELIST2";
    /*设置电话本1*/
    public static final String PHB = "PHB";
    /*设置电话本2*/
    public static final String PHB2 = "PHB2";
    /*设置电话本+白名单（最多50条）*/
    public static final String PHL = "PHL";
    /*情景模式*/
    public static final String profile = "profile";
    /*跌倒提醒开关*/
    public static final String FALLDOWN = "FALLDOWN";
    /*远程拍照*/
    public static final String rcapture = "rcapture";
    /*心率协议*/
    public static final String hrtstart = "hrtstart";
    /*终端心率上传*/
    public static final String heart = "heart";

    private static HashMap<String, String> mHeaderContent = new HashMap<>();

    static {
        mHeaderContent.put(LK, LK);
        mHeaderContent.put(UD, UD);
        mHeaderContent.put(UD_CDMA, UD_CDMA);
        mHeaderContent.put(UD2, UD2);
        mHeaderContent.put(UD_CDMA2, UD_CDMA2);
        mHeaderContent.put(AL_CDMA, AL_CDMA);
        mHeaderContent.put(Time, Time);
        mHeaderContent.put(WT2, WT2);
        mHeaderContent.put(img, img);
        mHeaderContent.put(VOICE, VOICE);
        mHeaderContent.put(UPLOAD, UPLOAD);
        mHeaderContent.put(CENTER, CENTER);
        mHeaderContent.put(PW, PW);
        mHeaderContent.put(CALL, CALL);
        mHeaderContent.put(MONITOR, MONITOR);
        mHeaderContent.put(SOS, SOS);
        mHeaderContent.put(SOS3, SOS3);
        mHeaderContent.put(FACTORY, FACTORY);
        mHeaderContent.put(SOSSMS, SOSSMS);
        mHeaderContent.put(LOWBAT, LOWBAT);
        mHeaderContent.put(VERNO, VERNO);
        mHeaderContent.put(RESET, RESET);
        mHeaderContent.put(POWEROFF, POWEROFF);
        mHeaderContent.put(REMOVE, REMOVE);
        mHeaderContent.put(REMOVESMS, REMOVESMS);
        mHeaderContent.put(PEDO, PEDO);
        mHeaderContent.put(WALKTIME, WALKTIME);
        mHeaderContent.put(ANY, ANY);
        mHeaderContent.put(SLEEPTIME, SLEEPTIME);
        mHeaderContent.put(SILENCETIME, SILENCETIME);
        mHeaderContent.put(FIND, FIND);
        mHeaderContent.put(FLOWER, FLOWER);
        mHeaderContent.put(REMIND, REMIND);
        mHeaderContent.put(TK, TK);
        mHeaderContent.put(TKQ, TKQ);
        mHeaderContent.put(MESSAGE, MESSAGE);
        mHeaderContent.put(WHITELIST1, WHITELIST1);
        mHeaderContent.put(WHITELIST2, WHITELIST2);
        mHeaderContent.put(PHB, PHB);
        mHeaderContent.put(PHB2, PHB2);
        mHeaderContent.put(PHL, PHL);
        mHeaderContent.put(profile, profile);
        mHeaderContent.put(FALLDOWN, FALLDOWN);
        mHeaderContent.put(rcapture, rcapture);
        mHeaderContent.put(hrtstart, hrtstart);
        mHeaderContent.put(heart, heart);

    }

    private MsgType() {
    }

    private static class SingletonHolder {
        private static final MsgType INSTANCE = new MsgType();
    }

    public static MsgType instance() {
        return SingletonHolder.INSTANCE;
    }

    public HashMap<String, String> getmHeaderContent() {
        return MsgType.mHeaderContent;
    }

    public boolean verifyMsgType(String msgType) {
        if (TextUtils.isEmpty(msgType)) {
            return false;
        }
        return MsgType.mHeaderContent.containsKey(msgType);
    }

}

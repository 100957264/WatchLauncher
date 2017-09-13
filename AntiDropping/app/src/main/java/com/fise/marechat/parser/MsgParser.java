package com.fise.marechat.parser;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fise.marechat.client.GlobalSettings;
import com.fise.marechat.client.msg.MsgType;
import com.fise.marechat.util.Md5;
import com.fise.marechat.util.ToastUtils;
import com.fise.marechat.utils.DigitalConvert;
import com.fise.marechat.utils.LogUtils;
import com.fise.marechat.utils.TypeConvert;

import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析拼接头部信息 以及加密要发送的信息
 * Created by fanyang on 2017/8/4.
 */
public class MsgParser {
    //[product*id*len*content]
    private MsgParser() {

    }

    private static class SingletonHolder {
        private static final MsgParser INSTANCE = new MsgParser();
    }

    public static MsgParser instance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 解析出收到的header信息
     *
     * @param msg [bca78c3ac6437502a11d9b4b844950a3FISE*123456789123459*000F*Time, 1502181849]
     * @return product*id*len*content
     */
    public String[] getHeaderAndContent(String msg) {
        String[] segemnts = null;
        String reg = "";
        if (!msg.contains(GlobalSettings.MSG_CONTENT_SEPERATOR)) {
            segemnts = new String[1];
            reg = "\\[(.*?)\\]";
        } else if (msg.contains(GlobalSettings.MSG_CONTENT_SEPERATOR)) {
            segemnts = new String[2];
            reg = "\\[(.*?),";
        }
        Pattern pattern = Pattern.compile(reg);
        Matcher m = pattern.matcher(msg);
        while (m.find()) {
            segemnts[0] = m.group(1);
            break;
        }
        if (segemnts.length < 2) {
            return segemnts;
        }
        segemnts[1] = msg.substring(msg.indexOf(',') + 1, msg.length() - 1);
        return segemnts;
    }

    /**
     * 解析出收到的content信息
     *
     * @param msg
     * @return
     */
    public String getNoneHeaderContent(String msg) {
        return msg.substring(msg.indexOf(',') + 1, msg.indexOf(']'));
    }

    /**
     *将header解析成字符串数组格式
     * @param header product*id*len*content
     * @return String[] [product , id , len , content]数组
     */
    private String[] getRecvHeaderSegments(String header) {
        String[] segemnt = header.split(GlobalSettings.MSG_HEADER_SEPERATOR);
        return segemnt;
    }

    /**
     * 获取完整的未加密的header
     *
     * @param header product*id*len*content
     * @return msgType 消息头部标记位
     */
    public String parseHeaderTypeByHeader(String header) {
        String[] segment = getRecvHeaderSegments(header);
        String msgType = segment[segment.length -1];
        LogUtils.e(msgType);
        return msgType;
    }

    /**
     * 获取完整的未加密的header
     *
     * @param msg [product*id*len*content]
     * @return
     */
    public String getHeaderSegmentsString(String msg) {
        String[] segemnts = getHeaderAndContent(msg)[0].split(GlobalSettings.MSG_HEADER_SEPERATOR);
        String content = segemnts[3];
        byte[] srcBytes = content.getBytes();
        int len = srcBytes.length;
        byte[] byte4 = TypeConvert.int2byte(len);
        segemnts[2] = DigitalConvert.byte2String(byte4);
        segemnts[0] = GlobalSettings.instance().getProduct();
        segemnts[1] = GlobalSettings.instance().getImei();
        String result = array2String(segemnts);
        LogUtils.e(result);
        return result;
    }

    /**
     * 将msgType 转成 product*id*len*content格式
     *
     * @param msgType content
     * @return product*id*len*content
     */
    public String getHeaderByType(String msgType) {
        if (!MsgType.instance().verifyMsgType(msgType)) {//校验msgType
            ToastUtils.showShort("content 格式不对");
            return null;
        }
        String[] segemnts = new String[4];
        segemnts[3] = msgType;
        byte[] srcBytes = segemnts[3].getBytes();
        int len = srcBytes.length;
        byte[] byte4 = TypeConvert.int2byte(len);//转成4字节的byte数组
        segemnts[2] = DigitalConvert.byte2String(byte4);
        segemnts[0] = GlobalSettings.instance().getProduct();
        segemnts[1] = GlobalSettings.instance().getImei();

        String result = array2String(segemnts);
        LogUtils.e(result);
        return result;
    }

    /**
     * 将数组转为带*的字符串
     *
     * @param segemnts [product,id,len,content]
     * @return product*id*len*content
     */
    private String array2String(String[] segemnts) {
        int len = segemnts.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(segemnts[i]);
            if (i != len - 1) {
                sb.append(GlobalSettings.MSG_HEADER_SEPERATOR);
            }
        }
        LogUtils.i(sb.toString());
        return sb.toString();
    }

    /**
     * 将product*id*len*content转成[product*id*len*content]
     *
     * @param unComposeHedader
     * @return
     */
    public String composeHeader(String unComposeHedader) {
        StringBuilder sb = new StringBuilder();
        String result = sb.append(GlobalSettings.MSG_PREFIX)
                .append(unComposeHedader)
                .append(GlobalSettings.MSG_SUFFIX).toString();
        LogUtils.i(result);
        return result;
    }

    /**
     * 进行加密算法后的header
     * eg :9fa56f7e41aaf7273f30faff536619e6SG*8800000015*0002*LK
     *
     * @param msgType LK
     * @return 9fa56f7e41aaf7273f30faff536619e6SG*8800000015*0002*LK
     */
    public String encryptHeaderByType(String msgType) {
        String originalHeader = getHeaderByType(msgType);//product*id*len*content
        return encryptHeader(originalHeader);
    }

    /**
     *
     * @param originalHeader SG*8800000015*0002*LK
     * @return 9fa56f7e41aaf7273f30faff536619e6SG*8800000015*0002*LK
     */
    @NonNull
    private String encryptHeader(String originalHeader) {
        StringBuilder sb = new StringBuilder(originalHeader);
        sb.append(GlobalSettings.instance().getPrivateKey());//用于签名的数据
        String md5 = "";
        try {
            md5 = Md5.getMD5Lower32(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            LogUtils.e(e);
        } finally {
            sb.setLength(0);
            return sb.append(md5).append(originalHeader).toString();
        }
    }

    /**
     * @param msgType Time
     * @return [9fa56f7e41aaf7273f30faff536619e6product*id*len*Time]
     */
    public String composedEncryptedHeader(String msgType) {
        return composeHeader(encryptHeaderByType(msgType));
    }

    /**
     * 实际传输的完整数据
     *
     * @param msgType LK
     * @param content [9fa56f7e41aaf7273f30faff536619e6SG*8800000015*0002*LK,content]
     * @return
     */
    public String composedTypeContent(String msgType, String content) {
        String header = encryptHeaderByType(msgType);
        StringBuffer sb = new StringBuffer(header);
        if (!TextUtils.isEmpty(content)) {
            sb.append(GlobalSettings.MSG_CONTENT_SEPERATOR + content);//没有前后缀的信息
        }
        return composeHeader(sb.toString());
    }

    /**
     * 实际传输的完整数据
     *
     * @param header 未加密的header  product*id*len*content
     * @param content [9fa56f7e41aaf7273f30faff536619e6SG*8800000015*0002*LK,content]
     * @return
     */
    public String composedHeaderContent(String header, String content) {
        String encryptedHeader = encryptHeader(header);
        StringBuffer sb = new StringBuffer(encryptedHeader);
        if (!TextUtils.isEmpty(content)) {
            sb.append(GlobalSettings.MSG_CONTENT_SEPERATOR + content);//没有前后缀的信息
        }
        return composeHeader(sb.toString());
    }

}

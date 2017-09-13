package com.fise.marechat.prenster.msg;

import com.fise.marechat.util.StringUtils;
import com.fise.marechat.utils.DigitalConvert;

/**
 * @author mare
 * @Description:文本消息解析
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/14
 * @time 14:11
 */
public class TextMessageUtils {


    /**
     *
     * @param recvMsg FF1FFF1F... 该指令向终端推送显示的短语.短语采用Unicode编码下发给终端
     * @return
     */
    public static String parseRecvMsgContent(String recvMsg){
        if (StringUtils.isBlank(recvMsg))return recvMsg;
        String msg = null;
        int recvMsglen = recvMsg.length();
        int wordsLen = recvMsglen / 4 ;
        String hexWord ;
        String word ;
        StringBuffer sb  =new StringBuffer();
        for (int i = 0; i < wordsLen; i++) {
            hexWord = recvMsg.substring(i * 4 ,i * 4  + 4);
            word =  DigitalConvert.hexStr2Str(hexWord);
            sb.append(word);
        }
        return msg;
    }
}

package com.fise.marechat.manager;

import android.text.TextUtils;

import com.fise.marechat.bean.TargetInfo;
import com.fise.marechat.client.GlobalSettings;
import com.fise.marechat.client.TcpClient;
import com.fise.marechat.parser.MsgParser;

/**
 * @author mare
 * @Description:
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/9
 * @time 16:50
 */
public class MsgManager {
    TargetInfo targetInfo;

    private MsgManager() {
        targetInfo = new TargetInfo(GlobalSettings.getIP(), GlobalSettings.PORT);
    }

    private static class SingletonHolder {
        private static final MsgManager INSTANCE = new MsgManager();
    }

    public static MsgManager instance() {
        return SingletonHolder.INSTANCE;
    }

    private TcpClient getClient() {
        return TcpClient.instance();
    }

    public void sendMsg(String msgType, String content) {

        getClient().sendMsg(MsgParser.instance().composedTypeContent(msgType, content));
    }

    public void sendCompleteMsg(String header, String content) {
        if (!TextUtils.isEmpty(header)) {
            getClient().sendMsg(MsgParser.instance().composedHeaderContent(header, content));
        }
    }

}

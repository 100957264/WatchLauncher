package com.fise.marechat.thread;

import android.os.CountDownTimer;

import com.fise.marechat.client.TcpClient;
import com.fise.marechat.client.msg.MsgType;
import com.fise.marechat.parser.MsgParser;

/**
 * @author mare
 * @Description:心跳处理方式一
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/9
 * @time 17:28
 */
public class HeartBeatThread {
    static final String tag = HeartBeatThread.class.getSimpleName();
    private boolean mConnected;
    private static final int INTERVAL = 60 * 1000;
    private static final int LINK_INTERVAL = 10 * 60 * 1000;
    private static final int RECONECT_INTERVAL = 5 * 60 * 1000;
    private Timer mKeepLink;
    private Timer mReconnect;
    private boolean isReconnectRunning = false;
    private boolean isLinkRunning = false;
    private boolean isAvailable = false;//总开关

    private HeartBeatThread() {
        mKeepLink = new Timer(TimerType.LINK);
        mReconnect = new Timer(TimerType.RECONECT);
        startLinkThread();
    }

    private static class SingletonHolder {
        private static final HeartBeatThread INSTANCE = new HeartBeatThread();
    }

    public static HeartBeatThread instance() {
        return SingletonHolder.INSTANCE;
    }

    public enum TimerType {
        LINK, RECONECT
    }


    public void startLinkThread() {
        if (!isAvailable) return;
        if (isLinkRunning) return;
        mKeepLink.start();
        isLinkRunning = true;
    }

    public void stopLinkThread() {
        mKeepLink.cancel();
        mReconnect.cancel();
        isLinkRunning = false;
        isReconnectRunning = false;
    }

    private void reConnectClient() {
        TcpClient.instance().connect();
    }

    public void setConnnectedState(boolean connected) {
        this.mConnected = connected;
        if (connected) {
            mReconnect.cancel();
        } else {
            startReconnectThread();
        }
    }

    private void sendLink() {
        TcpClient.instance().sendMsg(MsgParser.instance().composedTypeContent(MsgType.LK, null));
    }

    private void startReconnectThread() {
        if (isReconnectRunning) return;
        mReconnect.cancel();
        mReconnect.start();
        isReconnectRunning = true;
    }

    private void stopReconnectThread() {
        mReconnect.cancel();
        isReconnectRunning = false;
    }

    class Timer extends CountDownTimer {
        TimerType type;

        public Timer(TimerType type) {
            this(type == TimerType.LINK ? LINK_INTERVAL : RECONECT_INTERVAL, INTERVAL);
            this.type = type;
        }

        public Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 时长,间隔
        }

        @Override
        public void onFinish() {
            switch (type) {
                case LINK:
                    isLinkRunning = false;
                    sendLink();
                    startLinkThread();
                    break;

                case RECONECT:
                    isReconnectRunning = false;
                    restartApp();
                    break;
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {

            switch (type) {
                case LINK:
                    isLinkRunning = true;
                    sendLink();
                    break;

                case RECONECT:
                    isReconnectRunning = true;
                    reConnectClient();
                    break;
            }
        }
    }

    private void restartApp() {
        //AppUtils.re
    }

    /**
     * 设置心跳是否Running
     *
     * @param available
     */
    public void setAvailable(boolean available) {
        if (isAvailable == available) return;
        if (available) {
            startLinkThread();
        } else {
            stopLinkThread();
        }
        isAvailable = available;
    }

    public boolean isAlive() {
        return isAvailable;
    }
}

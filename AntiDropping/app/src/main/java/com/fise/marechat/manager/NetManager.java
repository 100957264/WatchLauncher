package com.fise.marechat.manager;

import android.os.CountDownTimer;

import com.fise.marechat.KApplication;
import com.fise.marechat.utils.NetworkUtil;

/**
 * @author mare
 * @Description:
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/9
 * @time 9:34
 */
public class NetManager {
    private boolean isLinkRunning = false;
    private Timer timer;
    private boolean isNetAvailable;

    private static final int INTERVAL = 1000;
    private static final int LINK_INTERVAL = 10 * 60 * 1000;
    private NetManager() {
        timer = new Timer(LINK_INTERVAL,INTERVAL);
        start();
    }

    private static class SingletonHolder {
        private static final NetManager INSTANCE = new NetManager();
    }

    public static NetManager instance() {
        return SingletonHolder.INSTANCE;
    }

    public boolean isNetAvailable() {
        return isNetAvailable;
    }

    public void setNetAvailable(boolean netAvailable) {
        isNetAvailable = netAvailable;
    }

    private void start() {
        if (isLinkRunning) timer.cancel();
        timer.start();
        isLinkRunning = true;
    }

    class Timer extends CountDownTimer {

        public Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 时长,间隔
        }

        @Override
        public void onFinish() {
            isLinkRunning = false;
            setNetAvailable(NetworkUtil.isNetworkAvailable(KApplication.sContext));
            start();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            isLinkRunning = true;
            setNetAvailable(NetworkUtil.isNetworkAvailable(KApplication.sContext));
        }
    }
}


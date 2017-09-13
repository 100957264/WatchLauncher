package com.fise.xiaoyu.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.fise.xiaoyu.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xiejianghong on 2017/8/30.
 */

public class TextSwitchView extends TextSwitcher implements ViewSwitcher.ViewFactory {
    private int index = -1;
    private Context context;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    index = next(); //取得下标值
                    updateText();  //更新TextSwitcherd显示内容;
                    break;
            }
        }
    };
    private String[] resources = {};
    private Timer timer;

    public TextSwitchView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public TextSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        if (timer == null)
            timer = new Timer();
        this.setFactory(this);
        this.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.textswitcher_in_anim));
        this.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.textswitcher_out_anim));
    }

    public void setResources(String[] res) {
        if (res != null && res.length > 0) {
            this.resources = res;
        }
    }

    public void setTextStillTime(long time) {
        if (timer == null) {
            timer = new Timer();
        } else {
            timer.scheduleAtFixedRate(new MyTask(), 1, time);
        }
    }

    private class MyTask extends TimerTask {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(1);
        }
    }

    private int next() {
        int flag = index + 1;
        if (flag > resources.length - 1) {
            flag = 0;
        }
        return flag;
    }

    private void updateText() {
        if (index >= 0 && index < resources.length) {
            this.setText(resources[index]);
        }
    }

    @Override
    public View makeView() {
        TextView tv = new TextView(context);
        tv.setTextColor(getResources().getColor(R.color.black));
        return tv;
    }
}

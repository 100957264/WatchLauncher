package com.fise.xiaoyu.imservice.service;


import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.app.IMApplication;
import com.fise.xiaoyu.ui.activity.MainActivity;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by qingfeng on 2017/9/1.
 */

public class AlertReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_FLAG = 1;
    public static final String EVENT_ACTION_LOVE_ALERT = "com.fise.xiaoyu.ACTION_LOVE_ALERT";
    public static final String EVENT_ACTION_FORBID_EBALE = "com.fise.xiaoyu.EVENT_ACTION_FORBID_EBALE";
    public static final String EVENT_ACTION_FORBID_DISABLE = "com.fise.xiaoyu.EVENT_ACTION_FORBID_DISABLE";
    Dialog dialog = null;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(EVENT_ACTION_LOVE_ALERT)) {
            Bundle bundle = intent.getExtras();
            String loveName = bundle.getString("love");
            ArrayList week = bundle.getCharSequenceArrayList("weeklist");
            if (isTodaySelected(week)) {
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent targetIntent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, targetIntent, 0);
                Notification loveAlert = new Notification.Builder(context)
                        .setSmallIcon(R.drawable.icon)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setTicker(loveName)
                        .setContentText(loveName)
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .build();
                mNotificationManager.notify(NOTIFICATION_FLAG, loveAlert);
            }
        } else if(action.equals(EVENT_ACTION_FORBID_EBALE)){
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                ArrayList week = bundle.getCharSequenceArrayList("forbid_week");
                if (isTodaySelected(week)) {
                    Intent enableIntent = new Intent("fise.intent.action.FORBID_ENABLE");
                    context.sendBroadcast(enableIntent);
                    showForbidDialog(context);
                }
            }
        }else if(action.equals(EVENT_ACTION_FORBID_DISABLE)){
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                ArrayList week = bundle.getCharSequenceArrayList("forbid_week");
                if (isTodaySelected(week)) {
                    Intent disableIntent = new Intent("fise.intent.action.FORBID_DISABLE");
                    context.sendBroadcast(disableIntent);
                    if(dialog != null){
                        dialog.dismiss();
                    }
                }
            }
        }
    }

    /**
     * 判断今天是否被选择
     */
    public boolean isTodaySelected(ArrayList week) {
        Calendar mCalendar = Calendar.getInstance();
        int today = mCalendar.get(Calendar.DAY_OF_WEEK);
        if (week != null && week.contains(today)) {
            return true;
        }
        return false;
    }
    /**
     * 顶层dialog，周围区域透明，无法点击消失
     * */
    public void showForbidDialog(Context context){
        dialog = new AlertDialog.Builder(IMApplication.getApplication(),R.style.TransparentWindowBg)
                .setView(new ForbidView(context))
                .create();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        dialog.setCancelable(false);
        dialog.show();
    }
    class ForbidView extends View {
        Paint mPaint;
        int width  = getWidth();
        int height = getHeight();
        public ForbidView(Context context) {
            super(context);
            mPaint  = new Paint();
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            mPaint.setColor(Color.RED);
            mPaint.setTextSize(24);
            canvas.translate(width/2,height/2);
            canvas.drawText("禁用时段",width/2,height/2,mPaint);
        }
    }
}

package android.fise.com.fiseassitant;

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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by qingfeng on 2017/9/8.
 */

public class AlertReceiver extends BroadcastReceiver{

        private static final int NOTIFICATION_FLAG = 1;
        public static final String EVENT_ACTION_LOVE_ALERT = "com.fise.xiaoyu.ACTION_LOVE_ALERT";
        public static final String EVENT_ACTION_FORBID_EBALE = "com.fise.xiaoyu.EVENT_ACTION_FORBID_EBALE";
        public static final String EVENT_ACTION_FORBID_DISABLE = "com.fise.xiaoyu.EVENT_ACTION_FORBID_DISABLE";
        Dialog dialog = null;
        Display display ;
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("fengqing","onReceive. action=" + action);
            Toast.makeText(context,"闹钟妖孽，还不现形，更待何时？",Toast.LENGTH_LONG).show();
            if (action.equals(EVENT_ACTION_LOVE_ALERT)) {
               // Bundle bundle = intent.getExtras();
               // String loveName = bundle.getString("love");
                //ArrayList week = bundle.getCharSequenceArrayList("weeklist");
               // if (isTodaySelected(week)) {
                    NotificationManager mNotificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    Intent targetIntent = new Intent(context, ShutdownRebootHelperActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, targetIntent, 0);
                    Notification loveAlert = new Notification.Builder(context)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setTicker("爱心提醒哈哈哈")
                            .setContentText("爱心提醒哈哈哈")
                            .setContentIntent(pendingIntent)
                            .setPriority(Notification.PRIORITY_DEFAULT)
                            .build();
                    mNotificationManager.notify(NOTIFICATION_FLAG, loveAlert);
               // }
            } else if(action.equals(EVENT_ACTION_FORBID_EBALE)){
               // Bundle bundle = intent.getExtras();
                //if(bundle != null) {
                   // ArrayList week = bundle.getCharSequenceArrayList("forbid_week");
                   // if (isTodaySelected(week)) {
                       // Intent enableIntent = new Intent("fise.intent.action.FORBID_ENABLE");
                       // context.sendBroadcast(enableIntent);
                        showForbidDialog(context);
                  //  }
                //}
            }else if(action.equals(EVENT_ACTION_FORBID_DISABLE)){
               // Bundle bundle = intent.getExtras();
               // if(bundle != null) {
                   // ArrayList week = bundle.getCharSequenceArrayList("forbid_week");
                   // if (isTodaySelected(week)) {
                        //Intent disableIntent = new Intent("fise.intent.action.FORBID_DISABLE");
                       // context.sendBroadcast(disableIntent);
                        if(dialog != null){
                            dialog.dismiss();
                        }
                   // }
                //}
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
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();
        dialog = new AlertDialog.Builder(context,R.style.TransparentWindowBg)
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
        int width =720;
        int height = 1280;
        public ForbidView(Context context) {
            super(context);
            width = display.getWidth();
            height = display.getHeight();
            mPaint  = new Paint();
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            mPaint.setColor(Color.RED);
            mPaint.setTextSize(48);
            canvas.translate(width/2,height/2);
            canvas.drawText("禁用时段",width/2,height/2,mPaint);
        }
    }
}

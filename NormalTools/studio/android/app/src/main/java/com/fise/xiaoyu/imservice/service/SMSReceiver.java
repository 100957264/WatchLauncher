package com.fise.xiaoyu.imservice.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.app.IMApplication;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.manager.IMDeviceManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xiejianghong on 2017/9/1.
 * 短信广播接收器，接收话费余额短信并发送余额到雨友群
 */

public class SMSReceiver extends BroadcastReceiver {
    public static final String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    public static final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    public static final String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";

    private boolean receivedAble = false;
    private boolean sendBySelf = false;
    private Timer timer;
    private TimerTask timerTask;


    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case SMS_ACTION:
                if (receivedAble) {
                    UserEntity loginInfo = IMLoginManager.instance().getLoginInfo();
                    if (loginInfo != null && Utils.isClientType(loginInfo)) {
                        onSmsReceive(context, intent);
                    }
                }
                break;
            case SENT_SMS_ACTION:
                onSmsSendState(context, intent);
                break;
            case DELIVERED_SMS_ACTION:
                onSmsReceiveState(context, intent);
                break;
        }
    }

    private void onSmsReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] myOBJpdus = (Object[]) bundle.get("pdus");
            String format = bundle.getString("format");
            SmsMessage[] messages = new SmsMessage[myOBJpdus.length];
            for (int i = 0; i < myOBJpdus.length; i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) myOBJpdus[i], format);
                } else {
                    messages[i] = SmsMessage.createFromPdu((byte[]) myOBJpdus[i]);
                }
            }
            StringBuilder body = new StringBuilder();
            String number = "";
            String regx = null;
            for (SmsMessage currentMessage : messages) {
                number = currentMessage.getDisplayOriginatingAddress();
                body.append(currentMessage.getDisplayMessageBody());
            }


            String sms_phone = null;
            //短信验证码
            if (body.toString().indexOf(DBConstant.SMS_NUMBER) != -1) {
                sms_phone = "验证码[^\n\\d.]*?(-?[\\d.]+?)[^\n\\d.]*?如";
                Pattern pattern = Pattern.compile(sms_phone);
                Matcher matcher = pattern.matcher(body.toString());
                if (matcher.find()) {
                    String sms = matcher.group(1);
                    IMDeviceManager.instance().verificationCode(sms);
                    if (sendBySelf) {
                        closeSmsBySelf();
                        markMessageRead(context, number, body.toString());
                    }
                }

            } else {

                switch (IMApplication.getBillType()) {
                    case DBConstant.IMSI_TYPE_MOBILE:
                        if (!number.equals("10086")) {
                            return;
                        }
                        regx = "余额[^\n\\d.]*?(-?[\\d.]+?)[^\n\\d.]*?元";
                        break;
                    case DBConstant.IMSI_TYPE_UNICOM:
                        if (!number.equals("10010")) {
                            return;
                        }
                        regx = "余额[^\n\\d.]*?(-?[\\d.]+?)[^\n\\d.]*?元";
                        break;
                    case DBConstant.IMSI_TYPE_TELECOM:
                        if (!number.equals("10000")) {
                            return;
                        }
                        regx = "余额[^\n\\d.]*?(-?[\\d.]+?)[^\n\\d.]*?元";
                        break;
                    default:
                        return;
                }

                Pattern pattern = Pattern.compile(regx);
                Matcher matcher = pattern.matcher(body.toString());
                if (matcher.find()) {
                    String balance = matcher.group(1);
                    IMDeviceManager.instance().postionPublishEvent(balance);
                    if (sendBySelf) {
                        closeSmsBySelf();
                        markMessageRead(context, number, body.toString());
                    }
                }
            }

        }
    }

    //处理返回的发送状态
    private void onSmsSendState(Context context, Intent intent) {
        switch (getResultCode()) {
            case Activity.RESULT_OK: {
                waitSmsBySelf();
            }
            break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                break;
        }
    }

    //处理返回的对方接收状态
    private void onSmsReceiveState(Context context, Intent intent) {

    }

    private void markMessageRead(Context context, String number, String body) {
        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        try {
            while (cursor != null && cursor.moveToFirst()) {
                if ((cursor.getString(cursor.getColumnIndex("address")).equals(number)) && (cursor.getString(cursor.getColumnIndex("body")).startsWith(body))) {
                    if (cursor.getInt(cursor.getColumnIndex("read")) == 0) {
                        String SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                        ContentValues values = new ContentValues();
                        values.put("read", "1");
                        context.getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + SmsMessageId, null);
                    }
                    return;
                }
            }
        } catch (Exception e) {
            Log.e("Mark Read", "Error in Read: " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void setReceivedAble(boolean receivedAble) {
        this.receivedAble = receivedAble;
    }

    /**
     * 如果短信是主动发送的，在60s内收到回信则设为已读
     */
    private void waitSmsBySelf() {
        if (timer == null) {
            timer = new Timer();
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        sendBySelf = true;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                sendBySelf = false;
            }
        };
        timer.schedule(timerTask, 30 * 1000);
    }

    private void closeSmsBySelf() {
        if (timer != null && timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        sendBySelf = false;
    }
}

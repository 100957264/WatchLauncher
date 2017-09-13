package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fise.xiaoyu.DB.sp.LoginSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.UrlConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMSms;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.HttpUtil;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 *
 */
@SuppressLint("NewApi")
public class InputTelActivity extends TTBaseActivity {

    private Logger logger = Logger.getLogger(InputTelActivity.class);
    private Handler uiHandler = new Handler();
    private IMService imService;
    public String error_msg;
    public final int data = 303;
    public String imei;
    private String phone = "";
    private EditText input_tel_text;

    public final int OUTTIME = 101;

    Handler smsHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    Intent intent = new Intent(InputTelActivity.this, VerificationSmsActivity.class);
                    intent.putExtra(IntentConstant.KEY_REGIST_NAME, phone);
                    InputTelActivity.this.startActivity(intent);
                    finish();
                }
                break;
                case 1: {
                    Utils.showToast(InputTelActivity.this, "" + error_msg);
                }
                break;
                case OUTTIME: {
                    Utils.showToast(InputTelActivity.this, "网络连接失败");
                }
                break;
                default: {
                    Utils.showToast(InputTelActivity.this, "" + error_msg);
                }
                break;
            }
            super.handleMessage(msg);
        }
    };


    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            logger.d("login#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            try {
                do {
                    if (imService == null) {
                        // 后台服务启动链接失败
                        break;
                    }
                    IMLoginManager loginManager = imService.getLoginManager();
                    LoginSp loginSp = imService.getLoginSp();
                    if (loginManager == null || loginSp == null) {
                        // 无法获取登陆控制器
                        break;
                    }

                    TextView replace_tel_text = (TextView) findViewById(R.id.replace_tel_text);
                    replace_tel_text.setText("当前手机号: " + loginManager.getLoginInfo().getPhone());

                    return;
                } while (false);

                // 异常分支都会执行这个
                // handleNoLoginIdentity();
            } catch (Exception e) {
                // 任何未知的异常
                logger.w("loadIdentity failed");
                // handleNoLoginIdentity();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        imServiceConnector.connect(InputTelActivity.this);

        setContentView(R.layout.tt_activity_input_tel);
        Button icon_arrow = (Button) findViewById(R.id.icon_arrow_tel);
        icon_arrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(InputTelActivity.this, AccountSecurityActivity.class));
                InputTelActivity.this.finish();
            }
        });

        TextView left_text = (TextView) findViewById(R.id.left_text_tel);
        left_text.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(InputTelActivity.this, AccountSecurityActivity.class));
                InputTelActivity.this.finish();
            }
        });

        input_tel_text = (EditText) findViewById(R.id.input_tel_text);

        TextView right_text = (TextView) findViewById(R.id.right_text);
        right_text.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                String phoneStr = input_tel_text.getText().toString();
                boolean isPhone = Utils.isMobileNO(phoneStr);
                if (isPhone == false) {
                    Utils.showToast(InputTelActivity.this,
                            "输入的号码不正确");
                } else {
                    phone = phoneStr;
                    //postHttp( phoneStr,IMSms.SmsActionType.SMS_ACTION_MODIFY_PHONE);
                    postSendSmS(phoneStr, IMSms.SmsActionType.SMS_ACTION_MODIFY_PHONE);
                }
            }
        });

        TelephonyManager tm = (TelephonyManager) this
                .getSystemService(TELEPHONY_SERVICE);
        imei = tm.getDeviceId();

        if (imei == null) {
            imei = "";
        }

    }


    void postSendSmS(final String phone, final IMSms.SmsActionType type) {

        new Thread() {
            @Override
            public void run() {
                String url = UrlConstant.ACCESS_MSG_SEND_SMS;
                OkHttpClient okHttpClient = new OkHttpClient();
                try {
                    String md5Imei = new String(Security.getInstance().EncryptPass(imei + "fise_zn_xw@fise.com.cn"));
                    JSONObject param = new JSONObject();
                    param.put("action", type.ordinal());
                    param.put("mobile", phone);
                    param.put("app_dev", imei);
                    param.put("app_key", md5Imei);
                    RequestBody requestBody = RequestBody.create(HttpUtil.JSON, param.toString());
                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                    Response response = okHttpClient.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        JSONObject json = new JSONObject(result);
                        String code = json.getString("error_code");
                        error_msg = json.getString("error_msg");

                        Message message = new Message();
                        message.what = Integer.valueOf(code);
                        InputTelActivity.this.smsHandler.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = OUTTIME;
                    InputTelActivity.this.smsHandler
                            .sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = OUTTIME;
                    InputTelActivity.this.smsHandler
                            .sendMessage(message);
                }
            }
        }.start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(InputTelActivity.this);
    }

    // 为什么会有两个这个
    // 可能是 兼容性的问题 导致两种方法onBackPressed
    @Override
    public void onBackPressed() {
        logger.d("login#onBackPressed");
        // imLoginMgr.cancel();
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * ----------------------------event 事件驱动----------------------------
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginEvent event) {
        switch (event) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
                break;

        }
    }

}

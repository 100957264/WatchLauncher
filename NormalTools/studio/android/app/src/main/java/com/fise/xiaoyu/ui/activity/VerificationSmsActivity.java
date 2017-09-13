package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.LoginSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.UrlConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.ui.base.ActivityManager;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMSms;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.HttpUtil;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

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
public class VerificationSmsActivity extends TTBaseActivity {


    private Logger logger = Logger.getLogger(VerificationSmsActivity.class);
    private Handler uiHandler = new Handler();
    private IMService imService;
    private String err_code;
    public String imei;
    private String phoneName;
    private EditText xiaowei_sms_text;


    Handler smsHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    UserEntity user = IMLoginManager.instance().getLoginInfo();
                    String data = "" + phoneName;
                    IMContactManager.instance().ChangeUserInfo(
                            user.getPeerId(),
                            IMBaseDefine.ChangeDataType.CHANGE_USERINFO_PHONE, data); //CHANGE_USERINFO_DOMAIN
                }
                break;
                case 1: {
                    Utils.showToast(VerificationSmsActivity.this, "输入验证码不对");
                }
                break;
                default: {
                    Utils.showToast(VerificationSmsActivity.this, "" + err_code);
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

        imServiceConnector.connect(VerificationSmsActivity.this);
        setContentView(R.layout.tt_activity_verification_sms);

        TelephonyManager telephonyManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        try {
            imei = telephonyManager.getDeviceId();
        } catch (Exception e) {
            imei = "";
        }


        phoneName = VerificationSmsActivity.this.getIntent().getStringExtra(
                IntentConstant.KEY_REGIST_NAME);

        Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
        icon_arrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(VerificationSmsActivity.this, AccountSecurityActivity.class));
                VerificationSmsActivity.this.finish();
            }
        });

        TextView left_text = (TextView) findViewById(R.id.left_text);
        left_text.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(VerificationSmsActivity.this, AccountSecurityActivity.class));
                VerificationSmsActivity.this.finish();
            }
        });

        Button next_button = (Button) findViewById(R.id.next_button);

        xiaowei_sms_text = (EditText) findViewById(R.id.xiaowei_sms_text);
        next_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (xiaowei_sms_text.getText().toString() != null) {
                    postHttp(phoneName, xiaowei_sms_text.getText().toString());
                } else {
                    Utils.showToast(VerificationSmsActivity.this, "请输入验证码");
                }


            }
        });
    }


    void postHttp(final String phone, final String auth_code) {

        new Thread() {
            @Override
            public void run() {

                String url = UrlConstant.ACCESS_MSG_ACTION_VERIFY;
                OkHttpClient okHttpClient = new OkHttpClient();
                try {
                    String md5Imei = new String(Security.getInstance().EncryptPass(imei + "fise_zn_xw@fise.com.cn"));
                    JSONObject param = new JSONObject();
                    param.put("mobile", phone);
                    param.put("app_dev", imei);
                    param.put("app_key", md5Imei);
                    param.put("action",
                            IMSms.SmsActionType.SMS_ACTION_MODIFY_PHONE.ordinal());
                    param.put("auth_code", auth_code);
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
                        err_code = json.getString("error_msg");

                        Message message = new Message();
                        message.what = Integer.valueOf(code);
                        VerificationSmsActivity.this.smsHandler
                                .sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(VerificationSmsActivity.this);
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


    /**
     * ----------------------------event 事件驱动----------------------------
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event) {
        switch (event) {

            case USER_INFO_DATA_UPDATE_PHONE: {

                ActivityManager.getInstance().finishActivity(InputTelActivity.class);
                ActivityManager.getInstance().finishActivity(VerificationPassWordActivity.class);
                imService.getLoginManager().updateLoginUserName();
                Utils.showToast(VerificationSmsActivity.this, "手机号码修改成功");
                VerificationSmsActivity.this.finish();
            }
            break;
            case USER_INFO_DATA_FAIL: {
                Utils.showToast(VerificationSmsActivity.this, "数据失败");

            }
            break;

        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeviceEvent event) {
        switch (event) {
            case DEVICE_VERIFICATION_CODE: {
                xiaowei_sms_text.setText(imService.getDeviceManager().getVerificationCode());
            }
            break;
        }
    }
}

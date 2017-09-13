package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.fise.xiaoyu.DB.sp.LoginSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 *
 */
@SuppressLint("NewApi")
public class UpdateTelActivity extends TTBaseActivity {

    private TextView tel_phone_text;
    private Logger logger = Logger.getLogger(UpdateTelActivity.class);
    private Handler uiHandler = new Handler();
    private IMService imService;

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
                    tel_phone_text.setText("手机号:" + loginManager.getLoginInfo().getPhone());


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

        imServiceConnector.connect(UpdateTelActivity.this);

        setContentView(R.layout.tt_activity_update_tel);
        Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
        icon_arrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                UpdateTelActivity.this.finish();
            }
        });

        TextView left_text = (TextView) findViewById(R.id.left_text);
        left_text.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        UpdateTelActivity.this.finish();
                    }
                });


        tel_phone_text = (TextView) findViewById(R.id.tel_phone_text);

        Button button_change_number = (Button) findViewById(R.id.button_change_number);
        button_change_number.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (imService != null && Utils.isClientType(imService.getLoginManager().getLoginInfo())) {
                    Intent intent = new Intent(UpdateTelActivity.this, InputTelActivity.class);
                    UpdateTelActivity.this.startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(UpdateTelActivity.this, VerificationPassWordActivity.class);
                    UpdateTelActivity.this.startActivity(intent);
                    finish();
                }

            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(UpdateTelActivity.this);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event) {
        switch (event) {

            case USER_INFO_DATA_UPDATE_PHONE: {
                tel_phone_text.setText("手机号:" + imService.getLoginManager().getLoginInfo().getPhone());

            }
            break;
        }
    }

}

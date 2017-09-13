package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * 消息请求界面
 */
@SuppressLint("NewApi")
public class ReqYuMessageActivity extends TTBaseActivity {

    private Logger logger = Logger.getLogger(ReqYuMessageActivity.class);
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


                    TextView contact_req_yu_title = (TextView) findViewById(R.id.contact_req_yu_title);


                    int unReqNum = imService.getUnReadMsgManager()
                            .getTotalReqYuFriendsCount();

                    if (unReqNum > 0) {
                        contact_req_yu_title.setVisibility(View.VISIBLE);
                        contact_req_yu_title.setText("" + unReqNum);
                    } else {
                        contact_req_yu_title.setVisibility(View.GONE);
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

        imServiceConnector.connect(ReqYuMessageActivity.this);

        setContentView(R.layout.tt_activity_yu_message);
        Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
        icon_arrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ReqYuMessageActivity.this.finish();
            }
        });

        TextView left_text = (TextView) findViewById(R.id.left_text);
        left_text.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ReqYuMessageActivity.this.finish();
            }
        });


        RelativeLayout message_request = (RelativeLayout) findViewById(R.id.message_request);
        message_request.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                IMUIHelper.openYuMessageListActivity(ReqYuMessageActivity.this);

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(ReqYuMessageActivity.this);
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

            case USER_INFO_REQ_YU:
            case USER_INFO_REQ_UPDATE: {
                TextView contact_req_yu_title = (TextView) findViewById(R.id.contact_req_yu_title);
                int unReqNum = imService.getUnReadMsgManager()
                        .getTotalReqYuFriendsCount();

                if (unReqNum > 0) {
                    contact_req_yu_title.setVisibility(View.VISIBLE);
                    contact_req_yu_title.setText("" + unReqNum);
                } else {
                    contact_req_yu_title.setVisibility(View.GONE);
                }
            }
            break;

        }
    }


}

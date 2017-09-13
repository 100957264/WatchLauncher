package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.sp.LoginSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * 显示设置
 */
@SuppressLint("NewApi") public class ShowSettingActivity extends TTBaseActivity {

    private Logger logger = Logger.getLogger(ShowSettingActivity.class);
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
                        //后台服务启动链接失败
                        break;
                    }
                    IMLoginManager loginManager = imService.getLoginManager();
                    LoginSp loginSp = imService.getLoginSp();
                    if (loginManager == null || loginSp == null) {
                        // 无法获取登陆控制器
                        break;
                    }

                    LoginSp.SpLoginIdentity loginIdentity = loginSp.getLoginIdentity();
                    if (loginIdentity == null) {
                        // 之前没有保存任何登陆相关的，跳转到登陆页面
                        break;
                    }



                    return;
                } while (false);

                // 异常分支都会执行这个
              //  handleNoLoginIdentity();
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

        imServiceConnector.connect(ShowSettingActivity.this);

        setContentView(R.layout.tt_activity_setting);
   
        
        
        RelativeLayout account_security = (RelativeLayout)findViewById(R.id.account_security);
        account_security.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
				IMUIHelper.openAccountSecurityActivity(ShowSettingActivity.this);
			}
		});
        
        
        Button icon_arrow = (Button)findViewById(R.id.icon_arrow);
        icon_arrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
				ShowSettingActivity.this.finish();
			}
		});
        
        TextView left_text = (TextView)findViewById(R.id.left_text);
        left_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
				ShowSettingActivity.this.finish();
			}
		});
        
        
        
        RelativeLayout new_message = (RelativeLayout)findViewById(R.id.new_message_Page);
        new_message.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
				IMUIHelper.openMessageNotifyActivity(ShowSettingActivity.this);
			}
		});
        
        
        RelativeLayout currency = (RelativeLayout)findViewById(R.id.currencyPage);
        
        currency.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
				IMUIHelper.openCurrencyActivity(ShowSettingActivity.this);
			}
		});
        
        
        RelativeLayout privacyPage = (RelativeLayout)findViewById(R.id.privacyPage);
        
        privacyPage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
				IMUIHelper.openPrivacyActivity(ShowSettingActivity.this);
			}
		});
        
        

        RelativeLayout aboutPage = (RelativeLayout)findViewById(R.id.aboutPage);
        
        aboutPage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
				IMUIHelper.openAboutActivity(ShowSettingActivity.this);
			}
		});

        RelativeLayout exitPage = (RelativeLayout)findViewById(R.id.exitPage);

        exitPage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

                final FilletDialog myDialog = new FilletDialog(ShowSettingActivity.this ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE);
                myDialog.setTitle(ShowSettingActivity.this.getString(R.string.exit_teamtalk_tip));//设置内容
                myDialog.dialog.show();//显示

                //确认按键回调，按下确认后在此做处理
                myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
                    @Override
                    public void ok() {

                        IMLoginManager.instance().setKickout(false);
                        IMLoginManager.instance().logOut();
                        ShowSettingActivity.this.finish();
                        myDialog.dialog.dismiss();
                    }
                });

			}
		});
        

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(ShowSettingActivity.this);
    }


 

    // 为什么会有两个这个
    // 可能是 兼容性的问题 导致两种方法onBackPressed
    @Override
    public void onBackPressed() {
        logger.d("login#onBackPressed");
        //imLoginMgr.cancel();
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
        }
    }

}

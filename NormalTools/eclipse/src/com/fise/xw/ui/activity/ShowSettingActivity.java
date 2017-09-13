package com.fise.xw.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.DB.sp.LoginSp;
import com.fise.xw.DB.sp.SystemConfigSp;
import com.fise.xw.R;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.UrlConstant;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.event.SocketEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;


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
        EventBus.getDefault().register(this);
        
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
		        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ShowSettingActivity.this, android.R.style.Theme_Holo_Light_Dialog));
	              LayoutInflater inflater = (LayoutInflater)ShowSettingActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	              View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
	              final EditText editText = (EditText)dialog_view.findViewById(R.id.dialog_edit_content);
	              editText.setVisibility(View.GONE);
	              TextView textText = (TextView)dialog_view.findViewById(R.id.dialog_title);
	              textText.setText(R.string.exit_teamtalk_tip);
	              builder.setView(dialog_view);
	              builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {

	                  @Override
	                  public void onClick(DialogInterface dialog, int which) {
	                      IMLoginManager.instance().setKickout(false);
	                      IMLoginManager.instance().logOut();
	                      ShowSettingActivity.this.finish();
	                      dialog.dismiss();
	                  }
	              });

	              builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
	                  @Override
	                  public void onClick(DialogInterface dialogInterface, int i) {
	                     dialogInterface.dismiss();
	                  }
	              });
	              builder.show();
			}
		});
        
        
         
           
//      Button button_logout = (Button)findViewById(R.id.button_logout);
//        
//      button_logout.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View arg0) { 
//		        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ShowSettingActivity.this, android.R.style.Theme_Holo_Light_Dialog));
//	              LayoutInflater inflater = (LayoutInflater)ShowSettingActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//	              View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
//	              final EditText editText = (EditText)dialog_view.findViewById(R.id.dialog_edit_content);
//	              editText.setVisibility(View.GONE);
//	              TextView textText = (TextView)dialog_view.findViewById(R.id.dialog_title);
//	              textText.setText(R.string.exit_teamtalk_tip);
//	              builder.setView(dialog_view);
//	              builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {
//
//	                  @Override
//	                  public void onClick(DialogInterface dialog, int which) {
//	                      IMLoginManager.instance().setKickout(false);
//	                      IMLoginManager.instance().logOut();
//	                      ShowSettingActivity.this.finish();
//	                      dialog.dismiss();
//	                  }
//	              });
//
//	              builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
//	                  @Override
//	                  public void onClick(DialogInterface dialogInterface, int i) {
//	                     dialogInterface.dismiss();
//	                  }
//	              });
//	              builder.show();
//			}
//		});
        
        
        
        
        
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(ShowSettingActivity.this);
        EventBus.getDefault().unregister(this); 
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
    public void onEventMainThread(LoginEvent event) {
        switch (event) { 
        }
    }

}

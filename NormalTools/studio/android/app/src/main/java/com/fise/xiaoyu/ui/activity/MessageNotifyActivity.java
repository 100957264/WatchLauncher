package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.fise.xiaoyu.DB.sp.ConfigurationSp;
import com.fise.xiaoyu.DB.sp.LoginSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.helper.CheckboxConfigHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.NotificationsUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * 消息通知界面
 */
@SuppressLint("NewApi") public class MessageNotifyActivity extends TTBaseActivity {

    private Logger logger = Logger.getLogger(MessageNotifyActivity.class);
    private Handler uiHandler = new Handler(); 
    private IMService imService; 
    CheckboxConfigHelper checkBoxConfiger = new CheckboxConfigHelper();
    private CheckBox notificationNoDisturbCheckBox;
    private CheckBox notificationGotSoundCheckBox;
    private CheckBox notificationGotVibrationCheckBox;
    
    
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

                    checkBoxConfiger.init(imService.getConfigSp()); 
             
                	notificationNoDisturbCheckBox = (CheckBox) MessageNotifyActivity.this.findViewById(R.id.forbid_checkbox);
            		notificationGotSoundCheckBox = (CheckBox) MessageNotifyActivity.this.findViewById(R.id.sound_checkbox);
            		notificationGotVibrationCheckBox = (CheckBox) MessageNotifyActivity.this.findViewById(R.id.vibration_checkbox);
            		
//            		saveTrafficModeCheckBox = (CheckBox) curView.findViewById(R.id.saveTrafficCheckBox);
            		checkBoxConfiger.initCheckBox(notificationNoDisturbCheckBox, SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.NOTIFICATION );
            		checkBoxConfiger.initCheckBox(notificationGotSoundCheckBox, SysConstant.SETTING_GLOBAL , ConfigurationSp.CfgDimension.SOUND);
            		checkBoxConfiger.initCheckBox(notificationGotVibrationCheckBox, SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.VIBRATION );
                    
            		
            		
            		CheckBox checkbox = (CheckBox)findViewById(R.id.checkbox);
            		final SharedPreferences sp = MessageNotifyActivity.this.getSharedPreferences("Notification", Activity.MODE_PRIVATE);
            		
            		boolean Notification = sp.getBoolean("Bool_Notification", true);
            		checkbox.setChecked(Notification);
            		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                        
                        @Override
                        public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                            // TODO Auto-generated method stub
                        	Editor editor = sp.edit();
                        	editor.putBoolean("Bool_Notification", arg1);
                        	editor.commit();// 提交修改 
                        }
                    });
            		
            		
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

        imServiceConnector.connect(MessageNotifyActivity.this);

        setContentView(R.layout.tt_activity_message_notify);
   
        
        Button icon_arrow = (Button)findViewById(R.id.icon_arrow);
        icon_arrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
				MessageNotifyActivity.this.finish();
			}
		});
        
        TextView left_text = (TextView)findViewById(R.id.left_text);
        left_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
				MessageNotifyActivity.this.finish();
			}
		});
        
        
        TextView notice_text = (TextView)findViewById(R.id.notice_text);
        if(NotificationsUtils.isNotificationEnabled(this.getApplicationContext())){
        	notice_text.setText("已开启");
        }else{
        	notice_text.setText("已关闭");
        }
    	
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(MessageNotifyActivity.this);
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

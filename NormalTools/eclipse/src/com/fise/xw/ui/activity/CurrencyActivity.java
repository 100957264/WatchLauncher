package com.fise.xw.ui.activity;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.sp.LoginSp;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.entity.RecentInfo;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.manager.IMSessionManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.helper.AudioPlayerHandler;
import com.fise.xw.ui.widget.SpeekerToast;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;


/** 
 * 我的 中通用界面
 */
@SuppressLint("NewApi") public class CurrencyActivity extends TTBaseActivity {

    private Logger logger = Logger.getLogger(CurrencyActivity.class);
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

        imServiceConnector.connect(CurrencyActivity.this);
        EventBus.getDefault().register(this);
        
        setContentView(R.layout.tt_activity_currency);
   
        Button icon_arrow = (Button)findViewById(R.id.icon_arrow);
        icon_arrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
				CurrencyActivity.this.finish();
			}
		});
        
        TextView left_text = (TextView)findViewById(R.id.left_text);
        left_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
				CurrencyActivity.this.finish();
			}
		});
        
        RelativeLayout message_bg = (RelativeLayout) findViewById(R.id.message_bg);
		message_bg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				String curSessionKey = imService.getLoginManager().getLoginInfo().getSessionKey();
				 Intent intent = new
				 Intent(CurrencyActivity.this,
						 SettingMessageBgActivity.class);
				 intent.putExtra(IntentConstant.KEY_SESSION_KEY,  curSessionKey);
				 intent.putExtra(IntentConstant.KEY_ALL_MESSAGE_BG,  true); 
				 CurrencyActivity.this.startActivity(intent);
			}
		});
         
		
		
		
        RelativeLayout typeface_daxiao = (RelativeLayout) findViewById(R.id.typeface_daxiao);
        typeface_daxiao.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				 
				 Intent intent = new
				 Intent(CurrencyActivity.this,
						 SettingTypefaceActivity.class); 
				 CurrencyActivity.this.startActivity(intent);
			}
		});
        
        
        RelativeLayout clear_space = (RelativeLayout)findViewById(R.id.clear_space);
        clear_space.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
					// TODO Auto-generated method stub 
            	
            	//final AlertDialog.Builder normalDialog = 
                  //      new AlertDialog.Builder(CurrencyActivity.this);
				
				final AlertDialog.Builder normalDialog = new AlertDialog.Builder(new ContextThemeWrapper(
						CurrencyActivity.this, android.R.style.Theme_Holo_Light_Dialog)); //
				
                   // normalDialog.setIcon(R.drawable.icon_dialog);
                    //normalDialog.setTitle("我是一个普通Dialog");
                    normalDialog.setMessage("整理小位空间");
                    normalDialog.setPositiveButton("确定", 
                        new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //...To-do
                        	imService.getMessageManager().deleteMessageAll();//curSessionKey 
                   		 	List<RecentInfo> recentSessionList = imService.getSessionManager().getRecentListInfo(); 
                   		 	
								for (int i = 0; i < recentSessionList.size(); i++) {
									imService.getSessionManager()
											.reqRemoveSession(
													recentSessionList.get(i),DBConstant.SESSION_ALL);
								}
								imService.getSessionManager()
										.removeRecentSessionList(); 
								
								
								 File file = new  File(Environment.getExternalStorageDirectory() +"/" + "fise");
								RecursionDeleteFile(file);
								
								Toast.makeText(CurrencyActivity.this, "整理小位空间成功",
									     Toast.LENGTH_SHORT).show();
                   		 
                        }
                    });
                    normalDialog.setNegativeButton("取消", 
                        new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //...To-do
                        	 
                        }
                    });
                    // 显示
                    normalDialog.show();
			}
		});
        
        
        
        Button clear_record = (Button)findViewById(R.id.clear_record);
        clear_record.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
 // TODO Auto-generated method stub 
            	
            //	final AlertDialog.Builder normalDialog = 
                   //     new AlertDialog.Builder(CurrencyActivity.this);
				final AlertDialog.Builder normalDialog = new AlertDialog.Builder(new ContextThemeWrapper(
						CurrencyActivity.this, android.R.style.Theme_Holo_Light_Dialog));
				
                   // normalDialog.setIcon(R.drawable.icon_dialog);
                    //normalDialog.setTitle("我是一个普通Dialog");
                    normalDialog.setMessage("确定删除全部聊天记录");
                    normalDialog.setPositiveButton("清空", 
                        new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        	
                        	UserEntity user = imService.getLoginManager().getLoginInfo();
                        	String session = user.getSessionKey();

								IMSessionManager.instance().reqRemoveSessionAll(user,
										DBConstant.SESSION_MESSAGE_ALL);
								
								Toast.makeText(CurrencyActivity.this, "清除聊天记录成功",
									     Toast.LENGTH_SHORT).show();
                   		 
                        }
                    }); 
                    normalDialog.setNegativeButton("取消", 
                        new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //...To-do
                        	 
                        }
                    });
                    // 显示
                    normalDialog.show();
			}
		});
        
        
        
      //获取CheckBox实例
        CheckBox cb = (CheckBox)this.findViewById(R.id.vibration_checkbox);
        SharedPreferences   user = CurrencyActivity.this.getSharedPreferences("SP", Activity.MODE_PRIVATE);
        boolean speaker = user.getBoolean("speaker", true);
        cb.setChecked(speaker);
        
        //绑定监听器
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub 
                    	AudioPlayerHandler audioPlayerHandler = AudioPlayerHandler.getInstance();
                    	if(arg1)
                    	{
                            audioPlayerHandler.setAudioMode(AudioManager.MODE_IN_CALL, CurrencyActivity.this,arg1);
                            SpeekerToast.show(CurrencyActivity.this, CurrencyActivity.this.getText(R.string.audio_in_call), Toast.LENGTH_SHORT);
                    	}else{
                            audioPlayerHandler.setAudioMode(AudioManager.MODE_NORMAL, CurrencyActivity.this,arg1);
                            SpeekerToast.show(CurrencyActivity.this, CurrencyActivity.this.getText(R.string.audio_in_speeker), Toast.LENGTH_SHORT);
                    	}
                    }
                });
        
    	
    }

    
    public static void RecursionDeleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(CurrencyActivity.this);
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
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
                break;
      
        }
    }

}

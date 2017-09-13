package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.LoginSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.manager.IMSessionManager;
import com.fise.xiaoyu.imservice.manager.IMUnreadMsgManager;
import com.fise.xiaoyu.imservice.manager.IMUserActionManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.helper.AudioPlayerHandler;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.ui.widget.SpeekerToast;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;


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

                Intent intent = new
                        Intent(CurrencyActivity.this,  ClearSpaceActivity.class);
                CurrencyActivity.this.startActivity(intent);

                /*
                final FilletDialog myDialog = new FilletDialog(CurrencyActivity.this ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE);
                myDialog.setTitle(getString(R.string.arrangement_space));//设置内容
                myDialog.dialog.show();//显示

                //确认按键回调，按下确认后在此做处理
                myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
                    @Override
                    public void ok() {
                        imService.getMessageManager().deleteMessageAll();//curSessionKey
                        List<RecentInfo> recentSessionList = imService.getSessionManager().getRecentListInfo();

                        for (int i = 0; i < recentSessionList.size(); i++) {
                            imService.getSessionManager()
                                    .reqRemoveSession(recentSessionList.get(i),DBConstant.SESSION_ALL);
                        }


                        // imService.getSessionManager().removeRecentSessionList();
                        File file = new  File(Environment.getExternalStorageDirectory() +"/" + "fise");
                        RecursionDeleteFile(file);

                        Utils.showToast(CurrencyActivity.this, "整理小位空间成功");

                        myDialog.dialog.dismiss();
                    }
                });
                */

			}
		});
        
        
        
        Button clear_record = (Button)findViewById(R.id.clear_record);
        clear_record.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) { 
            // TODO Auto-generated method stub

                final FilletDialog myDialog = new FilletDialog(CurrencyActivity.this ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE);
                myDialog.setTitle(getString(R.string.delete_chatting_records_notice));//设置内容
                myDialog.dialog.show();//显示

                //确认按键回调，按下确认后在此做处理
                myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
                    @Override
                    public void ok() {
                     	UserEntity user = imService.getLoginManager().getLoginInfo();
                        IMSessionManager.instance().reqRemoveSessionAll(user,
										DBConstant.SESSION_MESSAGE_ALL);

                        IMUnreadMsgManager.instance().ClearUnReqFriends();
                        IMUserActionManager.instance().ClearReqFriends();
                        Utils.showToast(CurrencyActivity.this, "清除聊天记录成功");

                        myDialog.dialog.dismiss();
                    }
                });
			}
		});
        
        
        
      //获取CheckBox实例
        CheckBox cb = (CheckBox)this.findViewById(R.id.vibration_checkbox);
        //SharedPreferences   user = CurrencyActivity.this.getSharedPreferences("SP", Activity.MODE_PRIVATE);
        boolean speaker = AudioPlayerHandler.getInstance().getMessageAudioMode(CurrencyActivity.this);//user.getBoolean("speaker", true);
        cb.setChecked(speaker);

        //绑定监听器
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub 
                    	AudioPlayerHandler audioPlayerHandler = AudioPlayerHandler.getInstance();
                    	if(arg1)
                    	{
                            audioPlayerHandler.setAudioMode( CurrencyActivity.this,arg1);
                            SpeekerToast.show(CurrencyActivity.this, CurrencyActivity.this.getText(R.string.audio_in_call), Toast.LENGTH_SHORT);
                    	}else{
                            audioPlayerHandler.setAudioMode( CurrencyActivity.this,arg1);
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
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
                break;
      
        }
    }

}


package com.fise.xiaoyu.ui.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.entity.OnLineVedioMessage;
import com.fise.xiaoyu.imservice.event.PriorityEvent;
import com.fise.xiaoyu.ui.activity.GuestActivity;
import com.fise.xiaoyu.utils.Logger;

import org.greenrobot.eventbus.Subscribe;


/**
 * @Description
 */
public abstract class TTGuideBaseActivity extends AppBaseActivity {
    protected ImageView topLeftBtn;
    protected ImageView topRightBtn;
    protected TextView topTitleTxt;
    protected TextView letTitleTxt;
    protected ViewGroup topBar;
    protected ViewGroup topContentView;
    protected LinearLayout baseRoot;
    protected float x1, y1, x2, y2 = 0;
    public int fntLevel = 1;
    public int tempLevel = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SharedPreferences sp = this.getApplication().getSharedPreferences("ziTing", MODE_PRIVATE);
        fntLevel = sp.getInt("ziTing1", 1);
        tempLevel = fntLevel;

        topContentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.tt_activity_base, null);
        topBar = (ViewGroup) topContentView.findViewById(R.id.topbar);
        topTitleTxt = (TextView) topContentView.findViewById(R.id.base_activity_title);
        topLeftBtn = (ImageView) topContentView.findViewById(R.id.left_btn);
        topRightBtn = (ImageView) topContentView.findViewById(R.id.right_btn);
        letTitleTxt = (TextView) topContentView.findViewById(R.id.left_txt);
        baseRoot = (LinearLayout)topContentView.findViewById(R.id.act_base_root);

        topTitleTxt.setVisibility(View.GONE);
        topRightBtn.setVisibility(View.GONE);
        letTitleTxt.setVisibility(View.GONE);
        topLeftBtn.setVisibility(View.GONE); 
        setContentView(topContentView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void setLeftText(String text) {
        if (null == text) {
            return;
        }
        letTitleTxt.setText(text);
        letTitleTxt.setVisibility(View.VISIBLE);
    }

    protected void setTitle(String title) {
        if (title == null) {
            return;
        }
        if (title.length() > 12) {
            title = title.substring(0, 11) + "...";
        }
        topTitleTxt.setText(title);
        topTitleTxt.setVisibility(View.VISIBLE);
    }

    @Override
    public void setTitle(int id) {
        String strTitle = getResources().getString(id);
        setTitle(strTitle);
    }

    protected void setLeftButton(int resID) {
        if (resID <= 0) {
            return;
        }

        topLeftBtn.setImageResource(resID);
        topLeftBtn.setVisibility(View.VISIBLE);
    }

    protected void setRightButton(int resID) {
        if (resID <= 0) {
            return;
        }

        topRightBtn.setImageResource(resID);
        topRightBtn.setVisibility(View.VISIBLE);
    }

    protected void setTopBar(int resID) {
        if (resID <= 0) {
            return;
        }
        topBar.setBackgroundResource(resID);
    }
    
    @Override  
    public Resources getResources() {  
        Resources res = super.getResources();    
        Configuration config=new Configuration();    
        config.setToDefaults();
        if(fntLevel == 0){
            config.fontScale = 0.9f;
        }else if(fntLevel == 1){
            config.fontScale = 1.0f;
        }else if(fntLevel == 2){
            config.fontScale = 1.1f;
        }else if(fntLevel == 3){
            config.fontScale = 1.2f;
        }else if(fntLevel == 4){
            config.fontScale = 1.3f;
        }else if(fntLevel == 5){
            config.fontScale = 1.35f;
        }
        // co
       // config.fontScale = 1.4f;
        res.updateConfiguration(config,res.getDisplayMetrics());   
        return res;  
    }  
     
    
    public void setFntLevel(int fntLevel) { 
    	this.fntLevel = fntLevel; 
    } 
    
    public void setTempFntLevel(int fntLevel) { 
    	this.tempLevel = fntLevel;  
    	this.fntLevel = fntLevel;
        SharedPreferences sp = this.getApplication().getSharedPreferences("ziTing", MODE_PRIVATE);
        Editor editor=sp.edit();
        editor.putInt("ziTing1", fntLevel);
        editor.commit();
        fntLevel = sp.getInt("ziTing1", 1);
    } 
    
    public void setFntLevel1(int fntLevel) {  
    	this.fntLevel = fntLevel;
        SharedPreferences sp = this.getApplication().getSharedPreferences("ziTing", MODE_PRIVATE);
        Editor editor=sp.edit();
        editor.putInt("ziTing1", fntLevel);
        editor.commit();
        fntLevel = sp.getInt("ziTing1", 1);
    }


    @Subscribe
    public void onMessageEvent(PriorityEvent event) {
        switch (event.event) {
            case MSG_VEDIO_MESSAGE: {
                MessageEntity entity = (MessageEntity) event.object;
                /** 正式当前的会话 */
                if(entity.getMsgType() == DBConstant.MSG_TYPE_VIDEO_CALL){
                    Intent intent = new Intent(TTGuideBaseActivity.this,
                            GuestActivity.class);

                    OnLineVedioMessage message = OnLineVedioMessage.parseFromNet(entity);
                    String pushUrl = message.getPushUrl();
                    String pullUrl = message.getPullUrl();

                    intent.putExtra(IntentConstant.KEY_PEERID, message.getFromId());
                    intent.putExtra(IntentConstant.PUSHURL, pushUrl);
                    intent.putExtra(IntentConstant.PULLURL, pullUrl);

                    TTGuideBaseActivity.this.startActivity(intent);

                }
            }
            break;
        }
    }
}

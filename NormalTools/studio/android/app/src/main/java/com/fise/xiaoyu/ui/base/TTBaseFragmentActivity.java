package com.fise.xiaoyu.ui.base;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.entity.OnLineVedioMessage;
import com.fise.xiaoyu.imservice.event.PriorityEvent;
import com.fise.xiaoyu.ui.activity.GuestActivity;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.StatusBarUtil;

import org.greenrobot.eventbus.Subscribe;


public abstract class TTBaseFragmentActivity extends AppBaseActivity {
	int fntLevel;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
		SharedPreferences sp = this.getApplication().getSharedPreferences(
				"ziTing", MODE_PRIVATE);
		fntLevel = sp.getInt("ziTing1", 1);

	//	applyKitKatTranslucency();
		StatusBarUtil.transparencyBar1(this);

	}


	@TargetApi(19)
	private void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;        // a|=b的意思就是把a和b按位或然后赋值给a   按位或的意思就是先把a和b都换成2进制，然后用或操作，相当于a=a|b
		} else {
			winParams.flags &= ~bits;        //&是位运算里面，与运算  a&=b相当于 a = a&b  ~非运算符
		}
		win.setAttributes(winParams);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	
	
	@Override
	public Resources getResources() {
		Resources res = super.getResources();
		Configuration config = new Configuration();
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
		res.updateConfiguration(config, res.getDisplayMetrics());
		return res;
	}


	//创建一个Handler
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 2001:
				{
					final MessageEntity entity = (MessageEntity) msg.obj;
					new Handler(new Handler.Callback() {
						@Override
						public boolean handleMessage(Message msg) {
							//实现页面跳转
							Intent intent = new Intent(TTBaseFragmentActivity.this,
									GuestActivity.class);

							OnLineVedioMessage message = OnLineVedioMessage.parseFromNet(entity);
							String pushUrl = message.getPushUrl();
							String pullUrl = message.getPullUrl();

							intent.putExtra(IntentConstant.KEY_PEERID, message.getFromId());
							intent.putExtra(IntentConstant.PUSHURL, pushUrl);
							intent.putExtra(IntentConstant.PULLURL, pullUrl);

							TTBaseFragmentActivity.this.startActivity(intent);


							return false;
						}
					}).sendEmptyMessageDelayed(0,600);//表示延迟3秒发送任务
				}
				 break;
				default:
					break;
			}
		}
	};


	@Subscribe
	public void onMessageEvent(PriorityEvent event) {
		switch (event.event) {
			case MSG_VEDIO_MESSAGE: {
				MessageEntity entity = (MessageEntity) event.object;
				/** 正式当前的会话 */
				if(entity.getMsgType() == DBConstant.MSG_TYPE_VIDEO_CALL){

					Message message = new Message();
					message.what = 2001;
					message.obj = entity;
					handler.sendMessage(message);

				}
			}
			break;
		}
	}

}

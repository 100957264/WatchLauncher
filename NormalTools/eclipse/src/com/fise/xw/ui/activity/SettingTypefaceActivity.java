package com.fise.xw.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.widget.IMBaseImageView;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;

/**
 *设置字体界面
 */
@SuppressLint("NewApi")
public class SettingTypefaceActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(SettingTypefaceActivity.class);
	private Handler uiHandler = new Handler();
	private IMService imService;
	private SeekBar seekBar = null;

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

					IMBaseImageView user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);
					user_portrait
							.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
					user_portrait.setCorner(8);
					user_portrait
							.setImageResource(R.drawable.tt_default_user_portrait_corner);
					user_portrait.setImageUrl(imService.getLoginManager()
							.getLoginInfo().getAvatar());

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
		// setContentView(R.layout.setting_typeface);

		imServiceConnector.connect(SettingTypefaceActivity.this);
		EventBus.getDefault().register(this);
		SharedPreferences sp = this.getApplication().getSharedPreferences(
				"ziTing", MODE_PRIVATE);
		fntLevel = sp.getInt("ziTing1", 0);
		tempLevel = fntLevel;
		initView();

	}

	public void initView() {
		this.setContentView(R.layout.setting_typeface);

		TextView message_content_left1 = (TextView) findViewById(R.id.message_content_left1);
		message_content_left1.setText(this
				.getString(R.string.preview_typeface_left1));
		TextView message_content_left = (TextView) findViewById(R.id.message_content_left);
		message_content_left.setText(this
				.getString(R.string.preview_typeface_left));

		TextView message_content = (TextView) findViewById(R.id.message_content);
		message_content.setText(this.getString(R.string.preview_typeface));

		seekBar = (SeekBar) findViewById(R.id.seekBar);

		if (fntLevel == 1) {
			seekBar.setProgress(1);
		} else if (fntLevel == 2) {
			seekBar.setProgress(2);
		} else if (fntLevel == 3) {
			seekBar.setProgress(3);
		} else if (fntLevel == 4) {
			seekBar.setProgress(4);
		} else if (fntLevel == 5) {
			seekBar.setProgress(5);
		}

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			/**
			 * 拖动条停止拖动的时候调用
			 */
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				initView();
			}

			/**
			 * 拖动条开始拖动的时候调用
			 */
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				initView();
			}

			/**
			 * 拖动条进度改变的时候调用
			 */
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// SettingTypefaceActivity.this.setFntLevel(seekBar.getProgress());
				SettingTypefaceActivity.this.setFntLevel(seekBar.getProgress());
				initView();

			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// IMUIHelper.openCurrencyActivity(SettingTypefaceActivity.this);
				if (SettingTypefaceActivity.this.tempLevel != SettingTypefaceActivity.this.fntLevel) {
					SettingTypefaceActivity.this.setTempFntLevel(fntLevel);

					Intent intent = new Intent(SettingTypefaceActivity.this,
							MainActivity.class);
					SettingTypefaceActivity.this.startActivity(intent);

				} else {
					SettingTypefaceActivity.this.finish();
				}

			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// IMUIHelper.openCurrencyActivity(SettingTypefaceActivity.this);
				if (SettingTypefaceActivity.this.tempLevel != SettingTypefaceActivity.this.fntLevel) {
					SettingTypefaceActivity.this.setTempFntLevel(fntLevel);
					Intent intent = new Intent(SettingTypefaceActivity.this,
							MainActivity.class);
					SettingTypefaceActivity.this.startActivity(intent);
				} else {
					SettingTypefaceActivity.this.finish();
				}
			}
		});

		IMBaseImageView user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);
		user_portrait
				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
		user_portrait.setCorner(8);
		user_portrait
				.setImageResource(R.drawable.tt_default_user_portrait_corner);
		user_portrait.setImageUrl(IMLoginManager.instance().getLoginInfo()
				.getAvatar());
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// do something...
			if (SettingTypefaceActivity.this.tempLevel != SettingTypefaceActivity.this.fntLevel) {
				SettingTypefaceActivity.this.setTempFntLevel(fntLevel);
				Intent intent = new Intent(SettingTypefaceActivity.this,
						MainActivity.class);
				SettingTypefaceActivity.this.startActivity(intent);
			} else {
				SettingTypefaceActivity.this.finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(SettingTypefaceActivity.this);
		EventBus.getDefault().unregister(this);
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
		SettingTypefaceActivity.this.setFntLevel1(fntLevel);
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

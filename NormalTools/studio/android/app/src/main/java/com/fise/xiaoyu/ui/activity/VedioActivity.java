package com.fise.xiaoyu.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.ui.base.TTGuideBaseActivity;
import com.fise.xiaoyu.utils.PermissionUtil;
import com.fise.xiaoyu.utils.Utils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.sdut.lsy.video.MovieRecorderView;


/**
 *  聊天 发送小视频界面
 */
public class VedioActivity extends TTGuideBaseActivity {
	private MovieRecorderView mRecorderView;
	private Button mShootBtn;
	private boolean isFinish = true;
	private ProgressBar recordPb;
	private int mTimeCount;// 时间计数
	private Timer mTimer;// 计时器
	private int mRecordMaxTime;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏android系统的状态栏
		setContentView(R.layout.tt_activity_vedio);
		mRecorderView = (MovieRecorderView) findViewById(R.id.movieRecorderView);
		mShootBtn = (Button) findViewById(R.id.shoot_button);
		recordPb = (ProgressBar) findViewById(R.id.progressBar_record);
		mRecordMaxTime = 10;// 默认为10
		recordPb.setMax(mRecordMaxTime);
		mShootBtn.setOnTouchListener(new OnTouchListener() {
			private String[] permissions = new String[]{
					Manifest.permission.CAMERA,
					Manifest.permission.RECORD_AUDIO,
			};
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(PermissionUtil.lackPermission(permissions)){
					requestRunPermisssion(permissions, new PermissionListener() {
						@Override
						protected void onGranted() {
						}

						@Override
						protected void onDenied(List<String> deniedPermission) {
							Toast.makeText(VedioActivity.this, PermissionUtil.getPermissionString(VedioActivity.this, deniedPermission), Toast.LENGTH_SHORT).show();
						}
					});
				}else if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mRecorderView.record(new MovieRecorderView.OnRecordFinishListener() {

								@Override
								public void onRecordFinish() {
									handler.sendEmptyMessage(1);
								} 
							});

					mTimeCount = 0;// 时间计数器重新赋值
					mTimer = new Timer();
					mTimer.schedule(new TimerTask() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mTimeCount++;
                            handler.sendEmptyMessage(2);

						}
					}, 0, 1000);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (mTimeCount > 1)
						handler.sendEmptyMessage(1);
					else {
						if (mRecorderView.getmRecordFile() != null)
							mRecorderView.getmRecordFile().delete();

						mTimer.cancel();
                        handler.removeMessages(2);
                         recordPb.setProgress(0);
						mRecorderView.stop();

						///mRecorderView.resetCamera();
						Utils.showToast(VedioActivity.this, "视频录制时间太短");

						  
					} 
				}	
				return true;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		isFinish = true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		isFinish = false;
		mRecorderView.stop();
	}

	@Override
	public void onPause() {
		super.onPause();
		mRecorderView.stop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mRecorderView.stop();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    recordPb.setProgress(0);
                    if (mTimer != null)
                        mTimer.cancel();
                    finishActivity();
                    break;

                case 2:
                    Log.i("aaa", "run: "+mTimeCount);
                    recordPb.setProgress(mTimeCount);// 设置进度条
                    if (mTimeCount == mRecordMaxTime) {// 达到指定时间，停止拍摄
                        mRecorderView.stop();

                    }
                    break;
            }

		}
	};

	private void finishActivity() {
		if (isFinish) {
			mRecorderView.stop();
			// 返回到播放页面
			Intent intent = new Intent();
			Log.d("TAG", mRecorderView.getmRecordFile().getAbsolutePath());
			intent.putExtra("path", mRecorderView.getmRecordFile()
					.getAbsolutePath());
			setResult(RESULT_OK, intent);
		}
		// isFinish = false;
		finish();
	}

	/**
	 * 录制完成回调
	 */
	public interface OnShootCompletionListener {
		public void OnShootSuccess(String path, int second);

		public void OnShootFailure();
	}

}

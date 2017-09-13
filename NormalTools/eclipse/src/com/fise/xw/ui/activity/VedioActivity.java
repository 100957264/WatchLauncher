package com.fise.xw.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;
import cn.sdut.lsy.video.MovieRecorderView;

import com.fise.xw.R;


/**
 *  聊天 发送小视频界面
 * @author weileiguan
 *
 */
public class VedioActivity extends Activity {
	private MovieRecorderView mRecorderView;
	private Button mShootBtn;
	private boolean isFinish = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vedio);
		mRecorderView = (MovieRecorderView) findViewById(R.id.movieRecorderView);
		mShootBtn = (Button) findViewById(R.id.shoot_button);

		mShootBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mRecorderView
							.record(new MovieRecorderView.OnRecordFinishListener() {

								@Override
								public void onRecordFinish() {
									handler.sendEmptyMessage(1);
								} 
							});
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (mRecorderView.getTimeCount() > 1)
						handler.sendEmptyMessage(1);
					else {
						if (mRecorderView.getmRecordFile() != null)
							mRecorderView.getmRecordFile().delete();
						mRecorderView.stop();

						Toast.makeText(VedioActivity.this, "视频录制时间太短",
								Toast.LENGTH_SHORT).show();
						  
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
			finishActivity();
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
	 * 
	 * @author liuyinjun
	 * 
	 * @date 2015-2-9
	 */
	public interface OnShootCompletionListener {
		public void OnShootSuccess(String path, int second);

		public void OnShootFailure();
	}

}

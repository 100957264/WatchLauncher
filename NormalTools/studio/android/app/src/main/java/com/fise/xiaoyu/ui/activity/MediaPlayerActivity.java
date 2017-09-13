package com.fise.xiaoyu.ui.activity;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.fise.xiaoyu.DB.DBInterface;
import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.MessageConstant;
import com.fise.xiaoyu.imservice.entity.VedioMessage;
import com.fise.xiaoyu.imservice.event.MessageEvent;
import com.fise.xiaoyu.ui.base.TTGuideBaseActivity;
import com.fise.xiaoyu.ui.widget.LoadingCircleView;
import com.fise.xiaoyu.ui.widget.RowItem;
import com.fise.xiaoyu.utils.CommonUtil;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 *  小视频播放界面
 */
public class MediaPlayerActivity extends TTGuideBaseActivity {

	private static final String VIDEO_PATH = "video_path";
	private final String TAG = "main";
	private int currentPosition = 0;
	private boolean isPlaying;
	private int Type;
	private VedioMessage vedioMessage;
	private String videoPath;
	private boolean start ;


	private SurfaceView sv;
	private MediaPlayer mediaPlayer;


	private Button btnClose;
	private DownloadTask task;
	private LoadingCircleView progressBar;
	private AudioManager audioManager;
	private Boolean isMute;
    private int mSurfaceViewWidth;
    private int mSurfaceViewHeight;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏android系统的状态栏
		setContentView(R.layout.tt_activity_media_player);

		sv = (SurfaceView) findViewById(R.id.sv);
		Button btnFinish = (Button) findViewById(R.id.btn_finish);
		btnFinish.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		progressBar   = (LoadingCircleView)findViewById(R.id.surface_video_progress);
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		// 为SurfaceHolder添加回调
		sv.getHolder().addCallback(callback);

		start = false;
		Type = MediaPlayerActivity.this.getIntent().getIntExtra(
				IntentConstant.VIDEO_DOWN_TYPE, 0);
		videoPath = MediaPlayerActivity.this.getIntent().getStringExtra(
				IntentConstant.VIDEO_PATH);
		isMute = MediaPlayerActivity.this.getIntent().getBooleanExtra(
				IntentConstant.PALY_VIDEO_MUTE ,false);
		        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mSurfaceViewWidth = dm.widthPixels;
        mSurfaceViewHeight = dm.heightPixels;

		// 4.0版本之下需要设置的属性
		// 设置Surface不维护自己的缓冲区，而是等待屏幕的渲染引擎将内容推送到界面
		 sv.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}


	private Callback callback = new Callback() {
		// SurfaceHolder被修改的时候回调
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i(TAG, "SurfaceHolder 被销毁");
			// 销毁SurfaceHolder的时候记录当前的播放位置并停止播放
			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				currentPosition = mediaPlayer.getCurrentPosition();
				mediaPlayer.stop();
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.i(TAG, "SurfaceHolder 被创建");

			if (currentPosition > 0) {
				// 创建SurfaceHolder的时候，如果存在上次播放的位置，则按照上次播放位置进行播放
				play(currentPosition);
				currentPosition = 0;
			}
			 if(start == false){
				 
				 if (Type == 0) { 
						play(0);
					} else {

						vedioMessage = (VedioMessage) getIntent().getSerializableExtra(
								IntentConstant.VIDEO_DOWN_MESSAGE);

					   progressBar.setVisibility(View.VISIBLE);

						task = new DownloadTask(MediaPlayerActivity.this);
						task.execute(new String[] { videoPath });

					}
				 start = true;
			 }

			
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.i(TAG, "SurfaceHolder 大小被改变");
		}

	};

	private OnSeekBarChangeListener change = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// 当进度条停止修改的时候触发
			// 取得当前进度条的刻度
			int progress = seekBar.getProgress();
			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				// 设置当前播放的位置
				mediaPlayer.seekTo(progress);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

		}
	};

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(MessageEvent event) {
		MessageEvent.Event type = event.getEvent();
		MessageEntity entity = event.getMessageEntity();
		switch (type) {
		case HANDLER_VEDIO_UPLOAD_SUCCESS: {
			//play(0);
		}
			break;

		}
	}

	/*
	 * 停止播放
	 */
	protected void stop() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			isPlaying = false;
		}
	}


	/**
	 * 开始播放
	 * 
	 * @param msec
	 *            播放初始位置
	 */
	protected void play(final int msec) {
		// 获取视频文件地址 
		File file = new File(videoPath);
		if (!file.exists()) {
			Utils.showToast(this, "视频文件路径错误");
			return;
		} 
		try {
			mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    if (width == 0 || height == 0) {
                        return;
                    }

                    int w = mSurfaceViewHeight * width / height;
                    int margin = (mSurfaceViewWidth - w) / 2;
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    lp.setMargins(margin, 0, margin, 0);
                    sv.setLayoutParams(lp);

                }
            });

			if(isMute){
				mediaPlayer.setVolume(0, 0);
			}
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			// 设置播放的视频源
			mediaPlayer.setDataSource(file.getAbsolutePath());
			// 设置显示视频的SurfaceHolder
			mediaPlayer.setDisplay(sv.getHolder()); 
			mediaPlayer.prepareAsync();
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) { 
					mediaPlayer.start();
					// 按照初始位置播放
					mediaPlayer.seekTo(msec);
					// 设置进度条的最大进度为视频流的最大播放时长
					// // 开始线程，更新进度条的刻度 
				}
			});
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					// 在播放完毕被回调
					// btn_play.setEnabled(true);
					//play(0);
					mediaPlayer.start();
					// 按照初始位置播放
					mediaPlayer.seekTo(msec);
				}
			});

			mediaPlayer.setOnErrorListener(new OnErrorListener() {
 
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					// 发生错误重新播放
					play(0);
					isPlaying = false;
					return false;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if(mediaPlayer!=null){
			if (!mediaPlayer.isPlaying()) {
				mediaPlayer.start(); // 停止播放视频
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mediaPlayer!=null){
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause(); // 停止播放视频
			}
		}

	}

	@Override
	protected void onDestroy() {
		if(mediaPlayer!=null){ 
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop(); // 停止播放视频
			}
			progressBar  = null;
			mediaPlayer.release(); // 释放资源
		}
		
		//取消
		if(task!=null){
			task.cancel(true);
		}
		super.onDestroy();
	}


	/** 内部下载类，微信的机制是下载好再播放的，也可以直接边下载边播放 */
	private boolean interceptFlag = false;
	private class DownloadTask extends
			AsyncTask<String, Integer, List<RowItem>> {

		private Activity context;
		List<RowItem> rowItems;
		int taskCount;

		public DownloadTask(Activity context) {
			this.context = context;
		}

		@Override
		protected List<RowItem> doInBackground(String... urls) {
			taskCount = urls.length;
			rowItems = new ArrayList<RowItem>();
			String path = null;
			for (String url : urls) {
				path = downloadImage(url);

				vedioMessage.setVedioPath(path);
				vedioMessage.setLoadStatus(MessageConstant.VEDIO_LOADED_SUCCESS);
				DBInterface.instance().insertOrUpdateMessage(vedioMessage);

				EventBus.getDefault()
						.post(new MessageEvent(
								MessageEvent.Event.HANDLER_VEDIO_UPLOAD_SUCCESS,
								vedioMessage));

				this.cancel(true);
				// rowItems.add(new RowItem(map));
			}
			return rowItems; 
		}

		protected void onProgressUpdate(Integer... progress) {
		//	progressDialog.setProgress(progress[0]);
			if (rowItems != null) {
				if(progressBar == null)
					return;
				int progressNum = progress[0];
				progressBar.setProgerss(progressNum,true);
				if(progressNum >= 100){
					Log.d("zzzzz","开始播放 ");
					//play(0);
					progressBar.setVisibility(View.GONE);
				}
			}
		}

		@Override
		protected void onPostExecute(List<RowItem> rowItems) {
			progressBar.setVisibility(View.VISIBLE);
		}

		/**
		 * 下载Image
		 * 
		 * @param urlString
		 * @return
		 */
		private String downloadImage(String urlString) {
			
			int count = 0; 
			URL url;
			InputStream in = null;
			BufferedOutputStream out = null;
			File mRecordFile = null;

			try {
				url = new URL(urlString);
				URLConnection conn = url.openConnection();
				int lengthOfFile = conn.getContentLength();

				in = new BufferedInputStream(url.openStream());
				ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
				out = new BufferedOutputStream(dataStream);

				byte[] data = new byte[512];
				long total = 0L;
				while ((count = in.read(data)) != -1) {
					total += count;
					publishProgress((int) ((total * 100) / lengthOfFile));
					out.write(data, 0, count);
				}
				out.flush();
				// BitmapFactory.Options options = new BitmapFactory.Options();
				// options.inSampleSize = 1;

				byte[] bytes = dataStream.toByteArray();
				// bitmap = BitmapFactory.decodeByteArray(bytes, 0,
				// bytes.length);

				File vecordDir = CommonUtil.getVedioSavePath();
				mRecordFile = File.createTempFile("recording", ".mp4",
						vecordDir); // mp4鏍煎紡
				FileOutputStream fos = new FileOutputStream(mRecordFile);
				fos.write(bytes);
				fos.close();
				 
				videoPath = mRecordFile.getAbsolutePath();
				play(0);
				// adfs
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (mRecordFile == null) {
				return null;
			} else {
				return mRecordFile.getAbsolutePath();
			}
		}

	}
}

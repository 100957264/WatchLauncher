package com.fise.xw.ui.activity;

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

import com.fise.xw.R;
import com.fise.xw.DB.DBInterface;
import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.MessageConstant;
import com.fise.xw.imservice.entity.VedioMessage;
import com.fise.xw.imservice.event.MessageEvent;
import com.fise.xw.ui.widget.RowItem;

import de.greenrobot.event.EventBus;

import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.VideoView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;


/**
 *  小视频播放界面
 * @author weileiguan
 *
 */
public class MediaPlayerActivity extends Activity {
	private final String TAG = "main";
	private SurfaceView sv;
	private MediaPlayer mediaPlayer;
	private int currentPosition = 0;
	private boolean isPlaying;

	private static final String VIDEO_PATH = "video_path";

	private String videoPath;  
	private Button btnClose;

	private int Type;
	private VedioMessage vedioMessage; 
	ProgressDialog progressDialog;
	
	boolean start ;
	private DownloadTask task;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_media_player);

		sv = (SurfaceView) findViewById(R.id.sv);

		// 为SurfaceHolder添加回调
		sv.getHolder().addCallback(callback);

		start = false;
		Type = MediaPlayerActivity.this.getIntent().getIntExtra(
				IntentConstant.VIDEO_DOWN_TYPE, 0);
		videoPath = MediaPlayerActivity.this.getIntent().getStringExtra(
				IntentConstant.VIDEO_PATH);

		EventBus.getDefault().register(this);
 

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

						progressDialog = new ProgressDialog(MediaPlayerActivity.this);
						progressDialog.setTitle("In progress...");// 设置Title
						progressDialog.setMessage("Loading...");
						progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // STYLE_HORIZONTAL
						progressDialog.setIndeterminate(false);
						progressDialog.setMax(100);
						progressDialog.setCancelable(true);
						progressDialog.show();

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

	public void onEventMainThread(MessageEvent event) {
		MessageEvent.Event type = event.getEvent();
		MessageEntity entity = event.getMessageEntity();
		switch (type) {
		case HANDLER_VEDIO_UPLOAD_SUCCESS: {
			play(0);
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
			
			Toast.makeText(this, "视频文件路径错误", 0).show();
			return;
		} 
		try {
			mediaPlayer = new MediaPlayer();
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
	protected void onDestroy() {
		if(mediaPlayer!=null){ 
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop(); // 停止播放视频
			}
			

			mediaPlayer.release(); // 释放资源
		}
		
		//取消
		if(task!=null){
			task.cancel(true);
		}
		super.onDestroy();
	}

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
				vedioMessage
						.setLoadStatus(MessageConstant.VEDIO_LOADED_SUCCESS);
				DBInterface.instance().insertOrUpdateMessage(vedioMessage);

				EventBus.getDefault()
						.post(new MessageEvent(
								MessageEvent.Event.HANDLER_VEDIO_UPLOAD_SUCCESS,
								vedioMessage));
						
				progressDialog.dismiss();  
				this.cancel(true);
				// rowItems.add(new RowItem(map));
			}
			return rowItems; 
		}

		protected void onProgressUpdate(Integer... progress) {
			progressDialog.setProgress(progress[0]);
			if (rowItems != null) {
				progressDialog.setMessage("Loading " + (rowItems.size() + 1)
						+ "/" + taskCount);
			}
		}

		@Override
		protected void onPostExecute(List<RowItem> rowItems) { 
			progressDialog.dismiss(); 
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

				File sampleDir = new File(
						Environment.getExternalStorageDirectory()
								+ File.separator + "im/video/");
				if (!sampleDir.exists()) {
					sampleDir.mkdirs();
				}
				
				File vecordDir = sampleDir;
				mRecordFile = File.createTempFile("recording", ".mp4",
						vecordDir); // mp4鏍煎紡
				FileOutputStream fos = new FileOutputStream(mRecordFile);
				fos.write(bytes);
				fos.close();
				 
				videoPath = mRecordFile.getAbsolutePath();
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

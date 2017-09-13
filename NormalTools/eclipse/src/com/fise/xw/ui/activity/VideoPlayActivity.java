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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.fise.xw.R;
import com.fise.xw.DB.DBInterface;
import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.MessageConstant; 
import com.fise.xw.imservice.entity.VedioMessage;
import com.fise.xw.imservice.event.MessageEvent;
import com.fise.xw.ui.widget.RowItem;

import de.greenrobot.event.EventBus;


/**
 *  小视频播放界面
 * @author weileiguan
 *
 */
@SuppressLint("NewApi")
public class VideoPlayActivity extends Activity {

	private static final String VIDEO_PATH = "video_path";
             
	private String videoPath;

	private VideoView mVideoView;

	private Button btnClose;

	private int Type;
	private VedioMessage vedioMessage;

	ProgressDialog progressDialog;

	public VideoPlayActivity() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_video);

		Type = VideoPlayActivity.this.getIntent().getIntExtra(
				IntentConstant.VIDEO_DOWN_TYPE, 0);
		videoPath = VideoPlayActivity.this.getIntent().getStringExtra(
				IntentConstant.VIDEO_PATH);

		EventBus.getDefault().register(this);

		mVideoView = (VideoView) findViewById(R.id.video_view);
		btnClose = (Button) findViewById(R.id.btn_close);

		if (Type == 0) {
			//
			play();
		} else {

			vedioMessage = (VedioMessage) getIntent().getSerializableExtra(
					IntentConstant.VIDEO_DOWN_MESSAGE);

			progressDialog = new ProgressDialog(this);
			progressDialog.setTitle("In progress...");// 设置Title
			progressDialog.setMessage("Loading...");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); //STYLE_HORIZONTAL
			progressDialog.setIndeterminate(false);
			progressDialog.setMax(100);
			progressDialog.setCancelable(true);
			progressDialog.show();
			   
 
			DownloadTask task = new DownloadTask(this);
			task.execute(new String[] { videoPath });

		}

		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mVideoView.stopPlayback();
				VideoPlayActivity.this.finish();
			}
		});
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public void play() {

		mVideoView.setMediaController(new MediaController(this));
		mVideoView.setVideoURI(Uri.parse(videoPath));
		mVideoView.start();
		mVideoView.requestFocus();
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				mp.setLooping(true);

			}
		});

		mVideoView
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						mVideoView.setVideoPath(videoPath);
						mVideoView.start();

					}
				});
	}

	public void onEventMainThread(MessageEvent event) {
		MessageEvent.Event type = event.getEvent();
		MessageEntity entity = event.getMessageEntity();
		switch (type) {
		case HANDLER_VEDIO_UPLOAD_SUCCESS: {
			play();
		}
			break;

		}
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
			Bitmap bitmap = null;

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

				// adfs
			} catch (Exception e) {
				e.printStackTrace();
			}

			if(mRecordFile == null){
				return null;
			}else{ 
				return mRecordFile.getAbsolutePath();
			}
		}

	}

}

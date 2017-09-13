package com.fise.xw.ui.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.PeerEntity;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;

/**
 * 设置聊天背景
 */
@SuppressLint("NewApi")
public class SettingMessageBgActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(SettingMessageBgActivity.class);
	private Handler uiHandler = new Handler();
	private IMService imService;

	private PeerEntity peerEntity;
	private String curSessionKey;
	private Bitmap photo;

	private boolean allMessageBg;
	
	private static final int PHOTO_REQUEST_CAMERA = 1;// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	private static final String PHOTO_FILE_NAME = "message_bg.jpg";

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

					curSessionKey = SettingMessageBgActivity.this.getIntent()
							.getStringExtra(IntentConstant.KEY_SESSION_KEY);
				
					
					if (TextUtils.isEmpty(curSessionKey)) {
						logger.e("groupmgr#getSessionInfoFromIntent failed");
						return;
					}
					peerEntity = imService.getSessionManager().findPeerEntity(
							curSessionKey);

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

		imServiceConnector.connect(SettingMessageBgActivity.this);
		EventBus.getDefault().register(this);
		allMessageBg = SettingMessageBgActivity.this.getIntent()
				.getBooleanExtra(IntentConstant.KEY_ALL_MESSAGE_BG, false);
		
		setContentView(R.layout.setting_message_bg);
		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SettingMessageBgActivity.this.finish();
			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SettingMessageBgActivity.this.finish();
			}
		});

		RelativeLayout default_bg = (RelativeLayout) findViewById(R.id.default_bg);
		default_bg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(SettingMessageBgActivity.this,
						SelectMessageBgActivity.class);
				intent.putExtra(IntentConstant.KEY_ALL_MESSAGE_BG, allMessageBg); 
				intent.putExtra(IntentConstant.KEY_SESSION_KEY, curSessionKey);
				SettingMessageBgActivity.this.startActivity(intent);
			}
		});

		RelativeLayout select_gallery = (RelativeLayout) findViewById(R.id.select_gallery);
		select_gallery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(Intent.ACTION_PICK,
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
				// 激活系统图库，选择一张图片
				// Intent intent = new Intent(Intent.ACTION_PICK);
				// intent.setType("image/*");
				// SettingMessageBgActivity.this.startActivityForResult(intent,
				// PHOTO_REQUEST_GALLERY);
			}
		});

		RelativeLayout select_photograph = (RelativeLayout) findViewById(R.id.select_photograph);
		select_photograph.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				// Intent intent = new
				// Intent("android.media.action.IMAGE_CAPTURE");
				// // 判断存储卡是否可以用，可用进行存储
				// if (hasSdcard()) {
				// intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
				// .fromFile(new File(Environment
				// .getExternalStorageDirectory(),
				// PHOTO_FILE_NAME)));
				// }
				//
				// // Activity activity = (Activity) context;
				// (SettingMessageBgActivity.this).startActivityForResult(intent,
				// PHOTO_REQUEST_CAMERA);

				Intent takePictureIntent = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(takePictureIntent, PHOTO_REQUEST_CAMERA);
			}
		});
	}

	private boolean hasSdcard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(SettingMessageBgActivity.this);
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
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PHOTO_REQUEST_GALLERY) {
			if (data != null) {

				// 得到图片的全路径
				Uri uri = data.getData();
				String path = null;
				Cursor cursor = getContentResolver().query(uri, null, null,
						null, null);
				if (cursor != null && cursor.moveToFirst()) {
					path = cursor
							.getString(cursor
									.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
				}

				Intent intent = new Intent(SettingMessageBgActivity.this,
						MessageBgQueActivity.class);
				intent.putExtra(IntentConstant.KEY_AVATAR_URL, path);
				intent.putExtra(IntentConstant.KEY_SESSION_KEY, curSessionKey);
				intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR,
						true);
				intent.putExtra(IntentConstant.KEY_ALL_MESSAGE_BG, allMessageBg); 

				startActivity(intent);
				// crop(uri);
			}

		} else if (requestCode == PHOTO_REQUEST_CAMERA) {
			if (data != null) {
				 Uri uri = data.getData();
				    if (uri != null) {
				        this.photo = BitmapFactory.decodeFile(uri.getPath());
				    }
				    if (this.photo == null) {
				        Bundle bundle = data.getExtras();
				        if (bundle != null) {
				            this.photo = (Bitmap) bundle.get("data");
				        } else {
				           
				            return;
				        }
				    }
				 
				    FileOutputStream fileOutputStream = null;
				    try {
				        // 获取 SD 卡根目录
				        String saveDir = Environment.getExternalStorageDirectory() + "/meitian_photos";
				        // 新建目录
				        File dir = new File(saveDir);
				        if (! dir.exists()) dir.mkdir();
				        // 生成文件名
				        SimpleDateFormat t = new SimpleDateFormat("yyyyMMddssSSS");
				        String filename = "MT" + (t.format(new Date())) + ".jpg";
				        // 新建文件
				        File file = new File(saveDir, filename);
				        // 打开文件输出流
				        fileOutputStream = new FileOutputStream(file);
				        // 生成图片文件
				        this.photo.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream); 
				        // 相片的完整路径
				        //this.picPath = file.getPath();
						Intent intent = new Intent(SettingMessageBgActivity.this,
								MessageBgQueActivity.class);
						intent.putExtra(IntentConstant.KEY_AVATAR_URL, file.getPath());
						intent.putExtra(IntentConstant.KEY_SESSION_KEY, curSessionKey);
						intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR,
								true);
						intent.putExtra(IntentConstant.KEY_ALL_MESSAGE_BG, allMessageBg); 
						startActivity(intent);

				        
				        
				    } catch (Exception e) {
				        e.printStackTrace();
				    } finally {
				        if (fileOutputStream != null) {
				            try {
				                fileOutputStream.close();
				            } catch (Exception e) {
				                e.printStackTrace();
				            }
				        }
				    }
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
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

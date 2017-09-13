package com.fise.xw.ui.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.SysConstant;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.service.AvatarImageService;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.menu.HeadMenu;
import com.fise.xw.ui.widget.HeadImageView;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;



/**
 *  设置个人头像
 * @author weileiguan
 *
 */
public class HeadPortraitActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(HeadPortraitActivity.class);
	public static String imageUri = "";
	public HeadMenu menu;

	private static final int PHOTO_REQUEST_CAMERA = 1;// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	private static final int PHOTO_REQUEST_CUT = 3;// 结果

	private ImageView mFace;
	private Bitmap bitmap;
	private Button uploadButton;
	private IMService imService;

	/* 头像名称 */
	private static final String PHOTO_FILE_NAME = "temp_photo.jpg";
	private static final String PHOTO_FILE_DEVICE_NAME = "temp_photo_dev.jpg";

	private File tempFile;
	private String filePath;
	private Context mContext;
	private HeadImageView portraitView;
	private boolean isShow;
	private Button right_button;
	private Button user_button;
	public int nick_mode;
	public int currentUserId;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_my_head);
		imServiceConnector.connect(this);
		EventBus.getDefault().register(this);

		Intent intent = getIntent();
		if (intent == null) {
			logger.e("detailPortrait#displayimage#null intent");
			return;
		}

		String resUri = intent.getStringExtra(IntentConstant.KEY_AVATAR_URL);
		imageUri = resUri;

		nick_mode = intent.getIntExtra(IntentConstant.KEY_NICK_MODE, 0);
		if (nick_mode == DBConstant.DEVICE_NICK) {
			currentUserId = intent.getIntExtra(IntentConstant.KEY_PEERID, 0);
		}

		boolean isContactAvatar = intent.getBooleanExtra(
				IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, false);
		logger.d("displayimage#isContactAvatar:%s", isContactAvatar);

		portraitView = (HeadImageView) findViewById(R.id.head_portrait);
		portraitView.setAvatar(resUri);
		if (portraitView == null) {
			return;
		}

		if (isContactAvatar) {
			IMUIHelper.setEntityImageViewAvatarNoDefaultPortrait(portraitView,
					resUri, DBConstant.SESSION_TYPE_SINGLE, 0);
		} else {
			IMUIHelper.displayImageNoOptions(portraitView, resUri, -1, 0);
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				portraitView.setFinishActivity(new finishActivity() {
					@Override
					public void finish() {
						if (HeadPortraitActivity.this != null) {
							HeadPortraitActivity.this.finish();
							overridePendingTransition(R.anim.tt_stay,
									R.anim.tt_image_exit);
						}
					}
				});
			}
		}, 500);

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				HeadPortraitActivity.this.finish();
			}
		});

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				HeadPortraitActivity.this.finish();
			}
		});

		menu = new HeadMenu(this);
		menu.addItems(new String[] { "手机相册", "拍照", "保存到手机" });

		right_button = (Button) findViewById(R.id.right_button);
		right_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				menu.showAsDropDown(arg0);
			}
		});

		user_button = (Button) findViewById(R.id.user_button);
		user_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// menu.showAsDropDown(arg0);

				Intent loadImageIntent = new Intent(HeadPortraitActivity.this,
						AvatarImageService.class);
				loadImageIntent
						.putExtra(
								SysConstant.UPLOAD_IMAGE_INTENT_PARAMS_AVATAR,
								filePath);
				loadImageIntent.putExtra(IntentConstant.KEY_NICK_MODE,
						nick_mode);
				loadImageIntent.putExtra(IntentConstant.KEY_PEERID,
						currentUserId);
				HeadPortraitActivity.this.startService(loadImageIntent);

			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PHOTO_REQUEST_GALLERY) {
			if (data != null) {
				// 得到图片的全路径
				Uri uri = data.getData();
				crop(uri);
			}

		} else if (requestCode == PHOTO_REQUEST_CAMERA) {

			if (resultCode == RESULT_OK) {

				if (hasSdcard()) {

					if (nick_mode == DBConstant.DEVICE_NICK) {

						tempFile = new File(
								Environment.getExternalStorageDirectory(),
								PHOTO_FILE_DEVICE_NAME);
					} else {
						tempFile = new File(
								Environment.getExternalStorageDirectory(),
								PHOTO_FILE_NAME);
					}

					// tempFile = new
					// File(Environment.getExternalStorageDirectory(),
					// PHOTO_FILE_NAME);
					filePath = tempFile.getPath();
					crop(Uri.fromFile(tempFile));
				} else {
					Toast.makeText(HeadPortraitActivity.this, "未找到存储卡，无法存储照片！",
							0).show();
				}
			}

		} else if (requestCode == PHOTO_REQUEST_CUT) {
			try {
				bitmap = data.getParcelableExtra("data");
				if (data != null) {
					setImageToHeadView(data);
				}
				// boolean delete = tempFile.delete();
				// System.out.println("delete = " + delete);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 剪切图片
	 * 
	 * @function:
	 * @author:Jerry
	 * @date:2013-12-30
	 * @param uri
	 */
	private void crop(Uri uri) {

		// 裁剪图片意图
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		// 裁剪框的比例，1：1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// 裁剪后输出图片的尺寸大小
		intent.putExtra("outputX", 350);
		intent.putExtra("outputY", 350);
		// 图片格式
		intent.putExtra("outputFormat", "JPEG");
		intent.putExtra("noFaceDetection", true);// 取消人脸识别
		intent.putExtra("return-data", true);// true:不返回uri，false：返回uri
		startActivityForResult(intent, PHOTO_REQUEST_CUT);
	}

	private void setImageToHeadView(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			portraitView.setImageBitmap(photo);
			user_button.setVisibility(View.VISIBLE);
			right_button.setVisibility(View.GONE);

			// 新建文件夹 先选好路径 再调用mkdir函数 现在是根目录下面的Ask文件夹
			File nf = new File(Environment.getExternalStorageDirectory()
					+ "/touxiang");
			nf.mkdir();

			// 在根目录下面的ASk文件夹下 创建okkk.jpg文件
			File f = new File(Environment.getExternalStorageDirectory()
					+ "/touxiang", "touxiang.jpg");

			filePath = f.getPath();
			FileOutputStream out = null;
			try {
				// 打开输出流 将图片数据填入文件中
				out = new FileOutputStream(f);
				photo.compress(Bitmap.CompressFormat.PNG, 90, out);

				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}
	}

	private boolean hasSdcard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			// Toast.makeText(HeadPortraitActivity.this, "修改头像成功",
			// Toast.LENGTH_SHORT).show();
			HeadPortraitActivity.this.finish();
			break;

		}
	}

	public void onEventMainThread(DeviceEvent event) {
		switch (event) {
		case USER_INFO_SETTING_DEVICE_SUCCESS:
			HeadPortraitActivity.this.finish();
			break;

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
	}

	public interface finishActivity {
		public void finish();
	}

}

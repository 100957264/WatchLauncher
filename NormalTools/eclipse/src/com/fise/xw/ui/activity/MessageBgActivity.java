package com.fise.xw.ui.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseActivity;

import de.greenrobot.event.EventBus;


/**
 *  聊天背景界面
 * @author weileiguan
 *
 */
public class MessageBgActivity extends TTBaseActivity {
	 

	private static final int PHOTO_REQUEST_CAMERA = 1;// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	private static final int PHOTO_REQUEST_CUT = 3;// 结果

	private ImageView mFace;
	private Bitmap bitmap;
	private Button uploadButton;
	private IMService imService; 
	 
	/* 头像名称 */
	private static final String PHOTO_FILE_NAME = "message_bg_photo.jpg";
	private File tempFile;
	private String filePath;
	private Context mContext;

	
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
                        //后台服务启动链接失败
                        break;
                    }
                    IMLoginManager loginManager = imService.getLoginManager();
                  
                    
                    return;
                } while (false);

                // 异常分支都会执行这个
              //  handleNoLoginIdentity();
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
		setContentView(R.layout.activity_message_bg);
		this.mFace = (ImageView) this.findViewById(R.id.iv_image);
		mContext = this;
		
		 EventBus.getDefault().register(this);
	     imServiceConnector.connect(this);
	        
		this.uploadButton = (Button) this.findViewById(R.id.upload);
		uploadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				SharedPreferences ww = getSharedPreferences(IntentConstant.KEY_MESSAGE_BG, Activity.MODE_PRIVATE);
	           	SharedPreferences.Editor editor = ww.edit(); 
	           	editor.putString("KEY_MESSAGE_BG", filePath); 
	           	editor.commit(); 
	           	   
//				Intent loadImageIntent = new Intent(MessageBgActivity.this,
//						AvatarImageService.class);
//				loadImageIntent
//						.putExtra(
//								SysConstant.UPLOAD_IMAGE_INTENT_PARAMS_AVATAR,
//								filePath);
//				MessageBgActivity.this.startService(loadImageIntent);
			}
		});

	}

	/*
	 * 从相册获取
	 */
	public void gallery(View view) {
		// 激活系统图库，选择一张图片
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
	}

	/*
	 * 从相机获取
	 */
	public void camera(View view) {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		// 判断存储卡是否可以用，可用进行存储
		if (hasSdcard()) {
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(Environment
							.getExternalStorageDirectory(), PHOTO_FILE_NAME)));
		}
		startActivityForResult(intent, PHOTO_REQUEST_CAMERA);
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
			if (hasSdcard()) {
				tempFile = new File(Environment.getExternalStorageDirectory(),
						PHOTO_FILE_NAME);
				filePath = tempFile.getPath();
				crop(Uri.fromFile(tempFile));
			} else {
				Toast.makeText(MessageBgActivity.this, "未找到存储卡，无法存储照片！", 0).show();
			}

		} else if (requestCode == PHOTO_REQUEST_CUT) {
			try {
				bitmap = data.getParcelableExtra("data");
				this.mFace.setImageBitmap(bitmap);
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
		intent.putExtra("outputX", 250);
		intent.putExtra("outputY", 250);
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

			// 新建文件夹 先选好路径 再调用mkdir函数 现在是根目录下面的Ask文件夹
			File nf = new File(Environment.getExternalStorageDirectory()
					+ "/Ask");
			nf.mkdir();

			// 在根目录下面的ASk文件夹下 创建okkk.jpg文件
			File f = new File(Environment.getExternalStorageDirectory()
					+ "/Ask", "okkk.jpg");

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
	
	
    public void onEventMainThread(UserInfoEvent event){
        switch (event){  
            case USER_INFO_DATA_UPDATE:    
            	Toast.makeText(MessageBgActivity.this, "设置个人信息成功",
            		     Toast.LENGTH_SHORT).show();
                break; 
                
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(this);
    }

}

package com.fise.xiaoyu.imservice.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.entity.ImageMessage;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMGroupManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.manager.IMMessageManager;
import com.fise.xiaoyu.protobuf.IMBaseDefine;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * LocalService
 */
@SuppressLint("NewApi")
public class LocalService extends Service {

    private AlarmManager am = null;
    private Camera camera;
    private SurfaceHolder myHolder;

    private final IBinder mBinder = new LocalBinder();
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private View mRecorderView;
    private SurfaceHolder mSurfaceHolder;
    private int peer_Id;
    private int peer_type;
    private UserEntity user;
    private boolean isOpen;

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            isOpen = true;

            if (checkCameraHardware(getApplicationContext())) {
                // 获取摄像头（首选前置，无前置选后置）
                if (openFacingFrontCamera()) {
                    Log.d("Demo", "openCameraSuccess");
                    // 进行对焦
                    autoFocus();
                } else {
                    if (camera != null) {
                        camera.release();
                        camera = null;
                    }
                    LocalService.this.stopSelf();
                    Log.d("Demo", "openCameraFailed");
                }
            } else {
                if (camera != null) {
                    camera.release();
                    camera = null;
                }
                LocalService.this.stopSelf();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            isOpen = false;
        }
    };

    public class LocalBinder extends Binder {
        public LocalService getService() {
            return LocalService.this;
        }

    }

    @Override
    public void onCreate() {
        init();
    }

    private void init() {

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        am = (AlarmManager) getSystemService(ALARM_SERVICE);

        mRecorderView = LayoutInflater.from(this).inflate(
                R.layout.camera_activity, null);
        SurfaceView surfaceView = (SurfaceView) mRecorderView
                .findViewById(R.id.camera_surfaceview);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(mCallback);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.setFixedSize(1, 1);
        mSurfaceHolder.setKeepScreenOn(true);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        mLayoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        mLayoutParams.width = screenWidth;
        mLayoutParams.height = screenHeight;
        mWindowManager.addView(mRecorderView, mLayoutParams);
        // camera = openFacingBackCamera();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        peer_Id = intent.getIntExtra(IntentConstant.KEY_PEERID, 0);
        peer_type = intent.getIntExtra(IntentConstant.KEY_SESSION_TYPE, 0);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mWindowManager != null) {
            mWindowManager.removeView(mRecorderView);
        }

        if (camera != null) {
            camera.release();
            camera = null;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // 对焦并拍照
    private void autoFocus() {

        // 自动对焦
        camera.autoFocus(myAutoFocus);
        try {
            // 因为开启摄像头需要时间，这里让线程睡两秒
            Thread.sleep(200);// 200

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 对焦后拍照
        camera.takePicture(null, null, commandPicCallback);
        // camera.takePicture(null, null, this);

    }

    // 判断是否存在摄像头
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // 设备存在摄像头
            return true;
        } else {
            // 设备不存在摄像头
            return false;
        }
    }

    // 得到后置摄像头
    private boolean openFacingFrontCamera() {

        // 尝试开启前置摄像头
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        // 如果开启前置失败（无前置）则开启后置
        if (camera == null) {
            for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) { //CAMERA_FACING_FRONT
                    try {
                        camera = Camera.open(camIdx);
                        Log.d("camera" + camera, "camera" + camera);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }
        }

        if (camera == null) {
            for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) { //CAMERA_FACING_BACK
                    try {
                        Log.d("Demo", "tryToOpenCamera");
                        camera = Camera.open(camIdx);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }
        }

        try {
            camera.setPreviewDisplay(mSurfaceHolder);
        } catch (Exception e) {

        }
        camera.startPreview();

        return true;
    }

    // 自动对焦回调函数(空实现)
    private AutoFocusCallback myAutoFocus = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
        }
    };

    // 拍照成功回调函数
    private PictureCallback commandPicCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // 完成拍照后关闭Activity

            // 将得到的照片进行270°旋转，使其竖直
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.preRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);

            // 创建并保存图片文件
            File pictureFile = new File(getDir(), "camera.jpg");
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (Exception error) {

                error.printStackTrace();
                camera.stopPreview();
                camera.release();
                camera = null;
            }

            UserEntity loginUser = IMLoginManager.instance().getLoginInfo();

            if (peer_type == IMBaseDefine.SessionType.SESSION_TYPE_GROUP.ordinal()) {

                GroupEntity groupEntity = IMGroupManager.instance().findFamilyGroup(peer_Id);
                if (groupEntity != null) {

                    ImageMessage imageMessage = ImageMessage.buildForSend(
                            pictureFile.getAbsolutePath(), loginUser, groupEntity, "");
                    List<ImageMessage> sendList = new ArrayList<>(1);
                    sendList.add(imageMessage);

                    IMMessageManager.instance().sendPhotoImages(sendList);  //做特殊处理
                }

            } else if (peer_type == IMBaseDefine.SessionType.SESSION_TYPE_SINGLE.ordinal()) {

                user = IMContactManager.instance().findFriendsContact(peer_Id);
                if (user == null) {
                    user = IMContactManager.instance().findContact(peer_Id);
                }

                if (user != null) {
                    ImageMessage imageMessage = ImageMessage.buildForSend(
                            pictureFile.getAbsolutePath(), loginUser, user, "");
                    List<ImageMessage> sendList = new ArrayList<>(1);
                    sendList.add(imageMessage);

                    IMMessageManager.instance().sendPhotoImages(sendList);  //做特殊处理
                }
            }


            camera.stopPreview();
            camera.release();
            camera = null;
            LocalService.this.stopSelf();
        }
    };

    // 获取文件夹
    private File getDir() {
        // 得到SD卡根目录
        File dir = Environment.getExternalStorageDirectory();

        if (dir.exists()) {
            return dir;
        } else {
            dir.mkdirs();
            return dir;
        }
    }

}

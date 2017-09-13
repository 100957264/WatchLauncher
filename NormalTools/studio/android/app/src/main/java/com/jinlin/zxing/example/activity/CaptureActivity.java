
package com.jinlin.zxing.example.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMDeviceManager;
import com.fise.xiaoyu.imservice.manager.IMUserActionManager;
import com.fise.xiaoyu.protobuf.IMDevice.ManageType;
import com.fise.xiaoyu.ui.activity.DevWebViewActivity;
import com.fise.xiaoyu.ui.activity.ScanGroupQRActivity;
import com.fise.xiaoyu.ui.activity.SearchFriednsActivity;
import com.fise.xiaoyu.ui.activity.WebViewActivity;
import com.fise.xiaoyu.ui.activity.WebViewTextActivity;
import com.fise.xiaoyu.ui.base.TTGuideBaseActivity;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.PermissionUtil;
import com.fise.xiaoyu.utils.Utils;
import com.google.zxing.Result;
import com.jinlin.zxing.example.camera.CameraManager;
import com.jinlin.zxing.example.decode.DecodeThread;
import com.jinlin.zxing.example.utils.BeepManager;
import com.jinlin.zxing.example.utils.CaptureActivityHandler;
import com.jinlin.zxing.example.utils.InactivityTimer;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 */
public final class CaptureActivity extends TTGuideBaseActivity implements
        SurfaceHolder.Callback, OnClickListener {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private static final int REQUEST_CODE = 100;

    private static final int PARSE_BARCODE_FAIL = 300;

    private static final int PARSE_BARCODE_SUC = 200;

    public final static int SCANNIN_GREQUEST_CODE = 1;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;

    private SurfaceView scanPreview = null;
    private RelativeLayout scanContainer;
    private RelativeLayout scanCropView;
    private ImageView scanLine;

    private Rect mCropRect = null;
    private int currId;
    private int type;
    private int qrType;

    private boolean reUserInfo = false;

    /**
     * 图片的路径
     */
    private String photoPath;

    private Handler mHandler = new MyHandler(this);

    static class MyHandler extends Handler {

        private WeakReference<Activity> activityReference;

        public MyHandler(Activity activity) {
            activityReference = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PARSE_BARCODE_SUC: // 解析图片成功
                    Utils.showToast(activityReference.get(), "解析成功，结果为：" + msg.obj);
                    break;
                case PARSE_BARCODE_FAIL:// 解析图片失败
                    Utils.showToast(activityReference.get(), "解析图片失败");
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    private boolean isHasSurface = false;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // getActionBar().setDisplayHomeAsUpEnabled(true);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.tt_activity_capture);
        type = DBConstant.SESSION_TYPE_SINGLE;

        qrType = this.getIntent().getIntExtra(
                IntentConstant.KEY_QR_ACTIVITY_TYPE, 0);

        scanPreview = (SurfaceView) findViewById(R.id.capture_preview);
        scanContainer = (RelativeLayout) findViewById(R.id.capture_container);
        scanCropView = (RelativeLayout) findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);

        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);

        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.9f);
        animation.setDuration(4500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        scanLine.startAnimation(animation);

        Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
        icon_arrow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                CaptureActivity.this.finish();

            }
        });

        TextView left_text = (TextView) findViewById(R.id.left_text);
        left_text.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                CaptureActivity.this.finish();

            }
        });
        checkCameraPermission();
    }

    private boolean allowedCamera = false;

    private void checkCameraPermission() {
        requestRunPermisssion(Manifest.permission.CAMERA, new PermissionListener() {
            @Override
            protected void onGranted() {
                allowedCamera = true;
            }

            @Override
            protected void onDenied(List<String> deniedPermission) {
                Toast.makeText(CaptureActivity.this, PermissionUtil.getPermissionString(CaptureActivity.this, deniedPermission), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
        if (!allowedCamera) {
            return;
        }

        cameraManager = new CameraManager(getApplication());

        handler = null;

        if (isHasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(scanPreview.getHolder());
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            scanPreview.getHolder().addCallback(this);
        }

        inactivityTimer.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (!allowedCamera) {
            super.onPause();
            return;
        }
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        if (!isHasSurface) {
            scanPreview.getHolder().removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG,
                    "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!isHasSurface) {
            isHasSurface = true;
            initCamera(holder);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeviceEvent event) {
        switch (event) {

            case USER_INFO_UPDATE_DEVICE_SUCCESS:
                this.finish();
                break;

            case USER_INFO_ADD_DEVICE_SUCCESS:
                this.finish();
                break;
            case USER_INFO_ADD_DEVICE_FAILED:
                Utils.showToast(CaptureActivity.this, "" + IMDeviceManager.instance().getError());
                break;


        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event) {
        switch (event) {
            case USER_SCAN_INFO_UPDATE: {

                if (reUserInfo) {  //没有扫描的信息　请求服务器得到数据　reUserInfo　true 请求过服务器
                    if (type == DBConstant.SESSION_TYPE_SINGLE) {
                        type = 0;

                        UserEntity entity = IMContactManager.instance().findParentContact(currId);
                        if (entity != null) {

                            UserEntity entity1 = IMContactManager.instance()
                                    .findContact(currId);
                            entity1.setFriend(entity.getIsFriend());
                            IMContactManager.instance().insertOrUpdateUser(entity1);

                            IMUserActionManager.instance().setSearchInfo(entity);
                            IMUIHelper.openUserProfileActivity(CaptureActivity.this,
                                    currId, true);
                            CaptureActivity.this.finish();

                        } else {
                            entity = IMContactManager.instance().findDeviceContact(currId);
                            if (entity != null) {
                                IMUserActionManager.instance().setSearchInfo(entity);
                                IMUIHelper.openUserProfileActivity(CaptureActivity.this,
                                        currId, true);
                                CaptureActivity.this.finish();
                            } else {
                                entity = IMContactManager.instance()
                                        .findContact(currId);

                                IMUserActionManager.instance().setSearchInfo(entity);

                                Intent intent1 = new Intent(CaptureActivity.this,
                                        SearchFriednsActivity.class);
                                CaptureActivity.this.startActivity(intent1);

                                CaptureActivity.this.finish();
                            }
                        }
                    }
                }
            }

            break;
            case USER_INFO_UPDATE: {
                if (reUserInfo) {  //没有扫描的信息　请求服务器得到数据　reUserInfo　true 请求过服务器
                    if (type == DBConstant.SESSION_TYPE_SINGLE) {
                        type = 0;

                        UserEntity entity = IMContactManager.instance().findParentContact(currId);
                        if (entity != null) {

                            UserEntity entity1 = IMContactManager.instance()
                                    .findContact(currId);
                            entity1.setFriend(entity.getIsFriend());
                            IMContactManager.instance().insertOrUpdateUser(entity1);

                            IMUserActionManager.instance().setSearchInfo(entity);
                            IMUIHelper.openUserProfileActivity(CaptureActivity.this,
                                    currId, true);
                            CaptureActivity.this.finish();

                        } else {
                            entity = IMContactManager.instance().findDeviceContact(currId);
                            if (entity != null) {
                                IMUserActionManager.instance().setSearchInfo(entity);
                                IMUIHelper.openUserProfileActivity(CaptureActivity.this,
                                        currId, true);
                                CaptureActivity.this.finish();
                            } else {
                                entity = IMContactManager.instance()
                                        .findContact(currId);

                                IMUserActionManager.instance().setSearchInfo(entity);

                                Intent intent1 = new Intent(CaptureActivity.this,
                                        SearchFriednsActivity.class);
                                CaptureActivity.this.startActivity(intent1);

                                CaptureActivity.this.finish();
                            }
                        }
                    }
                }
            }
            break;

            case USER_INFO_DATA_FAIL:
                Utils.showToast(CaptureActivity.this, "扫描二维码失败");
                CaptureActivity.this.finish();
                break;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param
     * @param bundle The extras
     */
    public void handleDecode(Result result, Bundle bundle) {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();

        bundle.putInt("width", mCropRect.width());
        bundle.putInt("height", mCropRect.height());
        bundle.putString("result", result.getText());

        String resultString = result.getText();

        if (qrType == DBConstant.SEX_INFO_DEV) {

            if (resultString.equals("")) {
                Utils.showToast(CaptureActivity.this, "Scan failed!");

            } else {
                String device = resultString;
                List<UserEntity> devList = IMDeviceManager.instance().loadDevice();
                boolean isDev = false;
                for (int i = 0; i < devList.size(); i++) {
                    if (devList.get(i).getRealName().equals(device)) {
                        isDev = true;
                        break;
                    }
                }

                if (isDev == false) {
                    IMDeviceManager.instance().addDevice(device, ManageType.MANAGE_TYPE_ADD_DEVICE, null);
                } else {
                    Utils.showToast(CaptureActivity.this, "你已经添加该设备");
                }
            }

            return;
        }


        //个人与群
        if (resultString.equals("")) {
            Utils.showToast(CaptureActivity.this, "Scan failed!");
        } else {
            Intent intent = getIntent();
            intent.putExtra("codedContent", result.getText());
            // intent.putExtra("codedBitmap", barcode);
            setResult(RESULT_OK, intent);

            String infoUrl = IMContactManager.instance().getSystemConfig().getWebsite();
            if (resultString.indexOf(infoUrl) != -1) {

                String peerIdString = resultString.replaceAll(infoUrl, "");
                String desId = new String(Security.getInstance().DecryptMsg(peerIdString));


                if (desId.startsWith("wgid=")) {

                    String name = desId.substring("wgid=".length());
                    type = DBConstant.SESSION_TYPE_GROUP;

                    int curGroupId = Integer.parseInt(name);
                    Intent intent1 = new Intent(CaptureActivity.this,
                            ScanGroupQRActivity.class);
                    intent1.putExtra(IntentConstant.QR_GROUP_ID, curGroupId);
                    CaptureActivity.this.startActivity(intent1);
                    CaptureActivity.this.finish();

                } else if (desId.indexOf("wuid=") != -1) {

                    String userName = desId.substring("wgid=".length());

                    type = DBConstant.SESSION_TYPE_SINGLE;
                    boolean result1 = userName.matches("[0-9]+");
                    if (result1) {
//                        UserEntity contact = IMContactManager.instance()
//                                .getSearchContact(userName);
//                        if (contact == null){
//                            contact = IMContactManager.instance().findDeviceContact(Integer.parseInt(userName));
//                        }

                        UserEntity contact = IMContactManager.instance().findDeviceContact(Integer.parseInt(userName));
                        if (contact != null) {
                            IMUIHelper.openUserProfileActivity(CaptureActivity.this, contact.getPeerId(), true);
                            CaptureActivity.this.finish();
                        } else {
                            contact = IMContactManager.instance().findParentContact(Integer.parseInt(userName));
                            if (contact != null) {
                                IMUIHelper.openUserProfileActivity(CaptureActivity.this, contact.getPeerId(), true);
                                CaptureActivity.this.finish();
                            } else {

                                contact = IMContactManager.instance().getSearchContact(userName);
                                if (contact == null) {
                                    currId = Integer.parseInt(userName);
                                    ArrayList<Integer> userIds = new ArrayList<>(1);
                                    // just single type
                                    userIds.add(currId);
                                    IMContactManager.instance().reqGetDetaillUsers(userIds);
                                    reUserInfo = true;
                                } else {
                                    // contact.setFriend(1);
                                    IMUserActionManager.instance().setSearchInfo(contact);

                                    Intent intent1 = new Intent(CaptureActivity.this,
                                            SearchFriednsActivity.class);
                                    CaptureActivity.this.startActivity(intent1);
                                    CaptureActivity.this.finish();
                                }
                            }
                        }

                    } else {

                        if (isHttp(resultString)) {
                            //打开url
                            Intent intentUrl = new Intent(CaptureActivity.this, WebViewActivity.class);
                            intentUrl.putExtra(IntentConstant.WEB_URL, resultString);
                            intentUrl.putExtra(IntentConstant.WEB_URL_RETURN, "返回");

                            CaptureActivity.this.startActivity(intentUrl);
                            CaptureActivity.this.finish();
                        } else {
                            Intent intentUrl = new Intent(CaptureActivity.this, WebViewTextActivity.class);
                            intentUrl.putExtra(IntentConstant.WEB_URL, resultString);
                            intentUrl.putExtra(IntentConstant.WEB_URL_RETURN, "返回");

                            CaptureActivity.this.startActivity(intentUrl);
                            CaptureActivity.this.finish();
                        }
                    }

                } else {

                    if (isHttp(resultString)) {
                        //打开url
                        Intent intentUrl = new Intent(CaptureActivity.this, WebViewActivity.class);
                        intentUrl.putExtra(IntentConstant.WEB_URL, resultString);
                        intentUrl.putExtra(IntentConstant.WEB_URL_RETURN, "返回");
                        CaptureActivity.this.startActivity(intentUrl);
                        CaptureActivity.this.finish();
                    } else {
                        //打开url
                        Intent intentUrl = new Intent(CaptureActivity.this, WebViewTextActivity.class);
                        intentUrl.putExtra(IntentConstant.WEB_URL, resultString);
                        intentUrl.putExtra(IntentConstant.WEB_URL_RETURN, "返回");
                        CaptureActivity.this.startActivity(intentUrl);
                        CaptureActivity.this.finish();
                    }
                }

            } else {

                if (isHttp(resultString)) {
                    //打开url
                    Intent intentWeb = new Intent(CaptureActivity.this, DevWebViewActivity.class);
                    intentWeb.putExtra(IntentConstant.WEB_URL, resultString);
                    intentWeb.putExtra(IntentConstant.WEB_URL_RETURN, "返回");

                    CaptureActivity.this.startActivity(intentWeb);
                    CaptureActivity.this.finish();
                } else {
                    //打开url
                    Intent intentUrl = new Intent(CaptureActivity.this, WebViewTextActivity.class);
                    intentUrl.putExtra(IntentConstant.WEB_URL, resultString);
                    intentUrl.putExtra(IntentConstant.WEB_URL_RETURN, "返回");
                    CaptureActivity.this.startActivity(intentUrl);
                    CaptureActivity.this.finish();
                }
            }
        }
    }


    public static boolean isHttp(String htts) {

        String url = "[a-zA-z]+://[^\\s]*";
        Pattern pattern = Pattern.compile(url);
        return pattern.matcher(htts).matches();
    }


    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG,
                    "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager,
                        DecodeThread.ALL_MODE);
            }

            initCrop();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        // camera error

        final FilletDialog myDialog = new FilletDialog(CaptureActivity.this, FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE);
        myDialog.setTitle("相机打开出错，请稍后重试");//设置内容
        myDialog.dialog.show();//显示

        //确认按键回调，按下确认后在此做处理
        myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
            @Override
            public void ok() {
                finish();
                myDialog.dialog.dismiss();
            }
        });

    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

    public Rect getCropRect() {
        return mCropRect;
    }

    /**
     * 初始化截取的矩形区域
     */
    private void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().y;
        int cameraHeight = cameraManager.getCameraResolution().x;

        /** 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();

        /** 获取布局容器的宽高 */
        int containerWidth = scanContainer.getWidth();
        int containerHeight = scanContainer.getHeight();

        /** 计算最终截取的矩形的左上角顶点x坐标 */
        int x = cropLeft * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的左上角顶点y坐标 */
        int y = cropTop * cameraHeight / containerHeight;

        /** 计算最终截取的矩形的宽度 */
        int width = cropWidth * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的高度 */
        int height = cropHeight * cameraHeight / containerHeight;

        /** 生成最终的截取的矩形 */
        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode == RESULT_OK) {
            final ProgressDialog progressDialog;
            switch (requestCode) {
                case REQUEST_CODE:

                    break;
                case SCANNIN_GREQUEST_CODE: {// 扫描成功

                }
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {

    }
}
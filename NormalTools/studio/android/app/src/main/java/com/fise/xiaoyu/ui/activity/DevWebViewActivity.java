package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.manager.IMGroupManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.ProgressWebView;
import com.fise.xiaoyu.utils.CompatUtil;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 跳转到Url 界面
 */
@SuppressLint("NewApi")
public class DevWebViewActivity extends TTBaseActivity {

    private static final int WEAK_NETWORK_LOADING_FAIL = 001 ;
    private Logger logger = Logger.getLogger(DevWebViewActivity.class);
    private IMService imService;
    private ProgressWebView webView;
    private String url;
    private int type = 0;
    private int devId;
    public List<UserEntity> userList;
    private DeviceEntity device;
    private  UserEntity currentUser;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 2;
    private String mCameraFilePath ;
    private AnimationDrawable loginLoadingAni;
    private ImageView loginLoading;
    private ProgressBar mPb;
    private  Boolean isShowLoading =true;
    private  int mNewProgress;
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

                    userList = imService.getContactManager().getContactFriendsList();
                    if(devId>0){
                        device = imService.getDeviceManager().findDeviceCard(
                                devId);
                        currentUser = imService.getContactManager().findDeviceContact(
                                devId);
                        int deviceType = currentUser.getUserType();

                    }
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

   Handler mHandler = new Handler(){
       @Override
       public void handleMessage(Message msg) {
           super.handleMessage(msg);
           switch (msg.what){
               case WEAK_NETWORK_LOADING_FAIL:
                   if(mNewProgress != 100){
                       loginLoading.setVisibility(View.GONE);
                       loginLoadingAni.stop();
                       mPb.setVisibility(View.GONE);
                       showLoadingFailLayout();
                   }

                    timer.cancel();
                   break;

           }
       }
   };
    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_dev_webview);
        imServiceConnector.connect(DevWebViewActivity.this);

        type = getIntent().getIntExtra(IntentConstant.WEB_URL_TYPE,0);
        url = getIntent().getStringExtra(IntentConstant.WEB_URL);
        if(type == DBConstant.DEVICE_WEBVIEW_TYPE){
            devId = getIntent().getIntExtra(IntentConstant.KEY_PEERID,0);

        }

        webView = (ProgressWebView) findViewById(R.id.webView);
        loginLoading = (ImageView) findViewById(R.id.progress_loading_img);
        loginLoadingAni = (AnimationDrawable) loginLoading.getBackground();

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(true);
        findViewById(R.id.left_btn_tt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.left_txt_tt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        webView.addJavascriptInterface(DevWebViewActivity.this, "android");

        webView.getSettings().setJavaScriptEnabled(true);
        // 开启DOM缓存。
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDatabasePath(
                this.getApplicationContext().getCacheDir().getAbsolutePath());
        mPb = (ProgressBar) findViewById(R.id.progressBar_loading);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.i("aaa", "onReceivedError: ");
                showLoadingFailLayout();

            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                Log.i("aaa", "onReceivedSslError: ");
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){

            protected void openFileChooser(ValueCallback uploadMsg, String acceptType)
            {
                Log.i("aaa", "openFileChooser1: ");
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,  GeolocationPermissions.Callback callback) {

                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);

            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                super.onProgressChanged(view, newProgress);
                mPb.setProgress(newProgress);
                if(isShowLoading){
                    if(newProgress == 100){
                        loginLoading.setVisibility(View.GONE);
                        loginLoadingAni.stop();
                        mPb.setVisibility(View.GONE);
                        isShowLoading = false;
                    }else{
                        loginLoading.setVisibility(View.VISIBLE);
                        loginLoadingAni.start();
                    }
                }
                mNewProgress = newProgress;
                Log.i("aaa", "onProgressChanged: "+newProgress);
            }

            // For Lollipop 5.0+ Devices
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                Log.i("aaa", "onShowFileChooser: ");
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try
                {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    uploadMessage = null;
                    Toast.makeText(getBaseContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
            {
                Log.i("aaa", "openFileChooser: ");
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg)
            {
                Log.i("aaa", "openFileChooser2: ");
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }


            private Intent createDefaultOpenableIntent() {
                // Create and return a chooser with the default OPENABLE
                // actions including the camera, camcorder and sound
                // recorder where available.
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");

                Intent chooser = createChooserIntent(createCameraIntent(),
                        createCamcorderIntent(), createSoundRecorderIntent());
                chooser.putExtra(Intent.EXTRA_INTENT, i);
                return chooser;
            }

            private Intent createChooserIntent(Intent... intents) {
                Intent chooser = new Intent(Intent.ACTION_CHOOSER);
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
                chooser.putExtra(Intent.EXTRA_TITLE, "File Chooser");
                return chooser;
            }

            private Intent createCameraIntent() {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File externalDataDir = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                System.out.println("externalDataDir:" + externalDataDir);
                File cameraDataDir = new File(externalDataDir.getAbsolutePath()
                        + File.separator + "browser-photo");
                cameraDataDir.mkdirs();
                mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator
                        + System.currentTimeMillis() + ".jpg";
                System.out.println("mcamerafilepath:" + mCameraFilePath);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, CompatUtil.getUriForFile(DevWebViewActivity.this,new File(mCameraFilePath)));


                return cameraIntent;
            }

            private Intent createCamcorderIntent() {
                return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            }

            private Intent createSoundRecorderIntent() {
                return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
            }

        });

        webView.loadUrl(url);
       //启动定时器
        timer = new Timer();
        timer.schedule(task, 10000, 10000);       // timeTask
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {      // UI thread
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(WEAK_NETWORK_LOADING_FAIL);
                }
            });
        }
    };

    private void showLoadingFailLayout() {
        findViewById(R.id.loading_fail_layout).setVisibility(View.VISIBLE);
    }

    // 鐢变簬瀹夊叏鍘熷洜 闇?瑕佸姞 @JavascriptInterface
    @JavascriptInterface
    public String jsCall_DevLoginInfo() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", imService.getLoginManager().getLoginId());
            jsonObject.put("device_id", devId);
            // jsonObject.put("luanmeng",5);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();

    }

    @JavascriptInterface
    public void jsCall_CallDev(String phone) {

        if(currentUser.getPhone().equals("")){

            Utils.showToast(DevWebViewActivity.this,"号码为空");
        }else{
            Intent intent =new Intent();
            intent.setAction(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + currentUser.getPhone()));
            DevWebViewActivity.this.startActivity(intent);
        }

    }




    // 鐢变簬瀹夊叏鍘熷洜 闇?瑕佸姞 @JavascriptInterface
    @JavascriptInterface
    public void jsCall_ContactDev() {

        GroupEntity group = IMGroupManager.instance().findFamilyGroup(device.getFamilyGroupId());
        if(group!=null){
            IMUIHelper.openChatActivity(DevWebViewActivity.this, group.getSessionKey() );
        }

    }

    @JavascriptInterface
    public void jsCall_go_back() {
        DevWebViewActivity.this.finish();


    }

    // 鐢变簬瀹夊叏鍘熷洜 闇?瑕佸姞 @JavascriptInterface
    @JavascriptInterface
    public String jsCall_AvatarDev() {
        String url = "";

        UserEntity loginContact = IMLoginManager.instance()
                .getLoginInfo();
        if (device.getMasterId() == loginContact.getPeerId()) {

            Intent intent = new Intent(DevWebViewActivity.this,
                    HeadPortraitActivity.class);
            intent.putExtra(IntentConstant.KEY_AVATAR_URL,
                    currentUser.getAvatar());
            intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR,
                    true);

            intent.putExtra(IntentConstant.KEY_NICK_MODE,
                    DBConstant.DEVICE_NICK);
            intent.putExtra(IntentConstant.KEY_PEERID, devId);

            startActivity(intent);

        }

        return url;
    }

    // 鐢变簬瀹夊叏鍘熷洜 闇?瑕佸姞 @JavascriptInterface
    @JavascriptInterface
    public String jsCall_DevFriendsInfo(String param) {

        JSONObject jsonInfo;
        JSONArray json = new JSONArray();
        for (int i = 0; i < userList.size(); i++) {
            UserEntity info = userList.get(i);
            jsonInfo = new JSONObject();
            try {
                jsonInfo.put("friend_id", info.getPeerId());
                jsonInfo.put("avatar", info.getUserAvatar()); //getAvatar
                jsonInfo.put("nickname", info.getRealName());
                jsonInfo.put("tel", info.getPhone());
                json.put(jsonInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json.toString();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {

        Log.i("aaa", "onActivityResult: "+requestCode);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode == REQUEST_SELECT_FILE)
            {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
        else if (requestCode == FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
        else
            Toast.makeText(getBaseContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(DevWebViewActivity.this);
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


}

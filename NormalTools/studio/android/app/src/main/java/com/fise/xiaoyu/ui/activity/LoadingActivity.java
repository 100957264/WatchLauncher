package com.fise.xiaoyu.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.fise.xiaoyu.DB.entity.SystemConfigEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.LoginSp;
import com.fise.xiaoyu.DB.sp.SystemConfigSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.app.IMApplication;
import com.fise.xiaoyu.app.UpdateDetection;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.UrlConstant;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.SocketEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.helper.ProtoBuf2JavaBean;
import com.fise.xiaoyu.ui.base.TTGuideBaseActivity;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.HttpUtil;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.PermissionUtil;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 启动Activity
 */
public class LoadingActivity extends TTGuideBaseActivity {
    private Button btn_login;
    private boolean autoLogin = true;
    private Handler uiHandler = new Handler();
    private Button mRegist;
    private IMService imService;
    private boolean loginSuccess = false;

    public final int GOLOGIN = 103;
    public final int GORIGET = 104;

    public final int TIMEOUT = 101; // 请求超时
    public final int POSTSYSTEM = 304; // stystem 系统数据请求失败
    public final int ADVERTISEMENT = 0;// 广告启动图
    public final int GUIDERIGHT = 102; // guide

    private String advertisement;
    private String advertisementUrl;
    private String updateUrl;

    private String versionNew;
    private String versionApp;
    private int advTime;
    private int loadingTime = 2 * 1000;
    private boolean guide;
    private IMBaseImageView activity_bg;
    private String imei = "";
    private int clientType = 0;

    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GOLOGIN: {
                    Intent intent = new Intent(LoadingActivity.this,
                            LoginActivity.class);
                    startActivity(intent);
                    LoadingActivity.this.finish();
                }
                break;
                case GORIGET: {
                    Intent intent = new Intent(LoadingActivity.this,
                            LoginActivity.class);
                    startActivity(intent);
                    LoadingActivity.this.finish();
                }
                break;

                case POSTSYSTEM: {
                    autoLogin();
                }
                break;

                case TIMEOUT: {
                    autoLogin();
                }
                break;

                case GUIDERIGHT: {

                    if (clientType == DBConstant.CLIENTTYPE) {

                        Context ctx = LoadingActivity.this;
                        SharedPreferences sp = ctx.getSharedPreferences("SharedGuide",
                                MODE_PRIVATE);
                        Editor editor = sp.edit();
                        editor.putBoolean("guide", false);
                        editor.commit();

                        Intent intent = new Intent(LoadingActivity.this,
                                LoginActivity.class);
                        LoadingActivity.this.startActivity(intent);
                        LoadingActivity.this.finish();

                    } else {

                        guide = true;
                        Context ctx = LoadingActivity.this;
                        SharedPreferences sp = ctx.getSharedPreferences("SharedGuide",
                                MODE_PRIVATE);
                        Editor editor = sp.edit();
                        editor.putBoolean("guide", true);
                        editor.commit();

                        Intent intent = new Intent(LoadingActivity.this,
                                GuideActivity.class);
                        LoadingActivity.this.startActivity(intent);
                        LoadingActivity.this.finish();
                    }

                }
                break;

                case ADVERTISEMENT: {

                    if (advertisement == null || advertisement.equals("")) {

                        // 当前版本小于最低版本 强制更新
                        if (IMApplication.getApplication().getVersion().compareTo(versionApp) < 0) {
                            dialog();
                        } else {
                            autoLogin();
                        }

                    } else {

                        // activity_bg.setBackgroundResource(R.drawable.loading);
                        activity_bg.setImageUrl(advertisement);
                        activity_bg
                                .setOnClickListener(new Button.OnClickListener() {// 创建监听
                                    public void onClick(View v) {

                                        if (advertisementUrl != null
                                                && (!advertisementUrl.equals(""))) {
                                            Intent intent = new Intent();
                                            intent.setAction("android.intent.action.VIEW");
                                            Uri content_url = Uri
                                                    .parse(advertisementUrl);
                                            intent.setData(content_url);
                                            startActivity(intent);
                                        }

                                    }

                                });

                        uiHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                // 当前版本小于最低版本 强制更新
                                if (IMApplication.getApplication().getVersion().compareTo(versionApp) < 0) {
                                    dialog();
                                } else {
                                    autoLogin();
                                }
                            }
                        }, 1000 * advTime);
                    }

                }
                break;

            }
            super.handleMessage(msg);
        }
    };

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {

        }

        @Override
        public void onIMServiceConnected() {
            logger.d("login#onIMServiceConnected");
            imService = imServiceConnector.getIMService();

            if (imService == null)
                return;

            logger.e("loading error");

            JSONObject param = new JSONObject();
            String md5Imei = new String(com.fise.xiaoyu.Security.getInstance()
                    .EncryptPass(imei + "fise_zn_xw@fise.com.cn"));
            try {
                param.put("app_dev", imei);
                param.put("app_key", md5Imei);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestBody requestBody = RequestBody.create(HttpUtil.JSON,
                    param.toString());

            OkHttpClient mOkHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(UrlConstant.APP_TYPE_URL).post(requestBody).build();

            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    autoLogin();
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws IOException {
                    String result = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(result);

                        String errorInfo = "";
                        if (!jsonObject.isNull("ErrorInfo")) {
                            errorInfo = jsonObject.getString("ErrorInfo");
                        }

                        if (!jsonObject.isNull("ClientType")) {
                            clientType = jsonObject.getInt("ClientType");
                        }

                        if (errorInfo.equals("success")
                                && (clientType == DBConstant.CLIENTTYPE)) {

                            uiHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    postSystemConf(imei, DBConstant.CLIENTTYPE);
                                }
                            }, loadingTime);


                        } else {
                            uiHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    postSystemConf(imei, DBConstant.ANDROIDTYPE);
                                }
                            }, loadingTime);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();

                        uiHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                postSystemConf(imei, DBConstant.ANDROIDTYPE);
                            }
                        }, loadingTime);

                    }
                }
            });
        }
    };

    /**
     * 登陆接口
     */
    private void autoLogin() {

        IMLoginManager loginManager = imService.getLoginManager();
        LoginSp loginSp = imService.getLoginSp();
        if (loginManager == null || loginSp == null) {
            // 无法获取登陆控制器
            GotRegisterIdentity();
            return;
        }

        LoginSp.SpLoginIdentity loginIdentity = loginSp.getLoginIdentity();
        if (loginIdentity == null) {
            // 之前没有保存任何登陆相关的，跳转到登陆页面
            GotRegisterIdentity();
            return;
        }
        if (TextUtils.isEmpty(loginIdentity.getPwd())) {
            // 密码为空，可能是loginOut

            GotLoginIdentity();
            return;
        }

        // 调整到小位界面
        handleGotLoginIdentity(loginIdentity);
    }

    /**
     * 自动登录
     */
    private void handleGotLoginIdentity(
            final LoginSp.SpLoginIdentity loginIdentity) {

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (imService == null || imService.getLoginManager() == null) {
                }

                imService.getLoginManager().login(loginIdentity);
            }
        }, 500);
    }

    /**
     * 跳转到登录界面
     */
    private void GotLoginIdentity() {

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Message message = new Message();
                message.what = GOLOGIN;
                LoadingActivity.this.myHandler.sendMessage(message);
            }
        }, loadingTime);
    }

    /**
     * 跳转到登录注册界面
     */
    private void GotRegisterIdentity() {

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Message message = new Message();
                message.what = GORIGET;
                LoadingActivity.this.myHandler.sendMessage(message);
            }
        }, loadingTime);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.loading_activity);

        activity_bg = (IMBaseImageView) this.findViewById(R.id.bg);
        if (activity_bg != null) {
            activity_bg.setDefaultImageRes(R.drawable.loading);
        }

        Context ctx = LoadingActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("SharedGuide", MODE_PRIVATE);
        guide = sp.getBoolean("guide", false);

        SystemConfigSp.instance().init(getApplicationContext());

        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(
                SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(
                    SystemConfigSp.SysCfgDimension.LOGINSERVER,
                    UrlConstant.ACCESS_MSG_ADDRESS);
        }

        requestRunPermisssion(Manifest.permission.READ_PHONE_STATE, new PermissionListener() {
            @Override
            public void onGranted() {
                TelephonyManager telephonyManager = (TelephonyManager) LoadingActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    imei = telephonyManager.getDeviceId();
                }
                if (Logger.DEBUG_FORCE_ANDROID_LOGIN) {
                    imei = "";
                }
                initConnector();
            }

            @Override
            public void onDenied(List<String> deniedPermission) {
                imei = "";
                initConnector();
            }
        });
    }

    private void initConnector() {
        imServiceConnector.connect(LoadingActivity.this);
    }

    /**
     * @param imei
     * @param devType
     */
    void postSystemConf(final String imei, final int devType) {

        new Thread() {
            @Override
            public void run() {
                String url = UrlConstant.ACCESS_MSG_SYSTEM;
                OkHttpClient okHttpClient = new OkHttpClient();
                try {
                    String md5Imei = new String(Security.getInstance()
                            .EncryptPass(imei + "fise_zn_xw@fise.com.cn"));
                    JSONObject param = new JSONObject();
                    param.put("app_dev", imei);
                    param.put("app_key", md5Imei);
                    param.put("client_type", "android");
                    RequestBody requestBody = RequestBody.create(HttpUtil.JSON,
                            param.toString());
                    Request request = new Request.Builder().url(url)
                            .post(requestBody).build();
                    Response response = okHttpClient.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        JSONObject json = new JSONObject(result);

                        String error_code = json.getString("error_code");

                        if (error_code.equals("0")) {
                            String launch = "";
                            String launchAction = "";
                            if (!json.isNull("launch")) {
                                JSONObject launchJson = json
                                        .getJSONObject("launch");
                                launch = launchJson.getString("value");

                                if (!json.isNull("action")) {
                                    launchAction = launchJson
                                            .getString("action");
                                }
                            }

                            int launch_time = json.getJSONObject("launch_time")
                                    .getInt("value");
                            String comment_url = json.getJSONObject(
                                    "comment_url").getString("value");
                            String system_notice = json.getJSONObject(
                                    "system_notice").getString("value");
                            String update_url = json
                                    .getJSONObject("update_url").getString(
                                            "value");

                            String version_support = json.getJSONObject(
                                    "version_support").getString("value");

                            String website = json.getJSONObject("website")
                                    .getString("value");
                            String version = json.getJSONObject("version")
                                    .getString("value");

                            String version_comment = json.getJSONObject(
                                    "version_comment").getString("value");

                            String adviceFeedbackUrl = json.getJSONObject(
                                    "suggest_url").getString("value");

                            String mallUrl = "";
                            if (!json.isNull("mall_url")) {
                                mallUrl = json.getJSONObject("mall_url")
                                        .getString("value");
                            }

                            SystemConfigEntity entityTemp = ProtoBuf2JavaBean
                                    .getSystemConfigEntity(launch, launch_time,
                                            launchAction, system_notice,
                                            update_url, website,
                                            version_support, comment_url,
                                            version, version_comment,
                                            adviceFeedbackUrl, mallUrl);

                            imService.getContactManager().setsystemConfig(
                                    entityTemp);

                            advertisement = launch;
                            advertisementUrl = launchAction;
                            updateUrl = update_url;
                            versionNew = version;
                            versionApp = version_support;
                            advTime = launch_time;


                            if (devType == DBConstant.CLIENTTYPE) {

                                imService.getLoginManager().login(imei,
                                        DBConstant.DEVICE_PASSWORD, imei,
                                        clientType);
                            } else {
                                if (guide) {
                                    Message message = new Message();
                                    message.what = ADVERTISEMENT;
                                    LoadingActivity.this.myHandler
                                            .sendMessage(message);
                                } else {
                                    Message message = new Message();
                                    message.what = GUIDERIGHT; // guide
                                    LoadingActivity.this.myHandler
                                            .sendMessage(message);
                                }
                            }

                        } else {

                            Message message = new Message();
                            message.what = POSTSYSTEM;
                            LoadingActivity.this.myHandler.sendMessage(message);
                        }

                    } else {

                        Message message = new Message();
                        message.what = POSTSYSTEM;
                        LoadingActivity.this.myHandler.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = POSTSYSTEM;
                    LoadingActivity.this.myHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = POSTSYSTEM;
                    LoadingActivity.this.myHandler.sendMessage(message);
                }
            }
        }.start();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        imServiceConnector.disconnect(LoadingActivity.this);

    }

    // 主动退出的时候， 这个地方会有值,更具pwd来判断
    private boolean shouldAutoLogin() {
        SharedPreferences read = getSharedPreferences(
                IntentConstant.KEY_LOGIN_NOT_AUTO, Activity.MODE_PRIVATE);
        return read.getBoolean("login_not_auto", false);
    }

    // 为什么会有两个这个
    // 可能是 兼容性的问题 导致两种方法onBackPressed
    @Override
    public void onBackPressed() {
        // imLoginMgr.cancel();
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkedPermission) {
            checkedPermission = false;
            checkPermission();
        }
    }

    /**
     * ----------------------------event 事件驱动----------------------------
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginEvent event) {
        switch (event) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
                // onLoginSuccess();
                // Intent intent = new Intent(LoadingActivity.this,
                // LoginProtectionActivity.class);
                // intent.putExtra(IntentConstant.KEY_REGIST_NAME,
                // imService.getLoginManager().getLoginInfo().getPhone());
                // startActivity(intent);
                // LoadingActivity.this.finish();

                onLoginSuccess();
                break;

            case LOGIN_AUTH_DEVICE: {

                IMLoginManager loginManager = imService.getLoginManager();
                LoginSp loginSp = imService.getLoginSp();
                if (loginManager == null || loginSp == null) {
                    return;
                }

                Intent intent = new Intent(LoadingActivity.this,
                        LoginProtectionActivity.class);
                intent.putExtra(IntentConstant.KEY_REGIST_NAME, loginSp
                        .getLoginIdentity().getLoginName());
                intent.putExtra(IntentConstant.KEY_LOGIN_PASS, loginSp
                        .getLoginIdentity().getPwd());
                intent.putExtra(IntentConstant.KEY_LOGIN_IMEI, loginSp
                        .getLoginIdentity().getImei());

                startActivity(intent);
                LoadingActivity.this.finish();
                // imService.getLoginManager().login(loginIdentity);
            }
            break;

            case FORCE_FAILED:
                if (!loginSuccess)
                    onLoginFailure(event);
                break;
            case LOGIN_AUTH_FAILED:
            case LOGIN_INNER_FAILED:
                if (!loginSuccess)
                    onLoginFailure(event);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SocketEvent event) {
        switch (event) {
            case CONNECT_MSG_SERVER_FAILED:
            case REQ_MSG_SERVER_ADDRS_FAILED:
                if (!loginSuccess)
                    onSocketFailure(event);
                break;
        }
    }

    private void onLoginSuccess() {
        loginSuccess = true;
        checkPermission();
    }

    private void goMainActivity() {
        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
        startActivity(intent);
        LoadingActivity.this.finish();
    }

    private boolean checkedPermission = false;

    private void checkPermission() {
        UserEntity userEntity = IMLoginManager.instance().getLoginInfo();
        String[] permissions = null;
        if (userEntity != null && Utils.isClientType(userEntity)) {
            permissions = PermissionUtil.ALL_PERMISSION_GROUP;
        } else {
            permissions = PermissionUtil.MUST_PERMISSION_GROUP;
        }
        requestRunPermisssion(permissions, new PermissionListener() {
            @Override
            protected void onGranted() {
                goMainActivity();
            }

            @Override
            protected void onDenied(List<String> deniedPermission) {
                if (!PermissionUtil.lackPermission(deniedPermission.get(0))) {
                    onGranted();
                    return;
                }
                StringBuffer sb = new StringBuffer("权限不足:\n");
                sb.append(PermissionUtil.getPermissionString(LoadingActivity.this, deniedPermission));
                // for (String permission : deniedPermission) {
                // 	sb.append("\n").append(permission);
                // }
                final FilletDialog myDialog = new FilletDialog(
                        LoadingActivity.this,
                        FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE);
                myDialog.setTitle(sb.toString());// 设置内容
                myDialog.setRight("设置权限");
                myDialog.setLeft("退出");
                myDialog.setFinsh(true);
                myDialog.dialog.show();// 显示

                // 确认按键回调，按下确认后在此做处理
                myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
                    @Override
                    public void ok() {
                        PermissionUtil.getAppDetailSettingIntent(LoadingActivity.this);
                        checkedPermission = true;
                    }
                });
            }

            @Override
            protected boolean onConvert() {
                return super.onConvert();
            }
        });
    }

    public void dialog() {

        final FilletDialog myDialog = new FilletDialog(LoadingActivity.this,
                FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITH_MESSAGE);
        myDialog.setFinsh(true);
        myDialog.setTitle(getString(R.string.device_prompt));
        myDialog.setMessage("当前版本为:" + IMApplication.getApplication().getVersion() + " " + "系统最新版本为: "
                + versionNew + "   如果不更新 小雨APP 将不能使用");// 设置内容
        myDialog.dialog.show();// 显示

        // 确认按键回调，按下确认后在此做处理
        myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
            @Override
            public void ok() {

                // Intent intent = new Intent();
                // intent.setAction("android.intent.action.VIEW");
                // Uri content_url = Uri.parse(updateUrl);
                // intent.setData(content_url);
                // startActivity(intent);
                UpdateDetection update = new UpdateDetection(
                        LoadingActivity.this, updateUrl);
                update.showNoticeDialog();

                myDialog.dialog.dismiss();

            }
        });

    }

    private void openApplicationMarket(String url) { // packageName

        try {
            Intent localIntent = new Intent(Intent.ACTION_VIEW);
            localIntent.setData(Uri.parse(url));
            this.startActivity(localIntent);
        } catch (Exception e) {
            // 打开应用商店失败 可能是没有手机没有安装应用市场
            e.printStackTrace();
            Utils.showToast(LoadingActivity.this, "打开应用商店失败");
        }

    }

    private void onLoginFailure(LoginEvent event) {

    }

    private void onSocketFailure(SocketEvent event) {

        LoginSp loginSp = imService.getLoginSp();
        if (loginSp == null) {
            return;
        }
        LoginSp.SpLoginIdentity loginIdentity = loginSp.getLoginIdentity();
        if (loginIdentity == null) {
            // 之前没有保存任何登录相关的，跳转到登录页面
            return;
        }
        if (TextUtils.isEmpty(loginIdentity.getPwd())) {
            // 密码为空，可能是loginOut
            return;
        }

        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
        startActivity(intent);
        LoadingActivity.this.finish();
    }
}

package com.fise.xiaoyu.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.LoginSp;
import com.fise.xiaoyu.DB.sp.SystemConfigSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.app.IMApplication;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.UrlConstant;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.SocketEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.ui.base.ActivityManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.utils.HttpUtil;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.PermissionUtil;
import com.fise.xiaoyu.utils.StatusBarUtil;
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
 * @YM 1. 链接成功之后，直接判断是否loginSp是否可以直接登录 true: 1.可以登录，从DB中获取历史的状态
 *     2.建立长连接，请求最新的数据状态 【网络断开没有这个状态】 3.完成
 *     <p/>
 *     false:1. 不能直接登录，跳转到登录页面 2. 请求消息服务器地址，链接，验证，触发loginSuccess 3. 保存登录状态
 */
@SuppressLint("NewApi")
public class LoginActivity extends TTBaseActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private Logger logger = Logger.getLogger(LoginActivity.class);
    private Handler uiHandler = new Handler();
    private EditText mNameView;
    private EditText mPasswordView;
    private View loginPage;
    // private View mLoginStatusView;
    private TextView mSwitchLoginServer;
    private InputMethodManager intputManager;
    private Button sign_in_button;

    private int inputNum;

    private IMService imService;
    private boolean autoLogin = true;
    private boolean loginSuccess = false;
    private String imei = "";
    private FilletDialog myDialog;
    private int clientType = 0;

    private final int SETTTINGNAME = 505;
    private final int PASSWORD = 506;
    public final static int REQUEST_READ_PHONE_STATE = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SETTTINGNAME:
                    mNameView.setText((String) msg.obj);
                    break;
                case PASSWORD:
                    mPasswordView.setText((String) msg.obj);
                    break;
            }
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
            try {
                do {
                    if (imService == null) {
                        // 后台服务启动链接失败
                        break;
                    }

                    JSONObject param = new JSONObject();
                    String md5Imei = new String(com.fise.xiaoyu.Security
                            .getInstance().EncryptPass(
                                    imei + "fise_zn_xw@fise.com.cn"));
                    param.put("app_dev", imei);
                    param.put("app_key", md5Imei);
                    RequestBody requestBody = RequestBody.create(HttpUtil.JSON,
                            param.toString());

                    OkHttpClient mOkHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(UrlConstant.APP_TYPE_URL).post(requestBody)
                            .build();

                    mOkHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Login();
                        }

                        @Override
                        public void onResponse(Call call, Response response)
                                throws IOException {
                            String result = response.body().string();
                            try {
                                JSONObject jsonObject = new JSONObject(result);

                                String errorInfo = "";

                                if (!jsonObject.isNull("ErrorInfo")) {
                                    errorInfo = jsonObject
                                            .getString("ErrorInfo");
                                }

                                if (!jsonObject.isNull("ClientType")) {
                                    clientType = jsonObject
                                            .getInt("ClientType");
                                }

                                if (errorInfo.equals("success")
                                        && (clientType == DBConstant.CLIENTTYPE)) {
                                    imService.getLoginManager().login(imei,
                                            DBConstant.DEVICE_PASSWORD, imei,
                                            clientType);
                                } else {
                                    Login();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Login();
                            }

                        }
                    });

					/*

                    */
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

    private void Login() {

        IMLoginManager loginManager = imService.getLoginManager();
        LoginSp loginSp = imService.getLoginSp();
        if (loginManager == null || loginSp == null) {
            // 无法获取登陆控制器
            return;
        }

        LoginSp.SpLoginIdentity loginIdentity = loginSp.getLoginIdentity();
        if (loginIdentity == null) {
            // 之前没有保存任何登陆相关的，跳转到登陆页面
            return;
        }

        Message message = new Message();
        message.what = SETTTINGNAME;
        message.obj = loginIdentity.getLoginName();
        LoginActivity.this.mHandler.sendMessage(message);

        if (TextUtils.isEmpty(loginIdentity.getPwd())) {
            // 密码为空，可能是loginOut
            return;
        }
        // mPasswordView.setText(loginIdentity.getPwd());
        Message message1 = new Message();
        message1.what = PASSWORD;
        message1.obj = loginIdentity.getPwd();
        LoginActivity.this.mHandler.sendMessage(message1);

        if (autoLogin == false) {
            return;
        }

        handleGotLoginIdentity(loginIdentity);
    }

    private int kickOutDevType;
    private ImageView loginLoading;
    private AnimationDrawable loginLoadingAni;
    private RelativeLayout rootRl;
    private String currentInputMethod;
    private int phoneMode;
    private String manuFacturer;

    /**
     * 自动登陆
     */
    private void handleGotLoginIdentity(
            final LoginSp.SpLoginIdentity loginIdentity) {
        logger.i("login#handleGotLoginIdentity");

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logger.d("login#start auto login");
                if (imService == null || imService.getLoginManager() == null) {
                    Utils.showToast(LoginActivity.this,
                            getString(R.string.login_failed));
                    showLoginPage();
                }

                imService.getLoginManager().login(loginIdentity);
            }
        }, 500);
    }

    private void showLoginPage() {
        loginPage.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        imServiceConnector.connect(LoginActivity.this);

        intputManager = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        logger.d("login#onCreate");

        SystemConfigSp.instance().init(getApplicationContext());
        currentInputMethod = Settings.Secure.getString(
                LoginActivity.this.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(
                SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(
                    SystemConfigSp.SysCfgDimension.LOGINSERVER,
                    UrlConstant.ACCESS_MSG_ADDRESS);
        }
        setContentView(R.layout.tt_activity_login);
        manuFacturer = android.os.Build.MANUFACTURER;
        phoneMode = StatusBarUtil.StatusBarLightMode(LoginActivity.this);

        requestRunPermisssion(Manifest.permission.READ_PHONE_STATE, new PermissionListener() {
            @Override
            public void onGranted() {
                TelephonyManager telephonyManager = (TelephonyManager) LoginActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    imei = telephonyManager.getDeviceId();
                }
                if (Logger.DEBUG_FORCE_ANDROID_LOGIN) {
                    imei = "";
                }
            }

            @Override
            public void onDenied(List<String> deniedPermission) {
                imei = "";
            }
        });

        initAutoLogin();
        mSwitchLoginServer = (TextView) findViewById(R.id.sign_switch_login_server);
        mSwitchLoginServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final FilletDialog myDialog = new FilletDialog(
                        LoginActivity.this,
                        FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE);
                myDialog.setTitle("忘记密码?");// 设置内容
                myDialog.setRight("找回密码");
                myDialog.dialog.show();// 显示

                // 确认按键回调，按下确认后在此做处理
                myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
                    @Override
                    public void ok() {
                        Intent intent = new Intent(LoginActivity.this,
                                BlackPassName.class);
                        startActivity(intent);
                        myDialog.dialog.dismiss();
                    }
                });
            }

        });

        inputNum = 0;
        mNameView = (EditText) findViewById(R.id.name);
        mPasswordView = (EditText) findViewById(R.id.password);

        mNameView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    //
                    // 当键盘弹出隐藏的时候会 调用此方法。
                    @Override
                    public void onGlobalLayout() {
                        // 获取底部状态栏的高度
                        if (!mNameView.hasFocus()) {
                            return;
                        }

                        Resources resources = LoginActivity.this.getResources();
                        int resourceId = resources.getIdentifier(
                                "navigation_bar_height", "dimen", "android");
                        int navigation_bar_height = resources
                                .getDimensionPixelSize(resourceId);
                        Rect r = new Rect();
                        LoginActivity.this.getWindow().getDecorView()
                                .getWindowVisibleDisplayFrame(r);
                        // 获取屏幕的高度
                        int screenHeight = LoginActivity.this.getWindow()
                                .getDecorView().getRootView().getHeight();
                        // 此处就是用来获取键盘的高度的， 在键盘没有弹出的时候 此高度为0 键盘弹出的时候为一个正数

                        int keyboardHeightTemp = 0;

                        // if(phoneMode == 2 ||
                        // manuFacturer.contains("Xiaomi")||
                        // (manuFacturer.contains("samsung"))){
                        // keyboardHeightTemp = screenHeight - r.bottom ;
                        // }else
                        if (manuFacturer.contains("HUAWEI")) {
                            keyboardHeightTemp = screenHeight - r.bottom
                                    - navigation_bar_height;
                        } else {
                            keyboardHeightTemp = screenHeight - r.bottom;
                        }

                        if (keyboardHeightTemp > navigation_bar_height) {

                            SystemConfigSp.instance().setIntConfig(
                                    currentInputMethod, keyboardHeightTemp);

                            // 隐藏deviceView
                            Log.i("aaa", "onGlobalLayout: " + "显示键盘: "
                                    + keyboardHeightTemp);

                        }

                    }

                });

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id,
                                                  KeyEvent keyEvent) {

                        if (id == R.id.login || id == EditorInfo.IME_NULL) {
                            attemptLogin();
                            return true;
                        }
                        return false;
                    }
                });

        TextView switch_register_server = (TextView) findViewById(R.id.switch_register_server);
        switch_register_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,
                        RegistActivityName.class);
                startActivity(intent);
            }

        });

        // mLoginStatusView = findViewById(R.id.login_status);
        loginLoading = (ImageView) findViewById(R.id.login_loading_img);
        loginLoadingAni = (AnimationDrawable) loginLoading.getBackground();
        sign_in_button = (Button) findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputNum > 0) {
                    intputManager.hideSoftInputFromWindow(
                            mPasswordView.getWindowToken(), 0);
                    attemptLogin();
                }
            }
        });
        rootRl = (RelativeLayout) findViewById(R.id.login_layout_root);
        if (inputNum > 0) {
            sign_in_button.setBackgroundResource(R.drawable.button_normal);
        } else {
            sign_in_button.setBackgroundResource(R.drawable.button_disabled);
        }

        mNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                inputNum = s.length();
                if (inputNum > 0) {
                    sign_in_button
                            .setBackgroundResource(R.drawable.button_normal);
                } else {
                    sign_in_button
                            .setBackgroundResource(R.drawable.button_disabled);
                }
            }
        });

    }

    private void initAutoLogin() {
        logger.i("login#initAutoLogin");

        loginPage = findViewById(R.id.login_page);
        autoLogin = shouldAutoLogin();

        loginPage.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mPasswordView != null) {
                    intputManager.hideSoftInputFromWindow(
                            mPasswordView.getWindowToken(), 0);
                }

                if (mNameView != null) {
                    intputManager.hideSoftInputFromWindow(
                            mNameView.getWindowToken(), 0);
                }

                return false;
            }
        });
    }

    // 主动退出的时候， 这个地方会有值,更具pwd来判断
    private boolean shouldAutoLogin() {
        SharedPreferences read = getSharedPreferences(
                IntentConstant.KEY_LOGIN_NOT_AUTO, Activity.MODE_PRIVATE);
        return read.getBoolean("login_not_auto", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Boolean isKickOut =
        // getIntent().getBooleanExtra(IntentConstant.KEY_IS_KICK_OUT , false);
        if (checkedPermission) {
            checkedPermission = false;
            checkPermission();
            return;
        }
        SharedPreferences read = getSharedPreferences(
                IntentConstant.KEY_LOGIN_NOT_AUTO, Activity.MODE_PRIVATE);
        if (read.getBoolean("login_kick", false)) {
            kickOutDevType = getIntent().getIntExtra(
                    IntentConstant.KEY_IS_KICK_OUT_DEVICE_TYPE, -1);
            showKickOutDia();
        }

    }

    private void showKickOutDia() {

        if (myDialog == null) {
            myDialog = new FilletDialog(
                    this,
                    FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITH_MESSAGE_ONE_BTN);
            myDialog.setTitle(getString(R.string.off_line_remind));// 设置内容

            if (kickOutDevType == UserInfoEvent.USER_BE_KICK_OUT_IOS_LOGIN
                    .ordinal()) {
                myDialog.setMessage(getString(R.string.off_line_ios_remind_text));// 设置内容
            } else if (kickOutDevType == UserInfoEvent.USER_BE_KICK_OUT_ANDROID_LOGIN
                    .ordinal()) {
                myDialog.setMessage(getString(R.string.off_line_android_remind_text));// 设置内容
            } else {
                myDialog.setMessage(getString(R.string.off_line_remind_text));// 设置内容
            }

            myDialog.dialog.show();// 显示
            myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
                @Override
                public void ok() {
                    myDialog.cancel();
                    myDialog.dialog.dismiss();
                    myDialog = null;
                    SharedPreferences ww = getSharedPreferences(
                            IntentConstant.KEY_LOGIN_NOT_AUTO,
                            Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = ww.edit();
                    editor.putBoolean("login_not_auto", true);
                    editor.commit();
                    imService.getLoginManager().setKickout(false);
                    mPasswordView.setText("");
                    mPasswordView.requestFocus();

                    SharedPreferences wwKick = getSharedPreferences(
                            IntentConstant.KEY_LOGIN_NOT_AUTO,
                            Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editorKick = wwKick.edit();
                    editorKick.putBoolean("login_kick", false);
                    editorKick.commit();
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(LoginActivity.this);
                loginPage = null;

        // 退出销毁 Play单例
        try {
            IMApplication.getPlaySound().close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void attemptLogin() {
        String loginName = mNameView.getText().toString();
        String mPassword = mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mPassword)) {
            Utils.showToast(this, getString(R.string.error_pwd_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(loginName)) {
            Utils.showToast(this, getString(R.string.error_name_required));
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            if (imService != null) {
                // boolean userNameChanged = true;
                // boolean pwdChanged = true;
                loginName = loginName.trim();
                mPassword = mPassword.trim();

                SharedPreferences ww = getSharedPreferences(
                        IntentConstant.KEY_LOGIN_NOT_AUTO,
                        Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = ww.edit();
                editor.putBoolean("login_not_auto", true);
                editor.commit();
                imService.getLoginManager().login(loginName, mPassword, imei,
                        clientType);
            }
        }
    }

    private void showProgress(final boolean show) {
        if (show) {
            loginLoading.setVisibility(View.VISIBLE);
            loginLoadingAni.start();
        } else {
            loginLoading.setVisibility(View.GONE);
            if (loginLoadingAni.isRunning()) {
                loginLoadingAni.stop();

            }
        }
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

    /**
     * ----------------------------event 事件驱动----------------------------
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginEvent event) {
        switch (event) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
                onLoginSuccess();

                break;

            case LOGIN_AUTH_DEVICE:
                // Intent intent = new Intent(LoginActivity.this,
                // LoginProtectionActivity.class);
                // intent.putExtra(IntentConstant.KEY_REGIST_NAME,
                // mNameView.getText().toString());//imService.getLoginManager().getLoginInfo().getPhone()
                // startActivity(intent);
                // LoginActivity.this.finish();
                showProgress(false);
                Intent intent = new Intent(LoginActivity.this,
                        ConfirmEnterProtectionActivity.class);

                intent.putExtra(IntentConstant.KEY_REGIST_NAME, mNameView.getText()
                        .toString());// imService.getLoginManager().getLoginInfo().getPhone()
                intent.putExtra(IntentConstant.KEY_LOGIN_PASS, mPasswordView
                        .getText().toString());
                intent.putExtra(IntentConstant.KEY_LOGIN_IMEI, imei);
                startActivity(intent);

                // LoginActivity.this.finish();
                // imService.getLoginManager().login(loginIdentity);

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
        logger.i("login#onLoginSuccess");
        loginSuccess = true;
        checkPermission();
    }

    private void goMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        LoginActivity.this.finish();
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
                sb.append(PermissionUtil.getPermissionString(LoginActivity.this, deniedPermission));
                // for (String permission : deniedPermission) {
                // 	sb.append("\n").append(permission);
                // }
                final FilletDialog myDialog = new FilletDialog(
                        LoginActivity.this,
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
                        PermissionUtil.getAppDetailSettingIntent(LoginActivity.this);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            ActivityManager.getInstance().finishAllActivity();
            LoginActivity.this.finish();

            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void onLoginFailure(LoginEvent event) {
        logger.e("login#onLoginError -> errorCode:%s", event.name());
        showLoginPage();
        // String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
        String errorTip = IMLoginManager.instance().getError();

        logger.d("login#errorTip:%s", errorTip);
        showProgress(false);
        Utils.showToast(this, errorTip);
    }

    private void onSocketFailure(SocketEvent event) {
        logger.e("login#onLoginError -> errorCode:%s,", event.name());
        showLoginPage();
        String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
        logger.d("login#errorTip:%s", errorTip);
        showProgress(false);
        // mLoginStatusView.setVisibility(View.GONE);
        Utils.showToast(this, errorTip);
    }

    // @Override
    // public boolean onKeyDown(int keyCode, KeyEvent event) {
    // if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
    // // Intent intent = new Intent(LoginActivity.this, QiDongActivity.class);
    // // startActivity(intent);
    // LoginActivity.this.finish();
    // return true;
    // }
    // return super.onKeyDown(keyCode, event);
    // }
}

package com.fise.xw.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R; 
import com.fise.xw.DB.sp.LoginSp;
import com.fise.xw.DB.sp.SystemConfigSp;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.UrlConstant;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.event.SocketEvent;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;


/**
 * @YM 1. 链接成功之后，直接判断是否loginSp是否可以直接登录
 * true: 1.可以登录，从DB中获取历史的状态
 * 2.建立长连接，请求最新的数据状态 【网络断开没有这个状态】
 * 3.完成
 * <p/>
 * false:1. 不能直接登录，跳转到登录页面
 * 2. 请求消息服务器地址，链接，验证，触发loginSuccess
 * 3. 保存登录状态
 */
@SuppressLint("NewApi") public class LoginActivity extends TTBaseActivity {

    private Logger logger = Logger.getLogger(LoginActivity.class);
    private Handler uiHandler = new Handler();
    private EditText mNameView;
    private EditText mPasswordView;
    private View loginPage; 
    private View mLoginStatusView;
    private TextView mSwitchLoginServer;
    private InputMethodManager intputManager;
    private Button sign_in_button;
    
    private int inputNum;

    private IMService imService;
    private boolean autoLogin = true;
    private boolean loginSuccess = false;
    private String imei;

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
                    LoginSp loginSp = imService.getLoginSp();
                    if (loginManager == null || loginSp == null) {
                        // 无法获取登陆控制器
                        break;
                    }

                    LoginSp.SpLoginIdentity loginIdentity = loginSp.getLoginIdentity();
                    if (loginIdentity == null) {
                        // 之前没有保存任何登陆相关的，跳转到登陆页面
                        break;
                    }

                    mNameView.setText(loginIdentity.getLoginName());
                    if (TextUtils.isEmpty(loginIdentity.getPwd())) {
                        // 密码为空，可能是loginOut
                        break;
                    }
                    mPasswordView.setText(loginIdentity.getPwd());

                    if (autoLogin == false) {
                        break;
                    }

                    handleGotLoginIdentity(loginIdentity);
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




    /**
     * 自动登陆
     */
    private void handleGotLoginIdentity(final LoginSp.SpLoginIdentity loginIdentity) {
        logger.i("login#handleGotLoginIdentity");

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logger.d("login#start auto login");
                if (imService == null || imService.getLoginManager() == null) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
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
        EventBus.getDefault().register(this);
        
        intputManager = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        logger.d("login#onCreate");

        SystemConfigSp.instance().init(getApplicationContext());
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS);
        }

        
        setContentView(R.layout.tt_activity_login);
   
    	TelephonyManager telephonyManager=(TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		imei =telephonyManager.getDeviceId();
	        
        
         
//        TextView black = (TextView)findViewById(R.id.black);
//        black.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) { 
//            	//LoginActivity.this.finish();
//            	  Intent intent = new Intent(LoginActivity.this, QiDongActivity.class);
//                  startActivity(intent);
//                  LoginActivity.this.finish();
//            }
//        });
        
        initAutoLogin();
        mSwitchLoginServer = (TextView)findViewById(R.id.sign_switch_login_server);
        mSwitchLoginServer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            	
				//AlertDialog.Builder builder = new Builder(LoginActivity.this);
        		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(
        				LoginActivity.this, android.R.style.Theme_Holo_Light_Dialog));
        		
				builder.setMessage("忘记密码?");
				//builder.setTitle("提示");
				builder.setPositiveButton("找回密码", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) { 
			            Intent intent = new Intent(LoginActivity.this, BlackPassName.class);
			            startActivity(intent);
			           // LoginActivity.this.finish();
					}
				});
				builder.setNegativeButton("取消", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
			}
            
        });

        inputNum = 0;
        mNameView = (EditText) findViewById(R.id.name);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {

                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        
        TextView  switch_register_server = (TextView) findViewById(R.id.switch_register_server);
        switch_register_server.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) { 
	            Intent intent = new Intent(LoginActivity.this, RegistActivityName.class);
	            startActivity(intent); 
            }
            
        });
        
        mLoginStatusView = findViewById(R.id.login_status);
         
        sign_in_button =  (Button) findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	if (inputNum > 0) { 
                    intputManager.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                    attemptLogin();
            	}
            }
        });
         
 
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
					sign_in_button.setBackgroundResource(R.drawable.button_normal);
				} else { 
					sign_in_button.setBackgroundResource(R.drawable.button_disabled);
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
                    intputManager.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                }

                if (mNameView != null) {
                    intputManager.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
                }

                return false;
            }
        }); 
    }

    // 主动退出的时候， 这个地方会有值,更具pwd来判断
    private boolean shouldAutoLogin() {
    	 SharedPreferences read = getSharedPreferences(IntentConstant.KEY_LOGIN_NOT_AUTO, Activity.MODE_PRIVATE);
    	 return  read.getBoolean("login_not_auto", false);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(LoginActivity.this);
        EventBus.getDefault().unregister(this); 
        loginPage = null;
    }


    public void attemptLogin() {
        String loginName = mNameView.getText().toString();
        String mPassword = mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mPassword)) {
            Toast.makeText(this, getString(R.string.error_pwd_required), Toast.LENGTH_SHORT).show();
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(loginName)) {
            Toast.makeText(this, getString(R.string.error_name_required), Toast.LENGTH_SHORT).show();
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            if (imService != null) {
//				boolean userNameChanged = true;
//				boolean pwdChanged = true;
                loginName = loginName.trim();
                mPassword = mPassword.trim();
                
         	   SharedPreferences ww = getSharedPreferences(IntentConstant.KEY_LOGIN_NOT_AUTO, Activity.MODE_PRIVATE);
           	   SharedPreferences.Editor editor = ww.edit(); 
           	   editor.putBoolean("login_not_auto", true); 
           	   editor.commit(); 
           	   
                imService.getLoginManager().login(loginName, mPassword,imei);
                 
            }
        }
    }

    private void showProgress(final boolean show) {
        if (show) {
            mLoginStatusView.setVisibility(View.VISIBLE);
        } else {
            mLoginStatusView.setVisibility(View.GONE);
        }
    }

    // 为什么会有两个这个
    // 可能是 兼容性的问题 导致两种方法onBackPressed
    @Override
    public void onBackPressed() {
        logger.d("login#onBackPressed");
        //imLoginMgr.cancel();
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
////        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
////            LoginActivity.this.finish();
////            return true;
////        }
//        return super.onKeyDown(keyCode, event);
//    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * ----------------------------event 事件驱动----------------------------
     */
    public void onEventMainThread(LoginEvent event) {
        switch (event) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
                onLoginSuccess(); 
                
                break;
                      
            case  LOGIN_AUTH_DEVICE:
//                Intent intent = new Intent(LoginActivity.this, LoginProtectionActivity.class); 
//                intent.putExtra(IntentConstant.KEY_REGIST_NAME,  mNameView.getText().toString());//imService.getLoginManager().getLoginInfo().getPhone()
//                startActivity(intent);
//                LoginActivity.this.finish();
            	 showProgress(false);
                Intent intent = new Intent(LoginActivity.this, LoginProtectionActivity.class); 
                
                intent.putExtra(IntentConstant.KEY_REGIST_NAME,  mNameView.getText().toString());//imService.getLoginManager().getLoginInfo().getPhone()
                intent.putExtra(IntentConstant.KEY_LOGIN_PASS,  mPasswordView.getText().toString());
                intent.putExtra(IntentConstant.KEY_LOGIN_IMEI,  imei);
                
                 
                startActivity(intent);
               // LoginActivity.this.finish();

            	//imService.getLoginManager().login(loginIdentity);
                 
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


    public void onEventMainThread(SocketEvent event) {
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
          
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        LoginActivity.this.finish();
  
    }
    

    private void onLoginFailure(LoginEvent event) {
        logger.e("login#onLoginError -> errorCode:%s", event.name());
        showLoginPage();
       // String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
        String errorTip = IMLoginManager.instance().getError();
        
        logger.d("login#errorTip:%s", errorTip);
        mLoginStatusView.setVisibility(View.GONE);
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    }

    private void onSocketFailure(SocketEvent event) {
        logger.e("login#onLoginError -> errorCode:%s,", event.name());
        showLoginPage();
        String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
        logger.d("login#errorTip:%s", errorTip);
        mLoginStatusView.setVisibility(View.GONE);
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    } 
    
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
////            Intent intent = new Intent(LoginActivity.this, QiDongActivity.class);
////            startActivity(intent);
//            LoginActivity.this.finish();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

}

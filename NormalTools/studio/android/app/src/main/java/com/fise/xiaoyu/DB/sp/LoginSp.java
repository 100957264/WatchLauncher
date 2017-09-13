package com.fise.xiaoyu.DB.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

/**
 *  登陆信息配置文件 login.ini
 * todo need Encryption
 */
public class LoginSp {

    private final String fileName = "login.ini";
    private Context ctx;
    private final String KEY_LOGIN_NAME = "loginName";
    private final String KEY_PWD = "pwd";
    private final String KEY_LOGIN_ID = "loginId";
    private final String KEY_LOGIN_IMEI = "loginImei";
    private final String KEY_LOGIN_TOKEN = "loginToken";
    private final String KEY_LOGIN_TYPE = "loginType";



    SharedPreferences sharedPreferences;

    private static LoginSp loginSp = null;
    public static LoginSp instance(){
        if(loginSp ==null){
            synchronized (LoginSp.class){
                loginSp = new LoginSp();
            }
        }
        return loginSp;
    }
    private LoginSp(){
    }


    public void  init(Context ctx){
        this.ctx = ctx;
        sharedPreferences= ctx.getSharedPreferences
                (fileName,ctx.MODE_PRIVATE);
    }

    public void setLoginTokenStr( String data){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LOGIN_TOKEN, data);
        //提交当前数据
        Log.i("aaa", "setLoginTokenStr: "+data);
        editor.commit();
    }

    public String getLoginToken(){
        String loginToken =  sharedPreferences.getString(KEY_LOGIN_TOKEN,null);
        Log.i("aaa", "getLoginToken: "+loginToken);
        return  loginToken;
    }

    public  void setLoginInfo(String userName,String pwd,int loginId,String mImei){
        // 横写
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LOGIN_NAME, userName);
        editor.putString(KEY_PWD, pwd);
        editor.putInt(KEY_LOGIN_ID, loginId);
        editor.putString(KEY_LOGIN_IMEI, mImei); 
        
        //提交当前数据
        editor.commit();
    }

    public  void setLoginInfo(String userName,String pwd,int loginId,String mImei,int type){
        // 横写
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LOGIN_NAME, userName);
        editor.putString(KEY_PWD, pwd);
        editor.putInt(KEY_LOGIN_ID, loginId);
        editor.putString(KEY_LOGIN_IMEI, mImei);
        editor.putInt(KEY_LOGIN_TYPE, type);

        //提交当前数据
        editor.commit();
    }

    public  void setLoginInfo(String userName){
        // 横写
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LOGIN_NAME, userName);

        //提交当前数据
        editor.commit();
    }


    public  void setType(int type){
        // 横写
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_LOGIN_TYPE, type);

        //提交当前数据
        editor.commit();
    }


    public SpLoginIdentity getLoginIdentity(){

        String userName =  sharedPreferences.getString(KEY_LOGIN_NAME,null);
        String pwd = sharedPreferences.getString(KEY_PWD,null);
        int loginId = sharedPreferences.getInt(KEY_LOGIN_ID,0);
        String imei = sharedPreferences.getString(KEY_LOGIN_IMEI,null);
        int type = sharedPreferences.getInt(KEY_LOGIN_TYPE,0);

        /**pwd不判空: loginOut的时候会将pwd清空*/
        if(TextUtils.isEmpty(userName) || loginId == 0){
            return null;
        }
        return new SpLoginIdentity(userName,pwd,loginId,imei,type);
    }

    public class SpLoginIdentity{
        private String loginName;
        private String pwd;
        private int loginId;
        private String  imei;
        private int type;

        public SpLoginIdentity(String mUserName,String mPwd,int mLoginId,String mImei,int type){
            this.loginName = mUserName;
            this.pwd = mPwd;
            this.loginId = mLoginId;
            Log.i("aaa", "SpLoginIdentity: "+mLoginId);
            this.imei = mImei;
            this.type = type;
        }

        public int getLoginId() {
            return loginId;
        }

        public void setLoginId(int loginId) {
            this.loginId = loginId;
        }

        public String getLoginName() {
            return loginName;
        }

        public String getPwd() {
            return pwd;
        }
        public String getImei() {
            return imei;
        }

        public int getType() {
            return type;
        }
    }
}

package com.fise.xw.DB.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * @author : yingmu on 15-1-6.
 * @email : yingmu@mogujie.com.
 *
 * todo need Encryption
 */
public class RegistSp {

    private final String fileName = "regist.ini";
    private Context ctx;
    private final String KEY_REGIST_NAME = "registName";
    private final String KEY_PWD = "pwd";
    private final String KEY_REGIST_ID = "registId";
    private final String KEY_REGIST_IMEI = "imei";
    

    SharedPreferences sharedPreferences;

    private static RegistSp registSp = null;
    public static RegistSp instance(){
        if(registSp ==null){
            synchronized (RegistSp.class){
            	registSp = new RegistSp();
            }
        }
        return registSp;
    }
    private RegistSp(){
    }


    public void  init(Context ctx){
        this.ctx = ctx;
        sharedPreferences= ctx.getSharedPreferences
                (fileName,ctx.MODE_PRIVATE);
    }

    public  void setRegistInfo(String userName,String pwd,int loginId,String imei){
        // 横写
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_REGIST_NAME, userName);
        editor.putString(KEY_PWD, pwd);
        editor.putInt(KEY_REGIST_ID, loginId);
        editor.putString(KEY_REGIST_IMEI, imei);
        
        //提交当前数据
        editor.commit();
    }

    public SpRegistIdentity getRegistIdentity(){
        String userName =  sharedPreferences.getString(KEY_REGIST_NAME,null);
        String pwd = sharedPreferences.getString(KEY_PWD,null);
        String imei = sharedPreferences.getString(KEY_REGIST_IMEI,null);
        
        int loginId = sharedPreferences.getInt(KEY_REGIST_ID,0);
        /**pwd不判空: loginOut的时候会将pwd清空*/
        if(TextUtils.isEmpty(userName) || loginId == 0){
            return null;
        }
        return new SpRegistIdentity(userName,pwd,loginId,imei);
    }

    public class SpRegistIdentity{
        private String registName;
        private String pwd;
        private String imei;
        private int registId;

        public SpRegistIdentity(String mUserName,String mPwd,int mLoginId,String mImei){
        	registName = mUserName;
            pwd = mPwd;
            registId = mLoginId;
            imei = mImei;
        }

        public int getLoginId() {
            return registId;
        }

        public void setRegistId(int registId) {
            this.registId = registId;
        }

        public String getRegistName() {
            return registName;
        }

        public String getPwd() {
            return pwd;
        }
        
        public String getImei() {
            return imei;
        }
    }
}

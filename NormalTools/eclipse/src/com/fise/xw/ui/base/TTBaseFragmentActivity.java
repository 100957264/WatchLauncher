package com.fise.xw.ui.base;

import java.text.Format.Field;

import com.android.volley.Request.Method;
import com.fise.xw.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

public abstract class TTBaseFragmentActivity extends FragmentActivity {
	int fntLevel;

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sp = this.getApplication().getSharedPreferences(
				"ziTing", MODE_PRIVATE);
		fntLevel = sp.getInt("ziTing1", 0);
		
//		if(VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
//            //透明状态栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            //透明导航栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//             
//		} 
//	 
//		setMiuiStatusBarDarkMode(this,true);
		 
		
	}

	
	public static boolean setMiuiStatusBarDarkMode(Activity activity, boolean darkmode) {
	    Class<? extends Window> clazz = activity.getWindow().getClass();
	    try {
	        int darkModeFlag = 0;
	        Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
	        java.lang.reflect.Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
	        darkModeFlag = field.getInt(layoutParams);
	        java.lang.reflect.Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
	        extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
	        return true;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	
	@Override
	public Resources getResources() {
		Resources res = super.getResources();
		Configuration config = new Configuration();
		config.setToDefaults();

        if(fntLevel == 1){
        	config.fontScale = 1.05f;
        }else if(fntLevel == 2){
        	config.fontScale = 1.1f;
        }else if(fntLevel == 3){
        	config.fontScale = 1.2f;
        }else if(fntLevel == 4){
        	config.fontScale = 1.3f;
        }else if(fntLevel == 5){
        	config.fontScale = 1.4f;
        }

		// config.fontScale = 1.4f;
		res.updateConfiguration(config, res.getDisplayMetrics());
		return res;
	}

}

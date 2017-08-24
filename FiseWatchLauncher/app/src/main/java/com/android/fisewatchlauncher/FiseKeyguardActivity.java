package com.android.fisewatchlauncher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.util.Log;
import android.graphics.Color;


public class FiseKeyguardActivity extends Activity implements OnTouchListener, OnGestureListener{
	GestureDetector mGestureDetector;
	SharedPreferences mSharedPreference;
   FiseRectAnalogClockOne mFiseRectAnalogClockOne;
   FiseRectAnalogClockZero mFiseRectAnalogClockZero;
   FiseRectAnalogClockTwo mFiseRectAnalogClockTwo;
   FiseRectAnalogClockThree mFiseRectAnalogClockThree;
   FiseRectAnalogClockFive mFiseRectAnalogClockFive;
   FiseRectAnalogClockSix mFiseRectAnalogClockSix;
    RelativeLayout mRelativeLayout;
	int defaultValue = 1;

	 private static final int FLING_MIN_DISTANCE = 50;     
	 private static final int FLING_MIN_VELOCITY = 0;   
	 final String ACTION_SERVICE = "com.android.fisewatchlauncher.FiseService";
	 final String ANALOG_CLOCK ="analog_clock";
       @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
	    Log.d("fengqing", "onCreate  start.....---> setContentView.");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			 WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		getWindow().setStatusBarColor(Color.TRANSPARENT);
	    setContentView(R.layout.keyguard_fise_analog_clock);
	    
	    startService(new Intent(FiseKeyguardActivity.this,FiseService.class));
		mSharedPreference = this.getSharedPreferences(ANALOG_CLOCK, Context.MODE_PRIVATE);
		mGestureDetector = new GestureDetector((OnGestureListener) this);
		mRelativeLayout = (RelativeLayout) findViewById(R.id.activity_fise_keyguard);
		mRelativeLayout.setOnTouchListener(this);
		mRelativeLayout.setLongClickable(true);
		initView();
    }
        @SuppressLint("NewApi") @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	
    	getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION 
				|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				|View.SYSTEM_UI_FLAG_IMMERSIVE);
    	showSlectedView(mSharedPreference.getInt(ANALOG_CLOCK, defaultValue));
    }
    private void initView(){
    	mFiseRectAnalogClockZero = (FiseRectAnalogClockZero) findViewById(R.id.analog_clock_0000);
    	mFiseRectAnalogClockOne = (FiseRectAnalogClockOne) findViewById(R.id.analog_clock_1);
    	mFiseRectAnalogClockTwo = (FiseRectAnalogClockTwo) findViewById(R.id.analog_clock_2);
    	mFiseRectAnalogClockThree= (FiseRectAnalogClockThree) findViewById(R.id.analog_clock_3);
    	mFiseRectAnalogClockFive= (FiseRectAnalogClockFive) findViewById(R.id.analog_clock_5);
    	mFiseRectAnalogClockSix= (FiseRectAnalogClockSix) findViewById(R.id.analog_clock_6);
    	new View(this).setVisibility(View.VISIBLE);
    }
    private void showSlectedView(int id){
        switch(id){
        case 0:
        	mFiseRectAnalogClockOne.setVisibility(View.GONE);
        	mFiseRectAnalogClockZero.setVisibility(View.VISIBLE);
        	mFiseRectAnalogClockTwo.setVisibility(View.GONE);
        	mFiseRectAnalogClockThree.setVisibility(View.GONE);
        	mFiseRectAnalogClockFive.setVisibility(View.GONE);
        	mFiseRectAnalogClockSix.setVisibility(View.GONE);
        	break;
        case 1:
        	mFiseRectAnalogClockOne.setVisibility(View.VISIBLE);
        	mFiseRectAnalogClockZero.setVisibility(View.GONE);
        	mFiseRectAnalogClockTwo.setVisibility(View.GONE);
        	mFiseRectAnalogClockThree.setVisibility(View.GONE);
        	mFiseRectAnalogClockFive.setVisibility(View.GONE);
        	mFiseRectAnalogClockSix.setVisibility(View.GONE);
        	break;
        case 2:
        	mFiseRectAnalogClockOne.setVisibility(View.GONE);
        	mFiseRectAnalogClockZero.setVisibility(View.GONE);
        	mFiseRectAnalogClockTwo.setVisibility(View.VISIBLE);
        	mFiseRectAnalogClockThree.setVisibility(View.GONE);
        	mFiseRectAnalogClockFive.setVisibility(View.GONE);
        	mFiseRectAnalogClockSix.setVisibility(View.GONE);
        	break;
        case 3:
        	mFiseRectAnalogClockOne.setVisibility(View.GONE);
        	mFiseRectAnalogClockZero.setVisibility(View.GONE);
        	mFiseRectAnalogClockTwo.setVisibility(View.GONE);
        	mFiseRectAnalogClockThree.setVisibility(View.VISIBLE);
        	mFiseRectAnalogClockFive.setVisibility(View.GONE);
        	mFiseRectAnalogClockSix.setVisibility(View.GONE);
        	break;
        case 4:
        	mFiseRectAnalogClockOne.setVisibility(View.GONE);
        	mFiseRectAnalogClockZero.setVisibility(View.GONE);
        	mFiseRectAnalogClockTwo.setVisibility(View.GONE);
        	mFiseRectAnalogClockThree.setVisibility(View.GONE);
        	mFiseRectAnalogClockFive.setVisibility(View.VISIBLE);
        	mFiseRectAnalogClockSix.setVisibility(View.GONE);
        	break;
        case 5:
        	mFiseRectAnalogClockOne.setVisibility(View.GONE);
        	mFiseRectAnalogClockZero.setVisibility(View.GONE);
        	mFiseRectAnalogClockTwo.setVisibility(View.GONE);
        	mFiseRectAnalogClockThree.setVisibility(View.GONE);
        	mFiseRectAnalogClockFive.setVisibility(View.GONE);
        	mFiseRectAnalogClockSix.setVisibility(View.VISIBLE);
        }
    }

    private void setAnalogClockStyle(int result){
    	Editor editor =mSharedPreference.edit();
    	editor.putInt(ANALOG_CLOCK, result);
    	editor.commit();
    	
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == 1){
    		if(resultCode == Activity.RESULT_OK){
    			int style = data.getIntExtra("style",mSharedPreference.getInt(ANALOG_CLOCK, defaultValue));
    			showSlectedView(style);
    			setAnalogClockStyle(style);
    		}
    	}
    	
    }

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
	     if (e1.getX()-e2.getX() > FLING_MIN_DISTANCE   
	                && Math.abs(velocityX) >FLING_MIN_VELOCITY ) {   
	                Intent mIntent = new Intent(FiseKeyguardActivity.this,FiseLauncherActivity.class);
	                startActivity(mIntent);
	        } else if (e2.getX()-e1.getX() > FLING_MIN_DISTANCE  
	                && Math.abs(velocityX) > FLING_MIN_VELOCITY) {   
	        	    Intent mIntent = new Intent(FiseKeyguardActivity.this,FiseLauncherActivity.class);
	                startActivity(mIntent);
	        } 
		return false;
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
	
		return mGestureDetector.onTouchEvent(event);
	}
	@Override
	public boolean onDown(MotionEvent e) {

		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {
	
		
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onLongPress(MotionEvent e) {
		Intent intent = new Intent(FiseKeyguardActivity.this,FiseAnalogClockSelect.class);
		startActivityForResult(intent, 1);
	}
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    }
}

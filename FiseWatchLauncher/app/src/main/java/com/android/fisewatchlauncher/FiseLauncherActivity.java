package com.android.fisewatchlauncher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.Color;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import android.view.View;

import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;


public class FiseLauncherActivity extends Activity {
	ViewPager viewPager;
	LinearLayout llPointGroup;
	ImageView ivWhitePoint;
	private int leftMax;
	
	List<View> page;
    Button mDialerButton;
    Button mContactButton;
    Button mMmsButton;
    Button mGalleryButton;
    Button mCameraButton;
    Button mFileManagerButton;
    Button mSettingsButton;
    Button mQQButton;
    Button mWeChatButton;
	Button mVideoCall;
	ImageView mStepsCounterButton;
	Button mAppListButton;
    int mCurrentPosition = 0;
    int isFirstPage = 0;
    int defaultValue = 0;
    View mView;

    TextClock mTime;
    TextClock mDate;
    TextClock mWeek;
    Typeface fontFace;
    TextView mDialerTextView;
    TextView mContactTextView;
    TextView mMmsTextView;
    TextView mGalleryTextView;
    TextView mCameraTextView;
    TextView mFileManagerTextView;
    TextView mSettingTextView;
    TextView mWechatTextView;
    TextView mQQTextView;
	TextView mVideoTextView;
	TextView mStepsCounter;
    TextView mAppListTextView;
    private boolean isFirstBoot = true;
    private boolean isFiseLauncherRunning=true;
    private  int previousPage = 0;
	
    
     Context mContext;
    @SuppressLint("NewApi")@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		getWindow().setStatusBarColor(Color.TRANSPARENT);
		setContentView(R.layout.activity_fise_launcher);
		mContext = FiseLauncherActivity.this;
		fontFace = Typeface.createFromAsset(getAssets(),
                "fonts/round_font.ttf");
//		mSharedPreference = this.getSharedPreferences(ANALOG_CLOCK, Context.MODE_PRIVATE);
	//	pointProgress = findViewById(R.id.point_progress);
		
//		initView();
//		initData();
//		initListener();
	}
	@SuppressLint("NewApi") @Override
   protected void onResume() {
		// TODO Auto-generated method stub
//		pointProgress.setVisibility(View.INVISIBLE);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION 
				|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				|View.SYSTEM_UI_FLAG_IMMERSIVE);
		initView(); 
		if(isFirstBoot){
			isFirstBoot =false;
		   initData();
		 }
				
		initListener();
		viewPager.setAdapter(new FisePagerAdapter());
		viewPager.setCurrentItem(previousPage);
		super.onResume();
	}

	private void initView() {
		// TODO Auto-generated method stub
		llPointGroup = (LinearLayout) findViewById(R.id.ll_point_group);
		
		viewPager = (ViewPager) findViewById(R.id.view_page);
		ivWhitePoint = (ImageView) findViewById(R.id.iv_white_point);
		page = new ArrayList<View>();
		page.clear();
	    page.add(getLayoutInflater().inflate(R.layout.activity_fise_text_clock
				, null));
		page.add(getLayoutInflater().inflate(R.layout.fise_dialer_activity,
				null));
		page.add(getLayoutInflater().inflate(R.layout.fise_contect_activity,
				null));
		page.add(getLayoutInflater().inflate(R.layout.fise_mms_activity, null));
		page.add(getLayoutInflater().inflate(R.layout.fise_gallery_activity,
				null));
		page.add(getLayoutInflater().inflate(R.layout.fise_camera_activity,
				null));
		page.add(getLayoutInflater().inflate(
				R.layout.fise_file_manager_activity, null));
		page.add(getLayoutInflater().inflate(R.layout.fise_settings_activity,
				null));
		page.add(getLayoutInflater().inflate(R.layout.fise_wechat_activity, null));
		page.add(getLayoutInflater().inflate(R.layout.fise_qq_activity, null));
	    // page.add(getLayoutInflater().inflate(R.layout.fise_video_activity, null));
	    //page.add(getLayoutInflater().inflate(R.layout.fise_stepcounter_activity, null));
		page.add(getLayoutInflater().inflate(R.layout.fise_all_apps_activity, null));
		
	   View mTextClockView = page.get(0);
	   mDate = (TextClock) mTextClockView.findViewById(R.id.fise_text_clock_1);
	   mDate.setFormat12Hour("MMMM dd");
	   mDate.setTypeface(fontFace);
	   mTime = (TextClock) mTextClockView.findViewById(R.id.fise_text_clock);
	   mTime.setFormat12Hour("hh:mm");
	   mTime.setTypeface(fontFace);
	   mWeek = (TextClock) mTextClockView.findViewById(R.id.fise_text_clock_2);
	   mWeek.setFormat12Hour("EEEE");
	   mWeek.setTypeface(fontFace);
		View mDialView = page.get(1);
		mDialerButton = (Button) mDialView.findViewById(R.id.app_dialer);
		mDialerTextView = (TextView) mDialView.findViewById(R.id.ic_tv_dialer);
		mDialerTextView.setTypeface(fontFace);
	    mDialerButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent dialerIntent = new Intent();
				dialerIntent.setAction("android.intent.action.DIAL");
			    dialerIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(dialerIntent);
			    overridePendingTransition(R.anim.activity_open, android.R.anim.fade_out);
			}
		});
           View mContactView = page.get(2);
	    	mContactButton =(Button)mContactView. findViewById(R.id.app_contact);
	    	mContactTextView = (TextView) mContactView.findViewById(R.id.ic_tv_contact);
			mContactTextView.setTypeface(fontFace);
	    	mContactButton.setOnClickListener(new OnClickListener() {	
	   			@Override
	   			public void onClick(View v) {
	   				// TODO Auto-generated method stub
	   				Intent contactIntent = new Intent("android.intent.action.MAIN");
	   				contactIntent.addCategory("android.intent.category.APP_CONTACTS");
	   				startActivity(contactIntent);
	   			}
	   		});
	       View mMmsView = page.get(3);
	    	mMmsButton =(Button)mMmsView. findViewById(R.id.app_mms);
	    	mMmsTextView = (TextView) mMmsView.findViewById(R.id.ic_tv_mms);
			mMmsTextView.setTypeface(fontFace);
	    	mMmsButton.setOnClickListener(new OnClickListener() {	
	   			@Override
	   			public void onClick(View v) {
	   				// TODO Auto-generated method stub
	   				Intent mmsIntent = new Intent("android.intent.action.MAIN");
	   				mmsIntent.addCategory("android.intent.category.APP_MESSAGING");
	   				startActivity(mmsIntent);
	   			}
	   		});
	    	View mGalleryView = page.get(4);
	    	mGalleryButton =(Button)mGalleryView. findViewById(R.id.app_gallery);
	    	mGalleryTextView = (TextView) mGalleryView.findViewById(R.id.ic_tv_gallery);
			mGalleryTextView.setTypeface(fontFace);
	    	mGalleryButton.setOnClickListener(new OnClickListener() {	
	   			@Override
	   			public void onClick(View v) {
	   				// TODO Auto-generated method stub
	   				Intent galleryIntent =new Intent("android.intent.action.MAIN");
	   				galleryIntent.addCategory("android.intent.category.APP_GALLERY");
	   				startActivity(galleryIntent);
	   			}
	   		});
	    	View mCameraView = page.get(5);
	    	mCameraButton =(Button)mCameraView. findViewById(R.id.app_camera);
	      	mCameraTextView = (TextView) mCameraView.findViewById(R.id.ic_tv_camera);
	    	mCameraTextView.setTypeface(fontFace);
	    	mCameraButton.setOnClickListener(new OnClickListener() {	
	   			@Override
	   			public void onClick(View v) {
	   				// TODO Auto-generated method stub
	   				try{
	   				Intent cameraIntent = new Intent();
	   				ComponentName component = new ComponentName("com.mediatek.camera", "com.android.camera.CameraLauncher");
	   				cameraIntent.setComponent(component);
	   				startActivity(cameraIntent);
	   				}catch(ActivityNotFoundException e){
	   					
	   					Toast.makeText(FiseLauncherActivity.this, R.string.camera_can_not_found, Toast.LENGTH_SHORT).show();
	   					e.printStackTrace();
	   				}
	   			}
	   		});
	    	View mFileMangerView = page.get(6);
	    	mFileManagerButton =(Button)mFileMangerView. findViewById(R.id.app_file_manager);
	    	mFileManagerTextView = (TextView) mFileMangerView.findViewById(R.id.ic_tv_file_manager);
	    	mFileManagerTextView.setTypeface(fontFace);
	    	mFileManagerButton.setOnClickListener(new OnClickListener() {	
	   			@Override
	   			public void onClick(View v) {
	   				// TODO Auto-generated method stub
	   				Intent fileManagerIntent = new Intent("com.mediatek.hotknot.action.FILEMANAGER_FILE_RECEIVED");
	   				startActivity(fileManagerIntent);
	   				overridePendingTransition(R.anim.activity_open, android.R.anim.fade_out);
	   			}
	   		});
	    	View mSettingView = page.get(7);
	    	mSettingsButton =(Button) mSettingView.findViewById(R.id.app_settings);
	    	mSettingTextView = (TextView) mSettingView.findViewById(R.id.ic_tv_setting);
	    	mSettingTextView.setTypeface(fontFace);
	    	mSettingsButton.setOnClickListener(new OnClickListener() {	
	   			@Override
	   			public void onClick(View v) {
	   				// TODO Auto-generated method stub
	   				Intent dialerIntent = new Intent("android.settings.SETTINGS");
	   				startActivity(dialerIntent);
	   				overridePendingTransition(R.anim.activity_open, android.R.anim.fade_out);
	   			}
	   		});

	   		View mStepsView = page.get(8);
	    	mStepsCounterButton=(ImageView) mStepsView.findViewById(R.id.app_stepcounter);
		    mStepsCounterButton.setImageBitmap(generateQRBitmap("0123456789ABCDEF",240,240));
	    	mStepsCounter= (TextView) mStepsView.findViewById(R.id.ic_tv_stepscounter);
	    	mStepsCounter.setTypeface(fontFace);

			View mAllAppsView = page.get(9);
		mAppListButton =(Button) mAllAppsView.findViewById(R.id.app_all);
		mAppListTextView = (TextView) mAllAppsView.findViewById(R.id.ic_tv_all_app);
		mAppListTextView.setTypeface(fontFace);
		mAppListButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try{
					Intent appListIntent = new Intent(FiseLauncherActivity.this,FiseAppsListActivity.class);
					startActivity(appListIntent);
				}catch(ActivityNotFoundException e){
					e.printStackTrace();
				}
			}
		});
	}

	private Bitmap generateQRBitmap(String content,int width, int height) {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		Map<EncodeHintType, String> hints = new HashMap<>();
		Bitmap mBitmap = null;
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		try {
			BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

			int[] pixels = new int[width * height];
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if (encode.get(j, i)) {
						pixels[i * width + j] = 0x00000000;
					} else {
						pixels[i * width + j] = 0xffffffff;
					}
				}
			}
			mBitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
			Log.d("fengqing","encode =" + encode + "mBitmap =" + mBitmap);
			return mBitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return mBitmap;
	}

	private void initData() {
		// TODO Auto-generated method stub
		LinearLayout.LayoutParams params;

		int widthdpi = (int) SizeUtil.Dp2Px(this, 5);
		for (int i = 0; i <page.size(); i++) {
			if(i==0){
				continue;
			}
			ImageView point = new ImageView(this);
			point.setBackgroundResource(R.drawable.ic_pageindicator_default);
			params = new LinearLayout.LayoutParams(8, 8);
			if (i != 0) {
				params.leftMargin = widthdpi;
			}
			point.setLayoutParams(params);
			llPointGroup.addView(point);
		}

	}

	// 适配器
	class FisePagerAdapter extends PagerAdapter {
		// 返回数据总个数
		@Override
		public int getCount() {
			return page.size();
		}

		// 当前视图
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		// 销毁视图
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		// 显示视图
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			
			if (position < page.size()) {
				mView = page.get(position);
			
				container.addView(mView);
			} else {
				// view = pageNine;
				// container.addView(view);
			}
			return mView;
		}
	}

	/**
	 * 绑定监听
	 */
	private void initListener() {
		ivWhitePoint.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnFiseGlobalLayoutListener());
		viewPager.setOnPageChangeListener(new FiseOnPageChangeListener());
		

	}
	private class OnFiseGlobalLayoutListener implements
			ViewTreeObserver.OnGlobalLayoutListener {

		@Override
		public void onGlobalLayout() {
			// 默认会调用俩次，只需要一次，第一次进入就移除
			ivWhitePoint.getViewTreeObserver().removeGlobalOnLayoutListener(
					OnFiseGlobalLayoutListener.this);
			// 间距 = 第1个点距离左边距离 - 第0个点距离左边距离
			leftMax = llPointGroup.getChildAt(1).getLeft()
					- llPointGroup.getChildAt(0).getLeft();
		}
	}

	private class FiseOnPageChangeListener implements
			ViewPager.OnPageChangeListener {
		/**
		 * 当页面滑动回调会调用此方法
		 * 
		 * @param position
		 *            当前页面位置
		 * @param positionOffset
		 *            当前页面滑动百分比
		 * @param positionOffsetPixels
		 *            滑动的像素数
		 */
		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			int leftmagin = (int) (position * leftMax + (positionOffset * leftMax));
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivWhitePoint
					.getLayoutParams();
			params.leftMargin = leftmagin;

			ivWhitePoint.setLayoutParams(params);
			Log.d("fengqing","page.size-1=" + (page.size()-1));
			if(lastPage && positionOffset == 0 && positionOffsetPixels == 0 && isScrolling){
				shouldBackFirstPage = true;
			}
         
		}

		/**
		 * 页面被选中，回调此方法
		 * 
		 * @param position
		 *            被选中的页面位置 此作用是最后一张图片显示button
		 */
		@SuppressLint("NewApi") @Override
		public void onPageSelected(int position) {
			mView = page.get(position);          
			mCurrentPosition = position;
			lastPage = position == (page.size()-1);
		}
      
		@Override
		public void onPageScrollStateChanged(int state) {
             if(state == 1){
				 isScrolling = true;
			 } else {
				 isScrolling = false;
				 if(shouldBackFirstPage && state == 0){
					 viewPager.setCurrentItem(0,true);
					 shouldBackFirstPage = false;
				 }
			 }
		}
	}
	boolean isScrolling = false;
	boolean lastPage = false;
	boolean shouldBackFirstPage = false;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mCurrentPosition == 0){
				return true;
			}else {
				mCurrentPosition = mCurrentPosition -1;
				//Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_left);
			   //viewPager.setAnimation(animation);
				viewPager.setCurrentItem(mCurrentPosition,true);
				return true;
			}
	
		}
    	Log.d("fengqing","onKeyDown:back key is pressed");
		return  super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onPause() {
		super.onPause();
		isFiseLauncherRunning = false;
		previousPage = mCurrentPosition;
		Log.d("fengqing","onPause is called");
	}
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	//do nothing
    	Log.d("fengqing","back key is pressed");
    }
}

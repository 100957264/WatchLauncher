package com.android.fisewatchlauncher;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
public class FiseService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
    @Override
    public void onCreate() {
    	// TODO Auto-generated method stub
    	super.onCreate();
    	IntentFilter mScreenOnFilter = new IntentFilter();
    	mScreenOnFilter.addAction(Intent.ACTION_SCREEN_OFF);
    	mScreenOnFilter.addAction(Intent.ACTION_SCREEN_ON);
    	FiseService.this.registerReceiver(mFiseScreenActionReceiver, mScreenOnFilter);
    }
    private BroadcastReceiver mFiseScreenActionReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
		     String action = intent.getAction();
	            if (action.equals(Intent.ACTION_SCREEN_ON)) {        
	            	      Log.d("fengqing","onReceive:Intent.ACTION_SCREEN_ON");
	                       //Intent lockIntent = new Intent(FiseService.this,FiseKeyguardActivity.class);
	                        //lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                       // startActivity(lockIntent);
	            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
	            	        Log.d("fengqing","onReceive:Intent.ACTION_SCREEN_OFF");
							Intent lockIntent = new Intent(FiseService.this,FiseKeyguardActivity.class);
	                        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                        startActivity(lockIntent);
	            }
		}
    	
    };
    public void onDestroy() {
    	super.onDestroy();
    	this.unregisterReceiver(mFiseScreenActionReceiver);
    	startService(new Intent(this,FiseService.class));
    };
}

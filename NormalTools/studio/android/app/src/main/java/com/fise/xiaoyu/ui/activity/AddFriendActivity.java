package com.fise.xiaoyu.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;


/**
 *  添加好友界面
 */
public class AddFriendActivity extends  TTBaseFragmentActivity{
	
	 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        setContentView(R.layout.tt_fragment_activity_add_friends); 
    }
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		 
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); 
		 
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
        startActivity(new Intent(this , MainActivity.class));
        finish();

	}
}

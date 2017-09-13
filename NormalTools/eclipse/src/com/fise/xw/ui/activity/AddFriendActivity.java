package com.fise.xw.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseFragmentActivity;


/**
 *  添加好友界面
 * @author weileiguan
 *
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

}

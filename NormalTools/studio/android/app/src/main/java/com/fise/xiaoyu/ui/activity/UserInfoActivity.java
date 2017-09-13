package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;


/**
 *  个人信息界面
 */
public class UserInfoActivity extends  TTBaseFragmentActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        setContentView(R.layout.tt_fragment_activity_userinfo);
    }
}

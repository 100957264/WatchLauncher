package com.fise.xw.ui.activity;

import android.os.Bundle;

import com.fise.xw.R;
import com.fise.xw.ui.base.TTBaseFragmentActivity;


/**
 *  个人信息界面
 * @author weileiguan
 *
 */
public class UserInfoActivity extends  TTBaseFragmentActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        setContentView(R.layout.tt_fragment_activity_userinfo);
    }
}

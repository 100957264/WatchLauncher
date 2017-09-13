
package com.fise.xiaoyu.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;


/**
 *  位群选择界面
 */
public class WeiGroupSelectActivity extends TTBaseFragmentActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.tt_activity_wei_group_select);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode)
            return;
    }
}
 

package com.fise.xiaoyu.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;


/**
 *  群的详细信息界面
 */
public class GroupManagermentActivity extends TTBaseFragmentActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.tt_activity_group_manage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode)
            return;
    }
}


package com.fise.xw.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.fise.xw.R;
import com.fise.xw.ui.base.TTBaseFragmentActivity;


/**
 *  群的详细信息界面
 * @author weileiguan
 *
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


package com.fise.xw.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.fise.xw.R;
import com.fise.xw.ui.base.TTBaseFragmentActivity;

/*
 *  发送名片 (好友中)
 */
public class FriendsSelectActivity extends TTBaseFragmentActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.tt_activity_friends_select); //tt_activity_group_select
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode)
            return;
    }
}

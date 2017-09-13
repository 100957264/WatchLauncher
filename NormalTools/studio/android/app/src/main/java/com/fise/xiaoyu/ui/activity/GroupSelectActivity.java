
package com.fise.xiaoyu.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.utils.OnkeyBackListener;


/** 
 * 选择群界面
 */
public class GroupSelectActivity extends TTBaseFragmentActivity {

   private OnkeyBackListener listener;
    private Boolean hideCheckBox;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.tt_activity_group_select);
        hideCheckBox = getIntent().getBooleanExtra(IntentConstant.HIDE_SELECT_CHECK_BOX ,false);

    }

   public  void setBackListener (OnkeyBackListener listener){
       this.listener = listener;
   }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (RESULT_OK != resultCode)
            return;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(hideCheckBox){
            if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
                if(listener != null){
                    listener.onkeyBack();
                    return true;
                }

            }

        }

        return super.onKeyDown(keyCode, event);

    }
}

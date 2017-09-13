package com.fise.xiaoyu.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.ui.base.TTGuideBaseActivity;
import com.fise.xiaoyu.ui.widget.FilletDialog;

/**
 * Created by lenovo on 2017/5/15.
 */

public class ConfirmEnterProtectionActivity  extends TTGuideBaseActivity {

    private TextView confirmText;
    private Button confirmBtn;
    private String phoneName;
    private String loginPass;
    private String imei;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_confirm_enter_sms_verify);
        initView();
        init();
    }

    private void initView() {
        confirmText = (TextView) findViewById(R.id.confirm_text);
        confirmBtn = (Button) findViewById(R.id.sms_confirm_button);

    }

    private void init() {
        phoneName = getIntent().getStringExtra(IntentConstant.KEY_REGIST_NAME);
        loginPass = getIntent().getStringExtra(IntentConstant.KEY_LOGIN_PASS);
        imei = getIntent().getStringExtra(IntentConstant.KEY_LOGIN_IMEI);

        confirmText.setText(getString(R.string.confirm_enter_protection_string)+"(" + phoneName + ")");
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final FilletDialog myDialog = new FilletDialog(ConfirmEnterProtectionActivity.this ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITH_MESSAGE);
                myDialog.setTitle(ConfirmEnterProtectionActivity.this.getString(R.string.confirm_phone_number));//设置内容
                myDialog.setMessage(ConfirmEnterProtectionActivity.this.getString(R.string.confirm_phone_hint_text)+ phoneName);//设置内容
                myDialog.dialog.show();//显示

                //确认按键回调，按下确认后在此做处理
                myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
                    @Override
                    public void ok() {
                        Intent intent = new Intent(ConfirmEnterProtectionActivity.this, LoginProtectionActivity.class);
                        intent.putExtra(IntentConstant.KEY_REGIST_NAME, phoneName);//imService.getLoginManager().getLoginInfo().getPhone()
                        intent.putExtra(IntentConstant.KEY_LOGIN_PASS, loginPass);
                        intent.putExtra(IntentConstant.KEY_LOGIN_IMEI, imei);
                        startActivity(intent);
                        myDialog.dialog.dismiss();;
                    }
                });

            }
        });
    }

}



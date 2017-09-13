package com.fise.marechat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.fise.marechat.client.GlobalSettings;
import com.fise.marechat.function.micro.RecorderWavActivity;
import com.fise.marechat.utils.DeviceInfoUtils;
import com.fise.marechat.utils.LogUtils;
import com.tbruyelle.rxpermissions.RxPermissions;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RxPermissions.getInstance(this)
                .request(Manifest.permission.READ_PHONE_STATE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        if (granted) { // 在android 6.0之前会默认返回true
                            // 已经获取权限
                            String id = DeviceInfoUtils.getIccid(MainActivity.this);
                            LogUtils.i("id = " + id);
                            String imei = DeviceInfoUtils.getIMEI(MainActivity.this);
                            LogUtils.i("imei = " + imei);
                            GlobalSettings.instance().setImei(imei);
                        } else {
                            // 未获取权限
                            Toast.makeText(MainActivity.this, "您没有授权该权限，请在设置中打开授权", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void tcpclient(View v) {
        start(TcpclientActivity.class);
    }

    public void microRecord(View v) {
        start(RecorderWavActivity.class);
    }

    private void start(Class activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

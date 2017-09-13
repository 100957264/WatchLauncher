package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.device.entity.MobilePhoneDeviceEntity;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by weileiguan on 2017/5/20 0020.
 */
public class LightTimeActivity extends TTBaseActivity {

    private static IMService imService;
    private IMLoginManager imLoginManager = IMLoginManager.instance();
    private ImageView three_seconds_right;
    private ImageView sex_seconds_right;
    private ImageView ten_seconds_right;
    private MobilePhoneDeviceEntity rsp;
    private int currentUserId;
    private UserEntity currentUser;
    private DeviceEntity device;
    private UserEntity loginContact;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("config#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                // 后台服务启动链接失败
                return;
            }

            device = imService.getDeviceManager().findDeviceCard(currentUserId);
            DeviceEntity rspTest  = imService.getDeviceManager().findDeviceCard(currentUserId);
            if(device!=null){
                rsp = MobilePhoneDeviceEntity.parseFromDB(rspTest);
            }

            loginContact = IMLoginManager.instance().getLoginInfo();
            if (rsp == null) {
                return;
            }
            currentUser = imService.getContactManager().findDeviceContact(
                    currentUserId);

            initDetailProfile();
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tt_activity_light_time);

        imServiceConnector.connect(this);

        currentUserId = LightTimeActivity.this.getIntent().getIntExtra(
                IntentConstant.KEY_PEERID, 0);

        TextView left_text = (TextView) findViewById(R.id.left_text);
        left_text.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                LightTimeActivity.this.finish();
            }
        });

        Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
        icon_arrow.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                LightTimeActivity.this.finish();
            }
        });

        three_seconds_right = (ImageView) findViewById(R.id.three_seconds_right);
        sex_seconds_right = (ImageView) findViewById(R.id.sex_seconds_right);
        ten_seconds_right = (ImageView) findViewById(R.id.ten_seconds_right);

        RelativeLayout three_seconds = (RelativeLayout) findViewById(R.id.three_seconds);
        three_seconds.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (device.getMasterId() == loginContact.getPeerId()){
                    imService.getDeviceManager().settingOpen(currentUserId, "",
                            IMDevice.SettingType.SETTING_TYPE_PHONE_LIGHT_TIME, 15, rsp);
                }else{
                    Utils.showToast(LightTimeActivity.this , getString(R.string.no_authority_to_operate));
                }
            }
        });

        RelativeLayout sex_seconds = (RelativeLayout) findViewById(R.id.sex_seconds);
        sex_seconds.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (device.getMasterId() == loginContact.getPeerId()){
                    imService.getDeviceManager().settingOpen(currentUserId, "",
                            IMDevice.SettingType.SETTING_TYPE_PHONE_LIGHT_TIME, 30, rsp);
                }else{
                    Utils.showToast(LightTimeActivity.this , getString(R.string.no_authority_to_operate));
                }

            }
        });

        RelativeLayout ten_seconds = (RelativeLayout) findViewById(R.id.ten_seconds);
        ten_seconds.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (device.getMasterId() == loginContact.getPeerId()){
                    imService.getDeviceManager().settingOpen(currentUserId, "",
                            IMDevice.SettingType.SETTING_TYPE_PHONE_LIGHT_TIME, 60, rsp);
                }else{
                    Utils.showToast(LightTimeActivity.this , getString(R.string.no_authority_to_operate));
                }

            }
        });

    }

    public void initDetailProfile() {

        if (rsp.getLight() == 15) {
            three_seconds_right.setVisibility(View.VISIBLE);
            sex_seconds_right.setVisibility(View.GONE);
            ten_seconds_right.setVisibility(View.GONE);

        } else if (rsp.getLight() == 30) {
            three_seconds_right.setVisibility(View.GONE);
            sex_seconds_right.setVisibility(View.VISIBLE);
            ten_seconds_right.setVisibility(View.GONE);

        } else if (rsp.getLight() == 60) {

            three_seconds_right.setVisibility(View.GONE);
            sex_seconds_right.setVisibility(View.GONE);
            ten_seconds_right.setVisibility(View.VISIBLE);

        }

        if (currentUser != null) {
            if (currentUser.getUserType() == IMBaseDefine.ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE) {
                TextView left_text = (TextView) findViewById(R.id.left_text);
                left_text.setText("小雨手机");
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeviceEvent event) {
        switch (event) {

            case USER_INFO_SETTING_DEVICE_SUCCESS:
                LightTimeActivity.this.finish();
                break;

        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
                imServiceConnector.disconnect(this);
        super.onDestroy();
    }

}
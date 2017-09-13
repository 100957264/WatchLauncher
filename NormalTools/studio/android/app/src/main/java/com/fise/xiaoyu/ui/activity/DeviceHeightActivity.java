package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.ui.base.TTBaseFragmentActivity;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 *  设备的身高体重
 */
public class DeviceHeightActivity  extends TTBaseFragmentActivity {
    private Logger logger = Logger.getLogger(HelpActivity.class);
    private Handler uiHandler = new Handler();
    private IMService imService;
    private ListView listView = null;
    private int currentUserId;
    private UserEntity currentUser;
    private EditText device_height;
    private DeviceEntity rsp;
    private UserEntity loginContact;
    private TextView confirm;
    private int type;


    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            logger.d("login#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                // 后台服务启动链接失败
                return;
            }

            currentUserId = DeviceHeightActivity.this.getIntent().getIntExtra(
                    IntentConstant.KEY_PEERID, 0);
            type = DeviceHeightActivity.this.getIntent().getIntExtra(
                    IntentConstant.DEVICE_HEIGHT, 0);


            if (currentUserId == 0) {
                logger.e("detail#intent params error!!");
                return;
            }
            currentUser = imService.getContactManager().findDeviceContact(
                    currentUserId);
            if (currentUser == null) {
                return;
            }

            rsp = imService.getDeviceManager().findDeviceCard(currentUserId);
            if (rsp == null) {
                return;
            }
            TextView left_text = (TextView) findViewById(R.id.left_text);
            TextView height_type = (TextView) findViewById(R.id.height_type);
            if(type == DBConstant.DEVICE_HEIGHT){
                device_height.setText("" + currentUser.getHeight());
                left_text.setText("身高");
                height_type.setText("cm");

            }else{
                device_height.setText("" + currentUser.getWeight());
                left_text.setText("体重");
                height_type.setText("kg");
            }


            loginContact = IMLoginManager.instance().getLoginInfo();
            if (rsp.getMasterId() != loginContact.getPeerId()) {
                device_height.setEnabled(false);
                confirm.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        imServiceConnector.connect(DeviceHeightActivity.this);


        setContentView(R.layout.tt_activity_device_height);

        device_height = (EditText) findViewById(R.id.device_height);

        TextView left_text = (TextView) findViewById(R.id.left_text);
        left_text.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                DeviceHeightActivity.this.finish();
            }
        });

        confirm = (TextView) findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (device_height.getText().toString().equals("")) {
                    Utils.showToast(DeviceHeightActivity.this, "请输入数字");
                } else {
                    int num;
                    try { // 如果转换异常则输入的不是数字


                        if(type == DBConstant.DEVICE_HEIGHT){
                            if(device_height.getText().toString().equals(currentUser.getHeight()+"")){
                                return;
                            }
                        }else{
                            if(device_height.getText().toString().equals(currentUser.getWeight()+"")){
                                return;
                            }
                        }

                        num = Integer.parseInt(device_height.getText().toString());
                        if (num >= 0) {
                            if(type == DBConstant.DEVICE_HEIGHT){
                                if (currentUser.getHeight() == 0) {
                                    imService.getDeviceManager().settingWhite(
                                            currentUserId,
                                            ""+num,
                                            IMDevice.SettingType.SETTING_TYPE_USER_HEIGHT,
                                            DBConstant.ADD);
                                } else {
                                    imService.getDeviceManager().settingWhite(
                                            currentUserId,
                                            ""+num,
                                            IMDevice.SettingType.SETTING_TYPE_USER_HEIGHT,
                                            DBConstant.UPDATE);
                                }

                            }else{
                                if (currentUser.getWeight() == 0) {
                                    imService.getDeviceManager().settingWhite(
                                            currentUserId,
                                            ""+num,
                                            IMDevice.SettingType.SETTING_TYPE_USER_WEIGHT,
                                            DBConstant.ADD);
                                } else {
                                    imService.getDeviceManager().settingWhite(
                                            currentUserId,
                                            ""+num,
                                            IMDevice.SettingType.SETTING_TYPE_USER_WEIGHT,
                                            DBConstant.UPDATE);
                                }
                            }

                        } else{
                            Utils.showToast(DeviceHeightActivity.this, "输入的不是数字");
                        }
                    } catch (Exception e) {
                        Utils.showToast(DeviceHeightActivity.this, "输入的不是数字");
                    }

                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(DeviceHeightActivity.this);
            }

    // 为什么会有两个这个
    // 可能是 兼容性的问题 导致两种方法onBackPressed
    @Override
    public void onBackPressed() {
        logger.d("login#onBackPressed");
        // imLoginMgr.cancel();
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * ----------------------------event 事件驱动----------------------------
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeviceEvent event) {
        switch (event) {

            case USER_INFO_SETTING_DEVICE_SUCCESS:
                currentUser = imService.getContactManager().findDeviceContact(
                        currentUserId);
                DeviceHeightActivity.this.finish();

                break;
            case USER_INFO_SETTING_DEVICE_FAILED:
                Utils.showToast(DeviceHeightActivity.this,
                        "设置失败");

                break;

        }
    }




}

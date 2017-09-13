package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.CustomDatePicker;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by weileiguan on 2017/5/23 0023.
 */
public class DeviceBirthdayActivity  extends TTBaseActivity implements View.OnClickListener {

    private RelativeLayout selectDate;
    private TextView currentDate;
    private CustomDatePicker customDatePicker1;
    private IMService imService;
    private UserEntity currentDevice;
    private DeviceEntity rsp;
    private int deviceUserId;


    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            logger.d("login#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            try {
                do {
                    if (imService == null) {
                        // 后台服务启动链接失败
                        break;
                    }
                    deviceUserId = DeviceBirthdayActivity.this.getIntent()
                            .getIntExtra(IntentConstant.KEY_PEERID, 0);

                    currentDevice = imService.getContactManager()
                            .findDeviceContact(deviceUserId);
                    rsp = imService.getDeviceManager().findDeviceCard(
                            deviceUserId);
                    if (rsp == null) {
                        return;
                    }


                    if(currentDevice.getBirthday()!=null
                            &&(!currentDevice.getBirthday().equals(""))
                            &&(!currentDevice.getBirthday().equals("0"))
                            &&(Utils.isDataNumeric(currentDevice.getBirthday()))){
                        currentDate.setText(Utils.timesTwo(currentDevice.getBirthday()));//(currentUser.getBirthday()+"");
                    }


                    return;
                } while (false);

                // 异常分支都会执行这个
                // handleNoLoginIdentity();
            } catch (Exception e) {
                // 任何未知的异常
                logger.w("loadIdentity failed");
                // handleNoLoginIdentity();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_device_birthday);

        selectDate = (RelativeLayout) findViewById(R.id.selectDate);
        selectDate.setOnClickListener(this);
        currentDate = (TextView) findViewById(R.id.currentDate);
        deviceUserId = DeviceBirthdayActivity.this.getIntent()
                .getIntExtra(IntentConstant.KEY_PEERID, 0);

        imServiceConnector.connect(DeviceBirthdayActivity.this);

        TextView left_text = (TextView) findViewById(R.id.left_text);
        left_text.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                DeviceBirthdayActivity.this.finish();
            }
        });

        Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
        icon_arrow.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                DeviceBirthdayActivity.this.finish();
            }
        });
        initDatePicker();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.selectDate:
                // 日期格式为yyyy-MM-dd
                customDatePicker1.show(currentDate.getText().toString());
                break;

        }
    }

    private void initDatePicker() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        String now = sdf.format(new Date());
        currentDate.setText(now.split(" ")[0]);

        customDatePicker1 = new CustomDatePicker(this, new CustomDatePicker.ResultHandler() {
            @Override
            public void handle(String time) { // 回调接口，获得选中的时间
                currentDate.setText(time.split(" ")[0]);


                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = null;
                try {
                    date = format.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                long  shijian = date.getTime()/1000;
                if (currentDevice.getPhone().equals("")) {
                    imService.getDeviceManager().settingWhite(
                            deviceUserId,
                            shijian+"",
                            IMDevice.SettingType.SETTING_TYPE_USER_BIRTHDAY,
                            DBConstant.ADD);
                } else {
                    imService.getDeviceManager().settingWhite(
                            deviceUserId,
                            shijian+"",
                            IMDevice.SettingType.SETTING_TYPE_USER_BIRTHDAY,
                            DBConstant.UPDATE);
                }
            }
        }, "1900-01-01 00:00", now); // 初始化日期格式请用：yyyy-MM-dd HH:mm，否则不能正常运行
        customDatePicker1.showSpecificTime(false); // 不显示时和分
        customDatePicker1.setIsLoop(false); // 不允许循环滚动


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(DeviceBirthdayActivity.this);
            }


    /**
     * ----------------------------event 事件驱动----------------------------
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginEvent event) {
        switch (event) {


        }
    }
}


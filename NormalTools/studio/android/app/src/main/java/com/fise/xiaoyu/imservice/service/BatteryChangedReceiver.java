package com.fise.xiaoyu.imservice.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.imservice.manager.IMDeviceManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.utils.Utils;

/**
 * Created by weileiguan on 2017/8/31 0031.
 */
public class BatteryChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        final String action = intent.getAction();
        if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED))
        {//电量变化会

        }else if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_LOW)) {
            // 表示当前电池电量低

            if(IMLoginManager.instance().getLoginInfo()!=null){

                if(Utils.isClientType(IMLoginManager.instance().getLoginInfo())){
                     DeviceEntity device = IMDeviceManager.instance().findDeviceCard(IMLoginManager.instance().getLoginId());
                    if(device!=null&&(device.getAlrBattery() == 1)){  //如果设备打开了　开报警
                        IMDeviceManager.instance().postionPublishEvent(IMBaseDefine.EventKey.EVENT_KEY_LOW_BATTERY,device.getFamilyGroupId(), IMBaseDefine.SessionType.SESSION_TYPE_GROUP);
                    }
                }
            }
        } else if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_OKAY)) {
            // 表示当前电池已经从电量低恢复为正常

        }
    }

}
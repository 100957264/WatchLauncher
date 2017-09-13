package com.fise.xw.device.entity;

import com.fise.xw.DB.entity.DeviceEntity;

/**
 * 卡片机的系统配置信息
 * 
 * @author weileiguan
 * 
 */
public class CardDeviceEntity extends DeviceEntity {

	private CardDeviceEntity(DeviceEntity entity) {
		/** 父类的id */
		id = entity.getId();
		device_id = entity.getDeviceId();
		master_id = entity.getMasterId();
		devType = entity.getDevType();
		mobile = entity.getMobile();
		alr_battery = entity.getAlrBattery();
		alr_poweroff = entity.getAlrPoweroff();
		alr_call = entity.getAlrCall();
		mode = entity.getMode();
		bell_mode = entity.getBellMode();
		updated = entity.getUpdated();
		family_group_id = entity.getFamilyGroupId();
		diff = entity.getDiff();
	} 
}

package com.fise.xw.device.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.config.DBConstant;


/**
 * 儿童手表的系统配置信息
 * @author weileiguan
 *
 */
public class WatchDeviceEntity extends DeviceEntity {
	private int light_time; 

	public WatchDeviceEntity() { 

	}
	
	private WatchDeviceEntity(DeviceEntity entity) {
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
 

	public void setLight(int light_time) {
		this.light_time = light_time;
	}

	public int getLight() {
		return this.light_time;
	}

	public static WatchDeviceEntity parseFromDB(DeviceEntity entity) {

		WatchDeviceEntity watchEntity = new WatchDeviceEntity(
				entity);
		String diff = entity.getDiff();
		JSONObject extraContent;
		try {
			extraContent = new JSONObject(diff);
			watchEntity.setLight(extraContent.getInt("light_time")); 
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return watchEntity;
	}

	public void setContent() {
		JSONObject extraContent = new JSONObject();
		try {
			extraContent.put("light_time", light_time); 
			diff = extraContent.toString();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}

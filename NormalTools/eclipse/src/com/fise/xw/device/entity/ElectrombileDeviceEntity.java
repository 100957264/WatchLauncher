package com.fise.xw.device.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.config.DBConstant;


/**
 *  电动车的系统配置信息
 * @author weileiguan
 *
 */
public class ElectrombileDeviceEntity extends DeviceEntity {
	private int speed;
	private int speed_limit;

	public ElectrombileDeviceEntity() { 

	}
	
	private ElectrombileDeviceEntity(DeviceEntity entity) {
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

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getSpeed() {
		return this.speed;
	}

	public void setSpeedLimit(int speed_limit) {
		this.speed_limit = speed_limit;
	}

	public int getSpeedLimit() {
		return this.speed_limit;
	}

	public static ElectrombileDeviceEntity parseFromDB(DeviceEntity entity) {

		ElectrombileDeviceEntity electrombileEntity = new ElectrombileDeviceEntity(
				entity);
		String diff = entity.getDiff();
		JSONObject extraContent;
		try {
			extraContent = new JSONObject(diff);
			electrombileEntity.setSpeed(extraContent.getInt("speed"));
			electrombileEntity
					.setSpeedLimit(extraContent.getInt("speed_limit"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return electrombileEntity;
	}

	public void setContent() {
		JSONObject extraContent = new JSONObject();
		try {
			extraContent.put("speed", speed);
			extraContent.put("speed_limit", speed_limit);
			diff = extraContent.toString();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}

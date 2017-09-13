package com.fise.xiaoyu.device.entity;

import com.fise.xiaoyu.DB.entity.DeviceEntity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 小雨手机的系统配置信息
 */
public class MobilePhoneDeviceEntity extends DeviceEntity {
	private int light_time;
	private int step_mode;// 新增计步 getStepMode
	private int school_id; // 学校id

	public MobilePhoneDeviceEntity() {

	}

	private MobilePhoneDeviceEntity(DeviceEntity entity) {
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

	public void setSchool_id(int school_id) {
		this.school_id = school_id;
	}

	public int getSchool_id() {
		return school_id;
	}

	public void setStep_mode(int step_mode) {
		this.step_mode = step_mode;
	}

	public int getStep_mode() {
		return this.step_mode;
	}

	public static MobilePhoneDeviceEntity parseFromDB(DeviceEntity entity) {

		MobilePhoneDeviceEntity mobilePhoneEntity = new MobilePhoneDeviceEntity(
				entity);
		String diff = entity.getDiff();
		JSONObject extraContent;
		try {
			extraContent = new JSONObject(diff);
			if (!extraContent.isNull("light_time")) {
				mobilePhoneEntity.setLight(extraContent.getInt("light_time"));
			}

			if (!extraContent.isNull("step_mode")) {
				mobilePhoneEntity
						.setStep_mode(extraContent.getInt("step_mode"));
			}

			if (!extraContent.isNull("school_id")) {
				mobilePhoneEntity
						.setSchool_id(extraContent.getInt("school_id"));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return mobilePhoneEntity;
	}

	public void setContent() {
		JSONObject extraContent = new JSONObject();
		try {

			extraContent.put("light_time", light_time);
			extraContent.put("step_mode", step_mode);
			extraContent.put("school_id", school_id);

			diff = extraContent.toString();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}

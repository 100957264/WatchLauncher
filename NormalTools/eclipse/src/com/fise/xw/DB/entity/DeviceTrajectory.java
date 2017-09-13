package com.fise.xw.DB.entity;


/**
 *  设备的信息
 * @author weileiguan
 *
 */
public class DeviceTrajectory {

	protected Long id;
	protected int action_id;
	protected int device_id;
	protected int action_type;
	protected int action_value;
	protected String action_param;
	protected int status;
	protected int updated;
	protected int Lastupdated;
	protected String lng;
	protected String lat;
	protected String param;
	

	public DeviceTrajectory() {
	}

	public DeviceTrajectory(Long id) {
		this.id = id;
	}

	public DeviceTrajectory(Long id, int action_id, int device_id,
			int action_type, int action_value, String action_param, int status,
			int updated, int Lastupdated,String lng,String lat,String param) {
		this.id = id;
		this.action_id = action_id;
		this.device_id = device_id;
		this.action_type = action_type;
		this.action_value = action_value;
		this.action_param = action_param;
		this.status = status;
		this.updated = updated;
		this.Lastupdated = Lastupdated;
		
		this.lng = lng;
		this.lat = lat;
		
		this.param = param;
		
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof DeviceTrajectory))
			return false;

		DeviceTrajectory entity = (DeviceTrajectory) o;

		if (action_id != entity.action_id)
			return false;
		if (device_id != entity.device_id)
			return false;
		if (action_type != entity.action_type)
			return false;
		if (action_value != entity.action_value)
			return false;

		if (action_param != null && (action_param.equals(entity.updated)))
			return false;
		if (status != entity.status)
			return false;
		if (updated != entity.updated)
			return false;
		if (Lastupdated != entity.Lastupdated)
			return false;
		
		if (lng != entity.lng)
			return false;
		if (lat != entity.lat)
			return false;

		
		if (param != entity.param)
			return false;
		return true;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setActionId(int action_id) {
		this.action_id = action_id;
	}

	public int getActionId() {
		return action_id;
	}

	public void setDeviceId(int device_id) {
		this.device_id = device_id;
	}

	public int getDeviceId() {
		return device_id;
	}

	public void setActionType(int action_type) {
		this.action_type = action_type;
	}

	public int getActionType() {
		return action_type;
	}

	public void setActionValue(int action_value) {
		this.action_value = action_value;
	}

	public int getActionValue() {
		return action_value;
	}

	public void setActionParam(String action_param) {
		this.action_param = action_param;
	}

	public String getActionParam() {
		return action_param;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public void setUpdated(int updated) {
		this.updated = updated;
	}

	public int getUpdated() {
		return updated;
	}

	public void setLastUpdated(int Lastupdated) {
		this.Lastupdated = Lastupdated;
	}

	public int getLastUpdated() {
		return Lastupdated;
	}
	
	
	
	public void setLng(String lng) {
		this.lng = lng;
	}

	public String getLng() {
		return lng;
	}
	
	
	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLat() {
		return lat;
	}
	
	
	public void setParam(String param) {
		this.param = param;
	}

	public String getParam() {
		return param;
	}

}

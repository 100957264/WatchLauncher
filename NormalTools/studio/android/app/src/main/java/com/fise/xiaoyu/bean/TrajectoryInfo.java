package com.fise.xiaoyu.bean;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *  请求历史轨迹的数据
 */
public class TrajectoryInfo implements Serializable {

	private static final long serialVersionUID = -758459502806858414L;
	/**
	 * 经度
	 */
	private double latitude;
	/**
	 * 纬度
	 */
	private double longitude;

	/**
	 * 时间
	 */
	private int time;



	public static List<TrajectoryInfo> infos = new ArrayList<TrajectoryInfo>();


	public TrajectoryInfo() {

	}


	public void addInfo(double latitude, double longitude,int time){
		infos.add(new TrajectoryInfo(latitude,longitude,time));
	}

	public TrajectoryInfo(double latitude, double longitude, int time) {

		//super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.time = time;

	}

	public double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}


	public int getTime()
	{
		return time;
	}

	public void setTime(int time)
	{
		this.time = time;
	}

}

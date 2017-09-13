package com.fise.historytrack.data;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
 
public class Info implements Serializable
{
	private static final long serialVersionUID = -758459502806858414L; 
	/**          														
	 * 精度						
	 */
	private double latitude;
	/**
	 * 纬度
	 */
	private double longitude;



	public static List<Info> infos = new ArrayList<Info>();

	static
	{
		//infos.add(new Info(34.72557,113.798519));
		//infos.add(new Info(34.75457,113.768519));
		//infos.add(new Info(34.75357,113.790519));
		//infos.add(new Info(34.75657,113.788519));
	}

	public Info()
	{
	}
	 
	
	public void addInfo(double latitude, double longitude){
		infos.add(new Info(latitude,longitude));
	}

	public Info(double latitude, double longitude)
	{
		super();
		this.latitude = latitude;
		this.longitude = longitude;

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

	
}

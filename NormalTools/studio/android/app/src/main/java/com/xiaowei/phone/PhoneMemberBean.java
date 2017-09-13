package com.xiaowei.phone;

import com.fise.xiaoyu.DB.entity.UserEntity;

public class PhoneMemberBean {

	private String name;    
	private String sortLetters;   
	 
	private String number;  
	private UserEntity userInfo;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	public UserEntity getUserEntity() {
		return userInfo;
	}
	public void setUserEntity(UserEntity userInfo) {
		this.userInfo = userInfo;
	}
	
	
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
}

package com.fise.xw.DB.entity;

import java.io.Serializable;

import com.fise.xw.DB.sp.SystemConfigSp;
import com.fise.xw.protobuf.helper.EntityChangeEngine;



/**
 * 群昵称的信息表
 * @author weileiguan
 *
 */
public class GroupNickEntity {

	protected Long id;
	protected int groupId;
	protected int userId;
	protected int status;
	protected String nick;
	protected int created;
	protected int updated; 
	protected int save; 

	public GroupNickEntity() {

	}

	public GroupNickEntity(Long id) {
		this.id = id;
	}

	public GroupNickEntity(Long id, int groupId, int userId, int status,
			String nick, int created, int updated,int save) {
		this.id = id;
		this.groupId = groupId;
		this.userId = userId; 
		this.status = status; 
		this.nick = nick; 
		this.created = created; 
		this.updated = updated; 
		this.save = save; 

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	 

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	

	public int getCreated() {
		return created;
	}

	public void setCreated(int created) {
		this.created = created;
	}

	
	public int getUpdated() {
		return updated;
	}

	public void setUpdated(int updated) {
		this.updated = updated;
	}
	
	
	
	public int getSave() {
		return save;
	}

	public void setSave(int save) {
		this.save = save;
	}


 

	// KEEP METHODS - put your custom methods here

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof GroupNickEntity))
			return false;

		GroupNickEntity entity = (GroupNickEntity) o;

		if (groupId != entity.groupId)
			return false;
		if (userId != entity.userId)
			return false;
		if (status != entity.status)
			return false; 
		if (updated != entity.updated)
			return false;
		if (status != entity.status)
			return false;
		if (!(nick.equals(entity.nick)))
			return false;
		if (created != entity.created)
			return false;
		
		if (save != entity.save)
			return false;
		
		return true;
	}

	// KEEP METHODS END

}

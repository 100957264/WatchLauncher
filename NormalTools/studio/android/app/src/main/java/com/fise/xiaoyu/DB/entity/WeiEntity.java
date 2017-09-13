package com.fise.xiaoyu.DB.entity;

import android.text.TextUtils;
import android.util.Log;

import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.entity.SearchElement;
import com.fise.xiaoyu.utils.pinyin.PinYin.PinYinElement;
 

/**
 *  好友/位友请求数据
 */
public class WeiEntity {
 
	protected Long id; 
    protected int from_id;
    protected int to_id;
    protected int act_id;
    protected int act_type;
    protected int status;
    protected int updated;
    public int device_id = 0;
    public String masgData;

   
    
    public WeiEntity() {
    	
    }

    public WeiEntity(Long id) {
        this.id = id;
    }

    public WeiEntity(Long id, int from_id, int to_id, int act_id, int  act_type, int status,int updated,int device_id,String masgData) {
    	 this.id = id; 
    	 this.from_id = from_id;
    	 this.to_id = to_id;
    	 this.act_id = act_id;
    	 this.act_type = act_type;
    	 this.status = status;
    	 this.updated = updated;
        this.device_id = device_id;
    	 this.masgData = masgData;
    }
 
     
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) { 
        this.id = id;
    }
    
    
    
    public int getFromId() {
        return from_id;
    }

    public void setFromId(int fromId) {
        this.from_id = fromId;
    }
 
    
    public int getToId() {
        return to_id;
    }

    public void setToId(int toId) {
        this.to_id = toId;
    }
 
    
    public int getActId() {
        return act_id;
    }

    public void setActId(int act_id) {
        this.act_id = act_id;
    }
    
    public int getActType() {
        return act_type;
    }

    public void setActType(int act_type) {
        this.act_type = act_type;
    }
    
    

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

 

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }
     
    public String getMasgData() {
        return this.masgData;
    }

    public void setMasgData(String masgData) {
        this.masgData = masgData;
    }


    public int getDevice_id() {
        return this.device_id;
    }

    public void setDevice_id(int device_id) {
        this.device_id = device_id;
    }


    // KEEP METHODS - put your custom methods here


 

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeiEntity)) return false;

        WeiEntity entity = (WeiEntity) o;

        if (from_id != entity.from_id) return false;
        if (to_id != entity.to_id) return false;
        if (act_id != entity.act_id) return false;
        if (act_type != entity.act_type) return false;
        if (updated != entity.updated) return false; 
        if (status != entity.status) return false; 
        if(!(masgData.equals(entity.masgData))) return false;
        if (device_id != entity.device_id) return false;

        return true;
    }
 

    // KEEP METHODS END

}

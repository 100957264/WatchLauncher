package com.fise.xiaoyu.DB.entity;

 
import android.text.TextUtils;
import android.util.Log;

import com.fise.xiaoyu.DB.sp.SystemConfigSp;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.entity.SearchElement;
import com.fise.xiaoyu.utils.pinyin.PinYin.PinYinElement;
 

/**
 * 授权人的基本信息
 *
 */
public class FamilyConcernEntity {
 
	protected Long id; 
	private int peeId;
    protected String identity;  
    protected String avatar;
    protected String phone;
    private int devId;
     
    public FamilyConcernEntity() {
    	
    }

    public FamilyConcernEntity(Long id) {
        this.id = id;
    }

    public FamilyConcernEntity(Long id,int peeId,String identity,String avatar,String phone,int devId) {
    	 this.id = id;  
    	 this.peeId = peeId;  
    	 this.identity = identity; 
    	 this.avatar = avatar; 
    	 this.phone = phone; 
    	 this.devId = devId;
    }
 
     
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) { 
        this.id = id;
    }
     
    
    public int getDevId() {
        return devId;
    }

    public void setDevId(int devId) { 
        this.devId = devId;
    }
    
    
    
    
    public int getPeeId() {
        return peeId;
    }

    public void setPeeId(int peeId) { 
        this.peeId = peeId;
    }
    
    
    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }
  
     
    public String getAvatar() {
        return avatar; 
    }
    
    public String getUserAvatar() {
    	return SystemConfigSp.instance().getStrConfig(
				SystemConfigSp.SysCfgDimension.MSFSSERVER)
				+ avatar;
    }


    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
 
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FamilyConcernEntity)) return false;

        FamilyConcernEntity entity = (FamilyConcernEntity) o; 
        if (identity != entity.identity) return false; 
        if (avatar != entity.avatar) return false; 
        if (phone != entity.phone) return false; 
        return true;
    }
 

    // KEEP METHODS END

}

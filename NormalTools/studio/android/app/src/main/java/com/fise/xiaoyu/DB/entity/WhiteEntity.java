package com.fise.xiaoyu.DB.entity;


/**
 *  白名单/紧急号码  数据
 */
public class WhiteEntity {
 
	protected Long id; 
    protected String phone;
    private String name="";
    private int devId;
     
    public WhiteEntity() {
    	
    }

    public WhiteEntity(Long id) {
        this.id = id;
    }

    public WhiteEntity(Long id,String phone,String name,int devId) {
    	 this.id = id;  
    	 this.phone = phone; 
    	 this.devId = devId;
         this.name = name;
    }
 
     
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) { 
        this.id = id;
    }
     
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    
    
    public int getDevId() {
        return devId;
    }

    public void setDevId(int devId) {
        this.devId = devId;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WhiteEntity)) return false;

        WhiteEntity entity = (WhiteEntity) o; 
        
        if (!phone.equals(entity.phone)) return false; 
        if (devId != entity.devId) return false;
        if (!name.equals(entity.name)) return false;

        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    // KEEP METHODS END

}

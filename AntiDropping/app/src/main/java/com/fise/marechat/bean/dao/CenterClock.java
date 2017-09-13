package com.fise.marechat.bean.dao;

import com.fise.marechat.dao.CenterClockDao;
import com.fise.marechat.dao.ClockFormatDao;
import com.fise.marechat.dao.DaoSession;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

/**
 * @author mare
 * @Description:
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/10
 * @time 17:31
 */
@Entity
public class CenterClock extends CenterSettingBase {
    @Id
    private long id;

    public String imei;//主键

    private long center_clock_id;//CenterClock的主键

//    说明:闹铃格式为：时间-开关-频率(1：一次；2:每天;3：自定义)
//    08:10-1-1：闹钟时间8:10，打开，响铃一次
//     08:10-1-2：闹钟时间8:10，打开，每天响铃
//     08:10-1-3-0111110：闹钟时间8:10，打开，自定义周一至周五打开

    @ToMany(referencedJoinProperty = "clock_id")
    private List<ClockFormat> clocks;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1445287121)
    private transient CenterClockDao myDao;

    @Generated(hash = 1207994741)
    public CenterClock(long id, String imei, long center_clock_id) {
        this.id = id;
        this.imei = imei;
        this.center_clock_id = center_clock_id;
    }

    @Generated(hash = 1738805301)
    public CenterClock() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 946840857)
    public List<ClockFormat> getClocks() {
        if (clocks == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ClockFormatDao targetDao = daoSession.getClockFormatDao();
            List<ClockFormat> clocksNew = targetDao._queryCenterClock_Clocks(id);
            synchronized (this) {
                if (clocks == null) {
                    clocks = clocksNew;
                }
            }
        }
        return clocks;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1279489683)
    public synchronized void resetClocks() {
        clocks = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 749018373)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getCenterClockDao() : null;
    }

    public long getCenter_clock_id() {
        return this.center_clock_id;
    }

    public void setCenter_clock_id(long center_clock_id) {
        this.center_clock_id = center_clock_id;
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

}

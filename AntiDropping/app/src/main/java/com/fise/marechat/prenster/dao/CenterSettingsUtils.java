package com.fise.marechat.prenster.dao;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.fise.marechat.bean.dao.CenterSettings;
import com.fise.marechat.dao.CenterSettingsDao;
import com.fise.marechat.manager.DBManager;
import com.fise.marechat.utils.LogUtils;

import java.util.List;

/**
 * @author mare
 * @Description:
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/11
 * @time 17:12
 */
public class CenterSettingsUtils implements GreenDaoChargeRecordImpl<CenterSettings> {

    private CenterSettingsDao settingsDao;

    private CenterSettingsUtils() {
        getSettingsDao();
    }

    private static class SingletonHolder {
        private static final CenterSettingsUtils INSTANCE = new CenterSettingsUtils();
    }

    public static CenterSettingsUtils instance() {
        return SingletonHolder.INSTANCE;
    }

    private CenterSettingsDao getSettingsDao() {
        if (null == settingsDao) {
            settingsDao = DBManager.instance().getDaoSession().getCenterSettingsDao();
        }
        return settingsDao;
    }


    @Override
    public long insert(CenterSettings data) {
        long add = settingsDao.insert(data);
        return add;
    }

    /****
     * 指定imei修改信息
     * @param data
     */
    @Override
    public boolean update(CenterSettings data) {
        String imei = data.getImei();
        CenterSettings user = settingsDao.queryBuilder().where(CenterSettingsDao.Properties.Imei.eq(imei)).build().unique();
        if (null != user) {
            settingsDao.update(data);
        } else {
            Log.i("update", "该条件下的数据为空");
        }

        CenterSettings userQuery = null;
        CenterSettingsDao dao = getSettingsDao();
        long insertUsersID = -1;
        String className = data.getClass().getSimpleName();
        try {
            userQuery = dao.queryBuilder().where(CenterSettingsDao.Properties.Imei.eq(imei)).build().unique();
        } catch (SQLiteException e) {
            LogUtils.e("查询 " + className + "失败");
        }
        if (null != userQuery) {
            data.setId(userQuery.getId());
            LogUtils.d("开始更新" + className + "数据...");
            insertUsersID = dao.insertOrReplace(data);
        } else {
            LogUtils.d("没找到" + className + "的所在数据 开始插入");
            insertUsersID = dao.insertOrReplace(data);
        }
        boolean isSuccess = insertUsersID >= 0;
        return isSuccess;
    }

    /***
     * 删除全部
     */
    @Override
    public void deleteAll() {
        getSettingsDao().deleteAll();
    }

    /***
     * 条件删除
     * @param id
     */
    @Override
    public void deleteWhere(long id) {
        getSettingsDao().deleteByKey(id);
    }

    /***
     * 查询全部
     * @return
     */
    @Override
    public List<CenterSettings> selectAll() {
        List<CenterSettings> list = getSettingsDao().loadAll();
        return null != list && list.size() > 0 ? list : null;
    }

    /***
     * 模糊查询
     * @param data
     * @return
     */
    @Override
    public List<CenterSettings> selectWhere(CenterSettings data) {
        String imei = data.getImei();
        List<CenterSettings> list = getSettingsDao().queryBuilder().where(
                CenterSettingsDao.Properties.Imei.like(imei)).build().list();
        return null != list && list.size() > 0 ? list : null;
    }

    /***
     * 唯一查询
     * @param name
     * @return
     */
    @Override
    public CenterSettings seelctWhrer(String name) {
        CenterSettings user = getSettingsDao().queryBuilder().where(
                CenterSettingsDao.Properties.Imei.eq(name)).build().unique();
        return null != user ? user : null;
    }

    /***
     * Id查询
     * @param id
     * @return
     */
    @Override
    public List<CenterSettings> selectWhrer(long id) {
        List<CenterSettings> users = getSettingsDao().queryBuilder().where(
                CenterSettingsDao.Properties.Id.le(id)).build().list();
        return null != users && users.size() > 0 ? users : null;
    }

    /**
     * 更新中心设置信息
     *
     * @param settings
     * @return
     */
    public boolean updateCenterPhoneNum(CenterSettings settings) {
        String phoneNum = settings.getCenterPhoneNum();
        String imei = settings.getImei();
        CenterSettings userQuery = null;
        CenterSettingsDao dao = getSettingsDao();
        long insertUsersID = -1;
        String className = settings.getClass().getSimpleName();
        try {
            userQuery = dao.queryBuilder().where(CenterSettingsDao.Properties.Imei.eq(imei)).build().unique();
        } catch (SQLiteException e) {
            LogUtils.e("查询 " + className + "失败");
        }
        if (null != userQuery) {
            userQuery.setCenterPhoneNum(phoneNum);
            LogUtils.d("开始更新" + className + "数据...");
            insertUsersID = dao.insertOrReplace(userQuery);
        } else {
            LogUtils.d("没找到" + className + "的所在数据 开始插入");
            insertUsersID = dao.insertOrReplace(settings);
        }
        boolean isSuccess = insertUsersID >= 0;
        return isSuccess;
    }

    /**
     * 清空中心设置信息
     *
     * @param ctx
     * @return
     */
    public void clearCenterSettings(Context ctx) {
        CenterSettingsDao dao = getSettingsDao();
        dao.deleteAll();
    }
}

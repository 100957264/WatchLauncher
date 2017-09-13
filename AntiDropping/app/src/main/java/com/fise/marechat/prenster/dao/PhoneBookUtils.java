package com.fise.marechat.prenster.dao;

import android.content.Context;
import android.database.sqlite.SQLiteException;

import com.fise.marechat.bean.dao.MessagePhrase;
import com.fise.marechat.bean.dao.PhoneBook;
import com.fise.marechat.dao.MessagePhraseDao;
import com.fise.marechat.dao.PhoneBookDao;
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
public class PhoneBookUtils implements GreenDaoChargeRecordImpl<PhoneBook> {

    private PhoneBookDao centerDao;

    private MessagePhraseDao phraseDao;

    private PhoneBookUtils() {
        getCenterDao();

    }

    private static class SingletonHolder {
        private static final PhoneBookUtils INSTANCE = new PhoneBookUtils();
    }

    public static PhoneBookUtils instance() {
        return SingletonHolder.INSTANCE;
    }

    private PhoneBookDao getCenterDao() {
        if (null == centerDao) {
            centerDao = DBManager.instance().getDaoSession().getPhoneBookDao();
        }
        return centerDao;
    }

    private MessagePhraseDao getPhraseDao() {
        if (null == phraseDao) {
            phraseDao = DBManager.instance().getDaoSession().getMessagePhraseDao();
        }
        return phraseDao;
    }

    @Override
    public long insert(PhoneBook data) {
        long add = centerDao.insert(data);
        return add;
    }

    /****
     * 指定imei修改信息
     * @param data
     */
    @Override
    public boolean update(PhoneBook data) {
        String imei = data.getImei();
        PhoneBook userQuery = null;
        PhoneBookDao dao = getCenterDao();
        long insertUsersID = -1;
        String className = data.getClass().getSimpleName();
        try {
            userQuery = dao.queryBuilder().where(PhoneBookDao.Properties.Imei.eq(imei)).build().unique();
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
        getCenterDao().deleteAll();
    }

    /***
     * 条件删除
     * @param id
     */
    @Override
    public void deleteWhere(long id) {
        getCenterDao().deleteByKey(id);
    }

    /***
     * 查询全部
     * @return
     */
    @Override
    public List<PhoneBook> selectAll() {
        List<PhoneBook> list = getCenterDao().loadAll();
        return null != list && list.size() > 0 ? list : null;
    }

    /***
     * 模糊查询
     * @param data
     * @return
     */
    @Override
    public List<PhoneBook> selectWhere(PhoneBook data) {
        String imei = data.getImei();
        List<PhoneBook> list = getCenterDao().queryBuilder().where(
                PhoneBookDao.Properties.Imei.like(imei)).build().list();
        return null != list && list.size() > 0 ? list : null;
    }

    /***
     * 唯一查询
     * @param name
     * @return
     */
    @Override
    public PhoneBook seelctWhrer(String name) {
        PhoneBook user = getCenterDao().queryBuilder().where(
                PhoneBookDao.Properties.Imei.eq(name)).build().unique();
        return null != user ? user : null;
    }

    /***
     * Id查询
     * @param id
     * @return
     */
    @Override
    public List<PhoneBook> selectWhrer(long id) {
        List<PhoneBook> users = getCenterDao().queryBuilder().where(
                PhoneBookDao.Properties.Id.le(id)).build().list();
        return null != users && users.size() > 0 ? users : null;
    }

    /**
     * 更新短消息
     *
     * @param settings
     * @return
     */
    public boolean updatePhraseMsg(PhoneBook settings) {
        String sourceFrom = "";
        List<MessagePhrase> phrases = settings.getPhraseSettings();
        String imei = settings.getImei();
        PhoneBook userQuery = null;
        PhoneBookDao dao = getCenterDao();
        long insertUsersID = -1;
        String className = settings.getClass().getSimpleName();
        try {
            userQuery = dao.queryBuilder().where(PhoneBookDao.Properties.Imei.eq(imei)).build().unique();
        } catch (SQLiteException e) {
            LogUtils.e("查询 " + className + "失败");
        }
        if (null != userQuery) {
            userQuery.setPhraseSettings(phrases);
            LogUtils.d("开始更新" + className + "数据...");
            insertUsersID = dao.insertOrReplace(userQuery);
        } else {
            LogUtils.d("没找到" + className + "的所在数据 开始插入");
            insertUsersID = dao.insertOrReplace(settings);
        }
        boolean isSuccess = insertUsersID >= 0;
        return isSuccess;
    }

    public List<MessagePhrase> queryPhraseMsg(String sourceFrom) {
        List<MessagePhrase> phrases = null;
        MessagePhrase userQuery = null;
        MessagePhraseDao dao = getPhraseDao();
        long insertUsersID = -1;
        String className = PhoneBook.class.getSimpleName();
        try {
            userQuery = dao.queryBuilder().where(MessagePhraseDao.Properties.Message_sourceFrom.eq(sourceFrom)).
            build().unique();
        } catch (SQLiteException e) {
            LogUtils.e("查询 " + className + "失败");
        }
        if (null != userQuery) {
            phrases =  dao.loadAll();
            LogUtils.d("查询完毕" + className + "数据完毕");
        } else {
            LogUtils.d("没找到" + className + "的所在数据");
        }
        boolean isSuccess = null != phrases && phrases.size() > 0;
        String alertMsg = isSuccess ? "找到了" + phrases.size() +"条数据" : "没找到" +className + "的数据";
        LogUtils.d(alertMsg);
        return phrases;
    }

    /**
     * 清空中心设置信息
     *
     * @param ctx
     * @return
     */
    public void clearCenterSettings(Context ctx) {
        PhoneBookDao dao = getCenterDao();
        dao.deleteAll();
    }
}

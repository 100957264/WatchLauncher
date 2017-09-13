package com.fise.marechat.util;


import android.content.Context;
import android.util.Log;

import com.fise.marechat.dao.CenterAlertDao;
import com.fise.marechat.dao.CenterClockDao;
import com.fise.marechat.dao.CenterCounterDao;
import com.fise.marechat.dao.CenterSettingsDao;
import com.fise.marechat.dao.ClockFormatDao;
import com.fise.marechat.dao.DaoMaster;
import com.fise.marechat.dao.DaoSession;
import com.fise.marechat.dao.MessagePhraseDao;
import com.fise.marechat.dao.PhoneBookDao;
import com.fise.marechat.dao.PhoneContractorDao;

import org.greenrobot.greendao.database.Database;

/**
 * Created by zhangqie on 2016/3/26.
 */

public class DaoHelper extends DaoMaster.OpenHelper {

    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    private static String DBNAME = "anti.db";

    public DaoHelper(Context context, String name) {
        super(context, name, null);
        DaoHelper.DBNAME = name;
    }


    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        Log.i("version", oldVersion + "---先前和更新之后的版本---" + newVersion);
        if (oldVersion < newVersion) {
            Log.i("version", oldVersion + "---先前和更新之后的版本---" + newVersion);
            MigrationHelper.getInstance().migrate(db, CenterAlertDao.class, CenterClockDao.class, CenterCounterDao.class, CenterSettingsDao.class,
                    ClockFormatDao.class, PhoneBookDao.class,MessagePhraseDao.class, PhoneContractorDao.class);
            //更改过的实体类(新增的不用加)   更新UserDao文件 可以添加多个  XXDao.class 文件
//             MigrationHelper.getInstance().migrate(db, UserDao.class,XXDao.class);
        }
    }

    /**
     * 取得DaoMaster
     *
     * @param context
     * @return
     */
    public static DaoMaster getDaoMaster(Context context) {
        if (daoMaster == null) {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(context,
                    DBNAME, null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return daoMaster;
    }

    /**
     * 取得DaoSession
     *
     * @param context
     * @return
     */
    public static DaoSession getDaoSession(Context context) {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster(context);
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }
}

package com.fise.xiaoyu.DB.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.fise.xiaoyu.DB.entity.StepData;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;


/**
 *  计步排行榜信息
*/
public class StepDataDao extends AbstractDao<StepData, Long> {

    public static final String TABLENAME = "StepDataDao";

    /**
     * Properties of entity GroupEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property toDay = new Property(1, String.class, "today", false, "TODAY");
        public final static Property step = new Property(2, String.class, "step", false, "STEP");

    };

    public StepDataDao(DaoConfig config) {
        super(config);
    }

    public StepDataDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }   

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'StepDataDao' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'TODAY' TEXT NOT NULL ," + // 1: toDay
                "'STEP' TEXT NOT NULL );"); // 2: step
    }


    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'StepDataDao'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, StepData entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getToday());
        stmt.bindString(3, entity.getStep());
    }


    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

   
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, StepData entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setToday(cursor.getString(offset + 1));
        entity.setStep(cursor.getString(offset + 2));
     }


    
    /** @inheritdoc */
    @Override
    public StepData readEntity(Cursor cursor, int offset) {
        StepData entity = new StepData( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.getString(offset + 1), // today
                cursor.getString(offset + 2) // step_num
        );
        return entity;
    }

    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(StepData entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(StepData entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }


}

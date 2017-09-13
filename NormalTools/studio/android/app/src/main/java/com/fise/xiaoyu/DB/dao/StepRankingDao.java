package com.fise.xiaoyu.DB.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.fise.xiaoyu.DB.entity.StepRanking;
import com.fise.xiaoyu.DB.entity.WhiteEntity;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;


/**
 *  计步排行榜信息
*/
public class StepRankingDao extends AbstractDao<StepRanking, Long> {

    public static final String TABLENAME = "StepRankingDao";

    /**
     * Properties of entity GroupEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");

        public final static Property ranking = new Property(1, int.class, "ranking", false, "RANKING");
        public final static Property step_num = new Property(2, int.class, "step_num", false, "STEP_NUM");
        public final static Property champion_id = new Property(3, int.class, "champion_id", false, "CHAMPION_ID");
        public final static Property create_time = new Property(4, int.class, "create_time", false, "CREATE_TIME");
        public final static Property update_time = new Property(5, int.class, "update_time", false, "UPDATE_TIME");
        public final static Property latest_data = new Property(6, int.class, "latest_data", false, "LATEST_DATA");

    };

    public StepRankingDao(DaoConfig config) {
        super(config);
    }

    public StepRankingDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }   

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'StepRankingDao' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id  
                "'RANKING' INTEGER NOT NULL ," + // 1: ranking
                "'STEP_NUM' INTEGER NOT NULL ," + // 2: step_num
                "'CHAMPION_ID' INTEGER NOT NULL ," + // 3: champion_id
                "'CREATE_TIME' INTEGER NOT NULL ," + // 4: create_time
                "'UPDATE_TIME' INTEGER NOT NULL ," + // 5: update_time
                "'LATEST_DATA' INTEGER NOT NULL );"); // 6: latest_data
    }


    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'StepRankingDao'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, StepRanking entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getRanking());
        stmt.bindLong(3, entity.getStep_num());
        stmt.bindLong(4, entity.getChampion_id());
        stmt.bindLong(5, entity.getCreate_time());
        stmt.bindLong(6, entity.getUpdate_time());
        stmt.bindLong(7, entity.getLatest_data());
    }


    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

   
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, StepRanking entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setRanking(cursor.getInt(offset + 1));
        entity.setStep_num(cursor.getInt(offset + 2));
        entity.setChampion_id(cursor.getInt(offset + 3));
        entity.setCreate_time(cursor.getInt(offset + 4));
        entity.setUpdate_time(cursor.getInt(offset + 5));
        entity.setLatest_data(cursor.getInt(offset + 6));
     }


    
    /** @inheritdoc */
    @Override
    public StepRanking readEntity(Cursor cursor, int offset) {
        StepRanking entity = new StepRanking( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.getInt(offset + 1), // ranking
                cursor.getInt(offset + 2), // step_num
                cursor.getInt(offset + 3), // champion_id
                cursor.getInt(offset + 4), // create_time
                cursor.getInt(offset + 5), // update_time
                cursor.getInt(offset + 6) // latest_data
        );
        return entity;
    }

    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(StepRanking entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(StepRanking entity) {
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

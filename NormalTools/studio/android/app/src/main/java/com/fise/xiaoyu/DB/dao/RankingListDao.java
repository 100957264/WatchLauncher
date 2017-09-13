package com.fise.xiaoyu.DB.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.fise.xiaoyu.DB.entity.RankingListEntity;
import com.fise.xiaoyu.DB.entity.StepRanking;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;


/**
 *  计步列表排行榜信息
*/
public class RankingListDao extends AbstractDao<RankingListEntity, Long> {

    public static final String TABLENAME = "RankingListDao";

    /**
     * Properties of entity GroupEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");

        public final static Property ranking_id = new Property(1, Long.class, "ranking_id", false, "RANKING_ID");
        public final static Property step_num = new Property(2, int.class, "step_num", false, "STEP_NUM");
        public final static Property champion_id = new Property(3, int.class, "champion_id", false, "CHAMPION_ID");
        public final static Property create_time = new Property(4, int.class, "create_time", false, "CREATE_TIME");
        public final static Property update_time = new Property(5, int.class, "update_time", false, "UPDATE_TIME");

    };


    public RankingListDao(DaoConfig config) {
        super(config);
    }

    public RankingListDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }   

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'RankingListDao' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'RANKING_ID' INTEGER NOT NULL ," + // 1: ranking_id
                "'STEP_NUM' INTEGER NOT NULL ," + // 2: step_num
                "'CHAMPION_ID' INTEGER NOT NULL ," + // 3: champion_id
                "'CREATE_TIME' INTEGER NOT NULL ," + // 4: create_time
                "'UPDATE_TIME' INTEGER NOT NULL );"); // 5: update_time
    }


    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'RankingListDao'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, RankingListEntity entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getRankingId());
        stmt.bindLong(3, entity.getStep_num());
        stmt.bindLong(4, entity.getChampion_id());
        stmt.bindLong(5, entity.getCreate_time());
        stmt.bindLong(6, entity.getUpdate_time());
    }


    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

   
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, RankingListEntity entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setRankingId(cursor.getLong(offset + 1));
        entity.setStep_num(cursor.getInt(offset + 2));
        entity.setChampion_id(cursor.getInt(offset + 3));
        entity.setCreate_time(cursor.getInt(offset + 4));
        entity.setUpdate_time(cursor.getInt(offset + 5));
     }

    
    /** @inheritdoc */
    @Override
    public RankingListEntity readEntity(Cursor cursor, int offset) {
        RankingListEntity entity = new RankingListEntity( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.getLong(offset + 1), // ranking
                cursor.getInt(offset + 2), // step_num
                cursor.getInt(offset + 3), // champion_id
                cursor.getInt(offset + 4), // create_time
                cursor.getInt(offset + 5) // update_time
        );
        return entity;
    }


    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(RankingListEntity entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(RankingListEntity entity) {
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

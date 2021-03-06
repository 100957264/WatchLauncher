package com.fise.marechat.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.fise.marechat.bean.dao.CenterAlert;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "CENTER_ALERT".
*/
public class CenterAlertDao extends AbstractDao<CenterAlert, Long> {

    public static final String TABLENAME = "CENTER_ALERT";

    /**
     * Properties of entity CenterAlert.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "_id");
        public final static Property Imei = new Property(1, String.class, "imei", false, "IMEI");
        public final static Property Sos_alert_switch = new Property(2, int.class, "sos_alert_switch", false, "SOS_ALERT_SWITCH");
        public final static Property Remove_alert_switch = new Property(3, int.class, "remove_alert_switch", false, "REMOVE_ALERT_SWITCH");
        public final static Property Remove_sms_alert_switch = new Property(4, int.class, "remove_sms_alert_switch", false, "REMOVE_SMS_ALERT_SWITCH");
    }


    public CenterAlertDao(DaoConfig config) {
        super(config);
    }
    
    public CenterAlertDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CENTER_ALERT\" (" + //
                "\"_id\" INTEGER PRIMARY KEY NOT NULL ," + // 0: id
                "\"IMEI\" TEXT," + // 1: imei
                "\"SOS_ALERT_SWITCH\" INTEGER NOT NULL ," + // 2: sos_alert_switch
                "\"REMOVE_ALERT_SWITCH\" INTEGER NOT NULL ," + // 3: remove_alert_switch
                "\"REMOVE_SMS_ALERT_SWITCH\" INTEGER NOT NULL );"); // 4: remove_sms_alert_switch
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CENTER_ALERT\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, CenterAlert entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        String imei = entity.getImei();
        if (imei != null) {
            stmt.bindString(2, imei);
        }
        stmt.bindLong(3, entity.getSos_alert_switch());
        stmt.bindLong(4, entity.getRemove_alert_switch());
        stmt.bindLong(5, entity.getRemove_sms_alert_switch());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, CenterAlert entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        String imei = entity.getImei();
        if (imei != null) {
            stmt.bindString(2, imei);
        }
        stmt.bindLong(3, entity.getSos_alert_switch());
        stmt.bindLong(4, entity.getRemove_alert_switch());
        stmt.bindLong(5, entity.getRemove_sms_alert_switch());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public CenterAlert readEntity(Cursor cursor, int offset) {
        CenterAlert entity = new CenterAlert( //
            cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // imei
            cursor.getInt(offset + 2), // sos_alert_switch
            cursor.getInt(offset + 3), // remove_alert_switch
            cursor.getInt(offset + 4) // remove_sms_alert_switch
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, CenterAlert entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setImei(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setSos_alert_switch(cursor.getInt(offset + 2));
        entity.setRemove_alert_switch(cursor.getInt(offset + 3));
        entity.setRemove_sms_alert_switch(cursor.getInt(offset + 4));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(CenterAlert entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(CenterAlert entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(CenterAlert entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}

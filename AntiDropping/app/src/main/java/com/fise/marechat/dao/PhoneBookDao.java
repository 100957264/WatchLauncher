package com.fise.marechat.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.fise.marechat.util.DaoStringConverter;
import java.util.List;

import com.fise.marechat.bean.dao.PhoneBook;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "PHONE_BOOK".
*/
public class PhoneBookDao extends AbstractDao<PhoneBook, Long> {

    public static final String TABLENAME = "PHONE_BOOK";

    /**
     * Properties of entity PhoneBook.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "_id");
        public final static Property Imei = new Property(1, String.class, "imei", false, "IMEI");
        public final static Property Phone_book_id = new Property(2, long.class, "phone_book_id", false, "PHONE_BOOK_ID");
        public final static Property White_list = new Property(3, String.class, "white_list", false, "WHITE_LIST");
    }

    private DaoSession daoSession;

    private final DaoStringConverter white_listConverter = new DaoStringConverter();

    public PhoneBookDao(DaoConfig config) {
        super(config);
    }
    
    public PhoneBookDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"PHONE_BOOK\" (" + //
                "\"_id\" INTEGER PRIMARY KEY NOT NULL ," + // 0: id
                "\"IMEI\" TEXT," + // 1: imei
                "\"PHONE_BOOK_ID\" INTEGER NOT NULL ," + // 2: phone_book_id
                "\"WHITE_LIST\" TEXT);"); // 3: white_list
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"PHONE_BOOK\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, PhoneBook entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        String imei = entity.getImei();
        if (imei != null) {
            stmt.bindString(2, imei);
        }
        stmt.bindLong(3, entity.getPhone_book_id());
 
        List white_list = entity.getWhite_list();
        if (white_list != null) {
            stmt.bindString(4, white_listConverter.convertToDatabaseValue(white_list));
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, PhoneBook entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        String imei = entity.getImei();
        if (imei != null) {
            stmt.bindString(2, imei);
        }
        stmt.bindLong(3, entity.getPhone_book_id());
 
        List white_list = entity.getWhite_list();
        if (white_list != null) {
            stmt.bindString(4, white_listConverter.convertToDatabaseValue(white_list));
        }
    }

    @Override
    protected final void attachEntity(PhoneBook entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public PhoneBook readEntity(Cursor cursor, int offset) {
        PhoneBook entity = new PhoneBook( //
            cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // imei
            cursor.getLong(offset + 2), // phone_book_id
            cursor.isNull(offset + 3) ? null : white_listConverter.convertToEntityProperty(cursor.getString(offset + 3)) // white_list
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, PhoneBook entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setImei(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setPhone_book_id(cursor.getLong(offset + 2));
        entity.setWhite_list(cursor.isNull(offset + 3) ? null : white_listConverter.convertToEntityProperty(cursor.getString(offset + 3)));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(PhoneBook entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(PhoneBook entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(PhoneBook entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}

package com.fise.xiaoyu.DB.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.fise.xiaoyu.DB.entity.DeviceCrontab;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;


/**
 * 上课/爱心等任务表
 * 
 */

public class TaskDao extends AbstractDao<DeviceCrontab, Long> {

	public static final String TABLENAME = "TaskInfo";
 

	public static class Properties {
		public final static Property Id = new Property(0, Long.class, "id",
				true, "_id");
		public final static Property task_id = new Property(1, int.class,
				"task_id", false, "TASK_ID");
		public final static Property device_id = new Property(2, int.class,
				"device_id", false, "DEVICE_ID");
		public final static Property task_type = new Property(3, int.class,
				"task_type", false, "TASK_TYPE");
		public final static Property task_name = new Property(4, String.class,
				"task_name", false, "TASK_NAME");
		public final static Property task_param = new Property(5, String.class,
				"task_param", false, "TASK_PARAM");
		public final static Property begin_time = new Property(6, String.class,
				"begin_time", false, "BEGIN_TIME");
		public final static Property end_time = new Property(7, String.class,
				"end_time", false, "END_TIME");
		public final static Property status = new Property(8, int.class,
				"status", false, "STATUS");
		public final static Property repeat_mode = new Property(9, int.class,
				"repeat_mode", false, "REPEEAT_MODE");
		public final static Property repeat_value = new Property(10,
				String.class, "repeat_value", false, "REPEEAT_VALUE");

	};

	public TaskDao(DaoConfig config) {
		super(config);
	}

	public TaskDao(DaoConfig config, DaoSession daoSession) {
		super(config, daoSession);
	}

	/** Creates the underlying database table. */
	public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
		String constraint = ifNotExists ? "IF NOT EXISTS " : "";
		db.execSQL("CREATE TABLE " + constraint + "'TaskInfo' (" + //
				"'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
				"'TASK_ID' INTEGER NOT NULL UNIQUE ," + // 1: task_id
				"'DEVICE_ID' INTEGER NOT NULL ," + // 2: device_id
				"'TASK_TYPE' INTEGER NOT NULL ," + // 3: task_type
				"'TASK_NAME' TEXT NOT NULL ," + // 4: task_name
				"'TASK_PARAM' TEXT NOT NULL ," + // 5: task_param
				"'BEGIN_TIME' TEXT NOT NULL ," + // 6: begin_time
				"'END_TIME' TEXT NOT NULL ," + // 7: end_time
				"'STATUS' INTEGER NOT NULL ," + // 8: status
				"'REPEEAT_MODE' INTEGER NOT NULL ," + // 9: repeat_mode
				"'REPEEAT_VALUE' TEXT NOT NULL );"); // 10: repeat_value

		// Add Indexes
		db.execSQL("CREATE INDEX " + constraint
				+ "IDX_TaskInfo_PEER_ID ON TaskInfo" + " (TASK_ID);");
	}

	/** Drops the underlying database table. */
	public static void dropTable(SQLiteDatabase db, boolean ifExists) {
		String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "")
				+ "'TaskInfo'";
		db.execSQL(sql);
	}

	/** @inheritdoc */
	@Override
	protected void bindValues(SQLiteStatement stmt, DeviceCrontab entity) {
		stmt.clearBindings();

		Long id = entity.getId();
		if (id != null) {
			stmt.bindLong(1, id);
		}
		stmt.bindLong(2, entity.getTaskId());
		stmt.bindLong(3, entity.getDeviceId());
		stmt.bindLong(4, entity.getTaskType());
		stmt.bindString(5, entity.getTaskName());
		stmt.bindString(6, entity.getTaskParam());
		stmt.bindString(7, entity.getBeginTime());
		stmt.bindString(8, entity.getEndTime());
		stmt.bindLong(9, entity.getStatus());
		stmt.bindLong(10, entity.getRepeatMode());
		stmt.bindString(11, entity.getRepeatValue());
		
	}

	/** @inheritdoc */
	@Override
	public Long readKey(Cursor cursor, int offset) {
		return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
	}

	/** @inheritdoc */
	@Override
	public DeviceCrontab readEntity(Cursor cursor, int offset) {
		DeviceCrontab entity = new DeviceCrontab( //
				cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
				cursor.getInt(offset + 1), // TaskId
				cursor.getInt(offset + 2), // DeviceId
				cursor.getInt(offset + 3), // TaskType
				cursor.getString(offset + 4), // TaskName
				cursor.getString(offset + 5), // TaskParam
				cursor.getString(offset + 6), // BeginTime
				cursor.getString(offset + 7), // EndTime
				cursor.getInt(offset + 8), // Status
				cursor.getInt(offset + 9), // RepeatMode
				cursor.getString(offset + 10) // RepeatValue
		);

		return entity;
	}

	/** @inheritdoc */
	@Override
	public void readEntity(Cursor cursor, DeviceCrontab entity, int offset) {
		entity.setId(cursor.isNull(offset + 0) ? null : cursor
				.getLong(offset + 0));
		entity.setTaskId(cursor.getInt(offset + 1));
		entity.setDeviceId(cursor.getInt(offset + 2));
		entity.setTaskType(cursor.getInt(offset + 3));
		entity.setTaskName(cursor.getString(offset + 4));
		entity.setTaskParam(cursor.getString(offset + 5));
		entity.setBeginTime(cursor.getString(offset + 6));
		entity.setEndTime(cursor.getString(offset + 7));
		entity.setStatus(cursor.getInt(offset + 8));
		entity.setRepeatMode(cursor.getInt(offset + 9));
		entity.setRepeatValue(cursor.getString(offset + 10));

	}

	/** @inheritdoc */
	@Override
	protected Long updateKeyAfterInsert(DeviceCrontab entity, long rowId) {
		entity.setId(rowId);
		return rowId;
	}

	/** @inheritdoc */
	@Override
	public Long getKey(DeviceCrontab entity) {
		if (entity != null) {
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

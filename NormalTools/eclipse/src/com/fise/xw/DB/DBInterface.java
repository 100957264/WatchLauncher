package com.fise.xw.DB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fise.xw.DB.dao.AlarmDao;
import com.fise.xw.DB.dao.AuthUserDao;
import com.fise.xw.DB.dao.BlackListDao;
import com.fise.xw.DB.dao.DaoMaster;
import com.fise.xw.DB.dao.DaoSession;
import com.fise.xw.DB.dao.DeviceConfigureDao;
import com.fise.xw.DB.dao.DeviceDao; 
import com.fise.xw.DB.dao.DeviceTrajectoryDao;
import com.fise.xw.DB.dao.ElectricFenceDao;
import com.fise.xw.DB.dao.FamilyConcernDao;
import com.fise.xw.DB.dao.FriendsDao;
import com.fise.xw.DB.dao.GroupDao;
import com.fise.xw.DB.dao.GroupNickDao;
import com.fise.xw.DB.dao.GroupVersionDao;
import com.fise.xw.DB.dao.GroupWeiDao;
import com.fise.xw.DB.dao.MessageDao;
import com.fise.xw.DB.dao.RelationsUserDao;
import com.fise.xw.DB.dao.ReqFriendsDao;
import com.fise.xw.DB.dao.ReqMessageDao;
import com.fise.xw.DB.dao.SessionDao;
import com.fise.xw.DB.dao.SystemConfigDao;
import com.fise.xw.DB.dao.TaskDao;
import com.fise.xw.DB.dao.UserDao;
import com.fise.xw.DB.dao.UserFriendsDao;
import com.fise.xw.DB.dao.WeiDao;
import com.fise.xw.DB.dao.WeiFriendsDao;
import com.fise.xw.DB.dao.WhiteListDao;
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.DeviceCrontab;
import com.fise.xw.DB.entity.DeviceTrajectory;
import com.fise.xw.DB.entity.ElectricFenceEntity;
import com.fise.xw.DB.entity.FamilyConcernEntity;
import com.fise.xw.DB.entity.GroupEntity;
import com.fise.xw.DB.entity.GroupNickEntity;
import com.fise.xw.DB.entity.GroupVersion;
import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.DB.entity.ReqFriendsEntity;
import com.fise.xw.DB.entity.SessionEntity;
import com.fise.xw.DB.entity.SystemConfigEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.entity.WeiEntity;
import com.fise.xw.DB.entity.WhiteEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.MessageConstant;
import com.fise.xw.imservice.entity.AddFriendsMessage;
import com.fise.xw.imservice.entity.AudioMessage;
import com.fise.xw.imservice.entity.CardMessage;
import com.fise.xw.imservice.entity.DevMessage;
import com.fise.xw.imservice.entity.ImageMessage;
import com.fise.xw.imservice.entity.MixMessage;
import com.fise.xw.imservice.entity.NoticeMessage;
import com.fise.xw.imservice.entity.PostionMessage;
import com.fise.xw.imservice.entity.TextMessage;
import com.fise.xw.imservice.entity.VedioMessage;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.utils.Logger;

import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * @author : yingmu on 15-1-5.
 * @email : yingmu@mogujie.com.
 * 
 *        有两个静态标识可开启QueryBuilder的SQL和参数的日志输出： QueryBuilder.LOG_SQL = true;
 *        QueryBuilder.LOG_VALUES = true;
 */
public class DBInterface {
	private Logger logger = Logger.getLogger(DBInterface.class);
	private static DBInterface dbInterface = null;
	private DaoMaster.DevOpenHelper openHelper;
	private Context context = null;
	private int loginUserId = 0;

	public static DBInterface instance() {
		if (dbInterface == null) {
			synchronized (DBInterface.class) {
				if (dbInterface == null) {
					dbInterface = new DBInterface();
				}
			}
		}
		return dbInterface;
	}

	private DBInterface() {
	}

	/**
	 * 上下文环境的更新 1. 环境变量的clear check
	 */
	public void close() {
		if (openHelper != null) {
			openHelper.close();
			openHelper = null;
			context = null;
			loginUserId = 0;
		}
	}

	public void initDbHelp(Context ctx, int loginId) {
		if (ctx == null || loginId <= 0) {
			throw new RuntimeException("#DBInterface# init DB exception!");
		}
		// 临时处理，为了解决离线登录db实例初始化的过程
		if (context != ctx || loginUserId != loginId) {
			context = ctx;
			loginUserId = loginId;
			close();
			logger.i("DB init,loginId:%d", loginId);
			String DBName = "tt_" + loginId + ".db";
			DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(ctx,
					DBName, null);
			this.openHelper = helper;
		}
	}

	/**
	 * Query for readable DB
	 */
	private DaoSession openReadableDb() {
		isInitOk();
		SQLiteDatabase db = openHelper.getReadableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		DaoSession daoSession = daoMaster.newSession();
		return daoSession;
	}

	/**
	 * Query for writable DB
	 */
	private DaoSession openWritableDb() {
		isInitOk();
		SQLiteDatabase db = openHelper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		DaoSession daoSession = daoMaster.newSession();
		return daoSession;
	}

	private void isInitOk() {
		if (openHelper == null) {
			logger.e("DBInterface#isInit not success or start,cause by openHelper is null");
			// 抛出异常 todo
			throw new RuntimeException(
					"DBInterface#isInit not success or start,cause by openHelper is null");
		}
	}

	// /**-------------------------下面开始ElectricFence
	// 操作相关---------------------------------------*/

	public List<ElectricFenceEntity> loadAllElectricFence() {
		ElectricFenceDao dao = openReadableDb().getElectricFenceDao();
		List<ElectricFenceEntity> result = dao.loadAll();
		return result;
	}

	public void deleteElectricFence() {
		ElectricFenceDao dao = openReadableDb().getElectricFenceDao();
		dao.deleteAll();
	}

	public void insertOrUpdateElectricFence(ElectricFenceEntity entity) {
		ElectricFenceDao dao = openReadableDb().getElectricFenceDao();
		long rowId = dao.insertOrReplace(entity);
	}

	public void deleteElectricFence(ElectricFenceEntity entity) {
		ElectricFenceDao dao = openReadableDb().getElectricFenceDao();
		dao.delete(entity);
	}

	public void deleteElectricFenceAll() {
		ElectricFenceDao dao = openReadableDb().getElectricFenceDao();
		dao.deleteAll();
	}

	/*
	 * start 设备信息数据表
	 */
	public List<DeviceEntity> loadAllCard() {
		DeviceConfigureDao dao = openReadableDb().getDeviceCardDao();
		List<DeviceEntity> result = dao.loadAll();
		return result;
	}

	public void insertOrUpdateCard(DeviceEntity entity) {
		DeviceConfigureDao dao = openReadableDb().getDeviceCardDao();
		long rowId = dao.insertOrReplace(entity);
	}

	public void deleteOrUpdateCard(DeviceEntity entity) {
		DeviceConfigureDao dao = openReadableDb().getDeviceCardDao();
		dao.delete(entity);
	}

	/*
	 * end
	 */

	public List<UserEntity> loadAllAuthUser() {
		AuthUserDao dao = openReadableDb().getAuthUserDao();
		List<UserEntity> result = dao.loadAll();
		return result;
	}

	public void insertOrUpdateAuthUser(UserEntity entity) {
		AuthUserDao reqDao = openWritableDb().getAuthUserDao();
		long rowId = reqDao.insertOrReplace(entity);
	}

	public void deleteOrUpdateAuthUser(UserEntity entity) {
		AuthUserDao reqDao = openWritableDb().getAuthUserDao();
		reqDao.delete(entity);
	}

	public void insertOrDeleteAuthUser(UserEntity entity) {
		AuthUserDao reqDao = openWritableDb().getAuthUserDao();
		reqDao.delete(entity);
	}

	public List<ReqFriendsEntity> loadAllReqUnMessage() {
		ReqMessageDao dao = openReadableDb().getReqUnMessageDao();
		List<ReqFriendsEntity> result = dao.loadAll();
		return result;
	}

	public void insertOrUpdateReqUnMessage(ReqFriendsEntity entity) {
		ReqMessageDao reqDao = openWritableDb().getReqUnMessageDao();
		long rowId = reqDao.insertOrReplace(entity);
	}

	public void insertOrDeleteReqFriends(ReqFriendsEntity entity) {
		ReqMessageDao reqDao = openWritableDb().getReqUnMessageDao();
		reqDao.delete(entity);
	}

	public void batchInsertOrUpdateReqFirends(List<ReqFriendsEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		ReqMessageDao reqDao = openWritableDb().getReqUnMessageDao();
		reqDao.insertOrReplaceInTx(entityList);
	}

	public List<UserEntity> loadAllReqFriends() {
		ReqFriendsDao dao = openReadableDb().getReqFriends();
		List<UserEntity> result = dao.loadAll();
		return result;
	}

	/**
	 * -------------------------下面开始User
	 * 操作相关---------------------------------------
	 */
	/**
	 * @return todo USER_STATUS_LEAVE
	 */
	public List<UserEntity> loadAllUsers() {
		UserDao dao = openReadableDb().getUserDao();
		List<UserEntity> result = dao.loadAll();
		return result;
	}

	public UserEntity getByUserName(String uName) {
		UserDao dao = openReadableDb().getUserDao();
		UserEntity entity = dao.queryBuilder()
				.where(UserDao.Properties.PinyinName.eq(uName)).unique();
		return entity;
	}

	public UserEntity getByLoginId(int loginId) {
		UserDao dao = openReadableDb().getUserDao();
		UserEntity entity = dao.queryBuilder()
				.where(UserDao.Properties.PeerId.eq(loginId)).unique();
		return entity;
	}

	public void insertOrUpdateUser(UserEntity entity) {
		UserDao userDao = openWritableDb().getUserDao();
		long rowId = userDao.insertOrReplace(entity);
	}

	// 加载好友
	public List<UserEntity> loadAllFriends() {
		FriendsDao dao = openReadableDb().getFriendsDao();
		List<UserEntity> result = dao.loadAll();
		return result;
	}

	public void insertOrUpdateFriends(UserEntity entity) {

		FriendsDao userDao = openWritableDb().getFriendsDao();
		long rowId = userDao.insertOrReplace(entity);

	}

	public void insertOrDeleteFriens(UserEntity entity) {

		FriendsDao friendsDao = openWritableDb().getFriendsDao();
		friendsDao.delete(entity);

	}

	public void batchInsertOrUpdateFriends(List<UserEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		FriendsDao friendsDao = openWritableDb().getFriendsDao();
		friendsDao.insertOrReplaceInTx(entityList);

	}

	// 小位好友
	public List<UserEntity> loadAllXiaoWeiFriends() {
		WeiFriendsDao dao = openReadableDb().getWeiFriendsDao();
		List<UserEntity> result = dao.loadAll();
		return result;
	}

	public void insertOrUpdateXiaoWeiFriends(UserEntity entity) {
		WeiFriendsDao userDao = openWritableDb().getWeiFriendsDao();
		long rowId = userDao.insertOrReplace(entity);
	}

	public void insertOrDeleteXiaoWeiFriens(UserEntity entity) {
		WeiFriendsDao friendsDao = openWritableDb().getWeiFriendsDao();
		friendsDao.delete(entity);
	}

	public void batchInsertOrUpdateXiaoWeiFriends(List<UserEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		WeiFriendsDao friendsDao = openWritableDb().getWeiFriendsDao();
		friendsDao.insertOrReplaceInTx(entityList);

	}

	/*
	 * 黑名单 Start
	 */
	public List<UserEntity> loadAllBlackList() {
		BlackListDao dao = openReadableDb().getBlackListDao();
		List<UserEntity> result = dao.loadAll();
		return result;
	}

	public void insertOrUpdateBlackList(UserEntity entity) {
		BlackListDao userDao = openWritableDb().getBlackListDao();
		long rowId = userDao.insertOrReplace(entity);
	}

	public void insertOrDeleteBlackList(UserEntity entity) {
		BlackListDao blackListDao = openWritableDb().getBlackListDao();
		blackListDao.delete(entity);
	}

	public void batchInsertOrUpdateBlackList(List<UserEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		BlackListDao blackListDao = openWritableDb().getBlackListDao();
		blackListDao.insertOrReplaceInTx(entityList);

	}

	/*
	 * 黑名单 end
	 */

	// 设备行为历史轨迹
	public List<DeviceTrajectory> loadAllTrajectory() {
		DeviceTrajectoryDao deviceDao = openReadableDb().getTrajectoryDao();
		List<DeviceTrajectory> result = deviceDao.loadAll();
		return result;
	}

	public void insertOrUpdateTrajectory(DeviceTrajectory entity) {
		DeviceTrajectoryDao deviceDao = openWritableDb().getTrajectoryDao();
		long rowId = deviceDao.insertOrReplace(entity);
	}

	public void insertOrDeleteTrajectory(DeviceTrajectory entity) {
		DeviceTrajectoryDao deviceDao = openWritableDb().getTrajectoryDao();
		deviceDao.delete(entity);
	}

	public void batchInsertOrUpdateTrajectoryList(
			List<DeviceTrajectory> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		DeviceTrajectoryDao deviceDao = openWritableDb().getTrajectoryDao();
		deviceDao.insertOrReplaceInTx(entityList);

	}

	/*
	 * 设备行为历史轨迹 end
	 */

	public List<UserEntity> loadAllDevice() {
		DeviceDao deviceDao = openReadableDb().getDeviceDao();
		List<UserEntity> result = deviceDao.loadAll();

		// 排序
		Comparator<UserEntity> comparator = new Comparator<UserEntity>() {
			public int compare(UserEntity s1, UserEntity s2) {
				// 以ID排序
				return s1.getPeerId() - s2.getPeerId();
			}
		};

		Collections.sort(result, comparator);

		List<UserEntity> authList = new ArrayList<>();
		for (int i = 0; i < result.size(); i++) {
			if (result.get(i).getIsFriend() == DBConstant.FRIENDS_TYPE_WEI) {
				authList.add(result.get(i));
			}
		}
		return authList;
	}

	public void insertOrDeleteDevice(UserEntity entity) {
		DeviceDao deviceDao = openReadableDb().getDeviceDao();
		deviceDao.delete(entity);
	}

	/*
	 * 位友请求信息 start
	 */
	public List<WeiEntity> loadAllWei() {
		WeiDao dao = openReadableDb().getWeiDao();
		List<WeiEntity> result = dao.loadAll();
		return result;
	}

	public void insertOrUpdateWeiFriens(WeiEntity entity) {
		WeiDao weiDao = openWritableDb().getWeiDao();
		long rowId = weiDao.insertOrReplace(entity);
	}

	public void insertOrDeleteWeiFriens(WeiEntity entity) {
		WeiDao weiDao = openWritableDb().getWeiDao();
		weiDao.delete(entity);
	}

	public void batchInsertOrUpdateWei(List<WeiEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		WeiDao weiDao = openWritableDb().getWeiDao();
		weiDao.insertOrReplaceInTx(entityList);

	}

	/*
	 * 位友请求信息 end
	 */

	/*
	 * 好友请求友请求信息 start
	 */
	public List<WeiEntity> loadAllUserReq() {
		UserFriendsDao dao = openReadableDb().getUserFriends();
		List<WeiEntity> result = dao.loadAll();
		return result;
	}

	public void insertOrUpdateUserReqFriens(WeiEntity entity) {
		UserFriendsDao weiDao = openWritableDb().getUserFriends();
		long rowId = weiDao.insertOrReplace(entity);
	}

	public void insertOrDeleteUserFriends(WeiEntity entity) {
		UserFriendsDao weiDao = openWritableDb().getUserFriends();
		weiDao.delete(entity);
	}

	public void batchInsertOrUpdateUserFriends(List<WeiEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		UserFriendsDao weiDao = openWritableDb().getUserFriends();
		weiDao.insertOrReplaceInTx(entityList);

	}

	// 加载亲切关注的身份
	public List<FamilyConcernEntity> loadAllFamilyConcern() {
		FamilyConcernDao dao = openReadableDb().getFamilyConcernDao();
		List<FamilyConcernEntity> result = dao.loadAll();
		return result;
	}

	// 插入亲切关注的身份
	public void insertOrUpdatFamilyConcern(FamilyConcernEntity entity) {
		FamilyConcernDao userDao = openWritableDb().getFamilyConcernDao();
		long rowId = userDao.insertOrReplace(entity);
	}

	// 扫除亲切关注的身份
	public void deleteOrUpdateFamilyConcern(FamilyConcernEntity entity) {
		FamilyConcernDao userDao = openWritableDb().getFamilyConcernDao();
		userDao.delete(entity);
	}

	// 扫除亲切关注的身份
	public void removeAllFamily() {
		FamilyConcernDao userDao = openWritableDb().getFamilyConcernDao();
		userDao.deleteAll();
	}

	/*
	 * 好友请求信息 end
	 */

	/*
	 * 获取系统配置
	 */
	public List<SystemConfigEntity> loadAllSystemConfig() {
		SystemConfigDao dao = openReadableDb().getSystemConfigDao();
		List<SystemConfigEntity> result = dao.loadAll();
		return result;
	}

	//
	public void insertOrUpdateSystemConfig(SystemConfigEntity entity) {
		SystemConfigDao userDao = openWritableDb().getSystemConfigDao();
		long rowId = userDao.insertOrReplace(entity);
	}

	//
	public void deleteOrUpdateSystemConfig(SystemConfigEntity entity) {
		SystemConfigDao userDao = openWritableDb().getSystemConfigDao();
		userDao.delete(entity);
	}

	// end

	// 加载白名单好
	public List<WhiteEntity> loadAllWhiteList() {
		WhiteListDao dao = openReadableDb().getWhiteListDao();
		List<WhiteEntity> result = dao.loadAll();
		return result;
	}

	// 插入白名单
	public void insertOrUpdateWhiteList(WhiteEntity entity) {
		WhiteListDao userDao = openWritableDb().getWhiteListDao();
		long rowId = userDao.insertOrReplace(entity);
	}

	public void deleteOrUpdateWhiteAll() {
		WhiteListDao userDao = openWritableDb().getWhiteListDao();
		userDao.deleteAll();
	}

	// 扫除白名单
	public void deleteOrUpdateWhiteList(WhiteEntity entity) {
		WhiteListDao userDao = openWritableDb().getWhiteListDao();
		userDao.delete(entity);
	}

	// 插入多个白名单
	public void batchInsertOrUpdateWhiteList(List<WhiteEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		WhiteListDao userDao = openWritableDb().getWhiteListDao();
		userDao.insertOrReplaceInTx(entityList);

	}

	// 加载紧急号码
	public List<WhiteEntity> loadAllAlarmList() {
		AlarmDao dao = openReadableDb().getAlarmDao();
		List<WhiteEntity> result = dao.loadAll();
		return result;
	}

	// 插入紧急号码
	public void insertOrUpdateAlarmList(WhiteEntity entity) {
		AlarmDao userDao = openWritableDb().getAlarmDao();
		long rowId = userDao.insertOrReplace(entity);
	}

	// 扫除插入紧急号码
	public void deleteOrUpdateAlarmList(WhiteEntity entity) {
		AlarmDao userDao = openWritableDb().getAlarmDao();
		userDao.delete(entity);
	}

	// 扫除插入紧急号码
	public void deleteOrUpdateAlarmAll() {
		AlarmDao userDao = openWritableDb().getAlarmDao();
		userDao.deleteAll();
	}

	// 插入多个紧急号码
	public void batchInsertOrUpdateAlarmList(List<WhiteEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		AlarmDao userDao = openWritableDb().getAlarmDao();
		userDao.insertOrReplaceInTx(entityList);

	}

	// 加载设备所有的任务提醒
	public List<DeviceCrontab> loadAllDevTask() {
		TaskDao dao = openReadableDb().getTaskDao();
		List<DeviceCrontab> result = dao.loadAll();
		return result;
	}

	// 插入设备的任务提醒
	public void insertOrUpdateDevTask(DeviceCrontab entity) {
		TaskDao dao = openWritableDb().getTaskDao();
		long rowId = dao.insertOrReplace(entity);
	}

	// 扫除设备的任务提醒
	public void deleteOrUpdateDevTask(DeviceCrontab entity) {
		TaskDao dao = openWritableDb().getTaskDao();
		dao.delete(entity);
	}

	// 扫除设备全部的任务提醒
	public void deleteOrUpdateDevTask() {
		TaskDao dao = openWritableDb().getTaskDao();
		dao.deleteAll();
	}

	// 插入多个设备全部的任务提醒
	public void batchInsertOrUpdateDevTaskList(List<DeviceCrontab> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		TaskDao dao = openWritableDb().getTaskDao();
		dao.insertOrReplaceInTx(entityList);

	}

	public void insertOrUpdateReqFriens(UserEntity entity) {
		ReqFriendsDao reqDao = openWritableDb().getReqFriends();
		long rowId = reqDao.insertOrReplace(entity);
	}

	public void insertOrDeleteReqFriens(UserEntity entity) {
		ReqFriendsDao reqDao = openWritableDb().getReqFriends();
		reqDao.delete(entity);
		// long rowId = reqDao.delete(entity);
	}

	public void batchDeleteUser(UserEntity entity) {

		UserDao userDao = openWritableDb().getUserDao();
		userDao.delete(entity);

	}

	public void batchInsertOrUpdateUser(List<UserEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		UserDao userDao = openWritableDb().getUserDao();
		userDao.insertOrReplaceInTx(entityList);

	}

	public void batchInsertOrUpdateReq(List<UserEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		ReqFriendsDao reqDao = openWritableDb().getReqFriends();
		reqDao.insertOrReplaceInTx(entityList);
	}

	public void batchInsertOrUpdateRelations(List<UserEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}

		RelationsUserDao reqDao = openWritableDb().getRelationsDao();
		reqDao.insertOrReplaceInTx(entityList);
	}

	// 跟我有关系的人
	public List<UserEntity> loadAllRelationsList() {
		RelationsUserDao dao = openReadableDb().getRelationsDao();
		List<UserEntity> result = dao.loadAll();
		return result;
	}

	public void insertOrUpdateDevice(UserEntity user) {

		DeviceDao deviceDao = openWritableDb().getDeviceDao();
		deviceDao.insertOrReplace(user);
	}

	public void batchInsertOrUpdateDevice(List<UserEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}

		DeviceDao deviceDao = openWritableDb().getDeviceDao();
		deviceDao.insertOrReplaceInTx(entityList);

	}

	/** update */
	public int getReqFriendsLastTime() {
		WeiDao sessionDao = openReadableDb().getWeiDao();
		WeiEntity entity = sessionDao.queryBuilder()
				.orderDesc(WeiDao.Properties.updated).limit(1).unique();
		if (entity == null) {
			return 0;
		} else {
			return entity.getUpdated();
		}
	}

	/** update */
	public int getUserInfoLastTime() {
		UserDao sessionDao = openReadableDb().getUserDao();
		UserEntity userEntity = sessionDao.queryBuilder()
				.orderDesc(UserDao.Properties.Updated).limit(1).unique();
		if (userEntity == null) {
			return 0;
		} else {
			return userEntity.getUpdated();
		}
	}

	/**
	 * -------------------------下面开始Group
	 * 操作相关---------------------------------------
	 */
	/**
	 * 载入Group的所有数据
	 * 
	 * @return
	 */
	public List<GroupEntity> loadAllGroup() {
		GroupDao dao = openReadableDb().getGroupDao();
		List<GroupEntity> result = dao.loadAll();
		return result;
	}

	public long insertOrUpdateGroup(GroupEntity groupEntity) {
		GroupDao dao = openWritableDb().getGroupDao();
		long pkId = dao.insertOrReplace(groupEntity);
		return pkId;
	}

	public void deleteUpdateGroup(GroupEntity groupEntity) {
		GroupDao dao = openWritableDb().getGroupDao();
		dao.delete(groupEntity); 
	}

	public void batchInsertOrUpdateGroup(List<GroupEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		GroupDao dao = openWritableDb().getGroupDao();
		dao.insertOrReplaceInTx(entityList);
	}

	/*
	 * 获取群成员昵称 start
	 */

	public List<GroupNickEntity> loadAllGroupNick() {
		GroupNickDao dao = openReadableDb().getGroupNickDao();
		List<GroupNickEntity> result = dao.loadAll();
		return result;
	}

	public long insertOrUpdateGroupNick(GroupNickEntity groupEntity) {
		GroupNickDao dao = openWritableDb().getGroupNickDao();
		long pkId = dao.insertOrReplace(groupEntity);
		return pkId;
	}

	public void batchInsertOrUpdateGroupNick(List<GroupNickEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		GroupNickDao dao = openWritableDb().getGroupNickDao();
		dao.insertOrReplaceInTx(entityList);
	}

	/*
	 * 获取群成员昵称 end
	 */

	/**
	 * -------------------------下面开始Group
	 * 操作相关---------------------------------------
	 */
	/**
	 * 载入Group的所有数据
	 * 
	 * @return
	 */
	public List<GroupEntity> loadAllWeiGroup() {
		GroupWeiDao dao = openReadableDb().getGroupWeiDao();
		List<GroupEntity> result = dao.loadAll();
		return result;
	}

	public long insertOrUpdateWeiGroup(GroupEntity groupEntity) {
		GroupWeiDao dao = openWritableDb().getGroupWeiDao();
		long pkId = dao.insertOrReplace(groupEntity);
		return pkId;
	}

	public void deletUpdateWeiGroup(GroupEntity groupEntity) {
		GroupWeiDao dao = openWritableDb().getGroupWeiDao();
		dao.delete(groupEntity);

	}

	public void batchInsertOrUpdateGroupWei(List<GroupEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		GroupWeiDao dao = openWritableDb().getGroupWeiDao();
		dao.insertOrReplaceInTx(entityList);
	}

	/**
	 * 载入Group的所有数据
	 * 
	 * @return
	 */
	public List<GroupVersion> loadAllGroupVersion() {
		GroupVersionDao dao = openReadableDb().getGroupVersionDao();
		List<GroupVersion> result = dao.loadAll();
		return result;
	}

	public long insertOrUpdateGroupVersion(GroupVersion groupVersion) {
		GroupVersionDao dao = openWritableDb().getGroupVersionDao();
		long pkId = dao.insertOrReplace(groupVersion);
		return pkId;
	}

	public void batchInsertOrUpdateGroupVersion(List<GroupVersion> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		GroupVersionDao dao = openWritableDb().getGroupVersionDao();
		dao.insertOrReplaceInTx(entityList);
	}

	/**
	 * -------------------------下面开始session
	 * 操作相关---------------------------------------
	 */
	/**
	 * 载入session 表中的所有数据
	 * 
	 * @return
	 */
	public List<SessionEntity> loadAllSession() {
		SessionDao dao = openReadableDb().getSessionDao();
		List<SessionEntity> result = dao.queryBuilder()
				.orderDesc(SessionDao.Properties.Updated).list();
		return result;
	}

	public void removeSession() {
		SessionDao dao = openReadableDb().getSessionDao();
		dao.deleteAll();
	}

	public long insertOrUpdateSession(SessionEntity sessionEntity) {
		SessionDao dao = openWritableDb().getSessionDao();
		long pkId = dao.insertOrReplace(sessionEntity);
		return pkId;
	}

	public void batchInsertOrUpdateSession(List<SessionEntity> entityList) {
		if (entityList.size() <= 0) {
			return;
		}
		SessionDao dao = openWritableDb().getSessionDao();
		dao.insertOrReplaceInTx(entityList);
	}

	public void deleteSession(String sessionKey) {
		SessionDao sessionDao = openWritableDb().getSessionDao();
		DeleteQuery<SessionEntity> bd = sessionDao.queryBuilder()
				.where(SessionDao.Properties.SessionKey.eq(sessionKey))
				.buildDelete();

		bd.executeDeleteWithoutDetachingEntities();
	}

	/**
	 * 获取最后回话的时间，便于获取联系人列表变化 问题: 本地消息发送失败，依旧会更新session的时间 [存在会话、不存在的会话]
	 * 本质上还是最后一条成功消息的时间
	 * 
	 * @return
	 */
	public int getSessionLastTime() {
		int timeLine = 0;
		MessageDao messageDao = openReadableDb().getMessageDao();
		String successType = String.valueOf(MessageConstant.MSG_SUCCESS);
		String sql = "select created from Message where status=? order by created desc limit 1";
		Cursor cursor = messageDao.getDatabase().rawQuery(sql,
				new String[] { successType });
		try {
			if (cursor != null && cursor.getCount() == 1) {
				cursor.moveToFirst();
				timeLine = cursor.getInt(0);
			}
		} catch (Exception e) {
			logger.e("DBInterface#getSessionLastTime cursor 查询异常");
		} finally {
			cursor.close();
		}
		return timeLine;
	}

	/**
	 * -------------------------下面开始message
	 * 操作相关---------------------------------------
	 */

	// where (msgId >= startMsgId and msgId<=lastMsgId) or
	// (msgId=0 and status = 0)
	// order by created desc
	// limit count;
	// 按照时间排序
	public List<MessageEntity> getHistoryMsg(String chatKey, int lastMsgId,
			int lastCreateTime, int count) {
		/** 解决消息重复的问题 */
		int preMsgId = lastMsgId + 1;
		MessageDao dao = openReadableDb().getMessageDao();
		List<MessageEntity> listMsg = dao
				.queryBuilder()
				.where(MessageDao.Properties.Created.le(lastCreateTime),
						MessageDao.Properties.SessionKey.eq(chatKey),
						MessageDao.Properties.MsgId.notEq(preMsgId),
						MessageDao.Properties.isDelete.notEq(1))
				.whereOr(MessageDao.Properties.MsgId.le(lastMsgId),
						MessageDao.Properties.MsgId.gt(90000000))
				.orderDesc(MessageDao.Properties.MsgId)
				.orderDesc(MessageDao.Properties.Created).limit(count).list();

		int loginId = IMLoginManager.instance().getLoginId();
		List<MessageEntity> NewlistMsg = new ArrayList<MessageEntity>();
		for (int i = 0; i < listMsg.size(); i++) {
			if ((listMsg.get(i).getMsgType() == DBConstant.MSG_TYPE_SINGLE_AUTH_IMAGE
					|| listMsg.get(i).getMsgType() == DBConstant.MSG_TYPE_GROUP_AUTH_IMAGE
					|| listMsg.get(i).getMsgType() == DBConstant.MSG_TYPE_SINGLE_AUTH_SOUND || listMsg
					.get(i).getMsgType() == DBConstant.MSG_TYPE_GROUP_AUTH_SOUND)
					&& listMsg.get(i).getFromId() == loginId) {

			} else {
				NewlistMsg.add(listMsg.get(i));
			}
		}
		// List<MessageEntity> listMsg =
		// dao.queryBuilder().where(MessageDao.Properties.Created.le(lastCreateTime)
		// ,MessageDao.Properties.SessionKey.eq(chatKey)
		// ,MessageDao.Properties.MsgId.notEq(preMsgId) )
		// .whereOr(MessageDao.Properties.MsgId.le(lastMsgId),
		// MessageDao.Properties.MsgId.gt(90000000))
		// .orderDesc(MessageDao.Properties.Created)
		// .orderDesc(MessageDao.Properties.MsgId)
		// .limit(count)
		// .list();

		// List<MessageEntity> listMsg =
		// dao.queryBuilder().where(MessageDao.Properties.Created.le(lastCreateTime)
		// ,MessageDao.Properties.SessionKey.eq(chatKey)
		// ,MessageDao.Properties.MsgId.notEq(preMsgId)
		// ,MessageDao.Properties.isDelete.notEq(1))
		// // .whereOr(MessageDao.Properties.MsgId.le(lastMsgId),
		// // MessageDao.Properties.MsgId.gt(90000000))
		//
		// .orderDesc(MessageDao.Properties.MsgId)
		// .orderDesc(MessageDao.Properties.Created)
		// .limit(count)
		// .list();

		return formatMessage(NewlistMsg);
		// return formatMessage(listMsg);
	}

	public void deleteHistoryMsg(String chatKey) {
		/** 解决消息重复的问题 */
		MessageDao dao = openReadableDb().getMessageDao();
		List<MessageEntity> entityList = dao.queryBuilder()
				.where(MessageDao.Properties.SessionKey.eq(chatKey)).list();//
		// .buildDelete();
		for (int i = 0; i < entityList.size(); i++) {
			entityList.get(i).setDelete(1);
		}
		batchInsertOrUpdateMessage(entityList);
		batchDeleteOrUpdateMessage(entityList);
	}

	public void deleteHistoryMsg() {

		MessageDao dao = openReadableDb().getMessageDao();
		List<MessageEntity> entityList = dao.loadAll();
		for (int i = 0; i < entityList.size(); i++) {
			entityList.get(i).setDelete(1);
		}
		batchInsertOrUpdateMessage(entityList);
		batchDeleteOrUpdateMessage(entityList);
		// dao.deleteAll();

	}

	public List<MessageEntity> loadHistoryMsg(String chatKey) {
		/** 解决消息重复的问题 */
		MessageDao dao = openReadableDb().getMessageDao();
		List<MessageEntity> entityList = dao.queryBuilder()
				.where(MessageDao.Properties.SessionKey.eq(chatKey)).list();//
		// .buildDelete();
		return entityList;
	}

	/**
	 * IMGetLatestMsgIdReq 后去最后一条合法的msgid
	 * */
	public List<Integer> refreshHistoryMsgId(String chatKey, int beginMsgId,
			int lastMsgId) {
		MessageDao dao = openReadableDb().getMessageDao();

		String sql = "select MSG_ID from Message where SESSION_KEY = ? and MSG_ID >= ? and MSG_ID <= ? order by MSG_ID asc";
		Cursor cursor = dao.getDatabase().rawQuery(
				sql,
				new String[] { chatKey, String.valueOf(beginMsgId),
						String.valueOf(lastMsgId) });

		List<Integer> msgIdList = new ArrayList<>();
		try {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				int msgId = cursor.getInt(0);
				msgIdList.add(msgId);
			}
		} finally {
			cursor.close();
		}
		return msgIdList;
	}

	public long insertOrUpdateMix(MessageEntity message) {
		MessageDao dao = openWritableDb().getMessageDao();
		MessageEntity parent = dao
				.queryBuilder()
				.where(MessageDao.Properties.MsgId.eq(message.getMsgId()),
						MessageDao.Properties.SessionKey.eq(message
								.getSessionKey())).unique();

		long resId = parent.getId();
		if (parent.getDisplayType() != DBConstant.SHOW_MIX_TEXT) {
			return resId;
		}

		boolean needUpdate = false;
		MixMessage mixParent = (MixMessage) formatMessage(parent);
		List<MessageEntity> msgList = mixParent.getMsgList();
		for (int index = 0; index < msgList.size(); index++) {
			if (msgList.get(index).getId() == message.getId()) {
				msgList.set(index, message);
				needUpdate = true;
				break;
			}
		}

		if (needUpdate) {
			mixParent.setMsgList(msgList);
			long pkId = dao.insertOrReplace(mixParent);
			return pkId;
		}
		return resId;
	}

	public List<MessageEntity> getAllMessage() {

		MessageDao dao = openReadableDb().getMessageDao();
		List<MessageEntity> entityList = dao.loadAll();
		return entityList;
	}

	/**
	 * 删除消息
	 * */
	public void deletUpdateMessage(MessageEntity message) {

		MessageDao dao = openWritableDb().getMessageDao();
		dao.delete(message);
	}

	/**
	 * 有可能是混合消息 批量接口{batchInsertOrUpdateMessage} 没有存在场景
	 * */
	public long insertOrUpdateMessage(MessageEntity message) {
		if (message.getId() != null && message.getId() < 0) {
			// mix消息
			return insertOrUpdateMix(message);
		}
		MessageDao dao = openWritableDb().getMessageDao();
		long pkId = dao.insertOrReplace(message);
		return pkId;
	}

	/**
	 * todo 这个地方调用存在特殊场景，如果list中包含Id为负Mix子类型，更新就有问题 现在的调用列表没有这个情景，使用的时候注意
	 * */
	public void batchInsertOrUpdateMessage(List<MessageEntity> entityList) {
		MessageDao dao = openWritableDb().getMessageDao();
		dao.insertOrReplaceInTx(entityList);
	}

	public void batchDeleteOrUpdateMessage(List<MessageEntity> entityList) {
		MessageDao dao = openWritableDb().getMessageDao();
		dao.deleteInTx(entityList);

	}

	public void deleteMessageById(long localId) {
		if (localId <= 0) {
			return;
		}
		Set<Long> setIds = new TreeSet<>();
		setIds.add(localId);
		batchDeleteMessageById(setIds);
	}

	public void batchDeleteMessageById(Set<Long> pkIds) {
		if (pkIds.size() <= 0) {
			return;
		}
		MessageDao dao = openWritableDb().getMessageDao();
		dao.deleteByKeyInTx(pkIds);
	}

	public void deleteMessageByMsgId(int msgId) {
		if (msgId <= 0) {
			return;
		}

		MessageDao messageDao = openWritableDb().getMessageDao();
		QueryBuilder<MessageEntity> qb = openWritableDb().getMessageDao()
				.queryBuilder();
		DeleteQuery<MessageEntity> bd = qb.where(
				MessageDao.Properties.MsgId.eq(msgId)).buildDelete();
		bd.executeDeleteWithoutDetachingEntities();
	}

	// public MessageEntity getMessageByMsgId(int messageId) {
	// MessageDao dao = openReadableDb().getMessageDao();
	// Query query = dao.queryBuilder()
	// .where(MessageDao.Properties.Id.eq(messageId)).build();
	// return formatMessage((MessageEntity) query.unique());
	// }

	public MessageEntity getMessageByMsgIdTemp(int messageId, String chatKey) {
		MessageDao dao = openReadableDb().getMessageDao();
		Query query = dao
				.queryBuilder()
				.where(MessageDao.Properties.SessionKey.eq(chatKey),
						MessageDao.Properties.MsgId.eq(messageId)).build();

		if ((MessageEntity) query.unique() == null) {
			return null;
		} else {
			return formatMessage((MessageEntity) query.unique());
		}
	}

	/**
	 * 根据主键查询 not use
	 * */
	public MessageEntity getMessageById(long localId) {
		MessageDao dao = openReadableDb().getMessageDao();
		MessageEntity messageEntity = dao.queryBuilder()
				.where(MessageDao.Properties.Id.eq(localId)).unique();
		return formatMessage(messageEntity);
	}

	private MessageEntity formatMessage(MessageEntity msg) {
		MessageEntity messageEntity = null;
		int displayType = msg.getDisplayType();
		switch (displayType) {
		case DBConstant.SHOW_MIX_TEXT:
			try {
				messageEntity = MixMessage.parseFromDB(msg);
			} catch (JSONException e) {
				logger.e(e.toString());
			}
			break;
		case DBConstant.SHOW_AUDIO_TYPE:
			messageEntity = AudioMessage.parseFromDB(msg);
			break;
		case DBConstant.SHOW_IMAGE_TYPE:
			messageEntity = ImageMessage.parseFromDB(msg);
			break;
		case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
			messageEntity = TextMessage.parseFromDB(msg);
			break;
		case DBConstant.SHOW_TYPE_ADDFRIENDS:
			messageEntity = AddFriendsMessage.parseFromDB(msg);
			break;
		case DBConstant.CHANGE_NOT_FRIEND:
			messageEntity = NoticeMessage.parseFromNoteDB(msg);
			break;
		case DBConstant.SHOW_TYPE_NOTICE_BLACK:
			messageEntity = NoticeMessage.parseFromNoteDB(msg);
			break;

		case DBConstant.SHOW_TYPE_CARD:
			messageEntity = CardMessage.parseFromDB(msg);
			break;

		case DBConstant.SHOW_TYPE_VEDIO:
			messageEntity = VedioMessage.parseFromDB(msg);
			break;
		case DBConstant.SHOW_TYPE_POSTION:
			messageEntity = PostionMessage.parseFromDB(msg);
			break;

		}
		return messageEntity;
	}

	public List<MessageEntity> formatMessage(List<MessageEntity> msgList) {
		if (msgList.size() <= 0) {
			return Collections.emptyList();
		}
		ArrayList<MessageEntity> newList = new ArrayList<>();
		for (MessageEntity info : msgList) {
			int displayType = info.getDisplayType();
			switch (displayType) {
			case DBConstant.SHOW_MIX_TEXT:
				try {
					newList.add(MixMessage.parseFromDB(info));
				} catch (JSONException e) {
					logger.e(e.toString());
				}
				break;
			case DBConstant.SHOW_AUDIO_TYPE:
				newList.add(AudioMessage.parseFromDB(info));
				break;
			case DBConstant.SHOW_IMAGE_TYPE:
				newList.add(ImageMessage.parseFromDB(info));
				break;
			case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
				newList.add(TextMessage.parseFromDB(info));
				break;
			case DBConstant.SHOW_TYPE_ADDFRIENDS:
				newList.add(AddFriendsMessage.parseFromDB(info));
				break;
			case DBConstant.CHANGE_NOT_FRIEND:
				newList.add(NoticeMessage.parseFromNoteDB(info));

				break;

			case DBConstant.SHOW_TYPE_NOTICE_BLACK:
				newList.add(NoticeMessage.parseFromNoteDB(info));

				break;

			case DBConstant.SHOW_TYPE_CARD:
				newList.add(CardMessage.parseFromDB(info));

				break;

			case DBConstant.SHOW_TYPE_VEDIO:
				newList.add(VedioMessage.parseFromDB(info));
				break;

			case DBConstant.SHOW_TYPE_POSTION:
				newList.add(PostionMessage.parseFromDB(info));
				break;

			case DBConstant.SHOW_TYPE_DEV_MESSAGE:
				newList.add(DevMessage.parseFromDB(info));
				break;

			}
		}
		return newList;
	}

}

package com.fise.xiaoyu.imservice.manager;

import com.fise.xiaoyu.DB.DBInterface;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.GroupNickEntity;
import com.fise.xiaoyu.DB.entity.GroupVersion;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.callback.Packetlistener;
import com.fise.xiaoyu.imservice.entity.NoticeMessage;
import com.fise.xiaoyu.imservice.entity.RecentInfo;
import com.fise.xiaoyu.imservice.entity.TextMessage;
import com.fise.xiaoyu.imservice.event.GroupEvent;
import com.fise.xiaoyu.imservice.event.MessageEvent;
import com.fise.xiaoyu.imservice.event.SessionEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xiaoyu.protobuf.IMBaseDefine.GroupMemberStatus;
import com.fise.xiaoyu.protobuf.IMBaseDefine.GroupType;
import com.fise.xiaoyu.protobuf.IMGroup;
import com.fise.xiaoyu.protobuf.helper.ProtoBuf2JavaBean;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.pinyin.PinYin;
import com.google.protobuf.CodedInputStream;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IMGroupManager extends IMManager {
	private Logger logger = Logger.getLogger(IMGroupManager.class);
	private static IMGroupManager inst = new IMGroupManager();

	public static IMGroupManager instance() {
		return inst;
	}

	// 依赖的服务管理
	private IMSocketManager imSocketManager = IMSocketManager.instance();
	private IMLoginManager imLoginManager = IMLoginManager.instance();
	private DBInterface dbInterface = DBInterface.instance();

	// todo Pinyin的处理
	// 正式群,临时群都会有的，存在竞争 如果不同时请求的话
	private Map<Integer, GroupEntity> groupMap = new ConcurrentHashMap<>();

	// 位友群,临时群都会有的，存在竞争 如果不同时请求的话
	private Map<Integer, GroupVersion> groupVersionMap = new ConcurrentHashMap<>();
	private Map<Integer, GroupEntity> groupQrMap = new ConcurrentHashMap<>();
	private Map<Integer, GroupNickEntity> localGroupNickMap = new ConcurrentHashMap<>();

   //设备家庭群
	private Map<Integer, GroupEntity> familyGroupMap = new ConcurrentHashMap<>();

	// 群组状态
	private boolean isGroupReady = false;

	@Override
	public void doOnStart() {
		registerEventbus(inst);
		// groupMap.clear();
	}

	public void onNormalLoginOk() {
		onLocalLoginOk();
		onLocalNetOk();
	}

	/**
	 * 1. 加载本地信息 2. 请求正规群信息 ， 与本地进行对比 3. version groupId 请求
	 * */
	public void onLocalLoginOk() {
		logger.i("group#loadFromDb");

		if (!EventBus.getDefault().isRegistered(inst)) {
			EventBus.getDefault().register(inst);
		}

		// 加载本地位友group
		List<GroupVersion> localVersionList = dbInterface.loadAllGroupVersion();
		for (GroupVersion groupInfo : localVersionList) {
			groupVersionMap.put(groupInfo.getGroupId(), groupInfo);
		}

		// 加载本地group
		List<GroupEntity> localGroupInfoList = dbInterface.loadAllGroup();
		for (GroupEntity groupInfo : localGroupInfoList) {
			groupMap.put(groupInfo.getPeerId(), groupInfo);

		}



		// 加载本地家庭group
		List<GroupEntity> localGroupFamilyInfoList = dbInterface.loadAllamilyGroup();
		for (GroupEntity groupInfo : localGroupFamilyInfoList) {
			if (groupInfo.getGroupType() == DBConstant.GROUP_TYPE_FAMILY){
				familyGroupMap.put(groupInfo.getPeerId(), groupInfo);
			}
		}



		// 加载本地位友group
		List<GroupNickEntity> groupNickList = dbInterface.loadAllGroupNick();
		for (GroupNickEntity groupInfo : groupNickList) {
			// localGroupNick = dbInterface.loadAllGroupNick();
			localGroupNickMap.put(groupInfo.getUserId(), groupInfo);
		}

		triggerEvent(new GroupEvent(GroupEvent.Event.GROUP_INFO_OK));
	}

	public void onLocalNetOk() {
		reqGetNormalGroupList();
	}

	@Override
	public void reset() {
		unregisterEventbus(inst);
		isGroupReady = false;
		groupMap.clear();
		groupQrMap.clear();
		familyGroupMap.clear();

		groupVersionMap.clear();
	}

	@Subscribe(sticky = true)
	public void onMessageEvent(SessionEvent event) {
		switch (event) {
		case RECENT_SESSION_GRPUP_LIST_UPDATE:
			// groupMap 本地已经加载完毕之后才触发
			// loadSessionGroupInfo();
			break;
		}
	}

	/**
	 * 实现自身的事件驱动
	 * 
	 * @param event
	 */
	public synchronized void triggerEvent(GroupEvent event) {
		switch (event.getEvent()) {
		case GROUP_INFO_OK:
			isGroupReady = true;
			break;
		case GROUP_INFO_UPDATED:
			isGroupReady = true;
			break;
		}
		EventBus.getDefault().postSticky(event);
	}

	/**
	 * 自身的事件驱动
	 * 
	 * @param event
	 */
	public void triggerEvent(Object event) {
		EventBus.getDefault().post(event);
	}

	public GroupNickEntity findGroupNick(int groupId, int userId) {

		GroupNickEntity entity = null;
		List<GroupNickEntity> groupNickList = new ArrayList<>(
				localGroupNickMap.values());

		for (int i = 0; i < groupNickList.size(); i++) {
			if ((groupNickList.get(i).getGroupId() == groupId)
					&& (userId == groupNickList.get(i).getUserId())) {
				entity = groupNickList.get(i);
			}
		}
		return entity;
	}

	/**
	 * 实现自身的事件驱动
	 * 
	 * @param event
	 */
	public synchronized void triggerEvent(UserInfoEvent event) {
		EventBus.getDefault().postSticky(event);
	}

	/** ---------------事件驱动end------------------------------ */


	/**
	 * 联系人页面正式群的请求 todo 正式群与临时群逻辑上的分开的，但是底层应该是想通的
	 */
	private void reqGetNormalGroupList() {
		logger.i("group#reqGetNormalGroupList");
		int loginId = imLoginManager.getLoginId();
		IMGroup.IMNormalGroupListReq normalGroupListReq = IMGroup.IMNormalGroupListReq
				.newBuilder().setUserId(loginId).build();
		int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
		int cid = IMBaseDefine.GroupCmdID.CID_GROUP_NORMAL_LIST_REQUEST_VALUE;
		imSocketManager.sendRequest(normalGroupListReq, sid, cid);
		logger.i("group#send packet to server");
	}

	/**
	 * 请求普通群的数据信息
	 * 
	 * @param normalGroupListRsp
	 */
	public void onRepNormalGroupList(
			IMGroup.IMNormalGroupListRsp normalGroupListRsp) {
		logger.i("group#onRepNormalGroupList");
		int groupSize = normalGroupListRsp.getGroupVersionListCount();
		logger.i("group#onRepNormalGroupList cnt:%d", groupSize);
		List<IMBaseDefine.GroupVersionInfo> versionInfoList = normalGroupListRsp
				.getGroupVersionListList();

		/** 对比DB中的version字段 */
		// 这块对比的可以抽离出来
		List<IMBaseDefine.GroupVersionInfo> needInfoList = new ArrayList<>();

 		for (IMBaseDefine.GroupVersionInfo groupVersionInfo : versionInfoList) {
			int groupId = groupVersionInfo.getGroupId();
			int version = groupVersionInfo.getVersion();
			GroupType type = groupVersionInfo.getType();
			GroupMemberStatus stats = groupVersionInfo.getStatus();


 			if(type == IMBaseDefine.GroupType.GROUP_TYPE_DEVICE){
				if (familyGroupMap.containsKey(groupId)
						&& familyGroupMap.get(groupId).getVersion() == version) {

					GroupEntity groupEntity = this.findFamilyGroup(groupId);
					if (groupEntity != null) {
						groupEntity.setStatus(stats.ordinal());
						groupMap.put(groupId, groupEntity);
						dbInterface.insertOrUpdateGroup(groupEntity);
					}
					continue;
				}
			}else 	if(type == IMBaseDefine.GroupType.GROUP_TYPE_NORMAL
					||type == IMBaseDefine.GroupType.GROUP_TYPE_TMP){
				if (groupMap.containsKey(groupId)
						&& groupMap.get(groupId).getVersion() == version) {

					GroupEntity groupEntity = this.findGroup(groupId);
					if (groupEntity != null) {
						groupEntity.setStatus(stats.ordinal());
						groupMap.put(groupId, groupEntity);
						dbInterface.insertOrUpdateGroup(groupEntity);
					}
					continue;
				}
			}



/*
			if (groupMap.containsKey(groupId)
					&& groupMap.get(groupId).getVersion() == version) {

				GroupEntity groupEntity = this.findGroup(groupId);
				if (groupEntity != null) {
					groupEntity.setStatus(stats.ordinal());
					groupMap.put(groupId, groupEntity);
					dbInterface.insertOrUpdateGroup(groupEntity);
				}
				continue;
			} else if (groupWeiMap.containsKey(groupId)
					&& groupWeiMap.get(groupId).getVersion() == version) {

				GroupEntity groupEntity = this.findGroup(groupId);
				if (groupEntity != null) {
					groupEntity.setStatus(stats.ordinal());
					groupMap.put(groupId, groupEntity);
					dbInterface.insertOrUpdateGroup(groupEntity);
				}
				continue;

			} else if (familyGroupMap.containsKey(groupId)
					&& familyGroupMap.get(groupId).getVersion() == version) {

				GroupEntity groupEntity = this.findFamilyGroup(groupId);
				if (groupEntity != null) {
					groupEntity.setStatus(stats.ordinal());
					groupMap.put(groupId, groupEntity);
					dbInterface.insertOrUpdateGroup(groupEntity);
				}
				continue;
			}

*/
			int tempType = 0;
			if (type == IMBaseDefine.GroupType.GROUP_TYPE_TMP) {
				tempType = DBConstant.GROUP_TYPE_TEMP;

			} else if (type == IMBaseDefine.GroupType.GROUP_TYPE_NORMAL) {
				tempType = DBConstant.GROUP_TYPE_NORMAL;

			} else if (type == IMBaseDefine.GroupType.GROUP_TYPE_DEVICE) {
				tempType = DBConstant.GROUP_TYPE_FAMILY;
			}

			GroupVersion Version = ProtoBuf2JavaBean.getGroupVersion(groupId,
					tempType, stats.ordinal(), version);
			groupVersionMap.put(Version.getGroupId(), Version);

			IMBaseDefine.GroupVersionInfo versionInfo = IMBaseDefine.GroupVersionInfo
					.newBuilder().setVersion(0).setGroupId(groupId)
					.setType(type).setStatus(stats).build();
			needInfoList.add(versionInfo);

		}

		// 事件触发的时候需要注意 todo
		if (needInfoList.size() > 0) {
			reqGetGroupDetailInfo(needInfoList);
		}

	}

	/**
	 * 二维码 请求群的版本数据
	 * 
	 * @param groupId
	 */
	public void reqGroupQrDetailInfo(int groupId) {
		IMBaseDefine.GroupVersionInfo groupVersionInfo = IMBaseDefine.GroupVersionInfo
				.newBuilder().setGroupId(groupId).setVersion(0).build();
		ArrayList<IMBaseDefine.GroupVersionInfo> list = new ArrayList<>();
		list.add(groupVersionInfo);
		reqGetGroupQRDetailInfo(list);

	}

	/**
	 * 二维码 请求群组的详细信息
	 */
	public void reqGetGroupQRDetailInfo(
			List<IMBaseDefine.GroupVersionInfo> versionInfoList) {
		logger.i("group#reqGetGroupDetailInfo");
		if (versionInfoList == null || versionInfoList.size() <= 0) {
			logger.e("group#reqGetGroupDetailInfo# please check your params,cause by empty/null");
			return;
		}
		int loginId = imLoginManager.getLoginId();
		IMGroup.IMGroupInfoListReq groupInfoListReq = IMGroup.IMGroupInfoListReq
				.newBuilder().setUserId(loginId)
				.addAllGroupVersionList(versionInfoList).build();

		int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
		int cid = IMBaseDefine.GroupCmdID.CID_GROUP_INFO_REQUEST_VALUE;
		// imSocketManager.sendRequest(groupInfoListReq, sid, cid);

		imSocketManager.sendRequest(groupInfoListReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {
						try {
							IMGroup.IMGroupInfoListRsp imGroupRsp = IMGroup.IMGroupInfoListRsp
									.parseFrom((CodedInputStream) response);
							onRepGroupQRDetailInfo(imGroupRsp);

						} catch (IOException e) {

						}
					}

					@Override
					public void onFaild() {

					}

					@Override
					public void onTimeout() {

					}
				});

	}

	/**
	 * 二维码 请求群的数据 回复
	 * 
	 * @param groupInfoListRsp
	 */
	public void onRepGroupQRDetailInfo(
			IMGroup.IMGroupInfoListRsp groupInfoListRsp) {
		logger.i("group#onRepGroupDetailInfo");
		int groupSize = groupInfoListRsp.getGroupInfoListCount();
		int userId = groupInfoListRsp.getUserId();
		int loginId = imLoginManager.getLoginId();

		if (groupSize <= 0 || userId != loginId) {
			logger.i(
					"group#onRepGroupDetailInfo size empty or userid[%d]≠ loginId[%d]",
					userId, loginId);
			return;
		}

		for (IMBaseDefine.GroupInfo groupInfo : groupInfoListRsp
				.getGroupInfoListList()) {
			// 群组的详细信息
			// 保存在DB中
			// GroupManager 中的变量
			GroupEntity groupEntity = ProtoBuf2JavaBean
					.getGroupEntity(groupInfo);

			GroupVersion version = findGroupVersion(groupEntity.getPeerId());
			if (version != null) {
				groupEntity.setSave(version.getStats());
			}

			groupQrMap.put(groupEntity.getPeerId(), groupEntity);

			// guanweile //youhui
			ArrayList<Integer> list = new ArrayList<>();
			list.addAll(groupEntity.getlistGroupMemberIds());

			ArrayList<Integer> getList = new ArrayList<>();
			for (int i = 0; i < list.size(); i++) {
				if (IMContactManager.instance().findContact(list.get(i)) == null) {
					getList.add(list.get(i));
				}
			}

			// just single type
			IMContactManager.instance().reqGetDetaillUsers(getList);
		}

		triggerEvent(new GroupEvent(GroupEvent.Event.GROUP_INFO_UPDATED));
	}

	/**
	 * 请求察看群的版本数据
	 * 
	 * @param groupId
	 */

	public void reqGroupDetailInfo(int groupId) {
		IMBaseDefine.GroupVersionInfo groupVersionInfo = IMBaseDefine.GroupVersionInfo
				.newBuilder().setGroupId(groupId).setVersion(0).build();
		ArrayList<IMBaseDefine.GroupVersionInfo> list = new ArrayList<>();
		list.add(groupVersionInfo);
		reqGetGroupDetailInfo(list);
	}

	/**
	 * 请求群组的详细信息
	 * 
	 * @param versionInfoList
	 */
	public void reqGetGroupDetailInfo(
			List<IMBaseDefine.GroupVersionInfo> versionInfoList) {
		logger.i("group#reqGetGroupDetailInfo");
		if (versionInfoList == null || versionInfoList.size() <= 0) {
			logger.e("group#reqGetGroupDetailInfo# please check your params,cause by empty/null");
			return;
		}
		int loginId = imLoginManager.getLoginId();
		IMGroup.IMGroupInfoListReq groupInfoListReq = IMGroup.IMGroupInfoListReq
				.newBuilder().setUserId(loginId)
				.addAllGroupVersionList(versionInfoList).build();

		int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
		int cid = IMBaseDefine.GroupCmdID.CID_GROUP_INFO_REQUEST_VALUE;
		imSocketManager.sendRequest(groupInfoListReq, sid, cid);
	}

	/**
	 * 请求群组的详细信息 回复
	 * 
	 * @param groupInfoListRsp
	 */
	public void onRepGroupDetailInfo(IMGroup.IMGroupInfoListRsp groupInfoListRsp) {
		logger.i("group#onRepGroupDetailInfo");
		int groupSize = groupInfoListRsp.getGroupInfoListCount();
		int userId = groupInfoListRsp.getUserId();
		int loginId = imLoginManager.getLoginId();

		if (groupSize <= 0 || userId != loginId) {
			logger.i(
					"group#onRepGroupDetailInfo size empty or userid[%d]≠ loginId[%d]",
					userId, loginId);
			return;
		}
		ArrayList<GroupEntity> needDb = new ArrayList<>();
		ArrayList<GroupEntity> needFamilyDb = new ArrayList<>();


		for (IMBaseDefine.GroupInfo groupInfo : groupInfoListRsp
				.getGroupInfoListList()) {
			// 群组的详细信息
			// 保存在DB中
			// GroupManager 中的变量
			GroupEntity groupEntity = ProtoBuf2JavaBean
					.getGroupEntity(groupInfo);

			GroupVersion version = findGroupVersion(groupEntity.getPeerId());
			if (version != null) {
				groupEntity.setSave(version.getStats());
			}

			//设备家庭群
			  if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY){
				familyGroupMap.put(groupEntity.getPeerId(), groupEntity);
				needFamilyDb.add(groupEntity);

				groupMap.put(groupEntity.getPeerId(), groupEntity);
				needDb.add(groupEntity);

			}else {
				groupMap.put(groupEntity.getPeerId(), groupEntity);
				needDb.add(groupEntity);


			}

			// guanweile //youhui
			ArrayList<Integer> list = new ArrayList<>();
			list.addAll(groupEntity.getlistGroupMemberIds());

			ArrayList<Integer> getList = new ArrayList<>();
			for (int i = 0; i < list.size(); i++) {
				if (IMContactManager.instance().findContact(list.get(i)) == null) {
					getList.add(list.get(i));
				}
			}

			// just single type
			IMContactManager.instance().reqGetDetaillUsers(getList);
		}

		dbInterface.batchInsertOrUpdateGroup(needDb);
		dbInterface.batchInsertOrUpdateFamilyGroup(needFamilyDb);

		triggerEvent(new GroupEvent(GroupEvent.Event.GROUP_INFO_UPDATED));
	}


	/**
	 * 创建群 默认是创建临时群，且客户端只能创建临时群
	 * 
	 * @param groupName
	 * @param memberList
	 */
	public void reqCreateTempGroup(String groupName, Set<Integer> memberList) {

		logger.i("group#reqCreateTempGroup, tempGroupName = %s", groupName);

		int loginId = imLoginManager.getLoginId();

		IMGroup.IMGroupCreateReq groupCreateReq = IMGroup.IMGroupCreateReq
				.newBuilder().setUserId(loginId)
				.setGroupType(IMBaseDefine.GroupType.GROUP_TYPE_TMP) // GROUP_TYPE_TMP
																		// guanweile
				.setGroupName(groupName).setGroupAvatar("")// todo 群头像 现在是四宫格
				.addAllMemberIdList(memberList).build();

		int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
		int cid = IMBaseDefine.GroupCmdID.CID_GROUP_CREATE_REQUEST_VALUE;
		imSocketManager.sendRequest(groupCreateReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {
						try {
							IMGroup.IMGroupCreateRsp groupCreateRsp = IMGroup.IMGroupCreateRsp
									.parseFrom((CodedInputStream) response);
							IMGroupManager.instance().onReqCreateTempGroup(
									groupCreateRsp);
						} catch (IOException e) {
							logger.e("reqCreateTempGroup parse error");
							triggerEvent(new GroupEvent(
									GroupEvent.Event.CREATE_GROUP_FAIL));
						}
					}

					@Override
					public void onFaild() {
						triggerEvent(new GroupEvent(
								GroupEvent.Event.CREATE_GROUP_FAIL));
					}

					@Override
					public void onTimeout() {
						triggerEvent(new GroupEvent(
								GroupEvent.Event.CREATE_GROUP_TIMEOUT));
					}
				});

	}

	
	/**
	 *  更新群的数据   好友群和位友群
	 * @param groupEntity
	 */
	public void updateGroup(GroupEntity groupEntity) {

		if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_TEMP) {
			groupMap.put(groupEntity.getPeerId(), groupEntity);
			dbInterface.insertOrUpdateGroup(groupEntity);

		}  else if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
			familyGroupMap.put(groupEntity.getPeerId(), groupEntity);
			dbInterface.insertOrUpdateFamilyGroup(groupEntity);
		}
	}


	 
	/**
	 *  创建临时群
	 * @param groupCreateRsp
	 */
	public void onReqCreateTempGroup(IMGroup.IMGroupCreateRsp groupCreateRsp) {
		logger.d("group#onReqCreateTempGroup");

		int resultCode = groupCreateRsp.getResultCode();
		if (0 != resultCode) {
			logger.e("group#createGroup failed");
			triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_FAIL));
			return;
		}

		GroupEntity groupEntity = ProtoBuf2JavaBean
				.getGroupEntity(groupCreateRsp);
		// 更新DB 更新map
		groupMap.put(groupEntity.getPeerId(), groupEntity);
		IMSessionManager.instance().updateSession(groupEntity);
		dbInterface.insertOrUpdateGroup(groupEntity);

		triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_OK,
				groupEntity)); // 接收到之后修改UI
	}


	/**
	 *  修改群的昵称信息
	 * @param groupId
	 * @param change_type
	 * @param value
	 * @param groupEntity
	 */
	public void modifyChangeGroupMember(int groupId,
			ChangeDataType change_type, String value,
			final GroupEntity groupEntity) {

		final int loginId = imLoginManager.getLoginId();
		IMGroup.IMGroupSettingReq groupChangeMemberReq = IMGroup.IMGroupSettingReq
				.newBuilder().setUserId(loginId).setGroupId(groupId)
				.setNewValue(value).setChangeType(change_type).build();

		int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
		int cid = IMBaseDefine.GroupCmdID.CID_GROUP_SETTING_REQUEST_VALUE;// id:CID_GROUP_SETTING_RESPONSE

		final int modifyGroupId = groupId;
		final String groupNick = value;
		imSocketManager.sendRequest(groupChangeMemberReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {
						try {
							IMGroup.IMGroupSettingRsp groupSettingRsp = IMGroup.IMGroupSettingRsp
									.parseFrom((CodedInputStream) response);

							if (groupSettingRsp.getChangeType() == ChangeDataType.CHANGE_GROUP_USER_UPDATE_NICK) {
								UserEntity login = IMLoginManager.instance()
										.getLoginInfo();
								login.setGroupNick(groupNick);
								IMLoginManager.instance().setLoginInfo(login);

								UserEntity currentUser = IMContactManager
										.instance().findContact(
												login.getPeerId());
								if (currentUser != null) {
									currentUser.setGroupNick(login
											.getMainName());
									IMContactManager.instance()
											.insertOrUpdateUser(currentUser);
								}

								GroupNickEntity entity = findGroupNick(
										modifyGroupId, login.getPeerId());
								if (entity != null) {
									entity.setNick(groupNick);
									int timeNow = (int) (System
											.currentTimeMillis() / 1000);
									entity.setUpdated(timeNow);
									dbInterface.insertOrUpdateGroupNick(entity);
								}

								triggerEvent(UserInfoEvent.USER_INFO_UPDATE);

							} else {

								if (groupSettingRsp.getChangeType() == ChangeDataType.CHANGE_GROUP_NOTICE_BOARD) {

									groupEntity.setBoardTime(Integer
											.toString(groupSettingRsp.getTime()));
									groupEntity.setBoard(groupNick);
									IMGroupManager.instance()
											.onModifyGroupSettingMember(
													groupSettingRsp,
													groupEntity);

									UserEntity loginUser = imLoginManager
											.getLoginInfo();
									String content = "群公告: \n" + groupNick;
									TextMessage textMessage = TextMessage
											.buildForSend(content, loginUser,
													groupEntity);
									IMMessageManager.instance().sendText(
											textMessage);

									triggerEvent(new GroupEvent(
											GroupEvent.Event.CHANGE_GROUP_NOTICE_SUCCESS));

									triggerEvent(new MessageEvent(
											MessageEvent.Event.CARD_SUCCESS,
											textMessage));

								} else {

									groupEntity.setMainName(groupNick);

									IMGroupManager.instance()
											.onModifyGroupSettingMember(
													groupSettingRsp,
													groupEntity);

								}
							}

						} catch (IOException e) {
							logger.e("reqChangeGroupMember parse error!");
							triggerEvent(new GroupEvent(
									GroupEvent.Event.CHANGE_GROUP_MODIFY_FAIL));
						}
					}

					@Override
					public void onFaild() {
						triggerEvent(new GroupEvent(
								GroupEvent.Event.CHANGE_GROUP_MODIFY_FAIL));
					}

					@Override
					public void onTimeout() {
						triggerEvent(new GroupEvent(
								GroupEvent.Event.CHANGE_GROUP_MODIFY_TIMEOUT));
					}
				});

	}

	
	/**
	 * 添加群昵称数据 GroupNickEntity为群昵称数据表
	 * @param entity
	 */
	public void addGroupNick(GroupNickEntity entity) {
		dbInterface.insertOrUpdateGroupNick(entity);
		localGroupNickMap.put(entity.getUserId(), entity);

	}

	
	/**
	 * 群设置  回复
	 * @param groupSettingRsp
	 * @param groupEntity
	 */
	public void onModifyGroupSettingMember(
			IMGroup.IMGroupSettingRsp groupSettingRsp, GroupEntity groupEntity) {
		int resultCode = groupSettingRsp.getResultCode();
		if (0 != resultCode) {
			triggerEvent(new GroupEvent(
					GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
			return;
		}

		int groupId = groupSettingRsp.getGroupId();

		if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_TEMP) {
			groupMap.put(groupId, groupEntity);
			dbInterface.insertOrUpdateGroup(groupEntity);

		} else if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
			familyGroupMap.put(groupId, groupEntity);
			dbInterface.insertOrUpdateFamilyGroup(groupEntity);
		}

		triggerEvent(new GroupEvent(
				GroupEvent.Event.CHANGE_GROUP_MODIFY_SUCCESS, groupEntity));

	}


	/**
	 * 删除并且退出群
	 * @param gourId
	 * @param change_type
	 * @param value
	 * @param groupEntity
	 */
	public void deleteGroupMember(int gourId, ChangeDataType change_type,
			String value, final GroupEntity groupEntity) {

		final int loginId = imLoginManager.getLoginId();
		IMGroup.IMGroupSettingReq groupChangeMemberReq = IMGroup.IMGroupSettingReq
				.newBuilder().setUserId(loginId).setGroupId(gourId)
				.setNewValue(value).setChangeType(change_type).build();

		int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
		int cid = IMBaseDefine.GroupCmdID.CID_GROUP_SETTING_REQUEST_VALUE;// id:CID_GROUP_SETTING_RESPONSE

		final String name = value;
		imSocketManager.sendRequest(groupChangeMemberReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {
						try {
							IMGroup.IMGroupSettingRsp groupSettingRsp = IMGroup.IMGroupSettingRsp
									.parseFrom((CodedInputStream) response);

							IMGroupManager.instance()
									.onDeleteGroupSettingMember(
											groupSettingRsp, groupEntity);
						} catch (IOException e) {
							logger.e("reqChangeGroupMember parse error!");
							triggerEvent(new GroupEvent(
									GroupEvent.Event.CHANGE_GROUP_DELETE_FAIL));
						}
					}

					@Override
					public void onFaild() {
						triggerEvent(new GroupEvent(
								GroupEvent.Event.CHANGE_GROUP_DELETE_FAIL));
					}

					@Override
					public void onTimeout() {
						triggerEvent(new GroupEvent(
								GroupEvent.Event.CHANGE_GROUP_DELETE_TIMEOUT));
					}
				});

	}

	
	/**
	 * 删除并退出群回复
	 * @param groupSettingRsp
	 * @param groupEntity
	 */
	public void onDeleteGroupSettingMember(
			IMGroup.IMGroupSettingRsp groupSettingRsp, GroupEntity groupEntity) {
		int resultCode = groupSettingRsp.getResultCode();
		if (0 != resultCode) {
			triggerEvent(new GroupEvent(
					GroupEvent.Event.CHANGE_GROUP_DELETE_FAIL));
			return;
		}

		int groupId = groupSettingRsp.getGroupId();

		if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_TEMP) {
			// groupMap.remove(groupEntity);
			groupMap.remove(groupId);
			// groupMap.put(groupId, groupEntity);
			dbInterface.deleteUpdateGroup(groupEntity);

		}
		String session = groupEntity.getSessionKey();
 

		List<RecentInfo> recentSessionList = IMSessionManager.instance()
				.getRecentListInfo();
		for (int i = 0; i < recentSessionList.size(); i++) {
			if (recentSessionList.get(i).getSessionKey().equals(session)) {
				IMSessionManager.instance().reqRemoveSession(
						recentSessionList.get(i), DBConstant.SESSION_ALL);
			}
		}

		triggerEvent(new GroupEvent(
				GroupEvent.Event.USER_GROUP_DELETE_SUCCESS, groupEntity));

	}

	
	/**
	 *  删除群在本地的数据
	 * @param groupEntity
	 */
	public void deleteGroup(GroupEntity groupEntity) {

		int groupId = groupEntity.getPeerId();
		if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_TEMP) {
			// groupMap.remove(groupEntity);
			groupMap.remove(groupId);
			// groupMap.put(groupId, groupEntity);
			dbInterface.deleteUpdateGroup(groupEntity);

		}  else if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {

			familyGroupMap.remove(groupId);
			dbInterface.deletUpdateFamilyGroup(groupEntity);


		}

		String session = groupEntity.getSessionKey();
 

		List<RecentInfo> recentSessionList = IMSessionManager.instance()
				.getRecentListInfo();
		for (int i = 0; i < recentSessionList.size(); i++) {
			if (recentSessionList.get(i).getSessionKey().equals(session)) {
				IMSessionManager.instance().reqRemoveSession(
						recentSessionList.get(i), DBConstant.SESSION_ALL);
			}
		}

		triggerEvent(new GroupEvent(
				GroupEvent.Event.CHANGE_GROUP_DELETE_SUCCESS, groupEntity));
	}



	/**
	 * 保存群到通讯录的请求
	 * @param gourId
	 * @param change_type
	 * @param value
	 * @param groupEntity
	 */
	public void saveChangeGroupMember(int gourId, ChangeDataType change_type,
			String value, final GroupEntity groupEntity) {

		// ChangeDataType change_type = ProtoBuf2JavaBean.getJavaSave(type);
		final int loginId = imLoginManager.getLoginId();
		IMGroup.IMGroupSettingReq groupChangeMemberReq = IMGroup.IMGroupSettingReq
				.newBuilder().setUserId(loginId).setGroupId(gourId)
				.setNewValue(value).setChangeType(change_type).build();

		int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
		int cid = IMBaseDefine.GroupCmdID.CID_GROUP_SETTING_REQUEST_VALUE;// id:CID_GROUP_SETTING_RESPONSE

		final String showvalue = value;
		imSocketManager.sendRequest(groupChangeMemberReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {
						try {
							IMGroup.IMGroupSettingRsp groupSettingRsp = IMGroup.IMGroupSettingRsp
									.parseFrom((CodedInputStream) response);
							IMGroupManager.instance()
									.onReqChangeGroupSettingMember(
											groupSettingRsp, groupEntity,
											showvalue);
						} catch (IOException e) {
							logger.e("reqChangeGroupMember parse error!");
							triggerEvent(new GroupEvent(
									GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
						}
					}

					@Override
					public void onFaild() {
						triggerEvent(new GroupEvent(
								GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
					}

					@Override
					public void onTimeout() {
						triggerEvent(new GroupEvent(
								GroupEvent.Event.CHANGE_GROUP_MEMBER_TIMEOUT));
					}
				});

	}

	
	/**
	 *  保存群岛通讯录的回复
	 * @param groupSettingRsp
	 * @param groupEntity
	 * @param showValue
	 */
	public void onReqChangeGroupSettingMember(
			IMGroup.IMGroupSettingRsp groupSettingRsp, GroupEntity groupEntity,
			String showValue) {
		int resultCode = groupSettingRsp.getResultCode();
		if (0 != resultCode) {
			triggerEvent(new GroupEvent(
					GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
			return;
		}
 

		if (Integer.parseInt(showValue) == 0) {
			groupEntity.setSave(DBConstant.GROUP_MEMBER_STATUS_TEMP);
		} else if (Integer.parseInt(showValue) == 1) {
			groupEntity.setSave(DBConstant.GROUP_MEMBER_STATUS_SAVE);
		}

		int groupId = groupSettingRsp.getGroupId();

		if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_TEMP) {
			groupMap.put(groupId, groupEntity);
			dbInterface.insertOrUpdateGroup(groupEntity);

		}
	}

	 /**
	  *  删除群成员 REMOVE_CHANGE_MEMBER_TYPE 可能会触发头像的修改
	  * @param groupId
	  * @param removeMemberlist
	  */
	public void reqRemoveGroupMember(int groupId, Set<Integer> removeMemberlist) {
		reqChangeGroupMember(groupId,
				IMBaseDefine.ChangeDataType.CHANGE_GROUP_USER_DEL,
				removeMemberlist);
	}

	/**
	 * 新增群成员 ADD_CHANGE_MEMBER_TYPE 可能会触发头像的修改
	 */
	public void reqAddGroupMember(int groupId, Set<Integer> addMemberlist,
			IMBaseDefine.ChangeDataType type) {
		reqChangeGroupMember(groupId, type,
		// IMBaseDefine.ChangeDataType.CHANGE_GROUP_USER_ADD,
				addMemberlist);
	}

	
	/**
	 * 修改群成员
	 * @param groupId
	 * @param groupModifyType
	 * @param changeMemberlist
	 */
	private void reqChangeGroupMember(int groupId,
			IMBaseDefine.ChangeDataType groupModifyType,
			Set<Integer> changeMemberlist) {
		logger.i("group#reqChangeGroupMember, changeGroupMemberType = %s",
				groupModifyType.toString());

		final int loginId = imLoginManager.getLoginId();
		IMGroup.IMGroupChangeMemberReq groupChangeMemberReq = IMGroup.IMGroupChangeMemberReq
				.newBuilder().setUserId(loginId).setChangeType(groupModifyType)
				.addAllMemberIdList(changeMemberlist).setGroupId(groupId)
				.build();

		int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
		int cid = IMBaseDefine.GroupCmdID.CID_GROUP_CHANGE_MEMBER_REQUEST_VALUE;
		imSocketManager.sendRequest(groupChangeMemberReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {
						try {
							IMGroup.IMGroupChangeMemberRsp groupChangeMemberRsp = IMGroup.IMGroupChangeMemberRsp
									.parseFrom((CodedInputStream) response);
							IMGroupManager.instance().onReqChangeGroupMember(
									groupChangeMemberRsp);
						} catch (IOException e) {
							logger.e("reqChangeGroupMember parse error!");
							triggerEvent(new GroupEvent(
									GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
						}
					}

					@Override
					public void onFaild() {
						triggerEvent(new GroupEvent(
								GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
					}

					@Override
					public void onTimeout() {
						triggerEvent(new GroupEvent(
								GroupEvent.Event.CHANGE_GROUP_MEMBER_TIMEOUT));
					}
				});

	}

	
	/**
	 * 修改群成员  回复
	 * @param groupChangeMemberRsp
	 */
	public void onReqChangeGroupMember(
			IMGroup.IMGroupChangeMemberRsp groupChangeMemberRsp) {
		int resultCode = groupChangeMemberRsp.getResultCode();

		if (0 != resultCode) {
			triggerEvent(new GroupEvent(
					GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
			return;
		}

		int groupId = groupChangeMemberRsp.getGroupId();
		List<Integer> changeUserIdList = groupChangeMemberRsp
				.getChgUserIdListList();
		IMBaseDefine.ChangeDataType groupModifyType = groupChangeMemberRsp
				.getChangeType();

		GroupEntity groupEntityRet = groupMap.get(groupId);
		if (groupEntityRet != null) {
			groupEntityRet.setlistGroupMemberIds(groupChangeMemberRsp
					.getCurUserIdListList());
			groupMap.put(groupId, groupEntityRet);
			dbInterface.insertOrUpdateGroup(groupEntityRet);
		} else {

			//家庭群
			groupEntityRet = familyGroupMap.get(groupId);
			if (groupEntityRet != null) {
				groupEntityRet.setlistGroupMemberIds(groupChangeMemberRsp
						.getCurUserIdListList());
				familyGroupMap.put(groupId, groupEntityRet);
				dbInterface.insertOrUpdateFamilyGroup(groupEntityRet);

			}else{
				groupEntityRet = groupQrMap.get(groupId);
				groupEntityRet.setlistGroupMemberIds(groupChangeMemberRsp
						.getCurUserIdListList());
				groupMap.put(groupId, groupEntityRet);
				dbInterface.insertOrUpdateGroup(groupEntityRet);
			}

		}

		GroupEvent groupEvent = new GroupEvent(
				GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
		groupEvent.setChangeList(changeUserIdList);
		groupEvent.setChangeType(ProtoBuf2JavaBean
				.getGroupChangeType(groupModifyType));
		groupEvent.setGroupEntity(groupEntityRet);
		triggerEvent(groupEvent);
	}




	/**
	 * 收到群成员发生变更消息 服务端主动发出 DB
	 * @param notify
	 */
	public void receiveGroupChangeMemberNotify(
			IMGroup.IMGroupChangeMemberNotify notify) {

		int groupId = notify.getGroupId();
		int changeType = ProtoBuf2JavaBean.getGroupChangeType(notify
				.getChangeType());
		List<Integer> changeList = notify.getChgUserIdListList();

		List<Integer> curMemberList = notify.getCurUserIdListList();

		// 人员变更
		if (IMBaseDefine.ChangeDataType.CHANGE_GROUP_USER_ADD == notify
				.getChangeType()) {

			if (groupMap.containsKey(groupId)) {

				GroupEntity entity = groupMap.get(groupId);
				entity.setlistGroupMemberIds(curMemberList);
				entity.setSave(DBConstant.GROUP_MEMBER_STATUS_TEMP);

				dbInterface.insertOrUpdateGroup(entity);
				groupMap.put(groupId, entity);



				ArrayList<Integer> msgIdList = new ArrayList<>();
				String userName="";
				for(int i=0;i<changeList.size();i++){
					 if(IMContactManager.instance().findContact(changeList.get(i))!=null){
						UserEntity user = IMContactManager.instance().findContact(changeList.get(i));
						userName = userName + user.getMainName() +",";
					}  else {
						msgIdList.add(changeList.get(i));
					}
				}

				if(msgIdList.size()<=0){
					NoticeMessage noticeMessage = NoticeMessage.buildGroupForSend(userName + "加入群中",IMLoginManager.instance().getLoginInfo(),entity);
					IMMessageManager.instance().sendGorupNotice(noticeMessage);
				}else{
					IMContactManager.instance().reqGetDetaillUsersAdd(msgIdList,userName,entity,DBConstant.REQ_USER_ADD);
				}

				GroupEvent groupEvent = new GroupEvent(
						GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
				groupEvent.setChangeList(changeList);
				groupEvent.setChangeType(changeType);
				groupEvent.setGroupEntity(entity);
				triggerEvent(groupEvent);
//				NoticeMessage noticeMessage = NoticeMessage.buildGroupForSend(IMLoginManager.instance().getLoginInfo().getMainName() + "加入群中",IMLoginManager.instance().getLoginInfo(),entity);
//				IMMessageManager.instance().sendGorupNotice(noticeMessage);

			} else if (familyGroupMap.containsKey(groupId)) {

				GroupEntity entity = familyGroupMap.get(groupId);
				entity.setlistGroupMemberIds(curMemberList);
				dbInterface.insertOrUpdateFamilyGroup(entity);
				familyGroupMap.put(groupId, entity);

				GroupEvent groupEvent = new GroupEvent(
						GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
				groupEvent.setChangeList(changeList);
				groupEvent.setChangeType(changeType);
				groupEvent.setGroupEntity(entity);
				triggerEvent(groupEvent);

			} else {
				// todo 没有就暂时不管了，只要聊过天都会显示在回话里面
			}

		} else if (IMBaseDefine.ChangeDataType.CHANGE_GROUP_USER_ADD_BY_SCAN == notify
				.getChangeType()) {
			if (groupMap.containsKey(groupId)) {
				GroupEntity entity = groupMap.get(groupId);
				entity.setlistGroupMemberIds(curMemberList);

				dbInterface.insertOrUpdateGroup(entity);
				groupMap.put(groupId, entity);

				GroupEvent groupEvent = new GroupEvent(
						GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
				groupEvent.setChangeList(changeList);
				groupEvent.setChangeType(changeType);
				groupEvent.setGroupEntity(entity);
				triggerEvent(groupEvent);


				ArrayList<Integer> msgIdList = new ArrayList<>();
				String userName="";
				for(int i=0;i<changeList.size();i++){
					if(IMContactManager.instance().findContact(changeList.get(i))!=null){
						UserEntity user = IMContactManager.instance().findContact(changeList.get(i));
						userName = userName + user.getMainName() +",";
					}else {
						msgIdList.add(changeList.get(i));
					}
				}

				IMContactManager.instance().reqGetDetaillUsersAdd(msgIdList,userName,entity,DBConstant.REQ_USER_ADD);

			} else if (familyGroupMap.containsKey(groupId)) {

				GroupEntity entity = familyGroupMap.get(groupId);
				entity.setlistGroupMemberIds(curMemberList);
				dbInterface.insertOrUpdateFamilyGroup(entity);
				familyGroupMap.put(groupId, entity);

				GroupEvent groupEvent = new GroupEvent(
						GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
				groupEvent.setChangeList(changeList);
				groupEvent.setChangeType(changeType);
				groupEvent.setGroupEntity(entity);
				triggerEvent(groupEvent);

			}else {
				// todo 没有就暂时不管了，只要聊过天都会显示在回话里面
			}

			// guanweile
			ArrayList<Integer> list = new ArrayList<>();
			for (int i = 0; i < changeList.size(); i++) {
				list.add(changeList.get(i));
			}
			// just single type
			IMContactManager.instance().reqGetDetaillUsers(list);

		} else if (IMBaseDefine.ChangeDataType.CHANGE_GROUP_USER_DEL == notify
				.getChangeType()) {

			if (groupMap.containsKey(groupId)) {
				GroupEntity entity = groupMap.get(groupId);
				entity.setlistGroupMemberIds(curMemberList);
				for(int i=0;i<changeList.size();i++){
					if(changeList.get(i) == IMLoginManager.instance().getLoginId()){
						entity.setSave(DBConstant.GROUP_MEMBER_STATUS_EXIT);
					}
				}


				dbInterface.insertOrUpdateGroup(entity);
				groupMap.put(groupId, entity);

				GroupEvent groupEvent = new GroupEvent(
						GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
				groupEvent.setChangeList(changeList);
				groupEvent.setChangeType(changeType);
				groupEvent.setGroupEntity(entity);
				triggerEvent(groupEvent);


//				NoticeMessage noticeMessage = NoticeMessage.buildGroupForSend( IMLoginManager.instance().getLoginInfo().getMainName() + "已被踢出群了",IMLoginManager.instance().getLoginInfo(),entity);
//				IMMessageManager.instance().sendGorupNotice(noticeMessage);

				ArrayList<Integer> msgIdList = new ArrayList<>();
				String userName="";
				for(int i=0;i<changeList.size();i++){
					if(IMContactManager.instance().findContact(changeList.get(i))!=null){
						UserEntity user = IMContactManager.instance().findContact(changeList.get(i));
						userName = userName + user.getMainName() +",";
					}  else {
						msgIdList.add(changeList.get(i));
					}
				}

				if(msgIdList.size()<=0){
					NoticeMessage noticeMessage = NoticeMessage.buildGroupForSend(userName +  "已被移出群了",IMLoginManager.instance().getLoginInfo(),entity);
					IMMessageManager.instance().sendGorupNotice(noticeMessage);
				}else{
					IMContactManager.instance().reqGetDetaillUsersAdd(msgIdList,userName,entity,DBConstant.REQ_USER_DEL);
				}


			}else if (familyGroupMap.containsKey(groupId)) {

				GroupEntity entity = familyGroupMap.get(groupId);
				entity.setlistGroupMemberIds(curMemberList);
				dbInterface.insertOrUpdateFamilyGroup(entity);
				familyGroupMap.put(groupId, entity);

				GroupEvent groupEvent = new GroupEvent(
						GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
				groupEvent.setChangeList(changeList);
				groupEvent.setChangeType(changeType);
				groupEvent.setGroupEntity(entity);
				triggerEvent(groupEvent);

			}else {
				// todo 没有就暂时不管了，只要聊过天都会显示在回话里面
			}
 

		} else {

			if (groupMap.containsKey(groupId)) {
				GroupEntity entity = groupMap.get(groupId);
				entity.setlistGroupMemberIds(curMemberList);
				dbInterface.insertOrUpdateGroup(entity);
				groupMap.put(groupId, entity);

				GroupEvent groupEvent = new GroupEvent(
						GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
				groupEvent.setChangeList(changeList);
				groupEvent.setChangeType(changeType);
				groupEvent.setGroupEntity(entity);
				triggerEvent(groupEvent);

			}else if (familyGroupMap.containsKey(groupId)) {

				GroupEntity entity = familyGroupMap.get(groupId);
				entity.setlistGroupMemberIds(curMemberList);
				dbInterface.insertOrUpdateFamilyGroup(entity);
				familyGroupMap.put(groupId, entity);

				GroupEvent groupEvent = new GroupEvent(
						GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
				groupEvent.setChangeList(changeList);
				groupEvent.setChangeType(changeType);
				groupEvent.setGroupEntity(entity);
				triggerEvent(groupEvent);

			}else {
				// todo 没有就暂时不管了，只要聊过天都会显示在回话里面
			}

		}

	}



	/**
	 *  获取全部的家庭群
	 * @return
	 */
	public List<GroupEntity> getNormalFamilyGroupList() {
		List<GroupEntity> normalGroupList = new ArrayList<>();

		for (Entry<Integer, GroupEntity> entry : familyGroupMap.entrySet()) {
			GroupEntity group = entry.getValue();
			if (group == null) {
				continue;
			}

			// guanweile
			if (group.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
				normalGroupList.add(group);
			}
		}
		return normalGroupList;
	}

	
	/**
	 *  获取全部的好友群
	 * @return
	 */
	public List<GroupEntity> getNormalGroupList() {
		List<GroupEntity> normalGroupList = new ArrayList<>();

		for (Entry<Integer, GroupEntity> entry : groupMap.entrySet()) {
			GroupEntity group = entry.getValue();
			if (group == null) {
				continue;
			}

			// guanweile
			if (group.getGroupType() == DBConstant.GROUP_TYPE_NORMAL) {

				if (group.getSave() == DBConstant.GROUP_MEMBER_STATUS_SAVE) {
					normalGroupList.add(group);
				}
			} else if (group.getGroupType() == DBConstant.GROUP_TYPE_TEMP) {

				if (group.getSave() == DBConstant.GROUP_MEMBER_STATUS_SAVE) {
					normalGroupList.add(group);
				}
			}
		}
		return normalGroupList;
	}

	 /**
	  * 该方法获取全部的正式群
	  * @return
	  */
	// todo eric efficiency
	public List<GroupEntity> getNormalGroupSortedList() {

		List<GroupEntity> groupList = getNormalGroupList();
		Collections.sort(groupList, new Comparator<GroupEntity>() {
			@Override
			public int compare(GroupEntity entity1, GroupEntity entity2) {
				if (entity1.getPinyinElement().pinyin == null) {
					PinYin.getPinYin(entity1.getMainName(),
							entity1.getPinyinElement());
				}
				if (entity2.getPinyinElement().pinyin == null) {
					PinYin.getPinYin(entity2.getMainName(),
							entity2.getPinyinElement());
				}
				return entity1.getPinyinElement().pinyin
						.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
			}
		});

		return groupList;
	}

	
	/**
	 *  通过id找全部的群信息
	 * @param groupId
	 * @return
	 */
	public GroupEntity findGroup(int groupId) {
		logger.d("group#findGroup groupId:%s", groupId);
		  if (familyGroupMap.containsKey(groupId)) {
			return familyGroupMap.get(groupId);
		}else 	if (groupMap.containsKey(groupId)) {
			 return groupMap.get(groupId);
		 }

		return null;
	}

	
	/**
	 * 通过id查找二维码扫描的群数据
	 * @param groupId
	 * @return
	 */
	public GroupEntity findQRGroup(int groupId) {
		if (groupQrMap.containsKey(groupId)) {
			return groupQrMap.get(groupId);
		}
		return null;
	}

	
	/**
	 * 通过ID查找群的版本数据
	 * @param groupId
	 * @return
	 */
	public GroupVersion findGroupVersion(int groupId) {

		if (groupVersionMap.containsKey(groupId)) {
			return groupVersionMap.get(groupId);
		}

		return null;
	}

	


	/**
	 *  通过ID查找家庭群群的数据
	 * @param groupId
	 * @return
	 */
	public GroupEntity findFamilyGroup(int groupId) {
		logger.d("group#findGroup groupId:%s", groupId);
		if (familyGroupMap.containsKey(groupId)) {
			return familyGroupMap.get(groupId);
		}
		return null;
	}


	
	/**
	 * 搜索匹配key获取的好友群数据
	 * @param key
	 * @return
	 */
	public List<GroupEntity> getSearchAllGroupList(String key) {
		List<GroupEntity> searchList = new ArrayList<>();
		for (Map.Entry<Integer, GroupEntity> entry : groupMap.entrySet()) {
			GroupEntity groupEntity = entry.getValue();
			if (IMUIHelper.handleGroupSearch(key, groupEntity)) {
				searchList.add(groupEntity);
			}
		}
		return searchList;
	}

	
	/**
	 *  查找群的成员数据
	 * @param groupId
	 * @return
	 */
	public List<UserEntity> getGroupMembers(int groupId) {

		logger.d("group#getGroupMembers groupId:%s", groupId);
		GroupEntity group = findGroup(groupId);
		if (group == null) {
			logger.e("group#no such group id:%s", groupId);
			return null;
		}
		Set<Integer> userList = group.getlistGroupMemberIds();
		ArrayList<UserEntity> memberList = new ArrayList<UserEntity>();
		for (Integer id : userList) {
			UserEntity contact = IMContactManager.instance().findContact(id);
			if (contact == null) {
				logger.e("group#no such contact id:%s", id);
				continue;
			}
			memberList.add(contact);
		}
		return memberList;
	}

	/** ------set/get 的定义 */
	public Map<Integer, GroupEntity> getGroupMap() {
		return groupMap;
	}



	public boolean isGroupReady() {
		return isGroupReady;
	}
}

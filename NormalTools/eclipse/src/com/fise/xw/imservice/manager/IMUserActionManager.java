package com.fise.xw.imservice.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;

import com.amap.api.maps2d.model.LatLng;
import com.google.protobuf.CodedInputStream;
import com.fise.xw.DB.DBInterface;
import com.fise.xw.DB.entity.DeviceEntity;
import com.fise.xw.DB.entity.FamilyConcernEntity;
import com.fise.xw.DB.entity.ReqFriendsEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.entity.WeiEntity;
import com.fise.xw.DB.entity.WhiteEntity;
import com.fise.xw.DB.sp.LoginSp;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.MessageConstant;
import com.fise.xw.imservice.callback.Packetlistener;
import com.fise.xw.imservice.entity.AddFriendsMessage;
import com.fise.xw.imservice.entity.AudioMessage;
import com.fise.xw.imservice.entity.RecentInfo;
import com.fise.xw.imservice.event.DeviceEvent;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.event.PriorityEvent;
import com.fise.xw.imservice.event.ReqEvent;
import com.fise.xw.imservice.event.ReqFriendsEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.service.LocalService;
import com.fise.xw.protobuf.IMBaseDefine;
import com.fise.xw.protobuf.IMBaseDefine.AuthConfirmType;
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.protobuf.IMBaseDefine.CommandType;
import com.fise.xw.protobuf.IMBaseDefine.SessionType;
import com.fise.xw.protobuf.IMBaseDefine.UserActionInfo;
import com.fise.xw.protobuf.IMBaseDefine.UserLocation;
import com.fise.xw.protobuf.IMBuddy;
import com.fise.xw.protobuf.IMSms.SmsActionType;
import com.fise.xw.protobuf.IMUserAction;
import com.fise.xw.protobuf.IMUserAction.ActionResult;
import com.fise.xw.protobuf.IMUserAction.ActionType;
import com.fise.xw.protobuf.helper.ProtoBuf2JavaBean;
import com.fise.xw.ui.helper.AudioPlayerHandler;
import com.fise.xw.ui.helper.AudioRecordHandler;
import com.fise.xw.utils.CommonUtil;
import com.fise.xw.utils.Logger;
import com.fise.xw.utils.Utils;
import com.xiaowei.phone.PhoneMemberBean;

import de.greenrobot.event.EventBus;

/**
 * 负责用户信息的请求 为回话页面以及联系人页面提供服务
 * 
 * 联系人信息管理 普通用户的version 有总版本 群组没有总version的概念， 每个群有version 具体请参见 服务端具体的pd协议
 */

public class IMUserActionManager extends IMManager {
	private Logger logger = Logger.getLogger(IMUserActionManager.class);

	// 单例
	private static IMUserActionManager inst = new IMUserActionManager();

	private AudioRecordHandler audioRecorderInstance = null;
	private Thread audioRecorderThread = null;
	public String audioSavePath;
	private AudioTimeCount time = new AudioTimeCount(20000, 1000);
	private int audioId;

	public static IMUserActionManager instance() {
		return inst;
	}

	private IMSocketManager imSocketManager = IMSocketManager.instance();
	private DBInterface dbInterface = DBInterface.instance();
	private IMContactManager imContactManager = IMContactManager.instance();

	// 自身状态字段
	private boolean userDataReady = false;

	private Map<Integer, UserEntity> userMonitorMap = new ConcurrentHashMap<>();
	ArrayList<UserEntity> monitorDb = new ArrayList<>();
	private List<UserEntity> searchInfo = new ArrayList<UserEntity>();

	private List<LatLng> lat = new ArrayList<LatLng>();
	public ArrayList<UserEntity> phoneList = new ArrayList<>();

	public List<PhoneMemberBean> sourceList = new ArrayList<>();

	private String pass_code = "";
	private String modify_code = "";
	private Map<Integer, WeiEntity> userFriendsMap = new ConcurrentHashMap<>();
	private Map<Integer, WeiEntity> userWeiMap = new ConcurrentHashMap<>();

	@Override
	public void doOnStart() {

	}

	/**
	 * 登陆成功触发 auto自动登陆
	 * */
	public void onNormalLoginOk() {
		onLocalLoginOk();
		onLocalNetOk();
	}

	/**
	 * 加载本地DB的状态 不管是离线还是在线登陆，loadFromDb 要运行的
	 */
	public void onLocalLoginOk() {
		logger.d("contact#loadAllUserInfo");

		// 部门
		// List<DepartmentEntity> deptlist = dbInterface.loadAllDept();
		logger.d("contact#loadAllDept dbsuccess");

		// 位友请求信息
		List<WeiEntity> userWeilist = dbInterface.loadAllWei();
		for (WeiEntity userInfo : userWeilist) {
			userWeiMap.put(userInfo.getFromId(), userInfo);
		}

		// 好友请求信息
		List<WeiEntity> userReqlist = dbInterface.loadAllUserReq();
		for (WeiEntity userInfo : userReqlist) {
			userFriendsMap.put(userInfo.getFromId(), userInfo);
		}

		triggerEvent(UserInfoEvent.USER_INFO_OK);
	}

	/**
	 * 加载本地DB的状态 不管是离线还是在线登陆，loadFromDb 要运行的
	 */
	public List<WeiEntity> loadWei() {
		logger.d("contact#reqWeilist");

		// 原来是部门
		// List<DepartmentEntity> deptlist = dbInterface.loadAllDept();
		List<WeiEntity> reqWeilist = dbInterface.loadAllWei();

		return reqWeilist;
	}

	/**
	 * 加载本地DB的状态 不管是离线还是在线登陆，loadFromDb 要运行的
	 */
	public List<UserEntity> reqFriends() {
		logger.d("contact#loadAllUserInfo");

		List<UserEntity> reqFriendslist = dbInterface.loadAllReqFriends();

		return reqFriendslist;
	}

	/**
	 * 加载本地DB的状态 不管是离线还是在线登陆，loadFromDb 要运行的
	 */
	public List<UserEntity> monitorFriends() {

		List<UserEntity> userlist = dbInterface.loadAllUsers();
		List<UserEntity> monitorDbTest = new ArrayList<>();

		for (UserEntity userInfo : userlist) { // guanweile
			// todo DB的状态不包含拼音的，这个样每次都要加载啊
			int loginId = IMLoginManager.instance().getLoginId();
			if (true && (loginId != userInfo.getPeerId())) {
				monitorDbTest.add(userInfo);
			}
		}
		return monitorDbTest;
	}

	/**
	 * 网络链接成功，登陆之后请求
	 */
	public void onLocalNetOk() {
		// 用户信息
		int updateTime = dbInterface.getReqFriendsLastTime();

		// 请求位友请求
		reqWeiFriends(updateTime);
	}

	@Override
	public void reset() {
		userDataReady = false;
		userMonitorMap.clear();
		searchInfo.clear();
		lat.clear();
		userFriendsMap.clear();
		userWeiMap.clear();
	}

	public List<UserEntity> getSearchInfo() {
		return searchInfo;
	}

	public void setSearchInfo(List<UserEntity> searchInfo) {
		this.searchInfo = searchInfo;
	}

	public void setSearchInfo(UserEntity Info) {
		this.searchInfo.clear();
		this.searchInfo.add(Info);
	}

	public void changePassword(int fromId, final String password) {

		String desPwd = new String(com.fise.xw.Security.getInstance()
				.EncryptPass(password));

		IMUserAction.ChangePasswordReq passReq = IMUserAction.ChangePasswordReq
				.newBuilder().setFromId(fromId).setPassword(desPwd).build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_UPDATE_PASSWORD_REQ_VALUE;

		imSocketManager.sendRequest(passReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMUserAction.ChangePasswordRsp imPassRes = IMUserAction.ChangePasswordRsp
							.parseFrom((CodedInputStream) response);
					onChangePasswordRsp(imPassRes, password);
				} catch (IOException e) {
					triggerEvent(UserInfoEvent.USER_MODIFY_PASS_FAIL);
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(UserInfoEvent.USER_MODIFY_PASS_FAIL);
			}

			@Override
			public void onTimeout() {
				triggerEvent(UserInfoEvent.USER_MODIFY_PASS_FAIL);
			}
		});

	}

	public void onChangePasswordRsp(IMUserAction.ChangePasswordRsp imPassRes,
			String pass) {

		if (imPassRes == null) {
			triggerEvent(UserInfoEvent.USER_MODIFY_PASS_FAIL);
			return;
		}

		int code = imPassRes.getRetCode();

		switch (code) {
		case 0: {

			LoginSp.instance().setLoginInfo(
					LoginSp.instance().getLoginIdentity().getLoginName(), pass,
					LoginSp.instance().getLoginIdentity().getLoginId(),
					LoginSp.instance().getLoginIdentity().getImei());

			triggerEvent(UserInfoEvent.USER_MODIFY_PASS_SUCCESS);
		}
			break;

		default: {
			modify_code = imPassRes.getRetMsg();
			triggerEvent(UserInfoEvent.USER_MODIFY_PASS_FAIL);
		}
			break;
		}
	}

	public String getModifyPass() {
		return modify_code;
	}

	/**
	 * 发送搜索请求
	 * 
	 * @param searReq
	 */
	public void reqFriends(String searReq) {

		logger.i("login#reqLoginMsgServer");

		int userId = IMLoginManager.instance().getLoginId();

		IMUserAction.SearchReq search = IMUserAction.SearchReq.newBuilder()
				.setUserId(userId).setSearchName(searReq).build();
		// .setSearchType("").build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_SEARCH_REQ_VALUE;
		imSocketManager.sendRequest(search, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMUserAction.SearchRes imSearchRes = IMUserAction.SearchRes
							.parseFrom((CodedInputStream) response);
					onRepMsgServerReqFriends(imSearchRes);

				} catch (IOException e) {
					triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_FAILED);
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_FAILED);
			}
		});
	}

	/**
	 * 验证好友请求信息结果
	 * 
	 * @param loginRes
	 */
	public void onRepMsgServerReqFriends(IMUserAction.SearchRes imSearchRes) {
		logger.i("login#onRepMsgServerLogin");

		if (imSearchRes == null) {
			logger.e("login#decode LoginResponse failed");
			triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_FAILED);
			return;
		}

		IMBaseDefine.ResultType code = imSearchRes.getResultCode();

		switch (code) {
		case REFUSE_REASON_NONE: {

			searchInfo.clear();
			for (int i = 0; i < imSearchRes.getInfoListCount(); i++) {
				IMBaseDefine.UserInfo userInfo = imSearchRes.getInfoList(i);
				UserEntity info = ProtoBuf2JavaBean.getUserEntity(userInfo);
				searchInfo.add(info);
			}
			triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_SUCCESS);
		}
			break;

		case REFUSE_REASON_DB_VALIDATE_FAILED: {
			triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_FAILED);
		}
			break;

		default: {
			triggerReqEvent(ReqFriendsEvent.REQ_FRIENDS_FAILED);
		}
			break;
		}
	}

	/**
	 * 事件分发
	 * 
	 * @param event
	 */
	public void triggerEvent(UserInfoEvent event) {
		// 先更新自身的状态
		switch (event) {
		case USER_INFO_OK:
			userDataReady = true;
			break;
		}
		EventBus.getDefault().postSticky(event);
	}

	/** -----------------------事件驱动---end--------- */

	/**
	 * 打开或者取消位友监控的请求
	 * 
	 * @param fromId
	 * @param verifyType
	 * @param actionType
	 * @param value
	 */
	public void verifyAuthValue(int fromId, AuthConfirmType verifyType,
			SmsActionType actionType, String value) {

		IMUserAction.VerifyAuthValueReq verifyReq = IMUserAction.VerifyAuthValueReq
				.newBuilder().setFromId(fromId).setVerifyType(verifyType)
				.setActionType(actionType).setAuthValue(value).build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_VERIFY_VALUE_REQ_VALUE;
		imSocketManager.sendRequest(verifyReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {

				IMUserAction.VerifyAuthValueRsp authValueRsp;
				try {
					authValueRsp = IMUserAction.VerifyAuthValueRsp
							.parseFrom((CodedInputStream) response);
					onVerifyAuthValue(authValueRsp);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
	 * 原密码验证请求 (修改密码需要密码验证)
	 * 
	 * @param authValueRsp
	 */
	public void onVerifyAuthValue(IMUserAction.VerifyAuthValueRsp authValueRsp) {

		// if(authValueRsp.getRetCode()!=0){
		// throw new RuntimeException("请求验证原始秘密不正确");
		// }
		// 阿第三方

		int code = authValueRsp.getRetCode();
		switch (code) {
		case 0: {

			triggerEvent(UserInfoEvent.USER_INFO_VERIFYAUTH_SUCCESS);
		}
			break;

		default: {
			logger.e("Avatar#Avatar msg server inner failed, result:%s", code);
			pass_code = authValueRsp.getRetMsg();
			triggerEvent(UserInfoEvent.USER_INFO_VERIFYAUTH_FAIL);

		}
			break;
		}

	}

	public String getPassCode() {
		return pass_code;
	}

	/**
	 * 黑名单
	 * 
	 * @param type
	 * @param toId
	 * @param entity
	 */
	public void cancelBlackList(ActionType type, int toId,
			final UserEntity entity) {

		int loginId = IMLoginManager.instance().getLoginId();
		IMUserAction.CommonRequest weiActionReq = IMUserAction.CommonRequest
				.newBuilder().setFromId(loginId).setToId(toId).setActId(0)
				.setActType(type).build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_VALUE;

		final ActionType Reqtype = type;
		final int showToId = toId;
		imSocketManager.sendRequest(weiActionReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {

						IMUserAction.ActionAck imMsgDataAck = null;
						try {
							imMsgDataAck = IMUserAction.ActionAck
									.parseFrom((CodedInputStream) response);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (imMsgDataAck.getRetCode() != 0) {
							// 更新黑名单信息
							triggerEvent(UserInfoEvent.USER_BLACKLIST_FAIL); // guanweile
							throw new RuntimeException(
									"Msg ack error,cause by msgId <=0");
						}

						// 加入黑名单
						if (Reqtype == ActionType.ACTION_TYPE_ADD_BLACKLIST) {

							imContactManager.addBlackList(entity);
							// 更新好友信息
							triggerEvent(UserInfoEvent.USER_INFO_UPDATE); // guanweile

							// 更新黑名单信息
							triggerEvent(UserInfoEvent.USER_BLACKLIST_SUCCESS); // guanweile

							// 移出黑名单
						} else if (Reqtype == ActionType.ACTION_TYPE_DEL_BLACKLIST) {

							imContactManager.removeBlackList(entity);
							// 更新黑名单信息
							triggerEvent(UserInfoEvent.USER_BLACKLIST_DEL_SUCCESS); // guanweile

							// 更新好友信息
							triggerEvent(UserInfoEvent.USER_INFO_UPDATE); // guanweile

						}

					}

					@Override
					public void onFaild() {
						// 更新黑名单信息
						triggerEvent(UserInfoEvent.USER_BLACKLIST_FAIL); // guanweile
					}

					@Override
					public void onTimeout() {
						// 更新黑名单信息
						triggerEvent(UserInfoEvent.USER_BLACKLIST_FAIL); // guanweile

					}
				});

	}

	/**
	 * 打开或者取消位友监控的请求
	 * 
	 * @param type
	 * @param toId
	 */
	public void cancelWeiFriends(ActionType type, int toId) {

		int loginId = IMLoginManager.instance().getLoginId();
		IMUserAction.CommonRequest weiActionReq = IMUserAction.CommonRequest
				.newBuilder().setFromId(loginId).setToId(toId).setActId(0)
				.setActType(type).build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_VALUE;

		final ActionType Reqtype = type;
		final int showToId = toId;
		imSocketManager.sendRequest(weiActionReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {

						IMUserAction.ActionAck imMsgDataAck = null;
						try {
							imMsgDataAck = IMUserAction.ActionAck
									.parseFrom((CodedInputStream) response);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (imMsgDataAck.getRetCode() != 0) {

							throw new RuntimeException(
									"Msg ack error,cause by msgId <=0");
						}

						if (Reqtype == ActionType.ACTION_TYPE_OPEN_PEER_MONITOR) {
							SharedPreferences sp = ctx.getSharedPreferences(
									"WATCHPOSTION" + showToId,
									Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sp.edit();
							editor.putBoolean("ISWATCH", true);
							editor.commit();

						} else if (Reqtype == ActionType.ACTION_TYPE_CLOSE_PEER_MONITOR) {
							SharedPreferences sp = ctx.getSharedPreferences(
									"WATCHPOSTION" + showToId,
									Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sp.edit();
							editor.putBoolean("ISWATCH", false);
							editor.commit();
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
	 * 打开或关闭自己给位有监控
	 * 
	 * @param type
	 * @param toId
	 */
	public void canceMyPostion(ActionType type, int toId) {

		int loginId = IMLoginManager.instance().getLoginId();
		IMUserAction.CommonRequest weiActionReq = IMUserAction.CommonRequest
				.newBuilder().setFromId(loginId).setToId(toId).setActId(0)
				.setActType(type).build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_VALUE;

		final ActionType Reqtype = type;
		final int showToId = toId;
		imSocketManager.sendRequest(weiActionReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {

						IMUserAction.ActionAck imMsgDataAck = null;
						try {
							imMsgDataAck = IMUserAction.ActionAck
									.parseFrom((CodedInputStream) response);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// fail
						if (imMsgDataAck.getRetCode() != 0) {
							throw new RuntimeException("处理失败");
						}

						if (Reqtype == ActionType.ACTION_TYPE_OPEN_MONITOR_PEER) {
							SharedPreferences sp = ctx.getSharedPreferences(
									"MYPOSTION" + showToId,
									Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sp.edit();
							editor.putBoolean("ISWATCH", false);
							editor.commit();

						} else if (Reqtype == ActionType.ACTION_TYPE_CLOSE_MONITOR_PEER) {
							SharedPreferences sp = ctx.getSharedPreferences(
									"MYPOSTION" + showToId,
									Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sp.edit();
							editor.putBoolean("ISWATCH", true);
							editor.commit();
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
	 * 请求获取加位友的请求
	 * 
	 * @param lastUpdateTime
	 */
	private void reqWeiFriends(int lastUpdateTime) {

		int loginId = IMLoginManager.instance().getLoginId();
		IMUserAction.UserActionReq userActionReq = IMUserAction.UserActionReq
				.newBuilder().setFromId(loginId).setLastUpdate(lastUpdateTime)
				.build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_INFOLIST_REQ_VALUE;
		imSocketManager.sendRequest(userActionReq, sid, cid);
	}

	/**
	 * 请求加好友的请求
	 * 
	 * @param userInfo
	 * @param msg_data
	 */
	public void addReqFriends(UserEntity userInfo, String msg_data) {

		int loginId = IMLoginManager.instance().getLoginId();
		IMUserAction.CommonRequest friendsActionReq = IMUserAction.CommonRequest
				.newBuilder().setFromId(loginId).setToId(userInfo.getPeerId())
				.setActId(0).setActType(ActionType.ACTION_TYPE_NEW_FRIEDN)
				.setMsgData(msg_data).build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_VALUE;

		final UserEntity mUserInfo = userInfo;

		imSocketManager.sendRequest(friendsActionReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {

						IMUserAction.ActionAck imMsgDataAck = null;
						try {
							imMsgDataAck = IMUserAction.ActionAck
									.parseFrom((CodedInputStream) response);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// cuowu

						if (imMsgDataAck.getRetCode() == 3) {

							mUserInfo.setFriend(DBConstant.FRIENDS_TYPE_YES);
							dbInterface.insertOrUpdateFriends(mUserInfo);

							UserEntity userInfoInt = ProtoBuf2JavaBean
									.getUserCopyEntity(mUserInfo);
							dbInterface.insertOrUpdateUser(userInfoInt);
							triggerEvent(UserInfoEvent.USER_INFO_UPDATE);

						} else if (imMsgDataAck.getRetCode() != 0) {
							throw new RuntimeException("处理失败");
						} else {

							triggerEvent(UserInfoEvent.USER_INFO_REQ_FRIENDS_SUCCESS); // guanweile
						}

					}

					@Override
					public void onFaild() {
						triggerEvent(UserInfoEvent.USER_INFO_REQ_FRIENDS_FAIL); // guanweile

					}

					@Override
					public void onTimeout() {
						triggerEvent(UserInfoEvent.USER_INFO_REQ_FRIENDS_FAIL); // guanweile

					}
				});
	}

	/**
	 * 请求加位友的请求
	 * 
	 * @param toId
	 */
	public void addWeiFriends(int toId) {

		int loginId = IMLoginManager.instance().getLoginId();
		IMUserAction.CommonRequest weiActionReq = IMUserAction.CommonRequest
				.newBuilder().setFromId(loginId).setToId(toId).setActId(0)
				.setActType(ActionType.ACTION_TYPE_MONITOR).build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_VALUE;

		imSocketManager.sendRequest(weiActionReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {

						IMUserAction.ActionAck imMsgDataAck = null;
						try {
							imMsgDataAck = IMUserAction.ActionAck
									.parseFrom((CodedInputStream) response);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// cuowu
						if (imMsgDataAck.getRetCode() != 0) {
							throw new RuntimeException("处理失败");
						}

						triggerEvent(UserInfoEvent.WEI_FRIENDS_REQ_SUCCESS); // guanweile

					}

					@Override
					public void onFaild() {
						triggerEvent(UserInfoEvent.WEI_FRIENDS_REQ_FAIL); // guanweile

					}

					@Override
					public void onTimeout() {
						triggerEvent(UserInfoEvent.WEI_FRIENDS_REQ_FAIL); // guanweile

					}
				});
	}

	/**
	 * 删除好友的请求
	 * 
	 * @param toId
	 * @param currentUser
	 */
	public void deleteFriends(int toId, final UserEntity currentUser) {

		int loginId = IMLoginManager.instance().getLoginId();
		IMUserAction.CommonRequest deleteReq = IMUserAction.CommonRequest
				.newBuilder().setFromId(loginId).setToId(toId).setActId(0)
				.setActType(ActionType.ACTION_TYPE_DELETE_FRIEND).build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_VALUE;

		imSocketManager.sendRequest(deleteReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {

				IMUserAction.ActionAck imMsgDataAck = null;
				try {
					imMsgDataAck = IMUserAction.ActionAck
							.parseFrom((CodedInputStream) response);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// asdf
				if (imMsgDataAck.getRetCode() == 1) {
					throw new RuntimeException("处理失败");
				}

				if (Utils.isClientType(currentUser)) {

					DeviceEntity rsp = IMDeviceManager.instance()
							.findDeviceCard(currentUser.getPeerId());

					IMContactManager.instance().deleteDevUser(currentUser);

					if (rsp != null) {
						List<FamilyConcernEntity> list = IMDeviceManager
								.instance().findFamilyConcern(
										currentUser.getPeerId());
						for (int i = 0; i < list.size(); i++) {
							if (list.get(i) != null) {
								IMDeviceManager.instance()
										.deleteFamilyConcernEntity(list.get(i));
							}
						}

					}

					List<WhiteEntity> whiteListTemp = IMDeviceManager
							.instance().getWhiteListContactList(
									currentUser.getPeerId());
					for (int i = 0; i < whiteListTemp.size(); i++) {
						if (whiteListTemp.get(i) != null) {
							IMDeviceManager.instance().deleteWhiteUser(
									whiteListTemp.get(i));
						}
					}

					List<WhiteEntity> alarmListTemp = IMDeviceManager
							.instance().getAlarmListContactList(
									currentUser.getPeerId());
					for (int i = 0; i < alarmListTemp.size(); i++) {
						if (alarmListTemp.get(i) != null) {
							IMDeviceManager.instance().deleteAlarmUser(
									alarmListTemp.get(i));
						}
					}

					IMDeviceManager.instance().LoaddAllWhite();
					IMDeviceManager.instance().LoaddAllAlarm();

					IMDeviceManager.instance().deleteFamilyConcernEntity();
					triggerEvent(DeviceEvent.USER_INFO_DELETE_DEVICE_SUCCESS);

				} else {

					if ((currentUser.getUserType() != ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE)
							&& (currentUser.getUserType() != ClientType.CLIENT_TYPE_FISE_CAR_VALUE)) {

						String session = currentUser.getSessionKey();
						imContactManager.deleteFriends(currentUser);

						List<RecentInfo> recentSessionList = IMSessionManager
								.instance().getRecentListInfo();
						for (int i = 0; i < recentSessionList.size(); i++) {
							if (recentSessionList.get(i).getSessionKey()
									.equals(session)) {
								IMSessionManager.instance().reqRemoveSession(
										recentSessionList.get(i),
										DBConstant.SESSION_ALL);
							}
						}
						triggerEvent(UserInfoEvent.USER_INFO_DELETE_SUCCESS); //
					}

				}

			}

			@Override
			public void onFaild() {
				triggerEvent(UserInfoEvent.USER_INFO_DELETE_FAIL); //

			}

			@Override
			public void onTimeout() {
				triggerEvent(UserInfoEvent.USER_INFO_DELETE_FAIL); // guanweile

			}
		});
	}

	/**
	 * 取消位友的请求
	 * 
	 * @param toId
	 * @param currentUser
	 */
	public void confirmXiaoWei(int toId, final UserEntity currentUser) {

		int loginId = IMLoginManager.instance().getLoginId();
		IMUserAction.CommonRequest weiActionReq = IMUserAction.CommonRequest
				.newBuilder().setFromId(loginId).setToId(toId).setActId(0)
				.setActType(ActionType.ACTION_TYPE_CANCEL_MONITOR).build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_VALUE;

		imSocketManager.sendRequest(weiActionReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {

						IMUserAction.ActionAck imMsgDataAck = null;
						try {
							imMsgDataAck = IMUserAction.ActionAck
									.parseFrom((CodedInputStream) response);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// cuowu
						if (imMsgDataAck.getRetCode() == 1) {
							throw new RuntimeException("处理失败");
						}

						if (imMsgDataAck.getRetCode() == 2) {
							triggerEvent(UserInfoEvent.USER_INFO_CALCEL_FOLLOW); // guanweile
							return;
						}

						// asdf
						currentUser.setFriend(DBConstant.FRIENDS_TYPE_YES);

						int buddId = currentUser.getPeerId();
						UserEntity userInfo = ProtoBuf2JavaBean
								.getUserCopyEntity(currentUser);
						UserEntity userFriends = ProtoBuf2JavaBean
								.getUserCopyEntity(currentUser);

						dbInterface.insertOrUpdateUser(userInfo);
						dbInterface.insertOrUpdateFriends(userFriends);
						dbInterface.insertOrDeleteXiaoWeiFriens(currentUser);
						imContactManager.UpdateFriendsContact(buddId);

						triggerEvent(UserInfoEvent.USER_INFO_UPDATE); // guanweile

					}

					@Override
					public void onFaild() {
						triggerEvent(UserInfoEvent.WEI_FRIENDS_REQ_FAIL); // guanweile

					}

					@Override
					public void onTimeout() {
						triggerEvent(UserInfoEvent.WEI_FRIENDS_REQ_FAIL); // guanweile

					}
				});
	}

	/**
	 * 同意请求
	 * 
	 * @param toId
	 * @param act_id
	 * @param actType
	 * @param type
	 * @param weiReq
	 * @param msgData
	 * @param entity
	 */
	public void confirmFriends(int toId, int act_id, int actType,
			ActionResult type, final WeiEntity weiReq, String msgData,
			UserEntity entity) {

		int loginId = IMLoginManager.instance().getLoginId();
		IMUserAction.CommonConfirm weiActionReq = IMUserAction.CommonConfirm
				.newBuilder().setFromId(loginId).setToId(toId).setActId(act_id)
				.setActType(actType).setResult(type).setMsgData(msgData)
				.build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_CONFIRM_VALUE;

		final ActionResult _type = type;
		final int id = toId;
		final int _actType = actType;
		final UserEntity agreeInfo = entity;

		imSocketManager.sendRequest(weiActionReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {

						IMUserAction.ActionAck imMsgDataAck = null;
						try {
							imMsgDataAck = IMUserAction.ActionAck
									.parseFrom((CodedInputStream) response);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						if (imMsgDataAck.getRetCode() != 0) {
							throw new RuntimeException("处理失败");
						}

						// 同意了

						agreeInfo.setFriend(DBConstant.FRIENDS_TYPE_YES);
						dbInterface.insertOrUpdateReqFriens(agreeInfo);

						//

						UserEntity userFriends = ProtoBuf2JavaBean
								.getUserCopyEntity(agreeInfo);
						imContactManager.inserOrFriendsContact(userFriends);
						// dbInterface.insertOrUpdateFriends(userFriends);

						UserEntity userInfo = ProtoBuf2JavaBean
								.getUserCopyEntity(agreeInfo);
						imContactManager.insertOrUpdateUser(userInfo);
						// dbInterface.insertOrUpdateUser(userInfo);

						UserEntity loginUser = IMLoginManager.instance()
								.getLoginInfo();

						String content = "我通过了你的朋友验证请求，现在我们可以开始聊天了";

						AddFriendsMessage textMessage = AddFriendsMessage
								.buildForSend(content, agreeInfo, loginUser, 0);
						textMessage.setStatus(MessageConstant.MSG_SUCCESS);
						long pkId = DBInterface.instance()
								.insertOrUpdateMessage(textMessage);
						IMSessionManager.instance().updateSession(textMessage);

						/**
						 * 发送已读确认由上层的activity处理 特殊处理 1. 未读计数、 通知、session页面 2.
						 * 当前会话
						 * */
						PriorityEvent notifyEvent = new PriorityEvent();
						notifyEvent.event = PriorityEvent.Event.MSG_DEV_MESSAGE;
						notifyEvent.object = textMessage;
						triggerEvent(notifyEvent);

						triggerEvent(UserInfoEvent.USER_INFO_REQ_ALL);

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
	 * 命令(例如抓拍等命令)
	 * 
	 * @param fromId
	 * @param toId
	 * @param type
	 * @param commandType
	 * @param ext_value
	 * @param isShow
	 */
	public void UserP2PCommand(int fromId, int toId, SessionType type,
			CommandType commandType, String ext_value, final boolean isShow) {

		int loginId = IMLoginManager.instance().getLoginId();
		IMUserAction.P2PCommand commandReq = IMUserAction.P2PCommand
				.newBuilder().setFromId(loginId).setToId(toId)
				.setCmdType(commandType).setType(type).setExtValue(ext_value)
				.build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMAND_VALUE;
		final CommandType _commandType = commandType;
		imSocketManager.sendRequest(commandReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {

				IMUserAction.ActionAck imMsgDataAck = null;
				try {
					imMsgDataAck = IMUserAction.ActionAck
							.parseFrom((CodedInputStream) response);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (imMsgDataAck.getRetCode() == 1) { // 当对方离线
					// throw new RuntimeException("处理失败");
					triggerEvent(UserInfoEvent.USER_P2PCOMMAND_OFFLINE);

				} else if (imMsgDataAck.getRetCode() == 0)// 当发送成功
				{
					triggerEvent(UserInfoEvent.USER_P2PCOMMAND_ONLINE);

					if (_commandType == CommandType.COMMAND_TYPE_TAKE_PHOTO) {
						triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_TAKE_PHOTO);
					} else if (_commandType == CommandType.COMMAND_TYPE_SOUND_COPY) {
						triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_SOUND_COPY);

					} else if (_commandType == CommandType.COMMAND_TYPE_DEVICE_CALLBACK) {
						triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_DEVICE_CALLBACK);

					} else if (_commandType == CommandType.COMMAND_TYPE_DEVICE_BILL) {
						triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_DEVICE_BELL);

					} else if (_commandType == CommandType.COMMAND_TYPE_DEVICE_CURRENT_INFO) { // 同步数据
						// adf
						if (isShow) {
							triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_DEVICE_CURRENT);
						}

					} else if (_commandType == CommandType.COMMAND_TYPE_DEVICE_SHUTDOWN) { // 断电控制
						triggerEvent(UserInfoEvent.USER_COMMAND_TYPE_DEVICE_SHUTDOWN);

					}

				} else {
					triggerEvent(UserInfoEvent.USER_P2PCOMMAND_FAIL);
					throw new RuntimeException("处理失败");
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
	 * 同意或者拒绝加位友的请求
	 * 
	 * @param toId
	 * @param act_id
	 * @param actType
	 * @param type
	 * @param weiReq
	 * @param msgData
	 */
	public void confirmWeiFriends(int toId, int act_id, int actType,
			ActionResult type, final WeiEntity weiReq, String msgData) {

		int loginId = IMLoginManager.instance().getLoginId();
		IMUserAction.CommonConfirm weiActionReq = IMUserAction.CommonConfirm
				.newBuilder().setFromId(loginId).setToId(toId).setActId(act_id)
				.setActType(actType).setResult(type).setMsgData(msgData)
				.build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_COMMON_CONFIRM_VALUE;

		final ActionResult _type = type;
		final int id = toId;
		final int _actType = actType;
		imSocketManager.sendRequest(weiActionReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {

						IMUserAction.ActionAck imMsgDataAck = null;
						try {
							imMsgDataAck = IMUserAction.ActionAck
									.parseFrom((CodedInputStream) response);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						if (imMsgDataAck.getRetCode() != 0) {
							throw new RuntimeException("处理失败");
						}

						// 同意了
						if (_type == ActionResult.ACTION_RESULT_YES) {

							UserEntity entity = imContactManager
									.findFriendsContact(id);

							entity.setFriend(DBConstant.FRIENDS_TYPE_WEI);
							UserEntity userWeiEntity = ProtoBuf2JavaBean
									.getUserCopyEntity(entity);

							UserEntity userEntity = imContactManager
									.findContact(id);

							userEntity.setFriend(DBConstant.FRIENDS_TYPE_WEI);

							dbInterface.insertOrDeleteFriens(entity);

							dbInterface.insertOrUpdateUser(userEntity);
							dbInterface
									.insertOrUpdateXiaoWeiFriends(userWeiEntity);

							ArrayList<Integer> userIds = new ArrayList<>(1);
							// just single type
							userIds.add(entity.getPeerId());
							imContactManager.reqGetDetaillUsers(userIds);

							// dbInterface.insertOrDeleteWeiFriens(weiReq);
							weiReq.setStatus(1);
							if (weiReq.getActType() == ActionType.ACTION_TYPE_NEW_FRIEDN
									.ordinal()) {

								dbInterface.insertOrUpdateUserReqFriens(weiReq);// (weiReq);
								userFriendsMap.put(weiReq.getFromId(), weiReq);

							} else if (weiReq.getActType() == ActionType.ACTION_TYPE_MONITOR
									.ordinal()) {
								dbInterface.insertOrUpdateWeiFriens(weiReq);// (weiReq);
								userWeiMap.put(weiReq.getFromId(), weiReq);
							}

							triggerEvent(UserInfoEvent.USER_INFO_UPDATE); // guanweile

						} else if (_type == ActionResult.ACTION_RESULT_NO) {

							// dbInterface.insertOrDeleteWeiFriens(weiReq);
							weiReq.setStatus(1);
							// dbInterface.insertOrUpdateWeiFriens(weiReq);
							// reqList.add(weiReq);
							if (weiReq.getActType() == ActionType.ACTION_TYPE_NEW_FRIEDN
									.ordinal()) {

								dbInterface.insertOrUpdateUserReqFriens(weiReq);// (weiReq);
								userFriendsMap.put(weiReq.getFromId(), weiReq);

							} else if (weiReq.getActType() == ActionType.ACTION_TYPE_MONITOR
									.ordinal()) {
								dbInterface.insertOrUpdateWeiFriens(weiReq);// (weiReq);
								userWeiMap.put(weiReq.getFromId(), weiReq);
							}

							triggerEvent(UserInfoEvent.USER_INFO_UPDATE); // guanweile

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
	 * 手机通讯录好友查询是否注册小位
	 * 
	 * @param from_id
	 * @param list
	 */
	public void onPhoneBookFriendReq(int from_id, List<String> list) { // ,

		IMUserAction.DiscoverFriendReq imDiscoverFriendReq = IMUserAction.DiscoverFriendReq
				.newBuilder().setFromId(from_id).addAllPhoneList(list).build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_USERACTION_DISCOVERY_FRIEND_REQ_VALUE;

		imSocketManager.sendRequest(imDiscoverFriendReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {

						IMUserAction.DiscoverFriendRsp DiscoverFriendRsp;
						try {
							DiscoverFriendRsp = IMUserAction.DiscoverFriendRsp
									.parseFrom((CodedInputStream) response);
							onDiscoverFriendRsp(DiscoverFriendRsp);

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

					@Override
					public void onFaild() {
						triggerEvent(UserInfoEvent.USER_PHONE_FAIL);
						// 注册失败
					}

					@Override
					public void onTimeout() {
						triggerEvent(UserInfoEvent.USER_PHONE_FAIL);
						// 注册超时
					}
				});

	}

	/**
	 * 通讯录查询回复
	 * 
	 * @param imDiscoverFriendRsp
	 */
	public void onDiscoverFriendRsp(
			IMUserAction.DiscoverFriendRsp imDiscoverFriendRsp) {

		int code = imDiscoverFriendRsp.getRetCode(); // getActId
		if (code != 0) {
			triggerEvent(UserInfoEvent.USER_PHONE_FAIL);
			return;
		}

		phoneList.clear();
		List<IMBaseDefine.UserInfo> changeList = imDiscoverFriendRsp
				.getUserListList();
		for (IMBaseDefine.UserInfo userInfo : changeList) {
			UserEntity entity = ProtoBuf2JavaBean.getUserEntity(userInfo);
			IMContactManager.instance().insertOrUpdateUser(entity);
			phoneList.add(entity);
		}
		triggerEvent(UserInfoEvent.USER_PHONE_SUCCESS);

	}

	public ArrayList<UserEntity> getPhoneUserList() {

		return phoneList;
	}

	public List<PhoneMemberBean> getPhoneMemberBeanList() {
		return sourceList;
	}

	public void setPhoneMemberBeanList(List<PhoneMemberBean> sourceList) {
		this.sourceList = sourceList;
	}

	/**
	 * 自身的事件驱动
	 * 
	 * @param event
	 */
	public void triggerEvent(Object event) {
		EventBus.getDefault().post(event);
	}

	/**
	 * 请求位置信息
	 * 
	 * @param userId
	 * @param statTime
	 * @param endTime
	 */
	public void onRepLocationReq(int userId, long statTime, long endTime) {
		int usrId = IMLoginManager.instance().getLoginId();

		IMUserAction.LocationReq imLocation = IMUserAction.LocationReq
				.newBuilder().setUserId(userId).setFromId(usrId)
				.setStartTime(((int) statTime)).setEndTime(((int) endTime))
				.build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.UserActionCmdID.CID_LOCATION_REQ_VALUE;

		imSocketManager.sendRequest(imLocation, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMUserAction.LocationRsp LocationRsp = IMUserAction.LocationRsp
							.parseFrom((CodedInputStream) response);
					onLocationRsp(LocationRsp);

				} catch (IOException e) {
					triggerEvent(LoginEvent.REGIST_INNER_FAILED);
					// 注册失败
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(UserInfoEvent.USER_INFO_UPDATE_QUERY_FAIL);
				// 注册失败
			}

			@Override
			public void onTimeout() {
				triggerEvent(UserInfoEvent.USER_INFO_UPDATE_QUERY_FAIL);
				// 注册超时
			}
		});
	}

	/**
	 * 请求位置回复
	 * 
	 * @param imLocationRsp
	 */
	public void onLocationRsp(IMUserAction.LocationRsp imLocationRsp) {

		lat.clear();
		int fromId = imLocationRsp.getFromId();
		int userId = imLocationRsp.getUserId();
		List<UserLocation> Location = imLocationRsp.getInfoListList();
		for (int i = 0; i < Location.size(); i++) {
			double latLng = Double.valueOf(Location.get(i).getLat());
			double Lng = Double.valueOf(Location.get(i).getLng());

			LatLng postion = new LatLng(latLng, Lng);
			lat.add(postion);
		}

		triggerEvent(UserInfoEvent.USER_INFO_UPDATE_QUERY_SUCCESS);

	}

	public List<LatLng> onLocation() {
		return lat;
	}

	/**
	 * 位置信息回复
	 * 
	 * @param imLocationRsp
	 */
	public void onRepLocationRsp(IMUserAction.LocationRsp imLocationRsp) {

		int fromId = imLocationRsp.getFromId();
		int userId = imLocationRsp.getUserId();
		List<UserLocation> Location = imLocationRsp.getInfoListList();

		List<UserEntity> list = imContactManager.getContactWeiFriendsList();

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getPeerId() == userId) {
				if (Location.size() <= 0) {
					list.get(i).setLongitude(0.0);
					list.get(i).setLongitude(0.0);
					list.get(i).setLoseMonitor(false);

					imContactManager.UpdateWeiFriendsContact(list.get(i));
				} else {

					String lnt = Location.get(0).getLng();
					String lat = Location.get(0).getLat();

					if (lnt == "") {
						list.get(i).setLongitude(0.0);
					} else {
						list.get(i).setLongitude(Double.parseDouble(lnt));
					}

					if (lat == "") {
						list.get(i).setLatitude(0.0);
					} else {
						list.get(i).setLatitude(Double.parseDouble(lat));
					}

					list.get(i).setLoseMonitor(true);

					imContactManager.UpdateWeiFriendsContact(list.get(i));
				}
			}
		}

		triggerEvent(UserInfoEvent.USER_INFO_UPDATE_WEIFRIENDS);

	}

	/**
	 * 接收到同意获取拒绝通知
	 * 
	 * @param imWeiRsp
	 */
	public void onRepConfirmRequest(IMUserAction.CommonConfirm imWeiRsp) {
		int act_it = imWeiRsp.getActId(); // getActId

		if (imWeiRsp.getActType() == ActionType.ACTION_TYPE_NEW_FRIEDN
				.ordinal()) {

			UserEntity entity = imContactManager.findContact(imWeiRsp
					.getFromId());

			entity.setFriend(DBConstant.FRIENDS_TYPE_YES);
			imContactManager.inserOrFriendsContact(entity);

			UserEntity userInfoInt = ProtoBuf2JavaBean
					.getUserCopyEntity(entity);
			imContactManager.insertOrUpdateUser(entity);

			UserEntity loginUser = IMLoginManager.instance().getLoginInfo();

			String content = "我通过了你的朋友验证请求，现在我们可以开始聊天了";

			AddFriendsMessage textMessage = AddFriendsMessage.buildForSend(
					content, entity, loginUser, 0);

			textMessage.setStatus(MessageConstant.MSG_SUCCESS);
			long pkId = DBInterface.instance().insertOrUpdateMessage(
					textMessage);
			IMSessionManager.instance().updateSession(textMessage);

			/**
			 * 发送已读确认由上层的activity处理 特殊处理 1. 未读计数、 通知、session页面 2. 当前会话
			 * */
			PriorityEvent notifyEvent = new PriorityEvent();
			notifyEvent.event = PriorityEvent.Event.MSG_DEV_MESSAGE;
			notifyEvent.object = textMessage;
			triggerEvent(notifyEvent);

			triggerEvent(UserInfoEvent.USER_INFO_UPDATE);

		} else if (imWeiRsp.getActType() == ActionType.ACTION_TYPE_MONITOR
				.ordinal()) {
			if (imWeiRsp.getResult() == ActionResult.ACTION_RESULT_YES) {

				int id = imWeiRsp.getFromId();

				UserEntity entity = imContactManager.findFriendsContact(id);
				entity.setFriend(DBConstant.FRIENDS_TYPE_WEI);
				UserEntity userWeiEntity = ProtoBuf2JavaBean
						.getUserCopyEntity(entity);

				UserEntity userEntity = imContactManager.findContact(id);

				userEntity.setFriend(DBConstant.FRIENDS_TYPE_WEI);

				imContactManager.insertOrUpdateUser(userEntity);
				imContactManager.deleteOrFriendsContact(entity);
				imContactManager.inserWeiFriendsContact(userWeiEntity);

				ArrayList<Integer> userIds = new ArrayList<>(1);
				// just single type
				userIds.add(entity.getPeerId());
				imContactManager.reqGetDetaillUsers(userIds);

				triggerEvent(UserInfoEvent.USER_INFO_UPDATE); // guanweile

			}
		}

	}

	/**
	 * 接收命令请求 (抓拍等命令)
	 * 
	 * @param commandRsp
	 */
	public void onP2PCommandRequest(IMUserAction.P2PCommand commandRsp) {

		CommandType cmd_type = commandRsp.getCmdType();
		if (cmd_type == CommandType.COMMAND_TYPE_TAKE_PHOTO)// 拍照
		{

			Intent loadImageIntent = new Intent(ctx, LocalService.class);
			loadImageIntent.putExtra(IntentConstant.KEY_PEERID,
					commandRsp.getFromId());
			// loadImageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ctx.startService(loadImageIntent);

		} else if (cmd_type == CommandType.COMMAND_TYPE_SOUND_COPY)// 录音
		{

			if (AudioPlayerHandler.getInstance().isPlaying())
				AudioPlayerHandler.getInstance().stopPlayer();

			audioId = commandRsp.getFromId();
			audioSavePath = CommonUtil.getAudioSavePath(commandRsp.getFromId());

			audioRecorderInstance = new AudioRecordHandler(audioSavePath);
			audioRecorderThread = new Thread(audioRecorderInstance);
			audioRecorderInstance.setRecording(true);
			audioRecorderThread.start();
			time.start();
		}
	}

	/**
	 * 接收到请求通知
	 * 
	 * @param imWeiRsp
	 */
	public void onRepFriendRequest(IMUserAction.CommonRequest imWeiRsp) {

		int peeid = imWeiRsp.getFromId();
		int toId = imWeiRsp.getToId();
		int act_id = imWeiRsp.getActId();
		int act_type = imWeiRsp.getActType().ordinal();

		if (imWeiRsp.getActType() == ActionType.ACTION_TYPE_CANCEL_MONITOR) {

			UserEntity currentUser = imContactManager.findXiaoWeiContact(peeid);
			if (currentUser != null) {
				currentUser.setFriend(DBConstant.FRIENDS_TYPE_YES);
				int buddId = currentUser.getPeerId();
				UserEntity userInfo = ProtoBuf2JavaBean
						.getUserCopyEntity(currentUser);
				UserEntity userFriends = ProtoBuf2JavaBean
						.getUserCopyEntity(currentUser);

				dbInterface.insertOrUpdateUser(userInfo);
				dbInterface.insertOrUpdateFriends(userFriends);
				dbInterface.insertOrDeleteXiaoWeiFriens(currentUser);
				imContactManager.UpdateFriendsContact(buddId);
			}

			triggerEvent(UserInfoEvent.USER_INFO_UPDATE); // guanweile

		} else if (imWeiRsp.getActType() == ActionType.ACTION_TYPE_NEW_FRIEDN) {

			UserEntity loginUser = IMLoginManager.instance().getLoginInfo();

			if (loginUser.getFriendNeedAuth() == 0) {
				ArrayList<Integer> userIds = new ArrayList<>(1);
				// just single type
				userIds.add(peeid);
				reqGetDetaillUserFriends(userIds, 0);

			} else {

				ArrayList<Integer> userIds = new ArrayList<>(1);
				// just single type
				userIds.add(peeid);
				reqGetDetaillUserFriends(userIds, 1);

				// 好友请求列表数据
				int timeNow = (int) (System.currentTimeMillis() / 1000);

				WeiEntity weiEntity = findUserReqEntity(peeid);
				if (weiEntity == null) {

					weiEntity = new WeiEntity();
					weiEntity.setFromId(peeid);
					weiEntity.setToId(toId);
					weiEntity.setActId(act_id);
					weiEntity.setActType(act_type);
					weiEntity.setStatus(0);
					weiEntity.setUpdated(timeNow);
					weiEntity.setMasgData(imWeiRsp.getMsgData());
				} else {

					weiEntity.setFromId(peeid);
					weiEntity.setToId(toId);
					weiEntity.setActId(act_id);
					weiEntity.setActType(act_type);
					weiEntity.setStatus(0);
					weiEntity.setUpdated(timeNow);
					weiEntity.setMasgData(imWeiRsp.getMsgData());
				}

				dbInterface.insertOrUpdateUserReqFriens(weiEntity);// (weiReq);
				userFriendsMap.put(weiEntity.getFromId(), weiEntity);

				// 位友请求标注
				ReqFriendsEntity reqFriends = ProtoBuf2JavaBean
						.getUnreadFriendsEntity(peeid, 0, 0);
				IMUnreadMsgManager.instance().updateUnReqFriends(reqFriends);

				ReqEvent reqEvent = new ReqEvent();
				reqEvent.event = ReqEvent.Event.REQ_FRIENDS_MESSAGE;
				reqEvent.entity = weiEntity;
				triggerEvent(reqEvent);

				triggerEvent(UserInfoEvent.WEI_FRIENDS_INFO_REQ_ALL);

			}

		} else if (imWeiRsp.getActType() == ActionType.ACTION_TYPE_MONITOR) {

			// 好友请求列表
			WeiEntity entity = findWeiEntity(peeid);
			if (entity == null) {
				entity = new WeiEntity();
				int timeNow = (int) (System.currentTimeMillis() / 1000);

				entity.setFromId(peeid);
				entity.setToId(toId);
				entity.setActId(act_id);
				entity.setActType(act_type);
				entity.setStatus(0);
				entity.setUpdated(timeNow);
				entity.setMasgData(imWeiRsp.getMsgData());

			} else {

				int timeNow = (int) (System.currentTimeMillis() / 1000);

				entity.setFromId(peeid);
				entity.setToId(toId);
				entity.setActId(act_id);
				entity.setActType(act_type);
				entity.setStatus(0);
				entity.setUpdated(timeNow);
				entity.setMasgData(imWeiRsp.getMsgData());
			}

			dbInterface.insertOrUpdateWeiFriens(entity);
			userWeiMap.put(entity.getFromId(), entity);

			// 好友请求 标注(数字)
			ReqFriendsEntity reqFriends = ProtoBuf2JavaBean
					.getUnreadFriendsEntity(peeid, 0, 0);
			IMUnreadMsgManager.instance().updateUnReqFriends(reqFriends);

			triggerEvent(UserInfoEvent.WEI_FRIENDS_WEI_REQ_ALL);

			ReqEvent reqEvent = new ReqEvent();
			reqEvent.event = ReqEvent.Event.REQ_WEI_MESSAGE;
			reqEvent.entity = entity;
			triggerEvent(reqEvent);
		}

	}

	/**
	 * 请求用户详细信息
	 * 
	 * @param userIds
	 */
	public void reqGetDetaillUserFriends(ArrayList<Integer> userIds, int isAuth) {
		logger.i("contact#contact#reqGetDetaillUsers");
		if (null == userIds || userIds.size() <= 0) {
			logger.i("contact#contact#reqGetDetaillUsers return,cause by null or empty");
			return;
		}
		int loginId = IMLoginManager.instance().getLoginId();
		IMBuddy.IMUsersInfoReq imUsersInfoReq = IMBuddy.IMUsersInfoReq
				.newBuilder().setUserId(loginId).addAllUserIdList(userIds)
				.build();

		int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
		int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_USER_INFO_REQUEST_VALUE;

		final int _isAuth = isAuth;
		imSocketManager.sendRequest(imUsersInfoReq, sid, cid,
				new Packetlistener() {
					@Override
					public void onSuccess(Object response) {
						try {

							IMBuddy.IMUsersInfoRsp imAllUserRsp = IMBuddy.IMUsersInfoRsp
									.parseFrom((CodedInputStream) response);
							onRepDetailUsersReq(imAllUserRsp, _isAuth);

						} catch (IOException e) {
							triggerEvent(UserInfoEvent.USER_INFO_DATA_FAIL);

							logger.e("login failed,cause by %s", e.getCause());
						}
					}

					@Override
					public void onFaild() {
						triggerEvent(UserInfoEvent.USER_INFO_DATA_FAIL);

					}

					@Override
					public void onTimeout() {
						triggerEvent(UserInfoEvent.USER_INFO_DATA_FAIL);

					}
				});

	}

	/**
	 * 好友请求的个人信息
	 * 
	 * @param imUsersInfoRsp
	 * @param _isAuth
	 */
	public void onRepDetailUsersReq(IMBuddy.IMUsersInfoRsp imUsersInfoRsp,
			int _isAuth) {

		List<IMBaseDefine.UserInfo> userInfoList = imUsersInfoRsp
				.getUserInfoListList();

		if (_isAuth == 0) {

			ArrayList<UserEntity> needDb = new ArrayList<>();
			ArrayList<UserEntity> friendsDbReq = new ArrayList<>();

			for (IMBaseDefine.UserInfo userInfo : userInfoList) {

				UserEntity entity = ProtoBuf2JavaBean.getUserEntity(userInfo);
				entity.setFriend(DBConstant.FRIENDS_TYPE_YES);

				friendsDbReq.add(entity);

				UserEntity entityUser = ProtoBuf2JavaBean
						.getUserCopyEntity(entity);
				needDb.add(entityUser);
				imContactManager.inserOrFriendsContact(entity);
				imContactManager.insertOrUpdateUser(entityUser);

			}

			triggerEvent(UserInfoEvent.USER_INFO_UPDATE); //

		} else {
			for (IMBaseDefine.UserInfo userInfo : userInfoList) {

				UserEntity entity = ProtoBuf2JavaBean.getUserEntity(userInfo);
				UserEntity entityUser = ProtoBuf2JavaBean
						.getUserCopyEntity(entity);
				dbInterface.insertOrUpdateReqFriens(entity);
				imContactManager.insertOrUpdateUser(entityUser);

				int UserId = entity.getPeerId();
				ReqFriendsEntity reqFriends = ProtoBuf2JavaBean
						.getUnreadFriendsEntity(UserId, 0, 0);
				IMUnreadMsgManager.instance().updateUnReqFriends(reqFriends);
			}

			triggerEvent(UserInfoEvent.USER_INFO_REQ_UPDATE);
		}

	}

	/**
	 * 位友请求回复
	 * 
	 * @param imWeiRsp
	 */
	public void onRepWeiFriends(IMUserAction.UserActionRsp imWeiRsp) {

		int actCnt = imWeiRsp.getActCnt();
		if (actCnt <= 0) {
			return;
		}

		ArrayList<WeiEntity> needDb = new ArrayList<>();
		ArrayList<UserEntity> needDbReq = new ArrayList<>();
		ArrayList<ReqFriendsEntity> reqUnFriends = new ArrayList<>();

		List<UserActionInfo> changeList = imWeiRsp.getActionListList();

		for (UserActionInfo weiInfo : changeList) {

			// 自己发出去的消息不作处理
			if (weiInfo.getFromId() == IMLoginManager.instance().getLoginId()) {
				return;
			}

			if (weiInfo.getActType() == ActionType.ACTION_TYPE_NEW_FRIEDN
					.ordinal()) {

				ReqFriendsEntity reqFriends = IMUnreadMsgManager.instance()
						.findUnFriendsMap(weiInfo.getFromId());

				if (reqFriends == null) {
					ReqFriendsEntity reqUnMessage = ProtoBuf2JavaBean
							.getUnreadFriendsEntity(weiInfo.getFromId(), 0, 0);
					IMUnreadMsgManager.instance().updateUnReqFriends(
							reqUnMessage);
				} else {

					reqFriends.setMessageReq(1);
					reqFriends.setTableReq(1);

					IMUnreadMsgManager.instance()
							.updateUnReqFriends(reqFriends);
				}

				dbInterface.batchInsertOrUpdateReq(needDbReq);
				// dbInterface.batchInsertOrUpdateReqFirends(reqUnFriends);

				// 好友请求列表数据
				int timeNow = (int) (System.currentTimeMillis() / 1000);

				WeiEntity weiEntity = findUserReqEntity(weiInfo.getFromId());
				if (weiEntity == null) {

					weiEntity = new WeiEntity();
					weiEntity.setFromId(weiInfo.getFromId());
					weiEntity.setToId(weiInfo.getToId());
					weiEntity.setActId(weiInfo.getActId());
					weiEntity.setActType(weiInfo.getActType());
					weiEntity.setStatus(0);
					weiEntity.setUpdated(timeNow);
					weiEntity.setMasgData(weiInfo.getMsgData());
				} else {

					weiEntity.setFromId(weiInfo.getFromId());
					weiEntity.setToId(weiInfo.getToId());
					weiEntity.setActId(weiInfo.getActId());
					weiEntity.setActType(weiInfo.getActType());
					weiEntity.setStatus(0);
					weiEntity.setUpdated(timeNow);
					weiEntity.setMasgData(weiInfo.getMsgData());
				}

				dbInterface.insertOrUpdateUserReqFriens(weiEntity);// (weiReq);
				userFriendsMap.put(weiEntity.getFromId(), weiEntity);

				triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
				if (reqUnFriends.size() > 0) {
					triggerEvent(UserInfoEvent.USER_INFO_REQ_UPDATE);
				}

			} else if (weiInfo.getActType() == ActionType.ACTION_TYPE_MONITOR
					.ordinal()) {

				WeiEntity entity = findWeiEntity(weiInfo.getFromId());
				if (entity == null) {
					entity = new WeiEntity();
					int timeNow = (int) (System.currentTimeMillis() / 1000);

					entity.setFromId(weiInfo.getFromId());
					entity.setToId(weiInfo.getToId());
					entity.setActId(weiInfo.getActId());
					entity.setActType(weiInfo.getActType());
					entity.setStatus(0);
					entity.setUpdated(timeNow);
					entity.setMasgData(weiInfo.getMsgData());

				} else {

					int timeNow = (int) (System.currentTimeMillis() / 1000);

					entity.setFromId(weiInfo.getFromId());
					entity.setToId(weiInfo.getToId());
					entity.setActId(weiInfo.getActId());
					entity.setActType(weiInfo.getActType());
					entity.setStatus(0);
					entity.setUpdated(timeNow);
					entity.setMasgData(weiInfo.getMsgData());
				}

				dbInterface.insertOrUpdateWeiFriens(entity);
				userWeiMap.put(entity.getFromId(), entity);

				ReqFriendsEntity reqWeiFriends = IMUnreadMsgManager.instance()
						.findUnFriendsMap(weiInfo.getFromId());

				if (reqWeiFriends == null) {
					ReqFriendsEntity reqFriends = ProtoBuf2JavaBean
							.getUnreadFriendsEntity(weiInfo.getFromId(), 0, 0);
					IMUnreadMsgManager.instance()
							.updateUnReqFriends(reqFriends);
				} else {
					reqWeiFriends.setMessageReq(1);
					reqWeiFriends.setTableReq(1);

					IMUnreadMsgManager.instance().updateUnReqFriends(
							reqWeiFriends);
				}
			}

		}

		triggerEvent(UserInfoEvent.WEI_FRIENDS_INFO_REQ_ALL);
	}

	/**
	 * 查询位友请求的数据
	 * 
	 * @param buddyId
	 * @return
	 */
	public WeiEntity findWeiEntity(int buddyId) {
		if (buddyId > 0 && userWeiMap.containsKey(buddyId)) {
			return userWeiMap.get(buddyId);
		}
		return null;
	}

	public WeiEntity findUserReqEntity(int buddyId) {
		if (buddyId > 0 && userFriendsMap.containsKey(buddyId)) {
			return userFriendsMap.get(buddyId);
		}
		return null;
	}

	/**
	 * 返回位友请求数据
	 * 
	 * @return
	 */
	public List<WeiEntity> getReqWeiList() {
		List<WeiEntity> contactList = new ArrayList<>(userWeiMap.values());
		return contactList;
	}

	/**
	 * 返回好友请求数据
	 * 
	 * @return
	 */
	public List<WeiEntity> getReqFriendsList() {
		List<WeiEntity> contactList = new ArrayList<>(userFriendsMap.values());
		return contactList;
	}

	/**
	 * 删除好友/位友 请求
	 * 
	 * @param type
	 * @param entity
	 */
	public void deleteReqFriends(int type, WeiEntity entity) {
		if (type == 1) {
			dbInterface.insertOrDeleteWeiFriens(entity);// (weiReq);
			userWeiMap.remove(entity.getFromId());

		} else if (type == 2) {
			dbInterface.insertOrDeleteUserFriends(entity);
			userFriendsMap.remove(entity.getFromId());

		}

		triggerEvent(UserInfoEvent.WEI_FRIENDS_WEI_REQ_ALL);
	}

	/**
	 * 实现自身的事件驱动
	 * 
	 * @param event
	 */
	public void triggerReqEvent(ReqFriendsEvent event) {
		EventBus.getDefault().postSticky(event);
	}

	//
	// /**------------------------部门相关的协议 end------------------------------*/

	/** -----------------------实体 get set 定义----------------------------------- */

	public boolean isUserDataReady() {
		return userDataReady;
	}

	/**
	 * 定时器 用于时间计算(短信60s到计时)
	 * 
	 * @author weileiguan
	 * 
	 */
	class AudioTimeCount extends CountDownTimer {

		public AudioTimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {

		}

		@Override
		public void onFinish() {
			if (audioRecorderInstance.isRecording()) {
				audioRecorderInstance.setRecording(false);
			}

			UserEntity loginUser = IMLoginManager.instance().getLoginInfo();
			UserEntity peerEntity = IMContactManager.instance()
					.findXiaoWeiContact(audioId);
			AudioMessage audioMessage = AudioMessage.buildForSend(
					audioRecorderInstance.getRecordTime(), audioSavePath,
					loginUser, peerEntity, "");
			IMMessageManager.instance().sendAuthVoice(audioMessage);

		}
	}

}

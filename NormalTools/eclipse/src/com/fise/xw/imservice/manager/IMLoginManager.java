package com.fise.xw.imservice.manager;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
 
import android.text.TextUtils;
import android.util.Log;

import com.google.protobuf.CodedInputStream;
import com.fise.xw.DB.DBInterface; 
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.sp.LoginSp;
import com.fise.xw.DB.sp.RegistSp; 
import com.fise.xw.config.UrlConstant;
import com.fise.xw.imservice.callback.Packetlistener;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.event.ReqFriendsEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.protobuf.IMBaseData;
import com.fise.xw.protobuf.IMBaseData.AddressCity;
import com.fise.xw.protobuf.IMBaseData.AddressProvince;
import com.fise.xw.protobuf.IMBaseData.AddressType;
import com.fise.xw.protobuf.IMBaseDefine; 
import com.fise.xw.protobuf.IMBuddy;
import com.fise.xw.protobuf.IMLogin;
import com.fise.xw.protobuf.IMRegist;
import com.fise.xw.protobuf.IMSms;
import com.fise.xw.protobuf.IMSms.SmsActionType;
import com.fise.xw.protobuf.helper.ProtoBuf2JavaBean; 
import com.fise.xw.ui.activity.RegistActivity;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;

/**
 * 很多情况下都是一种权衡 登陆控制
 * 
 * @yingmu
 */
public class IMLoginManager extends IMManager {
	private Logger logger = Logger.getLogger(IMLoginManager.class);

	/** 单例模式 */
	private static IMLoginManager inst = new IMLoginManager();

	public static IMLoginManager instance() {
		return inst;
	}

	public String loginError = "";

	private List<AddressProvince> province_list;
	private List<AddressCity> city_list;

	public IMLoginManager() {
		logger.d("login#creating IMLoginManager");
	}

	IMSocketManager imSocketManager = IMSocketManager.instance();

	/** 登陆参数 以便重试 */
	private String loginUserName;
	private String loginPwd;
	private int loginId;
	private UserEntity loginInfo;
	private String imei;
	private String mobile;

	// private UserEntity searchInfo;

	/** loginManger 自身的状态 todo 状态太多就采用enum的方式 */
	private boolean identityChanged = false;
	private boolean isKickout = false;
	private boolean isPcOnline = false;
	// 以前是否登陆过，用户重新登陆的判断
	private boolean everLogined = false;
	// 本地包含登陆信息了[可以理解为支持离线登陆了]
	private boolean isLocalLogin = false;
	private boolean isSms = false;

	private LoginEvent loginStatus = LoginEvent.NONE;

	public boolean getLocalLogin() {
		return isLocalLogin;
	}

	/**
	 * -------------------------------功能方法--------------------------------------
	 */

	@Override
	public void doOnStart() {

	}

	@Override
	public void reset() {
		loginUserName = null;
		loginPwd = null;
		loginId = -1;
		loginInfo = null;
		imei = null;

		identityChanged = false;
		isKickout = false;
		isPcOnline = false;
		everLogined = false;
		loginStatus = LoginEvent.NONE;
		isLocalLogin = false;
		isSms = false;

	}

	public String getLoginUserName() {
		return loginUserName;
	}

	public String getLoginUserPwd() {
		return loginPwd;
	}

	public String getLoginUserImei() {
		return imei;
	}

	/**
	 * 实现自身的事件驱动
	 * 
	 * @param event
	 */
	public void triggerEvent(LoginEvent event) {
		loginStatus = event;
		EventBus.getDefault().postSticky(event);
	}

	/**
	 * 实现自身的事件驱动
	 * 
	 * @param event
	 */
	public void triggerReqEvent(ReqFriendsEvent event) {
		EventBus.getDefault().postSticky(event);
	}

	/**
	 * if not login, do nothing send logOuting message, so reconnect won't react
	 * abnormally but when reconnect start to work again?use isEverLogined close
	 * the socket send logOuteOk message mainactivity jumps to login page
	 * 
	 */
	public void logOut() {
		logger.d("login#logOut");
		logger.d("login#stop reconnecting");
		// everlogined is enough to stop reconnecting
		everLogined = false;
		isLocalLogin = false;
		isSms = false;
		reqLoginOut();
	}

	
	/**
	 * 修改位置信息 (个人信息的国家/地区 城市等)
	 * @param from_user
	 * @param country
	 * @param province
	 * @param city
	 */
	public void AddressRequest(int from_user, String country, String province,
			String city) {

		IMBuddy.IMChangeAddressReq address = IMBuddy.IMChangeAddressReq
				.newBuilder().setFromUser(from_user).setCountry(country)
				.setProvince(province).setCity(city).build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_CHANGE_ADDRESS_REQ_VALUE; // CID_BUDDY_CHANGE_ADDRESS_REQ_VALUE

		final String showCountry = country;
		final String showProvince = province;
		final String showCity = city;

		imSocketManager.sendRequest(address, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMBuddy.IMChangeAddressRsp imAddressRes = IMBuddy.IMChangeAddressRsp
							.parseFrom((CodedInputStream) response);
					onRepAddressResponse(imAddressRes, showCountry,
							showProvince, showCity);
				} catch (IOException e) {
					triggerEvent(LoginEvent.INFO_ADDRESS_FAILED);
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(LoginEvent.INFO_ADDRESS_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(LoginEvent.INFO_ADDRESS_FAILED);
			}
		});
	}

	
	/**
	 * 修改位置信息(个人信息的国家/地区 城市等)  回复
	 * @param address
	 * @param country
	 * @param province
	 * @param city
	 */
	public void onRepAddressResponse(IMBuddy.IMChangeAddressRsp address,
			String country, String province, String city) {
		if (address == null) {
			logger.e("login#decode LoginResponse failed");
			triggerEvent(LoginEvent.INFO_ADDRESS_FAILED);
			return;
		}

		int code = address.getResultCode();
		switch (code) {
		case 0: {

			UserEntity login = IMLoginManager.instance().getLoginInfo();
			login.setCountry(country);
			login.setProvince(province);
			login.setCity(city);

			IMLoginManager.instance().setLoginInfo(login);

			UserEntity currentUser = IMContactManager.instance().findContact(
					login.getPeerId());
			if (currentUser != null) {

				currentUser.setCountry(login.getCountry());
				currentUser.setProvince(login.getProvince());
				currentUser.setCity(login.getCity());
				IMContactManager.instance().insertOrUpdateUser(currentUser);
			}
			triggerEvent(UserInfoEvent.USER_INFO_DATA_UPDATE);
		}
			break;

		default: {
			triggerEvent(LoginEvent.INFO_ADDRESS_FAILED);
		}
			break;
		}
	}

	/**
	 * @param event
	 */
	public void triggerEvent(UserInfoEvent event) {
		// 先更新自身的状态
		EventBus.getDefault().postSticky(event);
	}

	
	
	/**
	 * 获取地址信息 (个人信息的国家/地区 城市等)
	 * @param from_user
	 * @param last_update
	 * @param data_type
	 * @param address_id
	 */
	public void AddressRequest(int from_user, int last_update,
			AddressType data_type, int address_id) {

		IMBaseData.IMAddressRequest address = IMBaseData.IMAddressRequest
				.newBuilder().setFromUser(from_user).setLastUpdate(last_update)
				.setDataType(data_type).setAddressId(address_id).build();

		int sid = IMBaseDefine.ServiceID.SID_USER_VALUE;
		int cid = IMBaseDefine.BaseDataCmdID.CID_BASE_DATA_ADDRESS_REQ_VALUE;

		final AddressType type = data_type;
		imSocketManager.sendRequest(address, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMBaseData.IMAddressResponse imLoginRes = IMBaseData.IMAddressResponse
							.parseFrom((CodedInputStream) response);
					onRepAddressResponse(imLoginRes, type);
				} catch (IOException e) {
					triggerEvent(LoginEvent.INFO_CITY_FAILED);
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(LoginEvent.INFO_CITY_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(LoginEvent.INFO_CITY_FAILED);
			}
		});

	}

	/**
	 * 获取位置信息(个人信息的国家/地区 城市等)  回复
	 * @param address
	 * @param data_type
	 */
	public void onRepAddressResponse(IMBaseData.IMAddressResponse address,
			AddressType data_type) {

		if (address == null) {
			logger.e("login#decode LoginResponse failed");
			triggerEvent(LoginEvent.INFO_CITY_FAILED);
			return;
		}

		IMBaseDefine.ResultType code = address.getResultCode();
		switch (code) {
		case REFUSE_REASON_NONE: {
			if (data_type == AddressType.ADDRESS_TYPE_PROVINCE) {
				List<AddressProvince> provinceList = address
						.getProvinceListList();
				province_list = provinceList;

				triggerEvent(LoginEvent.INFO_PROVINCE_SUCCESS);
			} else if (data_type == AddressType.ADDRESS_TYPE_CITY) {
				List<AddressCity> cityList = address.getCityListList();
				city_list = cityList;

				triggerEvent(LoginEvent.INFO_CITY_SUCCESS);
			}

		}
			break;

		case REFUSE_REASON_DB_VALIDATE_FAILED: {
			triggerEvent(LoginEvent.INFO_CITY_FAILED);
		}
			break;

		default: {
			triggerEvent(LoginEvent.INFO_CITY_FAILED);
		}
			break;
		}

	}

	public List<AddressProvince> provinceList() {
		return province_list;
	}

	public List<AddressCity> CityList() {
		return city_list;
	}
	

	/**
	 * 退出登陆
	 */
	private void reqLoginOut() {
		IMLogin.IMLogoutReq imLogoutReq = IMLogin.IMLogoutReq.newBuilder()
				.build();
		int sid = IMBaseDefine.ServiceID.SID_LOGIN_VALUE;
		int cid = IMBaseDefine.LoginCmdID.CID_LOGIN_REQ_LOGINOUT_VALUE;
		try {
			imSocketManager.sendRequest(imLogoutReq, sid, cid);
		} catch (Exception e) {
			logger.e("#reqLoginOut#sendRequest error,cause by" + e.toString());
		} finally {
			LoginSp.instance().setLoginInfo(loginUserName, null, loginId, imei);
			logger.d("login#send logout finish message");
			triggerEvent(LoginEvent.LOGIN_OUT);
		}
	}

	/**
	 * 现在这种模式 req与rsp之间没有必然的耦合关系。是不是太松散了
	 * 
	 * @param imLogoutRsp
	 */
	public void onRepLoginOut(IMLogin.IMLogoutRsp imLogoutRsp) {
		int code = imLogoutRsp.getResultCode();
		logger.d("login#send logout finish message");
	}

	/**
	 * 重新请求登陆 IMReconnectManager 1. 检测当前的状态 2. 请求msg server的地址 3. 建立链接 4. 验证登陆信息
	 * 
	 * @return
	 */
	public void relogin() {
		if (!TextUtils.isEmpty(loginUserName) && !TextUtils.isEmpty(loginPwd)) {
			logger.d("reconnect#login#relogin");
			imSocketManager.reqMsgServerAddrs();
		} else {
			logger.d("reconnect#login#userName or loginPwd is null!!");
			everLogined = false;
			triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
		}
	}



	/**
	 * 自动登陆流程
	 * @param identity
	 */
	public void login(LoginSp.SpLoginIdentity identity) {
		if (identity == null) {
			triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
			return;
		}
		loginUserName = identity.getLoginName();
		loginPwd = identity.getPwd();
		imei = identity.getImei();

		identityChanged = false;

		int mLoginId = identity.getLoginId();
		// 初始化数据库
		DBInterface.instance().initDbHelp(ctx, mLoginId);
		UserEntity loginEntity = DBInterface.instance().getByLoginId(mLoginId);
		do {
			if (loginEntity == null) {
				break;
			}
			loginInfo = loginEntity;
			loginId = loginEntity.getPeerId();
			// 这两个状态不要忘记掉
			isLocalLogin = true;
			everLogined = true;
			triggerEvent(LoginEvent.LOCAL_LOGIN_SUCCESS);

		} while (false);
		// 开始请求网络
		imSocketManager.reqMsgServerAddrs();
	}


	/**
	 * 自动注册流程
	 * @param identity
	 */
	public void Register(RegistSp.SpRegistIdentity identity) {
		if (identity == null) {
			triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
			return;
		}
		loginUserName = identity.getRegistName();
		loginPwd = identity.getPwd();
		imei = identity.getImei();

		identityChanged = false;

		int mLoginId = identity.getLoginId();
		// 初始化数据库
		DBInterface.instance().initDbHelp(ctx, mLoginId);
		UserEntity loginEntity = DBInterface.instance().getByLoginId(mLoginId);
		do {
			if (loginEntity == null) {
				break;
			}
			loginInfo = loginEntity;
			loginId = loginEntity.getPeerId();
			// 这两个状态不要忘记掉
			isLocalLogin = true;
			everLogined = true;
			triggerEvent(LoginEvent.LOCAL_LOGIN_SUCCESS);

		} while (false);
		// 开始请求网络
		registMsgServer();
		// imSocketManager.registerServerAddrs();
	}

	
	/**
	 *  登陆模块
	 * @param userName
	 * @param password
	 * @param imei
	 */
	public void login(String userName, String password, String imei) {
		logger.i("login#login -> userName:%s", userName);

		// test 使用
		LoginSp.SpLoginIdentity identity = LoginSp.instance()
				.getLoginIdentity();
		if (identity != null && !TextUtils.isEmpty(identity.getPwd())) {
			if (identity.getPwd().equals(password)
					&& identity.getLoginName().equals(userName)) {
				login(identity);
				return;
			}
		}
		// test end
		this.imei = imei;
		loginUserName = userName;
		loginPwd = password;
		this.imei = imei;

		identityChanged = true;
		imSocketManager.reqMsgServerAddrs();

	}

	
	/**
	 *  注册模块
	 * @param userName
	 * @param password
	 * @param imei
	 */
	public void Regist(String userName, String password, String imei) {
		logger.i("login#login -> userName:%s", userName);

		// test 使用
		RegistSp.SpRegistIdentity identity = RegistSp.instance()
				.getRegistIdentity();
		if (identity != null && !TextUtils.isEmpty(identity.getPwd())) {
			if (identity.getPwd().equals(password)
					&& identity.getRegistName().equals(userName)) {
				// login(identity);
				Register(identity);
				return;
			}
		}
		// test end
		loginUserName = userName;
		loginPwd = password;
		identityChanged = true;
		this.imei = imei;
		// registMsgServer();
		imSocketManager.registerServerAddrs();
	}

	/**
	 * 链接成功之后
	 * */
	public void reqLoginMsgServer() {
		logger.i("login#reqLoginMsgServer");
		triggerEvent(LoginEvent.LOGINING);
		/** 加密 */
		String desPwd = new String(com.fise.xw.Security.getInstance()
				.EncryptPass(loginPwd));

		IMLogin.IMLoginReq imLoginReq = IMLogin.IMLoginReq.newBuilder()
				.setUserName(loginUserName).setPassword(desPwd).setIme(imei)
				.setOnlineStatus(IMBaseDefine.UserStatType.USER_STATUS_ONLINE)
				.setClientType(IMBaseDefine.ClientType.CLIENT_TYPE_ANDROID)
				.setClientVersion("1.0.1").build();

		int sid = IMBaseDefine.ServiceID.SID_LOGIN_VALUE;
		int cid = IMBaseDefine.LoginCmdID.CID_LOGIN_REQ_USERLOGIN_VALUE;
		imSocketManager.sendRequest(imLoginReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMLogin.IMLoginRes imLoginRes = IMLogin.IMLoginRes
							.parseFrom((CodedInputStream) response);
					onRepMsgServerLogin(imLoginRes);
				} catch (IOException e) {
					triggerEvent(LoginEvent.LOGIN_INNER_FAILED);
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(LoginEvent.LOGIN_INNER_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(LoginEvent.LOGIN_INNER_FAILED);
			}
		});
	}

	public void SendSms(String mobile) {

		this.mobile = mobile;
		identityChanged = true;
		imSocketManager.SmsServerAddrs();
	}

	/**
	 * 注册链接开始发送注册数据
	 * */
	public void registSmsServer() {
		logger.i("login#reqLoginMsgServer");
		triggerEvent(LoginEvent.LOGINING);

		IMSms.IMSendSmsReq imSmsReq = IMSms.IMSendSmsReq.newBuilder()
				.setMobile(mobile).setAction(SmsActionType.SMS_ACTION_REGIST)
				.build();

		int sid = IMBaseDefine.ServiceID.SID_SMS_VALUE;
		int cid = IMBaseDefine.SmsCmdID.CID_SMS_SEND_REQUEST_VALUE; // CID_LOGIN_REQ_USERLOGIN_VALUE

		imSocketManager.sendRequest(imSmsReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {

					IMSms.IMSendSmsRes imSendSmsRes = IMSms.IMSendSmsRes
							.parseFrom((CodedInputStream) response);
					onRepMsgRegist(imSendSmsRes);

				} catch (IOException e) {
					triggerEvent(LoginEvent.REGIST_SMS_FAILED);
					// 注册失败
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(LoginEvent.REGIST_SMS_FAILED);
				// 注册失败
			}

			@Override
			public void onTimeout() {
				triggerEvent(LoginEvent.REGIST_SMS_FAILED);
				// 注册超时
			}
		});
	}
	
	
	/**
	 *   注册 回复
	 * @param registRes
	 */
	public void onRepMsgRegist(IMSms.IMSendSmsRes registRes) {
		logger.i("login#onRepMsgServerLogin");

		if (registRes == null) {
			logger.e("login#decode LoginResponse failed");
			triggerEvent(LoginEvent.REGIST_SMS_FAILED);
			return;
		}

		IMBaseDefine.ResultType code = registRes.getResultCode();
		switch (code) {
		case REFUSE_REASON_NONE: {
			triggerEvent(LoginEvent.REGIST_SMS_SUCCESS);
		}
			break;

		case REFUSE_REASON_DB_VALIDATE_FAILED: {
			triggerEvent(LoginEvent.REGIST_SMS_FAILED);
		}
			break;

		default: {
			triggerEvent(LoginEvent.REGIST_SMS_FAILED);
		}
			break;
		}
	}

	
	/**
	 *  post sms 请求
	 * @param phone
	 * @param type
	 * @param imei
	 */
	void postSendSmS(String phone, SmsActionType type, String imei) {

		String url = UrlConstant.ACCESS_MSG_SEND_SMS;
		// 第一步，创建HttpPost对象
		HttpPost request = new HttpPost(url);

		try {
			// 设置httpPost请求参数

			String md5Imei = new String(com.fise.xw.Security.getInstance()
					.EncryptPass(imei + "fise_zn_xw@fise.com.cn"));

			// 先封装一个 JSON 对象
			JSONObject param = new JSONObject();
			param.put("action", type.ordinal());
			param.put("mobile", phone);
			param.put("app_dev", imei);
			param.put("app_key", md5Imei);

			// 绑定到请求 Entry
			StringEntity se = new StringEntity(param.toString());
			request.setEntity(se);
			// 发送请求
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(request);
			// 得到应答的字符串，这也是一个 JSON 格式保存的数据

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// 第三步，使用getEntity方法活得返回结果
				String result = EntityUtils.toString(httpResponse.getEntity());

				JSONObject json = new JSONObject(result);
				String code = json.getString("error_code");
				String error_msg = json.getString("error_msg");

			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 注册链接开始发送注册数据
	 * */
	public void registMsgServer() {
		logger.e("registMsgServer", "registMsgServer");
		triggerEvent(LoginEvent.LOGINING);
		/** 加密 */
		String desPwd = new String(com.fise.xw.Security.getInstance()
				.EncryptPass(loginPwd));

		IMRegist.IMRegistReq imRegReq = IMRegist.IMRegistReq.newBuilder()
				.setAccount(loginUserName).setPassword(desPwd).setIme(imei)
				.setClientType(IMBaseDefine.ClientType.CLIENT_TYPE_ANDROID)
				.setClientVersion("1.0.1").build(); 

		int sid = IMBaseDefine.ServiceID.SID_REGIST_VALUE;
		int cid = IMBaseDefine.RegistCmdID.CID_REGIST_REQ_MSGSERVER_VALUE; // CID_LOGIN_REQ_USERLOGIN_VALUE
		imSocketManager.sendRequest(imRegReq, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				try {
					IMRegist.IMRegistRes imRegistRes = IMRegist.IMRegistRes
							.parseFrom((CodedInputStream) response);
					onRepMsgServerRegist(imRegistRes);

				} catch (IOException e) {
					triggerEvent(LoginEvent.REGIST_INNER_FAILED);
					// 注册失败
					logger.e("login failed,cause by %s", e.getCause());
				}
			}

			@Override
			public void onFaild() {
				triggerEvent(LoginEvent.REGIST_INNER_FAILED);
				// 注册失败
			}

			@Override
			public void onTimeout() {
				triggerEvent(LoginEvent.REGIST_INNER_FAILED);
				// 注册超时
			}
		});
	}

	/**
	 * 验证注册信息结果
	 * 
	 * @param loginRes
	 */
	public void onRepMsgServerRegist(IMRegist.IMRegistRes registRes) {
		logger.i("login#onRepMsgServerLogin");

		if (registRes == null) {
			logger.e("login#decode LoginResponse failed");
			triggerEvent(LoginEvent.REGIST_AUTH_FAILED);
			return;
		}

		IMBaseDefine.ResultType code = registRes.getResultCode();
		String result = registRes.getResultString();
		RegistActivity.ShowText = result;
		switch (code) {
		case REFUSE_REASON_NONE: {
			IMBaseDefine.UserStatType userStatType = registRes
					.getOnlineStatus();
			IMBaseDefine.UserInfo userInfo = registRes.getUserInfo();
			loginId = userInfo.getUserId();
			loginInfo = ProtoBuf2JavaBean.getUserEntity(userInfo);
			onRegistOk();
		}
			break;

		case REFUSE_REASON_DB_VALIDATE_FAILED: {
			logger.e("login#login msg server failed, result:%s", code);
			loginError = registRes.getResultString();
			triggerEvent(LoginEvent.REGIST_AUTH_FAILED);
		}
			break;

		default: {
			logger.e("login#login msg server inner failed, result:%s", code);
			loginError = registRes.getResultString();
			triggerEvent(LoginEvent.REGIST_INNER_FAILED);
		}
			break;
		}
	}

	public String getError() {
		return loginError;

	}

	/**
	 * 验证登陆信息结果
	 * 
	 * @param loginRes
	 */
	public void onRepMsgServerLogin(IMLogin.IMLoginRes loginRes) {
		logger.i("login#onRepMsgServerLogin");

		if (loginRes == null) {
			logger.e("login#decode LoginResponse failed");
			triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
			return;
		}

		IMBaseDefine.ResultType code = loginRes.getResultCode();

		switch (code) {
		case REFUSE_REASON_NONE: {
			IMBaseDefine.UserStatType userStatType = loginRes.getOnlineStatus();
			IMBaseDefine.UserInfo userInfo = loginRes.getUserInfo();
			loginId = userInfo.getUserId();
			loginInfo = ProtoBuf2JavaBean.getUserEntity(userInfo);
			onLoginOk();
		}
			break;

		case REFUSE_REASON_DB_VALIDATE_FAILED: {
			logger.e("login#login msg server failed, result:%s", code);
			loginError = loginRes.getResultString();
			triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
		}
			break;

		case REFUSE_REASON_NEED_AUTH_DEVICE: { 

			triggerEvent(LoginEvent.LOGIN_AUTH_DEVICE);

		}
			break;

		default: {
			logger.e("login#login msg server inner failed, result:%s", code);
			loginError = loginRes.getResultString();
			triggerEvent(LoginEvent.LOGIN_INNER_FAILED);
		}
			break;
		}
	}

	public void onLoginOk() {
		logger.i("login#onLoginOk");
		everLogined = true;
		isKickout = false;

		// 判断登陆的类型
		if (isLocalLogin) {

			Log.d("onLoginOk", "onLoginOk");
			triggerEvent(LoginEvent.LOCAL_LOGIN_MSG_SERVICE);
		} else {
			isLocalLogin = true;
			triggerEvent(LoginEvent.LOGIN_OK);
		}
		// reqSystemConfigReq();

		// 发送token
		// reqDeviceToken();
		if (identityChanged) {
			// reqSystemConfigReq();
			LoginSp.instance().setLoginInfo(loginUserName, loginPwd, loginId,
					imei);
			identityChanged = false;
		}
	}

	public void onRegistOk() {
		logger.i("login#onRegistOk");
		everLogined = true;
		isKickout = false;

		// 判断登陆的类型
		if (isLocalLogin) {

			Log.d("onRegistOk", "onRegistOk");
			triggerEvent(LoginEvent.LOCAL_LOGIN_MSG_SERVICE);
		} else {
			isLocalLogin = true;
			triggerEvent(LoginEvent.REGIST_OK);
		}

		// reqSystemConfigReq();

		// 发送token
		// reqDeviceToken();
		if (identityChanged) {
			// reqSystemConfigReq();
			LoginSp.instance().setLoginInfo(loginUserName, loginPwd, loginId,
					imei);
			identityChanged = false;
		}
	}

	public void onKickout(IMLogin.IMKickUser imKickUser) {
		logger.i("login#onKickout");
		int kickUserId = imKickUser.getUserId();
		IMBaseDefine.KickReasonType reason = imKickUser.getKickReason();
		isKickout = true;
		imSocketManager.onMsgServerDisconn();
	}

	// 收到PC端登陆的通知，另外登陆成功之后，如果PC端在线，也会立马收到该通知
	public void onLoginStatusNotify(IMBuddy.IMPCLoginStatusNotify statusNotify) {
		int userId = statusNotify.getUserId();
		// todo 由于交互不太友好 暂时先去掉
		if (true || userId != loginId) {
			logger.i("login#onLoginStatusNotify userId ≠ loginId");
			return;
		}

		if (isKickout) {
			logger.i("login#already isKickout");
			return;
		}

		switch (statusNotify.getLoginStat()) {
		case USER_STATUS_ONLINE: {
			isPcOnline = true;
			EventBus.getDefault().postSticky(LoginEvent.PC_ONLINE);
		}
			break;

		case USER_STATUS_OFFLINE: {
			isPcOnline = false;
			EventBus.getDefault().postSticky(LoginEvent.PC_OFFLINE);
		}
			break;
		}
	}

	// 踢出PC端登陆
	public void reqKickPCClient() {
		IMLogin.IMKickPCClientReq req = IMLogin.IMKickPCClientReq.newBuilder()
				.setUserId(loginId).build();
		int sid = IMBaseDefine.ServiceID.SID_LOGIN_VALUE;
		int cid = IMBaseDefine.LoginCmdID.CID_LOGIN_REQ_KICKPCCLIENT_VALUE;
		imSocketManager.sendRequest(req, sid, cid, new Packetlistener() {
			@Override
			public void onSuccess(Object response) {
				triggerEvent(LoginEvent.KICK_PC_SUCCESS);
			}

			@Override
			public void onFaild() {
				triggerEvent(LoginEvent.KICK_PC_FAILED);
			}

			@Override
			public void onTimeout() {
				triggerEvent(LoginEvent.KICK_PC_FAILED);
			}
		});
	}

	/** ------------------状态的 set get------------------------------ */
	public int getLoginId() {
		return loginId;
	}

	public void setLoginId(int loginId) {
		logger.d("login#setLoginId -> loginId:%d", loginId);
		this.loginId = loginId;

	}

	public boolean isEverLogined() {
		return everLogined;
	}

	public void setEverLogined(boolean everLogined) {
		this.everLogined = everLogined;
	}

	public UserEntity getLoginInfo() {
		return loginInfo;
	}

	public void setLoginInfo(UserEntity loginInfo) {
		this.loginInfo = loginInfo;
	}

	public LoginEvent getLoginStatus() {
		return loginStatus;
	}

	public boolean isKickout() {
		return isKickout;
	}

	public void setKickout(boolean isKickout) {
		this.isKickout = isKickout;
	}

	public boolean isPcOnline() {
		return isPcOnline;
	}

	public void setPcOnline(boolean isPcOnline) {
		this.isPcOnline = isPcOnline;
	}

}

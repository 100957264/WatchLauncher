package com.fise.xiaoyu.imservice.event;

/**
 * 登陆事件
 */
public enum LoginEvent {
    NONE,
    LOGINING, 
    REGIST_INNER_SUCCESS,
    
 // 网络登录验证成功
    REGIST_OK,
    REGIST_INNER_FAILED,
    REGIST_AUTH_FAILED,
    REGIST_OUT,
    
    // 网络登录验证成功
    LOGIN_OK,
    LOGIN_INNER_FAILED,
    LOGIN_AUTH_FAILED,
    LOGIN_OUT,

    // 对于离线登录
    // 如果在此时，网络登录返回账号密码错误应该怎么处理? todo 强制退出
    // 登录成功之后触发 LOCAL_LOGIN_MSG_SERVICE
    LOCAL_LOGIN_SUCCESS,
    LOCAL_LOGIN_MSG_SERVICE,


    PC_ONLINE,
    PC_OFFLINE,
    KICK_PC_SUCCESS,
    KICK_PC_FAILED,
    REGIST_SMS_FAILED,
    REGIST_SMS_SUCCESS,
    INFO_PROVINCE_SUCCESS,
    INFO_PROVINCE_FAILED, 
    INFO_CITY_SUCCESS,
    INFO_CITY_FAILED,
    INFO_ADDRESS_FAILED,
    
    FORCE_FAILED,
   // FORCE_SUCCESS,
    LOGIN_AUTH_DEVICE
     
}

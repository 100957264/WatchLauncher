package com.fise.xw.config;

/**
 * @author : yingmu on 15-1-5.
 * @email : yingmu@mogujie.com.
 *
 */
public interface DBConstant {


	//1-启用该安全区域 2-弃用该安全区域
	 public final int ELECTRONIC_STATS_ENABLE = 1; // 
	 public final int ELECTRONIC_STATS_DISABLE = 2; // 
	 
	//安全围栏 0-删除 1-增加 2修改
	 public final int ELECTRONIC_DELETE = 0; // 
	 public final int ELECTRONIC_ADD = 1; // 
	 public final int ELECTRONIC_UPDATE = 2; // 
	
	 public final int GROUP_AVATAR_NUM = 9; //显示群头像的数量
	  
	 public final int SEX_INFO_USER = 0; //个人
	 public final int SEX_INFO_DEV = 1; //设备
	 
	 
	
	 public final int REGIST_TYPE_LOGIN = 0; //注册登录
	 public final int REGIST_TYPE_BLACKSMS = 1; // 找回密码
	 
	 
	 
	 
	 public final int SESSION_GROUP_WHILTE_NUM = 5; //白名单数量
	 public final int SESSION_GROUP_ALARM_NUM = 3; // 紧急号码
	 
	 
	 
    public final int SESSION_TYPE_GROUP_WEI = 4;
	   
    
    
	 /**昵称修改 
     * 1. 群位友 2.个人位友  3.设备
     * */
    public final int POSTION_WEI = 1;
    public final int POSTION_INFO_WEI = 2;
    public final int POSTION_DEV = 3;
    
    
	 
	 /**昵称修改 
    * 0. 自己 1.设备
    * */
   public final int OWN_NICK = 0;
   public final int DEVICE_NICK = 1;
   
	
    /**群昵称
     * 1. 关闭 2.打开
     * */
    public final int SHOW_GROUP_NICK_CLOSE = 0;
    public final int SHOW_GROUP_NICK_OPEN = 1;
    
    /**性别
     * 1. 男性 2.女性
     * */
    public final int SEX_MAILE = 1;
    public final int SEX_FEMALE = 2;
    
    
    //session_type ：0-单个删除整个session[原] 1-单个删除聊天记录 2-清空所有
    public final int  SESSION_ALL = 0;
    public final int  SESSION_MESSAGE = 1;
    public final int  SESSION_MESSAGE_ALL = 2;
    

    
    public final int PASS_LENGTH = 6;
    
    
    /**msgType*/
    public final int  MSG_TYPE_SINGLE_TEXT = 0x01;
    public final int  MSG_TYPE_SINGLE_AUDIO = 0x02;
    public final int  MSG_TYPE_GROUP_TEXT = 0x11;
    public final int  MSG_TYPE_GROUP_AUDIO = 0x12;
    public final int  MSG_TYPE_SINGLE_NOTICE = 0x05; 
    public final int  MSG_TYPE_SINGLE_VIDIO = 0x04; 
    public final int  MSG_TYPE_GROUP_VIDIO = 0x14; 
    
    public final int  MSG_TYPE_SINGLE_BUSSINESS_CARD  = 0x22; 
    public final int  MSG_TYPE_GROUP_BUSSINESS_CARD  = 0x23;
    public final int  MSG_TYPE_SINGLE_LOCATION = 0x20;
    public final int  MSG_TYPE_GROUP_LOCATION = 0x21;
      
    public final int  MSG_TYPE_SINGLE_IMAGE   = 0x15; //普通图片消息
    public final int  MSG_TYPE_GROUP_IMAGE    = 0x16; //普通图片消息
    
    
    
    
    public final int  MSG_TYPE_GROUP_ADD_FRIENDS = 0x55;
    public final int  MSG_TYPE_GROUP_DEV_MESSAGE = 0x96;
    
    
     
    
    public final int  MSG_TYPE_SINGLE_AUTH_IMAGE  = 0x07; //被抓拍图片消息-私聊
    public final int   MSG_TYPE_GROUP_AUTH_IMAGE  = 0x08; //被抓拍图片消息-群聊
    
    public final int   MSG_TYPE_SINGLE_AUTH_SOUND = 0x09; //被录音消息-私聊
    public final int    MSG_TYPE_GROUP_AUTH_SOUND = 0x10; //被录音消息-群聊
    
      
    
   // public final int  MSG_TYPE_MAKE_FRIEND   = 0x15;
   // public final int  MSG_TYPE_CONFIRM_FRIEND    = 0x16;
    public final int  MSG_TYPE_REFUSE_FRIEND     = 0x17;
    public final int  MSG_TYPE_DEVICE_COMMAND     = 0x30;
            
             
    public final String  SYSTEM_ANDROID_UPDATE_URL     = "android_update_url";
    public final String  SYSTEM_ANDROID_VERSION     = "android_version";
    public final String  SYSTEM_ANDROID_FORCE_UPDATE     = "android_force_update";
    public final String  SYSTEM_ANDROID_VERSION_COMMENT    = "android_version_comment";
    
    
     
    /**msgDisplayType
     * 保存在DB中，与服务端一致，图文混排也是一条
     * 1. 最基础的文本信息
     * 2. 纯图片信息
     * 3. 语音
     * 4. 图文混排
     * */
    public final int SHOW_ORIGIN_TEXT_TYPE = 1;
    public final int  SHOW_IMAGE_TYPE = 2;
    public final int  SHOW_AUDIO_TYPE = 3;
    public final int  SHOW_MIX_TEXT = 4;
    public final int  SHOW_GIF_TYPE = 5;
    public final int  CHANGE_NOT_FRIEND = 18;
    public final int  SHOW_TYPE_CARD = 7;
    public final int  SHOW_TYPE_VEDIO = 8;
    public final int  SHOW_TYPE_POSTION = 9;
    public final int  SHOW_TYPE_ADDFRIENDS = 10;
    public final int  SHOW_TYPE_DEV_MESSAGE = 11;
    public final int  SHOW_TYPE_NOTICE_BLACK = 19;
    
    
    

    public final String DISPLAY_FOR_IMAGE = "[图片]";
    public final String DISPLAY_FOR_MIX = "[图文消息]";
    public final String DISPLAY_FOR_AUDIO = "[语音]";
    public final String DISPLAY_FOR_ERROR = "[未知消息]"; 
    public final String DISPLAY_FOR_NOTICE = "[通知]"; 
    public final String DISPLAY_FOR_CARD = "[名片]"; 
    public final String DISPLAY_FOR_VEDIO = "[小视频]"; 
    public final String DISPLAY_FOR_POSTION = "[位置]"; 
    public final String DISPLAY_FOR_ADDFRIENDS = "[提示]"; 
    public final String DISPLAY_FOR_DEV_MESSAGE = "[安全提醒]"; 
    public final String DISPLAY_FOR_DEV_PHONE = "[通话记录]"; 
    public final String DISPLAY_FOR_DEV_SILENT = "[静默监听]"; 
    
    
    /**sessionType*/
    public final int  SESSION_TYPE_SINGLE = 1;
    public final int  SESSION_TYPE_GROUP = 2;
    public final int SESSION_TYPE_ERROR= 3;
    

    /**好友/位友 请求*/
    public final int  FRIENDS_REQ_NO 	 = 0; //未处理
    public final int  FRIENDS_REQ_YES   = 1; //已接受
    public final int  FRIENDS_REQ_REJECT = 2;
    
    
    
    /**friendsType*/
    public final int  FRIENDS_TYPE_NO 	 = 0;
    public final int  FRIENDS_TYPE_YES   = 1;
    public final int  FRIENDS_TYPE_WEI = 2;
    
    /**authType*/
    public final int  AUTH_TYPE_BASE 	   = 0;  //基本权限-非好友
    public final int  AUTH_TYPE_PRIVACY    = 1;  //好友权限-好友
    public final int  AUTH_TYPE_LOCATION   = 2;  //位置权限-位友权限
    public final int  AUTH_TYPE_BLACK      = 3;  //黑名单中
    
    
    /**friendsType*/
    public final int  ONLINE 	 = 1;
    public final int  OFFLINE    = 0; 
    
    
    
    /**SeachType*/
    public final int  SEACHDEVICE 	 = 1;
    public final int  SEACHFRIENDS    = 0; 
    
    
    /**deviceType*/
    public final int  TELEPHONE 	 = 0; //电话定位机
    public final int  CAMERA    	= 1; //网络摄像头
    public final int  INTERCOM    	= 2; //对讲机
    public final int  CHILDRENWATCH    	= 3;  //儿童手表
    public final int  ELECTRICVEHICLE    	= 4; 
    public final int  VEHICLEDEVICE    	= 5; //车载机
    public final int  NAVIGATOR    	= 6;   //导航仪
    public final int  SMARTHOME    	= 7;   //智能家居
    public final int  MORE    	= 8;   //更多
    
    /**status */
    public final int  ADD  	 = 1;
    public final int  DELTE    = 0; 
    public final int  UPDATE    = 2; 
    
    
    /**user status
     * 1. 试用期 2. 正式 3. 离职 4.实习
     * */
    public final int  USER_STATUS_PROBATION = 1;
    public final int  USER_STATUS_OFFICIAL = 2;
    public final int  USER_STATUS_LEAVE = 3;
    public final int  USER_STATUS_INTERNSHIP =4;

   /**group type*/
   public final int  GROUP_TYPE_NORMAL = 1;
   public final int  GROUP_TYPE_TEMP = 2;
   public final int  GROUP_TYPE_WEI_TEMP = 3;
   
   
   
   /**save type 是否保存*/
   public final int  GROUP_MEMBER_STATUS_TEMP = 0;
   public final int  GROUP_MEMBER_STATUS_SAVE = 1;
   public final int  GROUP_MEMBER_STATUS_EXIT = 2;
   
    /**group status
     * 1: shield  0: not shield
     * */

    public final int  GROUP_STATUS_ONLINE = 0;
    public final int  GROUP_STATUS_SHIELD = 1;

    /**group change Type*/
    public final int  GROUP_MODIFY_TYPE_ADD= 0;
    public final int  GROUP_MODIFY_TYPE_DEL =1;
    public final int  GROUP_USER_ADD_BY_SCAN =2;  

    /**depart status Type*/
    public final int  DEPT_STATUS_OK= 0;
    public final int  DEPT_STATUS_DELETE =1;

}

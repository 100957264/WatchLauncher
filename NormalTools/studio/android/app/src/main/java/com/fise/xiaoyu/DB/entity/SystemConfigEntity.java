package com.fise.xiaoyu.DB.entity;


/**
 *  系统基本配置信息
 */
public class SystemConfigEntity {

	public String comment_url; // 评价url
	public String launch;// 启动图地址,action是点击启动图跳转的地址(类似广告)
	public int launch_time; // 加载页广告时间
	public String system_notice; // 系统通知
	public String update_url; // 升级软件的地址（应用商城地址或者我们自己的发布地址）
	public String website; // 小雨二维码的前缀，后面为加密过的字符串
	public String version_support; // 对应客户端版本，最小可用版本号，本地软件版本小于此值，提示更新此版本不可用

	public String version;
	public String launchAction;
	protected Long id;
	public String version_comment;
	private String suggestUrl;
	private String mallUrl = "";
	
	public SystemConfigEntity() {

	}
	 
    
    

	public SystemConfigEntity(Long id,String comment_url, String launch,
			int launch_time, String system_notice, String website,
			String version_support, String version,String launchAction,String update_url,String version_comment ,String suggest_url,String mallUrl) {
		
//	public SystemConfigEntity(Long id,String launch, int launch_time,
//			String system_notice, String update_url, String website,
//			String version_support, String comment_url,String version) {

		this.id = id;
		
		this.launch = launch;
		this.launch_time = launch_time;
		this.system_notice = system_notice;
		this.update_url = update_url;
		this.website = website;
		this.version_support = version_support;
		this.comment_url = comment_url;
		this.version = version;
		this.version_comment = version_comment;
		this.suggestUrl = suggest_url;
		this.mallUrl = mallUrl;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	public String getLaunch() {
		return launch;
	}

	public void setLaunch(String launch) {
		this.launch = launch;
	}

	public int getLaunchTime() {
		return launch_time;
	}

	public void setLaunchTime(int launch_time) {
		this.launch_time = launch_time;
	}

	public String getSystemNotice() {
		return system_notice;
	}

	public void setSystemNotice(String system_notice) {
		this.system_notice = system_notice;
	}

	public String getUpdateUrl() {
		return update_url;
	}

	public void setUpdateUrl(String update_url) {
		this.update_url = update_url;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getVersionSupport() {
		return version_support;
	}

	public void setVersionSupport(String version_support) {
		this.version_support = version_support;
	}

	public String getCommentUrl() {
		return comment_url;
	}

	public void setCommentUrl(String comment_url) {
		this.comment_url = comment_url;
	}

	public String getLaunchAction() {
		return launchAction;
	}

	public void setLaunchAction(String launchAction) {
		this.launchAction = launchAction;
	}

	
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersionComment() {
		return version_comment;
	}

	public void SetVersionComment(String version_comment) {
		this.version_comment = version_comment;
	}


	public String getSuggestUrl() {
		return suggestUrl;
	}

	public void setSuggestUrl(String suggestUrl) {
		this.suggestUrl = suggestUrl;
	}


	public String getMallUrl() {
		return mallUrl;
	}

	public void setMallUrl(String mallUrl) {
		this.mallUrl = mallUrl;
	}

}

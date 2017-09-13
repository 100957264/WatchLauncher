package com.fise.xw.imservice.manager;

import java.lang.reflect.Method;
import java.text.Format.Field;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.fise.xw.R;
import com.fise.xw.DB.entity.GroupEntity;
import com.fise.xw.DB.entity.PeerEntity;
import com.fise.xw.DB.entity.SessionEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.entity.WeiEntity;
import com.fise.xw.DB.sp.ConfigurationSp;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.SysConstant;
import com.fise.xw.imservice.entity.UnreadEntity;
import com.fise.xw.imservice.event.GroupEvent;
import com.fise.xw.imservice.event.ReqEvent;
import com.fise.xw.imservice.event.UnreadEvent;
import com.fise.xw.protobuf.IMUserAction.ActionType;
import com.fise.xw.ui.activity.MainActivity;
import com.fise.xw.ui.activity.MessageActivity;
import com.fise.xw.ui.activity.NewFriendActivity;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import de.greenrobot.event.EventBus;

/**
 * 伪推送; app退出之后就不会收到推送的信息 通知栏新消息通知 a.每个session 只显示一条 b.每个msg 信息都显示 配置依赖与
 * configure
 */
@SuppressLint("NewApi")
public class IMNotificationManager extends IMManager {

	private Logger logger = Logger.getLogger(IMNotificationManager.class);
	private static IMNotificationManager inst = new IMNotificationManager();

	public static IMNotificationManager instance() {
		return inst;
	}

	private ConfigurationSp configurationSp;

	private IMNotificationManager() {
	}

	@Override
	public void doOnStart() {
		cancelAllNotifications();
	}

	public void onLoginSuccess() {
		int loginId = IMLoginManager.instance().getLoginId();
		configurationSp = ConfigurationSp.instance(ctx, loginId);
		if (!EventBus.getDefault().isRegistered(inst)) {
			EventBus.getDefault().register(inst);
		}
	}

	public void reset() {
		EventBus.getDefault().unregister(this);
		cancelAllNotifications();
	}

	public void onEventMainThread(ReqEvent event) {
		switch (event.event) {
		case REQ_FRIENDS_MESSAGE:
			WeiEntity entity = event.entity;
			handleFriendsReqRecv(entity);
			break;
		case REQ_WEI_MESSAGE:
			WeiEntity entity1 = event.entity;
			handleFriendsReqRecv(entity1);
			break;
		}
	}

	public void onEventMainThread(UnreadEvent event) {
		switch (event.event) {
		case UNREAD_MSG_RECEIVED:
			UnreadEntity unreadEntity = event.entity;
			handleMsgRecv(unreadEntity);
			break;
		}
	}

	// 屏蔽群，相关的通知全部删除
	public void onEventMainThread(GroupEvent event) {
		GroupEntity gEntity = event.getGroupEntity();
		if (event.getEvent() == GroupEvent.Event.SHIELD_GROUP_OK) {
			if (gEntity == null) {
				return;
			}
			cancelSessionNotifications(gEntity.getSessionKey());
		}
	}

	private void handleMsgRecv(UnreadEntity entity) {
		logger.d("notification#recv unhandled message");
		int peerId = entity.getPeerId();
		int sessionType = entity.getSessionType();
		logger.d("notification#msg no one handled, peerId:%d, sessionType:%d",
				peerId, sessionType);

		// 判断是否设定了免打扰
		if (entity.isForbidden()) {
			logger.d("notification#GROUP_STATUS_SHIELD");
			return;
		}

		// PC端是否登陆 取消 【暂时先关闭】
		// if(IMLoginManager.instance().isPcOnline()){
		// logger.d("notification#isPcOnline");
		// return;
		// } 


        // 单独的设置
        boolean singleOnOff = configurationSp.getCfg(entity.getSessionKey(),ConfigurationSp.CfgDimension.NOTIFICATION);
        if (singleOnOff) {
            logger.d("notification#shouldShowNotificationBySession is false, return");
            return;
        }
 
	     // 全局开关
        boolean  globallyOnOff = configurationSp.getCfg(SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.NOTIFICATION);
        if (globallyOnOff) {
            logger.d("notification#shouldGloballyShowNotification is false, return");
            return;
        }

        //if the message is a multi login message which send from another terminal,not need notificate to status bar
        // 判断是否是自己的消息
        if(IMLoginManager.instance().getLoginId() != peerId){
             showNotification(entity);
        }
 
        
		/*
		SessionEntity entityTop = IMSessionManager.instance().findSession(
				entity.getSessionKey());
		if (entityTop != null) {
			if (entityTop.getMuteNotification() != 1) {
				//
				showNotification(entity);
			} else {
				boolean globallyOnOff = configurationSp.getCfg(
						SysConstant.SETTING_GLOBAL,
						ConfigurationSp.CfgDimension.NOTIFICATION);
				if (!globallyOnOff) {
					showNotification(entity);
				}
			}
		} else {
			// 全局开关

			// // 鍗曠嫭鐨勮缃�
			boolean singleOnOff = configurationSp.getCfg(
					entity.getSessionKey(),
					ConfigurationSp.CfgDimension.NOTIFICATION);
			if (singleOnOff) {
				// logger.d("notification#shouldShowNotificationBySession is false, return");
				return;
			}

			boolean globallyOnOff = configurationSp.getCfg(
					SysConstant.SETTING_GLOBAL,
					ConfigurationSp.CfgDimension.NOTIFICATION);
			if (globallyOnOff) {
				// logger.d("notification#shouldGloballyShowNotification is false, return");
				return;
			}
			
			//

			// if the message is a multi login message which send from another
			// terminal,not need notificate to status bar
			// 判断是否是自己的消息
			if (IMLoginManager.instance().getLoginId() != peerId) {
				showNotification(entity);
			}
		}
		*/
		// 

	}

	private void handleFriendsReqRecv(WeiEntity entity) {

		logger.d("notification#recv unhandled message");
		int peerId = entity.getActId();

		// if the message is a multi login message which send from another
		// terminal,not need notificate to status bar
		boolean globallyOnOff = configurationSp.getCfg(
				SysConstant.SETTING_GLOBAL,
				ConfigurationSp.CfgDimension.NOTIFICATION);
		if (globallyOnOff) {
			// logger.d("notification#shouldGloballyShowNotification is false, return");
			return;
		}
		
		// 判断是否是自己的消息
		if (IMLoginManager.instance().getLoginId() != peerId) {
			showReqNotification(entity);

		}
	}

	public void cancelAllNotifications() {
		logger.d("notification#cancelAllNotifications");
		if (null == ctx) {
			return;
		}
		NotificationManager notifyMgr = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notifyMgr == null) {
			return;
		}
		notifyMgr.cancelAll();
	}

	/**
	 * 在通知栏中删除特定回话的状态
	 * 
	 * @param sessionKey
	 */
	public void cancelSessionNotifications(String sessionKey) { 
		NotificationManager notifyMgr = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (null == notifyMgr) {
			return;
		}
		int notificationId = getSessionNotificationId(sessionKey);
		notifyMgr.cancel(notificationId);
	}

	 
	
	private void showReqNotification(final WeiEntity entity) {
		// todo eric need to set the exact size of the big icon
		// 服务端有些特定的支持 尺寸是不是要调整一下 todo 100*100 下面的就可以不要了
		ImageSize targetSize = new ImageSize(80, 80);
		int peerId = entity.getFromId();

		String avatarUrl = "";
		String title = "";

		UserEntity currentUser = IMContactManager.instance()
				.findContact(peerId);

		if (currentUser == null) {
			currentUser = IMContactManager.instance().findReq(peerId);
		}

		String type = "";
		if (entity.getActType() == ActionType.ACTION_TYPE_NEW_FRIEDN.ordinal()) {
			type = "好友";

		} else if (entity.getActType() == ActionType.ACTION_TYPE_MONITOR
				.ordinal()) {
			type = "位友";
		}

		title = currentUser.getMainName();
		avatarUrl = currentUser.getAvatar();

		// 获取头像
		avatarUrl = IMUIHelper.getRealAvatarUrl(avatarUrl);

		// final String ticker = String.format("[%d%s]%s: %s", totalUnread,
		// unit, title, content);
		final String ticker;

		ticker = title + "请求你为" + type;// String.format("[%d%s]%s: %s",
										// totalUnread, unit, title, content);

		// 好友请求与消息处理不同 currentUser.getSessionKey()
		final int notificationId = getSessionNotificationId(currentUser
				.getPeerId() + "");
		final Intent intent = new Intent(ctx, NewFriendActivity.class);

		logger.d("notification#notification avatarUrl:%s", avatarUrl);
		final String finalTitle = title;
		ImageLoader.getInstance().loadImage(avatarUrl, targetSize, null,
				new SimpleImageLoadingListener() {

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						logger.d("notification#icon onLoadComplete");
						// holder.image.setImageBitmap(loadedImage);
						showInNotificationBar(finalTitle, ticker, loadedImage,
								notificationId, intent, 1);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						logger.d("notification#icon onLoadFailed");
						// 服务器支持的格式有哪些
						// todo eric default avatar is too small, need big
						// size(128 * 128)
						Bitmap defaultBitmap = BitmapFactory.decodeResource(
								ctx.getResources(), R.drawable.icon);
						showInNotificationBar(finalTitle, ticker,
								defaultBitmap, notificationId, intent, 1);

					}
				});
	}

	private void showNotification(final UnreadEntity unreadEntity) {
		// todo eric need to set the exact size of the big icon
		// 服务端有些特定的支持 尺寸是不是要调整一下 todo 100*100 下面的就可以不要了
		ImageSize targetSize = new ImageSize(80, 80);
		int peerId = unreadEntity.getPeerId();
		int sessionType = unreadEntity.getSessionType();
		String avatarUrl = "";
		String title = "";
		String content = unreadEntity.getLatestMsgData();
		String unit = ctx.getString(R.string.msg_cnt_unit);
		final int totalUnread = unreadEntity.getUnReadCnt();

		boolean singleOnOff = configurationSp.getCfg(
				unreadEntity.getSessionKey(),
				ConfigurationSp.CfgDimension.NOTIFICATION);

		if (unreadEntity.getSessionType() == DBConstant.SESSION_TYPE_SINGLE) {
			UserEntity contact = IMContactManager.instance()
					.findContact(peerId);
			if (contact != null) {
				title = contact.getMainName();
				avatarUrl = contact.getAvatar();
			} else {
				if (contact == null) {
					contact = IMContactManager.instance().findDeviceContact(
							peerId);
				}

				if (contact == null) {
					contact = IMContactManager.instance().findXiaoWeiContact(
							peerId);
				}

				if (contact == null) {
					contact = IMContactManager.instance().findContact(peerId);
				}

				if (contact != null) {
					title = contact.getMainName();
					avatarUrl = contact.getAvatar();
				} else {
					title = "User_" + peerId;
					avatarUrl = "";
				}

			}

			// 如果是免打扰则不现实状态通知
			// if(contact!=null&&contact.getMuteNotification() == 1){
			// return ;
			// }else if(singleOnOff){
			// return ;
			// }

		} else {
			GroupEntity group = IMGroupManager.instance().findGroup(peerId);
			if (group != null) {
				title = group.getMainName();
				avatarUrl = group.getAvatar();
			} else {
				title = "Group_" + peerId;
				avatarUrl = "";
			}

			// //如果是免打扰则不现实状态通知
			// if(group!=null&&group.getMuteNotification() == 1){
			// return ;
			// }else if(singleOnOff){
			// return ;
			// }

		}
		// 获取头像
		avatarUrl = IMUIHelper.getRealAvatarUrl(avatarUrl);

		// final String ticker = String.format("[%d%s]%s: %s", totalUnread,
		// unit, title, content);
		final String ticker;
		SharedPreferences sp = ctx.getSharedPreferences("Notification",
				Activity.MODE_PRIVATE);
		boolean Notification = sp.getBoolean("Bool_Notification", true);

		if (!Notification) {
			String start = "你有";
			String end = "未读消息";
			ticker = String.format("%s%d%s", start, totalUnread, end); // "[%d%s]%s: %s",
																		// totalUnread,
																		// unit,
																		// title,
																		// content
		} else {
			ticker = String.format("[%d%s]%s: %s", totalUnread, unit, title,
					content);
		}
		final int notificationId = getSessionNotificationId(unreadEntity
				.getSessionKey());
		final Intent intent = new Intent(ctx, MessageActivity.class);
		intent.putExtra(IntentConstant.KEY_SESSION_KEY,
				unreadEntity.getSessionKey());

		logger.d("notification#notification avatarUrl:%s", avatarUrl);
		final String finalTitle = title;
		ImageLoader.getInstance().loadImage(avatarUrl, targetSize, null,
				new SimpleImageLoadingListener() {

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						logger.d("notification#icon onLoadComplete");
						// holder.image.setImageBitmap(loadedImage);
						showInNotificationBar(finalTitle, ticker, loadedImage,
								notificationId, intent, totalUnread,
								unreadEntity);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						logger.d("notification#icon onLoadFailed");
						// 服务器支持的格式有哪些
						// todo eric default avatar is too small, need big
						// size(128 * 128)
						Bitmap defaultBitmap = BitmapFactory.decodeResource(ctx
								.getResources(), IMUIHelper
								.getDefaultAvatarResId(unreadEntity
										.getSessionType()));
						showInNotificationBar(finalTitle, ticker,
								defaultBitmap, notificationId, intent,
								totalUnread, unreadEntity);
					}
				});
	}

	private void showInNotificationBar(String title, String ticker,
			Bitmap iconBitmap, int notificationId, Intent intent, int total) {
		logger.d("notification#showInNotificationBar title:%s ticker:%s",
				title, ticker);

		NotificationManager notifyMgr = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notifyMgr == null) {
			return;
		}

		Builder builder = new NotificationCompat.Builder(ctx);
		builder.setContentTitle(title);
		builder.setContentText(ticker);
		builder.setSmallIcon(R.drawable.icon);
		builder.setTicker(ticker);
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);

		// this is the content near the right bottom side
		// builder.setContentInfo("content info");

		if (configurationSp.getCfg(SysConstant.SETTING_GLOBAL,
				ConfigurationSp.CfgDimension.VIBRATION)) {
			// delay 0ms, vibrate 200ms, delay 250ms, vibrate 200ms
			long[] vibrate = { 0, 200, 250, 200 };
			builder.setVibrate(vibrate);
		} else {
			logger.d("notification#setting is not using vibration");
		}

		// sound
		if (configurationSp.getCfg(SysConstant.SETTING_GLOBAL,
				ConfigurationSp.CfgDimension.SOUND)) {
			builder.setDefaults(Notification.DEFAULT_SOUND);
		} else {
			logger.d("notification#setting is not using sound");
		}
		if (iconBitmap != null) {
			logger.d("notification#fetch icon from network ok");
			builder.setLargeIcon(iconBitmap);
		} else {
			// do nothint ?
		}
		// if MessageActivity is in the background, the system would bring it to
		// the front
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(ctx,
				notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		Notification notification = builder.build();

		if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")
				|| Build.MANUFACTURER.equalsIgnoreCase("sony")
				|| Build.MANUFACTURER.toLowerCase().contains("samsung")) {
			// sendBadgeNumber(ctx,total +"",notificationId);
			sendIconNumber(ctx, "", "", total, notification, notificationId);
		} else {
			// notifyMgr.notify(notificationId, notification);

			/*
			try {

				java.lang.reflect.Field field = notification.getClass()
						.getDeclaredField("extraNotification");

				Object extraNotification = field.get(notification);

				Method method = extraNotification.getClass().getDeclaredMethod(
						"setMessageCount", int.class);

				method.invoke(extraNotification, total);

			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
			notifyMgr.notify(notificationId, notification);

		}
	}

	private void showInNotificationBar(String title, String ticker,
			Bitmap iconBitmap, int notificationId, Intent intent, int total,
			UnreadEntity unreadEntity) {
		logger.d("notification#showInNotificationBar title:%s ticker:%s",
				title, ticker);

		NotificationManager notifyMgr = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notifyMgr == null) {
			return;
		}

		Builder builder = new NotificationCompat.Builder(ctx);
		builder.setContentTitle(title);
		builder.setContentText(ticker);
		builder.setSmallIcon(R.drawable.icon);
		builder.setTicker(ticker);
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);

		// this is the content near the right bottom side
		// builder.setContentInfo("content info");

		if (configurationSp.getCfg(SysConstant.SETTING_GLOBAL,
				ConfigurationSp.CfgDimension.VIBRATION)) {
			// delay 0ms, vibrate 200ms, delay 250ms, vibrate 200ms
			long[] vibrate = { 0, 200, 250, 200 };
			builder.setVibrate(vibrate);
		} else {
			logger.d("notification#setting is not using vibration");
		}

		// sound
		if (configurationSp.getCfg(SysConstant.SETTING_GLOBAL,
				ConfigurationSp.CfgDimension.SOUND)) {
			builder.setDefaults(Notification.DEFAULT_SOUND);
		} else {
			logger.d("notification#setting is not using sound");
		}
		if (iconBitmap != null) {
			logger.d("notification#fetch icon from network ok");
			builder.setLargeIcon(iconBitmap);
		} else {
			// do nothint ?
		}

		// if MessageActivity is in the background, the system would bring it to
		// the front
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(ctx,
				notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		Notification notification = builder.build();

		if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")
				|| Build.MANUFACTURER.equalsIgnoreCase("sony")
				|| Build.MANUFACTURER.toLowerCase().contains("samsung")) {
			// sendBadgeNumber(ctx,total +"",notificationId);
			sendIconNumber(ctx, "", "", total, notification, notificationId);
		} else {
			// notifyMgr.notify(notificationId, notification);

			/*
			try {

				java.lang.reflect.Field field = notification.getClass()
						.getDeclaredField("extraNotification");

				Object extraNotification = field.get(notification);

				Method method = extraNotification.getClass().getDeclaredMethod(
						"setMessageCount", int.class);

				method.invoke(extraNotification, total);

			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
			notifyMgr.notify(notificationId, notification);

		}
	}

	// come from
	// http://www.partow.net/programming/hashfunctions/index.html#BKDRHashFunction
	private long hashBKDR(String str) {
		long seed = 131; // 31 131 1313 13131 131313 etc..
		long hash = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = (hash * seed) + str.charAt(i);
		}
		return hash;
	}

	/* End Of BKDR Hash Function */
	public int getSessionNotificationId(String sessionKey) {
		logger.d("notification#getSessionNotificationId sessionTag:%s",
				sessionKey);
		int hashedNotificationId = (int) hashBKDR(sessionKey);
		logger.d("notification#hashedNotificationId:%d", hashedNotificationId);
		return hashedNotificationId;
	}

	/**
	 * 设置桌面Icon角标，当前支持小米，索尼，三星。
	 * 
	 * @param context
	 * @param number
	 */
	public static void sendBadgeNumber(Context context, String number,
			int notificationId) {
		if (TextUtils.isEmpty(number)) {
			number = "0";
		} else {
			int numInt = Integer.valueOf(number);
			number = String.valueOf(Math.max(0, Math.min(numInt, 99)));
		}

		if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
			sendToXiaoMi(context, number, notificationId);
		} else if (Build.MANUFACTURER.equalsIgnoreCase("sony")) {
			sendToSony(context, number);
		} else if (Build.MANUFACTURER.toLowerCase().contains("samsung")) {
			sendToSamsumg(context, number);
		} else {
			Log.e("AppUtils", "sendBadgeNumber: Not Support");
		}
	}

	/**
	 * 创建通知小米系统设置桌面icon数字角标 官网文档
	 * 
	 * @param context
	 * @param title
	 * @param content
	 * @param number
	 */
	public static void sendIconNumber(Context context, String title,
			String content, int number, Notification notification,
			int notificationId) {
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// Notification.Builder builder = new Notification.Builder(context)
		// .setContentTitle(title).setContentText(content).setSmallIcon(R.drawable.ic_launcher);
		// //设置通知点击后意图
		// Intent intent = new Intent(context, MainActivity.class);
		// PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
		// intent, 0);
		// builder.setContentIntent(pendingIntent);
		//
		// Notification notification = builder.build();

		try {
			java.lang.reflect.Field field = notification.getClass()
					.getDeclaredField("extraNotification");
			Object extraNotification = field.get(notification);
			Method method = extraNotification.getClass().getDeclaredMethod(
					"setMessageCount", int.class);
			method.invoke(extraNotification, number);

		} catch (Exception e) {
			e.printStackTrace();
		}
		mNotificationManager.notify(notificationId, notification);
	}

	private final static String lancherActivityClassName = MainActivity.class
			.getName();

	private static void sendToXiaoMi(Context context, String number,
			int notificationId) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = null;
		boolean isMiUIV6 = true;
		try {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					context);
			builder.setContentTitle("您有" + number + "未读消息");
			builder.setTicker("您有" + number + "未读消息");
			builder.setAutoCancel(true);
			builder.setSmallIcon(R.drawable.ic_launcher);
			builder.setDefaults(Notification.DEFAULT_LIGHTS);
			notification = builder.build();
			Class miuiNotificationClass = Class
					.forName("android.app.MiuiNotification");
			Object miuiNotification = miuiNotificationClass.newInstance();
			java.lang.reflect.Field field = miuiNotification.getClass()
					.getDeclaredField("messageCount");
			field.setAccessible(true);
			field.set(miuiNotification, Integer.valueOf(number));// 设置信息数
			field = notification.getClass().getField("extraNotification");
			field.setAccessible(true);
			field.set(notification, miuiNotification);
		} catch (Exception e) { 
			e.printStackTrace();
			// miui 6之前的版本
			isMiUIV6 = false;
			Intent localIntent = new Intent(
					"android.intent.action.APPLICATION_MESSAGE_UPDATE");
			localIntent.putExtra(
					"android.intent.extra.update_application_component_name",
					context.getPackageName() + "/" + lancherActivityClassName);
			localIntent.putExtra(
					"android.intent.extra.update_application_message_text",
					number);
			context.sendBroadcast(localIntent);
		} finally {
			if (notification != null && isMiUIV6) {
				// miui6以上版本需要使用通知发送
				nm.notify(notificationId, notification);
			}
		}

	}

	private static void sendToSony(Context context, String number) {
		boolean isShow = true;
		if ("0".equals(number)) {
			isShow = false;
		}
		Intent localIntent = new Intent();
		localIntent
				.putExtra(
						"com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE",
						isShow);// 是否显示
		localIntent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
		localIntent.putExtra(
				"com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME",
				lancherActivityClassName);// 启动页
		localIntent.putExtra(
				"com.sonyericsson.home.intent.extra.badge.MESSAGE", number);// 数字
		localIntent.putExtra(
				"com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME",
				context.getPackageName());// 包名
		context.sendBroadcast(localIntent);
	}

	private static void sendToSamsumg(Context context, String number) {
		Intent localIntent = new Intent(
				"android.intent.action.BADGE_COUNT_UPDATE");
		localIntent.putExtra("badge_count", Integer.valueOf(number));// 数字
		localIntent.putExtra("badge_count_package_name",
				context.getPackageName());// 包名
		localIntent
				.putExtra("badge_count_class_name", lancherActivityClassName); // 启动页
		context.sendBroadcast(localIntent);
		Log.d("AppUtils", "Samsumg isSendOk" + number);
	}
}

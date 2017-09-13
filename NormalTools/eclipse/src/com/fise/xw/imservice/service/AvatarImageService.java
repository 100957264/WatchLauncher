package com.fise.xw.imservice.service;

import java.io.File;
import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.sp.SystemConfigSp;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.SysConstant;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.manager.IMDeviceManager;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.protobuf.IMBaseDefine.ChangeDataType; 
import com.fise.xw.protobuf.IMDevice.SettingType;
import com.fise.xw.ui.helper.PhotoHelper;
import com.fise.xw.utils.AvatarHttpClient;
import com.fise.xw.utils.FileUtil;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;

/**
 * @author : yingmu on 15-1-12.
 * @email : yingmu@mogujie.com.
 * 
 */
public class AvatarImageService extends IntentService {

	private static Logger logger = Logger.getLogger(AvatarImageService.class);
	private int nick_mode;
	private int currentUserId;
	public static String avatar;

	public AvatarImageService() {
		super("LoadImageService");
	}

	public AvatarImageService(String name) {
		super(name);
	}

	/**
	 * This method is invoked on the worker thread with a request to process.
	 * Only one Intent is processed at a time, but the processing happens on a
	 * worker thread that runs independently from other application logic. So,
	 * if this code takes a long time, it will hold up other requests to the
	 * same IntentService, but it will not hold up anything else. When all
	 * requests have been handled, the IntentService stops itself, so you should
	 * not call {@link #stopSelf}.
	 * 
	 * @param intent
	 *            The value passed to
	 *            {@link android.content.Context#startService(android.content.Intent)}
	 *            .
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		String avatarInfo = (String) intent
				.getSerializableExtra(SysConstant.UPLOAD_IMAGE_INTENT_PARAMS_AVATAR);
		nick_mode = intent.getIntExtra(IntentConstant.KEY_NICK_MODE, 0);
		currentUserId = intent.getIntExtra(IntentConstant.KEY_PEERID, 0);

		String result = null;
		Bitmap bitmap;
		try {
			File file = new File(avatarInfo);
			if (file.exists()
					&& FileUtil.getExtensionName(avatarInfo).toLowerCase()
							.equals(".gif")) {
				AvatarHttpClient httpClient = new AvatarHttpClient();
				SystemConfigSp.instance().init(getApplicationContext());
				result = httpClient.uploadImage3(
						SystemConfigSp.instance().getStrConfig(
								SystemConfigSp.SysCfgDimension.MSFSSERVER),
						FileUtil.File2byte(avatarInfo), avatarInfo);
			} else {
				bitmap = PhotoHelper.revitionImage(avatarInfo);

				if (null != bitmap) {
					AvatarHttpClient httpClient = new AvatarHttpClient();
					byte[] bytes = PhotoHelper.getBytes(bitmap);
					result = httpClient.uploadImage3(
							SystemConfigSp.instance().getStrConfig(
									SystemConfigSp.SysCfgDimension.MSFSSERVER),
							bytes, avatarInfo);
				}
			}

			if (TextUtils.isEmpty(result)) {
				logger.i("upload image faild,cause by result is empty/null");
				// EventBus.getDefault().post(new
				// MessageEvent(MessageEvent.Event.IMAGE_UPLOAD_FAILD
				// ,messageInfo));
			} else {
				logger.i("upload image succcess,imageUrl is %s", result);
				String imageUrl = result;
				// messageInfo.setUrl(imageUrl);
				// EventBus.getDefault().post(new MessageEvent(
				// MessageEvent.Event.IMAGE_UPLOAD_SUCCESS
				// ,messageInfo));

				if (nick_mode == DBConstant.DEVICE_NICK) {

					UserEntity currentUser = IMContactManager.instance()
							.findDeviceContact(currentUserId);

					IMDeviceManager.instance().settingPhone(currentUser,
							currentUser, imageUrl,
							SettingType.SETTING_TYPE_DEVICE_AVATAR,
							AvatarImageService.avatar);

				} else {
					UserEntity user = IMLoginManager.instance().getLoginInfo();
					IMContactManager.instance().ChangeUserInfo(
							user.getPeerId(),
							ChangeDataType.CHANGE_USERINFO_AVATAR, imageUrl);

				}

			}
		} catch (IOException e) {
			logger.e(e.getMessage());
		}
	}

	/**
	 * @param event
	 */
	public void triggerEvent(UserInfoEvent event) {
		EventBus.getDefault().postSticky(event);
	}

}

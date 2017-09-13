package com.fise.xiaoyu.imservice.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.SystemConfigSp;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMDeviceManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xiaoyu.protobuf.IMDevice.SettingType;
import com.fise.xiaoyu.ui.helper.PhotoHelper;
import com.fise.xiaoyu.utils.AvatarHttpClient;
import com.fise.xiaoyu.utils.FileUtil;
import com.fise.xiaoyu.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

/**
 * 个人头像上传
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

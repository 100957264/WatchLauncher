package com.fise.xiaoyu.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.fise.xiaoyu.DB.entity.CommonUserInfo;
import com.fise.xiaoyu.DB.entity.FamilyConcernEntity;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.entity.WhiteEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.UrlConstant;
import com.fise.xiaoyu.imservice.entity.SearchElement;
import com.fise.xiaoyu.imservice.entity.VedioMessage;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.SocketEvent;
import com.fise.xiaoyu.imservice.manager.IMDeviceManager;
import com.fise.xiaoyu.protobuf.IMDevice.SettingType;
import com.fise.xiaoyu.ui.activity.AboutActivity;
import com.fise.xiaoyu.ui.activity.AccountProtectionActivity;
import com.fise.xiaoyu.ui.activity.AccountSecurityActivity;
import com.fise.xiaoyu.ui.activity.ActivityLoginInfo;
import com.fise.xiaoyu.ui.activity.AddDeviceActivity;
import com.fise.xiaoyu.ui.activity.AddFriendActivity;
import com.fise.xiaoyu.ui.activity.AuthSelectActivity;
import com.fise.xiaoyu.ui.activity.AvatarActivity;
import com.fise.xiaoyu.ui.activity.BlackListInfoActivity;
import com.fise.xiaoyu.ui.activity.CardFriednsActivity;
import com.fise.xiaoyu.ui.activity.CityListActivity;
import com.fise.xiaoyu.ui.activity.CurrencyActivity;
import com.fise.xiaoyu.ui.activity.DeviceBirthdayActivity;
import com.fise.xiaoyu.ui.activity.DeviceFollowActivity;
import com.fise.xiaoyu.ui.activity.DeviceHeightActivity;
import com.fise.xiaoyu.ui.activity.DeviceInfoActivity;
import com.fise.xiaoyu.ui.activity.DeviceInfoBells;
import com.fise.xiaoyu.ui.activity.DeviceInfoSettingActivity;
import com.fise.xiaoyu.ui.activity.DevicePhoneActivity;
import com.fise.xiaoyu.ui.activity.DeviceTaskListActivity;
import com.fise.xiaoyu.ui.activity.DeviceWorkingMode;
import com.fise.xiaoyu.ui.activity.EmailActivity;
import com.fise.xiaoyu.ui.activity.EmergencyActivity;
import com.fise.xiaoyu.ui.activity.FamilyFollowActivity;
import com.fise.xiaoyu.ui.activity.FriendsSelectActivity;
import com.fise.xiaoyu.ui.activity.GroupFriendsActivity;
import com.fise.xiaoyu.ui.activity.GroupListActivity;
import com.fise.xiaoyu.ui.activity.GroupMemberActivity;
import com.fise.xiaoyu.ui.activity.GroupMemberSelectActivity;
import com.fise.xiaoyu.ui.activity.GroupNameActivity;
import com.fise.xiaoyu.ui.activity.GroupNoticeActivity;
import com.fise.xiaoyu.ui.activity.GroupQRActivity;
import com.fise.xiaoyu.ui.activity.GroupSelectActivity;
import com.fise.xiaoyu.ui.activity.HelpActivity;
import com.fise.xiaoyu.ui.activity.HistoryQueryActivity;
import com.fise.xiaoyu.ui.activity.HistoryTrackActivity;
import com.fise.xiaoyu.ui.activity.LessonRemindDetailActivity;
import com.fise.xiaoyu.ui.activity.LightTimeActivity;
import com.fise.xiaoyu.ui.activity.LoginInfoNickName;
import com.fise.xiaoyu.ui.activity.LoginInfoSex;
import com.fise.xiaoyu.ui.activity.LoginInfoSigned;
import com.fise.xiaoyu.ui.activity.MainActivity;
import com.fise.xiaoyu.ui.activity.MediaPlayerActivity;
import com.fise.xiaoyu.ui.activity.MessageActivity;
import com.fise.xiaoyu.ui.activity.MessageBgActivity;
import com.fise.xiaoyu.ui.activity.MessageNotifyActivity;
import com.fise.xiaoyu.ui.activity.MessageSearchActivity;
import com.fise.xiaoyu.ui.activity.NewFriendActivity;
import com.fise.xiaoyu.ui.activity.NickNameActivity;
import com.fise.xiaoyu.ui.activity.PassActivityVerify;
import com.fise.xiaoyu.ui.activity.ProvinceListActivity;
import com.fise.xiaoyu.ui.activity.RealTimeInfoActivity;
import com.fise.xiaoyu.ui.activity.ReqYuFriendsListActivity;
import com.fise.xiaoyu.ui.activity.ReqYuMessageActivity;
import com.fise.xiaoyu.ui.activity.SearchActivity;
import com.fise.xiaoyu.ui.activity.SearchMessageActivity;
import com.fise.xiaoyu.ui.activity.SelectGroupListActivity;
import com.fise.xiaoyu.ui.activity.SelectSchoolActivity;
import com.fise.xiaoyu.ui.activity.SettingPrivacyActivity;
import com.fise.xiaoyu.ui.activity.ShowSettingActivity;
import com.fise.xiaoyu.ui.activity.StepCounterActivity;
import com.fise.xiaoyu.ui.activity.SweetRemindDetailActivity;
import com.fise.xiaoyu.ui.activity.UpdateTelActivity;
import com.fise.xiaoyu.ui.activity.UserInfoActivity;
import com.fise.xiaoyu.ui.activity.UserInfoFollowActivity;
import com.fise.xiaoyu.ui.activity.UserInfoRemarks;
import com.fise.xiaoyu.ui.activity.UserInfoSigned;
import com.fise.xiaoyu.ui.activity.WarningActivity;
import com.fise.xiaoyu.ui.activity.WeiGroupSelectActivity;
import com.fise.xiaoyu.ui.activity.WhiteListActivity;
import com.fise.xiaoyu.ui.activity.YuFriendListActivity;
import com.fise.xiaoyu.ui.widget.ListLayoutDialog;
import com.fise.xiaoyu.ui.widget.PassDialog;
import com.fise.xiaoyu.utils.pinyin.PinYin.PinYinElement;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

@SuppressLint("NewApi")
public class IMUIHelper {

	// 在视图中，长按用户信息条目弹出的对话框
	public static void handleContactItemLongClick(final UserEntity contact,
			final Context ctx) {
		if (contact == null || ctx == null) {
			return;
		}

		String name;
		if (contact.getComment().equals("")) {
			name = contact.getMainName();
		} else {
			name = contact.getComment();
		}

//		AlertDialog.Builder builder = new AlertDialog.Builder(
//				new ContextThemeWrapper(ctx,
//						android.R.style.Theme_Holo_Light_Dialog));
        ListLayoutDialog listLayoutDialog = new ListLayoutDialog(ctx);
		// builder.setTitle(name);
		String[] items = new String[] { ctx.getString(R.string.check_profile),
				ctx.getString(R.string.start_session) };

		final int userId = contact.getPeerId();
		final int friendsType = contact.getIsFriend();
        listLayoutDialog.setItems(items,  new ListLayoutDialog.onItemClickListener() {

			@Override
            public void onClick(int item, Dialog dialog) {
				switch (item) {
				case 0: {
					boolean isWeiFriends = false;
					if (friendsType == DBConstant.FRIENDS_TYPE_YUYOU) {
						isWeiFriends = true;
					}
					IMUIHelper.openUserProfileActivity(ctx, userId,
							isWeiFriends);
				}

					break;
				case 1:
					IMUIHelper.openChatActivity(ctx, contact.getSessionKey());
					break;
				}

                dialog.dismiss();
			}
		});
//		AlertDialog alertDialog = builder.create();
//		alertDialog.setCanceledOnTouchOutside(true);
//		alertDialog.show();
	}

	protected static void showAddDialog(Context ctx, final UserEntity dev,
			final UserEntity contact) {

		LayoutInflater factory = LayoutInflater.from(ctx);

		final PassDialog myDialog = new PassDialog(ctx,PassDialog.PASS_DIALOG_TYPE.PASS_DIALOG_WITHOUT_MESSAGE);
		myDialog.setTitle("身份设置");// 设置标题
		// myDialog.setMessage("为保障你的数据安全,修改密码请填写原始密码。");//设置内容

		myDialog.dialog.show();// 显示

		// 确认按键回调，按下确认后在此做处理
		myDialog.setMyDialogOnClick(new PassDialog.MyDialogOnClick() {
			@Override
			public void ok() {
				String tt = myDialog.getEditText().getText().toString();
				if (myDialog.getEditText().getText().toString() != "") {

					IMDeviceManager.instance().settingPhone(dev, contact,
							myDialog.getEditText().getText().toString(),
							SettingType.SETTING_TYPE_FAMILIT_NAME, "");
					myDialog.dialog.dismiss();
				} else {

				}
			}
		});
	}

	// 在视图中，长按设备设置的信息
	public static void handleDeviceItemLongClick(final String device,
			final Context ctx, final UserEntity object,
			final FamilyConcernEntity family, final UserEntity dev) {
		if (ctx == null) {
			return;
		}


		ListLayoutDialog listLayoutDialog = new ListLayoutDialog(ctx);
		// builder.setTitle("删除授权人");
//		String[] items = new String[] { ctx.getString(R.string.family_concern),
//				ctx.getString(R.string.device_delete_follow) };
		String[] items = new String[] {
				ctx.getString(R.string.device_delete_follow) };
//		listLayoutDialog.setItems(items, new ListLayoutDialog.onItemClickListener(){
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				switch (which) {
////				case 0: {
////					showAddDialog(ctx, dev, object);
////
////				}
////
////					break;
//				case 0: {
//					// IMDeviceManager.instance().settingDeleteWhite(device,phone,type,DBConstant.DELTE);
//					IMDeviceManager.instance().authDeleteDevice(device, object,
//							family);
//				}
//
//					break;
//				}
//			}
//		});
		listLayoutDialog.setItems(items , new ListLayoutDialog.onItemClickListener(){

			@Override
			public void onClick(int item, Dialog dialog) {
				if(item == 0 ){
					IMDeviceManager.instance().authDeleteDevice(device, object,
							family);

					dialog.dismiss();
				}
			}
		});

	}

	// 在视图中，长按设备设置的信息
	public static void handleDeviceItemLongClick(final int device,
			final WhiteEntity phone, final SettingType type, final Context ctx) {
		if (phone == null || ctx == null) {
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(ctx,
						android.R.style.Theme_Holo_Light_Dialog));
		// builder.setTitle("删除号码");
		String[] items = new String[] { ctx.getString(R.string.device_delete_phone) };

		builder.setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0: {
					IMDeviceManager.instance().settingDeleteWhite(device,
							phone, type, DBConstant.DELTE);
				}

					break;
				}
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.setCanceledOnTouchOutside(true);
		alertDialog.show();
	}

	// 根据event 展示提醒文案
	public static int getLoginErrorTip(LoginEvent event) {
		switch (event) {
		case LOGIN_AUTH_FAILED:
			return R.string.login_error_general_failed;
		case LOGIN_INNER_FAILED:
			return R.string.login_error_unexpected;
		default:
			return R.string.login_error_unexpected;
		}
	}

	public static int getSocketErrorTip(SocketEvent event) {
		switch (event) {
		case CONNECT_MSG_SERVER_FAILED:
			return R.string.connect_msg_server_failed;
		case REQ_MSG_SERVER_ADDRS_FAILED:
			return R.string.req_msg_server_addrs_failed;
		default:
			return R.string.login_error_unexpected;
		}
	}

	// 跳转到通用全页面
	public static void openCurrencyActivity(Context ctx) {
		Intent intent = new Intent(ctx, CurrencyActivity.class);
		ctx.startActivity(intent);
	}

	// 跳转到账号安全页面
	public static void openAccountSecurityActivity(Context ctx) {
		Intent intent = new Intent(ctx, AccountSecurityActivity.class);
		ctx.startActivity(intent);
	}

	// 跳转到关于页面
	public static void openAboutActivity(Context ctx) {
		Intent intent = new Intent(ctx, AboutActivity.class);
		ctx.startActivity(intent);
	}



	// 跳转到雨友消息中心页面
	public static void openYuMessageActivity(Context ctx) {
		Intent intent = new Intent(ctx, ReqYuMessageActivity.class);
		ctx.startActivity(intent);
	}

	// 跳转到雨友消息中心列表页面
	public static void openYuMessageListActivity(Context ctx) {
		Intent intent = new Intent(ctx, ReqYuFriendsListActivity.class);
		ctx.startActivity(intent);
	}


	// 跳转到主界面
	public static void openMainActivity(Context ctx , int fragmentID) {
		Intent intent = new Intent(ctx, MainActivity.class);
		intent.putExtra("fragment_id" ,fragmentID );
		ctx.startActivity(intent);
	}

	// 跳转到消息推送设置界面
	public static void openMessageNotifyActivity(Context ctx) {
		Intent intent = new Intent(ctx, MessageNotifyActivity.class);
		ctx.startActivity(intent);
	}

	// 跳转到帮助与反馈页面
	public static void openHelpActivity(Context ctx) {
		Intent intent = new Intent(ctx, HelpActivity.class);
		ctx.startActivity(intent);
	}

	// 跳转到设置页面
	public static void openSettingActivity(Context ctx) {
		Intent intent = new Intent(ctx, ShowSettingActivity.class);
		ctx.startActivity(intent);
	}

	// 跳转到昵称修改页面
	public static void openNickNameActivity(Context ctx) {
		Intent intent = new Intent(ctx, NickNameActivity.class);
		ctx.startActivity(intent);
	}

	// 跳转到邮箱地址页面
	public static void openEmailActivity(Context ctx) {
		Intent intent = new Intent(ctx, EmailActivity.class);
		ctx.startActivity(intent);
	}

	// 跳转到更换手机号码
	public static void openUpdateTel(Context ctx) {
		Intent intent = new Intent(ctx, UpdateTelActivity.class);
		ctx.startActivity(intent);
	}

	// 跳转到账号保护
	public static void openAccountProtection(Context ctx) {
		Intent intent = new Intent(ctx, AccountProtectionActivity.class);
		ctx.startActivity(intent);
	}

	// 跳转到聊天页面
	public static void openChatActivity(Context ctx, String sessionKey) {
		Intent intent = new Intent(ctx, MessageActivity.class);
		intent.putExtra(IntentConstant.KEY_SESSION_KEY, sessionKey);
		intent.putExtra(IntentConstant.KEY_SCROLL_KEY, 0);

		ctx.startActivity(intent);
	}

	// 跳转到聊天页面
	public static void openChaSearchtActivity(Context ctx, String sessionKey,
			int index) {
		Intent intent = new Intent(ctx, MessageSearchActivity.class);
		intent.putExtra(IntentConstant.KEY_SESSION_KEY, sessionKey);
		intent.putExtra(IntentConstant.KEY_INDEX_KEY, index);
		ctx.startActivity(intent);
	}


	// 跳转到计步页面
	public static void openStepCounterActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, StepCounterActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}


	// 跳转到设备信息页面
	public static void openDeviceProfileActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, DeviceInfoActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}


	// 跳转到雨友列表
	public static void openYuFriendsActivity(Context ctx) {
		Intent intent = new Intent(ctx, YuFriendListActivity.class);
		ctx.startActivity(intent);
	}


	// 跳转到设备信息页面
	public static void openDeviceInfoActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, DeviceFollowActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 跳转到设备工作模式选择页面
	public static void openDeviceWorkingModeActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, DeviceWorkingMode.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}


	// 跳转到设备亮屏时间设置页面
	public static void openDeviceLightTimeActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, LightTimeActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 跳转到设备铃声选择页面
	public static void openDeviceBellsActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, DeviceInfoBells.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 跳转到设备信息页面
	public static void openDeviceSettingActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, DeviceInfoSettingActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}


	// 跳转到设备身高体重信息页面
	public static void openDeviceHeightActivity(Context ctx, int contactId,int type) {
		Intent intent = new Intent(ctx, DeviceHeightActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		intent.putExtra(IntentConstant.DEVICE_HEIGHT, type);

		ctx.startActivity(intent);
	}


	// 跳转到设备身高体重信息页面
	public static void openDeviceBirthdayActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, DeviceBirthdayActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);

		ctx.startActivity(intent);
	}



	// 跳转到用户备注
	public static void openUserInfoRemarks(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, UserInfoRemarks.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 跳转到城市列表页面
	public static void openCityListActivity(Context ctx, int contactId,
			String province) {
		Intent intent = new Intent(ctx, CityListActivity.class);
		intent.putExtra(IntentConstant.KEY_PROVINCE_ID, contactId);
		intent.putExtra(IntentConstant.KEY_PROVINCE_NAME, province);
		ctx.startActivity(intent);
	}

	// 跳转到設置背景页面
	public static void openMessageBgActivity(Context ctx) {
		Intent intent = new Intent(ctx, MessageBgActivity.class);
		ctx.startActivity(intent);
	}

	// 跳转到查看全部成员列表
	public static void openGroupMemberActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, GroupMemberActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}



	// 跳转到用户信息页面
	public static void openUserProfileActivity(Context ctx, int contactId,
			boolean isWeiFriends) {

		Intent intent = new Intent(ctx, UserInfoFollowActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 跳转到用户信息页面
	public static void openUserInfoActivity(Context ctx, int contactId) {

		Intent intent = new Intent(ctx, UserInfoActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 跳转到用户信息页面
	public static void openBlackListInfoActivity(Context ctx, int contactId) {

		Intent intent = new Intent(ctx, BlackListInfoActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 跳转到用户信息页面
	public static void openCardInfoActivity(Context ctx, int contactId) {

		Intent intent = new Intent(ctx, CardFriednsActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 跳转到隱私页面
	public static void openPrivacyActivity(Context ctx) {

		Intent intent = new Intent(ctx, SettingPrivacyActivity.class);
		ctx.startActivity(intent);
	}

	public static void openGroupNameActivity(Context ctx, String sessionKey) {
		Intent intent = new Intent(ctx, GroupNameActivity.class);
		intent.putExtra(IntentConstant.KEY_SESSION_KEY, sessionKey);
		ctx.startActivity(intent);
	}

	public static void openGroupQRActivity(Context ctx, String sessionKey) {
		Intent intent = new Intent(ctx, GroupQRActivity.class);
		intent.putExtra(IntentConstant.KEY_SESSION_KEY, sessionKey);
		ctx.startActivity(intent);
	}

	// 群公告
	public static void openGroupNoticeActivity(Context ctx, String sessionKey) {
		Intent intent = new Intent(ctx, GroupNoticeActivity.class);
		intent.putExtra(IntentConstant.KEY_SESSION_KEY, sessionKey);
		ctx.startActivity(intent);
	}

	// 跳转到加好友列表页面
	public static void openFriendActivity(Context ctx) {
		Intent intent = new Intent(ctx, NewFriendActivity.class);
		ctx.startActivity(intent);

	}

	// 跳转到加群组列表页面
	public static void openGroupActivity(Context ctx) {
		Intent intent = new Intent(ctx, GroupListActivity.class);
		ctx.startActivity(intent);

	}

	// 跳转到选择组列表页面
	public static void openSelectGroupActivity(Context ctx) {
		Intent intent = new Intent(ctx, SelectGroupListActivity.class);
		ctx.startActivity(intent);

	}


	// 跳转到城市列表页面
	public static void openCityListActivity(Context ctx) {
		Intent intent = new Intent(ctx, ProvinceListActivity.class);
		ctx.startActivity(intent);

	}

	// 跳转个人信息页面
	public static void openLoginInfoActivity(Context ctx) {
		Intent intent = new Intent(ctx, ActivityLoginInfo.class);
		ctx.startActivity(intent);

	}

	// 跳转设置个人性别页面
	public static void openLoginInfoSexActivity(Context ctx, int type,
			int contactId) {
		Intent intent = new Intent(ctx, LoginInfoSex.class);
		intent.putExtra(IntentConstant.SEX_INFO_TYPE, type);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);

	}

	// 跳转设置个性签名页面
	public static void openLoginInfoSignedActivity(Context ctx) {
		Intent intent = new Intent(ctx, LoginInfoSigned.class);
		ctx.startActivity(intent);

	}

	// 跳转设置个性签名页面
	public static void openUserInfoSignedActivity(Context ctx, String signed) {
		Intent intent = new Intent(ctx, UserInfoSigned.class);
		intent.putExtra(IntentConstant.KEY_PEERID_SIGNED, signed);
		ctx.startActivity(intent);

	}

	// 跳转设置昵称页面
	public static void openSetNickNameActivity(Context ctx, int modifyType,
			int contactId) {
		Intent intent = new Intent(ctx, LoginInfoNickName.class);
		intent.putExtra(IntentConstant.KEY_NICK_MODE, modifyType);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);

	}

	// 跳转到加好友页面
	public static void openAddFriendActivity(Context ctx) {
		Intent intent = new Intent(ctx, AddFriendActivity.class);
		ctx.startActivity(intent);

	}

	// 跳转到搜索页面
	public static void openSearchFriendActivity(Context ctx, int searchType) {
		Intent intent = new Intent(ctx, SearchActivity.class);
		intent.putExtra(IntentConstant.KEY_SEARCH_TYPE, searchType);
		ctx.startActivity(intent);

	}

	// 跳转到加搜索消息页面
	public static void openSearchMessageActivity(Context ctx, String sessionKey) {
		Intent intent = new Intent(ctx, SearchMessageActivity.class);
		intent.putExtra(IntentConstant.KEY_SESSION_KEY, sessionKey);
		ctx.startActivity(intent);

	}

	// 跳转到实时监护页面
	public static void openRealTimeActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, RealTimeInfoActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}


    //跳转到爱心提醒详情界面
	 public static void openSweetRemindDetailActivity(Context ctx, int contactId ,int taskID
	    ) {
		Intent intent = new Intent(ctx, SweetRemindDetailActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		intent.putExtra(IntentConstant.DEVICE_CRONTAB_ID, taskID);
//		Bundle bundle = new Bundle();
//		bundle.putSerializable(IntentConstant.FENCE_ENTITY, fenceEntity);
//		intent.putExtras(bundle);
		ctx.startActivity(intent);
	}

	//跳转到上课模式醒详情界面
	public static void openLessonRemindDetailActivity(Context ctx, int contactId ,int taskID
	) {
		Intent intent = new Intent(ctx, LessonRemindDetailActivity.class);

		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		intent.putExtra(IntentConstant.DEVICE_CRONTAB_ID, taskID);
//		Bundle bundle = new Bundle();
//		bundle.putSerializable(IntentConstant.FENCE_ENTITY, fenceEntity);
//		intent.putExtras(bundle);
		ctx.startActivity(intent);
	}

	// 跳转到安全围栏
	public static void openModifyPassActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, PassActivityVerify.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 跳转小视频
	public static void openVedioPlayActivity(Context ctx, String path,
			int type, VedioMessage vedioMessage ,Boolean isMute) {
		Intent intent = new Intent(ctx, MediaPlayerActivity.class);
		intent.putExtra(IntentConstant.VIDEO_PATH, path);
		intent.putExtra(IntentConstant.VIDEO_DOWN_TYPE, type);
		intent.putExtra(IntentConstant.VIDEO_DOWN_MESSAGE, vedioMessage);
		intent.putExtra(IntentConstant.PALY_VIDEO_MUTE , isMute);
		// intent.putExtra(IntentConstant.VIDEO_DOWN_MESSAGE, vedioMessage);

		ctx.startActivity(intent);
	}

	// 跳转到历史轨迹页面
	public static void openHistoryTrackActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, HistoryTrackActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 跳转到历史轨迹查詢页面
	public static void openHistoryQueryActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, HistoryQueryActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 跳转到警告界面
	public static void openWarningActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, WarningActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 跳转到群组中的朋友
	public static void opeGroupFriendsActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, GroupFriendsActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 跳转到加设备页面
	public static void openAddDeviceActivity(Context ctx,int schoolId) {
		Intent intent = new Intent(ctx, AddDeviceActivity.class);
		intent.putExtra(IntentConstant.SCHOOL_ID, schoolId);
		ctx.startActivity(intent);
	}

	// 跳转到选择学校页面
	public static void openSelectSchoolActivity(Context ctx) {
		Intent intent = new Intent(ctx, SelectSchoolActivity.class);
		ctx.startActivity(intent);
	}

	// 跳转到授权页面
	public static void openAuthSelectActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, AuthSelectActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);

	}

	public static void openGroupMemberSelectActivity(Context ctx,
			String sessionKey, int type) {
		Intent intent = new Intent(ctx, GroupMemberSelectActivity.class);
		intent.putExtra(IntentConstant.KEY_SESSION_KEY, sessionKey);
		intent.putExtra(IntentConstant.KEY_SESSION_KEY_TYPE, type);


		ctx.startActivity(intent);
	}

	public static void openGroupMemberSelectActivity(Context ctx,
			boolean showCheckBox) {
		//showCheckBox true 为选择好友通讯录名单
		Intent intent = new Intent(ctx, GroupSelectActivity.class);
		intent.putExtra(IntentConstant.HIDE_SELECT_CHECK_BOX, showCheckBox);
		ctx.startActivity(intent);
	}

	public static void openFriendsMemberSelectActivity(Context ctx) {
		Intent intent = new Intent(ctx, FriendsSelectActivity.class);
		ctx.startActivity(intent);
	}

	public static void openGroupWeiSelectActivity(Context ctx) {
		Intent intent = new Intent(ctx, WeiGroupSelectActivity.class);
		ctx.startActivity(intent);
	}

	// 亲情关注
	public static void openFamilyFollowActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, FamilyFollowActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 设备号码
	public static void openDevicePhonActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, DevicePhoneActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 白名單
	public static void openWhiteListActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, WhiteListActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	// 设置头像界面
	public static void openAvatarActivity(Context ctx) {
		Intent intent = new Intent(ctx, AvatarActivity.class);
		ctx.startActivity(intent);
	}

	// 紧急号码
	public static void openAlarmListActivity(Context ctx, int contactId) {
		Intent intent = new Intent(ctx, EmergencyActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		ctx.startActivity(intent);
	}

	public static void openSweetRemindSettingActivity(Context ctx, int contactId ,int taskType) {
		Intent intent = new Intent(ctx, DeviceTaskListActivity.class);
		intent.putExtra(IntentConstant.KEY_PEERID, contactId);
		intent.putExtra(IntentConstant.DEVICE_CRONTAB_TYPE, taskType);
		ctx.startActivity(intent);

	}

	// 对话框回调函数
	public interface dialogCallback {
		public void callback();
	}


	public static void callPhone(Context ctx, String phoneNumber) {
		if (ctx == null) {
			return;
		}
		if (phoneNumber == null || phoneNumber.isEmpty()) {
			return;
		}
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
				+ phoneNumber));

		ctx.startActivity(intent);
	}

	// 文字高亮显示
	public static void setTextHilighted(TextView textView, String text,
			SearchElement searchElement) {
		textView.setText(text);
		if (textView == null || TextUtils.isEmpty(text)
				|| searchElement == null) {
			return;
		}

		int startIndex = searchElement.startIndex;
		int endIndex = searchElement.endIndex;
		if (startIndex < 0 || endIndex > text.length()) {
			return;
		}
		// 开始高亮处理
		int color = Color.rgb(69, 192, 26);
		textView.setText(text, BufferType.SPANNABLE);
		Spannable span = (Spannable) textView.getText();
		span.setSpan(new ForegroundColorSpan(color), startIndex, endIndex,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	/**
	 * 如果图片路径是以 http开头,直接返回 如果不是， 需要集合自己的图像路径生成规律
	 * 
	 * @param avatarUrl
	 * @return
	 */
	public static String getRealAvatarUrl(String avatarUrl) {
		if (avatarUrl.toLowerCase().contains("http")) {
			return avatarUrl;
		} else if (avatarUrl.trim().isEmpty()) {
			return "";
		} else {
			return UrlConstant.AVATAR_URL_PREFIX + avatarUrl;
		}
	}

	// // search helper start
	// public static boolean handleDepartmentSearch(String key, DepartmentEntity
	// department) {
	// if (TextUtils.isEmpty(key) || department == null) {
	// return false;
	// }
	// department.getSearchElement().reset();
	//
	// return handleTokenFirstCharsSearch(key, department.getPinyinElement(),
	// department.getSearchElement())
	// || handleTokenPinyinFullSearch(key, department.getPinyinElement(),
	// department.getSearchElement())
	// || handleNameSearch(department.getDepartName(), key,
	// department.getSearchElement());
	// }

	public static boolean handleGroupSearch(String key, GroupEntity group) {
		if (TextUtils.isEmpty(key) || group == null) {
			return false;
		}
		group.getSearchElement().reset();

		return handleTokenFirstCharsSearch(key, group.getPinyinElement(),
				group.getSearchElement())
				|| handleTokenPinyinFullSearch(key, group.getPinyinElement(),
						group.getSearchElement())
				|| handleNameSearch(group.getMainName(), key,
						group.getSearchElement());
	}

	public static boolean handleContactSearch(String key, UserEntity contact) {
		if (TextUtils.isEmpty(key) || contact == null) {
			return false;
		}
		contact.getSearchElement().reset();

		return handleTokenFirstCharsSearch(key, contact.getPinyinElement(),
				contact.getSearchElement())
				|| handleTokenPinyinFullSearch(key, contact.getPinyinElement(),
						contact.getSearchElement())
				|| handleNameSearch(contact.getMainName(), key,
						contact.getSearchElement());
		// 原先是 contact.name 代表花名的意思嘛??
	}

	public static boolean handleContactSearch(String key, CommonUserInfo contact) {
		if (TextUtils.isEmpty(key) || contact == null) {
			return false;
		}
		contact.getSearchElement().reset();

		return handleTokenFirstCharsSearch(key, contact.getPinyinElement(),
				contact.getSearchElement())
				|| handleTokenPinyinFullSearch(key, contact.getPinyinElement(),
				contact.getSearchElement())
				|| handleNameSearch(contact.getUserName(), key,
				contact.getSearchElement());
		// 原先是 contact.name 代表花名的意思嘛??
	}

	public static boolean handleContactSearchMap(String key, UserEntity contact) {
		if (TextUtils.isEmpty(key) || contact == null) {
			return false;
		}

		contact.getSearchElement().reset();

		if (contact.getComment().equals("") != true) {
			return handleNameSearch(contact.getComment(), key,
					contact.getSearchElement());

		} else {
			return handleTokenFirstCharsSearch(key, contact.getPinyinElement(),
					contact.getSearchElement())
					|| handleTokenPinyinFullSearch(key,
							contact.getPinyinElement(),
							contact.getSearchElement())
					|| handleNameSearch(contact.getMainName(), key,
							contact.getSearchElement());
			// 原先是 contact.name 代表花名的意思嘛??
		}

	}

	public static boolean handleNameSearch(String name, String key,
			SearchElement searchElement) {
		int index = name.indexOf(key);
		if (index == -1) {
			return false;
		}

		searchElement.startIndex = index;
		searchElement.endIndex = index + key.length();

		return true;
	}

	public static boolean handleTokenFirstCharsSearch(String key,
			PinYinElement pinYinElement, SearchElement searchElement) {
		return handleNameSearch(pinYinElement.tokenFirstChars,
				key.toUpperCase(), searchElement);
	}

	public static boolean handleTokenPinyinFullSearch(String key,
			PinYinElement pinYinElement, SearchElement searchElement) {
		if (TextUtils.isEmpty(key)) {
			return false;
		}

		String searchKey = key.toUpperCase();

		// onLoginOut the old search result
		searchElement.reset();

		int tokenCnt = pinYinElement.tokenPinyinList.size();
		int startIndex = -1;
		int endIndex = -1;

		for (int i = 0; i < tokenCnt; ++i) {
			String tokenPinyin = pinYinElement.tokenPinyinList.get(i);

			int tokenPinyinSize = tokenPinyin.length();
			int searchKeySize = searchKey.length();

			int keyCnt = Math.min(searchKeySize, tokenPinyinSize);
			String keyPart = searchKey.substring(0, keyCnt);

			if (tokenPinyin.startsWith(keyPart)) {

				if (startIndex == -1) {
					startIndex = i;
				}

				endIndex = i + 1;
			} else {
				continue;
			}

			if (searchKeySize <= tokenPinyinSize) {
				searchKey = "";
				break;
			}

			searchKey = searchKey.substring(keyCnt, searchKeySize);
		}

		if (!searchKey.isEmpty()) {
			return false;
		}

		if (startIndex >= 0 && endIndex > 0) {
			searchElement.startIndex = startIndex;
			searchElement.endIndex = endIndex;

			return true;
		}

		return false;
	}

	// search helper end

	public static void setViewTouchHightlighted(final View view) {
		if (view == null) {
			return;
		}

		view.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					view.setBackgroundColor(Color.rgb(1, 175, 244));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					view.setBackgroundColor(Color.rgb(255, 255, 255));
				}
				return false;
			}
		});
	}

	// 这个还是蛮有用的,方便以后的替换
	public static int getDefaultAvatarResId(int sessionType) {
		if (sessionType == DBConstant.SESSION_TYPE_SINGLE) {
			return R.drawable.tt_default_user_portrait_corner;
		} else if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
			return R.drawable.group_default;
		} else if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
			return R.drawable.group_default;
		}

		return R.drawable.tt_default_user_portrait_corner;
	}

	public static void setEntityImageViewAvatarNoDefaultPortrait(
			ImageView imageView, String avatarUrl, int sessionType,
			int roundPixel) {
		setEntityImageViewAvatarImpl(imageView, avatarUrl, sessionType, false,
				roundPixel);
	}

	public static void setEntityImageViewAvatarImpl(ImageView imageView,
			String avatarUrl, int sessionType, boolean showDefaultPortrait,
			int roundPixel) {
		if (avatarUrl == null) {
			avatarUrl = "";
		}

		String fullAvatar = getRealAvatarUrl(avatarUrl);
		int defaultResId = -1;

		if (showDefaultPortrait) {
			defaultResId = getDefaultAvatarResId(sessionType);
		}

		displayImage(imageView, fullAvatar, defaultResId, roundPixel);
	}

	public static void displayImage(ImageView imageView, String resourceUri,
			int defaultResId, int roundPixel) {

		Logger logger = Logger.getLogger(IMUIHelper.class);

		logger.d(
				"displayimage#displayImage resourceUri:%s, defeaultResourceId:%d",
				resourceUri, defaultResId);

		if (resourceUri == null) {
			resourceUri = "";
		}

		boolean showDefaultImage = !(defaultResId <= 0);

		if (TextUtils.isEmpty(resourceUri) && !showDefaultImage) {
			logger.e("displayimage#, unable to display image");
			return;
		}

		DisplayImageOptions options;
		if (showDefaultImage) {
			options = new DisplayImageOptions.Builder()
					.showImageOnLoading(defaultResId)
					.showImageForEmptyUri(defaultResId)
					.showImageOnFail(defaultResId).cacheInMemory(true)
					.cacheOnDisk(true).considerExifParams(true)
					.displayer(new RoundedBitmapDisplayer(roundPixel))
					.imageScaleType(ImageScaleType.EXACTLY).// 改善OOM
					bitmapConfig(Bitmap.Config.RGB_565).// 改善OOM
					build();
		} else {
			options = new DisplayImageOptions.Builder().cacheInMemory(true)
					.cacheOnDisk(true).
					// considerExifParams(true).
					// displayer(new RoundedBitmapDisplayer(roundPixel)).
					// imageScaleType(ImageScaleType.EXACTLY).// 改善OOM
					// bitmapConfig(Bitmap.Config.RGB_565).// 改善OOM
					build();
		}

		ImageLoader.getInstance().displayImage(resourceUri, imageView, options,
				null);
	}

	public static void displayImageNoOptions(ImageView imageView,
			String resourceUri, int defaultResId, int roundPixel) {

		Logger logger = Logger.getLogger(IMUIHelper.class);

		logger.d(
				"displayimage#displayImage resourceUri:%s, defeaultResourceId:%d",
				resourceUri, defaultResId);

		if (resourceUri == null) {
			resourceUri = "";
		}

		boolean showDefaultImage = !(defaultResId <= 0);

		if (TextUtils.isEmpty(resourceUri) && !showDefaultImage) {
			logger.e("displayimage#, unable to display image");
			return;
		}

		DisplayImageOptions options;
		if (showDefaultImage) {
			options = new DisplayImageOptions.Builder()
					.showImageOnLoading(defaultResId)
					.showImageForEmptyUri(defaultResId)
					.showImageOnFail(defaultResId).cacheInMemory(true)
					.cacheOnDisk(true).considerExifParams(true)
					.displayer(new RoundedBitmapDisplayer(roundPixel))
					.imageScaleType(ImageScaleType.EXACTLY).// 改善OOM
					bitmapConfig(Bitmap.Config.RGB_565).// 改善OOM
					build();
		} else {
			options = new DisplayImageOptions.Builder().cacheInMemory(true)
					.cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY).// 改善OOM
					bitmapConfig(Bitmap.Config.RGB_565).// 改善OOM
					build(); // guanweile
		}
		ImageLoader.getInstance().displayImage(resourceUri, imageView, options,
				null);
	}

}

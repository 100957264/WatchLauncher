package com.fise.xiaoyu.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearch.OnWeatherSearchListener;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.activity.ActivityReqVerification;
import com.fise.xiaoyu.ui.activity.DetailPortraitActivity;
import com.fise.xiaoyu.ui.activity.PostionTouchActivity;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.FileUtil;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * 1.18 添加currentUser变量
 */
public class UserInfoFollowFragment extends MainFragment implements
		OnWeatherSearchListener, OnGeocodeSearchListener {

	private View curView = null;
	private IMService imService;
	private UserEntity currentUser;
	private int currentUserId;

	private double longitude;
	private double latitude;
	private Button icon_user_info;

	static final String[] SIGNAL_STRENGTH_NAMES = { "没信号", "很弱", "弱", "一般",
			"好", "强" };

	private int mPhoneSignalIconId;
	private int[] iconList;
	private int iconLevel;

	public static int UPDATEWEATHER = 909;

	private GeocodeSearch geocoderSearch;
	private WeatherSearchQuery mquery;
	private WeatherSearch mweathersearch;
	private LocalWeatherLive weatherlive;
	private ImageView fenxiang_weather;
	private TextView weather_text;
	private TextView fenxiang_weather_text;
	private TextView show_postion_name;

	// GSM/UMTS
	static final int[][] TELEPHONY_SIGNAL_STRENGTH = {

	{ R.drawable.wei_signal_ls_0, R.drawable.wei_signal_ls_1,
			R.drawable.wei_signal_ls_2, R.drawable.wei_signal_ls_3,
			R.drawable.wei_signal_ls_4, R.drawable.wei_signal_ls_5 } };

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("detail#onIMServiceConnected");

			imService = imServiceConnector.getIMService();
			if (imService == null) {
				logger.e("detail#imService is null");
				return;
			}

			currentUserId = getActivity().getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);
			if (currentUserId == 0) {
				logger.e("detail#intent params error!!");
				return;
			}
			currentUser = imService.getContactManager().findFriendsContact(
					currentUserId);

			if (currentUser == null) {
				currentUser = imService.getContactManager().findDeviceContact(
						currentUserId);
			}

			if (currentUser == null) {
				currentUser = imService.getContactManager().findContact(
						currentUserId);
			}

			if (currentUser == null) {
				return;
			}

			initBaseProfile();
			initDetailProfile();

			if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {

				// 查看用户的信息
				ArrayList<Integer> userIds = new ArrayList<>(1);
				// just single type
				userIds.add(currentUserId);
				IMContactManager.instance().reqGetDetaillUsers(userIds);

				// 查看用户状态 是否在线
				ArrayList<Integer> userIdStats = new ArrayList<>(1);
				// just single type
				userIdStats.add(currentUserId);
				imService.getContactManager().reqGetDetaillUsersStat(
						userIdStats);

			}

		}

		@Override
		public void onServiceDisconnected() {

		}
	};

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		imServiceConnector.disconnect(getActivity());
			}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		imServiceConnector.connect(getActivity());
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}

		curView = inflater.inflate(R.layout.tt_fragment_user_detail_follow,
				topContentView);
		super.init(curView);
		showProgressBar();
		initRes();
		return curView;
	}

	@Override
	public void onResume() {
		Intent intent = getActivity().getIntent();
		if (null != intent) {
			String fromPage = intent
					.getStringExtra(IntentConstant.USER_DETAIL_PARAM);
			setTopLeftText(fromPage);
		}
		super.onResume();
	}

	/**
	 * @Description 初始化资源
	 */
	private void initRes() {
		// 设置标题栏
		hideTopBar();
		Button icon_arrow = (Button) curView.findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});

		TextView left_text = (TextView) curView.findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});

		icon_user_info = (Button) curView.findViewById(R.id.icon_user_info);
		icon_user_info.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU&&(!Utils.isClientType(IMLoginManager.instance().getLoginInfo())))
				{
					IMUIHelper.openDeviceProfileActivity(UserInfoFollowFragment.this.getActivity(),
							currentUserId);
				}else{

					IMUIHelper.openUserInfoActivity(
							UserInfoFollowFragment.this.getActivity(),
							currentUserId);
				}

			}
		});

		topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
		setTopLeftText(getResources().getString(R.string.top_left_back));
	}

	@Override
	protected void initHandler() {

	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {
		case USER_INFO_UPDATE:
			UserEntity entity = imService.getContactManager().findContact(
					currentUserId);
			if (entity != null) {
				currentUser = entity;
				initBaseProfile();
				initDetailProfile();
			}
			break;

		case USER_INFO_UPDATE_STAT:
			UserEntity entityStat = imService.getContactManager().findContact(
					currentUserId);
			if (entityStat != null) {
				currentUser = entityStat;
				initBaseProfile();
				initDetailProfile();
			}
			break;

		case WEI_FRIENDS_REQ_SUCCESS:
			Utils.showToast(this.getActivity(), "发送请求成功,等待对方同意");
			break;
		case WEI_FRIENDS_REQ_FAIL:
			Utils.showToast(this.getActivity(), "发送失败,请检查网络是否正常");
			break;
		case USER_INFO_CALCEL_FOLLOW:
			Utils.showToast(this.getActivity(), "禁止取消位友");
			break;
		case USER_INFO_REQ_FRIENDS_SUCCESS:
			break;

		case USER_INFO_REQ_FRIENDS_FAIL:
			break;

		case USER_BLACKLIST_SUCCESS:
			currentUser = imService.getContactManager().findContact(
					currentUserId);
			if (currentUser.getAuth() == DBConstant.AUTH_TYPE_BLACK) {
				//button_follow.setVisibility(View.GONE);
			}
			icon_user_info.setVisibility(View.GONE);
			break;
		case USER_INFO_DATA_UPDATE:
			updatePortraitImage();
			break;

		case USER_BLACKLIST_DEL_SUCCESS: {
			Utils.showToast(this.getActivity(), "移除黑名单成功");
			UserEntity entityStat2 = imService.getContactManager().findContact(
					currentUserId);
			if (entityStat2 != null) {
				currentUser = entityStat2;
				initBaseProfile();
				initDetailProfile();
			}
		}
			break;
		default:
			break;
		}
	}

	public void updatePortraitImage() {
		UserEntity loginContact = IMLoginManager.instance().getLoginInfo();

		IMBaseImageView portraitImageView = (IMBaseImageView) curView
				.findViewById(R.id.user_portrait);
		portraitImageView
				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
		portraitImageView.setCorner(15);
		portraitImageView
				.setImageResource(R.drawable.tt_default_user_portrait_corner);
		portraitImageView.setImageUrl(loginContact.getAvatar());

		// 如果不在线为灰色
		if ((currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)
				&& (currentUser.getOnLine() != DBConstant.ONLINE)) {
			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(0);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
			portraitImageView.setColorFilter(filter);
		} else {
			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(1);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
			portraitImageView.setColorFilter(filter);
		}
	}

	private void initBaseProfile() {

		logger.d("detail#initBaseProfile");
		IMBaseImageView portraitImageView = (IMBaseImageView) curView
				.findViewById(R.id.user_portrait);
		fenxiang_weather = (ImageView) curView
				.findViewById(R.id.fenxiang_weather);
		weather_text = (TextView) curView.findViewById(R.id.weather_text);
		fenxiang_weather_text = (TextView) curView
				.findViewById(R.id.fenxiang_weather_text);

		show_postion_name = (TextView) curView
				.findViewById(R.id.show_postion_name);

		if (currentUser.getComment().equals("")) {
			setTextViewContent(R.id.remarksName, currentUser.getMainName());
		} else {
			setTextViewContent(R.id.remarksName, currentUser.getComment());
		}

		setTextViewContent(R.id.userName, "小雨号: " + currentUser.getRealName());
		setTextViewContent(R.id.nickName, "昵称: " + currentUser.getMainName());
		setTextViewContent(R.id.show_phone_name, "" + currentUser.getPhone());

		TextView show_phone_name = (TextView) curView.findViewById(R.id.show_phone_name);
		show_phone_name.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

					AlertDialog.Builder builder = new AlertDialog.Builder(
							new ContextThemeWrapper(UserInfoFollowFragment.this.getActivity(),
									android.R.style.Theme_Holo_Light_Dialog));
					//builder.setTitle(recentInfo.getName()); //暂时屏蔽

					String[] items = new String[] { UserInfoFollowFragment.this.getActivity().getString(R.string.call_phone_name) };

					builder.setItems(items, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
								case 0:
									// guanweile
									//用intent启动拨打电话
									Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ currentUser.getPhone()));
									UserInfoFollowFragment.this.getActivity().startActivity(intent);
									break;

							}
						}
					});
					AlertDialog alertDialog = builder.create();
					alertDialog.setCanceledOnTouchOutside(true);
					alertDialog.show();

			}
		});


		// setTextViewContent(R.id.phone, currentUser.getPhone());

		if (currentUser.getProvince().equals(currentUser.getCity())) {
			setTextViewContent(R.id.locality_string,
					" " + currentUser.getCity());

		} else {
			setTextViewContent(R.id.locality_string, currentUser.getProvince()
					+ " " + currentUser.getCity());
		}

		// setTextViewContent(R.id.userName, currentUser.getRealName());
		// 头像设置
		portraitImageView
				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
		portraitImageView.setCorner(8);
		//portraitImageView
		//		.setImageResource(R.drawable.tt_default_user_portrait_corner);
		portraitImageView.setImageUrl(currentUser.getAvatar());

		if ((currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)
				&& (currentUser.getOnLine() != DBConstant.ONLINE)) {
			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(0);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
			portraitImageView.setColorFilter(filter);
		} else {
			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(1);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
			portraitImageView.setColorFilter(filter);
		}

		iconList = TELEPHONY_SIGNAL_STRENGTH[0];

		mPhoneSignalIconId = R.drawable.wei_signal_ls_0;
		iconLevel = currentUser.getSignal();// getIconLevel(currentUser.getSignal());
		if (iconLevel >= 5) {
			mPhoneSignalIconId = R.drawable.wei_signal_ls_5;
		} else {
			mPhoneSignalIconId = iconList[iconLevel];
		}

		ImageView mSignalImageView = (ImageView) curView
				.findViewById(R.id.level_icon_signal);
		mSignalImageView.setBackgroundResource(mPhoneSignalIconId);

		TextView signal_text = (TextView) curView
				.findViewById(R.id.signal_text);

		if (iconLevel >= 5) {
			signal_text.setText("强");

		} else {
			signal_text.setText(SIGNAL_STRENGTH_NAMES[iconLevel]);
		}

		// ProgressBar icon_electricity = (ProgressBar) curView
		// .findViewById(R.id.icon_electricity);
		// icon_electricity.setProgress(currentUser.getBattery());

		ImageView icon_electricity = (ImageView) curView
				.findViewById(R.id.icon_electricity);
		if (currentUser.getBattery() <= 0) {
			icon_electricity
					.setBackgroundResource(R.drawable.shebei_noelectric);
		} else if (currentUser.getBattery() > 0
				&& currentUser.getBattery() <= 25) {
			icon_electricity
					.setBackgroundResource(R.drawable.shebei_electric_1_red);

		} else if (currentUser.getBattery() > 25
				&& currentUser.getBattery() <= 50) {
			icon_electricity
					.setBackgroundResource(R.drawable.shebei_electric_1);

		} else if (currentUser.getBattery() > 50
				&& currentUser.getBattery() <= 75) {

			icon_electricity
					.setBackgroundResource(R.drawable.shebei_electric_2);

		} else if (currentUser.getBattery() > 75
				&& currentUser.getBattery() <= 100) {

			icon_electricity
					.setBackgroundResource(R.drawable.shebei_electric_3);

		}

		else if (currentUser.getBattery() == 100) {

			icon_electricity
					.setBackgroundResource(R.drawable.shebei_electric_4);
		}
//		else if (currentUser.getBattery() > 75
//				&& currentUser.getBattery() < 90) {
//
//			icon_electricity
//					.setBackgroundResource(R.drawable.shebei_electric_6);
//		}
		else {
			//TODO chongdian
			icon_electricity.setBackgroundResource(R.drawable.shebei_electric_charging);
		}

		TextView electricity_text_type = (TextView) curView
				.findViewById(R.id.electricity_text);

		electricity_text_type.setText(currentUser.getBattery() + "%");

//	  if ((currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)
//				&& (currentUser.getOnLine() == DBConstant.ONLINE)) {
		if ((currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)) {

			electricity_text_type.setVisibility(View.VISIBLE);
			signal_text.setVisibility(View.VISIBLE);
		} else {
			electricity_text_type.setVisibility(View.GONE);
			signal_text.setVisibility(View.GONE);
		}

		longitude = currentUser.getLongitude();
		latitude = currentUser.getLatitude();


		RelativeLayout setting_label = (RelativeLayout) curView
				.findViewById(R.id.setting_label);

		if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO
				||currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_VERIFY) {
			setting_label.setVisibility(View.GONE);
		}

		RelativeLayout more = (RelativeLayout) curView.findViewById(R.id.more);
		more.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				IMUIHelper.openUserInfoSignedActivity(
						UserInfoFollowFragment.this.getActivity(),
						currentUser.getSign_info());
			}
		});

		RelativeLayout setting_postion = (RelativeLayout) curView
				.findViewById(R.id.setting_postion);

		// if ((currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)
		// && (currentUser.getOnLine() == DBConstant.ONLINE)) {

		setting_postion.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent intent = new Intent(UserInfoFollowFragment.this
						.getActivity(), PostionTouchActivity.class);
				intent.putExtra(IntentConstant.POSTION_LAT,
						currentUser.getLatitude());
				intent.putExtra(IntentConstant.POSTION_LNG,
						currentUser.getLongitude());
//				intent.putExtra(IntentConstant.POSTION_TYPE,
//						DBConstant.POSTION_INFO_WEI);
				intent.putExtra(IntentConstant.DEV_USER_ID,
						currentUser.getPeerId());

				UserInfoFollowFragment.this.getActivity().startActivity(intent);
			}
		});

		// }

		TextView more_text = (TextView) curView.findViewById(R.id.more_text);
		more_text.setText(currentUser.getSign_info() + "");

		TextView show_name = (TextView) curView.findViewById(R.id.show_name);
		if (currentUser.getComment().equals("")) {
			show_name.setText(currentUser.getMainName());
		} else {
			show_name.setText(currentUser.getComment());
		}

		setting_label.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				IMUIHelper.openUserInfoRemarks(
						UserInfoFollowFragment.this.getActivity(),
						currentUserId);
			}
		});

		// if ((currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)
		// && (currentUser.getOnLine() == DBConstant.ONLINE)) {

		if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {

//			LinearLayout information = (LinearLayout) curView
//					.findViewById(R.id.information);
//			information.setVisibility(View.VISIBLE);
//			View line0 = curView.findViewById(R.id.line0);
//			line0.setVisibility(View.VISIBLE);
//
//			LatLonPoint latLonPoint = new LatLonPoint(
//					currentUser.getLatitude(), currentUser.getLongitude());
//
//			geocoderSearch = new GeocodeSearch(this.getActivity());
//			geocoderSearch.setOnGeocodeSearchListener(this);
//
//			if (currentUser.getOnLine() == DBConstant.ONLINE) {
//				getAddress(latLonPoint);
//			} else {
//
//				// 最后一次位置
//				getAddress(latLonPoint);
//				ColorMatrix matrix = new ColorMatrix();
//				matrix.setSaturation(0);
//				ColorMatrixColorFilter filter = new ColorMatrixColorFilter(
//						matrix);
//				fenxiang_weather.setColorFilter(filter);
//				show_postion_name.setText("");
//			}
//
//			setting_postion.setVisibility(View.VISIBLE);


			LinearLayout information = (LinearLayout) curView
					.findViewById(R.id.information);
			information.setVisibility(View.GONE);
			View line0 =  curView.findViewById(R.id.line0);
			line0.setVisibility(View.GONE);

			setting_postion.setVisibility(View.GONE);


		} else {
			LinearLayout information = (LinearLayout) curView
					.findViewById(R.id.information);
			information.setVisibility(View.GONE);
			View line0 =  curView.findViewById(R.id.line0);
			line0.setVisibility(View.GONE);

			setting_postion.setVisibility(View.GONE);

		}



		if (currentUser.getIsFriend() != DBConstant.FRIENDS_TYPE_YUYOU) {

			View postionl_arrow_line = (View) curView
					.findViewById(R.id.postionl_arrow_line);

			postionl_arrow_line.setVisibility(View.GONE);
		}


		portraitImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(getActivity(),
						DetailPortraitActivity.class);
				intent.putExtra(IntentConstant.KEY_AVATAR_URL,
						currentUser.getAvatar());
				intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR,
						true);

				startActivity(intent);

			}
		});

		// 设置界面信息
		Button chatBtn = (Button) curView.findViewById(R.id.chat_btn);
		if (currentUserId == imService.getLoginManager().getLoginId()) {
			chatBtn.setVisibility(View.GONE);
		} else {
			chatBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {

					if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO
							||currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_VERIFY) {

						Intent intent = new Intent(getActivity(),
								ActivityReqVerification.class);
						intent.putExtra(IntentConstant.KEY_PEERID,
								currentUser.getPeerId());
						getActivity().startActivity(intent);

					} else {
						IMUIHelper.openChatActivity(getActivity(),
								currentUser.getSessionKey());
						getActivity().finish();
					}

				}
			});

		}

		if (currentUser.getAuth() == DBConstant.AUTH_TYPE_BLACK) {
			//button_follow.setText("移出黑名单");
		}


//		if (Utils.isClientType(currentUser)) {
//			chatBtn.setVisibility(View.GONE);
//			button_follow.setVisibility(View.GONE);
//		} else {
//
//			if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO
//					||currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_VERIFY) {
//				chatBtn.setText("添加好友");
//				button_follow.setVisibility(View.GONE);
//				icon_user_info.setVisibility(View.GONE);
//			} else {
//				chatBtn.setText("发送消息");
//				button_follow.setVisibility(View.VISIBLE);
//			}
//		}

		if (currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_NO
				||currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_VERIFY) {
			chatBtn.setText("添加好友");
			icon_user_info.setVisibility(View.GONE);
		} else {
			chatBtn.setText("发送消息");
		}

		if(Utils.isClientType(IMLoginManager.instance().getLoginInfo())){
			if(currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU){
				icon_user_info.setVisibility(View.GONE);
			}
		}

		// 如果加入黑名单了 不能设置黑名单和扫除功能
		if ( currentUser.getAuth() == DBConstant.AUTH_TYPE_BLACK) {
			icon_user_info.setVisibility(View.GONE);
		}

	}

	private void initDetailProfile() {
		logger.d("detail#initDetailProfile");
		hideProgressBar();
		setSex(currentUser.getGender());
	}

	private void setTextViewContent(int id, String content) {
		TextView textView = (TextView) curView.findViewById(id);
		if (textView == null) {
			return;
		}

		textView.setText(content);
	}

	private void setSex(int sex) {
		if (curView == null) {
			return;
		}

		ImageView sexImageView = (ImageView) curView.findViewById(R.id.sex);
		if (sexImageView == null) {
			return;
		}

		if (sex == DBConstant.SEX_MAILE) {
			sexImageView.setBackgroundResource(R.drawable.sex_head_man);
		} else {
			sexImageView.setBackgroundResource(R.drawable.icon_head_woman);
		}

	}

	/**
	 * 响应逆地理编码
	 */
	public void getAddress(final LatLonPoint latLonPoint) {
		RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
				GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
	}

	@Override
	public void onWeatherForecastSearched(LocalWeatherForecastResult arg0,
			int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult,
			int rCode) {
		// TODO Auto-generated method stub

		if (rCode == 1000) {
			if (weatherLiveResult != null
					&& weatherLiveResult.getLiveResult() != null) {

				weatherlive = weatherLiveResult.getLiveResult();
				FileUtil.setWeather(fenxiang_weather, weatherlive.getWeather());
				// fenxiang_weather.setText(weatherlive.getWeather());
				weather_text.setText(weatherlive.getTemperature() + "℃");
				weather_text.setVisibility(View.VISIBLE);
				fenxiang_weather_text.setVisibility(View.VISIBLE);
				fenxiang_weather_text.setText(weatherlive.getWeather());

				if ((currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)
						&& (currentUser.getOnLine() != DBConstant.ONLINE)) {

					ColorMatrix matrix = new ColorMatrix();
					matrix.setSaturation(0);
					ColorMatrixColorFilter filter = new ColorMatrixColorFilter(
							matrix);
					fenxiang_weather.setColorFilter(filter);

				} else {

					ColorMatrix matrix = new ColorMatrix();
					matrix.setSaturation(1);
					ColorMatrixColorFilter filter = new ColorMatrixColorFilter(
							matrix);
					fenxiang_weather.setColorFilter(filter);
				}

			}
		}
	}

	/**
	 * 逆地理编码回调
	 */
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		if (rCode == 1000) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {

				String cityName = result.getRegeocodeAddress().getCity();
				mquery = new WeatherSearchQuery(cityName,
						WeatherSearchQuery.WEATHER_TYPE_LIVE);

				// 如果在线才请求数据
				if ((currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)
						&& (currentUser.getOnLine() == DBConstant.ONLINE)) {
					show_postion_name.setText(result.getRegeocodeAddress()
							.getFormatAddress());

					mweathersearch = new WeatherSearch(this.getActivity());
					mweathersearch.setOnWeatherSearchListener(this);
					mweathersearch.setQuery(mquery);
					mweathersearch.searchWeatherAsyn(); // 异步搜索

				} else if ((currentUser.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)
						&& (currentUser.getOnLine() != DBConstant.ONLINE)) {

					ColorMatrix matrix = new ColorMatrix();
					matrix.setSaturation(0);
					ColorMatrixColorFilter filter = new ColorMatrixColorFilter(
							matrix);
					fenxiang_weather.setColorFilter(filter);

					// 最后一次位置
					show_postion_name.setText(result.getRegeocodeAddress()
							.getFormatAddress());

					mweathersearch = new WeatherSearch(this.getActivity());
					mweathersearch.setOnWeatherSearchListener(this);
					mweathersearch.setQuery(mquery);
					mweathersearch.searchWeatherAsyn(); // 异步搜索

				} else {
					ColorMatrix matrix = new ColorMatrix();
					matrix.setSaturation(1);
					ColorMatrixColorFilter filter = new ColorMatrixColorFilter(
							matrix);
					fenxiang_weather.setColorFilter(filter);
				}

				//

			} else {

			}
		} else {

		}
	}

	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
		// TODO Auto-generated method stub

	}

}

package com.fise.xw.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.R;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.SysConstant;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.ui.activity.SettingActivity;
import com.fise.xw.ui.activity.ShowSettingActivity; 
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.utils.FileUtil;
import com.fise.xw.ui.widget.IMBaseImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

import de.greenrobot.event.EventBus;

public class MyFragment extends MainFragment {
	private View curView = null;
	private View contentView;
	private View exitView;
	private View clearView;
	// private View settingView;
	private IMService imService;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onServiceDisconnected() {

		}

		@Override
		public void onIMServiceConnected() {
			if (curView == null) {
				return;
			}
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				return;
			}
			if (!imService.getContactManager().isUserDataReady()) {
				logger.i("detail#contact data are not ready");
			} else {
				init(imService);
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		imServiceConnector.connect(getActivity());
		EventBus.getDefault().register(this);

		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_my, topContentView);

		initRes();

		return curView;
	}

	/**
	 * @Description 初始化资源
	 */
	private void initRes() {
		super.init(curView);

		contentView = curView.findViewById(R.id.content);
		this.hideTopBar(); // 隐藏 Bar

		RelativeLayout account_security = (RelativeLayout) curView
				.findViewById(R.id.zhanghu_anquan);
		account_security.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				IMUIHelper.openAccountSecurityActivity(MyFragment.this
						.getActivity());

			}
		});

		RelativeLayout message_note_Page = (RelativeLayout) curView
				.findViewById(R.id.message_note_Page);
		message_note_Page.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				IMUIHelper.openMessageNotifyActivity(MyFragment.this
						.getActivity());

			}
		});

		RelativeLayout currencyPage = (RelativeLayout) curView
				.findViewById(R.id.currencyPage);
		currencyPage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				IMUIHelper.openCurrencyActivity(MyFragment.this.getActivity());

			}
		});

		RelativeLayout privacyPage = (RelativeLayout) curView
				.findViewById(R.id.privacyPage);
		privacyPage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				IMUIHelper.openPrivacyActivity(MyFragment.this.getActivity());

			}
		});

		RelativeLayout about_xiao_Page = (RelativeLayout) curView
				.findViewById(R.id.about_xiao_Page);
		about_xiao_Page.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				IMUIHelper.openAboutActivity(MyFragment.this.getActivity());

			}
		});

		hideContent();

		// 设置顶部标题栏
		setTopTitle(getActivity().getString(R.string.show_app_name));
		// 设置页面其它控件

	}

	private void hideContent() {
		if (contentView != null) {
			contentView.setVisibility(View.GONE);
		}
	}

	private void showContent() {
		if (contentView != null) {
			contentView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 应该放在这里嘛??
		imServiceConnector.disconnect(getActivity());
		EventBus.getDefault().unregister(this);
	}

	@Override
	protected void initHandler() {
	}

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {
		case USER_INFO_OK:
			init(imServiceConnector.getIMService());
		case USER_INFO_DATA_UPDATE:
			updatePortraitImage();
			break;

		}
	}

	public void onEventMainThread(LoginEvent event) {
		switch (event) {
		case LOGIN_OK:
			init(imServiceConnector.getIMService());
			break;

		}
	}

	public void updatePortraitImage() {

		UserEntity loginContact = IMLoginManager.instance().getLoginInfo();

		IMBaseImageView portraitImageView = (IMBaseImageView) curView
				.findViewById(R.id.user_portrait);
		portraitImageView
				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
		portraitImageView.setCorner(90);
		portraitImageView
				.setImageResource(R.drawable.tt_default_user_portrait_corner);
		portraitImageView.setImageUrl(loginContact.getAvatar());

		TextView nickNameView = (TextView) curView.findViewById(R.id.nickName);
		TextView userNameView = (TextView) curView.findViewById(R.id.userName);

		nickNameView.setText(loginContact.getMainName());
		userNameView.setText("小位号:" + loginContact.getRealName());

	}

	private void init(IMService imService) {
		showContent();
		hideProgressBar();

		if (imService == null) {
			return;
		}
		final UserEntity loginContact = imService.getLoginManager()
				.getLoginInfo();
		if (loginContact == null) {
			return;
		}
		TextView nickNameView = (TextView) curView.findViewById(R.id.nickName);
		TextView userNameView = (TextView) curView.findViewById(R.id.userName);
		IMBaseImageView portraitImageView = (IMBaseImageView) curView
				.findViewById(R.id.user_portrait);

		nickNameView.setText(loginContact.getMainName());
		userNameView.setText("小位号:" + loginContact.getRealName());

		// 头像设置
		portraitImageView
				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
		portraitImageView.setCorner(90);
		portraitImageView
				.setImageResource(R.drawable.tt_default_user_portrait_corner);
		portraitImageView.setImageUrl(loginContact.getAvatar());

		RelativeLayout userContainer = (RelativeLayout) curView
				.findViewById(R.id.user_container);
		userContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				IMUIHelper.openLoginInfoActivity(getActivity());
			}
		});

	}

}

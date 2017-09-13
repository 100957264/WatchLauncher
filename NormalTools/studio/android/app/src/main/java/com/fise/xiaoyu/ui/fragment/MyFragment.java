package com.fise.xiaoyu.ui.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.SystemConfigEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.app.IMApplication;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.activity.InfoQRActivity;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.IMUIHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
            if (imService.getLoginManager().getLoginInfo() != null) {
                init(imService);
            }
//			if (!imService.getContactManager().isUserDataReady()) {
//				logger.i("detail#contact data are not ready");
//			} else {
//				init(imService);
//			}


            TextView unread_update_version = (TextView) (MyFragment.this.getActivity()).findViewById(R.id.unread_update_version);
            unread_update_version.setText("1");
            final SystemConfigEntity systemVerstion = imService.getContactManager().getSystemConfig();

            if (systemVerstion != null) {
                if (IMApplication.getApplication().getVersion().compareTo(systemVerstion.getVersion()) < 0) {
                    unread_update_version.setVisibility(View.VISIBLE);
                } else {
                    unread_update_version.setVisibility(View.GONE);
                }
            }

            updateReqYu();
        }
    };


    public void updateReqYu() {
        Button new_friends = (Button) curView.findViewById(R.id.new_friends);
        ImageView new_friends_image = (ImageView) curView.findViewById(R.id.new_friends_image);
        int unReqNum = imService.getUnReadMsgManager().getReqUnYuFriendsMap().size();
        //new_friends.setVisibility(View.VISIBLE);
        if (unReqNum > 0) {
            new_friends.setVisibility(View.VISIBLE);
            new_friends_image.setVisibility(View.VISIBLE);
        } else {
            new_friends.setVisibility(View.GONE);
            new_friends_image.setVisibility(View.GONE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        imServiceConnector.connect(getActivity());

        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_my, topContentView);
        hideTopSpliteLine();
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


        Button new_friends = (Button) curView.findViewById(R.id.new_friends);
        new_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                IMUIHelper.openYuMessageActivity(MyFragment.this.getActivity());
            }
        });


        curView.findViewById(R.id.qr_code_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), InfoQRActivity.class);
                startActivity(intent);
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
            }

    @Override
    protected void initHandler() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_OK:
                init(imServiceConnector.getIMService());
            case USER_INFO_DATA_UPDATE:
                updatePortraitImage();
                break;

            case USER_INFO_REQ_YU:
                updateReqYu();
                break;

            case WEI_FRIENDS_WEI_REQ_ALL:
            case WEI_FRIENDS_INFO_REQ_ALL:
            case USER_INFO_WEI_DATA:
                updateReqYu();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginEvent event) {
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
        portraitImageView
                .setImageResource(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setImageUrl(loginContact.getAvatar());

        TextView nickNameView = (TextView) curView.findViewById(R.id.nickName);
        TextView userNameView = (TextView) curView.findViewById(R.id.userName);

        nickNameView.setText(loginContact.getMainName());
        userNameView.setText("小雨号:" + loginContact.getRealName());

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
        userNameView.setText("小雨号:" + loginContact.getRealName());

        // 头像设置
        portraitImageView
                .setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
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

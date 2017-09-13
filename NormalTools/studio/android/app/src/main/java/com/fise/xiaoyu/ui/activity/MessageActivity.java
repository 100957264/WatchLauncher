package com.fise.xiaoyu.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.GroupNickEntity;
import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.SystemConfigSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.app.IMApplication;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.DevVedioInfo;
import com.fise.xiaoyu.config.HandlerConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.entity.AudioMessage;
import com.fise.xiaoyu.imservice.entity.ImageMessage;
import com.fise.xiaoyu.imservice.entity.OnLineVedioMessage;
import com.fise.xiaoyu.imservice.entity.TextMessage;
import com.fise.xiaoyu.imservice.entity.UnreadEntity;
import com.fise.xiaoyu.imservice.entity.VedioMessage;
import com.fise.xiaoyu.imservice.event.GroupEvent;
import com.fise.xiaoyu.imservice.event.MessageEvent;
import com.fise.xiaoyu.imservice.event.PriorityEvent;
import com.fise.xiaoyu.imservice.event.SelectEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.ui.base.ActivityManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.manager.IMUserActionManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine.CommandType;
import com.fise.xiaoyu.protobuf.IMBaseDefine.SessionType;
import com.fise.xiaoyu.ui.adapter.MessageAdapter;
import com.fise.xiaoyu.ui.adapter.OthersPagerAdapter;
import com.fise.xiaoyu.ui.adapter.album.AlbumHelper;
import com.fise.xiaoyu.ui.adapter.album.ImageBucket;
import com.fise.xiaoyu.ui.adapter.album.ImageItem;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.helper.AudioPlayerHandler;
import com.fise.xiaoyu.ui.helper.AudioRecordHandler;
import com.fise.xiaoyu.ui.helper.Emoparser;
import com.fise.xiaoyu.ui.widget.CustomEditView;
import com.fise.xiaoyu.ui.widget.EmoGridView;
import com.fise.xiaoyu.ui.widget.EmoGridView.OnEmoGridViewItemClick;
import com.fise.xiaoyu.ui.widget.MGProgressbar;
import com.fise.xiaoyu.ui.widget.YayaEmoGridView;
import com.fise.xiaoyu.utils.CommonUtil;
import com.fise.xiaoyu.utils.CompatUtil;
import com.fise.xiaoyu.utils.FileUtil;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.PermissionUtil;
import com.fise.xiaoyu.utils.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.jinlin.zxing.example.utils.BitmapUtils.calculateInSampleSize;

/**
 * 主消息界面
 */
@SuppressLint("NewApi")
public class MessageActivity extends TTBaseActivity implements
        OnRefreshListener2<ListView>, View.OnClickListener, OnTouchListener,
        TextWatcher, SensorEventListener {

    private static Handler uiHandler = null;// 处理语音

    private PullToRefreshListView lvPTR = null;
    private CustomEditView messageEdt = null;
    private TextView sendBtn = null;
    private Button recordAudioBtn = null;
    private ImageView keyboardInputImg = null;
    private ImageView soundVolumeImg = null;
    private LinearLayout soundVolumeLayout = null;

    private ImageView audioInputImg = null;
    private ImageView addPhotoBtn = null;
    private ImageView addEmoBtn = null;
    private LinearLayout emoLayout = null;
    private EmoGridView emoGridView = null;
    // private ProgressWebView webView;// test guanweile


    private YayaEmoGridView yayaEmoGridView = null;
    private RadioGroup emoRadioGroup = null;
    private String audioSavePath = null;

    private InputMethodManager inputManager = null;
    private AudioRecordHandler audioRecorderInstance = null;
    private TextView textView_new_msg_tip = null;

    private MessageAdapter adapter = null;
    private Thread audioRecorderThread = null;
    private Dialog soundVolumeDialog = null;
    private View addOthersPanelView = null;

    private AlbumHelper albumHelper = null;

    private List<ImageBucket> albumList = null;
    MGProgressbar progressbar = null;

    // private boolean audioReday = false; 语音先关的
    private SensorManager sensorManager = null;
    private Sensor sensor = null;

    private String takePhotoSavePath = "";
    private Logger logger = Logger.getLogger(MessageActivity.class);
    private IMService imService;

    private UserEntity loginUser;
    private PeerEntity peerEntity;
    private UserEntity peerUser;

    // 当前的session
    private String currentSessionKey;
    private int index;
    private int historyTimes = 0;

    // 键盘布局相关参数
    int rootBottom = Integer.MIN_VALUE, keyboardHeight = 0;
    switchInputMethodReceiver receiver;
    private String currentInputMethod;

    private String path;
    private ZhuaiTimeCount time2;
    private ZhuaiTimeCount time10;
    public static int TimeStart = 0;
    public static boolean isShowNick = false;

    private boolean isInput = false;

    /**
     * 全局Toast
     */
    private Toast mToast;
    private LinearLayout message_bg;
    private ImageView mTelephoneIcon;
    private TextView tv;
    private SharedPreferences mPreference;
    //    private LinearLayout mLlDevicePanel;
    private UserEntity userEntity;
    private Boolean isHideDviceView = true;
    private Boolean isHideEmoLayout = true;
    private View camera_video_replay_btn;


    private View view1, view2;//需要滑动的页卡
    private ViewPager viewPager;//viewpager
    private List<View> viewList;//把需要滑动的页卡添加到这个list中

    //定义一个点集合
    private List<View> dots;
    private int oldPosition = 0;// 记录上一次点的位置
    private int currentItem; // 当前页面

    public void showToast(int resId) {
        String text = getResources().getString(resId);
        if (mToast == null) {
            mToast = Utils.getToast(MessageActivity.this, text);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
    }

    public void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        IMApplication.gifRunning = false;
        cancelToast();
        super.onBackPressed();
    }

    /**
     * end 全局Toast
     */
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("message_activity#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                return;
            }

            initData();
            //如果是设备群
            if (peerEntity != null && peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {

                GroupEntity groupEntity = (GroupEntity) peerEntity;
                if ((groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) && (Utils.isClientType(imService.getLoginManager().getLoginInfo()))) {

                    addYuYouFriendsOthersPanelView();
                } else if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
                    setAddOthersPanelView(); //管理员
                } else {
                    addFriendsOthersPanelView();
                }
            } else if (peerEntity != null && peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
                UserEntity userEntity = (UserEntity) peerEntity;

                if (userEntity != null && (!Utils.isClientType(imService.getLoginManager().getLoginInfo())) && Utils.isClientType(userEntity)) { //如果是设备
                    setAddOthersPanelView(); //管理员
                    //如果自己是设备  对方是的雨友布局
                } else if (userEntity != null && Utils.isClientType(imService.getLoginManager().getLoginInfo()) && (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU)) {
                    addYuYouFriendsOthersPanelView();
                } else {
                    addFriendsOthersPanelView();
                }
            } else {
                addFriendsOthersPanelView();
            }

        }

        @Override
        public void onServiceDisconnected() {

        }
    };

    public void addFriendsOthersPanelView() {

        viewPager = (ViewPager) MessageActivity.this.findViewById(R.id.viewpager);
        LayoutInflater lf = getLayoutInflater().from(MessageActivity.this);
        view1 = lf.inflate(R.layout.tt_layout_friends, null);
        viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        viewList.add(view1);

        OthersPagerAdapter pagerAdapter = new OthersPagerAdapter(viewList);
        viewPager.setAdapter(pagerAdapter);


        View takePhotoBtn = view1.findViewById(R.id.take_photo_btn);
        takePhotoBtn.setOnClickListener(MessageActivity.this);

        View take_camera_btn = view1.findViewById(R.id.take_camera_btn);
        take_camera_btn.setOnClickListener(MessageActivity.this);


        View take_vedio_btn = view1.findViewById(R.id.take_vedio_btn);
        take_vedio_btn.setOnClickListener(MessageActivity.this);

        View take_mingpian_btn = view1.findViewById(R.id.take_mingpian_btn);
        take_mingpian_btn.setOnClickListener(MessageActivity.this);


        RelativeLayout take_postion_view = (RelativeLayout) (view1.findViewById(R.id.take_postion_view));
        take_postion_view.setOnClickListener(MessageActivity.this);

        RelativeLayout rl_phone_callback_btn = (RelativeLayout) (view1.findViewById(R.id.rl_phone_callback_btn));
        // rl_phone_callback_btn.setOnClickListener(MessageActivity.this);

        View phone_callback_btn = view1.findViewById(R.id.phone_callback_btn);
        phone_callback_btn.setOnClickListener(MessageActivity.this);

        View take_position_btn = view1.findViewById(R.id.take_position_btn);
        take_position_btn.setOnClickListener(MessageActivity.this);

        rl_phone_callback_btn.setVisibility(View.GONE);

        LinearLayout rl_phone_callback_temp = (LinearLayout) (view1.findViewById(R.id.rl_phone_callback_temp));
        rl_phone_callback_temp.setVisibility(View.VISIBLE);

    }


    /**
     * 雨友关系的
     */
    public void addYuYouFriendsOthersPanelView() {
        viewPager = (ViewPager) MessageActivity.this.findViewById(R.id.viewpager);
        LayoutInflater lf = getLayoutInflater().from(MessageActivity.this);
        view1 = lf.inflate(R.layout.tt_layout_friends, null);

        viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        viewList.add(view1);

        OthersPagerAdapter pagerAdapter = new OthersPagerAdapter(viewList);
        viewPager.setAdapter(pagerAdapter);


        View takePhotoBtn = view1.findViewById(R.id.take_photo_btn);
        takePhotoBtn.setOnClickListener(MessageActivity.this);

        View take_camera_btn = view1.findViewById(R.id.take_camera_btn);
        take_camera_btn.setOnClickListener(MessageActivity.this);


        View take_vedio_btn = view1.findViewById(R.id.take_vedio_btn);
        take_vedio_btn.setOnClickListener(MessageActivity.this);

        View take_mingpian_btn = view1.findViewById(R.id.take_mingpian_btn);
        take_mingpian_btn.setOnClickListener(MessageActivity.this);


        RelativeLayout take_postion_view = (RelativeLayout) (view1.findViewById(R.id.take_postion_view));
        take_postion_view.setOnClickListener(MessageActivity.this);

        RelativeLayout rl_phone_callback_btn = (RelativeLayout) (view1.findViewById(R.id.rl_phone_callback_btn));
        rl_phone_callback_btn.setOnClickListener(MessageActivity.this);

        View phone_callback_btn = view1.findViewById(R.id.phone_callback_btn);
        phone_callback_btn.setOnClickListener(MessageActivity.this);

        View take_position_btn = view1.findViewById(R.id.take_position_btn);
        take_position_btn.setOnClickListener(MessageActivity.this);
    }


    /**
     * 如果是设备群 或者雨友 而自己是管理员的
     */
    public void setAddOthersPanelView() {

        viewPager = (ViewPager) MessageActivity.this.findViewById(R.id.viewpager);
        LayoutInflater lf = getLayoutInflater().from(MessageActivity.this);
        view1 = lf.inflate(R.layout.tt_layout_others, null);
        LayoutInflater lf1 = getLayoutInflater().from(MessageActivity.this);
        view2 = lf1.inflate(R.layout.tt_layout_others_more, null);

        viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        viewList.add(view1);
        viewList.add(view2);

        dots = new ArrayList<View>();
        dots.add(findViewById(R.id.dot_1));
        dots.add(findViewById(R.id.dot_2));
        //并且，默认第一个是选中状态
        dots.get(0).setBackgroundResource(R.drawable.red_dot);

        LinearLayout bottom_dots = (LinearLayout) (MessageActivity.this.findViewById(R.id.bottom_dots));
        bottom_dots.setVisibility(View.VISIBLE);

        OthersPagerAdapter pagerAdapter = new OthersPagerAdapter(viewList);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                //下面就是获取上一个位置，并且把点的状体设置成默认状体
                dots.get(oldPosition).setBackgroundResource(R.drawable.point_normal);
                //获取到选中页面对应的点，设置为选中状态
                dots.get(position).setBackgroundResource(R.drawable.red_dot);
                //下面是记录本次的位置，因为在滑动，他就会变成过时的点了
                oldPosition = position;
                //关联页卡
                currentItem = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        View take_camera_btn = view1.findViewById(R.id.take_camera_btn);
        take_camera_btn.setOnClickListener(MessageActivity.this);

        View take_photo_btn = view1.findViewById(R.id.take_photo_btn);
        take_photo_btn.setOnClickListener(MessageActivity.this);

        View take_vedio_btn = view1.findViewById(R.id.take_vedio_btn);
        take_vedio_btn.setOnClickListener(MessageActivity.this);

        View take_mingpian_btn = view1.findViewById(R.id.take_mingpian_btn);
        take_mingpian_btn.setOnClickListener(MessageActivity.this);


        View take_position_btn = view1.findViewById(R.id.take_position_btn);
        take_position_btn.setOnClickListener(MessageActivity.this);

        View check_position_btn = view1.findViewById(R.id.check_position_btn);
        check_position_btn.setOnClickListener(MessageActivity.this);

        View take_zhua_photo_btn = view1.findViewById(R.id.take_zhua_photo_btn);
        take_zhua_photo_btn.setOnClickListener(MessageActivity.this);

        View take_recordings_btn = view1.findViewById(R.id.take_recordings_btn);
        take_recordings_btn.setOnClickListener(MessageActivity.this);


        View phone_callback_btn = view2.findViewById(R.id.phone_callback_btn);
        phone_callback_btn.setOnClickListener(MessageActivity.this);

        View check_telephone_charge = view2.findViewById(R.id.check_telephone_charge);
        check_telephone_charge.setOnClickListener(MessageActivity.this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.d("message_activity#onCreate:%s", this);
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        super.onCreate(savedInstanceState);
        currentSessionKey = getIntent().getStringExtra(
                IntentConstant.KEY_SESSION_KEY);
        index = getIntent().getIntExtra(IntentConstant.KEY_INDEX_KEY, 0);

        mPreference = getSharedPreferences("SP", Context.MODE_PRIVATE);
        initSoftInputMethod();
        initEmo();
        initAlbumHelper();
        initAudioHandler();
        initAudioSensor();
        initView();

        time2 = new ZhuaiTimeCount(20000, 1000);
        time10 = new ZhuaiTimeCount(10000, 1000);
        imServiceConnector.connect(this);
        logger.d("message_activity#register im service and eventBus");

    }

    Drawable bitmap2Drawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    // 判断文件是否存在
    public boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }


    // 触发条件,imservice链接成功，或者newIntent
    @SuppressLint("ResourceAsColor")
    private void initData() {
        historyTimes = 0;
        adapter.clearItem();
        ImageMessage.clearImageMessageList();
        loginUser = imService.getLoginManager().getLoginInfo();
        peerEntity = imService.getSessionManager().findPeerEntity(
                currentSessionKey);


        if (peerEntity != null && peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {

            GroupEntity groupEntity = (GroupEntity) peerEntity;
            if (groupEntity != null) {

                ArrayList<Integer> memberList = new ArrayList<Integer>();
                for (Integer memId : groupEntity.getlistGroupMemberIds()) {
                    UserEntity user = imService.getContactManager().findContact(memId);
                    if (user == null) {
                        user = imService.getContactManager().findDeviceContact(memId);
                        if (user == null) {
                            memberList.add(memId);
                        }
                    }
                }
                if (memberList.size() > 0) {
                    imService.getContactManager().reqGetDetaillUsers(memberList);
                }
            }
        }

        if (peerEntity != null && peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {

            UserEntity loginInfo = imService.getLoginManager().getLoginInfo();
            GroupNickEntity entity = imService.getGroupManager().findGroupNick(
                    peerEntity.getPeerId(), loginInfo.getPeerId());

            if (entity != null) {
                if (entity.getStatus() == DBConstant.SHOW_GROUP_NICK_CLOSE) {
                    isShowNick = false;
                } else {
                    isShowNick = true;
                }
            } else {
                isShowNick = true;
            }
        }

        // 头像、历史消息加载、取消通知
        setTitleByUser();
        reqHistoryMsg();
        initMessageBg();

        if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
            peerUser = imService.getContactManager().findContact(
                    peerEntity.getPeerId());
        }

        int actId = 0;
        if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
            GroupEntity groupEntity = (GroupEntity) peerEntity;
            if (groupEntity.getGroupType() != DBConstant.GROUP_TYPE_FAMILY) {

                GroupEntity userGroup = (GroupEntity) peerEntity;
                if (userGroup.getSave() == DBConstant.GROUP_MEMBER_STATUS_EXIT) {
                    topRightBtn.setVisibility(View.GONE);
                }
            }
            setRightButton(R.drawable.nav_group); //guanweile

        } else {
            //??
            setRightButton(R.drawable.tt_tab_me_nor); //guanweile
        }

        adapter.setImService(imService, loginUser, peerEntity.getPeerId(),
                actId, peerEntity);
        // imService.getUnReadMsgManager().readUnreadSession(currentSessionKey);
        imService.getNotificationManager().cancelSessionNotifications(
                currentSessionKey);
    }

    public Bitmap getSmallBitmap(String filepath, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filepath, options);
    }

    public void initMessageBg() {
        String filePath = null;
        SharedPreferences sp = MessageActivity.this.getSharedPreferences(
                "select_bg", MODE_PRIVATE);
        if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
            filePath = sp.getString("group_" + peerEntity.getPeerId(), "0");

        } else if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
            filePath = sp.getString("single_" + peerEntity.getPeerId(), "0");
        }
        if (filePath == null || filePath.length() <= 1) {
            filePath = sp.getString("message_bg_all", "0");
        }
        if (FileUtil.isFileExist(filePath)) {
            final String tempPath = filePath;
            message_bg.post(new Runnable() {
                @Override
                public void run() {
                    int weight = message_bg.getHeight();
                    int height = message_bg.getWidth();
                    Bitmap photo = getSmallBitmap(tempPath, weight, height);
                    BitmapDrawable bd = new BitmapDrawable(getResources(), photo);
                    message_bg.setBackground(bd);
                }
            });
        }else{
            message_bg.setBackgroundResource(R.color.default_layout_color);
        }
    }

    public void initMessageBg_old() {
        String file = null;
        SharedPreferences sp = MessageActivity.this.getSharedPreferences(
                "select_bg", MODE_PRIVATE);
        if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
            file = sp.getString("group_" + peerEntity.getPeerId(), "0");

        } else if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
            file = sp.getString("single_" + peerEntity.getPeerId(), "0");
        }

        if (file != null && file.equals("0")) {

            file = sp.getString("message_bg_all", "0");
            if (FileUtil.isFileExist(file)) {
                Bitmap bmp = BitmapFactory.decodeFile(file);
                Drawable drawable = new BitmapDrawable(bmp);
                message_bg.setBackground(drawable);
            } else {
                message_bg.setBackgroundResource(R.color.default_layout_color);
            }

        } else if (file != null && (!file.equals(""))) {
            if (FileUtil.isFileExist(file)) {
                Bitmap bmp = BitmapFactory.decodeFile(file);
                Drawable drawable = new BitmapDrawable(bmp);
                message_bg.setBackground(drawable);
            } else {
                message_bg.setBackgroundResource(R.color.default_layout_color);
            }
        } else {
            file = sp.getString("message_bg_all", "0");
            if (FileUtil.isFileExist(file)) {
                Bitmap bmp = BitmapFactory.decodeFile(file);
                Drawable drawable = new BitmapDrawable(bmp);
                message_bg.setBackground(drawable);
            } else {
                message_bg.setBackgroundResource(R.color.default_layout_color);
            }
        }
    }

    private void initSoftInputMethod() {
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        receiver = new switchInputMethodReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.INPUT_METHOD_CHANGED");
        registerReceiver(receiver, filter);

        SystemConfigSp.instance().init(this);
        currentInputMethod = Settings.Secure.getString(
                MessageActivity.this.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);

        keyboardHeight = SystemConfigSp.instance().getIntConfig(currentInputMethod);

        if (keyboardHeight == 0) {  //如果是设备 模式人一个值 (因为帐户的登陆界面获取了键盘的高的了)
            keyboardHeight = 546;
        }

    }


    public static int getDpi(Context context) {
        int dpi = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, displayMetrics);
            dpi = displayMetrics.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }


    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }


    /**
     * 获取 虚拟按键的高度
     *
     * @param context
     * @return
     */
    public static int getBottomStatusHeight(Context context) {

        int totalHeight = getDpi(context);
        int contentHeight = getScreenHeight(context);
        return totalHeight - contentHeight;
    }

    /**
     * 本身位于Message页面，点击通知栏其他session的消息
     */
    @Override
    protected void onNewIntent(Intent intent) {
        logger.d("message_activity#onNewIntent:%s", this);
        super.onNewIntent(intent);
        setIntent(intent);
        historyTimes = 0;
        if (intent == null) {
            return;
        }
        String newSessionKey = getIntent().getStringExtra(
                IntentConstant.KEY_SESSION_KEY);
        if (newSessionKey == null) {
            return;
        }
        logger.d("chat#newSessionInfo:%s", newSessionKey);
        if (!newSessionKey.equals(currentSessionKey)) {
            currentSessionKey = newSessionKey;
            initData();
        }
    }

    @Override
    protected void onResume() {
        logger.d("message_activity#onresume:%s", this);
        super.onResume();
        IMApplication.gifRunning = true;
        historyTimes = 0;
        // not the first time
        boolean mIsTelephoneMode = mPreference.getBoolean("speaker", false);
        if (mIsTelephoneMode) {
            mTelephoneIcon.setVisibility(View.VISIBLE);
        }
        Boolean isActive = inputManager.isActive();
        Log.i("aaa", "onResume: " + isActive);

        if (messageEdt.hasFocus()) {
            //显示panel
            if (isActive) {
                showDevicePanel();
            } else {
                hideDeviceView();
            }
        }

        if (imService != null) {
            // 处理session的未读信息
            handleUnreadMsgs();
        }
    }

    @Override
    protected void onDestroy() {
        logger.d("message_activity#onDestroy:%s", this);
        historyTimes = 0;
        imServiceConnector.disconnect(this);
        adapter.clearItem();
        if (albumList != null) {
            albumList.clear();
        }

        sensorManager.unregisterListener(this, sensor);
        ImageMessage.clearImageMessageList();
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    /**
     * 设定聊天名称 1. 如果是user类型， 点击触发UserProfile 2. 如果是群组，检测自己是不是还在群中
     */
    private void setTitleByUser() {
        int peerType = peerEntity.getType();
        switch (peerType) {
            case DBConstant.SESSION_TYPE_GROUP: {
                setTitle(peerEntity.getMainName());
                GroupEntity group = (GroupEntity) peerEntity;
                Set<Integer> memberLists = group.getlistGroupMemberIds();
                if (!memberLists.contains(loginUser.getPeerId())) {
                    group.setSave(DBConstant.GROUP_MEMBER_STATUS_EXIT);
                    Utils.showToast(MessageActivity.this, R.string.no_group_member);
                    // add  gzc
                    hideRightButton();
                }

                if (group.getSave() == DBConstant.GROUP_MEMBER_STATUS_EXIT) {
                    topRightBtn.setVisibility(View.GONE);
                }
            }
            break;
            case DBConstant.SESSION_TYPE_SINGLE: {
                UserEntity user = (UserEntity) peerEntity;
                if (!(user.getComment().equals(""))) {
                    setTitle(user.getComment());
                } else {
                    setTitle(user.getMainName());
                }


                if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {

                    GroupEntity groupEntity = (GroupEntity) peerEntity;
                    if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
                        hideBottom();
                    }
                }
                // guanweile
                topTitleTxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // guanweile
                        IMUIHelper.openUserProfileActivity(MessageActivity.this,
                                peerEntity.getPeerId(), false);
                    }
                });

            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode)
            return;
        switch (requestCode) {
            case SysConstant.CAMERA_WITH_DATA:
                handleTakePhotoData(data);
                break;
            case SysConstant.ALBUM_BACK_DATA:
                logger.d("pic#ALBUM_BACK_DATA");
                setIntent(data);
                break;
            case 200:
                if (resultCode == RESULT_OK) {
                    // 成功
                    path = data.getStringExtra("path");


                    /*
                    final String[] imagePath = {""};
                    new Thread() {
                        public void run() {
                            final Bitmap bitmap = Utils
                                    .createVideoThumbnail(path);
                            if (bitmap != null) {
                                imagePath[0] = Utils.saveImageUrl(bitmap);
                            }
                        }
                    }.start();
                    */

                    String imagePath = "";
                    final Bitmap bitmap = Utils
                            .createVideoThumbnail(path);
                    if (bitmap != null) {
                        imagePath = Utils.saveImageUrl(bitmap);
                    } else {
                        onRecordVedioEnd(path, "");
                    }

                    if (imagePath != null && (!imagePath.equals(""))) {
                        onRecordVedioEnd(path, imagePath);
                    }


                } else {
                    // 失败
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void handleImagePickData(List<ImageItem> list) {
        ArrayList<ImageMessage> listMsg = new ArrayList<>();
        ArrayList<ImageItem> itemList = (ArrayList<ImageItem>) list;
        for (ImageItem item : itemList) {
            ImageMessage imageMessage = ImageMessage.buildForSend(item,
                    loginUser, peerEntity);
            listMsg.add(imageMessage);
            pushList(imageMessage);
        }
        imService.getMessageManager().sendImages(listMsg);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = SysConstant.MESSAGE_EVENTBUS_PRIORITY)
    public void onMessageEvent(SelectEvent event) {
        List<ImageItem> itemList = event.getList();
        if (itemList != null || itemList.size() > 0)
            handleImagePickData(itemList);
    }

    /**
     * 以下是EventBus 2.x 版本的注释，现已升级<br/>
     * <p/>
     * 背景: 1.EventBus的cancelEventDelivery的只能在postThread中运行，而且没有办法绕过这一点 2.
     * onEvent(A a) onEventMainThread(A a) 这个两个是没有办法共存的 解决:
     * 抽离出那些需要优先级的event，在onEvent通过handler调用主线程， 然后cancelEventDelivery
     * <p/>
     * todo need find good solution
     */
    @Subscribe(priority = SysConstant.MESSAGE_EVENTBUS_PRIORITY)
    public void onMessageEvent(PriorityEvent event) {
        switch (event.event) {
            case MSG_RECEIVED_MESSAGE: {
                MessageEntity entity = (MessageEntity) event.object;
                /** 正式当前的会话 */
                if (currentSessionKey.equals(entity.getSessionKey())) {
                    Message message = Message.obtain();
                    message.what = HandlerConstant.MSG_RECEIVED_MESSAGE;
                    message.obj = entity;
                    uiHandler.sendMessage(message);
                    EventBus.getDefault().cancelEventDelivery(event);
                }
            }
            break;

            case MSG_DEV_MESSAGE: {
                MessageEntity entity = (MessageEntity) event.object;
                /** 正式当前的会话 */
                if (currentSessionKey.equals(entity.getSessionKey())) {
                    Message message = Message.obtain();
                    message.what = HandlerConstant.MSG_DEV_MESSAGE;
                    message.obj = entity;
                    uiHandler.sendMessage(message);
                    EventBus.getDefault().cancelEventDelivery(event);
                }
            }
            break;


            case MSG_VEDIO_ONLINE_DEV_START: {
                DevVedioInfo info = (DevVedioInfo) event.object;
                if (info != null) {
                    Intent intent1 = new Intent(MessageActivity.this, HosterActivity.class);
                    intent1.putExtra(IntentConstant.KEY_PEERID, peerEntity.getPeerId());
                    intent1.putExtra(IntentConstant.KEY_DEV_URL, info.getPushUrl());
                    intent1.putExtra(IntentConstant.KEY_DEV_VEDIO, DBConstant.DEV_VEDIO_TYPE);

                    MessageActivity.this.startActivity(intent1);
                }

            }
            break;

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = SysConstant.MESSAGE_EVENTBUS_PRIORITY)
    public void onMessageEvent(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_UPDATE: {

                if (imService != null) {
                    peerEntity = imService.getSessionManager().findPeerEntity(
                            currentSessionKey);

                    if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
                        peerUser = imService.getContactManager().findContact(
                                peerEntity.getPeerId());
                    }
                }

            }
            setTitleByUser();
            updateYuFirendsReq();
            adapter.notifyDataSetChanged();
            break;
            case USER_UPDATE_MESSAGE_BG_SUCCESS: {
                initMessageBg();
            }
            break;

            case USER_INFO_DELETE_DATA_SUCCESS:
                if (imService != null) {
                    peerEntity = imService.getSessionManager().findPeerEntity(
                            currentSessionKey);
                    if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
                        peerUser = imService.getContactManager().findContact(
                                peerEntity.getPeerId());
                    }
                    initData();

                    setTitleByUser();
                    updateYuFirendsReq();
                    adapter.notifyDataSetChanged();
                }

                break;


            case USER_INFO_DEV_DATA_SUCCESS:

                if (imService != null) {

                    peerEntity = imService.getSessionManager().findPeerEntity(
                            currentSessionKey);
                    if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
                        peerUser = imService.getContactManager().findContact(
                                peerEntity.getPeerId());
                    }
                    initData();

                    setTitleByUser();
                    updateYuFirendsReq();
                    adapter.notifyDataSetChanged();

                }
                break;


            case USER_INFO_REQ_FRIENDS_SUCCESS:
                break;
            case USER_P2PCOMMAND_OFFLINE_HINT:
                Utils.showToast(this, "对方不在线");
                break;
            case USER_P2PCOMMAND_ONLINE:
                Utils.showToast(MessageActivity.this, "你的请求已发送,请稍候");
                break;

            case WEI_FRIENDS_REQ_SUCCESS: {

                if (imService != null) {
                    peerEntity = imService.getSessionManager().findPeerEntity(
                            currentSessionKey);
                    if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
                        peerUser = imService.getContactManager().findContact(
                                peerEntity.getPeerId());
                    }
                    setTitleByUser();
                    updateYuFirendsReq();
                    adapter.notifyDataSetChanged();
                }
            }
            break;

            case WEI_FRIENDS_INFO_REQ_ALL: {

                if (imService != null) {
                    peerEntity = imService.getSessionManager().findPeerEntity(
                            currentSessionKey);
                    if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
                        peerUser = imService.getContactManager().findContact(
                                peerEntity.getPeerId());
                    }
                    setTitleByUser();
                    updateYuFirendsReq();
                    adapter.notifyDataSetChanged();
                }

            }
            break;

            case USER_INFO_REQ_FRIENDS_FAIL:
                Utils.showToast(MessageActivity.this, "请求加好友失败");
                break;

            case USER_COMMAND_TYPE_TAKE_PHOTO:
                Utils.showToast(MessageActivity.this, "发送成功");
                break;

            case USER_COMMAND_TYPE_SOUND_COPY:
                Utils.showToast(MessageActivity.this, "发送成功");
                break;

            case USER_MESSAGE_DATA_FAIL:
                Utils.showToast(MessageActivity.this, "网络异常,获取消息失败");
                break;
        }
    }

    void updateYuFirendsReq() {
        boolean isReqWei = false;
        int actId = 0;

        adapter.setImService(imService, loginUser, peerEntity.getPeerId(),
                actId, peerEntity);
    }

    /**
     * 事件驱动通知
     */
    @Subscribe(threadMode = ThreadMode.MAIN, priority = SysConstant.MESSAGE_EVENTBUS_PRIORITY)
    public void onMessageEvent(GroupEvent event) {
        switch (event.getEvent()) {

            case CHANGE_GROUP_MEMBER_FAIL:
            case CHANGE_GROUP_MEMBER_TIMEOUT: {
            }
            break;
            case CHANGE_GROUP_MEMBER_SUCCESS: {
                if (imService != null) {
                    peerEntity = imService.getSessionManager().findPeerEntity(
                            currentSessionKey);
                    if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
                        GroupEntity groupEntity = imService.getGroupManager().findGroup(
                                peerEntity.getPeerId());

                        if (groupEntity != null) {
                            if (groupEntity.getSave() == DBConstant.GROUP_MEMBER_STATUS_EXIT) {
                                topRightBtn.setVisibility(View.GONE);
                            } else {
                                topRightBtn.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

            }
            break;
            case CHANGE_GROUP_DELETE_SUCCESS: {
                GroupEntity groupEntity = event.getGroupEntity();
                if (peerEntity.getPeerId() == groupEntity.getPeerId()) {
                    Utils.showToast(this, getString(R.string.exit_group_hint));
                    this.finish();
                }
            }
            break;
            case USER_GROUP_DELETE_SUCCESS: {
                GroupEntity groupEntity = event.getGroupEntity();
                if (peerEntity.getPeerId() == groupEntity.getPeerId()) {
                    Utils.showToast(MessageActivity.this, getString(R.string.exit_group_hint_word));
                    this.finish();
                }
            }
            break;
            case CHANGE_GROUP_NICK_SUCCESS: { // 群聊昵称
                adapter.notifyDataSetChanged();
            }
            break;


            case CHANGE_GROUP_DELETE_TIMEOUT: {
                Utils.showToast(this, "退出群失败");
            }
            break;

            case CHANGE_GROUP_MODIFY_FAIL: {
            }
            break;
            case CHANGE_GROUP_MODIFY_TIMEOUT: {
            }
            break;
            case CHANGE_GROUP_MODIFY_SUCCESS: {
                onModifyberChangeSuccess();
            }
            break;

        }
    }

    private void onModifyberChangeSuccess() {

        if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
            peerEntity = imService.getSessionManager().findPeerEntity(
                    currentSessionKey);
            setTitle(peerEntity.getMainName());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = SysConstant.MESSAGE_EVENTBUS_PRIORITY)
    public void onMessageEvent(MessageEvent event) {
        MessageEvent.Event type = event.getEvent();
        switch (type) {
            case ACK_SEND_MESSAGE_OK: {
                onMsgAck(event.getMessageEntity());
            }
            break;

            case IMAGE_VEDIO_LOADED_SUCCESS:
//                pushList(event.getMessageEntity());
                break;

            case CARD_SUCCESS: {
                pushList(event.getMessageEntity());
            }
            break;

            case VEDIO_SUCCESS: {
                pushList(event.getMessageEntity());
            }
            break;

            case VEDIO_ONLIE_SUCCESS: {
                //这条消息是当前界面的消息　才更新
                if (event.getMessageEntity().getToId() == peerEntity.getPeerId()) {
                    pushList(event.getMessageEntity());
                }
            }
            break;


            case DEL_FRIENDS_SUCCESS: {
                //这条消息是当前界面的消息　才更新
                pushList(event.getMessageEntity());
            }
            break;
            case NOTICE_SUCCESS: {
                pushList(event.getMessageEntity());
            }
            break;
            case POSTION_SUCCESS: {
                pushList(event.getMessageEntity());
            }
            break;

            case USER_INFO_DEV_POS_DATA_SUCCESS: {
                adapter.updateItemState(event.getMessageEntity());

            }
            break;


            case ACK_SEND_MESSAGE_FAILURE:
                // 失败情况下新添提醒
                //showToast(R.string.message_send_failed);
            case ACK_SEND_MESSAGE_TIME_OUT: {
                onMsgUnAckTimeoutOrFailure(event.getMessageEntity());
            }
            break;

            case HANDLER_VEDIO_UPLOAD_FAILD: {
                logger.d("pic#onUploadImageFaild");
                VedioMessage vedioMessage = (VedioMessage) event.getMessageEntity();
                adapter.updateItemState(vedioMessage);
                showToast(R.string.message_send_failed);
            }
            break;

            case HANDLER_VEDIO_UPLOAD_SUCCESS: {
                VedioMessage vedioMessage = (VedioMessage) event.getMessageEntity();
                adapter.updateItemState(vedioMessage);
            }
            break;

            case HANDLER_IMAGE_UPLOAD_FAILD: {
                logger.d("pic#onUploadImageFaild");
                ImageMessage imageMessage = (ImageMessage) event.getMessageEntity();
                adapter.updateItemState(imageMessage);
//                pushList(imageMessage);
                showToast(R.string.message_send_failed);
            }
            break;


            case HANDLER_VEDIO_IMAGE_UPLOAD_FAILD: {
                logger.d("pic#onUploadImageFaild");
                VedioMessage imageMessage = (VedioMessage) event.getMessageEntity();
                adapter.updateItemState(imageMessage);
//                pushList(imageMessage);
                showToast(R.string.message_send_failed);
            }
            break;


            case HANDLER_IMAGE_UPLOAD_SUCCESS: {
                ImageMessage imageMessage = (ImageMessage) event.getMessageEntity();
                adapter.updateItemState(imageMessage);
            }
            break;

            case HISTORY_MSG_OBTAIN: {
                if (historyTimes == 1) {
                    adapter.clearItem();
                    reqHistoryMsg();
                }
            }
            break;


            case HISTORY_MSG_SERVER: {

//                ArrayList<MessageEntity> result = event.getMsgList();
//                pushList(result);
//                adapter.notifyDataSetChanged();
                sortHistoryMsg();

            }
            break;
        }
    }


    //创建一个Handler
    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2001: {
                    final MessageEntity entity = (MessageEntity) msg.obj;
                    new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            //实现页面跳转

                            Intent intent = new Intent(MessageActivity.this,
                                    GuestActivity.class);

                            OnLineVedioMessage message = OnLineVedioMessage.parseFromNet(entity);
                            String pushUrl = message.getPushUrl();
                            String pullUrl = message.getPullUrl();

                            intent.putExtra(IntentConstant.KEY_PEERID, message.getFromId());
                            intent.putExtra(IntentConstant.PUSHURL, pushUrl);
                            intent.putExtra(IntentConstant.PULLURL, pullUrl);

                            MessageActivity.this.startActivity(intent);


                            return false;
                        }
                    }).sendEmptyMessageDelayed(0, 600);//表示延迟3秒发送任务
                }
                break;
                default:
                    break;
            }
        }
    };

    /**
     * audio状态的语音还在使用这个
     */
    protected void initAudioHandler() {
        uiHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HandlerConstant.HANDLER_RECORD_FINISHED: {
                        onRecordVoiceEnd((Float) msg.obj);
                    }
                    break;

                    // 录音结束
                    case HandlerConstant.HANDLER_STOP_PLAY:
                        // 其他地方处理了
                        // adapter.stopVoicePlayAnim((String) msg.obj);
                        break;

                    case HandlerConstant.RECEIVE_MAX_VOLUME:
                        onReceiveMaxVolume((Integer) msg.obj);
                        break;

                    case HandlerConstant.RECEIVE_SPEAKER: {
                        boolean speaker = (Boolean) msg.obj;
                        if (speaker) {
                            mTelephoneIcon.setVisibility(View.VISIBLE);
                        } else {
                            mTelephoneIcon.setVisibility(View.GONE);
                        }
                    }
                    break;

                    case HandlerConstant.RECORD_AUDIO_TOO_LONG:
                        doFinishRecordAudio();
                        break;


                    case HandlerConstant.MSG_RECEIVED_MESSAGE:
                        MessageEntity entity = (MessageEntity) msg.obj;
                        onMsgRecv(entity);

//                        //如果在线视频
//                        if (entity.getMsgType() == DBConstant.MSG_TYPE_VIDEO_CALL) {
//
//                            Message message = new Message();
//                            message.what = 2001;
//                            message.obj = entity;
//                            handler.sendMessage(message);
//
//                        }
                        break;


                    case HandlerConstant.MSG_DEV_MESSAGE:
                        MessageEntity entity1 = (MessageEntity) msg.obj;
                        onMsgDevRecv(entity1);
                        break;

                    default:
                        break;
                }
            }
        };
    }

    /**
     * [备注] DB保存，与session的更新manager已经做了
     *
     * @param messageEntity
     */
    private void onMsgAck(MessageEntity messageEntity) {
        logger.d("message_activity#onMsgAck");
        int msgId = messageEntity.getMsgId();
        logger.d("chat#onMsgAck, msgId:%d", msgId);

        /** 到底采用哪种ID呐?? */
        long localId = messageEntity.getId();
        adapter.updateItemState(messageEntity);
    }

    private void handleUnreadMsgs() {
        logger.d("messageacitivity#handleUnreadMsgs sessionId:%s",
                currentSessionKey);
        // 清除未读消息
        UnreadEntity unreadEntity = imService.getUnReadMsgManager().findUnread(
                currentSessionKey);
        if (null == unreadEntity) {
            return;
        }
        int unReadCnt = unreadEntity.getUnReadCnt();
        if (unReadCnt > 0) {
            imService.getNotificationManager().cancelSessionNotifications(
                    currentSessionKey);
            adapter.notifyDataSetChanged();
            scrollToBottomListItem();
        }
    }

    // 肯定是在当前的session内
    private void onMsgRecv(MessageEntity entity) {
        logger.d("message_activity#onMsgRecv");

        imService.getUnReadMsgManager().ackReadMsg(entity);
        logger.d("chat#start pushList");
        pushList(entity);
        ListView lv = lvPTR.getRefreshableView();
        if (lv != null) {

            if (lv.getLastVisiblePosition() < adapter.getCount()) {
                textView_new_msg_tip.setVisibility(View.VISIBLE);
            } else {
                scrollToBottomListItem();
            }
        }
    }

    // 肯定是在当前的session内
    private void onMsgDevRecv(MessageEntity entity) {
        logger.d("message_activity#onMsgRecv");

        // imService.getUnReadMsgManager().ackReadMsg(entity);
        logger.d("chat#start pushList");
        pushList(entity);
        ListView lv = lvPTR.getRefreshableView();
        if (lv != null) {

            if (lv.getLastVisiblePosition() < adapter.getCount()) {
                textView_new_msg_tip.setVisibility(View.VISIBLE);
            } else {
                scrollToBottomListItem();
            }
        }
    }

    private void onMsgUnAckTimeoutOrFailure(MessageEntity messageEntity) {
        logger.d("chat#onMsgUnAckTimeoutOrFailure, msgId:%s",
                messageEntity.getMsgId());
        // msgId 应该还是为0
        adapter.updateItemState(messageEntity);
    }

    /**
     * @Description 显示联系人界面
     */

    private void showGroupManageActivity() {
        Intent i = new Intent(this, GroupManagermentActivity.class);
        i.putExtra(IntentConstant.KEY_SESSION_KEY, currentSessionKey);
        startActivity(i);
    }

    /**
     * @Description 初始化AudioManager，用于访问控制音量和钤声模式
     */
    private void initAudioSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * @Description 初始化数据（相册,表情,数据库相关）
     */
    private void initAlbumHelper() {
        albumHelper = AlbumHelper.getHelper(MessageActivity.this);
        albumList = albumHelper.getImagesBucketList(false);
    }

    private void initEmo() {
        Emoparser.getInstance(MessageActivity.this);
        IMApplication.gifRunning = true;
    }


    /**
     * @Description 初始化界面控件 有点庞大 todo
     */
    private void initView() {
        // 绑定布局资源(注意放所有资源初始化之前)
        LayoutInflater.from(this).inflate(R.layout.tt_activity_message,
                topContentView);


        // TOP_CONTENT_VIEW
        setLeftButton(R.drawable.icon_arrow_friends_info);
        setLeftText(getResources().getString(R.string.top_left_back));
        setRightButton(R.drawable.tt_tab_me_nor); //guanweile
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        topRightBtn.setOnClickListener(this);
//        mLlDevicePanel = (LinearLayout) findViewById(R.id.add_device_panel);
        message_bg = (LinearLayout) this.findViewById(R.id.message_bg);
        mTelephoneIcon = (ImageView) topContentView.findViewById(R.id.base_activity_telephone_icon);
        tv = new TextView(this);
        tv.setText("听筒");


        // 列表控件(开源PTR)
        lvPTR = (PullToRefreshListView) this.findViewById(R.id.message_list);
        textView_new_msg_tip = (TextView) findViewById(R.id.tt_new_msg_tip);
        lvPTR.getRefreshableView().addHeaderView(
                LayoutInflater.from(this).inflate(
                        R.layout.tt_messagelist_header,
                        lvPTR.getRefreshableView(), false));
        Drawable loadingDrawable = getResources().getDrawable(
                R.drawable.pull_to_refresh_indicator);
        final int indicatorWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 29, getResources()
                        .getDisplayMetrics());
        loadingDrawable
                .setBounds(new Rect(0, indicatorWidth, 0, indicatorWidth));
        lvPTR.getLoadingLayoutProxy().setLoadingDrawable(loadingDrawable);
        // lvPTR.getRefreshableView().setCacheColorHint(Color.WHITE);
        // lvPTR.getRefreshableView().setSelector(new
        // ColorDrawable(Color.WHITE));
        lvPTR.getRefreshableView().setCacheColorHint(Color.TRANSPARENT);
        lvPTR.getRefreshableView().setSelector(
                new ColorDrawable(Color.TRANSPARENT));

        lvPTR.getRefreshableView().setOnTouchListener(lvPTROnTouchListener);
        adapter = new MessageAdapter(this);

        lvPTR.setAdapter(adapter);
        scrollToBottomListItem();
        lvPTR.setOnRefreshListener(this);
        lvPTR.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true) {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                            textView_new_msg_tip.setVisibility(View.GONE);
                        }
                        break;
                }
            }
        });
        textView_new_msg_tip.setOnClickListener(this);


        // 界面底部输入框布局
        sendBtn = (TextView) this.findViewById(R.id.send_message_btn);
        recordAudioBtn = (Button) this.findViewById(R.id.record_voice_btn);
        audioInputImg = (ImageView) this.findViewById(R.id.voice_btn);
        messageEdt = (CustomEditView) this.findViewById(R.id.message_text);
        // if (!messageEdt.hasFocus()) {
        //     messageEdt.requestFocus();
        // }
        isHideEmoLayout = true;

        /*
        inputManager.toggleSoftInputFromWindow(
                messageEdt.getWindowToken(), 1, 0);
     */
        messageEdt.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            //当键盘弹出隐藏的时候会 调用此方法。
            @Override
            public void onGlobalLayout() {
//                if(!messageEdt.hasFocus()){
//                    return;
//                }


                //获取底部状态栏的高度
                Resources resources = MessageActivity.this.getResources();
                int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                int navigation_bar_height = resources.getDimensionPixelSize(resourceId);
                Rect r = new Rect();
                MessageActivity.this.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                //获取屏幕的高度
                int screenHeight = MessageActivity.this.getWindow().getDecorView().getRootView().getHeight();
                //此处就是用来获取键盘的高度的， 在键盘没有弹出的时候 此高度为0 键盘弹出的时候为一个正数

                int keyboardHeightTemp = 0;
                // if(phoneMode == 2 || phoneMode == 1){
                keyboardHeightTemp = screenHeight - r.bottom;
                //}else{
                // keyboardHeightTemp = screenHeight - r.bottom  - navigation_bar_height;
                //}

                if (keyboardHeightTemp > navigation_bar_height) {
                    keyboardHeight = keyboardHeightTemp;
                    SystemConfigSp.instance().setIntConfig(currentInputMethod, keyboardHeight);

                    //隐藏deviceView
                    Log.i("aaa", "onGlobalLayout: " + "显示键盘: " + keyboardHeightTemp);

//                    hideDeviceView();

                    LayoutParams params = (LayoutParams) addOthersPanelView
                            .getLayoutParams();
                    params.height = keyboardHeight;

                    LayoutParams params1 = (LayoutParams) emoLayout
                            .getLayoutParams();
                    params1.height = keyboardHeight;

                } else {
                    Log.i("aaa", "onGlobalLayout: " + "隐藏键盘: " + keyboardHeightTemp);
//                    keyboardHeight = 0;
                }
            }

        });


        RelativeLayout.LayoutParams messageEdtParam = (LayoutParams) messageEdt
                .getLayoutParams();
        messageEdtParam.addRule(RelativeLayout.LEFT_OF, R.id.show_emo_btn);
        messageEdtParam.addRule(RelativeLayout.RIGHT_OF, R.id.voice_btn);
        keyboardInputImg = (ImageView) this
                .findViewById(R.id.show_keyboard_btn);
        addPhotoBtn = (ImageView) this.findViewById(R.id.show_add_photo_btn);
        addEmoBtn = (ImageView) this.findViewById(R.id.show_emo_btn);

        messageEdt.setOnFocusChangeListener(msgEditOnFocusChangeListener);
        messageEdt.setOnClickListener(this);
        messageEdt.addTextChangedListener(this);

        addPhotoBtn.setOnClickListener(this);
        addEmoBtn.setOnClickListener(this);
        keyboardInputImg.setOnClickListener(this);
        audioInputImg.setOnClickListener(this);
        recordAudioBtn.setOnTouchListener(this);
        sendBtn.setOnClickListener(this);
        initSoundVolumeDlg();

        // OTHER_PANEL_VIEW
        addOthersPanelView = findViewById(R.id.add_others_panel);

        LayoutParams params = (LayoutParams) addOthersPanelView.getLayoutParams();
//        LayoutParams llDeviceParams = (LayoutParams)mLlDevicePanel.getLayoutParams();
        if (keyboardHeight > 0) {
            params.height = keyboardHeight;
//            llDeviceParams.height = keyboardHeight;
            addOthersPanelView.setLayoutParams(params);
//            mLlDevicePanel.setLayoutParams(llDeviceParams);
        }

        isHideDviceView = true;

        // EMO_LAYOUT
        emoLayout = (LinearLayout) findViewById(R.id.emo_layout);
        LayoutParams paramEmoLayout = (LayoutParams) emoLayout
                .getLayoutParams();


        if (keyboardHeight > 0) {
            paramEmoLayout.height = keyboardHeight;
            emoLayout.setLayoutParams(paramEmoLayout);
        }
        emoGridView = (EmoGridView) findViewById(R.id.emo_gridview);
        //   webView = (ProgressWebView) findViewById(R.id.webView);

        yayaEmoGridView = (YayaEmoGridView) findViewById(R.id.yaya_emo_gridview);
        emoRadioGroup = (RadioGroup) findViewById(R.id.emo_tab_group);
        emoGridView.setOnEmoGridViewItemClick(onEmoGridViewItemClick);
        //  messageEdt.setOnKeyListener(onEmoGridViewDelClick);

        emoGridView.setAdapter();
        yayaEmoGridView.setOnEmoGridViewItemClick(yayaOnEmoGridViewItemClick);
        yayaEmoGridView.setAdapter();

        // 去掉牙牙表情
        //emoRadioGroup.setOnCheckedChangeListener(emoOnCheckedChangeListener);
        emoRadioGroup.setVisibility(View.GONE);

        yayaEmoGridView.setVisibility(View.GONE);
        emoGridView.setVisibility(View.VISIBLE);


        //test
//        emoGridView.setVisibility(View.GONE);
//        webView.setVisibility(View.VISIBLE);


        // LOADING
        View view = LayoutInflater.from(MessageActivity.this).inflate(
                R.layout.tt_progress_ly, null);
        progressbar = (MGProgressbar) view.findViewById(R.id.tt_progress);
        LayoutParams pgParms = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        pgParms.bottomMargin = 50;
        addContentView(view, pgParms);

        hideDeviceView();

        // ROOT_LAYOUT_LISTENER
//        baseRoot.getViewTreeObserver().addOnGlobalLayoutListener(
//                onGlobalLayoutListener);
    }

    /**
     * @Description 初始化音量对话框
     */
    private void initSoundVolumeDlg() {
        soundVolumeDialog = new Dialog(this, R.style.SoundVolumeStyle);
        soundVolumeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        soundVolumeDialog.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        soundVolumeDialog.setContentView(R.layout.tt_sound_volume_dialog);
        soundVolumeDialog.setCanceledOnTouchOutside(true);
        soundVolumeImg = (ImageView) soundVolumeDialog
                .findViewById(R.id.sound_volume_img);
        soundVolumeLayout = (LinearLayout) soundVolumeDialog
                .findViewById(R.id.sound_volume_bk);
    }

    public void hideBottom() {
        topRightBtn.setVisibility(View.GONE);
        soundVolumeLayout.setVisibility(View.GONE);
        recordAudioBtn.setVisibility(View.GONE);
        keyboardInputImg.setVisibility(View.GONE);
//		messageEdt.setVisibility(View.GONE);
//		audioInputImg.setVisibility(View.GONE);
//		addEmoBtn.setVisibility(View.GONE);
        emoLayout.setVisibility(View.GONE);
        addOthersPanelView.setVisibility(View.GONE);
        sendBtn.setVisibility(View.GONE);
//        mLlDevicePanel.setVisibility(View.GONE);
//		RelativeLayout tt_layout_bottom = (RelativeLayout) this
//				.findViewById(R.id.tt_layout_bottom);
//		tt_layout_bottom.setVisibility(View.GONE);
    }

    /**
     * 1.初始化请求历史消息 2.本地消息不全，也会触发
     */
    private void reqHistoryMsg() {
        historyTimes++;
        List<MessageEntity> msgList = imService.getMessageManager().loadHistoryMsg(historyTimes, currentSessionKey, peerEntity);
        pushList(msgList);
        scrollToBottomListItem();
    }


    /**
     * 消息排序
     */
    private void sortHistoryMsg() {
        historyTimes++;
        List<MessageEntity> msgList = imService.getMessageManager().loadSortHistoryMsg(historyTimes, currentSessionKey, peerEntity);
        if (msgList.size() > 0) {
            adapter.clearItem();
        }
        pushList(msgList);
        scrollToBottomListItem();

    }

    /**
     * @param msg
     */
    public void pushList(MessageEntity msg) {
        logger.d("chat#pushList msgInfo:%s", msg);
        if (msg.getMsgType() == DBConstant.MSG_TYPE_VIDEO_CALL
                || msg.getMsgType() == DBConstant.MSG_TYPE_VIDEO_ANSWER) {

        } else {
            adapter.addItem(msg);
        }
    }

    public void pushList(List<MessageEntity> entityList) {
        logger.d("chat#pushList list:%d", entityList.size());
        adapter.loadHistoryList(entityList);
    }

    /**
     * @Description 录音超时(60s)，发消息调用该方法
     */
    public void doFinishRecordAudio() {
        try {
            if (audioRecorderInstance.isRecording()) {
                audioRecorderInstance.setRecording(false);
            }
            if (soundVolumeDialog.isShowing()) {
                soundVolumeDialog.dismiss();
            }

            recordAudioBtn.setBackgroundResource(R.drawable.tt_pannel_btn_voiceforward_normal);

            audioRecorderInstance.setRecordTime(SysConstant.MAX_SOUND_RECORD_TIME);
            onRecordVoiceEnd(SysConstant.MAX_SOUND_RECORD_TIME);
        } catch (Exception e) {
        }
    }


    /**
     * @param voiceValue
     * @Description 根据分贝值设置录音时的音量动画
     */
    private void onReceiveMaxVolume(int voiceValue) {
        if (voiceValue < 200.0) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_01);
        } else if (voiceValue > 200.0 && voiceValue < 600) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_02);
        } else if (voiceValue > 600.0 && voiceValue < 1200) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_03);
        } else if (voiceValue > 1200.0 && voiceValue < 2400) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_04);
        } else if (voiceValue > 2400.0 && voiceValue < 10000) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_05);
        } else if (voiceValue > 10000.0 && voiceValue < 28000.0) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_06);
        } else if (voiceValue > 28000.0) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_07);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    /**
     * @param data
     * @Description 处理拍照后的数据 应该是从某个 activity回来的
     */
    private void handleTakePhotoData(Intent data) {
        ImageMessage imageMessage = ImageMessage.buildForSend(
                takePhotoSavePath, loginUser, peerEntity);
        List<ImageMessage> sendList = new ArrayList<>(1);
        sendList.add(imageMessage);
        imService.getMessageManager().sendImages(sendList);
        // 格式有些问题
        pushList(imageMessage);
        messageEdt.clearFocus();// 消除焦点
    }

    /**
     * @param audioLen
     * @Description 录音结束后处理录音数据
     */
    private void onRecordVoiceEnd(float audioLen) {
        logger.d("message_activity#chat#audio#onRecordVoiceEnd audioLen:%f",
                audioLen);
        AudioMessage audioMessage = AudioMessage.buildForSend(audioLen,
                audioSavePath, loginUser, peerEntity);
        imService.getMessageManager().sendVoice(audioMessage);
        pushList(audioMessage);
    }

    /**
     * @param
     * @Description 视频结束结束后处理视频数据
     */
    private void onRecordVedioEnd(String vedioPath, String imagePath) {
        // logger.d("message_activity#chat#audio#onRecordVoiceEnd audioLen:%f",
        // vedioLen);
        // /VedioMessage vedioMessage = VedioMessage.buildForSend(vedioLen,
        // vedioSavePath,vedioEndSavePath, loginUser, peerEntity);

        VedioMessage vedioMessage = VedioMessage.buildForSend(vedioPath,
                loginUser, peerEntity, imagePath);
        imService.getMessageManager().sendVedio(vedioMessage, false);
//        pushList(vedioMessage);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

    }

    @Override
    public void onPullDownToRefresh(
            final PullToRefreshBase<ListView> refreshView) {
        // 获取消息
        refreshView.postDelayed(new Runnable() {
            @Override
            public void run() {
                ListView mlist = lvPTR.getRefreshableView();
                int preSum = mlist.getCount();
                MessageEntity messageEntity = adapter.getTopMsgEntity();
                if (messageEntity != null) {
                    List<MessageEntity> historyMsgInfo = imService
                            .getMessageManager().loadHistoryMsg(messageEntity,
                                    historyTimes);
                    if (historyMsgInfo.size() > 0) {
                        historyTimes++;
                        adapter.loadHistoryList(historyMsgInfo);
                    }
                }

                int afterSum = mlist.getCount();
                mlist.setSelection(afterSum - preSum);
                /** 展示位置为这次消息的最末尾 */
                // mlist.setSelection(size);
                // 展示顶部
                // if (!(mlist).isStackFromBottom()) {
                // mlist.setStackFromBottom(true);
                // }
                // mlist.setStackFromBottom(false);
                refreshView.onRefreshComplete();
            }
        }, 200);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.left_btn:
            case R.id.left_txt:
                actFinish();
                break;
            case R.id.right_btn:
                showGroupManageActivity();
                break;
            case R.id.show_add_photo_btn: {

//                UserEntity user = (UserEntity) peerEntity;
                recordAudioBtn.setVisibility(View.GONE);
                keyboardInputImg.setVisibility(View.GONE);  //显示键盘按钮
                messageEdt.setVisibility(View.VISIBLE);
                audioInputImg.setVisibility(View.VISIBLE);
                addEmoBtn.setVisibility(View.VISIBLE);

                isInput = false;
                addEmoBtn.setBackgroundResource(R.drawable.icon_expression);


                if (keyboardHeight != 0) {
                    this.getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                }

                if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {

                    GroupEntity groupEntity = (GroupEntity) peerEntity;
//                    if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
//
//
//                        if (mLlDevicePanel.getVisibility() == View.VISIBLE) {
//                        if (!messageEdt.hasFocus()) {
//                            messageEdt.requestFocus();
//                        }
//                            isHideDviceView = true;
//                        inputManager.toggleSoftInputFromWindow(
//                                messageEdt.getWindowToken(), 1, 0);
////                        if (keyboardHeight == 0) {   //键盘不显示，则panel不显示
////                            mLlDevicePanel.setVisibility(View.GONE);
////                        }
//                    } else if (mLlDevicePanel.getVisibility() == View.GONE) {
//
//                        mLlDevicePanel.setVisibility(View.VISIBLE);
//                        inputManager.hideSoftInputFromWindow(
//                                messageEdt.getWindowToken(), 0);
//                        isHideDviceView = false;
//                        isInput = false;
//                        addEmoBtn.setBackgroundResource(R.drawable.icon_expression);
//                    }
//
//                }else{

                    if (addOthersPanelView.getVisibility() == View.VISIBLE) {
                        if (!messageEdt.hasFocus()) {
                            messageEdt.requestFocus();
                        }
                        isHideDviceView = true;
                        inputManager.toggleSoftInputFromWindow(
                                messageEdt.getWindowToken(), 1, 0);
//                            if (keyboardHeight == 0) {
//                                addOthersPanelView.setVisibility(View.GONE);
//                            }
                    } else if (addOthersPanelView.getVisibility() == View.GONE) {
                        addOthersPanelView.setVisibility(View.VISIBLE);
                        inputManager.hideSoftInputFromWindow(
                                messageEdt.getWindowToken(), 0);
                        isHideDviceView = false;
                        isInput = false;
                        addEmoBtn.setBackgroundResource(R.drawable.icon_expression);
                    }

//                    }

                } else {
                    if (addOthersPanelView.getVisibility() == View.VISIBLE) {
                        if (!messageEdt.hasFocus()) {
                            messageEdt.requestFocus();
                        }
                        isHideDviceView = true;
                        inputManager.toggleSoftInputFromWindow(
                                messageEdt.getWindowToken(), 1, 0);
//                        if (keyboardHeight == 0) {
//                            addOthersPanelView.setVisibility(View.GONE);
//                        }
                    } else if (addOthersPanelView.getVisibility() == View.GONE) {
                        addOthersPanelView.setVisibility(View.VISIBLE);
                        isHideDviceView = false;
                        inputManager.hideSoftInputFromWindow(
                                messageEdt.getWindowToken(), 0);

                        isInput = false;
                        addEmoBtn.setBackgroundResource(R.drawable.icon_expression);
                    }

                }

                if (null != emoLayout && emoLayout.getVisibility() == View.VISIBLE) {
                    emoLayout.setVisibility(View.GONE);
                }

                scrollToBottomListItem();
            }
            break;


            case R.id.take_photo_btn: {
                if (albumList.size() < 1) {
                    Utils.showToast(MessageActivity.this,
                            getResources().getString(R.string.not_found_album));
                    return;
                }
                // 选择图片的时候要将session的整个回话 传过来
                Intent intent = new Intent(MessageActivity.this,
                        PickPhotoActivity.class);
                intent.putExtra(IntentConstant.KEY_SESSION_KEY, currentSessionKey);
                startActivityForResult(intent, SysConstant.ALBUM_BACK_DATA);

                MessageActivity.this.overridePendingTransition(
                        R.anim.tt_album_enter, R.anim.tt_stay);
                // addOthersPanelView.setVisibility(View.GONE);
                messageEdt.clearFocus();// 切记清除焦点
                scrollToBottomListItem();
            }
            break;
            case R.id.take_camera_btn: {
                requestRunPermisssion(Manifest.permission.CAMERA, new PermissionListener() {
                    @Override
                    protected void onGranted() {
                        takePhotoSavePath = CommonUtil.getImageSavePath(String
                                .valueOf(System.currentTimeMillis()) + ".jpg");
                        File file = new File(takePhotoSavePath);
                        CompatUtil.startActionCapture(MessageActivity.this, file, SysConstant.CAMERA_WITH_DATA);
                        // addOthersPanelView.setVisibility(View.GONE);
                        messageEdt.clearFocus();// 切记清除焦点
                        scrollToBottomListItem();
                    }

                    @Override
                    protected void onDenied(List<String> deniedPermission) {
                        Toast.makeText(MessageActivity.this, PermissionUtil.getPermissionString(MessageActivity.this, deniedPermission), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            break;
            case R.id.take_vedio_btn: {

                // 启动拍摄的Activity{
                Intent intent = new Intent(MessageActivity.this,
                        VedioActivity.class);
                MessageActivity.this.startActivityForResult(intent, 200);


            }
            break;


            case R.id.take_position_btn: {
                Intent intent = new Intent();
                intent.setClass(MessageActivity.this, MessagePostionActivity.class);
                intent.putExtra(IntentConstant.KEY_SESSION_KEY, currentSessionKey);
                MessageActivity.this.startActivity(intent);

            }
            break;

            case R.id.take_mingpian_btn: { // 名片
                Intent intent = new Intent();
                intent.setClass(MessageActivity.this, FriendsSelectActivity.class);
                intent.putExtra(IntentConstant.KEY_SESSION_KEY, currentSessionKey);
                MessageActivity.this.startActivity(intent);

            }
            break;

            case R.id.take_zhua_photo_btn: { // 抓拍
                if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
                    GroupEntity groupEntity = (GroupEntity) peerEntity;
                    if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
                        UserEntity user = getUserEntityFromGroupEntity(groupEntity);
                        if (user != null) {
                            int formId = groupEntity.getPeerId();
                            int toId = user.getPeerId();

                            imService.getUserActionManager().UserP2PCommand(formId, toId,
                                    SessionType.SESSION_TYPE_GROUP,
                                    CommandType.COMMAND_TYPE_TAKE_PHOTO, "", true);
                        }
                    }

                } else if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
                    UserEntity userEntity = (UserEntity) peerEntity;
                    if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                        int formId = imService.getLoginManager().getLoginId();
                        int toId = userEntity.getPeerId();

                        imService.getUserActionManager().UserP2PCommand(formId, toId,
                                SessionType.SESSION_TYPE_SINGLE,
                                CommandType.COMMAND_TYPE_TAKE_PHOTO, "", true);
                    }
                }


            }
            break;

            case R.id.take_recordings_btn: { // 录音

                if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
                    GroupEntity groupEntity = (GroupEntity) peerEntity;
                    if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
                        UserEntity user = getUserEntityFromGroupEntity(groupEntity);
                        if (user != null) {
                            if (TimeStart == 0) {
                                time2.start();

                                int formId = groupEntity.getPeerId();
                                int toId = user.getPeerId();
                                imService.getUserActionManager().UserP2PCommand(formId, toId,
                                        SessionType.SESSION_TYPE_GROUP,
                                        CommandType.COMMAND_TYPE_SOUND_COPY, "", true);
                            } else {
                                Utils.showToast(MessageActivity.this, "请勿频繁操作");
                            }
                        }
                    }
                }
                if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
                    UserEntity userEntity = (UserEntity) peerEntity;
                    if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                        if (TimeStart == 0) {
                            time2.start();

                            int formId = imService.getLoginManager().getLoginId();
                            int toId = userEntity.getPeerId();
                            imService.getUserActionManager().UserP2PCommand(formId, toId,
                                    SessionType.SESSION_TYPE_SINGLE,
                                    CommandType.COMMAND_TYPE_SOUND_COPY, "", true);
                        } else {
                            Utils.showToast(MessageActivity.this, "请勿频繁操作");
                        }
                    }

                }

            }
            break;
            //add by gzc
            case R.id.check_position_btn:   //同步数据

                //同步数据
                peerEntity = imService.getSessionManager().findPeerEntity(
                        currentSessionKey);
                //先判断是不是群聊 ，UserEntity GorupEntity

                if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {

                    GroupEntity groupEntity = (GroupEntity) peerEntity;
                    if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
                        UserEntity user = getUserEntityFromGroupEntity(groupEntity);
                        if (user != null) {
                            if (TimeStart == 0) {
                                time2.start();
                                String posContent = "查询" + peerEntity.getMainName() + "位置";
                                // sendTextToServer(posContent);
                                IMUserActionManager.instance().UserP2PCommand(
                                        groupEntity.getPeerId(),
                                        user.getPeerId(),
                                        SessionType.SESSION_TYPE_GROUP, // SESSION_TYPE_SINGLE
                                        CommandType.COMMAND_TYPE_DEVICE_CURRENT_INFO, "", false);

                            } else {
                                Utils.showToast(MessageActivity.this, "请勿频繁操作");
                            }
                        }
                    }

                } else if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
                    UserEntity userEntity = (UserEntity) peerEntity;
                    if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                        if (TimeStart == 0) {
                            time2.start();
                            String posContent = "查询" + peerEntity.getMainName() + "位置";
                            // sendTextToServer(posContent);
                            IMUserActionManager.instance().UserP2PCommand(
                                    IMLoginManager.instance().getLoginId(),
                                    userEntity.getPeerId(),
                                    SessionType.SESSION_TYPE_SINGLE, // SESSION_TYPE_SINGLE
                                    CommandType.COMMAND_TYPE_DEVICE_CURRENT_INFO, "", false);

                        } else {
                            Utils.showToast(MessageActivity.this, "请勿频繁操作");
                        }
                    }
                }


                break;

            case R.id.phone_callback_btn:   //回拨

                peerEntity = imService.getSessionManager().findPeerEntity(currentSessionKey);
                if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
                    GroupEntity groupEntity = (GroupEntity) peerEntity;
                    UserEntity userEntity = getUserEntityFromGroupEntity(groupEntity);
                    if (userEntity != null) {
                        if (userEntity.getPhone().equals("")) {
                            Utils.showToast(MessageActivity.this, "设备未设置号码");
                        } else {
                            if (TimeStart == 0) {
                                time2.start();
                                String content = peerEntity.getMainName() + "电话回拨";
                                // sendTextToServer(content);
                                //回拨
                                IMUserActionManager.instance().UserP2PCommand(
                                        IMLoginManager.instance().getLoginId(),
                                        userEntity.getPeerId(),
                                        SessionType.SESSION_DEVICE_SINGLE, // SESSION_TYPE_SINGLE
                                        CommandType.COMMAND_TYPE_DEVICE_CALLBACK, loginUser.getPhone() + "", false);
                            } else {
                                Utils.showToast(MessageActivity.this, "请勿频繁操作");
                            }
                        }
                    }
                } else if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
                    UserEntity userEntity = (UserEntity) peerEntity;
                    if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                        if (userEntity.getPhone().equals("")) {
                            Utils.showToast(MessageActivity.this, "设备未设置号码");

                        } else {
                            if (TimeStart == 0) {
                                time2.start();
                                String content = peerEntity.getMainName() + "电话回拨";
                                // sendTextToServer(content);
                                //回拨
                                IMUserActionManager.instance().UserP2PCommand(
                                        IMLoginManager.instance().getLoginId(),
                                        userEntity.getPeerId(),
                                        SessionType.SESSION_TYPE_SINGLE, // SESSION_TYPE_SINGLE
                                        CommandType.COMMAND_TYPE_DEVICE_CALLBACK, loginUser.getPhone() + "", false);
                            } else {
                                Utils.showToast(MessageActivity.this, "请勿频繁操作");
                            }
                        }
                    }

                }
                break;
            case R.id.check_telephone_charge:
                if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
                    GroupEntity groupEntity = (GroupEntity) peerEntity;
                    final UserEntity userEntity = getUserEntityFromGroupEntity(groupEntity);
                    if (userEntity != null) {
                        if (TimeStart == 0) {
                            time2.start();
                            String bellContent = "查询" + peerEntity.getMainName() + "话费";
                            // sendTextToServer(bellContent);
                            imService.getUserActionManager().UserP2PCommand(
                                    groupEntity.getPeerId(),
                                    userEntity.getPeerId(),
                                    SessionType.SESSION_TYPE_GROUP, // SESSION_TYPE_SINGLE
                                    CommandType.COMMAND_TYPE_DEVICE_BILL,
                                    "101", true);
                        } else {
                            Utils.showToast(MessageActivity.this, "请勿频繁操作");
                        }

                    }
                } else if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
                    UserEntity userEntity = (UserEntity) peerEntity;
                    if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {

                        if (TimeStart == 0) {
                            time2.start();
                            String bellContent = "查询" + peerEntity.getMainName() + "话费";
                            // sendTextToServer(bellContent);
                            imService.getUserActionManager().UserP2PCommand(
                                    imService.getLoginManager().getLoginId(),
                                    userEntity.getPeerId(),
                                    SessionType.SESSION_TYPE_SINGLE, // SESSION_TYPE_SINGLE
                                    CommandType.COMMAND_TYPE_DEVICE_BILL,
                                    "101", true);
                        } else {
                            Utils.showToast(MessageActivity.this, "请勿频繁操作");
                        }
                    }
                }
                break;

            case R.id.show_emo_btn: {
                /** yingmu 调整成键盘输出 */
                recordAudioBtn.setVisibility(View.GONE);
                keyboardInputImg.setVisibility(View.GONE);
                messageEdt.setVisibility(View.VISIBLE);
                audioInputImg.setVisibility(View.VISIBLE);
                addEmoBtn.setVisibility(View.VISIBLE);
                /** end */
                if (keyboardHeight != 0) {
                    this.getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                }
                if (emoLayout.getVisibility() == View.VISIBLE) {
                    if (!messageEdt.hasFocus()) {
                        messageEdt.requestFocus();
                    }
                    isHideEmoLayout = true;
                    inputManager.toggleSoftInputFromWindow(
                            messageEdt.getWindowToken(), 1, 0);
                } else if (emoLayout.getVisibility() == View.GONE) {
                    inputManager.hideSoftInputFromWindow(
                            messageEdt.getWindowToken(), 0);
                    isHideEmoLayout = false;
                    emoLayout.setVisibility(View.VISIBLE);

                }

                if (addOthersPanelView.getVisibility() == View.VISIBLE) {
                    addOthersPanelView.setVisibility(View.GONE);
                }
//                if (mLlDevicePanel.getVisibility() == View.VISIBLE) {
//                    mLlDevicePanel.setVisibility(View.GONE);
//                }
                if (isInput) {
                    addEmoBtn.setBackgroundResource(R.drawable.icon_expression);
                } else {
                    addEmoBtn.setBackgroundResource(R.drawable.icon_keyboard);
                }

                isInput = !isInput;
            }
            break;
            case R.id.send_message_btn: {
                logger.d("message_activity#send btn clicked");

                String content = messageEdt.getText().toString();
                logger.d("message_activity#chat content:%s", content);
                if (content.trim().equals("")) {
                    Utils.showToast(MessageActivity.this,
                            getResources().getString(R.string.message_null));
                    return;
                }

                TextMessage textMessage = TextMessage.buildForSend(content,
                        loginUser, peerEntity);
                imService.getMessageManager().sendText(textMessage);
                messageEdt.setText("");
                pushList(textMessage);
                scrollToBottomListItem();

                //	isInput = false;
                //	addEmoBtn.setBackgroundResource(R.drawable.icon_expression);
            }
            break;
            case R.id.voice_btn: {
                inputManager
                        .hideSoftInputFromWindow(messageEdt.getWindowToken(), 0);
                messageEdt.setVisibility(View.GONE);
                audioInputImg.setVisibility(View.GONE);
                recordAudioBtn.setVisibility(View.VISIBLE);
                keyboardInputImg.setVisibility(View.VISIBLE);
                emoLayout.setVisibility(View.GONE);
                addOthersPanelView.setVisibility(View.GONE);
                messageEdt.setText("");

                isInput = false;
                addEmoBtn.setBackgroundResource(R.drawable.icon_expression);
            }
            break;
            case R.id.message_text: {

                isInput = false;
                addEmoBtn.setBackgroundResource(R.drawable.icon_expression);
                if (messageEdt.hasFocus()) {
                    //显示panel
                    showDevicePanel();
                }
            }
            break;
            case R.id.show_keyboard_btn: {
                recordAudioBtn.setVisibility(View.GONE);
                keyboardInputImg.setVisibility(View.GONE);
                messageEdt.setVisibility(View.VISIBLE);
                audioInputImg.setVisibility(View.VISIBLE);
                addEmoBtn.setVisibility(View.VISIBLE);

                isInput = false;
                addEmoBtn.setBackgroundResource(R.drawable.icon_expression);
            }
            break;
            case R.id.tt_new_msg_tip: {
                scrollToBottomListItem();
                textView_new_msg_tip.setVisibility(View.GONE);
            }
            break;
        }
    }

    private void sendTextToServer(String content) {
//        String posContent = "查询"+peerEntity.getMainName()+"位置";

        //暂时屏蔽

//        TextMessage posTextMessage = TextMessage.buildForSend(content,
//                loginUser, peerEntity);
//        imService.getMessageManager().sendText(posTextMessage);
//        messageEdt.setText("");
//        pushList(posTextMessage);
//        scrollToBottomListItem();
    }

    private void hideDeviceView() {
        if (isHideDviceView) {
            if (addOthersPanelView.getVisibility() != View.GONE) {
                addOthersPanelView.setVisibility(View.GONE);
            }
        }

        if (isHideEmoLayout) {
            emoLayout.setVisibility(View.GONE);
        }

    }

    private void showDevicePanel() {
        if (isHideDviceView) {
            addOthersPanelView.setVisibility(View.GONE);
        } else {
            if (addOthersPanelView.getVisibility() != View.VISIBLE) {
                addOthersPanelView.setVisibility(View.VISIBLE);
            }
        }
    }

    private UserEntity getUserEntityFromGroupEntity(GroupEntity groupEntity) {

        UserEntity user = null;
        if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
            Set<Integer> groupMemberIds = groupEntity.getlistGroupMemberIds();
            Iterator<Integer> iterator = groupMemberIds.iterator();

            while (iterator.hasNext()) {
                int userId = iterator.next();
                user = imService.getContactManager().findDeviceContact(userId);
                if (user != null) {
                    break;
                }
            }
        }
        return user;
    }

    private void lightoff() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.3f;
        getWindow().setAttributes(lp);
    }

    // 主要是录制语音的
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int id = v.getId();
        scrollToBottomListItem();
        if (id == R.id.record_voice_btn) {
            if (PermissionUtil.lackPermission(Manifest.permission.RECORD_AUDIO)) {
                requestRunPermisssion(Manifest.permission.RECORD_AUDIO, new PermissionListener() {
                    @Override
                    protected void onGranted() {

                    }

                    @Override
                    protected void onDenied(List<String> deniedPermission) {
                        Toast.makeText(MessageActivity.this, PermissionUtil.getPermissionString(MessageActivity.this, deniedPermission), Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                if (AudioPlayerHandler.getInstance().isPlaying())
                    AudioPlayerHandler.getInstance().stopPlayer();
                y1 = event.getY();
                recordAudioBtn.setBackgroundResource(R.drawable.tt_pannel_btn_voiceforward_pressed);
                recordAudioBtn.setText(MessageActivity.this.getResources()
                        .getString(R.string.release_to_send_voice));

                soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_01);
                soundVolumeImg.setVisibility(View.VISIBLE);
                soundVolumeLayout.setBackgroundResource(0);// 去掉背景
                soundVolumeDialog.show();
                audioSavePath = CommonUtil
                        .getAudioSavePath(IMLoginManager.instance().getLoginId());

                // 这个callback很蛋疼，发送消息从MotionEvent.ACTION_UP 判断
                audioRecorderInstance = new AudioRecordHandler(audioSavePath);

                audioRecorderThread = new Thread(audioRecorderInstance);
                audioRecorderInstance.setRecording(true);
                logger.d("message_activity#audio#audio record thread starts");
                audioRecorderThread.start();
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                y2 = event.getY();
                if (y1 - y2 > 180) {
                    soundVolumeImg.setVisibility(View.GONE);
                    soundVolumeLayout.setBackgroundResource(R.drawable.tt_sound_volume_cancel_bk);
                } else {
                    soundVolumeImg.setVisibility(View.VISIBLE);
                    soundVolumeLayout.setBackgroundResource(0);// 去掉背景setBackgroundResource
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                y2 = event.getY();
                if (audioRecorderInstance.isRecording()) {
                    audioRecorderInstance.setRecording(false);
                }
                if (soundVolumeDialog.isShowing()) {
                    soundVolumeDialog.dismiss();
                }
                recordAudioBtn.setBackgroundResource(R.drawable.tt_pannel_btn_voiceforward_normal);
                recordAudioBtn.setText(MessageActivity.this.getResources().getString(
                        R.string.tip_for_voice_forward));
                if (y1 - y2 <= 180) {
                    if (audioRecorderInstance.getRecordTime() >= 0.5) {
                        if (audioRecorderInstance.getRecordTime() < SysConstant.MAX_SOUND_RECORD_TIME) {
                            Message msg = uiHandler.obtainMessage();
                            msg.what = HandlerConstant.HANDLER_RECORD_FINISHED;
                            msg.obj = audioRecorderInstance.getRecordTime();
                            uiHandler.sendMessage(msg);
                        }
                    } else {
                        soundVolumeImg.setVisibility(View.GONE);
                        soundVolumeLayout
                                .setBackgroundResource(R.drawable.tt_sound_volume_short_tip_bk);
                        soundVolumeDialog.show();
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            public void run() {
                                if (soundVolumeDialog.isShowing())
                                    soundVolumeDialog.dismiss();
                                this.cancel();
                            }
                        }, 700);
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onStop() {

        logger.d("message_activity#onStop:%s", this);
        if (null != adapter) {
            adapter.hidePopup();
        }

        AudioPlayerHandler.getInstance().clear();
        super.onStop();
    }

    @Override
    protected void onStart() {
        logger.d("message_activity#onStart:%s", this);
        super.onStart();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            sendBtn.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams param = (LayoutParams) messageEdt
                    .getLayoutParams();
            param.addRule(RelativeLayout.LEFT_OF, R.id.show_emo_btn);
            addPhotoBtn.setVisibility(View.GONE);
        } else {
            addPhotoBtn.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams param = (LayoutParams) messageEdt
                    .getLayoutParams();
            param.addRule(RelativeLayout.LEFT_OF, R.id.show_emo_btn);
            sendBtn.setVisibility(View.GONE);
        }
    }

    /**
     * @Description 滑动到列表底部
     */
    private void scrollToBottomListItem() {
        logger.d("message_activity#scrollToBottomListItem");

        // todo eric, why use the last one index + 2 can real scroll to the
        // bottom?
        ListView lv = lvPTR.getRefreshableView();
        if (lv != null) {
            lv.setSelection(adapter.getCount() + 1);
        }
        textView_new_msg_tip.setVisibility(View.GONE);
    }

    /**
     * @Description 滑动到列表指定行
     */
    private void scrollToBottomListItem(int index) {
        logger.d("message_activity#scrollToBottomListItem");

        // todo eric, why use the last one index + 2 can real scroll to the
        // bottom?
        ListView lv = lvPTR.getRefreshableView();
        if (lv != null) {
            lv.setSelection(index + 1);
        }
        textView_new_msg_tip.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        logger.d("message_activity#onPause:%s", this);
        super.onPause();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent arg0) {
        try {
            if (!AudioPlayerHandler.getInstance().isPlaying()) {
                return;
            }
            float range = arg0.values[0];
            if (null != sensor && range == sensor.getMaximumRange()) {
                // 屏幕恢复亮度
                //  AudioPlayerHandler.getInstance().setScreenAudioMode(this);
            } else {
                // 屏幕变黑
                //  AudioPlayerHandler.getInstance().setAudioMode(this,false);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static Handler getUiHandler() {
        return uiHandler;
    }

    private void actFinish() {

        inputManager.hideSoftInputFromWindow(messageEdt.getWindowToken(), 0);
        ActivityManager.getInstance().finishAllActivityExcept(this);
        IMApplication.gifRunning = false;

        //跳到MainActivity
        Intent intent = new Intent(MessageActivity.this, MainActivity.class);
        startActivity(intent);
        MessageActivity.this.finish();

        isInput = false;
        addEmoBtn.setBackgroundResource(R.drawable.icon_expression);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            inputManager.hideSoftInputFromWindow(messageEdt.getWindowToken(), 0);
            ActivityManager.getInstance().finishAllActivityExcept(this);
            IMApplication.gifRunning = false;

            //跳到MainActivity
            Intent intent = new Intent(MessageActivity.this, MainActivity.class);
            startActivity(intent);
            MessageActivity.this.finish();

            isInput = false;
            addEmoBtn.setBackgroundResource(R.drawable.icon_expression);

            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private YayaEmoGridView.OnEmoGridViewItemClick yayaOnEmoGridViewItemClick = new YayaEmoGridView.OnEmoGridViewItemClick() {
        @Override
        public void onItemClick(int facesPos, int viewIndex) {
            int resId = Emoparser.getInstance(MessageActivity.this)
                    .getYayaResIdList()[facesPos];
            logger.d("message_activity#yayaEmoGridView be clicked");

            String content = Emoparser.getInstance(MessageActivity.this)
                    .getYayaIdPhraseMap().get(resId);
            if (content.equals("")) {
                Utils.showToast(MessageActivity.this,
                        getResources().getString(R.string.message_null));
                return;
            }

            TextMessage textMessage = TextMessage.buildForSend(content,
                    loginUser, peerEntity);
            imService.getMessageManager().sendText(textMessage);
            pushList(textMessage);
            scrollToBottomListItem();
        }
    };


    private OnEmoGridViewItemClick onEmoGridViewItemClick = new OnEmoGridViewItemClick() {
        @Override
        public void onItemClick(int facesPos, int viewIndex) {

            int deleteId = (++viewIndex) * (SysConstant.pageSize - 1);
            if (deleteId > Emoparser.getInstance(MessageActivity.this).getResIdList().length) {

                deleteId = Emoparser.getInstance(MessageActivity.this).getResIdList().length;
            }

            if (deleteId == facesPos) {
                String msgContent = messageEdt.getText().toString();
                if (msgContent.isEmpty())
                    return;
                if (msgContent.contains("["))
                    msgContent = msgContent.substring(0,
                            msgContent.lastIndexOf("["));
                messageEdt.setText(msgContent);
            } else {
                int resId = Emoparser.getInstance(MessageActivity.this)
                        .getResIdList()[facesPos];
                String pharse = Emoparser.getInstance(MessageActivity.this)
                        .getIdPhraseMap().get(resId);
                int startIndex = messageEdt.getSelectionStart();
                Editable edit = messageEdt.getEditableText();
                if (startIndex < 0 || startIndex >= edit.length()) {
                    if (null != pharse) {
                        edit.append(pharse);
                    }
                } else {
                    if (null != pharse) {
                        edit.insert(startIndex, pharse);
                    }
                }
            }

            Editable edtable = messageEdt.getText();
            int position = edtable.length();
            Selection.setSelection(edtable, position);
        }
    };


    private OnTouchListener lvPTROnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                messageEdt.clearFocus();
                if (emoLayout.getVisibility() == View.VISIBLE) {
                    emoLayout.setVisibility(View.GONE);
                }

                if (addOthersPanelView.getVisibility() == View.VISIBLE) {
                    addOthersPanelView.setVisibility(View.GONE);
                }

                inputManager.hideSoftInputFromWindow(
                        messageEdt.getWindowToken(), 0);

                isInput = false;
                addEmoBtn.setBackgroundResource(R.drawable.icon_expression);
            }
            return false;
        }
    };

    private View.OnFocusChangeListener msgEditOnFocusChangeListener = new android.view.View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                if (keyboardHeight == 0) {
//                    mLlDevicePanel.setVisibility(View.GONE);
                    addOthersPanelView.setVisibility(View.GONE);
                    emoLayout.setVisibility(View.GONE);

                    isInput = false;
                    addEmoBtn.setBackgroundResource(R.drawable.icon_expression);

                } else {

                    MessageActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                    if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
                        GroupEntity user = (GroupEntity) peerEntity;
                        addOthersPanelView.setVisibility(View.VISIBLE);
                    } else {
                        addOthersPanelView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };


    private class switchInputMethodReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    "android.intent.action.INPUT_METHOD_CHANGED")) {
                currentInputMethod = Settings.Secure.getString(
                        MessageActivity.this.getContentResolver(),
                        Settings.Secure.DEFAULT_INPUT_METHOD);
                SystemConfigSp.instance().setStrConfig(
                        SystemConfigSp.SysCfgDimension.DEFAULTINPUTMETHOD,
                        currentInputMethod);
                int height = SystemConfigSp.instance().getIntConfig(
                        currentInputMethod);
                Log.i("aaa", "onReceive: " + height);
                if (keyboardHeight != height) {
                    keyboardHeight = height;
                    addOthersPanelView.setVisibility(View.GONE);
                    emoLayout.setVisibility(View.GONE);
                    MessageActivity.this
                            .getWindow()
                            .setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    messageEdt.requestFocus();
                    if (keyboardHeight != 0
                            && addOthersPanelView.getLayoutParams().height != keyboardHeight) {
                        LayoutParams params = (LayoutParams) addOthersPanelView
                                .getLayoutParams();
                        params.height = keyboardHeight;
                    }
                    if (keyboardHeight != 0
                            && emoLayout.getLayoutParams().height != keyboardHeight) {
                        LayoutParams params = (LayoutParams) emoLayout
                                .getLayoutParams();
                        params.height = keyboardHeight;
                    }
                } else {
                    addOthersPanelView.setVisibility(View.VISIBLE);
                    LayoutParams params = (LayoutParams) emoLayout
                            .getLayoutParams();
                    params.height = keyboardHeight;
                    emoLayout.setVisibility(View.VISIBLE);
                    MessageActivity.this
                            .getWindow()
                            .setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                    messageEdt.requestFocus();
                }
            }
        }
    }

    class ZhuaiTimeCount extends CountDownTimer {

        public ZhuaiTimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            TimeStart = (int) millisUntilFinished;
        }

        @Override
        public void onFinish() {
            TimeStart = 0;
        }
    }
}

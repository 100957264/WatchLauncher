package com.fise.xiaoyu.imservice.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fise.xiaoyu.DB.DBInterface;
import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.StepData;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.ConfigurationSp;
import com.fise.xiaoyu.DB.sp.LoginSp;
import com.fise.xiaoyu.DB.sp.RegistSp;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.device.entity.MobilePhoneDeviceEntity;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.PriorityEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMDeviceManager;
import com.fise.xiaoyu.imservice.manager.IMGroupManager;
import com.fise.xiaoyu.imservice.manager.IMHeartBeatManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.manager.IMMessageManager;
import com.fise.xiaoyu.imservice.manager.IMNotificationManager;
import com.fise.xiaoyu.imservice.manager.IMPositionManager;
import com.fise.xiaoyu.imservice.manager.IMReconnectManager;
import com.fise.xiaoyu.imservice.manager.IMSessionManager;
import com.fise.xiaoyu.imservice.manager.IMSocketManager;
import com.fise.xiaoyu.imservice.manager.IMUnreadMsgManager;
import com.fise.xiaoyu.imservice.manager.IMUserActionManager;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.step.UpdateUiCallBack;
import com.fise.xiaoyu.step.accelerometer.StepCount;
import com.fise.xiaoyu.step.accelerometer.StepValuePassListener;
import com.fise.xiaoyu.ui.activity.MainActivity;
import com.fise.xiaoyu.utils.ImageLoaderUtil;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * IMService 负责所有IMManager的初始化与reset
 * 并且Manager的状态的改变 也会影响到IMService的操作
 * 备注: 有些服务应该在LOGIN_OK 之后进行
 * todo IMManager reflect or just like  ctx.getSystemService()
 */
public class IMService extends Service implements SensorEventListener {
    private final int GRAY_SERVICE_ID = 1001;
    private Logger logger = Logger.getLogger(IMService.class);


    /**
     * 默认为30秒进行一次存储
     */
    private static int duration = 30 * 1000;
    /**
     * 当前的日期
     */
    private static String CURRENT_DATE = "";
    /**
     * 传感器管理对象
     */
    private SensorManager sensorManager;
    /**
     * 广播接受者
     */
    private BroadcastReceiver mBatInfoReceiver;
    /**
     * 保存记步计时器
     */
    private TimeCount time;
    /**
     * 当前所走的步数
     */
    public int CURRENT_STEP;
    /**
     * 计步传感器类型  Sensor.TYPE_STEP_COUNTER或者Sensor.TYPE_STEP_DETECTOR
     */
    private static int stepSensorType = -1;
    /**
     * 每次第一次启动记步服务时是否从系统中获取了已有的步数记录
     */
    private boolean hasRecord = false;
    /**
     * 系统中获取到的已有的步数
     */
    private int hasStepCount = 0;
    /**
     * 上一次的步数
     */
    private int previousStepCount = 0;
    /**
     * 通知管理对象
     */
    private NotificationManager mNotificationManager;
    /**
     * 加速度传感器中获取的步数
     */
    private StepCount mStepCount;
    /**
     * 通知构建者
     */
    private NotificationCompat.Builder mBuilder;

    /**
     * 记步Notification的ID
     */
    int notifyId_Step = 100;

    /**
     * binder
     */
    private IMServiceBinder binder = new IMServiceBinder();

    public class IMServiceBinder extends Binder {
        public IMService getService() {
            return IMService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onBind");
        logger.i("IMService onBind");
        return binder;
    }

    //所有的管理类
    private IMSocketManager socketMgr = IMSocketManager.instance();
    private IMLoginManager loginMgr = IMLoginManager.instance();
    private IMContactManager contactMgr = IMContactManager.instance();
    private IMGroupManager groupMgr = IMGroupManager.instance();
    private IMMessageManager messageMgr = IMMessageManager.instance();
    private IMSessionManager sessionMgr = IMSessionManager.instance();
    private IMReconnectManager reconnectMgr = IMReconnectManager.instance();
    private IMUnreadMsgManager unReadMsgMgr = IMUnreadMsgManager.instance();
    private IMNotificationManager notificationMgr = IMNotificationManager.instance();
    private IMHeartBeatManager heartBeatManager = IMHeartBeatManager.instance();

    private IMPositionManager postitionManager = IMPositionManager.instance();
    // private IMReqPositionManager  ReqPostitionManager = IMReqPositionManager.instance();  //guanweile
    private IMUserActionManager userActionManager = IMUserActionManager.instance();
    private IMDeviceManager userDeviceManager = IMDeviceManager.instance();


    private ConfigurationSp configSp;
    private LoginSp loginSp = LoginSp.instance();
    private DBInterface dbInterface = DBInterface.instance();
    private RegistSp registSp = RegistSp.instance();


    @Override
    public void onCreate() {
        Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onCreate");
        logger.i("IMService onCreate");
        super.onCreate();
        EventBus.getDefault().register(this);
        Intent innerIntent = new Intent(this, EmptyInnerService.class);
        startService(innerIntent);
        // todo eric study wechat's mechanism, use a better solution
        startForeground(GRAY_SERVICE_ID, new Notification());
        //  startForeground((int) System.currentTimeMillis(), new Notification());

        initNotification();
        initTodayData();
        initBroadcastReceiver();
        new Thread(new Runnable() {
            public void run() {
                startStepDetector();
            }
        }).start();
        startTimeCount();

        Log.e("onCreate", "onCreate");
    }

    @Override
    public void onDestroy() {
        Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onDestroy");
        logger.i("IMService onDestroy");
        // todo 在onCreate中使用startForeground
        // 在这个地方是否执行 stopForeground呐
        EventBus.getDefault().unregister(this);
        handleLoginout();
        // DB的资源的释放
        dbInterface.close();

        IMNotificationManager.instance().cancelAllNotifications();
        unregisterReceiver(mBatInfoReceiver);
        super.onDestroy();
    }

    /**
     * 收到消息需要上层的activity判断 {MessageActicity onEvent(PriorityEvent event)}，这个地方是特殊分支
     */
    @Subscribe(priority = SysConstant.SERVICE_EVENTBUS_PRIORITY)
    public void onMessageEvent(PriorityEvent event) {
        switch (event.event) {
            case MSG_RECEIVED_MESSAGE: {
                MessageEntity entity = (MessageEntity) event.object;
                /**非当前的会话*/
                logger.d("messageactivity#not this session msg -> id:%s", entity.getFromId());
                messageMgr.ackReceiveMsg(entity);
                unReadMsgMgr.add(entity);
            }
            break;

            case REQ_RECEIVED_MESSAGE: {
                MessageEntity entity = (MessageEntity) event.object;

                int toid = entity.getToId();
                int fromId = entity.getFromId();
                /**非当前的会话*/
                logger.d("messageactivity#not this session msg -> id:%s", entity.getFromId());
                messageMgr.ackReceiveMsg(entity);
            }
            break;

            case MSG_VEDIO_MESSAGE: {
                MessageEntity entity = (MessageEntity) event.object;

                int toid = entity.getToId();
                int fromId = entity.getFromId();
                /**非当前的会话*/
                logger.d("messageactivity#not this session msg -> id:%s", entity.getFromId());
                messageMgr.ackReceiveMsg(entity);
            }
            break;

            case MSG_DEV_MESSAGE: {

                MessageEntity entity = (MessageEntity) event.object;
                unReadMsgMgr.add(entity);
            }
            break;
        }
    }

    // EventBus 事件驱动
    @Subscribe(priority = SysConstant.SERVICE_EVENTBUS_PRIORITY)
    public void onMessageEvent(LoginEvent event) {
        switch (event) {
            case LOGIN_OK:
                onNormalLoginOk();
                break;
            case REGIST_OK:
                onNormalLoginOk();
                break;
            case LOCAL_LOGIN_SUCCESS:
                onLocalLoginOk();
                break;
            case LOCAL_LOGIN_MSG_SERVICE:
                Log.d("LOCAL_LOGIN_MSG_SERVICE", "LOCAL_LOGIN_MSG_SERVICE");
                onLocalNetOk();
                break;
            case LOGIN_OUT:
                handleLoginout();
                break;
        }
    }

    // 负责初始化 每个manager
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.dd(Logger.LOG_APPLICATION, getClass().getSimpleName() + "	onStartCommand");
        logger.i("IMService onStartCommand");
        //应用开启初始化 下面这几个怎么释放 todo
        Context ctx = getApplicationContext();
        loginSp.init(ctx);
        registSp.init(ctx);
        // 放在这里还有些问题 todo
        socketMgr.onStartIMManager(ctx);
        loginMgr.onStartIMManager(ctx);
        contactMgr.onStartIMManager(ctx);
        messageMgr.onStartIMManager(ctx);
        groupMgr.onStartIMManager(ctx);
        sessionMgr.onStartIMManager(ctx);
        unReadMsgMgr.onStartIMManager(ctx);
        notificationMgr.onStartIMManager(ctx);
        reconnectMgr.onStartIMManager(ctx);
        heartBeatManager.onStartIMManager(ctx);
        postitionManager.onStartIMManager(ctx);
        // ReqPostitionManager.onStartIMManager(ctx);   //guanweile
        userActionManager.onStartIMManager(ctx);
        userDeviceManager.onStartIMManager(ctx);

        ImageLoaderUtil.initImageLoaderConfig(getApplicationContext());
        return START_STICKY;
    }


    /**
     * 用户输入登陆流程
     * userName/pwd -> reqMessage ->connect -> loginMessage ->loginSuccess
     */
    private void onNormalLoginOk() {
        logger.d("imservice#onLogin Successful");
        //初始化其他manager todo 这个地方注意上下文的清除
        Context ctx = getApplicationContext();
        int loginId = loginMgr.getLoginId();
        configSp = ConfigurationSp.instance(ctx, loginId);
        dbInterface.initDbHelp(ctx, loginId);

        contactMgr.onNormalLoginOk();
        sessionMgr.onNormalLoginOk();
        groupMgr.onNormalLoginOk();
        unReadMsgMgr.onNormalLoginOk();

        reconnectMgr.onNormalLoginOk();
        //依赖的状态比较特殊
        messageMgr.onLoginSuccess();
        notificationMgr.onLoginSuccess();
        heartBeatManager.onloginNetSuccess();
        postitionManager.onStartPointSuccess();
        //  ReqPostitionManager.onStartPointSuccess();   //guanweile
        userActionManager.onNormalLoginOk();
        userDeviceManager.onNormalLoginOk();

        initData();
        // 这个时候loginManage中的localLogin 被置为true
    }


    /**
     * 自动登陆/离线登陆成功
     * autoLogin -> DB(loginInfo,loginId...) -> loginSucsess
     */
    private void onLocalLoginOk() {
        Context ctx = getApplicationContext();
        int loginId = loginMgr.getLoginId();
        configSp = ConfigurationSp.instance(ctx, loginId);
        dbInterface.initDbHelp(ctx, loginId);

        contactMgr.onLocalLoginOk();
        groupMgr.onLocalLoginOk();
        sessionMgr.onLocalLoginOk();
        reconnectMgr.onLocalLoginOk();
        notificationMgr.onLoginSuccess();
        messageMgr.onLoginSuccess();
        userActionManager.onLocalLoginOk();
        userDeviceManager.onLocalLoginOk();

        initData();
    }

    /**
     * 1.从本机加载成功之后，请求MessageService建立链接成功(loginMessageSuccess)
     * 2. 重练成功之后
     */
    private void onLocalNetOk() {

        /**为了防止逗比直接把loginId与userName的对应直接改了,重刷一遍*/
        Context ctx = getApplicationContext();
        int loginId = loginMgr.getLoginId();
        configSp = ConfigurationSp.instance(ctx, loginId);
        dbInterface.initDbHelp(ctx, loginId);

        contactMgr.onLocalNetOk();
        groupMgr.onLocalNetOk();
        sessionMgr.onLocalNetOk();
        unReadMsgMgr.onLocalNetOk();
        reconnectMgr.onLocalNetOk();
        heartBeatManager.onloginNetSuccess();
        postitionManager.onStartPointSuccess();
        //  ReqPostitionManager.onStartPointSuccess();   //guanweile
        userActionManager.onLocalNetOk();
        userDeviceManager.onLocalNetOk();

        initData();

    }

    private void handleLoginout() {
        logger.d("imservice#handleLoginout");

        // login需要监听socket的变化,在这个地方不能释放，设计上的不合理?
        socketMgr.reset();
        loginMgr.reset();
        contactMgr.reset();
        messageMgr.reset();
        groupMgr.reset();
        sessionMgr.reset();
        unReadMsgMgr.reset();
        notificationMgr.reset();
        reconnectMgr.reset();
        heartBeatManager.reset();
        postitionManager.reset();

        //  ReqPostitionManager.reset();    //guanweile
        userActionManager.reset();
        userDeviceManager.reset();
        configSp = null;
        EventBus.getDefault().removeAllStickyEvents();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        logger.d("imservice#onTaskRemoved");
        // super.onTaskRemoved(rootIntent);
        this.stopSelf();
    }

    public void updateUnreadReqList(UserEntity userInfo) {
        dbInterface.insertOrUpdateReqFriens(userInfo);
    }


    /**
     * -----------------get/set 的实体定义---------------------
     */
    public IMLoginManager getLoginManager() {
        return loginMgr;
    }

    public IMContactManager getContactManager() {
        return contactMgr;
    }

    public IMMessageManager getMessageManager() {
        return messageMgr;
    }


    public IMGroupManager getGroupManager() {
        return groupMgr;
    }

    public IMSessionManager getSessionManager() {
        return sessionMgr;
    }

    public IMReconnectManager getReconnectManager() {
        return reconnectMgr;
    }

    public IMUserActionManager getUserActionManager() {
        return userActionManager;
    }

    public IMDeviceManager getDeviceManager() {
        return userDeviceManager;
    }


    //guanweile
//    public IMReqPositionManager getReqPostionManager() {
//        return ReqPostitionManager;
//    }


    public IMUnreadMsgManager getUnReadMsgManager() {
        return unReadMsgMgr;
    }

    public IMNotificationManager getNotificationManager() {
        return notificationMgr;
    }

    public DBInterface getDbInterface() {
        return dbInterface;
    }

    public ConfigurationSp getConfigSp() {
        return configSp;
    }

    public LoginSp getLoginSp() {
        return loginSp;
    }

    public RegistSp getRegistSp() {
        return registSp;
    }


    /**
     * 获取当天日期
     *
     * @return
     */
    private String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * 初始化通知栏
     */
    private void initNotification() {
//        mBuilder = new NotificationCompat.Builder(this);
//        mBuilder.setContentTitle(getResources().getString(R.string.app_name))
//                .setContentText("今日步数" + CURRENT_STEP + " 步")
//               // .setContentIntent(getDefalutIntent(Notification.FLAG_ONGOING_EVENT))
//                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
//                .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
//                .setAutoCancel(false)//设置这个标志当用户单击面板就可以让通知将自动取消
//                .setOngoing(true)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
//                .setSmallIcon(R.drawable.icon);
//        Notification notification = mBuilder.build();
//        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        startForeground(notifyId_Step, notification);

    }

    /**
     * 初始化当天的步数
     */
    private void initTodayData() {
        CURRENT_DATE = getTodayDate();
        updateNotification();
    }

    public void initData() {

        StepData data = IMDeviceManager.instance().getStep(CURRENT_DATE);
        if (data == null) {
            CURRENT_STEP = 0;
            MainActivity.stepNum = CURRENT_STEP;
        } else {
            CURRENT_STEP = Integer.parseInt(data.getStep());
            MainActivity.stepNum = CURRENT_STEP;
        }

        if (mStepCount != null) {
            mStepCount.setSteps(CURRENT_STEP);
        }
    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter();
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        //关机广播
        filter.addAction(Intent.ACTION_SHUTDOWN);
        // 屏幕亮屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON);
        // 屏幕解锁广播
//        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //监听日期变化
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);

        mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    //  Log.d(TAG, "screen on");
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    //Log.d(TAG, "screen off");
                    //改为60秒一存储
                    duration = 60000;
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    // Log.d(TAG, "screen unlock");
//                    save();
                    //改为30秒一存储
                    duration = 30000;
                } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                    // Log.i(TAG, " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS");
                    //保存一次
                    save();
                } else if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                    //  Log.i(TAG, " receive ACTION_SHUTDOWN");
                    save();
                } else if (Intent.ACTION_DATE_CHANGED.equals(action)) {//日期变化步数重置为0
//                    Logger.d("重置步数" + StepDcretor.CURRENT_STEP);
                    save();
                    isNewDay();
                } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
                    //时间变化步数重置为0
                    save();
                    isNewDay();
                } else if (Intent.ACTION_TIME_TICK.equals(action)) {//日期变化步数重置为0
//                    Logger.d("重置步数" + StepDcretor.CURRENT_STEP);
                    save();
                    isNewDay();
                }
            }
        };
        registerReceiver(mBatInfoReceiver, filter);
    }


    /**
     * 监听晚上0点变化初始化数据
     */
    private void isNewDay() {
        String time = "00:00";
        if (time.equals(new SimpleDateFormat("HH:mm").format(new Date())) || !CURRENT_DATE.equals(getTodayDate())) {
            initTodayData();
            initData();
//            if (IMLoginManager.instance().getLoginInfo() != null
//                    && Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {
//
//            }

            if (IMLoginManager.instance().getLoginInfo() != null) {

                int timeNow1 = (int) ((System.currentTimeMillis() / 1000) - 86000); // 86400
                IMBaseDefine.ClientType client_type = IMBaseDefine.ClientType.CLIENT_TYPE_ANDROID;
                if (Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {
                    client_type = IMBaseDefine.ClientType.CLIENT_TYPE_MOBILE_DEVICE;
                }
                IMUserActionManager.instance().getRankingListRequest(client_type, IMDevice.StepRecordType.GET_STEP_COUNT_INFO, timeNow1);
            }
        }
    }


    /**
     * 开始保存记步数据
     */
    private void startTimeCount() {
        if (time == null) {
            time = new TimeCount(duration, 1000);
        }
        time.start();
    }

    /**
     * 更新步数通知
     */
    private void updateNotification() {
        //设置点击跳转
//        Intent hangIntent = new Intent(this, MainActivity.class);
//        PendingIntent hangPendingIntent = PendingIntent.getActivity(this, 0, hangIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//        Notification notification = mBuilder.setContentTitle(getResources().getString(R.string.app_name))
//                .setContentText("今日步数" + CURRENT_STEP + " 步")
//                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
//                .setContentIntent(hangPendingIntent)
//                .build();
//        mNotificationManager.notify(notifyId_Step, notification);
        if (mCallback != null) {
            mCallback.updateUi(CURRENT_STEP);
        }
    }

    /**
     * UI监听器对象
     */
    private UpdateUiCallBack mCallback;

    /**
     * 注册UI更新监听
     *
     * @param paramICallback
     */
    public void registerCallback(UpdateUiCallBack paramICallback) {
        this.mCallback = paramICallback;
    }


    /**
     * 获取当前步数
     *
     * @return
     */
    public int getStepCount() {
        return CURRENT_STEP;
    }

    /**
     * 获取传感器实例
     */
    private void startStepDetector() {
        if (sensorManager != null) {
            sensorManager = null;
        }
        // 获取传感器管理器的实例
        sensorManager = (SensorManager) this
                .getSystemService(SENSOR_SERVICE);
        //android4.4以后可以使用计步传感器
        int VERSION_CODES = Build.VERSION.SDK_INT;
        if (VERSION_CODES >= 19) {
            addCountStepListener();
        } else {
            addBasePedometerListener();
        }
    }

    /**
     * 添加传感器监听
     * 1. TYPE_STEP_COUNTER API的解释说返回从开机被激活后统计的步数，当重启手机后该数据归零，
     * 该传感器是一个硬件传感器所以它是低功耗的。
     * 为了能持续的计步，请不要反注册事件，就算手机处于休眠状态它依然会计步。
     * 当激活的时候依然会上报步数。该sensor适合在长时间的计步需求。
     * <p>
     * 2.TYPE_STEP_DETECTOR翻译过来就是走路检测，
     * API文档也确实是这样说的，该sensor只用来监监测走步，每次返回数字1.0。
     * 如果需要长事件的计步请使用TYPE_STEP_COUNTER。
     */
    private void addCountStepListener() {
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (countSensor != null) {
            stepSensorType = Sensor.TYPE_STEP_COUNTER;
            sensorManager.registerListener(IMService.this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (detectorSensor != null) {
            stepSensorType = Sensor.TYPE_STEP_DETECTOR;
            sensorManager.registerListener(IMService.this, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            addBasePedometerListener();
        }
    }

    /**
     * 传感器监听回调
     * 记步的关键代码
     * 1. TYPE_STEP_COUNTER API的解释说返回从开机被激活后统计的步数，当重启手机后该数据归零，
     * 该传感器是一个硬件传感器所以它是低功耗的。
     * 为了能持续的计步，请不要反注册事件，就算手机处于休眠状态它依然会计步。
     * 当激活的时候依然会上报步数。该sensor适合在长时间的计步需求。
     * <p>
     * 2.TYPE_STEP_DETECTOR翻译过来就是走路检测，
     * API文档也确实是这样说的，该sensor只用来监监测走步，每次返回数字1.0。
     * 如果需要长事件的计步请使用TYPE_STEP_COUNTER。
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (stepSensorType == Sensor.TYPE_STEP_COUNTER) {
            //获取当前传感器返回的临时步数
            int tempStep = (int) event.values[0];
            //首次如果没有获取手机系统中已有的步数则获取一次系统中APP还未开始记步的步数
            if (!hasRecord) {
                hasRecord = true;
                hasStepCount = tempStep;
            } else {
                //获取APP打开到现在的总步数=本次系统回调的总步数-APP打开之前已有的步数
                int thisStepCount = tempStep - hasStepCount;
                //本次有效步数=（APP打开后所记录的总步数-上一次APP打开后所记录的总步数）
                int thisStep = thisStepCount - previousStepCount;
                //总步数=现有的步数+本次有效步数
                CURRENT_STEP += (thisStep);
                MainActivity.stepNum = CURRENT_STEP;
                //记录最后一次APP打开到现在的总步数
                previousStepCount = thisStepCount;
            }
        } else if (stepSensorType == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0) {
                CURRENT_STEP++;
                MainActivity.stepNum = CURRENT_STEP;
            }
        }
        updateNotification();
    }

    /**
     * 通过加速度传感器来记步
     */
    private void addBasePedometerListener() {
        mStepCount = new StepCount();
        mStepCount.setSteps(CURRENT_STEP);
        // 获得传感器的类型，这里获得的类型是加速度传感器
        // 此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
        Sensor sensor = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        boolean isAvailable = sensorManager.registerListener(mStepCount.getStepDetector(), sensor,
                SensorManager.SENSOR_DELAY_UI);
        mStepCount.initListener(new StepValuePassListener() {
            @Override
            public void stepChanged(int steps) {
                CURRENT_STEP = steps;
                MainActivity.stepNum = CURRENT_STEP;
                updateNotification();
            }
        });
        if (isAvailable) {
            //   Log.v(TAG, "加速度传感器可以使用");
        } else {
            //  Log.v(TAG, "加速度传感器无法使用");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /**
     * 保存记步数据
     */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override

        public void onFinish() {
            // 如果计时器正常结束，则开始计步
            time.cancel();
            save();
            //如果是设备
            if (IMLoginManager.instance().getLoginInfo() != null
                    && Utils.isClientType(IMLoginManager.instance().getLoginInfo())) {

                DeviceEntity device = IMDeviceManager.instance().findDeviceCard(IMLoginManager.instance().getLoginId());
                if (device != null) {
                    MobilePhoneDeviceEntity mobilePhoneDeviceEntity = MobilePhoneDeviceEntity.parseFromDB(device);
                    if (mobilePhoneDeviceEntity != null && (mobilePhoneDeviceEntity.getStep_mode() == 1)) {  //如果设备打开了　开报警
                        startTimeCount();
                    }
                }
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

    }

    /**
     * 保存记步数据
     */
    private void save() {
        int tempStep = CURRENT_STEP;
        if (IMLoginManager.instance().getLoginInfo() != null) {
            StepData data = IMDeviceManager.instance().getStep(CURRENT_DATE);
            if (data == null) {
                data = new StepData();
                data.setToday(CURRENT_DATE);
                data.setStep(tempStep + "");
                IMDeviceManager.instance().updateStepData(data);
            } else {
                data.setStep(tempStep + "");
                data.setToday(CURRENT_DATE);
                IMDeviceManager.instance().updateStepData(data);
            }
        }
    }
}

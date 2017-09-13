package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceCrontab;
import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.manager.IMDeviceManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.ui.adapter.DeviceTaskAdapter;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/4/6.
 */

public class DeviceTaskListActivity extends TTBaseActivity implements DeviceTaskAdapter.RepeateStatusChangeLinstener {
    private IMService imService;
    private  int mDeviceId;
    private int  mLoginId;
    private IMDeviceManager mDeviceManager;
    private ArrayList<DeviceCrontab> mCrontabList = new ArrayList<>();
    private ListView mTaskList;
    private DeviceTaskAdapter mAdapter;
    private int mDataSize;
    private int mTaskType;
    private ArrayList<DeviceCrontab> mAllCrotabList ;
    private TextView mCenterText;
    private  RelativeLayout mNoDataHintView;
    private DeviceEntity device;
    private  UserEntity loginContact;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {

        @Override
        public void onServiceDisconnected() {

        }

        @Override
        public void onIMServiceConnected() {
            logger.d("login#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                // 后台服务启动链接失败
                return;
            }
            mDeviceId = getIntent().getIntExtra(IntentConstant.KEY_PEERID, 0);

            if (mDeviceId == 0) {
                logger.e("detail#intent params error!!");
                return;
            }
            device = imService.getDeviceManager().findDeviceCard(mDeviceId);
            mTaskType = getIntent().getIntExtra(IntentConstant.DEVICE_CRONTAB_TYPE ,-1);
            if(mTaskType == -1){
                logger.e("detail#intent params error!!");
                return;
            }
            if(mTaskType == IMDevice.TaskType.TASK_TYPE_LOVE_REMIND.ordinal()){
                mCenterText.setText(R.string.sweet_remind);
                warnText1.setText(getString(R.string.no_sweet_reminder_hint1));
                warnText2.setText(getString(R.string.no_sweet_reminder_hint2));
            } else{
                mCenterText.setText(R.string.attend_class);
                warnText2.setText(getString(R.string.no_attend_class_hint3));
//                warnText2.setText(getString(R.string.no_attend_class_hint2));
                imgNoData.setImageResource(R.drawable.lesson_no_data_image_hint);
                warnText1.setText(R.string.no_lesson_data);
                warnText2.setText(getString(R.string.no_lesson_data_hint2));
            }
            loginContact = IMLoginManager.instance().getLoginInfo();
            mLoginId = imService.getLoginManager().getLoginId();
            mDeviceManager = imService.getDeviceManager();
            mAllCrotabList = mDeviceManager.getAllCrotabList(mDeviceId);

            classifyAllCrontab();
            mDataSize = mCrontabList.size();
            if(mCrontabList.size() == 0 ){
              mNoDataHintView.setVisibility(View.VISIBLE);
//                return;
            }
            if(device.getMasterId() != loginContact.getPeerId()){
                mAdapter = new DeviceTaskAdapter(DeviceTaskListActivity.this ,false);
            }else{
                mAdapter = new DeviceTaskAdapter(DeviceTaskListActivity.this ,true);
            }

            mAdapter.setDataList(mCrontabList);
            mAdapter.setStatusChangeListener(DeviceTaskListActivity.this);

            mTaskList.setAdapter(mAdapter);
            mTaskList.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                    if(device != null && device.getMasterId() != loginContact.getPeerId()){
//
//                        Utils.showToast(DeviceTaskListActivity.this,DeviceTaskListActivity.this.getString(R.string.no_authority_to_operate));
//                        return;
//                    }

                    DeviceCrontab deviceTask =  mCrontabList.get(position);

                    if(mTaskType == IMDevice.TaskType.TASK_TYPE_LOVE_REMIND.ordinal()){
                        IMUIHelper.openSweetRemindDetailActivity(DeviceTaskListActivity.this,
                                mDeviceId ,deviceTask.getTaskId());
                    }else{
                        IMUIHelper.openLessonRemindDetailActivity(DeviceTaskListActivity.this,
                                mDeviceId ,deviceTask.getTaskId());
                    }

                }
            });

            mTaskList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final  int position, long id) {

                    if(device.getMasterId() != loginContact.getPeerId()){
                        return false;
                    }

                    final DeviceCrontab crontab = mCrontabList.get(position);
                    final FilletDialog myDialog = new FilletDialog(DeviceTaskListActivity.this ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITH_MESSAGE);
                    myDialog.setTitle(getString(R.string.device_prompt));
                    myDialog.setMessage("确定删除 \""+crontab.getTaskName()+" \"任务?");//设置内容
                    myDialog.dialog.show();//显示

                    //确认按键回调，按下确认后在此做处理
                    myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
                        @Override
                        public void ok() {

                            if(mTaskType == IMDevice.TaskType.TASK_TYPE_LOVE_REMIND.ordinal()){
                                mDeviceManager.setDevTask(mLoginId , mDeviceId ,IMBaseDefine.OperateType.OPERATE_TYPE_DELETE ,
                                        crontab.getTaskId(), IMDevice.TaskType.TASK_TYPE_LOVE_REMIND,crontab.getTaskName(),"",crontab.getBeginTime(),"",crontab.getStatus(),0,crontab.getRepeatValue());
                            }else{
                                mDeviceManager.setDevTask(mLoginId , mDeviceId ,IMBaseDefine.OperateType.OPERATE_TYPE_DELETE ,
                                        crontab.getTaskId(), IMDevice.TaskType.TASK_TYPE_LESSION_MODE, crontab.getTaskName(),"",crontab.getBeginTime(),crontab.getEndTime(),crontab.getStatus(),0,crontab.getRepeatValue());
                            }

                            myDialog.dialog.dismiss();
                        }
                    });

                    return true;
                }
            });
        }
    };
    private TextView warnText1;
    private TextView warnText2;
    private ImageView imgNoData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_sweet_remind_list);
        init();
        initView();
    }

    private void  classifyAllCrontab(){
        mCrontabList.clear();
        if(mTaskType == IMDevice.TaskType.TASK_TYPE_LOVE_REMIND.ordinal()){
            for (int i = 0;i< mAllCrotabList.size() ; i ++) {
                if (mAllCrotabList.get(i).getTaskType() == IMDevice.TaskType.TASK_TYPE_LOVE_REMIND.ordinal() && mAllCrotabList.get(i).getDeviceId() == mDeviceId) {
                    mCrontabList.add(mAllCrotabList.get(i));
                }
            }
        }else if(mTaskType == IMDevice.TaskType.TASK_TYPE_LESSION_MODE.ordinal() ){
            for (int i = 0;i< mAllCrotabList.size() ; i ++) {
                if (mAllCrotabList.get(i).getTaskType() == IMDevice.TaskType.TASK_TYPE_LESSION_MODE.ordinal() && mAllCrotabList.get(i).getDeviceId() == mDeviceId) {
                    mCrontabList.add(mAllCrotabList.get(i));
                }
            }
        }


        }

    @Override
    protected void onResume() {
        super.onResume();
    }
    private void init() {
        imServiceConnector.connect(this);
            }

    private void initView() {

        LinearLayout icon_arrow = (LinearLayout) findViewById(R.id.icon_arrow_layout);
        mCenterText = (TextView) findViewById(R.id.center_text);
        mTaskList = (ListView) findViewById(R.id.list_device_task);
        imgNoData = (ImageView) findViewById(R.id.no_data_hint_image);
        mNoDataHintView = (RelativeLayout) findViewById(R.id.no_data_hint_view);
        warnText1 = (TextView) findViewById(R.id.tv_no_data1);
        warnText2 = (TextView) findViewById(R.id.tv_no_data2);
        icon_arrow.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                DeviceTaskListActivity.this.finish();
            }
        });
        Button right_button = (Button) findViewById(R.id.right_button);
        right_button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if(device != null && device.getMasterId() != loginContact.getPeerId()){

                    Utils.showToast(DeviceTaskListActivity.this,DeviceTaskListActivity.this.getString(R.string.no_authority_to_operate));
                    return;
                }



                if(mTaskType == IMDevice.TaskType.TASK_TYPE_LOVE_REMIND.ordinal()){

                    if (mCrontabList.size() >= DBConstant.SESSION_SWEET_REMIND_NUM) {
                        Utils.showToast(DeviceTaskListActivity.this, "爱心提醒最多" + DBConstant.SESSION_SWEET_REMIND_NUM + "个" );
                        return;
                    }
                    IMUIHelper.openSweetRemindDetailActivity(DeviceTaskListActivity.this,
                            mDeviceId ,-1);
                }else if(mTaskType == IMDevice.TaskType.TASK_TYPE_LESSION_MODE.ordinal()){
                    if (mCrontabList.size() >= DBConstant.SESSION_SWEET_REMIND_NUM) {
                        Utils.showToast(DeviceTaskListActivity.this, "上课模式最多" + DBConstant.SESSION_SWEET_REMIND_NUM + "个" );
                        return;
                    }

                    IMUIHelper.openLessonRemindDetailActivity(DeviceTaskListActivity.this,
                            mDeviceId ,-1);
                }

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeviceEvent event) {
        switch (event) {
            case DEVICE_TASK_ADD_SUCCESS:
                if(mDeviceManager != null){
                    notifyDataChanged();
                }
            break;
            case DEVICE_TASK_DELETE_SUCCESS:
                if(mDeviceManager != null){
                    notifyDataChanged();
                }
                break;
            case DEVICE_TASK_UPDATE_SUCCESS:
                if(mDeviceManager != null){
                    notifyDataChanged();
                }
                break;
        }

        if(mCrontabList.size() == 0 ){
            if(mNoDataHintView.getVisibility() != View.VISIBLE)
                mNoDataHintView.setVisibility(View.VISIBLE);
        }else{
            if(mNoDataHintView.getVisibility() != View.GONE)
                mNoDataHintView.setVisibility(View.GONE);
        }
    }

    private  void notifyDataChanged(){
       if(mDeviceManager != null){
           mAllCrotabList = mDeviceManager.getAllCrotabList(mDeviceId);
           classifyAllCrontab();
           mDataSize = mCrontabList.size();
           mAdapter.setDataList(mCrontabList);
           mAdapter.notifyDataSetChanged();

       }
    }
    @Override
    public void rpeateStatuChange(int position, boolean changed) {

        DeviceCrontab deviceCrontab = mCrontabList.get(position);

        if(mTaskType == IMDevice.TaskType.TASK_TYPE_LOVE_REMIND.ordinal()){
            mDeviceManager.setDevTask(mLoginId , mDeviceId , IMBaseDefine.OperateType.OPERATE_TYPE_UPDATE ,
                    deviceCrontab.getTaskId(), IMDevice.TaskType.TASK_TYPE_LOVE_REMIND,deviceCrontab.getTaskName(),"",deviceCrontab.getBeginTime(),"",changed ? 1 : 2, 0,deviceCrontab.getRepeatValue());

        }else{
            mDeviceManager.setDevTask(mLoginId , mDeviceId , IMBaseDefine.OperateType.OPERATE_TYPE_UPDATE ,
                    deviceCrontab.getTaskId(), IMDevice.TaskType.TASK_TYPE_LESSION_MODE,deviceCrontab.getTaskName(),"",deviceCrontab.getBeginTime(),deviceCrontab.getEndTime(),changed ? 1 : 2, 0,deviceCrontab.getRepeatValue());

        }

    }
}

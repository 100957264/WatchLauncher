package com.fise.xiaoyu.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byl.datepicker.wheelview.OnWheelScrollListener;
import com.byl.datepicker.wheelview.WheelView;
import com.byl.datepicker.wheelview.adapter.NumericWheelAdapter;
import com.fise.xiaoyu.DB.entity.DeviceCrontab;
import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.manager.IMDeviceManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.ui.adapter.TaskRepeateVauleAdapter;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by lenovo on 2017/4/13.
 */


public class LessonRemindDetailActivity extends TTBaseActivity implements  View.OnClickListener{
    private  AlertDialog mSelRepeateDialog;
    private  AlertDialog mSelTimeDialog;
    private ListView mRepeateList;
    private Boolean[] checkedList;
    private TaskRepeateVauleAdapter repeateVauleAdapter;
    private EditText etTitle;
    private InputMethodManager inputManager;
    private String mRepeateVaule = "";
    private int mTaskId;
    private IMService imService;
    private  int mDeviceId;
    private int  mLoginId;
    private IMDeviceManager mDeviceManager;
    private DeviceCrontab mUpdateCrontab;
    private  WheelView mWheelHour;
    private WheelView mWheelMin;
    private TextView tvMorningStartTime;
    private TextView tvMorningEndTime;
    private TextView tvafternoonStartTime;
    private TextView tvAfternoonEndTime;

    private String mMorningStartTimeStr = "00:00";
    private String mMorningEndTimeStr = "00:00";
    private String mtvafternoonStartTimeStr = "00:00";
    private String mAfternoonEndTimeStr = "00:00";
    private DeviceEntity device;
    private UserEntity loginContact;
    //    private LinearLayout llSelTime;
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
            mDeviceId = LessonRemindDetailActivity.this.getIntent().getIntExtra(IntentConstant.KEY_PEERID, 0);
            mTaskId = LessonRemindDetailActivity.this.getIntent().getIntExtra(IntentConstant.DEVICE_CRONTAB_ID ,-2);
            mLoginId = imService.getLoginManager().getLoginId();
            if(mTaskId == -2){
                logger.e("detail#intent params error!!");
                return;
            }
            if (mDeviceId == 0) {
                logger.e("detail#intent params error!!");
                return;
            }

            device = imService.getDeviceManager().findDeviceCard(mDeviceId);
            loginContact = IMLoginManager.instance().getLoginInfo();
            mDeviceManager = imService.getDeviceManager();
            if(device != null && device.getMasterId() != loginContact.getPeerId()){
                etTitle.setEnabled(false);
                etTitle.setClickable(false);
            }
            if(mTaskId != -1){
                mUpdateCrontab = mDeviceManager.findCrontab(mTaskId);
                if(mUpdateCrontab != null){
                    String taskName = mUpdateCrontab.getTaskName();
                    if(taskName.equals("")){
                        etTitle.setText(R.string.device_task);
                    }else{
                        etTitle.setText(taskName);
                    }

                    String beginTime = mUpdateCrontab.getBeginTime();
                    String endTime = mUpdateCrontab.getEndTime();
                    mMorningStartTimeStr = beginTime.substring(0,beginTime.indexOf(","));
                    mtvafternoonStartTimeStr = beginTime.substring(beginTime.indexOf(",")+1,beginTime.length());
                    mMorningEndTimeStr = endTime.substring(0,endTime.indexOf(","));
                    mAfternoonEndTimeStr = endTime.substring(endTime.indexOf(",")+1,endTime.length());
                    tvMorningStartTime.setText(mMorningStartTimeStr);
                    tvafternoonStartTime.setText(mtvafternoonStartTimeStr);
                    tvMorningEndTime.setText(mMorningEndTimeStr);
                    tvAfternoonEndTime.setText(mAfternoonEndTimeStr);
                    handlerTimeInt();
                    String repeateValue = mUpdateCrontab.getRepeatValue();
                    mRepeateVaule = repeateValue;
                    String[] repeateValueArray ;
                    if(repeateValue != null && repeateValue.contains(",")){
                        repeateValueArray =  repeateValue.split(",");
                        HandlerRepateValue(repeateValueArray);
                        setRepatText(repeateValueArray);
                    }else if(repeateValue != null && !repeateValue.equals("")){

                        repeateValueArray = new  String[]{ repeateValue};
                    }else{
                        repeateValueArray = new  String[]{""};
                        return;
                    }

                    HandlerRepateValue(repeateValueArray);


                }

            }



        }
    };
    private int mTimeMode;
    private int[] listStrIds;
    private ProgressBar pb;

    private void handlerTimeInt() {
        if(mMorningStartTimeStr != null && mMorningStartTimeStr.contains(":")){
            String[] morningSartTimes = mMorningStartTimeStr.split(":");
            morningStartHour = Integer.parseInt(morningSartTimes[0]);
            morningStartmin = Integer.parseInt(morningSartTimes[1]);
        }
        if(mMorningEndTimeStr != null && mMorningEndTimeStr.contains(":")){
            String[] morningEndTimes = mMorningEndTimeStr.split(":");
            morningEndHour = Integer.parseInt(morningEndTimes[0]);
            morningEndmin = Integer.parseInt(morningEndTimes[1]);
        }
        if(mAfternoonEndTimeStr != null && mAfternoonEndTimeStr.contains(":")){
            String[] afternoonSartTimes = mAfternoonEndTimeStr.split(":");
            afternoonStartHour = Integer.parseInt(afternoonSartTimes[0]);
            afternoonStartmin = Integer.parseInt(afternoonSartTimes[1]);
        }
        if(mAfternoonEndTimeStr != null && mAfternoonEndTimeStr.contains(":")){
            String[] afternoonEndTimes = mAfternoonEndTimeStr.split(":");
            afternoonEndHour = Integer.parseInt(afternoonEndTimes[0]);
            afternoonEndmin = Integer.parseInt(afternoonEndTimes[1]);
        }

    }

    private TextView repeatText;
    private int morningStartHour;
    private int morningStartmin;
    private int morningEndHour;
    private int morningEndmin;
    private int afternoonStartHour;
    private int afternoonStartmin;
    private int afternoonEndHour;
    private int afternoonEndmin;
    private Boolean compareResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_lesson_reminder_detail);
        init();
        initView();
    }

    private void initView() {
        etTitle = (EditText) findViewById(R.id.et_love_remind_title);
        pb = (ProgressBar) findViewById(R.id.progress_bar);
        RelativeLayout rlMorningStartTime = (RelativeLayout) findViewById(R.id.rl_morning_set_start_time);
        RelativeLayout rlMorningEndTime = (RelativeLayout) findViewById(R.id.rl_morning_set_end_time);
        RelativeLayout rlAfernoonStartTime = (RelativeLayout) findViewById(R.id.rl_afternoon_set_start_time);
        RelativeLayout rlAfternoonEndTime = (RelativeLayout) findViewById(R.id.rl_afternoon_set_end_time);
        RelativeLayout rlSetRepeate = (RelativeLayout) findViewById(R.id.rl_set_repeat);
        TextView btnRight = (TextView) findViewById(R.id.right_button);
        LinearLayout btnLeft = (LinearLayout) findViewById(R.id.icon_arrow_layout);
        tvMorningStartTime = (TextView) findViewById(R.id.tv_morning_start_time);
        tvMorningEndTime = (TextView) findViewById(R.id.tv_morning_end_time);
        tvafternoonStartTime = (TextView) findViewById(R.id.tv_afternoon_start_time);
        tvAfternoonEndTime = (TextView) findViewById(R.id.tv_afternoon_end_time);
        repeatText = (TextView) findViewById(R.id.repeat_text);
        rlMorningStartTime.setOnClickListener(this);
        rlMorningEndTime.setOnClickListener(this);
        rlAfernoonStartTime.setOnClickListener(this);
        rlAfternoonEndTime.setOnClickListener(this);
        rlSetRepeate.setOnClickListener(this);
        btnRight.setOnClickListener(this);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void init() {
                imServiceConnector.connect(this);
        listStrIds = new int[]{R.string.monday,R.string.tuesday ,R.string.wednesdays,R.string.thursday ,R.string.friday , R.string.saturday , R.string.sunday};
        checkedList = new Boolean[]{false,false,false,false,false,false,false};
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

    }

    @Override
    public void onClick(View v) {
        if(device != null && device.getMasterId() != loginContact.getPeerId()){
             Utils.showToast(LessonRemindDetailActivity.this , getString(R.string.no_authority_to_operate));
            return;
        }

    switch (v.getId()){
        case R.id.rl_set_repeat:
            showSelRepeateDia();
            break;

        case R.id.rl_morning_set_start_time:

            showTimeSelDia(tvMorningStartTime);
            inputManager.hideSoftInputFromWindow(
                    etTitle.getWindowToken(), 0);
            break;
        case R.id.rl_morning_set_end_time:

            showTimeSelDia(tvMorningEndTime);
            inputManager.hideSoftInputFromWindow(
                    etTitle.getWindowToken(), 0);
            break;
        case R.id.rl_afternoon_set_start_time:
//
            showTimeSelDia(tvafternoonStartTime);
            inputManager.hideSoftInputFromWindow(
                    etTitle.getWindowToken(), 0);
            break;
        case R.id.rl_afternoon_set_end_time:
//
            showTimeSelDia(tvAfternoonEndTime);
            inputManager.hideSoftInputFromWindow(
                    etTitle.getWindowToken(), 0);
            break;
        case R.id.selrepeate_dialog_button_cancel:

            mSelRepeateDialog.dismiss();
            break;

        case R.id.selrepeate_dialog_button_confirm:
            checkedList =  repeateVauleAdapter.getCheckArray();
            mRepeateVaule = getRepeateValueStr(checkedList);
            // 整合 --》传递到服务器

            mSelRepeateDialog.dismiss();
            break;
        case R.id.right_button:
            // send to server
            IMBaseDefine.OperateType operateType = null;
            if(mTaskId == -1){
                operateType = IMBaseDefine.OperateType.OPERATE_TYPE_INSERT;
                mTaskId = 0;
            }else {
                operateType = IMBaseDefine.OperateType.OPERATE_TYPE_UPDATE;
                mTaskId = LessonRemindDetailActivity.this.getIntent().getIntExtra(IntentConstant.DEVICE_CRONTAB_ID ,-2);
            }

            if(mRepeateVaule.equals("")){
                mRepeateVaule = "1,2,3,4,5,6,7";
            }

            if((!mMorningStartTimeStr.equals("00:00")  ||  !mMorningEndTimeStr.equals("00:00") ) || ( !mtvafternoonStartTimeStr.equals("00:00")  ||  !mAfternoonEndTimeStr.equals("00:00") )){

                mDeviceManager.setDevTask(mLoginId , mDeviceId ,operateType , mTaskId, IMDevice.TaskType.TASK_TYPE_LESSION_MODE, etTitle.getText().toString(),"",mMorningStartTimeStr+","+mtvafternoonStartTimeStr,mMorningEndTimeStr+","+mAfternoonEndTimeStr,1,0,mRepeateVaule);
                pb.setVisibility(View.VISIBLE);
            }else{

                Utils.showToast(this , R.string.please_slecte_complete_time);

            }


            break;

        case R.id.seltime_dialog_button_confirm:

            int hour = mWheelHour.getCurrentItem();
            int minute = mWheelMin.getCurrentItem();

            if(mTimeMode == AFTERNOON_TIME){
                hour += 12;
            }
            String hourTxt = hour+"";
            String minuteTxt = minute+"";
            TextView textView = (TextView) mWheelHour.getTag();
            switch (textView.getId()){
                case R.id.tv_morning_start_time:
                    morningStartHour = hour;
                    morningStartmin = minute;
                    compareResult = compareTime(morningStartHour , morningStartmin , morningEndHour , morningEndmin);
                    if(!compareResult){
                        Utils.showToast(LessonRemindDetailActivity.this , R.string.time_compare_result_hint);
                        return;
                    }
                    break;
                case R.id.tv_morning_end_time:
                    morningEndHour = hour;
                    morningEndmin = minute;
                     compareResult = compareTime(morningStartHour , morningStartmin , morningEndHour , morningEndmin);
                    if(!compareResult){
                        Utils.showToast(LessonRemindDetailActivity.this , R.string.time_compare_result_hint);
                        return;
                    }
                    break;
                case R.id.tv_afternoon_start_time:
                    afternoonStartHour = hour;
                    afternoonStartmin = minute;

                    compareResult = compareTime(afternoonStartHour , afternoonStartmin , afternoonEndHour , afternoonEndmin);
                    if(!compareResult){
                        Utils.showToast(LessonRemindDetailActivity.this , R.string.time_compare_result_hint);
                        return;
                    }
                    break;
                case R.id.tv_afternoon_end_time:
                    afternoonEndHour = hour;
                    afternoonEndmin = minute;
                    compareResult = compareTime(afternoonStartHour , afternoonStartmin , afternoonEndHour , afternoonEndmin);
                    if(!compareResult){
                        Utils.showToast(LessonRemindDetailActivity.this , R.string.time_compare_result_hint);
                        return;
                    }
                    break;

            }

            if(hour < 10){
                hourTxt = "0"+hourTxt;
            }
            if(minute < 10){
                minuteTxt = "0"+minute;
            }
            String timeStr = hourTxt+":"+minuteTxt;

//          tvShowTime.setText(hourTxt+"时"+minuteTxt+"分");
            textView.setText(timeStr);
            mSelTimeDialog.dismiss();
            handlerTimeStr(timeStr);
            break;
        case R.id.seltime_dialog_button_cancel:
            mSelTimeDialog.dismiss();

            break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeviceEvent event) {
        switch (event) {
            case DEVICE_TASK_ADD_SUCCESS:
                pb.setVisibility(View.GONE);
                finish();
                break;
            case DEVICE_TASK_UPDATE_SUCCESS:
                pb.setVisibility(View.GONE);
                finish();
                break;
            case USER_INFO_CRONTAB_DEVICE_FAILED:
                pb.setVisibility(View.GONE);
                Utils.showToast(this , getString(R.string.device_task_operate_fail));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
            }

    private Boolean compareTime(int morningStartHour, int morningStartmin, int morningEndHour, int morningEndmin) {

        boolean result = false;
        if(morningEndHour == 0 && morningEndmin == 0){
            result = true;
            return result;
        }
        if(morningStartHour == 0 && morningStartmin == 0){
            result = true;
            return result;
        }
        if(morningStartHour > morningEndHour){
            return result;
        }else if(morningStartHour == morningEndHour && morningStartmin >= morningEndmin ){
            return result;
        }

        result = true;
        return result;
    }

    private void handlerTimeStr(String timeStr) {

        TextView textView = (TextView) mWheelHour.getTag();
        if(textView == tvMorningStartTime){
            mMorningStartTimeStr = timeStr;
        }else if(textView == tvMorningEndTime){
            mMorningEndTimeStr = timeStr;
        }else if(textView == tvafternoonStartTime){
            mtvafternoonStartTimeStr = timeStr;
        }else if(textView == tvAfternoonEndTime){
            mAfternoonEndTimeStr = timeStr;
        }

    }

    private final int MORNING_TIME = 0;
    private final int AFTERNOON_TIME = 1;
    private void showTimeSelDia(TextView view) {
        mSelTimeDialog = new AlertDialog.Builder(this).create();
        mSelTimeDialog.show();
        mSelTimeDialog.getWindow().setContentView(R.layout.device_task_time_sel_dialog);
        mSelTimeDialog.setCanceledOnTouchOutside(true);
        mSelTimeDialog.findViewById(R.id.seltime_dialog_button_cancel).setOnClickListener(this);
        mSelTimeDialog.findViewById(R.id.seltime_dialog_button_confirm).setOnClickListener(this);
        mWheelHour = (WheelView) mSelTimeDialog.findViewById(R.id.dialog_wheel_hour);
        mWheelMin = (WheelView) mSelTimeDialog.findViewById(R.id.dialog_wheel_min);
        mWheelHour.setTag(view);
        switch (view.getId()){
            case R.id.tv_morning_start_time:
            case R.id.tv_morning_end_time:

                initWheelView(MORNING_TIME);
                break;
            case R.id.tv_afternoon_start_time:
            case R.id.tv_afternoon_end_time:

                initWheelView(AFTERNOON_TIME);
                break;
        }



    }

    private void initWheelView(int timeMode) {
        NumericWheelAdapter numericWheelAdapter3 = null;
        mTimeMode = timeMode;
       if(timeMode == MORNING_TIME){

            numericWheelAdapter3=new NumericWheelAdapter(this,0, 11, "%02d");
       }else{
           numericWheelAdapter3=new NumericWheelAdapter(this,12, 23, "%02d");
       }

        numericWheelAdapter3.setLabel("时");
        mWheelHour.setViewAdapter(numericWheelAdapter3);
        mWheelHour.setCyclic(true);
        mWheelHour.addScrollingListener(scrollListener);
        mWheelHour.setVisibleItems(7);

        NumericWheelAdapter numericWheelAdapter4=new NumericWheelAdapter(this,0, 59, "%02d");
        numericWheelAdapter4.setLabel("分");
        mWheelMin.setViewAdapter(numericWheelAdapter4);
        mWheelMin.setCyclic(true);
        mWheelMin.addScrollingListener(scrollListener);
        mWheelMin.setVisibleItems(7);
    }


    private void showSelRepeateDia() {
        mSelRepeateDialog = new AlertDialog.Builder(this).create();

        mSelRepeateDialog.show();
        mSelRepeateDialog.getWindow().setContentView(R.layout.repeate_value_set_dialog);
        mSelRepeateDialog.setCanceledOnTouchOutside(true);
        mRepeateList = (ListView) mSelRepeateDialog.getWindow().findViewById(R.id.dialog_list);
        TextView btnCnacle = (TextView) mSelRepeateDialog.getWindow().findViewById(R.id.selrepeate_dialog_button_cancel);
        TextView btnConfirm = (TextView) mSelRepeateDialog.getWindow().findViewById(R.id.selrepeate_dialog_button_confirm);
        repeateVauleAdapter = new TaskRepeateVauleAdapter(this , listStrIds , null ,checkedList);
        mRepeateList.setAdapter(repeateVauleAdapter);
        btnCnacle.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    private String getRepeateValueStr(Boolean[] checkedList) {
        StringBuffer repeateValueBuf = new StringBuffer();
        String repeateValueStr = "";
        for (int i=0 ;i < checkedList.length  ;i++){
            if(checkedList[i]){

                repeateValueBuf.append((i+1)+",");
            }
        }
        repeateValueStr = repeateValueBuf.toString();
        setRepatText(repeateValueStr.split(","));
        if(repeateValueStr != null && repeateValueStr.contains(",")){
            return repeateValueStr.substring(0,repeateValueStr.lastIndexOf(","));
        }else{
            return repeateValueStr;
        }


    }


    private void setRepatText(String[] values ){
        StringBuffer relpeatValueTextBuf = new StringBuffer();
        String repeateValueText = "";
        for (int i =0 ;i < values.length ; i++){
            relpeatValueTextBuf.append("周"+values[i]+" ");

        }
        repeateValueText = relpeatValueTextBuf.toString();
        repeatText.setText(repeateValueText);
    }


    private void HandlerRepateValue(String[] values ){
        for (int i =0 ;i < values.length ; i++){
            int valueItem = Integer.parseInt(values[i]);
            if(valueItem >0 && valueItem <= checkedList.length){
                checkedList[valueItem -1] = true;
            }
        }
    }

    OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {

        }

        @Override
        public void onScrollingFinished(WheelView wheel) {



        }
    };
}

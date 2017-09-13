package com.fise.xiaoyu.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2017/4/6.
 */

public class SweetRemindDetailActivity extends TTBaseActivity implements View.OnClickListener , AdapterView.OnItemClickListener{
    private LinearLayout mLlWheelView;
    private RelativeLayout mRlShowBtn;
    private  WheelView mWheelHour;
    private WheelView mWheelMin;
    private  TextView tvRemindTime;
    private ListView mRepeateList;
    private Boolean[] checkedList;
    private TaskRepeateVauleAdapter repeateVauleAdapter;
    private  AlertDialog mDialog;
    private IMService imService;
    private IMDeviceManager mDeviceManager;
    private  int mDeviceId;
    private int  mLoginId;
    private  EditText etRemindTitle;
    private String timeStr = "";
    private String mRepeatVaule ="";
    private InputMethodManager inputManager;
    private int mTaskId;
    private DeviceCrontab mUpdateCrontab;
    private String[] sweetListStr;
    private ListView lv;
    private PopupWindow pw;
    private NumberAdapter adapter;
    private ArrayList<DeviceCrontab> mAllCrotabList ;
    private DeviceEntity device;
    private UserEntity loginContact;
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
            mDeviceId = SweetRemindDetailActivity.this.getIntent().getIntExtra(IntentConstant.KEY_PEERID, 0);
            mTaskId = SweetRemindDetailActivity.this.getIntent().getIntExtra(IntentConstant.DEVICE_CRONTAB_ID ,-2);
            if(mTaskId == -2){
                logger.e("detail#intent params error!!");
                return;
            }
            if (mDeviceId == 0) {
                logger.e("detail#intent params error!!");
                return;
            }
            mDeviceManager = imService.getDeviceManager();
            mAllCrotabList = mDeviceManager.getAllCrotabList(mDeviceId);
            device = imService.getDeviceManager().findDeviceCard(mDeviceId);
            loginContact = IMLoginManager.instance().getLoginInfo();
//            getAllSweetCrontabName(mAllCrotabList);
            mLoginId = imService.getLoginManager().getLoginId();
            if(mTaskId != -1){
                mUpdateCrontab = mDeviceManager.findCrontab(mTaskId);
                if(mUpdateCrontab != null){
                    etRemindTitle.setText(mUpdateCrontab.getTaskName());
                    tvRemindTime.setText(mUpdateCrontab.getBeginTime());
                    timeStr = mUpdateCrontab.getBeginTime();

                    String repeateValue = mUpdateCrontab.getRepeatValue();
                    mRepeatVaule = repeateValue;
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
//                    Log.i("aaa","repeateValue: "+repeateValue);
                }

            }
            if(device != null && device.getMasterId() != loginContact.getPeerId()){
                etRemindTitle.setEnabled(false);
                etRemindTitle.setClickable(false);
            }

        }
    };
    private List<String> crontabNameList;

    private void getAllSweetCrontabName(ArrayList<DeviceCrontab> mAllCrotabList) {
        crontabNameList = new ArrayList<>();
        for (int i = 0; i < mAllCrotabList.size(); i++) {
            if(mAllCrotabList.get(i).getTaskType() == IMDevice.TaskType.TASK_TYPE_LOVE_REMIND.ordinal()){
                crontabNameList.add(mAllCrotabList.get(i).getTaskName());
            }
        }
    }

    private TextView mRepeatText;
    private int[] listStrIds;
    private ProgressBar pb;
    private  String oldInputText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_sweet_reminder_detail);
        init();
        initView();
    }

    private void init() {
                listStrIds = new int[]{R.string.monday,R.string.tuesday ,R.string.wednesdays,R.string.thursday ,R.string.friday , R.string.saturday , R.string.sunday};
        imServiceConnector.connect(this);
        checkedList = new Boolean[]{false,false,false,false,false,false,false};
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        sweetListStr = new String[]{"起床" ,"睡觉" ,"写作业" ,"吃药"};
    }

    private void initView() {
        pb = (ProgressBar) findViewById(R.id.progress_bar);
        RelativeLayout rlSetTime = (RelativeLayout) findViewById(R.id.rl_set_time);
        RelativeLayout rlSetRepeat = (RelativeLayout) findViewById(R.id.rl_set_repeat);
        Button btnCancel = (Button) findViewById(R.id.button_cancel);
        Button btnConfirm = (Button) findViewById(R.id.button_confirm);
        TextView btnSave = (TextView) findViewById(R.id.right_button);
        LinearLayout btnLeft = (LinearLayout) findViewById(R.id.icon_arrow_layout);
        etRemindTitle = (EditText) findViewById(R.id.et_love_remind_title);
//        etRemindTitle.setFocusable(false);
        mLlWheelView = (LinearLayout) findViewById(R.id.ll_wheelview);
        mRlShowBtn = (RelativeLayout) findViewById(R.id.show_button);
        mWheelHour = (WheelView) findViewById(R.id.wheel_hour);
        mWheelMin = (WheelView) findViewById(R.id.wheel_min);
        tvRemindTime = (TextView) findViewById(R.id.tv_show_time);
        mRepeatText = (TextView) findViewById(R.id.tv_repeat_text);
        rlSetTime.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        rlSetRepeat.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        etRemindTitle.setOnClickListener(this);
//        etRemindTitle.setText(sweetListStr[0]);
        NumericWheelAdapter numericWheelAdapter3=new NumericWheelAdapter(this,0, 24, "%02d");
        numericWheelAdapter3.setLabel("时");
        mWheelHour.setViewAdapter(numericWheelAdapter3);
        mWheelHour.setCyclic(true);
        mWheelHour.addScrollingListener(scrollListener);

        NumericWheelAdapter numericWheelAdapter4=new NumericWheelAdapter(this,0, 59, "%02d");
        numericWheelAdapter4.setLabel("分");
        mWheelMin.setViewAdapter(numericWheelAdapter4);
        mWheelMin.setCyclic(true);
        mWheelMin.addScrollingListener(scrollListener);
        etRemindTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int textLen  = s.toString().replaceAll("[^\\x00-\\xff]", "**").length();
                if(textLen > 6){
                    etRemindTitle.setText(oldInputText);
                }else{
                    oldInputText = s.toString();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {

        if(device != null && device.getMasterId() != loginContact.getPeerId()){
            Utils.showToast(SweetRemindDetailActivity.this , getString(R.string.no_authority_to_operate));
            return;
        }
        switch (v.getId()){
            case R.id.button_cancel:
                if(mRlShowBtn.getVisibility() != View.GONE){
                    mRlShowBtn.setVisibility(View.GONE);
                    mLlWheelView.setVisibility(View.GONE);
                }

                break;

            case R.id.button_confirm:
                int hour = mWheelHour.getCurrentItem();
                int minute = mWheelMin.getCurrentItem();
                String hourTxt = hour+"";
                String minuteTxt = minute+"";
                if(hour < 10){
                    hourTxt = "0"+hourTxt;
                }
                if(minute < 10){
                    minuteTxt = "0"+minute;
                }
                timeStr = hourTxt+":"+minuteTxt;
//                tvShowTime.setText(hourTxt+"时"+minuteTxt+"分");
                tvRemindTime.setText(timeStr);
                if(mRlShowBtn.getVisibility() != View.GONE){
                    mRlShowBtn.setVisibility(View.GONE);
                    mLlWheelView.setVisibility(View.GONE);
                }
                break;
            case R.id.rl_set_time:
               if(mRlShowBtn.getVisibility() != View.VISIBLE){
                   mRlShowBtn.setVisibility(View.VISIBLE);
                   mLlWheelView.setVisibility(View.VISIBLE);
               }
                inputManager.hideSoftInputFromWindow(etRemindTitle.getWindowToken(), 0);
                break;
            case R.id.rl_set_repeat:
                showSelRepeatDia();
                break;

            case R.id.selrepeate_dialog_button_cancel:

                mDialog.dismiss();
                break;
            case R.id.selrepeate_dialog_button_confirm:
                checkedList =  repeateVauleAdapter.getCheckArray();
                mRepeatVaule = getRepeateValueStr(checkedList);
                // 整合 --》传递到服务器

                mDialog.dismiss();
                break;
            case R.id.right_button:
                 // send to server
                IMBaseDefine.OperateType operateType = null;
//                if(crontabNameList.contains(etRemindTitle.getText().toString())){
//                    Utils.showToast(SweetRemindDetailActivity.this , "请勿重复关爱提醒");
//                    return;
//                }
                if(mTaskId == -1){
                    operateType = IMBaseDefine.OperateType.OPERATE_TYPE_INSERT;
                    mTaskId = 0;
                }else {
                    operateType = IMBaseDefine.OperateType.OPERATE_TYPE_UPDATE;
                }
                if(mRepeatVaule.equals("")){
                    mRepeatVaule = "1,2,3,4,5,6,7";
                }
                mDeviceManager.setDevTask(mLoginId , mDeviceId ,operateType ,
                        mTaskId, IMDevice.TaskType.TASK_TYPE_LOVE_REMIND,etRemindTitle.getText().toString(),"",timeStr,"",1,0,mRepeatVaule);
                pb.setVisibility(View.VISIBLE);
//                finish();
                break;

            case R.id.et_love_remind_title:
                showSelectNumberPoupWindow();

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
    /**
     * 弹出选择号码对话框
     */
    private void showSelectNumberPoupWindow() {

        initListView();

        pw = new PopupWindow(lv, etRemindTitle.getWidth() - 4, 500);

        // 点击外部可以被关闭
        pw.setOutsideTouchable(true);		// 设置外部可以被点击
        pw.setBackgroundDrawable(new BitmapDrawable());

        pw.setFocusable(true);		// 使popupwindow可以获得焦点
        // 显示在输入框的左下角
        pw.showAsDropDown(etRemindTitle, 1, -15);
    }

    /**
     * 创建ListView对象
     */
    private void initListView() {
        lv = new ListView(this);
        lv.setDividerHeight(1);
        lv.setVerticalScrollBarEnabled(false);		// 消除滚动条
        lv.setBackgroundResource(R.color.my_setting_color);
        lv.setSelector(R.color.transparent);
        adapter = new NumberAdapter();
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
    }

    class NumberAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return sweetListStr.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            HolderView mHolder = null;
            if(convertView == null) {
                convertView = View.inflate(SweetRemindDetailActivity.this, R.layout.sweet_remind_list_item, null);
                mHolder = new HolderView();
                mHolder.tvNumber = (TextView) convertView.findViewById(R.id.tv_numberlist_item_number);
                convertView.setTag(mHolder);
            } else {
                mHolder = (HolderView) convertView.getTag();
            }
            mHolder.tvNumber.setText(sweetListStr[position]);
            return convertView;
        }
    }

    public class HolderView {
        public TextView tvNumber;
        public ImageButton ibDelete;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String number = sweetListStr[position];
        etRemindTitle.setText(number);
        pw.dismiss();
    }




    private void showSelRepeatDia() {
        mDialog = new AlertDialog.Builder(this).create();
        mDialog.show();
        mDialog.getWindow().setContentView(R.layout.repeate_value_set_dialog);
        mDialog.setCanceledOnTouchOutside(true);
        mRepeateList = (ListView) mDialog.getWindow().findViewById(R.id.dialog_list);
        TextView btnCnacle = (TextView) mDialog.getWindow().findViewById(R.id.selrepeate_dialog_button_cancel);
        TextView btnConfirm = (TextView) mDialog.getWindow().findViewById(R.id.selrepeate_dialog_button_confirm);
        repeateVauleAdapter = new TaskRepeateVauleAdapter(this ,listStrIds , null , checkedList);
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
        if(repeateValueStr != null && repeateValueStr.contains(",")){
            repeateValueStr =  repeateValueStr.substring(0,repeateValueStr.lastIndexOf(","));
        }
        setRepatText(repeateValueStr.split(","));
        return repeateValueStr;


    }

    private void setRepatText(String[] values ){
        StringBuffer relpeatValueTextBuf = new StringBuffer();
        String repeateValueText = "";
        for (int i =0 ;i < values.length ; i++){

            relpeatValueTextBuf.append("周"+values[i]+",");

        }
        repeateValueText = relpeatValueTextBuf.toString();
        mRepeatText.setText(repeateValueText.substring(0,repeateValueText.lastIndexOf(",")));
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

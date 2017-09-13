package com.fise.xiaoyu.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.entity.WhiteEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMDevice.SettingType;
import com.fise.xiaoyu.ui.adapter.TaskRepeateVauleAdapter;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Utils;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by lenovo on 2017/5/31.
 */

public class WhitePhoneNumberSetActivity extends TTBaseActivity implements View.OnClickListener{

    private RelativeLayout rlIdentity;
    private RelativeLayout rlPhoneNumber;
    private int[] textIds;
    private int[] imgIds;
    private Boolean[] checkedList;
    private TaskRepeateVauleAdapter repeateVauleAdapter;
    private AlertDialog mSelRepeateDialog;
    private EditText tvIdentity;
    private EditText erInputPhoneNum;
    private TextView tvInputNum;
    private  final  int ACTIVITY_REQUEST_CODE = 1000;
    private TextView tvRightText;
    private IMService imService;
    private int currentUserId;
    private DeviceEntity device;
    private  Boolean mIsIdentitySelected = false;
    private UserEntity currentDevice;
    List<WhiteEntity> whiteList = new ArrayList<>();
    private final int SEL_SOS_NUMBER = 1;
    private final int SEL_WHITE_NUMBER = 0;
    private  String  oldInputText = "";
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

            currentUserId = WhitePhoneNumberSetActivity.this.getIntent().getIntExtra(
                    IntentConstant.KEY_PEERID, 0);

            if (currentUserId == 0) {
                logger.e("detail#intent params error!!");
                return;
            }

            currentDevice = imService.getContactManager().findDeviceContact(
                    currentUserId);
            device = imService.getDeviceManager().findDeviceCard(currentUserId);
            if (device == null) {
                return;
            }
            if(selNumberType == SEL_WHITE_NUMBER){
                whiteList = imService.getDeviceManager().getWhiteListContactList(
                        currentUserId);
            }else {
                whiteList = imService.getDeviceManager().getAlarmListContactList(
                        currentUserId);
            }


            handlerView();


        }
    };
    private TextView title;
    private int selNumberType;

    private void handlerView() {


       if(currentDevice.getUserType() == IMBaseDefine.ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE && selNumberType == SEL_WHITE_NUMBER){
//           tvIdentity.setFocusable(false);
//           tvIdentity.setOnClickListener(this);
//           rlPhoneNumber.setOnClickListener(this);

           tvInputNum.setVisibility(View.GONE);
           erInputPhoneNum.setVisibility(View.VISIBLE);

       }

        if(currentDevice.getUserType() == IMBaseDefine.ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE && selNumberType == SEL_SOS_NUMBER){
            tvIdentity.setHint(R.string.input_identity);
            rlPhoneNumber.setOnClickListener(this);

        }

       if(selNumberType == SEL_SOS_NUMBER){
           tvInputNum.setVisibility(View.GONE);
//           tvIdentity.setHint(R.string.input_identity);
           erInputPhoneNum.setVisibility(View.VISIBLE);
       }
        //只有儿童手表要做限制
       if(currentDevice.getUserType() == IMBaseDefine.ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE){
           tvIdentity.addTextChangedListener(new TextWatcher() {
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
                       tvIdentity.setText(oldInputText);
                   }else{
                       oldInputText = s.toString();
                   }
               }
           });
       }


    }

    private LinearLayout backLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(this);
        setContentView(R.layout.tt_activity_set_white_phone_number);
        init();
        initView();
    }

    private void init() {
        checkedList = new Boolean[]{false,false,false,false,false,false,false};
        textIds = new int[]{R.string.father,R.string.mather , R.string.grandpa , R.string.grandma , R.string.grandfather , R.string.grandmamather };
        imgIds = new int[]{R.drawable.head_daddy , R.drawable.head_mother ,R.drawable.head_grandpa , R.drawable.head_grandma , R.drawable.head_grandfather , R.drawable.head_grandmother};

    }

    private void initView() {
        rlIdentity = (RelativeLayout) findViewById(R.id.rl_set_identity);
        rlPhoneNumber = (RelativeLayout) findViewById(R.id.rl_set_phone_number);
        backLayout = (LinearLayout) findViewById(R.id.back_layout);
        erInputPhoneNum = (EditText) findViewById(R.id.et_inout_phone_number);
        tvInputNum = (TextView) findViewById(R.id.tv_input_number_hint);
        tvIdentity = (EditText) findViewById(R.id.tv_identity);
        tvRightText = (TextView) findViewById(R.id.right_text);
        title = (TextView) findViewById(R.id.activity_title);
        backLayout.setOnClickListener(this);
        tvRightText.setOnClickListener(this);


        String phoneNumber =  getIntent().getStringExtra("phone_number");
        selNumberType = getIntent().getIntExtra(IntentConstant.SEL_PHONE_NUMBER , -1);
        if(selNumberType == SEL_SOS_NUMBER){
            title.setText(getString(R.string.sos_phone_number_add));

        }
        if(phoneNumber !=  null){
            tvIdentity.setText(getIntent().getStringExtra("phone_name"));
            erInputPhoneNum.setText(getIntent().getStringExtra("phone_number"));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_identity:
                showSelIdentityDia();
                break;
            case R.id.rl_set_phone_number:
                showSelPhoneNumberDia();
                break;
            case R.id.back_layout:
                finish();
                break;
            case R.id.icon_arrow_back:
                finish();
                break;

            case R.id.left_text:
                finish();
                break;
            case R.id.selrepeate_dialog_button_cancel:
                mSelRepeateDialog.dismiss();
                break;
            case R.id.selrepeate_dialog_button_confirm:

                Boolean[] checkArray =  repeateVauleAdapter.getCheckArray();
                for (int i = 0; i < checkArray.length; i++) {
                    if(checkArray[i]){
                        tvIdentity.setText(getResources().getString(textIds[i]));
                        mIsIdentitySelected = true;
                    }

                }
                mSelRepeateDialog.dismiss();
                break;
            case R.id.right_text:
                String phoneStr = erInputPhoneNum.getText().toString().trim();
//                int a  = tvIdentity.getText().toString().replaceAll("[^\\x00-\\xff]", "**").length();
                if(phoneStr.contains(" ")){
                phoneStr = phoneStr.replaceAll(" ","");
            }
                boolean isPhone = Utils.isMobileNO(phoneStr);
						if (isPhone == false) {
							Utils.showToast(WhitePhoneNumberSetActivity.this,
									"输入的号码不正确");

							return;
						}

						boolean isExisted = false;
                        boolean isIdentityExisted = false;
						for (int i = 0; i < whiteList.size(); i++) {
							if (whiteList.get(i).getPhone().equals(erInputPhoneNum.getText().toString())) {
								isExisted = true;
								break;
							}
                            if (whiteList.get(i).getName().equals(tvIdentity.getText().toString())) {
                                isIdentityExisted = true;
                                break;
                            }

						}

						if (isExisted) {
							Utils.showToast(WhitePhoneNumberSetActivity.this,
									"您的输入手机号码已经存在");
							return;
						}
                        if(isIdentityExisted){
                            Utils.showToast(WhitePhoneNumberSetActivity.this,
                                    "该身份已存在，请重新选择");
                            return;
                        }
                        String identity = tvIdentity.getText().toString();
						if(identity.equals("")){
                            Utils.showToast(WhitePhoneNumberSetActivity.this,
                                    "请输入昵称");
                            return;
                        }


                        if(selNumberType == SEL_SOS_NUMBER){
                            imService.getDeviceManager().settingWhite(
								currentUserId,
                                tvIdentity.getText().toString()+":"+phoneStr,
								SettingType.SETTING_TYPE_ALARM_MOBILE,
								DBConstant.ADD);
                        }else{
                            imService.getDeviceManager().settingWhite(
                                    currentUserId,
                                    tvIdentity.getText().toString()+":"+phoneStr,
                                    SettingType.SETTING_TYPE_ALLOW_MOBILE,
                                    DBConstant.ADD);
                        }

                        finish();
                break;

        }

    }

    private void showSelPhoneNumberDia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(this,
                        android.R.style.Theme_Holo_Light_Dialog));
        //builder.setTitle(recentInfo.getName());
        String[] items = new String[] { getString(R.string.input_manual_operation),
                getString(R.string.go_contact_list)};
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        erInputPhoneNum.setVisibility(View.VISIBLE);
                        tvInputNum.setVisibility(View.GONE);
                        erInputPhoneNum.requestFocus();
                        break;
                    case 1: {
                        Intent intent = new Intent(WhitePhoneNumberSetActivity.this , GroupSelectActivity.class);
                        intent.putExtra(IntentConstant.HIDE_SELECT_CHECK_BOX, true);
                        intent.putExtra(IntentConstant.KEY_PEERID ,currentUserId);
                        startActivityForResult(intent , ACTIVITY_REQUEST_CODE);
//                        IMUIHelper.openGroupMemberSelectActivity(WhitePhoneNumberSetActivity.this , true);
                    }
                    break;
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }


    private void showSelIdentityDia() {
        mSelRepeateDialog = new AlertDialog.Builder(this).create();

        mSelRepeateDialog.show();
        mSelRepeateDialog.getWindow().setContentView(R.layout.identity_value_set_dialog);
        mSelRepeateDialog.setCanceledOnTouchOutside(true);
        ListView  mRepeateList = (ListView) mSelRepeateDialog.getWindow().findViewById(R.id.dialog_list);
        RelativeLayout rlShowBtn = (RelativeLayout) mSelRepeateDialog.findViewById(R.id.show_button);
        rlShowBtn.setVisibility(View.GONE);
        repeateVauleAdapter = new TaskRepeateVauleAdapter(this , textIds , imgIds , checkedList);
        mRepeateList.setAdapter(repeateVauleAdapter);
        mRepeateList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tvIdentity.setText(textIds[position]);
                mSelRepeateDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTIVITY_REQUEST_CODE){

                String number = data.getStringExtra("phone_number");

//            String name = data.getStringExtra("phone_name")
                if(!number.equals("")){
                  erInputPhoneNum.setText(number.trim());
//                tvIdentity.setText(name);
                  tvInputNum.setText(number.trim());
//                tvInputNum.setVisibility(View.GONE);
//                erInputPhoneNum.setVisibility(View.VISIBLE);



            }

        }

    }


}

package com.fise.xiaoyu.ui.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.fise.xiaoyu.R;

public class FilletDialog implements View.OnClickListener {
    private Context context;
    public Dialog dialog;
    public TextView title, message, cancle, ok;
    private MyDialogOnClick myDialogOnClick;
    private boolean isFinsh = false;

    public void setFinsh(boolean finsh) {
        isFinsh = finsh;
    }

    public  enum FILLET_DIALOG_TYPE{
        FILLET_DIALOG_WITH_MESSAGE,
        FILLET_DIALOG_WITHOUT_MESSAGE,
        FILLET_DIALOG_WITH_MESSAGE_FOTA,
        FILLET_DIALOG_WITH_MESSAGE_ONE_BTN

    }


    public FilletDialog(Context context ,FILLET_DIALOG_TYPE TYPE) {
        this.context = context;
        //下面三步非常重要
        dialog = new Dialog(context, R.style.dialog);//Dialog的Style
        Window window = dialog.getWindow();

        if(TYPE == FILLET_DIALOG_TYPE.FILLET_DIALOG_WITH_MESSAGE){
            window.setContentView(R.layout.fillet_dialog_with_message);//引用Dialog的布局
        }else if(TYPE == FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE){
            window.setContentView(R.layout.fillet_dialog_without_message);//引用Dialog的布局
        }else if(TYPE == FILLET_DIALOG_TYPE.FILLET_DIALOG_WITH_MESSAGE_FOTA){  //fota 升级
            window.setContentView(R.layout.fillet_dialog_with_message_fota_update);//引用Dialog的布局
        }else{
            window.setContentView(R.layout.fillet_dialog_with_message_one_btn);//引用Dialog的布局

        }


        title = (TextView) window.findViewById(R.id.tv_title);
        message = (TextView) window.findViewById(R.id.tv_message);
        cancle = (TextView) window.findViewById(R.id.btn_cancel);
        ok = (TextView) window.findViewById(R.id.btn_ok); 
        
        cancle.setOnClickListener(this);
        ok.setOnClickListener(this);
    }


    public void setCanceledOnTouchOutside(Boolean flag){
        dialog.setCanceledOnTouchOutside(flag);
    }

    public FilletDialog(Context context,boolean isFinsh) {
        this.context = context;
        //下面三步非常重要
        dialog = new Dialog(context, R.style.dialog);//Dialog的Style
        Window window = dialog.getWindow();
        window.setContentView(R.layout.fillet_dialog_without_message);//引用Dialog的布局

        title = (TextView) window.findViewById(R.id.tv_title);
        message = (TextView) window.findViewById(R.id.tv_message);
        cancle = (TextView) window.findViewById(R.id.btn_cancel);
        ok = (TextView) window.findViewById(R.id.btn_ok);

        cancle.setOnClickListener(this);
        ok.setOnClickListener(this);
        this.isFinsh = isFinsh;
    }

    public void setTitle(String titleStr) {
        title.setText(titleStr);
    }

    public void setRight(String right) {

        ok.setText(right);
    }

    public void setLeft(String left) {

        cancle.setText(left);
    }

    public void setMessage(String messageStr) {
        message.setText(messageStr);
    }
    
     
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_ok:
               myDialogOnClick.ok();
                break;
            case R.id.btn_cancel:
                dialog.dismiss();
                if(isFinsh){
                    Activity activity = (Activity) this.context;
                    activity.finish();
                }
                break;

        }
    }

    //给确认按钮设置回调的接口
    public interface MyDialogOnClick{
         void ok();
    }

    public void setMyDialogOnClick(MyDialogOnClick myDialogOnClick){
        this.myDialogOnClick =myDialogOnClick;
    }


    public void cancel(){
        dialog.dismiss();
    }

}
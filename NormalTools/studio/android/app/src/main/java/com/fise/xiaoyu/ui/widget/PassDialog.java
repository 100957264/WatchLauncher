package com.fise.xiaoyu.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.fise.xiaoyu.R;

public class PassDialog implements View.OnClickListener {
    private Context context;
    public Dialog dialog;
    public TextView title, message, cancle, ok;
    private MyDialogOnClick myDialogOnClick;
    private EditText editTextName;

    public  enum PASS_DIALOG_TYPE{
        PASS_DIALOG_WITH_MESSAGE,
        PASS_DIALOG_WITHOUT_MESSAGE

    }

    public PassDialog(Context context ,PASS_DIALOG_TYPE DIALOG_TYPE) {
        this.context = context;
        //下面三步非常重要
        dialog = new Dialog(context, R.style.dialog);//Dialog的Style
        Window window = dialog.getWindow();
        if(DIALOG_TYPE == PASS_DIALOG_TYPE.PASS_DIALOG_WITH_MESSAGE){

            window.setContentView(R.layout.pass_dialog_with_message);//引用Dialog的布局

        }else{

            window.setContentView(R.layout.pass_dialog_without_message);//引用Dialog的布局
        }


        title = (TextView) window.findViewById(R.id.tv_title);
        message = (TextView) window.findViewById(R.id.tv_message);
        cancle = (TextView) window.findViewById(R.id.btn_cancel);
        ok = (TextView) window.findViewById(R.id.btn_ok);
        editTextName = (EditText) window.findViewById(R.id.editTextName);
        
        cancle.setOnClickListener(this);
        ok.setOnClickListener(this);
    }

    public void setTitle(String titleStr) {
        title.setVisibility(View.VISIBLE);
        title.setText(titleStr);
    }

    public void setMessage(String messageStr) {
        message.setText(messageStr);
    }
    
    
    public TextView getMessage() {
       return  message;
    }
    
    public EditText getEditText() {
       return editTextName;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_ok:
               myDialogOnClick.ok();
                break;
            case R.id.btn_cancel:
                dialog.dismiss();
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

}
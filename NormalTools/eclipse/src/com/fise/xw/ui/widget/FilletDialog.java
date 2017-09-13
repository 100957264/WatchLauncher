package com.fise.xw.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.fise.xw.R;

public class FilletDialog implements View.OnClickListener {
    private Context context;
    public Dialog dialog;
    public TextView title, message, cancle, ok;
    private MyDialogOnClick myDialogOnClick;
    

    public FilletDialog(Context context) {
        this.context = context;
        //下面三步非常重要
        dialog = new Dialog(context, R.style.dialog);//Dialog的Style
        Window window = dialog.getWindow();
        window.setContentView(R.layout.fillet_dialog);//引用Dialog的布局

        title = (TextView) window.findViewById(R.id.tv_title);
        message = (TextView) window.findViewById(R.id.tv_message);
        cancle = (TextView) window.findViewById(R.id.btn_cancel);
        ok = (TextView) window.findViewById(R.id.btn_ok); 
        
        cancle.setOnClickListener(this);
        ok.setOnClickListener(this);
    }

    public void setTitle(String titleStr) {
        title.setText(titleStr);
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
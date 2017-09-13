package com.fise.xiaoyu.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.fise.xiaoyu.R;

/**
 * Created by lenovo on 2017/7/24.
 */

public class ListLayoutDialog  implements View.OnClickListener{
    private Context context;
    public Dialog dialog;
    private final TextView mTxtOne;
    private final TextView mTxtTwo;
    private final TextView mTxtThree;
    public onItemClickListener mListener;


    public interface  onItemClickListener{
        public void onClick(int item , Dialog dialog);
    }
    public ListLayoutDialog(Context context ) {
        this.context = context;
        //下面三步非常重要
        dialog = new Dialog(context, R.style.dialog);//Dialog的Style
        Window window = dialog.getWindow();
        window.setContentView(R.layout.list_item_dialog);//引用Dialog的布局
        mTxtOne = (TextView) window.findViewById(R.id.text_item_one);
        mTxtTwo = (TextView) window.findViewById(R.id.text_item_two);
        mTxtThree = (TextView) window.findViewById(R.id.text_item_three);
        mTxtOne.setOnClickListener(this);
        mTxtTwo.setOnClickListener(this);
        mTxtThree.setOnClickListener(this);
        dialog.show();
    }


    public void setItems(String[] items , onItemClickListener itemClickListener){

        switch (items.length){
            case 1:
                mTxtTwo.setVisibility(View.GONE);
                mTxtThree.setVisibility(View.GONE);
                mTxtOne.setText(items[0]);
                break;
            case 2:
                mTxtThree.setVisibility(View.GONE);
                mTxtOne.setText(items[0]);
                mTxtTwo.setText(items[1]);
                break;
            case 3:
                mTxtOne.setText(items[0]);
                mTxtTwo.setText(items[1]);
                mTxtThree.setText(items[2]);

                break;
        }
        this.mListener = itemClickListener;

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_item_one:
                if(mListener != null){
                    mListener.onClick(0 ,dialog);
                }
                break;
            case R.id.text_item_two:
                if(mListener != null){
                    mListener.onClick(1 ,dialog);
                }
                break;
            case R.id.text_item_three:
                if(mListener != null){
                    mListener.onClick(2 ,dialog);
                }
                break;

        }
    }

}

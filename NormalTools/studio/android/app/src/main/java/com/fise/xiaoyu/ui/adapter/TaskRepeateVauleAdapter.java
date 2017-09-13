package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xiaoyu.R;


/**
 * Created by lenovo on 2017/4/7.
 */

public class TaskRepeateVauleAdapter extends BaseAdapter {

    private Context mContext;
    private Boolean[] checkedArray;
    private int[] imgsIdsd ;
    private int[] textIds;

    public TaskRepeateVauleAdapter(Context context ,int[] textIds , int[] imgsIds , Boolean[] list){
        this.mContext = context;
        this.checkedArray = list;
        this.imgsIdsd = imgsIds;
        this.textIds = textIds;
    }


    @Override
    public int getCount() {
        return textIds.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final  int position, View convertView, ViewGroup parent) {
         TaskRepeateHolder holder = null;
        if(convertView == null){
             holder =new TaskRepeateHolder();
            if(imgsIdsd == null){
                convertView = View.inflate(mContext, R.layout.task_repeate_list_item ,null);
                holder.repeateState = (CheckBox) convertView.findViewById(R.id.cb_repeate_value_state);
            }else{
                convertView = View.inflate(mContext, R.layout.white_number_identity_list_item ,null);
                holder.headImage = (ImageView) convertView.findViewById(R.id.head_imag);
                convertView.setMinimumHeight((int)mContext.getResources().getDimension(R.dimen.dialog_sel_identify_item_height));
            }

            holder.repeateText = (TextView) convertView.findViewById(R.id.tv_repeate_value);


            convertView.setTag(holder);
        }else{
            holder = (TaskRepeateHolder) convertView.getTag();
        }

//        convertView.setBackgroundResource(R.drawable.identity_item);

        holder.repeateText.setText(mContext.getString(textIds[position]));
        if(imgsIdsd != null){
            holder.headImage.setImageResource(imgsIdsd[position]);
        }else{
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TaskRepeateHolder holder = (TaskRepeateHolder) v.getTag();
                    holder.repeateState.setChecked( !holder.repeateState.isChecked());
                    checkedArray[position] = holder.repeateState.isChecked();


                }
            });
            holder.repeateState.setChecked(checkedArray[position]);
        }





        return convertView;
    }

    public Boolean[] getCheckArray(){
        return checkedArray;
    }

    public static class TaskRepeateHolder{
        ImageView headImage;
        TextView repeateText;
        CheckBox repeateState;
    }
}

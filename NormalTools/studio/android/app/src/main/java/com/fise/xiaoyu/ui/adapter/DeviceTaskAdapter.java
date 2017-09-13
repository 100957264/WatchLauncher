package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.DeviceCrontab;
import com.fise.xiaoyu.R;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/4/10.
 */

public class DeviceTaskAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<DeviceCrontab> mListData;
    private RepeateStatusChangeLinstener linstener;
    private Boolean isMaster;
    public DeviceTaskAdapter(Context mContext ,Boolean isMaster ) {
        this.mContext = mContext;
        this.isMaster = isMaster;
    }
    public interface RepeateStatusChangeLinstener{

        void rpeateStatuChange(int position, boolean changed);
    }

    public void setDataList(ArrayList<DeviceCrontab> mListData){
     this.mListData = mListData;
    }

    public void setStatusChangeListener(RepeateStatusChangeLinstener listener){
        this.linstener = listener;
    }
    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHodler hodler = null;
        if(convertView == null){
            hodler = new ViewHodler();
            convertView = View.inflate(mContext , R.layout.task_list_item ,null);
            convertView.setTag(hodler);
            hodler.taskName = (TextView) convertView.findViewById(R.id.tv_task_name);
            hodler.taskState = (CheckBox) convertView.findViewById(R.id.cb_task_state);
            hodler.task_line = (View) convertView.findViewById(R.id.task_line);

        }else{
            hodler = (ViewHodler) convertView.getTag();
        }
        DeviceCrontab crontab =mListData.get(position);

        String taskName = crontab.getTaskName();
       if(taskName.equals("")){
           hodler.taskName.setText(R.string.device_task);
       }else{
           hodler.taskName.setText(crontab.getTaskName());
       }

        if(crontab.getStatus() == 1){
            hodler.taskState.setChecked(true);
        }else if(crontab.getStatus() == 2){
            hodler.taskState.setChecked(false);
        }
        if(!isMaster){
            hodler.taskState.setEnabled(false);
        }

        hodler.taskState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                Boolean isCheck = checkBox.isChecked();
                if(linstener != null){
                    linstener.rpeateStatuChange(position ,isCheck);
                }
            }
        });

        if(position == (mListData.size()-1)){
            hodler.task_line.setVisibility(View.GONE);
        }else{
            hodler.task_line.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public static class ViewHodler{
       TextView taskName;
        CheckBox taskState;
        View task_line;

    }



}

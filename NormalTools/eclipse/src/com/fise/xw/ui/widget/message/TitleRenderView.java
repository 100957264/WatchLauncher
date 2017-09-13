package com.fise.xw.ui.widget.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.utils.DateUtil;

import java.util.Date;

/**
 * @author : yingmu on 15-1-9.
 * @email : yingmu@mogujie.com.
 *
 * 消息列表中的时间气泡
 * [备注] 插入条件是前后两条消息发送的时间diff 超过某个范围
 *
 */
public class TitleRenderView extends LinearLayout {
    private TextView Prompt_title;

    public TitleRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static TitleRenderView inflater(Context context,ViewGroup viewGroup){
        TitleRenderView timeRenderView = (TitleRenderView) LayoutInflater.from(context).inflate(R.layout.tt_message_title_text, viewGroup, false);
        return timeRenderView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Prompt_title = (TextView) findViewById(R.id.prompt_title);
    }

 
    /**与数据绑定*/
    public void setCont(String msgCont){ 
    	Prompt_title.setText(msgCont);
    }

}

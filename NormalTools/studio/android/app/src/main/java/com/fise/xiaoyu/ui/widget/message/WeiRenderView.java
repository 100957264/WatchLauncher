package com.fise.xiaoyu.ui.widget.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.utils.DateUtil;

import java.util.Date;

/**
 *
 * 消息列表中的时间气泡
 * [备注] 插入条件是前后两条消息发送的时间diff 超过某个范围
 *
 */
public class WeiRenderView extends LinearLayout {
    private TextView Prompt_title; 
    private TextView agree_title;
    private TextView refuse_title;
    
    public WeiRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static WeiRenderView inflater(Context context,ViewGroup viewGroup){
        WeiRenderView timeRenderView = (WeiRenderView) LayoutInflater.from(context).inflate(R.layout.tt_message_wei_text, viewGroup, false);
        return timeRenderView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Prompt_title = (TextView) findViewById(R.id.prompt_title);
        agree_title = (TextView) findViewById(R.id.agree_title);
        refuse_title = (TextView) findViewById(R.id.refuse_title);
        
    }
    
    public TextView getTextAgree(){ 
    	return agree_title;
    }

    public TextView getTextRefuse(){ 
    	return refuse_title;
    }

 
    /**与数据绑定*/
    public void setCont(String msgCont){ 
    	Prompt_title.setText(msgCont);
    }

}

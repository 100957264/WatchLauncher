package com.fise.xiaoyu.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.GridView;

/**
 * Created by zhujian on 15/1/19.
 */
public class GroupManagerGridView extends GridView {
    private Context ctx;
    public GroupManagerGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
    }
    public GroupManagerGridView(Context context) {
        super(context);
        this.ctx = context;
    }
    public GroupManagerGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.ctx = context;
    }
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metric);
//        int height =metric.heightPixels;
//        int expandSpec = MeasureSpec.makeMeasureSpec(
//        height-ScreenUtil.instance(ctx).dip2px(10), MeasureSpec.AT_MOST);
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}

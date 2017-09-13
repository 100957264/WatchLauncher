package com.codbking.calendar;


import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarView extends ViewGroup {

    private static final String TAG = "CalendarView";

    private int selectPostion = -1;

    private CaledarAdapter adapter;
    private List<CalendarBean> data;
    private OnItemClickListener onItemClickListener;

    private int row = 6;
    private int column = 7;
    private int itemWidth;
    private int itemHeight;

    private boolean isToday;

    private int todayYear;
    private int todayMonth;
    private int todayDay ;



    public interface OnItemClickListener {
        void onItemClick(View view, int postion, CalendarBean bean);
        void onItemUpdateClick(View view, int postion, CalendarBean bean);
    }

    public CalendarView(Context context, int row) {
        super(context);
        this.row = row;
        Calendar c = Calendar.getInstance();
        todayYear = c.get(Calendar.YEAR);
        todayMonth = c.get(Calendar.MONTH) + 1;
        todayDay = c.get(Calendar.DAY_OF_MONTH);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        Calendar c = Calendar.getInstance();
        todayYear = c.get(Calendar.YEAR);
        todayMonth = c.get(Calendar.MONTH) + 1;
        todayDay = c.get(Calendar.DAY_OF_MONTH);
    }

    public void setAdapter(CaledarAdapter adapter) {
        this.adapter = adapter;
    }

    public void setData(List<CalendarBean> data,boolean isToday) {
        this.data = data;
        this.isToday=isToday;
        setItem();
        requestLayout();
    }

    private void setItem() {

        selectPostion = -1;
        if (adapter == null) {
            throw new RuntimeException("adapter is null,please setadapter");
        }

        for (int i = 0; i < data.size(); i++) {
            CalendarBean bean = data.get(i);
            View view = getChildAt(i);
            View chidView = adapter.getView(view, this, bean);

            if (view == null || view != chidView) {
                addViewInLayout(chidView, i, chidView.getLayoutParams(), true);
            }

            if(isToday&&selectPostion==-1){
                int[]date= CalendarUtil.getYMD(new Date());
                if(bean.year==date[0]&&bean.moth==date[1]&&bean.day==date[2]){
                     selectPostion=i;
                }
            }else {
                if (selectPostion == -1 && bean.day == 1) {
                    selectPostion = i;
                }
            }

            chidView.setSelected(selectPostion==i);

            setItemClick(chidView, i, bean);

        }
    }

    public Object[] getSelect(){
         return new Object[]{getChildAt(selectPostion),selectPostion,data.get(selectPostion)};
    }

    public void setItemClick(final View view, final int potsion, final CalendarBean bean) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(bean.year == todayYear){
                    if(bean.moth == todayMonth){
                        if(bean.day >todayDay){
                            return ;
                        }
                    }else if(bean.moth>todayMonth){
                        return ;
                    }
                }else if(bean.year >todayYear){
                    return ;
                }


                if (selectPostion != -1) {
                    getChildAt(selectPostion).setSelected(false);
                    getChildAt(potsion).setSelected(true);
                }
                selectPostion = potsion;

                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(view, potsion, bean);
                }
            }
        });
    }

    public int[] getSelectPostion() {
        Rect rect = new Rect();
        try {
            getChildAt(selectPostion).getHitRect(rect);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{rect.left, rect.top, rect.right, rect.top};
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int parentWidth = MeasureSpec.getSize(MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.EXACTLY));

        itemWidth = parentWidth / column;
        itemHeight = itemWidth;

        View view = getChildAt(0);
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null && params.height > 0) {
            itemHeight = params.height;
        }
        setMeasuredDimension(parentWidth, itemHeight * row);


        for(int i=0;i<getChildCount();i++){
            View childView=getChildAt(i);
            childView.measure(MeasureSpec.makeMeasureSpec(itemWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY));
        }

        Log.i(TAG, "onMeasure() called with: itemHeight = [" + itemHeight + "], itemWidth = [" + itemWidth + "]");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i <getChildCount(); i++) {
            layoutChild(getChildAt(i), i, l, t, r, b);
        }
    }

    private void layoutChild(View view, int postion, int l, int t, int r, int b) {

        int cc = postion % column;
        int cr = postion / column;

        int itemWidth = view.getMeasuredWidth();
        int itemHeight = view.getMeasuredHeight();

        l = cc * itemWidth;
        t = cr * itemHeight;
        r = l + itemWidth;
        b = t + itemHeight;
        view.layout(l, t, r, b);

    }
}

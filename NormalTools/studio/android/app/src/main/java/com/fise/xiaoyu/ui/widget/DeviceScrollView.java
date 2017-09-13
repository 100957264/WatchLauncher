package com.fise.xiaoyu.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.imservice.service.IMService;

import java.util.ArrayList;

//┏┓　　　┏┓  
//┏┛┻━━━┛┻┓  
//┃　　　　　　　┃ 　  
//┃　　　━　　　┃  
//┃　┳┛　┗┳　┃  
//┃　　　　　　　┃  
//┃　　　┻　　　┃  
//┃　　　　　　　┃  
//┗━┓　　　┏━┛  
//┃　　　┃ 神兽保佑　　　　　　　　  
//┃　　　┃ 代码无BUG！  
//┃　　　┗━━━┓  
//┃　　　　　　　┣┓  
//┃　　　　　　　┏┛  
//┗┓┓┏━┳┓┏┛  
//┃┫┫　┃┫┫  
//┗┻┛　┗┻┛  
public class DeviceScrollView extends ScrollView {

    // 拖动的距离 size = 4 的意思 只允许拖动屏幕的1/4
    private static final int size = 4;
    private View inner;
    private float y;
    private Rect normal = new Rect();

    private IMService imService;
    private UserEntity currentDevice;
    private DeviceEntity rsp;

    boolean isMove = false;

    public DeviceScrollView(Context context) {
        super(context);
    }

    public DeviceScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);  
    }

    public void setScrollData(IMService imService,UserEntity currentDevice,DeviceEntity rsp) {
        this.imService = imService;
        this.currentDevice = currentDevice;
        this.rsp = rsp;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {  
            inner = getChildAt(0);  
        }  
    }  
  
    @Override  
    public boolean onTouchEvent(MotionEvent ev) {  
        if (inner == null) {  
            return super.onTouchEvent(ev);  
        } else {  
            commOnTouchEvent(ev);  
        }  
        return super.onTouchEvent(ev);  
    }  
  
    public void commOnTouchEvent(MotionEvent ev) {  
        int action = ev.getAction();  
        switch (action) {  
        case MotionEvent.ACTION_DOWN:  
            y = ev.getY();
            isMove = false;
            break;  
        case MotionEvent.ACTION_UP:  
            if (isNeedAnimation()) {  
                // Log.v("mlguitar", "will up and animation");  
                animation();  
            }

            if(isMove){
                // 查看用户状态 是否在线
                ArrayList<Integer> userIdStats = new ArrayList<>();
                userIdStats.add(currentDevice.getPeerId());
                imService.getContactManager().reqGetDetaillUsersStat( userIdStats);

                imService.getUserActionManager().onRepLocationReq(currentDevice.getPeerId() ,0,0);
            }
            break;  
        case MotionEvent.ACTION_MOVE:  
            final float preY = y;  
            float nowY = ev.getY();  
            /** 
             * size=4 表示 拖动的距离为屏幕的高度的1/4 
             */  
            int deltaY = (int) (preY - nowY) / size;  
            // 滚动  
            // scrollBy(0, deltaY);  
  
            y = nowY;  
            // 当滚动到最上或者最下时就不会再滚动，这时移动布局  
            if (isNeedMove()) {  
                if (normal.isEmpty()) {  
                    // 保存正常的布局位置  
                    normal.set(inner.getLeft(), inner.getTop(),  
                            inner.getRight(), inner.getBottom());  
                    return;  
                }  
                int yy = inner.getTop() - deltaY;

                if((yy>=5||yy<=-5)&&currentDevice!=null){
                    isMove = true;
                }
  
                // 移动布局  
                inner.layout(inner.getLeft(), yy, inner.getRight(),  
                        inner.getBottom() - deltaY);  
            }  
            break;  
        default:  
            break;  
        }  
    }  
  
    // 开启动画移动  
  
    public void animation() {  
        // 开启移动动画  
        TranslateAnimation ta = new TranslateAnimation(0, 0, inner.getTop(),  
                normal.top);  
        ta.setDuration(200);  
        inner.startAnimation(ta);  
        // 设置回到正常的布局位置  
        inner.layout(normal.left, normal.top, normal.right, normal.bottom);  
        normal.setEmpty();  
    }  
  
    // 是否需要开启动画  
    public boolean isNeedAnimation() {  
        return !normal.isEmpty();  
    }  
  
    // 是否需要移动布局  
    public boolean isNeedMove() {  
        int offset = inner.getMeasuredHeight() - getHeight();  
        int scrollY = getScrollY();  
        if (scrollY == 0 || scrollY == offset) {  
            return true;  
        }  
        return false;  
    }  
  
} 

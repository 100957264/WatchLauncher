package com.fise.xiaoyu.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.utils.CompatUtil;

/**
 * Created by xiejianghong on 2017/8/30.
 * 顶部标题栏，包括标题、左右按钮，均可自定义视图
 */

public class ToolBarView extends FrameLayout implements View.OnClickListener {
    public final static String CUSTOM_VIEW_TYPE_LEFT = "left";
    public final static String CUSTOM_VIEW_TYPE_CENTER = "center";
    public final static String CUSTOM_VIEW_TYPE_RIGHT = "right";

    private Context mContext;
    private FrameLayout rootLayout;
    private LinearLayout mLeftParent;
    private LinearLayout mCenterParent;
    private LinearLayout mRightParent;
    private TextView mLeftTextView, mCenterTextView, mRightTextView;
    private ImageView mLeftImageView;
    private ImageView mRightImageView;
    private Drawable leftImg;
    private Drawable rightImg;
    private String leftText, centerText, rightText;
    private int leftColor, centerColor, rightColor;
    private OnToolBarClickListener mListener;

    public ToolBarView(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public ToolBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initData(context, attrs);
    }

    public ToolBarView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ToolBarView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initData(context, attrs);
    }

    private void initData(Context context, AttributeSet attrs) {
        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.app);
        leftImg = ta.getDrawable(R.styleable.app_toolbarview_left_background);
        rightImg = ta.getDrawable(R.styleable.app_toolbarview_right_background);
        leftText = ta.getString(R.styleable.app_toolbarview_left_text);
        centerText = ta.getString(R.styleable.app_toolbarview_center_text);
        rightText = ta.getString(R.styleable.app_toolbarview_right_text);
        int defaultTitleColor = getResources().getColor(R.color.colorTitle);
        int defaultButtonColor = getResources().getColor(R.color.colorToolBarButton);
        leftColor = ta.getColor(R.styleable.app_toolbarview_left_text_color, defaultButtonColor);
        centerColor = ta.getColor(R.styleable.app_toolbarview_center_text_color, defaultTitleColor);
        rightColor = ta.getColor(R.styleable.app_toolbarview_right_text_color, defaultButtonColor);
        //记得此处要recycle();
        ta.recycle();
    }

    /**
     * 显示指定的组内的指定按钮（或文本），隐藏组内其它按钮
     *
     * @param btnIndex 按钮或文本索引从0开始,共3个（左、中、右）
     * @param btnType  按钮或文本类型（见组内定义）
     */
    private void showView(int btnIndex, int btnType) {
        switch (btnIndex) {
            case 0:
                // mLeftText.setVisibility(btnType == 0 ? View.VISIBLE : View.GONE);
                mLeftParent.setVisibility(btnType == 1 ? View.VISIBLE : View.GONE);
                break;
            case 1:
                // mCenterTextView.setVisibility(btnType == 0 ? View.VISIBLE : View.GONE);
                mCenterParent.setVisibility(btnType == 1 ? View.VISIBLE : View.GONE);
                break;
            case 2:
                // mRightText.setVisibility(btnType == 0 ? View.VISIBLE : View.GONE);
                mRightParent.setVisibility(btnType == 1 ? View.VISIBLE : View.GONE);
                break;
            default:
                throw new IllegalArgumentException("btnIndex");
        }
    }

    /**
     * 设置左侧按钮为自定义视图
     */
    public void setLeftButtonAsCustomView(View v) {
        if (mLeftParent.getChildCount() > 0)
            mLeftParent.removeAllViews();
        mLeftParent.addView(v);
    }

    /**
     * 设置中间按钮为自定义视图
     */
    public void setCenterButtonAsCustomView(View v) {
        if (mCenterParent.getChildCount() > 0)
            mCenterParent.removeAllViews();
        mCenterParent.addView(v);
    }

    /**
     * 设置右侧按钮为自定义视图
     */
    public void setRightButtonAsCustomView(View v) {
        if (mRightParent.getChildCount() > 0)
            mRightParent.removeAllViews();
        mRightParent.addView(v);
    }

    public void setTitle(String text) {
        mCenterTextView.setText(text);
    }

    public void setLeftText(String text) {
        mLeftTextView.setText(text);
    }

    public void setRightText(String text) {
        mRightTextView.setText(text);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        /* 检查是否包含自定义视图 */
        View leftCustomView = null;
        View centerCustomView = null;
        View rightCustomView = null;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View cv = getChildAt(i);
            String typeTag = (String) cv.getTag();
            if (CUSTOM_VIEW_TYPE_LEFT.equals(typeTag)) {
                leftCustomView = cv;
            } else if (CUSTOM_VIEW_TYPE_CENTER.equals(typeTag)) {
                centerCustomView = cv;
            } else if (CUSTOM_VIEW_TYPE_RIGHT.equals(typeTag)) {
                rightCustomView = cv;
            } else {
                throw new InflateException("Error ToolBarView CustomView Type：" + typeTag);
            }
        }
        this.removeAllViews();
        /* 初始化父容器 */
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View root = inflater.inflate(R.layout.view_toolbar, this, true);
        rootLayout = (FrameLayout) root.findViewById(R.id.layout_root);
        mLeftParent = (LinearLayout) root.findViewById(R.id.layout_left);
        mCenterParent = (LinearLayout) root.findViewById(R.id.layout_center);
        mRightParent = (LinearLayout) root.findViewById(R.id.layout_right);
        mLeftTextView = (TextView) root.findViewById(R.id.tv_left);
        mCenterTextView = (TextView) root.findViewById(R.id.tv_center);
        mRightTextView = (TextView) root.findViewById(R.id.tv_right);
        mLeftImageView = (ImageView) root.findViewById(R.id.iv_left);
        mRightImageView = (ImageView) root.findViewById(R.id.iv_right);
        CompatUtil.setBackgroundOfVersion(mLeftImageView, leftImg);
        CompatUtil.setBackgroundOfVersion(mRightImageView, rightImg);
        if (leftText != null) {
            mLeftTextView.setText(leftText);
        }
        if (centerText != null) {
            mCenterTextView.setText(centerText);
        }
        if (rightText != null) {
            mRightTextView.setText(rightText);
        }
        mLeftTextView.setTextColor(centerColor);
        mCenterTextView.setTextColor(centerColor);
        mRightTextView.setTextColor(centerColor);
        mCenterTextView.setOnClickListener(this);
        mLeftImageView.setOnClickListener(this);
        mRightImageView.setOnClickListener(this);
        if (leftCustomView != null) {
            setLeftButtonAsCustomView(leftCustomView);
        }
        if (centerCustomView != null) {
            setCenterButtonAsCustomView(centerCustomView);
        }
        if (rightCustomView != null) {
            setRightButtonAsCustomView(rightCustomView);
        }
    }

    /**
     * 设置监听事件
     *
     * @param mOnToolBarClickListener
     */
    public void setOnToolBarClickListener(OnToolBarClickListener mOnToolBarClickListener) {
        this.mListener = mOnToolBarClickListener;
    }

    @Override
    public void onClick(View view) {
        if (mListener == null) {
            return;
        }
        if (view == mLeftImageView || view == mLeftTextView || view == mLeftParent) {
            mListener.onLeftButtonClicked(view);
        } else if (view == mCenterTextView || view == mCenterParent) {
            mListener.onCenterButtonClicked(view);
        } else if (view == mRightImageView || view == mRightTextView || view == mRightParent) {
            mListener.onRightButtonClicked(view);
        }
    }

    public interface OnToolBarClickListener {
        void onLeftButtonClicked(View view);

        void onCenterButtonClicked(View view);

        void onRightButtonClicked(View view);
    }
}
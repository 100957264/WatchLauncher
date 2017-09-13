package com.fise.xiaoyu.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.fise.xiaoyu.R;

/**
 * Created by lenovo on 2017/6/19.
 */

public class CameraNetVideoItemView extends FrameLayout {


    private String videoDuration;
    private String videoCreate;
    private Context mContext;
    private TextView tvDuration;
    private String imageUrl;
    private TextView tvCreate;
    private String videoUrl;
    private IMBaseImageView videoPreView;

    public CameraNetVideoItemView(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }



    public CameraNetVideoItemView( Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CameraNetVideo);
        initView();
    }

    public CameraNetVideoItemView( Context context, AttributeSet attrs ,int defStyle) {
        super(context, attrs ,defStyle);
        this.mContext = context;
        initView();
    }

    private void initView() {
        View view = View.inflate(mContext, R.layout.camera_net_video_item_view, this);
        tvDuration = (TextView) view.findViewById(R.id.video_duration);
        tvCreate = (TextView) view.findViewById(R.id.video_create);
        videoPreView = (IMBaseImageView) view.findViewById(R.id.net_video_preview_img);

    }



    public void setVideoCreate(String videoCreate) {

        tvCreate.setText(videoCreate);
        Log.i("aaa", "setVideoCreate: "+videoCreate);
        this.videoCreate = videoCreate;
    }


    public void setVideoDuration(String videoDuration) {
        tvDuration.setText(videoDuration);
        Log.i("aaa", "setVideoDuration: "+videoDuration);
        this.videoDuration = videoDuration;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        videoPreView.setImageUrl(imageUrl);
        this.imageUrl = imageUrl;
    }


    public void setVideoUrl(String videoUrl) {

        this.videoUrl = videoUrl;
    }
}

package com.fise.xw.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.fise.xw.R;
import com.fise.xw.utils.CommonUtil;
import com.fise.xw.utils.FileUtil;
import com.fise.xw.utils.ImageLoaderUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener; 
 

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by zhujian on 15/2/13.
 */
public class MapImageView extends ImageView {
    /**
     * 图片设置相关
     */
    protected String imageUrl = null;
    protected boolean isAttachedOnWindow = false;
    protected int defaultImageRes = R.drawable.icon_postion_normal;  //scan_mask  地图默认图片

    protected ImageLoaddingCallback imageLoaddingCallback;


    public MapImageView(Context context) {
        super(context);
    } 

    public MapImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MapImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* 图片设置相关 */

    public void setImageLoaddingCallback(ImageLoaddingCallback callback) {
        this.imageLoaddingCallback = callback;
    }

    public void setImageUrl(final String url) {
        this.imageUrl = url;
        if (isAttachedOnWindow) {
            final MapImageView view = this;
            if (!TextUtils.isEmpty(this.imageUrl)) {
                ImageAware imageAware = new ImageViewAware(this, false);
                ImageLoaderUtil.getImageLoaderInstance().displayImage(this.imageUrl, imageAware, new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .showImageOnLoading(R.drawable.icon_postion_normal)
                        .showImageOnFail(R.drawable.icon_postion_errorl)
                        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .delayBeforeLoading(100)
                        .build(), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        if (imageLoaddingCallback != null) {

                            String cachePath = ImageLoaderUtil.getImageLoaderInstance().getDiskCache().get(imageUri).getPath();//这个路径其实已不再更新
                            imageLoaddingCallback.onLoadingComplete(cachePath, view, loadedImage);
                        }
                    }  

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        if (imageLoaddingCallback != null) {
                            imageLoaddingCallback.onLoadingStarted(imageUri, view);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        super.onLoadingCancelled(imageUri, view);
                        if (imageLoaddingCallback != null) {
                            imageLoaddingCallback.onLoadingCanceled(imageUri, view);
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                        if (imageLoaddingCallback != null) {
                            imageLoaddingCallback.onLoadingFailed(imageUri, view);
                        }
                    }
                });
            }
        } else {
            this.setImageResource(R.drawable.icon_postion_normal);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedOnWindow = true;
        setImageUrl(this.imageUrl);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.isAttachedOnWindow = false;
        ImageLoaderUtil.getImageLoaderInstance().cancelDisplayTask(this);
    }

    public interface ImageLoaddingCallback {
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage);

        public void onLoadingStarted(String imageUri, View view);

        public void onLoadingCanceled(String imageUri, View view);

        public void onLoadingFailed(String imageUri, View view);
    }


}

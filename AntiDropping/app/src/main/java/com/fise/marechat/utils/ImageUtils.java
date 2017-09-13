package com.fise.marechat.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.IOException;
import java.net.URL;

/**
 * Created by blue on 2017/6/9 0009.
 */

public class ImageUtils {

    public static  Drawable url2Drawable(String url) {
        Drawable drawable = null;
        try{
            //judge if has picture locate or not according to filename
            drawable = Drawable.createFromStream(new URL(url).openStream(), "image.jpg");
        }catch(IOException e){
            Log.d("mare",e.getMessage());
        }
        if(drawable == null){
            Log.d("test","null drawable");
        }else{
            Log.d("test","not null drawable");
        }
        return drawable;
    }


    private BitmapFactory.Options getBitmapOption(String path,int inSampleSize){
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        Bitmap bitmap= BitmapFactory.decodeFile(path,options); //将图片的长和宽缩小味原来的1/2

        return options;
    }

}

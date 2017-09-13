package com.fise.marechat.function.photo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.fise.marechat.constant.FileConstant;
import com.fise.marechat.task.CommonTask;
import com.fise.marechat.util.CameraUtils;

import java.io.File;

/**
 * @author mare
 * @Description:拍照功能
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/12
 * @time 9:55
 */
public class TakePhoto extends CommonTask {

    public TakePhoto(Context ctx) {
        Intent intent = CameraUtils.getOpenCameraIntent();
        File file=new File(FileConstant.PHOTO_CAPTURE_OUTPUT_PATH);
        //拍照后原图回存入此路径下
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
    }

    @Override
    public void exeTask() {
        super.exeTask();

    }



}

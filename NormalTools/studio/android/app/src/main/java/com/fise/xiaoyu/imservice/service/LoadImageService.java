package com.fise.xiaoyu.imservice.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.fise.xiaoyu.DB.sp.SystemConfigSp;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.MessageConstant;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.entity.ImageMessage;
import com.fise.xiaoyu.imservice.entity.VedioMessage;
import com.fise.xiaoyu.imservice.event.MessageEvent;
import com.fise.xiaoyu.ui.helper.PhotoHelper;
import com.fise.xiaoyu.utils.FileUtil;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.MoGuHttpClient;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

/**
 *  加载图片上传服务
 */
public class LoadImageService extends IntentService {

    private static Logger logger = Logger.getLogger(LoadImageService.class);

    public LoadImageService(){
        super("LoadImageService");
    }

    public LoadImageService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int type =   intent.getIntExtra(SysConstant.UPLOAD_IMAGE_INTENT_PARAMS_TYPE,0);


        if(type ==  DBConstant.MESSAGE_VEDIO_IMAGE){

            VedioMessage messageInfo = (VedioMessage)intent.getSerializableExtra(SysConstant.UPLOAD_IMAGE_INTENT_PARAMS);
            String result = null;
            Bitmap bitmap;
            try {
                File file= new File(messageInfo.getImagePath());
                if(file.exists() && FileUtil.getExtensionName(messageInfo.getImagePath()).toLowerCase().equals(".gif"))
                {
                    MoGuHttpClient httpClient = new MoGuHttpClient();
                    SystemConfigSp.instance().init(getApplicationContext());
                    result = httpClient.uploadImage3(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), FileUtil.File2byte(messageInfo.getImagePath()), messageInfo.getImagePath());
                }
                else
                {
                    bitmap = PhotoHelper.revitionImage(messageInfo.getImagePath());
                    if (null != bitmap) {
                        MoGuHttpClient httpClient = new MoGuHttpClient();
                        byte[] bytes = PhotoHelper.getBytes(bitmap);
                        result = httpClient.uploadImage3(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), bytes, messageInfo.getImagePath());
                    }
                }

                if (TextUtils.isEmpty(result)) {
                    logger.i("upload image faild,cause by result is empty/null");
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.Event.IMAGE_VEDIO_UPLOAD_FAILD
                            ,messageInfo));
                } else {
                    logger.i("upload image succcess,imageUrl is %s",result);

                    String imageUrl = result;
                    messageInfo.setImageUrl(imageUrl);
                    messageInfo.setLoadStatus(MessageConstant.IMAGE_VEDIO_LOADED_SUCCESS);
                    messageInfo.setStatus(MessageConstant.MSG_SUCCESS);
                    EventBus.getDefault().post(new MessageEvent(
                            MessageEvent.Event.IMAGE_VEDIO_LOADED_SUCCESS
                            ,messageInfo));
                }
            } catch (IOException e) {
                logger.e(e.getMessage());
            }

        }else{
            ImageMessage messageInfo = (ImageMessage)intent.getSerializableExtra(SysConstant.UPLOAD_IMAGE_INTENT_PARAMS);


            String result = null;
            Bitmap bitmap;
            try {
                File file= new File(messageInfo.getPath());
                if(file.exists() && FileUtil.getExtensionName(messageInfo.getPath()).toLowerCase().equals(".gif"))
                {
                    MoGuHttpClient httpClient = new MoGuHttpClient();
                    SystemConfigSp.instance().init(getApplicationContext());
                    result = httpClient.uploadImage3(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), FileUtil.File2byte(messageInfo.getPath()), messageInfo.getPath());
                }
                else
                {
                    bitmap = PhotoHelper.revitionImage(messageInfo.getPath());
                    if (null != bitmap) {
                        MoGuHttpClient httpClient = new MoGuHttpClient();
                        byte[] bytes = PhotoHelper.getBytes(bitmap);
                        result = httpClient.uploadImage3(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), bytes, messageInfo.getPath());
                    }
                }

                if (TextUtils.isEmpty(result)) {
                    logger.i("upload image faild,cause by result is empty/null");
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.Event.IMAGE_UPLOAD_FAILD
                            ,messageInfo));
                } else {
                    logger.i("upload image succcess,imageUrl is %s",result);

                    String imageUrl = result;
                    messageInfo.setUrl(imageUrl);
                    EventBus.getDefault().post(new MessageEvent(
                            MessageEvent.Event.IMAGE_UPLOAD_SUCCESS
                            ,messageInfo));
                }
            } catch (IOException e) {
                logger.e(e.getMessage());
            }
        }


    }
}

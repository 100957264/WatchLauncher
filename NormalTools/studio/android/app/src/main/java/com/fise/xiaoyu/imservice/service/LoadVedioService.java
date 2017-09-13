package com.fise.xiaoyu.imservice.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.fise.xiaoyu.DB.sp.SystemConfigSp;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.entity.VedioMessage;
import com.fise.xiaoyu.imservice.event.MessageEvent;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.VedioHttpClient;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *  上传小视频服务
 */
public class LoadVedioService extends IntentService {

    private static Logger logger = Logger.getLogger(LoadImageService.class);

    public LoadVedioService(){
        super("LoadImageService");
    }

    public LoadVedioService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	VedioMessage messageInfo = (VedioMessage)intent.getSerializableExtra(SysConstant.UPLOAD_VEDIO_INTENT_PARAMS);
            String result = null;
            String vedioResult = null;
            Bitmap bitmap;

                
            	/* 创建File对象，确定需要读取文件的信息 */
        		File vedioFile = new File(messageInfo.getVedioPath());
        		/* FileInputSteam 输入流的对象， */
        		FileInputStream fis;
        		byte[] buffer = null;
        		try {
        			fis = new FileInputStream(vedioFile);

        			/* 准备一个字节数组用户装即将读取的数据 */
        			buffer = new byte[fis.available()];

        			/* 开始进行文件的读取 */
        			fis.read(buffer);
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}

        		vedioResult = VedioHttpClient.uploadImage3(SystemConfigSp.instance()
        				.getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER),
        				buffer, messageInfo.getVedioPath());
        		

                if (TextUtils.isEmpty(vedioResult)) {
                    logger.i("upload image faild,cause by result is empty/null");
//                    messageInfo.setStatus();
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.Event.VEDIO_UPLOAD_FAILD
                    ,messageInfo));
                } else {
                    logger.i("upload image succcess,imageUrl is %s",result);
                    String imageUrl = result;
                    String vedioUrl =  vedioResult; 
                    messageInfo.setVedioUrl(vedioUrl);

                    EventBus.getDefault().post(new MessageEvent(
                            MessageEvent.Event.VEDIO_UPLOAD_SUCCESS
                            ,messageInfo));
                }
           
    }
}

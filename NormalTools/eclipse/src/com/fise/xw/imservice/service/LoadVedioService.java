package com.fise.xw.imservice.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.fise.xw.DB.sp.SystemConfigSp;
import com.fise.xw.config.SysConstant;
import com.fise.xw.imservice.entity.VedioMessage;
import com.fise.xw.imservice.event.MessageEvent;
import com.fise.xw.ui.helper.PhotoHelper;
import com.fise.xw.utils.FileUtil;
import com.fise.xw.utils.Logger;
import com.fise.xw.utils.MoGuHttpClient;
import com.fise.xw.utils.VedioHttpClient;

import de.greenrobot.event.EventBus;

/**
 * @author : yingmu on 15-1-12.
 * @email : yingmu@mogujie.com.
 *
 */
public class LoadVedioService extends IntentService {

    private static Logger logger = Logger.getLogger(LoadImageService.class);

    public LoadVedioService(){
        super("LoadImageService");
    }

    public LoadVedioService(String name) {
        super(name);
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               android.content.Context#startService(android.content.Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
    	VedioMessage messageInfo = (VedioMessage)intent.getSerializableExtra(SysConstant.UPLOAD_VEDIO_INTENT_PARAMS);
            String result = null;
            String vedioResult = null;
            Bitmap bitmap;
            
//                File file= new File(messageInfo.getPath());
//                if(file.exists() && FileUtil.getExtensionName(messageInfo.getPath()).toLowerCase().equals(".gif"))
//                {
//                    MoGuHttpClient httpClient = new MoGuHttpClient();
//                    SystemConfigSp.instance().init(getApplicationContext());
//                    result = httpClient.uploadImage3(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), FileUtil.File2byte(messageInfo.getPath()), messageInfo.getPath());
//                }
//                else
//                {
//                    bitmap = PhotoHelper.revitionImage(messageInfo.getPath());
//                    if (null != bitmap) {
//                        MoGuHttpClient httpClient = new MoGuHttpClient();
//                        byte[] bytes = PhotoHelper.getBytes(bitmap);
//                        result = httpClient.uploadImage3(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), bytes, messageInfo.getPath());
//                    }
//                }
                
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

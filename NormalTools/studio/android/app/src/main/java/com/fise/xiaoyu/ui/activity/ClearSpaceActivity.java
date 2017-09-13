package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fise.xiaoyu.DB.DBInterface;
import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.entity.ImageMessage;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.SelfStatistics;
import com.fise.xiaoyu.utils.CommonUtil;
import com.fise.xiaoyu.utils.FileSizeUtil;
import com.fise.xiaoyu.utils.FileUtil;
import com.fise.xiaoyu.utils.ImageLoaderUtil;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

/**
 * Created by weileiguan on 2017/5/19 0019.
 */
public class ClearSpaceActivity  extends TTBaseActivity {
    private    IMService imService;
    private     SelfStatistics selfStatistics;


    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("config#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                // 后台服务启动链接失败
                return ;
            }


        }
        @Override
        public void onServiceDisconnected() {
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tt_activity_clear_space);
        imServiceConnector.connect(this);

        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();

       //
//         File folder = new File(Environment.getExternalStorageDirectory()
//                .getAbsolutePath()
//                + File.separator
//                + "MGJ-IM");
        File folder;
        if (CommonUtil.checkSDCard()) {
          folder = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + File.separator
                + "MGJ-IM" + File.separator + "images");

        } else {
            folder = new File(Environment.getDataDirectory()
                    .getAbsolutePath()
                    + File.separator
                    + "MGJ-IM" + File.separator + "images");
        }

        double imageSize = FileSizeUtil.getFileOrFilesSize(folder.getPath(),FileSizeUtil.SIZETYPE_MB);//imageStatFs.getBlockSize();


        File sampleDir ;
        if (CommonUtil.checkSDCard()) {
            sampleDir = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath()
                    + File.separator
                    + "MGJ-IM" + File.separator + "vedio");

        } else {
            sampleDir = new File(Environment.getDataDirectory()
                    .getAbsolutePath()
                    + File.separator
                    + "MGJ-IM" + File.separator + "vedio");
        }

        double vedioSize =  FileSizeUtil.getFileOrFilesSize(sampleDir.getPath(),FileSizeUtil.SIZETYPE_MB);
        double size = imageSize + vedioSize;
        double other = blockSize - size;


        selfStatistics = (SelfStatistics) findViewById(R.id.self_statistics);
        float datas[] = new float[]{(float) size,(float)other};
        selfStatistics.setDatas(datas);
        selfStatistics.startDraw();

        TextView otherText =(TextView)findViewById(R.id.other);

        //保留最后两位
        int otherInt = (int)((other/1024)*1000);
        float otherFloat = (float)(otherInt) / 1000;
        otherText.setText("其他  " + otherFloat +"GB");


        int xiaoWeiInt = (int)((size/1024)*1000);
        float xiaoWeiFloat = (float)(xiaoWeiInt) / 1000;
        if(xiaoWeiFloat<=0.0f){
            xiaoWeiFloat = 0.001f;
        }
        TextView xiaowei =(TextView)findViewById(R.id.xiaowei);
        xiaowei.setText("小雨  " + xiaoWeiFloat +"GB");

        Button icon_arrow =(Button)findViewById(R.id.icon_arrow);
        icon_arrow.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                ClearSpaceActivity.this.finish();
            }
        });


        TextView left_text =(TextView)findViewById(R.id.left_text);
        left_text.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                ClearSpaceActivity.this.finish();
            }
        });


        Button clear_space = (Button) findViewById(R.id.clear_space);
        clear_space.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ImageLoaderUtil.clearCache();

                File folder;

                if (CommonUtil.checkSDCard()) {
                    folder = new File(Environment.getExternalStorageDirectory()
                            .getAbsolutePath()
                            + File.separator
                            + "MGJ-IM");

                } else {
                    folder = new File(Environment.getDataDirectory()
                            .getAbsolutePath()
                            + File.separator
                            + "MGJ-IM");
                }


                File sampleDir ;
                if (CommonUtil.checkSDCard()) {
                    sampleDir = new File(
                            Environment.getExternalStorageDirectory()
                                    + File.separator + "im/video/");

                } else {
                    sampleDir = new File(
                            Environment.getDataDirectory()
                                    + File.separator + "im/video/");
                }

                RecursionDeleteFile(folder);
                RecursionDeleteFile(sampleDir);


                List<MessageEntity> msgList = DBInterface.instance().getAllMessage();
                for(int i=0;i<msgList.size();i++){
                    if(msgList.get(i).getDisplayType() == DBConstant.SHOW_IMAGE_TYPE){
                        ImageMessage imageMessage = ImageMessage.parseFromDB(msgList.get(i));//(ImageMessage) (msgList.get(i));
                        if(FileUtil.isFileExist(imageMessage.getPath())){
                            File file = new File(imageMessage.getPath());
                            file.delete();
                        }

                    }
                }
                Utils.showToast(ClearSpaceActivity.this,"清理空间成功");
            }
        });


    }

    /**
     * 递归删除文件和文件夹
     * @param file    要删除的根目录
     */
    public void RecursionDeleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event){
        switch (event){


        }
    }

    @Override
    public void onDestroy() {
                imServiceConnector.disconnect(this);
        super.onDestroy();
    }

}

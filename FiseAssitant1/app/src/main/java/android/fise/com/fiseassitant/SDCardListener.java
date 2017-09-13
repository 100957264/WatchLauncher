package android.fise.com.fiseassitant;

import android.content.Context;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.content.Intent;
/**
 * Created by qingfeng on 2017/6/30.
 */

public class SDCardListener extends FileObserver {

    Context mContext;
    FiseHandelr mFiseHandelr;
    final int FILE_MODIFY_MESSAGE_NUMBER = 1;
    final int FILE_MODIFY_MESSAGE_BIND= 2;
    final int FILE_MODIFY_MESSAGE_FORBID= 3;
    final int FILE_MODIFY_MESSAGE_NETWORK= 4;

    public SDCardListener(String path) {
        super(path);
    }

    public SDCardListener(Context context, String path) {
        super(path);
        mContext = context;
        mFiseHandelr = new FiseHandelr();
    }

    @Override
    public void onEvent(int event, String path) {
        switch (event) {
            case FileObserver.MODIFY:
                Log.d("fengqing", "SDCardListener: path =" + path);
                if(path.contains("number")){
                  mFiseHandelr.sendEmptyMessage(FILE_MODIFY_MESSAGE_NUMBER);
                }else if(path.contains("bind")){
                  mFiseHandelr.sendEmptyMessage(FILE_MODIFY_MESSAGE_BIND);
                }else if(path.contains("forbid")){
                    mFiseHandelr.sendEmptyMessage(FILE_MODIFY_MESSAGE_FORBID);
                }else if(path.contains("network")){
                    mFiseHandelr.sendEmptyMessage(FILE_MODIFY_MESSAGE_NETWORK);
                }
                break;
        }

    }

    class FiseHandelr extends Handler {
        @Override
        public void handleMessage(Message msg) {
		    Log.d("fengqing", "SDCardListener: msg.what =" + msg.what);
            switch (msg.what) {
                case FILE_MODIFY_MESSAGE_NUMBER:

                    break;
                case FILE_MODIFY_MESSAGE_BIND:
                    boolean isBindStatusChange = ReadSDcardFileUtil.isFileExisted(ReadSDcardFileUtil.BIND_STATUS_PATH);
                    String bindStatus = ReadSDcardFileUtil.readSDFile(ReadSDcardFileUtil.BIND_STATUS_PATH);
                    if (isBindStatusChange && bindStatus != null && bindStatus.equals("1")) {
                        Settings.System.putInt(mContext.getContentResolver(), "band_phone", 1);
                    } else {
                        Settings.System.putInt(mContext.getContentResolver(), "band_phone", 0);
                    }
                    break;
                case FILE_MODIFY_MESSAGE_FORBID:
                    boolean isForbidStatusChange = ReadSDcardFileUtil.isFileExisted(ReadSDcardFileUtil.STOP_STATUS_PATH);
                    String forbidStatus = ReadSDcardFileUtil.readSDFile(ReadSDcardFileUtil.STOP_STATUS_PATH);
                    if (isForbidStatusChange && forbidStatus != null && forbidStatus.equals("1")) {
                        Settings.System.putInt(mContext.getContentResolver(), "forbid_status", 1);
                    } else {
                        Settings.System.putInt(mContext.getContentResolver(), "forbid_status", 0);
                    }
                    break;
                case FILE_MODIFY_MESSAGE_NETWORK:
				    
                    boolean isNetworkStatusChange = ReadSDcardFileUtil.isFileExisted(ReadSDcardFileUtil.NETWORK_STATUS_PATH);
                    String networktatus = ReadSDcardFileUtil.readSDFile(ReadSDcardFileUtil.NETWORK_STATUS_PATH);
                    
                    Log.d("fengqing","networktatus =" + networktatus );
		           
                    if (isNetworkStatusChange && networktatus != null && networktatus.equals("1")) {
						Intent intent = new Intent("fise.intent.action.ACTION_NETWORK_2G");
						mContext.sendBroadcast(intent);
                    } else {
                      Intent intent = new Intent("fise.intent.action.ACTION_NETWORK_4G");
				      mContext.sendBroadcast(intent);
                    }
                    break;
            }
        }
    }
}

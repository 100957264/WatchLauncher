package android.fise.com.fiseassitant;

import android.content.Context;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
//import com.android.internal.telephony.Phone;
//import com.android.internal.telephony.PhoneConstants;
//import com.android.internal.telephony.PhoneFactory;
//import android.os.AsyncResult;
//import com.android.internal.telephony.RILConstants;
/**
 * Created by qingfeng on 2017/6/30.
 */

public class SDCardListener extends FileObserver {

    Context mContext;
    FiseHandelr mFiseHandelr;
    TelephonyManager mTelephoneManager;
  //  Phone mPhone;

    final int FILE_MODIFY_MESSAGE_NUMBER = 1;
    final int FILE_MODIFY_MESSAGE_BIND= 2;
    final int FILE_MODIFY_MESSAGE_FORBID= 3;
    final int FILE_MODIFY_MESSAGE_NETWORK= 4;
    final int EVENT_QUERY_PREFERRED_TYPE_DONE = 1000;
    final int EVENT_SET_PREFERRED_TYPE_DONE = 1001;
   // final int GSM_ONLY_VALUE = Phone.NT_MODE_GSM_ONLY;
   // final int LTE_GSM_WCDMA_VALUE = Phone.NT_MODE_LTE_GSM_WCDMA;
  //  final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;

    public SDCardListener(String path) {
        super(path);
    }

    public SDCardListener(Context context, String path) {
        super(path);
        mContext = context;
        mFiseHandelr = new FiseHandelr();
        mTelephoneManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
       // mPhone = PhoneFactory.getDefaultPhone();
    }

    @Override
    public void onEvent(int event, String path) {
        switch (event) {
            case FileObserver.MODIFY:
                Log.d("fengqing", "SDCardListener: path =" + path);
                if(path.equals("/sdcard/watchlauncher/number/")){
                  mFiseHandelr.sendEmptyMessage(FILE_MODIFY_MESSAGE_NUMBER);
                }else if(path.equals("/sdcard/watchlauncher/bind/")){
                  mFiseHandelr.sendEmptyMessage(FILE_MODIFY_MESSAGE_BIND);
                }else if(path.equals("/sdcard/watchlauncher/forbid/")){
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
 //           AsyncResult ar;
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
     /*               int settingsNetworkMode = android.provider.Settings.Global.getInt(
                            mContext.getContentResolver(),
                            android.provider.Settings.Global.PREFERRED_NETWORK_MODE + mPhone.getSubId(),
                            preferredNetworkMode);
                    Log.d("fengqing","settingsNetworkMode = " + settingsNetworkMode + ",networktatus =" + networktatus);
                    if (isNetworkStatusChange && networktatus != null && networktatus.equals("1")) {
                        Settings.Global.putInt(mContext.getContentResolver(),
                                Settings.Global.PREFERRED_NETWORK_MODE + mPhone.getSubId(), GSM_ONLY_VALUE);
                        if (mPhone != null) {
                            mPhone.setPreferredNetworkType(GSM_ONLY_VALUE, obtainMessage(EVENT_SET_PREFERRED_TYPE_DONE));
                        }
                    } else {
                        Settings.Global.putInt(mContext.getContentResolver(),
                                Settings.Global.PREFERRED_NETWORK_MODE + mPhone.getSubId(), LTE_GSM_WCDMA_VALUE);
                        if (mPhone != null) {
                            mPhone.setPreferredNetworkType(LTE_GSM_WCDMA_VALUE, obtainMessage(EVENT_SET_PREFERRED_TYPE_DONE));
                        }
                    }
                    break;
                case EVENT_SET_PREFERRED_TYPE_DONE:
                    ar = (AsyncResult) msg.obj;
                    if ((ar.exception != null) && (mPhone != null)) {
                        mPhone.getPreferredNetworkType(obtainMessage(EVENT_QUERY_PREFERRED_TYPE_DONE));
                    }
                    break;*/
            }
        }
    }
}

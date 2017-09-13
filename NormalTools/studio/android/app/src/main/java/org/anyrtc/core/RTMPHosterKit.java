
package org.anyrtc.core;

import android.app.Activity;
import android.util.Log;

import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.EglBase;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;

/**
 * Created by Eric on 2016/7/25.
 */
public class RTMPHosterKit {
    private static final String TAG = "RTMPHosterKit";
    public enum RTMPNetAdjustMode
    {
        RTMP_NA_Nor(0),		    // Normal
        RTMP_NA_Fast(1),		// When network is bad, we will drop some video frame.
        RTMP_NA_AutoBitrate(2);	// When network is bad, we will adjust video bitrate to match.

        public final int level;
        RTMPNetAdjustMode(int level) {
            this.level = level;
        };
    };
    /**
     * 构造访问jni底层库的对象
     */
    private long fNativeAppId;
    private Activity   mActivity;
    private RTMPHosterHelper mHosterHelper;
    private final LooperExecutor mExecutor;
    private final EglBase mEglBase;

    private int mCameraId = 0;
    private VideoCapturerAndroid mVideoCapturer;


    public RTMPHosterKit(Activity act, final RTMPHosterHelper hosterHelper) {
        RTMPUtils.assertIsTrue(act != null && hosterHelper != null);
        mActivity = act;
        mHosterHelper = hosterHelper;
        AnyRTMP.Inst().Init(mActivity.getApplicationContext());

        mExecutor = AnyRTMP.Inst().Executor();
        mEglBase = AnyRTMP.Inst().Egl();

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(hosterHelper);
            }
        });
    }

    public void Clear() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(mVideoCapturer != null) {
                    try {
                        mVideoCapturer.stopCapture();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    nativeSetVideoCapturer(null, 0);
                    mVideoCapturer = null;
                }
                nativeDestroy();
            }
        });
    }

    //* Common function
    public void SetAudioEnable(final boolean enabled){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetAudioEnable(enabled);
            }
        });
    }
    public void SetVideoEnable(final boolean enabled){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoEnable(enabled);
            }
        });
    }
    public void SetVideoCapturer(final long renderPointer, final boolean front){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {

                if(mVideoCapturer == null) {
                    mCameraId = 0;
                    String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(mCameraId);
                    String frontCameraDeviceName =
                            CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                    int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                    if (numberOfCameras > 1 && frontCameraDeviceName != null && front) {
                        cameraDeviceName = frontCameraDeviceName;
                        mCameraId = 1;
                    }
                    Log.d(TAG, "Opening camera: " + cameraDeviceName);
                    mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);
                    if (mVideoCapturer == null) {
                        Log.e("sys", "Failed to open camera");
                        return;
                    }
                }
                nativeSetVideoCapturer(mVideoCapturer, renderPointer);
            }
        });
    }
    public void SwitchCamera(){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(mVideoCapturer != null && CameraEnumerationAndroid.getDeviceCount() > 1) {
                    mCameraId = (mCameraId + 1) % CameraEnumerationAndroid.getDeviceCount();
                    mVideoCapturer.switchCamera(null);
                }
            }
        });
    }

    public void ExchangeVedio(final long renderPointer){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(mVideoCapturer!=null) {
                    nativeSetVideoCapturer(mVideoCapturer, renderPointer);
                }
            }
        });
    }

    //* Rtmp function for push rtmp stream
    public void StartRtmpStream(final String strUrl){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeStartRtmpStream(strUrl);
            }
        });
    }
    public void StopRtmpStream(){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeStopRtmpStream();
            }
        });
    }

    /**
     *  Native function
     */
    private native long nativeCreate(Object obj);
    private native void nativeSetAudioEnable(boolean enabled);
    private native void nativeSetVideoEnable(boolean enabled);
    private native void nativeSetVideoCapturer(VideoCapturer capturer, long nativeRenderer);
    private native void nativeStartRtmpStream(String strUrl);
    private native void nativeStopRtmpStream();
    private native void nativeDestroy();
}

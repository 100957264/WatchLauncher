package ageing.fise.com.fiseageingtest;

import android.hardware.Camera.CameraInfo;

public class CameraHolder {
	
    // Use a singleton.
    private static CameraHolder sHolder;
    public static synchronized CameraHolder instance() {
        if (sHolder == null) {
            sHolder = new CameraHolder();
        }
        return sHolder;
    }
    
    private int mNumberOfCameras;
    private int mBackCameraId = -1, mFrontCameraId = -1;
    private CameraInfo[] mInfo;

    private CameraHolder() {
        init();
    }

    public int getNumberOfCameras() {
        return mNumberOfCameras;
    }
    
    public CameraInfo[] getCameraInfo() {
        return mInfo;
    }

    public int getBackCameraId() {
        return mBackCameraId;
    }
    
    public int getFrontCameraId() {
        return mFrontCameraId;
    }

    public void init() {
        mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
        mInfo = new CameraInfo[mNumberOfCameras];
        for (int i = 0; i < mNumberOfCameras; i++) {
            mInfo[i] = new CameraInfo();
            android.hardware.Camera.getCameraInfo(i, mInfo[i]);
            if (mBackCameraId == -1 && mInfo[i].facing == CameraInfo.CAMERA_FACING_BACK) {
                mBackCameraId = i;
            }
            if (mFrontCameraId == -1 && mInfo[i].facing == CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCameraId = i;
            }
        }
    }

    public synchronized void release() {
    	mNumberOfCameras = -1;
        mInfo = null;
        mBackCameraId = -1;
        mFrontCameraId = -1;
        
        sHolder = null;
    }
}

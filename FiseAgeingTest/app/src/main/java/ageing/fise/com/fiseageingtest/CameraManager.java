package ageing.fise.com.fiseageingtest;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

public class CameraManager {
    private static final String TAG = "CameraManager";
    private static CameraManager sCameraManager = new CameraManager();
    
    // Thread progress signals
    private ConditionVariable mSig = new ConditionVariable();
    
    private Parameters mParameters;
    private IOException mReconnectException;
    
    private static final int RELEASE = 1;
    private static final int RECONNECT = 2;
    private static final int UNLOCK = 3;
    private static final int LOCK = 4;
    private static final int SET_PREVIEW_TEXTURE_ASYNC = 5;
    private static final int START_PREVIEW_ASYNC = 6;
    private static final int STOP_PREVIEW = 7;
    private static final int GET_PARAMETERS = 8;
    private static final int SET_PARAMETERS = 9;
    private static final int AUTO_FOCUS = 10;
    private static final int CANCEL_AUTO_FOCUS = 11;
    private static final int SET_DISPLAY_ORIENTATION = 12;
    
    private static final int OPEN_RETRY_COUNT = 5;
    
    private Handler mCameraHandler;
    private Camera mCamera;
    
    
    public static CameraManager instance() {
        return sCameraManager;
    }
    
    private CameraManager() {
        HandlerThread ht = new HandlerThread("Camera Handler Thread");
        ht.start();
        mCameraHandler = new CameraHandler(ht.getLooper());
        
        mCameraState = PREVIEW_STOPPED;
        mCameraId = -1;
    }
    
    
    @SuppressLint("HandlerLeak")
	private class CameraHandler extends Handler {
    	CameraHandler(Looper looper) {
            super(looper);
        }

    	@SuppressLint("NewApi")
		@Override
        public void handleMessage(final Message msg) {
            if (mCamera == null) {
                Log.e(TAG, "handleMessage msg.what = " + msg.what);
                Log.e(TAG, "handleMessage mCamera = " + mCamera);
            }
            try {
                switch (msg.what) {
	                case RELEASE:
	                	if (mCamera != null) {
	                    	mCamera.release();
	                    	mCamera = null;
	                    }
	                	break;
	                	
	                case RECONNECT:
	                	mReconnectException = null;
	                    if (mCamera != null) {
	                    	try {
	                    		mCamera.reconnect();
	                    	} catch (IOException ex) {
	                    		mReconnectException = ex;
	                    	}
	                    }
	                	break;
	                	
	                case UNLOCK:
	                	if (mCamera != null) {
	                    	mCamera.unlock();
	                    }
	                	break;
	                	
	                case LOCK:
	                	if (mCamera != null) {
	                    	mCamera.lock();
	                    }
	                	break;
	                	
	                case SET_PREVIEW_TEXTURE_ASYNC:
	                	if (mCamera != null) {
	                		try {
	                			mCamera.setPreviewTexture((SurfaceTexture) msg.obj);
	                		} catch (IOException e) {
	                			throw new RuntimeException(e);
	                		}
	                    }
	                	return;	 // no need to call mSig.open()
	                	
	                case START_PREVIEW_ASYNC:
	                	if (mCamera != null) {
	                    	mCamera.startPreview();
	                    }
	                	return;	 // no need to call mSig.open()
	                	
	                case STOP_PREVIEW:
	                	if (mCamera != null) {
	                    	mCamera.stopPreview();
	                    }
	                	break;
	                	
	                case GET_PARAMETERS:
	                	if (mCamera != null) {
	                    	mParameters = mCamera.getParameters();
	                    }
	                	break;
	                	
	                case SET_PARAMETERS:
	                	if (mCamera != null) {
	                		try {
	                			mCamera.setParameters((Parameters) msg.obj);
	                		} catch (RuntimeException e) {
	                			Log.e(TAG, "SET_PARAMETERS Fail e = " + e);
	                		}
	                    }
	                	break;
	                	
	                case AUTO_FOCUS:
	                	mAutoFocusException = null;
	                	if (mCamera != null) {
	                		try {
	                			mCamera.autoFocus((AutoFocusCallback) msg.obj);
	                		} catch (RuntimeException e) {
	                			mAutoFocusException = e;
	                		}
	                    }
	                	break;
	                	
	                case CANCEL_AUTO_FOCUS:
	                	if (mCamera != null) {
	                    	mCamera.cancelAutoFocus();
	                    }
	                	break;
	                	
	                case SET_DISPLAY_ORIENTATION:
	                	if (mCamera != null) {
	                    	mCamera.setDisplayOrientation(msg.arg1);
	                    }
	                	break;
	                	
                	default:
                		throw new RuntimeException("Invalid Camera message=" + msg.what);
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "RuntimeException e = " + e);
                if (msg.what != RELEASE && mCamera != null) {
                    try {
                        mCamera.release();
                    } catch (Exception ex) {
                        Log.e(TAG, "Fail to release the camera.");
                    }
                    mCamera = null;
                }
                throw e;
            }
            mSig.open();
        }
    }

    public static final int PREVIEW_STOPPED = 0;
    public static final int IDLE = 1;
    public static final int FOCUSING = 2;
    public static final int SNAPSHOT_IN_PROGRESS = 2;
    public static final int FOCUSING_SNAP_ON_FINISH = 4;
    
    private int mCameraDisplayOrientation = 0;
    
    private int mCameraId = -1;
    private boolean mCameraOpened = false;
    private volatile int mCameraState = PREVIEW_STOPPED;
    

    private void dumpPictureSize() {
    }

    private void dumpPreviewSize() {
    }

    private void stopPreviewInner() {
        if (mCameraState != PREVIEW_STOPPED) {
            stopPreview();
        }
        setCameraState(PREVIEW_STOPPED);
    }

    private RuntimeException mAutoFocusException;
    public void autoFocus(AutoFocusCallback cb) {
        mSig.close();
        mCameraHandler.obtainMessage(AUTO_FOCUS, cb).sendToTarget();
        mSig.block();
        if (mAutoFocusException != null) {
        	throw mAutoFocusException;
        }
    }
    
    public void cancelAutoFocus() {
        mSig.close();
        mCameraHandler.sendEmptyMessage(CANCEL_AUTO_FOCUS);
        mSig.block();
    }

    public boolean cameraIsOpened() {
        return mCameraOpened;
    }

    public Camera cameraOpen(int cameraId) {
        try {
            mCamera = Camera.open(cameraId);
            return mCamera;
        } catch (Exception e) {
            return null;
        }
    }

    public void chechCameraDisplayOrientation(int orientation) {
        mCameraDisplayOrientation = orientation;
        setDisplayOrientation(mCameraDisplayOrientation);
    }

    public void closeCamera() {
        stopPreviewInner();
        release();
        mParameters = null;
        mCameraId = -1;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public int getCameraState() {
        return mCameraState;
    }

    public Parameters getParameters() {
        mSig.close();
        mCameraHandler.sendEmptyMessage(GET_PARAMETERS);
        mSig.block();
        return mParameters;
    }

    public void lock() {
        mSig.close();
        mCameraHandler.sendEmptyMessage(LOCK);
        mSig.block();
    }
    
    public void unlock() {
        mSig.close();
        mCameraHandler.sendEmptyMessage(UNLOCK);
        mSig.block();
    }
    
    public void setDisplayOrientation(int degree) {
        mSig.close();
        mCameraHandler.obtainMessage(SET_DISPLAY_ORIENTATION, degree, 0)
        		.sendToTarget();
        mSig.block();
    }

    public void setParameters(Parameters params) {
        mSig.close();
        mCameraHandler.obtainMessage(SET_PARAMETERS, params).sendToTarget();
        mSig.block();
    }

    public void setPreviewTextureAsync(final SurfaceTexture surfaceTexture) {
        mCameraHandler.obtainMessage(SET_PREVIEW_TEXTURE_ASYNC, surfaceTexture).sendToTarget();
    }

    public void reconnect() throws IOException {
        mSig.close();
        mCameraHandler.sendEmptyMessage(RECONNECT);
        mSig.block();
        if (mReconnectException != null) {
            throw mReconnectException;
        }
    }

    public void release() {
        mSig.close();
        mCameraHandler.sendEmptyMessage(RELEASE);
        mSig.block();
        
        mCameraOpened = false;
    }

    public void setCameraState(int state) {
        mCameraState = state;
        switch (state) {
	        case PREVIEW_STOPPED: 
	        case IDLE: 
	        case FOCUSING: 
	        case FOCUSING_SNAP_ON_FINISH:
	        default:
	            break;
        }
    }
    
    public boolean startCamera(SurfaceTexture surface, int cameraId) {
        CameraHolder holder = CameraHolder.instance();
        if (-1 == cameraId) {
        	holder.init();
        	cameraId = getDefaultCameraId();
        }
        
        if ((-1 == cameraId) || tryOpen(cameraId) == null) {
            return false;
        }

        startPreview(surface);
        return true;
    }
    
    public boolean openCamera(int cameraId) {
        CameraHolder holder = CameraHolder.instance();
        if (-1 == cameraId) {
        	holder.init();
        	cameraId = getDefaultCameraId();
        }
        
        if ((-1 == cameraId) || tryOpen(cameraId) == null) {
            return false;
        }

        return true;
    }
    
    public int getCurrentCameraId() {
    	return mCameraId;
    }
    
    public int getDefaultCameraId() {
    	final CameraHolder holder = CameraHolder.instance();
    	int cameraId = -1;
    	// first use BACK camera, or then use FRONT camera
    	if ((cameraId = holder.getBackCameraId()) != -1) {
    		return cameraId;
    	} else if ((cameraId = holder.getFrontCameraId()) != -1) {
    		return cameraId;
    	}
    	return cameraId;
    }
    
    public boolean canSwitchCamera() {
    	CameraHolder holder = CameraHolder.instance();
    	return holder.getBackCameraId() != -1 && holder.getFrontCameraId() != -1;
    }
    
    public int getSwitchCameraId() {
    	CameraHolder holder = CameraHolder.instance();
    	
    	int targetCameraId = -1;
    	if (mCameraId == holder.getBackCameraId()) {
    		targetCameraId = holder.getFrontCameraId();
    	} else if (mCameraId == holder.getFrontCameraId()) {
    		targetCameraId = holder.getBackCameraId();
    	}
    	return targetCameraId;
    }
    
    public void startPreview(SurfaceTexture surface) {
        if (mCamera == null) {
            return;
        }
        stopPreviewInner();
        
        dumpPreviewSize();
        dumpPictureSize();
        
        mCamera.setDisplayOrientation(mCameraDisplayOrientation);
        
        //mParameters = mCamera.getParameters();
        // +++ FIXME: 
        //mParameters.setCameraMode(Parameters.CAMERA_MODE_MTK_PRV);
        // ---
        //Camera.Size previewSize = Util.getSuitablePreviewSizeFromPicture(mParameters);
        //Camera.Size pictureSize = Util.getSuitablePictureSize(mParameters);
        //Camera.Size previewSize = Util.getSuitablePreviewSizeEx(mParameters);
        //Camera.Size pictureSize = Util.getSuitablePictureSizeEx(mParameters);
        //mParameters.setPreviewSize(previewSize.width, previewSize.height);
        //mParameters.setPictureSize(pictureSize.width, pictureSize.height);
        
        //mParameters.setCameraMode(Camera.Parameters.CAMERA_MODE_MTK_PRV);
        
        //setParameters(mParameters);
        
        setPreviewTextureAsync(surface);
        startPreviewAsync();
        setCameraState(IDLE);
        
        //mParameters.getPreviewSize();
        //mParameters.getPictureSize();
    }

    public void startPreviewAsync() {
        mCameraHandler.sendEmptyMessage(START_PREVIEW_ASYNC);
    }

    public void stopPreview() {
        mSig.close();
        mCameraHandler.sendEmptyMessage(STOP_PREVIEW);
        mSig.block();
    }

    public void takePicture(final ShutterCallback shutter, final PictureCallback raw, 
    		final PictureCallback postview, final PictureCallback jpeg) {
        mSig.close();
        // Too many parameters, so use post for simplicity
        mCameraHandler.post(new Runnable() {
        	@Override
            public void run() {
                mCamera.takePicture(shutter, raw, postview, jpeg);
                mSig.open();
            }
        });
        mSig.block();
    }
    
	public synchronized CameraManager open(int cameraId) throws Exception {
		CameraUtil.Assert(!mCameraOpened);
    	if (-1 != mCameraId && mCameraId != cameraId) {
            release();
            mCameraId = -1;
        }
    	if (mCameraId == -1) {
    		try {
	    		mCamera = cameraOpen(cameraId);
	    		if (mCamera == null) {
	    			return null;
	    		}
	    		mCameraId = cameraId;
    		} catch (RuntimeException e) {
    			Log.e(TAG, "fail to connect Camera", e);
    			throw new Exception(e);
    		}
    		mParameters = getParameters();
    	} else {
    		Log.v(TAG, "reconnect camera ");
    		try {
	            reconnect();
    		} catch (IOException e) {
    			Log.e(TAG, "reconnect failed.");
    			throw new Exception(e);
    		}
            setParameters(mParameters);
    	}
    	mCameraOpened = true;
        return this;
    }
    
    /**
     * Tries to open the hardware camera. If the camera is being used or
     * unavailable then return {@code null}.
     */
    public synchronized CameraManager tryOpen(int cameraId) {
    	for (int i = 0; i < OPEN_RETRY_COUNT; ++i) {
	        try {
	        	return !mCameraOpened ? open(cameraId) : null;
	        } catch (Exception e) {
	        	if (i == 0) {
	        		try {
	        			//wait some time, and try another time
	        			//Camera device may be using VT or atv.
	        			Thread.sleep(1000);
	        		} catch (InterruptedException ie) {
	        		}
	        		continue;
	        	} else {
		        	// In eng build, we throw the exception so that test tool
		        	// can detect it and report it
		        	if ("eng".equals(Build.TYPE)) {
		        		throw new RuntimeException(e);
		        	}
		        	return null;
	        	}
	        }
    	}
    	// just for build pass
    	throw new RuntimeException("Should nerver get here");
    }
}

package ageing.fise.com.fiseageingtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FiseAgeingActivity extends AppCompatActivity implements View.OnClickListener {
    Button lcdBtn;
    Button speakerBtn;
    Button vibrateBtn;
    Button cameraBtn;
    Button start;
    Button stop;
    //Button mTakePicture;
    //Button mStopCapture;
    AgeingHandler mHandler;
    Vibrator mVibrator;
    LinearLayout mTestGroup;
    LinearLayout mTestGroup_2;
    //LinearLayout mTestOperation;
   // LinearLayout takeLinearLayout;
    TextView mTextViewLCD;
    final static int MSG_TEST_START = 0;
    final static int MSG_TEST_STOP = 1;
    final static int MSG_LCD_RED = 2;
    final static int MSG_LCD_GREEN = 3;
    final static int MSG_LCD_BLUE = 4;
    final static int MSG_LCD_FINISH = 5;
    final static int MSG_SWITCH_CAMERA = 6;
    final static int MSG_BEGIN_CAMERA_TEST = 400;
    final static int MSG_STOP_CAMERA_TEST = 401;
    boolean isNeedContinueRun = false;
    boolean isNeedContinueLcd = false;
    MediaPlayer mMediaPlayer;
    SurfaceView mCameraView;
    SurfaceHolder mSurfaceHolder;
    private RawPreviewCallback mRawPreviewCallback = new RawPreviewCallback();
    private boolean rawPreviewCallbackResult = false;
    //CameraManager mCameraManager;
    Camera mCamera;
    private boolean mCameraTaking = false;
    private boolean mCanTakePicture = true;
    private boolean isCaptureOk = true;
    private boolean mbPaused = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fise_ageing);
        initBtn();
        mHandler = new AgeingHandler();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mbPaused = true;
    }

    private void initBtn() {
        lcdBtn = (Button) findViewById(R.id.test_lcd);
        lcdBtn.setOnClickListener(this);
        speakerBtn = (Button) findViewById(R.id.test_speaker);
        speakerBtn.setOnClickListener(this);
        cameraBtn = (Button) findViewById(R.id.test_camera);
        cameraBtn.setOnClickListener(this);
        vibrateBtn = (Button) findViewById(R.id.test_vibrate);
        vibrateBtn.setOnClickListener(this);
        start = (Button) findViewById(R.id.start);
        start.setOnClickListener(this);
        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(this);

        mTestGroup = (LinearLayout) findViewById(R.id.test_group);
        mTestGroup_2 = (LinearLayout) findViewById(R.id.test_group_2);
        mTextViewLCD = (TextView) findViewById(R.id.lcd_show);
        mCameraView = (SurfaceView) findViewById(R.id.camera_preview);
        mSurfaceHolder = mCameraView.getHolder();
        mSurfaceHolder.addCallback(surfaceCallback);


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.test_lcd:
                startTestLCD();
                break;
            case R.id.test_vibrate:
                startTestVibrate();
                break;
            case R.id.test_speaker:
                startTestSpeaker();
                break;
            case R.id.test_camera:
                showCameraView();
                capture();
                break;
            case R.id.start:
                isNeedContinueRun = true;
                isNeedContinueLcd = true;
                mHandler.sendEmptyMessage(MSG_TEST_START);
                break;
            case R.id.stop:
                isNeedContinueRun = false;
                isNeedContinueLcd = false;
                showNormal();
                mHandler.removeCallbacks(cameraRunnable);
                closeCamera();
                mHandler.sendEmptyMessage(MSG_TEST_STOP);
                break;
            default:
                break;
        }
    }
    private void showNormal(){
        mCameraView.setVisibility(View.GONE);
        mTextViewLCD.setVisibility(View.GONE);
        mTestGroup_2.setVisibility(View.VISIBLE);
        mTestGroup.setVisibility(View.VISIBLE);
    }
    private void showCameraView(){
        mTestGroup_2.setVisibility(View.GONE);
        mTestGroup.setVisibility(View.GONE);
        mTextViewLCD.setVisibility(View.GONE);
        mCameraView.setVisibility(View.VISIBLE);
    }
    private  void startTestLCD() {
        mTestGroup.setVisibility(View.GONE);
        mTestGroup_2.setVisibility(View.GONE);
        mCameraView.setVisibility(View.GONE);
        mTextViewLCD.setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessageDelayed(MSG_LCD_GREEN, 1000);
    }
    private void initCamera(){
        mCamera = Camera.open();
        mCamera.setPreviewCallback(mRawPreviewCallback);
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
            return;
        }
        mCamera.setDisplayOrientation(90);
        mCamera.startPreview();
    }
    private void closeCamera(){
        if (mCamera != null) {
            mCamera.stopPreview();
            mHandler.removeCallbacks(cameraRunnable);
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }
    Runnable cameraRunnable = new Runnable() {
        @Override
        public void run() {
            initCamera();
        }
    };
    public void capture() {
        Log.d("fengqing","capture().....");
        if(isNeedContinueRun){
            mHandler.sendEmptyMessageDelayed(MSG_BEGIN_CAMERA_TEST,1000);
        }else {
            mHandler.sendEmptyMessage(MSG_BEGIN_CAMERA_TEST);
        }
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private final class RawPreviewCallback implements Camera.PreviewCallback {
        public void onPreviewFrame(byte[] rawData, Camera camera) {
            final PictureCallback jpeg = new PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    // TODO Auto-generated method stub
                    mCamera.stopPreview();
                    mCamera.startPreview();
                    //wangfeng add for bug132669 20161011
                    isCaptureOk = true;
                }
            };
           /* if (isCaptureOk) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                mCamera.setParameters(parameters);
               // mCamera.takePicture(null, null, jpeg);
                //wangfeng add for bug132669 20161011
                isCaptureOk = false;
            }*/
        }
    }

    class AgeingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d("fengqing","msg.what =" + msg.what);
            switch (msg.what) {
                case MSG_LCD_GREEN:
                    mTextViewLCD.setBackgroundColor(Color.GREEN);
                    mHandler.sendEmptyMessageDelayed(MSG_LCD_BLUE, 1000);
                    break;
                case MSG_LCD_BLUE:
                    mTextViewLCD.setBackgroundColor(Color.BLUE);
                    mHandler.sendEmptyMessageDelayed(MSG_LCD_RED, 1000);
                    break;
                case MSG_LCD_RED:
                    mTextViewLCD.setBackgroundColor(Color.RED);
                    if (isNeedContinueRun) {
                        mHandler.sendEmptyMessageDelayed(MSG_LCD_GREEN, 1000);
                    } else {
                        mHandler.sendEmptyMessageDelayed(MSG_LCD_FINISH, 1000);
                    }
                    break;
                case MSG_LCD_FINISH:
                    showNormal();
                    break;
                case MSG_TEST_START:
                    if(isNeedContinueRun){
                        mHandler.removeCallbacks(cameraRunnable);
                        closeCamera();
                    }
                    startAllTest();
                    mHandler.sendEmptyMessageDelayed(MSG_SWITCH_CAMERA, 60000);
                    break;
                case MSG_TEST_STOP:
                    mHandler.removeMessages(MSG_TEST_START);
                    mHandler.removeMessages(MSG_SWITCH_CAMERA);
                    stopAllTest();
                    break;
                case MSG_BEGIN_CAMERA_TEST:
                    mHandler.post(cameraRunnable);
                    break;
                case  MSG_STOP_CAMERA_TEST:
                    showNormal();
                    mHandler.removeCallbacks(cameraRunnable);
                    closeCamera();
                    break;
                case MSG_SWITCH_CAMERA:
                    stopAllTest();
                    showCameraView();
                    isNeedContinueLcd = false;
                    capture();
                    if (isNeedContinueRun) {
                        mHandler.sendEmptyMessageDelayed(MSG_TEST_START, 60000);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void startAllTest() {
        isNeedContinueLcd = true;
        isNeedContinueRun = true;
        startTestLCD();
        startTestVibrate();
        startTestSpeaker();
    }

    private void stopAllTest() {
        isNeedContinueLcd = false;
        if (mVibrator != null) {
            mVibrator.cancel();
            mVibrator = null;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isNeedContinueRun = false;
        isNeedContinueLcd = false;
        if (mVibrator != null) {
            mVibrator.cancel();
            mVibrator = null;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }


    void startTestSpeaker() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.earphone);
        }
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }


    void startTestVibrate() {
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {400, 800, 400, 800};
        if (mVibrator != null) {
            mVibrator.vibrate(pattern, 2);
        }
    }


}

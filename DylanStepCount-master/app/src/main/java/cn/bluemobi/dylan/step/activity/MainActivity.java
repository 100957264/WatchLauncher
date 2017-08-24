package cn.bluemobi.dylan.step.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.util.List;

import cn.bluemobi.dylan.step.R;
import cn.bluemobi.dylan.step.step.UpdateUiCallBack;
import cn.bluemobi.dylan.step.step.bean.StepData;
import cn.bluemobi.dylan.step.step.service.StepService;
import cn.bluemobi.dylan.step.step.utils.DbUtils;
import cn.bluemobi.dylan.step.step.utils.SharedPreferencesUtils;
import cn.bluemobi.dylan.step.view.StepArcView;

/**
 * 记步主页
 */
public class MainActivity extends AppCompatActivity {
    private StepArcView cc;
    private SharedPreferencesUtils sp;
    //GestureDetector mGestureDetector;
    //MyGestureListener mGestureListener;
    long[] mHits = new long[5];
    RelativeLayout main;
    int i = 0;
    long previousTime =0;
    private void assignViews() {
        cc = (StepArcView) findViewById(R.id.cc);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        assignViews();
        initData();
    }

    private void initData() {
        sp = new SharedPreferencesUtils(this);
        //获取用户设置的计划锻炼步数，没有设置过的话默认10000
        String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "10000");
        main = (RelativeLayout) findViewById(R.id.main);
        //设置当前步数为0
        cc.setCurrentCount(Integer.parseInt(planWalk_QTY), 0);
        setupService();

        main.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("StepCount","onclick =" + i);
                long currentTime =System.currentTimeMillis();
                Log.d("StepCount","onclick =" + i + ",currentTime =" + currentTime + ",previousTime" + previousTime);
                    if((currentTime - previousTime)< 500 ){
                        i ++;
                    } else {
                        i = 0;
                        previousTime =0;
                    }
                previousTime = currentTime;
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();// 开机后运行时间
                if (i == 5) {
                    startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                    i = 0;
                    previousTime = 0;
                }
            }
        });
        //mGestureListener = new MyGestureListener();
       // mGestureDetector = new  GestureDetector(this,mGestureListener);

    }


    private boolean isBind = false;
    class MyGestureListener implements  GestureDetector.OnGestureListener{
        final int FLING_MIN_DISTANCE = 50, FLING_MIN_VELOCITY = 50;
        @Override
        public boolean onDown(MotionEvent e) {
            System. arraycopy(mHits, 1, mHits, 0, mHits.length-1);
            mHits[ mHits. length-1] = SystemClock. uptimeMillis();
            if(mHits[0] >= (SystemClock. uptimeMillis()-500)){
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            }
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
                    && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                // Fling left
              //  startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
                    && Math.abs(velocityX) > FLING_MIN_VELOCITY) {

            }

            return true;
        }
    }



    /**
     * 开启计步服务
     */
    private void setupService() {
        Intent intent = new Intent(this, StepService.class);
        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    ServiceConnection conn = new ServiceConnection() {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StepService stepService = ((StepService.StepBinder) service).getService();
            //设置初始化数据
            String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "10000");
            int todaySteps = 0;
            //cc.setCurrentCount(Integer.parseInt(planWalk_QTY), stepService.getStepCount());
            List<StepData> list = DbUtils.getQueryByWhere(StepData.class, "today", new String[]{stepService.getTodayDate()});
            if (list.size() == 0 || list.isEmpty()) {
                todaySteps = 0;
            } else if (list.size() == 1) {
                todaySteps = Integer.parseInt(list.get(0).getStep());
                Log.v("steps", "todaySteps " + todaySteps);
            } else {
                Log.v("steps", "出错了！");
            }
            cc.setCurrentCount(Integer.parseInt(planWalk_QTY), todaySteps);

            //设置步数监听回调
            stepService.registerCallback(new UpdateUiCallBack() {
                @Override
                public void updateUi(int stepCount) {
                    String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "10000");
                    cc.setCurrentCount(Integer.parseInt(planWalk_QTY), stepCount);
                }
            });
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBind) {
            this.unbindService(conn);
        }
    }
}

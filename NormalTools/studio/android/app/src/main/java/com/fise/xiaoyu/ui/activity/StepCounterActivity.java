package com.fise.xiaoyu.ui.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.fise.xiaoyu.DB.entity.DeviceEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.adapter.DateAdapter;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.SpecialCalendar;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 计步界面
 */
public class StepCounterActivity extends TTBaseActivity implements GestureDetector.OnGestureListener {

    private Logger logger = Logger.getLogger(AboutActivity.class);
    private Handler uiHandler = new Handler();
    private IMService imService;



    private ViewFlipper flipper1 = null;
    // private ViewFlipper flipper2 = null;
    private static String TAG = "ZzL";
    private GridView gridView = null;
    private GestureDetector gestureDetector = null;
    private int year_c = 0;
    private int month_c = 0;
    private int day_c = 0;
    private int week_c = 0;
    private int week_num = 0;
    private String currentDate = "";
    private static int jumpWeek = 0;
    private static int jumpMonth = 0;
    private static int jumpYear = 0;
    private DateAdapter dateAdapter;
    private int daysOfMonth = 0; // 某月的天数
    private int dayOfWeek = 0; // 具体某一天是星期几
    private int weeksOfMonth = 0;
    private SpecialCalendar sc = null;
    private boolean isLeapyear = false; // 是否为闰年
    private int selectPostion = 0;
    private String dayNumbers[] = new String[7];
    private TextView tvDate;
    private int currentYear;
    private int currentMonth;
    private int currentWeek;
    private int currentDay;
    private int currentNum;
    private boolean isStart;// 是否是交接的月初

    private float step_cnt;
    private String step_date ;
    private UserEntity currentUser;
    private int currentUserId;
    private DeviceEntity rsp;

    private int year;
    private int month;
    private int day;



    private float weight;
    private float height;

    private double  energy;
    private double mileage;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            logger.d("login#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            try {
                do {
                    if (imService == null) {
                        // 后台服务启动链接失败
                        break;
                    }
                    currentUser = imService.getContactManager().findDeviceContact(
                            currentUserId);
                    if (currentUser == null) {
                        return;
                    }

                    rsp = imService.getDeviceManager().findDeviceCard(currentUserId);
                    if (rsp == null) {
                        return;
                    }


                    if(currentUser.getHeight() ==0){
                        height = 85;
                    }else{
                        height = currentUser.getHeight();
                    }

                    if(currentUser.getWeight() ==0){
                        weight = 30;
                    }else{
                        weight = currentUser.getWeight();
                    }



                    String monthString ;
                    if(month<10){
                        monthString = "0" + month;
                    }else{
                        monthString =  "" + month;
                    }

                    String dayString;
                    if( day<10){
                        dayString = "0" + day;
                    }else{
                        dayString =  "" + day;
                    }

                    String sendStepDate = year  + monthString  + dayString ;
                    imService.getUserActionManager().stepRequest(imService.getLoginManager().getLoginId(),currentUserId,sendStepDate);


                    initDetailProfile();
                    return;
                } while (false);

                // 异常分支都会执行这个
                // handleNoLoginIdentity();
            } catch (Exception e) {
                // 任何未知的异常
                logger.w("loadIdentity failed");
                // handleNoLoginIdentity();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        imServiceConnector.connect(StepCounterActivity.this);

        setContentView(R.layout.tt_activity_step_counter);
        Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
        icon_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                StepCounterActivity.this.finish();
            }
        });

        currentUserId = StepCounterActivity.this.getIntent().getIntExtra(
                IntentConstant.KEY_PEERID, 0);

        TextView left_text = (TextView) findViewById(R.id.left_text);
        left_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                StepCounterActivity.this.finish();
            }
        });

        Calendar mycalendar = Calendar.getInstance();
        year = mycalendar.get(Calendar.YEAR); //获取Calendar对象中的年
        month = mycalendar.get(Calendar.MONTH) + 1;//获取Calendar对象中的月
        day = mycalendar.get(Calendar.DAY_OF_MONTH);//



        tvDate = (TextView) findViewById(R.id.tv_date);

        Calendar c = Calendar.getInstance();
        c.setTime(new Date(System.currentTimeMillis()));
        int dayOfWeekTest = c.get(Calendar.DAY_OF_WEEK);
        tvDate.setText(year_c + "年" + month_c + "月" + day_c + "日" + "  " +  getDayOfWeekTest(dayOfWeekTest));

        gestureDetector = new GestureDetector(this);
        flipper1 = (ViewFlipper) findViewById(R.id.flipper1);
        dateAdapter = new DateAdapter(this, getResources(), currentYear,
                currentMonth, currentWeek, currentNum, selectPostion,
                currentWeek == 1 ? true : false);
        addGridView();
        dayNumbers = dateAdapter.getDayNumbers();
        gridView.setAdapter(dateAdapter);
        selectPostion = dateAdapter.getTodayPosition();
        gridView.setSelection(selectPostion);
        flipper1.addView(gridView, 0);


    }


    private void initDetailProfile() {

        //公里
//        DecimalFormat decimalFormat=new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
//        String mileageString = decimalFormat.format(mileage);//format 返回的是字符串
        String mileageString;
        if(mileage>1.0){
            DecimalFormat fnum = new DecimalFormat("##0.0");
             mileageString = fnum.format(mileage);
        }else{
            DecimalFormat fnum = new DecimalFormat("##0.00");
             mileageString = fnum.format(mileage);
        }



        TextView kilometre_text = (TextView) findViewById(R.id.kilometre_text);
        kilometre_text.setText(""+mileageString);

        TextView kilometre_time_text = (TextView) findViewById(R.id.kilometre_time_text);
        kilometre_time_text.setText(step_date);


        //步数
        int step_cnt_num = (int) step_cnt;
        TextView setp_number_text = (TextView) findViewById(R.id.setp_number_text);
        setp_number_text.setText(""+step_cnt_num);

        TextView setp_number_time_text = (TextView) findViewById(R.id.setp_number_time_text);
        setp_number_time_text.setText(step_date);


        //卡路里

        String energyString;
        if(energy>1.0){
            DecimalFormat energyNum = new DecimalFormat("##0.0");
            energyString = energyNum.format(energy);
        }else{
            DecimalFormat energyNum = new DecimalFormat("##0.00");
            energyString = energyNum.format(energy);
        }

        TextView calorie_text = (TextView) findViewById(R.id.calorie_text);
        calorie_text.setText(""+energyString);

        TextView calorie_time_text = (TextView) findViewById(R.id.calorie_time_text);
        calorie_time_text.setText(step_date);

    }


    public String getDayOfWeekTest(int dayOfWeek){

        if(dayOfWeek == 1){
            return "星期日";
        }else if(dayOfWeek == 2){
            return "星期一";
        }else if(dayOfWeek == 3){
            return "星期二";
        }else if(dayOfWeek == 4){
            return "星期三";
        }else if(dayOfWeek == 5){
            return "星期四";
        }else if(dayOfWeek == 6){
            return "星期五";
        }else if(dayOfWeek == 7){
            return "星期六";
        }
        return  "";
    }

public String getDayOfWeek(int day){

    if(day == 1){
        return "星期一";
    }else if(day == 2){
        return "星期二";
    }else if(day == 3){
        return "星期三";
    }else if(day == 4){
        return "星期四";
    }else if(day == 5){
        return "星期五";
    }else if(day == 6){
        return "星期六";
    }else if(day == 7){
        return "星期日";
    }
    return  "";
}
    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(StepCounterActivity.this);
            }

    // 为什么会有两个这个
    // 可能是 兼容性的问题 导致两种方法onBackPressed
    @Override
    public void onBackPressed() {
        logger.d("login#onBackPressed");
        // imLoginMgr.cancel();
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * ----------------------------event 事件驱动----------------------------
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event) {
        switch (event) {

            case USER_STEP_SUCCESS:
                step_cnt = imService.getUserActionManager().getStepCnt();
                step_date = imService.getUserActionManager().getStepdate();
                energy =  ((weight/2000))*step_cnt;
                mileage =  ((height*0.45 * step_cnt)/100)/1000;
                initDetailProfile();
                break;
            case USER_STEP_FAIL:
                Utils.showToast(StepCounterActivity.this,"运动查询失败");
                break;

        }
    }


    public StepCounterActivity() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
        currentDate = sdf.format(date);
        year_c = Integer.parseInt(currentDate.split("-")[0]);
        month_c = Integer.parseInt(currentDate.split("-")[1]);
        day_c = Integer.parseInt(currentDate.split("-")[2]);
        currentYear = year_c;
        currentMonth = month_c;
        currentDay = day_c;
        sc = new SpecialCalendar();
        getCalendar(year_c, month_c);
        week_num = getWeeksOfMonth();
        currentNum = week_num;
        if (dayOfWeek == 7) {
            week_c = day_c / 7 + 1;
        } else {
            if (day_c <= (7 - dayOfWeek)) {
                week_c = 1;
            } else {
                if ((day_c - (7 - dayOfWeek)) % 7 == 0) {
                    week_c = (day_c - (7 - dayOfWeek)) / 7 + 1;
                } else {
                    week_c = (day_c - (7 - dayOfWeek)) / 7 + 2;
                }
            }
        }
        currentWeek = week_c;
        getCurrent();

    }

    /**
     * 判断某年某月所有的星期数
     *
     * @param year
     * @param month
     */
    public int getWeeksOfMonth(int year, int month) {
        // 先判断某月的第一天为星期几
        int preMonthRelax = 0;
        int dayFirst = getWhichDayOfWeek(year, month);
        int days = sc.getDaysOfMonth(sc.isLeapYear(year), month);
        if (dayFirst != 7) {
            preMonthRelax = dayFirst;
        }
        if ((days + preMonthRelax) % 7 == 0) {
            weeksOfMonth = (days + preMonthRelax) / 7;
        } else {
            weeksOfMonth = (days + preMonthRelax) / 7 + 1;
        }
        return weeksOfMonth;

    }

    /**
     * 判断某年某月的第一天为星期几
     *
     * @param year
     * @param month
     * @return
     */
    public int getWhichDayOfWeek(int year, int month) {
        return sc.getWeekdayOfMonth(year, month);

    }

    /**
     *
     * @param year
     * @param month
     */
    public int getLastDayOfWeek(int year, int month) {
        return sc.getWeekDayOfLastMonth(year, month,
                sc.getDaysOfMonth(isLeapyear, month));
    }

    public void getCalendar(int year, int month) {
        isLeapyear = sc.isLeapYear(year); // 是否为闰年
        daysOfMonth = sc.getDaysOfMonth(isLeapyear, month); // 某月的总天数
        dayOfWeek = sc.getWeekdayOfMonth(year, month); // 某月第一天为星期几
    }

    public int getWeeksOfMonth() {
        // getCalendar(year, month);
        int preMonthRelax = 0;
        if (dayOfWeek != 7) {
            preMonthRelax = dayOfWeek;
        }
        if ((daysOfMonth + preMonthRelax) % 7 == 0) {
            weeksOfMonth = (daysOfMonth + preMonthRelax) / 7;
        } else {
            weeksOfMonth = (daysOfMonth + preMonthRelax) / 7 + 1;
        }
        return weeksOfMonth;
    }


    private void addGridView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        gridView = new GridView(this);
        gridView.setNumColumns(7);
        gridView.setGravity(Gravity.CENTER_VERTICAL);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gridView.setVerticalSpacing(1);
        gridView.setHorizontalSpacing(1);
        gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return StepCounterActivity.this.gestureDetector.onTouchEvent(event);
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.i(TAG, "day:" + dayNumbers[position]);

                if(dateAdapter.getCurrentYear(position) >year ){
                    Utils.showToast(StepCounterActivity.this,"选择日期超过当前日期");
                    return ;
                }else  {

                    if(dateAdapter.getCurrentYear(position)  == year){
                        if(dateAdapter.getCurrentMonth(position) >month ){
                            Utils.showToast(StepCounterActivity.this,"选择日期超过当前日期");
                            return ;
                        }else  if(dateAdapter.getCurrentMonth(position) == month ){
                            if(Integer.parseInt(dayNumbers[position]) >day ){
                                Utils.showToast(StepCounterActivity.this,"选择日期超过当前日期");
                                return ;
                            }
                        }
                    }
                }

                selectPostion = position;
                dateAdapter.setSeclection(position);
                dateAdapter.notifyDataSetChanged();


                tvDate.setText(dateAdapter.getCurrentYear(selectPostion) + "年"
                        + dateAdapter.getCurrentMonth(selectPostion) + "月"
                        + dayNumbers[position] + "日"  + " "   + getWeek(selectPostion) );


                String monthString ;
                if(dateAdapter.getCurrentMonth(selectPostion)<10){
                    monthString = "0" + dateAdapter.getCurrentMonth(selectPostion);
                }else{
                    monthString =  "" + dateAdapter.getCurrentMonth(selectPostion);
                }

                String dayString ;
                if( Integer.parseInt(dayNumbers[position])<10){
                    dayString = "0" + dayNumbers[position];
                }else{
                    dayString =  "" + dayNumbers[position];
                }

                String sendStepDate = dateAdapter.getCurrentYear(selectPostion)  + monthString  + dayString ;
                imService.getUserActionManager().stepRequest(imService.getLoginManager().getLoginId(),currentUserId,sendStepDate);

            }
        });
        gridView.setLayoutParams(params);
    }

    public String getWeek(int position){
        String week ="";
        if(position == 0){
            week = "星期天";
        }else if(position == 1){
            week = "星期一";
        }else if(position == 2){
            week = "星期二";
        }else if(position == 3){
            week = "星期三";
        }else if(position == 4){
            week = "星期四";
        }else if(position == 5){
            week = "星期五";
        }else if(position == 6){
            week = "星期六";
        }
        return  week;
    }

    @Override
    protected void onPause() {
        super.onPause();
        jumpWeek = 0;
    }

    @Override
    public boolean onDown(MotionEvent e) {

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
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    /**
     * 重新计算当前的年月
     */
    public void getCurrent() {
        if (currentWeek > currentNum) {
            if (currentMonth + 1 <= 12) {
                currentMonth++;
            } else {
                currentMonth = 1;
                currentYear++;
            }
            currentWeek = 1;
            currentNum = getWeeksOfMonth(currentYear, currentMonth);
        } else if (currentWeek == currentNum) {
            if (getLastDayOfWeek(currentYear, currentMonth) == 6) {
            } else {
                if (currentMonth + 1 <= 12) {
                    currentMonth++;
                } else {
                    currentMonth = 1;
                    currentYear++;
                }
                currentWeek = 1;
                currentNum = getWeeksOfMonth(currentYear, currentMonth);
            }

        } else if (currentWeek < 1) {
            if (currentMonth - 1 >= 1) {
                currentMonth--;
            } else {
                currentMonth = 12;
                currentYear--;
            }
            currentNum = getWeeksOfMonth(currentYear, currentMonth);
            currentWeek = currentNum - 1;
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        int gvFlag = 0;
        if (e1.getX() - e2.getX() > 80) {
            // 向左滑
            addGridView();
            currentWeek++;
            getCurrent();
            dateAdapter = new DateAdapter(this, getResources(), currentYear,
                    currentMonth, currentWeek, currentNum, selectPostion,
                    currentWeek == 1 ? true : false);
            dayNumbers = dateAdapter.getDayNumbers();
            gridView.setAdapter(dateAdapter);
            tvDate.setText(dateAdapter.getCurrentYear(selectPostion) + "年"
                    + dateAdapter.getCurrentMonth(selectPostion) + "月"
                    + dayNumbers[selectPostion] + "日" + " " + getWeek(selectPostion));
            gvFlag++;
            flipper1.addView(gridView, gvFlag);
            dateAdapter.setSeclection(selectPostion);
            this.flipper1.setInAnimation(AnimationUtils.loadAnimation(this,
                    R.anim.push_left_in));
            this.flipper1.setOutAnimation(AnimationUtils.loadAnimation(this,
                    R.anim.push_left_out));
            this.flipper1.showNext();
            flipper1.removeViewAt(0);
            return true;

        } else if (e1.getX() - e2.getX() < -80) {
            addGridView();
            currentWeek--;
            getCurrent();
            dateAdapter = new DateAdapter(this, getResources(), currentYear,
                    currentMonth, currentWeek, currentNum, selectPostion,
                    currentWeek == 1 ? true : false);
            dayNumbers = dateAdapter.getDayNumbers();
            gridView.setAdapter(dateAdapter);
            tvDate.setText(dateAdapter.getCurrentYear(selectPostion) + "年"
                    + dateAdapter.getCurrentMonth(selectPostion) + "月"
                    + dayNumbers[selectPostion] + "日" + " " + getWeek(selectPostion));
            gvFlag++;
            flipper1.addView(gridView, gvFlag);
            dateAdapter.setSeclection(selectPostion);
            this.flipper1.setInAnimation(AnimationUtils.loadAnimation(this,
                    R.anim.push_right_in));
            this.flipper1.setOutAnimation(AnimationUtils.loadAnimation(this,
                    R.anim.push_right_out));
            this.flipper1.showPrevious();
            flipper1.removeViewAt(0);
            return true;
            // }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.gestureDetector.onTouchEvent(event);
    }

}

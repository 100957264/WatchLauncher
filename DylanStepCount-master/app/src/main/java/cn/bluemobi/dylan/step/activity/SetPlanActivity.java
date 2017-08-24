package cn.bluemobi.dylan.step.activity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.preference.PreferenceActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.bluemobi.dylan.step.R;
import cn.bluemobi.dylan.step.step.utils.SharedPreferencesUtils;

/**
 * Created by yuandl on 2016-10-18.
 */

public class SetPlanActivity extends PreferenceActivity implements View.OnClickListener,Preference.OnPreferenceChangeListener {

    private SharedPreferencesUtils sp;

    private LinearLayout layout_titlebar;
    private ImageView iv_left;
    private ImageView iv_right;
    private EditText tv_step_number;
    private CheckBox cb_remind;
    private TextView tv_remind_time;
    private Button btn_save;
    private String walk_qty;
    private String remind;
    private String achieveTime;
    GestureDetector mGestureDetector;
    PlanGestureListener mGestureListener;
    private static final String BUTTON_4G_LTE_KEY = "enhanced_4g_lte";
    SwitchPreference mButton4glte;
    private void assignViews() {
        layout_titlebar = (LinearLayout) findViewById(R.id.layout_titlebar);
        iv_left = (ImageView) findViewById(R.id.iv_left);
        iv_right = (ImageView) findViewById(R.id.iv_right);
        tv_step_number = (EditText) findViewById(R.id.tv_step_number);
        cb_remind = (CheckBox) findViewById(R.id.cb_remind);
        tv_remind_time = (TextView) findViewById(R.id.tv_remind_time);
        btn_save = (Button) findViewById(R.id.btn_save);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ac_exercise_plan);
      //  addPreferencesFromResource(R.xml.fise_network_setting);
      // mButton4glte = (SwitchPreference) findPreference(BUTTON_4G_LTE_KEY);
      // mButton4glte.setOnPreferenceChangeListener(this);
        assignViews();
        initData();
        addListener();
        mGestureListener = new PlanGestureListener();
        mGestureDetector = new  GestureDetector(this,mGestureListener);
    }

    public void initData() {//获取锻炼计划
        sp = new SharedPreferencesUtils(this);
        String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "10000");
        String remind = (String) sp.getParam("remind", "1");
        String achieveTime = (String) sp.getParam("achieveTime", "20:00");
        if (!planWalk_QTY.isEmpty()) {
            if ("0".equals(planWalk_QTY)) {
                tv_step_number.setText("10000");
            } else {
                tv_step_number.setText(planWalk_QTY);
            }
        }
        if (!remind.isEmpty()) {
            if ("0".equals(remind)) {
                cb_remind.setChecked(false);
            } else if ("1".equals(remind)) {
                cb_remind.setChecked(true);
            }
        }

        if (!achieveTime.isEmpty()) {
            tv_remind_time.setText(achieveTime);
        }

    }


    public void addListener() {
        iv_left.setOnClickListener(this);
        iv_right.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        tv_remind_time.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left:
                finish();
                break;
            case R.id.btn_save:
                save();
                break;

            case R.id.tv_remind_time:
                showTimeDialog1();
                break;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    class PlanGestureListener implements  GestureDetector.OnGestureListener{
        final int FLING_MIN_DISTANCE = 50, FLING_MIN_VELOCITY = 50;
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


            } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
                    && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                finish();
            }

            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
    private void save() {
        walk_qty = tv_step_number.getText().toString().trim();
//        remind = "";
        if (cb_remind.isChecked()) {
            remind = "1";
        } else {
            remind = "0";
        }
        achieveTime = tv_remind_time.getText().toString().trim();
        if (walk_qty.isEmpty() || "0".equals(walk_qty)) {
            sp.setParam("planWalk_QTY", "10000");
        } else {
            sp.setParam("planWalk_QTY", walk_qty);
        }
        sp.setParam("remind", remind);

        if (achieveTime.isEmpty()) {
            sp.setParam("achieveTime", "21:00");
            this.achieveTime = "21:00";
        } else {
            sp.setParam("achieveTime", achieveTime);
        }
                finish();
    }

    private void showTimeDialog1() {
        final Calendar calendar = Calendar.getInstance(Locale.CHINA);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
//        String time = tv_remind_time.getText().toString().trim();
        final DateFormat df = new SimpleDateFormat("HH:mm");
//        Date date = null;
//        try {
//            date = df.parse(time);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        if (null != date) {
//            calendar.setTime(date);
//        }
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                String remaintime = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
                Date date = null;
                try {
                    date = df.parse(remaintime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (null != date) {
                    calendar.setTime(date);
                }
                tv_remind_time.setText(df.format(date));
            }
        }, hour, minute, true).show();
    }
}


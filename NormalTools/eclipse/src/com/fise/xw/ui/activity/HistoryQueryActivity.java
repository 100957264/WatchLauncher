package com.fise.xw.ui.activity;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
 
import com.byl.datepicker.wheelview.OnWheelScrollListener;
import com.byl.datepicker.wheelview.WheelView;
import com.byl.datepicker.wheelview.adapter.NumericWheelAdapter;
import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMSocketManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector; 
import com.fise.xw.protobuf.IMBaseDefine.ClientType;
import com.fise.xw.ui.base.TTBaseActivity; 
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;


/**
 *  查询历史轨迹界面
 * @author weileiguan
 *
 */
public class HistoryQueryActivity extends TTBaseActivity { 
	
	
	private LayoutInflater inflater = null;
	private WheelView year;
	private WheelView month;
	private WheelView day;
	private WheelView time;
	private WheelView min;
	private WheelView sec;
	
	private int mYear= 2017;
	private int mMonth=0;
	private int mDay=1;
	
	private RelativeLayout etStartTime;  
    private RelativeLayout end_time;  
        
    
    RelativeLayout ll;
	TextView tv;
	RelativeLayout show_button;
	Button cancel_button;
	Button button_confirm;
	
	TextView stat_edit;
	TextView end_edit;
	
	View view=null;
	
	private boolean stat = false;
	private boolean end  = false;
	
	private long statTime ;
	private long endTime ;
	
	
    private IMService imService;
    private int currentUserId;
    private UserEntity currentDevice;
    
    

	private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
            if(imService == null){
                throw new RuntimeException("#connect imservice success,but is null");
            }
            
        	currentUserId = HistoryQueryActivity.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);
        	
        	currentDevice = imService.getContactManager()
					.findDeviceContact(currentUserId);
        	
        	if(currentDevice!=null){
        		if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE) {
        			TextView black = (TextView) findViewById(R.id.black);
        			black.setText("定位卡片机");
        		}else if (currentDevice.getUserType() == ClientType.CLIENT_TYPE_FISE_CAR_VALUE) {
        			TextView black = (TextView) findViewById(R.id.black);
        			black.setText("电动车");
        		}
        	} 
    
        }

        @Override
        public void onServiceDisconnected() {

        }
    };
    
    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.historical_track); 
		imServiceConnector.connect(this); 
        EventBus.getDefault().register(this);
        
		
		inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
 
        etStartTime = (RelativeLayout) this.findViewById(R.id.stat_time);  
        end_time   = (RelativeLayout) this.findViewById(R.id.end_time);   
        
        
        stat_edit = (TextView)findViewById(R.id.stat_edit);  
        end_edit   = (TextView)findViewById(R.id.end_edit);   
        
        
        ll=(RelativeLayout) findViewById(R.id.ll);
		ll.addView(getDataPick());
		ll.setVisibility(View.GONE);
		show_button =(RelativeLayout) findViewById(R.id.show_button);
		show_button.setVisibility(View.GONE);
		
		button_confirm  = (Button)findViewById(R.id.button_confirm);   
		cancel_button  = (Button)findViewById(R.id.cancel_button); 
		
		button_confirm.setOnClickListener(new OnClickListener() { 
            @Override
            public void onClick(View v) {
              // TODO Auto-generated method stub 
            	show_button.setVisibility(View.GONE);
            	ll.setVisibility(View.GONE);
           
            	
            	if(stat)
            	{
            		
        			int n_year = year.getCurrentItem() + 1950;//
        			int n_month = month.getCurrentItem() + 1;//
        			int n_day = day.getCurrentItem()+1;
        			int n_min = min.getCurrentItem()+1;
        			int n_sec = sec.getCurrentItem()+1; 
        			
        			initDay(n_year,n_month);
        			String time =new StringBuilder().append((year.getCurrentItem()+1950)).append("-").append((month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1) : (month.getCurrentItem() + 1)).append("-").append(((day.getCurrentItem()+1) < 10) ? "0" + (day.getCurrentItem()+1) : (day.getCurrentItem()+1)).toString();
        			try {
						statTime = getTimestamp(n_year,n_month, n_day, n_min, n_sec);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}//timestampToDate(time);

        			stat_edit.setText( n_year+"年"+ n_month + "月" + n_day + "日"+ n_min +"时"+ n_sec +"分" ); 
        	        
            	}else if(end)
            	{
            		int n_year = year.getCurrentItem() + 1950;//
        			int n_month = month.getCurrentItem() + 1;//
        			int n_day = day.getCurrentItem()+1;
        			int n_min = min.getCurrentItem()+1;
        			int n_sec = sec.getCurrentItem()+1; 
        			
        			initDay(n_year,n_month);
        			String time =new StringBuilder().append((year.getCurrentItem()+1950)).append("-").append((month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1) : (month.getCurrentItem() + 1)).append("-").append(((day.getCurrentItem()+1) < 10) ? "0" + (day.getCurrentItem()+1) : (day.getCurrentItem()+1)).toString();
        			//endTime = timestampToDate(time);
          			try {
          				endTime = getTimestamp(n_year,n_month, n_day,  n_min, n_sec);
    					} catch (ParseException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}//t
          			
        			end_edit.setText( n_year+"年"+ n_month + "月" + n_day + "日"+ n_min +"时"+ n_sec +"分" );
            	}
            	 
                stat = false;
            	end = false;
            }
         });
           
		cancel_button.setOnClickListener(new OnClickListener() { 
            @Override
            public void onClick(View v) {
              // TODO Auto-generated method stub 
            	show_button.setVisibility(View.GONE);
            	ll.setVisibility(View.GONE);
            	
                stat = false;
            	end = false;
           
            }
         });
           
		
		
		TextView black  = (TextView)findViewById(R.id.black);   
		black.setOnClickListener(new OnClickListener() { 
            @Override
            public void onClick(View v) {
              // TODO Auto-generated method stub  
            	HistoryQueryActivity.this.finish();
            }
         });
		
		
		Button icon_arrow  = (Button)findViewById(R.id.icon_arrow);   
		icon_arrow.setOnClickListener(new OnClickListener() { 
            @Override
            public void onClick(View v) {
              // TODO Auto-generated method stub  
            	HistoryQueryActivity.this.finish();
            }
         });
		
		
        
		TextView query  = (TextView)findViewById(R.id.query);   
		 
		query.setOnClickListener(new OnClickListener() { 
            @Override
            public void onClick(View v) {
              // TODO Auto-generated method stub  
            	if((statTime == 0)||(endTime == 0))
            	{
            		return ;
            	}
            	
            	if(endTime<statTime)
            	{
            		Toast.makeText(HistoryQueryActivity.this, "时间设置错误", Toast.LENGTH_SHORT).show();
            		return ;
            	}
            	sendLocationPacket(currentUserId,statTime,endTime);
            	
            }
         });
           
		
		
        etStartTime.setOnClickListener(new OnClickListener() { 
            @Override
            public void onClick(View v) {
              // TODO Auto-generated method stub 
            	show_button.setVisibility(View.VISIBLE);
            	ll.setVisibility(View.VISIBLE);
           
            	stat = true;
            	end  = false;
            }
         });
           
        
        end_time.setOnClickListener(new OnClickListener() { 
            @Override
            public void onClick(View v) {
              // TODO Auto-generated method stub 
            	show_button.setVisibility(View.VISIBLE);
            	ll.setVisibility(View.VISIBLE);
            	
            	stat = false;
            	end  = true;
            }
         });
        
        
        
	} 
	
    public void sendLocationPacket(int userId ,long statTime,long endTime){
    
    	imService.getUserActionManager().onRepLocationReq(userId ,statTime,endTime);
    }
	
	private View getDataPick() {
		Calendar c = Calendar.getInstance();
		int norYear = c.get(Calendar.YEAR);
		int curMonthNum = c.get(Calendar.MONTH) + 1;//通过Calendar算出的月数要+1
		int curDateNum = c.get(Calendar.DATE);
		
		mYear = norYear;
		mMonth = curMonthNum;
		mDay = curDateNum;
		
		int curYear = mYear;
		int curMonth = mMonth;//mMonth+1;
		int curDate = mDay;
		
		view = inflater.inflate(R.layout.wheel_date_picker, null);
		
		year = (WheelView) view.findViewById(R.id.year);
		NumericWheelAdapter numericWheelAdapter1=new NumericWheelAdapter(this,1950, norYear); 
		numericWheelAdapter1.setLabel("年");
		year.setViewAdapter(numericWheelAdapter1);
		year.setCyclic(true);//是否可循环滑动
		year.addScrollingListener(scrollListener);
		
		month = (WheelView) view.findViewById(R.id.month);
		NumericWheelAdapter numericWheelAdapter2=new NumericWheelAdapter(this,1, 12, "%02d"); 
		numericWheelAdapter2.setLabel("月");
		month.setViewAdapter(numericWheelAdapter2);
		month.setCyclic(true);
		month.addScrollingListener(scrollListener);
		
		day = (WheelView) view.findViewById(R.id.day);
		initDay(curYear,curMonth);
		day.setCyclic(true);
		 
		
		min = (WheelView) view.findViewById(R.id.min);
		NumericWheelAdapter numericWheelAdapter3=new NumericWheelAdapter(this,1, 23, "%02d"); 
		numericWheelAdapter3.setLabel("时");
		min.setViewAdapter(numericWheelAdapter3);
		min.setCyclic(true);
		min.addScrollingListener(scrollListener);
		
		sec = (WheelView) view.findViewById(R.id.sec);
		NumericWheelAdapter numericWheelAdapter4=new NumericWheelAdapter(this,1, 59, "%02d"); 
		numericWheelAdapter4.setLabel("分");
		sec.setViewAdapter(numericWheelAdapter4);
		sec.setCyclic(true);
		sec.addScrollingListener(scrollListener);
		
		
		year.setVisibleItems(7);//设置显示行数
		month.setVisibleItems(7);
		day.setVisibleItems(7);
//		time.setVisibleItems(7);
		min.setVisibleItems(7);
		sec.setVisibleItems(7);
		
		year.setCurrentItem(curYear - 1950);
		month.setCurrentItem(curMonth - 1);
		day.setCurrentItem(curDate - 1);
		
		return view;
	}
	
	
	
	/**
	 */
	private void initDay(int arg1, int arg2) {
		NumericWheelAdapter numericWheelAdapter=new NumericWheelAdapter(this,1, getDay(arg1, arg2), "%02d");
		numericWheelAdapter.setLabel("日");
		day.setViewAdapter(numericWheelAdapter);
	}
	
	/**
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	private int getDay(int year, int month) {
		int day = 30;
		boolean flag = false;
		switch (year % 4) {
		case 0:
			flag = true;
			break;
		default:
			flag = false;
			break;
		}
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			day = 31;
			break;
		case 2:
			day = flag ? 29 : 28;
			break;
		default:
			day = 30;
			break;
		}
		return day;
	}
	
	
	
	public long getTimestamp(int year,int month, int day,int hour,int min) throws ParseException{
		
		String showHour;
		if(hour<=9)
		{
			showHour = "0" + hour;
		}else{
			showHour = "" + hour;
		}
		
		String shouwMin;
		if(min<=9)
		{
			shouwMin = "0" + min;
		}else{
			shouwMin = "" + min;
		}
		
		 
		
		String str = String.format("%s:%s:%s",showHour,shouwMin,"00");
		String time = year +"/" +month +"/" +day+" " + str;  
        Date date1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(time);
        Date date2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("1970/01/01 08:00:00");
        
       long time1 = date1.getTime()/1000;
       long time2 = date2.getTime()/1000;
        
       long l = time1 - time2 > 0 ? time1- time2 : 0;
  
       return l;
}
	
	/**
	 * unix时间戳转换为dateFormat
	 * 
	 * @param beginDate
	 * @return
	 */
	public  long timestampToDate(String beginDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date1 = null;
		try {
			date1 = sdf.parse(beginDate);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	   Date date2 = null;
		try {
			date2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("1970/01/01 08:00:00");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         long l = date1.getTime() - date2.getTime() > 0 ? date1.getTime()- date2.getTime() : date2.getTime() - date1.getTime();
         //long rand = (int)(Math.random()*1000);
         
		//String sd = sdf.format(new Date(Long.parseLong(beginDate)));
		return l;
	}
	
	public static final String calculateDatePoor(String birthday) {
		try {
			if (TextUtils.isEmpty(birthday))
				return "0";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date birthdayDate = sdf.parse(birthday);
			String currTimeStr = sdf.format(new Date());
			Date currDate = sdf.parse(currTimeStr);
			if (birthdayDate.getTime() > currDate.getTime()) {
				return "0";
			}
			long age = (currDate.getTime() - birthdayDate.getTime())
					/ (24 * 60 * 60 * 1000) + 1;
			String year = new DecimalFormat("0.00").format(age / 365f);
			if (TextUtils.isEmpty(year))
				return "0";
			return String.valueOf(new Double(year).intValue());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "0";
	}

	
	OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
		@Override
		public void onScrollingStarted(WheelView wheel) {

		}

		@Override
		public void onScrollingFinished(WheelView wheel) {
			int n_year = year.getCurrentItem() + 1950;//
			int n_month = month.getCurrentItem() + 1;//
			initDay(n_year,n_month);
			
			
			//String birthday=new StringBuilder().append((year.getCurrentItem()+1950)).append("-").append((month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1) : (month.getCurrentItem() + 1)).append("-").append(((day.getCurrentItem()+1) < 10) ? "0" + (day.getCurrentItem()+1) : (day.getCurrentItem()+1)).toString();
			//tv.setText("年龄             "+calculateDatePoor(birthday)+"岁");
		}
	};
	
    @Override
    public void onDestroy() { 
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(this);
        super.onDestroy();
    }
    @Override  
    protected void onResume() {  
        super.onResume();   
        }  
    @Override  
    protected void onPause() {  
        super.onPause();    
    }

    
    /**
     * ----------------------------event 事件驱动----------------------------
     */
    public void onEventMainThread(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_UPDATE_QUERY_SUCCESS:  
            	IMUIHelper.openHistoryTrackActivity(HistoryQueryActivity.this);
                break; 
            case USER_INFO_UPDATE_QUERY_FAIL:   
                break;  
                
        }
    }


}

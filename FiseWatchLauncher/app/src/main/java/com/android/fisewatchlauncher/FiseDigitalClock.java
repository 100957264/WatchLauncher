package com.android.fisewatchlauncher;

import java.util.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.DigitalClock;

@SuppressWarnings("deprecation")
public class FiseDigitalClock extends DigitalClock {
	private final static String m12 = "h:mm:ss aa";
	private final static String m24 = "k:mm:ss";
	Calendar mCalendar;
	
	private FormatChangeObserver mFormatChangeObserver;

    private Runnable mTicker;
    private Handler mHandler;

    private boolean mTickerStopped = false;

    String mFormat;
	public FiseDigitalClock(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initClock(context);
	}
	public FiseDigitalClock(Context context,AttributeSet attrs) {
		super(context,attrs);
		// TODO Auto-generated constructor stub
		initClock(context);
	}
	private void initClock(Context context) {
		// TODO Auto-generated method stub
		Resources r = context.getResources();

        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }

        mFormatChangeObserver = new FormatChangeObserver();
        getContext().getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver);

        setFormat();
	}
	private void setFormat() {
		mFormat = m24;
		// TODO Auto-generated method stub
//		if (get24HourMode()) {
//            mFormat = m24;
//        } else {
//            mFormat = m12;
//        }
	}
	@Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
    }
	@Override
    protected void onAttachedToWindow() {
        mTickerStopped = false;
        super.onAttachedToWindow();
        mHandler = new Handler();

        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = new Runnable() {
                public void run() {
                    if (mTickerStopped) return;
                    mCalendar.setTimeInMillis(System.currentTimeMillis());
                    setText(DateFormat.format(mFormat, mCalendar));
                    invalidate();
                    long now = SystemClock.uptimeMillis();
                    long next = now + (1000 - now % 1000);
                    mHandler.postAtTime(mTicker, next);
                }
            };
        mTicker.run();
    }
	private boolean get24HourMode() {
		// TODO Auto-generated method stub
		return android.text.format.DateFormat.is24HourFormat(getContext());
	}
	private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }
        public void onChange(boolean selfChange) {
            setFormat();
        }
		
    }
}

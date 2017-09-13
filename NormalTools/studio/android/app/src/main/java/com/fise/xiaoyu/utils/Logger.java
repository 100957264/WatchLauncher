package com.fise.xiaoyu.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Logger {
    /**
     * log tag
     */
    public static final boolean LOG_DEBUG = true;
    public static final boolean LOG_EVENTBUS = false;//打印EventBus
    public static final boolean LOG_URI = false;//打印uri转换
    public static final boolean LOG_PERMISSION = false;//打印权限申请
    public static final boolean LOG_APPLICATION = false;//打印生命周期
    public static final boolean LOG_ACTIVITY_NAME = true;//打印ACTIVITY名字
    public static final boolean DEBUG_LOCATION = true;//开启定位
    public static final boolean DEBUG_FORCE_ANDROID_LOGIN = false;//禁用设备登录

    private String tagName = "MoGuLogger";// tag name
    // private static int logLevel = Log.ERROR;
    private static int logLevel = Log.DEBUG;

    private static Logger inst;
    private Lock lock;

    private Logger() {
        lock = new ReentrantLock();
    }

    public static synchronized Logger getLogger(Class<?> key) {
        if (inst == null) {
            inst = new Logger();
        }
        return inst;
    }

    private String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();

        if (sts == null) {
            return null;
        }

        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }

            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }

            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }

            return "[" + st.getFileName() + ":" + st.getLineNumber() + "]";
        }

        return null;
    }

    private String createMessage(String msg) {
        String functionName = getFunctionName();
        long threadId = Thread.currentThread().getId();
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date());
        String message = (functionName == null ? msg : (functionName + " - "
                + String.valueOf(threadId) + " - " + msg));
        String finalRes = currentTime + " - " + message;
        return finalRes;
    }

    public static void dd(boolean opened, Object obj) {
        if (opened) {
            String log = obj == null ? "null" : obj.toString();
            Log.d("ddd", getLogger(Logger.class).getFunctionName() + " " + log);
        }
    }

    /**
     * log.i
     */
    public void i(String format, Object... args) {
        if (logLevel <= Log.INFO) {
            lock.lock();
            try {
                String message = createMessage(getInputString(format, args));
                Log.i(tagName, message);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * log.v
     */
    public void v(String format, Object... args) {
        if (logLevel <= Log.VERBOSE) {
            lock.lock();
            try {
                String message = createMessage(getInputString(format, args));
                Log.v(tagName, message);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * log.d
     */
    public void d(String format, Object... args) {
        if (logLevel <= Log.DEBUG) {
            lock.lock();
            try {
                String message = createMessage(getInputString(format, args));
                Log.d(tagName, message);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * log.e
     */
    public void e(String format, Object... args) {
        if (logLevel <= Log.ERROR) {
            lock.lock();
            try {
                String message = createMessage(getInputString(format, args));
                Log.e(tagName, message);
            } finally {
                lock.unlock();
            }
        }
    }

    private String getInputString(String format, Object... args) {
        if (format == null) {
            return "null log format";
        }

        return String.format(format, args);
    }

    /**
     * log.error
     */
    public void error(Exception e) {
        if (logLevel <= Log.ERROR) {
            StringBuffer sb = new StringBuffer();
            lock.lock();
            try {
                String name = getFunctionName();
                StackTraceElement[] sts = e.getStackTrace();

                if (name != null) {
                    sb.append(name + " - " + e + "\r\n");
                } else {
                    sb.append(e + "\r\n");
                }
                if (sts != null && sts.length > 0) {
                    for (StackTraceElement st : sts) {
                        if (st != null) {
                            sb.append("[ " + st.getFileName() + ":"
                                    + st.getLineNumber() + " ]\r\n");
                        }
                    }
                }
                Log.e(tagName, sb.toString());
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * log.d
     */
    public void w(String format, Object... args) {
        if (logLevel <= Log.WARN) {
            lock.lock();
            try {
                String message = createMessage(getInputString(format, args));
                Log.w(tagName, message);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * set log level
     */

    public void setLevel(int l) {
        lock.lock();
        try {
            logLevel = l;
        } finally {
            lock.unlock();
        }
    }

}

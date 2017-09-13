package com.fise.marechat.utils;

import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import com.fise.marechat.BuildConfig;

/**
 * 日志输出�
 *
 * @author mare
 * @Description:
 * @date 2016-03-21
 * @time 下午5:40:23
 */
public class LogUtils {

    /**
     * 关闭DEBUG日志输出
     */
    private static boolean DEBUG = BuildConfig.DEBUG;
    private static FileOutputStream fos;
    /**
     * TAG 名称
     */
    private static String tag = "[mare]";
    private static LogUtils log;
    public static final String SEPARATOR = ":";

    private LogUtils(String tag) {
        this.tag = tag;
    }

    /**
     * Get The Current Function Name
     *
     * @return Name
     */
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
            return "[ " + Thread.currentThread().getName() + ": "
                    + st.getFileName() + ":" + st.getLineNumber() + " "
                    + st.getMethodName() + " ]";
        }
        return null;
    }

    public static void i(Object str) {
        print(Log.INFO, str, null);
    }

    public static void d(Object str) {
        print(Log.DEBUG, str, null);
    }

    public static void v(Object str) {
        print(Log.VERBOSE, str, null);
    }

    public static void w(Object str) {
        print(Log.WARN, str, null);
    }

    public static void e(Object str) {
        print(Log.ERROR, str, null);
    }

    public static void d(Object str, Throwable throwable) {
        print(Log.DEBUG, str, throwable);
    }

    public static void v(Object str, Throwable throwable) {
        print(Log.VERBOSE, str, throwable);
    }

    public static void w(Object str, Throwable throwable) {
        print(Log.WARN, str, throwable);
    }

    public static void i(Object str, Throwable throwable) {
        print(Log.INFO, str, throwable);
    }

    public static void v(Object tag, String str) {
        print(Log.VERBOSE, str, null);
    }

    public static void d(Object tag, String str) {
        print(Log.DEBUG, str, null);
    }

    public static void i(Object tag, String str) {
        print(Log.INFO, str, null);
    }

    public static void e(Object tag, String str) {
        print(Log.ERROR, str, null);
    }

    public static void e(Object str, Throwable throwable) {
        print(Log.ERROR, str, throwable);
    }

    public static void e(String tag, Object str, Throwable throwable) {
        print(Log.ERROR, str, throwable);
    }

    /**
     * 用于区分不同接口数据 打印传入参数
     *
     * @param index
     * @param str
     */
    private static void print(int index, Object str, Throwable throwable) {

        if (log == null) {
            log = new LogUtils(tag);
        }
        String name = log.getFunctionName();
        if (name != null) {
            str = name + " - " + str;
        }

        // Close the debug log When DEBUG is false
        if (!DEBUG) {
            if (index <= Log.INFO) {
                return;
            }
        }
        if (null == throwable) {
            switch (index) {
                case Log.VERBOSE:
                    Log.v(tag, str.toString());
                    break;
                case Log.DEBUG:
                    Log.d(tag, str.toString());
                    break;
                case Log.INFO:
                    Log.i(tag, str.toString());
                    break;
                case Log.WARN:
                    Log.w(tag, str.toString());
                    break;
                case Log.ERROR:
                    Log.e(tag, str.toString());
                    break;
                default:
                    break;
            }
        } else {
            switch (index) {
                case Log.VERBOSE:
                    Log.v(tag, str.toString(), throwable);
                    break;
                case Log.DEBUG:
                    Log.d(tag, str.toString(), throwable);
                    break;
                case Log.INFO:
                    Log.i(tag, str.toString(), throwable);
                    break;
                case Log.WARN:
                    Log.w(tag, str.toString(), throwable);
                    break;
                case Log.ERROR:
                    Log.e(tag, str.toString(), throwable);
                    break;
                default:
                    break;
            }

        }
    }

    public static void saveLog(StringBuffer sb, String dir, String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(
                    new File(dir, fileName), true);
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            closeSilently(fos);
        }
    }

    protected static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable e) {
                // ignored
            }
        }
    }
}

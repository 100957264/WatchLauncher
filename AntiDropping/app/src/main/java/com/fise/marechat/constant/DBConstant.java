package com.fise.marechat.constant;

import com.fise.marechat.util.SDCardUtils;

import java.io.File;

/**
 * @author mare
 * @Description:
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/11
 * @time 16:29
 */
public class DBConstant {
    public static final String CURRENT_USR_NAME = "anti";
    public static final String DBNAME = "anti.db";
    public static final String DB_FILE_PATH = SDCardUtils.getSDCardPath() +
            File.separator + "anti/";
}

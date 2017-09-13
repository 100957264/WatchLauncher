package com.fise.marechat.constant;

import com.fise.marechat.util.SDCardUtils;

import java.io.File;

/**
 * @author mare
 * @Description:
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/12
 * @time 10:19
 */
public class FileConstant {

    /**拍照后原图回存入此路径下**/
    /**抓拍相片保存的位置路径**/
    public static final String PHOTO_CAPTURE_OUTPUT_PATH = SDCardUtils.getSDCardPath() +
            File.separator + "rcapture/";
}

package android.fise.com.fiseassitant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qingfeng on 2017/6/30.
 */

public class ReadSDcardFileUtil {
    final static String NUMBER_PATH="/sdcard/watchlauncher/number/number.txt";
    final static String BIND_STATUS_PATH="/sdcard/watchlauncher/bind/bind.txt";
    final static String STOP_STATUS_PATH="/sdcard/watchlauncher/forbid/forbidden.txt";
    final static String NETWORK_STATUS_PATH="/sdcard/watchlauncher/network/network.txt";
    public static String readSDFile(String path) {
        StringBuffer sb = new StringBuffer();
        File file = new File(path);
        try {
            FileInputStream fis = new FileInputStream(file);
            int c;
            while ((c = fis.read()) != -1) {
                if(Character.isWhitespace(c) ){
                    continue;
                }
                sb.append((char) c);
            }
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(sb.toString());
        return  m.replaceAll("");
    }
    public static boolean isFileExisted(String path){
        File file = new File(path);
        if(file.exists()){
            return  true;
        }
        return false;
    }
}

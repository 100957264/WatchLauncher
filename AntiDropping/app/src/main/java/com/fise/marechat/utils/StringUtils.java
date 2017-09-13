package com.fise.marechat.utils;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public  class StringUtils {
    private static final String TAG = "StringUtils";
    public static String join(List<String> listString, String sep) {

        if(listString.size()<=0) return null;

        StringBuilder sb = new StringBuilder();
        for(int i=0; i<listString.size(); i++) {
            if(i>0) sb.append(sep);
            sb.append(listString.get(i));
        }
        return sb.toString();
    }

    public static List<String> StringToList(String stringToList, String sep) {
        String[] strArr = stringToList.split(sep);
        List<String> list = new ArrayList<>(Arrays.asList(strArr));
        return list;
    }


    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
        catch (NoSuchAlgorithmException e) {
            LogUtils.e(TAG, e);
            return null;
        }
    }

    public static String signature(String body, String signKey) {
        return body+ StringUtils.getMD5(body+signKey);
    }

    public static String getHostName(String urlString) {
        String head = "";
        String original  = urlString;
        int index = urlString.indexOf("://");
        if (index != -1) {
            head = urlString.substring(0, index + 3);
            urlString = urlString.substring(index + 3);
        }
        index = urlString.indexOf("/");
        if (index != -1) {
            urlString = urlString.substring(0, index + 1);//baseUrl要加'/'
        }
        LogUtils.d("baseUrl:"+head + urlString);
        LogUtils.d("url:"+original);
        return head + urlString;
    }

    public static String getDataSize(long var0) {
        DecimalFormat var2 = new DecimalFormat("###.00");
        return var0 < 1024L ? var0 + "bytes" : (var0 < 1048576L ? var2.format((double) ((float) var0 / 1024.0F))
                + "KB" : (var0 < 1073741824L ? var2.format((double) ((float) var0 / 1024.0F / 1024.0F))
                + "MB" : (var0 < 0L ? var2.format((double) ((float) var0 / 1024.0F / 1024.0F / 1024.0F))
                + "GB" : "error")));
    }

    public static int[] string2Ascii(String value) {
        char[] chars = value.toCharArray();
        int[] charsAsccii = new int[chars.length];
        for (int i = 0; i < chars.length; i++) {
            charsAsccii[i] = (int) chars[i];
        }
        return charsAsccii;
    }

    /**
     * 返回字符串长度
     *
     * @param s 字符串
     * @return null返回0，其他返回自身长度
     */
    public static int length(CharSequence s) {
        return s == null ? 0 : s.length();
    }


    /**
     * 反转字符串
     *
     * @param s 待反转字符串
     * @return 反转字符串
     */
    public static String reverse(String s) {
        int len = length(s);
        if (len <= 1) return s;
        int mid = len >> 1;
        char[] chars = s.toCharArray();
        char c;
        for (int i = 0; i < mid; ++i) {
            c = chars[i];
            chars[i] = chars[len - i - 1];
            chars[len - i - 1] = c;
        }
        return new String(chars);
    }



    /**
     * Splits a string given a pattern (regex), considering escapes.
     * <p> For example considering a pattern "," we have:
     * one,two,three => {one},{two},{three}
     * one\,two\\,three => {one,two\\},{three}
     * <p>
     * NOTE: Untested with pattern regex as pattern and untested for escape chars in text or pattern.
     */
    public static String[] split(String text, String pattern) {
        String[] array = text.split(pattern, -1);
        ArrayList list = new ArrayList();
        for (int i = 0; i < array.length; i++) {
            boolean escaped = false;
            if (i > 0 && array[i - 1].endsWith("\\")) {
                // When the number of trailing "\" is odd then there was no separator and this pattern is part of
                // the previous match.
                int depth = 1;
                while (depth < array[i-1].length() && array[i-1].charAt(array[i-1].length() - 1 - depth) == '\\') depth ++;
                escaped = depth % 2 == 1;
            }
            if (!escaped) list.add(array[i]);
            else {
                String prev = (String) list.remove(list.size() - 1);
                list.add(prev.substring(0, prev.length() - 1) + pattern + array[i]);
            }
        }
        return (String[]) list.toArray(new String[0]);
    }

    /**
     * Creates an MD5 digest from the message.
     * Note that this implementation is unsalted.
     * @param message not null
     * @return MD5 digest, not null
     */
    public static String md5(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            StringBuffer sb = new StringBuffer();
            byte buf[] = message.getBytes();
            byte[] md5 = md.digest(buf);
            //System.out.println(message);
            for (int i = 0; i < md5.length; i++) {
                String tmpStr = "0" + Integer.toHexString((0xff & md5[i]));
                sb.append(tmpStr.substring(tmpStr.length() - 2));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * 16位的md5
     * @param str
     * @return
     */
    public static final String md5Hex(final String str) {

        return md5(str).substring(8,24);
    }

    /**
     * Capitalizes each word in the given text by converting the
     * first letter to upper case.
     * @param data text to be capitalize, possibly null
     * @return text with each work capitalized,
     * or null when the text is null
     */
    public static String capitalizeWords(String data) {
        if (data==null) return null;
        StringBuffer res = new StringBuffer();
        char ch;
        char prevCh = '.';
        for ( int i = 0;  i < data.length();  i++ ) {
            ch = data.charAt(i);
            if ( Character.isLetter(ch)) {
                if (!Character.isLetter(prevCh) ) res.append( Character.toUpperCase(ch) );
                else res.append( Character.toLowerCase(ch) );
            } else res.append( ch );
            prevCh = ch;
        }
        return res.toString();
    }

    /**
     * 获取异常信息
     * @param e
     * @return
     */
    public static String getCauseStr(Throwable e){
        return null == e ? "e = null" : null == e.getCause() ? "e.getCause() == null" :  e.getCause().toString();
    }
}
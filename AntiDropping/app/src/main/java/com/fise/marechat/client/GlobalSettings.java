package com.fise.marechat.client;

import com.fise.marechat.BuildConfig;

/**
 * Created by fanyang on 2017/8/4.
 */
public class GlobalSettings {

    /**
     * 厂商
     */
    protected final static String product = "SG";
    /**
     * 设备ID
     */
    private String imei = "123456789123459";
    private final static String privateKey = "3JN87QYchF44FSWfY6as4Y4JwAeRtVGm";
    public final static String MSG_HEADER_SEPERATOR = "*";
    public final static String MSG_CONTENT_SEPERATOR = ",";
    public final static String MSG_PREFIX = "[";
    public final static String MSG_SUFFIX = "]";
    private boolean isDebug = BuildConfig.DEBUG;
    public final static String DOMAIN_NAME = "api.xcloudtech.com";
    public final static int PORT = 9000;
    private static String IP = "";
    private TcpConnConfig config;

    private GlobalSettings() {
    }

    private static class SingletonHolder {
        private static final GlobalSettings INSTANCE = new GlobalSettings();
    }

    public static GlobalSettings instance() {
        return SingletonHolder.INSTANCE;
    }

    public String getProduct() {
        return product;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        if (isDebug) return;
        this.imei = imei;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public static String getIP() {
        return IP;
    }

    public static void setIP(String IP) {
        GlobalSettings.IP = IP;
    }

    public TcpConnConfig getConfig() {
        return config;
    }

    public void setConfig(TcpConnConfig config) {
        this.config = config;
    }
}

package com.fise.xiaoyu.imservice.entity;

import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.fise.xiaoyu.DB.DBInterface;
import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.app.IMApplication;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.MessageConstant;
import com.fise.xiaoyu.imservice.event.MessageEvent;
import com.fise.xiaoyu.imservice.manager.IMMessageManager;
import com.fise.xiaoyu.imservice.support.SequenceNumberMaker;
import com.fise.xiaoyu.protobuf.IMDevice.AlarmType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * 设备消息
 */
public class DevMessage extends MessageEntity implements Serializable,
        OnGeocodeSearchListener {

    private GeocodeSearch geocoderSearch;
    private int locationType;
    private int battery;
    private String devContent = "";

    private double longitude = 0;
    private double latitude = 0;

    public DevMessage() {
        msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();

        geocoderSearch = new GeocodeSearch(IMApplication.getApplication());
        geocoderSearch.setOnGeocodeSearchListener(this);
    }

    private DevMessage(MessageEntity entity) {
        /** 父类的id */
        id = entity.getId();
        msgId = entity.getMsgId();
        fromId = entity.getFromId();
        toId = entity.getToId();
        sessionKey = entity.getSessionKey();
        content = entity.getContent();
        msgType = entity.getMsgType();
        displayType = entity.getDisplayType();
        status = entity.getStatus();
        created = entity.getCreated();
        updated = entity.getUpdated();

        type = entity.getType();
        sendId = entity.getSendId();
        address = entity.getAddress();
        messageTime = entity.getMessageTime();

        geocoderSearch = new GeocodeSearch(IMApplication.getApplication());
        geocoderSearch.setOnGeocodeSearchListener(this);
    }

    public static DevMessage parseFromNet(MessageEntity entity) {
        DevMessage devMessage = new DevMessage(entity);
        devMessage.setStatus(MessageConstant.MSG_SUCCESS);
        devMessage.setDisplayType(DBConstant.SHOW_TYPE_DEV_MESSAGE);

        JSONObject extraContent;
        try {
            extraContent = new JSONObject(entity.getContent());
            if (!extraContent.isNull("from_type")) {
                devMessage.setLocationType(extraContent.getInt("from_type"));
            }
            if (!extraContent.isNull("battery")) {
                devMessage.setBattery(extraContent.getInt("battery"));
            }
            if (!extraContent.isNull("dev_content")) {
                devMessage.setDevContent(extraContent.getString("dev_content"));
            }

            if (!extraContent.isNull("longitude")) {
                devMessage.setLongitude(extraContent.getDouble("longitude"));
            }
            if (!extraContent.isNull("latitude")) {
                devMessage.setLatitude(extraContent.getDouble("latitude"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return devMessage;
    }

    public static DevMessage parseFromDB(MessageEntity entity) {
        if (entity.getDisplayType() != DBConstant.SHOW_TYPE_DEV_MESSAGE) {
            throw new RuntimeException(
                    "#TextMessage# parseFromDB,not SHOW_ORIGIN_TEXT_TYPE");
        }
        DevMessage devMessage = new DevMessage(entity);
        JSONObject extraContent;
        try {

            if (!TextUtils.isEmpty(entity.getContent())) {

                if(!entity.getContent().equals("")){
                    extraContent = new JSONObject(entity.getContent());
                    if (!extraContent.isNull("from_type")) {
                        devMessage.setLocationType(extraContent.getInt("from_type"));
                    }
                    if (!extraContent.isNull("battery")) {
                        devMessage.setBattery(extraContent.getInt("battery"));
                    }
                    if (!extraContent.isNull("dev_content")) {
                        devMessage.setDevContent(extraContent.getString("dev_content"));
                    }

                    if (!extraContent.isNull("longitude")) {
                        devMessage.setLongitude(extraContent.getDouble("longitude"));
                    }
                    if (!extraContent.isNull("latitude")) {
                        devMessage.setLatitude(extraContent.getDouble("latitude"));
                    }
                }

            } else if (entity != null) {
                devMessage.setDevContent("");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return devMessage;
    }


    public static DevMessage buildForSend(String content, UserEntity fromUser,
                                          PeerEntity peerEntity) {
        DevMessage devMessage = new DevMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        devMessage.setFromId(fromUser.getPeerId());
        devMessage.setToId(peerEntity.getPeerId());
        devMessage.setUpdated(nowTime);
        devMessage.setCreated(nowTime);
        devMessage.setDisplayType(DBConstant.SHOW_TYPE_DEV_MESSAGE);
        devMessage.setGIfEmo(true);
        int peerType = peerEntity.getType();
        int msgType = DBConstant.MSG_TYPE_GROUP_DEV_MESSAGE;
        devMessage.setMsgType(msgType);
        devMessage.setStatus(MessageConstant.MSG_SENDING);
        // 内容的设定
        devMessage.setContent(content);
        devMessage.buildSessionKey(true);
        devMessage.setMessageTime("" + nowTime);


        return devMessage;
    }

    public static DevMessage buildForSend(String content, UserEntity fromUser,
                                          UserEntity peerEntity, AlarmType alarmType, int sendId, String time) {
        DevMessage devMessage = new DevMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        devMessage.setFromId(fromUser.getPeerId());
        devMessage.setToId(peerEntity.getPeerId());
        devMessage.setUpdated(nowTime);
        devMessage.setCreated(nowTime);
        devMessage.setDisplayType(DBConstant.SHOW_TYPE_DEV_MESSAGE);
        devMessage.setGIfEmo(true);
        int peerType = peerEntity.getType();
        int msgType = DBConstant.MSG_TYPE_GROUP_DEV_MESSAGE;
        devMessage.setMsgType(msgType);
        devMessage.setStatus(MessageConstant.MSG_SENDING);
        // 内容的设定
        devMessage.setContent(content);
        devMessage.buildSessionKey(false);
        devMessage.setType(alarmType.ordinal());
        devMessage.setSendId(sendId);
        devMessage.setMessageTime(time);


        return devMessage;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }


    public void setAddressName(String lat, String lnt) {

        if ((!lat.equals("")) && (!lnt.equals(""))) {
            LatLonPoint latLonPoint = new LatLonPoint(
                    Double.parseDouble(lat), Double.parseDouble(lnt));

            getAddress(latLonPoint);

        }
    }

    /**
     * Not-null value. DB的时候需要
     */
    @Override
    public String getContent() {
        return content;
    }

    @Override
    public byte[] getSendContent() {
        try {
            /** 加密 */
            String sendContent = new String(Security
                    .getInstance().EncryptMsg(content));
            return sendContent.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == 1000) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {

                String address = result.getRegeocodeAddress().getFormatAddress();
                this.setAddress(address);

                long pkId = DBInterface.instance().insertOrUpdateMessage(this);
                //IMMessageManager.instance().triggerEvent(UserInfoEvent.USER_INFO_DEV_POS_DATA_SUCCESS);
                IMMessageManager.instance().triggerEvent(new MessageEvent(MessageEvent.Event.USER_INFO_DEV_POS_DATA_SUCCESS,
                        this));


            }
        }

    }

    @Override
    public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
        // TODO Auto-generated method stub

    }


    public int getLocationType() {
        return locationType;
    }

    public void setLocationType(int locationType) {
        this.locationType = locationType;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public String getDevContent() {
        return devContent;
    }

    public void setDevContent(String devContent) {
        this.devContent = devContent;
    }


    /**
     * 响应逆地理编码
     */
    public void getAddress(final LatLonPoint latLonPoint) {

		/*
        LatLng sourceLatLng  =  new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
		CoordinateConverter converter  = new CoordinateConverter();
		// CoordType.GPS 待转换坐标类型
		converter.from(CoordType.GPS);
		// sourceLatLng待转换坐标点 LatLng类型
		converter.coord(sourceLatLng);
		// 执行转换操作
		LatLng desLatLng = converter.convert();
		LatLonPoint desPoint = new LatLonPoint(desLatLng.latitude,
				desLatLng.longitude);
		*/
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
    }

}

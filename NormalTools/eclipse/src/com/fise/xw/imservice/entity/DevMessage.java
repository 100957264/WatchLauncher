package com.fise.xw.imservice.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import com.amap.api.maps2d.CoordinateConverter;
import com.amap.api.maps2d.CoordinateConverter.CoordType;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.fise.xw.DB.DBInterface;
import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.DB.entity.PeerEntity;
import com.fise.xw.DB.entity.UserEntity; 
import com.fise.xw.app.IMApplication;
import com.fise.xw.config.DBConstant; 
import com.fise.xw.config.MessageConstant;
import com.fise.xw.imservice.event.PriorityEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMMessageManager;
import com.fise.xw.imservice.manager.IMSessionManager;
import com.fise.xw.imservice.support.SequenceNumberMaker;
import com.fise.xw.protobuf.IMDevice.AlarmType;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 */
public class DevMessage extends MessageEntity implements Serializable,
		OnGeocodeSearchListener {

	private GeocodeSearch geocoderSearch;

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

	}

	public static DevMessage parseFromNet(MessageEntity entity) {
		DevMessage textMessage = new DevMessage(entity);
		textMessage.setStatus(MessageConstant.MSG_SUCCESS);
		textMessage.setDisplayType(DBConstant.SHOW_TYPE_DEV_MESSAGE);
		return textMessage;
	}

	public static DevMessage parseFromDB(MessageEntity entity) {
		if (entity.getDisplayType() != DBConstant.SHOW_TYPE_DEV_MESSAGE) {
			throw new RuntimeException(
					"#TextMessage# parseFromDB,not SHOW_ORIGIN_TEXT_TYPE");
		}
		DevMessage textMessage = new DevMessage(entity);
		return textMessage;
	}

	public static DevMessage parseFromNoteDB(MessageEntity entity) {

		DevMessage textMessage = new DevMessage(entity);
		return textMessage;
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
		devMessage.setMessageTime(""+nowTime);
		

		return devMessage;
	}

	public static DevMessage buildForSend(String content, UserEntity fromUser,
			UserEntity peerEntity, AlarmType alarmType, int sendId,String time) {
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

	
	
	
	public void setAddressName(String lat,String lnt){
		
	if((!lat.equals(""))&&(!lnt.equals(""))){ 
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
			String sendContent = new String(com.fise.xw.Security
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
				IMSessionManager.instance().updateSession(this); 
				  
				IMMessageManager.instance().triggerEvent(UserInfoEvent.USER_INFO_DEV_DATA_SUCCESS);
				
				
			} else {

			}
		} else {

		}
	}

	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
		// TODO Auto-generated method stub

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

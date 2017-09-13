package com.fise.xw.imservice.entity;

import android.annotation.SuppressLint;
import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.DB.entity.PeerEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.MessageConstant;
import com.fise.xw.imservice.support.SequenceNumberMaker;
import com.fise.xw.ui.adapter.album.ImageItem;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 */
@SuppressLint("NewApi")
public class PostionMessage extends MessageEntity implements Serializable {

	/** 本地保存的path */
	private String path = "";
	/** 图片的网络地址 */
	//private String url = "";
	private int loadStatus;
	private double lat;
	private double lng;
	private String postionName;

	// 存储图片消息
	private static java.util.HashMap<Long, PostionMessage> postionMessageMap = new java.util.HashMap<Long, PostionMessage>();
	private static ArrayList<PostionMessage> postionList = null;

	/**
	 * 添加一条图片消息
	 * 
	 * @param msg
	 */
	public static synchronized void addToImageMessageList(PostionMessage msg) {
		try {
			if (msg != null && msg.getId() != null) {
				postionMessageMap.put(msg.getId(), msg);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 获取图片列表
	 * 
	 * @return
	 */
	public static ArrayList<PostionMessage> getImageMessageList() {
		postionList = new ArrayList<>();
		java.util.Iterator it = postionMessageMap.keySet().iterator();
		while (it.hasNext()) {
			postionList.add(postionMessageMap.get(it.next()));
		}
		Collections.sort(postionList, new Comparator<PostionMessage>() {
			public int compare(PostionMessage postion1, PostionMessage postion2) {
				Integer a = postion1.getUpdated();
				Integer b = postion2.getUpdated();
				if (a.equals(b)) {
					return postion2.getId().compareTo(postion1.getId());
				}
				// 升序
				// return a.compareTo(b);
				// 降序
				return b.compareTo(a);
			}
		});
		return postionList;
	}

	/**
	 * 清除图片列表
	 */
	public static synchronized void clearPostionMessageList() {
		postionMessageMap.clear();
		postionMessageMap.clear();
	}

	public PostionMessage() {
		msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
	}

	/** 消息拆分的时候需要 */
	private PostionMessage(MessageEntity entity) {
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
	}

	/** 接受到网络包，解析成本地的数据 */
	public static PostionMessage parseFromNet(MessageEntity entity) {
		String strContent = entity.getContent();

		PostionMessage postionMessage = new PostionMessage(entity);
		postionMessage.setDisplayType(DBConstant.SHOW_TYPE_POSTION);

		/** 抽离出来 或者用gson */
		/*
		 * JSONObject extraContent = new JSONObject();
		 * extraContent.put("path",""); extraContent.put("url",strContent);
		 * extraContent.put("loadStatus", MessageConstant.IMAGE_UNLOAD); String
		 * imageContent = extraContent.toString();
		 */

		JSONObject extraContent = null;
		//String url = null;
		double jsonLat = 0;
		double jsonLng = 0;
		String name = null;
		try {
			extraContent = new JSONObject(strContent);
			//String path = extraContent.getString("path");
			jsonLat = extraContent.getDouble("lat");
			jsonLng = extraContent.getDouble("lng"); 
			name = extraContent.getString("address");

			// url = getMapUrl(jsonLat,jsonLng);

			//int loadStatus = extraContent.getInt("loadStatus");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// String url = extraContent.getString("url");
		postionMessage.setContent(extraContent.toString());
		// postionMessage.setUrl(url);//(strContent.isEmpty() ? null :
		// strContent);
		postionMessage.setContent(strContent);
		postionMessage.setLoadStatus(MessageConstant.IMAGE_UNLOAD);
		postionMessage.setLat(jsonLat);
		postionMessage.setLng(jsonLng);
		postionMessage.setPostionName(name);
		postionMessage.setStatus(MessageConstant.MSG_SUCCESS);
		return postionMessage;

	}

	public static PostionMessage parseFromDB(MessageEntity entity) {
		if (entity.getDisplayType() != DBConstant.SHOW_TYPE_POSTION) {
			throw new RuntimeException(
					"#ImageMessage# parseFromDB,not SHOW_IMAGE_TYPE");
		}
		PostionMessage postionMessage = new PostionMessage(entity);
		String originContent = entity.getContent();
		JSONObject extraContent;
		String name;
		try {
			extraContent = new JSONObject(originContent);
			postionMessage.setPath(extraContent.getString("path"));
			double jsonLat = extraContent.getDouble("lat");
			double jsonLng = extraContent.getDouble("lng");
			name = extraContent.getString("address"); 
			
			postionMessage.setPostionName(name);
			postionMessage.setLat(jsonLat);
			postionMessage.setLng(jsonLng); 
			
			int loadStatus =  MessageConstant.IMAGE_UNLOAD;// = extraContent.getInt("loadStatus");

			// todo temp solution
			if (loadStatus == MessageConstant.IMAGE_LOADING) {
				loadStatus = MessageConstant.IMAGE_UNLOAD;
			}

			postionMessage.setLoadStatus(loadStatus);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return postionMessage;
	}

	// 消息页面，发送图片消息
	public static PostionMessage buildForSend(ImageItem item,
			UserEntity fromUser, PeerEntity peerEntity) {
		PostionMessage msg = new PostionMessage();
		if (new File(item.getImagePath()).exists()) {
			msg.setPath(item.getImagePath());
		} else {
			if (new File(item.getThumbnailPath()).exists()) {
				msg.setPath(item.getThumbnailPath());
			} else {
				// 找不到图片路径时使用加载失败的图片展示
				msg.setPath(null);
			}
		}
		// 将图片发送至服务器
		int nowTime = (int) (System.currentTimeMillis() / 1000);

		msg.setFromId(fromUser.getPeerId());
		msg.setToId(peerEntity.getPeerId());
		msg.setCreated(nowTime);
		msg.setUpdated(nowTime);
		msg.setDisplayType(DBConstant.SHOW_TYPE_POSTION);
		// content 自动生成的
		int peerType = peerEntity.getType();
		int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_LOCATION
				: DBConstant.MSG_TYPE_SINGLE_LOCATION;
		msg.setMsgType(msgType);

		msg.setStatus(MessageConstant.MSG_SENDING);
		msg.setLoadStatus(MessageConstant.IMAGE_UNLOAD);
		msg.buildSessionKey(true);
		msg.setMessageTime(""+nowTime);
		
		return msg;
	}

	public static PostionMessage buildForSend(double lat, double lng,
			String name, UserEntity fromUser, PeerEntity peerEntity) {
		PostionMessage postionMessage = new PostionMessage();
		int nowTime = (int) (System.currentTimeMillis() / 1000);
		postionMessage.setFromId(fromUser.getPeerId());
		postionMessage.setToId(peerEntity.getPeerId());
		postionMessage.setUpdated(nowTime);
		postionMessage.setCreated(nowTime);
		postionMessage.setDisplayType(DBConstant.SHOW_TYPE_POSTION);
		postionMessage.setLat(lat);
		postionMessage.setLng(lng);
		// postionMessage.setUrl(getMapUrl(lat,lng));
		postionMessage.setPostionName(name);
		// postionMessage.setPath(takePhotoSavePath);
		int peerType = peerEntity.getType();
		int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_LOCATION
				: DBConstant.MSG_TYPE_SINGLE_LOCATION;
		postionMessage.setMsgType(msgType);

		postionMessage.setStatus(MessageConstant.MSG_SENDING);
		postionMessage.setLoadStatus(MessageConstant.IMAGE_UNLOAD);
		postionMessage.buildSessionKey(true);
		postionMessage.setMessageTime(""+nowTime);
		
		return postionMessage;
	}

	/**
	 * Not-null value.
	 */
	@Override
	public String getContent() {
		JSONObject extraContent = new JSONObject();
		try {
			extraContent.put("path", path);
			extraContent.put("lat", lat);
			extraContent.put("lng", lng);

			extraContent.put("address", postionName);
			extraContent.put("loadStatus", loadStatus);
			String imageContent = extraContent.toString();
			return imageContent;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public byte[] getSendContent() {
		// 发送的时候非常关键
		// String sendContent = MessageConstant.IMAGE_MSG_START
		// + url + MessageConstant.IMAGE_MSG_END;

		JSONObject extraContent = new JSONObject();
		try {
			extraContent.put("path", path);
			extraContent.put("lat", lat);
			extraContent.put("lng", lng); 
			
			extraContent.put("address", postionName);
			//extraContent.put("loadStatus", loadStatus);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String sendContent = extraContent.toString();
		/**
		 * 加密
		 */
		String encrySendContent = new String(com.fise.xw.Security
				.getInstance().EncryptMsg(sendContent));

		try {
			return encrySendContent.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** -----------------------set/get------------------------ */
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUrl() {
		return getMapUrl(lat, lng);
	}

	public static String getMapUrl(double x, double y) {
		String url = "http://restapi.amap.com/v3/staticmap?location=" + y + ","
				+ x + "&zoom=12&scale=2&size=230*100&markers=mid,,A:" + y + ","
				+ x + "&key=" + "ee95e52bf08006f63fd29bcfbcf21df0";

		return url;// Uri.parse(url);
	}

	 

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public String getPostionName() {
		return postionName;
	}

	public void setPostionName(String postionName) {
		this.postionName = postionName;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public int getLoadStatus() {
		return loadStatus;
	}

	public void setLoadStatus(int loadStatus) {
		this.loadStatus = loadStatus;
	}
}

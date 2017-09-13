package com.fise.xiaoyu.imservice.entity;

import android.annotation.SuppressLint;

import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.SystemConfigSp;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.MessageConstant;
import com.fise.xiaoyu.imservice.support.SequenceNumberMaker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 卡片消息
 */
@SuppressLint("NewApi")
public class CardMessage extends MessageEntity implements Serializable {

	/** 本地保存的path */
	private String user_id = "";
	private String nick = "";
	private String account = "";

	/** 图片的网络地址 */
	private String avatar = "";

	// 存储图片消息
	private static java.util.HashMap<Long, CardMessage> cardMessageMap = new java.util.HashMap<Long, CardMessage>();
	private static ArrayList<CardMessage> cardList = null;

	/**
	 * 添加一条图片消息
	 * 
	 * @param msg
	 */
	public static synchronized void addToCardMessageList(CardMessage msg) {
		try {
			if (msg != null && msg.getId() != null) {
				cardMessageMap.put(msg.getId(), msg);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 获取图片列表
	 * 
	 * @return
	 */
	public static ArrayList<CardMessage> getCardMessageList() {
		cardList = new ArrayList<>();
		java.util.Iterator it = cardMessageMap.keySet().iterator();
		while (it.hasNext()) {
			cardList.add(cardMessageMap.get(it.next()));
		}
		Collections.sort(cardList, new Comparator<CardMessage>() {
			public int compare(CardMessage image1, CardMessage image2) {
				Integer a = image1.getUpdated();
				Integer b = image2.getUpdated();
				if (a.equals(b)) {
					return image2.getId().compareTo(image1.getId());
				}
				// 升序
				// return a.compareTo(b);
				// 降序
				return b.compareTo(a);
			}
		});
		return cardList;
	}

	/**
	 * 清除图片列表
	 */
	public static synchronized void clearCardMessageList() {
		cardMessageMap.clear();
		cardMessageMap.clear();
	}

	public CardMessage() {
		msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
	}

	/** 消息拆分的时候需要 */
	private CardMessage(MessageEntity entity) {
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

	public static CardMessage parseFromNetTest(MessageEntity entity) {
		CardMessage cardMessage = new CardMessage(entity);
		cardMessage.setDisplayType(DBConstant.SHOW_TYPE_CARD);
		return cardMessage;
	}

	/** 接受到网络包，解析成本地的数据 */
	public static CardMessage parseFromNet(MessageEntity entity) {
		String strContent = entity.getContent();

		// 判断开头与结尾
		// image message todo 字符串处理下
		CardMessage cardMessage = new CardMessage(entity);

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(strContent);
			String imageUrl = "";
			String user_id = "";
			String nick = "";
			String account = "";

			if(!jsonObject.isNull("avatar")){
				imageUrl  = jsonObject.getString("avatar");
			}

			if(!jsonObject.isNull("user_id")){
				user_id = jsonObject.getString("user_id");
			}

			if(!jsonObject.isNull("nick")){
				nick = jsonObject.getString("nick");
			}

			if(!jsonObject.isNull("account")){
				account = jsonObject.getString("account");
			}

			/** 抽离出来 或者用gson */
			JSONObject extraContent = new JSONObject();
			extraContent.put("avatar", imageUrl);
			extraContent.put("user_id", user_id);
			extraContent.put("nick", nick);
			extraContent.put("account", account);

			String cardContent = extraContent.toString();
			cardMessage.setContent(cardContent);

			cardMessage.setAvatar(imageUrl.isEmpty() ? null : imageUrl);
			cardMessage.setAccount(account);
			cardMessage.setUserId(user_id);
			cardMessage.setNick(nick);
			// cardMessage.setDisplayType(entity.getDisplayType());

			cardMessage.setDisplayType(DBConstant.SHOW_TYPE_CARD);

			cardMessage.setContent(strContent);
			cardMessage.setStatus(MessageConstant.MSG_SUCCESS);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// String imageUrl =
		// strContent.substring(MessageConstant.URL_MSG_START.length());
		// imageUrl =
		// imageUrl.substring(0,imageUrl.indexOf(MessageConstant.URL_MSG_END));

		return cardMessage;
	}

	public static CardMessage parseFromDB(MessageEntity entity) {
		if (entity.getDisplayType() != DBConstant.SHOW_TYPE_CARD) {
			throw new RuntimeException(
					"#ImageMessage# parseFromDB,not SHOW_IMAGE_TYPE");
		}
		CardMessage cardMessage = new CardMessage(entity);
		String originContent = entity.getContent();
		JSONObject extraContent;
		try {
			extraContent = new JSONObject(originContent);
			if(!extraContent.isNull("avatar")){
				cardMessage.setAvatar(extraContent.getString("avatar"));
			}
			if(!extraContent.isNull("nick")){
				cardMessage.setNick(extraContent.getString("nick"));
			}

			if(!extraContent.isNull("account")){
				cardMessage.setAccount(extraContent.getString("account"));
			}
			if(!extraContent.isNull("user_id")){
				cardMessage.setUserId(extraContent.getString("user_id"));
			}


		} catch (JSONException e) {
			e.printStackTrace();
		}

		return cardMessage;
	}

	// 消息页面，发送图片消息
	public static CardMessage buildForSend(UserEntity fromUser,
			PeerEntity peerEntity, UserEntity toUser) {
		CardMessage msg = new CardMessage();

		// 将图片发送至服务器
		int nowTime = (int) (System.currentTimeMillis() / 1000);

		msg.setFromId(fromUser.getPeerId());
		msg.setToId(peerEntity.getPeerId());
		msg.setCreated(nowTime);
		msg.setUpdated(nowTime);   
		msg.setDisplayType(DBConstant.SHOW_TYPE_CARD);
		// content 自动生成的
		int peerType = peerEntity.getType();
		int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_BUSSINESS_CARD
				: DBConstant.MSG_TYPE_SINGLE_BUSSINESS_CARD;
		msg.setMsgType(msgType);
		msg.setAvatar(toUser.getUserAvatar());
		
		String aa = toUser.getUserAvatar();
		msg.setAccount(toUser.getPhone());
		msg.setNick(toUser.getMainName());   
		msg.setUserId(toUser.getPeerId() + "");
		msg.setContent(msg.getContent());

		msg.setStatus(MessageConstant.MSG_SENDING);
		msg.setMessageTime("");
		
		msg.buildSessionKey(true);
		
		return msg;
	}

	public static CardMessage buildForSend(String takePhotoSavePath,
			UserEntity fromUser, PeerEntity peerEntity) {
		CardMessage cardMessage = new CardMessage();
		int nowTime = (int) (System.currentTimeMillis() / 1000);
		cardMessage.setFromId(fromUser.getPeerId());
		cardMessage.setToId(peerEntity.getPeerId());
		cardMessage.setUpdated(nowTime);
		cardMessage.setCreated(nowTime);
		cardMessage.setDisplayType(DBConstant.SHOW_TYPE_CARD);

		int peerType = peerEntity.getType();
		int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_BUSSINESS_CARD
				: DBConstant.MSG_TYPE_SINGLE_BUSSINESS_CARD;
		cardMessage.setMsgType(msgType);

		cardMessage.setStatus(MessageConstant.MSG_SENDING);
		cardMessage.setMessageTime("");
		
		cardMessage.buildSessionKey(true);
		return cardMessage;
	}
 
	/**
	 * Not-null value.
	 */
	@Override
	public String getContent() {
		JSONObject extraContent = new JSONObject();
		try {
			extraContent.put("user_id", user_id);
			extraContent.put("nick", nick);
			extraContent.put("account", account);
			extraContent.put("avatar", avatar);

			String cardContent = extraContent.toString();
			return cardContent;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public byte[] getSendContent() {
		// 发送的时候非常关键

		JSONObject extraContent = new JSONObject();
		try {
			extraContent.put("user_id", user_id);
			extraContent.put("nick", nick);
			extraContent.put("account", account);
			extraContent.put("avatar", avatar);
			// String cardContent = extraContent.toString();

		} catch (JSONException e) {
			e.printStackTrace();
		}

		String sendContent = extraContent.toString();
		/**
		 * 加密
		 */
		String encrySendContent = new String(Security
				.getInstance().EncryptMsg(sendContent));

		try {
			return encrySendContent.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** -----------------------set/get------------------------ */
	public String getAvatar() {
		return SystemConfigSp.instance().getStrConfig(
				SystemConfigSp.SysCfgDimension.MSFSSERVER) + avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getUserId() {
		return user_id;
	}

	public void setUserId(String user_id) {
		this.user_id = user_id;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	} 

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}
}

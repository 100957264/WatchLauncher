package com.fise.xw.imservice.entity;

import com.fise.xw.DB.entity.PeerEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.config.MessageConstant;
import com.fise.xw.imservice.support.SequenceNumberMaker;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 */
public class AddFriendsMessage extends MessageEntity implements Serializable {

	public AddFriendsMessage() {
		msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
	}

	private AddFriendsMessage(MessageEntity entity) {
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

	public static AddFriendsMessage parseFromNet(MessageEntity entity) {
		AddFriendsMessage textMessage = new AddFriendsMessage(entity);
		textMessage.setStatus(MessageConstant.MSG_SUCCESS);
		textMessage.setDisplayType(DBConstant.SHOW_TYPE_ADDFRIENDS);
		return textMessage;
	}

	public static AddFriendsMessage parseFromDB(MessageEntity entity) {
		if (entity.getDisplayType() != DBConstant.SHOW_TYPE_ADDFRIENDS) {
			throw new RuntimeException(
					"#TextMessage# parseFromDB,not SHOW_ORIGIN_TEXT_TYPE");
		}
		AddFriendsMessage textMessage = new AddFriendsMessage(entity);
		return textMessage;
	}

	public static AddFriendsMessage parseFromNoteDB(MessageEntity entity) {

		AddFriendsMessage textMessage = new AddFriendsMessage(entity);
		return textMessage;
	}

	public static AddFriendsMessage buildForSend(String content,
			UserEntity fromUser, PeerEntity peerEntity) {
		AddFriendsMessage textMessage = new AddFriendsMessage();
		int nowTime = (int) (System.currentTimeMillis() / 1000);
		textMessage.setFromId(fromUser.getPeerId());
		textMessage.setToId(peerEntity.getPeerId());
		textMessage.setUpdated(nowTime);
		textMessage.setCreated(nowTime);
		textMessage.setDisplayType(DBConstant.SHOW_TYPE_ADDFRIENDS);
		textMessage.setGIfEmo(true);
		int peerType = peerEntity.getType();
		// int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ?
		// DBConstant.MSG_TYPE_GROUP_TEXT
		// : DBConstant.MSG_TYPE_SINGLE_TEXT;
		int msgType = DBConstant.MSG_TYPE_GROUP_ADD_FRIENDS;
		textMessage.setMsgType(msgType);
		textMessage.setStatus(MessageConstant.MSG_SENDING);
		// 内容的设定
		textMessage.setContent(content);
		textMessage.buildSessionKey(true);
		textMessage.setMessageTime("");

		return textMessage;
	}

	public static AddFriendsMessage buildForSend(String content,
			UserEntity fromUser, UserEntity peerEntity, int peeId) {
		AddFriendsMessage addMessage = new AddFriendsMessage();
		int nowTime = (int) (System.currentTimeMillis() / 1000);
		addMessage.setFromId(fromUser.getPeerId());
		addMessage.setToId(peerEntity.getPeerId());
		addMessage.setUpdated(nowTime);
		addMessage.setCreated(nowTime);
		addMessage.setDisplayType(DBConstant.SHOW_TYPE_ADDFRIENDS);
		addMessage.setGIfEmo(true);
		int peerType = peerEntity.getType();
		int msgType = DBConstant.MSG_TYPE_GROUP_ADD_FRIENDS;
		addMessage.setMsgType(msgType);
		addMessage.setStatus(MessageConstant.MSG_SENDING);
		// 内容的设定
		addMessage.setContent(content);
		addMessage.buildSessionKey(false);
		addMessage.setMsgId(0);
		addMessage.setMessageTime("");

		return addMessage;
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
}

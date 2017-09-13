package com.fise.xw.imservice.entity;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.MessageConstant;
import com.fise.xw.protobuf.helper.ProtoBuf2JavaBean;
import com.fise.xw.protobuf.IMBaseDefine;
import com.fise.xw.protobuf.IMBaseDefine.MsgType;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : yingmu on 15-1-6.
 * @email : yingmu@mogujie.com.
 *
 * historical reasons,娌℃湁鍏呭垎鍒╃敤msgType瀛楁
 * 澶氱鐨勫瘜鏂囨湰鐨勮�冭檻
 */
public class MsgAnalyzeEngine {
    @SuppressLint("NewApi") public static String analyzeMessageDisplay(String content){
        String finalRes = content;
        String originContent = content;
        
   	 if(!TextUtils.isEmpty(originContent)){
   		 finalRes = DBConstant.DISPLAY_FOR_MIX;
	  }else{
	      finalRes = DBConstant.DISPLAY_FOR_IMAGE;
	  }
	        
//        while (!originContent.isEmpty()) {
//        	 
//        	 if(!TextUtils.isEmpty(originContent)){
//                 finalRes = DBConstant.DISPLAY_FOR_MIX;
//             }else{
//                 finalRes = DBConstant.DISPLAY_FOR_IMAGE;
//             }
//        	 
//        	 /*
//            int nStart = originContent.indexOf(MessageConstant.IMAGE_MSG_START);
//            if (nStart < 0) {// 娌℃湁澶�
//                break;
//            } else {
//                String subContentString = originContent.substring(nStart);
//                int nEnd = subContentString.indexOf(MessageConstant.IMAGE_MSG_END);
//                if (nEnd < 0) {// 娌℃湁灏�
//                    String strSplitString = originContent;
//                    break;
//                } else {// 鍖归厤鍒�
//                    String pre = originContent.substring(0, nStart);
//
//                    originContent = subContentString.substring(nEnd
//                            + MessageConstant.IMAGE_MSG_END.length());
//
//                    if(!TextUtils.isEmpty(pre) || !TextUtils.isEmpty(originContent)){
//                        finalRes = DBConstant.DISPLAY_FOR_MIX;
//                    }else{
//                        finalRes = DBConstant.DISPLAY_FOR_IMAGE;
//                    }
//                }
//            }*/
//        }
        return finalRes;
    }


    // 鎶界鏀惧湪鍚屼竴鐨勫湴鏂�
    public static MessageEntity analyzeMessage(IMBaseDefine.MsgInfo msgInfo) {
       MessageEntity messageEntity = new MessageEntity();

       messageEntity.setCreated(msgInfo.getCreateTime());
       messageEntity.setUpdated(msgInfo.getCreateTime());
       messageEntity.setFromId(msgInfo.getFromSessionId());
       messageEntity.setMsgId(msgInfo.getMsgId());
       messageEntity.setMsgType(ProtoBuf2JavaBean.getJavaMsgType(msgInfo.getMsgType()));
       messageEntity.setStatus(MessageConstant.MSG_SUCCESS);
       messageEntity.setContent(msgInfo.getMsgData().toStringUtf8());
        /**
         * 瑙ｅ瘑鏂囨湰淇℃伅
         */
       String desMessage;
       if(msgInfo.getMsgType() == MsgType.MSG_TYPE_SINGLE_NOTICE)
       {
    	   desMessage = new String(msgInfo.getMsgData().toStringUtf8());
           messageEntity.setContent(desMessage);

       }else{
    	   desMessage = new String(com.fise.xw.Security.getInstance().DecryptMsg(msgInfo.getMsgData().toStringUtf8()));
    	   messageEntity.setContent(desMessage);
       }
  

       // 鏂囨湰淇℃伅涓嶄负绌�
       if(!TextUtils.isEmpty(desMessage)){
           List<MessageEntity> msgList =  textDecode(messageEntity);
           if(msgList.size()>1){
               // 娣峰悎娑堟伅
               MixMessage mixMessage = new MixMessage(msgList);
               return mixMessage;
           }else if(msgList.size() == 0){
              // 鍙兘瑙ｆ瀽澶辫触 榛樿杩斿洖鏂囨湰娑堟伅
             // return TextMessage.parseFromNet(messageEntity);
        	   if(msgInfo.getMsgType() == MsgType.MSG_TYPE_SINGLE_NOTICE){
        		   return NoticeMessage.parseFromNet(messageEntity); 
        	   }else if(msgInfo.getMsgType() == MsgType.MSG_TYPE_SINGLE_BUSSINESS_CARD||msgInfo.getMsgType() == MsgType.MSG_TYPE_GROUP_BUSSINESS_CARD){ 
        		   return CardMessage.parseFromNet(messageEntity);
        	   }else if(msgInfo.getMsgType() == MsgType.MSG_TYPE_SINGLE_LOCATION||msgInfo.getMsgType() == MsgType.MSG_TYPE_GROUP_LOCATION){ 
                   return PostionMessage.parseFromNet(messageEntity);
        	   }else if(msgInfo.getMsgType() == MsgType.MSG_TYPE_SINGLE_VIDIO||msgInfo.getMsgType() == MsgType.MSG_TYPE_GROUP_VIDIO){ 
                   return VedioMessage.parseFromNet(messageEntity);
        	   }else{ 
                   return TextMessage.parseFromNet(messageEntity);
        	   }
           }else{
        	   if(msgInfo.getMsgType() == MsgType.MSG_TYPE_SINGLE_NOTICE){
        		   return NoticeMessage.parseFromNet(messageEntity); 
        	   }else{
        		   //绠�鍗曟秷鎭紝杩斿洖绗竴涓� 
                   return msgList.get(0);
        	   }
               //绠�鍗曟秷鎭紝杩斿洖绗竴涓� 
               //return msgList.get(0);
           }
       }else{
           // 濡傛灉涓虹┖
    	   if(msgInfo.getMsgType() == MsgType.MSG_TYPE_SINGLE_NOTICE){
    		   return NoticeMessage.parseFromNet(messageEntity);
    		   
    	   }else if(msgInfo.getMsgType() == MsgType.MSG_TYPE_SINGLE_BUSSINESS_CARD||msgInfo.getMsgType() == MsgType.MSG_TYPE_GROUP_BUSSINESS_CARD){ 
    		   return CardMessage.parseFromNet(messageEntity);
    	   }else if(msgInfo.getMsgType() == MsgType.MSG_TYPE_SINGLE_LOCATION||msgInfo.getMsgType() == MsgType.MSG_TYPE_GROUP_LOCATION){ 
    		   return PostionMessage.parseFromNet(messageEntity);
    	   }else if(msgInfo.getMsgType() == MsgType.MSG_TYPE_GROUP_VIDIO||msgInfo.getMsgType() == MsgType.MSG_TYPE_SINGLE_VIDIO){ 
    		   return VedioMessage.parseFromNet(messageEntity);
    		   
    	   }else{  
    		   if(msgInfo.getMsgType() == MsgType.MSG_TYPE_SINGLE_NOTICE){
        		   return NoticeMessage.parseFromNet(messageEntity); 
        	   }else{
        		   return TextMessage.parseFromNet(messageEntity);
        	   } 
    	   }
       }
    }

    

    /**
     * todo 浼樺寲瀛楃涓插垎鏋�
     * @param msg
     * @return
     */
    private static List<MessageEntity> textDecode(MessageEntity msg){
        List<MessageEntity> msgList = new ArrayList<>();

        String originContent = msg.getContent();
        while (!TextUtils.isEmpty(originContent)) {
            int nStart = originContent.indexOf(MessageConstant.IMAGE_MSG_START);
            if (nStart < 0) {// 娌℃湁澶�
                String strSplitString = originContent;

                MessageEntity entity = addMessage(msg, strSplitString);
                if(entity!=null){
                    msgList.add(entity);
                }

                originContent = "";
            } else {
                String subContentString = originContent.substring(nStart);
                int nEnd = subContentString.indexOf(MessageConstant.IMAGE_MSG_END);
                if (nEnd < 0) {// 娌℃湁灏�
                    String strSplitString = originContent;

  
                    MessageEntity entity = addMessage(msg,strSplitString);
                    if(entity!=null){
                        msgList.add(entity);
                    }

                    originContent = "";
                } else {// 鍖归厤鍒�
                    String pre = originContent.substring(0, nStart);
                    MessageEntity entity1 = addMessage(msg,pre);
                    if(entity1!=null){
                        msgList.add(entity1);
                    }

                    String matchString = subContentString.substring(0, nEnd
                            + MessageConstant.IMAGE_MSG_END.length());

                    MessageEntity entity2 = addMessage(msg,matchString);
                    if(entity2!=null){
                        msgList.add(entity2);
                    }

                    originContent = subContentString.substring(nEnd
                            + MessageConstant.IMAGE_MSG_END.length());
                }
            }
        }

        return msgList;
    }


    public static MessageEntity addMessage(MessageEntity msg,String strContent) {
        if (TextUtils.isEmpty(strContent.trim())){
            return null;
        }
        msg.setContent(strContent);

//        if (strContent.startsWith(MessageConstant.IMAGE_MSG_START) 
//                && strContent.endsWith(MessageConstant.IMAGE_MSG_END)) {
//            try {
//                ImageMessage imageMessage =  ImageMessage.parseFromNet(msg);
//                return imageMessage;
//            } catch (JSONException e) {
//                // e.printStackTrace();
//                return null;
//            }
//        } 
        if(msg.getMsgType() == DBConstant.MSG_TYPE_SINGLE_IMAGE
        		||msg.getMsgType() == DBConstant.MSG_TYPE_GROUP_IMAGE
        		|| msg.getMsgType() == DBConstant.MSG_TYPE_SINGLE_AUTH_IMAGE
        		|| msg.getMsgType() == DBConstant.MSG_TYPE_GROUP_AUTH_IMAGE){
        	
		     try {
		          ImageMessage imageMessage =  ImageMessage.parseFromNet(msg);
		          return imageMessage;
		      } catch (JSONException e) {
		          // e.printStackTrace();
		          return null;
		      }
        	
        } else   if(msg.getMsgType() == DBConstant.MSG_TYPE_SINGLE_VIDIO
        		||msg.getMsgType() == DBConstant.MSG_TYPE_GROUP_VIDIO){
        	   VedioMessage imageMessage =  VedioMessage.parseFromNet(msg);
               return imageMessage;
               
        }else {
        	
			if (msg.getMsgType() == DBConstant.MSG_TYPE_SINGLE_BUSSINESS_CARD
					|| msg.getMsgType() == DBConstant.MSG_TYPE_GROUP_BUSSINESS_CARD) {
				return CardMessage.parseFromNet(msg);
			} else if (msg.getMsgType() == DBConstant.MSG_TYPE_SINGLE_NOTICE) {
				return TextMessage.parseFromNet(msg);
			} else if (msg.getMsgType() == DBConstant.MSG_TYPE_SINGLE_LOCATION
					|| msg.getMsgType() == DBConstant.MSG_TYPE_GROUP_LOCATION) {
				return PostionMessage.parseFromNet(msg);
			} else {
				return TextMessage.parseFromNet(msg);
			}
          
         }
    }
    
    

}

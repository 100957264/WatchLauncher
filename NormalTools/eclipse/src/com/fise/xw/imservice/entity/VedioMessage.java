package com.fise.xw.imservice.entity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.DB.entity.PeerEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.sp.SystemConfigSp;
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
public class VedioMessage extends MessageEntity implements Serializable {

    /**本地保存的path*/
    //private String path = "";
    private String Vediopath = "";
    
    /**Vedio的网络地址*/
   // private String url = "";
    private String Vediourl = "";
    
    private int loadStatus;
    private  Bitmap bitmap = null;
    private String path;
    

    //存储图片消息
    private static java.util.HashMap<Long,VedioMessage> vedioMessageMap = new java.util.HashMap<Long,VedioMessage>();
    private static ArrayList<VedioMessage> vedioList=null;
    /**
     * 添加一条Vedio消息
     * @param msg
     */
    public static synchronized void addToImageMessageList(VedioMessage msg){
        try {
            if(msg!=null && msg.getId()!=null)
            {
            	vedioMessageMap.put(msg.getId(),msg);
            }
        }catch (Exception e){
        }
    }

    /**
     * 获取Vedio列表
     * @return
     */
    public static ArrayList<VedioMessage> getVedioMessageList(){
    	vedioList = new ArrayList<>();
        java.util.Iterator it = vedioMessageMap.keySet().iterator();
        while (it.hasNext()) {
        	vedioList.add(vedioMessageMap.get(it.next()));
        }
        Collections.sort(vedioList, new Comparator<VedioMessage>(){
            public int compare(VedioMessage image1, VedioMessage image2) {
                Integer a =  image1.getUpdated();
                Integer b = image2.getUpdated();
                if(a.equals(b))
                {
                    return image2.getId().compareTo(image1.getId());
                }
                // 升序
                //return a.compareTo(b);
                // 降序
                return b.compareTo(a);
            }
        });
        return vedioList;
    }

    /**
     * 清除Vedio列表
     */
    public static synchronized void clearImageMessageList(){
    	vedioMessageMap.clear();
    	vedioMessageMap.clear();
    }



    public VedioMessage(){
        msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
    }

    /**消息拆分的时候需要*/
    private VedioMessage(MessageEntity entity){
        /**父类的id*/
         id =  entity.getId();
         msgId  = entity.getMsgId();
         fromId = entity.getFromId();
         toId   = entity.getToId();
        sessionKey = entity.getSessionKey();
         content=entity.getContent();
         msgType=entity.getMsgType();
         displayType=entity.getDisplayType();
         status = entity.getStatus();
         created = entity.getCreated();
         updated = entity.getUpdated();
        // path = entity.getPath();
    }

    /**接受到网络包，解析成本地的数据*/
    public static VedioMessage parseFromNet(MessageEntity msg)  {
        String strContent = msg.getContent();

        // image message todo 字符串处理下
    	VedioMessage vedioMessage = new VedioMessage(msg);
    	vedioMessage.setDisplayType(DBConstant.SHOW_TYPE_VEDIO);  
         
       // vedioUrl = vedioUrl.substring(0,vedioUrl.indexOf(MessageConstant.VEDIO_MSG_END));
         
        /**抽离出来 或者用gson*/
        JSONObject extraContent;
        String vedioUrl = null;
        String Vediopath; 
        int loadStatus;
        String vedioContent = null;
		try {
			extraContent = new JSONObject(strContent);
			vedioUrl = extraContent.getString("url");
			Vediopath = "";//extraContent.getString("path");
			loadStatus = MessageConstant.VEDIO_UNLOAD;//extraContent.getInt("status");
			vedioContent = extraContent.toString();
			  
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
     
        vedioMessage.setContent(vedioContent);
        vedioMessage.setVedioUrl(vedioUrl); 
        vedioMessage.setImagePath("");
        vedioMessage.setLoadStatus(MessageConstant.VEDIO_UNLOAD);
        vedioMessage.setStatus(MessageConstant.MSG_SUCCESS);
        return vedioMessage;
    }


    public static VedioMessage parseFromDB(MessageEntity entity)  {
        if(entity.getDisplayType() != DBConstant.SHOW_TYPE_VEDIO){
            throw new RuntimeException("#VedioMessage# parseFromDB,not SHOW_TYPE_VEDIO");
        }
        VedioMessage vedioMessage = new VedioMessage(entity);
        String originContent = entity.getContent();
        JSONObject extraContent;
        try {
            extraContent = new JSONObject(originContent); 
            vedioMessage.setVedioPath(extraContent.getString("path"));
            vedioMessage.setVedioUrl(extraContent.getString("url"));
            vedioMessage.setImagePath(extraContent.getString("Imagepath"));
            int loadStatus = extraContent.getInt("status"); 
            
            //todo temp solution
            if(loadStatus == MessageConstant.VEDIO_LOADING){
                loadStatus = MessageConstant.VEDIO_UNLOAD;
            }
            vedioMessage.setLoadStatus(loadStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return vedioMessage;
    }

    // 消息页面，发送Vedio消息
    public static VedioMessage buildForSend(ImageItem item,UserEntity fromUser,PeerEntity peerEntity){
    	VedioMessage msg = new VedioMessage();
//        if (new File(item.getImagePath()).exists()) {
//            msg.setPath(item.getImagePath());
//        } else {
//            if (new File(item.getThumbnailPath()).exists()) {
//                msg.setPath(item.getThumbnailPath());
//            } else {
//                // 找不到图片路径时使用加载失败的图片展示
//                msg.setPath(null);
//            }
//        }
        // 将图片发送至服务器
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        
        msg.setFromId(fromUser.getPeerId());
        msg.setToId(peerEntity.getPeerId());
        msg.setCreated(nowTime);
        msg.setUpdated(nowTime);
        msg.setDisplayType(DBConstant.SHOW_TYPE_VEDIO);
        // content 自动生成的
        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_VIDIO :
                DBConstant.MSG_TYPE_SINGLE_VIDIO;
        msg.setMsgType(msgType);
         
 
        msg.setStatus(MessageConstant.MSG_SENDING);
        msg.setLoadStatus(MessageConstant.VEDIO_UNLOAD);
        msg.buildSessionKey(true);
        msg.setMessageTime(""+nowTime);
        
        return msg;
    }

    public static VedioMessage buildForSend(String vedioPath,UserEntity fromUser,PeerEntity peerEntity){
    	VedioMessage vedioMessage = new VedioMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        vedioMessage.setFromId(fromUser.getPeerId());
        vedioMessage.setToId(peerEntity.getPeerId());
        vedioMessage.setUpdated(nowTime);
        vedioMessage.setCreated(nowTime);
        vedioMessage.setDisplayType(DBConstant.SHOW_TYPE_VEDIO); 
        vedioMessage.setVedioPath(vedioPath);
       
        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_VIDIO
                : DBConstant.MSG_TYPE_SINGLE_VIDIO;
        vedioMessage.setMsgType(msgType); 
 
        vedioMessage.setStatus(MessageConstant.MSG_SENDING);
        vedioMessage.setLoadStatus(MessageConstant.VEDIO_UNLOAD);
        vedioMessage.buildSessionKey(true);
        vedioMessage.setMessageTime(""+nowTime);
        
        return vedioMessage;
    }

    /**
     * Not-null value.
     */
    @Override
    public String getContent() {
        JSONObject extraContent = new JSONObject();
        try {
          //  extraContent.put("path",path);
           // extraContent.put("url",url); 
            extraContent.put("path",Vediopath);
            extraContent.put("url",Vediourl);
            
            extraContent.put("status",loadStatus);
            extraContent.put("Imagepath",path);
            
            String vedioContent = extraContent.toString();
            return vedioContent;
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
			extraContent.put("path", Vediopath);
			extraContent.put("url", Vediourl);
			extraContent.put("status", loadStatus); 
			extraContent.put("Imagepath",path);
			// String cardContent = extraContent.toString();

		} catch (JSONException e) {
			e.printStackTrace();
		}

		String sendContent = extraContent.toString();
		
        /**
         * 加密
         */
       String  encrySendContent =new String(com.fise.xw.Security.getInstance().EncryptMsg(sendContent));

        try {
            return encrySendContent.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**-----------------------set/get------------------------*/         
  
    public String getVedioPath() {
        return Vediopath;
    }        

    public void setVedioPath(String vedioPath) {
        this.Vediopath = vedioPath;
    }
    
    public String getVedioUrl() {
        return  SystemConfigSp.instance().getStrConfig(
				SystemConfigSp.SysCfgDimension.MSFSSERVER) + Vediourl;
    }
    
    
    public String getUrl() {
        return    Vediourl;
    }
          
    public void setVedioUrl(String Vediourl) {
        this.Vediourl = Vediourl;
    }
    
    public int getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(int loadStatus) {
        this.loadStatus = loadStatus;
    }
    
    
    public String  getImagePath() {
        return path; 
    }

    public void  setImagePath(String path) {
      this.path = path;
    }
     
}

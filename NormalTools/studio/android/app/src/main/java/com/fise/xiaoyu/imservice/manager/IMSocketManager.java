package com.fise.xiaoyu.imservice.manager;

import android.text.TextUtils;
import android.util.Log;

import com.fise.xiaoyu.DB.sp.SystemConfigSp;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.callback.ListenerQueue;
import com.fise.xiaoyu.imservice.callback.Packetlistener;
import com.fise.xiaoyu.imservice.event.SocketEvent;
import com.fise.xiaoyu.imservice.network.MsgServerHandler;
import com.fise.xiaoyu.imservice.network.RegistMsgServerHandler;
import com.fise.xiaoyu.imservice.network.RegistSmsServerHandler;
import com.fise.xiaoyu.imservice.network.SocketThread;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.base.DataBuffer;
import com.fise.xiaoyu.protobuf.base.DefaultHeader;
import com.fise.xiaoyu.utils.Logger;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessageLite;

import org.greenrobot.eventbus.EventBus;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 业务层面:
 * 长连接建立成功之后，就要发送登陆信息，否则15s之内就会断开
 * 所以connMsg 与 login是强耦合的关系
 */
public class IMSocketManager extends IMManager {

    private Logger logger = Logger.getLogger(IMSocketManager.class);
    private static IMSocketManager inst = new IMSocketManager();

    public static IMSocketManager instance() {
        return inst;
    }

    public IMSocketManager() {
        logger.d("login#creating IMSocketManager");
    }

    private ListenerQueue listenerQueue = ListenerQueue.instance();

    // 请求消息服务器地址
    private OkHttpClient okHttpClient = new OkHttpClient();

    /**底层socket*/
    private SocketThread msgServerThread;

    /**快速重新连接的时候需要*/
    private  MsgServerAddrsEntity currentMsgAddress = null;

    /**自身状态 */
     private SocketEvent socketStatus = SocketEvent.NONE;

    /**
     * 获取Msg地址，等待链接
     */
    @Override
    public void doOnStart() {
        socketStatus = SocketEvent.NONE;
    }


    //todo check
    @Override
    public void reset() {
        disconnectMsgServer();
        socketStatus = SocketEvent.NONE;
        currentMsgAddress = null;
    }

    /**
     * 实现自身的事件驱动
     * @param event
     */
    public void triggerEvent(SocketEvent event) {
       setSocketStatus(event);
       EventBus.getDefault().postSticky(event);
    }

    /**-------------------------------功能方法--------------------------------------*/

    public void sendRequest(GeneratedMessageLite requset,int sid,int cid){
        sendRequest(requset,sid,cid,null);
    }


    /**
     * todo check exception
     * */
    public void sendRequest(GeneratedMessageLite requset,int sid,int cid,Packetlistener packetlistener){
        int seqNo = 0;
        try{
            //组装包头 header
            com.fise.xiaoyu.protobuf.base.Header header = new DefaultHeader(sid, cid);
            int bodySize = requset.getSerializedSize();
            header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + bodySize);
            seqNo = header.getSeqnum();
            listenerQueue.push(seqNo,packetlistener);

            boolean sendRes = msgServerThread.sendRequest(requset,header);
        }catch (Exception e){
            if(packetlistener !=null){
                packetlistener.onFaild();
            }
            listenerQueue.pop(seqNo);
            logger.e("#sendRequest#channel is close!");
        }
    }

    public void packetDispatch(ChannelBuffer channelBuffer){
        DataBuffer buffer = new DataBuffer(channelBuffer);
        com.fise.xiaoyu.protobuf.base.Header header = new com.fise.xiaoyu.protobuf.base.Header();
        header.decode(buffer);
        /**buffer 的指针位于body的地方*/
        int commandId = header.getCommandId();
        int serviceId = header.getServiceId();
        int seqNo = header.getSeqnum();


        CodedInputStream codedInputStream = CodedInputStream.newInstance(new ChannelBufferInputStream(buffer.getOrignalBuffer()));

        Packetlistener listener = listenerQueue.pop(seqNo);
        if(listener!=null){
            listener.onSuccess(codedInputStream);
            return;
       }

        Log.i("aaa", "packetDispatch: "+header.getServiceId());
        // todo eric make it a table
        // 抽象 父类执行
        switch (serviceId){
            case IMBaseDefine.ServiceID.SID_LOGIN_VALUE:
            {
                IMPacketDispatcher.loginPacketDispatcher(commandId,codedInputStream);
            }
                break;
            case IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE:{
            	 IMPacketDispatcher.buddyPacketDispatcher(commandId,codedInputStream);
            	}

                break;
            case IMBaseDefine.ServiceID.SID_MSG_VALUE:{
            	 IMPacketDispatcher.msgPacketDispatcher(commandId,codedInputStream);
            	}

                break;
            case IMBaseDefine.ServiceID.SID_GROUP_VALUE:{
            	 IMPacketDispatcher.groupPacketDispatcher(commandId,codedInputStream);
            	}

                break;
            case IMBaseDefine.ServiceID.SID_USER_VALUE: {
            	 IMPacketDispatcher.userActionPacketDispatcher(commandId,codedInputStream);
            	}

                break;
            case IMBaseDefine.ServiceID.SID_DEVICE_VALUE: {
           	 	  IMPacketDispatcher.devicePacketDispatcher(commandId,codedInputStream);
           		}
               break;

            default:
                logger.e("packet#unhandled serviceId:%d, commandId:%d", serviceId,
                        commandId);
                break;
        }


    }



    /**
     * 新版本流程如下
     1.客户端通过域名获得login_server的地址
     2.客户端通过login_server获得msg_serv的地址
     3.客户端带着用户名密码对msg_serv进行登录
     4.msg_serv转给db_proxy进行认证（do not care on client）
     5.将认证结果返回给客户端
     */
    public void reqMsgServerAddrs() {
        logger.d("socket#reqMsgServerAddrs.");
        Request request = new Request.Builder()
                .header("User-Agent", "Android-TT")
                .url(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                logger.d("socket#req msgAddress Failure, errorResponse:%s", e.getMessage());
                triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_FAILED);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    logger.d("socket#req msgAddress onSuccess, response:%s", result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        MsgServerAddrsEntity msgServerAddrsEntity = onRepLoginServerAddrs(jsonObject);
                        connectMsgServer(msgServerAddrsEntity);
                        triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_SUCCESS);
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                logger.d("socket#req msgAddress Failure, errorResponse:%s", response.message());
                triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_FAILED);
            }
        });
    }




    /**
     * 新版本流程如下
     1.客户端通过域名获得login_server的地址
     2.客户端通过login_server获得msg_serv的地址
     3.客户端带着用户名密码对msg_serv进行登录
     4.msg_serv转给db_proxy进行认证（do not care on client）
     5.将认证结果返回给客户端
     */
    public void registerServerAddrs() {
        logger.d("socket#reqMsgServerAddrs.");
        Request request = new Request.Builder()
                .header("User-Agent", "Android-TT")
                .url(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                logger.d("socket#req msgAddress Failure, errorResponse:%s", e.getMessage());
                triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_FAILED);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    logger.d("socket#req msgAddress onSuccess, response:%s", result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        MsgServerAddrsEntity msgServerAddrsEntity = onRepLoginServerAddrs(jsonObject);
                        registerMsgServer(msgServerAddrsEntity);
                        triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_SUCCESS);
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                logger.d("socket#req msgAddress Failure, errorResponse:%s", response.message());
                triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_FAILED);
            }
        });
    }




    /**
     * 新版本流程如下
     */
    public void SmsServerAddrs() {
        logger.d("socket#reqMsgServerAddrs.");
        Request request = new Request.Builder()
                .header("User-Agent", "Android-TT")
                .url(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                logger.d("socket#req msgAddress Failure, errorResponse:%s", e.getMessage());
                triggerEvent(SocketEvent.REQ_MSG_SERVER_SMS_FAILED);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    logger.d("socket#req msgAddress onSuccess, response:%s", result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        MsgServerAddrsEntity msgServerAddrsEntity = onRepLoginServerAddrs(jsonObject);
                        registerSmsServer(msgServerAddrsEntity);
                        triggerEvent(SocketEvent.REQ_MSG_SERVER_SMS_SUCCESS);
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                logger.d("socket#req msgAddress Failure, errorResponse:%s", response.message());
                triggerEvent(SocketEvent.REQ_MSG_SERVER_SMS_FAILED);
            }
        });
    }


    /**
     * 与注册是强耦合的关系
     */
    private void registerSmsServer(MsgServerAddrsEntity currentMsgAddress) {
        triggerEvent(SocketEvent.CONNECTING_MSG_SERVER);
        this.currentMsgAddress = currentMsgAddress;

        String priorIP = currentMsgAddress.priorIP;
        int port = currentMsgAddress.port;
        logger.i("login#connectMsgServer -> (%s:%d)",priorIP, port);

        //check again,may be unimportance
        if (msgServerThread != null) {
            msgServerThread.close();
            msgServerThread = null;
        }

        msgServerThread = new SocketThread(priorIP, port,new RegistSmsServerHandler());
        msgServerThread.start();
    }



    /**
     * 与注册是强耦合的关系
     */
    private void registerMsgServer(MsgServerAddrsEntity currentMsgAddress) {
        triggerEvent(SocketEvent.CONNECTING_MSG_SERVER);
        this.currentMsgAddress = currentMsgAddress;

        String priorIP = currentMsgAddress.priorIP;
        int port = currentMsgAddress.port;
        logger.i("login#connectMsgServer -> (%s:%d)",priorIP, port);

        //check again,may be unimportance
        if (msgServerThread != null) {
            msgServerThread.close();
            msgServerThread = null;
        }

        msgServerThread = new SocketThread(priorIP, port,new RegistMsgServerHandler());
        msgServerThread.start();
    }



    public void reconnectMsg(){
        synchronized (IMSocketManager.class) {
            if (currentMsgAddress != null) {
                connectMsgServer(currentMsgAddress);
            } else {
                disconnectMsgServer();
                IMLoginManager.instance().relogin();
            }
        }
    }

    /**
     * 与登陆login是强耦合的关系
     */
    private void connectMsgServer(MsgServerAddrsEntity currentMsgAddress) {
        triggerEvent(SocketEvent.CONNECTING_MSG_SERVER);
        this.currentMsgAddress = currentMsgAddress;

        String priorIP = currentMsgAddress.priorIP;
        int port = currentMsgAddress.port;
        logger.i("login#connectMsgServer -> (%s:%d)",priorIP, port);

        //check again,may be unimportance
        if (msgServerThread != null) {
            msgServerThread.close();
            msgServerThread = null;
        }

        msgServerThread = new SocketThread(priorIP, port,new MsgServerHandler());
        msgServerThread.start();
    }

    /**
     * 断开与msg的链接
     */
    public void disconnectMsgServer() {
        listenerQueue.onDestory();
        logger.i("login#disconnectMsgServer");
        if (msgServerThread != null) {
            msgServerThread.close();
            msgServerThread = null;
            logger.i("login#do real disconnectMsgServer ok");
        }
    }

    /**判断链接是否处于断开状态*/
    public boolean isSocketConnect(){
        if(msgServerThread == null || msgServerThread.isClose()){
            return false;
        }
        return true;
    }

    //连接成功的处理结果
    public void onMsgServerConnected() {
        logger.i("login#onMsgServerConnected");
        listenerQueue.onStart();
        triggerEvent(SocketEvent.CONNECT_MSG_SERVER_SUCCESS);
        IMLoginManager.instance().reqLoginMsgServer();
    }


    //连接成功注册的处理结果
    public void onRegistMsgServerConnected() {
        logger.i("login#onMsgServerConnected");
        listenerQueue.onStart();
        triggerEvent(SocketEvent.CONNECT_MSG_SERVER_SUCCESS);
        IMLoginManager.instance().registMsgServer();
    }

    //连接成功注册的处理结果
    public void onRegistSmsServerConnected() {
        logger.i("login#onMsgServerConnected");
        triggerEvent(SocketEvent.REQ_MSG_SERVER_SMS_SUCCESS);
        IMLoginManager.instance().registSmsServer();

    }


    /**
     * 1. kickout 被踢出会触发这个状态   -- 不需要重连
     * 2. 心跳包没有收到 会触发这个状态   -- 链接断开，重连
     * 3. 链接主动断开                 -- 重连
     * 之前的长连接状态 connected
     */
    // 先断开链接
    // only 2 threads(ui thread, network thread) would request sending  packet
    // let the ui thread to close the connection
    // so if the ui thread has a sending task, no synchronization issue
    public void onMsgServerDisconn(){
        logger.w("login#onMsgServerDisconn");
        disconnectMsgServer();
        triggerEvent(SocketEvent.MSG_SERVER_DISCONNECTED);
    }

    /** 之前没有连接成功*/
    public void onConnectMsgServerFail(){
        triggerEvent(SocketEvent.CONNECT_MSG_SERVER_FAILED);
    }


    /**----------------------------请求Msg server地址--实体信息--------------------------------------*/
    /**请求返回的数据*/
    private class MsgServerAddrsEntity {
        int code;
        String msg;
        String priorIP;
        String backupIP;
        int port;
        @Override
        public String toString() {
            return "LoginServerAddrsEntity{" +
                    "code=" + code +
                    ", msg='" + msg + '\'' +
                    ", priorIP='" + priorIP + '\'' +
                    ", backupIP='" + backupIP + '\'' +
                    ", port=" + port +
                    '}';
        }
    }

    private MsgServerAddrsEntity onRepLoginServerAddrs(JSONObject json)
            throws JSONException {

        logger.d("login#onRepLoginServerAddrs");

        if (json == null) {
            logger.e("login#json is null");
            return null;
        }

        logger.d("login#onRepLoginServerAddrs json:%s", json);

        int code = json.getInt("code");
        if (code != 0) {
            logger.e("login#code is not right:%d, json:%s", code, json);
            return null;
        }

        String priorIP = json.getString("priorIP");
        String backupIP =  json.getString("backupIP");
        int port = json.getInt("port");

        if(json.has("msfsPrior"))
        {

            String msfsPrior = json.getString("msfsPrior");
            String msfsBackup = json.getString("msfsBackup");

            if(!TextUtils.isEmpty(msfsPrior))
            {
                SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER,msfsPrior);
            }
            else
            {
                SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER,msfsBackup);
            }
        }

        if(json.has("discovery"))
        {
            String discoveryUrl = json.getString("discovery");
            if(!TextUtils.isEmpty(discoveryUrl))
            {
                SystemConfigSp.instance().init(ctx.getApplicationContext());
                SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.DISCOVERYURI,discoveryUrl);
            }
        }

        MsgServerAddrsEntity addrsEntity = new MsgServerAddrsEntity();
        addrsEntity.priorIP = priorIP;
        addrsEntity.backupIP = backupIP;
        addrsEntity.port = port;
        logger.d("login#got loginserverAddrsEntity:%s", addrsEntity);
        return addrsEntity;
    }

    /**------------get/set----------------------------*/
    public SocketEvent getSocketStatus() {
        return socketStatus;
    }

    public void setSocketStatus(SocketEvent socketStatus) {
        this.socketStatus = socketStatus;
    }
}

package com.fise.marechat.client;


import com.fise.marechat.bean.TargetInfo;
import com.fise.marechat.bean.msg.TcpMsg;
import com.fise.marechat.client.state.ClientState;
import com.fise.marechat.listener.TcpClientListener;
import com.fise.marechat.client.msg.MsgInOut;
import com.fise.marechat.thread.HeartBeatThread;
import com.fise.marechat.utils.CharsetUtil;
import com.fise.marechat.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * tcp客户端
 */
public class TcpClient extends BaseSocket {
    public static final String TAG = "TcpClient";
    protected TargetInfo mTargetInfo;//目标ip和端口号
    protected Socket mSocket;
    protected ClientState mClientState;
    protected TcpConnConfig mTcpConnConfig;
    protected ConnectionThread mConnectionThread;
    protected SendThread mSendThread;
    protected ReceiveThread mReceiveThread;
    protected List<TcpClientListener> mTcpClientListeners;
    private LinkedBlockingQueue<TcpMsg> msgQueue;

    private TcpClient() {
        super();
        mTargetInfo = new TargetInfo(GlobalSettings.getIP(), GlobalSettings.PORT);
        mTcpConnConfig = GlobalSettings.instance().getConfig();
        init(mTargetInfo, mTcpConnConfig);
        HeartBeatThread.instance();//主线程初始化
    }

    private static class SingletonHolder {
        private static final TcpClient INSTANCE = new TcpClient();
    }

    public static TcpClient instance() {
        return SingletonHolder.INSTANCE;
    }

    private void init(TargetInfo targetInfo, TcpConnConfig connConfig) {
        this.mTargetInfo = targetInfo;
        mClientState = ClientState.Disconnected;
        mTcpClientListeners = new ArrayList<>();
        if (mTcpConnConfig == null && connConfig == null) {
            mTcpConnConfig = new TcpConnConfig.Builder().create();
        } else if (connConfig != null) {
            mTcpConnConfig = connConfig;
        }
    }

    public synchronized TcpMsg sendMsg(String message) {
        TcpMsg msg = new TcpMsg(message, MsgInOut.Send);
        return sendMsg(msg);
    }

    public synchronized TcpMsg sendMsg(byte[] message) {
        TcpMsg msg = new TcpMsg(message, MsgInOut.Send);
        return sendMsg(msg);
    }

    public synchronized TcpMsg sendMsg(TcpMsg msg) {
        if (isDisconnected()) {
            LogUtils.d("发送消息 " + msg + "，当前没有tcp连接，先进行连接");
            connect();
        }
        boolean re = enqueueTcpMsg(msg);
        LogUtils.e("发送消息：" + msg + " 成功 ? " + re);
        if (re) {
            return msg;
        }
        return null;
    }

    public synchronized boolean cancelMsg(TcpMsg msg) {
        return getSendThread().cancel(msg);
    }

    public synchronized boolean cancelMsg(int msgId) {
        return getSendThread().cancel(msgId);
    }

    public synchronized void connect() {
        if (!isDisconnected()) {
            LogUtils.d("已经连接了或正在连接");
        }
        LogUtils.d("tcp connecting");
        setClientState(ClientState.Connecting);//正在连接
        getConnectionThread().start();
    }

    public synchronized Socket getSocket() {
        if (mSocket == null || isDisconnected() || !mSocket.isConnected()) {
            mSocket = new Socket();
            try {
                mSocket.setSoTimeout((int) mTcpConnConfig.getReceiveTimeout());
            } catch (SocketException e) {
                LogUtils.e(e);
            }
        }
        return mSocket;
    }

    public synchronized void disconnect() {
        disconnect("手动关闭tcpclient", null);
    }

    protected synchronized void onErrorDisConnect(String msg, Exception e) {
        if (isDisconnected()) {
            return;
        }
        disconnect(msg, e);
//        if (mTcpConnConfig.isReconnect()) {
//            connect();
//        }
        HeartBeatThread.instance().setConnnectedState(false);
    }

    protected synchronized void disconnect(String msg, Exception e) {
        if (isDisconnected()) {
            return;
        }
        closeSocket();
        getConnectionThread().interrupt();
        getSendThread().interrupt();
        getReceiveThread().interrupt();
        setClientState(ClientState.Disconnected);
        notifyDisconnected(msg, e);
        LogUtils.d("tcp closed");
    }

    private synchronized boolean closeSocket() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                LogUtils.e(e);
            }
        }
        return true;
    }

    //连接已经连接，接下来的流程，创建发送和接受消息的线程
    private void onConnectSuccess() {
        LogUtils.d("tcp connect 建立成功");
        setClientState(ClientState.Connected);//标记为已连接
        HeartBeatThread.instance().setConnnectedState(true);
        getSendThread().start();
        getReceiveThread().start();

    }

    /**
     * tcp连接线程
     */
    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            try {
                int localPort = mTcpConnConfig.getLocalPort();
                if (localPort > 0) {
                    if (!getSocket().isBound()) {
                        getSocket().bind(new InetSocketAddress(localPort));
                    }
                }
                getSocket().connect(new InetSocketAddress(mTargetInfo.getIp(), mTargetInfo.getPort()),
                        (int) mTcpConnConfig.getConnTimeout());
                LogUtils.d("创建连接成功,target=" + mTargetInfo + ",localport=" + localPort);
            } catch (Exception e) {
                LogUtils.d("创建连接失败,target=" + mTargetInfo + "," + e);
                onErrorDisConnect("创建连接失败", e);
                return;
            }
            notifyConnected();
            onConnectSuccess();
        }
    }

    public boolean enqueueTcpMsg(final TcpMsg tcpMsg) {
        if (tcpMsg == null || getMsgQueue().contains(tcpMsg)) {
            return false;
        }
        try {
            getMsgQueue().put(tcpMsg);
            return true;
        } catch (InterruptedException e) {
            LogUtils.e(e);
        }
        return false;
    }

    protected LinkedBlockingQueue<TcpMsg> getMsgQueue() {
        if (msgQueue == null) {
            msgQueue = new LinkedBlockingQueue<>();
        }
        return msgQueue;
    }

    private class SendThread extends Thread {
        private TcpMsg sendingTcpMsg;

        protected SendThread setSendingTcpMsg(TcpMsg sendingTcpMsg) {
            this.sendingTcpMsg = sendingTcpMsg;
            return this;
        }

        public TcpMsg getSendingTcpMsg() {
            return this.sendingTcpMsg;
        }

        public boolean cancel(TcpMsg packet) {
            return getMsgQueue().remove(packet);
        }

        public boolean cancel(int tcpMsgID) {
            return getMsgQueue().remove(new TcpMsg(tcpMsgID));
        }

        @Override
        public void run() {
            TcpMsg msg;
            try {
                while (isConnected() && !Thread.interrupted() && (msg = getMsgQueue().take()) != null) {
                    setSendingTcpMsg(msg);//设置正在发送的
                    LogUtils.d("tcp sending msg=" + msg);
                    byte[] data = msg.getSourceDataBytes();
                    if (data == null) {//根据编码转换消息
                        data = CharsetUtil.stringToData(msg.getSourceDataString(), mTcpConnConfig.getCharsetName());
                    }
                    if (data != null && data.length > 0) {
                        try {
                            getSocket().getOutputStream().write(data);
                            getSocket().getOutputStream().flush();
                            msg.setTime();
                            notifySended(msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                            onErrorDisConnect("发送消息失败", e);
                            return;
                        }
                    }
                }
            } catch (InterruptedException e) {
                LogUtils.e(e);
            }
        }
    }

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            try {
                InputStream is = getSocket().getInputStream();
                while (isConnected() && !Thread.interrupted()) {
                    byte[] result = mTcpConnConfig.getStickPackageHelper().execute(is);//粘包处理
                    if (result == null) {//报错
                        LogUtils.d("tcp Receive 粘包处理失败 " + Arrays.toString(result));
                        onErrorDisConnect("粘包处理中发送错误", null);
                        break;
                    }
                    String data = CharsetUtil.dataToString(result, mTcpConnConfig.getCharsetName());
                    LogUtils.e("tcp Receive result" + data);
                    LogUtils.d("tcp Receive 解决粘包之后的数据 " + Arrays.toString(result));
                    TcpMsg tcpMsg = new TcpMsg(data, MsgInOut.Receive);
                    tcpMsg.setTime();
                    tcpMsg.setSourceDataString(data);
                    boolean va = mTcpConnConfig.getValidationHelper().execute(result);
                    if (!va) {
                        LogUtils.d("tcp Receive 数据验证失败 ");
                        notifyValidationFail(tcpMsg);//验证失败
                        continue;
                    }
                    byte[][] decodebytes = mTcpConnConfig.getDecodeHelper().execute(result, mTargetInfo, mTcpConnConfig);
                    tcpMsg.setEndDecodeData(decodebytes);
                    LogUtils.d("tcp Receive  succ msg= " + tcpMsg);
                    notifyReceive(tcpMsg);//notify listener
                }
            } catch (Exception e) {
                LogUtils.d("tcp Receive  error  " + e);
                onErrorDisConnect("接受消息错误", e);
            }
        }
    }

    protected ReceiveThread getReceiveThread() {
        if (mReceiveThread == null || !mReceiveThread.isAlive()) {
            mReceiveThread = new ReceiveThread();
        }
        return mReceiveThread;
    }

    protected SendThread getSendThread() {
        if (mSendThread == null || !mSendThread.isAlive()) {
            mSendThread = new SendThread();
        }
        return mSendThread;
    }

    protected ConnectionThread getConnectionThread() {
        if (mConnectionThread == null || !mConnectionThread.isAlive() || mConnectionThread.isInterrupted()) {
            mConnectionThread = new ConnectionThread();
        }
        return mConnectionThread;
    }

    public ClientState getClientState() {
        return mClientState;
    }

    protected void setClientState(ClientState state) {
        if (mClientState != state) {
            mClientState = state;
        }
    }

    public boolean isDisconnected() {
        return getClientState() == ClientState.Disconnected;
    }

    public boolean isConnected() {
        return getClientState() == ClientState.Connected;
    }

    private void notifyConnected() {
        TcpClientListener l;
        for (TcpClientListener wl : mTcpClientListeners) {
            final TcpClientListener finalL = wl;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finalL.onConnected(TcpClient.this);
                }
            });
        }
    }

    private void notifyDisconnected(final String msg, final Exception e) {
        TcpClientListener l;
        for (TcpClientListener wl : mTcpClientListeners) {
            final TcpClientListener finalL = wl;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finalL.onDisconnected(TcpClient.this, msg, e);
                }
            });
        }
    }


    private void notifyReceive(final TcpMsg tcpMsg) {
        for (TcpClientListener wl : mTcpClientListeners) {
            final TcpClientListener finalL = wl;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finalL.onReceive(TcpClient.this, tcpMsg);
                }
            });
        }
    }

    private void notifySended(final TcpMsg tcpMsg) {
        for (TcpClientListener wl : mTcpClientListeners) {
            final TcpClientListener finalL = wl;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finalL.onSended(TcpClient.this, tcpMsg);
                }
            });
        }
    }

    private void notifyValidationFail(final TcpMsg tcpMsg) {
        TcpClientListener l;
        for (TcpClientListener wl : mTcpClientListeners) {
            final TcpClientListener finalL = wl;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finalL.onValidationFail(TcpClient.this, tcpMsg);
                }
            });
        }
    }

    public TargetInfo getTargetInfo() {
        return mTargetInfo;
    }

    public void addTcpClientListener(TcpClientListener listener) {
        if (mTcpClientListeners.contains(listener)) {
            return;
        }
        mTcpClientListeners.add(listener);
    }

    public void removeTcpClientListener(TcpClientListener listener) {
        mTcpClientListeners.remove(listener);
    }

    @Override
    public String toString() {
        return "TcpClient{" +
                "mTargetInfo=" + mTargetInfo + ",state=" + mClientState + ",isconnect=" + isConnected() +
                '}';
    }
}

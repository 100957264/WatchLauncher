package com.fise.marechat.bean.msg;

import com.fise.marechat.client.msg.MsgInOut;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 */
public class TcpMsg {

    protected static final AtomicInteger IDAtomic = new AtomicInteger();
    protected byte[] sourceDataBytes;//数据源
    protected String sourceDataString;//数据源
    protected int id;
    protected long time;//发送、接受消息的时间戳
    protected MsgInOut msgInOut = MsgInOut.Send;
    protected byte[][] endDecodeData;
    private boolean needReply;
    private String headerType;
    private String header;
    private String content;
    private String sourceFrom;//来信人

    //[厂商*设备ID*内容长度*内容]


    public TcpMsg() {
    }

    public TcpMsg(int id) {
        this.id = id;
    }

    public TcpMsg(byte[] data, MsgInOut inOut) {
        this.sourceDataBytes = data;
        this.msgInOut = inOut;
        init();
    }

    public TcpMsg(String data, MsgInOut inOut) {
        this.sourceDataString = format(data);
        this.msgInOut = inOut;
        init();
    }

    public TcpMsg(String headerType, String content,String data,MsgInOut inOut) {
        this.headerType = headerType;
        this.content = content;
        this.sourceDataString = data;
        this.msgInOut = inOut;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getHeaderType() {
        return headerType;
    }

    public void setHeaderType(String headerType) {
        this.headerType = headerType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTime() {
        time = System.currentTimeMillis();
    }

    private void init() {
        id = IDAtomic.getAndIncrement();
    }

    public long getTime() {
        return time;
    }

    public byte[][] getEndDecodeData() {
        return endDecodeData;
    }

    public void setEndDecodeData(byte[][] endDecodeData) {
        this.endDecodeData = endDecodeData;
    }

    public MsgInOut getMsgInOut() {
        return msgInOut;
    }

    public void setMsgInOut(MsgInOut msgInOut) {
        this.msgInOut = msgInOut;
    }

    public boolean isNeedReply() {
        return needReply;
    }

    public void setNeedReply(boolean needReply) {
        this.needReply = needReply;
    }

    @Override
    public int hashCode() {
        return id;
    }


    public byte[] getSourceDataBytes() {
        return sourceDataBytes;
    }

    public void setSourceDataBytes(byte[] sourceDataBytes) {
        this.sourceDataBytes = sourceDataBytes;
    }

    public String getSourceDataString() {
        return sourceDataString;
    }

    public void setSourceDataString(String sourceDataString) {
        this.sourceDataString = format(sourceDataString);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSourceFrom() {
        return sourceFrom;
    }

    public void setSourceFrom(String sourceFrom) {
        this.sourceFrom = sourceFrom;
    }

    public static AtomicInteger getIDAtomic() {
        return IDAtomic;
    }

    public String format(String source) {
        return source;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TcpMsg tcpMsg = (TcpMsg) o;
        return id == tcpMsg.id;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (endDecodeData != null) {
            for (byte[] bs : endDecodeData) {
                sb.append(Arrays.toString(bs));
            }
        }
        return "TcpMsg{" +
                "sourceDataBytes=" + Arrays.toString(sourceDataBytes) +
                ", sourceDataString='" + sourceDataString + '\'' +
                ", id=" + id +
                ", time=" + time +
                ", msgInOut=" + msgInOut.toString() +
                ", needReply=" + needReply +
                ", headerType='" + headerType + '\'' +
                ", header=" + header +
                ", content='" + content + '\'' +
                ", endDecodeData=" + sb.toString() +
                '}';
    }
}

package com.fise.marechat.client.helper.decode;


import com.fise.marechat.client.TcpConnConfig;
import com.fise.marechat.bean.TargetInfo;

public class BaseDecodeHelper implements AbsDecodeHelper {
    @Override
    public byte[][] execute(byte[] data, TargetInfo targetInfo, TcpConnConfig tcpConnConfig) {
        return new byte[][]{data};
    }
}

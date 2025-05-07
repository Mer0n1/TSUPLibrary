package com.tsup.library;

import com.tsup.crypto.AEADUtils;

import javax.crypto.SecretKey;
import java.util.Map;

public abstract class TSUPBaseSocket {
    protected static Map<String, Object> rsaKeys;
    protected static SecretKey aeadKey;
    protected TSUPContext context;
    protected TSUPConnectionManager tsupConnectionManager;

    public void sendMessage(String message) throws Exception {

        if (tsupConnectionManager.getStatus() == TSUPConnectionManager.StatusConnection.connected) {

            byte[] nonce = AEADUtils.generateIv();
            byte[] data = AEADUtils.encrypt(message.getBytes(), nonce, aeadKey);

            context.sendData(data, nonce);
        } else
            throw new Exception("Connection disconnected");
    }

    public void disconnect() {
        if (tsupConnectionManager.getStatus() != TSUPConnectionManager.StatusConnection.disconnected)
            tsupConnectionManager.disconnect();
    }

    public void setOnMessageReceiver(TSUPMessageHandler callback) {
        tsupConnectionManager.setOnMessageListener(callback);
    }

    public synchronized static SecretKey getAeadKey() {
        return aeadKey;
    }

    public TSUPConnectionManager.StatusConnection getStatus() {
        if (tsupConnectionManager != null)
            return tsupConnectionManager.getStatus();
        else
            return TSUPConnectionManager.StatusConnection.none;
    }
}

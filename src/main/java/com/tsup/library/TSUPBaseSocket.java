package com.tsup.library;

import com.tsup.crypto.AEADUtils;

public abstract class TSUPBaseSocket {
    protected CryptoContext cryptoContext;
    protected TSUPContext context;
    protected TSUPConnectionManagerBase tsupConnectionManager;

    /** Стандартный порт прослушивания.
     * Например при создании клиента мы не указываем собственный порт прослушивания,
     * а устанавливаем стандартный*/
    public static final int myStandardPort = 6060;

    public void sendMessage(String message) throws Exception {

        if (tsupConnectionManager.getStatus() == TSUPConnectionManagerBase.StatusConnection.connected &&
                cryptoContext != null) {

            byte[] nonce = AEADUtils.generateIv();
            byte[] data = AEADUtils.encrypt(message.getBytes(), nonce, cryptoContext.getAeadKey());

            context.sendData(data, nonce);
        } else
            throw new Exception("Connection disconnected");
    }

    public void disconnect() {
        if (tsupConnectionManager.getStatus() != TSUPConnectionManagerBase.StatusConnection.disconnected)
            tsupConnectionManager.disconnect();
    }

    public void setOnMessageReceiver(TSUPMessageHandler callback) {
        tsupConnectionManager.setOnMessageListener(callback);
    }

    public TSUPConnectionManagerBase.StatusConnection getStatus() {
        if (tsupConnectionManager != null)
            return tsupConnectionManager.getStatus();
        else
            return TSUPConnectionManagerBase.StatusConnection.none;
    }
}

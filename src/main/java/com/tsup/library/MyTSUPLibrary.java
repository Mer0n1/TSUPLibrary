package com.tsup.library;

import com.tsup.crypto.AEADUtils;
import com.tsup.crypto.RSAUtils;

import javax.crypto.SecretKey;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

/**
 * Класс-надстройка над TSUPSendLibrary с которой разработчик может контактировать.
 * Ключи rsa и aead создаются внутри класса MyTSUPLibrary.
 * */
public class MyTSUPLibrary {

    private CryptoContext cryptoContext;
    private TSUPContext context;
    private TSUPConnectionManager tsupConnectionManager;

    public void startClient(String serverIp) throws Exception {
        if (getStatus() != TSUPConnectionManager.StatusConnection.connected) {

            //RSA ключи создаются на клиенте и пересылается публичный ключ серверу.
            Map<String, Object> rsaKeys = RSAUtils.initKey();

            //Создание контекста под клиента.
            context = new TSUPContext(new DatagramSocket(TSUPConnectionManager.getMyPort()),
                    InetAddress.getByName(serverIp), 6061); //todo посколько у нас локальное соединение добавляем новый порт - это тест.

            tsupConnectionManager = new TSUPConnectionManager(context);

            //Запускаем handshake
            tsupConnectionManager.handshake(rsaKeys, context);
            //
            cryptoContext = tsupConnectionManager.getCryptoContext();
            //после удачного handshake запускаем диспатчер
            tsupConnectionManager.startDispatcher();

        } else
            throw new Exception("active connection exist");
    }

    public void startServer() throws Exception {
        if (getStatus() != TSUPConnectionManager.StatusConnection.connected) {

            tsupConnectionManager = new TSUPConnectionManager();

            SecretKey aeadKey = AEADUtils.generateKey();

            //слушаем handshake
            context = tsupConnectionManager.listenerHandshake(aeadKey);
            //
            cryptoContext = tsupConnectionManager.getCryptoContext();
            //Запуск диспетчера
            tsupConnectionManager.startDispatcher();

        } else
            throw new Exception("active connection exist");
    }

    public void sendMessage(String message) throws Exception {

        if (tsupConnectionManager.getStatus() == TSUPConnectionManager.StatusConnection.connected &&
                cryptoContext != null) {

            byte[] nonce = AEADUtils.generateIv();
            byte[] data = AEADUtils.encrypt(message.getBytes(), nonce, cryptoContext.getAeadKey());

            //context.sendData(data, nonce);
            tsupConnectionManager.sendMessage(data, nonce);
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

    public TSUPConnectionManager.StatusConnection getStatus() {
        if (tsupConnectionManager != null)
            return tsupConnectionManager.getStatus();
        else
            return TSUPConnectionManager.StatusConnection.none;
    }
}

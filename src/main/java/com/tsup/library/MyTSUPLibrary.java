package com.tsup.library;

import com.tsup.crypto.AEADUtils;
import com.tsup.crypto.RSAUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Класс-надстройка над TSUPSendLibrary с которой разработчик может контактировать.
 * Ключи rsa и aead создаются внутри класса MyTSUPLibrary.
 * */
public class MyTSUPLibrary {
    private static Map<String, Object> rsaKeys;
    private static SecretKey aeadKey;
    private TSUPContext context;
    private TSUPConnectionManager tsupConnectionManager;

    public void startClient(String serverIp) throws Exception {
        if (getStatus() != TSUPConnectionManager.StatusConnection.connected) {

            //RSA ключи создаются на клиенте и пересылается публичный ключ серверу.
            rsaKeys = RSAUtils.initKey();

            //Создание контекста под клиента.
            context = new TSUPContext(new DatagramSocket(TSUPConnectionManager.getMyPort()),
                    InetAddress.getByName(serverIp), 6061); //todo посколько у нас локальное соединение добавляем новый порт - это тест.

            tsupConnectionManager = new TSUPConnectionManager(context);

            //Запускаем handshake
            aeadKey = tsupConnectionManager.handshake(rsaKeys, context);
            //после удачного handshake запускаем диспатчер
            tsupConnectionManager.startDispatcher();

        } else
            throw new Exception("active connection exist");
    }

    public void startServer() throws Exception {
        if (getStatus() != TSUPConnectionManager.StatusConnection.connected) {

            tsupConnectionManager = new TSUPConnectionManager();
            aeadKey = AEADUtils.generateKey();

            //слушаем handshake
            context = tsupConnectionManager.listenerHandshake(aeadKey);
            //Запуск диспетчера
            tsupConnectionManager.startDispatcher();

        } else
            throw new Exception("active connection exist");
    }

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

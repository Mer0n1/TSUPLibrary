package com.tsup.library;

import com.tsup.crypto.RSAUtils;
import com.tsup.protocol.Segment;
import com.tsup.protocol.SegmentUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class TSUPConnectionManager {

    enum StatusConnection { none, handshake, connected, disconnected }

    private DispatcherSocket dispatcher;
    private StatusConnection status = StatusConnection.none;
    //todo статусы - handshake, connected, disconnected; метод отключения по типы дисконект

    //Зарегистрированный под TSUP порт.
    private final static int myPort = 6060;
    private final int listenerBufferSize = 512;

    /** В случае если мы сервер.
     *  Создание диспатчера происходит в listenerHandshake. */
    public TSUPConnectionManager() {}

    /** В случае если это клиент.
     *  Передаем контекст и создаем диспатчер. */
    public TSUPConnectionManager(TSUPContext context) {
        //this.context = context;
        dispatcher = new DispatcherSocket(context);
    }

    public void startDispatcher() {
        dispatcher.setOnDisconnectHandler(new DisconnectHandler() {
            @Override
            public void onDisconnect() {
                status = StatusConnection.disconnected;
            }
        });
        dispatcher.startListener();
    }

    public void setOnMessageListener(TSUPMessageHandler callback) {
        if (dispatcher != null)
            dispatcher.setOnMessageListener(callback);
    }

    /** Проводит Handshake и возвращает AEAD ключ.*/
    public SecretKey handshake(Map<String, Object> rsaKeys, TSUPContext context) {

        try {
            status = StatusConnection.handshake;

            CompletableFuture<byte[]> handshakeFuture = CompletableFuture.supplyAsync(() -> {
                RSAPublicKey publicKey = (RSAPublicKey) rsaKeys.get("PUBLIC_KEY");

                try {
                    //отправляем публичный ключ.
                    context.sendINIT(RSAUtils.encryptBASE64(publicKey.getEncoded()).getBytes(StandardCharsets.UTF_8));

                    byte[] buffer = new byte[listenerBufferSize];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    context.getSocket().receive(packet);

                    return Arrays.copyOfRange(packet.getData(), 0, packet.getLength());

                } catch (IOException e) {
                    status = StatusConnection.none;
                    throw new RuntimeException(e);
                }
            });

            //получаем зашифрованный aead
            byte[] aeadKey = handshakeFuture.get();

            //разбираем на сегмент
            Segment segment = SegmentUtils.fromByteArray(aeadKey);

            //расшифровываем aead ключ своим приватным рса.
            String rsaPrivKey = RSAUtils.getPrivateKey(rsaKeys);
            byte[] aeadKeyDec = RSAUtils.decryptByPrivateKey(segment.encryptedPayloadWithAuthTag, rsaPrivKey);

            status = StatusConnection.connected;

            return new SecretKeySpec(aeadKeyDec, 0, aeadKeyDec.length, "AES");

        } catch (Exception e) {
            status = StatusConnection.none;
            throw new RuntimeException(e);
        }
    }

    /** Прослушивает и при удачном handshake возвращает aead ключ. */
    public TSUPContext listenerHandshake(SecretKey aeadKey) {
        try {
            status = StatusConnection.handshake;

            byte[] buffer = new byte[listenerBufferSize];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            DatagramSocket datagramSocket = new DatagramSocket(myPort);
            datagramSocket.receive(packet);
            byte[] actualData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength()); //обрезаем лишнее

            //создаем сегмент
            Segment segment = SegmentUtils.fromByteArray(actualData);

            //Получаем публичный rsa и создаем aead ключ
            String rsaKey = new String(segment.encryptedPayloadWithAuthTag, StandardCharsets.UTF_8);

            //готовим aead ключ для отправки. Шифруем его рса.
            byte[] aeadKeyBytes = RSAUtils.encryptByPublicKey(aeadKey.getEncoded(), rsaKey);

            //Создание контекста
            TSUPContext context = new TSUPContext(datagramSocket, packet.getAddress(), 6061);
            dispatcher = new DispatcherSocket(context);

            //отправляем ответ с зашифрованным aead ключем.
            context.sendHandshakeACK(aeadKeyBytes);

            status = StatusConnection.connected;

            return context;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Отключение на уровне статусов и диспатчера. */
    public void disconnect()  {
        try {
            dispatcher.makeDisconnect();

            status = StatusConnection.disconnected;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public StatusConnection getStatus() {
        return status;
    }

    public static int getMyPort() { return myPort; }
}

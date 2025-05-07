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

/** Более высокоуровневая обертка.
 * Статусы, диспатчер, handshake, ack-статистика*/
public class TSUPConnectionManager {

    enum StatusConnection { none, handshake, connected, disconnected }

    private DispatcherSocket dispatcher;
    private StatusConnection status = StatusConnection.none;
    private ACKMonitor ackMonitor;
    private TSUPContext context;
    private CryptoContext cryptoContext;

    //Зарегистрированный под TSUP порт.
    private final static int myPort = 6060;
    private final int listenerBufferSize = 512;

    /** В случае если мы сервер.
     *  Создание диспатчера происходит в listenerHandshake. */
    public TSUPConnectionManager() {
        ackMonitor = new ACKMonitor(this::disconnect);
    }

    /** В случае если это клиент.
     *  Передаем контекст и создаем диспатчер. */
    public TSUPConnectionManager(TSUPContext context) {
        this.context = context;
        ackMonitor = new ACKMonitor(this::disconnect);
    }

    public void startDispatcher() throws Exception {
        if ((context == null && cryptoContext != null) || status != StatusConnection.connected)
            throw new Exception("not connected");

        dispatcher = new DispatcherSocket(new ConnectionContext(ackMonitor, context, cryptoContext)); //перенес сюда
        dispatcher.setOnDisconnectHandler(new DisconnectHandler() {
            @Override
            public void onDisconnect(String message) {
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
            byte[] aeadKeyDec = RSAUtils.decryptByPrivateKey(segment.payload, rsaPrivKey);
            SecretKey secretKey = new SecretKeySpec(aeadKeyDec, 0, aeadKeyDec.length, "AES");
            //создание контекста крипто
            cryptoContext = new CryptoContext(secretKey);

            status = StatusConnection.connected;

            return secretKey;

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
            String rsaKey = new String(segment.payload, StandardCharsets.UTF_8);

            //готовим aead ключ для отправки. Шифруем его рса.
            byte[] aeadKeyBytes = RSAUtils.encryptByPublicKey(aeadKey.getEncoded(), rsaKey);

            //Создание контекста
            TSUPContext context = new TSUPContext(datagramSocket, packet.getAddress(), 6061);
            cryptoContext = new CryptoContext(aeadKey);

            //отправляем ответ с зашифрованным aead ключем.
            context.sendHandshakeACK(aeadKeyBytes);

            status = StatusConnection.connected;

            this.context = context;

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
            System.out.println("DISCONNECT");
            dispatcher.makeDisconnect();
            status = StatusConnection.disconnected;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CryptoContext getCryptoContext() {
        return cryptoContext;
    }

    /** Использование отправки сообщения с учетом ack-проверки.
     * В случае мертвого соединения происходит timeout и disconnect. */
    public void sendMessage(byte[] data, byte[] nonce) throws IOException {
        ackMonitor.checkAckEverPacket(context.getSeq());
        context.sendData(data, nonce);
    }

    public StatusConnection getStatus() {
        return status;
    }

    public static int getMyPort() { return myPort; }
}

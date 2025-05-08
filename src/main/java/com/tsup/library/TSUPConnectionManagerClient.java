package com.tsup.library;

import com.tsup.crypto.RSAUtils;
import com.tsup.protocol.Segment;
import com.tsup.protocol.SegmentUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TSUPConnectionManagerClient extends TSUPConnectionManagerBase {

    public TSUPConnectionManagerClient(TSUPContext context) {
        super(); //init ackMonitor
        this.context = context;
    }

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
}

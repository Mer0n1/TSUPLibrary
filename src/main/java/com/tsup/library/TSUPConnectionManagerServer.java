package com.tsup.library;

import com.tsup.crypto.RSAUtils;
import com.tsup.protocol.Segment;
import com.tsup.protocol.SegmentUtils;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class TSUPConnectionManagerServer extends TSUPConnectionManagerBase {

    public TSUPConnectionManagerServer() {
        super(); //init ackMonitor
    }

    /** Прослушивает и при удачном handshake возвращает aead ключ. */
    public TSUPContext listenerHandshake(SecretKey aeadKey, int port) {
        try {
            status = StatusConnection.handshake;

            byte[] buffer = new byte[listenerBufferSize];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            DatagramSocket datagramSocket = new DatagramSocket(port);
            datagramSocket.receive(packet);
            byte[] actualData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength()); //обрезаем лишнее

            //создаем сегмент
            Segment segment = SegmentUtils.fromByteArray(actualData);

            //Получаем публичный rsa и создаем aead ключ
            String rsaKey = new String(segment.payload, StandardCharsets.UTF_8);

            //готовим aead ключ для отправки. Шифруем его рса.
            byte[] aeadKeyBytes = RSAUtils.encryptByPublicKey(aeadKey.getEncoded(), rsaKey);

            //Создание контекста
            TSUPContext context = new TSUPContext(datagramSocket, packet.getAddress(), TSUPBaseSocket.myStandardPort);
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
}

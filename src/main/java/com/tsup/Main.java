package com.tsup;

import com.tsup.library.TSUPServerSocket;
import com.tsup.library.TSUPSocket;
import com.tsup.protocol.Flags;
import com.tsup.crypto.AEADUtils;
import com.tsup.crypto.RSAUtils;

import javax.crypto.SecretKey;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

class Testing {
    public static void testWithBase() throws Exception {
        String helloWorld = "Hello World";

        SecretKey secretKey = AEADUtils.generateKey();
        byte[] iv = AEADUtils.generateIv();

        String encryptStr = AEADUtils.encryptBASE64(AEADUtils.encrypt(helloWorld.getBytes(), iv, secretKey));
        String decryptStr = new String(AEADUtils.decrypt(AEADUtils.decryptBASE64(encryptStr), iv, secretKey));

        System.out.println(decryptStr);
    }

    public static void testWithoutBase() throws Exception {
        String helloWorld = "Hello World";

        SecretKey secretKey = AEADUtils.generateKey();
        byte[] iv = AEADUtils.generateIv();

        byte[] encryptBytes = AEADUtils.encrypt(helloWorld.getBytes(), iv, secretKey);
        String decryptStr = new String(AEADUtils.decrypt(encryptBytes, iv, secretKey));

        System.out.println(decryptStr);
    }

    public static void FlagTest() {
        Flags flags = new Flags((byte)4);
        flags.enable(Flags.ACK_BITFIELD);
        System.out.println(flags.has((byte)2));
        System.out.println((int)flags.getValue());
    }

    public static void encryptRSAPlusAEAD() throws Exception {
        Map<String, Object> rsaKeys = RSAUtils.initKey();
        RSAPublicKey publicKey = (RSAPublicKey) rsaKeys.get("PUBLIC_KEY");
        RSAPrivateKey privateKey = (RSAPrivateKey) rsaKeys.get("PRIVATE_KEY");

        String data = "Hello world1";

        byte[] bytes = RSAUtils.encryptByPublicKey(data.getBytes(), RSAUtils.encryptBASE64(publicKey.getEncoded()));

        System.out.println(bytes.length);

        String newData = new String(RSAUtils.decryptByPrivateKey(bytes, RSAUtils.encryptBASE64(privateKey.getEncoded())));

        System.out.println(newData);
    }


    public static void SpeedTest() throws Exception {
        TSUPSocket socket = new TSUPSocket();
        socket.startClient("127.0.0.1", 6040);
        System.out.println("____________Successful____________");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        StringBuilder test = new StringBuilder();
        for (int j = 0; j < 1000-32; j++)
            test.append("1");

        AtomicLong numberPacket = new AtomicLong();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                numberPacket.getAndIncrement();
                socket.sendMessage(test.toString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.MICROSECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            System.out.println(numberPacket);
        }, 1, 100, TimeUnit.MILLISECONDS);
    }

    public static void serverTest() throws Exception {
        TSUPServerSocket serverSocket = new TSUPServerSocket();
        serverSocket.startServer(6040);
        System.out.println("handshake done");
        serverSocket.setOnMessageReceiver(System.out::println);
    }

    public static void ackTest() throws Exception {
        TSUPSocket socket = new TSUPSocket();
        socket.startClient("192.168.0.103", 6040);
        System.out.println("handshake done");
        socket.setOnMessageReceiver(System.out::println);

        int sec = 0;

        while (true) {
            socket.sendMessage(("testMessage " + sec).getBytes());

            Thread.sleep(500);
            sec += 500;

            if (sec > 10000)
                socket.disconnect();
        }
    }

}
/** Будущие наработки... */
/**

 *
 * */

public class Main {

    public static void main(String[] args) throws Exception {
        //Testing.SpeedTest();
    }

}

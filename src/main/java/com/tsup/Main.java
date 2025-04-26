package com.tsup;

import com.tsup.protocol.Flags;
import com.tsup.crypto.AEADUtils;
import com.tsup.library.MyTSUPLibrary;
import com.tsup.crypto.RSAUtils;
import com.tsup.library.TSUPConnectionManager;

import javax.crypto.SecretKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    /*public static void creatingSegmentAndFromToByte() {
        try {
            String data = "Hello world";

            TSUPCipheredSegmentLibrary segmentLibrary = new TSUPCipheredSegmentLibrary(AEAD.generateKey());

            //обернем наше сообщение в качестве HANDSHAKE_INIT тип (хотя сообщение такого типа не имеют данных)
            //но именно на этом этапе выполняется шифровка что вероятно не совсем корректно
            Segment segment = segmentLibrary.wrap(data, Type.HANDSHAKE_INIT, new Flags((byte) 0), (short) 0);

            //Преобразуем в байты
            byte[] bytes = segment.toBytes();

            //Преобразуем обратно в сегмент
            Segment segment2 = TSUPSegmentLibrary.fromByteArray(bytes);
            //на данном этапе payload не расшифрован.

            //Расшифруем данные.
            String decryptedData = new String(AEAD.decrypt(segment2.encryptedPayloadWithAuthTag,
                    segment2.nonce, segmentLibrary.getSecretKey()));

            //И выведем их.
            System.out.println(decryptedData);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }*/

    public static void handshake() {
        TSUPConnectionManager sendLibrary = new TSUPConnectionManager();
        //sendLibrary.listenerTest();
        //sendLibrary.listenerHandshake();
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
        MyTSUPLibrary myTSUPLibrary = new MyTSUPLibrary();
        myTSUPLibrary.startClient("192.168.0.103");
        System.out.println("____________Successful____________");

        long time = System.currentTimeMillis();

        /*while (true) {
            myTSUPLibrary.sendMessage("Hello there12");

            Thread.sleep(1);

            //System.err.println((System.currentTimeMillis() - time)/1000f);
        }*/

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        String test = "oesntheprogram";
        scheduler.scheduleAtFixedRate(() -> {
            try {
                myTSUPLibrary.sendMessage(test);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 900, TimeUnit.MICROSECONDS);
    }

    public static void serverTest() throws Exception {
        MyTSUPLibrary myTSUPLibrary = new MyTSUPLibrary();
        myTSUPLibrary.startServer();
        System.out.println("____________Successful____________");

        myTSUPLibrary.setOnMessageReceiver(message ->  { System.out.println(message);});

    }

}

public class Main {

    public static void main(String[] args) throws Exception {
        //Testing.creatingSegmentAndFromToByte();
        //Testing.handshake();
        //Testing.encryptRSAPlusAEAD();
        //Testing.SpeedTest();
        Testing.serverTest();
    }


}
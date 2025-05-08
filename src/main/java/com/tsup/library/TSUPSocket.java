package com.tsup.library;

import com.tsup.crypto.RSAUtils;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;


/** Клиентский сокет */
public class TSUPSocket extends TSUPBaseSocket {

    /** */
    public void startClient(String serverIp, int port) throws Exception {
        if (getStatus() != TSUPConnectionManagerBase.StatusConnection.connected) {

            //RSA ключи создаются на клиенте и пересылается публичный ключ серверу.
            Map<String, Object> rsaKeys = RSAUtils.initKey();

            //Создание контекста под клиента.
            context = new TSUPContext(new DatagramSocket(myStandardPort),
                    InetAddress.getByName(serverIp), port);

            tsupConnectionManager = new TSUPConnectionManagerClient(context);

            //Запускаем handshake
            ((TSUPConnectionManagerClient)tsupConnectionManager).handshake(rsaKeys, context);
            //получаем контекст
            cryptoContext = tsupConnectionManager.getCryptoContext();
            //после удачного handshake запускаем диспатчер
            tsupConnectionManager.startDispatcher();

        } else
            throw new Exception("active connection exist");
    }

}

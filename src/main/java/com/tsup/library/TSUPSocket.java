package com.tsup.library;

import com.tsup.crypto.RSAUtils;
import java.net.DatagramSocket;
import java.net.InetAddress;


/** Клиентский сокет */
public class TSUPSocket extends TSUPBaseSocket {

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

}

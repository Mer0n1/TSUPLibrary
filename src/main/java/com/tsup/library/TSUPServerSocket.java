package com.tsup.library;

import com.tsup.crypto.AEADUtils;

public class TSUPServerSocket extends TSUPBaseSocket {

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

}

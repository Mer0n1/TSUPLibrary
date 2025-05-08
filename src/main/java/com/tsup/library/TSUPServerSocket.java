package com.tsup.library;

import com.tsup.crypto.AEADUtils;

import javax.crypto.SecretKey;

public class TSUPServerSocket extends TSUPBaseSocket {

    /** port - номер порта, который текущий сервер-сокет будет прослушивать */
    public void startServer(int port) throws Exception {
        if (getStatus() != TSUPConnectionManagerBase.StatusConnection.connected) {

            tsupConnectionManager = new TSUPConnectionManagerServer();
            SecretKey aeadKey = AEADUtils.generateKey();

            //слушаем handshake
            context = ((TSUPConnectionManagerServer)tsupConnectionManager).listenerHandshake(aeadKey, port);
            //получаем контекст
            cryptoContext = tsupConnectionManager.getCryptoContext();
            //Запуск диспетчера
            tsupConnectionManager.startDispatcher();

        } else
            throw new Exception("active connection exist");
    }

}

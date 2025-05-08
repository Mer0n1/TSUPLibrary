package com.tsup.library;

import java.io.IOException;

public abstract class TSUPConnectionManagerBase {
    enum StatusConnection { none, handshake, connected, disconnected }

    protected DispatcherSocket dispatcher;
    protected StatusConnection status = StatusConnection.none;
    protected ACKMonitor ackMonitor;
    protected TSUPContext context;
    protected CryptoContext cryptoContext;
    /** Размер буфера для handshake*/
    protected final int listenerBufferSize = 512;

    public TSUPConnectionManagerBase() {
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
}

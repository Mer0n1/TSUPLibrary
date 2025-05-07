package com.tsup.library;

/** Контекст существующий только для передачи зависимостей DispatcherSocket для filterChain */
public class ConnectionContext {

    private ACKMonitor ackMonitor;
    private TSUPContext context;
    private CryptoContext cryptoContext;

    public ConnectionContext(ACKMonitor ackMonitor, TSUPContext context,
                             CryptoContext cryptoContext) {
        this.ackMonitor = ackMonitor;
        this.context = context;
        this.cryptoContext = cryptoContext;
    }

    public ACKMonitor getAckMonitor() {
        return ackMonitor;
    }

    public TSUPContext getContext() {
        return context;
    }

    public CryptoContext getCryptoContext() {
        return cryptoContext;
    }
}

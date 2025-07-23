package com.tsup.filters;

import com.tsup.library.ConnectionContext;
import com.tsup.library.DisconnectHandler;
import com.tsup.protocol.Segment;
import com.tsup.library.TSUPContext;
import com.tsup.library.TSUPMessageHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Контекст существует единственный и не создается на каждый пакет.
 *  Контекст-фильтр имеет handler's и отвечает за передачу пакета по цепочке фильтров. */
public class FilterContext {
    private final List<SegmentFilter> filters;
    private final TSUPContext context;
    private final ConnectionContext connectionContext;
    private final List<TSUPMessageHandler> handlers;
    private DisconnectHandler disconnectHandler;

    public FilterContext(List<SegmentFilter> filters, ConnectionContext connectionContext) {
        this.filters  = filters;
        this.connectionContext  = connectionContext;
        this.context = connectionContext.getContext();
        handlers = new ArrayList<>();
    }

    public void setOnMessageListener(TSUPMessageHandler handler) {
        handlers.add(handler);
    }

    public void setOnDisconnectHandler(DisconnectHandler disconnectHandler) { this.disconnectHandler = disconnectHandler; }

    public void noticeAllListeners(byte[] bytes) {
        for (TSUPMessageHandler handler : handlers)
            handler.onMessage(bytes);
    }

    public void noticeDisconnectHandler(String message) {
        disconnectHandler.onDisconnect(message);
    }

    //другие методы для расширения и влияния с TSUPContext...

    public void dispatchWithFreshIterator(Segment segment) throws Exception {
        Iterator<SegmentFilter> iterator = filters.iterator();
        next(segment, iterator);
    }

    public void next(Segment segment, Iterator<SegmentFilter> iterator) throws Exception {
        if (iterator.hasNext()) {
            iterator.next().handle(segment, this, iterator);
        }
    }

    public TSUPContext getContext() {
        return context;
    }

    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}


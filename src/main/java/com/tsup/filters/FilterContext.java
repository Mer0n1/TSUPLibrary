package com.tsup.filters;

import com.tsup.library.DisconnectHandler;
import com.tsup.protocol.Segment;
import com.tsup.library.TSUPContext;
import com.tsup.library.TSUPMessageHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FilterContext {
    private final List<SegmentFilter> filters;
    private final TSUPContext context;
    private final List<TSUPMessageHandler> handlers;
    private DisconnectHandler disconnectHandler;

    public FilterContext(List<SegmentFilter> filters, TSUPContext context) {
        this.filters  = filters;
        this.context  = context;
        handlers = new ArrayList<>();
    }

    public void setOnMessageListener(TSUPMessageHandler handler) {
        handlers.add(handler);
    }

    public void setOnDisconnectHandler(DisconnectHandler disconnectHandler) { this.disconnectHandler = disconnectHandler; }

    public void noticeAllListeners(String message) {
        for (TSUPMessageHandler handler : handlers)
            handler.onMessage(message);
    }

    public void noticeDisconnectHandler() {
        disconnectHandler.onDisconnect();
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
}


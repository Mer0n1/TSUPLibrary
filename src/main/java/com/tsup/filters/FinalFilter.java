package com.tsup.filters;

import com.tsup.protocol.Segment;
import com.tsup.protocol.Type;

import java.util.Iterator;

public class FinalFilter implements SegmentFilter {
    @Override
    public void handle(Segment segment, FilterContext fContext,
                       Iterator<SegmentFilter> iterator) throws Exception {

        //switch по типам
        switch (segment.type) {
            case Type.DATA:
                fContext.noticeAllListeners(segment.payload);
                break;
            case Type.DISCONNECT:
                fContext.noticeDisconnectHandler("Disconnect request");
                break;
            case Type.PING:
                //fContext.getContext().
                break;
        }
    }
}

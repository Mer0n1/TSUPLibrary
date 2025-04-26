package com.tsup.filters;

import com.tsup.protocol.Segment;

import java.util.Iterator;

public class AckFilter implements SegmentFilter {
    @Override
    public void handle(Segment segment, FilterContext fContext,
                       Iterator<SegmentFilter> iterator) throws Exception {
        //передадим то что ACK дошел успешно и никаких ошибок timeout нет

        fContext.next(segment, iterator);
    }
}

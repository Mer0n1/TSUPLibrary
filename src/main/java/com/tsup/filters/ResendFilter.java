package com.tsup.filters;

import com.tsup.protocol.Segment;

import java.util.Iterator;

public class ResendFilter implements SegmentFilter {
    @Override
    public void handle(Segment segment, FilterContext fContext,
                       Iterator<SegmentFilter> iterator) throws Exception {
        //скажем повторно отправить запрос

        fContext.next(segment, iterator);
    }
}

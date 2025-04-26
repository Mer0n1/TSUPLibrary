package com.tsup.filters;

import com.tsup.protocol.Segment;

import java.util.Iterator;

public interface SegmentFilter {
    void handle(Segment segment, FilterContext fContext,
                Iterator<SegmentFilter> iterator) throws Exception;
}

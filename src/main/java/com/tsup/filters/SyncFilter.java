package com.tsup.filters;

import com.tsup.protocol.Segment;

import java.util.Iterator;

/**
 * Фильтр синхронизации.
 * Используем метод didn't arrive - lost.
 * Любые пакеты с seq различным чем seq+1 прошлого пакета считаются потерянными,
 * даже если они приходят позже.
 *
 * */
public class SyncFilter implements SegmentFilter {
    private short lastSeq; //seq последнего пришедшего пакета.

    @Override
    public void handle(Segment segment, FilterContext fContext,
                       Iterator<SegmentFilter> iterator) throws Exception {

        //Пропускаем старый неактуальный пакет.
        if (lastSeq != -1 && !fContext.getContext().isNewer(segment.seq, lastSeq)) //seq < responseSeq
            return;

        lastSeq = segment.seq;

        fContext.next(segment, iterator);
    }
}

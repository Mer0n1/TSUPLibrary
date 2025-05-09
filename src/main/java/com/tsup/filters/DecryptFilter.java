package com.tsup.filters;

import com.tsup.protocol.Flags;
import com.tsup.protocol.Segment;
import com.tsup.crypto.AEADUtils;

import java.util.Iterator;

public class DecryptFilter implements SegmentFilter {
    @Override
    public void handle(Segment segment, FilterContext fContext,
                       Iterator<SegmentFilter> iterator) throws Exception {

        if (segment.flags.has(Flags.ENCRYPTED))
            segment.payload = AEADUtils.decrypt(segment.payload,
                    segment.nonce, fContext.getConnectionContext().getCryptoContext().getAeadKey());


        fContext.next(segment, iterator);
    }
}

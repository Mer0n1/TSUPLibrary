package com.tsup.protocol;


import java.nio.ByteBuffer;

public class Segment {
    public byte type;
    public Flags flags;
    public short seq;
    public byte[] nonce;
    public byte[] payload;

    public byte[] toBytes() {
        if (nonce == null)
            nonce = new byte[12];
        if (payload == null)
            payload = new byte[0];

        ByteBuffer buf = ByteBuffer.allocate(16 + payload.length);
        buf.put(type);
        buf.put(flags.getValue());
        buf.putShort(seq);
        buf.put(nonce);
        buf.put(payload);
        return buf.array();
    }
}


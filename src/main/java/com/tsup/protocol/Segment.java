package com.tsup.protocol;


import java.nio.ByteBuffer;

public class Segment {
    public byte type;
    public Flags flags;
    public short seq;
    public byte[] nonce;
    public byte[] encryptedPayloadWithAuthTag;

    public byte[] toBytes() {
        if (nonce == null)
            nonce = new byte[12];
        if (encryptedPayloadWithAuthTag == null)
            encryptedPayloadWithAuthTag = new byte[0];

        ByteBuffer buf = ByteBuffer.allocate(16 + encryptedPayloadWithAuthTag.length);
        buf.put(type);
        buf.put(flags.getValue());
        buf.putShort(seq);
        buf.put(nonce);
        buf.put(encryptedPayloadWithAuthTag);
        return buf.array();
    }
}


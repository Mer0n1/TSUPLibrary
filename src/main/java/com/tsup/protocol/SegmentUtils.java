package com.tsup.protocol;

import com.tsup.crypto.AEADUtils;

import java.nio.ByteBuffer;


/** Класс работающий с Segment. Отвечает за упаковку/распаковку и анализа пакета. */
public class SegmentUtils {
    private SegmentUtils() {}

    /**
     * Оборачивает данные типа String в TSUP сегмент и возвращает.
     * */
    public static Segment wrapRaw(byte[] data, byte type, Flags flags, short seq, byte[] nonce) {

        Segment segment = new Segment();
        segment.type = type;
        segment.flags = flags;
        segment.seq = seq;
        segment.nonce = nonce;
        segment.encryptedPayloadWithAuthTag = data;

        return segment;
    }


    /** Разбор байтов на сегмент. */
    public static Segment fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);

        Segment segment = new Segment();

        segment.type = buffer.get();
        segment.flags = new Flags(buffer.get());
        segment.seq   = buffer.getShort();

        segment.nonce = new byte[AEADUtils.NonceSize];
        buffer.get(segment.nonce);

        segment.encryptedPayloadWithAuthTag = new byte[buffer.remaining()];
        buffer.get(segment.encryptedPayloadWithAuthTag);

        return segment;
    }

    /** Разбор байтов на сегмент. (при наличии offset */
    public static Segment fromByteArray(byte[] array, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(array, offset, array.length - offset);

        Segment segment = new Segment();

        segment.type = buffer.get();
        segment.flags = new Flags(buffer.get());
        segment.seq   = buffer.getShort();

        segment.nonce = new byte[AEADUtils.NonceSize];
        buffer.get(segment.nonce);

        segment.encryptedPayloadWithAuthTag = new byte[buffer.remaining()];
        buffer.get(segment.encryptedPayloadWithAuthTag);

        return segment;
    }
}

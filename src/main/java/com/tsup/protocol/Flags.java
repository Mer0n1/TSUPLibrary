package com.tsup.protocol;


public class Flags {
    /** Ответный ACK на каждый 8 пакет. */
    public static final byte ACK_BITFIELD     = 1 << 0; // 00000001
    /** Попытка получения обратной связи/повтор запроса */
    public static final byte RESEND_REQUEST   = 1 << 1; // 00000010
    /** Пакет является зашифрованным AEAD ключом.*/
    public static final byte ENCRYPTED        = 1 << 3; // 00001000

    private byte value;

    public Flags(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public boolean has(byte flag) {
        return (value & flag) != 0;
    }

    public void enable(byte flag) {
        value |= flag;
    }

    public void disable(byte flag) {
        value &= ~flag;
    }

    public static Flags empty() { return new Flags((byte) 0); }

    public static Flags encrypted() { return new Flags(ENCRYPTED); }

    public static Flags ack_bitfield() { return new Flags(ACK_BITFIELD); }

    public static Flags resend() { return new Flags(RESEND_REQUEST); }

    @Override
    public String toString() {
        return String.format("Flags[%s%s%s%s]",
                has(ACK_BITFIELD)     ? "ACK " : "",
                has(RESEND_REQUEST)   ? "RESEND " : "",
                has(ENCRYPTED)        ? "ENCRYPTED " : ""
        );
    }
}

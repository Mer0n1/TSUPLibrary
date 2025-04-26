package com.tsup.protocol;

public class Type {
    public final static byte HANDSHAKE_INIT = 1;
    public final static byte HANDSHAKE_ACK  = 2;
    public final static byte DATA           = 3;
    public final static byte ACK            = 4;
    public final static byte DISCONNECT     = 5;
    public final static byte PING           = 6;

    public static String getName(byte val) {
        return switch (val) {
            case HANDSHAKE_INIT -> "HANDSHAKE_INIT";
            case HANDSHAKE_ACK  -> "HANDSHAKE_ACK";
            case DATA           -> "DATA";
            case ACK            -> "ACK";
            case DISCONNECT     -> "DISCONNECT";
            case PING           -> "PING";
            default             -> "UNKNOWN";
        };
    }
}


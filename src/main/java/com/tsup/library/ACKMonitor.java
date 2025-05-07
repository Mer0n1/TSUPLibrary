package com.tsup.library;

/** Монитор ACK созданный для проверки ack-bitfield.
 * Своего рода замена keep-alive с пинговщиком. */
public class ACKMonitor {
    /** Количество не пришедших ack */
    private int missedAckCount = 0;
    /** В случае если более limitMissedAck ack не придет, то вызываем timeout и disconnect */
    private final int limitMissedAck = 10;
    /** Настраиваемое значение на какой N-запрос отправлять ack */
    private final int ackN = 8;

    private final Runnable onTimeout;

    public ACKMonitor(Runnable onTimeout) {
        this.onTimeout = onTimeout;
    }

    public void ackReceived() {
        missedAckCount = 0;
    }

    public void checkAckEverPacket(short seq) {
        if (seq != 0 && seq % ackN == 0)
            missedAckCount++;

        if (missedAckCount >= limitMissedAck)
            onTimeout.run();
    }
}

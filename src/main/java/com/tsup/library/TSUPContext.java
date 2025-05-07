package com.tsup.library;

import com.tsup.protocol.Flags;
import com.tsup.protocol.Segment;
import com.tsup.protocol.Type;
import com.tsup.crypto.AEADUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
* Контекст TSUP протокола. Отправка разного рода пакетов.
* */
public class TSUPContext {

    private final DatagramSocket socket;
    private final InetAddress remoteAddress;
    private final int remotePort;
    //private final ACKMonitor ackMonitor;
    private short seq;

    public TSUPContext(DatagramSocket socket, InetAddress address, int port) {
        this.socket = socket;
        this.remoteAddress = address;
        this.remotePort = port;
        seq = 0;
    }

    /** Отправка ACK_BITFIELD.
     *  Осуществляется на каждый 8 пакет. */ //todo отправить байт payload потерянных пакетов.
    public void sendAck() throws IOException {
        Segment ackSegment = new Segment();
        ackSegment.type = Type.ACK;
        ackSegment.seq = nextSeq();
        ackSegment.flags = Flags.ack_bitfield();

        byte[] data = ackSegment.toBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, remoteAddress, remotePort);

        socket.send(packet);
    }

    /** Start Handshake
     * Отправка первичного запроса для создания соединения.
     *  Пакет содержит публичный rsa ключ. */
    public void sendINIT(byte[] publicRSAkey) throws IOException {
        Segment initSegment = new Segment();
        initSegment.type  = Type.HANDSHAKE_INIT;
        initSegment.seq   = 0;
        initSegment.flags = Flags.empty();
        initSegment.payload = publicRSAkey;

        byte[] data = initSegment.toBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, remoteAddress, remotePort);

        socket.send(packet);
    }

    /** Отправка ответа handshake.
     *  Ответ содержит зашифрованный AEAD ключ. */
    public void sendHandshakeACK(byte[] AEADKey) throws IOException {
        Segment haSegment = new Segment();
        haSegment.type  = Type.HANDSHAKE_ACK;
        haSegment.seq   = nextSeq();
        haSegment.flags = Flags.empty();
        haSegment.payload = AEADKey;

        byte[] data = haSegment.toBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, remoteAddress, remotePort);

        socket.send(packet);
    }

    /** Отправка зашифрованных данных.
     *  data - уже должны быть зашифрованы AEAD ключом */
    public void sendData(byte[] data, byte[] nonce) throws IOException {
        Segment dataSegment = new Segment();
        dataSegment.type = Type.DATA;
        dataSegment.seq = nextSeq();
        dataSegment.flags = Flags.encrypted();
        dataSegment.nonce = nonce;
        dataSegment.payload = data;

        byte[] data_ = dataSegment.toBytes();
        DatagramPacket packet = new DatagramPacket(data_, data_.length, remoteAddress, remotePort);

        socket.send(packet);
    }

    /** Запрос на повторную отправку данных. */
    public void resendData(byte[] data, byte[] nonce) throws IOException {
        Segment dataSegment = new Segment();
        dataSegment.type = Type.DATA;
        dataSegment.seq = nextSeq();
        dataSegment.flags = Flags.encrypted();
        dataSegment.flags.enable(Flags.RESEND_REQUEST);
        dataSegment.nonce = nonce;
        dataSegment.payload = data;

        byte[] data_ = dataSegment.toBytes();
        DatagramPacket packet = new DatagramPacket(data_, data_.length, remoteAddress, remotePort);

        socket.send(packet);
    }

    public void resendData() throws IOException {
        resendData(new byte[]{}, new byte[AEADUtils.NonceSize]);
    }

    public void sendDisconnect() throws IOException {
        Segment discSegment = new Segment();
        discSegment.type = Type.DISCONNECT;
        discSegment.seq = nextSeq();
        discSegment.flags = Flags.empty();

        byte[] data = discSegment.toBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, remoteAddress, remotePort);

        socket.send(packet);
    }

    /** Номер i новее номера j? */
    public boolean isNewer(short i, short j) {
        return (i & 0xFFFF) > (j & 0xFFFF);
    }

    private synchronized short nextSeq() {
        return seq++;
    }

    public short getSeq() {
        return seq;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    /*public ACKMonitor getAckMonitor() {
        return ackMonitor;
    }*/
}

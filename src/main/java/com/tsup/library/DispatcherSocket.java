package com.tsup.library;

import com.tsup.filters.*;
import com.tsup.protocol.SegmentUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DispatcherSocket  {
    private final List<SegmentFilter> filters = new ArrayList<>();
    private final FilterContext filterContext;
    private final DatagramSocket datagramSocket;

    public final int maxBufferSize = 512;

    private Thread thread;

    public DispatcherSocket(TSUPContext context) {
        filters.add(new DecryptFilter());
        filters.add(new AckFilter());
        filters.add(new ResendFilter());
        filters.add(new FinalFilter());

        filterContext = new FilterContext(filters, context);
        this.datagramSocket = context.getSocket();
    }

    public void setOnMessageListener(TSUPMessageHandler handler) {
        filterContext.setOnMessageListener(handler);
    }

    public void setOnDisconnectHandler(DisconnectHandler disconnectHandler) { filterContext.setOnDisconnectHandler(disconnectHandler); }

    private void dispatch(byte[] packet) throws Exception {
        filterContext.dispatchWithFreshIterator(SegmentUtils.fromByteArray(packet));
    }

    public void startListener() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        byte[] buffer = new byte[maxBufferSize];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        datagramSocket.receive(packet);
                        byte[] actualData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());

                        dispatch(actualData);

                    } catch (Exception e) {
                        System.err.println("ERROR: " + e.getMessage());
                    }
                }
            }
        });

        thread.start();
    }

    public void makeDisconnect() throws IOException {
        //остановка слушателя
        thread.interrupt();
        //посылаем пакет-уведомление об отключении
        filterContext.getContext().disconnect();
    }

}

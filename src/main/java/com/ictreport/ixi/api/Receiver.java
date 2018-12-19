package com.ictreport.ixi.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import com.ictreport.ixi.ReportIxi;
import com.ictreport.ixi.model.Neighbor;

public class Receiver extends Thread {

    private static final Logger log = LoggerFactory.getLogger(Receiver.class);

    private final ReportIxi reportIxi;
    private final DatagramSocket socket;
    private boolean isReceiving = false;

    private List<IIncomingPacketListener> onIncomingPacketListeners = new ArrayList<>();

    public Receiver(ReportIxi reportIxi, DatagramSocket socket) {
        super("Receiver");
        this.reportIxi = reportIxi;
        this.socket = socket;

        // Default onIncomingPacketListener
        addOnIncomingPacketListener(new IIncomingPacketListener() {
            @Override
            public void OnIncomingPacket(DatagramPacket packet) {
                for (final Neighbor neighbor : getReportIxi().getNeighbors()) {
                    if (packet.getAddress().equals(neighbor.getAddress()) && packet.getPort() == neighbor.getReportPort()) {
                        String data = new String(packet.getData(), 0, packet.getLength());
                        log.info(String.format("Received data: %s from [%s]", data, packet.getSocketAddress()));
                        // TODO: Unpack the content of the packet.
                        // TODO: Determine what kind of further actions to take upon the received packet.
                    }
                }
            }
        });
    }

    @Override
    public void run() {

        isReceiving = true;

        while (isReceiving) {

            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                this.socket.receive(packet);
                notifyOnIncomingPacketListeners(packet);
            } catch (IOException e) {
                if (isReceiving)
                    e.printStackTrace();
            }
        }
    }

    public void shutDown() {
        isReceiving = false;
    }

    public void addOnIncomingPacketListener(IIncomingPacketListener onIncomingPacketListener) {
        if (!onIncomingPacketListeners.contains(onIncomingPacketListener)) {
            onIncomingPacketListeners.add(onIncomingPacketListener);
        }
    }

    public void removeOnIncomingPacketListener(IIncomingPacketListener onIncomingPacketListener) {
        if (onIncomingPacketListeners.contains(onIncomingPacketListener)) {
            onIncomingPacketListeners.remove(onIncomingPacketListener);
        }
    }

    private void notifyOnIncomingPacketListeners(DatagramPacket packet) {
        for (IIncomingPacketListener listener : onIncomingPacketListeners) {
            listener.OnIncomingPacket(packet);
        }
    }

    public interface IIncomingPacketListener {
        void OnIncomingPacket(DatagramPacket packet);
    }

    /**
     * @return the reportIxi
     */
    public ReportIxi getReportIxi() {
        return reportIxi;
    }
}

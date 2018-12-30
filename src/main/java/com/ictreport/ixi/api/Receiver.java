package com.ictreport.ixi.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ictreport.ixi.utils.Cryptography;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import com.ictreport.ixi.ReportIxi;
import com.ictreport.ixi.model.Neighbor;

public class Receiver extends Thread {
    public final static Logger LOGGER = LogManager.getLogger(Receiver.class);

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

                        Gson gson = new Gson();

                        JsonObject metadata = gson.fromJson(data, JsonObject.class);

                        if (metadata != null) {

                            String uuid = metadata.getAsJsonPrimitive("uuid").getAsString();
                            String encodedPublicKeyBase64 = metadata.getAsJsonPrimitive("publicKey").getAsString();

                            if (!uuid.isEmpty()) {
                                neighbor.setUuid(uuid);
                            }
                            if (!encodedPublicKeyBase64.isEmpty()) {

                                try {
                                    final byte[] encodedPublicKey = Base64.decodeBase64(encodedPublicKeyBase64.getBytes());
                                    PublicKey publicKey = Cryptography.getPublicKeyFromBytes(encodedPublicKey);
                                    System.out.println("Successfully decoded public key and set it for neighbor: " + neighbor.getUuid());
                                    neighbor.setPublicKey(publicKey);
                                } catch (InvalidKeySpecException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void run() {

        isReceiving = true;

        while (isReceiving) {

            byte[] buf = new byte[1024];
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

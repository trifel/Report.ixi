package com.ictreport.ixi.api;

import com.ictreport.ixi.exchange.MetadataPayload;
import com.ictreport.ixi.exchange.Payload;
import com.ictreport.ixi.exchange.PingPayload;
import com.ictreport.ixi.exchange.SignedPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.ictreport.ixi.ReportIxi;
import com.ictreport.ixi.model.Neighbor;

public class Receiver extends Thread {
    public final static Logger LOGGER = LogManager.getLogger(Receiver.class);

    private final ReportIxi reportIxi;
    private final DatagramSocket socket;
    private boolean isReceiving = false;

    public Receiver(ReportIxi reportIxi, DatagramSocket socket) {
        super("Receiver");
        this.reportIxi = reportIxi;
        this.socket = socket;
    }

    @Override
    public void run() {

        isReceiving = true;

        while (isReceiving) {

            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(packet);
                processPacket(packet);
            } catch (IOException e) {
                if (isReceiving)
                    e.printStackTrace();
            }
        }
    }

    public void shutDown() {
        isReceiving = false;
    }

    public void processPacket(final DatagramPacket packet) {

        for (final Neighbor neighbor : getReportIxi().getNeighbors()) {
            if (packet.getAddress().equals(neighbor.getAddress()) && packet.getPort() == neighbor.getReportPort()) {
                String data = new String(packet.getData(), 0, packet.getLength());

                final Payload payload = Payload.deserialize(data);
                processPayload(neighbor, payload);
            }
        }
    }

    public void processPayload(final Neighbor neighbor, final Payload payload) {

        if (payload instanceof MetadataPayload) {
            processMetadataPacket(neighbor, (MetadataPayload) payload);
        }
        if (payload instanceof SignedPayload) {
            processSignedPayload((SignedPayload) payload);
        }
    }

    public void processMetadataPacket(final Neighbor neighbor, final MetadataPayload metadataPayload) {

        if (neighbor.getReportIxiVersion() == null ||
                !neighbor.getReportIxiVersion().equals(metadataPayload.getReportIxiVersion())) {
            neighbor.setReportIxiVersion(metadataPayload.getReportIxiVersion());
            LOGGER.info(String.format("Neighbor[%s:%s] operates Report.ixi version: %s",
                    neighbor.getAddress(),
                    neighbor.getReportPort(),
                    neighbor.getReportIxiVersion()));
        }

        if (neighbor.getUuid() == null ||
                !neighbor.getUuid().equals(metadataPayload.getUuid())) {
            neighbor.setUuid(metadataPayload.getUuid());
            LOGGER.info(String.format("Received new uuid from neighbor[%s:%s]",
                    neighbor.getAddress(),
                    neighbor.getReportPort()));
        }

        if (neighbor.getPublicKey() == null ||
                !neighbor.getPublicKey().equals(metadataPayload.getPublicKey())) {
            neighbor.setPublicKey(metadataPayload.getPublicKey());
            LOGGER.info(String.format("Received new publicKey from neighbor[%s:%s]",
                    neighbor.getAddress(),
                    neighbor.getReportPort()));
        }
    }

    public void processSignedPayload(final SignedPayload signedPayload) {

        Neighbor signee = null;
        for (Neighbor neighbor : reportIxi.getNeighbors()) {

            if (neighbor.getPublicKey() != null && signedPayload.verify(neighbor.getPublicKey())) {

                signee = neighbor;
                break;
            }
        }

        if (signedPayload.getPayload() instanceof PingPayload) {
            PingPayload pingPayload = (PingPayload) signedPayload.getPayload();

            if (signee != null) {
                LOGGER.info(String.format("Received signed ping from neighbor [%s:%s]",
                        signee.getAddress(),
                        signee.getReportPort()));
                // TODO: Notify RCS that we received a PingPayload from a direct neighbor.
            } else {
                // TODO: Notify RCS that we received a PingPayload from an indirect neighbor.
            }
        }
    }

    /**
     * @return the reportIxi
     */
    public ReportIxi getReportIxi() {
        return reportIxi;
    }
}

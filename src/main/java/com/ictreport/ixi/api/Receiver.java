package com.ictreport.ixi.api;

import com.ictreport.ixi.exchange.MetadataPayload;
import com.ictreport.ixi.exchange.Payload;
import com.ictreport.ixi.exchange.PingPayload;
import com.ictreport.ixi.exchange.ReceivedPingPayload;
import com.ictreport.ixi.exchange.SignedPayload;
import com.ictreport.ixi.exchange.SilentPingPayload;
import com.ictreport.ixi.utils.Constants;
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

        Neighbor neighbor = determineNeighborWhoSent(packet);
        if (neighbor == null)
            return;

        String data = new String(packet.getData(), 0, packet.getLength());

        try {
            final Payload payload = Payload.deserialize(data);
            processPayload(neighbor, payload);
        } catch (Exception e) {
            LOGGER.info(String.format("Received invalid packet from Neighbor[%s]",
            neighbor.getAddress()));
        }
    }

    private Neighbor determineNeighborWhoSent(DatagramPacket packet) {
        for (Neighbor nb : getReportIxi().getNeighbors())
            if (nb.sentPacket(packet))
                return nb;
        for (Neighbor nb : getReportIxi().getNeighbors())
            if (nb.sentPacketFromSameIP(packet))
                return nb;
        LOGGER.warn("Received packet from unknown address: " + packet.getAddress());
        Metrics.setNonNeighborInvalidCount(Metrics.getNonNeighborInvalidCount()+1);
        return null;
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
            LOGGER.info(String.format("Neighbor[%s] operates Report.ixi version: %s",
                    neighbor.getAddress(),
                    neighbor.getReportIxiVersion()));
        }

        if (neighbor.getUuid() == null ||
                !neighbor.getUuid().equals(metadataPayload.getUuid())) {
            neighbor.setUuid(metadataPayload.getUuid());
            LOGGER.info(String.format("Received new uuid from neighbor[%s]",
                    neighbor.getAddress()));
        }

        if (neighbor.getPublicKey() == null ||
                !neighbor.getPublicKey().equals(metadataPayload.getPublicKey())) {
            neighbor.setPublicKey(metadataPayload.getPublicKey());
            LOGGER.info(String.format("Received new publicKey from neighbor[%s]",
                    neighbor.getAddress()));
        }

        neighbor.setMetadataCount(neighbor.getMetadataCount()+1);
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
            ReceivedPingPayload receivedPingPayload;
            if (signee != null) {
                receivedPingPayload = new ReceivedPingPayload(reportIxi.getProperties().getUuid(), pingPayload, true);
                signee.setPingCount(signee.getPingCount()+1);
            } else {
                receivedPingPayload = new ReceivedPingPayload(reportIxi.getProperties().getUuid(), pingPayload, false);
                Metrics.setNonNeighborPingCount(Metrics.getNonNeighborPingCount()+1);
            }
            reportIxi.getApi().getSender().send(receivedPingPayload, Constants.RCS_HOST, Constants.RCS_PORT);

        } else if (signedPayload.getPayload() instanceof SilentPingPayload) {
            if (signee != null) {
                signee.setPingCount(signee.getPingCount()+1);
            } else {
                Metrics.setNonNeighborPingCount(Metrics.getNonNeighborPingCount()+1);
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

package com.ictreport.ixi.api;

import com.ictreport.ixi.exchange.*;
import com.ictreport.ixi.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.ictreport.ixi.model.Neighbor;
import org.iota.ict.ixi.ReportIxi;

public class Receiver extends Thread {

    private final static Logger LOGGER = LogManager.getLogger(Receiver.class);
    private final ReportIxi reportIxi;
    private final DatagramSocket socket;
    private boolean isReceiving = false;

    public Receiver(final ReportIxi reportIxi, final DatagramSocket socket) {
        super("Receiver");

        this.reportIxi = reportIxi;
        this.socket = socket;
    }

    @Override
    public void run() {
        isReceiving = true;

        while (isReceiving) {
            final byte[] buf = new byte[1024];
            final DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(packet);
                processPacket(packet);
            } catch (final IOException e) {
                if (isReceiving)
                    e.printStackTrace();
            }
        }
    }

    public void processPayload(final Neighbor neighbor, final Payload payload) {
        if (payload instanceof MetadataPayload) {
            processMetadataPacket(neighbor, (MetadataPayload) payload);
        } else if (payload instanceof PingPayload) {
            processPingPayload((PingPayload) payload);
        } else if (payload instanceof UuidPayload) {
            processUuidPayload((UuidPayload) payload);
        }
    }

    public void shutDown() {
        isReceiving = false;
    }

    private void processPacket(final DatagramPacket packet) {
        Neighbor neighbor = determineNeighborWhoSent(packet);
        if (neighbor == null && !isPacketSentFromRCS(packet)) {
            LOGGER.warn("Received packet from unknown address: " + packet.getAddress());
            Metrics.incrementNonNeighborInvalidCount();
            return;
        }

        String data = new String(packet.getData(), 0, packet.getLength());

        try {
            final Payload payload = Payload.deserialize(data);
            processPayload(neighbor, payload);
        } catch (final Exception e) {
            if (neighbor != null) {
                LOGGER.info(String.format("Received invalid packet from Neighbor[%s]",
                        neighbor.getSocketAddress()));
            } else {
                LOGGER.info(String.format("Received invalid packet from RCS"));
            }
        }
    }

    private void processUuidPayload(final UuidPayload uuidPayload) {
        if (!uuidPayload.getUuid().equals(reportIxi.getMetadata().getUuid())) {
            reportIxi.getMetadata().setUuid(uuidPayload.getUuid());
            reportIxi.getMetadata().store(Constants.METADATA_FILE);
            LOGGER.info(String.format("Received new uuid from RCS"));
        } else {
            LOGGER.info(String.format("Current uuid was successfully validated by RCS"));
        }
        synchronized (reportIxi.waitingForUuid) {
            reportIxi.waitingForUuid.notify();
        }
    }

    private void processMetadataPacket(final Neighbor neighbor, final MetadataPayload metadataPayload) {
        if (neighbor.getReportIxiVersion() == null ||
                !neighbor.getReportIxiVersion().equals(metadataPayload.getReportIxiVersion())) {
            neighbor.setReportIxiVersion(metadataPayload.getReportIxiVersion());
            LOGGER.info(String.format("Neighbor[%s] operates Report.ixi version: %s",
                    neighbor.getSocketAddress(),
                    neighbor.getReportIxiVersion()));
        }

        if (neighbor.getUuid() == null ||
                !neighbor.getUuid().equals(metadataPayload.getUuid())) {
            neighbor.setUuid(metadataPayload.getUuid());
            LOGGER.info(String.format("Received new uuid from neighbor[%s]",
                    neighbor.getSocketAddress()));
        }

        neighbor.incrementMetadataCount();
    }

    private boolean isPacketSentFromRCS(final DatagramPacket packet) {
        try {
            final boolean sameIP = InetAddress.getByName(Constants.RCS_HOST).getHostAddress()
                    .equals(packet.getAddress().getHostAddress());
            final boolean samePort = Constants.RCS_PORT == packet.getPort();
            return sameIP && samePort;
        } catch (final UnknownHostException e) {
            return false;
        }
    }

    private void processPingPayload(final PingPayload pingPayload) {

        final ReceivedPingPayload receivedPingPayload =
                new ReceivedPingPayload(reportIxi.getMetadata().getUuid(), pingPayload);

        Metrics.incrementNonNeighborPingCount();

        if (reportIxi.getMetadata().getUuid() != null) {
            reportIxi.getApi().getSender().send(receivedPingPayload, Constants.RCS_HOST, Constants.RCS_PORT);
        }
    }

    private Neighbor determineNeighborWhoSent(final DatagramPacket packet) {
        for (final Neighbor neighbor : reportIxi.getNeighbors())
            if (neighbor.sentPacket(packet))
                return neighbor;
        for (final Neighbor neighbor : reportIxi.getNeighbors())
            if (neighbor.sentPacketFromSameIP(packet))
                return neighbor;
        return null;
    }
}

package com.ictreport.ixi.api;

import com.ictreport.ixi.exchange.*;
import com.ictreport.ixi.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.net.*;

import com.ictreport.ixi.model.Neighbor;
import org.iota.ict.ixi.ReportIxi;

public class Receiver extends Thread {

    private static final Logger log = LogManager.getLogger("ReportIxi/Receiver");
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
        if (payload instanceof PingPayload) {
            processPingPayload((PingPayload) payload);
        } else if (payload instanceof MetadataPayload) {
            processMetadataPacket(neighbor, (MetadataPayload) payload);
        }
    }

    public void shutDown() {
        isReceiving = false;
    }

    private void processPacket(final DatagramPacket packet) {
        log.debug("Processing packet from address:" + packet.getAddress() + ", port:" + packet.getPort());

        // Process RCS packets
        if (isPacketSentFromRCS(packet)) {
            try {
                processUuidPayload((UuidPayload) Payload.deserialize(packet));
            } catch (Exception e) {
                log.info("Received invalid payload from RCS");
            }
            return;
        }

        // Process Neighbor packets
        Neighbor neighbor = determineNeighborWhoSent(packet);
        if (neighbor != null) {
            try {
                processPayload(neighbor, Payload.deserialize(packet));
            } catch (Exception e) {
                log.info(String.format("Received invalid payload from Neighbor[%s]",
                        neighbor.getAddress().getReportSocketAddress()));
            }
            return;
        }

        log.warn("Received packet from unknown address: " + packet.getAddress());
    }

    private void processUuidPayload(final UuidPayload uuidPayload) {
        log.debug(String.format(
                "Received UuidPayload from RCS: %s",
                Payload.serialize(uuidPayload))
        );

        if (!uuidPayload.getUuid().equals(reportIxi.getMetadata().getUuid())) {
            reportIxi.getMetadata().setUuid(uuidPayload.getUuid());
            reportIxi.getMetadata().store(Constants.METADATA_FILE);
            log.info("Received new uuid from RCS");
        } else {
            log.info("Current uuid was successfully validated by RCS");
        }
        synchronized (reportIxi.waitingForUuid) {
            reportIxi.waitingForUuid.notify();
        }
    }

    private void processMetadataPacket(final Neighbor neighbor, final MetadataPayload metadataPayload) {
        log.debug(String.format(
                "Received MetadataPayload from neighbor[%s]: %s",
                neighbor,
                Payload.serialize(metadataPayload))
        );

        if (neighbor.getReportIxiVersion() == null ||
                !neighbor.getReportIxiVersion().equals(metadataPayload.getReportIxiVersion())) {
            neighbor.setReportIxiVersion(metadataPayload.getReportIxiVersion());
            log.info(String.format("Neighbor[%s] operates Report.ixi version: %s",
                    neighbor.getAddress().getReportSocketAddress(),
                    neighbor.getReportIxiVersion()));
        }

        if (neighbor.getUuid() == null ||
                !neighbor.getUuid().equals(metadataPayload.getUuid())) {
            neighbor.setUuid(metadataPayload.getUuid());
            log.info(String.format("Received new uuid from neighbor[%s]",
                    neighbor.getAddress().getReportSocketAddress()));
        }
    }

    private void processPingPayload(final PingPayload pingPayload) {
        final ReceivedPingPayload receivedPingPayload =
                new ReceivedPingPayload(reportIxi.getMetadata().getUuid(), pingPayload);

        if (reportIxi.getMetadata().getUuid() != null) {
            reportIxi.getApi().getSender().send(receivedPingPayload, Constants.RCS_HOST, Constants.RCS_PORT);
        }
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

    private Neighbor determineNeighborWhoSent(final DatagramPacket packet) {
        for (final Neighbor neighbor : reportIxi.getNeighbors()) {
            if (Neighbor.isNeighborWhoSent(neighbor, packet, true)) {
                log.debug("Strict match successful, packet received from neighbor: " + neighbor);
                return neighbor;
            }
        }
        for (final Neighbor neighbor : reportIxi.getNeighbors()) {
            if (Neighbor.isNeighborWhoSent(neighbor, packet, false)) {
                log.debug("Non-strict match successful, packet received from neighbor: " + neighbor);
                return neighbor;
            }
        }
        log.debug("Failed to match packet with any known neighbor");
        return null;
    }
}

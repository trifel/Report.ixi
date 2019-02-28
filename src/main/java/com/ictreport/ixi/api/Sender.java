package com.ictreport.ixi.api;

import com.ictreport.ixi.exchange.*;
import com.ictreport.ixi.model.Stats;
import com.ictreport.ixi.utils.CPUMonitor;
import com.ictreport.ixi.utils.RandomStringGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.ReportIxi;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.Constants;
import org.iota.ict.model.TransactionBuilder;

public class Sender {

    private static final Logger LOGGER = LogManager.getLogger("Sender");
    private final ReportIxi reportIxi;
    private final DatagramSocket socket;
    private final List<Timer> timers = new ArrayList<>();
    private final RandomStringGenerator randomStringGenerator = new RandomStringGenerator();

    public Sender(final ReportIxi reportIxi, final DatagramSocket socket) {
        this.reportIxi = reportIxi;
        this.socket = socket;
    }

    public void start() {

        // Metadata sender
        addTimerTask(new TimerTask() {
            @Override
            public void run() {
                if (!reportIxi.isRunning()) return;

                for (final Neighbor neighbor : reportIxi.getNeighbors()) {
                    final MetadataPayload metadataPayload =
                            new MetadataPayload(reportIxi.getMetadata().getUuid(), Constants.VERSION);

                    send (metadataPayload, neighbor.getAddress().getReportSocketAddress());
                    LOGGER.debug(String.format(
                            "Sent MetadataPayload to neighbor [%s]: %s",
                            neighbor.getAddress().getReportSocketAddress().toString(),
                            Payload.serialize(metadataPayload))
                    );
                }
            }
        }, 0, TimeUnit.MINUTES.toMillis(1));

        // Status sender
        addTimerTask(new TimerTask() {
            @Override
            public void run() {
                if (!reportIxi.isRunning()) return;

                reportIxi.syncIct();

                final List<NeighborPayload> neighborPayloads = new LinkedList<>();

                for (Neighbor neighbor : reportIxi.getNeighbors()) {

                    neighborPayloads.add(new NeighborPayload(
                            neighbor.getStats().getTimestamp(),
                            neighbor.getUuid(),
                            neighbor.getStats().getAllTx(),
                            neighbor.getStats().getNewTx(),
                            neighbor.getStats().getIgnoredTx(),
                            neighbor.getStats().getInvalidTx(),
                            neighbor.getStats().getRequestedTx()
                    ));
                }

                final StatusPayload statusPayload = new StatusPayload(
                        reportIxi.getMetadata().getUuid(),
                        reportIxi.getReportIxiContext().getName(),
                        reportIxi.getReportIxiContext().getIctVersion(),
                        Constants.VERSION,
                        reportIxi.getReportIxiContext().getIctRoundDuration(),
                        neighborPayloads,
                        CPUMonitor.getSystemLoadAverage());

                send(statusPayload, Constants.RCS_HOST, Constants.RCS_PORT);

                LOGGER.debug(String.format(
                        "Sent StatusPayload to RCS: %s",
                        Payload.serialize(statusPayload))
                );
            }
        }, 0, TimeUnit.MINUTES.toMillis(1));

        // Ping sender
        addTimerTask(new TimerTask() {
            @Override
            public void run() {
                if (!reportIxi.isRunning()) return;

                final PingPayload pingPayload = new PingPayload(randomStringGenerator.nextString());
                final String json = Payload.serialize(pingPayload);

                // Broadcast to neighbors
                final TransactionBuilder t = new TransactionBuilder();
                t.tag = Constants.TAG;
                t.asciiMessage(json);
                reportIxi.getIxi().submit(t.buildWhileUpdatingTimestamp());

                LOGGER.debug(String.format(
                        "Broadcasted PingPayload to Ict network: %s",
                        Payload.serialize(pingPayload))
                );

                // Send to RCS
                final SubmittedPingPayload submittedPingPayload =
                        new SubmittedPingPayload(reportIxi.getMetadata().getUuid(), pingPayload);

                send(submittedPingPayload, Constants.RCS_HOST, Constants.RCS_PORT);

                LOGGER.debug(String.format(
                        "Sent SubmittedPingPayload to RCS: %s",
                        Payload.serialize(submittedPingPayload))
                );
            }
        }, 0, TimeUnit.MINUTES.toMillis(5));
    }

    public void requestUuid() {
        final RequestUuidPayload requestUuidPayload =
                new RequestUuidPayload(reportIxi.getMetadata().getUuid(), reportIxi.getReportIxiContext().getExternalReportPort());
        send(requestUuidPayload, Constants.RCS_HOST, Constants.RCS_PORT);

        LOGGER.debug(String.format(
                "Sent RequestUuidPayload to RCS: %s",
                Payload.serialize(requestUuidPayload))
        );
    }

    public void send(final Payload payload, final InetAddress address, final int port) {
        send(payload, new InetSocketAddress(address, port));
    }

    public void send(final Payload payload, String host, final int port) {
        send(payload, new InetSocketAddress(host, port));
    }

    public synchronized void send(final Payload payload, final InetSocketAddress address) {
        try {
            final byte[] messageByteArray = Payload.serialize(payload).getBytes();
            if (socket != null && !socket.isClosed()) socket.send(new DatagramPacket(messageByteArray, messageByteArray.length, address));
        } catch (final IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void shutDown() {
        for (final Timer timer : timers) {
            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
        }
        timers.clear();
    }

    private void addTimerTask(final TimerTask timerTask, final long delay, final long period) {
        final Timer timer = new Timer();
        timer.schedule(timerTask, delay, period);
        timers.add(timer);
    }
}

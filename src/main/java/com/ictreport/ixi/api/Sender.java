package com.ictreport.ixi.api;

import com.ictreport.ixi.exchange.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.ReportIxi;
import org.iota.ict.model.TransactionBuilder;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.RandomStringGenerator;

public class Sender {

    private static final Logger LOGGER = LogManager.getLogger(Sender.class);
    private final ReportIxi reportIxi;
    private final DatagramSocket socket;
    private final RandomStringGenerator randomStringGenerator = new RandomStringGenerator();
    private final List<Timer> timers = new ArrayList<>();

    public Sender(final ReportIxi reportIxi, final DatagramSocket socket) {
        LOGGER.info(String.format("Report.ixi %s: Sender thread starting...", Constants.VERSION));

        this.reportIxi = reportIxi;
        this.socket = socket;
    }

    public void start() {
        timers.add(new Timer());
        addTimerTask(new TimerTask() {
            @Override
            public void run() {
                if (reportIxi.getMetadata().getUuid().isEmpty()) return;

                for (final Neighbor neighbor : reportIxi.getNeighbors()) {
                    final MetadataPayload metadataPayload = new MetadataPayload(reportIxi.getMetadata().getUuid(),
                            reportIxi.getKeyPair().getPublic(),
                            Constants.VERSION);
                    send (metadataPayload, neighbor.getSocketAddress());
                }
            }
        }, 0, 60000);

        addTimerTask(new TimerTask() {
            @Override
            public void run() {
                if (reportIxi.getMetadata().getUuid().isEmpty()) return;

                final List<String> neighborUuids = new LinkedList<>();
                for (final Neighbor neighbor : reportIxi.getNeighbors()) {
                    neighborUuids.add(neighbor.getUuid() != null ? neighbor.getUuid() : "");
                }
                final StatusPayload statusPayload = new StatusPayload(
                    reportIxi.getMetadata().getUuid(),
                    reportIxi.getProperties().getName(),
                    Constants.VERSION,
                    neighborUuids);
                send(statusPayload, Constants.RCS_HOST, Constants.RCS_PORT);

                Metrics.finishAndLog(reportIxi);
            }
        }, 0, 60000);

        addTimerTask(new TimerTask() {
            @Override
            public void run() {
                if (reportIxi.getMetadata().getUuid().isEmpty()) return;

                final PingPayload pingPayload = new PingPayload(randomStringGenerator.nextString());
                submitSignedPayload(pingPayload);

                // Send to RCS
                final SubmittedPingPayload submittedPingPayload =
                        new SubmittedPingPayload(reportIxi.getMetadata().getUuid(), pingPayload);
                send(submittedPingPayload, Constants.RCS_HOST, Constants.RCS_PORT);
            }
        }, 0, 60000);

        addTimerTask(new TimerTask() {
            @Override
            public void run() {
                if (reportIxi.getMetadata().getUuid().isEmpty()) return;

                final SilentPingPayload silentPingPayload = new SilentPingPayload(randomStringGenerator.nextString());
                submitSignedPayload(silentPingPayload);
            }
        }, 30000, 60000);
    }

    public void requestUuid() {
        final RequestUuidPayload requestUuidPayload =
                new RequestUuidPayload(reportIxi.getMetadata().getUuid(), reportIxi.getProperties().getExternalReportPort());
        send(requestUuidPayload, Constants.RCS_HOST, Constants.RCS_PORT);
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
            socket.send(new DatagramPacket(messageByteArray, messageByteArray.length, address));
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

    private void submitSignedPayload(final Payload payload) {
        // Prepare a signed payload
        final SignedPayload signedPayload = new SignedPayload(payload, reportIxi.getKeyPair().getPrivate());
        final String json = Payload.serialize(signedPayload);

        // Broadcast to neighbors
        final TransactionBuilder t = new TransactionBuilder();
        t.tag = Constants.TAG;
        t.asciiMessage(json);
        reportIxi.getIxi().submit(t.build());
    }
}

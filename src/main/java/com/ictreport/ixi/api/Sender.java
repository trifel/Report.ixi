package com.ictreport.ixi.api;

import com.ictreport.ixi.exchange.*;
import com.ictreport.ixi.utils.IctRestCaller;
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
import org.json.JSONArray;
import org.json.JSONObject;

public class Sender {

    private static final Logger LOGGER = LogManager.getLogger(Sender.class);
    private final ReportIxi reportIxi;
    private final DatagramSocket socket;
    private final RandomStringGenerator randomStringGenerator = new RandomStringGenerator();
    private final List<Timer> timers = new ArrayList<>();

    public Sender(final ReportIxi reportIxi, final DatagramSocket socket) {
        this.reportIxi = reportIxi;
        this.socket = socket;
    }

    public void start() {
        timers.add(new Timer());

        // Metadata sender
        addTimerTask(new TimerTask() {
            @Override
            public void run() {
                if (!reportIxi.isRunning()) return;

                for (final Neighbor neighbor : reportIxi.getNeighbors()) {
                    final MetadataPayload metadataPayload =
                            new MetadataPayload(reportIxi.getMetadata().getUuid(), Constants.VERSION);

                    neighbor.resolveHost();
                    send (metadataPayload, neighbor.getReportSocketAddress());
                }
            }
        }, 0, 60000);

        // Status sender
        addTimerTask(new TimerTask() {
            @Override
            public void run() {
                if (!reportIxi.isRunning()) return;

                final JSONArray ictNeighbors = IctRestCaller.getNeighbors(reportIxi.getReportIxiContext().getIctRestPassword());

                final List<NeighborPayload> neighbors = new LinkedList<>();
                for (final Neighbor neighbor : reportIxi.getNeighbors()) {
                    // Ensure that the neighbor's host is resolved
                    neighbor.resolveHost();

                    // Find the neighbor in the json array from ict-rest
                    boolean foundNeighbor = false;
                    for (int i=0; i<ictNeighbors.length(); i++) {
                        JSONObject ictNeighbor = (JSONObject)ictNeighbors.get(i);

                        LOGGER.info(ictNeighbor.toString());

                        if (ictNeighbor.getString("address").equals(neighbor.getIctSocketAddress().toString())) {
                            neighbors.add(new NeighborPayload(neighbor.getUuid(),
                                    ictNeighbor.getInt("all"),
                                    ictNeighbor.getInt("new"),
                                    ictNeighbor.getInt("ignored"),
                                    ictNeighbor.getInt("invalid"),
                                    ictNeighbor.getInt("requested")));
                            foundNeighbor = true;
                        }
                    }

                    if (!foundNeighbor) {
                        LOGGER.warn(String.format("Failed to match neighbor (%s) with (getNeighbors)-array from ict-rest.",
                                neighbor.getIctSocketAddress().toString()));
                    }
                }

                final StatusPayload statusPayload = new StatusPayload(
                    reportIxi.getMetadata().getUuid(),
                    reportIxi.getReportIxiContext().getName(),
                    reportIxi.getReportIxiContext().getIctVersion(),
                    Constants.VERSION,
                    neighbors);

                send(statusPayload, Constants.RCS_HOST, Constants.RCS_PORT);
            }
        }, 0, 60000);

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

                // Send to RCS
                final SubmittedPingPayload submittedPingPayload =
                        new SubmittedPingPayload(reportIxi.getMetadata().getUuid(), pingPayload);
                send(submittedPingPayload, Constants.RCS_HOST, Constants.RCS_PORT);
            }
        }, 0, 60000);
    }

    public void requestUuid() {
        final RequestUuidPayload requestUuidPayload =
                new RequestUuidPayload(reportIxi.getMetadata().getUuid(), reportIxi.getReportIxiContext().getExternalReportPort());
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

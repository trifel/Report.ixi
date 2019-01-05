package com.ictreport.ixi.api;

import com.ictreport.ixi.exchange.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ictreport.ixi.ReportIxi;
import org.iota.ict.model.TransactionBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.RandomStringGenerator;

public class Sender {
    private static final Logger LOGGER = LogManager.getLogger(Sender.class);

    private final ReportIxi reportIxi;
    private final DatagramSocket socket;
    private Timer uuidSenderTimer = new Timer();
    private Timer reportTimer = new Timer();
    private Timer submitRandomTransactionTimer = new Timer();
    private RandomStringGenerator randomStringGenerator = new RandomStringGenerator();

    public Sender(final ReportIxi reportIxi, DatagramSocket socket) {
        this.reportIxi = reportIxi;
        this.socket = socket;
    }

    public void start() {
        uuidSenderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (final Neighbor neighbor : reportIxi.getNeighbors()) {
                    MetadataPayload metadataPayload = new MetadataPayload(reportIxi.getProperties().getUuid(),
                            reportIxi.getKeyPair().getPublic(),
                            Constants.VERSION);
                    send (metadataPayload, neighbor.getAddress());
                }
            }
        }, 0, 60000);

        reportTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                List<String> neighborUuids = new LinkedList<>();
                for (final Neighbor neighbor : reportIxi.getNeighbors()) {
                    neighborUuids.add(neighbor.getUuid() != null ? neighbor.getUuid() : "");
                }
                StatusPayload statusPayload = new StatusPayload(
                    reportIxi.getProperties().getUuid(),
                    reportIxi.getProperties().getName(),
                    Constants.VERSION,
                    neighborUuids);
                send(statusPayload, Constants.RCS_HOST, Constants.RCS_PORT);

                Metrics.logMetrics(reportIxi);
            }
        }, 0, 60000);

        submitRandomTransactionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Prepare a signed ping payload
                PingPayload pingPayload = new PingPayload(randomStringGenerator.nextString());
                SignedPayload signedPayload = new SignedPayload(pingPayload, reportIxi.getKeyPair().getPrivate());

                String json = Payload.serialize(signedPayload);

                // Broadcast to neighbors
                TransactionBuilder t = new TransactionBuilder();
                t.tag = "REPORT9IXI99999999999999999";
                t.asciiMessage(json);
                reportIxi.submit(t.build());

                // Send to RCS
                SubmittedPingPayload submittedPingPayload = new SubmittedPingPayload(reportIxi.getProperties().getUuid(), pingPayload);
                send(submittedPingPayload, Constants.RCS_HOST, Constants.RCS_PORT);
            }
        }, 0, 60000);
    }

    public void send(Payload payload, InetAddress address, int port) {
        send(payload, new InetSocketAddress(address, port));
    }

    public void send(Payload payload, String host, int port) {
        send(payload, new InetSocketAddress(host, port));
    }

    public synchronized void send(Payload payload, InetSocketAddress address) {
        try {
            byte[] messageByteArray = Payload.serialize(payload).getBytes();
            socket.send(new DatagramPacket(messageByteArray, messageByteArray.length, address));
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void shutDown() {
        if (uuidSenderTimer != null) {
            uuidSenderTimer.cancel();
            uuidSenderTimer.purge();
        }
        if (reportTimer != null) {
            reportTimer.cancel();
            reportTimer.purge();
        }
        if (submitRandomTransactionTimer != null) {
            submitRandomTransactionTimer.cancel();
            submitRandomTransactionTimer.purge();
        }
    }

}

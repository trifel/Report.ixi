package org.iota.ict.ixi;

import com.ictreport.ixi.ReportIxiContext;
import com.ictreport.ixi.ReportIxiGossipListener;
import com.ictreport.ixi.api.MetadataSender;
import com.ictreport.ixi.api.PingSender;
import com.ictreport.ixi.api.Receiver;
import com.ictreport.ixi.api.StatusSender;
import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.exchange.payloads.RequestUuidPayload;
import com.ictreport.ixi.utils.ConfigurationMigrator;
import com.ictreport.ixi.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.context.IxiContext;
import org.iota.ict.network.gossip.GossipListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class ReportIxi extends IxiModule {

    private static final Logger log = LogManager.getLogger("ReportIxi");

    private final ReportIxiContext context = new ReportIxiContext();
    private DatagramSocket socket;
    private final GossipListener gossipListener = new ReportIxiGossipListener(this);

    public ReportIxi(Ixi ixi) {
        super(ixi);
        install();
    }

    @Override
    public void install() {
        // Attempt config migration from older Report.ixi versions if config is not found for the current version.
        ConfigurationMigrator.migrateIfConfigurationMissing();
    }

    @Override
    public void uninstall() {

    }

    @Override
    public void onStart() {
        log.info("Starting ReportIxi ...");

        createSocket();

        subWorkers.add(new Receiver(this));
        subWorkers.add(new MetadataSender(this));
        subWorkers.add(new StatusSender(this));
        subWorkers.add(new PingSender(this));

        getIxi().addGossipListener(gossipListener);

        requestUuidFromRCS();
    }

    @Override
    public void run() {
        try {
            while(isRunning()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            if (isRunning()) {
                e.printStackTrace();
                log.error("Report.ixi was unexpectedly interrupted");
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void onTerminate() {
        getIxi().removeGossipListener(gossipListener);
        socket.close();
        log.info("Stopped ReportIxi.");
    }

    public Ixi getIxi() {
        return ixi;
    }

    @Override
    public IxiContext getContext() {
        return context;
    }

    public ReportIxiContext getReportIxiContext() {
        return context;
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

    public DatagramSocket getSocket() {
        return socket;
    }

    private void createSocket() throws RuntimeException {
        if (socket != null) {
            log.debug("Closing current socket...");
            socket.close();
        }
        log.debug("getReportIxiContext(): " + getReportIxiContext());
        log.debug("getReportIxiContext().getReportPort(): " + getReportIxiContext().getReportPort());
        final int port = getReportIxiContext().getReportPort();
        log.debug(String.format("Opening new socket on port: %d...", port));
        try {
            socket = new DatagramSocket(getReportIxiContext().getReportPort());
        } catch (SocketException e) {
            log.error(String.format("Failed to open socket on port: %d", port));
            throw new RuntimeException(e);
        }
    }

    private void requestUuidFromRCS() {
        final RequestUuidPayload requestUuidPayload =
                new RequestUuidPayload(getReportIxiContext().getUuid(), getReportIxiContext().getExternalReportPort());
        send(requestUuidPayload, Constants.RCS_HOST, Constants.RCS_PORT);

        log.debug(String.format(
                "Sent RequestUuidPayload to RCS: %s",
                Payload.serialize(requestUuidPayload))
        );
        log.info("Requested uuid from RCS...");
    }
}

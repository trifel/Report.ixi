package org.iota.ict.ixi;

import com.ictreport.ixi.ReportIxiGossipListener;
import com.ictreport.ixi.api.Api;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.Cryptography;
import com.ictreport.ixi.utils.Metadata;
import com.ictreport.ixi.utils.Properties;

import java.io.File;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReportIxi extends IxiModule {

    private final static Logger LOGGER = LogManager.getLogger(ReportIxi.class);
    private Properties properties;
    private Metadata metadata;
    private final List<Neighbor> neighbors = new LinkedList<>();
    private Api api;
    private KeyPair keyPair;
    public final Object waitingForUuid = new Object();
    public byte state = STATE_TERMINATED;
    public final static byte STATE_TERMINATED = 0;
    public final static byte STATE_INITIALIZING = 1;
    public final static byte STATE_RUNNING = 2;
    public final static byte STATE_TERMINATING = 3;

    public ReportIxi(final Ixi ixi) {
        super(ixi);
    }

    @Override
    public void terminate() {
        state = STATE_TERMINATING;
        LOGGER.info("Terminating Report.ixi...");
        if (api != null) api.shutDown();
        super.terminate();
        state = STATE_TERMINATED;
        LOGGER.info("Report.ixi terminated.");
    }

    @Override
    public void run() {
        state = STATE_INITIALIZING;

        LOGGER.info(String.format("Report.ixi %s: Starting...", Constants.VERSION));
        properties = new Properties(Constants.PROPERTIES_FILE);
        properties.store(Constants.PROPERTIES_FILE);
        metadata = new Metadata(Constants.METADATA_FILE);
        keyPair = Cryptography.generateKeyPair(Constants.KEY_LENGTH);

        LOGGER.info("Assigning neighbor addresses...");
        for (final InetSocketAddress neighborAddress : properties.getNeighborAddresses()) {
            neighbors.add(new Neighbor(neighborAddress));
            if (neighbors.size() > 3) {
                throw new RuntimeException("Too many neighbors in properties!");
            }
        }

        LOGGER.info("Initiating API...");
        api = new Api(this);
        api.init();

        LOGGER.info("Request uuid from RCS...");

        synchronized (waitingForUuid) {
            try {
                api.getSender().requestUuid();
                waitingForUuid.wait();
            } catch (InterruptedException e) {
                LOGGER.error("Failed to receive uuid from RCS.");
            }
        }

        state = STATE_RUNNING;
        ixi.addGossipListener(new ReportIxiGossipListener(api));
        LOGGER.info(String.format("Report.ixi %s: Started on port: %d", Constants.VERSION, getProperties().getReportPort()));
    }

    public Properties getProperties() {
        return this.properties;
    }

    public Metadata getMetadata() {
        return this.metadata;
    }

    public List<Neighbor> getNeighbors() {
        return this.neighbors;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public Api getApi() {
        return api;
    }

    public Ixi getIxi() {
        return ixi;
    }

    public boolean isRunning() {
        return state == STATE_RUNNING;
    }
}

package com.ictreport.ixi;

import com.ictreport.ixi.api.Api;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.Cryptography;
import com.ictreport.ixi.utils.Properties;
import org.iota.ict.ixi.IctProxy;
import org.iota.ict.ixi.IxiModule;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReportIxi extends IxiModule {

    private final static Logger LOGGER = LogManager.getLogger(ReportIxi.class);
    private final Properties properties;
    private final List<Neighbor> neighbors = new LinkedList<>();
    private final Api api;
    private final KeyPair keyPair;

    public ReportIxi(final IctProxy proxy) {
        super(proxy);

        LOGGER.info(String.format("Report.ixi %s: Starting...", Constants.VERSION));
        properties = new Properties(Constants.PROPERTIES_FILE);
        properties.store(Constants.PROPERTIES_FILE);
        api = new Api(this);
        keyPair = Cryptography.generateKeyPair(Constants.KEY_LENGTH);
    }

    @Override
    public void terminate() {
        LOGGER.info(String.format("Report.ixi %s: Terminating...", Constants.VERSION));
        if (api != null) api.shutDown();
        super.terminate();
        LOGGER.info(String.format("Report.ixi %s: Terminated...", Constants.VERSION));
    }

    @Override
    public void run() {
        LOGGER.info(String.format("Report.ixi %s: Assigning neighbor addresses...", Constants.VERSION));
        for (final InetSocketAddress neighborAddress : properties.getNeighborAddresses()) {
            neighbors.add(new Neighbor(neighborAddress));
            if (neighbors.size() > 3) {
                throw new RuntimeException("Too many neighbors in properties!");
            }
        }

        LOGGER.info(String.format("Report.ixi %s: Initiating API...", Constants.VERSION));
        api.init();

        addGossipListener(new ReportIxiGossipListener(api));
        LOGGER.info(String.format("Report.ixi %s started!", Constants.VERSION));
    }

    public Properties getProperties() {
        return this.properties;
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
}

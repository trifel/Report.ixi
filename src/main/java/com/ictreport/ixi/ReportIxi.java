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
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReportIxi extends IxiModule {

    public final static Logger LOGGER = LogManager.getLogger(ReportIxi.class);

    private Properties properties;
    private final List<Neighbor> neighbors = new LinkedList<>();
    private static Api api = null;
    private KeyPair keyPair = null;

    public ReportIxi(IctProxy proxy) {
        super(proxy);
    }

    @Override
    public void terminate() {
        LOGGER.debug("Shutting down Report.ixi");
        if (api != null) api.shutDown();
        super.terminate();
    }

    @Override
    public void run() {
        LOGGER.info(String.format("Report.ixi %s started.", Constants.VERSION));

        final String propertiesFilePath = "report.ixi.cfg";
        properties = new Properties(propertiesFilePath);
        properties.store(propertiesFilePath);

        try {
            keyPair = Cryptography.generateKeyPair(1024);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        for (InetSocketAddress neighborAddress : properties.getNeighborAddresses()) {
            neighbors.add(new Neighbor(neighborAddress));
        }

        api = new Api(this);
        api.init();

        addGossipListener(new ReportIxiGossipListener(api));
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

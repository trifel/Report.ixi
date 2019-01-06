package com.ictreport.ixi;

import com.ictreport.ixi.api.Api;
import com.ictreport.ixi.exchange.*;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.Cryptography;
import org.iota.ict.ixi.IxiModule;
import org.iota.ict.network.event.GossipFilter;
import org.iota.ict.network.event.GossipReceiveEvent;
import org.iota.ict.network.event.GossipSubmitEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import com.ictreport.ixi.utils.Properties;

public class ReportIxi extends IxiModule {
    public final static Logger LOGGER = LogManager.getLogger(ReportIxi.class);

    private Properties properties;
    private final List<Neighbor> neighbors = new LinkedList<>();
    private static Api api = null;
    private KeyPair keyPair = null;
    private String uuid = null;

    public static void main(String[] args) {

        LOGGER.info(String.format("Report.ixi %s started.", Constants.VERSION));

        final String propertiesFilePath = (args.length == 0 ? "report.ixi.cfg" : args[0]);
        final Properties properties = new Properties(propertiesFilePath);
        properties.store(propertiesFilePath);

        try {
            new ReportIxi(properties);
        } catch (RuntimeException e) {
            LOGGER.info(String.format("Can't connect to Ict '%s'. Make sure that the Ict Client is running. Check 'ictName' in report.ixi.cfg and 'ixi_enabled=true' in ict.cfg.",
                properties.getIctName()));
            System.exit(0);
        }
    }

    public ReportIxi(Properties properties) {

        super(properties.getModuleName(), properties.getIctName());

        this.properties = properties;

        try {
            keyPair = Cryptography.generateKeyPair(1024);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        for (InetSocketAddress neighborAddress : properties.getNeighborAddresses()) {
            neighbors.add(new Neighbor(neighborAddress));
        }
              
        GossipFilter filter = new GossipFilter();
        filter.watchTag("REPORT9IXI99999999999999999");
        setGossipFilter(filter);
        
        api = new Api(this);
        api.init();

        api.getSender().requestUuid();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
              LOGGER.debug("Running Shutdown Hook");

              if (api != null) api.shutDown();
            }
        });
        
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Api getApi() {
        return api;
    }

    @Override
    public void onTransactionReceived(GossipReceiveEvent event) {
        if (api != null) {
            Payload payload = Payload.deserialize(event.getTransaction().decodedSignatureFragments);
            api.getReceiver().processPayload(null, payload);
        }
    }

    @Override
    public void onTransactionSubmitted(GossipSubmitEvent event) {
        
    }

    
}

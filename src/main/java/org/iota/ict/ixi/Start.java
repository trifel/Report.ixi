package org.iota.ict.ixi;

import com.ictreport.ixi.api.Api;
import com.ictreport.ixi.exchange.Payload;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.Cryptography;
import com.ictreport.ixi.utils.Properties;
import org.iota.ict.network.event.GossipFilter;
import org.iota.ict.network.event.GossipReceiveEvent;
import org.iota.ict.network.event.GossipSubmitEvent;

import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Start extends IxiModule {

    public final static Logger LOGGER = LogManager.getLogger(Start.class);

    static final String NAME = "Report.ixi";
    private Properties properties;
    private final List<Neighbor> neighbors = new LinkedList<>();
    private static Api api = null;
    private KeyPair keyPair = null;

    public Start(IctProxy ict) {
        super(ict);
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

    @Override
    public void onIctShutdown() {
        LOGGER.debug("Shutting down Report.ixi");
        if (api != null) api.shutDown();
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

        GossipFilter filter = new GossipFilter();
        filter.watchTag("REPORT9IXI99999999999999999");
        setGossipFilter(filter);

        api = new Api(this);
        api.init();
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

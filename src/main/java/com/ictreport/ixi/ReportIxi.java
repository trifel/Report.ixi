package com.ictreport.ixi;

import com.ictreport.ixi.api.Api;
import com.ictreport.ixi.model.Neighbor;

import org.iota.ict.ixi.IxiModule;
import org.iota.ict.network.event.GossipFilter;
import org.iota.ict.network.event.GossipReceiveEvent;
import org.iota.ict.network.event.GossipSubmitEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import com.ictreport.ixi.utils.Properties;

public class ReportIxi extends IxiModule {
    public static final String VERSION = "0.1";

    public final static Logger LOGGER = LogManager.getLogger(ReportIxi.class);

    private Properties properties;
    private final List<Neighbor> neighbors = new LinkedList<>();
    private static Api api = null;

    public static void main(String[] args) {

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

        InetSocketAddress neighborASocketAddress = new InetSocketAddress(properties.getNeighborAHost(), properties.getNeighborAPort());
        InetSocketAddress neighborBSocketAddress = new InetSocketAddress(properties.getNeighborBHost(), properties.getNeighborBPort());
        InetSocketAddress neighborCSocketAddress = new InetSocketAddress(properties.getNeighborCHost(), properties.getNeighborCPort());
        neighbors.add(new Neighbor(neighborASocketAddress.getAddress(), properties.getNeighborAPort()));
        neighbors.add(new Neighbor(neighborBSocketAddress.getAddress(), properties.getNeighborBPort()));
        neighbors.add(new Neighbor(neighborCSocketAddress.getAddress(), properties.getNeighborCPort()));
              
        GossipFilter filter = new GossipFilter();
        filter.watchTag("REPORT9IXI99999999999999999");
        setGossipFilter(filter);
        
        api = new Api(this);
        api.init();

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

    @Override
    public void onTransactionReceived(GossipReceiveEvent event) {
        LOGGER.info(String.format("message received '%s'",
                event.getTransaction().tag ));
        if (api != null) {
            api.getSender().reportTransactionReceived(event.getTransaction().decodedSignatureFragments);
        }
    }

    @Override
    public void onTransactionSubmitted(GossipSubmitEvent event) {
        LOGGER.info(String.format("message submitted '%s'",
                event.getTransaction().tag ));
        
    }

    
}

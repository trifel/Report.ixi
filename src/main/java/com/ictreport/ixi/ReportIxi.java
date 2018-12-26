package com.ictreport.ixi;

import com.ictreport.ixi.api.Api;
import com.ictreport.ixi.model.Neighbor;

import org.iota.ict.ixi.IxiModule;
import org.iota.ict.network.event.GossipReceiveEvent;
import org.iota.ict.network.event.GossipSubmitEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.ictreport.ixi.utils.Properties;

public class ReportIxi extends IxiModule {
    public final static Logger LOGGER = LogManager.getLogger(ReportIxi.class);

    private static final String NAME = "Report.ixi";

    private static final String DEFAULT_METADATA_FILE_PATH = "report.ixi.metadata";
    private static final String DEFAULT_PROPERTY_FILE_PATH = "report.ixi.cfg";
    private static final String DEFAULT_ICT_PROPERTY_FILE_PATH = "ict.cfg";

    private Properties properties = new Properties();
    private final List<Neighbor> neighbors = new LinkedList<>();
    private static Api api = null;

    public static void main(String[] args) {
        new ReportIxi();
    }

    public ReportIxi() {
        super(NAME);
        LOGGER.info(NAME + " started, waiting for Ict to connect ...");
        LOGGER.info("Just add '"+NAME+"' to 'ixis' in your ict.cfg file and restart your Ict.\n");

        initialize();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
              LOGGER.debug("Running Shutdown Hook");

              if (api != null) api.shutDown();
            }
        });
    }

    /**
     * General initializing when the module is loaded.
     */
    private void initialize() {
        loadIctProperties();
        loadReportProperties();
        loadOrCreateMetadata();
    }

    private void loadIctProperties() {
        if (new File(DEFAULT_ICT_PROPERTY_FILE_PATH).exists()) {
            org.iota.ict.utils.Properties ictProperties = org.iota.ict.utils.Properties.fromFile(DEFAULT_ICT_PROPERTY_FILE_PATH);
            properties.loadFromIctProperties(ictProperties, neighbors);
        } else {
            LOGGER.error("The file '" + DEFAULT_ICT_PROPERTY_FILE_PATH + "' could not be found.");
            System.exit(0);
        }
    }

    private void loadReportProperties() {
        try {
            if (new File(DEFAULT_PROPERTY_FILE_PATH).exists()) {

                java.util.Properties reportProperties = new java.util.Properties();
                FileInputStream dataInputStream = new FileInputStream(DEFAULT_PROPERTY_FILE_PATH);
                reportProperties.load(dataInputStream);

                properties.loadFromReportProperties(reportProperties, neighbors);

                dataInputStream.close();
            } else {
                LOGGER.error("The file '" + DEFAULT_PROPERTY_FILE_PATH + "' could not be found.");
                System.exit(0);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            LOGGER.error("The file '" + DEFAULT_PROPERTY_FILE_PATH + "' could not be loaded.");
            System.exit(0);
        }
    }

    /**
     * Loading the unique identifier that has been generated upon initial start of 
     * the Report.ixi-module. This uuid is stored within the "report.ixi.metadata"-file.
     */
    private void loadOrCreateMetadata() {
        String nodeUuid = null;

        try {
            java.util.Properties metaData = new java.util.Properties();
            FileInputStream metaDataInputStream = new FileInputStream(DEFAULT_METADATA_FILE_PATH);
            metaData.load(metaDataInputStream);
            nodeUuid = metaData.getProperty("uuid", nodeUuid);
        } catch (IOException exception) {}
    
        if (nodeUuid != null) {
            nodeUuid = nodeUuid.trim();
        } else {
            nodeUuid = UUID.randomUUID().toString();
            try {
                FileOutputStream metaDataOutputStream = new FileOutputStream(DEFAULT_METADATA_FILE_PATH);
                java.util.Properties metaData = new java.util.Properties();
                metaData.put("uuid", nodeUuid);
                metaData.store(metaDataOutputStream, "Report.ixi");
                metaDataOutputStream.close();
            } catch (IOException exception) {
                exception.printStackTrace();
                LOGGER.error("The file '" + DEFAULT_METADATA_FILE_PATH + "' could not be saved.");
                System.exit(0);
            }
        }

        properties.setUuid(nodeUuid);
    }

    public Properties getProperties() {
        return this.properties;
    }

    public List<Neighbor> getNeighbors() {
        return this.neighbors;
    }

    @Override
    public void onIctConnect(String name) {
        LOGGER.info("Ict '" + name + "' connected");
        
        api = new Api(this);
        api.init();
    }

    @Override
    public void onTransactionReceived(GossipReceiveEvent event) {
        LOGGER.info("message '" + event.getTransaction().decodedSignatureFragments );

    }

    @Override
    public void onTransactionSubmitted(GossipSubmitEvent event) {
        
    }

    
}

package com.ictreport.ixi;

import com.ictreport.ixi.api.Api;
import org.iota.ict.ixi.IxiModule;
import org.iota.ict.network.event.GossipReceiveEvent;
import org.iota.ict.network.event.GossipSubmitEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.UUID;

import com.ictreport.ixi.utils.Properties;

public class ReportIxi extends IxiModule {

    private static final Logger log = LoggerFactory.getLogger(ReportIxi.class);

    private static final String NAME = "Report.ixi";

    private static final String DEFAULT_METADATA_FILE_PATH = "report.ixi.metadata";
    private static final String DEFAULT_PROPERTY_FILE_PATH = "report.ixi.cfg";
    private static final String DEFAULT_ICT_PROPERTY_FILE_PATH = "ict.cfg";

    private Properties properties = new Properties();

    private static Api api = null;

    public static void main(String[] args) {
        new ReportIxi();
    }

    public ReportIxi() {
        super(NAME);
        log.info(NAME + " started, waiting for Ict to connect ...");
        log.info("Just add '"+NAME+"' to 'ixis' in your ict.cfg file and restart your Ict.\n");

        initialize();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
              log.debug("Running Shutdown Hook");

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
            properties.loadFromIctProperties(ictProperties);
        } else {
            log.error("The file 'ict.cfg' could not be found.");
            System.exit(0);
        }
    }

    private void loadReportProperties() {
        try {
            if (new File(DEFAULT_PROPERTY_FILE_PATH).exists()) {

                java.util.Properties reportProperties = new java.util.Properties();
                FileInputStream dataInputStream = new FileInputStream(DEFAULT_PROPERTY_FILE_PATH);
                reportProperties.load(dataInputStream);

                properties.loadFromReportProperties(reportProperties);

                dataInputStream.close();
            } else {
                log.error("The file 'report.ixi.cfg' could not be found.");
                System.exit(0);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("The file '" + DEFAULT_PROPERTY_FILE_PATH + "' could not be loaded.");
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
                log.error("The file '" + DEFAULT_METADATA_FILE_PATH + "' could not be saved.");
                System.exit(0);
            }
        }

        properties.setUuid(nodeUuid);
    }

    @Override
    public void onIctConnect(String name) {
        log.info("Ict '" + name + "' connected");
        
        api = new Api(properties);
        api.init();
    }

    @Override
    public void onTransactionReceived(GossipReceiveEvent event) {
        
    }

    @Override
    public void onTransactionSubmitted(GossipSubmitEvent event) {
        
    }

    
}

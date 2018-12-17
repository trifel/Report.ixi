package com.ictreport.ixi;

import org.iota.ict.utils.Properties;
import org.iota.ict.ixi.IxiModule;
import org.iota.ict.network.event.GossipReceiveEvent;
import org.iota.ict.network.event.GossipSubmitEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ReportIxi extends IxiModule {

    private static final Logger log = LoggerFactory.getLogger(ReportIxi.class);

    private static final String NAME = "Report.ixi";

    private static final String DEFAULT_METADATA_FILE_PATH = "report.ixi.metadata";
    private static final String DEFAULT_ICT_PROPERTY_FILE_PATH = "ict.cfg";

    private Properties ictProperties;
    private String ictName = "";

    private String nodeUUID = null;

    public static void main(String[] args) {
        new ReportIxi();
    }

    public ReportIxi() {
        super(NAME);
        log.info(NAME + " started, waiting for Ict to connect ...");
        log.info("Just add '"+NAME+"' to 'ixis' in your ict.cfg file and restart your Ict.\n");

        initialize();
    }

    private void initialize() {
        loadIctProperties();
        loadOrCreateMetadata();
    }

    private void loadIctProperties() {
        if (new File(DEFAULT_ICT_PROPERTY_FILE_PATH).exists()) {
            this.ictProperties = Properties.fromFile(DEFAULT_ICT_PROPERTY_FILE_PATH);
        } else {
            log.error("The file 'ict.cfg' could not be found.");
            System.exit(0);
        }
    }

    /**
     * Loading the unique identifier that has been generated upon initial start of 
     * the Report.ixi-module. This uuid is stored within the“report.ixi.metadata”-file located 
     * in the ICT root directory.
     */
    private void loadOrCreateMetadata() {
        try {
            java.util.Properties metaData = new java.util.Properties();
            FileInputStream metaDataInputStream = new FileInputStream(DEFAULT_METADATA_FILE_PATH);
            metaData.load(metaDataInputStream);
            nodeUUID = metaData.getProperty("uuid", nodeUUID);
        } catch (IOException exception) {}
    
        if (nodeUUID != null) {
            nodeUUID = nodeUUID.trim();
        } else {
            nodeUUID = UUID.randomUUID().toString();
            try {
                FileOutputStream metaDataOutputStream = new FileOutputStream(DEFAULT_METADATA_FILE_PATH);
                java.util.Properties metaData = new java.util.Properties();
                metaData.put("uuid", nodeUUID);
                metaData.store(metaDataOutputStream, "Report.ixi");
                metaDataOutputStream.close();
            } catch (IOException exception) {
                exception.printStackTrace();
                log.error("The file '" + DEFAULT_METADATA_FILE_PATH + "' could not be saved.");
                System.exit(0);
            }
        }
    }

    @Override
    public void onIctConnect(String name) {
        log.info("Ict '" + name + "' connected");
        this.ictName = name;

    }

    @Override
    public void onTransactionReceived(GossipReceiveEvent event) {
        
    }

    @Override
    public void onTransactionSubmitted(GossipSubmitEvent event) {
        
    }
}

package com.ictreport.ixi.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class Properties extends java.util.Properties {

    private final static Logger LOGGER = LogManager.getLogger(Properties.class);
    private static final String LIST_DELIMITER = ",";

    // Property names
    private final static String NAME = "name";
    private final static String HOST = "host";
    private final static String REPORT_PORT = "reportPort";
    private final static String EXTERNAL_REPORT_PORT = "externalReportPort";
    private final static String NEIGHBORS = "neighbors";

    // Property defaults
    private final static String DEFAULT_NAME = "";
    private final static String DEFAULT_HOST = "0.0.0.0";
    private final static int    DEFAULT_REPORT_PORT = 1338;
    private final static int    DEFAULT_EXTERNAL_REPORT_PORT = -1;
    private final static String DEFAULT_NEIGHBORS = "";

    public Properties() {

    }

    /**
     * @return the reportPort
     */
    public int getReportPort() {
        return Integer.parseInt(getProperty(REPORT_PORT, Integer.toString(DEFAULT_REPORT_PORT)).trim());
    }

    /**
     * @param reportPort the reportPort to set
     */
    public void setReportPort(final int reportPort) {
        put(REPORT_PORT, Integer.toString(reportPort));
    }

    /**
     * @param externalReportPort the externalReportPort to set
     */
    public void setExternalReportPort(final int externalReportPort) {
        put(EXTERNAL_REPORT_PORT, Integer.toString(externalReportPort));
    }

    /**
     * * @return the externalReportPort
     */
    public int getExternalReportPort() {
        return Integer.parseInt(getProperty(EXTERNAL_REPORT_PORT, Integer.toString(DEFAULT_EXTERNAL_REPORT_PORT)).trim());
    }

    /**
     * @return the host
     */
    public String getHost() {
        return getProperty(HOST, DEFAULT_HOST).trim();
    }

    /**
     * @param host the host to set
     */
    public void setHost(final String host) {
        put(HOST, host);
    }

    /**
     * @return the name
     */
    public String getName() {
        return getProperty(NAME, DEFAULT_NAME).trim();
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        put(NAME, name);
    }

    @SuppressWarnings("unchecked")
    public Enumeration keys() {
        final Enumeration keysEnum = super.keys();
        final Vector<String> keyList = new Vector<>();
        while(keysEnum.hasMoreElements()){
            keyList.add((String)keysEnum.nextElement());
        }
        Collections.sort(keyList);
        return keyList.elements();
    }
}
package com.ictreport.ixi.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Vector;

public class Properties extends java.util.Properties {
    private final static Logger LOGGER = LogManager.getLogger(Properties.class);

    // Property names
    private final static String MODULE_NAME = "moduleName";
    private final static String NAME = "name";
    private final static String UUID = "uuid";
    private final static String HOST = "host";
    private final static String REPORT_PORT = "reportPort";
    private final static String NEIGHBOR_A_HOST = "neighborAHost";
    private final static String NEIGHBOR_A_PORT = "neighborAPort";
    private final static String NEIGHBOR_B_HOST = "neighborBHost";
    private final static String NEIGHBOR_B_PORT = "neighborBPort";
    private final static String NEIGHBOR_C_HOST = "neighborCHost";
    private final static String NEIGHBOR_C_PORT = "neighborCPort";

    // Property defaults
    private final static String DEFAULT_MODULE_NAME = "Report.ixi";
    private final static String DEFAULT_NAME = "";
    private final static String DEFAULT_UUID = java.util.UUID.randomUUID().toString();
    private final static String DEFAULT_HOST = "0.0.0.0";
    private final static int    DEFAULT_REPORT_PORT = 1338;
    private final static String DEFAULT_NEIGHBOR_A_HOST = "";
    private final static int    DEFAULT_NEIGHBOR_A_PORT = 1338;
    private final static String DEFAULT_NEIGHBOR_B_HOST = "";
    private final static int    DEFAULT_NEIGHBOR_B_PORT = 1338;
    private final static String DEFAULT_NEIGHBOR_C_HOST = "";
    private final static int    DEFAULT_NEIGHBOR_C_PORT = 1338;

    public Properties() {

        // Add required properties
        setRequiredProps();
    }

    public Properties(final String propertiesFilePath) {

        // Load properties from filesystem
        load(propertiesFilePath);

        // Add required properties
        setRequiredProps();
    }

    /**
     * @return the ixi module name
     */
    public String getModuleName() {

        return getProperty(MODULE_NAME, DEFAULT_MODULE_NAME).trim();
    }

    /**
     * @param moduleName the ixi module name to set
     */
    public void setModuleName(final String moduleName) {

        put(MODULE_NAME, moduleName);
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
     * @return the uuid
     */
    public String getUuid() {

        return getProperty(UUID, DEFAULT_UUID).trim();
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(final String uuid) {

        put(UUID, uuid);
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
    public void setName(final String name) throws InvalidPropertiesFormatException {

        if (!name.matches(".+\\s\\(ict-\\d+\\)")) {
            throw new InvalidPropertiesFormatException(String.format("Unable to set %s. Please follow the naming convention: %s",
                    NAME,
                    "\"<name> (ict-<number>)\""));
        }

        put(NAME, name);
    }

    public String getNeighborAHost() {

        return getProperty(NEIGHBOR_A_HOST, DEFAULT_NEIGHBOR_A_HOST).trim();
    }

    public void setNeighborAHost(final String host) {

        put(NEIGHBOR_A_HOST, host);
    }

    public int getNeighborAPort() {

        return Integer.parseInt(getProperty(NEIGHBOR_A_PORT, Integer.toString(DEFAULT_NEIGHBOR_A_PORT)).trim());
    }

    public void setNeighborAPort(final int port) {

        put(NEIGHBOR_A_PORT, Integer.toString(port));
    }

    public String getNeighborBHost() {

        return getProperty(NEIGHBOR_B_HOST, DEFAULT_NEIGHBOR_B_HOST).trim();
    }

    public void setNeighborBHost(final String host) {

        put(NEIGHBOR_B_HOST, host);
    }

    public int getNeighborBPort() {

        return Integer.parseInt(getProperty(NEIGHBOR_B_PORT, Integer.toString(DEFAULT_NEIGHBOR_B_PORT)).trim());
    }

    public void setNeighborBPort(final int port) {

        put(NEIGHBOR_B_PORT, Integer.toString(port));
    }

    public String getNeighborCHost() {

        return getProperty(NEIGHBOR_C_HOST, DEFAULT_NEIGHBOR_C_HOST).trim();
    }

    public void setNeighborCHost(final String host) {

        put(NEIGHBOR_C_HOST, host);
    }

    public int getNeighborCPort() {

        return Integer.parseInt(getProperty(NEIGHBOR_C_PORT, Integer.toString(DEFAULT_NEIGHBOR_C_PORT)).trim());
    }

    public void setNeighborCPort(final int port) {

        put(NEIGHBOR_C_PORT, Integer.toString(port));
    }

    public void load(String propertiesFilePath) {

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(propertiesFilePath);
            load(inputStream);
        } catch (FileNotFoundException e) {
            LOGGER.info(String.format("Could not read properties file '%s', therefore a new one will be created.",
                    propertiesFilePath));
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to open input stream of file: '%s'",
                    propertiesFilePath));
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error(String.format("Failed to close input stream of file: '%s'",
                            propertiesFilePath));
                    e.printStackTrace();
                }
            }
        }
    }

    public void store(String propertiesFilePath) {

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(propertiesFilePath);
            store(outputStream, null);
        } catch (FileNotFoundException e) {
            LOGGER.error(String.format("Failed to open output stream of file: '%s'",
                    propertiesFilePath));
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to save properties to file: '%s'",
                    propertiesFilePath));
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error(String.format("Failed to close output stream of file: '%s'",
                            propertiesFilePath));
                    e.printStackTrace();
                }
            }
        }
    }

    private void setRequiredProps() {

        if (get(UUID) == null) {
            put(UUID, DEFAULT_UUID);
        }
        if (get(NAME) == null) {
            put(NAME, DEFAULT_NAME);
        }
        if (get(HOST) == null) {
            put(HOST, DEFAULT_HOST);
        }
        if (get(REPORT_PORT) == null) {
            put(REPORT_PORT, Integer.toString(DEFAULT_REPORT_PORT));
        }
        if (get(NEIGHBOR_A_HOST) == null) {
            put(NEIGHBOR_A_HOST, DEFAULT_NEIGHBOR_A_HOST);
        }
        if (get(NEIGHBOR_A_PORT) == null) {
            put(NEIGHBOR_A_PORT, Integer.toString(DEFAULT_NEIGHBOR_A_PORT));
        }
        if (get(NEIGHBOR_B_HOST) == null) {
            put(NEIGHBOR_B_HOST, DEFAULT_NEIGHBOR_B_HOST);
        }
        if (get(NEIGHBOR_B_PORT) == null) {
            put(NEIGHBOR_B_PORT, Integer.toString(DEFAULT_NEIGHBOR_B_PORT));
        }
        if (get(NEIGHBOR_C_HOST) == null) {
            put(NEIGHBOR_C_HOST, DEFAULT_NEIGHBOR_C_HOST);
        }
        if (get(NEIGHBOR_C_PORT) == null) {
            put(NEIGHBOR_C_PORT, Integer.toString(DEFAULT_NEIGHBOR_C_PORT));
        }
    }

    @SuppressWarnings("unchecked")
    public Enumeration keys() {
        Enumeration keysEnum = super.keys();
        Vector<String> keyList = new Vector<>();
        while(keysEnum.hasMoreElements()){
            keyList.add((String)keysEnum.nextElement());
        }
        Collections.sort(keyList);
        return keyList.elements();
    }
}
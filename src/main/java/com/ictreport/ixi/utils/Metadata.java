package com.ictreport.ixi.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Metadata extends java.util.Properties {

    private final static Logger LOGGER = LogManager.getLogger("Metadata");

    // Property names
    private final static String UUID = "uuid";

    // Property defaults
    private final static String DEFAULT_UUID = "";

    public Metadata() {
        setRequiredProps();
    }

    public Metadata(final String metdataFilePath) {
        load(metdataFilePath);
        setRequiredProps();
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

    public void load(final String metadataFilePath) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(metadataFilePath);
            load(inputStream);
        } catch (final FileNotFoundException e) {
            LOGGER.info(String.format("Could not read metadata file '%s', therefore a new one will be created.",
                    metadataFilePath));
        } catch (final IOException e) {
            LOGGER.error(String.format("Failed to open input stream of file: '%s'", metadataFilePath));
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error(String.format("Failed to close input stream of file: '%s'",
                            metadataFilePath));
                    e.printStackTrace();
                }
            }
        }
    }

    public void store(final String metadataFilePath) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(metadataFilePath);
            store(outputStream, null);
        } catch (final FileNotFoundException e) {
            LOGGER.error(String.format("Failed to open output stream of file: '%s'",
                    metadataFilePath));
            e.printStackTrace();
        } catch (final IOException e) {
            LOGGER.error(String.format("Failed to save metdata to file: '%s'",
                    metadataFilePath));
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error(String.format("Failed to close output stream of file: '%s'",
                            metadataFilePath));
                    e.printStackTrace();
                }
            }
        }
    }

    private void setRequiredProps() {
        if (get(UUID) == null) {
            put(UUID, DEFAULT_UUID);
        }
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
package org.iota.ict.ixi;

import com.ictreport.ixi.ReportIxiGossipListener;
import com.ictreport.ixi.api.Api;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.Cryptography;
import com.ictreport.ixi.utils.Metadata;
import com.ictreport.ixi.utils.Properties;

import java.io.File;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.context.ConfigurableIxiContext;
import org.iota.ict.ixi.context.IxiContext;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReportIxi extends IxiModule {

    private final static Logger LOGGER = LogManager.getLogger(ReportIxi.class);
    private final ReportIxiContext context = new ReportIxiContext();
    private final Properties properties = new Properties();
    private Metadata metadata;
    private List<Neighbor> neighbors = new LinkedList<>();
    private Api api;
    private KeyPair keyPair;
    public final Object waitingForUuid = new Object();
    private byte state = STATE_TERMINATED;
    private final static byte STATE_TERMINATED = 0;
    private final static byte STATE_INITIALIZING = 1;
    private final static byte STATE_RUNNING = 2;
    private final static byte STATE_TERMINATING = 3;

    public ReportIxi(final Ixi ixi) {
        super(ixi);
        context.applyConfiguration();
    }

    @Override
    public void terminate() {
        state = STATE_TERMINATING;
        LOGGER.info("Terminating Report.ixi...");
        if (api != null) api.shutDown();
        super.terminate();
        state = STATE_TERMINATED;
        LOGGER.info("Report.ixi terminated.");
    }

    @Override
    public void run() {
        state = STATE_INITIALIZING;

        LOGGER.info(String.format("Report.ixi %s: Starting...", Constants.VERSION));
        metadata = new Metadata(Constants.METADATA_FILE);
        keyPair = Cryptography.generateKeyPair(Constants.KEY_LENGTH);

        LOGGER.info("Initiating API...");
        api = new Api(this);
        api.init();

        LOGGER.info("Request uuid from RCS...");

        synchronized (waitingForUuid) {
            try {
                api.getSender().requestUuid();
                waitingForUuid.wait();
            } catch (InterruptedException e) {
                LOGGER.error("Failed to receive uuid from RCS.");
            }
        }

        state = STATE_RUNNING;
        ixi.addGossipListener(new ReportIxiGossipListener(api));
        LOGGER.info(String.format("Report.ixi %s: Started on port: %d", Constants.VERSION, getProperties().getReportPort()));
    }

    public Properties getProperties() {
        return this.properties;
    }

    public Metadata getMetadata() {
        return this.metadata;
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

    public Ixi getIxi() {
        return ixi;
    }

    public boolean isRunning() {
        return state == STATE_RUNNING;
    }

    @Override
    public IxiContext getContext() {
        return context;
    }

    private class ReportIxiContext extends ConfigurableIxiContext {

        private static final String FIELD_NAME = "name";
        private static final String FIELD_NEIGHBORS = "neighbors";
        private static final String FIELD_REPORT_PORT = "reportPort";

        protected ReportIxiContext() {
            super(new JSONObject()
                    .put(FIELD_NAME, "name (ict-1)")
                    .put(FIELD_REPORT_PORT, "1338")
                    .put(FIELD_NEIGHBORS, new JSONArray(neighbors)));
        }

        @Override
        protected void validateConfiguration(JSONObject newConfiguration) {
            validateName(newConfiguration);
            validateNeighbors(newConfiguration);
            validateReportPort(newConfiguration);
        }

        @Override
        protected void applyConfiguration() {
            properties.setName(configuration.getString(FIELD_NAME));
            properties.setReportPort(configuration.getInt(FIELD_REPORT_PORT));
            JSONArray neighborsArray = configuration.getJSONArray(FIELD_NEIGHBORS);
            neighbors = new ArrayList<>();
            for(Object neighbor : neighborsArray.toList())
                neighbors.add(new Neighbor(inetSocketAddressFromString((String) neighbor)));
        }

        private void validateName(final JSONObject newConfiguration) {

            if (!newConfiguration.has(FIELD_NAME)) {
                throw new IllegalPropertyException(FIELD_NAME, "not defined");
            }
            if (!(newConfiguration.get(FIELD_NAME) instanceof String)) {
                throw new IllegalPropertyException(FIELD_NAME, "not a string");
            }
            if (!newConfiguration.getString(FIELD_NAME).matches(".+\\s\\(ict-\\d+\\)")) {
                throw new IllegalPropertyException(FIELD_NAME,
                        "please follow the naming convention: \"<name> (ict-<number>)\"");
            }
        }

        private void validateNeighbors(final JSONObject newConfiguration) {

            if (!newConfiguration.has(FIELD_NEIGHBORS)) {
                throw new IllegalPropertyException(FIELD_NEIGHBORS, "not defined");
            }
            if(!(newConfiguration.get(FIELD_NEIGHBORS) instanceof JSONArray)) {
                throw new IllegalPropertyException(FIELD_NEIGHBORS, "not an array");
            }

            JSONArray array = newConfiguration.getJSONArray(FIELD_NEIGHBORS);

            if (array.length() > 3) {
                throw new IllegalPropertyException(FIELD_NEIGHBORS, "maximum 3 neighbors allowed");
            }

            for(int i = 0; i < array.length(); i++) {
                if (!(array.get(i) instanceof String)) {
                    throw new IllegalPropertyException(FIELD_NEIGHBORS, "array element at index " + i + " is not a string");
                }

                inetSocketAddressFromString((String)array.get(i));
            }
        }

        private void validateReportPort(final JSONObject newConfiguration) {

            if (!newConfiguration.has(FIELD_REPORT_PORT)) {
                throw new IllegalPropertyException(FIELD_REPORT_PORT, "not defined");
            }
            if (!(newConfiguration.get(FIELD_REPORT_PORT) instanceof Integer)) {
                throw new IllegalPropertyException(FIELD_REPORT_PORT, "not an integer");
            }
            if (newConfiguration.getInt(FIELD_REPORT_PORT) < 0 || newConfiguration.getInt(FIELD_REPORT_PORT) > 65535) {
                throw new IllegalPropertyException(FIELD_REPORT_PORT, "port must be within range 0-65535");
            }
        }

        private InetSocketAddress inetSocketAddressFromString(final String address) {
            int portColonIndex;
            for (portColonIndex = address.length() - 1; address.charAt(portColonIndex) != ':'; portColonIndex--);
            final String hostString = address.substring(0, portColonIndex);
            final int port = Integer.parseInt(address.substring(portColonIndex + 1, address.length()));
            return new InetSocketAddress(hostString, port);
        }
    }

    private class IllegalPropertyException extends IllegalArgumentException {

        private IllegalPropertyException(String field, String cause) {
            super("Invalid property '"+field+"': " + cause + ".");
        }
    }
}

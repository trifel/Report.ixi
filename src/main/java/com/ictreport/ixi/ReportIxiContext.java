package com.ictreport.ixi;

import org.iota.ict.ixi.context.ConfigurableIxiContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

public class ReportIxiContext extends ConfigurableIxiContext {

    // Property names
    private static final String           HOST                         = "host";
    private static final String           REPORT_PORT                  = "reportPort";
    private static final String           EXTERNAL_REPORT_PORT         = "externalReportPort";
    private static final String           NAME                         = "name";
    private static final String           NEIGHBORS                    = "neighbors";

    // Property defaults
    private static final JSONObject       DEFAULT_CONFIGURATION        = new JSONObject();
    private static final String           DEFAULT_HOST                 = null;
    private static final int              DEFAULT_REPORT_PORT          = 1338;
    private static final Integer          DEFAULT_EXTERNAL_REPORT_PORT = null;
    private static final String           DEFAULT_NAME                 = "YOUR_NAME (ict-0)";
    private static final JSONArray        DEFAULT_NEIGHBORS            = new JSONArray();

    // Context properties
    private String                        host                         = DEFAULT_HOST;
    private int                           reportPort                   = DEFAULT_REPORT_PORT;
    private Integer                       externalReportPort           = DEFAULT_EXTERNAL_REPORT_PORT;
    private String                        name                         = DEFAULT_NAME;
    private final List<InetSocketAddress> neighbors                    = new LinkedList<>();

    static {
        DEFAULT_CONFIGURATION.put(HOST, DEFAULT_HOST);
        DEFAULT_CONFIGURATION.put(REPORT_PORT, DEFAULT_REPORT_PORT);
        DEFAULT_CONFIGURATION.put(EXTERNAL_REPORT_PORT, DEFAULT_EXTERNAL_REPORT_PORT);
        DEFAULT_CONFIGURATION.put(NAME, DEFAULT_NAME);
        DEFAULT_CONFIGURATION.put(NEIGHBORS, DEFAULT_NEIGHBORS);
    }

    public ReportIxiContext() {
        super(DEFAULT_CONFIGURATION);
    }

    @Override
    public JSONObject getConfiguration() {
        JSONObject configuration = new JSONObject()
                .put(REPORT_PORT, getReportPort())
                .put(NAME, getName())
                .put(NEIGHBORS, new JSONArray(getNeighbors()));

        // Optional configuration properties
        if (!getHost().equals("0.0.0.0")) {
            configuration.put(HOST, getHost());
        }
        if (getExternalReportPort() != -1) {
            configuration.put(EXTERNAL_REPORT_PORT, getExternalReportPort());
        }

        return configuration;
    }

    @Override
    protected void validateConfiguration(final JSONObject newConfiguration) {
        validateReportPort(newConfiguration);
        validateName(newConfiguration);
        validateNeighbors(newConfiguration);
    }

    @Override
    public void applyConfiguration() {
        getNeighbors().clear();
        setName(configuration.getString(NAME));
        setReportPort(configuration.getInt(REPORT_PORT));
        for(Object neighbor : configuration.getJSONArray(NEIGHBORS).toList()) {
            getNeighbors().add(inetSocketAddressFromString((String) neighbor));
        }

        // Optional configuration properties
        if (configuration.has(HOST)) {
            setHost(configuration.getString(HOST));
        }
        if (configuration.has(EXTERNAL_REPORT_PORT)) {
            setExternalReportPort(configuration.getInt(EXTERNAL_REPORT_PORT));
        }
    }

    private void validateHost(final JSONObject newConfiguration) {
        if (newConfiguration.has(HOST)) {
            throw new IllegalPropertyException(HOST, "not defined");
        }
        if (!(newConfiguration.get(HOST) instanceof String)) {
            throw new IllegalPropertyException(HOST, "not a string");
        }
        // TODO: Validate format of string
    }

    private void validateReportPort(final JSONObject newConfiguration) {
        if (!newConfiguration.has(REPORT_PORT)) {
            throw new IllegalPropertyException(REPORT_PORT, "not defined");
        }
        if (!(newConfiguration.get(REPORT_PORT) instanceof Integer)) {
            throw new IllegalPropertyException(REPORT_PORT, "not an integer");
        }
        if (newConfiguration.getInt(REPORT_PORT) < 0 || newConfiguration.getInt(REPORT_PORT) > 65535) {
            throw new IllegalPropertyException(REPORT_PORT, "port must be within range 0-65535");
        }
    }

    private void validateExternalReportPort(final JSONObject newConfiguration) {
        if (!newConfiguration.has(EXTERNAL_REPORT_PORT)) {
            throw new IllegalPropertyException(EXTERNAL_REPORT_PORT, "not defined");
        }
        if (!(newConfiguration.get(EXTERNAL_REPORT_PORT) instanceof Integer)) {
            throw new IllegalPropertyException(EXTERNAL_REPORT_PORT, "not an integer");
        }
        if (newConfiguration.getInt(EXTERNAL_REPORT_PORT) < 0 || newConfiguration.getInt(EXTERNAL_REPORT_PORT) > 65535) {
            throw new IllegalPropertyException(EXTERNAL_REPORT_PORT, "port must be within range 0-65535");
        }
    }

    private void validateName(final JSONObject newConfiguration) {
        if (!newConfiguration.has(NAME)) {
            throw new IllegalPropertyException(NAME, "not defined");
        }
        if (!(newConfiguration.get(NAME) instanceof String)) {
            throw new IllegalPropertyException(NAME, "not a string");
        }
        if (!newConfiguration.getString(NAME).matches(".+\\s\\(ict-\\d+\\)")) {
            throw new IllegalPropertyException(NAME, "please follow the naming convention: \"<name> (ict-<number>)\"");
        }
    }

    private void validateNeighbors(final JSONObject newConfiguration) {
        if (!newConfiguration.has(NEIGHBORS)) {
            throw new IllegalPropertyException(NEIGHBORS, "not defined");
        }
        if(!(newConfiguration.get(NEIGHBORS) instanceof JSONArray)) {
            throw new IllegalPropertyException(NEIGHBORS, "not an array");
        }

        JSONArray array = newConfiguration.getJSONArray(NEIGHBORS);

        if (array.length() > 3) {
            throw new IllegalPropertyException(NEIGHBORS, "maximum 3 neighbors allowed");
        }

        for(int i = 0; i < array.length(); i++) {
            if (!(array.get(i) instanceof String)) {
                throw new IllegalPropertyException(NEIGHBORS, "array element at index " + i + " is not a string");
            }

            // TODO: Validate neighbor address
            final String address = (String) array.get(i);

        }
    }

    private InetSocketAddress inetSocketAddressFromString(final String address) {
        int portColonIndex;
        for (portColonIndex = address.length() - 1; address.charAt(portColonIndex) != ':'; portColonIndex--);
        final String hostString = address.substring(0, portColonIndex);
        final int port = Integer.parseInt(address.substring(portColonIndex + 1, address.length()));
        return new InetSocketAddress(hostString, port);
    }

    public String getHost() {
        // Optional configuration property
        if (host == null) {
            return "0.0.0.0";
        }

        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getReportPort() {
        return reportPort;
    }

    public void setReportPort(int reportPort) {
        this.reportPort = reportPort;
    }

    public List<InetSocketAddress> getNeighbors() {
        return neighbors;
    }

    public Integer getExternalReportPort() {
        // Optional configuration property
        if (externalReportPort == null) {
            return -1;
        }

        return externalReportPort;
    }

    public void setExternalReportPort(Integer externalReportPort) {
        this.externalReportPort = externalReportPort;
    }

    private class IllegalPropertyException extends IllegalArgumentException {
        private IllegalPropertyException(String field, String cause) {
            super("Invalid property '"+field+"': " + cause + ".");
        }
    }
}

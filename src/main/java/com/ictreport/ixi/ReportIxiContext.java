package com.ictreport.ixi;

import com.ictreport.ixi.model.Address;
import com.ictreport.ixi.model.AddressAndStats;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.IctRestCaller;
import org.iota.ict.ixi.ReportIxi;
import org.iota.ict.ixi.context.ConfigurableIxiContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReportIxiContext extends ConfigurableIxiContext {

    private final static Logger LOGGER = LogManager.getLogger("ReportIxiContext");
    private final        ReportIxi        reportIxi;

    // Property names
    private static final String           ICT_REST_PORT                = "Ict REST API Port";
    private static final String           ICT_REST_PASSWORD            = "Ict REST API Password";
    private static final String           HOST                         = "Ict Host";
    private static final String           REPORT_PORT                  = "Report.ixi Port";
    private static final String           EXTERNAL_REPORT_PORT         = "External Report.ixi Port";
    private static final String           NAME                         = "Name";
    private static final String           NEIGHBORS                    = "Neighbors";
    private static final String           NEIGHBOR_ADDRESS             = "_address";
    private static final String           NEIGHBOR_REPORT_PORT         = "reportPort";

    // Property defaults
    private static final String           DEFAULT_ICT_VERSION          = "";
    private static final int              DEFAULT_ICT_ROUND_DURATION   = 60000;
    private static final int              DEFAULT_ICT_REST_PORT        = 2187;
    private static final String           DEFAULT_ICT_REST_PASSWORD    = "change_me_now";
    private static final JSONObject       DEFAULT_CONFIGURATION        = new JSONObject();
    private static final String           DEFAULT_HOST                 = null;
    private static final int              DEFAULT_REPORT_PORT          = 1338;
    private static final Integer          DEFAULT_EXTERNAL_REPORT_PORT = null;
    private static final String           DEFAULT_NAME                 = "YOUR_NAME (ict-0)";
    private static final JSONArray        DEFAULT_NEIGHBORS            = new JSONArray();

    // Context properties
    private String                        ictVersion                   = DEFAULT_ICT_VERSION;
    private int                           ictRoundDuration             = DEFAULT_ICT_ROUND_DURATION;
    private int                           ictRestPort                  = DEFAULT_ICT_REST_PORT;
    private String                        ictRestPassword              = DEFAULT_ICT_REST_PASSWORD;
    private String                        host                         = DEFAULT_HOST;
    private int                           reportPort                   = DEFAULT_REPORT_PORT;
    private Integer                       externalReportPort           = DEFAULT_EXTERNAL_REPORT_PORT;
    private String                        name                         = DEFAULT_NAME;

    static {
        DEFAULT_CONFIGURATION.put(ICT_REST_PORT, DEFAULT_ICT_REST_PORT);
        DEFAULT_CONFIGURATION.put(ICT_REST_PASSWORD, DEFAULT_ICT_REST_PASSWORD);
        DEFAULT_CONFIGURATION.put(HOST, DEFAULT_HOST);
        DEFAULT_CONFIGURATION.put(REPORT_PORT, DEFAULT_REPORT_PORT);
        DEFAULT_CONFIGURATION.put(EXTERNAL_REPORT_PORT, DEFAULT_EXTERNAL_REPORT_PORT);
        DEFAULT_CONFIGURATION.put(NAME, DEFAULT_NAME);
        DEFAULT_CONFIGURATION.put(NEIGHBORS, DEFAULT_NEIGHBORS.toString());
    }

    public ReportIxiContext(final ReportIxi reportIxi) {
        super(DEFAULT_CONFIGURATION);
        this.reportIxi = reportIxi;

        applyConfiguration();
    }

    @Override
    public JSONObject getConfiguration() {

        reportIxi.syncIctNeighbors();

        final List<JSONObject> jsonNeighbor = new LinkedList<>();
        for (Neighbor neighbor : reportIxi.getNeighbors()) {
            final Address address = neighbor.getAddress();
            jsonNeighbor.add(new JSONObject()
                    .put(NEIGHBOR_ADDRESS, address.getIctSocketAddress().toString())
                    .put(NEIGHBOR_REPORT_PORT, address.getReportPort()));
        }
        final JSONArray jsonNeighbors = new JSONArray(jsonNeighbor);

        JSONObject configuration = new JSONObject()
                .put(ICT_REST_PORT, getIctRestPort())
                .put(ICT_REST_PASSWORD, getIctRestPassword())
                .put(REPORT_PORT, getReportPort())
                .put(NAME, getName())
                .put(NEIGHBORS, jsonNeighbors.toString());

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
        validateIctRestConnectivity(newConfiguration);
    }

    @Override
    public void applyConfiguration() {
        setIctRestPort(configuration.getInt(ICT_REST_PORT));
        setIctRestPassword(configuration.getString(ICT_REST_PASSWORD));
        setName(configuration.getString(NAME));
        setReportPort(configuration.getInt(REPORT_PORT));

        if (configuration.get(NEIGHBORS) instanceof String) {
            setNeighbors(new JSONArray(configuration.getString(NEIGHBORS)));
        } else if (configuration.get(NEIGHBORS) instanceof JSONArray) {
            setNeighbors(configuration.getJSONArray(NEIGHBORS));
        } else {
            setNeighbors(new JSONArray());
        }
        
        // Optional configuration properties
        if (configuration.has(HOST)) {
            setHost(configuration.getString(HOST));
        }
        if (configuration.has(EXTERNAL_REPORT_PORT)) {
            setExternalReportPort(configuration.getInt(EXTERNAL_REPORT_PORT));
        }
    }

    private void validateIctRestConnectivity(final JSONObject newConfiguration) {
        if (!newConfiguration.has(ICT_REST_PASSWORD)) {
            throw new IllegalPropertyException(ICT_REST_PASSWORD, "not defined");
        }
        if (!(newConfiguration.get(ICT_REST_PASSWORD) instanceof String)) {
            throw new IllegalPropertyException(ICT_REST_PASSWORD, "not a string");
        }
        final JSONObject response = IctRestCaller.getInfo(newConfiguration.getInt(ICT_REST_PORT), newConfiguration.getString(ICT_REST_PASSWORD));
        if (response == null) {
            throw new IllegalPropertyException("Ict REST API port/password", "Connectivity check with Ict REST API failed");
        }
    }

    private void validateHost(final JSONObject newConfiguration) {
        if (!newConfiguration.has(HOST)) {
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
        /*if (!(newConfiguration.get(REPORT_PORT) instanceof Integer)) {
            throw new IllegalPropertyException(REPORT_PORT, "not an integer");
        }*/
        if (newConfiguration.getInt(REPORT_PORT) < 0 || newConfiguration.getInt(REPORT_PORT) > 65535) {
            throw new IllegalPropertyException(REPORT_PORT, "port must be within range 0-65535");
        }
    }

    private void validateExternalReportPort(final JSONObject newConfiguration) {
        if (!newConfiguration.has(EXTERNAL_REPORT_PORT)) {
            throw new IllegalPropertyException(EXTERNAL_REPORT_PORT, "not defined");
        }
        /*if (!(newConfiguration.get(EXTERNAL_REPORT_PORT) instanceof Integer)) {
            throw new IllegalPropertyException(EXTERNAL_REPORT_PORT, "not an integer");
        }*/
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
        if (newConfiguration.getString(NAME).equals(DEFAULT_NAME)) {
            throw new IllegalPropertyException(NAME,
                    String.format("please assign your personal ict name instead of '%s'", DEFAULT_NAME));
        }
    }

    private void validateNeighbors(final JSONObject newConfiguration) {
        if (!newConfiguration.has(NEIGHBORS)) {
            throw new IllegalPropertyException(NEIGHBORS, "not defined");
        }
        /*if(!(newConfiguration.get(NEIGHBORS) instanceof JSONArray)) {
            throw new IllegalPropertyException(NEIGHBORS, "not an array");
        }*/

        JSONArray array = new JSONArray(newConfiguration.getString(NEIGHBORS));

        if (array.length() > 3) {
            throw new IllegalPropertyException(NEIGHBORS, "maximum 3 neighbors allowed");
        }

        for(int i = 0; i < array.length(); i++) {
            if (!(array.get(i) instanceof JSONObject)) {
                throw new IllegalPropertyException(NEIGHBORS, "array element at index " + i + " is not a JSONObject");
            }
        }
    }

    public String getIctVersion() {
        return ictVersion;
    }

    public void setIctVersion(String ictVersion) {
        this.ictVersion = ictVersion;
    }

    public int getIctRoundDuration() {
        return ictRoundDuration;
    }

    public void setIctRoundDuration(int ictRoundDuration) {
        this.ictRoundDuration = ictRoundDuration;
    }

    public int getIctRestPort() {
        return ictRestPort;
    }

    public void setIctRestPort(int ictRestPort) {
        this.ictRestPort = ictRestPort;
    }

    public String getIctRestPassword() {
        return ictRestPassword;
    }

    public void setIctRestPassword(String ictRestPassword) {
        this.ictRestPassword = ictRestPassword;
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

    public void setNeighbors(JSONArray neighbors) {

        List<AddressAndStats> addressesAndStatsToSync = new ArrayList<>();

        for (int i=0; i<neighbors.length(); i++) {
            JSONObject neighbor = neighbors.getJSONObject(i);
            String neighborAddress = neighbor.getString(NEIGHBOR_ADDRESS);
            int neighborReportPort = neighbor.getInt(NEIGHBOR_REPORT_PORT);

            final Address address = Address.parse(neighborAddress);
            address.setReportPort(neighborReportPort);
            addressesAndStatsToSync.add(new AddressAndStats(address));
        }

        reportIxi.syncNeighbors(addressesAndStatsToSync, true);
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

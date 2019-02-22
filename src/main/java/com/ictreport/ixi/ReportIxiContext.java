package com.ictreport.ixi;

import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.IctRestCaller;
import org.iota.ict.ixi.ReportIxi;
import org.iota.ict.ixi.context.ConfigurableIxiContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReportIxiContext extends ConfigurableIxiContext {

    private final static Logger LOGGER = LogManager.getLogger(ReportIxiContext.class);
    private final        ReportIxi        reportIxi;

    // Property names
    private static final String           ICT_REST_PASSWORD            = "ictRestPassword";
    private static final String           HOST                         = "host";
    private static final String           REPORT_PORT                  = "reportPort";
    private static final String           EXTERNAL_REPORT_PORT         = "externalReportPort";
    private static final String           NAME                         = "name";
    private static final String           NEIGHBORS                    = "neighbors";
    private static final String           NEIGHBOR_ADDRESS             = "_address";
    private static final String           NEIGHBOR_REPORT_PORT         = "reportPort";

    // Property defaults
    private static final String           DEFAULT_ICT_REST_PASSWORD    = "change_me_now";
    private static final JSONObject       DEFAULT_CONFIGURATION        = new JSONObject();
    private static final String           DEFAULT_HOST                 = null;
    private static final int              DEFAULT_REPORT_PORT          = 1338;
    private static final Integer          DEFAULT_EXTERNAL_REPORT_PORT = null;
    private static final String           DEFAULT_NAME                 = "YOUR_NAME (ict-0)";
    private static final String           DEFAULT_NEIGHBOR_ADDRESS     = "missing";
    private static final int              DEFAULT_NEIGHBOR_REPORT_PORT = 1338;
    private static final JSONArray        DEFAULT_NEIGHBORS            = new JSONArray();

    // Context properties
    private String                        ictRestPassword              = DEFAULT_ICT_REST_PASSWORD;
    private String                        host                         = DEFAULT_HOST;
    private int                           reportPort                   = DEFAULT_REPORT_PORT;
    private Integer                       externalReportPort           = DEFAULT_EXTERNAL_REPORT_PORT;
    private String                        name                         = DEFAULT_NAME;
    private JSONArray                     neighbors                    = DEFAULT_NEIGHBORS;

    // Other
    private String                        ictVersion                   = "";

    static {
        DEFAULT_CONFIGURATION.put(ICT_REST_PASSWORD, DEFAULT_ICT_REST_PASSWORD);
        DEFAULT_CONFIGURATION.put(HOST, DEFAULT_HOST);
        DEFAULT_CONFIGURATION.put(REPORT_PORT, DEFAULT_REPORT_PORT);
        DEFAULT_CONFIGURATION.put(EXTERNAL_REPORT_PORT, DEFAULT_EXTERNAL_REPORT_PORT);
        DEFAULT_CONFIGURATION.put(NAME, DEFAULT_NAME);
        DEFAULT_CONFIGURATION.put(NEIGHBORS, DEFAULT_NEIGHBORS);
    }

    public ReportIxiContext(final ReportIxi reportIxi) {
        super(DEFAULT_CONFIGURATION);
        this.reportIxi = reportIxi;

        applyConfiguration();
        loadIctInfo();
        matchNeighborsWithIct();
    }

    @Override
    public JSONObject getConfiguration() {

        matchNeighborsWithIct();
        JSONObject configuration = new JSONObject()
                .put(ICT_REST_PASSWORD, getIctRestPassword())
                .put(REPORT_PORT, getReportPort())
                .put(NAME, getName())
                .put(NEIGHBORS, getNeighbors());

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
        setIctRestPassword(configuration.getString(ICT_REST_PASSWORD));
        setName(configuration.getString(NAME));
        setReportPort(configuration.getInt(REPORT_PORT));
        setNeighbors(configuration.getJSONArray(NEIGHBORS));

        // Optional configuration properties
        if (configuration.has(HOST)) {
            setHost(configuration.getString(HOST));
        }
        if (configuration.has(EXTERNAL_REPORT_PORT)) {
            setExternalReportPort(configuration.getInt(EXTERNAL_REPORT_PORT));
        }
    }

    private void validateIctRestPassword(final JSONObject newConfiguration) {
        if (newConfiguration.has(ICT_REST_PASSWORD)) {
            throw new IllegalPropertyException(ICT_REST_PASSWORD, "not defined");
        }
        if (!(newConfiguration.get(ICT_REST_PASSWORD) instanceof String)) {
            throw new IllegalPropertyException(ICT_REST_PASSWORD, "not a string");
        }
        // TODO: Validate format of string
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
            if (!(array.get(i) instanceof JSONObject)) {
                throw new IllegalPropertyException(NEIGHBORS, "array element at index " + i + " is not a JSONObject");
            }
        }
    }

    private InetSocketAddress inetSocketAddressFromString(final String address) {
        int portColonIndex;
        for (portColonIndex = address.length() - 1; address.charAt(portColonIndex) != ':'; portColonIndex--);
        final String hostString = address.substring(0, portColonIndex);
        final int port = Integer.parseInt(address.substring(portColonIndex + 1, address.length()));
        return new InetSocketAddress(hostString, port);
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

    public JSONArray getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(JSONArray neighbors) {
        this.neighbors = neighbors;

        reportIxi.getNeighbors().clear();
        for (int i=0; i<neighbors.length(); i++) {
            JSONObject neighbor = neighbors.getJSONObject(i);
            String neighborAddress = neighbor.getString(NEIGHBOR_ADDRESS);
            if (!neighborAddress.isEmpty()) {
                reportIxi.getNeighbors().add(new Neighbor(inetSocketAddressFromString(neighborAddress)));
            }
        }
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

    public String getIctVersion() {
        return ictVersion;
    }

    private class IllegalPropertyException extends IllegalArgumentException {
        private IllegalPropertyException(String field, String cause) {
            super("Invalid property '"+field+"': " + cause + ".");
        }
    }

    private JSONArray getMatchedNeighborsWithIct() {
        List<JSONObject> newNeighbors = new LinkedList<>();
        JSONObject ictConfig = IctRestCaller.getConfig(getIctRestPassword());

        if (ictConfig == null) {
            // TODO: Return something else than null?
            return null;
        }

        JSONArray ictNeighbors = ictConfig.getJSONArray("neighbors");

        // Keep only report.ixi neighbors that also present in the ict config
        for (int i=0; i<getNeighbors().length(); i++) {
            JSONObject currentNeighbor = getNeighbors().getJSONObject(i);
            String currentNeighborAddress = currentNeighbor.getString(NEIGHBOR_ADDRESS);
            if (ictNeighbors.toList().contains(currentNeighborAddress)) {
                newNeighbors.add(currentNeighbor);
            } else {
                LOGGER.info(String.format("%s doesn't exists in Ict config." +
                                "Therefore, it is removed from report.ixi configuration.",
                        currentNeighborAddress));
            }
        }

        // Add neighbors that are not yet present in report.ixi configuration
        for (int i=0; i<ictNeighbors.length(); i++) {
            String ictNeighborAddress = ictNeighbors.getString(i);

            boolean found = false;
            for (int j=0; j<newNeighbors.size(); j++) {
                JSONObject currentNeighbor = newNeighbors.get(j);
                String currentNeighborAddress = currentNeighbor.getString(NEIGHBOR_ADDRESS);

                if (ictNeighborAddress.equals(currentNeighborAddress)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                LOGGER.info(String.format("%s exists in Ict config, but not present in report.ixi configuration." +
                                "Therefore, it is added to report.ixi configuration.",
                        ictNeighborAddress));
                newNeighbors.add(new JSONObject()
                        .put(NEIGHBOR_ADDRESS, ictNeighborAddress)
                        .put(NEIGHBOR_REPORT_PORT, DEFAULT_NEIGHBOR_REPORT_PORT));
            }
        }

        return new JSONArray(newNeighbors);
    }

    public void matchNeighborsWithIct() {
        final JSONArray neighbors = getMatchedNeighborsWithIct();
        if (neighbors != null) {
            setNeighbors(neighbors);
        }
    }

    public void loadIctInfo() {
        final JSONObject ictInfo = IctRestCaller.getInfo(getIctRestPassword());
        if (ictInfo != null) {
            String version = ictInfo.getString("version");
            this.ictVersion = version;
        }
    }
}

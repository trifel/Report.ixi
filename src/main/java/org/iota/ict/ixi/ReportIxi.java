package org.iota.ict.ixi;

import com.ictreport.ixi.ReportIxiContext;
import com.ictreport.ixi.ReportIxiGossipListener;
import com.ictreport.ixi.api.Api;
import com.ictreport.ixi.model.Address;
import com.ictreport.ixi.model.AddressAndStats;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.model.Stats;
import com.ictreport.ixi.utils.ConfigurationMigrator;
import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.IctRestCaller;
import com.ictreport.ixi.utils.Metadata;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.context.IxiContext;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReportIxi extends IxiModule {

    private static final Logger log = LogManager.getLogger("ReportIxi");
    private final ReportIxiContext context;
    private Metadata metadata;
    private final List<Neighbor> neighbors = new LinkedList<>();
    private Api api;
    public final Object waitingForUuid = new Object();
    private byte state = STATE_TERMINATED;
    private final static byte STATE_TERMINATED = 0;
    private final static byte STATE_INITIALIZING = 1;
    private final static byte STATE_RUNNING = 2;
    private final static byte STATE_TERMINATING = 3;

    public ReportIxi(final Ixi ixi) {
        super(ixi);

        // Attempt config migration from older Report.ixi versions if config is not found for the current version.
        ConfigurationMigrator.migrateIfConfigurationMissing();

        this.context = new ReportIxiContext(this);
        createDirectoryIfNotExists();
    }

    @Override
    public void terminate() {
        state = STATE_TERMINATING;
        log.info("Terminating Report.ixi...");
        if (api != null) api.shutDown();
        try {
            super.terminate();
        } catch (IllegalStateException e) {
            // TODO: handle exception.
            // After uninstalling Report.ixi module via Ict web GUI and then
            // terminating the Ict process causes this exception to throw.
        }
        state = STATE_TERMINATED;
        log.info("Report.ixi terminated.");
    }

    @Override
    public void install() {
        createDirectoryIfNotExists();
    }

    @Override
    public void uninstall() {

    }

    @Override
    public void run() {
        state = STATE_INITIALIZING;

        log.info(String.format("Report.ixi %s: Starting...", Constants.VERSION));
        metadata = new Metadata(Constants.METADATA_FILE);

        log.info("Initiating API...");
        api = new Api(this);
        api.init();

        log.info("Request uuid from RCS...");

        synchronized (waitingForUuid) {
            try {
                api.getSender().requestUuid();
                waitingForUuid.wait();
            } catch (InterruptedException e) {
                log.error("Failed to receive uuid from RCS.");
            }
        }

        state = STATE_RUNNING;
        ixi.addGossipListener(new ReportIxiGossipListener(api));
        log.info(String.format("Report.ixi %s: Started on port: %d",
                Constants.VERSION,
                getReportIxiContext().getReportPort()));
    }

    @Override
    public IxiContext getContext() {
        return context;
    }

    public ReportIxiContext getReportIxiContext() {
        return context;
    }

    public Metadata getMetadata() {
        return this.metadata;
    }

    public List<Neighbor> getNeighbors() {
        synchronized (this.neighbors) {
            return new LinkedList<>(this.neighbors);
        }
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

    private void createDirectoryIfNotExists() {
        if(!Constants.METADATA_DIRECTORY.exists()) {
            if (!Constants.METADATA_DIRECTORY.mkdirs()) {
                throw new RuntimeException("Failed to create metadata folder");
            }
        }
    }

    public void syncIct() {
        syncIctConfig();
        syncIctInfo();
        syncIctNeighbors();
    }

    public void syncIctConfig() {
        final JSONObject response = IctRestCaller.getConfig(
                getReportIxiContext().getIctRestPort(),
                getReportIxiContext().getIctRestPassword()
        );

        if (response != null) {
            getReportIxiContext().setIctRoundDuration(response.getNumber("round_duration").intValue());
        }
    }

    public void syncIctInfo() {
        final JSONObject response = IctRestCaller.getInfo(
                getReportIxiContext().getIctRestPort(),
                getReportIxiContext().getIctRestPassword()
        );

        if (response != null) {
            getReportIxiContext().setIctVersion(response.getString("version"));
        }
    }

    public void syncIctNeighbors() {
        final JSONArray response = IctRestCaller.getNeighbors(
                getReportIxiContext().getIctRestPort(),
                getReportIxiContext().getIctRestPassword()
        );

        final List<AddressAndStats> addressesAndStatsToSync = new LinkedList<>();
        for (int i=0; response != null && i<response.length(); i++) {
            final JSONObject ictNeighbor = (JSONObject)response.get(i);
            final String ictNeighborAddress = ictNeighbor.getString("address");

            try {
                final Address address = Address.parse(ictNeighborAddress);

                final JSONArray statsArray = ictNeighbor.getJSONArray("stats");

                JSONObject stats = null;
                if (getReportIxiContext().getIctVersion().equals("0.5")) {
                    // If this version of Report.ixi is operated on Ict 0.5, it will always try to get
                    // the last metrics/stats record from Ict api.
                    if (statsArray.length() > 0) {
                        stats = statsArray.getJSONObject(statsArray.length() - 1);
                    }
                } else {
                    // If this version of Report.ixi is on any newer version than Ict 0.5, it will always
                    // try to get the second to last metrics/stats record from Ict api.
                    if (statsArray.length() > 1) {
                        stats = statsArray.getJSONObject(statsArray.length() - 2);
                    }
                }

                if (stats != null) {
                    addressesAndStatsToSync.add(new AddressAndStats(
                            address,
                            new Stats(
                                stats.getNumber("timestamp").longValue(),
                                stats.getInt("all"),
                                stats.getInt("new"),
                                stats.getInt("ignored"),
                                stats.getInt("invalid"),
                                stats.getInt("requested")
                            )
                    ));
                } else {
                    addressesAndStatsToSync.add(new AddressAndStats(address));
                }

            } catch (Exception e) {
                e.printStackTrace();
                log.warn(String.format(
                        "Failed to parse InetSocketAddress from [%s] received from Ict REST API.",
                        ictNeighborAddress
                ));
            }
        }

        syncNeighbors(addressesAndStatsToSync, false);
    }

    public void syncNeighbors(List<AddressAndStats> addressesAndStats, boolean applyReportPort) {

        final List<Neighbor> keepNeighbors = new LinkedList<>();

        for (AddressAndStats addressAndStats : addressesAndStats) {
            Neighbor syncedNeighbor = null;
            for (Neighbor neighbor : getNeighbors()) {
                if (neighbor.isSyncableAddress(addressAndStats.getAddress())) {
                    neighbor.syncAddressAndStats(addressAndStats, applyReportPort);
                    syncedNeighbor = neighbor;
                    break;
                }
            }

            if (syncedNeighbor != null) {
                keepNeighbors.add(syncedNeighbor);
            } else {
                keepNeighbors.add(new Neighbor(addressAndStats.getAddress()));
            }
        }

        synchronized (this.neighbors) {
            neighbors.clear();
            neighbors.addAll(keepNeighbors);
        }
    }
}

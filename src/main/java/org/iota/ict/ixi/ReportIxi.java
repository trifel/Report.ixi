package org.iota.ict.ixi;

import com.ictreport.ixi.ReportIxiContext;
import com.ictreport.ixi.ReportIxiGossipListener;
import com.ictreport.ixi.api.Api;
import com.ictreport.ixi.model.Address;
import com.ictreport.ixi.model.AddressAndStats;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.ConfigurationMigrator;
import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.IctRestCaller;
import com.ictreport.ixi.utils.Metadata;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.context.IxiContext;
import org.iota.ict.utils.IOHelper;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReportIxi extends IxiModule {

    private final static Logger LOGGER = LogManager.getLogger(ReportIxi.class);
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

        // Attempt config migration from older Report.ixi version if config is found for the current version.
        if (!ConfigurationMigrator.configurationExists()) {
            if (ConfigurationMigrator.migrate("0.5")) {
                LOGGER.info("Report.ixi config migration completed successfully.");
            } else {
                LOGGER.info("Report.ixi config migration failed.");
            }
        }

        this.context = new ReportIxiContext(this);
        createDirectoryIfNotExists();
    }

    @Override
    public void terminate() {
        state = STATE_TERMINATING;
        LOGGER.info("Terminating Report.ixi...");
        if (api != null) api.shutDown();
        try {
            super.terminate();
        } catch (IllegalStateException e) {
            // TODO: handle exception.
            // After uninstalling Report.ixi module via Ict web GUI and then
            // terminating the Ict process causes this exception to throw.
        }
        state = STATE_TERMINATED;
        LOGGER.info("Report.ixi terminated.");
    }

    @Override
    public void install() {
        createDirectoryIfNotExists();
    }

    @Override
    public void uninstall() {
        IOHelper.deleteRecursively(Constants.METADATA_DIRECTORY);
    }

    @Override
    public void run() {
        state = STATE_INITIALIZING;

        LOGGER.info(String.format("Report.ixi %s: Starting...", Constants.VERSION));
        metadata = new Metadata(Constants.METADATA_FILE);

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
        LOGGER.info(String.format("Report.ixi %s: Started on port: %d",
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
        return this.neighbors;
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

                if (statsArray.length() > 0) {
                    final JSONObject stats = statsArray.getJSONObject(statsArray.length() - 1);

                    addressesAndStatsToSync.add(new AddressAndStats(
                            address,
                            stats.getNumber("timestamp").longValue(),
                            stats.getInt("all"),
                            stats.getInt("new"),
                            stats.getInt("ignored"),
                            stats.getInt("invalid"),
                            stats.getInt("requested")
                    ));
                } else {
                    addressesAndStatsToSync.add(new AddressAndStats(address));
                }

            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn(String.format(
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

        getNeighbors().clear();
        getNeighbors().addAll(keepNeighbors);
    }
}

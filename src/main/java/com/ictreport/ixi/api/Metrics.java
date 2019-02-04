package com.ictreport.ixi.api;

import com.ictreport.ixi.model.Neighbor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.ReportIxi;

public class Metrics {

    private final static Logger LOGGER = LogManager.getLogger(Metrics.class);
    private static int nonNeighborPingCount = 0;
    private static int nonNeighborInvalidCount = 0;

    public static void finishAndLog(final ReportIxi reportIxi) {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Report.ixi metrics:\n")
                .append(String.format("| %1$-8s | %2$-5s | %3$-8s | %4$-10s\n",
                        "METADATA",
                        "PINGS",
                        "INVALID",
                        "NEIGHBOR"))
                .append(String.format("| %1$-8s | %2$-5s | %3$-8s | %4$-10s\n",
                        "COUNT",
                        "COUNT",
                        "COUNT",
                        "ADDRESS"));

        LOGGER.info("Neighbor count: " + reportIxi.getNeighbors().size());
        for (Neighbor neighbor : reportIxi.getNeighbors()) {
            stringBuilder.append(String.format("| %1$-8d | %2$-5d | %3$-8d | %4$-10s\n",
                    neighbor.getMetadataCount(),
                    neighbor.getPingCount(),
                    neighbor.getInvalidCount(),
                    neighbor.getSocketAddress()));
        }

        stringBuilder.append(String.format("| %1$-8d | %2$-5d | %3$-8d | %4$-10s\n",
                0,
                nonNeighborPingCount,
                nonNeighborInvalidCount,
                "Other/Non-neighbor..."));

        LOGGER.info(stringBuilder.toString());

        resetNonNeighborPingCount();
        resetNonNeighborInvalidCount();

        for (final Neighbor neighbor : reportIxi.getNeighbors()) {
            neighbor.resetMetrics();
        }
    }

    public static void incrementNonNeighborPingCount() {
        Metrics.nonNeighborPingCount++;
    }

    public static void resetNonNeighborPingCount() {
        Metrics.nonNeighborPingCount = 0;
    }

    public static void incrementNonNeighborInvalidCount() {
        Metrics.nonNeighborInvalidCount++;
    }

    public static void resetNonNeighborInvalidCount() {
        Metrics.nonNeighborInvalidCount = 0;
    }
}

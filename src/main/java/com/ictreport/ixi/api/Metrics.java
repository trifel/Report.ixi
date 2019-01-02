package com.ictreport.ixi.api;

import com.ictreport.ixi.ReportIxi;
import com.ictreport.ixi.model.Neighbor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Metrics {
    public final static Logger LOGGER = LogManager.getLogger(Metrics.class);
    private static int nonNeighborPingCount = 0;
    private static int nonNeighborInvalidCount = 0;

    public static void logMetrics(ReportIxi reportIxi) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Report.ixi metrics:\n");
        stringBuilder.append(String.format("| %1$-8s | %2$-5s | %3$-8s | %4$-10s\n",
                "METADATA",
                "PINGS",
                "INVALID",
                "NEIGHBOR"));

        stringBuilder.append(String.format("| %1$-8s | %2$-5s | %3$-8s | %4$-10s\n",
                "COUNT",
                "COUNT",
                "COUNT",
                "ADDRESS"));

        for (Neighbor neighbor : reportIxi.getNeighbors()) {
            stringBuilder.append(String.format("| %1$-8d | %2$-5d | %3$-8d | %4$-10s\n",
                    neighbor.getMetadataCount(),
                    neighbor.getPingCount(),
                    neighbor.getInvalidCount(),
                    neighbor.getAddress()));
        }

        stringBuilder.append(String.format("| %1$-8d | %2$-5d | %3$-8d | %4$-10s\n",
                0,
                getNonNeighborPingCount(),
                getNonNeighborInvalidCount(),
                "Other/Non-neighbor..."));

        LOGGER.info(stringBuilder.toString());

        for (Neighbor neighbor : reportIxi.getNeighbors()) {
            neighbor.setMetadataCount(0);
            neighbor.setPingCount(0);
            neighbor.setInvalidCount(0);
            setNonNeighborPingCount(0);
        }

    }

    public static int getNonNeighborPingCount() {
        return nonNeighborPingCount;
    }

    public static void setNonNeighborPingCount(int nonNeighborPingCount) {
        Metrics.nonNeighborPingCount = nonNeighborPingCount;
    }

    public static int getNonNeighborInvalidCount() {
        return nonNeighborInvalidCount;
    }

    public static void setNonNeighborInvalidCount(int nonNeighborInvalidCount) {
        Metrics.nonNeighborInvalidCount = nonNeighborInvalidCount;
    }
}

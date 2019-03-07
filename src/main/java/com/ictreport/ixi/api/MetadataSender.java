package com.ictreport.ixi.api;

import com.ictreport.ixi.exchange.payloads.MetadataPayload;
import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.ReportIxi;
import org.iota.ict.utils.RestartableThread;

import java.util.concurrent.TimeUnit;

public class MetadataSender extends RestartableThread {

    private static final Logger log = LogManager.getLogger("ReportIxi/MetadataSender");

    private final ReportIxi reportIxi;
    private final Object waiter = new Object();

    public MetadataSender(final ReportIxi reportIxi) {
        super(log);
        this.reportIxi = reportIxi;
    }

    @Override
    public void run() {
        while (isRunning()) {
            if (reportIxi.getReportIxiContext().getUuid() != null) {
                for (final Neighbor neighbor : reportIxi.getReportIxiContext().getNeighbors()) {
                    final MetadataPayload metadataPayload =
                            new MetadataPayload(reportIxi.getReportIxiContext().getUuid(), Constants.VERSION);

                    reportIxi.send(metadataPayload, neighbor.getAddress().getReportSocketAddress());
                    log.debug(String.format(
                            "Sent MetadataPayload to neighbor [%s]: %s",
                            neighbor.getAddress().getReportSocketAddress().toString(),
                            Payload.serialize(metadataPayload))
                    );
                }
            } else {
                log.warn("Report.ixi has no uuid. MetadataPayload will not be sent to neighbors.");
            }

            synchronized (waiter) {
                try {
                    waiter.wait(TimeUnit.MINUTES.toMillis(1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onTerminate() {
        synchronized (waiter) {
            waiter.notify();
        }
    }
}

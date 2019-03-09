package com.ictreport.ixi.api;

import com.ictreport.ixi.exchange.payloads.NeighborPayload;
import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.exchange.payloads.StatusPayload;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.CPUMonitor;
import com.ictreport.ixi.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.ReportIxi;
import org.iota.ict.utils.RestartableThread;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StatusSender extends RestartableThread {

    private static final Logger log = LogManager.getLogger("ReportIxi/StatusSender");

    private final ReportIxi reportIxi;
    private final Object waiter = new Object();

    public StatusSender(final ReportIxi reportIxi) {
        super(log);
        this.reportIxi = reportIxi;
    }

    @Override
    public void run() {
        while (isRunning()) {
            if (reportIxi.getReportIxiContext().getUuid() != null) {
                reportIxi.getReportIxiContext().syncIct();

                final List<NeighborPayload> neighborPayloads = new LinkedList<>();

                for (Neighbor neighbor : reportIxi.getReportIxiContext().getNeighbors()) {
                    neighborPayloads.add(new NeighborPayload(
                            neighbor.getStats().getTimestamp(),
                            neighbor.getUuid(),
                            neighbor.getStats().getAllTx(),
                            neighbor.getStats().getNewTx(),
                            neighbor.getStats().getIgnoredTx(),
                            neighbor.getStats().getInvalidTx(),
                            neighbor.getStats().getRequestedTx()
                    ));
                }

                final StatusPayload statusPayload = new StatusPayload(
                        reportIxi.getReportIxiContext().getUuid(),
                        reportIxi.getReportIxiContext().getName(),
                        reportIxi.getReportIxiContext().getIctVersion(),
                        Constants.VERSION,
                        reportIxi.getReportIxiContext().getIctRoundDuration(),
                        neighborPayloads,
                        CPUMonitor.getSystemLoadAverage());

                reportIxi.send(statusPayload, Constants.RCS_HOST, Constants.RCS_PORT);

                log.debug(String.format(
                        "Sent StatusPayload to RCS: %s",
                        Payload.serialize(statusPayload))
                );
            } else {
                log.info("Report.ixi has no uuid yet. StatusPayload will not be sent to RCS.");
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

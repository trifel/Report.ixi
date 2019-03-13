package com.ictreport.ixi.api;

import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.exchange.payloads.PingPayload;
import com.ictreport.ixi.exchange.payloads.SubmittedPingPayload;
import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.RCSRestCaller;
import com.ictreport.ixi.utils.RandomStringGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.ReportIxi;
import org.iota.ict.model.transaction.TransactionBuilder;
import org.iota.ict.utils.RestartableThread;

import java.util.concurrent.TimeUnit;

public class PingSender extends RestartableThread {

    private static final Logger log = LogManager.getLogger("ReportIxi/PingSender");

    private final ReportIxi reportIxi;
    private final Object waiter = new Object();

    public PingSender(final ReportIxi reportIxi) {
        super(log);
        this.reportIxi = reportIxi;
    }

    @Override
    public void run() {
        final RandomStringGenerator randomStringGenerator = new RandomStringGenerator();

        while (isRunning()) {
            if (reportIxi.getReportIxiContext().getUuid() != null) {
                final PingPayload pingPayload = new PingPayload(randomStringGenerator.nextString());
                final String json = Payload.serialize(pingPayload);

                // Broadcast to neighbors
                final TransactionBuilder t = new TransactionBuilder();
                t.tag = Constants.TAG;
                t.asciiMessage(json);
                reportIxi.getIxi().submit(t.buildWhileUpdatingTimestamp());

                log.debug(String.format(
                        "Broadcasted PingPayload to Ict network: %s",
                        Payload.serialize(pingPayload))
                );

                // Send to RCS
                final SubmittedPingPayload submittedPingPayload =
                        new SubmittedPingPayload(reportIxi.getReportIxiContext().getUuid(), pingPayload);

                final String response = RCSRestCaller.send("submittedPingPayload", submittedPingPayload, false);

                if (response != null) {
                    log.debug(String.format(
                            "Sent SubmittedPingPayload to RCS: %s",
                            Payload.serialize(submittedPingPayload))
                    );
                }
            }

            if (isRunning()) {
                synchronized (waiter) {
                    try {
                        waiter.wait(TimeUnit.MINUTES.toMillis(5));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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

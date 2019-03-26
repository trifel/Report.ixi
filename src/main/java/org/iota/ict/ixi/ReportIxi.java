package org.iota.ict.ixi;

import com.ictreport.ixi.ReportIxiContext;
import com.ictreport.ixi.api.StatusSender;
import com.ictreport.ixi.configuration.Migrator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.context.IxiContext;

public class ReportIxi extends IxiModule {

    private static final Logger log = LogManager.getLogger("ReportIxi");

    private final ReportIxiContext context = new ReportIxiContext();

    public ReportIxi(Ixi ixi) {
        super(ixi);
        install();
    }

    @Override
    public void install() {
        // Attempt config migration from older Report.ixi versions if config is not found for the current version.
        Migrator.migrateIfConfigurationMissing();
    }

    @Override
    public void uninstall() {

    }

    @Override
    public void onStart() {
        log.info("Starting ReportIxi ...");

        subWorkers.add(new StatusSender(this));
    }

    @Override
    public void run() {
        try {
            while(isRunning()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            if (isRunning()) {
                e.printStackTrace();
                log.error("Report.ixi was unexpectedly interrupted");
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void onTerminate() {
        log.info("Stopped ReportIxi.");
    }

    public Ixi getIxi() {
        return ixi;
    }

    @Override
    public IxiContext getContext() {
        return context;
    }

    public ReportIxiContext getReportIxiContext() {
        return context;
    }
}

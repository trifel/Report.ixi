package org.iota.ict.ixi;

import com.ictreport.ixi.ReportIxiContext;
import com.ictreport.ixi.ReportIxiGossipListener;
import com.ictreport.ixi.api.Api;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.Constants;
import com.ictreport.ixi.utils.Cryptography;
import com.ictreport.ixi.utils.Metadata;

import java.security.KeyPair;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.context.IxiContext;
import org.iota.ict.utils.IOHelper;

public class ReportIxi extends IxiModule {

    public static final java.io.File METADATA_DIRECTORY = new java.io.File("modules/report.ixi/");

    private final static Logger LOGGER = LogManager.getLogger(ReportIxi.class);
    private final ReportIxiContext context = new ReportIxiContext();
    private Metadata metadata;
    private List<Neighbor> neighbors = new LinkedList<>();
    private Api api;
    private KeyPair keyPair;
    public final Object waitingForUuid = new Object();
    private byte state = STATE_TERMINATED;
    private final static byte STATE_TERMINATED = 0;
    private final static byte STATE_INITIALIZING = 1;
    private final static byte STATE_RUNNING = 2;
    private final static byte STATE_TERMINATING = 3;

    public ReportIxi(final Ixi ixi) {
        super(ixi);
        context.applyConfiguration();
    }

    @Override
    public void terminate() {
        state = STATE_TERMINATING;
        LOGGER.info("Terminating Report.ixi...");
        if (api != null) api.shutDown();
        super.terminate();
        state = STATE_TERMINATED;
        LOGGER.info("Report.ixi terminated.");
    }

    @Override
    public void install() {
        if(!METADATA_DIRECTORY.exists()) {
            if (!METADATA_DIRECTORY.mkdirs()) {
                throw new RuntimeException("Failed to create metadata folder");
            }
        }
    }

    @Override
    public void uninstall() {
        IOHelper.deleteRecursively(METADATA_DIRECTORY);
    }

    @Override
    public void run() {
        state = STATE_INITIALIZING;

        LOGGER.info(String.format("Report.ixi %s: Starting...", Constants.VERSION));
        metadata = new Metadata(Constants.METADATA_FILE);
        keyPair = Cryptography.generateKeyPair(Constants.KEY_LENGTH);

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

    public KeyPair getKeyPair() {
        return keyPair;
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
}

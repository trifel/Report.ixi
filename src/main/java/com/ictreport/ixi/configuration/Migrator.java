package com.ictreport.ixi.configuration;

import com.ictreport.ixi.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.List;

public class Migrator {

    private static final Logger log = LogManager.getLogger("ReportIxi/Migrator");

    public static void migrateIfConfigurationMissing() {
        if (!Migrator.configurationExists()) {
            log.debug("No configuration found for Report.ixi-" + Constants.VERSION);
            if (Migrator.migrate(Constants.getPreviousVersions())) {
                log.info("Report.ixi config migration completed successfully.");
            } else {
                log.info("Report.ixi config migration failed.");
            }
        }
    }

    public static boolean configurationExists() {
        return new File("modules/report.ixi-" + Constants.VERSION + ".jar.cfg").exists();
    }

    public static boolean migrate(List<String> fromVersions) {
        for (String previousVersion : fromVersions) {
            if (Migrator.migrate(previousVersion)) {
                return true;
            }
        }
        return false;
    }

    public static boolean migrate(String fromVersion) {
        final File oldConfig = new File("modules/report.ixi-" + fromVersion + ".jar.cfg");
        final File toConfig = new File(Constants.REPORT_IXI_CONFIG_FILE);

        if (!oldConfig.exists()) {
            log.info("Could not migrate from old configuration " + oldConfig.getPath() + " because it doesn't exist.");
            return false;
        }
        if (toConfig.exists()) {
            log.info("Destination configuration file " + toConfig.getPath() + " already exists. Aborting migration.");
            return false;
        }

        try {
            return copyFile(oldConfig, toConfig);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Failed to copy configuration " + oldConfig.getPath() + " to " + toConfig.getPath());
        }
        return false;
    }

    public static boolean copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            if (!destFile.createNewFile()) {
                log.error("Migration failed, could not create destination file: " + destFile.getPath());
                return false;
            }

            log.info("Migrating configuration from " + sourceFile.getPath() + " to " + destFile.getPath() + "...");
            FileChannel source = null;
            FileChannel destination = null;

            try {
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile).getChannel();
                destination.transferFrom(source, 0, source.size());
            }
            finally {
                if(source != null) {
                    source.close();
                }
                if(destination != null) {
                    destination.close();
                }
            }
            return true;
        } else {
            log.info("Aborted migration because the destination file already exists: " + destFile.getPath());
            return false;
        }
    }
}

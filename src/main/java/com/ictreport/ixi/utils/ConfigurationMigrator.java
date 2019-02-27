package com.ictreport.ixi.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.channels.FileChannel;

public class ConfigurationMigrator {

    private static final Logger LOGGER = LogManager.getLogger(ConfigurationMigrator.class);

    public static boolean configurationExists() {
        return new File("modules/report.ixi-" + Constants.VERSION + ".jar.cfg").exists();
    }

    public static boolean migrate(String fromVersion) {
        final File oldConfig = new File("modules/report.ixi-" + fromVersion + ".jar.cfg");
        final File toConfig = new File(Constants.REPORT_IXI_CONFIG_FILE);

        if (!oldConfig.exists()) {
            LOGGER.info("Could not migrate from old configuration " + oldConfig.getPath() + " because it doesn't exist.");
            return false;
        }
        if (toConfig.exists()) {
            LOGGER.info("Destination configuration file " + toConfig.getPath() + " already exists. Aborting migration.");
            return false;
        }

        try {
            return copyFile(oldConfig, toConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            if (!destFile.createNewFile()) {
                LOGGER.error("Migration failed, could not create destination file: " + destFile.getPath());
                return false;
            }

            LOGGER.info("Migrating configuration from " + sourceFile.getPath() + " to " + destFile.getPath() + "...");
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
            LOGGER.info("Aborted migration because the destination file already exists: " + destFile.getPath());
            return false;
        }
    }
}

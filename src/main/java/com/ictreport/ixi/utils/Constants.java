package com.ictreport.ixi.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String VERSION = "0.5.2";
    public static final String RCS_HOST = "api.ictreport.com";
    public static final int    RCS_PORT = 14265;
    public static final String TAG = "REPORT9IXI99999999999999999";
    public static final String PROPERTIES_LOCATION = "modules/report.ixi";
    public static final java.io.File METADATA_DIRECTORY = new java.io.File("modules/report.ixi/");
    public static final String METADATA_FILE = PROPERTIES_LOCATION+"/report.ixi.metadata";
    public static final String REPORT_IXI_CONFIG_FILE = "modules/report.ixi-" + Constants.VERSION + ".jar.cfg";
    private static final List<String> previousVersions = Arrays.asList("0.5.2-SNAPSHOT", "0.5.1", "0.5.1-SNAPSHOT", "0.5");

    public static List<String> getPreviousVersions() {
        return new ArrayList<>(previousVersions);
    }
}

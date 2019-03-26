package com.ictreport.ixi.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String VERSION = "0.5.3-SNAPSHOT";
    public static final String RCS_API = "http://api.ictreport.com/v1";
    public static final String TAG = "REPORT9IXI99999999999999999";
    public static final String REPORT_IXI_CONFIG_FILE = "modules/report.ixi-" + Constants.VERSION + ".jar.cfg";
    public static final int    REQUEST_TIMEOUT = 10;

    private static final List<String> previousVersions = Arrays.asList(
            "0.5.2",
            "0.5.2-SNAPSHOT",
            "0.5.1",
            "0.5.1-SNAPSHOT",
            "0.5"
    );

    public static List<String> getPreviousVersions() {
        return new ArrayList<>(previousVersions);
    }
}

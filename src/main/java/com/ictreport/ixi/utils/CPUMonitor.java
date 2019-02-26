package com.ictreport.ixi.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class CPUMonitor {

    public static int getSystemLoadAverage() {
        try {
            OperatingSystemMXBean systemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            final int availableProcessors = systemMXBean.getAvailableProcessors();

            if (availableProcessors > 0) {
                final double systemLoadAverage = systemMXBean.getSystemLoadAverage() / systemMXBean.getAvailableProcessors();

                // usually takes a couple of seconds before we get real values
                if (systemLoadAverage == -1.0) {
                    return -1;
                }

                // returns a percentage value with 1 decimal point precision
                return ((int) (systemLoadAverage * 100));
            }

            return -1;
        } catch (Exception e) {
            // Could not get the system load average
            return -1;
        }
    }
}

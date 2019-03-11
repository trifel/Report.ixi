package com.ictreport.ixi.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Neighbor {

    private static final Logger log = LogManager.getLogger("ReportIxi/Neighbor");
    private String uuid = null;
    private String address;
    private Stats stats = new Stats();
    private String publicAddress = "";

    public Neighbor(final String address) {
        this.address = address;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getAddress() {
        return address;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public String getPublicAddress() {
        return publicAddress;
    }

    public void setPublicAddress(String publicAddress) {
        this.publicAddress = publicAddress;
    }
}
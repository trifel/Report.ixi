package com.ictreport.ixi.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Neighbor {

    private static final Logger log = LogManager.getLogger("ReportIxi/Neighbor");

    private String uuid = null;
    private String address;
    private String publicAddress = "";
    private Long timestamp = null;
    private Integer allTx = null;
    private Integer newTx = null;
    private Integer ignoredTx = null;
    private Integer invalidTx = null;
    private Integer requestedTx = null;

    public Neighbor(final String address) {
        this.address = address;
    }

    public String getUuid() {
        return uuid;
    }

    public String getAddress() {
        return address;
    }

    public String getPublicAddress() {
        return publicAddress;
    }

    public void setPublicAddress(String publicAddress) {
        this.publicAddress = publicAddress;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getAllTx() {
        return allTx;
    }

    public void setAllTx(Integer allTx) {
        this.allTx = allTx;
    }

    public Integer getNewTx() {
        return newTx;
    }

    public void setNewTx(Integer newTx) {
        this.newTx = newTx;
    }

    public Integer getIgnoredTx() {
        return ignoredTx;
    }

    public void setIgnoredTx(Integer ignoredTx) {
        this.ignoredTx = ignoredTx;
    }

    public Integer getInvalidTx() {
        return invalidTx;
    }

    public void setInvalidTx(Integer invalidTx) {
        this.invalidTx = invalidTx;
    }

    public Integer getRequestedTx() {
        return requestedTx;
    }

    public void setRequestedTx(Integer requestedTx) {
        this.requestedTx = requestedTx;
    }

    @Override
    public String toString() {
        return "Neighbor{" +
                "uuid='" + uuid + '\'' +
                ", address='" + address + '\'' +
                ", publicAddress='" + publicAddress + '\'' +
                ", timestamp=" + timestamp +
                ", allTx=" + allTx +
                ", newTx=" + newTx +
                ", ignoredTx=" + ignoredTx +
                ", invalidTx=" + invalidTx +
                ", requestedTx=" + requestedTx +
                '}';
    }
}
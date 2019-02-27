package com.ictreport.ixi.model;

public class Stats {
    private Long timestamp;
    private Integer allTx;
    private Integer newTx;
    private Integer ignoredTx;
    private Integer invalidTx;
    private Integer requestedTx;

    public Stats() {
        this.timestamp = null;
        this.allTx = null;
        this.newTx = null;
        this.ignoredTx = null;
        this.invalidTx = null;
        this.requestedTx = null;
    }

    public Stats(Long timestamp, Integer allTx, Integer newTx, Integer ignoredTx, Integer invalidTx, Integer requestedTx) {
        this.timestamp = timestamp;
        this.allTx = allTx;
        this.newTx = newTx;
        this.ignoredTx = ignoredTx;
        this.invalidTx = invalidTx;
        this.requestedTx = requestedTx;
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
        return "Stats{" +
                "timestamp=" + timestamp +
                ", allTx=" + allTx +
                ", newTx=" + newTx +
                ", ignoredTx=" + ignoredTx +
                ", invalidTx=" + invalidTx +
                ", requestedTx=" + requestedTx +
                '}';
    }
}

package com.ictreport.ixi.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Neighbor extends AddressAndStats {

    private static final Logger LOGGER = LogManager.getLogger(Neighbor.class);
    private String uuid = null;
    private String reportIxiVersion = null;

    public Neighbor(final Address address) {
        super(address, null, 0, 0, 0, 0, 0);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getReportIxiVersion() {
        return reportIxiVersion;
    }

    public void setReportIxiVersion(final String reportIxiVersion) {
        this.reportIxiVersion = reportIxiVersion;
    }

    public boolean isSyncableAddress(Address address) {
        if (getAddress().equals(address)) {
            // Found a direct match
            return true;
        } else if (getAddress().getHostname().equals(address.getHostname()) &&
                !getAddress().getIp().equals(address.getIp()) &&
                getAddress().getPort() == address.getPort()) {
            // Hostname and port is equal, but ip is different.
            return true;
        } else if (!getAddress().getHostname().equals(address.getHostname()) &&
                getAddress().getIp().equals(address.getIp()) &&
                getAddress().getPort() == address.getPort()) {
            // Ip and port is equal, but hostname is different.
            return true;
        }
        return false;
    }

    public void syncAddressAndStats(AddressAndStats addressAndStats, boolean applyReportPort) {
        getAddress().setHostname(addressAndStats.getAddress().getHostname());
        getAddress().setIp(addressAndStats.getAddress().getIp());
        getAddress().setPort(addressAndStats.getAddress().getPort());
        if (applyReportPort) {
            getAddress().setReportPort(addressAndStats.getAddress().getReportPort());
        }
        if (addressAndStats.getTimestamp() != null) {
            setTimestamp(addressAndStats.getTimestamp());
        }
        if (addressAndStats.getAllTx() != null) {
            setAllTx(addressAndStats.getAllTx());
        }
        if (addressAndStats.getNewTx() != null) {
            setNewTx(addressAndStats.getNewTx());
        }
        if (addressAndStats.getIgnoredTx() != null) {
            setIgnoredTx(addressAndStats.getIgnoredTx());
        }
        if (addressAndStats.getInvalidTx() != null) {
            setInvalidTx(addressAndStats.getInvalidTx());
        }
        if (addressAndStats.getRequestedTx() != null) {
            setRequestedTx(addressAndStats.getRequestedTx());
        }
    }

    @Override
    public String toString() {
        return "Neighbor{" +
                "address=" + getAddress() +
                ", reportSocketAddress=" + getAddress().getReportSocketAddress() +
                ", ictSocketAddress=" + getAddress().getIctSocketAddress() +
                ", uuid='" + uuid + '\'' +
                ", reportIxiVersion='" + reportIxiVersion + '\'' +
                '}';
    }
}
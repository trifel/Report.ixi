package com.ictreport.ixi.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class Neighbor {

    private static final Logger log = LogManager.getLogger("ReportIxi/Neighbor");
    private String uuid = null;
    private String reportIxiVersion = null;
    private AddressAndStats addressAndStats;

    public Neighbor(final Address address) {
        addressAndStats = new AddressAndStats(address);
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

    public AddressAndStats getAddressAndStats() {
        return addressAndStats;
    }

    public void setAddressAndStats(AddressAndStats addressAndStats) {
        this.addressAndStats = addressAndStats;
    }

    public Address getAddress() {
        return getAddressAndStats().getAddress();
    }

    public Stats getStats() {
        return getAddressAndStats().getStats();
    }

    public boolean isNeighborReportAddress(Address address, boolean requirePortMatch) {
        if (getAddress().getHostname().equals(address.getHostname()) &&
            getAddress().getIp().equals(address.getIp())) {
            if (requirePortMatch && getAddress().getReportPort() != address.getPort()) {
                // Different port
                return false;
            }
            // Found a direct match
            return true;
        } else if (getAddress().getHostname().equals(address.getHostname()) &&
                !getAddress().getIp().equals(address.getIp())) {
            if (requirePortMatch && getAddress().getReportPort() != address.getPort()) {
                // Different port
                return false;
            }
            // Hostname and port is equal, but ip is different.
            return true;
        } else if (!getAddress().getHostname().equals(address.getHostname()) &&
                getAddress().getIp().equals(address.getIp())) {
            if (requirePortMatch && getAddress().getReportPort() != address.getPort()) {
                // Different port
                return false;
            }
            // Ip and port is equal, but hostname is different.
            return true;
        }
        return false;
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
        if (addressAndStats.getStats().getTimestamp() != null) {
            getStats().setTimestamp(addressAndStats.getStats().getTimestamp());
        }
        if (addressAndStats.getStats().getAllTx() != null) {
            getStats().setAllTx(addressAndStats.getStats().getAllTx());
        }
        if (addressAndStats.getStats().getNewTx() != null) {
            getStats().setNewTx(addressAndStats.getStats().getNewTx());
        }
        if (addressAndStats.getStats().getIgnoredTx() != null) {
            getStats().setIgnoredTx(addressAndStats.getStats().getIgnoredTx());
        }
        if (addressAndStats.getStats().getInvalidTx() != null) {
            getStats().setInvalidTx(addressAndStats.getStats().getInvalidTx());
        }
        if (addressAndStats.getStats().getRequestedTx() != null) {
            getStats().setRequestedTx(addressAndStats.getStats().getRequestedTx());
        }

        log.debug(String.format("Synced: %s", toString()));
    }

    @Override
    public String toString() {
        return "Neighbor{" +
                "uuid='" + uuid + '\'' +
                ", reportIxiVersion='" + reportIxiVersion + '\'' +
                ", addressAndStats=" + addressAndStats +
                '}';
    }

    public static boolean isNeighborWhoSent(Neighbor neighbor, DatagramPacket packet, boolean requirePortMatch) {
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
        final Address address = Address.parse(inetSocketAddress.toString());
        return neighbor.isNeighborReportAddress(address, requirePortMatch);
    }
}
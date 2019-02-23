package com.ictreport.ixi.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SyncedNeighbors {

    private final static Logger LOGGER = LogManager.getLogger(SyncedNeighbors.class);
    private final List<Address> neighbors = new LinkedList<>();

    public void syncFromIctRest(final String ictRestPassword) {
        final List<Address> addressesToSync = new LinkedList<>();

        final JSONArray ictNeighbors = IctRestCaller.getNeighbors(ictRestPassword);
        for (int i=0; ictNeighbors != null && i<ictNeighbors.length(); i++) {
            final JSONObject ictNeighbor = (JSONObject)ictNeighbors.get(i);
            final String ictNeighborAddress = ictNeighbor.getString("address");
            try {
                final Address address = parseAddress(ictNeighborAddress);
                addressesToSync.add(address);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn(String.format(
                        "Failed to parse InetSocketAddress from [%s] received from Ict REST API.",
                        ictNeighborAddress
                ));
            }
        }

        syncAddresses(addressesToSync, false);
    }

    public void syncAddresses(List<Address> addresses, boolean applyReportPort) {

        final List<Address> keepNeighbors = new LinkedList<>();

        for (Address address : addresses) {
            final Address foundNeighbor = syncAddress(address, applyReportPort);

            if (foundNeighbor != null) {
                keepNeighbors.add(foundNeighbor);
            } else {
                keepNeighbors.add(address);
            }
        }

        neighbors.removeAll(keepNeighbors);
        for (Address discardedNeighbor : neighbors) {
            LOGGER.warn(String.format("Neighbor [%s] doesn't exist in Ict neighbors list." +
                    "It's therefore discarded from Report.ixi configuration", discardedNeighbor));
        }

        neighbors.clear();
        neighbors.addAll(keepNeighbors);
    }

    private Address syncAddress(Address address, boolean applyReportPort) {
        Address foundNeighbor = null;
        for (Address neighborAddress : neighbors) {
            if (neighborAddress.equals(address)) {
                // Found a direct match
                if (applyReportPort) {
                    neighborAddress.reportPort = address.reportPort;
                }
                foundNeighbor = neighborAddress;
                break;
            } else if (neighborAddress.hostname.equals(address.hostname) &&
                    !neighborAddress.ip.equals(address.ip) &&
                    neighborAddress.port == address.port) {
                // Hostname and port is equal, but ip is different.
                neighborAddress.ip = address.ip;
                if (applyReportPort) {
                    neighborAddress.reportPort = address.reportPort;
                }
                foundNeighbor = neighborAddress;
                break;
            } else if (!neighborAddress.hostname.equals(address.hostname) &&
                    neighborAddress.ip.equals(address.ip) &&
                    neighborAddress.port == address.port) {
                // Ip and port is equal, but hostname is different.
                neighborAddress.hostname = address.hostname;
                if (applyReportPort) {
                    neighborAddress.reportPort = address.reportPort;
                }
                foundNeighbor = neighborAddress;
                break;
            }
        }

        return foundNeighbor;
    }

    public List<Address> getNeighbors() {
        return neighbors;
    }

    public Address parseAddress(String address) {
        final int slash = address.lastIndexOf("/");
        final int colon = address.lastIndexOf(":");

        final String hostname = address.substring(0, slash);
        final String ip = address.substring(slash+1, colon);
        final int port = Integer.parseInt(address.substring(colon+1));

        return new Address(hostname, ip, port);
    }

    public class Address {
        private String hostname;
        private String ip;
        private int port;
        private int reportPort;

        public Address(String hostname, String ip, int port) {
            this.hostname = hostname;
            this.ip = ip;
            this.port = port;
            this.reportPort = 1338;
        }

        public Address(String hostname, String ip, int port, int reportPort) {
            this.hostname = hostname;
            this.ip = ip;
            this.port = port;
            this.reportPort = reportPort;
        }

        public String getHostname() {
            return hostname;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }

        public int getReportPort() {
            return reportPort;
        }

        public void setReportPort(int reportPort) {
            this.reportPort = reportPort;
        }

        public InetSocketAddress asInetSocketAddress() {
            if (!hostname.isEmpty()) {
                return new InetSocketAddress(hostname, port);
            } else if (!ip.isEmpty()) {
                return new InetSocketAddress(ip, port);
            }
            return null;
        }

        public InetSocketAddress asReportInetSocketAddress() {
            if (!hostname.isEmpty()) {
                return new InetSocketAddress(hostname, reportPort);
            } else if (!ip.isEmpty()) {
                return new InetSocketAddress(ip, reportPort);
            }
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Address address = (Address) o;
            return port == address.port &&
                    Objects.equals(hostname, address.hostname) &&
                    Objects.equals(ip, address.ip);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hostname, ip, port);
        }

        @Override
        public String toString() {
            return String.format(
                    "Address{hostname='%s', ip='%s', port=%d, reportPort=%d}",
                    hostname,
                    ip,
                    port,
                    reportPort
            );
        }
    }
}

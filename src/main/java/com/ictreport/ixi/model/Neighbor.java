package com.ictreport.ixi.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class Neighbor {

    private static final Logger log = LogManager.getLogger("ReportIxi/Neighbor");
    private String uuid = null;
    private String reportIxiVersion = null;
    private String address;
    private InetSocketAddress reportSocketAddress;
    private Stats stats = new Stats();
    private int reportPort = 1338;

    public Neighbor(final String address) {
        this.address = address;
        final String host = address.split(":")[0];
        this.reportSocketAddress = new InetSocketAddress(host, this.reportPort);
    }

    public void resolveHost() {
        try {
            if (!reportSocketAddress.getAddress().equals(InetAddress.getByName(reportSocketAddress.getHostName())))
                reportSocketAddress = new InetSocketAddress(reportSocketAddress.getHostName(), reportSocketAddress.getPort());
        } catch (UnknownHostException e) {
            log.warn(("Unknown Host for: " + reportSocketAddress.getHostString()) + " (" + e.getMessage() + ")");
        }
    }

    public boolean sentPacket(DatagramPacket packet) {
        if (getReportSocketAddress() == null) {
            return false;
        }

        boolean sameIP = sentPacketFromSameIP(packet);
        boolean samePort = getReportSocketAddress().getPort() == packet.getPort();
        return sameIP && samePort;
    }

    public boolean sentPacketFromSameIP(DatagramPacket packet) {
        try {
            return getReportSocketAddress().getAddress().getHostAddress().equals(packet.getAddress().getHostAddress());
        } catch (NullPointerException e) {
            // cannot resolve ip
            return false;
        }
    }

    public InetSocketAddress getReportSocketAddress() {
        return reportSocketAddress;
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

    public String getAddress() {
        return address;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public int getReportPort() {
        return reportPort;
    }

    public void setReportPort(int reportPort) {
        this.reportPort = reportPort;
    }
}
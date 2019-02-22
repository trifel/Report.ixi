package com.ictreport.ixi.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class Neighbor {

    private static final Logger LOGGER = LogManager.getLogger(Neighbor.class);
    private InetSocketAddress reportSocketAddress;
    private InetSocketAddress ictSocketAddress;
    private String uuid = null;
    private String reportIxiVersion = null;

    public Neighbor(final InetSocketAddress socketAddress, final InetSocketAddress ictSocketAddress) {
        this.reportSocketAddress = socketAddress;
        this.ictSocketAddress = ictSocketAddress;
    }

    public boolean sentPacket(final DatagramPacket packet) {
        boolean sameIP = sentPacketFromSameIP(packet);
        boolean samePort = reportSocketAddress.getPort() == packet.getPort();
        return sameIP && samePort;
    }

    public boolean sentPacketFromSameIP(final DatagramPacket packet) {
        if (reportSocketAddress == null) {
            return false;
        }
        return reportSocketAddress.getAddress().getHostAddress().equals(packet.getAddress().getHostAddress());
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the reportSocketAddress
     */
    public InetSocketAddress getReportSocketAddress() {
        return reportSocketAddress;
    }

    /**
     * @param socketAddress the reportSocketAddress to set
     */
    public void setReportSocketAddress(final InetSocketAddress reportSocketAddress) {
        this.reportSocketAddress = reportSocketAddress;
    }

    /**
     * @return the ictSocketAddress
     */
    public InetSocketAddress getIctSocketAddress() {
        return ictSocketAddress;
    }

    /**
     * @param ictSocketAddress the ictSocketAddress to set
     */
    public void setIctSocketAddress(final InetSocketAddress ictSocketAddress) {
        this.ictSocketAddress = ictSocketAddress;
    }

    public String getReportIxiVersion() {
        return reportIxiVersion;
    }

    public void setReportIxiVersion(final String reportIxiVersion) {
        this.reportIxiVersion = reportIxiVersion;
    }

    public void resolveHost() {
        try {
            if (!reportSocketAddress.getAddress().equals(InetAddress.getByName(reportSocketAddress.getHostName()))) {
                reportSocketAddress = new InetSocketAddress(reportSocketAddress.getHostName(), reportSocketAddress.getPort());
            }
        } catch (UnknownHostException e) {
            LOGGER.warn("Failed to resolve host for: " + reportSocketAddress.getHostString());
        }
        try {
            if (!ictSocketAddress.getAddress().equals(InetAddress.getByName(ictSocketAddress.getHostName()))) {
                ictSocketAddress = new InetSocketAddress(ictSocketAddress.getHostName(), ictSocketAddress.getPort());
            }
        } catch (UnknownHostException e) {
        }
    }
}
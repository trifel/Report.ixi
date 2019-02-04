package com.ictreport.ixi.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class Neighbor {

    private static final Logger LOGGER = LogManager.getLogger(Neighbor.class);
    private InetSocketAddress socketAddress;
    private String uuid = null;
    private String reportIxiVersion = null;

    public Neighbor(final InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public boolean sentPacket(final DatagramPacket packet) {
        boolean sameIP = sentPacketFromSameIP(packet);
        boolean samePort = socketAddress.getPort() == packet.getPort();
        return sameIP && samePort;
    }

    public boolean sentPacketFromSameIP(final DatagramPacket packet) {
        if (socketAddress == null) {
            return false;
        }
        return socketAddress.getAddress().getHostAddress().equals(packet.getAddress().getHostAddress());
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
     * @return the socketAddress
     */
    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    /**
     * @param socketAddress the socketAddress to set
     */
    public void setSocketAddress(final InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public String getReportIxiVersion() {
        return reportIxiVersion;
    }

    public void setReportIxiVersion(final String reportIxiVersion) {
        this.reportIxiVersion = reportIxiVersion;
    }

    public void resolveHost() {
        try {
            if (!socketAddress.getAddress().equals(InetAddress.getByName(socketAddress.getHostName()))) {
                socketAddress = new InetSocketAddress(socketAddress.getHostName(), socketAddress.getPort());
            }
        } catch (UnknownHostException e) {
            LOGGER.warn("Failed to resolve host for: " + socketAddress.getHostString());
        }
    }
}
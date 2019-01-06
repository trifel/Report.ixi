package com.ictreport.ixi.model;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.security.PublicKey;

public class Neighbor {

    private InetSocketAddress socketAddress;
    private String uuid = null;
    private PublicKey publicKey = null;
    private String reportIxiVersion = null;
    private int pingCount = 0;
    private int metadataCount = 0;
    private int invalidCount = 0;

    public Neighbor(final InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public boolean sentPacket(final DatagramPacket packet) {
        boolean sameIP = sentPacketFromSameIP(packet);
        boolean samePort = socketAddress.getPort() == packet.getPort();
        return sameIP && samePort;
    }

    public boolean sentPacketFromSameIP(final DatagramPacket packet) {
        return socketAddress.getAddress().getHostAddress().equals(packet.getAddress().getHostAddress());
    }

    /**
     * @return the publicKey
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * @param publicKey the publicKey to set
     */
    public void setPublicKey(final PublicKey publicKey) {
        this.publicKey = publicKey;
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

    public void incrementPingCount() {
        pingCount++;
    }

    public int getPingCount() {
        return pingCount;
    }

    public void incrementMetadataCount() {
        metadataCount++;
    }

    public int getMetadataCount() {
        return metadataCount;
    }

    public void incrementInvalidCount() {
        invalidCount++;
    }

    public int getInvalidCount() {
        return invalidCount;
    }

    public void resetMetrics() {
        metadataCount = 0;
        pingCount = 0;
        invalidCount = 0;
    }
}
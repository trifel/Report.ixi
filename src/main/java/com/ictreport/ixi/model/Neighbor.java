package com.ictreport.ixi.model;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.PublicKey;

public class Neighbor {

    private InetSocketAddress address;
    private String uuid = null;
    private PublicKey publicKey = null;
    private String reportIxiVersion = null;

    public Neighbor(InetSocketAddress address) {
        this.address = address;
    }


    public boolean sentPacket(DatagramPacket packet) {
        boolean sameIP = sentPacketFromSameIP(packet);
        boolean samePort = address.getPort() == packet.getPort();
        return sameIP && samePort;
    }

    public boolean sentPacketFromSameIP(DatagramPacket packet) {
        return address.getAddress().getHostAddress().equals(packet.getAddress().getHostAddress());
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
    public void setPublicKey(PublicKey publicKey) {
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
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the address
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public String getReportIxiVersion() {
        return reportIxiVersion;
    }

    public void setReportIxiVersion(String reportIxiVersion) {
        this.reportIxiVersion = reportIxiVersion;
    }
}
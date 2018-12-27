package com.ictreport.ixi.model;

import java.net.InetAddress;
import java.security.PublicKey;

public class Neighbor {

    private InetAddress address;
    private int reportPort = -1;
    private String uuid = null;
    private PublicKey publicKey = null;

    public Neighbor(InetAddress address, int reportPort) {
        this.address = address;
        this.reportPort = reportPort;
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
     * @return the reportPort
     */
    public int getReportPort() {
        return reportPort;
    }

    /**
     * @param reportPort the reportPort to set
     */
    public void setReportPort(int reportPort) {
        this.reportPort = reportPort;
    }

    /**
     * @return the address
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(InetAddress address) {
        this.address = address;
    }

}
package com.ictreport.ixi.model;

import java.net.InetSocketAddress;
import java.util.Objects;

public class Address {
    private static final int DEFAULT_REPORT_PORT = 1338;
    private String hostname;
    private String ip;
    private int port;
    private int reportPort;

    public Address(String hostname, String ip, int port) {
        this(hostname, ip, port, DEFAULT_REPORT_PORT);
    }

    public Address(String hostname, String ip, int port, int reportPort) {
        this.hostname = hostname;
        this.ip = ip;
        this.port = port;
        this.reportPort = reportPort;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setPort(int port) {
        this.port = port;
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

    public InetSocketAddress getIctSocketAddress() {
        return asInetSocketAddress(hostname, ip, port);
    }

    public InetSocketAddress getReportSocketAddress() {
        return asInetSocketAddress(hostname, ip, reportPort);
    }

    public InetSocketAddress asInetSocketAddress(String hostname, String ip, int port) {
        if (!hostname.isEmpty()) {
            return new InetSocketAddress(hostname, port);
        } else if (!ip.isEmpty()) {
            return new InetSocketAddress(ip, port);
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
        return "Address{" +
                "hostname='" + hostname + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", reportPort=" + reportPort +
                '}';
    }

    public static Address parse(String inetSocketAddress) {
        final int slash = inetSocketAddress.lastIndexOf("/");
        final int colon = inetSocketAddress.lastIndexOf(":");

        final String hostname = inetSocketAddress.substring(0, slash);
        final String ip = inetSocketAddress.substring(slash+1, colon);
        final int port = Integer.parseInt(inetSocketAddress.substring(colon+1));

        return new Address(hostname, ip, port);
    }
}

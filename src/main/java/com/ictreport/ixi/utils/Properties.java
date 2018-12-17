package com.ictreport.ixi.utils;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

public class Properties {

    private String name = "";
    private String uuid = null;
    private String host = "localhost";
    private int port = 1337;
    private List<InetSocketAddress> neighbors = new LinkedList<>();

    public Properties() {

    }

    /**
     * @return the neighbors
     */
    public List<InetSocketAddress> getNeighbors() {
        return neighbors;
    }

    /**
     * @param neighbors the neighbors to set
     */
    public void setNeighbors(List<InetSocketAddress> neighbors) {
        this.neighbors = neighbors;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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

	public void loadFromIctProperties(org.iota.ict.utils.Properties ictProperties) {
        setHost(ictProperties.host);
        setPort(ictProperties.port);
        setNeighbors(ictProperties.neighbors);
	}   

}
package com.ictreport.ixi.api;

import com.ictreport.ixi.utils.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class Api {
    private static final Logger log = LoggerFactory.getLogger(Api.class);

    private Properties properties;
    private final InetSocketAddress address;
    private final DatagramSocket socket;
    private final Receiver receiver;

    public Api(Properties properties) {
        this.properties = properties;
        this.address = new InetSocketAddress(properties.getHost(), properties.getReportPort());

        try {
            this.socket = new DatagramSocket(this.address.getPort());
        } catch (SocketException socketException) {
            throw new RuntimeException(socketException);
        }

        this.receiver = new Receiver(socket);
    }

    public void init() {
        receiver.start();
	}

	public void shutDown() {
        receiver.shutDown();
	}

    public Receiver getReceiver() {
        return receiver;
    }

    public InetSocketAddress getAddress() {
        return address;
    }
}
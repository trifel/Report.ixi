package com.ictreport.ixi.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.ReportIxi;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class Api {

    private final static Logger LOGGER = LogManager.getLogger(Api.class);
    private final InetSocketAddress address;
    private final DatagramSocket socket;
    private final Receiver receiver;
    private final Sender sender;

    public Api(final ReportIxi reportIxi) {
        this.address = new InetSocketAddress(reportIxi.getReportIxiContext().getHost(), reportIxi.getReportIxiContext().getReportPort());

        try {
            this.socket = new DatagramSocket(this.address.getPort());
        } catch (SocketException socketException) {
            throw new RuntimeException(socketException);
        }

        this.receiver = new Receiver(reportIxi, socket);
        this.sender = new Sender(reportIxi, socket);
    }

    public void init() {
        receiver.start();
        sender.start();
	}

	public void shutDown() {
        receiver.shutDown();
        sender.shutDown();
	}

    public Receiver getReceiver() {
        return receiver;
    }

    public Sender getSender() {
        return sender;
    }

    public InetSocketAddress getAddress() {
        return address;
    }
}
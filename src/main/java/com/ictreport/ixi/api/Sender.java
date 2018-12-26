package com.ictreport.ixi.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import com.ictreport.ixi.ReportIxi;
import com.ictreport.ixi.model.Neighbor;

public class Sender {
    private final ReportIxi reportIxi;
    private final DatagramSocket socket;
    private static final Logger LOGGER = LogManager.getLogger();
    private Timer uuidSenderTimer = new Timer();

    public Sender(final ReportIxi reportIxi, DatagramSocket socket) {
        this.reportIxi = reportIxi;
        this.socket = socket;
    }

    public void start() {
        uuidSenderTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                for (final Neighbor neighbor : reportIxi.getNeighbors()) {
                    try {
                        LOGGER.info("sending uuid");

                        byte[] messageByteArray = new String("uuid:" + reportIxi.getProperties().getUuid()).getBytes();
                        DatagramPacket packet = new DatagramPacket(messageByteArray, messageByteArray.length);
                        packet.setSocketAddress(new InetSocketAddress(neighbor.getAddress(), neighbor.getReportPort()));
                        socket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 60000);
    }

    public void shutDown() {
        if (uuidSenderTimer != null) {
            uuidSenderTimer.cancel();
            uuidSenderTimer.purge();
        }
    }

}

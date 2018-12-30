package com.ictreport.ixi.utils;

import com.ictreport.ixi.api.Api;
import com.ictreport.ixi.api.Receiver;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ApiTest {

/*    @Test
    public void testReceivePacket() {

        final Object waitObject = new Object();
        final boolean[] isPacketReceived = {false};

        ReportIxi reportIxi = new ReportIxi();
        Api api = new Api(reportIxi);
        api.init();

        api.getReceiver().addOnIncomingPacketListener(new Receiver.IIncomingPacketListener() {
            @Override
            public void OnIncomingPacket(DatagramPacket packet) {
                synchronized(waitObject) {
                    waitObject.notify();
                    isPacketReceived[0] = true;
                }
            }
        });

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        byte[] payload = "API test payload".getBytes();
        DatagramPacket packet = new DatagramPacket(payload, payload.length,
                api.getAddress().getAddress(), api.getAddress().getPort());

        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Failed to send packet");
        }

        System.out.println("Waiting for packet...");
        synchronized(waitObject) {
            try {
                waitObject.wait(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!isPacketReceived[0]) {
                Assert.fail("No packet received");
            }
        }

        api.shutDown();
    }*/
}

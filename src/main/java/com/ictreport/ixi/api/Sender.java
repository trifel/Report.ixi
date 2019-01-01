package com.ictreport.ixi.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.model.TransactionBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ictreport.ixi.ReportIxi;
import com.ictreport.ixi.model.Neighbor;
import com.ictreport.ixi.utils.RandomStringGenerator;

public class Sender {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ReportIxi reportIxi;
    private final DatagramSocket socket;
    private Timer uuidSenderTimer = new Timer();
    private Timer reportTimer = new Timer();
    private Timer submitRandomTransactionTimer = new Timer();
    private RandomStringGenerator randomStringGenerator = new RandomStringGenerator();

    private final static String reportServerHost = "api.ictreport.com";
    private final static int reportServerPort = 14265;

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
                        byte[] messageByteArray = new String("uuid:" + reportIxi.getProperties().getUuid()).getBytes();
                        DatagramPacket packet = new DatagramPacket(messageByteArray, messageByteArray.length);
                        packet.setSocketAddress(new InetSocketAddress(neighbor.getAddress(), neighbor.getReportPort()));
                        socket.send(packet);
                    } catch (IOException | RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 60000);

        reportTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Gson gson = new Gson();
            
                    JsonObject nodeInfo = new JsonObject();
                    nodeInfo.addProperty("uuid", reportIxi.getProperties().getUuid());
                    nodeInfo.addProperty("name", reportIxi.getProperties().getName());
                    nodeInfo.addProperty("version", ReportIxi.VERSION);
                    
                    JsonArray neighborJSONArray = new JsonArray();
                    for (final Neighbor neighbor : reportIxi.getNeighbors()) {
                        JsonObject neighborInfo = new JsonObject();
                        neighborInfo.addProperty("uuid", neighbor.getUuid() != null ? neighbor.getUuid() : "");
                        
                        neighborJSONArray.add(neighborInfo);
                    }
                    nodeInfo.add("neighbors", neighborJSONArray);

                    JsonObject action = new JsonObject();
                    action.addProperty("action", "setNodeStatus");
                    action.add("value", nodeInfo);

                    byte[] messageByteArray = gson.toJson(action).getBytes();
                    DatagramPacket packet = new DatagramPacket(messageByteArray, messageByteArray.length);
                    InetSocketAddress reportServerAddress = new InetSocketAddress(reportServerHost, reportServerPort);
                    packet.setSocketAddress(reportServerAddress);
                    socket.send(packet);

                } catch (IOException | RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 60000);

        submitRandomTransactionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    String randomString = randomStringGenerator.nextString();
                    
                    TransactionBuilder t = new TransactionBuilder();
                    t.tag = "REPORT9IXI99999999999999999";
                    t.asciiMessage(randomString);
                    reportIxi.submit(t.build());

                    Gson gson = new Gson();

                    JsonObject transaction = new JsonObject();
                    transaction.addProperty("uuid", reportIxi.getProperties().getUuid());
                    transaction.addProperty("message", randomString);

                    JsonObject action = new JsonObject();
                    action.addProperty("action", "onTransactionSubmitted");
                    action.add("value", transaction);

                    byte[] messageByteArray = gson.toJson(action).getBytes();
                    DatagramPacket packet = new DatagramPacket(messageByteArray, messageByteArray.length);
                    InetSocketAddress reportServerAddress = new InetSocketAddress(reportServerHost, reportServerPort);
                    packet.setSocketAddress(reportServerAddress);
                    socket.send(packet);
                } catch (IOException | RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 60000);
    }

    public void reportTransactionReceived(String message) {
        try {
            Gson gson = new Gson();

            JsonObject transaction = new JsonObject();
            transaction.addProperty("uuid", reportIxi.getProperties().getUuid());
            transaction.addProperty("message", message);
    
            JsonObject action = new JsonObject();
            action.addProperty("action", "onTransactionReceived");
            action.add("value", transaction);
    
            byte[] messageByteArray = gson.toJson(action).getBytes();
            DatagramPacket packet = new DatagramPacket(messageByteArray, messageByteArray.length);
            InetSocketAddress reportServerAddress = new InetSocketAddress(reportServerHost, reportServerPort);
            packet.setSocketAddress(reportServerAddress);
            socket.send(packet);
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void shutDown() {
        if (uuidSenderTimer != null) {
            uuidSenderTimer.cancel();
            uuidSenderTimer.purge();
        }
        if (reportTimer != null) {
            reportTimer.cancel();
            reportTimer.purge();
        }
        if (submitRandomTransactionTimer != null) {
            submitRandomTransactionTimer.cancel();
            submitRandomTransactionTimer.purge();
        }
    }

}

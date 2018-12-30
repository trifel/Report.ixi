package com.ictreport.ixi.api;

import com.ictreport.ixi.exchange.SignedPayload;
import com.ictreport.ixi.exchange.UuidPayload;
import com.ictreport.ixi.utils.Cryptography;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.model.TransactionBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
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
    private Timer submitRandomTransactionTimerTwo = new Timer();
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
                        Gson gson = new Gson();

                        JsonObject metadata = new JsonObject();
                        metadata.addProperty("uuid", reportIxi.getProperties().getUuid());

                        byte[] encodedPublicKey = reportIxi.getKeyPair().getPublic().getEncoded();
                        String encodedPublicKeyBase64 = Base64.encodeBase64String(encodedPublicKey);
                        metadata.addProperty("publicKey", encodedPublicKeyBase64);

                        byte[] messageByteArray = gson.toJson(metadata).getBytes();
                        DatagramPacket packet = new DatagramPacket(messageByteArray, messageByteArray.length);
                        packet.setSocketAddress(new InetSocketAddress(neighbor.getAddress(), neighbor.getReportPort()));
                        socket.send(packet);
                    } catch (IOException e) {
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

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 60000);

        submitRandomTransactionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Gson gson = new Gson();

                    // Prepare signed payload
                    UuidPayload uuidPayload = new UuidPayload(reportIxi.getProperties().getUuid());
                    SignedPayload signedPayload = new SignedPayload(uuidPayload, reportIxi.getKeyPair().getPrivate());

                    // Prepare signed payload
                    JsonObject payload = new JsonObject();
                    payload.addProperty("uuid", reportIxi.getProperties().getUuid());

                    byte[] encodedData = gson.toJson(payload).getBytes(StandardCharsets.UTF_8);
                    final byte[] signature = Cryptography.sign(encodedData, reportIxi.getKeyPair().getPrivate());
                    String signatureBase64 = Base64.encodeBase64String(signature);

                    JsonObject transaction = new JsonObject();
                    transaction.add("payload", payload);
                    transaction.addProperty("payloadSignature", signatureBase64);

                    JsonObject action = new JsonObject();
                    action.addProperty("action", "onTransactionSubmitted");
                    action.add("value", transaction);

                    // Broadcast to neighbors
                    TransactionBuilder t = new TransactionBuilder();
                    t.tag = "REPORT9IXI99999999999999999";
                    t.asciiMessage(gson.toJson(transaction));
                    reportIxi.submit(t.build());

                    // Send to RCS
                    byte[] messageByteArray = gson.toJson(action).getBytes();
                    DatagramPacket packet = new DatagramPacket(messageByteArray, messageByteArray.length);
                    InetSocketAddress reportServerAddress = new InetSocketAddress(reportServerHost, reportServerPort);
                    packet.setSocketAddress(reportServerAddress);
                    socket.send(packet);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 60000);

        submitRandomTransactionTimerTwo.schedule(new TimerTask() {
            @Override
            public void run() {
                // Broadcast to neighbors
                TransactionBuilder t = new TransactionBuilder();
                t.tag = "REPORT9IXI99999999999999999";
                t.asciiMessage("Hello world!");
                reportIxi.submit(t.build());

            }
        }, 0, 20000);
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
        } catch (IOException e) {
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

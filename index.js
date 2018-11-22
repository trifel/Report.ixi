var Runnable = Java.type("java.lang.Runnable");
var Timer = Java.type('java.util.Timer');
var InetAddress = Java.type('java.net.InetAddress');
var InetSocketAddress = Java.type('java.net.InetSocketAddress');
var Properties = Java.type('java.util.Properties');
var FileInputStream = Java.type('java.io.FileInputStream');
var FileOutputStream = Java.type('java.io.FileOutputStream');
var OutputStreamWriter = Java.type('java.io.OutputStreamWriter');
var DatagramPacket = Java.type('java.net.DatagramPacket');
var DatagramSocket = Java.type('java.net.DatagramSocket');
var UUID = Java.type('java.util.UUID');

var PROPERTY_FILE = "report.properties";

var reportTimer;
var reportServerHost = "0.0.0.0";
var reportServerPort = "14245";

var nodeName = "";
var nodeUUID = "";
var nodeSocketAddress = "0.0.0.0";
var nodeSocketPort = "14244";
var nodeSocket = null;

function init() {
  try {
    loadProperties();
    initReportTimer();
  } catch (exception) {
    print(exception);
  }
}

function loadProperties() {
  var properties = new Properties();
  var propertiesInputStream = new FileInputStream(PROPERTY_FILE);
  
  properties.load(propertiesInputStream);

  nodeName = properties.getProperty("nodeName", nodeName).trim();
  reportServerHost = properties.getProperty("reportServerHost", reportServerHost).trim();
  reportServerPort = properties.getProperty("reportServerPort", reportServerPort).trim();
  nodeUUID = properties.getProperty("nodeUUID", null);

  if (nodeUUID !== null) {
    nodeUUID = nodeUUID.trim();
  } else {
    nodeUUID = UUID.randomUUID().toString();
    properties.put("nodeUUID", nodeUUID);

    try {
      var outputStreamWriter = new OutputStreamWriter(new FileOutputStream(PROPERTY_FILE, true));
      var data = "\nnodeUUID = " + nodeUUID;
      outputStreamWriter.append(data);
      outputStreamWriter.flush();
      outputStreamWriter.close();
    } catch (e) {
      print(e);
    }

  }
}

function initReportTimer() {
  reportTimer = new Timer();
  reportTimer.schedule(function () {

    for (var i in NODE.neighbors) {
      var neighbor = NODE.neighbors[i];
      print(neighbor);
    }

    //TODO send node status to report server
    var message = nodeUUID;
    var messageByteArray = message.getBytes();

    var packet = new DatagramPacket(messageByteArray, messageByteArray.length);
    nodeSocket = new DatagramSocket(nodeSocketPort, InetAddress.getByName(nodeSocketAddress));
    
    var reportServerAddress = new InetSocketAddress(reportServerHost, reportServerPort);
    
    packet.setSocketAddress(reportServerAddress);
    nodeSocket.send(packet);

    try {
      if (!nodeSocket.isClosed()) nodeSocket.close();
    } catch(exception) {
      print(exception);
    }

  }, 0, 60000);
}

init();

IXICycle.put("shutdown", new Runnable(function () {
  if (nodeSocket != undefined) {
    try {
      if (!nodeSocket.isClosed()) nodeSocket.close();
    } catch(exception) {
      print(exception);
    }
  }
  if (reportTimer != undefined) {
    reportTimer.cancel();
    reportTimer.purge();
  }
}));
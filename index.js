var Runnable = Java.type("java.lang.Runnable");
var Timer = Java.type('java.util.Timer');
var InetAddress = Java.type('java.net.InetAddress');
var Properties = Java.type('java.util.Properties');
var FileInputStream = Java.type('java.io.FileInputStream');
//var FileOutputStream = Java.type('java.io.FileOutputStream');
var DatagramPacket = Java.type('java.net.DatagramPacket');
var DatagramSocket = Java.type('java.net.DatagramSocket');
var UUID = Java.type('java.util.UUID');

var PROPERTY_FILE = "ixi/Report.ixi/report.properties";

var reportTimer;
var reportServerHost = "0.0.0.0";
var reportServerPort = "14245";

var nodeName = "";
var nodeUUID = "";

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
  //var propertiesOutputStream = new FileOutputStream(PROPERTY_FILE);

  properties.load(propertiesInputStream);

  nodeName = properties.getProperty("nodeName", nodeName).trim();
  reportServerHost = properties.getProperty("reportServerHost", reportServerHost).trim();
  reportServerPort = properties.getProperty("reportServerPort", reportServerPort).trim();
  nodeUUID = properties.getProperty("nodeUUID", null);

  if (nodeUUID !== null) {
    nodeUUID = nodeUUID.trim();
  } else {
    nodeUUID = UUID.randomUUID();
    properties.put("nodeUUID", nodeUUID);
    //properties.save(propertiesOutputStream);
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

  }, 0, 60000);
}

init();

IXICycle.put("shutdown", new Runnable(function () {
  if (reportTimer != undefined) {
    reportTimer.cancel();
    reportTimer.purge();
  }
}));
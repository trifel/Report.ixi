var Runnable = Java.type("java.lang.Runnable");
var Timer = Java.type('java.util.Timer');
var InetAddress = Java.type('java.net.InetAddress');
var DataOutputStream = Java.type('java.io.DataOutputStream');
var InputStreamReader = Java.type('java.io.InputStreamReader');
var BufferedReader = Java.type('java.io.BufferedReader');
var BufferedWriter = Java.type('java.io.BufferedWriter');
var InetSocketAddress = Java.type('java.net.InetSocketAddress');
var Socket = Java.type('java.net.Socket');
var String = Java.type('java.lang.String');
var Properties = Java.type('java.util.Properties');
var FileInputStream = Java.type('java.io.FileInputStream');
var FileOutputStream = Java.type('java.io.FileOutputStream');
var OutputStreamWriter = Java.type('java.io.OutputStreamWriter');
var DatagramPacket = Java.type('java.net.DatagramPacket');
var DatagramSocket = Java.type('java.net.DatagramSocket');
var UUID = Java.type('java.util.UUID');
var Gson = Java.type('com.google.gson.Gson');
var JsonObject = Java.type('com.google.gson.JsonObject');
var JsonArray = Java.type('com.google.gson.JsonArray');
var HashMap = Java.type('java.util.HashMap');

var Callable = Java.type('cfb.ict.service.CallableRequest');
var Response = Java.type('cfb.ict.service.dto.IXIResponse');

var PROPERTY_FILE = "ixi/Report.ixi/report.properties";
var METADATA_FILE = "report.ixi.metadata";

var reportTimer;
var fetchMetadataTimer;
var reportServerHost = "api.ictreport.com";
var reportServerPort = "14265";

var nodeName = "";
var nodeUUID = null;
var nodeSocketAddress = "0.0.0.0";
var nodeSocketPort = "14200";
var nodeSocket = null;

var nodeUUIDMap = new HashMap();

function init() {
  try {
    loadMetadata();
    loadProperties();
    initFetchMetadataTimer();
    initReportTimer();
  } catch (exception) {
    print(exception);
  }
}

function loadMetadata() {
  try {
    var metaData = new Properties();
    var metaDataInputStream = new FileInputStream(METADATA_FILE);
    metaData.load(metaDataInputStream);
    nodeUUID = metaData.getProperty("uuid", nodeUUID);
  } catch (exception) {}

  if (nodeUUID !== null) {
    nodeUUID = nodeUUID.trim();
  } else {
    nodeUUID = UUID.randomUUID().toString();
    try {
      var metaDataOutputStream = new FileOutputStream(METADATA_FILE);
      metaData.put("uuid", nodeUUID);
      metaData.store(metaDataOutputStream, "Report.ixi");
      metaDataOutputStream.close();
    } catch (exception) {
      print(exception);
    }
  }
}

function loadProperties() {
  try {
    var properties = new Properties();
    var propertiesInputStream = new FileInputStream(PROPERTY_FILE);
    properties.load(propertiesInputStream);

    nodeName = properties.getProperty("nodeName", nodeName).trim();

    propertiesInputStream.close();
  } catch (exception) {
    print(exception);
  }
}

function initFetchMetadataTimer() {
  fetchMetadataTimer = new Timer();
  fetchMetadataTimer.schedule(function () {
    var neighbors = NODE.getNeighbors();
    for (var i in neighbors) {
      var neighborAddress = neighbors[i].getAddress();

      var neighborUUID = fetchMetadata(neighborAddress.getHostName(), neighborAddress.getPort());

      if (!neighborUUID.equals("")) nodeUUIDMap.put(neighborAddress.toString(), neighborUUID);
    }
    
  }, 2000, 300000);
}

function fetchMetadata(address, port) {
  var uuid = "";

  try {
    var socket = new Socket(address, port);
    var data = "{\"command\": \"Report.ixi.getMetadata\"}";
    var path = "/servlet";
    var wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
    wr.write("POST " + path + " HTTP/1.0\r\n");
    wr.write("Content-Length: " + data.length() + "\r\n");
    wr.write("X-IOTA-API-Version: 1.4.1\r\nContent-Type: application/json\r\n");
    wr.write("\r\n");

    wr.write(data);
    wr.flush();

    var rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    var line;
    var response = null;
    while ((line = rd.readLine()) != null) {
      if (line !== null) response = line;
    }
    wr.close();
    rd.close();

    var gson = new Gson();
    var dataArray = gson.fromJson(response, JsonObject.class);

    uuid = gson.fromJson(dataArray.get("ixi").get("uuid"), String.class);
  } catch (exception) {
    //print("ixi api of neighbor '" + address + ":" + port + "' is not available: " + exception);
  }
  return uuid;
}

function initReportTimer() {
  reportTimer = new Timer();
  reportTimer.schedule(function () {
    try {
      var neighbors = NODE.getNeighbors();

      var gson = new Gson();

      var nodeInfo = new JsonObject();
      nodeInfo.addProperty("uuid", nodeUUID);
      nodeInfo.addProperty("name", nodeName);
      nodeInfo.addProperty("version", VERSION);
      
      var neighborJSONArray = new JsonArray();
  
      var neighbor, neighborInfo, metrics;
      for (var i in neighbors) {
        neighbor = neighbors[i];
        neighborInfo = new JsonObject();
        metrics = new JsonObject();
        neighborInfo.addProperty("uuid", (nodeUUIDMap.containsKey(neighbor.getAddress().toString()) ? nodeUUIDMap.get(neighbor.getAddress().toString()) : ""));
        metrics.addProperty("numberOfNewTransactions", neighbor.getPrevNumberOfNewTransactions());
        metrics.addProperty("numberOfInvalidTransactions", neighbor.getPrevNumberOfInvalidTransactions());
        metrics.addProperty("numberOfAllTransactions", neighbor.getPrevNumberOfAllTransactions());
        neighborInfo.add("metrics", metrics);
       
        neighborJSONArray.add(neighborInfo);
      }
      nodeInfo.add("neighbors", neighborJSONArray);

      var messageByteArray = gson.toJson(nodeInfo).getBytes();

      var packet = new DatagramPacket(messageByteArray, messageByteArray.length);
      nodeSocket = new DatagramSocket(nodeSocketPort, InetAddress.getByName(nodeSocketAddress));

      var reportServerAddress = new InetSocketAddress(reportServerHost, reportServerPort);

      packet.setSocketAddress(reportServerAddress);
      nodeSocket.send(packet);

      if (!nodeSocket.isClosed()) nodeSocket.close();

    } catch (exception) {
      print(exception);
    }

  }, 10000, 60000);
}

function getMetadata() {
  return Response.create({
    uuid: nodeUUID
  });
}

init();

IXICycle.put("shutdown", new Runnable(function () {
  if (nodeSocket != undefined) {
    try {
      if (!nodeSocket.isClosed()) nodeSocket.close();
    } catch (exception) {
      print(exception);
    }
  }
  if (reportTimer != undefined) {
    reportTimer.cancel();
    reportTimer.purge();
  }
}));

API.put("getMetadata", new Callable({
  call: getMetadata
}))
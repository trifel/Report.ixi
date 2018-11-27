var Runnable = Java.type("java.lang.Runnable");
var Timer = Java.type('java.util.Timer');
var InetAddress = Java.type('java.net.InetAddress');
var NetworkInterface = Java.type('java.net.NetworkInterface');
var InetSocketAddress = Java.type('java.net.InetSocketAddress');
var MessageDigest = Java.type('java.security.MessageDigest');
var Inet4Address = Java.type('java.net.Inet4Address');
var StringBuffer = Java.type('java.lang.StringBuffer');
var Integer = Java.type('java.lang.Integer');
var StringEscapeUtils = Java.type('org.apache.commons.lang3.StringEscapeUtils');
var StandardCharsets = Java.type('java.nio.charset.StandardCharsets');
var Properties = Java.type('java.util.Properties');
var FileInputStream = Java.type('java.io.FileInputStream');
var FileOutputStream = Java.type('java.io.FileOutputStream');
var OutputStreamWriter = Java.type('java.io.OutputStreamWriter');
var DatagramPacket = Java.type('java.net.DatagramPacket');
var DatagramSocket = Java.type('java.net.DatagramSocket');
var UUID = Java.type('java.util.UUID');

var PROPERTY_FILE = "ixi/Report.ixi/report.properties";

var reportTimer;
var reportServerHost = "0.0.0.0";
var reportServerPort = "14245";

var nodeName = "";
var nodeUUID = "";
var nodeExternalPort = "14265";
var nodeSocketAddress = "0.0.0.0";
var nodeSocketPort = "14244";
var nodeSocket = null;
var nodeInternalAddress;

function init() {
  try {      
    loadNetworkEnvironment();
    loadProperties();
    initReportTimer();
  } catch (exception) {
    print(exception);
  }
}

function loadNetworkEnvironment() {
  var localHostInetAddress = getCurrentIp();
  
  nodeInternalAddress = localHostInetAddress.getHostAddress();
  
  var network = NetworkInterface.getByInetAddress(localHostInetAddress);
  nodeUUID = UUID.nameUUIDFromBytes( network.getHardwareAddress() )
}

function getCurrentIp() {
  try {
      var networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
          var ni = networkInterfaces.nextElement();
          var nias = ni.getInetAddresses();
          while(nias.hasMoreElements()) {
              var ia= nias.nextElement();
              if (!ia.isLinkLocalAddress() 
               && !ia.isLoopbackAddress()
               && ia instanceof Inet4Address) {
                  return ia;
              }
          }
      }
  } catch (e) {
      print("unable to get current IP " + e.getMessage(), e);
  }
  return null;
}

function loadProperties() {
  var properties = new Properties();
  var propertiesInputStream = new FileInputStream(PROPERTY_FILE);
  
  properties.load(propertiesInputStream);

  nodeName = properties.getProperty("nodeName", nodeName).trim();
  reportServerHost = properties.getProperty("reportServerHost", reportServerHost).trim();
  reportServerPort = properties.getProperty("reportServerPort", reportServerPort).trim();
  nodeExternalPort = properties.getProperty("nodeExternalPort", nodeExternalPort).trim();
}

function initReportTimer() {
  reportTimer = new Timer();
  reportTimer.schedule(function () {

    var nodeInfo = "[\"" 
        + nodeUUID + "\"," 
        + nodeExternalPort + ",\"" 
        + sha256(nodeInternalAddress) + "\",\"" 
        + StringEscapeUtils.unescapeHtml4(nodeName) + "\",\"" 
        + VERSION + "\",[";
    var neighborInfo;
    var neighbor;
    var neighbors = NODE.getNeighbors();
    for (var i in neighbors) {
      neighbor = neighbors[i];
      if (i != 0) {
        nodeInfo += ",";
      }
      nodeInfo += "[\"" 
          + sha256(neighbor.getHostAddress()) + "\"," 
          + neighbor.getPort() + "," 
          + neighbor.getNumberOfNewTransactions() + "," 
          + neighbor.getNumberOfAllTransactions() + "," 
          + neighbor.getNumberOfInvalidTransactions() + "]";
    }
    nodeInfo += "]]";
    
    var messageByteArray = nodeInfo.getBytes();

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

function sha256(base) {
  try{
      var digest = MessageDigest.getInstance("SHA-256");
      var hash = digest.digest(base.getBytes("UTF-8"));
      var hexString = new StringBuffer();

      for (var i = 0; i < hash.length; i++) {
          var hex = Integer.toHexString(0xff & hash[i]);
          if(hex.length() == 1) hexString.append('0');
          hexString.append(hex);
      }

      return hexString.toString();
  } catch(e){
     return "";
  }
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
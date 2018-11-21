var Runnable = Java.type("java.lang.Runnable");
var Timer = Java.type('java.util.Timer');
var reportTimer;

function initReportTimer() {
    reportTimer = new Timer();
    reportTimer.schedule(function() {

      print(VERSION);

      for (var i in NODE.neighbors) {
        var neighbor = NODE.neighbors[i];
        print(neighbor);
      } 
      
      //TODO report node status to external web service
      
    }, 0, 60000); 
  }
  initReportTimer();
  IXICycle.put("shutdown", new Runnable( function() {
    //TODO Testing: function is never called on shutdown event
    print("Close");
    if (reportTimer != undefined) {
          reportTimer.cancel();
          reportTimer.purge();
    }
  }));
package com.ictreport.ixi;

import org.iota.ict.ixi.IxiModule;
import org.iota.ict.network.event.GossipReceiveEvent;
import org.iota.ict.network.event.GossipSubmitEvent;

public class ReportIxi extends IxiModule {

    public static final String NAME = "Report.ixi";

    private String ictName = "";

    public static void main(String[] args) {
        new ReportIxi();
    }

    public ReportIxi() {
        super(NAME);
        System.out.println(NAME + " started, waiting for Ict to connect ...");
        System.out.println("Just add '"+NAME+"' to 'ixis' in your ict.cfg file and restart your Ict.\n");
        // important: do not call any API functions such as 'findTransactionByHash()' before onIctConnect() is called!
    }

    @Override
    public void onIctConnect(String name) {
        System.out.println("Ict '" + name + "' connected");
        this.ictName = name;

    }

    @Override
    public void onTransactionReceived(GossipReceiveEvent event) {
        
    }

    @Override
    public void onTransactionSubmitted(GossipSubmitEvent event) {
        
    }
}

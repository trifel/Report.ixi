package com.ictreport.ixi.exchange.processors;

import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.model.Neighbor;
import org.iota.ict.ixi.ReportIxi;

public interface IPayloadProcessor {

    boolean process(final ReportIxi reportIxi, final Neighbor neighbor, final Payload payload);
}

package com.ictreport.ixi.exchange.processors;

import com.ictreport.ixi.exchange.payloads.Payload;
import org.iota.ict.ixi.ReportIxi;

public interface IPayloadProcessor {

    boolean process(final ReportIxi reportIxi, final Payload payload);
}

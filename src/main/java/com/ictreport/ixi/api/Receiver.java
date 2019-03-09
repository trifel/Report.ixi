package com.ictreport.ixi.api;

import com.ictreport.ixi.exchange.payloads.Payload;
import com.ictreport.ixi.exchange.processors.IPayloadProcessor;
import com.ictreport.ixi.exchange.processors.MetadataPayloadProcessor;
import com.ictreport.ixi.exchange.processors.UuidPayloadProcessor;
import com.ictreport.ixi.model.Neighbor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iota.ict.ixi.ReportIxi;
import org.iota.ict.utils.RestartableThread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

public class Receiver extends RestartableThread {

    private static final Logger log = LogManager.getLogger("ReportIxi/Receiver");

    private final ReportIxi reportIxi;
    private final List<IPayloadProcessor> payloadProcessors = new ArrayList<>();

    public Receiver(final ReportIxi reportIxi) {
        super(log);
        this.reportIxi = reportIxi;

        payloadProcessors.add(new UuidPayloadProcessor());
        payloadProcessors.add(new MetadataPayloadProcessor());
    }

    @Override
    public void run() {
        while(isRunning()) {
            final byte[] buf = new byte[1024];
            final DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                reportIxi.getSocket().receive(packet);
                if (!processPacket(packet)) {
                    log.debug(String.format(
                            "Received packet [%s:%d] was not processed.",
                            packet.getAddress(),
                            packet.getPort()
                    ));
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }

    private boolean processPacket(final DatagramPacket packet) {
        final Neighbor neighbor = determineNeighborWhoSent(packet);
        final Payload payload = Payload.deserialize(packet);

        for (IPayloadProcessor payloadProcessor : payloadProcessors) {
            if (payloadProcessor.process(reportIxi, neighbor, payload)) {
                return true;
            }
        }

        return false;
    }

    private Neighbor determineNeighborWhoSent(DatagramPacket packet) {
        for (Neighbor nb : reportIxi.getReportIxiContext().getNeighbors())
            if (nb.sentPacket(packet))
                return nb;
        for (Neighbor nb : reportIxi.getReportIxiContext().getNeighbors())
            if (nb.sentPacketFromSameIP(packet))
                return nb;
        return null;
    }
}

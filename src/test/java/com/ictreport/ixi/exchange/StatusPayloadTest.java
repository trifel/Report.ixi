package com.ictreport.ixi.exchange;

import com.ictreport.ixi.utils.Constants;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusPayloadTest {

    @Test
    public void testPingPayload() {
        final List<NeighborPayload> neighbors = new ArrayList<>();
        neighbors.add(new NeighborPayload("abc", 10, 20, 30, 40, 50));
        neighbors.add(new NeighborPayload("def", 11, 21, 31, 41, 51));
        neighbors.add(new NeighborPayload("ghi", 12, 22, 32, 42, 52));

        final StatusPayload statusPayload = new StatusPayload("xyz", "ict (ict-1)",
                Constants.VERSION, neighbors);

        final String json = Payload.serialize(statusPayload);

        System.out.println(json);

        final Payload deserializedPayload = Payload.deserialize(json);

        if (deserializedPayload instanceof StatusPayload) {
            final StatusPayload deserializedStatusPayload = (StatusPayload) deserializedPayload;
            Assert.assertEquals("xyz", deserializedStatusPayload.getUuid());
            Assert.assertEquals("ict (ict-1)", deserializedStatusPayload.getName());
            Assert.assertEquals(Constants.VERSION, deserializedStatusPayload.getReportIxiVersion());
        } else {
            Assert.fail("Deserialization of polymorphism object failed.");
        }
    }
}

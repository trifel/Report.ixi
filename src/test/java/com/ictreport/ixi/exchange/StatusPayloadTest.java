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
        final List<String> neighborUuids = new ArrayList<>(Arrays.asList("def", "ghi", "jkl"));
        final StatusPayload statusPayload = new StatusPayload("abc", "ict (ict-1)",
                Constants.VERSION, neighborUuids);
        final String json = Payload.serialize(statusPayload);
        final Payload deserializedPayload = Payload.deserialize(json);

        if (deserializedPayload instanceof StatusPayload) {
            final StatusPayload deserializedStatusPayload = (StatusPayload) deserializedPayload;
            Assert.assertEquals(deserializedStatusPayload.getUuid(), statusPayload.getUuid());
            Assert.assertEquals(deserializedStatusPayload.getName(), statusPayload.getName());
            Assert.assertEquals(deserializedStatusPayload.getReportIxiVersion(), statusPayload.getReportIxiVersion());
            Assert.assertEquals(deserializedStatusPayload.getNeighborUuids(), statusPayload.getNeighborUuids());
        } else {
            Assert.fail("Deserialization of polymorphism object failed.");
        }
    }
}

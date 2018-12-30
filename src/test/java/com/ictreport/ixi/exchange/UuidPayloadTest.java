package com.ictreport.ixi.exchange;

import org.junit.Assert;
import org.junit.Test;

public class UuidPayloadTest {

    @Test
    public void testUuidPayloadToJson() {

        final String senderUuid = java.util.UUID.randomUUID().toString();

        UuidPayload uuidPayload = new UuidPayload(senderUuid);
        {
            final String json = uuidPayload.toJson();

            final IPayload payload = new UuidPayload().fromJson(json);

            if (payload instanceof UuidPayload) {
                UuidPayload aUuidPayload = (UuidPayload) payload;
                System.out.println("A uuid payload");
                Assert.assertEquals(aUuidPayload.getUuid(), senderUuid);
            } else {
                System.out.println("Unknown uuid payload");
                Assert.fail();
            }
        }
    }

    @Test
    public void testUuidPayloadToJsonBytes() {

        final String senderUuid = java.util.UUID.randomUUID().toString();

        UuidPayload uuidPayload = new UuidPayload(senderUuid);
        {
            final byte[] bytes = uuidPayload.toJsonBytes();

            final IPayload payload = new UuidPayload().fromJsonBytes(bytes);

            if (payload instanceof UuidPayload) {
                UuidPayload aUuidPayload = (UuidPayload) payload;
                System.out.println("A uuid payload");
                Assert.assertEquals(aUuidPayload.getUuid(), senderUuid);
            } else {
                System.out.println("Unknown uuid payload");
                Assert.fail();
            }
        }
    }

    @Test
    public void testUuidPayloadToJsonBytesBase64() {

        final String senderUuid = java.util.UUID.randomUUID().toString();

        UuidPayload uuidPayload = new UuidPayload(senderUuid);
        {
            final String base64 = uuidPayload.toJsonBytesBase64();

            final IPayload payload = new UuidPayload().fromJsonBytesBase64(base64);

            if (payload instanceof UuidPayload) {
                UuidPayload aUuidPayload = (UuidPayload) payload;
                System.out.println("A uuid payload");
                Assert.assertEquals(aUuidPayload.getUuid(), senderUuid);
            } else {
                System.out.println("Unknown uuid payload");
                Assert.fail();
            }
        }
    }
}

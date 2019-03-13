package com.ictreport.ixi.utils;

import org.junit.Assert;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

public class UuidGeneratorTest {

    @Test
    public void testUuidGenerator() throws NoSuchAlgorithmException {
        Assert.assertEquals(
                "BKDDDWMMWFDWGYVPEVDYVOKWNMVCQWKDDGDWZKDPKWLWWKYVY9DMDDORD9NW",
                UuidGenerator.generate("ict.public.address:1337")
        );
        Assert.assertEquals(
                "BCVSHVXCVJQDBQWRBDY9DGFDHIVYCVHZWRKWGFD99DGVDGIDS9DSMCTSWNBD",
                UuidGenerator.generate("ict-1.hosting.org:1337")
        );
        Assert.assertEquals(
                "RNXZKDZIWWIWAVDNBDJZWCYVLZWPRDPXWKNDZKDSNXDDWNNXOXWVHVPNXFYV",
                UuidGenerator.generate("ict-1.hosting.org:14265")
        );
    }
}

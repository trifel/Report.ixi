package com.ictreport.ixi.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.iota.ict.utils.Trytes;

public class UuidGenerator {

    public static String generate(final String publicAddress) {
        return Trytes.fromAscii(DigestUtils.sha1Hex(publicAddress));
    }
}

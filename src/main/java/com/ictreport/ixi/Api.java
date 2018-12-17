package com.ictreport.ixi;

import com.ictreport.ixi.utils.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Api {
    private static final Logger log = LoggerFactory.getLogger(Api.class);

    private Properties properties;

    public Api(Properties properties) {
        this.properties = properties;
    }

    public void init() {
	}

	public void shutDown() {
	}

}
package com.ictreport.ixi.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class IctRestCaller {

    private static final Logger log = LogManager.getLogger("ReportIxi/IctRestCaller");

    public static JSONObject getInfo(final int ictRestPort, final String ictRestPassword) {
        log.debug("Fetching data from Ict REST API endpoint: /getInfo");
        String json = call("getInfo", ictRestPort, ictRestPassword);
        if (json != null) {
            return new JSONObject(json);
        }
        return null;
    }

    public static JSONObject getConfig(final int ictRestPort, final String ictRestPassword) {
        log.debug("Fetching data from Ict REST API endpoint: /getConfig");
        String json = call("getConfig", ictRestPort, ictRestPassword);
        if (json != null) {
            return new JSONObject(json);
        }
        return null;
    }

    public static JSONArray getNeighbors(final int ictRestPort, final String ictRestPassword) {
        String json = call("getNeighbors", ictRestPort, ictRestPassword);
        if (json != null) {
            JSONObject rootObject =  new JSONObject(json);
            return rootObject.getJSONArray("neighbors");
        }
        return null;
    }

    private static String call(final String route, final int ictRestPort, final String ictRestPassword) {
        //try {
        final String endpoint = "http://localhost:" + ictRestPort + "/" + route;
        log.debug("Fetching Ict REST API (" + endpoint + ")...");

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(endpoint);

        try {
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("password", ictRestPassword));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to set request params, error: UnsupportedEncodingException");
        }

        //Execute and get the response.
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            log.error("Exception thrown when calling Ict REST API (" + endpoint + "), IOException");
        }

        if (response != null) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                log.error("Failed to fetch details from Ict REST API (" + endpoint + "), status code: " + statusCode);
                return null;
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream instream = entity.getContent()) {
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(instream, writer, "UTF-8");
                    final String json = writer.toString();
                    log.debug("Successfully fetched details from " + endpoint);
                    return json;
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("Failed to parse response from Ict REST API (" + endpoint + ").");
                }
            }
        }
        return null;
    }
}

package com.ictreport.ixi.utils;

import com.google.gson.JsonObject;
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
import java.util.ArrayList;
import java.util.List;

public class IctRestCaller {

    private final static Logger LOGGER = LogManager.getLogger(IctRestCaller.class);

    public static JSONObject getInfo(final int ictRestPort, final String ictRestPassword) {
        String json = call("getInfo", ictRestPort, ictRestPassword);
        if (json != null) {
            return new JSONObject(json);
        }
        return null;
    }

    public static JSONObject getConfig(final int ictRestPort, final String ictRestPassword) {

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
        try {
            final CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("http://localhost:" + ictRestPort + "/" + route);

            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("password", ictRestPassword));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            //Execute and get the response.
            final CloseableHttpResponse response = httpclient.execute(httppost);

            if (response.getStatusLine().getStatusCode() != 200) {
                LOGGER.error(String.format("Failed to call ict-rest api, status code: %d.",
                        response.getStatusLine().getStatusCode()));
                return null;
            }

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                try (InputStream instream = entity.getContent()) {

                    StringWriter writer = new StringWriter();
                    IOUtils.copy(instream, writer, "UTF-8");
                    final String json = writer.toString();
                    return json;
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to call Ict REST API. Check Report.ixi configuration, especially 'Ict REST API port' and 'Ict REST API password'.");
        }
        return null;
    }
}

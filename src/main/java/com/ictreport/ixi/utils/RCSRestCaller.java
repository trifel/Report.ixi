package com.ictreport.ixi.utils;

import com.ictreport.ixi.exchange.payloads.Payload;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class RCSRestCaller {

    private static final Logger log = LogManager.getLogger("ReportIxi/RCSRestCaller");

    public static String send(final String route, final Payload payload) {

        final String endpoint = String.format("http://%s:%d/%s", Constants.RCS_HOST, Constants.RCS_PORT, route);

        final RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Constants.REQUEST_TIMEOUT * 1000)
                .setConnectionRequestTimeout(Constants.REQUEST_TIMEOUT * 1000)
                .setSocketTimeout(Constants.REQUEST_TIMEOUT * 1000)
                .build();

        final CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        final HttpPost httppost = new HttpPost(endpoint);

        try {
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("json", Payload.serialize(payload)));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.error("Failed to set request params, error: UnsupportedEncodingException");
            return null;
        }

        // Execute and get the response.
        CloseableHttpResponse response;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Exception thrown when sending to RCS (" + endpoint + "), IOException");
            return null;
        }

        if (response != null) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                log.error(String.format(
                        "Expected status code 200 but received status code %d from (%s)",
                        statusCode,
                        endpoint)
                );
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream instream = entity.getContent()) {
                    final StringWriter writer = new StringWriter();
                    IOUtils.copy(instream, writer, "UTF-8");
                    final String json = writer.toString();
                    log.debug(String.format("Successfully received response from (%s).", endpoint));
                    return json;
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error(String.format("Failed to parse response from RCS (%s).", endpoint));
                }
            }
        }
        return null;
    }
}

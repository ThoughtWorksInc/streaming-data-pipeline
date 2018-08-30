package com.free2wheelers.properties_integration_test;

import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class ApplicationPropertiesIntegrationTest {
    @Test
    public void should_san_francisco_station_data_can_get_data_from_api() throws IOException {
        Properties props = new Properties();
        InputStream is = ClassLoader.getSystemResourceAsStream("application-sanfrancisco-station-information.properties");
        props.load(is);
        String requestUrl = props.getProperty("producer.url");
        String response = httpRequest(requestUrl, new HashMap());
        assertTrue(response.contains("San Francisco Bay Area, CA"));
    }

    private String httpRequest(String requestUrl, Map params) {
        StringBuilder buffer = new StringBuilder();
        try {
            URL url = new URL(requestUrl+"?"+urlencode(params));
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
            httpUrlConn.setDoInput(true);
            httpUrlConn.setRequestMethod("GET");
            httpUrlConn.connect();

            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            httpUrlConn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }


    private String urlencode(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue() + "", "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}

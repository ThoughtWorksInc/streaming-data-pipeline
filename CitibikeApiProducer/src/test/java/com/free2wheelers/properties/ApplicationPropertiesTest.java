package com.free2wheelers.properties;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ApplicationPropertiesTest {


    private Properties props = new Properties();

    @Test
    public void should_config_correctly_info_for_san_francisco_station_data() throws IOException {

        InputStream is = ClassLoader.getSystemResourceAsStream("application-sanfrancisco-station-information.properties");
        props.load(is);
        assertEquals("https://api.citybik.es/v2/networks/ford-gobike", props.getProperty("producer.url"));
        assertEquals("station_information", props.getProperty("producer.topic"));
        assertEquals("producer_sanfrancisco-station_information", props.getProperty("producer.producerId"));
        assertEquals("0 * * * * *", props.getProperty("producer.cron"));
    }

}

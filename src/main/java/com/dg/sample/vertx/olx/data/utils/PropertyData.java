package com.dg.sample.vertx.olx.data.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertyData {
  private static final Properties APPLICATION_PROPERTIES = initializeProperties();

  public static final String SERVER_PORT_PROPERTY_PROP = "server.port";
  public static final String OLX_ENDPOINT_URL_PROP = "olx.endpoint.url";
  public static final String OLX_ENDPOINT_PATH_PROP = "olx.endpoint.path";
  public static final String OLX_ENDPOINT_PORT_PROP = "olx.endpoint.port";
  public static final String KEYWORD_PLACEHOLDER_PROP = "\\$\\{keyword}";
  public static final String OLX_OFFER_DATA_QUEUE_PROP = "olx.data.fetcher.queue";

  private PropertyData(){}

  private static Properties initializeProperties(){
    Properties props = new Properties();
    InputStream resource = PropertyData.class.getResourceAsStream("/app.properties");
    try {
      props.load(resource);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return props;
  }

  public static Properties getApplicationProperties(){
    return APPLICATION_PROPERTIES;
  }
}

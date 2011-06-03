package org.gbif.ipt.mock;

import org.gbif.ipt.config.AppConfig;

import static org.mockito.Mockito.mock;

public class MockAppConfig {

  private static AppConfig appConfig = mock(AppConfig.class);

  public static AppConfig buildMock() {
    setUpMock();
    return appConfig;
  }

  /** Stubbing some methods and assigning some default configurations. */
  private static void setUpMock() {
    // configuring properties:
    appConfig.setProperty(AppConfig.BASEURL, "http://localhost:7001/ipt");
    appConfig.setProperty(AppConfig.IPT_LATITUDE, "10");
    appConfig.setProperty(AppConfig.IPT_LONGITUDE, "10");
    appConfig.setProperty(AppConfig.DEBUG, "true");

    // TODO Configure the other properties.

  }

}

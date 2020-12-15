package org.gbif.ipt.mock;

import com.sun.jersey.json.impl.provider.entity.JSONArrayProvider;
import org.gbif.ipt.config.AppConfig;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockAppConfig {

  private static AppConfig appConfig = mock(AppConfig.class);

  public static AppConfig buildMock() {
    setUpMock();
    return appConfig;
  }

  public static AppConfig rebuildMock() {
    appConfig = mock(AppConfig.class);
    return buildMock();
  }

  /**
   * Stubbing some methods and assigning some default configurations.
   */
  private static void setUpMock() {
    // configuring properties:
    appConfig.setProperty(AppConfig.BASEURL, "http://localhost:7001/ipt");
    appConfig.setProperty(AppConfig.IPT_LATITUDE, "10");
    appConfig.setProperty(AppConfig.IPT_LONGITUDE, "10");
    appConfig.setProperty(AppConfig.DEBUG, "true");


    when(appConfig.getMaxThreads()).thenReturn(3);

    // TODO Configure the other properties.

  }

}

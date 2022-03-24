/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.mock;

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
